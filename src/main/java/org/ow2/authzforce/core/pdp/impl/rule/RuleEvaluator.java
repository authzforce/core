/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.rule;

import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Evaluates a XACML Rule to a Decision.
 *
 * 
 * @version $Id: $
 */
public class RuleEvaluator implements Decidable
{
	private static class RuleDecisionResult implements DecisionResult {
		
		private final DecisionType decision;
		
		/**
		 * Extended Indeterminate value, as defined in section 7.10 of XACML 3.0 core:
		 * <i>potential effect value which could have occurred if there would not
		 * have been an error causing the “Indeterminate”</i>. We use the following
		 * convention:
		 * <ul>
		 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
		 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
		 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
		 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means
		 * the decision is not Indeterminate, and therefore any extended
		 * Indeterminate value should be ignored</li>
		 * </ul>
		 * 
		 */
		private final DecisionType extIndeterminate;
		
		private final Status status;

		// initialized non-null
		private final ImmutablePepActions pepActions;

		private transient volatile int hashCode = 0;

		private RuleDecisionResult(final DecisionType decision, final DecisionType extendedIndeterminate, final Status status, final ImmutablePepActions pepActions)
		{
			assert decision != null && extendedIndeterminate != null;

			this.decision = decision;
			this.extIndeterminate = extendedIndeterminate;
			this.status = status;
			this.pepActions = pepActions == null ? new ImmutablePepActions(null, null) : pepActions;
		}
		
		/** {@inheritDoc} */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				hashCode = Objects.hash(this.decision, this.extIndeterminate, this.status, this.pepActions);
			}

