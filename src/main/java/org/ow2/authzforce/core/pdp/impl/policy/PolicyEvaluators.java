/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.XMLUtils;
import org.ow2.authzforce.core.pdp.api.combining.CombinerParameterEvaluator;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.VariableReference;
import org.ow2.authzforce.core.pdp.api.policy.ExtraPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.ow2.authzforce.core.pdp.impl.BaseDecisionResult;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.ow2.authzforce.xacml.identifiers.XACMLNodeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class consists exclusively of static methods that operate on or return {@link PolicyEvaluator}s
 *
 * 
 * @version $Id: $
 */
public final class PolicyEvaluators
{

	private static final IllegalArgumentException UNDEF_REF_POLICY_PROVIDER_EXCEPTION = new IllegalArgumentException("Policy(Set)IdReference resolver/Provider undefined");
	private static final IllegalArgumentException NULL_XACML_COMBINING_ALG_ARG_EXCEPTION = new IllegalArgumentException("Undefined policy/rule combining algorithm registry");
	private static final IllegalArgumentException NULL_XACML_POLICY_ARG_EXCEPTION = new IllegalArgumentException("Undefined XACML <Policy>");

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
		private BaseCombiningAlgParameter(T element, List<CombinerParameter> jaxbCombinerParameters, ExpressionFactory expFactory, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			this.element = element;
			if (jaxbCombinerParameters == null)
			{
				this.parameters = Collections.emptyList();
			} else
			{
				final List<CombinerParameterEvaluator> modifiableParamList = new ArrayList<>();
				int paramIndex = 0;
				for (CombinerParameter jaxbCombinerParam : jaxbCombinerParameters)
				{
					try
					{
						final CombinerParameterEvaluator combinerParam = new CombinerParameterEvaluator(jaxbCombinerParam, expFactory, xPathCompiler);
						modifiableParamList.add(combinerParam);
					} catch (IllegalArgumentException e)
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

	private static class BaseExtraPolicyMetadata implements ExtraPolicyMetadata
	{
		private final PolicyVersion version;
		private final Map<String, PolicyVersion> refPolicyVersionsByPolicyId;
		private final Map<String, PolicyVersion> refPolicySetVersionsByPolicyId;
		private final List<String> longestPolicyRefChain;

		/**
		 * This constructor will make all fields immutable, so do you need to make args immutable before passing them to this.
		 * 
		 * @param version
		 *            policy version
		 * @param refPolicies
		 *            policies referenced from the policy
		 * @param longestPolicyRefChain
		 *            longest chain of policy references (Policy(Set)IdReferences) originating from the policy
		 */
		protected BaseExtraPolicyMetadata(PolicyVersion version, Map<String, PolicyVersion> refPolicies, Map<String, PolicyVersion> refPolicySets, List<String> longestPolicyRefChain)
		{
			assert version != null && refPolicies != null && refPolicySets != null && longestPolicyRefChain != null;
			this.version = version;
			this.refPolicyVersionsByPolicyId = Collections.unmodifiableMap(refPolicies);
			this.refPolicySetVersionsByPolicyId = refPolicySets;
			this.longestPolicyRefChain = Collections.unmodifiableList(longestPolicyRefChain);
		}

		@Override
		public PolicyVersion getVersion()
		{
			return version;
		}

		@Override
		public List<String> getLongestPolicyRefChain()
		{
			return longestPolicyRefChain;
		}

		@Override
		public Map<String, PolicyVersion> getRefPolicies()
		{
			return refPolicyVersionsByPolicyId;
		}

		@Override
		public Map<String, PolicyVersion> getRefPolicySets()
		{
			return refPolicySetVersionsByPolicyId;
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
		private static final Logger LOGGER = LoggerFactory.getLogger(BaseTopLevelPolicyElementEvaluator.class);
		private static final IllegalArgumentException NULL_POLICY_ID_EXCEPTION = new IllegalArgumentException("Undefined Policy(Set)Id (required)");
		private static final IllegalArgumentException NULL_VERSION_EXCEPTION = new IllegalArgumentException("Undefined Policy(Set) Version (required)");

		private final String policyId;
		private final PolicyVersion policyVersion;
		private final TargetEvaluator targetEvaluator;
		private final CombiningAlg.Evaluator combiningAlgEvaluator;
		private final PolicyPepActionExpressionsEvaluator pepActionExps;
		private final Set<String> localVariableIds;

		private transient final String toString;
		private transient final int hashCode;
		private transient final JAXBElement<IdReferenceType> refToSelf;
		private transient final TopLevelPolicyElementType policyType;
		private transient final String requestScopedEvalResultsCacheKey;

		private static final class EvalResults
		{
			private final String policyId;
			private DecisionResult resultWithTarget = null;
			private DecisionResult resultWithoutTarget = null;

			private EvalResults(String policyId)
			{
				this.policyId = policyId;
			}

			private void setResult(boolean skipTarget, DecisionResult result)
			{
				assert result != null;
				if (skipTarget)
				{
					if (resultWithoutTarget != null)
					{
						throw new UnsupportedOperationException(policyId + ": evaluation result (skipTarget = true) already set in this context");
					}

					resultWithoutTarget = result;
				} else
				{
					if (resultWithoutTarget != null)
					{
						throw new UnsupportedOperationException(policyId + ": evaluation result (skipTarget = false) already set in this context");
					}

					resultWithTarget = result;
				}
			}
		}

		/**
		 * Instantiates an evaluator
		 * 
		 * @param combinedElementClass
		 *            combined element class
		 * 
		 * @param policyTarget
		 *            policy(Set) Target
		 * @param combinedElements
		 *            child elements combined in the policy(set) by {@code combiningAlg}
		 * @param combinerParameters
		 *            combining algorithm parameters
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
		 *             if {@code jaxbPolicyRef == null || jaxbPolicyRef.getValue().getValue() == null || jaxbPolicyRef.getValue().getVersion() == null}
		 */
		protected BaseTopLevelPolicyElementEvaluator(Class<T> combinedElementClass, String policyId, PolicyVersion version, Target policyTarget, String combiningAlgId, List<T> combinedElements,
				List<CombiningAlgParameter<? extends T>> combinerParameters, ObligationExpressions obligationExps, AdviceExpressions adviceExps, Set<String> localVariableIds,
				XPathCompiler defaultXPathCompiler, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
		{
			if (policyId == null)
			{
				throw NULL_POLICY_ID_EXCEPTION;
			}

			if (version == null)
			{
				throw NULL_VERSION_EXCEPTION;
			}

			this.policyId = policyId;
			this.policyVersion = version;
			final IdReferenceType idRef = new IdReferenceType(policyId, version.toString(), null, null);
			if (combinedElementClass == RuleEvaluator.class)
			{
				this.policyType = TopLevelPolicyElementType.POLICY;
				this.refToSelf = JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createPolicyIdReference(idRef);
			} else
			{
				this.policyType = TopLevelPolicyElementType.POLICY_SET;
				this.refToSelf = JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createPolicySetIdReference(idRef);
			}

			this.toString = policyType + "[" + this.policyId + "#v" + this.policyVersion + "]";
			/*
			 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well in PolicyIdReferences. So we consider it is useless for identification in the XACML model.
			 */
			this.hashCode = Objects.hash(this.policyType, this.policyId, this.policyVersion);

			try
			{
				this.targetEvaluator = new TargetEvaluator(policyTarget, defaultXPathCompiler, expressionFactory);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this + ": Invalid Target", e);
			}

			final CombiningAlg<T> combiningAlg;
			try
			{
				combiningAlg = combiningAlgRegistry.getAlgorithm(combiningAlgId, combinedElementClass);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this + ": Unknown combining algorithm ID = " + combiningAlgId, e);
			}

			this.combiningAlgEvaluator = combiningAlg.getInstance(combinerParameters, combinedElements);
			try
			{
				this.pepActionExps = PolicyPepActionExpressionsEvaluator.getInstance(obligationExps, adviceExps, defaultXPathCompiler, expressionFactory);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this + ": Invalid AttributeAssignmentExpressions", e);
			}

			this.localVariableIds = localVariableIds == null ? Collections.<String> emptySet() : localVariableIds;

			/*
			 * Define keys for caching the result of #evaluate() in the request context (see Object#toString())
			 */
			this.requestScopedEvalResultsCacheKey = this.getClass().getName() + '@' + Integer.toHexString(hashCode());

		}

		/**
		 * Policy(Set) evaluation which option to skip Target evaluation. The option is to be used by Only-one-applicable algorithm with value 'true', after calling
		 * {@link TopLevelPolicyElementEvaluator#isApplicable(EvaluationContext)} in particular.
		 * 
		 * @param context
		 *            evaluation context
		 * @param skipTarget
		 *            whether to evaluate the Target.
		 * @return decision result
		 */
		@Override
		public final DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
		{
			/*
			 * check whether the result is already cached in the evaluation context
			 */
			final Object cachedValue = context.getOther(this.requestScopedEvalResultsCacheKey);
			final EvalResults cachedResults;
			if (cachedValue instanceof EvalResults)
			{
				cachedResults = (EvalResults) cachedValue;
			} else
			{
				cachedResults = null;
			}

			DecisionResult newResult = null;

			try
			{
				final DecisionResult algResult;
				if (skipTarget)
				{
					// check cached result
					if (cachedResults != null && cachedResults.resultWithoutTarget != null)
					{
						LOGGER.debug("{} -> {} (result from context cache with skipTarget=true)", policyId, cachedResults.resultWithoutTarget);
						return cachedResults.resultWithoutTarget;
					}

					// evaluate with combining algorithm
					algResult = combiningAlgEvaluator.eval(context);
					LOGGER.debug("{}/Algorithm -> {}", policyId, algResult);
				} else
				{
					if (cachedResults != null && cachedResults.resultWithTarget != null)
					{
						LOGGER.debug("{} -> {} (result from context cache with skipTarget=false)", policyId, cachedResults.resultWithTarget);
						return cachedResults.resultWithTarget;
					}

					// evaluate target
					IndeterminateEvaluationException targetMatchIndeterminateException = null;
					try
					{
						if (!isApplicable(context))
						{
							LOGGER.debug("{} -> NotApplicable", policyId);
							newResult = BaseDecisionResult.NOT_APPLICABLE;
							return newResult;
						}
					} catch (IndeterminateEvaluationException e)
					{
						targetMatchIndeterminateException = e;
						/*
						 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than error)
						 */
						LOGGER.info("{}/Target -> Indeterminate", policyId, e);
					}

					// evaluate with combining algorithm
					algResult = combiningAlgEvaluator.eval(context);
					LOGGER.debug("{}/Algorithm -> {}", policyId, algResult);

					if (targetMatchIndeterminateException != null)
					{
						// Target is indeterminate
						/*
						 * Implement Extended Indeterminate according to table 7 of section 7.14 (XACML 3.0 Core). If the combining alg value is Indeterminate, use its extended Indeterminate value as
						 * this evaluation result's extended Indeterminate value; else (Permit or Deny) as our extended indeterminate value (part between {} in XACML notation).
						 */
						final DecisionType algDecision = algResult.getDecision();
						switch (algDecision)
						{
						case NOT_APPLICABLE:
							newResult = algResult;
							break;
						case PERMIT:
						case DENY:
							newResult = new BaseDecisionResult(targetMatchIndeterminateException.getStatus(), algDecision);
							break;
						default: // INDETERMINATE
							newResult = new BaseDecisionResult(targetMatchIndeterminateException.getStatus(), algResult.getExtendedIndeterminate());
							break;
						}

						return newResult;
					}
				}

				// target match not indeterminate
				final DecisionType algResultDecision = algResult.getDecision();
				final PepActions pepActions;
				final List<JAXBElement<IdReferenceType>> applicablePolicyIdList;
				switch (algResultDecision)
				{
				case NOT_APPLICABLE:
					newResult = algResult;
					return newResult;
				case INDETERMINATE:
					if (context.isApplicablePolicyIdListReturned())
					{
						applicablePolicyIdList = algResult.getApplicablePolicyIdList();
						applicablePolicyIdList.add(this.refToSelf);
						// PEP actions not returned with Indeterminate
						pepActions = null;
					} else
					{
						newResult = algResult;
						return newResult;
					}
					break;
				default:
					if (context.isApplicablePolicyIdListReturned())
					{
						applicablePolicyIdList = algResult.getApplicablePolicyIdList();
						applicablePolicyIdList.add(this.refToSelf);
					} else
					{
						// applicable policy identifiers are NOT requested
						applicablePolicyIdList = null;
					}

					if (pepActionExps == null)
					{
						if (applicablePolicyIdList == null)
						{
							/*
							 * nothing to add to the combining alg eval result
							 */
							newResult = algResult;
							return newResult;
						}

						/*
						 * no PEP actions on this Policy(Set) explicitly but maybe on the children evaluated by combining algorithm so we take the PEP actions resulting of algorithm evaluation as the
						 * result PEP actions
						 */
						pepActions = algResult.getPepActions();
					} else
					{
						// pepActionExps != null -> evaluate pepActionExps

						/*
						 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute evaluates to "Indeterminate", then the
						 * whole rule, policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
						 */
						try
						{
							pepActions = pepActionExps.evaluate(algResult, context);
						} catch (IndeterminateEvaluationException e)
						{
							/*
							 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than
							 * error)
							 */
							LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", policyId, e);
							newResult = new BaseDecisionResult(e.getStatus(), algResultDecision, applicablePolicyIdList);
							return newResult;
						}
					}
				}

				newResult = new BaseDecisionResult(algResult, pepActions, applicablePolicyIdList);
				return newResult;
			} finally
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
						final EvalResults newCachedResults = new EvalResults(this.policyId);
						newCachedResults.setResult(skipTarget, newResult);
						context.putOther(this.requestScopedEvalResultsCacheKey, newCachedResults);
					} else
					{
						cachedResults.setResult(skipTarget, newResult);
					}
				}
			}
		}

