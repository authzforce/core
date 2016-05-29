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
package org.ow2.authzforce.core.pdp.impl.policy;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.BasePepActions;
import org.ow2.authzforce.core.pdp.impl.PepActionExpression;
import org.ow2.authzforce.core.pdp.impl.PepActionExpressions;

/**
 * Evaluator of a Policy(Set)'s PEP action (Obligation/Advice) expressions
 *
 * 
 * @version $Id: $
 */
public class PolicyPepActionExpressionsEvaluator
{
	/**
	 * Policy(Set)-associated PEP action (obligation/advice) expressions parser used to initialize the evaluator's fields
	 * 
	 */
	private static class ActionExpressionsParser implements PepActionExpressions
	{

		private final XPathCompiler xPathCompiler;
		private final ExpressionFactory expFactory;

		private final PepActionExpressions.EffectSpecific denyActionExpressions = new EffectSpecific(EffectType.DENY);
		private final PepActionExpressions.EffectSpecific permitActionExpressions = new EffectSpecific(EffectType.PERMIT);

		/**
		 * Creates instance
		 * 
		 * @param xPathCompiler
		 *            XPath compiler corresponding to enclosing policy(set) default XPath version
		 * @param expressionFactory
		 *            expression factory for parsing expressions
		 */
		private ActionExpressionsParser(XPathCompiler xPathCompiler, ExpressionFactory expressionFactory)
		{
			this.xPathCompiler = xPathCompiler;
			this.expFactory = expressionFactory;
		}

		@Override
		public void add(ObligationExpression jaxbObligationExp) throws IllegalArgumentException
		{
			final PepActionExpression<Obligation> obligationExp = new PepActionExpression<>(BasePepActions.OBLIGATION_FACTORY, jaxbObligationExp.getObligationId(), jaxbObligationExp.getFulfillOn(),
					jaxbObligationExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final PepActionExpressions.EffectSpecific effectSpecificActionExps = obligationExp.getAppliesTo() == EffectType.DENY ? denyActionExpressions : permitActionExpressions;
			effectSpecificActionExps.addObligationExpression(obligationExp);
		}

		@Override
		public void add(AdviceExpression jaxbAdviceExp) throws IllegalArgumentException
		{
			final PepActionExpression<Advice> adviceExp = new PepActionExpression<>(BasePepActions.ADVICE_FACTORY, jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(),
					jaxbAdviceExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
			final PepActionExpressions.EffectSpecific effectSpecificActionExps = adviceExp.getAppliesTo() == EffectType.DENY ? denyActionExpressions : permitActionExpressions;
			effectSpecificActionExps.addAdviceExpression(adviceExp);
		}

		@Override
		public List<PepActionExpression<Obligation>> getObligationExpressionList()
		{
			final List<PepActionExpression<Obligation>> resultList = new ArrayList<>(denyActionExpressions.getObligationExpressions());
			resultList.addAll(permitActionExpressions.getObligationExpressions());
			return resultList;
		}

		@Override
		public List<PepActionExpression<Advice>> getAdviceExpressionList()
		{
			final List<PepActionExpression<Advice>> resultList = new ArrayList<>(denyActionExpressions.getAdviceExpressions());
			resultList.addAll(permitActionExpressions.getAdviceExpressions());
			return resultList;
		}
	}

	private static class ActionExpressionsFactory implements PepActionExpressions.Factory<ActionExpressionsParser>
	{

		@Override
		public ActionExpressionsParser getInstance(XPathCompiler xPathCompiler, ExpressionFactory expressionFactory)
		{
			return new ActionExpressionsParser(xPathCompiler, expressionFactory);
		}

	}

	private final PepActionExpressions.EffectSpecific denyActionExpressions;
	private final PepActionExpressions.EffectSpecific permitActionExpressions;

	private PolicyPepActionExpressionsEvaluator(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, XPathCompiler xPathCompiler, ExpressionFactory expFactory)
			throws IllegalArgumentException
	{
		final ActionExpressionsParser actionExpressionsParser = PepActionExpressions.Helper.parseActionExpressions(jaxbObligationExpressions, jaxbAdviceExpressions, xPathCompiler, expFactory,
				new ActionExpressionsFactory());
		this.denyActionExpressions = actionExpressionsParser.denyActionExpressions;
		this.permitActionExpressions = actionExpressionsParser.permitActionExpressions;
	}

	/**
	 * Instantiates the evaluator with given XACML-schema-derived ObligationExpressions/AdviceExpressions (a priori specific to a Policy(Set))
	 *
	 * @param jaxbObligationExpressions
	 *            XACML-schema-derived ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML-schema-derived AdviceExpressions
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing the AttributeAssignmentExpressions in the Obligation/Advice Expressions
	 * @return Policy's Obligation/Advice expressions evaluator
	 * @throws java.lang.IllegalArgumentException
	 *             if error parsing one of the AttributeAssignmentExpressions
	 */
	public static PolicyPepActionExpressionsEvaluator getInstance(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, XPathCompiler xPathCompiler,
			ExpressionFactory expFactory) throws IllegalArgumentException
	{
		if ((jaxbObligationExpressions == null || jaxbObligationExpressions.getObligationExpressions().isEmpty())
				&& (jaxbAdviceExpressions == null || jaxbAdviceExpressions.getAdviceExpressions().isEmpty()))
		{
			return null;
		}

		return new PolicyPepActionExpressionsEvaluator(jaxbObligationExpressions, jaxbAdviceExpressions, xPathCompiler, expFactory);
	}

	/**
	 * Evaluates the PEP action (obligations/Advice) expressions for a given decision and evaluation context
	 *
	 * @param combiningAlgResult
	 *            Policy(Set) combining algorithm evaluation result; this result's decision is used to select the Obligation/Advice expressions to apply, i.e. matching on FulfillOn/AppliesTo. This
	 *            result's PEP actions are also merged with the PEP actions computed in this method.
	 * @param context
	 *            evaluation context
	 * @return PEP actions (obligations/advices) or null if none
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             error evaluating one of ObligationExpression/AdviceExpressions' AttributeAssignmentExpressions' expressions
	 */
	public PepActions evaluate(DecisionResult combiningAlgResult, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final PepActionExpressions.EffectSpecific matchingActionExpressions;
		switch (combiningAlgResult.getDecision())
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

		return matchingActionExpressions == null ? null : PepActionExpressions.Helper.evaluate(matchingActionExpressions, context, combiningAlgResult.getPepActions());
	}

}
