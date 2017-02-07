/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResults;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticRootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.impl.PdpExtensionLoader;
import org.ow2.authzforce.core.pdp.impl.expression.ExpressionFactoryImpl;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * {@link RootPolicyEvaluator} implementations
 *
 * 
 * @version $Id: $
 */
public final class RootPolicyEvaluators
{
	/**
	 * Root Policy Provider base implementation.
	 */
	public static class Base implements RootPolicyEvaluator
	{
		private static final IllegalArgumentException ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException(
				"Invalid arguments to root policy Provider creation: missing one of these args: root policy Provider's XML/JAXB configuration (jaxbRootPolicyProviderConf), XACML Expression parser/factory (expressionFactory), combining algorithm registry (combiningAlgRegistry)");

		private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);

		private final RootPolicyProviderModule rootPolicyProviderMod;

		private transient final ExpressionFactory expressionFactory;

		private transient final boolean isRootPolicyProviderStatic;

		private transient volatile StaticView staticView = null;

		/**
		 * Creates a root policy Provider. If you want static resolution, i.e. use the same constant root policy (resolved at initialization time) for all evaluations, use the static root policy
		 * Provider provided by {@link #toStatic()} after calling this constructor; then {@link #close()} this instance.
		 * 
		 * @param attributeFactory
		 *            attribute value factory - mandatory
		 * @param functionRegistry
		 *            function registry - mandatory
		 * @param jaxbAttributeProviderConfs
		 *            XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static expression evaluation (out of context), in which case
		 *            AttributeSelectors/AttributeDesignators are not supported
		 * @param maxVariableReferenceDepth
		 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition ->... ('->' represents a VariableReference); strictly negative value means no limit
		 * @param enableXPath
		 *            allow XPath evaluation for AttributeSelectors, xpathExpressions, etc. (experimental, not for production, use with caution)
		 * 
		 * @param jaxbRootPolicyProviderConf
		 *            (mandatory) root policy Provider's XML/JAXB configuration
		 * @param combiningAlgRegistry
		 *            (mandatory) XACML policy/rule combining algorithm registry
		 * @param jaxbRefPolicyProviderConf
		 *            (optional) policy-by-reference Provider's XML/JAXB configuration, for resolving policies referred to by Policy(Set)IdReference in policies found by root policy Provider; null if
		 *            no refPolicyProvider specified
		 * @param maxPolicySetRefDepth
		 *            max allowed PolicySetIdReference chain: PolicySet1 (PolicySetIdRef1) -> PolicySet2 (PolicySetIdRef2) -> ...; a strictly negative value means no limit
		 * @param strictAttributeIssuerMatch
		 *            true iff strict Attribute Issuer matching is enabled, i.e. AttributeDesignators without Issuer only match request Attributes without Issuer (and same AttributeId, Category...).
		 *            This mode is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it performs better and is recommended when
		 *            all AttributeDesignators have an Issuer (best practice). Reminder: the XACML 3.0 specification for AttributeDesignator evaluation (5.29) says: "If the Issuer is not present in
		 *            the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone." if one of the mandatory
		 *            arguments is null
		 * @param environmentProperties
		 *            PDP configuration environment properties
		 * @throws IllegalArgumentException
		 *             if one of the mandatory arguments is null; or if any of attribute Provider modules created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in
		 *             conflict with another one already registered to provide the same or part of the same attributes.
		 * @throws IOException
		 *             if an {@link Exception} occured after instantiating the attribute Provider modules (from {@code jaxbAttributeProviderConfs}) but the modules could not be closed (with
		 *             {@link Closeable#close()} before throwing the exception)
		 */
		public Base(final DatatypeFactoryRegistry attributeFactory, final FunctionRegistry functionRegistry, final List<AbstractAttributeProvider> jaxbAttributeProviderConfs,
				final int maxVariableReferenceDepth, final boolean enableXPath, final CombiningAlgRegistry combiningAlgRegistry, final AbstractPolicyProvider jaxbRootPolicyProviderConf,
				final AbstractPolicyProvider jaxbRefPolicyProviderConf, final int maxPolicySetRefDepth, final boolean strictAttributeIssuerMatch, final EnvironmentProperties environmentProperties)
				throws IllegalArgumentException, IOException
		{
			if (jaxbRootPolicyProviderConf == null || combiningAlgRegistry == null)
			{
				throw ILLEGAL_ARGUMENT_EXCEPTION;
			}

			// Initialize ExpressionFactory
			this.expressionFactory = new ExpressionFactoryImpl(attributeFactory, functionRegistry, jaxbAttributeProviderConfs, maxVariableReferenceDepth, enableXPath, strictAttributeIssuerMatch,
					environmentProperties);

			final RootPolicyProviderModule.Factory<?> rootPolicyProviderModFactory = PdpExtensionLoader.getJaxbBoundExtension(RootPolicyProviderModule.Factory.class,
					jaxbRootPolicyProviderConf.getClass());

			final RefPolicyProviderModule.Factory<?> refPolicyProviderModFactory = jaxbRefPolicyProviderConf == null ? null : PdpExtensionLoader.getJaxbBoundExtension(
					RefPolicyProviderModule.Factory.class, jaxbRefPolicyProviderConf.getClass());
			rootPolicyProviderMod = ((RootPolicyProviderModule.Factory<AbstractPolicyProvider>) rootPolicyProviderModFactory).getInstance(jaxbRootPolicyProviderConf,
					JaxbXACMLUtils.getXACMLParserFactory(enableXPath), this.expressionFactory, combiningAlgRegistry, jaxbRefPolicyProviderConf,
					(RefPolicyProviderModule.Factory<AbstractPolicyProvider>) refPolicyProviderModFactory, maxPolicySetRefDepth, environmentProperties);
			isRootPolicyProviderStatic = rootPolicyProviderMod instanceof StaticRootPolicyProviderModule;

		}

