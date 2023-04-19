/*
 * Copyright 2012-2023 THALES.
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

import com.google.common.collect.ImmutableList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluator;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluators;
import org.ow2.authzforce.core.pdp.impl.PepActionExpression;
import org.ow2.authzforce.core.pdp.impl.TargetEvaluators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * XACML Rule evaluators
 * 
 * @version $Id: $
 */
public final class RuleEvaluators
{
	private static final IllegalArgumentException NULL_XACML_RULE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Cannot create Rule evaluator: undefined input XACML/JAXB Rule element");

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluator.class);

	private RuleEvaluators() {
		// prevent instantiation
	}

	/** RuleEvaluator subclass used for Rules that always return NotApplicable because their Condition's expression is constant False (cf. section 7.11 truth table in XACML 3.0 specification)  */
	private static final class NotApplicableRuleEvaluator extends RuleEvaluator {
		private  NotApplicableRuleEvaluator(String ruleId) {
			super(ruleId);
		}

		@Override
		public EffectType getEffect() {
			return null;
		}

		@Override
		public boolean isAlwaysApplicable() {
			return false;
		}

		@Override
		public boolean hasAnyPepAction() {
			return false;
		}

		public boolean isEmptyEquivalent() {
			return false;
		}

		@Override
		public DecisionResult evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
		{
			LOGGER.debug("Evaluating {}: NotApplicable Rule (Condition expression is constant False, cf. section 7.11 of XACML 3.0 specification) -> NotApplicable", this);
			return DecisionResults.SIMPLE_NOT_APPLICABLE;
		}
	}

	/**
	* Instantiates rule from XACML RuleType
	*
	* @param ruleElt
	*            Rule element definition (not null)
	* @param xPathCompiler
	*            XPath compiler, defined if XPath support enabled (by PDP configuration and some enclosing Policy(Set) defines a XPathVersion according to XACML standard)
	* @param expressionFactory
	*            Expression parser/factory
	* @throws java.lang.IllegalArgumentException
	*            Undefined rule element, Invalid Target, Condition or Obligation/Advice expressions
	*/
	public static RuleEvaluator getInstance(final Rule ruleElt, final ExpressionFactory expressionFactory,  final Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException
	{
		if (ruleElt == null)
		{
			throw NULL_XACML_RULE_ARGUMENT_EXCEPTION;
		}

		final String ruleId = ruleElt.getRuleId();

		final Condition condElt = ruleElt.getCondition();

		/*
		* Rule's condition considered as always True if condElt = null
		*/
		final BooleanEvaluator conditionEvaluator;
		if (condElt == null)
		{
			conditionEvaluator = BooleanEvaluators.TRUE;
		} else
		{
			try
			{
				conditionEvaluator = ConditionEvaluators.getInstance(condElt, expressionFactory, xPathCompiler);
			} catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Rule [" + ruleId + "]: invalid Condition", e);
			}

			if (conditionEvaluator == BooleanEvaluators.FALSE) {
				// always NotApplicable, see section 7.11 of XACML 3.0
				return new NotApplicableRuleEvaluator(ruleId);
			}
		}

		// Condition not null and not constant False
		return new ApplicableRuleEvaluator(ruleId, ruleElt.getEffect(), ruleElt.getTarget(), conditionEvaluator, ruleElt.getObligationExpressions(), ruleElt.getAdviceExpressions(), expressionFactory, xPathCompiler);
	}

	/**
	 * Rule decision result factory
	 * 
	 * @version $Id: $
	 */
	private interface DecisionResultFactory
	{
		EffectType getDecisionType();

		DecisionResult getInstance(EvaluationContext context, final Optional<EvaluationContext> mdpContext);

		DecisionResult newIndeterminate(IndeterminateEvaluationException e);

		boolean hasAnyPepAction();

	}

	private static final DecisionResultFactory PERMIT_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY = new DecisionResultFactory()
	{

		@Override
		public DecisionResult getInstance(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
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

		@Override
		public boolean hasAnyPepAction()
		{
			return false;
		}

	};

	private static final DecisionResultFactory DENY_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY = new DecisionResultFactory()
	{

		@Override
		public DecisionResult getInstance(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
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

		@Override
		public boolean hasAnyPepAction()
		{
			return false;
		}

	};

	private static abstract class DecisionWithPepActionResultFactory implements DecisionResultFactory
	{
		private final String ruleId;
		private final List<PepActionExpression> rulePepActionExpressions;
		private final DecisionType ruleEffectAsDecision;
		private transient final int numOfPepActionExpressions;

		private DecisionWithPepActionResultFactory(final String ruleId, final DecisionType ruleEffectAsDecision, final List<PepActionExpression> rulePepActionExpressions)
		{
			assert ruleId != null && rulePepActionExpressions != null;

			this.ruleId = ruleId;
			this.rulePepActionExpressions = rulePepActionExpressions;
			this.numOfPepActionExpressions = rulePepActionExpressions.size();
			this.ruleEffectAsDecision = ruleEffectAsDecision;
		}

		@Override
		public boolean hasAnyPepAction()
		{
			return true;
		}

		protected abstract DecisionResult getInstance(ImmutableList<PepAction> pepActions);

		@Override
		public DecisionResult getInstance(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
		{
			/*
			 * Evaluate obligations/advice elements. We have already filtered out obligations/advice that do not apply to Rule's effect in the constructor. So no need to do it again, that's why the
			 * rule's effect is not checked again here.
			 */
			/*
			 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute evaluates to "Indeterminate", then the whole rule,
			 * policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
			 */

			final List<PepAction> pepActions = new ArrayList<>(numOfPepActionExpressions);
			for (final PepActionExpression pepActionExpr : this.rulePepActionExpressions)
			{
				final PepAction pepAction;
				try
				{
					pepAction = pepActionExpr.evaluate(context, mdpContext);
				} catch (final IndeterminateEvaluationException e)
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

				pepActions.add(pepAction);
			}

			return getInstance(ImmutableList.copyOf(pepActions));
		}

		@Override
		public DecisionResult newIndeterminate(final IndeterminateEvaluationException e)
		{
			return DecisionResults.newIndeterminate(ruleEffectAsDecision, e, null);
		}
	}

	private static final class PermitDecisionWithPepActionResultFactory extends DecisionWithPepActionResultFactory
	{
		private PermitDecisionWithPepActionResultFactory(final String ruleId, final List<PepActionExpression> rulePepActionExpressions)
		{
			super(ruleId, DecisionType.PERMIT, rulePepActionExpressions);
		}

		@Override
		protected DecisionResult getInstance(final ImmutableList<PepAction> pepActions)
		{
			return DecisionResults.getPermit(Optional.empty(), pepActions, null);
		}

		@Override
		public EffectType getDecisionType()
		{
			return EffectType.PERMIT;
		}
	}

	private static final class DenyDecisionWithPepActionResultFactory extends DecisionWithPepActionResultFactory
	{
		private DenyDecisionWithPepActionResultFactory(final String ruleId, final List<PepActionExpression> rulePepActionExpressions)
		{
			super(ruleId, DecisionType.DENY, rulePepActionExpressions);
		}

		@Override
		protected DecisionResult getInstance(final ImmutableList<PepAction> pepActions)
		{
			return DecisionResults.getDeny(Optional.empty(), pepActions, null);
		}

		@Override
		public EffectType getDecisionType()
		{
			return EffectType.DENY;
		}
	}

	private static final class ApplicableRuleEvaluator extends RuleEvaluator {

		// non-null
		private final BooleanEvaluator targetEvaluator;

		// non-null
		private final BooleanEvaluator conditionEvaluator;

		// non-null
		private final DecisionResultFactory decisionResultFactory;

		private final transient boolean isAlwaysApplicable;

		/**
		* Instantiates rule from XACML RuleType
		*
		* @param ruleId
		*            Rule ID
		* @param xPathCompiler
		*            XPath compiler, defined if XPath support enabled (by PDP configuration and some enclosing Policy(Set) defines a XPathVersion according to XACML standard)
		* @param expressionFactory
		*            Expression parser/factory
		* @throws java.lang.IllegalArgumentException
		*             Invalid Target, Condition or Obligation/Advice expressions
		*/
		private ApplicableRuleEvaluator(final String ruleId, final EffectType ruleEffect, final Target target, final BooleanEvaluator conditionEvaluator, final ObligationExpressions obligationExpressions, final AdviceExpressions adviceExpressions, final ExpressionFactory expressionFactory,  final Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException
		{
			super(ruleId);
			this.targetEvaluator = TargetEvaluators.getInstance(target, expressionFactory, xPathCompiler);
			this.conditionEvaluator = conditionEvaluator;
			this.isAlwaysApplicable = this.targetEvaluator == TargetEvaluators.MATCH_ALL_TARGET_EVALUATOR && this.conditionEvaluator == BooleanEvaluators.TRUE;

			/*
			* Final decision result depends on rule's effect and Obligation/Advice elements
			*/
			final List<PepActionExpression> pepActionExpressions;
			final List<ObligationExpression> obligationExpList = obligationExpressions == null ? Collections.emptyList() : obligationExpressions.getObligationExpressions();
			final List<AdviceExpression> adviceExpList = adviceExpressions == null ? Collections.emptyList() : adviceExpressions.getAdviceExpressions();
			if (obligationExpList.isEmpty() && adviceExpList.isEmpty())
			{

				pepActionExpressions = Collections.emptyList();
			} else
			{
				pepActionExpressions = new ArrayList<>(obligationExpList.size() + adviceExpList.size());
				obligationExpList.forEach(obligationExp -> {
					final String pepActionId = obligationExp.getObligationId();
					if (obligationExp.getFulfillOn() != ruleEffect)
					{
						LOGGER.warn("{}: ignoring Obligation '{}' because fulfillOn does not match the rule's effect", this, pepActionId);
						return;
					}

					pepActionExpressions.add(new PepActionExpression(pepActionId, true, obligationExp.getAttributeAssignmentExpressions(), expressionFactory, xPathCompiler));
				});

				adviceExpList.forEach(adviceExp -> {
					final String pepActionId = adviceExp.getAdviceId();
					if (adviceExp.getAppliesTo() != ruleEffect)
					{
						LOGGER.warn("{}: ignoring Advice '{}' because appliesTo does not match the rule's effect", this, pepActionId);
						return;
					}

					pepActionExpressions.add(new PepActionExpression(pepActionId, false, adviceExp.getAttributeAssignmentExpressions(), expressionFactory, xPathCompiler));
				});

				/*
				* Since obligations/advice with non-matching fulfillOn/appliesTo filtered, final pepActionExpressions may still be empty
				*/
			}

			if (pepActionExpressions.isEmpty())
			{
				/*
				* No PEP obligation/advice -> rule is (equivalent to) empty rule if also target matches all and condition is null or always True
				*/
				/*
				* No PEP obligation/advice -> rule is (equivalent to) empty rule if also target matches all and condition is null or always True
				*/
				this.decisionResultFactory = ruleEffect == EffectType.DENY ? DENY_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY : PERMIT_DECISION_WITHOUT_PEP_ACTION_RESULT_FACTORY;
			} else
			{
				this.decisionResultFactory = ruleEffect == EffectType.DENY ? new DenyDecisionWithPepActionResultFactory(ruleId, pepActionExpressions)
						: new PermitDecisionWithPepActionResultFactory(ruleId, pepActionExpressions);
			}
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
		* @return true iff it has any PEP action
		*/
		public boolean hasAnyPepAction()
		{
			return this.decisionResultFactory.hasAnyPepAction();
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
			return this.isAlwaysApplicable && !this.decisionResultFactory.hasAnyPepAction();
		}

		/**
		* {@inheritDoc}
		*
		* Evaluates the rule against the supplied context. This will check that the target matches, and then try to evaluate the condition. If the target and condition apply, then the rule's effect is returned.
		* <p>
		* Note that rules are not required to have targets. If no target is specified, then the rule inherits its parent's target. In the event that this <code>RuleEvaluator</code> has no
		* <code>Target</code> then the match is assumed to be true, since evaluating a policy tree to this level required the parent's target to match. In debug level, this method logs the evaluation
		* result before return. Indeterminate results are logged in warn level only (which "includes" debug level).
		*/
		@Override
		public DecisionResult evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
		{
			LOGGER.debug("Evaluating {}", this);
			try
			{
				if (!targetEvaluator.evaluate(context, mdpContext))
				{
					LOGGER.debug("{}/Target -> No-match", this);
					final DecisionResult result = DecisionResults.SIMPLE_NOT_APPLICABLE;
					LOGGER.debug("{} -> {}", this, result);
					return result;
				}

				LOGGER.debug("{}/Target -> Match", this);
			} catch (final IndeterminateEvaluationException e)
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
				isConditionTrue = conditionEvaluator.evaluate(context, mdpContext);
			} catch (final IndeterminateEvaluationException e)
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
			final DecisionResult result = this.decisionResultFactory.getInstance(context, mdpContext);
			LOGGER.debug("{} -> {}", this, result);
			return result;
		}
	}

}
