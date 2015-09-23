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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import com.sun.xacml.ParsingException;
import com.sun.xacml.Rule;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.Target;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.VariableReference;

/**
 * Policy
 * 
 */
public class Policy extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy implements IPolicy
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_TARGET_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/Target is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_OBLIGATION_EXPRESSIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/ObligationExpressions is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_RULE_COMBINING_ALG_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/RuleCombiningAlgId is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_ADVICE_EXPRESSIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/AdviceExpressions is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_VERSION_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/Version is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICY_ID_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicySetId is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICY_DEFAULTS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicySetDefaults is read-only");

	private final String toString;
	private final int hashCode;

	private final PolicyEvaluator<Rule> policyEvaluator;

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
	public final void setRuleCombiningAlgId(String value)
	{
		throw UNSUPPORTED_SET_RULE_COMBINING_ALG_OPERATION_EXCEPTION;
	}

	@Override
	public final void setVersion(String value)
	{
		// make this field immutable because toString based on it
		throw UNSUPPORTED_SET_VERSION_OPERATION_EXCEPTION;
	}

	@Override
	public final void setPolicyId(String value)
	{
		// make this field immutable because toString based on it
		throw UNSUPPORTED_SET_POLICY_ID_OPERATION_EXCEPTION;
	}

	@Override
	public final void setPolicyDefaults(DefaultsType value)
	{
		// make this field immutable because policyEvaluator based on it
		throw UNSUPPORTED_SET_POLICY_DEFAULTS_OPERATION_EXCEPTION;
	}

	/**
	 * Creates Policy handler from Policy element as defined in OASIS XACML model
	 * 
	 * @param policyElement
	 *            Policy (XACML)
	 * @param parentPolicySetDefaults
	 *            parent PolicySetDefaults; null if this Policy has no parent PolicySet (root), or
	 *            none defined in parent
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgRegistry
	 *            rule/policy combining algorithm registry
	 * @throws ParsingException
	 *             if PolicyElement is invalid
	 */
	public Policy(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy policyElement, DefaultsType parentPolicySetDefaults, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws ParsingException
	{
		this.policyId = policyElement.getPolicyId();
		if (policyId == null)
		{
			throw new ParsingException("Undefined Policy required attribute: PolicyId");
		}

		this.version = policyElement.getVersion();
		if (version == null)
		{
			throw new ParsingException("Undefined Policy required attribute: Version");
		}

		this.policyIssuer = policyElement.getPolicyIssuer();

		this.toString = this.getClass().getSimpleName() + "[" + this.policyId + "#v" + this.version + "]";
		/*
		 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well
		 * in PolicyIdReferences. So we consider it is useless for identification in the XACML
		 * model.
		 */
		this.hashCode = Objects.hash(this.policyId, this.version);

		this.maxDelegationDepth = policyElement.getMaxDelegationDepth();
		this.description = policyElement.getDescription();
		this.policyDefaults = policyElement.getPolicyDefaults();
		// Inherited PolicySetDefaults is this.policyDefaults if not null, the
		// parentPolicySetDefaults otherwise
		final DefaultsType inheritedPolicyDefaults = policyDefaults == null ? parentPolicySetDefaults : policyDefaults;

		final Target evaluatableTarget;
		try
		{
			evaluatableTarget = new Target(policyElement.getTarget(), inheritedPolicyDefaults, expressionFactory);
		} catch (ParsingException e)
		{
			throw new ParsingException(this + ": Error parsing Target", e);
		}

		this.target = evaluatableTarget;

		final List<Rule> rules = new ArrayList<>();
		final List<CombinerElement<? extends Rule>> ruleCombinerParameters = new ArrayList<>();

		/*
		 * Keep a copy of locally-defined variable IDs defined in this policy, to remove them from
		 * the global manager at the end of parsing this policy. They should not be visible outside
		 * the scope of this policy.
		 */
		final Set<String> localVariableIds = new HashSet<>();
		/*
		 * Map to get rules by their ID so that we can resolve rules associated with
		 * CombinerParameters
		 */
		final Map<String, Rule> rulesById = new HashMap<>();
		int childIndex = 0;
		for (final Object policyChildElt : policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions())
		{
			if (policyChildElt instanceof RuleCombinerParameters)
			{
				final String combinedRuleId = ((RuleCombinerParameters) policyChildElt).getRuleIdRef();
				final Rule combinedRule = rulesById.get(combinedRuleId);
				if (combinedRule == null)
				{
					throw new ParsingException(this + ":  invalid RuleCombinerParameters: referencing undefined child Rule #" + combinedRuleId + " (no such rule defined before this element)");
				}

				final CombinerElement<Rule> combinerElt;
				try
				{
					combinerElt = new CombinerElement<>(combinedRule, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (RuleCombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof CombinerParametersType)
			{
				// CombinerParameters that is not RuleCombinerParameters already tested before
				final CombinerElement<Rule> combinerElt;
				try
				{
					combinerElt = new CombinerElement<>(null, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (CombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof VariableDefinition)
			{
				final VariableDefinition varDef = (VariableDefinition) policyChildElt;
				final VariableReference<?> var;
				try
				{
					var = expressionFactory.addVariable(varDef, inheritedPolicyDefaults);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (VariableDefinition)", e);
				}

				if (var != null)
				{
					/*
					 * Conflicts can occur between variables defined in this policy but also with
					 * others already in a wider scope, i.e. defined in parent/ancestor policy
					 */
					throw new ParsingException(this + ": Duplicable VariableDefinition for VariableId=" + var.getVariableId());
				}

				localVariableIds.add(varDef.getVariableId());
			} else if (policyChildElt instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule)
			{
				final Rule rule;
				try
				{
					rule = new Rule((oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule) policyChildElt, inheritedPolicyDefaults, expressionFactory);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (Rule)", e);
				}

				rulesById.put(rule.getRuleId(), rule);
				rules.add(rule);
			}

			childIndex++;
		}

		this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.<Object> unmodifiableList(policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions());

		this.ruleCombiningAlgId = policyElement.getRuleCombiningAlgId();
		final CombiningAlgorithm<Rule> ruleCombiningAlg;
		try
		{
			ruleCombiningAlg = combiningAlgRegistry.getAlgorithm(this.ruleCombiningAlgId, Rule.class);
		} catch (UnknownIdentifierException e)
		{
			throw new ParsingException(this + ": Unknown rule-combining algorithm ID=" + ruleCombiningAlgId, e);
		}

		final PolicyPepActionExpressionsEvaluator pepActionExps = PolicyPepActionExpressionsEvaluator.getInstance(policyElement.getObligationExpressions(), policyElement.getAdviceExpressions(), inheritedPolicyDefaults, expressionFactory);
		if (pepActionExps == null)
		{
			this.obligationExpressions = null;
			this.adviceExpressions = null;
		} else
		{
			this.obligationExpressions = pepActionExps.getObligationExpressions();
			this.adviceExpressions = pepActionExps.getAdviceExpressions();
		}

		this.policyEvaluator = new PolicyEvaluator<>(evaluatableTarget, rules, ruleCombinerParameters, ruleCombiningAlg, pepActionExps, Collections.unmodifiableSet(localVariableIds), toString);

		/*
		 * We are done parsing expressions in this policy, including VariableReferences, it's time
		 * to remove variables scoped to this policy from the variable manager
		 */
		for (final String varId : localVariableIds)
		{
			expressionFactory.removeVariable(varId);
		}
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Policy other = (Policy) obj;
		/*
		 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we
		 * consider it is not part of the Policy uniqueness
		 */
		if (this.policyId != other.policyId)
		{
			return false;
		}

		if (this.version != other.version)
		{
			return false;
		}

		return true;
	}

}
