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

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.UpdatableCollections;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.combining.CombinerParameterEvaluator;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.VariableReference;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.PolicyRefsMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluator;
import org.ow2.authzforce.core.pdp.impl.PepActionExpression;
import org.ow2.authzforce.core.pdp.impl.PepActionExpressions;
import org.ow2.authzforce.core.pdp.impl.PepActionFactories;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluators;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.ow2.authzforce.xacml.identifiers.XacmlNodeName;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * This class consists exclusively of static methods that operate on or return {@link PolicyEvaluator}s
 *
 * 
 * @version $Id: $
 */
public final class PolicyEvaluators
{

	private static final IllegalArgumentException NULL_XACML_COMBINING_ALG_ARG_EXCEPTION = new IllegalArgumentException("Undefined policy/rule combining algorithm registry");
	private static final IllegalArgumentException NULL_EXPRESSION_FACTORY_EXCEPTION = new IllegalArgumentException("Undefined XACML Expression factory/parser");
	private static final IllegalArgumentException NULL_XACML_POLICY_ARG_EXCEPTION = new IllegalArgumentException("Undefined XACML <Policy>");
	private static final IllegalArgumentException NULL_XACML_POLICYSET_ARG_EXCEPTION = new IllegalArgumentException("Undefined XACML <PolicySet>");

	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyEvaluators.class);

	/**
	 * Policy(Set)-associated PEP action (obligation/advice) expressions parser used to initialize the evaluator's fields
	 * 
	 */
	private static final class PolicyPepActionExpressions implements PepActionExpressions
	{

		private final XPathCompiler xPathCompiler;
		private final ExpressionFactory expFactory;

		private final PepActionExpressions.EffectSpecific denyActionExpressions = new EffectSpecific(EffectType.DENY);
		private final PepActionExpressions.EffectSpecific permitActionExpressions = new EffectSpecific(EffectType.PERMIT);

		/**
		 * Creates instance
		 * 
		 * @param xPathCompiler
		 *            XPath compiler corresponding to enclosing policy(set) default XPath version
		 * @param expressionFactory
		 *            expression factory for parsing expressions
		 */
		private PolicyPepActionExpressions(final XPathCompiler xPathCompiler, final ExpressionFactory expressionFactory)
		{
			this.xPathCompiler = xPathCompiler;
			this.expFactory = expressionFactory;
		}

		@Override
		public void add(final ObligationExpression jaxbObligationExp) throws IllegalArgumentException
		{
			final PepActionExpression<Obligation> obligationExp = new PepActionExpression<>(PepActionFactories.OBLIGATION_FACTORY, jaxbObligationExp.getObligationId(),
					jaxbObligationExp.getFulfillOn(), jaxbObligationExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final PepActionExpressions.EffectSpecific effectSpecificActionExps = obligationExp.getAppliesTo() == EffectType.DENY ? denyActionExpressions : permitActionExpressions;
			effectSpecificActionExps.addObligationExpression(obligationExp);
		}

		@Override
		public void add(final AdviceExpression jaxbAdviceExp) throws IllegalArgumentException
		{
			final PepActionExpression<Advice> adviceExp = new PepActionExpression<>(PepActionFactories.ADVICE_FACTORY, jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(),
					jaxbAdviceExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final PepActionExpressions.EffectSpecific effectSpecificActionExps = adviceExp.getAppliesTo() == EffectType.DENY ? denyActionExpressions : permitActionExpressions;
			effectSpecificActionExps.addAdviceExpression(adviceExp);
		}

		@Override
		public List<PepActionExpression<Obligation>> getObligationExpressionList()
		{
			final List<PepActionExpression<Obligation>> resultList = new ArrayList<>(denyActionExpressions.getObligationExpressions());
			resultList.addAll(permitActionExpressions.getObligationExpressions());
			return resultList;
		}

		@Override
		public List<PepActionExpression<Advice>> getAdviceExpressionList()
		{
			final List<PepActionExpression<Advice>> resultList = new ArrayList<>(denyActionExpressions.getAdviceExpressions());
			resultList.addAll(permitActionExpressions.getAdviceExpressions());
			return resultList;
		}
	}

	private static final PepActionExpressions.Factory<PolicyPepActionExpressions> PEP_ACTION_EXPRESSIONS_FACTORY = new PepActionExpressions.Factory<PolicyPepActionExpressions>()
	{

		@Override
		public PolicyPepActionExpressions getInstance(final XPathCompiler xPathCompiler, final ExpressionFactory expressionFactory)
		{
			return new PolicyPepActionExpressions(xPathCompiler, expressionFactory);
		}

	};

	/**
	 * Factory for returning Deny/Permit policy decision based on combining algorithm evaluation result, evaluation context, initial PEP actions (filled from results of evaluation of child elements by
	 * combining algorithm) and applicable Policy identifiers
	 */
	private interface DPResultFactory
	{

		DecisionResult getInstance(ExtendedDecision combiningAlgResult, EvaluationContext evaluationContext, UpdatablePepActions basePepActions, ImmutableList<PrimaryPolicyMetadata> applicablePolicies);

	}

	private static final DPResultFactory DP_WITHOUT_EXTRA_PEP_ACTION_RESULT_FACTORY = new DPResultFactory()
	{

		@Override
		public DecisionResult getInstance(final ExtendedDecision combiningAlgResult, final EvaluationContext evaluationContext, final UpdatablePepActions basePepActions,
				final ImmutableList<PrimaryPolicyMetadata> applicablePolicies)
		{
			// No Policy-defined PEP actions to add/merge, we can create the
			// result right away
			return DecisionResults.getInstance(combiningAlgResult, ImmutablePepActions.getInstance(basePepActions), applicablePolicies);
		}

	};

	private static final class PepActionAppendingDPResultFactory implements DPResultFactory
	{
		private final String policyToString; // policy fully qualifying name for
												// logs: Policy(Set)[ID#vXXX]
		private final PepActionExpressions.EffectSpecific denyActionExpressions;
		private final PepActionExpressions.EffectSpecific permitActionExpressions;

		private PepActionAppendingDPResultFactory(final String policyId, final PolicyPepActionExpressions policyPepActionExpressions)
		{
			assert policyId != null && policyPepActionExpressions != null;

			this.policyToString = policyId;
			this.denyActionExpressions = policyPepActionExpressions.denyActionExpressions;
			this.permitActionExpressions = policyPepActionExpressions.permitActionExpressions;
		}

		@Override
		public DecisionResult getInstance(final ExtendedDecision combiningAlgResult, final EvaluationContext context, final UpdatablePepActions basePepActions,
				final ImmutableList<PrimaryPolicyMetadata> applicablePolicies)
		{
			final PepActionExpressions.EffectSpecific matchingActionExpressions;
			final DecisionType combiningAlgDecision = combiningAlgResult.getDecision();
			switch (combiningAlgDecision)
			{
				case DENY:
					matchingActionExpressions = this.denyActionExpressions;
					break;
				case PERMIT:
					matchingActionExpressions = this.permitActionExpressions;
					break;
				default:
					throw new IllegalArgumentException("Invalid decision type for policy obligations/advice: " + combiningAlgDecision + ". Expected: Permit/Deny");
			}

			/*
			 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute evaluates to "Indeterminate", then the whole rule,
			 * policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
			 */

			final ImmutablePepActions resultPepActions;
			try
			{
				resultPepActions = PepActionExpressions.Helper.evaluate(matchingActionExpressions, context, basePepActions);
			}
			catch (final IndeterminateEvaluationException e)
			{
				/*
				 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than error)
				 */
				LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", policyToString, e);

				return DecisionResults.newIndeterminate(combiningAlgDecision, e, applicablePolicies);
			}

			return DecisionResults.getInstance(combiningAlgResult, resultPepActions, applicablePolicies);
		}
	}

	/**
	 * Represents a set of CombinerParameters to a combining algorithm that may or may not be associated with a policy/rule
	 * 
	 * @param <T>
	 *            Type of combined element (Policy, Rule...) with which the CombinerParameters are associated
	 */
	public static final class BaseCombiningAlgParameter<T extends Decidable> implements CombiningAlgParameter<T>
	{

		// the element to be combined
		private final T element;

		// the parameters used with this element
		private final List<CombinerParameterEvaluator> parameters;

		/**
		 * Constructor that takes both the element to combine and its associated combiner parameters.
		 * 
		 * @param element
		 *            combined element; null if
		 * 
		 * @param jaxbCombinerParameters
		 *            a (possibly empty) non-null <code>List</code> of <code>CombinerParameter<code>s provided for general use
		 * @param xPathCompiler
		 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null if none specified
		 * @param expFactory
		 *            attribute value factory
		 * @throws IllegalArgumentException
		 *             if if one of the CombinerParameters is invalid
		 */
		private BaseCombiningAlgParameter(final T element, final List<CombinerParameter> jaxbCombinerParameters, final ExpressionFactory expFactory, final XPathCompiler xPathCompiler)
				throws IllegalArgumentException
		{
			this.element = element;
			if (jaxbCombinerParameters == null)
			{
				this.parameters = Collections.emptyList();
			}
			else
			{
				final List<CombinerParameterEvaluator> modifiableParamList = new ArrayList<>(jaxbCombinerParameters.size());
				int paramIndex = 0;
				for (final CombinerParameter jaxbCombinerParam : jaxbCombinerParameters)
				{
					try
					{
						final CombinerParameterEvaluator combinerParam = new CombinerParameterEvaluator(jaxbCombinerParam, expFactory, xPathCompiler);
						modifiableParamList.add(combinerParam);
					}
					catch (final IllegalArgumentException e)
					{
						throw new IllegalArgumentException("Error parsing CombinerParameters/CombinerParameter#" + paramIndex, e);
					}

					paramIndex++;
				}

				this.parameters = Collections.unmodifiableList(modifiableParamList);
			}
		}

		/**
		 * Returns the combined element. If null, it means, this CombinerElement (i.e. all its CombinerParameters) is not associated with a particular rule
		 * 
		 * @return the combined element
		 */
		@Override
		public T getCombinedElement()
		{
			return element;
		}

		/**
		 * Returns the <code>CombinerParameterEvaluator</code>s associated with this element.
		 * 
		 * @return a <code>List</code> of <code>CombinerParameterEvaluator</code>s
		 */
		@Override
		public List<CombinerParameterEvaluator> getParameters()
		{
			return parameters;
		}

	}

	private static final class BasePrimaryPolicyMetadata implements PrimaryPolicyMetadata
	{
		private final TopLevelPolicyElementType type;
		private final String id;
		private final PolicyVersion version;

		private transient volatile String toString = null;
		private transient volatile int hashCode = 0;

		private BasePrimaryPolicyMetadata(final TopLevelPolicyElementType type, final String id, final PolicyVersion version)
		{
			assert type != null && id != null && version != null;
			this.type = type;
			this.id = id;
			this.version = version;
		}

		@Override
		public TopLevelPolicyElementType getType()
		{
			return this.type;
		}

		@Override
		public String getId()
		{
			return this.id;
		}

		@Override
		public PolicyVersion getVersion()
		{
			return this.version;
		}

		@Override
		public String toString()
		{
			if (toString == null)
			{
				this.toString = type + "[" + id + "#v" + version + "]";
			}

			return toString;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				/*
				 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well in PolicyIdReferences. So we consider it is useless for identification in the XACML model.
				 */
				this.hashCode = Objects.hash(type, id, version);
			}

			return hashCode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (!(obj instanceof PrimaryPolicyMetadata))
			{
				return false;
			}

			final PrimaryPolicyMetadata other = (PrimaryPolicyMetadata) obj;
			return this.type.equals(other.getType()) && this.id.equals(other.getId()) && this.version.equals(other.getVersion());
		}

		@Override
		public Optional<PolicyIssuer> getIssuer()
		{
			// TODO: support PolicyIssuer. This field is relevant only to XACML Administrative Profile which is not supported here.
			return Optional.empty();
		}

		@Override
		public Optional<String> getDescription()
		{
			/*
			 * TODO: support Description. This field has no use in policy evaluation, therefore not a priority.
			 */
			return Optional.empty();
		}

	}

	private static final class BasePolicyRefsMetadata implements PolicyRefsMetadata
	{
		private final ImmutableSet<PrimaryPolicyMetadata> refPolicies;
		private final ImmutableList<String> longestPolicyRefChain;

		/**
		 * This constructor will make all fields immutable, so do you need to make args immutable before passing them to this.
		 * 
		 * @param refPolicies
		 *            policies referenced from the policy
		 * @param longestPolicyRefChain
		 *            longest chain of policy references (Policy(Set)IdReferences) originating from the policy
		 */
		private BasePolicyRefsMetadata(final Set<PrimaryPolicyMetadata> refPolicies, final List<String> longestPolicyRefChain)
		{
			assert refPolicies != null && longestPolicyRefChain != null;
			this.refPolicies = ImmutableSet.copyOf(refPolicies);
			this.longestPolicyRefChain = ImmutableList.copyOf(longestPolicyRefChain);
		}

		@Override
		public List<String> getLongestPolicyRefChain()
		{
			return longestPolicyRefChain;
		}

		@Override
		public Set<PrimaryPolicyMetadata> getRefPolicies()
		{
			return refPolicies;
		}

	}

	/**
	 * Generic Policy(Set) evaluator. Evaluates to a Decision.
	 * 
	 * @param <T>
	 *            type of combined child elements in evaluated Policy(Set)
	 * 
	 */
	private static abstract class BaseTopLevelPolicyElementEvaluator<T extends Decidable> implements TopLevelPolicyElementEvaluator
	{
		private static final IllegalArgumentException NULL_POLICY_METADATA_EXCEPTION = new IllegalArgumentException("Undefined Policy(Set) metadata (required)");
		private static final IllegalArgumentException NULL_ALG_EXCEPTION = new IllegalArgumentException("Undefined Policy(Set) combining algorithm ID (required)");

		private static final class EvalResults
		{
			private final String policyId;
			private DecisionResult resultWithTarget = null;
			private DecisionResult resultWithoutTarget = null;

			private EvalResults(final String policyId)
			{
				assert policyId != null;
				this.policyId = policyId;
			}

			private void setResult(final boolean skipTarget, final DecisionResult result)
			{
				assert result != null;
				if (skipTarget)
				{
					if (resultWithoutTarget != null)
					{
						throw new UnsupportedOperationException(policyId + ": evaluation result (skipTarget = true) already set in this context");
					}

					resultWithoutTarget = result;
				}
				else
				{
					if (resultWithoutTarget != null)
					{
						throw new UnsupportedOperationException(policyId + ": evaluation result (skipTarget = false) already set in this context");
					}

					resultWithTarget = result;
				}
			}
		}

		private IndeterminateEvaluationException enforceNoNullCauseForIndeterminate(final Optional<IndeterminateEvaluationException> causeForIndeterminate)
		{
			if (causeForIndeterminate.isPresent())
			{
				return causeForIndeterminate.get();
			}

			// not present
			LOGGER.error("{} evaluation failed for UNKNOWN reason. Make sure all AuthzForce extensions provide meaningful information when throwing instances of {}", this,
					IndeterminateEvaluationException.class);
			return new IndeterminateEvaluationException("Cause unknown/hidden", XacmlStatusCode.PROCESSING_ERROR.value());
		}

		// non-null
		private final PrimaryPolicyMetadata policyMetadata;

		// non-null
		private final BooleanEvaluator targetEvaluator;

		// non-null
		private final CombiningAlg.Evaluator combiningAlgEvaluator;

		// non-null
		private final DPResultFactory decisionResultFactory;

		// non-null
		private final Set<String> localVariableIds;

		private transient final String requestScopedEvalResultsCacheKey;

		/**
		 * Instantiates an evaluator
		 * 
		 * @param combinedElementClass
		 *            combined element class
		 * @param policyMetadata
		 *            policy metadata (type, ID, version...)
		 * @param policyTarget
		 *            policy(Set) Target
		 * @param combinedElements
		 *            child elements combined in the policy(set) by {@code combiningAlg}, in order of declaration
		 * @param combinerParameters
		 *            combining algorithm parameters, in order of declaration
		 * @param localVariableIds
		 *            IDs of variables defined locally (in policy {@code policyId})
		 * @param combiningAlgId
		 *            (policy/rule-)combining algorithm ID
		 * @param obligationExps
		 *            ObligationExpressions
		 * @param adviceExps
		 *            AdviceExpressions
		 * @param defaultXPathCompiler
		 *            Default XPath compiler corresponding to the Policy(Set) default XPath version
		 * @param expressionFactory
		 *            Expression factory/parser
		 * @param combiningAlgRegistry
		 *            rule/policy combining algorithm registry
		 * @throws IllegalArgumentException
		 *             if {@code policyMetadata == null || combiningAlgId  == null}
		 */
		protected BaseTopLevelPolicyElementEvaluator(final Class<T> combinedElementClass, final PrimaryPolicyMetadata policyMetadata, final Target policyTarget, final String combiningAlgId,
				final Iterable<T> combinedElements, final Iterable<CombiningAlgParameter<? extends T>> combinerParameters, final List<ObligationExpression> obligationExps,
				final List<AdviceExpression> adviceExps, final Set<String> localVariableIds, final XPathCompiler defaultXPathCompiler, final ExpressionFactory expressionFactory,
				final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
		{
			if (policyMetadata == null)
			{
				throw NULL_POLICY_METADATA_EXCEPTION;
			}

			if (combiningAlgId == null)
			{
				throw NULL_ALG_EXCEPTION;
			}

			this.policyMetadata = policyMetadata;

			this.targetEvaluator = TargetEvaluators.getInstance(policyTarget, defaultXPathCompiler, expressionFactory);

			final CombiningAlg<T> combiningAlg;
			try
			{
				combiningAlg = combiningAlgRegistry.getAlgorithm(combiningAlgId, combinedElementClass);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this + ": Unknown/unsupported " + (RuleEvaluator.class.isAssignableFrom(combinedElementClass) ? "rule" : "policy") + "-combining algorithm ID = '"
						+ combiningAlgId + "'", e);
			}

			this.combiningAlgEvaluator = combiningAlg.getInstance(combinerParameters, combinedElements);

			if ((obligationExps == null || obligationExps.isEmpty()) && (adviceExps == null || adviceExps.isEmpty()))
			{
				// no PEP obligation/advice
				this.decisionResultFactory = DP_WITHOUT_EXTRA_PEP_ACTION_RESULT_FACTORY;
			}
			else
			{
				final PolicyPepActionExpressions policyPepActionExpressions;
				try
				{
					policyPepActionExpressions = PepActionExpressions.Helper
							.parseActionExpressions(obligationExps, adviceExps, defaultXPathCompiler, expressionFactory, PEP_ACTION_EXPRESSIONS_FACTORY);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(this + ": Invalid AttributeAssignmentExpression(s)", e);
				}

				this.decisionResultFactory = new PepActionAppendingDPResultFactory(this.policyMetadata.toString(), policyPepActionExpressions);
			}

			this.localVariableIds = localVariableIds == null ? Collections.<String> emptySet() : localVariableIds;

			/*
			 * Define keys for caching the result of #evaluate() in the request context (see Object#toString())
			 */
			this.requestScopedEvalResultsCacheKey = this.getClass().getName() + '@' + Integer.toHexString(hashCode());

		}

		/**
		 * Policy(Set) evaluation which option to skip Target evaluation. The option is to be used by Only-one-applicable algorithm with value 'true', after calling
		 * {@link TopLevelPolicyElementEvaluator#isApplicableByTarget(EvaluationContext)} in particular.
		 * 
		 * @param context
		 *            evaluation context
		 * @param skipTarget
		 *            whether to evaluate the Target.
		 * @return decision result
		 */
		@Override
		public final DecisionResult evaluate(final EvaluationContext context, final boolean skipTarget)
		{
			/*
			 * check whether the result is already cached in the evaluation context
			 */
			final Object cachedValue = context.getOther(this.requestScopedEvalResultsCacheKey);
			final EvalResults cachedResults;
			if (cachedValue instanceof EvalResults)
			{
				cachedResults = (EvalResults) cachedValue;
			}
			else
			{
				cachedResults = null;
			}

			DecisionResult newResult = null;
			final UpdatablePepActions updatablePepActions;

			/*
			 * We add the current policy (this.refToSelf) to the applicablePolicyIdList only at the end when we know for sure the result is different from NotApplicable
			 */
			final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList;

			try
			{
				final ExtendedDecision algResult;
				if (skipTarget)
				{
					// check cached result
					if (cachedResults != null && cachedResults.resultWithoutTarget != null)
					{
						LOGGER.debug("{} -> {} (result from context cache with skipTarget=true)", this, cachedResults.resultWithoutTarget);
						return cachedResults.resultWithoutTarget;
					}

					// evaluate with combining algorithm
					updatablePepActions = new UpdatablePepActions();
					updatableApplicablePolicyIdList = context.isApplicablePolicyIdListRequested() ? UpdatableCollections.<PrimaryPolicyMetadata> newUpdatableList() : UpdatableCollections
							.<PrimaryPolicyMetadata> emptyList();
					algResult = combiningAlgEvaluator.evaluate(context, updatablePepActions, updatableApplicablePolicyIdList);
					LOGGER.debug("{}/Algorithm -> {}", this, algResult);
				}
				else
				{
					if (cachedResults != null && cachedResults.resultWithTarget != null)
					{
						LOGGER.debug("{} -> {} (result from context cache with skipTarget=false)", this, cachedResults.resultWithTarget);
						return cachedResults.resultWithTarget;
					}

					// evaluate target
					IndeterminateEvaluationException targetMatchIndeterminateException = null;
					try
					{
						if (!isApplicableByTarget(context))
						{
							LOGGER.debug("{}/Target -> No-match", this);
							LOGGER.debug("{} -> NotApplicable", this);
							newResult = DecisionResults.SIMPLE_NOT_APPLICABLE;
							return newResult;
						}

						// Target Match
						LOGGER.debug("{}/Target -> Match", this);
					}
					catch (final IndeterminateEvaluationException e)
					{
						targetMatchIndeterminateException = e;
						/*
						 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than error)
						 */
						LOGGER.info("{}/Target -> Indeterminate", this, e);
					}

					// evaluate with combining algorithm
					updatablePepActions = new UpdatablePepActions();
					updatableApplicablePolicyIdList = context.isApplicablePolicyIdListRequested() ? UpdatableCollections.<PrimaryPolicyMetadata> newUpdatableList() : UpdatableCollections
							.<PrimaryPolicyMetadata> emptyList();
					algResult = combiningAlgEvaluator.evaluate(context, updatablePepActions, updatableApplicablePolicyIdList);
					LOGGER.debug("{}/Algorithm -> {}", this, algResult);

					if (targetMatchIndeterminateException != null)
					{
						// Target is Indeterminate
						/*
						 * Implement Extended Indeterminate according to table 7 of section 7.14 (XACML 3.0 Core). If the combining alg value is Indeterminate, use its extended Indeterminate value as
						 * this evaluation result's extended Indeterminate value; else (Permit or Deny) as our extended indeterminate value (part between {} in XACML notation).
						 */
						final DecisionType algDecision = algResult.getDecision();

						switch (algDecision)
						{
							case NOT_APPLICABLE:
								newResult = DecisionResults.getNotApplicable(algResult.getStatus());
								break;
							case PERMIT:
							case DENY:
								/*
								 * Result != NotApplicable -> consider current policy as applicable
								 */
								updatableApplicablePolicyIdList.add(this.policyMetadata);
								newResult = DecisionResults.newIndeterminate(algDecision, targetMatchIndeterminateException, updatableApplicablePolicyIdList.copy());
								break;
							default: // INDETERMINATE
								/*
								 * Result != NotApplicable -> consider current policy as applicable
								 */
								updatableApplicablePolicyIdList.add(this.policyMetadata);
								newResult = DecisionResults.newIndeterminate(algResult.getExtendedIndeterminate(), targetMatchIndeterminateException, updatableApplicablePolicyIdList.copy());
								break;
						}

						/*
						 * newResult must be initialized and used as return variable at this point, in order to be used in finally{} block below
						 */
						return newResult;
					}
					// Else Target Match
				} // End of Target evaluation

				/*
				 * Target Match (or assumed Match if skipTarget=true) -> the policy decision is the one from the combining algorithm
				 */
				/*
				 * The spec is unclear about what is considered an "applicable" policy, therefore in what case should we add the policy to the PolicyIdentifierList in the final XACML Result. See the
				 * discussion here for more info: https://lists.oasis-open.org/archives/xacml-comment/201605/ msg00004.html. Here we choose to consider a policy applicable if and only if its
				 * evaluation does not return NotApplicable.
				 */
				final DecisionType algResultDecision = algResult.getDecision();
				final Status algResultStatus = algResult.getStatus();
				switch (algResultDecision)
				{
					case NOT_APPLICABLE:
						/*
						 * Final evaluation result is NotApplicable, so we don't add to applicable policy identifier list
						 */
						newResult = DecisionResults.getNotApplicable(algResultStatus);
						return newResult;

					case INDETERMINATE:
						/*
						 * Final result is the Indeterminate from algResult (no PEP actions), XACML §7.12, 7.13
						 * 
						 * Result != NotApplicable -> consider current policy as applicable
						 */
						updatableApplicablePolicyIdList.add(this.policyMetadata);

						newResult = DecisionResults.newIndeterminate(algResultDecision, enforceNoNullCauseForIndeterminate(algResult.getCauseForIndeterminate()),
								updatableApplicablePolicyIdList.copy());
						return newResult;

					default:
						// Permit/Deny decision
						/*
						 * Result != NotApplicable -> consider current policy as applicable
						 */
						updatableApplicablePolicyIdList.add(this.policyMetadata);
						newResult = this.decisionResultFactory.getInstance(algResult, context, updatablePepActions, updatableApplicablePolicyIdList.copy());
						return newResult;
				}
			}
			finally
			{
				// remove local variables from context
				for (final String varId : this.localVariableIds)
				{
					context.removeVariable(varId);
				}

				// update cache with new result
				if (newResult != null)
				{
					if (cachedResults == null)
					{
						final EvalResults newCachedResults = new EvalResults(this.policyMetadata.getId());
						newCachedResults.setResult(skipTarget, newResult);
						context.putOther(this.requestScopedEvalResultsCacheKey, newCachedResults);
					}
					else
					{
						cachedResults.setResult(skipTarget, newResult);
					}
				}
			}
		}

		@Override
		public final boolean isApplicableByTarget(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			return targetEvaluator.evaluate(context);
		}

		@Override
		public final DecisionResult evaluate(final EvaluationContext context)
		{
			return evaluate(context, false);
		}

		@Override
		public final TopLevelPolicyElementType getPolicyElementType()
		{
			return this.policyMetadata.getType();
		}

		@Override
		public final String getPolicyId()
		{
			return this.policyMetadata.getId();
		}

		@Override
		public final PolicyVersion getPolicyVersion()
		{
			return this.policyMetadata.getVersion();
		}

		@Override
		public final PrimaryPolicyMetadata getPrimaryPolicyMetadata()
		{
			return this.policyMetadata;
		}

		@Override
		public final String toString()
		{
			return this.policyMetadata.toString();
		}

		@Override
		public final int hashCode()
		{
			return this.policyMetadata.hashCode();
		}

		@Override
		public final boolean equals(final Object obj)
		{
			// Effective Java - Item 8
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof TopLevelPolicyElementEvaluator))
			{
				return false;
			}

			final TopLevelPolicyElementEvaluator other = (TopLevelPolicyElementEvaluator) obj;
			/*
			 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we consider it is not part of the Policy uniqueness
			 */
			return this.policyMetadata.equals(other.getPrimaryPolicyMetadata());
		}

	}

	private static final class StaticBaseTopLevelPolicyElementEvaluator<T extends Decidable> extends BaseTopLevelPolicyElementEvaluator<T> implements StaticTopLevelPolicyElementEvaluator
	{
		private transient final Optional<PolicyRefsMetadata> extraPolicyMetadata;

		private StaticBaseTopLevelPolicyElementEvaluator(final Class<T> combinedElementClass, final PrimaryPolicyMetadata policyMetadata, final Optional<PolicyRefsMetadata> extraPolicyMetadata,
				final Target policyTarget, final String combiningAlgId, final Iterable<T> combinedElements, final Iterable<CombiningAlgParameter<? extends T>> combinerParameters,
				final List<ObligationExpression> obligationExps, final List<AdviceExpression> adviceExps, final Set<String> localVariableIds, final XPathCompiler defaultXPathCompiler,
				final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
		{
			super(combinedElementClass, policyMetadata, policyTarget, combiningAlgId, combinedElements, combinerParameters, obligationExps, adviceExps, localVariableIds, defaultXPathCompiler,
					expressionFactory, combiningAlgRegistry);
			this.extraPolicyMetadata = extraPolicyMetadata;
		}

		@Override
		public Optional<PolicyRefsMetadata> getPolicyRefsMetadata()
		{
			return this.extraPolicyMetadata;
		}

	}

	/**
	 * This class is responsible for evaluating XACML Policy(Set)IdReferences.
	 * 
	 */
	private static abstract class PolicyRefEvaluator implements PolicyEvaluator
	{
		protected final TopLevelPolicyElementType referredPolicyType;
		protected final String refPolicyId;
		// and version constraints on this reference
		protected final Optional<VersionPatterns> versionConstraints;

		private transient final String toString;
		private transient final int hashCode;

		/**
		 * Get Policy reference description
		 * 
		 * @param refPolicyType
		 *            type of referenced policy (PolicySet for PolicySetIdReference or Policy for PolicyIdReference)
		 * @param policyRefId
		 *            referenced policy ID
		 * @param versionConstraints
		 *            referenced policy version constraints
		 * @return description
		 */
		private static String toString(final TopLevelPolicyElementType refPolicyType, final String policyRefId, final Optional<VersionPatterns> versionConstraints)
		{
			return refPolicyType + "IdReference[Id=" + policyRefId + ", " + versionConstraints + "]";
		}

		private PolicyRefEvaluator(final TopLevelPolicyElementType refPolicyType, final String policyId, final Optional<VersionPatterns> versionConstraints)
		{
			assert policyId != null && refPolicyType != null;
			this.refPolicyId = policyId;
			this.versionConstraints = versionConstraints;
			this.referredPolicyType = refPolicyType;
			this.toString = toString(referredPolicyType, policyId, versionConstraints);
			this.hashCode = Objects.hash(this.referredPolicyType, this.refPolicyId, this.versionConstraints);
		}

		@Override
		public final DecisionResult evaluate(final EvaluationContext context)
		{
			return evaluate(context, false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator#getPolicyElementType()
		 */
		@Override
		public final TopLevelPolicyElementType getPolicyElementType()
		{
			return this.referredPolicyType;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator#getPolicyId()
		 */
		@Override
		public final String getPolicyId()
		{
			return this.refPolicyId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public final String toString()
		{
			return toString;
		}

		@Override
		public final int hashCode()
		{
			return hashCode;
		}

		@Override
		public final boolean equals(final Object obj)
		{
			// Effective Java - Item 8
			if (this == obj)
			{
				return true;
			}

			// if not both PolicyEvaluators or not both PolicySetEvaluators
			if (!(obj instanceof PolicyRefEvaluator))
			{
				return false;
			}

			final PolicyRefEvaluator other = (PolicyRefEvaluator) obj;
			/*
			 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we consider it is not part of the Policy uniqueness
			 */
			return this.referredPolicyType.equals(other.referredPolicyType) && this.refPolicyId.equals(other.refPolicyId) && Objects.equals(this.versionConstraints, other.versionConstraints);
		}

	}

	/**
	 * 
	 * @param referredPolicy
	 * @return extra policy metadata
	 * @throws IndeterminateEvaluationException
	 *             if the extra policy metadata of {@code referredPolicy} could not be determined in {@code evalCtx} (with
	 *             {@link TopLevelPolicyElementEvaluator#getPolicyRefsMetadata(EvaluationContext)} )
	 */
	private static PolicyRefsMetadata newPolicyRefExtraMetadata(final TopLevelPolicyElementEvaluator referredPolicy, final EvaluationContext evalCtx) throws IndeterminateEvaluationException
	{
		assert referredPolicy != null;

		final PrimaryPolicyMetadata referredPolicyMetadata = referredPolicy.getPrimaryPolicyMetadata();
		final Optional<PolicyRefsMetadata> referredPolicyRefsMetadata = referredPolicy.getPolicyRefsMetadata(evalCtx);
		final Set<PrimaryPolicyMetadata> newRefPolicies;
		final List<String> newLongestPolicyRefChain;
		if (referredPolicyRefsMetadata.isPresent())
		{
			final Set<PrimaryPolicyMetadata> childRefPolicies = referredPolicyRefsMetadata.get().getRefPolicies();
			// LinkedHashSet to preserve order
			newRefPolicies = new LinkedHashSet<>(childRefPolicies.size() + 1);
			newRefPolicies.addAll(childRefPolicies);
			newRefPolicies.add(referredPolicyMetadata);

			final List<String> referredPolicyLongestRefChain = referredPolicyRefsMetadata.get().getLongestPolicyRefChain();
			newLongestPolicyRefChain = new ArrayList<>(referredPolicyLongestRefChain.size() + 1);
			newLongestPolicyRefChain.add(referredPolicy.getPolicyId());
			newLongestPolicyRefChain.addAll(referredPolicyLongestRefChain);
		}
		else
		{
			newRefPolicies = Sets.newHashSet(referredPolicyMetadata);
			newLongestPolicyRefChain = Arrays.asList(referredPolicy.getPolicyId());
		}

		return new BasePolicyRefsMetadata(newRefPolicies, newLongestPolicyRefChain);
	}

	private static final class StaticPolicySetChildRefsMetadataProvider
	{
		// LinkedHashSet to preserve order
		private final Set<PrimaryPolicyMetadata> refPolicies = new LinkedHashSet<>();
		private final List<String> longestPolicyRefChain = new ArrayList<>();

		private StaticPolicySetChildRefsMetadataProvider(final PrimaryPolicyMetadata primaryPolicyMetadata)
		{
			assert primaryPolicyMetadata != null;
		}

		private Optional<PolicyRefsMetadata> getMetadata()
		{
			return refPolicies.isEmpty() ? Optional.empty() : Optional.of(new BasePolicyRefsMetadata(refPolicies, longestPolicyRefChain));
		}

		private void updateMetadata(final PolicyRefsMetadata childPolicyRefsMetadata)
		{
			assert childPolicyRefsMetadata != null;

			// Modify refPolicies
			refPolicies.addAll(childPolicyRefsMetadata.getRefPolicies());

			/*
			 * update longest policy ref chain depending on the length of the longest in this child policy element
			 */
			final List<String> childLongestPolicyRefChain = childPolicyRefsMetadata.getLongestPolicyRefChain();
			if (childLongestPolicyRefChain.size() > longestPolicyRefChain.size())
			{
				longestPolicyRefChain.clear();
				longestPolicyRefChain.addAll(childLongestPolicyRefChain);
			}
		}
	}

	private static final class DynamicPolicySetChildRefsMetadataProvider
	{
		private static final class GetMetadataResult
		{
			private final Optional<PolicyRefsMetadata> extraMetadata;
			private final IndeterminateEvaluationException exception;

			private GetMetadataResult(final Optional<PolicyRefsMetadata> metadata)
			{
				assert metadata != null;
				this.exception = null;
				this.extraMetadata = metadata;
			}

			private GetMetadataResult(final IndeterminateEvaluationException exception)
			{
				assert exception != null;
				this.exception = exception;
				this.extraMetadata = null;
			}
		}

		private final List<PolicyEvaluator> childPolicySetElementsOrRefs = new ArrayList<>();

		private transient final String requestScopedCacheKey;

		private DynamicPolicySetChildRefsMetadataProvider()
		{
			/*
			 * Define a key for caching the result of #getMetadata() in the request context (see Object#toString())
			 */
			this.requestScopedCacheKey = this.getClass().getName() + '@' + Integer.toHexString(hashCode());
		}

		private void addChildPolicySetElementOrRef(final PolicyEvaluator childElement)
		{
			childPolicySetElementsOrRefs.add(childElement);
		}

		private Optional<PolicyRefsMetadata> getMetadata(final EvaluationContext evalCtx) throws IndeterminateEvaluationException
		{
			/*
			 * check whether the result is already cached in the evaluation context
			 */
			final Object cachedValue = evalCtx.getOther(requestScopedCacheKey);
			if (cachedValue instanceof GetMetadataResult)
			{
				final GetMetadataResult result = (GetMetadataResult) cachedValue;
				if (result.exception == null)
				{
					return result.extraMetadata;
				}

				throw result.exception;
			}

			/*
			 * cachedValue == null, i.e. result not cached yet; or cachedValue of the wrong type (unexpected), so we just overwrite with proper type
			 */
			final Set<PrimaryPolicyMetadata> refPolicies = new LinkedHashSet<>();
			final List<String> longestPolicyRefChain = new ArrayList<>();
			for (final PolicyEvaluator policyRef : childPolicySetElementsOrRefs)
			{
				final Optional<PolicyRefsMetadata> extraMetadata = policyRef.getPolicyRefsMetadata(evalCtx);
				if (extraMetadata.isPresent())
				{
					refPolicies.addAll(extraMetadata.get().getRefPolicies());
					final List<String> policyRefLongestPolicyRefChain = extraMetadata.get().getLongestPolicyRefChain();
					if (policyRefLongestPolicyRefChain.size() > longestPolicyRefChain.size())
					{
						longestPolicyRefChain.clear();
						longestPolicyRefChain.addAll(policyRefLongestPolicyRefChain);
					}
				}
			}

			final Optional<PolicyRefsMetadata> extraMetadata = refPolicies.isEmpty() ? Optional.empty() : Optional.of(new BasePolicyRefsMetadata(refPolicies, longestPolicyRefChain));
			final GetMetadataResult newCachedValue = new GetMetadataResult(extraMetadata);
			evalCtx.putOther(requestScopedCacheKey, newCachedValue);
			return extraMetadata;
		}
	}

	private static final class DynamicPolicySetEvaluator extends BaseTopLevelPolicyElementEvaluator<PolicyEvaluator>
	{
		private transient final DynamicPolicySetChildRefsMetadataProvider extraPolicyMetadataProvider;

		private DynamicPolicySetEvaluator(final PrimaryPolicyMetadata policyMetadata, final DynamicPolicySetChildRefsMetadataProvider extraPolicyMetadataProvider, final Target policyTarget,
				final String combiningAlgId, final Iterable<PolicyEvaluator> combinedElements, final Iterable<CombiningAlgParameter<? extends PolicyEvaluator>> combinerParameters,
				final List<ObligationExpression> obligationExps, final List<AdviceExpression> adviceExps, final Set<String> localVariableIds, final XPathCompiler defaultXPathCompiler,
				final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
		{
			super(PolicyEvaluator.class, policyMetadata, policyTarget, combiningAlgId, combinedElements, combinerParameters, obligationExps, adviceExps, localVariableIds, defaultXPathCompiler,
					expressionFactory, combiningAlgRegistry);
			this.extraPolicyMetadataProvider = extraPolicyMetadataProvider;
		}

		@Override
		public Optional<PolicyRefsMetadata> getPolicyRefsMetadata(final EvaluationContext evaluationCtx) throws IndeterminateEvaluationException
		{
			return this.extraPolicyMetadataProvider.getMetadata(evaluationCtx);
		}

	}

	private static final class StaticPolicyRefEvaluator extends PolicyRefEvaluator implements StaticPolicyEvaluator
	{
		/*
		 * statically defined policy referenced by this policy reference evaluator
		 */
		private final StaticTopLevelPolicyElementEvaluator referredPolicy;
		private transient final Optional<PolicyRefsMetadata> extraMetadata;

		private static TopLevelPolicyElementType validate(final TopLevelPolicyElementEvaluator referredPolicy)
		{
			return referredPolicy.getPolicyElementType();
		}

		private StaticPolicyRefEvaluator(final StaticTopLevelPolicyElementEvaluator referredPolicy, final Optional<VersionPatterns> refVersionConstraints)
		{
			super(validate(referredPolicy), referredPolicy.getPolicyId(), refVersionConstraints);
			this.referredPolicy = referredPolicy;
			try
			{
				this.extraMetadata = Optional.of(newPolicyRefExtraMetadata(referredPolicy, null));
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new RuntimeException(this + ": unexpected error: could not get extra metadata of statically defined policy: " + referredPolicy, e);
			}
		}

		@Override
		public DecisionResult evaluate(final EvaluationContext context, final boolean skipTarget)
		{
			return referredPolicy.evaluate(context, skipTarget);
		}

		@Override
		public boolean isApplicableByTarget(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			try
			{
				return referredPolicy.isApplicableByTarget(context);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Error checking whether Policy(Set) referenced by " + this + " is applicable to the request context", e.getStatusCode(), e);
			}
		}

		@Override
		public PolicyVersion getPolicyVersion()
		{
			return this.referredPolicy.getPolicyVersion();
		}

		@Override
		public Optional<PolicyRefsMetadata> getPolicyRefsMetadata()
		{
			return this.extraMetadata;
		}

	}

	/**
	 * Dynamic Policy/PolicySet reference evaluator
	 *
	 */
	private static abstract class DynamicTopLevelPolicyElementRefEvaluator extends PolicyRefEvaluator
	{

		protected static final class RefResolvedResult
		{

			private final TopLevelPolicyElementEvaluator resolvedPolicy;
			private final Optional<PolicyRefsMetadata> extraMetadata;
			private final IndeterminateEvaluationException exception;

			private RefResolvedResult(final TopLevelPolicyElementEvaluator policy, final EvaluationContext evalCtx) throws IndeterminateEvaluationException
			{
				assert policy != null && evalCtx != null;
				this.exception = null;
				this.resolvedPolicy = policy;
				this.extraMetadata = Optional.of(newPolicyRefExtraMetadata(policy, evalCtx));
			}

			private RefResolvedResult(final IndeterminateEvaluationException exception)
			{
				assert exception != null;
				this.exception = exception;
				this.resolvedPolicy = null;
				this.extraMetadata = null;
			}
		}

		// this policyProvider to use in finding the referenced policy
		private final RefPolicyProvider refPolicyProvider;

		private final String requestScopedCacheKey;

		private DynamicTopLevelPolicyElementRefEvaluator(final TopLevelPolicyElementType policyType, final String policyId, final Optional<VersionPatterns> versionConstraints,
				final RefPolicyProvider refPolicyProvider)
		{
			super(policyType, policyId, versionConstraints);
			assert refPolicyProvider != null;
			this.refPolicyProvider = refPolicyProvider;
			/*
			 * define a key for caching the resolved policy in the request context (see Object#toString())
			 */
			this.requestScopedCacheKey = this.getClass().getName() + '@' + Integer.toHexString(hashCode());
		}

		protected final void checkJoinedPolicySetRefChain(final Deque<String> chain1, final List<String> chain2) throws IllegalArgumentException
		{
			refPolicyProvider.joinPolicyRefChains(chain1, chain2);
		}

		protected final TopLevelPolicyElementEvaluator resolvePolicy(final Deque<String> policySetRefChainWithResolvedPolicyIfPolicySet, final EvaluationContext evalCtx)
				throws IllegalArgumentException, IndeterminateEvaluationException
		{
			return refPolicyProvider.get(this.referredPolicyType, this.refPolicyId, this.versionConstraints, policySetRefChainWithResolvedPolicyIfPolicySet, evalCtx);
		}

		protected abstract void checkPolicyRefChain(TopLevelPolicyElementEvaluator nonNullRefResultPolicy, final EvaluationContext evalCtx) throws IllegalArgumentException,
				IndeterminateEvaluationException;

		protected abstract TopLevelPolicyElementEvaluator resolvePolicyWithRefDepthCheck(final EvaluationContext evalCtx) throws IllegalArgumentException, IndeterminateEvaluationException;

		/**
		 * Resolves this to the actual Policy
		 * 
		 * @throws IllegalArgumentException
		 *             Error parsing the policy referenced by this. The referenced policy may be parsed on the fly, when calling this method.
		 * @throws IndeterminateEvaluationException
		 *             if error determining the policy referenced by this, e.g. if more than one policy is found
		 */
		private RefResolvedResult resolve(final EvaluationContext evalCtx) throws IndeterminateEvaluationException, IllegalArgumentException
		{
			// check whether the policy was already resolved in the same context
			final Object cachedValue = evalCtx.getOther(requestScopedCacheKey);
			if (cachedValue instanceof RefResolvedResult)
			{
				final RefResolvedResult result = (RefResolvedResult) cachedValue;
				if (result.exception == null)
				{
					checkPolicyRefChain(result.resolvedPolicy, evalCtx);
					return result;
				}

				throw result.exception;
			}

			/*
			 * cachedValue == null, i.e. ref resolution result not cached yet; or cachedValue of the wrong type (unexpected), so we just overwrite with proper type
			 */
			try
			{
				final TopLevelPolicyElementEvaluator policy = resolvePolicyWithRefDepthCheck(evalCtx);
				final RefResolvedResult newCacheValue = new RefResolvedResult(policy, evalCtx);
				evalCtx.putOther(requestScopedCacheKey, newCacheValue);
				return newCacheValue;
			}
			catch (final IllegalArgumentException e)
			{
				final IndeterminateEvaluationException resolutionException = new IndeterminateEvaluationException("Error resolving " + this + " to the policy to evaluate in the request context",
						XacmlStatusCode.PROCESSING_ERROR.value(), e);
				final RefResolvedResult newCacheValue = new RefResolvedResult(resolutionException);
				evalCtx.putOther(requestScopedCacheKey, newCacheValue);
				throw resolutionException;
			}
			catch (final IndeterminateEvaluationException e)
			{
				final RefResolvedResult newCacheValue = new RefResolvedResult(e);
				evalCtx.putOther(requestScopedCacheKey, newCacheValue);
				throw e;
			}
		}

		@Override
		public final DecisionResult evaluate(final EvaluationContext context, final boolean skipTarget)
		{
			// we must have found a policy
			final RefResolvedResult refResolvedResult;
			try
			{
				refResolvedResult = resolve(context);
			}
			catch (final IndeterminateEvaluationException e)
			{
				LOGGER.info("", e);
				/*
				 * Dynamic policy ref could not be resolved to an actual policy (-> no applicable policy found)
				 */
				return DecisionResults.newIndeterminate(DecisionType.INDETERMINATE, e, null);
			}

			return refResolvedResult.resolvedPolicy.evaluate(context, skipTarget);
		}

		@Override
		public final boolean isApplicableByTarget(final EvaluationContext evalCtx) throws IndeterminateEvaluationException
		{
			final RefResolvedResult refResolvedResult = resolve(evalCtx);
			return refResolvedResult.resolvedPolicy.isApplicableByTarget(evalCtx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator#getPolicyVersion(org.ow2.authzforce.core.pdp.api.EvaluationContext)
		 */
		@Override
		public PolicyVersion getPolicyVersion(final EvaluationContext evalCtx) throws IndeterminateEvaluationException
		{
			final RefResolvedResult refResolvedResult = resolve(evalCtx);
			return refResolvedResult.resolvedPolicy.getPolicyVersion();
		}

		@Override
		public final Optional<PolicyRefsMetadata> getPolicyRefsMetadata(final EvaluationContext evalCtx) throws IndeterminateEvaluationException
		{
			final RefResolvedResult refResolvedResult = resolve(evalCtx);
			return refResolvedResult.extraMetadata;
		}

	}

	/**
	 * Evaluator of PolicyIdReference with context-dependent resolution
	 */
	private static final class DynamicPolicyRefEvaluator extends DynamicTopLevelPolicyElementRefEvaluator
	{
		private DynamicPolicyRefEvaluator(final String policyId, final Optional<VersionPatterns> versionConstraints, final RefPolicyProvider refPolicyProvider)
		{
			super(TopLevelPolicyElementType.POLICY, policyId, versionConstraints, refPolicyProvider);
		}

		@Override
		protected void checkPolicyRefChain(final TopLevelPolicyElementEvaluator nonNullRefResultPolicy, final EvaluationContext evalCtx)
		{
			// nothing to do for XACML Policy (no nested policy ref)
		}

		@Override
		protected TopLevelPolicyElementEvaluator resolvePolicyWithRefDepthCheck(final EvaluationContext evalCtx) throws IllegalArgumentException, IndeterminateEvaluationException
		{
			// no policy ref depth check to do for XACML Policy (no nested policy ref)
			return resolvePolicy(null, evalCtx);
		}
	}

	private static final class DynamicPolicySetRefEvaluator extends DynamicTopLevelPolicyElementRefEvaluator
	{
		/*
		 * Chain of PolicySet Reference leading from root PolicySet down to this reference (included) (Do not use a Queue as it is FIFO, and we need LIFO and iteration in order of insertion, so
		 * different from Collections.asLifoQueue(Deque) as well.)
		 */
		private final Deque<String> policySetRefChainToThisRefTarget;

		private DynamicPolicySetRefEvaluator(final String policyId, final Optional<VersionPatterns> versionConstraints, final RefPolicyProvider refPolicyProvider,
				final Deque<String> policySetRefChainWithPolicyIdArgIfPolicySet) throws IllegalArgumentException
		{
			super(TopLevelPolicyElementType.POLICY_SET, policyId, versionConstraints, refPolicyProvider);
			assert policySetRefChainWithPolicyIdArgIfPolicySet != null && !policySetRefChainWithPolicyIdArgIfPolicySet.isEmpty();
			this.policySetRefChainToThisRefTarget = policySetRefChainWithPolicyIdArgIfPolicySet;
		}

		@Override
		protected void checkPolicyRefChain(final TopLevelPolicyElementEvaluator nonNullRefResultPolicy, final EvaluationContext evalCtx) throws IllegalArgumentException,
				IndeterminateEvaluationException
		{
			assert nonNullRefResultPolicy != null;
			/*
			 * Check PolicySet reference depth resulting from resolving this new PolicySet ref
			 */
			final Optional<PolicyRefsMetadata> optionalRefsMetadata = nonNullRefResultPolicy.getPolicyRefsMetadata(evalCtx);
			if (optionalRefsMetadata.isPresent())
			{
				checkJoinedPolicySetRefChain(policySetRefChainToThisRefTarget, optionalRefsMetadata.get().getLongestPolicyRefChain());
			}
		}

		@Override
		protected TopLevelPolicyElementEvaluator resolvePolicyWithRefDepthCheck(final EvaluationContext evalCtx) throws IllegalArgumentException, IndeterminateEvaluationException
		{
			return resolvePolicy(policySetRefChainToThisRefTarget, evalCtx);
		}

	}

	/**
	 * Creates Policy handler from XACML Policy element
	 *
	 * @param policyElement
	 *            Policy (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicyDefaults/XPathVersion; null if this Policy has no parent Policy (root), or none defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML Policy (XML) document, to be used for namespace-aware XPath evaluation; null or empty iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser; may be null iff {@code policyElement} does not contain any XACML {@link ExpressionType}
	 * @param combiningAlgRegistry
	 *            rule/policy combining algorithm registry
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument is invalid
	 */
	public static StaticTopLevelPolicyElementEvaluator getInstance(final Policy policyElement, final XPathCompiler parentDefaultXPathCompiler, final Map<String, String> namespacePrefixesByURI,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		if (policyElement == null)
		{
			throw NULL_XACML_POLICY_ARG_EXCEPTION;
		}

		if (expressionFactory == null)
		{
			throw NULL_EXPRESSION_FACTORY_EXCEPTION;
		}

		if (combiningAlgRegistry == null)
		{
			throw NULL_XACML_COMBINING_ALG_ARG_EXCEPTION;
		}

		final String policyId = policyElement.getPolicyId();
		final PolicyVersion policyVersion = new PolicyVersion(policyElement.getVersion());
		final String policyFriendlyId = "Policy[" + policyId + "#v" + policyVersion + "]";
		final DefaultsType policyDefaults = policyElement.getPolicyDefaults();

		/*
		 * Inherited PolicyDefaults is this.policyDefaults if not null, the parentPolicyDefaults otherwise
		 */
		final XPathCompiler defaultXPathCompiler;
		if (policyDefaults == null)
		{
			defaultXPathCompiler = parentDefaultXPathCompiler;
		}
		else
		{
			try
			{
				defaultXPathCompiler = XmlUtils.newXPathCompiler(policyDefaults.getXPathVersion(), namespacePrefixesByURI);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(policyFriendlyId + ": Invalid PolicyDefaults/XPathVersion or XML namespace prefix/URI undefined", e);
			}

		}

		/*
		 * Elements defined in xs:choice of XACML schema type PolicyType: Rules/(Rule)CombinerParameters/VariableDefinitions
		 */
		final List<Serializable> policyChoiceElements = policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions();
		/*
		 * There are at most as many combining alg parameters as policyChoiceElements.size().
		 */
		final List<CombiningAlgParameter<? extends RuleEvaluator>> combiningAlgParameters = new ArrayList<>(policyChoiceElements.size());

		/*
		 * Keep a copy of locally-defined variable IDs defined in this policy, to remove them from the global manager at the end of parsing this policy. They should not be visible outside the scope of
		 * this policy. There are at most as many VariableDefinitions as policyChoiceElements.size().
		 */
		final Set<String> localVariableIds = HashCollections.newUpdatableSet(policyChoiceElements.size());
		/*
		 * We keep a record of the size of the longest chain of VariableReference in this policy, and update it when a VariableDefinition occurs
		 */
		int sizeOfPolicyLongestVarRefChain = 0;
		/*
		 * Map to get rules by their ID so that we can resolve rules associated with RuleCombinerParameters, and detect duplicate RuleId. We want to preserve insertion order, to get map.values() in
		 * order of declaration, so that ordered-* algorithms have rules in order. There are at most as many Rules as policyChoiceElements.size().
		 */
		final Map<String, RuleEvaluator> ruleEvaluatorsByRuleIdInOrderOfDeclaration = new LinkedHashMap<>(policyChoiceElements.size());
		int childIndex = 0;
		for (final Serializable policyChildElt : policyChoiceElements)
		{
			if (policyChildElt instanceof RuleCombinerParameters)
			{
				final String combinedRuleId = ((RuleCombinerParameters) policyChildElt).getRuleIdRef();
				final RuleEvaluator ruleEvaluator = ruleEvaluatorsByRuleIdInOrderOfDeclaration.get(combinedRuleId);
				if (ruleEvaluator == null)
				{
					throw new IllegalArgumentException(policyFriendlyId + ":  invalid RuleCombinerParameters: referencing undefined child Rule #" + combinedRuleId
							+ " (no such rule defined before this element)");
				}

				final BaseCombiningAlgParameter<RuleEvaluator> combiningAlgParameter;
				try
				{
					combiningAlgParameter = new BaseCombiningAlgParameter<>(ruleEvaluator, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (RuleCombinerParameters)", e);
				}

				combiningAlgParameters.add(combiningAlgParameter);
			}
			else if (policyChildElt instanceof CombinerParametersType)
			{
				/*
				 * CombinerParameters that is not RuleCombinerParameters already tested before
				 */
				final BaseCombiningAlgParameter<RuleEvaluator> combiningAlgParameter;
				try
				{
					combiningAlgParameter = new BaseCombiningAlgParameter<>(null, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (CombinerParameters)", e);
				}

				combiningAlgParameters.add(combiningAlgParameter);
			}
			else if (policyChildElt instanceof VariableDefinition)
			{
				final VariableDefinition varDef = (VariableDefinition) policyChildElt;
				final Deque<String> varDefLongestVarRefChain = new ArrayDeque<>();
				final VariableReference<?> var;
				try
				{
					var = expressionFactory.addVariable(varDef, defaultXPathCompiler, varDefLongestVarRefChain);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (VariableDefinition)", e);
				}

				if (var != null)
				{
					/*
					 * Conflicts can occur between variables defined in this policy but also with others already in a wider scope, i.e. defined in parent/ancestor policy
					 */
					throw new IllegalArgumentException(policyFriendlyId + ": Duplicable VariableDefinition for VariableId = " + var.getVariableId());
				}

				localVariableIds.add(varDef.getVariableId());
				/*
				 * check whether the longest VariableReference chain in the VariableDefinition is longer than what we've got so far
				 */
				final int sizeOfVarDefLongestVarRefChain = varDefLongestVarRefChain.size();
				if (sizeOfVarDefLongestVarRefChain > sizeOfPolicyLongestVarRefChain)
				{
					sizeOfPolicyLongestVarRefChain = sizeOfVarDefLongestVarRefChain;
				}
			}
			else if (policyChildElt instanceof Rule)
			{
				final RuleEvaluator ruleEvaluator;
				try
				{
					ruleEvaluator = new RuleEvaluator((Rule) policyChildElt, defaultXPathCompiler, expressionFactory);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": Error parsing child #" + childIndex + " (Rule)", e);
				}

				final RuleEvaluator conflictingRuleEvaluator = ruleEvaluatorsByRuleIdInOrderOfDeclaration.putIfAbsent(ruleEvaluator.getRuleId(), ruleEvaluator);
				if (conflictingRuleEvaluator != null)
				{
					/*
					 * Conflict: 2 Rule elements with same RuleId -> violates uniqueness of RuleId within a Policy (XACML spec)
					 */
					throw new IllegalArgumentException(policyFriendlyId + ": Duplicate Rule with RuleId = " + conflictingRuleEvaluator.getRuleId());
				}
			}

			childIndex++;
		}

		final PrimaryPolicyMetadata primaryPolicyMetadata = new BasePrimaryPolicyMetadata(TopLevelPolicyElementType.POLICY, policyId, policyVersion);
		final ObligationExpressions obligationExps = policyElement.getObligationExpressions();
		final AdviceExpressions adviceExps = policyElement.getAdviceExpressions();
		final StaticTopLevelPolicyElementEvaluator policyEvaluator = new StaticBaseTopLevelPolicyElementEvaluator<>(RuleEvaluator.class, primaryPolicyMetadata, Optional.empty(),
				policyElement.getTarget(), policyElement.getRuleCombiningAlgId(), ruleEvaluatorsByRuleIdInOrderOfDeclaration.values(), combiningAlgParameters, obligationExps == null ? null
						: obligationExps.getObligationExpressions(), adviceExps == null ? null : adviceExps.getAdviceExpressions(), Collections.<String> unmodifiableSet(localVariableIds),
				defaultXPathCompiler, expressionFactory, combiningAlgRegistry);

		/*
		 * We are done parsing expressions in this policy, including VariableReferences, it's time to remove variables scoped to this policy from the variable manager
		 */
		for (final String varId : localVariableIds)
		{
			expressionFactory.removeVariable(varId);
		}

		return policyEvaluator;
	}

	private interface PolicyRefEvaluatorFactory<INSTANCE extends PolicyRefEvaluator>
	{

		INSTANCE getInstance(TopLevelPolicyElementType refPolicyType, String idRefPolicyId, Optional<VersionPatterns> versionConstraints, Deque<String> policySetRefChainWithIdRefIfPolicySet);
	}

	private static final class StaticPolicyRefEvaluatorFactory implements PolicyRefEvaluatorFactory<StaticPolicyRefEvaluator>
	{
		private final StaticRefPolicyProvider refPolicyProvider;

		private StaticPolicyRefEvaluatorFactory(final StaticRefPolicyProvider refPolicyProvider)
		{
			assert refPolicyProvider != null;
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		public StaticPolicyRefEvaluator getInstance(final TopLevelPolicyElementType refPolicyType, final String refPolicyId, final Optional<VersionPatterns> versionConstraints,
				final Deque<String> policySetRefChainWithRefPolicyIfPolicySet)
		{
			final StaticTopLevelPolicyElementEvaluator policy;
			try
			{
				policy = refPolicyProvider.get(refPolicyType, refPolicyId, versionConstraints, policySetRefChainWithRefPolicyIfPolicySet);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IllegalArgumentException("Error resolving statically or parsing " + PolicyRefEvaluator.toString(refPolicyType, refPolicyId, versionConstraints)
						+ " into its referenced policy (via static policy provider)", e);
			}

			if (policy == null)
			{
				throw new IllegalArgumentException("No " + refPolicyType + " matching reference: id = " + refPolicyId + ", " + versionConstraints);
			}

			return new StaticPolicyRefEvaluator(policy, versionConstraints);
		}
	}

	private static final class DynamicPolicyRefEvaluatorFactory implements PolicyRefEvaluatorFactory<PolicyRefEvaluator>
	{
		private final RefPolicyProvider refPolicyProvider;

		private DynamicPolicyRefEvaluatorFactory(final RefPolicyProvider refPolicyProvider)
		{
			assert refPolicyProvider != null;
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		public PolicyRefEvaluator getInstance(final TopLevelPolicyElementType refPolicyType, final String refPolicyId, final Optional<VersionPatterns> versionConstraints,
				final Deque<String> policySetRefChainWithRefPolicyIfPolicySet)
		{
			// dynamic reference resolution
			if (refPolicyType == TopLevelPolicyElementType.POLICY)
			{
				return new DynamicPolicyRefEvaluator(refPolicyId, versionConstraints, refPolicyProvider);
			}

			return new DynamicPolicySetRefEvaluator(refPolicyId, versionConstraints, refPolicyProvider, policySetRefChainWithRefPolicyIfPolicySet);
		}
	}

	private static <PRE extends PolicyRefEvaluator> PRE getInstanceGeneric(final PolicyRefEvaluatorFactory<PRE> policyRefEvaluatorFactory, final TopLevelPolicyElementType refPolicyType,
			final IdReferenceType idRef, final Deque<String> policySetRefChainWithIdRefIfPolicySet) throws IllegalArgumentException
	{
		assert policyRefEvaluatorFactory != null && idRef != null;

		final VersionPatterns versionConstraints = new VersionPatterns(idRef.getVersion(), idRef.getEarliestVersion(), idRef.getLatestVersion());
		return policyRefEvaluatorFactory.getInstance(refPolicyType, idRef.getValue(), Optional.of(versionConstraints), policySetRefChainWithIdRefIfPolicySet);
	}

	/**
	 * Instantiates Policy(Set) Reference evaluator from XACML Policy(Set)IdReference
	 *
	 * @param idRef
	 *            Policy(Set)IdReference
	 * @param refPolicyProvider
	 *            Policy(Set)IdReference resolver/Provider
	 * @param refPolicyType
	 *            type of policy referenced, i.e. whether it refers to Policy or PolicySet
	 * @param policySetRefChainWithIdRefIfPolicySet
	 *            null if {@code refPolicyType == TopLevelPolicyElementType.POLICY}; else it is the chain of PolicySets linked via PolicySetIdReferences, from the root PolicySet up to this reference
	 *            target (last item is the {@code idRef} value). Each item is a PolicySetId of a PolicySet that is referenced by the previous item (except the first item which is the root policy) and
	 *            references the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops (circular references) and prevent exceeding
	 *            reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code ancestorPolicySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as
	 *            well.)
	 *            </p>
	 * @return instance instance of PolicyReference
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code refPolicyProvider} undefined, or there is no policy of type {@code refPolicyType} matching {@code idRef} to be found by {@code refPolicyProvider}, or PolicySetIdReference
	 *             loop detected or PolicySetIdReference depth exceeds the max enforced by {@code policyProvider}
	 */
	public static PolicyRefEvaluator getInstance(final TopLevelPolicyElementType refPolicyType, final IdReferenceType idRef, final RefPolicyProvider refPolicyProvider,
			final Deque<String> policySetRefChainWithIdRefIfPolicySet) throws IllegalArgumentException
	{
		final PolicyRefEvaluatorFactory<? extends PolicyRefEvaluator> factory = refPolicyProvider instanceof StaticRefPolicyProvider ? new StaticPolicyRefEvaluatorFactory(
				(StaticRefPolicyProvider) refPolicyProvider) : new DynamicPolicyRefEvaluatorFactory(refPolicyProvider);
		return getInstanceGeneric(factory, refPolicyType, idRef, policySetRefChainWithIdRefIfPolicySet);
	}

	/**
	 * Instantiates Static Policy(Set) Reference evaluator from XACML Policy(Set)IdReference, "static" meaning that given {@code idRef} and {@code refPolicyType}, the returned policy is always the
	 * same statically defined policy
	 *
	 * @param idRef
	 *            Policy(Set)IdReference
	 * @param refPolicyProvider
	 *            Policy(Set)IdReference resolver/Provider
	 * @param refPolicyType
	 *            type of policy referenced, i.e. whether it refers to Policy or PolicySet
	 * @param ancestorPolicySetRefChain
	 *            chain of ancestor PolicySets linked via PolicySetIdReferences, from the root PolicySet up to the Policy(Set) reference being resolved by this method (excluded). <b>Null/empty if
	 *            {@code policyElement} this method is used to resolve the root PolicySet (no ancestor).</b> Each item is a PolicySetId of a PolicySet that is referenced by the previous item (except
	 *            the first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops
	 *            (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code ancestorPolicySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as
	 *            well.)
	 *            </p>
	 * @return instance instance of PolicyReference
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code refPolicyProvider} undefined, or there is no policy of type {@code refPolicyType} matching {@code idRef} to be found by {@code refPolicyProvider}, or PolicySetIdReference
	 *             loop detected or PolicySetIdReference depth exceeds the max enforced by {@code policyProvider}
	 */
	public static StaticPolicyRefEvaluator getInstanceStatic(final TopLevelPolicyElementType refPolicyType, final IdReferenceType idRef, final StaticRefPolicyProvider refPolicyProvider,
			final Deque<String> ancestorPolicySetRefChain) throws IllegalArgumentException
	{
		final StaticPolicyRefEvaluatorFactory factory = new StaticPolicyRefEvaluatorFactory(refPolicyProvider);
		return getInstanceGeneric(factory, refPolicyType, idRef, ancestorPolicySetRefChain);
	}

	private static abstract class PolicySetElementEvaluatorFactory<INSTANCE extends TopLevelPolicyElementEvaluator, COMBINED_ELT extends PolicyEvaluator>
	{
		protected final PrimaryPolicyMetadata policyMetadata;
		protected final XPathCompiler defaultXPathCompiler;
		protected final Map<String, String> namespacePrefixesByURI;
		protected final ExpressionFactory expressionFactory;
		protected final CombiningAlgRegistry combiningAlgorithmRegistry;

		private PolicySetElementEvaluatorFactory(final PrimaryPolicyMetadata policyMetadata, final DefaultsType policyDefaults, final XPathCompiler parentDefaultXPathCompiler,
				final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgorithmRegistry)
		{
			assert policyMetadata != null && combiningAlgorithmRegistry != null;
			this.policyMetadata = policyMetadata;
			/*
			 * Inherited PolicyDefaults is policyDefaults if not null, the parentPolicyDefaults otherwise
			 */
			if (policyDefaults == null)
			{
				defaultXPathCompiler = parentDefaultXPathCompiler;
			}
			else
			{
				try
				{
					defaultXPathCompiler = XmlUtils.newXPathCompiler(policyDefaults.getXPathVersion(), namespacePrefixesByURI);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyMetadata + ": Invalid PolicySetDefaults/XPathVersion or XML namespace prefix/URI undefined", e);
				}
			}

			this.namespacePrefixesByURI = namespacePrefixesByURI;
			this.expressionFactory = expressionFactory;
			this.combiningAlgorithmRegistry = combiningAlgorithmRegistry;
		}

		protected final StaticPolicyEvaluator getChildStaticPolicyEvaluator(final int childIndex, final Policy policyChildElt)
		{
			final StaticPolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstance(policyChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this.policyMetadata + ": invalid child #" + childIndex + " (Policy)", e);
			}

			return childElement;
		}

		protected abstract Deque<String> joinPolicySetRefChains(final Deque<String> policyRefChain1, final List<String> policyRefChain2);

		protected abstract COMBINED_ELT getChildPolicyEvaluator(int childIndex, Policy policyChildElt);

		protected abstract COMBINED_ELT getChildPolicySetEvaluator(int childIndex, PolicySet policySetChildElt, Set<String> updatableParsedPolicyIds, Set<String> updatableParsedPolicySetIds,
				Deque<String> policySetRefChain);

		/**
		 * 
		 * @param childIndex
		 *            index of this child policyRef element among all its parent's children (in order of declaration)
		 * @param refPolicyType
		 *            type of reference target (Policy or PolicySet
		 * @param idRef
		 *            policy reference
		 * @param policySetRefChainWithArgIfPolicySet
		 *            policySet reference chain that includes {@code idRef} value (target policyset ID) iff {@code refPolicyType == TopLevelPolicyElementType.POLICY_SET} (reference target is a
		 *            PolicySet)
		 * @return target policy evaluator
		 */
		protected abstract COMBINED_ELT getChildPolicyRefEvaluator(int childIndex, TopLevelPolicyElementType refPolicyType, IdReferenceType idRef, Deque<String> policySetRefChainWithArgIfPolicySet);

		protected abstract INSTANCE getInstance(PrimaryPolicyMetadata primaryPolicyMetadata, Target target, String policyCombiningAlgId, Iterable<COMBINED_ELT> combinedElements,
				Iterable<CombiningAlgParameter<? extends COMBINED_ELT>> policyCombinerParameters, List<ObligationExpression> obligationExpressions, List<AdviceExpression> adviceExpressions,
				Set<String> localVariableIDs);
	}

	private static final class StaticPolicySetElementEvaluatorFactory extends PolicySetElementEvaluatorFactory<StaticTopLevelPolicyElementEvaluator, StaticPolicyEvaluator>
	{
		private final StaticPolicySetChildRefsMetadataProvider extraMetadataProvider;
		private final StaticRefPolicyProvider refPolicyProvider;

		private StaticPolicySetElementEvaluatorFactory(final PrimaryPolicyMetadata primaryPolicyMetadata, final DefaultsType policyDefaults, final StaticRefPolicyProvider refPolicyProvider,
				final XPathCompiler parentDefaultXPathCompiler, final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory,
				final CombiningAlgRegistry combiningAlgorithmRegistry)
		{
			super(primaryPolicyMetadata, policyDefaults, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			this.extraMetadataProvider = new StaticPolicySetChildRefsMetadataProvider(primaryPolicyMetadata);
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		protected Deque<String> joinPolicySetRefChains(final Deque<String> policyRefChain1, final List<String> policyRefChain2)
		{
			return refPolicyProvider.joinPolicyRefChains(policyRefChain1, policyRefChain2);
		}

		@Override
		protected StaticPolicyEvaluator getChildPolicyEvaluator(final int childIndex, final Policy policyChildElt)
		{
			return getChildStaticPolicyEvaluator(childIndex, policyChildElt);
		}

		@Override
		protected StaticPolicyEvaluator getChildPolicySetEvaluator(final int childIndex, final PolicySet policySetChildElt, final Set<String> updatableParsedPolicyIds,
				final Set<String> updatableParsedPolicySetIds, final Deque<String> policySetRefChain)
		{
			/*
			 * Since it's a child PolicySet, there should be something in updatableParsedPolicySetIds containing itself and parent PolicySet
			 */
			assert updatableParsedPolicySetIds != null && !updatableParsedPolicySetIds.isEmpty();

			final StaticPolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstanceStatic(policySetChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry,
						updatableParsedPolicyIds, updatableParsedPolicySetIds, refPolicyProvider, policySetRefChain == null ? null : new ArrayDeque<>(policySetRefChain));
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this.policyMetadata + ": Invalid child #" + childIndex + " (PolicySet)", e);
			}

			/*
			 * This child PolicySet may have extra metadata such as nested policy references that we need to merge into the parent PolicySet's metadata
			 */
			final Optional<PolicyRefsMetadata> childPolicyRefsMetadata = childElement.getPolicyRefsMetadata();
			if (childPolicyRefsMetadata.isPresent())
			{
				extraMetadataProvider.updateMetadata(childPolicyRefsMetadata.get());
			}

			return childElement;
		}

		@Override
		protected StaticPolicyEvaluator getChildPolicyRefEvaluator(final int childIndex, final TopLevelPolicyElementType refPolicyType, final IdReferenceType idRef,
				final Deque<String> ancestorPolicySetRefChain)
		{
			if (refPolicyProvider == null)
			{
				throw new IllegalArgumentException(this.policyMetadata + ": invalid child #" + childIndex
						+ " (PolicyIdReference): no refPolicyProvider (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
			}

			final StaticPolicyRefEvaluator childElement = PolicyEvaluators.getInstanceStatic(refPolicyType, idRef, refPolicyProvider, ancestorPolicySetRefChain);
			final Optional<PolicyRefsMetadata> childPolicyRefsMetadata = childElement.getPolicyRefsMetadata();
			if (childPolicyRefsMetadata.isPresent())
			{
				extraMetadataProvider.updateMetadata(childPolicyRefsMetadata.get());
			}

			return childElement;
		}

		@Override
		protected StaticTopLevelPolicyElementEvaluator getInstance(final PrimaryPolicyMetadata primaryPolicyMetadata, final Target policyTarget, final String policyCombiningAlgId,
				final Iterable<StaticPolicyEvaluator> combinedElements, final Iterable<CombiningAlgParameter<? extends StaticPolicyEvaluator>> policyCombinerParameters,
				final List<ObligationExpression> obligationExpressions, final List<AdviceExpression> adviceExpressions, final Set<String> localVariableIDs)
		{
			return new StaticBaseTopLevelPolicyElementEvaluator<>(StaticPolicyEvaluator.class, primaryPolicyMetadata, extraMetadataProvider.getMetadata(), policyTarget, policyCombiningAlgId,
					combinedElements, policyCombinerParameters, obligationExpressions, adviceExpressions, localVariableIDs, defaultXPathCompiler, expressionFactory, combiningAlgorithmRegistry);
		}
	}

	private static final class DynamicPolicySetElementEvaluatorFactory extends PolicySetElementEvaluatorFactory<TopLevelPolicyElementEvaluator, PolicyEvaluator>
	{
		private final DynamicPolicySetChildRefsMetadataProvider extraMetadataProvider;
		private final RefPolicyProvider refPolicyProvider;

		private DynamicPolicySetElementEvaluatorFactory(final PrimaryPolicyMetadata primaryPolicyMetadata, final DefaultsType policyDefaults, final RefPolicyProvider refPolicyProvider,
				final XPathCompiler parentDefaultXPathCompiler, final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory,
				final CombiningAlgRegistry combiningAlgorithmRegistry)
		{
			super(primaryPolicyMetadata, policyDefaults, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			this.extraMetadataProvider = new DynamicPolicySetChildRefsMetadataProvider();
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		protected Deque<String> joinPolicySetRefChains(final Deque<String> policyRefChain1, final List<String> policyRefChain2)
		{
			return refPolicyProvider.joinPolicyRefChains(policyRefChain1, policyRefChain2);
		}

		@Override
		protected PolicyEvaluator getChildPolicyEvaluator(final int childIndex, final Policy policyChildElt)
		{
			return getChildStaticPolicyEvaluator(childIndex, policyChildElt);
		}

		@Override
		protected PolicyEvaluator getChildPolicySetEvaluator(final int childIndex, final PolicySet policySetChildElt, final Set<String> updatableParsedPolicyIds,
				final Set<String> updatableParsedPolicySetIds, final Deque<String> policySetRefChain)
		{
			/*
			 * Since it's a child PolicySet, there should be something in parsedPolicySetids containing itself and parent PolicySet
			 */
			assert updatableParsedPolicySetIds != null && !updatableParsedPolicySetIds.isEmpty();

			final PolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstance(policySetChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry, updatableParsedPolicyIds,
						updatableParsedPolicySetIds, refPolicyProvider, policySetRefChain == null ? null : new ArrayDeque<>(policySetRefChain));
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this.policyMetadata + ": Invalid child #" + childIndex + " (PolicySet)", e);
			}

			/*
			 * This child PolicySet may have extra metadata such as nested policy references that we need to merge into the parent PolicySet's metadata
			 */
			extraMetadataProvider.addChildPolicySetElementOrRef(childElement);
			return childElement;
		}

		@Override
		protected PolicyEvaluator getChildPolicyRefEvaluator(final int childIndex, final TopLevelPolicyElementType refPolicyType, final IdReferenceType idRef,
				final Deque<String> policySetRefChainWithArgIfPolicySet)
		{
			if (refPolicyProvider == null)
			{
				throw new IllegalArgumentException(this.policyMetadata + ": invalid child #" + childIndex
						+ " (PolicyIdReference): no refPolicyProvider (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
			}

			final PolicyRefEvaluator childElement = PolicyEvaluators.getInstance(refPolicyType, idRef, refPolicyProvider, policySetRefChainWithArgIfPolicySet);
			extraMetadataProvider.addChildPolicySetElementOrRef(childElement);
			return childElement;
		}

		@Override
		protected TopLevelPolicyElementEvaluator getInstance(final PrimaryPolicyMetadata primaryPolicyMetadata, final Target policyTarget, final String policyCombiningAlgId,
				final Iterable<PolicyEvaluator> combinedElements, final Iterable<CombiningAlgParameter<? extends PolicyEvaluator>> policyCombinerParameters,
				final List<ObligationExpression> obligationExpressions, final List<AdviceExpression> adviceExpressions, final Set<String> localVariableIDs)
		{
			return new DynamicPolicySetEvaluator(primaryPolicyMetadata, extraMetadataProvider, policyTarget, policyCombiningAlgId, combinedElements, policyCombinerParameters, obligationExpressions,
					adviceExpressions, localVariableIDs, defaultXPathCompiler, expressionFactory, combiningAlgorithmRegistry);
		}
	}

	/**
	 * Generic creation of PolicySet evaluator
	 * 
	 * @param policySetRefChainWithArgIffRefTarget
	 *            null/empty if {@code policyElement} is the root policySet; else it is the chain of top-level (as opposed to nested inline) PolicySets linked by PolicySetIdReferences from the root
	 *            PolicySet up to (and including) the top-level (PolicySetIdReference-targeted) PolicySet that encloses or is {@code policyElement}
	 */
	private static <TLPEE extends TopLevelPolicyElementEvaluator, COMBINED_EVALUATOR extends PolicyEvaluator> TLPEE getInstanceGeneric(
			final PolicySetElementEvaluatorFactory<TLPEE, COMBINED_EVALUATOR> policyEvaluatorFactory, final PolicySet policyElement, final Set<String> updatableParsedPolicyIds,
			final Set<String> updatableParsedPolicySetIds, final Deque<String> policySetRefChainWithArgIffRefTarget) throws IllegalArgumentException
	{
		assert policyEvaluatorFactory != null && policyElement != null && updatableParsedPolicySetIds != null;

		final String policyId = policyElement.getPolicySetId();
		// we are parsing a new policyset -> update updatableParsedPolicySetIds
		if (!updatableParsedPolicySetIds.add(policyId))
		{
			throw new IllegalArgumentException("Duplicate PolicySetId = " + policyId);
		}

		/*
		 * Make sure we use a non-null set of already parsed policy identifiers (to find duplicates) to avoid checking non-nullity at every iteration of the for loop below where we find a policy
		 */
		final Set<String> nonNullParsedPolicyIds = updatableParsedPolicyIds == null ? HashCollections.<String> newUpdatableSet() : updatableParsedPolicyIds;
		/*
		 * Elements defined in xs:choice of PolicySetType in XACML schema (Policy(Set)/Policy(Set)IdReference/CombinerParameters/Policy(Set)CombinerParameters
		 */
		final List<Serializable> jaxbPolicySetChoiceElements = policyElement.getPolicySetsAndPoliciesAndPolicySetIdReferences();
		/*
		 * Prepare the list of evaluators combined by the combining algorithm in this PolicySet, i.e. Policy(Set)/Policy(Set)IdReference evaluators. combinedEvaluators.size() <=
		 * jaxbPolicySetChoiceElements.size() since combinedEvaluators does not include *CombinerParameter evaluators
		 */
		final List<COMBINED_EVALUATOR> combinedEvaluators = new ArrayList<>(jaxbPolicySetChoiceElements.size());

		/**
		 * Why isn't there any VariableDefinition in XACML PolicySet like in Policy? If there were, we would keep a copy of variable IDs defined in this policy, to remove them from the global manager
		 * at the end of parsing this PolicySet. They should not be visible outside the scope of this.
		 * <p>
		 * final Set<String> variableIds = HashCollections.newUpdatableSet(jaxbPolicySetChoiceElements.size());
		 */

		/*
		 * Map to get child Policies by their ID so that we can resolve Policies associated with PolicyCombinerParameters Size cannot get bigger than jaxbPolicySetChoiceElements.size()
		 */
		final Map<String, COMBINED_EVALUATOR> childPolicyEvaluatorsByPolicyId = HashCollections.newUpdatableMap(jaxbPolicySetChoiceElements.size());

		/*
		 * Map to get child PolicySets by their ID so that we can resolve PolicySets associated with PolicySetCombinerParameters Size cannot get bigger than jaxbPolicySetChoiceElements.size()
		 */
		final Map<String, COMBINED_EVALUATOR> childPolicySetEvaluatorsByPolicySetId = HashCollections.newUpdatableMap(jaxbPolicySetChoiceElements.size());

		/*
		 * *CombinerParameters (combining algorithm parameters), size <= jaxbPolicySetChoiceElements.size()
		 */
		final List<CombiningAlgParameter<? extends COMBINED_EVALUATOR>> combiningAlgParameters = new ArrayList<>(jaxbPolicySetChoiceElements.size());
		int childIndex = 0;
		for (final Serializable policyChildElt : jaxbPolicySetChoiceElements)
		{
			if (policyChildElt instanceof PolicyCombinerParameters)
			{
				final String combinedPolicyId = ((PolicyCombinerParameters) policyChildElt).getPolicyIdRef();
				final COMBINED_EVALUATOR childPolicyEvaluator = childPolicyEvaluatorsByPolicyId.get(combinedPolicyId);
				if (childPolicyEvaluator == null)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyMetadata + ":  invalid PolicyCombinerParameters: referencing undefined child Policy #" + combinedPolicyId
							+ " (no such policy defined before this element)");
				}

				final BaseCombiningAlgParameter<COMBINED_EVALUATOR> combiningAlgParameter;
				try
				{
					combiningAlgParameter = new BaseCombiningAlgParameter<>(childPolicyEvaluator, ((CombinerParametersType) policyChildElt).getCombinerParameters(),
							policyEvaluatorFactory.expressionFactory, policyEvaluatorFactory.defaultXPathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyMetadata + ": invalid child #" + childIndex + " (PolicyCombinerParameters)", e);
				}

				combiningAlgParameters.add(combiningAlgParameter);

			}
			else if (policyChildElt instanceof PolicySetCombinerParameters)
			{
				final String combinedPolicySetId = ((PolicySetCombinerParameters) policyChildElt).getPolicySetIdRef();
				final COMBINED_EVALUATOR combinedPolicySetEvaluator = childPolicySetEvaluatorsByPolicySetId.get(combinedPolicySetId);
				if (combinedPolicySetEvaluator == null)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyMetadata + ":  invalid PolicySetCombinerParameters: referencing undefined child PolicySet #" + combinedPolicySetId
							+ " (no such policySet defined before this element)");
				}

				final BaseCombiningAlgParameter<COMBINED_EVALUATOR> combiningAlgParameter;
				try
				{
					combiningAlgParameter = new BaseCombiningAlgParameter<>(combinedPolicySetEvaluator, ((CombinerParametersType) policyChildElt).getCombinerParameters(),
							policyEvaluatorFactory.expressionFactory, policyEvaluatorFactory.defaultXPathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyMetadata + ": invalid child #" + childIndex + " (PolicySetCombinerParameters)", e);
				}

				combiningAlgParameters.add(combiningAlgParameter);
			}
			else if (policyChildElt instanceof JAXBElement)
			{
				final JAXBElement<?> jaxbPolicyChildElt = (JAXBElement<?>) policyChildElt;
				final String eltNameLocalPart = jaxbPolicyChildElt.getName().getLocalPart();
				if (eltNameLocalPart.equals(XacmlNodeName.POLICY_ID_REFERENCE.value()))
				{
					final IdReferenceType policyChildIdRef = (IdReferenceType) jaxbPolicyChildElt.getValue();
					final COMBINED_EVALUATOR childEvaluator = policyEvaluatorFactory.getChildPolicyRefEvaluator(childIndex, TopLevelPolicyElementType.POLICY, policyChildIdRef, null);
					combinedEvaluators.add(childEvaluator);
					final COMBINED_EVALUATOR duplicate = childPolicySetEvaluatorsByPolicySetId.putIfAbsent(childEvaluator.getPolicyId(), childEvaluator);
					if (duplicate != null)
					{
						throw new IllegalArgumentException("Duplicate PolicyIdReference's id = " + childEvaluator.getPolicyId());
					}
				}
				else if (eltNameLocalPart.equals(XacmlNodeName.POLICYSET_ID_REFERENCE.value()))
				{
					final IdReferenceType policyChildIdRef = (IdReferenceType) jaxbPolicyChildElt.getValue();
					final String policyChildId = policyChildIdRef.getValue();
					/*
					 * Add this new reference to policyChildIdRef to the policyRef chain arg of getChildPolicyRefEvaluator(...). If policySetRefChainWithArgIffRefTarget is null/empty, policyElement is
					 * the root policy (no ancestor in the chain), therefore it should be added before policyChildIdRef, as the antecedent; Else non-empty policySetRefChainWithArgIffRefTarget's last
					 * item is either policyElement (iff it is a policy ref's target) or the top-level (as opposed to nested inline) PolicySet that encloses policyElement, in which either case we just
					 * add policyChildIdRef to the chain.
					 */
					final Deque<String> newPolicySetRefChainWithArgIffRefTarget = policySetRefChainWithArgIffRefTarget == null || policySetRefChainWithArgIffRefTarget.isEmpty() ? new ArrayDeque<>(
							Arrays.asList(policyId, policyChildId)) : policyEvaluatorFactory.joinPolicySetRefChains(policySetRefChainWithArgIffRefTarget, Collections.singletonList(policyChildId));
					final COMBINED_EVALUATOR childEvaluator = policyEvaluatorFactory.getChildPolicyRefEvaluator(childIndex, TopLevelPolicyElementType.POLICY_SET, policyChildIdRef,
							newPolicySetRefChainWithArgIffRefTarget);
					combinedEvaluators.add(childEvaluator);
					final COMBINED_EVALUATOR duplicate = childPolicySetEvaluatorsByPolicySetId.put(policyChildId, childEvaluator);
					if (duplicate != null)
					{
						throw new IllegalArgumentException("Duplicate PolicySetIdReference's id = " + policyChildId);
					}
				}
				else if (eltNameLocalPart.equals(XacmlNodeName.COMBINER_PARAMETERS.value()))
				{
					/*
					 * CombinerParameters that is not Policy(Set)CombinerParameters already tested before
					 */
					final BaseCombiningAlgParameter<COMBINED_EVALUATOR> combiningAlgParameter;
					try
					{
						combiningAlgParameter = new BaseCombiningAlgParameter<>(null, ((CombinerParametersType) jaxbPolicyChildElt.getValue()).getCombinerParameters(),
								policyEvaluatorFactory.expressionFactory, policyEvaluatorFactory.defaultXPathCompiler);
					}
					catch (final IllegalArgumentException e)
					{
						throw new IllegalArgumentException(policyEvaluatorFactory.policyMetadata + ": invalid child #" + childIndex + " (CombinerParameters)", e);
					}

					combiningAlgParameters.add(combiningAlgParameter);
				}
			}
			else if (policyChildElt instanceof PolicySet)
			{
				final PolicySet childPolicy = (PolicySet) policyChildElt;
				/*
				 * XACML spec §5.1: "ensure that no two policies visible to the PDP have the same identifier"
				 */
				final String childPolicyId = childPolicy.getPolicySetId();
				/*
				 * Create/Update the policySet ref chain if necessary. If policySetRefChainWithArgIffRefTarget is null/empty, this means policyElement is the root policyset (no antecedent), and we
				 * create a chain with its ID, to know the antecedent of the next encountered policyset ref (which may be found deep under multiple levels of nested PolicySets).; else
				 * policySetRefChainWithArgIffRefTarget's last item is either policyElement (iff it is a policy ref's target) or the top-level (as opposed to nested inline) PolicySet that encloses
				 * policyElement, in which either case we already have the info we need in the chain so keep it as is.
				 */
				final Deque<String> newPolicySetRefChain = policySetRefChainWithArgIffRefTarget == null || policySetRefChainWithArgIffRefTarget.isEmpty() ? new ArrayDeque<>(
						Collections.singletonList(policyId)) : policySetRefChainWithArgIffRefTarget;
				final COMBINED_EVALUATOR childEvaluator = policyEvaluatorFactory.getChildPolicySetEvaluator(childIndex, childPolicy, nonNullParsedPolicyIds, updatableParsedPolicySetIds,
						newPolicySetRefChain);
				combinedEvaluators.add(childEvaluator);
				final COMBINED_EVALUATOR duplicate = childPolicySetEvaluatorsByPolicySetId.putIfAbsent(childPolicyId, childEvaluator);
				if (duplicate != null)
				{
					throw new IllegalArgumentException("Duplicate PolicySetId = " + childPolicyId);
				}
			}
			else if (policyChildElt instanceof Policy)
			{
				final Policy childPolicy = (Policy) policyChildElt;
				/*
				 * XACML spec §5.1: "ensure that no two policies visible to the PDP have the same identifier"
				 */
				final String childPolicyId = childPolicy.getPolicyId();
				if (!nonNullParsedPolicyIds.add(childPolicyId))
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyMetadata + ": invalid child #" + childIndex + ": duplicate PolicyId = " + childPolicyId);
				}

				final COMBINED_EVALUATOR childEvaluator = policyEvaluatorFactory.getChildPolicyEvaluator(childIndex, childPolicy);
				combinedEvaluators.add(childEvaluator);
				final COMBINED_EVALUATOR duplicate = childPolicyEvaluatorsByPolicyId.putIfAbsent(childPolicyId, childEvaluator);
				if (duplicate != null)
				{
					throw new IllegalArgumentException("Duplicate PolicyId = " + childPolicyId);
				}

			}

			/*
			 * Why isn't there any VariableDefinition in XACML PolicySet defined by OASIS XACML 3.0 spec, like in Policy? If there were, the following code would be used (same as in PolicyEvaluator
			 * class).
			 */
			// else if (policySetChildElt instanceof VariableDefinition)
			// {
			// final VariableDefinition varDef = (VariableDefinition)
			// policyChildElt;
			// final Deque<String> varDefLongestVarRefChain = new
			// ArrayDeque<>();
			// final VariableReference<?> var;
			// try
			// {
			// var = expressionFactory.addVariable(varDef, defaultXPathCompiler,
			// varDefLongestVarRefChain);
			// } catch (IllegalArgumentException e)
			// {
			// throw new IllegalArgumentException(policyFriendlyId + ": invalid
			// child #" + childIndex + " (VariableDefinition)", e);
			// }
			//
			// if (var != null)
			// {
			// /*
			// * Conflicts can occur between variables defined in this policy
			// but also with others already in a wider scope, i.e. defined in
			// * parent/ancestor policy
			// */
			// throw new IllegalArgumentException(policyFriendlyId + ":
			// Duplicable VariableDefinition for VariableId=" +
			// var.getVariableId());
			// }
			//
			// localVariableIds.add(varDef.getVariableId());
			// // check whether the longest VariableReference chain in the
			// VariableDefinition is longer than what we've got so far
			// final int sizeOfVarDefLongestVarRefChain =
			// varDefLongestVarRefChain.size();
			// if(sizeOfVarDefLongestVarRefChain >
			// sizeOfPolicyLongestVarRefChain) {
			// sizeOfPolicyLongestVarRefChain = sizeOfVarDefLongestVarRefChain;
			// }
			// }

			childIndex++;
		}

		/*
		 * Why isn't there any VariableDefinition in XACML PolicySet like in Policy? If there were, the final following code would be used: We are done parsing expressions in this policy, including
		 * VariableReferences, it's time to remove variables scoped to this policy from the variable manager
		 */
		// for (final String varId : variableIds)
		// {
		// expFactory.remove(varId);
		// }

		final ObligationExpressions obligationExps = policyElement.getObligationExpressions();
		final AdviceExpressions adviceExps = policyElement.getAdviceExpressions();
		final Set<String> localVariableIds = Collections.emptySet();
		return policyEvaluatorFactory.getInstance(policyEvaluatorFactory.policyMetadata, policyElement.getTarget(), policyElement.getPolicyCombiningAlgId(), combinedEvaluators,
				combiningAlgParameters, obligationExps == null ? null : obligationExps.getObligationExpressions(), adviceExps == null ? null : adviceExps.getAdviceExpressions(), localVariableIds);
	}

	/**
	 * Creates statically defined PolicySet handler from XACML PolicySet element
	 *
	 * @param policyElement
	 *            PolicySet (XACML) without any dynamic policy references
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySet's default XPath version, or null if either no parent or no default XPath version defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML PolicySet (XML) document, to be used for namespace-aware XPath evaluation; null or empty iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgorithmRegistry
	 *            policy/rule combining algorithm registry
	 * @param refPolicyProvider
	 *            static policy-by-reference (Policy(Set)IdReference) Provider - all references statically resolved - to find references used in this policyset
	 * @param policySetRefChainWithPolicyElementIfRefTarget
	 *            null/empty if {@code policyElement} is a root PolicySet; else it is the chain of top-level (as opposed to nested inline) PolicySets linked via PolicySetIdReferences, from the root
	 *            PolicySet up to - and including - the top-level PolicySet that encloses or is {@code policyElement} (i.e. it a reference's target). Each item is a PolicySetId of a PolicySet that is
	 *            referenced by the previous item (except the first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the
	 *            result policy, in order to detect loops (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code ancestorPolicySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as
	 *            well.)
	 *            </p>
	 * @param updatableParsedPolicyIds
	 *            set of PolicyIds of all Policy elements parsed so far by the PDP or in the same PDP instantiation. Used to detect duplicate PolicyId.
	 * @param updatableParsedPolicySetIds
	 *            set of PolicySetIds of all PolicySet elements previously parsed so far by the PDP or in the same PDP instantiation. Used to detect duplicate PolicySetId.
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument (e.g. {@code policyElement}) is invalid
	 */
	public static StaticTopLevelPolicyElementEvaluator getInstanceStatic(final PolicySet policyElement, final XPathCompiler parentDefaultXPathCompiler,
			final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgorithmRegistry,
			final Set<String> updatableParsedPolicyIds, final Set<String> updatableParsedPolicySetIds, final StaticRefPolicyProvider refPolicyProvider,
			final Deque<String> policySetRefChainWithPolicyElementIfRefTarget) throws IllegalArgumentException
	{
		if (policyElement == null)
		{
			throw NULL_XACML_POLICYSET_ARG_EXCEPTION;
		}

		final PrimaryPolicyMetadata policyMetadata = new BasePrimaryPolicyMetadata(TopLevelPolicyElementType.POLICY_SET, policyElement.getPolicySetId(), new PolicyVersion(policyElement.getVersion()));
		final StaticPolicySetElementEvaluatorFactory factory = new StaticPolicySetElementEvaluatorFactory(policyMetadata, policyElement.getPolicySetDefaults(), refPolicyProvider,
				parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
		final Set<String> nonNullParsedPolicySetIds = updatableParsedPolicySetIds == null ? HashCollections.<String> newUpdatableSet() : updatableParsedPolicySetIds;
		return getInstanceGeneric(factory, policyElement, updatableParsedPolicyIds, nonNullParsedPolicySetIds, policySetRefChainWithPolicyElementIfRefTarget);
	}

	/**
	 * Creates statically defined PolicySet handler from XACML PolicySet element
	 *
	 * @param policyElement
	 *            PolicySet (XACML) without any dynamic policy references
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySet's default XPath version, or null if either no parent or no default XPath version defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML PolicySet (XML) document, to be used for namespace-aware XPath evaluation; null or empty iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgorithmRegistry
	 *            policy/rule combining algorithm registry
	 * @param refPolicyProvider
	 *            static policy-by-reference (Policy(Set)IdReference) Provider - all references statically resolved - to find references used in this policyset
	 * @param policySetRefChainWithPolicyElementIfRefTarget
	 *            null/empty if {@code policyElement} is a root PolicySet; else it is the chain of top-level (as opposed to nested inline) PolicySets linked via PolicySetIdReferences, from the root
	 *            PolicySet up to - and including - the top-level PolicySet that encloses or is {@code policyElement} (i.e. it a reference's target). Each item is a PolicySetId of a PolicySet that is
	 *            referenced by the previous item (except the first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the
	 *            result policy, in order to detect loops (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code ancestorPolicySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as
	 *            well.)
	 *            </p>
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument (e.g. {@code policyElement}) is invalid
	 */
	public static StaticTopLevelPolicyElementEvaluator getInstanceStatic(final PolicySet policyElement, final XPathCompiler parentDefaultXPathCompiler,
			final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgorithmRegistry,
			final StaticRefPolicyProvider refPolicyProvider, final Deque<String> policySetRefChainWithPolicyElementIfRefTarget) throws IllegalArgumentException
	{
		return getInstanceStatic(policyElement, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry, null, null, refPolicyProvider,
				policySetRefChainWithPolicyElementIfRefTarget);
	}

	/**
	 * Creates PolicySet handler from XACML PolicySet element with additional check of duplicate Policy(Set)Ids against a list of Policy(Set)s parsed during the PDP initialization so far
	 *
	 * @param policyElement
	 *            PolicySet (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySet's default XPath version, or null if either no parent or no default XPath version defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML PolicySet (XML) document, to be used for namespace-aware XPath evaluation; null or empty iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgorithmRegistry
	 *            policy/rule combining algorithm registry
	 * @param refPolicyProvider
	 *            policy-by-reference (Policy(Set)IdReference) Provider to find references used in this policyset
	 * @param ancestorPolicySetRefChain
	 *            chain of ancestor PolicySets linked via PolicySetIdReferences, from the root PolicySet up to {@code policyElement} (excluded). <b>Null/empty if {@code policyElement} is the root
	 *            PolicySet (no ancestor).</b> Each item is a PolicySetId of a PolicySet that is referenced by the previous item (except the first item which is the root policy) and references the
	 *            next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code ancestorPolicySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as
	 *            well.)
	 *            </p>
	 * @param updatableParsedPolicyIds
	 *            set of PolicyIds of all Policy elements parsed so far by the PDP or in the same PDP instantiation. Used to detect duplicate PolicyId.
	 * @param updatableParsedPolicySetIds
	 *            set of PolicySetIds of all PolicySet elements parsed so far by the PDP or in the same PDP instantiation. Used to detect duplicate PolicySetId.
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument (e.g. {@code policyElement}) is invalid
	 */
	public static TopLevelPolicyElementEvaluator getInstance(final PolicySet policyElement, final XPathCompiler parentDefaultXPathCompiler, final Map<String, String> namespacePrefixesByURI,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgorithmRegistry, final Set<String> updatableParsedPolicyIds,
			final Set<String> updatableParsedPolicySetIds, final RefPolicyProvider refPolicyProvider, final Deque<String> ancestorPolicySetRefChain) throws IllegalArgumentException
	{
		if (policyElement == null)
		{
			throw NULL_XACML_POLICYSET_ARG_EXCEPTION;
		}

		final PrimaryPolicyMetadata policyMetadata = new BasePrimaryPolicyMetadata(TopLevelPolicyElementType.POLICY_SET, policyElement.getPolicySetId(), new PolicyVersion(policyElement.getVersion()));
		final PolicySetElementEvaluatorFactory<?, ?> factory = refPolicyProvider instanceof StaticRefPolicyProvider ? new StaticPolicySetElementEvaluatorFactory(policyMetadata,
				policyElement.getPolicySetDefaults(), (StaticRefPolicyProvider) refPolicyProvider, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry)
				: new DynamicPolicySetElementEvaluatorFactory(policyMetadata, policyElement.getPolicySetDefaults(), refPolicyProvider, parentDefaultXPathCompiler, namespacePrefixesByURI,
						expressionFactory, combiningAlgorithmRegistry);
		final Set<String> nonNullParsedPolicySetIds = updatableParsedPolicySetIds == null ? HashCollections.<String> newUpdatableSet() : updatableParsedPolicySetIds;
		return getInstanceGeneric(factory, policyElement, updatableParsedPolicyIds, nonNullParsedPolicySetIds, ancestorPolicySetRefChain);
	}

	/**
	 * Creates PolicySet handler from XACML PolicySet element
	 *
	 * @param policyElement
	 *            PolicySet (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySet's default XPath version, or null if either no parent or no default XPath version defined in parent
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML PolicySet (XML) document, to be used for namespace-aware XPath evaluation; null or empty iff XPath support disabled
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgorithmRegistry
	 *            policy/rule combining algorithm registry
	 * @param refPolicyProvider
	 *            policy-by-reference (Policy(Set)IdReference) Provider to find references used in this policyset
	 * @param ancestorPolicySetRefChain
	 *            chain of ancestor PolicySets linked via PolicySetIdReferences, from the root PolicySet up to {@code policyElement} (excluded). <b>Null/empty if {@code policyElement} is the root
	 *            PolicySet (no ancestor).</b> Each item is a PolicySetId of a PolicySet that is referenced by the previous item (except the first item which is the root policy) and references the
	 *            next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code ancestorPolicySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as
	 *            well.)
	 *            </p>
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument (e.g. {@code policyElement}) is invalid
	 */
	public static TopLevelPolicyElementEvaluator getInstance(final PolicySet policyElement, final XPathCompiler parentDefaultXPathCompiler, final Map<String, String> namespacePrefixesByURI,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgorithmRegistry, final RefPolicyProvider refPolicyProvider, final Deque<String> ancestorPolicySetRefChain)
			throws IllegalArgumentException
	{
		return getInstance(policyElement, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry, null, null, refPolicyProvider, ancestorPolicySetRefChain);
	}
}