		@Override
		public void close() throws IOException
		{
			this.expressionFactory.close();
			this.rootPolicyProviderMod.close();
		}

		@Override
		public PdpDecisionResult findAndEvaluate(final EvaluationContext context)
		{
			final PolicyEvaluator policy;
			try
			{
				policy = rootPolicyProviderMod.getPolicy(context);
			}
			catch (final IndeterminateEvaluationException e)
			{
				LOGGER.info("Root policy Provider module {} could not find an applicable root policy to evaluate", rootPolicyProviderMod, e);
				return new ImmutablePdpDecisionResult(e.getStatus(), context);
			}
			catch (final IllegalArgumentException e)
			{
				LOGGER.warn("One of the possible root policies (resolved by the root policy provider module {}) is invalid", rootPolicyProviderMod, e);
				// we consider that
				return new ImmutablePdpDecisionResult(new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR, e.getMessage()), context);
			}

			if (policy == null)
			{
				return PdpDecisionResults.SIMPLE_NOT_APPLICABLE;
			}

			final DecisionResult result = policy.evaluate(context, true);
			return new ImmutablePdpDecisionResult(result, context);
		}

		@Override
		public StaticApplicablePolicyView getStaticApplicablePolicies()
		{
			return staticView == null ? null : staticView.getStaticApplicablePolicies();
		}

		/**
		 * Gets the static version of this policy Provider, i.e. a policy Provider using the same constant root policy resolved by this Provider (once and for all) when calling this method. This root
		 * policy will be used for all evaluations. This is possible only for Providers independent from the evaluation context (static resolution).
		 * 
		 * @return static view of this policy Provider; or null if none could be created because the Provider depends on the evaluation context to find the root policy (no static resolution is
		 *         possible). If not null, this Provider's sub-module responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext)} is closed (calling
		 *         {@link RootPolicyProviderModule#close()} and therefore not useable anymore. The resulting static view must be used instead.
		 * @throws IOException
		 *             error closing the Provider's sub-module responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext)}
		 */
		public RootPolicyEvaluator toStatic() throws IOException
		{
			// If staticView not yet initialized and root policy provider module
			// is actually static (in which case staticView can be initialized)
			if (staticView == null && isRootPolicyProviderStatic)
			{
				staticView = new StaticView((StaticRootPolicyProviderModule) rootPolicyProviderMod, this.expressionFactory);
			}

			return staticView;
		}

	}

	private static final class ImmutablePdpDecisionResult implements PdpDecisionResult
	{

		private final DecisionType decision;

		private final Status status;

		private final ImmutablePepActions pepActions;

		/**
		 * Extended Indeterminate value, only in case {@link #getDecision()} returns {@value DecisionType#INDETERMINATE}, else it should be ignored, as defined in section 7.10 of XACML 3.0 core:
		 * <i>potential effect value which could have occurred if there would not have been an error causing the “Indeterminate”</i>. We use the following convention:
		 * <ul>
		 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
		 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
		 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
		 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
		 * </ul>
		 * 
		 */
		private final DecisionType extIndeterminate;

		private final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList;

		// null if not required
		private final Set<AttributeGUID> usedNamedAttributeIdList;

		// null if not required
		private final Set<AttributeSelectorId> usedExtraContentSelectorList;

		private ImmutablePdpDecisionResult(final DecisionType decision, final DecisionType extendedIndeterminate, final Status status, final ImmutablePepActions pepActions,
				final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList, final EvaluationContext evalCtx)
		{
			assert decision != null && extendedIndeterminate != null;
			this.decision = decision;
			this.status = status;
			this.pepActions = pepActions;
			this.extIndeterminate = extendedIndeterminate;
			this.applicablePolicyIdList = decision == DecisionType.NOT_APPLICABLE ? null : applicablePolicyIdList == null ? ImmutableList.<JAXBElement<IdReferenceType>> of() : applicablePolicyIdList;
			this.usedNamedAttributeIdList = evalCtx.getUsedNamedAttributes();
			this.usedExtraContentSelectorList = evalCtx.getUsedExtraAttributeContents();
		}

		private ImmutablePdpDecisionResult(final DecisionResult decisionResult, final EvaluationContext evalCtx)
		{
			this(decisionResult.getDecision(), decisionResult.getExtendedIndeterminate(), decisionResult.getStatus(), decisionResult.getPepActions(), decisionResult.getApplicablePolicies(), evalCtx);
		}

		private ImmutablePdpDecisionResult(final Status status, final EvaluationContext evalCtx)
		{
			this(DecisionType.INDETERMINATE, DecisionType.INDETERMINATE, status, null, null, evalCtx);
		}

		@Override
		public ImmutableList<JAXBElement<IdReferenceType>> getApplicablePolicies()
		{
			return this.applicablePolicyIdList;
		}

		@Override
		public DecisionType getDecision()
		{
			return this.decision;
		}

		@Override
		public DecisionType getExtendedIndeterminate()
		{
			return this.extIndeterminate;
		}

		@Override
		public ImmutablePepActions getPepActions()
		{
			return this.pepActions;
		}

		@Override
		public Status getStatus()
		{
			return this.status;
		}

		@Override
		public Set<AttributeSelectorId> getUsedExtraAttributeContents()
		{
			return this.usedExtraContentSelectorList;
		}

		@Override
		public Set<AttributeGUID> getUsedNamedAttributes()
		{
			return this.usedNamedAttributeIdList;
		}

		@Override
		public Result toXACMLResult(final List<Attributes> returnedAttributes)
		{
			final List<Obligation> obligationList;
			final List<Advice> adviceList;
			if (pepActions == null)
			{
				obligationList = null;
				adviceList = null;
			}
			else
			{
				obligationList = this.pepActions.getObligatory();
				adviceList = this.pepActions.getAdvisory();
			}

			return new Result(this.decision, this.status, obligationList == null || obligationList.isEmpty() ? null : new Obligations(obligationList),
					adviceList == null || adviceList.isEmpty() ? null : new AssociatedAdvice(adviceList), returnedAttributes, applicablePolicyIdList == null || applicablePolicyIdList.isEmpty() ? null
							: new PolicyIdentifierList(applicablePolicyIdList));
		}

	}

	/**
	 * 
	 * Static view of policy Provider. The root policy is resolved once and for all at initialization time, and is then used for all evaluation requests.
	 *
	 */
	static class StaticView implements RootPolicyEvaluator
	{
		private final StaticTopLevelPolicyElementEvaluator staticRootPolicyEvaluator;
		private final ExpressionFactory expressionFactory;
		private transient final StaticApplicablePolicyView staticApplicablePolicies;

		private StaticView(final StaticRootPolicyProviderModule staticProviderModule, final ExpressionFactory expressionFactoryForClosing) throws IOException
		{
			assert staticProviderModule != null && expressionFactoryForClosing != null;
			this.expressionFactory = expressionFactoryForClosing;
			this.staticRootPolicyEvaluator = staticProviderModule.getPolicy();
			this.staticApplicablePolicies = new StaticApplicablePolicyView(staticRootPolicyEvaluator.getPolicyElementType(), staticRootPolicyEvaluator.getPolicyId(),
					staticRootPolicyEvaluator.getExtraPolicyMetadata());

			staticProviderModule.close();
		}

		@Override
		public void close() throws IOException
		{
			this.expressionFactory.close();
		}

		@Override
		public PdpDecisionResult findAndEvaluate(final EvaluationContext context)
		{
			final DecisionResult result = staticRootPolicyEvaluator.evaluate(context);
			return new ImmutablePdpDecisionResult(result, context);
		}

		@Override
		public StaticApplicablePolicyView getStaticApplicablePolicies()
		{
			return staticApplicablePolicies;
		}
	}
}
