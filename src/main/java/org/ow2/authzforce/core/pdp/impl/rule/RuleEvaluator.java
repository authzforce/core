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
package org.ow2.authzforce.core.pdp.impl.rule;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.impl.BaseDecisionResult;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates a XACML Rule to a Decision.
 */
public class RuleEvaluator implements Decidable
{

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluator.class);

	private final transient DecisionType effectAsDecision;
	private final transient TargetEvaluator evaluatableTarget;
	private final transient ConditionEvaluator evaluatableCondition;
	private final transient RulePepActionExpressionsEvaluator effectMatchPepActionExps;
	private final transient BaseDecisionResult nullActionsRuleDecisionResult;
	private final String toString;
	private final String ruleId;

	/**
	 * Instantiates rule from XACML RuleType
	 * 
	 * @param ruleElt
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expressionFactory
	 *            Expression parser/factory
	 * @throws IllegalArgumentException
	 *             Invalid Target, Condition or Obligation/Advice expressions
	 */
	public RuleEvaluator(Rule ruleElt, XPathCompiler xPathCompiler, ExpressionFactory expressionFactory) throws IllegalArgumentException
	// throws ParsingException
	{
		// JAXB fields initialization
		this.ruleId = ruleElt.getRuleId();
		final EffectType effect = ruleElt.getEffect();

		this.toString = this.getClass().getSimpleName() + "[" + ruleId + "]";
		this.effectAsDecision = effect == EffectType.DENY ? DecisionType.DENY : DecisionType.PERMIT;

		final oasis.names.tc.xacml._3_0.core.schema.wd_17.Target targetElt = ruleElt.getTarget();
		try
		{
			this.evaluatableTarget = targetElt == null ? null : new TargetEvaluator(targetElt, xPathCompiler, expressionFactory);
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": Invalid Target", e);
		}

		final Condition condElt = ruleElt.getCondition();
		try
		{
			this.evaluatableCondition = condElt == null ? null : new ConditionEvaluator(condElt, xPathCompiler, expressionFactory);
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": invalid Condition", e);
		}

		try
		{
			this.effectMatchPepActionExps = RulePepActionExpressionsEvaluator.getInstance(ruleElt.getObligationExpressions(), ruleElt.getAdviceExpressions(),
					xPathCompiler, expressionFactory, effect);
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": Invalid ObligationExpressions/AdviceExpressions", e);
		}

		if (this.effectMatchPepActionExps == null)
		{

			this.nullActionsRuleDecisionResult = new BaseDecisionResult(this.effectAsDecision, null);
		} else
		{
			this.nullActionsRuleDecisionResult = null;
		}
	}

	/**
	 * Get evaluated rule ID
	 * 
	 * @return evaluated rule ID
	 */
	public String getRuleId()
	{
		return this.ruleId;
	}

	/**
	 * Evaluates the rule against the supplied context. This will check that the target matches, and then try to evaluate the condition. If the target and
	 * condition apply, then the rule's effect is returned in the result.
	 * <p>
	 * Note that rules are not required to have targets. If no target is specified, then the rule inherits its parent's target. In the event that this
	 * <code>RuleEvaluator</code> has no <code>Target</code> then the match is assumed to be true, since evaluating a policy tree to this level required the
	 * parent's target to match. In debug level, this method logs the evaluation result before return. Indeterminate results are logged in warn level only
	 * (which "includes" debug level).
	 * 
	 * @param context
	 *            the representation of the request we're evaluating
	 * 
	 * @return the result of the evaluation
	 */
	@Override
	public DecisionResult evaluate(EvaluationContext context)
	{
		/*
		 * Null or empty Target matches all So we just check if target non-null matches
		 */
		if (evaluatableTarget == null)
		{
			LOGGER.debug("{}/Target (none/empty) -> Match", this);
		} else
		{
			try
			{
				if (!evaluatableTarget.match(context))
				{
					LOGGER.debug("{}/Target -> No-match", this);
					final DecisionResult result = BaseDecisionResult.NOT_APPLICABLE;
					LOGGER.debug("{} -> {}", this, result);
					return result;
				}

				LOGGER.debug("{}/Target -> Match", this);
			} catch (IndeterminateEvaluationException e)
			{
				/*
				 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error,
				 * therefore lower level than error)
				 */
				LOGGER.info("{}/Target -> Indeterminate", this, e);

				/*
				 * FIXME: implement Extended Indeterminate: "Indeterminate{P}" if the Rule's Effect is Permit, or "Indeterminate{D}" if the Rule's Effect is
				 * Deny
				 */
				final DecisionResult result = new BaseDecisionResult(e.getStatus());
				LOGGER.debug("{} -> {}", this, result);
				return result;
			}
		}

		/*
		 * Target matches -> check condition Rule's condition considered as True if condition = null or condition's expression evaluated to true. See section
		 * 7.9 of XACML core spec, so result is the Rule's Effect, unless condition is not null AND it evaluates to False or throws Indeterminate exception.
		 */
		if (evaluatableCondition == null)
		{
			LOGGER.debug("{}/Condition (none/empty) -> True", this);
		} else
		{
			// ...otherwise we evaluate the condition
			final boolean isConditionTrue;
			try
			{
				isConditionTrue = evaluatableCondition.evaluate(context);
			} catch (IndeterminateEvaluationException e)
			{
				// if it was INDETERMINATE, then that's what we return
				/*
				 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error,
				 * therefore lower level than Error level)
				 */
				LOGGER.info("{}/Condition -> Indeterminate", this, e);
				final DecisionResult result = new BaseDecisionResult(e.getStatus());
				LOGGER.debug("{} -> {}", this, result);
				return result;
			}

			if (!isConditionTrue)
			{
				LOGGER.debug("{}/Condition -> False", this);
				final DecisionResult result = BaseDecisionResult.NOT_APPLICABLE;
				LOGGER.debug("{} -> {}", this, result);
				return result;
			}

			LOGGER.debug("{}/Condition -> True", this);
		}

		/*
		 * Target match and condition true -> return Rule's effect as decision
		 */
		if (effectMatchPepActionExps == null)
		{
			// no obligations/advice
			LOGGER.debug("{} -> {}", this, nullActionsRuleDecisionResult);
			return nullActionsRuleDecisionResult;
		}

		/*
		 * Evaluate obligations/advice we have already filtered out obligations/advice that do not apply to Rule's effect, after calling
		 * PepActionExpressionsEvaluator.getInstance(..., effect) in the constructor. So no need to do it again, that's why Effect not used as argument to
		 * evaluate() here.
		 */
		final PepActions pepActions;
		try
		{
			pepActions = this.effectMatchPepActionExps.evaluate(context);
		} catch (IndeterminateEvaluationException e)
		{
			/*
			 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error,
			 * therefore lower level than Error level)
			 */
			LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", this, e);

			/*
			 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute evaluates
			 * to "Indeterminate", then the whole rule, policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
			 */
			final BaseDecisionResult result = new BaseDecisionResult(e.getStatus());
			LOGGER.debug("{} -> {}", this, result);
			return result;
		}

		final BaseDecisionResult result = new BaseDecisionResult(this.effectAsDecision, pepActions);
		LOGGER.debug("{} -> {}", this, result);
		return result;
	}

	@Override
	public String toString()
	{
		return toString;
	}

}
