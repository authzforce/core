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
package org.ow2.authzforce.core;

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

import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Low-level interface to a list of PEP action (obligation/advice) expressions
 * 
 */
public interface PepActionExpressions
{
	/**
	 * PepActionExpressions factory
	 * 
	 * @param <T>
	 *            type of created instance
	 */
	interface Factory<T extends PepActionExpressions>
	{
		T getInstance(XPathCompiler xPathCompiler, ExpressionFactory expressionFactory);
	}

	/**
	 * Effect-specific obligation/advice expressions. Only expressions applying to such effect are allowed to be added to the list.
	 * 
	 */
	final class EffectSpecific
	{
		// effect to which obligation and advice below apply
		private final EffectType effect;
		private final List<PepActionExpression<Obligation>> obligationExpList = new ArrayList<>();
		private final List<PepActionExpression<Advice>> adviceExpList = new ArrayList<>();

		/**
		 * @param effect
		 *            Effect to which all obligation/advice expressions must apply
		 */
		public EffectSpecific(EffectType effect)
		{
			this.effect = effect;
		}

		/**
		 * Adds an ObligationExpression to the list only if matching the the effect argument to {@link EffectSpecific#EffectSpecific(EffectType)}
		 * 
		 * @param obligationExpressionEvaluator
		 *            ObligationExpressionEvaluator
		 * @return true iff {@code obligationExpression} actually added to the expressions, i.e. fulfillOn matches the effect argument to
		 *         {@link EffectSpecific#EffectSpecific(EffectType)}
		 */
		public boolean addObligationExpression(PepActionExpression<Obligation> obligationExpressionEvaluator)
		{
			if (obligationExpressionEvaluator.getAppliesTo() != effect)
			{
				return false;
			}

			return obligationExpList.add(obligationExpressionEvaluator);
		}

		/**
		 * Adds an AdviceExpression to the list only if matching the the effect argument to {@link EffectSpecific#EffectSpecific(EffectType)}
		 * 
		 * @param adviceExpressionEvaluator
		 *            AdviceExpressionEvaluator
		 * @return true iff {@code adviceExpression} actually added to the expressions, i.e. appliesTo matches the effect argument to
		 *         {@link EffectSpecific#EffectSpecific(EffectType)}
		 */
		public boolean addAdviceExpression(PepActionExpression<Advice> adviceExpressionEvaluator)
		{
			if (adviceExpressionEvaluator.getAppliesTo() != effect)
			{
				return false;
			}

			return adviceExpList.add(adviceExpressionEvaluator);
		}

		/**
		 * Effect-specific ObligationExpressions
		 * 
		 * @return the effect-specific ObligationExpressions
		 */
		public List<PepActionExpression<Obligation>> getObligationExpressions()
		{
			return this.obligationExpList;
		}

		/**
		 * Effect-specific AdviceExpressions
		 * 
		 * @return the effect-specific AdviceExpressions
		 */
		public List<PepActionExpression<Advice>> getAdviceExpressions()
		{
			return this.adviceExpList;
		}

		/**
		 * Get Effect to be matched by all expressions
		 * 
		 * @return effect
		 */
		public EffectType getEffect()
		{
			return effect;
		}
	}

	/**
	 * Helps parse/evaluate PEP action expressions (ObligationExpressions, AdviceExpressions)
	 *
	 */
	final class Helper
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

		private Helper()
		{
		}

