/*
 * Copyright 2012-2024 THALES.
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

import com.google.common.base.Preconditions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluator;
import org.ow2.authzforce.core.pdp.impl.BooleanEvaluators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * XACML Condition evaluators.
 *
 * @version $Id: $
 */
public final class ConditionEvaluators
{

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConditionEvaluators.class);

	private static final IllegalArgumentException NULL_EXPR_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Cannot create Condition evaluator: undefined input XACML expression parser (expressionFactory)");

	private static final class BooleanExpressionEvaluator implements BooleanEvaluator
	{

		private transient final Expression<BooleanValue> evaluableBoolExpression;

		private BooleanExpressionEvaluator(final Expression<BooleanValue> boolExpression) throws IllegalArgumentException
		{

			assert boolExpression != null;
			this.evaluableBoolExpression = boolExpression;
		}

		/**
		 * Evaluates the <code>Condition</code> to boolean by evaluating its child boolean <code>Expression</code>.
		 *
		 * @param context
		 *            the representation of the Individual Decision request
		 * @return true if and only if condition is true, i.e. its expression evaluates to True
		 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
		 *             if error evaluating the condition
		 * 
		 * 
		 */
		@Override
		public boolean evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
		{
			final BooleanValue boolVal = evaluableBoolExpression.evaluate(context, mdpContext);
			return boolVal.getUnderlyingValue();
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
	 *            XPath compiler, defined if XPath support enabled (by PDP configuration and some enclosing Policy(Set) defines a XPathVersion according to XACML standard)
	 * @return instance of Condition evaluator
	 * @throws java.lang.IllegalArgumentException
	 *             if the expression is not a valid boolean Expression
	 */
	public static BooleanEvaluator getInstance(final Condition condition, final ExpressionFactory expressionFactory, final Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException
	{
		Preconditions.checkArgument(condition != null, "<Condition> element undefined");

		/*
		 * condition != null -> condition's Expression is not null (by definition of XACML schema), therefore expressionFactory is needed
		 */
		final ExpressionType exprElt = condition.getExpression().getValue();
		if (expressionFactory == null)
		{
			throw NULL_EXPR_FACTORY_ARGUMENT_EXCEPTION;
		}

		final Expression<?> expr = expressionFactory.getInstance(exprElt,null, xPathCompiler);

		// make sure it's a boolean expression...
		if (!(expr.getReturnType().equals(StandardDatatypes.BOOLEAN)))
		{
			throw new IllegalArgumentException("Invalid return datatype (" + expr.getReturnType() + ") for Expression (" + expr.getClass().getSimpleName() + ") in Condition. Expected: Boolean.");
		}

		// WARNING: unchecked cast
		final Expression<BooleanValue> evaluableExpression = (Expression<BooleanValue>) expr;

		/*
		 * Check whether the expression is constant
		 */
		final Optional<BooleanValue> constant = evaluableExpression.getValue();
		if (constant.isPresent())
		{
			if (constant.get().getUnderlyingValue())
			{
				// constant TRUE
				LOGGER.warn("Condition's expression is equivalent to constant True -> optimization: replacing with constant True condition");
				return BooleanEvaluators.TRUE;
			}

			// constant False (according to section 7.11 of XACML 3.0, Rule evaluates always to NotApplicable )
			LOGGER.warn("Condition's expression is equivalent to constant False -> replacing with constant False condition");
			return BooleanEvaluators.FALSE;
		}

		// constant == null
		LOGGER.debug("Condition's Expression is not constant (evaluation without context failed)");
		return new BooleanExpressionEvaluator(evaluableExpression);
	}

	private ConditionEvaluators()
	{
		// prevent instantiation
	}
}
