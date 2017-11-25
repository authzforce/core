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
package org.ow2.authzforce.core.pdp.impl.rule;

import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluator;
import org.ow2.authzforce.core.pdp.impl.PepActionExpression;
import org.ow2.authzforce.core.pdp.impl.PepActionExpressions;
import org.ow2.authzforce.core.pdp.impl.PepActionFactories;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates a XACML Rule to a Decision.
 *
 * 
 * @version $Id: $
 */
public final class RuleEvaluator implements Decidable
{
	private static final IllegalArgumentException NULL_XACML_RULE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Cannot create Rule evaluator: undefined input XACML/JAXB Rule element");

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluator.class);

	/**
	 * Rule-associated PEP action (obligation/advice) expressions parser used to initialize the evaluator's fields
	 * 
	 */
	private static final class RulePepActionExpressions implements PepActionExpressions
	{
		private final XPathCompiler xPathCompiler;
		private final ExpressionFactory expFactory;
		private final PepActionExpressions.EffectSpecific ruleEffectMatchingActionExpressions;

		/**
		 * Creates instance
		 * 
		 * @param xPathCompiler
		 *            XPath compiler corresponding to enclosing policy(set) default XPath version
		 * @param expressionFactory
		 *            expression factory for parsing expressions
		 * @param ruleEffect
		 *            XACML rule's Effect
		 */
		private RulePepActionExpressions(final XPathCompiler xPathCompiler, final ExpressionFactory expressionFactory, final EffectType ruleEffect)
		{
			assert ruleEffect != null;

			this.ruleEffectMatchingActionExpressions = new EffectSpecific(ruleEffect);
			this.xPathCompiler = xPathCompiler;
			this.expFactory = expressionFactory;
		}

		@Override
		public void add(final ObligationExpression jaxbObligationExp) throws IllegalArgumentException
		{
			assert jaxbObligationExp != null;

			final PepActionExpression<Obligation> obligationExp = new PepActionExpression<>(PepActionFactories.OBLIGATION_FACTORY, jaxbObligationExp.getObligationId(),
					jaxbObligationExp.getFulfillOn(), jaxbObligationExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final boolean isMatching = ruleEffectMatchingActionExpressions.addObligationExpression(obligationExp);
			if (LOGGER.isWarnEnabled() && !isMatching)
			{
				LOGGER.warn("Ignored ObligationExpression[@ObligationId='{}'] because @FulfillOn = {} does not match the rule's Effect = {}", jaxbObligationExp.getObligationId(),
						jaxbObligationExp.getFulfillOn(), ruleEffectMatchingActionExpressions.getEffect());
			}
		}

		@Override
		public void add(final AdviceExpression jaxbAdviceExp) throws IllegalArgumentException
		{
			assert jaxbAdviceExp != null;

			final PepActionExpression<Advice> adviceExp = new PepActionExpression<>(PepActionFactories.ADVICE_FACTORY, jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(),
					jaxbAdviceExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final boolean isMatching = ruleEffectMatchingActionExpressions.addAdviceExpression(adviceExp);
			if (LOGGER.isWarnEnabled() && !isMatching)
			{
				LOGGER.warn("Ignored AdviceExpression[@AdviceId='{}'] because @AppliesTo = {} does not match the rule's Effect = {}", jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(),
						ruleEffectMatchingActionExpressions.getEffect());
			}
		}

		@Override
		public List<PepActionExpression<Obligation>> getObligationExpressionList()
		{
			return ruleEffectMatchingActionExpressions.getObligationExpressions();
		}

		@Override
		public List<PepActionExpression<Advice>> getAdviceExpressionList()
		{
			return ruleEffectMatchingActionExpressions.getAdviceExpressions();
		}
	}

	private static final class RulePepActionExpressionsFactory implements PepActionExpressions.Factory<RulePepActionExpressions>
	{
		private final EffectType ruleEffect;

		private RulePepActionExpressionsFactory(final EffectType ruleEffect)
		{
			assert ruleEffect != null;

			this.ruleEffect = ruleEffect;
		}

		@Override
		public RulePepActionExpressions getInstance(final XPathCompiler xPathCompiler, final ExpressionFactory expressionFactory)
		{
			return new RulePepActionExpressions(xPathCompiler, expressionFactory, ruleEffect);
		}

	}

	/**
	 * Rule decision result factory
	 *
	 * 
	 * @version $Id: $
	 */
	private interface DecisionResultFactory
	{
		EffectType getDecisionType();

		DecisionResult getInstance(EvaluationContext context);

		DecisionResult newIndeterminate(IndeterminateEvaluationException e);

	}

	private static final DecisionResultFactory PERMIT_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY = new DecisionResultFactory()
	{

		@Override
		public DecisionResult getInstance(final EvaluationContext context)
		{
			return DecisionResults.SIMPLE_PERMIT;
		}

		@Override
		public DecisionResult newIndeterminate(final IndeterminateEvaluationException e)
		{
			assert e != null;

			return DecisionResults.newIndeterminate(DecisionType.PERMIT, e, null);
		}

		@Override
		public EffectType getDecisionType()
		{
			return EffectType.PERMIT;
		}

	};

	private static final DecisionResultFactory DENY_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY = new DecisionResultFactory()
	{

		@Override
		public DecisionResult getInstance(final EvaluationContext context)
		{
			return DecisionResults.SIMPLE_DENY;
		}

		@Override
		public DecisionResult newIndeterminate(final IndeterminateEvaluationException e)
		{
			assert e != null;

			return DecisionResults.newIndeterminate(DecisionType.DENY, e, null);
		}

		@Override
		public EffectType getDecisionType()
		{
			return EffectType.DENY;
		}

	};

	private static abstract class DecisionWithPepActionResultFactory implements DecisionResultFactory
	{
		private final String ruleId;
		private final PepActionExpressions.EffectSpecific rulePepActionExpressions;
		private final DecisionType ruleEffectAsDecision;

		private DecisionWithPepActionResultFactory(final String ruleId, final PepActionExpressions.EffectSpecific rulePepActionExpressions)
		{
			assert ruleId != null && rulePepActionExpressions != null;

			this.ruleId = ruleId;
			this.rulePepActionExpressions = rulePepActionExpressions;
			final EffectType ruleEffect = rulePepActionExpressions.getEffect();
			this.ruleEffectAsDecision = ruleEffect == EffectType.DENY ? DecisionType.DENY : DecisionType.PERMIT;
		}

		protected abstract DecisionResult getInstance(ImmutablePepActions pepActions);

		@Override
		public DecisionResult getInstance(final EvaluationContext context)
		{
			/*
			 * Evaluate obligations/advice elements. We have already filtered out obligations/advice that do not apply to Rule's effect in the constructor. So no need to do it again, that's why the
			 * rule's effect is not checked again here.
			 */
			/*
			 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute evaluates to "Indeterminate", then the whole rule,
			 * policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
			 */

			final ImmutablePepActions pepActions;
			try
			{
				pepActions = PepActionExpressions.Helper.evaluate(rulePepActionExpressions, context);
			}
			catch (final IndeterminateEvaluationException e)
			{
				/*
				 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error, therefore lower level than error).
				 */
				LOGGER.info("Rule['{}']/{Obligation|Advice}Expressions -> Indeterminate", ruleId, e);
				/*
				 * Create an Indeterminate Decision Result For the Extended Indeterminate, we do like for Target or Condition evaluation in section 7.11 (same as the rule's Effect).
				 */
				return newIndeterminate(e);
			}

			return getInstance(pepActions);
		}

		@Override
		public DecisionResult newIndeterminate(final IndeterminateEvaluationException e)
		{
			return DecisionResults.newIndeterminate(ruleEffectAsDecision, e, null);
		}
	}

	private static final class PermitDecisionWithPepActionResutFactory extends DecisionWithPepActionResultFactory
	{
		private PermitDecisionWithPepActionResutFactory(final String ruleId, final PepActionExpressions.EffectSpecific rulePepActionExpressions)
		{
			super(ruleId, rulePepActionExpressions);
			assert rulePepActionExpressions.getEffect() == EffectType.PERMIT;
		}

		@Override
		protected DecisionResult getInstance(final ImmutablePepActions pepActions)
		{
			return DecisionResults.getPermit(null, pepActions, null);
		}

		@Override
		public EffectType getDecisionType()
		{
			return EffectType.PERMIT;
		}
	}

	private static final class DenyDecisionWithPepActionResutFactory extends DecisionWithPepActionResultFactory
	{
		private DenyDecisionWithPepActionResutFactory(final String ruleId, final PepActionExpressions.EffectSpecific rulePepActionExpressions)
		{
			super(ruleId, rulePepActionExpressions);
			assert rulePepActionExpressions.getEffect() == EffectType.DENY;
		}

		@Override
		protected DecisionResult getInstance(final ImmutablePepActions pepActions)
		{
			return DecisionResults.getDeny(null, pepActions, null);
		}

		@Override
		public EffectType getDecisionType()
		{
			return EffectType.DENY;
		}
	}

	private static final BooleanEvaluator TRUE_CONDITION = new BooleanEvaluator()
	{

		@Override
		public boolean evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			LOGGER.debug("Condition null -> True");
			return true;
		}
	};

	// non-null
	private final BooleanEvaluator targetEvaluator;

	// non-null
	private final BooleanEvaluator conditionEvaluator;

	// non-null
	private final String ruleId;

	// non-null
	private final DecisionResultFactory decisionResultFactory;

	private final transient boolean isAlwaysApplicable;

	private final transient boolean hasNoPepAction;

	private final transient String toString;

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
		if (ruleElt == null)
		{
			throw NULL_XACML_RULE_ARGUMENT_EXCEPTION;
		}

		// JAXB fields initialization
		this.ruleId = ruleElt.getRuleId();

		this.toString = "Rule['" + ruleId + "']";

		this.targetEvaluator = TargetEvaluators.getInstance(ruleElt.getTarget(), xPathCompiler, expressionFactory);

		final Condition condElt = ruleElt.getCondition();

		/*
		 * Rule's condition considered as always True if condElt = null
		 */
		if (condElt == null)
		{
			this.conditionEvaluator = TRUE_CONDITION;
		}
		else
		{
			try
			{
				this.conditionEvaluator = ConditionEvaluators.getInstance(condElt, xPathCompiler, expressionFactory);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this + ": invalid Condition", e);
			}
		}

		this.isAlwaysApplicable = this.targetEvaluator == TargetEvaluators.MATCH_ALL_TARGET_EVALUATOR && this.conditionEvaluator == ConditionEvaluators.TRUE_CONDITION;

		/*
		 * Final decision result depends on rule's effect and Obligation/Advice elements
		 */
		final EffectType effect = ruleElt.getEffect();
		final ObligationExpressions obligationExps = ruleElt.getObligationExpressions();
		final AdviceExpressions adviceExps = ruleElt.getAdviceExpressions();
		if ((obligationExps == null || obligationExps.getObligationExpressions().isEmpty()) && (adviceExps == null || adviceExps.getAdviceExpressions().isEmpty()))
		{
			/*
			 * No PEP obligation/advice -> rule is (equivalent to) empty rule if also target matches all and condition is null or always True
			 */

			this.hasNoPepAction = true;
			this.decisionResultFactory = effect == EffectType.DENY ? DENY_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY : PERMIT_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY;
		}
		else
		{
			this.hasNoPepAction = false;
			final List<ObligationExpression> obligationExpList = obligationExps == null ? null : obligationExps.getObligationExpressions();
			final List<AdviceExpression> adviceExpList = adviceExps == null ? null : adviceExps.getAdviceExpressions();
			final RulePepActionExpressions rulePepActionExpressions;
			try
			{
				rulePepActionExpressions = PepActionExpressions.Helper.parseActionExpressions(obligationExpList, adviceExpList, xPathCompiler, expressionFactory, new RulePepActionExpressionsFactory(
						effect));
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(this + ": Invalid AttributeAssignmentExpression(s)", e);
			}

			this.decisionResultFactory = effect == EffectType.DENY ? new DenyDecisionWithPepActionResutFactory(ruleId, rulePepActionExpressions.ruleEffectMatchingActionExpressions)
					: new PermitDecisionWithPepActionResutFactory(ruleId, rulePepActionExpressions.ruleEffectMatchingActionExpressions);
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
	 * Get evaluated rule Effect (Permit/Deny) when applicable
	 *
	 * @return evaluated rule Effect
	 */
	public EffectType getEffect()
	{
		return this.decisionResultFactory.getDecisionType();
	}

	/**
	 * Is the rule always applicable, i.e. applies to all requests, i.e. the rule's Target matches all, and the condition is undefined or always evaluates to True?
	 * <p>
	 * Knowing that a rule is always applicable is useful for optimizing combining algorithm evaluators at initialization time, e.g. First-applicable algorithm.
	 * 
	 * @return true iff it has no PEP action
	 */
	public boolean isAlwaysApplicable()
	{
		return this.isAlwaysApplicable;
	}

	/**
	 * Does the rule has any PEP action (obligation/advice) ?
	 * <p>
	 * Knowing that a rule has no PEP action is useful for optimizing combining algorithm evaluators at initialization time, e.g. deny-unless-permit/permit-unless-deny algorithms.
	 * 
	 * @return true iff it has no PEP action
	 */
	public boolean hasNoPepAction()
	{
		return this.hasNoPepAction;
	}

	/**
	 * Is the rule (equivalent to) an empty rule? I.e. the rule's Target matches all, the condition is undefined or always evaluates to True, and there is no PEP action (obligation/advice), in other
	 * words the rule always evaluates to the simple Permit/Deny decision corresponding to its Effect.
	 * <p>
	 * Knowing that a rule is empty(-equivalent) is useful for optimizing combining algorithm evaluators at initialization time, e.g. (ordered-)permit-overrides/deny-overrides algorithms.
	 * 
	 * @return true iff it is empty equivalent (empty rule or equivalent to an empty rule)
	 */
	public boolean isEmptyEquivalent()
	{
		return this.isAlwaysApplicable && this.hasNoPepAction;
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
		try
		{
			if (!targetEvaluator.evaluate(context))
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
			final DecisionResult result = decisionResultFactory.newIndeterminate(e);
			LOGGER.debug("{} -> {}", this, result);
			return result;
		}

		/*
		 * Target matches -> check Rule's condition. See section 7.9 of XACML core spec, so result is the Rule's Effect, unless condition evaluates to False or throws Indeterminate exception.
		 */
		final boolean isConditionTrue;
		try
		{
			isConditionTrue = conditionEvaluator.evaluate(context);
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
			final DecisionResult result = decisionResultFactory.newIndeterminate(e);
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

		/*
		 * Target match and condition true
		 */
		final DecisionResult result = this.decisionResultFactory.getInstance(context);
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
