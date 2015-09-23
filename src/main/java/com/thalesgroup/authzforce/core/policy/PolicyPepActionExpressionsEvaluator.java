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
package com.thalesgroup.authzforce.core.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PepActions;
import com.thalesgroup.authzforce.core.eval.AdviceExpression;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.ObligationExpression;
import com.thalesgroup.authzforce.core.eval.PepActionExpressions;
import com.thalesgroup.authzforce.core.eval.PepActionExpressionsEvaluator;

/**
 * Evaluator of a Policy(Set)'s PEP action (Obligation/Advice) expressions
 * 
 */
public class PolicyPepActionExpressionsEvaluator extends PepActionExpressionsEvaluator
{
	/**
	 * Policy(Set)-associated PEP action (obligation/advice) expressions parser used to initialize
	 * the evaluator's fields
	 * 
	 */
	private static class ActionExpressionsParser implements PepActionExpressions
	{

		private final DefaultsType policyDefaults;
		private final ExpressionFactory expFactory;

		private final PepActionExpressions.EffectSpecific denyActionExpressions = new EffectSpecific(EffectType.DENY);
		private final PepActionExpressions.EffectSpecific permitActionExpressions = new EffectSpecific(EffectType.PERMIT);

		/**
		 * Creates instance
		 * 
		 * @param policyDefaults
		 *            Enclosing policy default parameters for parsing expressions
		 * @param expressionFactory
		 *            expression factory for parsing expressions
		 */
		private ActionExpressionsParser(DefaultsType policyDefaults, ExpressionFactory expressionFactory)
		{
			this.policyDefaults = policyDefaults;
			this.expFactory = expressionFactory;
		}

		@Override
		public void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp) throws ParsingException
		{
			final ObligationExpression obligationExp = new ObligationExpression(jaxbObligationExp, policyDefaults, expFactory);
			final PepActionExpressions.EffectSpecific effectSpecificActionExps = obligationExp.getFulfillOn() == EffectType.DENY ? denyActionExpressions : permitActionExpressions;
			effectSpecificActionExps.add(obligationExp);
		}

		@Override
		public void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp) throws ParsingException
		{
			final AdviceExpression adviceExp = new AdviceExpression(jaxbAdviceExp, policyDefaults, expFactory);
			final PepActionExpressions.EffectSpecific effectSpecificActionExps = adviceExp.getAppliesTo() == EffectType.DENY ? denyActionExpressions : permitActionExpressions;
			effectSpecificActionExps.add(adviceExp);
		}

		@Override
		public List<ObligationExpression> getObligationExpressionList()
		{
			final List<ObligationExpression> resultList = new ArrayList<>(denyActionExpressions.getObligationExpressions());
			resultList.addAll(permitActionExpressions.getObligationExpressions());
			return resultList;
		}

		@Override
		public List<AdviceExpression> getAdviceExpressionList()
		{
			final List<AdviceExpression> resultList = new ArrayList<>(denyActionExpressions.getAdviceExpressions());
			resultList.addAll(permitActionExpressions.getAdviceExpressions());
			return resultList;
		}
	}

	private static class ActionExpressionsFactory implements PepActionExpressions.Factory<ActionExpressionsParser>
	{

		@Override
		public ActionExpressionsParser getInstance(DefaultsType policyDefaults, ExpressionFactory expressionFactory)
		{
			return new ActionExpressionsParser(policyDefaults, expressionFactory);
		}

	}

	private final ObligationExpressions jaxbObligationExpressions;
	private final AdviceExpressions jaxbAdviceExpressions;
	private final PepActionExpressions.EffectSpecific denyActionExpressions;
	private final PepActionExpressions.EffectSpecific permitActionExpressions;

	private PolicyPepActionExpressionsEvaluator(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		final ActionExpressionsParser actionExpressionsParser = super.parseActionExpressions(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory, new ActionExpressionsFactory());
		this.jaxbObligationExpressions = new ObligationExpressions(Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression> unmodifiableList(actionExpressionsParser.getObligationExpressionList()));
		this.jaxbAdviceExpressions = new AdviceExpressions(Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression> unmodifiableList(actionExpressionsParser.getAdviceExpressionList()));
		this.denyActionExpressions = actionExpressionsParser.denyActionExpressions;
		this.permitActionExpressions = actionExpressionsParser.permitActionExpressions;
	}

	/**
	 * Instantiates the evaluator with given XACML-schema-derived
	 * ObligationExpressions/AdviceExpressions (a priori specific to a Policy(Set))
	 * 
	 * @param jaxbObligationExpressions
	 *            XACML-schema-derived ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML-schema-derived AdviceExpressions
	 * @param policyDefaults
	 *            policy's default parameters, e.g. XPath version
	 * @param expFactory
	 *            Expression factory for parsing the AttributeAssignmentExpressions in the
	 *            Obligation/Advice Expressions
	 * @return Policy's Obligation/Advice expressions evaluator
	 * @throws ParsingException
	 *             if error parsing one of the AttributeAssignmentExpressions
	 */
	public static PolicyPepActionExpressionsEvaluator getInstance(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		if ((jaxbObligationExpressions == null || jaxbObligationExpressions.getObligationExpressions().isEmpty()) && (jaxbAdviceExpressions == null || jaxbAdviceExpressions.getAdviceExpressions().isEmpty()))
		{
			return null;
		}

		return new PolicyPepActionExpressionsEvaluator(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory);
	}

	/**
	 * Evaluates the PEP action (obligations/Advice) expressions for a given decision and evaluation
	 * context
	 * 
	 * @param decision
	 *            PERMIT/DENY decision to select the Obligation/Advice expressions to apply, based
	 *            on FulfillOn/AppliesTo, typically resuling from evaluation of the parent
	 *            Policy(Set)/Rule
	 * @param context
	 *            evaluation context
	 * @return PEP actions (obligations/advices) or null if none
	 * @throws IndeterminateEvaluationException
	 *             error evaluating one of ObligationExpression/AdviceExpressions'
	 *             AttributeAssignmentExpressions' expressions
	 */
	public PepActions evaluate(DecisionType decision, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final PepActionExpressions.EffectSpecific matchingActionExpressions;
		switch (decision)
		{
			case DENY:
				matchingActionExpressions = this.denyActionExpressions;
				break;
			case PERMIT:
				matchingActionExpressions = this.permitActionExpressions;
				break;
			default:
				matchingActionExpressions = null;
				break;
		}

		return matchingActionExpressions == null ? null : super.evaluate(matchingActionExpressions, context);
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
