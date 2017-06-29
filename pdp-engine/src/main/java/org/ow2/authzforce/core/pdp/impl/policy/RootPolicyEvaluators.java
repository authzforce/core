/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ImmutablePdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
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
import org.ow2.authzforce.core.pdp.impl.expression.DepthLimitingExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			this.expressionFactory = new DepthLimitingExpressionFactory(attributeFactory, functionRegistry, jaxbAttributeProviderConfs, maxVariableReferenceDepth, enableXPath,
					strictAttributeIssuerMatch, environmentProperties);

			final RefPolicyProviderModule.Factory<AbstractPolicyProvider> refPolicyProviderModFactory = jaxbRefPolicyProviderConf == null ? null : PdpExtensionLoader
					.getRefPolicyProviderModuleFactory(jaxbRefPolicyProviderConf);
			rootPolicyProviderMod = PdpExtensionLoader.getRootPolicyProviderModule(jaxbRootPolicyProviderConf, enableXPath, this.expressionFactory, combiningAlgRegistry, jaxbRefPolicyProviderConf,
					refPolicyProviderModFactory, maxPolicySetRefDepth, environmentProperties);
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
				return new ImmutablePdpDecisionResult(e.getStatus());
			}
			catch (final IllegalArgumentException e)
			{
				LOGGER.warn("One of the possible root policies (resolved by the root policy provider module {}) is invalid", rootPolicyProviderMod, e);
				// we consider that
				return new ImmutablePdpDecisionResult(new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR, Optional.ofNullable(e.getMessage())));
			}

			if (policy == null)
			{
				return PdpDecisionResults.SIMPLE_NOT_APPLICABLE;
			}

			final DecisionResult result = policy.evaluate(context, true);
			return new ImmutablePdpDecisionResult(result);
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
			return new ImmutablePdpDecisionResult(result);
		}

		@Override
		public StaticApplicablePolicyView getStaticApplicablePolicies()
		{
			return staticApplicablePolicies;
		}
	}
}
