/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.thalesgroup.authzforce.core.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetCombinerParameters;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.Target;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Utils;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.xacml.schema.XACMLNodeName;

/**
 * PolicySet Evaluator
 * 
 */
public class PolicySet extends oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet implements IPolicy
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_TARGET_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/Target is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_OBLIGATION_EXPRESSIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/ObligationExpressions is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICY_COMBINING_ALG_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicyCombiningAlgId is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_ADVICE_EXPRESSIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/AdviceExpressions is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_VERSION_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/Version is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICYSET_ID_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicySetId is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICYSET_DEFAULTS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicySetDefaults is read-only");
	private static final ParsingException NULL_POLICYSET_ID_EXCEPTION = new ParsingException("Undefined PolicySetId");
	private static final ParsingException NULL_VERSION_EXCEPTION = new ParsingException("Undefined Version");

	private final PolicyEvaluator<IPolicy> policyEvaluator;

	private final String toString;
	private final int hashCode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy#setTarget(oasis.names.tc.xacml._3_0.core
	 * .schema.wd_17.Target)
	 */
	@Override
	public final void setTarget(oasis.names.tc.xacml._3_0.core.schema.wd_17.Target value)
	{
		// make this field immutable because policyEvaluator based on it
		throw UNSUPPORTED_SET_TARGET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy#setObligationExpressions(oasis.names.tc
	 * .xacml._3_0.core.schema.wd_17.ObligationExpressions)
	 */
	@Override
	public final void setObligationExpressions(ObligationExpressions value)
	{
		// make this field immutable because policyEvaluator based on it
		throw UNSUPPORTED_SET_OBLIGATION_EXPRESSIONS_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy#setAdviceExpressions(oasis.names.tc.xacml
	 * ._3_0.core.schema.wd_17.AdviceExpressions)
	 */
	@Override
	public final void setAdviceExpressions(AdviceExpressions value)
	{
		// make this field immutable because policyEvaluator based on it
		throw UNSUPPORTED_SET_ADVICE_EXPRESSIONS_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy#setRuleCombiningAlgId(java.lang.String)
	 */
	@Override
	public final void setPolicyCombiningAlgId(String value)
	{
		// make this field immutable because policyEvaluator based on it
		throw UNSUPPORTED_SET_POLICY_COMBINING_ALG_OPERATION_EXCEPTION;
	}

	@Override
	public final void setVersion(String value)
	{
		// make this field immutable because toString based on it
		throw UNSUPPORTED_SET_VERSION_OPERATION_EXCEPTION;
	}

	@Override
	public final void setPolicySetId(String value)
	{
		// make this field immutable because toString based on it
		throw UNSUPPORTED_SET_POLICYSET_ID_OPERATION_EXCEPTION;
	}

	@Override
	public final void setPolicySetDefaults(DefaultsType value)
	{
		// make this field immutable because policyEvaluator based on it
		throw UNSUPPORTED_SET_POLICYSET_DEFAULTS_OPERATION_EXCEPTION;
	}

	/**
	 * Creates Policy handler from Policy element as defined in OASIS XACML model
	 * 
	 * @param policySetElement
	 *            PolicySet (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySet's default XPath version, or null
	 *            if either no parent or no default XPath version defined in parent
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgorithmRegistry
	 *            policy/rule combining algorithm registry
	 * @param refPolicyFinder
	 *            policy-by-reference (Policy(Set)IdReference) finder to find references used in
	 *            this policyset
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to this PolicySet, if any:
	 *            PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n -> this. This allows to detect
	 *            circular references and validate the size of the chain against the max depth
	 *            enforced by {@code refPolicyFinder}. This may be null if no ancestor, e.g. a
	 *            PolicySetIdReference in a top-level PolicySet. Beware that we only keep the IDs in
	 *            the chain, and not the version, because we consider that a reference loop on the
	 *            same policy ID is not allowed, no matter what the version is.
	 * @throws ParsingException
	 *             if PolicyElement is invalid
	 */
	public PolicySet(oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet policySetElement, XPathCompiler parentDefaultXPathCompiler, Expression.Factory expressionFactory, CombiningAlgRegistry combiningAlgorithmRegistry, RefPolicyFinder refPolicyFinder, Deque<String> policySetRefChain)
			throws ParsingException
	{
		this.policySetId = policySetElement.getPolicySetId();
		if (policySetId == null)
		{
			throw NULL_POLICYSET_ID_EXCEPTION;
		}

		this.version = policySetElement.getVersion();
		if (version == null)
		{
			throw NULL_VERSION_EXCEPTION;
		}

		this.toString = this.getClass().getSimpleName() + "[" + this.policySetId + "#v" + this.version + "]";
		/*
		 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well
		 * in PolicySetIdReferences. So we consider it is useless for identification in the XACML
		 * model.
		 */
		this.hashCode = Objects.hash(this.policySetId, this.version);

		this.maxDelegationDepth = policySetElement.getMaxDelegationDepth();
		this.description = policySetElement.getDescription();
		this.policyIssuer = policySetElement.getPolicyIssuer();
		this.policySetDefaults = policySetElement.getPolicySetDefaults();
		// Inherited PolicySetDefaults is this.policyDefaults if not null, the
		// parentPolicySetDefaults otherwise
		final XPathCompiler defaultXPathCompiler;
		if (policySetDefaults == null)
		{
			defaultXPathCompiler = parentDefaultXPathCompiler;
		} else
		{
			defaultXPathCompiler = Utils.XPATH_COMPILERS_BY_VERSION.get(policySetDefaults.getXPathVersion());
			if (defaultXPathCompiler == null)
			{
				throw new ParsingException(this + ": Invalid PolicyDefaults/XPathVersion: " + policySetDefaults.getXPathVersion());
			}
		}

		final Target evaluatableTarget;
		try
		{
			evaluatableTarget = new Target(policySetElement.getTarget(), defaultXPathCompiler, expressionFactory);
		} catch (ParsingException e)
		{
			throw new ParsingException(this + ": Error parsing Target", e);
		}

		this.target = evaluatableTarget;

		/*
		 * Why isn't there any VariableDefinition in XACML PolicySet like in Policy? If there were,
		 * we would keep a copy of variable IDs defined in this policy, to remove them from the
		 * global manager at the end of parsing this PolicySet. They should not be visible outside
		 * the scope of this. final Set<String> variableIds = new HashSet<>();
		 */

		/*
		 * Map to get child Policies by their ID so that we can resolve Policies associated with
		 * CombinerParameters
		 */
		final Map<String, Policy> childPoliciesById = new HashMap<>();

		/*
		 * Map to get child PolicySets by their ID so that we can resolve PolicySets associated with
		 * CombinerParameters
		 */
		final Map<String, PolicySet> childPolicySetsById = new HashMap<>();

		final List<IPolicy> combinedChildElements = new ArrayList<>();
		final List<CombinerElement<? extends IPolicy>> policyCombinerParameters = new ArrayList<>();
		int childIndex = 0;
		for (final Object policySetChildElt : policySetElement.getPolicySetsAndPoliciesAndPolicySetIdReferences())
		{
			if (policySetChildElt instanceof PolicyCombinerParameters)
			{
				final String combinedPolicyId = ((PolicyCombinerParameters) policySetChildElt).getPolicyIdRef();
				final Policy combinedPolicy = childPoliciesById.get(combinedPolicyId);
				if (combinedPolicy == null)
				{
					throw new ParsingException(this + ":  invalid PolicyCombinerParameters: referencing undefined child Policy #" + combinedPolicyId + " (no such policy defined before this element)");
				}

				final CombinerElement<Policy> combinerElt;
				try
				{
					combinerElt = new CombinerElement<>(combinedPolicy, ((CombinerParametersType) policySetChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (PolicyCombinerParameters)", e);
				}

				policyCombinerParameters.add(combinerElt);

			} else if (policySetChildElt instanceof PolicySetCombinerParameters)
			{
				final String combinedPolicySetId = ((PolicySetCombinerParameters) policySetChildElt).getPolicySetIdRef();
				final PolicySet combinedPolicySet = childPolicySetsById.get(combinedPolicySetId);
				if (combinedPolicySet == null)
				{
					throw new ParsingException(this + ":  invalid PolicySetCombinerParameters: referencing undefined child PolicySet #" + combinedPolicySetId + " (no such policySet defined before this element)");
				}

				final CombinerElement<PolicySet> combinerElt;
				try
				{
					combinerElt = new CombinerElement<>(combinedPolicySet, ((CombinerParametersType) policySetChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (PolicySetCombinerParameters)", e);
				}

				policyCombinerParameters.add(combinerElt);
			} else if (policySetChildElt instanceof JAXBElement)
			{
				final JAXBElement<?> jaxbElt = (JAXBElement<?>) policySetChildElt;
				final String eltNameLocalPart = jaxbElt.getName().getLocalPart();
				if (eltNameLocalPart.equals(XACMLNodeName.POLICY_ID_REFERENCE.value()))
				{
					if (refPolicyFinder == null)
					{
						throw new ParsingException(this + ": Error parsing child #" + childIndex + " (PolicyIdReference): no refPolicyFinder (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
					}

					final IdReferenceType idRef = (IdReferenceType) jaxbElt.getValue();
					combinedChildElements.add(PolicyReference.getInstance(idRef, refPolicyFinder, Policy.class, null));
				} else if (eltNameLocalPart.equals(XACMLNodeName.POLICYSET_ID_REFERENCE.value()))
				{
					if (refPolicyFinder == null)
					{
						throw new ParsingException(this + ": Error parsing child #" + childIndex + " (PolicyIdReference): no refPolicyFinder (module responsible for resolving Policy(Set)IdReferences) defined to support it.");
					}

					final IdReferenceType idRef = (IdReferenceType) jaxbElt.getValue();
					combinedChildElements.add(PolicyReference.getInstance(idRef, refPolicyFinder, PolicySet.class, policySetRefChain));
				} else if (eltNameLocalPart.equals(XACMLNodeName.COMBINER_PARAMETERS.value()))
				{
					// CombinerParameters that is not Policy(Set)CombinerParameters already tested
					// before
					final CombinerElement<IPolicy> combinerElt;
					try
					{
						combinerElt = new CombinerElement<>(null, ((CombinerParametersType) jaxbElt.getValue()).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
					} catch (ParsingException e)
					{
						throw new ParsingException(this + ": Error parsing child #" + childIndex + " (CombinerParameters)", e);
					}

					policyCombinerParameters.add(combinerElt);
				}
			} else if (policySetChildElt instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
			{
				final PolicySet childPolicySet;
				try
				{
					childPolicySet = new PolicySet((oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) policySetChildElt, defaultXPathCompiler, expressionFactory, combiningAlgorithmRegistry, refPolicyFinder, policySetRefChain);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (PolicySet)", e);
				}

				childPolicySetsById.put(childPolicySet.getPolicySetId(), childPolicySet);
				combinedChildElements.add(childPolicySet);
			} else if (policySetChildElt instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy)
			{
				final Policy childPolicy;
				try
				{
					childPolicy = new Policy((oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) policySetChildElt, defaultXPathCompiler, expressionFactory, combiningAlgorithmRegistry);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (Policy)", e);
				}

				childPoliciesById.put(childPolicy.getPolicyId(), childPolicy);
				combinedChildElements.add(childPolicy);
			}

			/*
			 * Why isn't there any VariableDefinition in XACML PolicySet like in Policy? If there
			 * were, the following code would be used.
			 */
			// else if (policySetChildElt instanceof VariableDefinition)
			// {
			// final VariableDefinition varDef = (VariableDefinition) policySetChildElt;
			// final VariableReference var;
			// try
			// {
			// var = expFactory.addVariable(varDef);
			// } catch (ParsingException e)
			// {
			// throw new ParsingException(this + ": Error parsing child #" + childIndex +
			// " (VariableDefinition)", e);
			// }
			//
			// if (var != null)
			// {
			// // Conflicts can occur between variables defined in this policySet but also with
			// // others defined in parent/ancestor policySet
			// throw new ParsingException(this + ": Duplicable VariableDefinition for VariableId=" +
			// var.getVariableId());
			// }
			//
			// variableIds.add(varDef.getVariableId());
			// }

			childIndex++;
		}

		this.policySetsAndPoliciesAndPolicySetIdReferences = Collections.<Object> unmodifiableList(policySetElement.getPolicySetsAndPoliciesAndPolicySetIdReferences());

		this.policyCombiningAlgId = policySetElement.getPolicyCombiningAlgId();
		final CombiningAlgorithm<IPolicy> policyCombiningAlg;
		try
		{
			policyCombiningAlg = combiningAlgorithmRegistry.getAlgorithm(this.policyCombiningAlgId, IPolicy.class);
		} catch (UnknownIdentifierException e)
		{
			throw new ParsingException(this + ": Unknown policy-combining algorithm ID=" + policyCombiningAlgId, e);
		}

		final PolicyPepActionExpressionsEvaluator pepActionExps = PolicyPepActionExpressionsEvaluator.getInstance(policySetElement.getObligationExpressions(), policySetElement.getAdviceExpressions(), defaultXPathCompiler, expressionFactory);
		if (pepActionExps == null)
		{
			this.obligationExpressions = null;
			this.adviceExpressions = null;
		} else
		{
			this.obligationExpressions = pepActionExps.getObligationExpressions();
			this.adviceExpressions = pepActionExps.getAdviceExpressions();
		}

		/*
		 * Why isn't there any VariableDefinition in XACML PolicySet like in Policy? If there were,
		 * the final following code would be used: We are done parsing expressions in this policy,
		 * including VariableReferences, it's time to remove variables scoped to this policy from
		 * the variable manager
		 */
		// for (final String varId : variableIds)
		// {
		// expFactory.remove(varId);
		// }

		this.policyEvaluator = new PolicyEvaluator<>(evaluatableTarget, combinedChildElements, policyCombinerParameters, policyCombiningAlg, pepActionExps, Collections.EMPTY_SET, toString);
	}

	@Override
	public boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return policyEvaluator.matchTarget(context);
	}

	@Override
	public DecisionResult evaluate(EvaluationContext context)
	{
		return evaluate(context, false);
	}

	@Override
	public DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
	{
		return policyEvaluator.eval(context, skipTarget);
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
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final PolicySet other = (PolicySet) obj;
		/*
		 * We ignore he policySetIssuer because it is no part of PolicyReferences, therefore we
		 * consider it is not part of the Policy uniqueness
		 */
		/*
		 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we
		 * consider it is not part of the Policy uniqueness
		 */
		return this.policySetId.equals(other.policySetId) && this.version.equals(other.version);
	}

}
