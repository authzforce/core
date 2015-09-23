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
package com.thalesgroup.authzforce.core.eval;

import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PepActions;

/**
 * Evaluator of a Rule's PEP action (Obligation/Advice) expressions
 * 
 */
public class RulePepActionExpressionsEvaluator extends PepActionExpressionsEvaluator
{
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(RulePepActionExpressionsEvaluator.class);

	/**
	 * Rule-associated PEP action (obligation/advice) expressions parser used to initialize the
	 * evaluator's fields
	 * 
	 */
	private static class ActionExpressionsParser implements PepActionExpressions
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(ActionExpressionsParser.class);

		private final DefaultsType policyDefaults;
		private final ExpressionFactory expFactory;
		private final PepActionExpressions.EffectSpecific ruleEffectMatchingActionExpressions;

		/**
		 * Creates instance
		 * 
		 * @param policyDefaults
		 *            Enclosing policy default parameters for parsing expressions
		 * @param expressionFactory
		 *            expression factory for parsing expressions
		 * @param ruleEffect
		 *            XACML rule's Effect
		 */
		private ActionExpressionsParser(DefaultsType policyDefaults, ExpressionFactory expressionFactory, EffectType ruleEffect)
		{
			if (ruleEffect == null)
			{
				throw new IllegalArgumentException("Undefined Rule's Effect to which obligations/advice must apply");
			}

			this.ruleEffectMatchingActionExpressions = new EffectSpecific(ruleEffect);
			this.policyDefaults = policyDefaults;
			this.expFactory = expressionFactory;
		}

		@Override
		public void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp) throws ParsingException
		{
			final ObligationExpression obligationExp = new ObligationExpression(jaxbObligationExp, policyDefaults, expFactory);
			final boolean isMatching = ruleEffectMatchingActionExpressions.add(obligationExp);
			if (LOGGER.isWarnEnabled() && !isMatching)
			{
				LOGGER.warn("Ignored ObligationExpression[@ObligationId='{}'] because @FulfillOn = {} does not match the rule's Effect = {}", jaxbObligationExp.getObligationId(), jaxbObligationExp.getFulfillOn(), ruleEffectMatchingActionExpressions.getEffect());
			}
		}

		@Override
		public void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp) throws ParsingException
		{
			final AdviceExpression adviceExp = new AdviceExpression(jaxbAdviceExp, policyDefaults, expFactory);
			final boolean isMatching = ruleEffectMatchingActionExpressions.add(adviceExp);
			if (LOGGER.isWarnEnabled() && !isMatching)
			{
				LOGGER.warn("Ignored AdviceExpression[@AdviceId='{}'] because @AppliesTo = {} does not match the rule's Effect = {}", jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(), ruleEffectMatchingActionExpressions.getEffect());
			}
		}

		@Override
		public List<ObligationExpression> getObligationExpressionList()
		{
			return ruleEffectMatchingActionExpressions.getObligationExpressions();
		}

		@Override
		public List<AdviceExpression> getAdviceExpressionList()
		{
			return ruleEffectMatchingActionExpressions.getAdviceExpressions();
		}
	}

	private static class ActionExpressionsFactory implements PepActionExpressions.Factory<ActionExpressionsParser>
	{
		private final EffectType ruleEffect;

		private ActionExpressionsFactory(EffectType ruleEffect)
		{
			this.ruleEffect = ruleEffect;
		}

		@Override
		public ActionExpressionsParser getInstance(DefaultsType policyDefaults, ExpressionFactory expressionFactory)
		{
			return new ActionExpressionsParser(policyDefaults, expressionFactory, ruleEffect);
		}

	}

	private final ObligationExpressions jaxbObligationExpressions;
	private final AdviceExpressions jaxbAdviceExpressions;
	private final PepActionExpressions.EffectSpecific ruleEffectMatchingActionExpressions;

	private RulePepActionExpressionsEvaluator(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory, EffectType effect) throws ParsingException
	{
		final ActionExpressionsParser actionExpressionsParser = super.parseActionExpressions(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory, new ActionExpressionsFactory(effect));
		this.jaxbObligationExpressions = new ObligationExpressions(Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression> unmodifiableList(actionExpressionsParser.getObligationExpressionList()));
		this.jaxbAdviceExpressions = new AdviceExpressions(Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression> unmodifiableList(actionExpressionsParser.getAdviceExpressionList()));
		this.ruleEffectMatchingActionExpressions = actionExpressionsParser.ruleEffectMatchingActionExpressions;
	}

	/**
	 * Instantiates the evaluator with given XACML-schema-derived
	 * ObligationExpressions/AdviceExpressions an Effect to be match by these expressions (a priori
	 * specific to a Rule)
	 * 
	 * @param jaxbObligationExpressions
	 *            XACML-schema-derived ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML-schema-derived AdviceExpressions
	 * @param policyDefaults
	 *            enclosing policy default parameters, e.g. XPath version
	 * @param expFactory
	 *            Expression factory for parsing the AttributeAssignmentExpressions in the
	 *            Obligation/Advice Expressions
	 * @param effect
	 *            rule's Effect to be matched by ObligationExpressions/FulfillOn and
	 *            AdviceExpressions/AppliesTo
	 * @return Rule's Obligation/Advice expressions evaluator
	 * @throws ParsingException
	 *             if error parsing one of the AttributeAssignmentExpressions
	 */
	public static RulePepActionExpressionsEvaluator getInstance(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory, EffectType effect) throws ParsingException
	{
		if ((jaxbObligationExpressions == null || jaxbObligationExpressions.getObligationExpressions().isEmpty()) && (jaxbAdviceExpressions == null || jaxbAdviceExpressions.getAdviceExpressions().isEmpty()))
		{
			return null;
		}

		return new RulePepActionExpressionsEvaluator(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory, effect);
	}

	/**
	 * Evaluates the PEP action (obligations/Advice) expressions in a given evaluation context
	 * 
	 * @param context
	 *            evaluation context
	 * @return PEP actions (obligations/advices) or null if none
	 * @throws IndeterminateEvaluationException
	 *             error evaluating one of ObligationExpression/AdviceExpressions'
	 *             AttributeAssignmentExpressions' expressions
	 */
	public PepActions evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return super.evaluate(this.ruleEffectMatchingActionExpressions, context);
	}

	@Override
	public ObligationExpressions getObligationExpressions()
	{
		return this.jaxbObligationExpressions;
	}

	@Override
	public AdviceExpressions getAdviceExpressions()
	{
		return this.jaxbAdviceExpressions;
	}

}
