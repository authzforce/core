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
package org.ow2.authzforce.core.pdp.impl;

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

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.MutablePepActions;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Low-level interface to a list of PEP action (obligation/advice) expressions
 *
 * @version $Id: $
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
		public EffectSpecific(final EffectType effect)
		{
			this.effect = effect;
		}

		/**
		 * Adds an ObligationExpression to the list only if matching the {@link EffectType} to which this instance is specific, as defined by the constructor argument
		 * 
		 * @param obligationExpressionEvaluator
		 *            ObligationExpressionEvaluator
		 * @return true iff {@code obligationExpression} actually added to the expressions, i.e. fulfillOn matches the {@link EffectType} to which this instance is specific, as defined by the
		 *         constructor argument
		 */
		public boolean addObligationExpression(final PepActionExpression<Obligation> obligationExpressionEvaluator)
		{
			if (obligationExpressionEvaluator.getAppliesTo() != effect)
			{
				return false;
			}

			return obligationExpList.add(obligationExpressionEvaluator);
		}

		/**
		 * Adds an AdviceExpression to the list only if matching the {@link EffectType} to which this instance is specific, as defined by the constructor argument
		 * 
		 * @param adviceExpressionEvaluator
		 *            AdviceExpressionEvaluator
		 * @return true iff {@code adviceExpression} actually added to the PEP action expressions, i.e. appliesTo matches the {@link EffectType} to which this instance is specific, as defined by the
		 *         constructor argument
		 */
		public boolean addAdviceExpression(final PepActionExpression<Advice> adviceExpressionEvaluator)
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
		 * @throws IllegalArgumentException
		 *             if there is an invalid obligation/advice expression
		 */
		public static <T extends PepActionExpressions> T parseActionExpressions(final ObligationExpressions jaxbObligationExpressions, final AdviceExpressions jaxbAdviceExpressions,
				final XPathCompiler xPathCompiler, final ExpressionFactory expFactory, final PepActionExpressions.Factory<T> actionExpressionsFactory) throws IllegalArgumentException
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
					}
					catch (final IllegalArgumentException e)
					{
						throw new IllegalArgumentException("One of the ObligationExpression[@ObligationId='" + jaxbObligationExp.getObligationId()
								+ "']/AttributeAssignmentExpression/Expression elements is invalid", e);
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
					}
					catch (final IllegalArgumentException e)
					{
						throw new IllegalArgumentException("One of the AdviceExpression[@AdviceId='" + jaxbAdviceExp.getAdviceId() + "']/AttributeAssignmentExpression/Expression elements is invalid",
								e);
					}
				}
			}

			return actionExpressions;
		}

		private static <PEP_ACTION> List<PEP_ACTION> evaluate(final List<PepActionExpression<PEP_ACTION>> pepActionExpressions, final EvaluationContext context, final String pepActionXmlTagName)
				throws IndeterminateEvaluationException
		{
			final List<PEP_ACTION> obligations;
			if (pepActionExpressions.isEmpty())
			{
				obligations = null;
			}
			else
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
					}
					catch (final IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException("Error evaluating one of the " + pepActionXmlTagName + "Expression[@" + pepActionXmlTagName + "Id=" + obligationExp.getActionId()
								+ "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
					}

					obligations.add(obligation);
				}
			}

			return obligations;
		}

		/**
		 * Evaluate DecisionType-specific PepActionExpressions in a given context
		 * 
		 * @param pepActionExpressions
		 *            PEP action expressions
		 * @param context
		 *            evaluation context
		 * @param pepActionsToUpdate
		 *            mutable PepActions to which the PEP actions resulting from evaluation of {@code pepActionExpressions} are added
		 * @return new PepActions if {@code pepActionsToUpdate == null}, else null (result is in {@code pepActionsToUpdate})
		 * @throws IndeterminateEvaluationException
		 *             if evaluation of {@code pepActionExpressions} failed
		 */
		public static ImmutablePepActions evaluate(final PepActionExpressions.EffectSpecific pepActionExpressions, final EvaluationContext context, final MutablePepActions pepActionsToUpdate)
				throws IndeterminateEvaluationException
		{
			final List<Obligation> newObligations = evaluate(pepActionExpressions.getObligationExpressions(), context, PepActionFactories.OBLIGATION_FACTORY.getActionXmlElementName());
			final List<Advice> newAdvices = evaluate(pepActionExpressions.getAdviceExpressions(), context, PepActionFactories.ADVICE_FACTORY.getActionXmlElementName());
			if (pepActionsToUpdate == null)
			{
				return new ImmutablePepActions(newObligations, newAdvices);
			}

			pepActionsToUpdate.addAll(newObligations, newAdvices);
			return null;
		}

		public static ImmutablePepActions evaluate(final PepActionExpressions.EffectSpecific pepActionExpressions, final EvaluationContext context) throws IndeterminateEvaluationException
		{
			final List<Obligation> newObligations = evaluate(pepActionExpressions.getObligationExpressions(), context, PepActionFactories.OBLIGATION_FACTORY.getActionXmlElementName());
			final List<Advice> newAdvices = evaluate(pepActionExpressions.getAdviceExpressions(), context, PepActionFactories.ADVICE_FACTORY.getActionXmlElementName());
			return new ImmutablePepActions(newObligations, newAdvices);
		}
	}

	/**
	 * Adds a XACML ObligationExpression to the list
	 *
	 * @param jaxbObligationExp
	 *            XACML ObligationExpression
	 * @throws java.lang.IllegalArgumentException
	 *             if invalid expression
	 */
	void add(ObligationExpression jaxbObligationExp) throws IllegalArgumentException;

	/**
	 * Adds a XACML AdviceExpression to the list
	 *
	 * @param jaxbAdviceExp
	 *            XACML ObligationExpression
	 * @throws java.lang.IllegalArgumentException
	 *             if invalid expression
	 */
	void add(AdviceExpression jaxbAdviceExp) throws IllegalArgumentException;

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
