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
package com.thalesgroup.authzforce.core;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Evaluator of PEP action (Obligation/Advice) expressions of a Policy(Set) or Rule
 * 
 * 
 */
public abstract class PepActionExpressionsEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PepActionExpressionsEvaluator.class);

	/**
	 * Initialize
	 * 
	 * @param inoutEvaluableActionExpressions
	 *            lists of expressions of PEP actions to be updated from
	 *            {@code jaxbObligationExpressions} and {@code jaxbAdviceExpressions}
	 * @param jaxbObligationExpressions
	 *            XACML ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML AdviceExpressions
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            XACML Expression factory
	 * @throws ParsingException
	 *             if there is a parsing/syntax error with one of the obligation/advice expressions
	 */
	protected static <T extends PepActionExpressions> T parseActionExpressions(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, XPathCompiler xPathCompiler, Expression.Factory expFactory, PepActionExpressions.Factory<T> actionExpressionsFactory)
			throws ParsingException
	{
		final T actionExpressions = actionExpressionsFactory.getInstance(xPathCompiler, expFactory);
		if (jaxbObligationExpressions != null)
		{
			final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression> jaxbObligationExpList = jaxbObligationExpressions.getObligationExpressions();
			for (oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp : jaxbObligationExpList)
			{
				try
				{
					actionExpressions.add(jaxbObligationExp);
				} catch (ParsingException e)
				{
					throw new ParsingException("Error parsing one of the ObligationExpression[@ObligationId='" + jaxbObligationExp.getObligationId() + "']/AttributeAssignmentExpression/Expression elements", e);
				}
			}
		}

		if (jaxbAdviceExpressions != null)
		{
			final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression> jaxbAdviceExpList = jaxbAdviceExpressions.getAdviceExpressions();
			for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp : jaxbAdviceExpList)
			{
				try
				{
					actionExpressions.add(jaxbAdviceExp);
				} catch (ParsingException e)
				{
					throw new ParsingException("Error parsing one of the AdviceExpression[@AdviceId='" + jaxbAdviceExp.getAdviceId() + "']/AttributeAssignmentExpression/Expression elements", e);
				}
			}
		}

		return actionExpressions;
	}

	protected static PepActions evaluate(PepActionExpressions.EffectSpecific pepActionExpressions, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final List<ObligationExpressionEvaluator> obligationExpList = pepActionExpressions.getObligationExpressions();
		final List<Obligation> obligations;
		if (obligationExpList.isEmpty())
		{
			obligations = null;
		} else
		{
			obligations = new ArrayList<>(obligationExpList.size());
			for (final ObligationExpressionEvaluator obligationExp : obligationExpList)
			{
				final Obligation obligation;
				try
				{
					obligation = obligationExp.evaluate(context);
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("ObligationExpression[@ObligationId={}] -> {}", obligationExp.getObligationId(), obligation);
					}
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException("Error evaluating one of the ObligationExpression[@ObligationId=" + obligationExp.getObligationId() + "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
				}

				obligations.add(obligation);
			}
		}

		final List<AdviceExpressionEvaluator> adviceExpList = pepActionExpressions.getAdviceExpressions();
		final List<Advice> advices;
		if (adviceExpList.isEmpty())
		{
			advices = null;
		} else
		{
			advices = new ArrayList<>(adviceExpList.size());
			for (final AdviceExpressionEvaluator adviceExp : adviceExpList)
			{
				final Advice advice;
				try
				{
					advice = adviceExp.evaluate(context);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException("Error evaluating one of the AdviceExpression[@AdviceId=" + adviceExp.getAdviceId() + "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
				}
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("AdviceExpression[@AdviceId={}] -> {}", adviceExp.getAdviceId(), advice);
				}

				advices.add(advice);
			}
		}

		return obligations == null && advices == null ? null : new PepActions(obligations, advices);
	}

	/**
	 * Get the corresponding XACML/JAXB ObligationExpressions element
	 * 
	 * @return ObligationExpressions element
	 */
	public abstract ObligationExpressions getObligationExpressions();

	/**
	 * Get the corresponding XACML/JAXB AdviceExpressions element
	 * 
	 * @return AdviceExpressions element
	 */
	public abstract AdviceExpressions getAdviceExpressions();

}
