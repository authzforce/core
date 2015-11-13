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

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleCombinerParameters;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.DecisionResult;
import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.Expression.Utils;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.TargetEvaluator;
import com.thalesgroup.authzforce.core.VariableReference;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.rule.RuleEvaluator;

/**
 * Evaluates a XACML Policy to a Decision.
 * 
 */
public final class PolicyEvaluator extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy implements IPolicy
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_TARGET_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/Target is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_OBLIGATION_EXPRESSIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/ObligationExpressions is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_RULE_COMBINING_ALG_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/RuleCombiningAlgId is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_ADVICE_EXPRESSIONS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/AdviceExpressions is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_VERSION_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/Version is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICY_ID_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicySetId is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_POLICY_DEFAULTS_OPERATION_EXCEPTION = new UnsupportedOperationException("PolicySet/PolicySetDefaults is read-only");

	private transient volatile String toString = null;
	private final int hashCode;

	private final GenericPolicyEvaluator<RuleEvaluator> genericPolicyEvaluator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy#setTarget(oasis.names.tc.xacml._3_0.core
	 * .schema.wd_17.Target)
	 */
	@Override
	public void setTarget(oasis.names.tc.xacml._3_0.core.schema.wd_17.Target value)
	{
		// make this field immutable because genericPolicyEvaluator based on it
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
	public void setObligationExpressions(ObligationExpressions value)
	{
		// make this field immutable because genericPolicyEvaluator based on it
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
	public void setAdviceExpressions(AdviceExpressions value)
	{
		// make this field immutable because genericPolicyEvaluator based on it
		throw UNSUPPORTED_SET_ADVICE_EXPRESSIONS_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy#setRuleCombiningAlgId(java.lang.String)
	 */
	@Override
	public void setRuleCombiningAlgId(String value)
	{
		throw UNSUPPORTED_SET_RULE_COMBINING_ALG_OPERATION_EXCEPTION;
	}

	@Override
	public void setVersion(String value)
	{
		// make this field immutable because toString based on it
		throw UNSUPPORTED_SET_VERSION_OPERATION_EXCEPTION;
	}

	@Override
	public void setPolicyId(String value)
	{
		// make this field immutable because toString based on it
		throw UNSUPPORTED_SET_POLICY_ID_OPERATION_EXCEPTION;
	}

	@Override
	public void setPolicyDefaults(DefaultsType value)
	{
		// make this field immutable because genericPolicyEvaluator based on it
		throw UNSUPPORTED_SET_POLICY_DEFAULTS_OPERATION_EXCEPTION;
	}

	/**
	 * Creates Policy handler from Policy element as defined in OASIS XACML model
	 * 
	 * @param policyElement
	 *            Policy (XACML)
	 * @param parentDefaultXPathCompiler
	 *            XPath compiler corresponding to parent PolicySetDefaults/XPathVersion; null if
	 *            this Policy has no parent PolicySet (root), or none defined in parent
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgRegistry
	 *            rule/policy combining algorithm registry
	 * @throws ParsingException
	 *             if PolicyElement is invalid
	 */
	public PolicyEvaluator(oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy policyElement, XPathCompiler parentDefaultXPathCompiler, Expression.Factory expressionFactory, CombiningAlgRegistry combiningAlgRegistry) throws ParsingException
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
		final XPathCompiler defaultXPathCompiler;
		if (policyDefaults == null)
		{
			defaultXPathCompiler = parentDefaultXPathCompiler;
		} else
		{
			defaultXPathCompiler = Utils.XPATH_COMPILERS_BY_VERSION.get(policyDefaults.getXPathVersion());
			if (defaultXPathCompiler == null)
			{
				throw new ParsingException(this + ": Invalid PolicyDefaults/XPathVersion: " + policyDefaults.getXPathVersion());
			}
		}

		final TargetEvaluator evaluatableTarget;
		try
		{
			evaluatableTarget = new TargetEvaluator(policyElement.getTarget(), defaultXPathCompiler, expressionFactory);
		} catch (ParsingException e)
		{
			throw new ParsingException(this + ": Error parsing Target", e);
		}

		this.target = evaluatableTarget;

		final List<RuleEvaluator> ruleEvaluators = new ArrayList<>();
		final List<CombinerElement<? extends RuleEvaluator>> ruleCombinerParameters = new ArrayList<>();

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
					throw new ParsingException(this + ":  invalid RuleCombinerParameters: referencing undefined child Rule #" + combinedRuleId + " (no such rule defined before this element)");
				}

				final CombinerElement<RuleEvaluator> combinerElt;
				try
				{
					combinerElt = new CombinerElement<>(combinedRule, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (RuleCombinerParameters)", e);
				}

				ruleCombinerParameters.add(combinerElt);
			} else if (policyChildElt instanceof CombinerParametersType)
			{
				// CombinerParameters that is not RuleCombinerParameters already tested before
				final CombinerElement<RuleEvaluator> combinerElt;
				try
				{
					combinerElt = new CombinerElement<>(null, ((CombinerParametersType) policyChildElt).getCombinerParameters(), expressionFactory, defaultXPathCompiler);
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
					var = expressionFactory.addVariable(varDef, defaultXPathCompiler);
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
				final RuleEvaluator ruleEvaluator;
				try
				{
					ruleEvaluator = new RuleEvaluator((oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule) policyChildElt, defaultXPathCompiler, expressionFactory);
				} catch (ParsingException e)
				{
					throw new ParsingException(this + ": Error parsing child #" + childIndex + " (Rule)", e);
				}

				rulesById.put(ruleEvaluator.getRuleId(), ruleEvaluator);
				ruleEvaluators.add(ruleEvaluator);
			}

			childIndex++;
		}

		this.combinerParametersAndRuleCombinerParametersAndVariableDefinitions = Collections.<Object> unmodifiableList(policyElement.getCombinerParametersAndRuleCombinerParametersAndVariableDefinitions());

		this.ruleCombiningAlgId = policyElement.getRuleCombiningAlgId();
		final CombiningAlgorithm<RuleEvaluator> ruleCombiningAlg;
		try
		{
			ruleCombiningAlg = combiningAlgRegistry.getAlgorithm(this.ruleCombiningAlgId, RuleEvaluator.class);
		} catch (UnknownIdentifierException e)
		{
			throw new ParsingException(this + ": Unknown rule-combining algorithm ID=" + ruleCombiningAlgId, e);
		}

		final PolicyPepActionExpressionsEvaluator pepActionExps = PolicyPepActionExpressionsEvaluator.getInstance(policyElement.getObligationExpressions(), policyElement.getAdviceExpressions(), defaultXPathCompiler, expressionFactory);
		if (pepActionExps == null)
		{
			this.obligationExpressions = null;
			this.adviceExpressions = null;
		} else
		{
			this.obligationExpressions = pepActionExps.getObligationExpressions();
			this.adviceExpressions = pepActionExps.getAdviceExpressions();
		}

		this.genericPolicyEvaluator = new GenericPolicyEvaluator<>(evaluatableTarget, ruleEvaluators, ruleCombinerParameters, ruleCombiningAlg, pepActionExps, Collections.unmodifiableSet(localVariableIds), toString);

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
		return genericPolicyEvaluator.matchTarget(context);
	}

	@Override
	public DecisionResult evaluate(EvaluationContext context)
	{
		return evaluate(context, false);
	}

	@Override
	public DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
	{
		return genericPolicyEvaluator.eval(context, skipTarget);
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

		if (!(obj instanceof PolicyEvaluator))
		{
			return false;
		}

		final PolicyEvaluator other = (PolicyEvaluator) obj;
		/*
		 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we
		 * consider it is not part of the Policy uniqueness
		 */
		return this.policyId.equals(other.policyId) && this.version.equals(other.version);
	}

}