		/**
		 * Parse XACML ObligationExpressions/AdviceExpressions
		 * 
		 * @param jaxbObligationExpressions
		 *            XACML ObligationExpressions
		 * @param jaxbAdviceExpressions
		 *            XACML AdviceExpressions
		 * @param xPathCompiler
		 *            XPath compiler corresponding to enclosing policy(set) default XPath version
		 * @param expFactory
		 *            XACML Expression factory
		 * @param actionExpressionsFactory
		 *            PepActionExpressions factory
		 * @return PEP action expressions
		 * @throws ParsingException
		 *             if there is a parsing/syntax error with one of the obligation/advice expressions
		 */
		public static <T extends PepActionExpressions> T parseActionExpressions(ObligationExpressions jaxbObligationExpressions,
				AdviceExpressions jaxbAdviceExpressions, XPathCompiler xPathCompiler, ExpressionFactory expFactory,
				PepActionExpressions.Factory<T> actionExpressionsFactory) throws ParsingException
		{
			final T actionExpressions = actionExpressionsFactory.getInstance(xPathCompiler, expFactory);
			if (jaxbObligationExpressions != null)
			{
				final List<ObligationExpression> jaxbObligationExpList = jaxbObligationExpressions.getObligationExpressions();
				for (final ObligationExpression jaxbObligationExp : jaxbObligationExpList)
				{
					try
					{
						actionExpressions.add(jaxbObligationExp);
					} catch (ParsingException e)
					{
						throw new ParsingException("Error parsing one of the ObligationExpression[@ObligationId='" + jaxbObligationExp.getObligationId()
								+ "']/AttributeAssignmentExpression/Expression elements", e);
					}
				}
			}

			if (jaxbAdviceExpressions != null)
			{
				final List<AdviceExpression> jaxbAdviceExpList = jaxbAdviceExpressions.getAdviceExpressions();
				for (final AdviceExpression jaxbAdviceExp : jaxbAdviceExpList)
				{
					try
					{
						actionExpressions.add(jaxbAdviceExp);
					} catch (ParsingException e)
					{
						throw new ParsingException("Error parsing one of the AdviceExpression[@AdviceId='" + jaxbAdviceExp.getAdviceId()
								+ "']/AttributeAssignmentExpression/Expression elements", e);
					}
				}
			}

			return actionExpressions;
		}

		private static <PEP_ACTION> List<PEP_ACTION> evaluate(List<PepActionExpression<PEP_ACTION>> pepActionExpressions, EvaluationContext context,
				String pepActionXmlTagName) throws IndeterminateEvaluationException
		{
			final List<PEP_ACTION> obligations;
			if (pepActionExpressions.isEmpty())
			{
				obligations = null;
			} else
			{
				obligations = new ArrayList<>(pepActionExpressions.size());
				for (final PepActionExpression<PEP_ACTION> obligationExp : pepActionExpressions)
				{
					final PEP_ACTION obligation;
					try
					{
						obligation = obligationExp.evaluate(context);
						if (LOGGER.isDebugEnabled())
						{
							LOGGER.debug("{}Expression[@{}Id={}] -> {}", pepActionXmlTagName, pepActionXmlTagName, obligationExp.getActionId(), obligation);
						}
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException("Error evaluating one of the " + pepActionXmlTagName + "Expression[@" + pepActionXmlTagName
								+ "Id=" + obligationExp.getActionId() + "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
					}

					obligations.add(obligation);
				}
			}

			return obligations;
		}

		public static PepActions evaluate(PepActionExpressions.EffectSpecific pepActionExpressions, EvaluationContext context)
				throws IndeterminateEvaluationException
		{
			final List<Obligation> obligations = evaluate(pepActionExpressions.getObligationExpressions(), context,
					PepActions.OBLIGATION_FACTORY.getActionXmlElementName());
			final List<Advice> advices = evaluate(pepActionExpressions.getAdviceExpressions(), context, PepActions.ADVICE_FACTORY.getActionXmlElementName());
			return obligations == null && advices == null ? null : new PepActions(obligations, advices);
		}
	}

	/**
	 * Adds a XACML ObligationExpression to the list
	 * 
	 * @param jaxbObligationExp
	 *            XACML ObligationExpression
	 * @throws ParsingException
	 *             if error (e.g. syntax error) parsing the expression
	 */
	void add(ObligationExpression jaxbObligationExp) throws ParsingException;

	/**
	 * Adds a XACML AdviceExpression to the list
	 * 
	 * @param jaxbAdviceExp
	 *            XACML ObligationExpression
	 * @throws ParsingException
	 *             if error (e.g. syntax error) parsing the expression
	 */
	void add(AdviceExpression jaxbAdviceExp) throws ParsingException;

	/**
	 * Gets all the expressions added with {@link #add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression)}
	 * 
	 * @return list of ObligationExpressions
	 */
	List<PepActionExpression<Obligation>> getObligationExpressionList();

	/**
	 * Gets all the expressions added with {@link #add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression)}
	 * 
	 * @return list of AdviceExpressions
	 */
	List<PepActionExpression<Advice>> getAdviceExpressionList();
}
