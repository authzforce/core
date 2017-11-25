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
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

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
		private static final IllegalArgumentException NULL_OBLIGATION_EXPRESSION_EVALUATOR_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined ObligationExpression evaluator");
		private static final IllegalArgumentException NULL_EFFECT_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Effect (to which obligation/advice should apply");
		private static final IllegalArgumentException NULL_ADVICE_EXPRESSION_EVALUATOR_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined AdviceExpression evaluator");
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
			if (effect == null)
			{
				throw NULL_EFFECT_ARGUMENT_EXCEPTION;
			}

			this.effect = effect;
		}

		/**
		 * Adds an ObligationExpression to the list only if matching the {@link EffectType} to which this instance is specific, as defined by the constructor argument
		 * 
		 * @param obligationExpressionEvaluator
		 *            ObligationExpression evaluator
		 * @return true iff {@code obligationExpression} actually added to the expressions, i.e. fulfillOn matches the {@link EffectType} to which this instance is specific, as defined by the
		 *         constructor argument
		 */
		public boolean addObligationExpression(final PepActionExpression<Obligation> obligationExpressionEvaluator)
		{
			if (obligationExpressionEvaluator == null)
			{
				throw NULL_OBLIGATION_EXPRESSION_EVALUATOR_ARGUMENT_EXCEPTION;
			}

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
			if (adviceExpressionEvaluator == null)
			{
				throw NULL_ADVICE_EXPRESSION_EVALUATOR_ARGUMENT_EXCEPTION;
			}

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
		private static final IllegalArgumentException NULL_PEP_ACTION_EXPRESSIONS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined input PEP action expressions");
		private static final IllegalArgumentException NULL_PEP_ACTION_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Obligation/Advice expression parser");
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
		 *             if there is an invalid AttributeAssignmentExpression in any obligation/advice
		 */
		public static <T extends PepActionExpressions> T parseActionExpressions(final List<ObligationExpression> jaxbObligationExpressions, final List<AdviceExpression> jaxbAdviceExpressions,
				final XPathCompiler xPathCompiler, final ExpressionFactory expFactory, final PepActionExpressions.Factory<T> actionExpressionsFactory) throws IllegalArgumentException
		{
			if (actionExpressionsFactory == null)
			{
				throw NULL_PEP_ACTION_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION;
			}

			final T actionExpressions = actionExpressionsFactory.getInstance(xPathCompiler, expFactory);
			if (jaxbObligationExpressions != null)
			{
				for (final ObligationExpression jaxbObligationExp : jaxbObligationExpressions)
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
				for (final AdviceExpression jaxbAdviceExp : jaxbAdviceExpressions)
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

		private static <PEP_ACTION> ImmutableList<PEP_ACTION> evaluate(final List<PepActionExpression<PEP_ACTION>> pepActionExpressions, final EvaluationContext context,
				final String pepActionXmlTagName) throws IndeterminateEvaluationException
		{
			assert pepActionExpressions != null;

			final ImmutableList<PEP_ACTION> immutablePepActions;
			if (pepActionExpressions.isEmpty())
			{
				immutablePepActions = null;
			}
			else
			{
				final List<PEP_ACTION> mutablePepActions = new ArrayList<>(pepActionExpressions.size());
				for (final PepActionExpression<PEP_ACTION> pepActionExp : pepActionExpressions)
				{
					final PEP_ACTION pepAction;
					try
					{
						pepAction = pepActionExp.evaluate(context);
						if (LOGGER.isDebugEnabled())
						{
							LOGGER.debug("{}Expression[@{}Id={}] -> {}", pepActionXmlTagName, pepActionXmlTagName, pepActionExp.getActionId(), pepAction);
						}
					}
					catch (final IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException("Error evaluating one of the " + pepActionXmlTagName + "Expression[@" + pepActionXmlTagName + "Id=" + pepActionExp.getActionId()
								+ "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
					}

					mutablePepActions.add(pepAction);
				}

				immutablePepActions = ImmutableList.copyOf(mutablePepActions);
			}

			return immutablePepActions;
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
		 * @return new PepActions merging the expression evaluation result to {@code pepActionsToUpdate} if {@code pepActionsToUpdate != null}, else it is only the expression evaluation result
		 * @throws IndeterminateEvaluationException
		 *             if evaluation of {@code pepActionExpressions} failed
		 */
		public static ImmutablePepActions evaluate(final PepActionExpressions.EffectSpecific pepActionExpressions, final EvaluationContext context, final UpdatablePepActions pepActionsToUpdate)
				throws IndeterminateEvaluationException
		{
			if (pepActionExpressions == null)
			{
				throw NULL_PEP_ACTION_EXPRESSIONS_ARGUMENT_EXCEPTION;
			}

			final ImmutableList<Obligation> newObligations = evaluate(pepActionExpressions.getObligationExpressions(), context, PepActionFactories.OBLIGATION_FACTORY.getActionXmlElementName());
			final ImmutableList<Advice> newAdvices = evaluate(pepActionExpressions.getAdviceExpressions(), context, PepActionFactories.ADVICE_FACTORY.getActionXmlElementName());
			if (pepActionsToUpdate == null)
			{
				return ImmutablePepActions.getInstance(newObligations, newAdvices);
			}

			pepActionsToUpdate.addAll(newObligations, newAdvices);
			return ImmutablePepActions.getInstance(pepActionsToUpdate);
		}

		public static ImmutablePepActions evaluate(final PepActionExpressions.EffectSpecific pepActionExpressions, final EvaluationContext context) throws IndeterminateEvaluationException
		{
			if (pepActionExpressions == null)
			{
				throw NULL_PEP_ACTION_EXPRESSIONS_ARGUMENT_EXCEPTION;
			}

			final ImmutableList<Obligation> newObligations = evaluate(pepActionExpressions.getObligationExpressions(), context, PepActionFactories.OBLIGATION_FACTORY.getActionXmlElementName());
			final ImmutableList<Advice> newAdvices = evaluate(pepActionExpressions.getAdviceExpressions(), context, PepActionFactories.ADVICE_FACTORY.getActionXmlElementName());
			return ImmutablePepActions.getInstance(newObligations, newAdvices);
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
