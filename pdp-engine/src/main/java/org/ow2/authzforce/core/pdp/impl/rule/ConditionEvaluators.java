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

import java.util.Optional;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XACML Condition evaluators.
 *
 * @version $Id: $
 */
public final class ConditionEvaluators
{

	private static final IllegalArgumentException INVALID_CONSTANT_FALSE_EXPRESSION_EXCEPTION = new IllegalArgumentException("Invalid condition: Expression is equivalent to constant False");

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConditionEvaluators.class);

	private static final IllegalArgumentException NULL_EXPR_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Cannot create Condition evaluator: undefined input XACML expression parser (expressionFactory)");

	/**
	 * Condition that always evaluates to True
	 */
	public static final BooleanEvaluator TRUE_CONDITION = new BooleanEvaluator()
	{

		@Override
		public boolean evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			LOGGER.debug("Condition is null or Expression equals constant True -> True");
			return true;
		}
	};

	private static final class BooleanExpressionEvaluator implements BooleanEvaluator
	{

		private transient final Expression<BooleanValue> evaluatableBoolExpression;

		private BooleanExpressionEvaluator(final Expression<BooleanValue> boolExpression) throws IllegalArgumentException
		{

			assert boolExpression != null;
			this.evaluatableBoolExpression = boolExpression;
		}

		/**
		 * Evaluates the <code>Condition</code> to boolean by evaluating its child boolean <code>Expression</code>.
		 *
		 * @param context
		 *            the representation of the request
		 * @return true if and only if condition is true, i.e. its expression evaluates to True
		 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
		 *             if error evaluating the condition
		 * 
		 * 
		 */
		@Override
		public boolean evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			final BooleanValue boolVal = evaluatableBoolExpression.evaluate(context);
			return boolVal.getUnderlyingValue().booleanValue();
		}

	}

	/**
	 * Instantiates a Condition evaluator from XACML-Schema-derived <code>Condition</code>
	 *
	 * @param condition
	 *            XACML-schema-derived JAXB Condition element
	 * @param expressionFactory
	 *            expression factory
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @return instance of Condition evaluator
	 * @throws java.lang.IllegalArgumentException
	 *             if the expression is not a valid boolean Expression
	 */
	public static BooleanEvaluator getInstance(final Condition condition, final XPathCompiler xPathCompiler, final ExpressionFactory expressionFactory) throws IllegalArgumentException
	{
		if (condition == null)
		{
			return TRUE_CONDITION;
		}

		/*
		 * condition != null -> condition's Expression is not null (by definition of XACML schema), therefore expressionFactory is needed
		 */
		final ExpressionType exprElt = condition.getExpression().getValue();
		if (expressionFactory == null)
		{
			throw NULL_EXPR_FACTORY_ARGUMENT_EXCEPTION;
		}

		final Expression<?> expr = expressionFactory.getInstance(exprElt, xPathCompiler, null);

		// make sure it's a boolean expression...
		if (!(expr.getReturnType().equals(StandardDatatypes.BOOLEAN)))
		{
			throw new IllegalArgumentException("Invalid return datatype (" + expr.getReturnType() + ") for Expression (" + expr.getClass().getSimpleName() + ") in Condition. Expected: Boolean.");
		}

		// WARNING: unchecked cast
		final Expression<BooleanValue> evaluatableExpression = (Expression<BooleanValue>) expr;

		/*
		 * Check whether the expression is constant
		 */
		final Optional<BooleanValue> constant = evaluatableExpression.getValue();
		if (constant.isPresent())
		{
			if (constant.get().getUnderlyingValue())
			{
				// constant TRUE
				LOGGER.warn("Condition's expression is equivalent to constant True -> optimization: replacing with constant True condition");
				return TRUE_CONDITION;
			}

			// constant False -> unacceptable
			throw INVALID_CONSTANT_FALSE_EXPRESSION_EXCEPTION;
		}

		// constant == null
		LOGGER.debug("Condition's Expression is not constant (evaluation without context failed)");
		return new BooleanExpressionEvaluator(evaluatableExpression);
	}

	private ConditionEvaluators()
	{
		// prevent instantiation
	}
}