			return hashCode;
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof DecisionResult))
			{
				return false;
			}

			final RuleDecisionResult other = (RuleDecisionResult) obj;
			if (this.decision != other.getDecision())
			{
				return false;
			}

			if (this.extIndeterminate != other.getExtendedIndeterminate())
			{
				return false;
			}

			// Status is optional in XACML
			if (this.status == null)
			{
				if (other.getStatus() != null)
				{
					return false;
				}
			}
			else if (!this.status.equals(other.getStatus()))
			{
				return false;
			}

			// this.getObligations() derived from this.pepActions
			// pepActions never null
			// applicablePolicyIdList never null
			return this.pepActions.equals(other.getPepActions());
		}


		@Override
		public DecisionType getDecision() {
			return this.decision;
		}

		@Override
		public DecisionType getExtendedIndeterminate() {
			return DecisionType.NOT_APPLICABLE;
		}

		@Override
		public PepActions getPepActions() {
			return this.pepActions;
		}

		@Override
		public Status getStatus() {
			return this.status;
		}
		
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluator.class);

	private final transient DecisionType effectAsDecision;
	private final transient TargetEvaluator evaluatableTarget;
	private final transient ConditionEvaluator evaluatableCondition;
	private final transient RulePepActionExpressionsEvaluator effectMatchPepActionExps;
	private final transient DecisionResult nullActionsRuleDecisionResult;
	private final transient String toString;
	private final String ruleId;

	/**
	 * Instantiates rule from XACML RuleType
	 *
	 * @param ruleElt
	 *            Rule element definition
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expressionFactory
	 *            Expression parser/factory
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid Target, Condition or Obligation/Advice expressions
	 */
	public RuleEvaluator(final Rule ruleElt, final XPathCompiler xPathCompiler, final ExpressionFactory expressionFactory) throws IllegalArgumentException
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
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": Invalid Target", e);
		}

		final Condition condElt = ruleElt.getCondition();
		try
		{
			this.evaluatableCondition = condElt == null ? null : new ConditionEvaluator(condElt, xPathCompiler, expressionFactory);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": invalid Condition", e);
		}

		try
		{
			this.effectMatchPepActionExps = RulePepActionExpressionsEvaluator.getInstance(ruleElt.getObligationExpressions(), ruleElt.getAdviceExpressions(), xPathCompiler, expressionFactory, effect);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException(this + ": Invalid ObligationExpressions/AdviceExpressions", e);
		}

		if (this.effectMatchPepActionExps == null)
		{

			this.nullActionsRuleDecisionResult = new RuleDecisionResult(this.effectAsDecision, DecisionType.NOT_APPLICABLE, null, null);
		}
		else
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
	 * Create an Indeterminate Decision Result
	 * For the Extended Indeterminate, we do like for Target or Condition evaluation in section 7.11
			  (same as the rule's Effect).
	 * @param e
	 * @return
	 */
	private RuleDecisionResult newIndeterminateResult(final IndeterminateEvaluationException e) {
		return new RuleDecisionResult(DecisionType.INDETERMINATE, this.effectAsDecision, e.getStatus(), null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the rule against the supplied context. This will check that the target matches, and then try to evaluate the condition. If the target and condition apply, then the rule's effect is
	 * returned in the result.
	 * <p>
	 * Note that rules are not required to have targets. If no target is specified, then the rule inherits its parent's target. In the event that this <code>RuleEvaluator</code> has no
	 * <code>Target</code> then the match is assumed to be true, since evaluating a policy tree to this level required the parent's target to match. In debug level, this method logs the evaluation
	 * result before return. Indeterminate results are logged in warn level only (which "includes" debug level).
	 */
	@Override
	public DecisionResult evaluate(final EvaluationContext context)
	{
		/*
		 * Null or empty Target matches all So we just check if target non-null matches
		 */
		if (evaluatableTarget == null)
		{
			LOGGER.debug("{}/Target (none/empty) -> Match", this);
		}
		else
		{
			try
			{
				if (!evaluatableTarget.match(context))
				{
					LOGGER.debug("{}/Target -> No-match", this);
					final DecisionResult result = DecisionResults.SIMPLE_NOT_APPLICABLE;
					LOGGER.debug("{} -> {}", this, result);
					return result;
				}

				LOGGER.debug("{}/Target -> Match", this);
			}
			catch (final IndeterminateEvaluationException e)
			{
				// Target is Indeterminate
				/*
				 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than error)
				 */
				LOGGER.info("{}/Target -> Indeterminate", this, e);

				/*
				 * Condition is Indeterminate, determine Extended Indeterminate (section 7.11) which is the value of the Rule's Effect
				 */
				final DecisionResult result = newIndeterminateResult(e);
				LOGGER.debug("{} -> {}", this, result);
				return result;
			}
		}

		/*
		 * Target matches -> check condition Rule's condition considered as True if condition = null or condition's expression evaluated to true. See section 7.9 of XACML core spec, so result is the
		 * Rule's Effect, unless condition is not null AND it evaluates to False or throws Indeterminate exception.
		 */
		if (evaluatableCondition == null)
		{
			LOGGER.debug("{}/Condition (none/empty) -> True", this);
		}
		else
		{
			// ...otherwise we evaluate the condition
			final boolean isConditionTrue;
			try
			{
				isConditionTrue = evaluatableCondition.evaluate(context);
			}
			catch (final IndeterminateEvaluationException e)
			{
				/*
				 * Condition is Indeterminate, determine Extended Indeterminate (section 7.11) which is the value of the Rule's Effect
				 */
				/*
				 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but not a critical application error, therefore lower level than Error level)
				 */
				LOGGER.info("{}/Condition -> Indeterminate", this, e);
				final DecisionResult result = newIndeterminateResult(e);
				LOGGER.debug("{} -> {}", this, result);
				return result;
			}

			if (!isConditionTrue)
			{
				LOGGER.debug("{}/Condition -> False", this);
				final DecisionResult result = DecisionResults.SIMPLE_NOT_APPLICABLE;
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
		 * Evaluate obligations/advice we have already filtered out obligations/advice that do not apply to Rule's effect, after calling PepActionExpressionsEvaluator.getInstance(..., effect) in the
		 * constructor. So no need to do it again, that's why Effect not used as argument to evaluate() here.
		 */
		final ImmutablePepActions pepActions;
		try
		{
			pepActions = this.effectMatchPepActionExps.evaluate(context);
		}
		
		catch (final IndeterminateEvaluationException e)
		{
			/*
			 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than Error level)
			 */
			LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", this, e);

			/*
			 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute evaluates to "Indeterminate", then the whole rule,
			 * policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18). For the Extended Indeterminate, we do like for Target or Condition evaluation in section 7.11
			 * (same as the rule's Effect).
			 */
			final DecisionResult result = newIndeterminateResult(e);
			LOGGER.debug("{} -> {}", this, result);
			return result;
		}

		final DecisionResult result = new RuleDecisionResult(this.effectAsDecision, DecisionType.NOT_APPLICABLE, null, pepActions);
		LOGGER.debug("{} -> {}", this, result);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return toString;
	}

}
