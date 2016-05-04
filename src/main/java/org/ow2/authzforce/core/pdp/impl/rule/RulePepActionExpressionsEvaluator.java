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

import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.impl.BasePepActions;
import org.ow2.authzforce.core.pdp.impl.PepActionExpression;
import org.ow2.authzforce.core.pdp.impl.PepActionExpressions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator of a Rule's PEP action (Obligation/Advice) expressions
 *
 * 
 * @version $Id: $
 */
public class RulePepActionExpressionsEvaluator
{
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(RulePepActionExpressionsEvaluator.class);

	/**
	 * Rule-associated PEP action (obligation/advice) expressions parser used to initialize the evaluator's fields
	 * 
	 */
	private static class ActionExpressionsParser implements PepActionExpressions
	{
		private static final IllegalArgumentException UNDEF_RULE_EFFECT_ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Rule's Effect to which obligations/advice must apply");

		private static final Logger LOGGER = LoggerFactory.getLogger(ActionExpressionsParser.class);

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
		private ActionExpressionsParser(XPathCompiler xPathCompiler, ExpressionFactory expressionFactory, EffectType ruleEffect)
		{
			if (ruleEffect == null)
			{
				throw UNDEF_RULE_EFFECT_ILLEGAL_ARGUMENT_EXCEPTION;
			}

			this.ruleEffectMatchingActionExpressions = new EffectSpecific(ruleEffect);
			this.xPathCompiler = xPathCompiler;
			this.expFactory = expressionFactory;
		}

		@Override
		public void add(ObligationExpression jaxbObligationExp) throws IllegalArgumentException
		{
			final PepActionExpression<Obligation> obligationExp = new PepActionExpression<>(BasePepActions.OBLIGATION_FACTORY,
					jaxbObligationExp.getObligationId(), jaxbObligationExp.getFulfillOn(), jaxbObligationExp.getAttributeAssignmentExpressions(),
					xPathCompiler, expFactory);
			final boolean isMatching = ruleEffectMatchingActionExpressions.addObligationExpression(obligationExp);
			if (LOGGER.isWarnEnabled() && !isMatching)
			{
				LOGGER.warn("Ignored ObligationExpression[@ObligationId='{}'] because @FulfillOn = {} does not match the rule's Effect = {}",
						jaxbObligationExp.getObligationId(), jaxbObligationExp.getFulfillOn(), ruleEffectMatchingActionExpressions.getEffect());
			}
		}

		@Override
		public void add(AdviceExpression jaxbAdviceExp) throws IllegalArgumentException
		{
			final PepActionExpression<Advice> adviceExp = new PepActionExpression<>(BasePepActions.ADVICE_FACTORY, jaxbAdviceExp.getAdviceId(),
					jaxbAdviceExp.getAppliesTo(), jaxbAdviceExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final boolean isMatching = ruleEffectMatchingActionExpressions.addAdviceExpression(adviceExp);
			if (LOGGER.isWarnEnabled() && !isMatching)
			{
				LOGGER.warn("Ignored AdviceExpression[@AdviceId='{}'] because @AppliesTo = {} does not match the rule's Effect = {}",
						jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(), ruleEffectMatchingActionExpressions.getEffect());
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

	private static class ActionExpressionsFactory implements PepActionExpressions.Factory<ActionExpressionsParser>
	{
		private final EffectType ruleEffect;

		private ActionExpressionsFactory(EffectType ruleEffect)
		{
			this.ruleEffect = ruleEffect;
		}

		@Override
		public ActionExpressionsParser getInstance(XPathCompiler xPathCompiler, ExpressionFactory expressionFactory)
		{
			return new ActionExpressionsParser(xPathCompiler, expressionFactory, ruleEffect);
		}

	}

	private final PepActionExpressions.EffectSpecific ruleEffectMatchingActionExpressions;

	private RulePepActionExpressionsEvaluator(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions,
			XPathCompiler xPathCompiler, ExpressionFactory expFactory, EffectType effect) throws IllegalArgumentException
	{
		final ActionExpressionsParser actionExpressionsParser = PepActionExpressions.Helper.parseActionExpressions(jaxbObligationExpressions,
				jaxbAdviceExpressions, xPathCompiler, expFactory, new ActionExpressionsFactory(effect));
		this.ruleEffectMatchingActionExpressions = actionExpressionsParser.ruleEffectMatchingActionExpressions;
	}

	/**
	 * Instantiates the evaluator with given XACML-schema-derived ObligationExpressions/AdviceExpressions an Effect to be match by these expressions (a priori
	 * specific to a Rule)
	 *
	 * @param jaxbObligationExpressions
	 *            XACML-schema-derived ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML-schema-derived AdviceExpressions
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing the AttributeAssignmentExpressions in the Obligation/Advice Expressions
	 * @param effect
	 *            rule's Effect to be matched by ObligationExpressions/FulfillOn and AdviceExpressions/AppliesTo
	 * @return Rule's Obligation/Advice expressions evaluator
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the AttributeAssignmentExpressions is invalid
	 */
	public static RulePepActionExpressionsEvaluator getInstance(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions,
			XPathCompiler xPathCompiler, ExpressionFactory expFactory, EffectType effect) throws IllegalArgumentException
	{
		if ((jaxbObligationExpressions == null || jaxbObligationExpressions.getObligationExpressions().isEmpty())
				&& (jaxbAdviceExpressions == null || jaxbAdviceExpressions.getAdviceExpressions().isEmpty()))
		{
			return null;
		}

		return new RulePepActionExpressionsEvaluator(jaxbObligationExpressions, jaxbAdviceExpressions, xPathCompiler, expFactory, effect);
	}

	/**
	 * Evaluates the PEP action (obligations/Advice) expressions in a given evaluation context
	 *
	 * @param context
	 *            evaluation context
	 * @return PEP actions (obligations/advices) or null if none
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             error evaluating one of ObligationExpression/AdviceExpressions' AttributeAssignmentExpressions' expressions
	 */
	public PepActions evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return PepActionExpressions.Helper.evaluate(this.ruleEffectMatchingActionExpressions, context, null);
	}

}
