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

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

import org.ow2.authzforce.core.pdp.api.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.RefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.VersionPatterns;
import org.ow2.authzforce.core.pdp.impl.BaseDecisionResult;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Generic Policy(Set) evaluator. Evaluates to a Decision.
 * 
 * @param <T>
 *            type of combined child elements in evaluated Policy(Set)
 * 
 */
public abstract class GenericPolicyEvaluator<T extends Decidable> implements IPolicyEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericPolicyEvaluator.class);
	private static final IllegalArgumentException NULL_POLICY_ID_EXCEPTION = new IllegalArgumentException(
			"Undefined Policy(Set)Id (required)");
	private static final IllegalArgumentException NULL_VERSION_EXCEPTION = new IllegalArgumentException(
			"Undefined Policy(Set) Version (required)");

	private static final IllegalArgumentException UNDEF_REF_POLICY_PROVIDER_EXCEPTION = new IllegalArgumentException(
			"Policy(Set)IdReference resolver/Provider undefined");
	private static final IllegalArgumentException NULL_POLICY_REF_EXCEPTION = new IllegalArgumentException(
			"Undefined Policy(Set) reference (ID, Version) (required)");

	/**
	 * Instantiates Policy(Set) Reference evaluator
	 * 
	 * @param idRef
	 *            Policy(Set)IdReference
	 * @param refPolicyProvider
	 *            Policy(Set)IdReference resolver/Provider
	 * @param refPolicyType
	 *            type of policy referenced, i.e. whether it refers to Policy or PolicySet
	 * @param parentPolicySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the reference identified here by {@code idRef}
	 *            (exclusive): PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n -> {@code idRef}. This allows to
	 *            detect circular references and validate the size of the chain against the max depth enforced by
	 *            {@code policyProvider}. This may be null if no ancestor, e.g. a PolicySetIdReference in a top-level
	 *            PolicySet. Beware that we only keep the IDs in the chain, and not the version, because we consider
	 *            that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 * @return instance instance of PolicyReference
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyProvider} undefined, or there is no policy of type {@code refPolicyType} matching
	 *             {@code idRef} to be found by {@code refPolicyProvider}, or PolicySetIdReference loop detected or
	 *             PolicySetIdReference depth exceeds the max enforced by {@code policyProvider}
	 */
	public static <T extends IPolicyEvaluator> PolicyReferenceEvaluator<T> getPolicyRefEvaluator(IdReferenceType idRef,
			RefPolicyProvider refPolicyProvider, Class<T> refPolicyType, Deque<String> parentPolicySetRefChain)
			throws IllegalArgumentException
	{
		if (refPolicyProvider == null)
		{
			throw UNDEF_REF_POLICY_PROVIDER_EXCEPTION;
		}

		final VersionPatterns versionConstraints = new VersionPatterns(idRef.getVersion(), idRef.getEarliestVersion(),
				idRef.getLatestVersion());
		/*
		 * REMINDER: parentPolicySetRefChain is handled/updated by the refPolicyProvider. So do not modify it here, just
		 * pass the parameter. modify it here.
		 */
		if (refPolicyProvider.isStatic())
		{
			final T policy;
			try
			{
				policy = refPolicyProvider.get(refPolicyType, idRef.getValue(), versionConstraints,
						parentPolicySetRefChain);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IllegalArgumentException("Error resolving statically or parsing "
						+ PolicyReferenceEvaluator.toString(refPolicyType, idRef.getValue(), versionConstraints)
						+ " into its referenced policy (via static policy Provider)", e);
			}

			if (policy == null)
			{
				throw new IllegalArgumentException("No "
						+ (refPolicyType == PolicyEvaluator.class ? "Policy" : "PolicySet") + " matching reference: "
						+ idRef);
			}

			return new StaticPolicyRefEvaluator<>(idRef.getValue(), versionConstraints, policy);
		}

		// dynamic reference resolution
		return new DynamicPolicyRefEvaluator<>(idRef.getValue(), versionConstraints, refPolicyType, refPolicyProvider,
				parentPolicySetRefChain);
	}

	private final String policyId;
	private final PolicyVersion policyVersion;
	private final TargetEvaluator targetEvaluator;
	private final CombiningAlg.Evaluator combiningAlgEvaluator;
	private final PolicyPepActionExpressionsEvaluator pepActionExps;
	private final Set<String> localVariableIds;
	private final String combiningAlgId;

	private transient final String toString;
	private transient final int hashCode;
	private transient final JAXBElement<IdReferenceType> refToSelf;

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
	 * @param jaxbPolicyRef
	 *            policy reference (identifier and version) of the policy that this evaluator evaluates
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
	 *             if
	 *             {@code jaxbPolicyRef == null || jaxbPolicyRef.getValue().getValue() == null || jaxbPolicyRef.getValue().getVersion() == null}
	 */
	public GenericPolicyEvaluator(Class<T> combinedElementClass, JAXBElement<IdReferenceType> jaxbPolicyRef,
			Target policyTarget, String combiningAlgId, List<? extends T> combinedElements,
			List<CombiningAlgParameter<? extends T>> combinerParameters, ObligationExpressions obligationExps,
			AdviceExpressions adviceExps, Set<String> localVariableIds, XPathCompiler defaultXPathCompiler,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry)
			throws IllegalArgumentException
	{
		if (jaxbPolicyRef == null)
		{
			throw NULL_POLICY_REF_EXCEPTION;
		}

		this.refToSelf = jaxbPolicyRef;
		final IdReferenceType jaxbIdRef = jaxbPolicyRef.getValue();
		final String id = jaxbIdRef.getValue();
		if (id == null)
		{
			throw NULL_POLICY_ID_EXCEPTION;
		}

		this.policyId = id;

		final String version = jaxbIdRef.getVersion();
		if (version == null)
		{
			throw NULL_VERSION_EXCEPTION;
		}

		this.policyVersion = new PolicyVersion(version);

		this.toString = this.getClass().getSimpleName() + "[" + this.policyId + "#v" + this.policyVersion + "]";
		/*
		 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well in
		 * PolicyIdReferences. So we consider it is useless for identification in the XACML model.
		 */
		this.hashCode = Objects.hash(this.getClass(), this.policyId, this.policyVersion);

		try
		{
			this.targetEvaluator = new TargetEvaluator(policyTarget, defaultXPathCompiler, expressionFactory);
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": Invalid Target", e);
		}

		this.combiningAlgId = combiningAlgId;
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
			this.pepActionExps = PolicyPepActionExpressionsEvaluator.getInstance(obligationExps, adviceExps,
					defaultXPathCompiler, expressionFactory);
		} catch (IllegalArgumentException | ParsingException e)
		{
			throw new IllegalArgumentException(this + ": Invalid AttributeAssignmentExpressions", e);
		}

		this.localVariableIds = localVariableIds == null ? Collections.<String> emptySet() : localVariableIds;
	}

	/**
	 * Policy(Set) evaluation which option to skip Target evaluation. The option is to be used by Only-one-applicable
	 * algorithm with value 'true', after calling {@link #isApplicable(EvaluationContext)} in particular.
	 * 
	 * @param context
	 *            evaluation context
	 * @param skipTarget
	 *            whether to evaluate the Target.
	 * @return decision result
	 */
	@Override
	public DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
	{
		try
		{
			final DecisionResult algResult;
			if (skipTarget)
			{
				// evaluate with combining algorithm
				algResult = combiningAlgEvaluator.eval(context);
				LOGGER.debug("{}/Algorithm -> {}", policyId, algResult);
			} else
			{
				// evaluate target
				IndeterminateEvaluationException targetMatchIndeterminateException = null;
				try
				{
					if (!isApplicable(context))
					{
						LOGGER.debug("{} -> NotApplicable", policyId);
						return BaseDecisionResult.NOT_APPLICABLE;
					}
				} catch (IndeterminateEvaluationException e)
				{
					targetMatchIndeterminateException = e;
					/*
					 * Before we lose the exception information, log it at a higher level because it is an evaluation
					 * error (but no critical application error, therefore lower level than error)
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
					 * Implement Extended Indeterminate according to table 7 of section 7.14 (XACML 3.0 Core). If the
					 * combining alg value is Indeterminate, use its extended Indeterminate value as this evaluation
					 * result's extended Indeterminate value; else (Permit or Deny) as our extended indeterminate value
					 * (part between {} in XACML notation).
					 */
					final DecisionType algDecision = algResult.getDecision();
					switch (algDecision)
					{
					case NOT_APPLICABLE:
						return algResult;						
					case PERMIT:
					case DENY:
						return new BaseDecisionResult(targetMatchIndeterminateException.getStatus(), algDecision);
					default: // INDETERMINATE
						return new BaseDecisionResult(targetMatchIndeterminateException.getStatus(),
								algResult.getExtendedIndeterminate());
					}

				}
			}

			// target match not indeterminate
			final DecisionType algResultDecision = algResult.getDecision();
			final PepActions pepActions;
			final List<JAXBElement<IdReferenceType>> applicablePolicyIdList;
			switch (algResultDecision)
			{
			case NOT_APPLICABLE:
				return algResult;
			case INDETERMINATE:
				if (context.isApplicablePolicyIdListReturned())
				{
					applicablePolicyIdList = algResult.getApplicablePolicyIdList();
					applicablePolicyIdList.add(this.refToSelf);
					// PEP actions not returned with Indeterminate
					pepActions = null;
				} else
				{
					return algResult;
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
						// nothing to add to the combining alg eval result
						return algResult;
					}

					// no PEP actions on this Policy(Set) explicitly but maybe
					// on the children evaluated by combining algorithm
					// so we take the PEP actions resulting of algorithm
					// evaluation as the result PEP actions
					pepActions = algResult.getPepActions();
				} else
				{
					// pepActionExps != null -> evaluate pepActionExps

					/*
					 * If any of the attribute assignment expressions in an obligation or advice expression with a
					 * matching FulfillOn or AppliesTo attribute evaluates to "Indeterminate", then the whole rule,
					 * policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
					 */
					try
					{
						pepActions = pepActionExps.evaluate(algResult, context);
					} catch (IndeterminateEvaluationException e)
					{
						/*
						 * Before we lose the exception information, log it at a higher level because it is an
						 * evaluation error (but no critical application error, therefore lower level than error)
						 */
						LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", policyId, e);
						return new BaseDecisionResult(DecisionType.INDETERMINATE, algResultDecision, e.getStatus(),
								null, applicablePolicyIdList);
					}
				}
			}

			return new BaseDecisionResult(algResultDecision, algResult.getExtendedIndeterminate(),
					algResult.getStatus(), pepActions, applicablePolicyIdList);
		} finally
		{
			// remove local variables from context
			for (final String varId : this.localVariableIds)
			{
				context.removeVariable(varId);
			}
		}
	}

	@Override
	public boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
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
	public DecisionResult evaluate(EvaluationContext context)
	{
		return evaluate(context, false);
	}

	@Override
	public String toString()
	{
		return toString;
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		// if not both PolicyEvaluators or not both PolicySetEvaluators
		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final GenericPolicyEvaluator<?> other = (GenericPolicyEvaluator<?>) obj;
		/*
		 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we consider it is not part of
		 * the Policy uniqueness
		 */
		return this.policyId.equals(other.policyId) && this.policyVersion.equals(other.policyVersion);
	}

	@Override
	public String getPolicyId()
	{
		return this.policyId;
	}

	@Override
	public PolicyVersion getPolicyVersion()
	{
		return this.policyVersion;
	}

	@Override
	public String getCombiningAlgId()
	{
		return this.combiningAlgId;
	}

}