		@Override
		public final boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
		{
			/*
			 * Null or empty Target matches all
			 */
			if (targetEvaluator == null)
			{
				LOGGER.debug("{}/Target (none/empty) -> Match", policyId);
				return true;
			}

			final boolean isMatched = targetEvaluator.match(context);
			LOGGER.debug("{}/Target -> Match={}", policyId, isMatched);
			return isMatched;
		}

		@Override
		public final DecisionResult evaluate(EvaluationContext context)
		{
			return evaluate(context, false);
		}

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
		public final boolean equals(Object obj)
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
			return this.policyType.equals(other.getPolicyElementType()) && this.policyId.equals(other.getPolicyId()) && this.policyVersion.equals(other.getPolicyVersion());
		}

		@Override
		public final String getPolicyId()
		{
			return this.policyId;
		}

		@Override
		public final PolicyVersion getPolicyVersion()
		{
			return this.policyVersion;
		}

		@Override
		public final TopLevelPolicyElementType getPolicyElementType()
		{
			return this.policyType;
		}

	}

	private static final class StaticBaseTopLevelPolicyElementEvaluator<T extends Decidable> extends BaseTopLevelPolicyElementEvaluator<T> implements StaticTopLevelPolicyElementEvaluator
	{
		private static final IllegalArgumentException NULL_EXTRA_POLICY_METADATA_ARGUMENT_EXCEPTION = new IllegalArgumentException("Invalid extraPolicyElementMetadata arg: undefined");
		private transient final ExtraPolicyMetadata extraPolicyMetadata;

		private static ExtraPolicyMetadata validate(ExtraPolicyMetadata extraPolicyMetadata)
		{
			if (extraPolicyMetadata == null)
			{
				throw NULL_EXTRA_POLICY_METADATA_ARGUMENT_EXCEPTION;
			}

			return extraPolicyMetadata;
		}

		private StaticBaseTopLevelPolicyElementEvaluator(Class<T> combinedElementClass, String policyId, ExtraPolicyMetadata extraPolicyMetadata, Target policyTarget, String combiningAlgId,
				List<T> combinedElements, List<CombiningAlgParameter<? extends T>> combinerParameters, ObligationExpressions obligationExps, AdviceExpressions adviceExps,
				Set<String> localVariableIds, XPathCompiler defaultXPathCompiler, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
		{
			super(combinedElementClass, policyId, validate(extraPolicyMetadata).getVersion(), policyTarget, combiningAlgId, combinedElements, combinerParameters, obligationExps, adviceExps,
					localVariableIds, defaultXPathCompiler, expressionFactory, combiningAlgRegistry);
			this.extraPolicyMetadata = extraPolicyMetadata;
		}

		@Override
		public ExtraPolicyMetadata getExtraPolicyMetadata()
		{
			return this.extraPolicyMetadata;
		}

		@Override
		public ExtraPolicyMetadata getExtraPolicyMetadata(EvaluationContext evaluationCtx) throws IndeterminateEvaluationException
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

		protected final String refPolicyId;
		// and version constraints on this reference
		protected final VersionPatterns versionConstraints;
		protected final TopLevelPolicyElementType referredPolicyType;
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
		private static String toString(TopLevelPolicyElementType refPolicyType, String policyRefId, VersionPatterns versionConstraints)
		{
			return refPolicyType + "IdReference[Id=" + policyRefId + ", " + versionConstraints + "]";
		}

		private PolicyRefEvaluator(TopLevelPolicyElementType refPolicyType, String policyId, VersionPatterns versionConstraints)
		{
			assert policyId != null && refPolicyType != null;
			this.refPolicyId = policyId;
			this.versionConstraints = versionConstraints;
			this.referredPolicyType = refPolicyType;
			this.toString = toString(referredPolicyType, policyId, versionConstraints);
			this.hashCode = Objects.hash(this.referredPolicyType, this.refPolicyId, this.versionConstraints);
		}

		@Override
		public final DecisionResult evaluate(EvaluationContext context)
		{
			return evaluate(context, false);
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
		public final TopLevelPolicyElementType getPolicyElementType()
		{
			return this.referredPolicyType;
		}

		@Override
		public final String getPolicyId()
		{
			// IdReference
			return this.refPolicyId;
		}

		@Override
		public final int hashCode()
		{
			return hashCode;
		}

		@Override
		public final boolean equals(Object obj)
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
			return this.referredPolicyType.equals(other.referredPolicyType) && this.refPolicyId.equals(other.refPolicyId)
					&& (this.versionConstraints == null && other.versionConstraints == null || this.versionConstraints != null && this.versionConstraints.equals(other.versionConstraints));
		}

	}

	private static void updateStaticRefPolicies(String policyFriendlyId, Map<String, PolicyVersion> staticRefPoliciesToUpdate, String newRefPolicyId, PolicyVersion newRefPolicyVersion)
	{
		assert staticRefPoliciesToUpdate != null && newRefPolicyId != null && newRefPolicyVersion != null;

		final PolicyVersion otherVersion = staticRefPoliciesToUpdate.put(newRefPolicyId, newRefPolicyVersion);
		if (otherVersion != null && !otherVersion.equals(newRefPolicyVersion))
		{
			throw new IllegalArgumentException(policyFriendlyId + ": policy references to same policy ID (" + newRefPolicyId + ") but different versions (" + otherVersion + " and "
					+ newRefPolicyVersion + ") - from different places - is not allowed");
		}

	}

	private static void updateStaticRefPolicies(String policyFriendlyId, Map<String, PolicyVersion> staticRefPoliciesToUpdate, Map<String, PolicyVersion> newRefPolicies)
	{
		assert staticRefPoliciesToUpdate != null && newRefPolicies != null;

		for (final Entry<String, PolicyVersion> newRefPolicyEntry : newRefPolicies.entrySet())
		{
			updateStaticRefPolicies(policyFriendlyId, staticRefPoliciesToUpdate, newRefPolicyEntry.getKey(), newRefPolicyEntry.getValue());
		}
	}

	/**
	 * 
	 * @param referredPolicy
	 * @return extra policy metadata
	 * @throws IndeterminateEvaluationException
	 *             if the extra policy metadata of {@code referredPolicy} could not be determined in {@code evalCtx} (with
	 *             {@link TopLevelPolicyElementEvaluator#getExtraPolicyMetadata(EvaluationContext)} )
	 */
	private static ExtraPolicyMetadata getPolicyRefExtraMetadata(TopLevelPolicyElementEvaluator referredPolicy, EvaluationContext evalCtx) throws IndeterminateEvaluationException
	{
		final ExtraPolicyMetadata referredPolicyMetadata = referredPolicy.getExtraPolicyMetadata(evalCtx);

		final Map<String, PolicyVersion> refPolicies = new HashMap<>();
		refPolicies.putAll(referredPolicyMetadata.getRefPolicies());

		final Map<String, PolicyVersion> refPolicySets = new HashMap<>();
		refPolicySets.putAll(referredPolicyMetadata.getRefPolicySets());

		if (referredPolicy.getPolicyElementType() == TopLevelPolicyElementType.POLICY)
		{
			refPolicies.put(referredPolicy.getPolicyId(), referredPolicyMetadata.getVersion());
		} else
		{
			refPolicySets.put(referredPolicy.getPolicyId(), referredPolicyMetadata.getVersion());
		}

		final List<String> thisLongestPolicyRefChain = new ArrayList<>();
		thisLongestPolicyRefChain.add(referredPolicy.getPolicyId());
		thisLongestPolicyRefChain.addAll(referredPolicyMetadata.getLongestPolicyRefChain());

		return new BaseExtraPolicyMetadata(referredPolicyMetadata.getVersion(), refPolicies, refPolicySets, thisLongestPolicyRefChain);
	}

	private static abstract class ExtraPolicySetMetadataProvider
	{
		protected final String friendlyId;
		protected final PolicyVersion version;
		private transient final int hashCode;

		private ExtraPolicySetMetadataProvider(String policyFriendlyId, PolicyVersion policyVersion)
		{
			assert policyFriendlyId != null && policyVersion != null;
			this.friendlyId = policyFriendlyId;
			this.version = policyVersion;
			this.hashCode = Objects.hash(this.friendlyId, this.version);
		}

		private final PolicyVersion getVersion()
		{
			return this.version;
		}

		@Override
		public final int hashCode()
		{
			return hashCode;
		}

		@Override
		public final boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof ExtraPolicySetMetadataProvider))
			{
				return false;
			}

			final ExtraPolicySetMetadataProvider other = (ExtraPolicySetMetadataProvider) obj;
			return this.friendlyId.equals(other.friendlyId) && this.version.equals(other.version);
		}

	}

	private static final class StaticExtraPolicySetMetadataProvider extends ExtraPolicySetMetadataProvider
	{
		private final Map<String, PolicyVersion> refPolicies = new HashMap<>();
		private final Map<String, PolicyVersion> refPolicySets = new HashMap<>();
		private final List<String> longestPolicyRefChain = new ArrayList<>();

		private StaticExtraPolicySetMetadataProvider(String policyFriendlyId, PolicyVersion version)
		{
			super(policyFriendlyId, version);
		}

		private ExtraPolicyMetadata getMetadata()
		{
			return new BaseExtraPolicyMetadata(version, refPolicies, refPolicySets, longestPolicyRefChain);
		}

		private void updateMetadata(ExtraPolicyMetadata childMetadata)
		{
			updateStaticRefPolicies(friendlyId, refPolicies, childMetadata.getRefPolicies());
			/*
			 * update longest policy ref chain depending on the length of the longest in this child policy element
			 */
			final List<String> childLongestPolicyRefChain = childMetadata.getLongestPolicyRefChain();
			if (childLongestPolicyRefChain.size() > longestPolicyRefChain.size())
			{
				longestPolicyRefChain.clear();
				longestPolicyRefChain.addAll(childLongestPolicyRefChain);
			}

			updateStaticRefPolicies(friendlyId, refPolicySets, childMetadata.getRefPolicySets());
		}
	}

	private static final class DynamicExtraPolicySetMetadataProvider extends ExtraPolicySetMetadataProvider
	{
		private static final class GetMetadataResult
		{
			private final ExtraPolicyMetadata extraMetadata;
			private final IndeterminateEvaluationException exception;

			private GetMetadataResult(ExtraPolicyMetadata metadata) throws IndeterminateEvaluationException
			{
				assert metadata != null;
				this.exception = null;
				this.extraMetadata = metadata;
			}

			private GetMetadataResult(IndeterminateEvaluationException exception)
			{
				assert exception != null;
				this.exception = exception;
				this.extraMetadata = null;
			}
		}

		private final List<PolicyEvaluator> childPolicySetElementsOrRefs = new ArrayList<>();

		private transient final String requestScopedCacheKey;

		private DynamicExtraPolicySetMetadataProvider(String policyFriendlyId, PolicyVersion version)
		{
			super(policyFriendlyId, version);
			/*
			 * Define a key for caching the result of #getMetadata() in the request context (see Object#toString())
			 */
			this.requestScopedCacheKey = this.getClass().getName() + '@' + Integer.toHexString(hashCode());
		}

		private void addChildPolicySetElementOrRef(PolicyEvaluator childElement)
		{
			childPolicySetElementsOrRefs.add(childElement);
		}

		private ExtraPolicyMetadata getMetadata(EvaluationContext evalCtx) throws IndeterminateEvaluationException
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
			final Map<String, PolicyVersion> refPolicies = new HashMap<>();
			final Map<String, PolicyVersion> refPolicySets = new HashMap<>();
			final List<String> longestPolicyRefChain = new ArrayList<>();
			for (final PolicyEvaluator policyRef : childPolicySetElementsOrRefs)
			{
				final ExtraPolicyMetadata extraMetadata = policyRef.getExtraPolicyMetadata(evalCtx);
				updateStaticRefPolicies(friendlyId, refPolicies, extraMetadata.getRefPolicies());
				updateStaticRefPolicies(friendlyId, refPolicySets, extraMetadata.getRefPolicySets());
				final List<String> policyRefLongestPolicyRefChain = extraMetadata.getLongestPolicyRefChain();
				if (policyRefLongestPolicyRefChain.size() > longestPolicyRefChain.size())
				{
					longestPolicyRefChain.clear();
					longestPolicyRefChain.addAll(policyRefLongestPolicyRefChain);
				}
			}

			final ExtraPolicyMetadata extraMetadata = new BaseExtraPolicyMetadata(version, refPolicies, refPolicySets, longestPolicyRefChain);
			final GetMetadataResult newCachedValue = new GetMetadataResult(extraMetadata);
			evalCtx.putOther(requestScopedCacheKey, newCachedValue);
			return extraMetadata;
		}
	}

	private static final class DynamicPolicySetEvaluator extends BaseTopLevelPolicyElementEvaluator<PolicyEvaluator>
	{
		private static final IllegalArgumentException NULL_EXTRA_POLICYSET_METADATA_PROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Invalid extraPolicyMetadataProvider arg: undefined");
		private transient final DynamicExtraPolicySetMetadataProvider extraPolicyMetadataProvider;

		private static ExtraPolicySetMetadataProvider validate(ExtraPolicySetMetadataProvider extraPolicyMetadataProvider)
		{
			if (extraPolicyMetadataProvider == null)
			{
				throw NULL_EXTRA_POLICYSET_METADATA_PROVIDER_ARGUMENT_EXCEPTION;
			}

			return extraPolicyMetadataProvider;
		}

		private DynamicPolicySetEvaluator(String policyId, DynamicExtraPolicySetMetadataProvider extraPolicyMetadataProvider, Target policyTarget, String combiningAlgId,
				List<PolicyEvaluator> combinedElements, List<CombiningAlgParameter<? extends PolicyEvaluator>> combinerParameters, ObligationExpressions obligationExps, AdviceExpressions adviceExps,
				Set<String> localVariableIds, XPathCompiler defaultXPathCompiler, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
		{
			super(PolicyEvaluator.class, policyId, validate(extraPolicyMetadataProvider).getVersion(), policyTarget, combiningAlgId, combinedElements, combinerParameters, obligationExps, adviceExps,
					localVariableIds, defaultXPathCompiler, expressionFactory, combiningAlgRegistry);
			this.extraPolicyMetadataProvider = extraPolicyMetadataProvider;
		}

		@Override
		public ExtraPolicyMetadata getExtraPolicyMetadata(EvaluationContext evaluationCtx) throws IndeterminateEvaluationException
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
		private transient final ExtraPolicyMetadata extraMetadata;

		private static TopLevelPolicyElementType validate(TopLevelPolicyElementEvaluator referredPolicy)
		{
			return referredPolicy.getPolicyElementType();
		}

		private StaticPolicyRefEvaluator(StaticTopLevelPolicyElementEvaluator referredPolicy, VersionPatterns refVersionConstraints)
		{
			super(validate(referredPolicy), referredPolicy.getPolicyId(), refVersionConstraints);
			this.referredPolicy = referredPolicy;
			try
			{
				this.extraMetadata = getPolicyRefExtraMetadata(referredPolicy, null);
			} catch (IndeterminateEvaluationException e)
			{
				throw new RuntimeException(this + ": unexpected error: could not get extra metadata of statically defined policy: " + referredPolicy, e);
			}
		}

		@Override
		public DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
		{
			return referredPolicy.evaluate(context, skipTarget);
		}

		@Override
		public boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
		{
			try
			{
				return referredPolicy.isApplicable(context);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Error checking whether Policy(Set) referenced by " + this, e.getStatusCode() + " is applicable to the request context", e);
			}
		}

		@Override
		public ExtraPolicyMetadata getExtraPolicyMetadata()
		{
			return this.extraMetadata;
		}

		@Override
		public ExtraPolicyMetadata getExtraPolicyMetadata(EvaluationContext evaluationCtx)
		{
			return this.extraMetadata;
		}

	}

	private static final class DynamicPolicyRefEvaluator extends PolicyRefEvaluator
	{

		private static final class RefResolvedResult
		{

			private final TopLevelPolicyElementEvaluator resolvedPolicy;
			private final ExtraPolicyMetadata extraMetadata;
			private final IndeterminateEvaluationException exception;

			private RefResolvedResult(TopLevelPolicyElementEvaluator policy, EvaluationContext evalCtx) throws IndeterminateEvaluationException
			{
				assert policy != null && evalCtx != null;
				this.exception = null;
				this.resolvedPolicy = policy;
				this.extraMetadata = getPolicyRefExtraMetadata(policy, evalCtx);
			}

			private RefResolvedResult(IndeterminateEvaluationException exception)
			{
				assert exception != null;
				this.exception = exception;
				this.resolvedPolicy = null;
				this.extraMetadata = null;
			}
		}

		private static final Logger LOGGER = LoggerFactory.getLogger(DynamicPolicyRefEvaluator.class);

		// this policyProvider to use in finding the referenced policy
		private final RefPolicyProvider refPolicyProvider;

		private final String requestScopedCacheKey;

		/*
		 * Chain of Policy Reference leading from root policy down to this reference (excluded) (Do not use a Queue as it is FIFO, and we need LIFO and iteration in order of insertion, so different
		 * from Collections.asLifoQueue(Deque) as well.)
		 */
		private final Deque<String> ancestorPolicyRefChain;

		private DynamicPolicyRefEvaluator(TopLevelPolicyElementType policyReferenceType, String policyId, VersionPatterns versionConstraints, RefPolicyProvider refPolicyProvider,
				Deque<String> ancestorPolicyRefChain)
		{
			super(policyReferenceType, policyId, versionConstraints);
			assert refPolicyProvider != null;
			this.refPolicyProvider = refPolicyProvider;
			this.ancestorPolicyRefChain = ancestorPolicyRefChain;
			/*
			 * define a key for caching the resolved policy in the request context (see Object#toString())
			 */
			this.requestScopedCacheKey = this.getClass().getName() + '@' + Integer.toHexString(hashCode());
		}

		/**
		 * Resolves this to the actual Policy
		 * 
		 * @throws IllegalArgumentException
		 *             Error parsing the policy referenced by this. The referenced policy may be parsed on the fly, when calling this method.
		 * @throws IndeterminateEvaluationException
		 *             if error determining the policy referenced by this, e.g. if more than one policy is found
		 */
		private RefResolvedResult resolve(EvaluationContext evalCtx) throws IndeterminateEvaluationException, IllegalArgumentException
		{
			// check whether the policy was already resolved in the same context
			final Object cachedValue = evalCtx.getOther(requestScopedCacheKey);
			if (cachedValue instanceof RefResolvedResult)
			{
				final RefResolvedResult result = (RefResolvedResult) cachedValue;
				if (result.exception == null)
				{
					return result;
				}

				throw result.exception;
			}

			/*
			 * cachedValue == null, i.e. ref resolution result not cached yet; or cachedValue of the wrong type (unexpected), so we just overwrite with proper type
			 */
			try
			{
				final TopLevelPolicyElementEvaluator policy = refPolicyProvider.get(this.referredPolicyType, this.refPolicyId, this.versionConstraints, ancestorPolicyRefChain, evalCtx);
				final RefResolvedResult newCacheValue = new RefResolvedResult(policy, evalCtx);
				evalCtx.putOther(requestScopedCacheKey, newCacheValue);
				return newCacheValue;
			} catch (IllegalArgumentException e)
			{
				final IndeterminateEvaluationException resolutionException = new IndeterminateEvaluationException("Error resolving " + this + " to the policy to evaluate in the request context",
						StatusHelper.STATUS_PROCESSING_ERROR, e);
				final RefResolvedResult newCacheValue = new RefResolvedResult(resolutionException);
				evalCtx.putOther(requestScopedCacheKey, newCacheValue);
				throw resolutionException;
			} catch (IndeterminateEvaluationException e)
			{
				final RefResolvedResult newCacheValue = new RefResolvedResult(e);
				evalCtx.putOther(requestScopedCacheKey, newCacheValue);
				throw e;
			}
		}

		@Override
		public DecisionResult evaluate(EvaluationContext evalCtx, boolean skipTarget)
		{
			// we must have found a policy
			final RefResolvedResult refResolvedResult;
			try
			{
				refResolvedResult = resolve(evalCtx);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.info("", e);
				return new BaseDecisionResult(e.getStatus());
			}

			return refResolvedResult.resolvedPolicy.evaluate(evalCtx, skipTarget);
		}

		@Override
		public boolean isApplicable(EvaluationContext evalCtx) throws IndeterminateEvaluationException
		{
			final RefResolvedResult refResolvedResult = resolve(evalCtx);
			return refResolvedResult.resolvedPolicy.isApplicable(evalCtx);
		}

		@Override
		public ExtraPolicyMetadata getExtraPolicyMetadata(EvaluationContext evalCtx) throws IndeterminateEvaluationException
		{
			final RefResolvedResult refResolvedResult = resolve(evalCtx);
			return refResolvedResult.extraMetadata;
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
	public static StaticTopLevelPolicyElementEvaluator getInstance(Policy policyElement, XPathCompiler parentDefaultXPathCompiler, Map<String, String> namespacePrefixesByURI,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws IllegalArgumentException
	{
		if (policyElement == null)
		{
			throw NULL_XACML_POLICY_ARG_EXCEPTION;
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
		} else
		{
			try
			{
				defaultXPathCompiler = XMLUtils.newXPathCompiler(policyDefaults.getXPathVersion(), namespacePrefixesByURI);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(policyFriendlyId + ": Invalid PolicyDefaults/XPathVersion or XML namespace prefix/URI undefined", e);
			}

		}

		final List<RuleEvaluator> ruleEvaluators = new ArrayList<>();
		final List<CombiningAlgParameter<? extends RuleEvaluator>> ruleCombinerParameters = new ArrayList<>();

		/*
		 * Keep a copy of locally-defined variable IDs defined in this policy, to remove them from the global manager at the end of parsing this policy. They should not be visible outside the scope of
		 * this policy.
		 */
		final Set<String> localVariableIds = new HashSet<>();
		/*
		 * We keep a record of the size of the longest chain of VariableReference in this policy, and update it when a VariableDefinition occurs
		 */
		int sizeOfPolicyLongestVarRefChain = 0;
		/*
		 * Map to get rules by their ID so that we can resolve rules associated with CombinerParameters
		 */
		final Map<String, RuleEvaluator> rulesById = new HashMap<>();
		int childIndex = 0;
		for (final Object policyChildElt : policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions())
		{
			if (policyChildElt instanceof RuleCombinerParameters)
			{
				final String combinedRuleId = ((RuleCombinerParameters) policyChildElt).getRuleIdRef();
				final RuleEvaluator combinedRule = rulesById.get(combinedRuleId);
				if (combinedRule == null)
				{
					throw new IllegalArgumentException(policyFriendlyId + ":  invalid RuleCombinerParameters: referencing undefined child Rule #" + combinedRuleId
							+ " (no such rule defined before this element)");
				}

				final BaseCombiningAlgParameter<RuleEvaluator> combinerElt;
				try
				{
					combinerElt = new BaseCombiningAlgParameter<>(combinedRule, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (RuleCombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof CombinerParametersType)
			{
				/*
				 * CombinerParameters that is not RuleCombinerParameters already tested before
				 */
				final BaseCombiningAlgParameter<RuleEvaluator> combinerElt;
				try
				{
					combinerElt = new BaseCombiningAlgParameter<>(null, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (CombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof VariableDefinition)
			{
				final VariableDefinition varDef = (VariableDefinition) policyChildElt;
				final Deque<String> varDefLongestVarRefChain = new ArrayDeque<>();
				final VariableReference<?> var;
				try
				{
					var = expressionFactory.addVariable(varDef, defaultXPathCompiler, varDefLongestVarRefChain);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": invalid child #" + childIndex + " (VariableDefinition)", e);
				}

				if (var != null)
				{
					/*
					 * Conflicts can occur between variables defined in this policy but also with others already in a wider scope, i.e. defined in parent/ancestor policy
					 */
					throw new IllegalArgumentException(policyFriendlyId + ": Duplicable VariableDefinition for VariableId=" + var.getVariableId());
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
			} else if (policyChildElt instanceof Rule)
			{
				final RuleEvaluator ruleEvaluator;
				try
				{
					ruleEvaluator = new RuleEvaluator((Rule) policyChildElt, defaultXPathCompiler, expressionFactory);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": Error parsing child #" + childIndex + " (Rule)", e);
				}

				rulesById.put(ruleEvaluator.getRuleId(), ruleEvaluator);
				ruleEvaluators.add(ruleEvaluator);
			}

			childIndex++;
		}

		final ExtraPolicyMetadata extraPolicyMetadata = new BaseExtraPolicyMetadata(policyVersion, new HashMap<String, PolicyVersion>(), new HashMap<String, PolicyVersion>(), new ArrayList<String>());
		final StaticTopLevelPolicyElementEvaluator policyEvaluator = new StaticBaseTopLevelPolicyElementEvaluator<>(RuleEvaluator.class, policyId, extraPolicyMetadata, policyElement.getTarget(),
				policyElement.getRuleCombiningAlgId(), ruleEvaluators, ruleCombinerParameters, policyElement.getObligationExpressions(), policyElement.getAdviceExpressions(),
				Collections.<String> unmodifiableSet(localVariableIds), defaultXPathCompiler, expressionFactory, combiningAlgRegistry);

		/*
		 * We are done parsing expressions in this policy, including VariableReferences, it's time to remove variables scoped to this policy from the variable manager
		 */
		for (final String varId : localVariableIds)
		{
			expressionFactory.removeVariable(varId);
		}

		return policyEvaluator;
	}

	private static abstract class PolicyRefEvaluatorFactory<INSTANCE extends PolicyRefEvaluator>
	{
		private PolicyRefEvaluatorFactory(RefPolicyProvider refPolicyProvider)
		{
			if (refPolicyProvider == null)
			{
				throw UNDEF_REF_POLICY_PROVIDER_EXCEPTION;
			}
		}

		protected abstract INSTANCE getInstance(TopLevelPolicyElementType refPolicyType, String idRefPolicyId, VersionPatterns versionConstraints, Deque<String> parentPolicySetRefChain);
	}

	private static final class StaticPolicyRefEvaluatorFactory extends PolicyRefEvaluatorFactory<StaticPolicyRefEvaluator>
	{
		private final StaticRefPolicyProvider refPolicyProvider;

		private StaticPolicyRefEvaluatorFactory(StaticRefPolicyProvider refPolicyProvider)
		{
			super(refPolicyProvider);
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		protected StaticPolicyRefEvaluator getInstance(TopLevelPolicyElementType refPolicyType, String policyId, VersionPatterns versionConstraints, Deque<String> parentPolicySetRefChain)
		{
			final StaticTopLevelPolicyElementEvaluator policy;
			try
			{
				policy = refPolicyProvider.get(refPolicyType, policyId, versionConstraints, parentPolicySetRefChain);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IllegalArgumentException("Error resolving statically or parsing " + PolicyRefEvaluator.toString(refPolicyType, policyId, versionConstraints)
						+ " into its referenced policy (via static policy provider)", e);
			}

			if (policy == null)
			{
				throw new IllegalArgumentException("No " + refPolicyType + " matching reference: id = " + policyId + ", " + versionConstraints);
			}

			return new StaticPolicyRefEvaluator(policy, versionConstraints);
		}
	}

	private static final class DynamicPolicyRefEvaluatorFactory extends PolicyRefEvaluatorFactory<PolicyRefEvaluator>
	{
		private final RefPolicyProvider refPolicyProvider;

		private DynamicPolicyRefEvaluatorFactory(RefPolicyProvider refPolicyProvider)
		{
			super(refPolicyProvider);
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		protected PolicyRefEvaluator getInstance(TopLevelPolicyElementType refPolicyType, String idRefPolicyId, VersionPatterns versionConstraints, Deque<String> parentPolicySetRefChain)
		{
			// dynamic reference resolution
			return new DynamicPolicyRefEvaluator(refPolicyType, idRefPolicyId, versionConstraints, refPolicyProvider, parentPolicySetRefChain);
		}
	}

	private static <PRE extends PolicyRefEvaluator> PRE getInstanceGeneric(PolicyRefEvaluatorFactory<PRE> policyRefEvaluatorFactory, TopLevelPolicyElementType refPolicyType, IdReferenceType idRef,
			Deque<String> parentPolicySetRefChain) throws IllegalArgumentException
	{

		final VersionPatterns versionConstraints = new VersionPatterns(idRef.getVersion(), idRef.getEarliestVersion(), idRef.getLatestVersion());
		/*
		 * REMINDER: parentPolicySetRefChain is handled/updated by the refPolicyProvider. So do not modify it here, just pass the parameter. modify it here.
		 */
		return policyRefEvaluatorFactory.getInstance(refPolicyType, idRef.getValue(), versionConstraints, parentPolicySetRefChain);
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
	 * @param parentPolicySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the reference identified here by {@code idRef} (exclusive): PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n -> {@code idRef}.
	 *            This allows to detect circular references and validate the size of the chain against the max depth enforced by {@code policyProvider}. This may be null if no ancestor, e.g. a
	 *            PolicySetIdReference in a top-level PolicySet. Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is
	 *            not allowed, no matter what the version is.
	 * @return instance instance of PolicyReference
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code refPolicyProvider} undefined, or there is no policy of type {@code refPolicyType} matching {@code idRef} to be found by {@code refPolicyProvider}, or PolicySetIdReference
	 *             loop detected or PolicySetIdReference depth exceeds the max enforced by {@code policyProvider}
	 */
	public static PolicyRefEvaluator getInstance(TopLevelPolicyElementType refPolicyType, IdReferenceType idRef, RefPolicyProvider refPolicyProvider, Deque<String> parentPolicySetRefChain)
			throws IllegalArgumentException
	{
		final PolicyRefEvaluatorFactory<? extends PolicyRefEvaluator> factory = refPolicyProvider instanceof StaticRefPolicyProvider ? new StaticPolicyRefEvaluatorFactory(
				(StaticRefPolicyProvider) refPolicyProvider) : new DynamicPolicyRefEvaluatorFactory(refPolicyProvider);
		return getInstanceGeneric(factory, refPolicyType, idRef, parentPolicySetRefChain);
	}

	/**
	 * Instantiates Static Policy(Set) Reference evaluator from XACML Policy(Set)IdReference, "static" meaning that given {@code idRef} and {@code refPolicyType}, the returned policy is always the sam
	 * statically defined policy
	 *
	 * @param idRef
	 *            Policy(Set)IdReference
	 * @param refPolicyProvider
	 *            Policy(Set)IdReference resolver/Provider
	 * @param refPolicyType
	 *            type of policy referenced, i.e. whether it refers to Policy or PolicySet
	 * @param parentPolicySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the reference identified here by {@code idRef} (exclusive): PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n -> {@code idRef}.
	 *            This allows to detect circular references and validate the size of the chain against the max depth enforced by {@code policyProvider}. This may be null if no ancestor, e.g. a
	 *            PolicySetIdReference in a top-level PolicySet. Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is
	 *            not allowed, no matter what the version is.
	 * @return instance instance of PolicyReference
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code refPolicyProvider} undefined, or there is no policy of type {@code refPolicyType} matching {@code idRef} to be found by {@code refPolicyProvider}, or PolicySetIdReference
	 *             loop detected or PolicySetIdReference depth exceeds the max enforced by {@code policyProvider}
	 */
	public static StaticPolicyRefEvaluator getInstanceStatic(TopLevelPolicyElementType refPolicyType, IdReferenceType idRef, StaticRefPolicyProvider refPolicyProvider,
			Deque<String> parentPolicySetRefChain) throws IllegalArgumentException
	{
		final StaticPolicyRefEvaluatorFactory factory = new StaticPolicyRefEvaluatorFactory(refPolicyProvider);
		return getInstanceGeneric(factory, refPolicyType, idRef, parentPolicySetRefChain);
	}

	private static abstract class PolicySetElementEvaluatorFactory<INSTANCE extends TopLevelPolicyElementEvaluator, COMBINED_ELT extends PolicyEvaluator>
	{
		protected final XPathCompiler defaultXPathCompiler;
		protected final Map<String, String> namespacePrefixesByURI;
		protected final ExpressionFactory expressionFactory;
		protected final CombiningAlgRegistry combiningAlgorithmRegistry;
		protected final PolicyVersion policyVersion;
		protected final String policyFriendlyId;

		private PolicySetElementEvaluatorFactory(String policyId, String policyVersionId, DefaultsType policyDefaults, XPathCompiler parentDefaultXPathCompiler,
				Map<String, String> namespacePrefixesByURI, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgorithmRegistry)
		{
			assert policyId != null && policyVersionId != null && combiningAlgorithmRegistry != null;
			this.policyVersion = new PolicyVersion(policyVersionId);
			this.policyFriendlyId = "Policy[" + policyId + "#v" + policyVersionId + "]";
			/*
			 * Inherited PolicyDefaults is policyDefaults if not null, the parentPolicyDefaults otherwise
			 */
			if (policyDefaults == null)
			{
				defaultXPathCompiler = parentDefaultXPathCompiler;
			} else
			{
				try
				{
					defaultXPathCompiler = XMLUtils.newXPathCompiler(policyDefaults.getXPathVersion(), namespacePrefixesByURI);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyFriendlyId + ": Invalid PolicySetDefaults/XPathVersion or XML namespace prefix/URI undefined", e);
				}
			}

			this.namespacePrefixesByURI = namespacePrefixesByURI;
			this.expressionFactory = expressionFactory;
			this.combiningAlgorithmRegistry = combiningAlgorithmRegistry;
		}

		protected abstract COMBINED_ELT getChildPolicyEvaluator(int childIndex, Policy policyChildElt);

		protected abstract COMBINED_ELT getChildPolicySetEvaluator(int childIndex, PolicySet policySetChildElt, Deque<String> policySetRefChain);

		protected abstract COMBINED_ELT getChildPolicyRefEvaluator(int childIndex, TopLevelPolicyElementType refPolicyType, IdReferenceType idRef, Deque<String> policySetRefChain);

		protected abstract INSTANCE getInstance(String policyId, Target target, String policyCombiningAlgId, List<COMBINED_ELT> combinedElements,
				List<CombiningAlgParameter<? extends COMBINED_ELT>> policyCombinerParameters, ObligationExpressions obligationExpressions, AdviceExpressions adviceExpressions,
				Set<String> localVariableIDs);
	}

	private static final class StaticPolicySetElementEvaluatorFactory extends PolicySetElementEvaluatorFactory<StaticTopLevelPolicyElementEvaluator, StaticPolicyEvaluator>
	{
		private final StaticExtraPolicySetMetadataProvider extraMetadataProvider;
		private final StaticRefPolicyProvider refPolicyProvider;

		private StaticPolicySetElementEvaluatorFactory(String policyId, String policyVersionId, DefaultsType policyDefaults, StaticRefPolicyProvider refPolicyProvider,
				XPathCompiler parentDefaultXPathCompiler, Map<String, String> namespacePrefixesByURI, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgorithmRegistry)
		{
			super(policyId, policyVersionId, policyDefaults, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			this.extraMetadataProvider = new StaticExtraPolicySetMetadataProvider(policyFriendlyId, policyVersion);
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		public StaticPolicyEvaluator getChildPolicyEvaluator(int childIndex, Policy policyChildElt)
		{
			final StaticPolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstance(policyChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(extraMetadataProvider.friendlyId + ": invalid child #" + childIndex + " (Policy)", e);
			}

			return childElement;
		}

		@Override
		public StaticPolicyEvaluator getChildPolicySetEvaluator(int childIndex, PolicySet policySetChildElt, Deque<String> policySetRefChain)
		{
			final StaticPolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstanceStatic(policySetChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry, refPolicyProvider,
						policySetRefChain == null ? null : new ArrayDeque<>(policySetRefChain));
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(extraMetadataProvider.friendlyId + ": Invalid child #" + childIndex + " (PolicySet)", e);
			}

			/*
			 * This child PolicySet may have extra metadata such as nested policy references that we need to merge into the parent PolicySet's metadata
			 */
			extraMetadataProvider.updateMetadata(childElement.getExtraPolicyMetadata());
			return childElement;
		}

		@Override
		public StaticPolicyEvaluator getChildPolicyRefEvaluator(int childIndex, TopLevelPolicyElementType refPolicyType, IdReferenceType idRef, Deque<String> policySetRefChain)
		{
			if (refPolicyProvider == null)
			{
				throw new IllegalArgumentException(extraMetadataProvider.friendlyId + ": invalid child #" + childIndex
						+ " (PolicyIdReference): no refPolicyProvider (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
			}

			final StaticPolicyRefEvaluator childElement = PolicyEvaluators.getInstanceStatic(refPolicyType, idRef, refPolicyProvider, policySetRefChain);
			extraMetadataProvider.updateMetadata(childElement.getExtraPolicyMetadata());
			return childElement;
		}

		@Override
		public StaticTopLevelPolicyElementEvaluator getInstance(String policyId, Target policyTarget, String policyCombiningAlgId, List<StaticPolicyEvaluator> combinedElements,
				List<CombiningAlgParameter<? extends StaticPolicyEvaluator>> policyCombinerParameters, ObligationExpressions obligationExpressions, AdviceExpressions adviceExpressions,
				Set<String> localVariableIDs)
		{
			return new StaticBaseTopLevelPolicyElementEvaluator<>(StaticPolicyEvaluator.class, policyId, extraMetadataProvider.getMetadata(), policyTarget, policyCombiningAlgId, combinedElements,
					policyCombinerParameters, obligationExpressions, adviceExpressions, localVariableIDs, defaultXPathCompiler, expressionFactory, combiningAlgorithmRegistry);
		}
	}

	private static final class DynamicPolicySetElementEvaluatorFactory extends PolicySetElementEvaluatorFactory<TopLevelPolicyElementEvaluator, PolicyEvaluator>
	{
		private final DynamicExtraPolicySetMetadataProvider extraMetadataProvider;
		private final RefPolicyProvider refPolicyProvider;

		private DynamicPolicySetElementEvaluatorFactory(String policyId, String policyVersionId, DefaultsType policyDefaults, RefPolicyProvider refPolicyProvider,
				XPathCompiler parentDefaultXPathCompiler, Map<String, String> namespacePrefixesByURI, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgorithmRegistry)
		{
			super(policyId, policyVersionId, policyDefaults, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			this.extraMetadataProvider = new DynamicExtraPolicySetMetadataProvider(policyFriendlyId, policyVersion);
			this.refPolicyProvider = refPolicyProvider;
		}

		@Override
		public PolicyEvaluator getChildPolicyEvaluator(int childIndex, Policy policyChildElt)
		{
			final StaticPolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstance(policyChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(extraMetadataProvider.friendlyId + ": invalid child #" + childIndex + " (Policy)", e);
			}

			return childElement;
		}

		@Override
		public PolicyEvaluator getChildPolicySetEvaluator(int childIndex, PolicySet policySetChildElt, Deque<String> policySetRefChain)
		{
			final PolicyEvaluator childElement;
			try
			{
				childElement = PolicyEvaluators.getInstance(policySetChildElt, defaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry, refPolicyProvider,
						policySetRefChain == null ? null : new ArrayDeque<>(policySetRefChain));
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(extraMetadataProvider.friendlyId + ": Invalid child #" + childIndex + " (PolicySet)", e);
			}

			/*
			 * This child PolicySet may have extra metadata such as nested policy references that we need to merge into the parent PolicySet's metadata
			 */
			extraMetadataProvider.addChildPolicySetElementOrRef(childElement);
			return childElement;
		}

		@Override
		public PolicyEvaluator getChildPolicyRefEvaluator(int childIndex, TopLevelPolicyElementType refPolicyType, IdReferenceType idRef, Deque<String> policySetRefChain)
		{
			if (refPolicyProvider == null)
			{
				throw new IllegalArgumentException(extraMetadataProvider.friendlyId + ": invalid child #" + childIndex
						+ " (PolicyIdReference): no refPolicyProvider (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
			}

			final PolicyRefEvaluator childElement = PolicyEvaluators.getInstance(refPolicyType, idRef, refPolicyProvider, policySetRefChain);
			extraMetadataProvider.addChildPolicySetElementOrRef(childElement);
			return childElement;
		}

		@Override
		public TopLevelPolicyElementEvaluator getInstance(String policyId, Target policyTarget, String policyCombiningAlgId, List<PolicyEvaluator> combinedElements,
				List<CombiningAlgParameter<? extends PolicyEvaluator>> policyCombinerParameters, ObligationExpressions obligationExpressions, AdviceExpressions adviceExpressions,
				Set<String> localVariableIDs)
		{
			return new DynamicPolicySetEvaluator(policyId, extraMetadataProvider, policyTarget, policyCombiningAlgId, combinedElements, policyCombinerParameters, obligationExpressions,
					adviceExpressions, localVariableIDs, defaultXPathCompiler, expressionFactory, combiningAlgorithmRegistry);
		}
	}

	private static <TLPEE extends TopLevelPolicyElementEvaluator, COMBINED_ELT extends PolicyEvaluator> TLPEE getInstanceGeneric(
			PolicySetElementEvaluatorFactory<TLPEE, COMBINED_ELT> policyEvaluatorFactory, PolicySet policyElement, Deque<String> policySetRefChain) throws IllegalArgumentException
	{
		/*
		 * Why isn't there any VariableDefinition in XACML PolicySet like in Policy? If there were, we would keep a copy of variable IDs defined in this policy, to remove them from the global manager
		 * at the end of parsing this PolicySet. They should not be visible outside the scope of this. final Set<String> variableIds = new HashSet<>();
		 */

		/*
		 * Map to get child Policies by their ID so that we can resolve Policies associated with CombinerParameters
		 */
		final Map<String, COMBINED_ELT> childPoliciesById = new HashMap<>();

		/*
		 * Map to get child PolicySets by their ID so that we can resolve PolicySets associated with CombinerParameters
		 */
		final Map<String, COMBINED_ELT> childPolicySetsById = new HashMap<>();

		final List<COMBINED_ELT> combinedChildElements = new ArrayList<>();

		final List<CombiningAlgParameter<? extends COMBINED_ELT>> policyCombinerParameters = new ArrayList<>();
		int childIndex = 0;
		for (final Object policyChildElt : policyElement.getPolicySetsAndPoliciesAndPolicySetIdReferences())
		{
			if (policyChildElt instanceof PolicyCombinerParameters)
			{
				final String combinedPolicyId = ((PolicyCombinerParameters) policyChildElt).getPolicyIdRef();
				final COMBINED_ELT combinedPolicy = childPoliciesById.get(combinedPolicyId);
				if (combinedPolicy == null)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyFriendlyId + ":  invalid PolicyCombinerParameters: referencing undefined child Policy #" + combinedPolicyId
							+ " (no such policy defined before this element)");
				}

				final BaseCombiningAlgParameter<COMBINED_ELT> combinerElt;
				try
				{
					combinerElt = new BaseCombiningAlgParameter<>(combinedPolicy, ((CombinerParametersType) policyChildElt).getCombinerParameters(), policyEvaluatorFactory.expressionFactory,
							policyEvaluatorFactory.defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyFriendlyId + ": invalid child #" + childIndex + " (PolicyCombinerParameters)", e);
				}

				policyCombinerParameters.add(combinerElt);

			} else if (policyChildElt instanceof PolicySetCombinerParameters)
			{
				final String combinedPolicySetId = ((PolicySetCombinerParameters) policyChildElt).getPolicySetIdRef();
				final COMBINED_ELT combinedPolicySet = childPolicySetsById.get(combinedPolicySetId);
				if (combinedPolicySet == null)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyFriendlyId + ":  invalid PolicySetCombinerParameters: referencing undefined child PolicySet #"
							+ combinedPolicySetId + " (no such policySet defined before this element)");
				}

				final BaseCombiningAlgParameter<COMBINED_ELT> combinerElt;
				try
				{
					combinerElt = new BaseCombiningAlgParameter<>(combinedPolicySet, ((CombinerParametersType) policyChildElt).getCombinerParameters(), policyEvaluatorFactory.expressionFactory,
							policyEvaluatorFactory.defaultXPathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException(policyEvaluatorFactory.policyFriendlyId + ": invalid child #" + childIndex + " (PolicySetCombinerParameters)", e);
				}

				policyCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof JAXBElement)
			{
				final JAXBElement<?> jaxbElt = (JAXBElement<?>) policyChildElt;
				final String eltNameLocalPart = jaxbElt.getName().getLocalPart();
				if (eltNameLocalPart.equals(XACMLNodeName.POLICY_ID_REFERENCE.value()))
				{
					final IdReferenceType idRef = (IdReferenceType) jaxbElt.getValue();
					final COMBINED_ELT childElement = policyEvaluatorFactory.getChildPolicyRefEvaluator(childIndex, TopLevelPolicyElementType.POLICY, idRef, null);
					combinedChildElements.add(childElement);
				} else if (eltNameLocalPart.equals(XACMLNodeName.POLICYSET_ID_REFERENCE.value()))
				{
					final IdReferenceType idRef = (IdReferenceType) jaxbElt.getValue();
					final COMBINED_ELT childElement = policyEvaluatorFactory.getChildPolicyRefEvaluator(childIndex, TopLevelPolicyElementType.POLICY_SET, idRef, policySetRefChain);
					combinedChildElements.add(childElement);
				} else if (eltNameLocalPart.equals(XACMLNodeName.COMBINER_PARAMETERS.value()))
				{
					/*
					 * CombinerParameters that is not Policy(Set)CombinerParameters already tested before
					 */
					final BaseCombiningAlgParameter<COMBINED_ELT> combinerElt;
					try
					{
						combinerElt = new BaseCombiningAlgParameter<>(null, ((CombinerParametersType) jaxbElt.getValue()).getCombinerParameters(), policyEvaluatorFactory.expressionFactory,
								policyEvaluatorFactory.defaultXPathCompiler);
					} catch (IllegalArgumentException e)
					{
						throw new IllegalArgumentException(policyEvaluatorFactory.policyFriendlyId + ": invalid child #" + childIndex + " (CombinerParameters)", e);
					}

					policyCombinerParameters.add(combinerElt);
				}
			} else if (policyChildElt instanceof PolicySet)
			{
				/*
				 * This child PolicySet may have PoliSetIdReferences as well and therefore update the policySetRefChain and staticallyReferencedPolicies. However, if the current policySetRefChain is
				 * updated directly by a child PolicySet instantiation, then it is no longer valid for the other child PolicySets of this same PolicySet. So we need to pass a copy to
				 * PolicySetEvaluator.getInstance(() to avoid that inconsistency.
				 */
				final COMBINED_ELT childElement = policyEvaluatorFactory.getChildPolicySetEvaluator(childIndex, (PolicySet) policyChildElt, policySetRefChain == null ? null : new ArrayDeque<>(
						policySetRefChain));
				childPolicySetsById.put(childElement.getPolicyId(), childElement);
				combinedChildElements.add(childElement);
			} else if (policyChildElt instanceof Policy)
			{
				final COMBINED_ELT childPolicy = policyEvaluatorFactory.getChildPolicyEvaluator(childIndex, (Policy) policyChildElt);
				childPoliciesById.put(childPolicy.getPolicyId(), childPolicy);
				combinedChildElements.add(childPolicy);
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
		final Set<String> localVariableIds = Collections.emptySet();
		return policyEvaluatorFactory.getInstance(policyElement.getPolicySetId(), policyElement.getTarget(), policyElement.getPolicyCombiningAlgId(), combinedChildElements, policyCombinerParameters,
				policyElement.getObligationExpressions(), policyElement.getAdviceExpressions(), localVariableIds);
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
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to this PolicySet, if any: PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n -> this. This allows to detect circular references and
	 *            validate the size of the chain against the max depth enforced by {@code refPolicyProvider}. This may be null if no ancestor, e.g. a PolicySetIdReference in a top-level PolicySet.
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument (e.g. {@code policyElement}) is invalid
	 */
	public static StaticTopLevelPolicyElementEvaluator getInstanceStatic(PolicySet policyElement, XPathCompiler parentDefaultXPathCompiler, Map<String, String> namespacePrefixesByURI,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgorithmRegistry, StaticRefPolicyProvider refPolicyProvider, Deque<String> policySetRefChain)
			throws IllegalArgumentException
	{
		final StaticPolicySetElementEvaluatorFactory factory = new StaticPolicySetElementEvaluatorFactory(policyElement.getPolicySetId(), policyElement.getVersion(),
				policyElement.getPolicySetDefaults(), refPolicyProvider, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
		return getInstanceGeneric(factory, policyElement, policySetRefChain);
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
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to this PolicySet, if any: PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n -> this. This allows to detect circular references and
	 *            validate the size of the chain against the max depth enforced by {@code refPolicyProvider}. This may be null if no ancestor, e.g. a PolicySetIdReference in a top-level PolicySet.
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if any argument (e.g. {@code policyElement}) is invalid
	 */
	public static TopLevelPolicyElementEvaluator getInstance(PolicySet policyElement, XPathCompiler parentDefaultXPathCompiler, Map<String, String> namespacePrefixesByURI,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgorithmRegistry, RefPolicyProvider refPolicyProvider, Deque<String> policySetRefChain) throws IllegalArgumentException
	{
		final PolicySetElementEvaluatorFactory<?, ?> factory = refPolicyProvider instanceof StaticRefPolicyProvider ? new StaticPolicySetElementEvaluatorFactory(policyElement.getPolicySetId(),
				policyElement.getVersion(), policyElement.getPolicySetDefaults(), (StaticRefPolicyProvider) refPolicyProvider, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory,
				combiningAlgorithmRegistry) : new DynamicPolicySetElementEvaluatorFactory(policyElement.getPolicySetId(), policyElement.getVersion(), policyElement.getPolicySetDefaults(),
				refPolicyProvider, parentDefaultXPathCompiler, namespacePrefixesByURI, expressionFactory, combiningAlgorithmRegistry);
		return getInstanceGeneric(factory, policyElement, policySetRefChain);
	}
}
