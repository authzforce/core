/*
 * Copyright 2012-2023 THALES.
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
package org.ow2.authzforce.core.pdp.impl.func;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.func.*;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class that implements the logical function "and".
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:1.0:function:and': This function SHALL return "True" if it has no arguments and SHALL return "False" if one of its arguments
 * evaluates to "False". The order of evaluation SHALL be from first argument to last. The evaluation SHALL stop with a result of "False" if any argument evaluates to "False", leaving the rest of the
 * arguments unevaluated.
 * 
 * @version $Id: $
 */
final class LogicalAndFunction extends SingleParameterTypedFirstOrderFunction<BooleanValue, BooleanValue>
{
	private static final class CallFactory
	{

		private static final class Call extends BaseFirstOrderFunctionCall<BooleanValue>
		{
			private final String invalidArgTypeMsgPrefix;
			private final String indeterminateArgMsgPrefix;

			private final List<Expression<?>> checkedArgExpressions;

			private Call(final FirstOrderFunctionSignature<BooleanValue> functionSig, final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
			{
				super(functionSig, argExpressions, remainingArgTypes);
				this.checkedArgExpressions = argExpressions;
				invalidArgTypeMsgPrefix = "Function " + functionSig.getName() + ": Invalid type (expected = " + StandardDatatypes.BOOLEAN + ") of arg#";
				indeterminateArgMsgPrefix = "Function " + functionSig.getName() + ": Indeterminate arg #";
			}

			@Override
			public BooleanValue evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
			{
				IndeterminateEvaluationException indeterminateException = null;
				int argIndex = 0;
				for (final Expression<?> arg : checkedArgExpressions)
				{
					// Evaluate the argument
					final BooleanValue attrVal;
					try
					{
						attrVal = Expressions.eval(arg, context, mdpContext, StandardDatatypes.BOOLEAN);
						if (!attrVal.getUnderlyingValue())
						{
							return BooleanValue.FALSE;
						}
					}
					catch (final IndeterminateEvaluationException e)
					{
						// keep the indeterminate error to throw later if there was not any FALSE in
						// remaining args
						indeterminateException = new IndeterminateEvaluationException(indeterminateArgMsgPrefix + argIndex, e);
					}

					argIndex++;
				}

				// do the same with remaining arg values
				if (remainingArgs != null)
				{

					for (final AttributeValue arg : remainingArgs)
					{
						// Evaluate the argument
						final BooleanValue attrVal;
						try
						{
							attrVal = (BooleanValue) arg;
						}
						catch (final ClassCastException e)
						{
							throw new IndeterminateEvaluationException(invalidArgTypeMsgPrefix + argIndex + ": " + arg.getClass().getName(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
						}

						if (!attrVal.getUnderlyingValue())
						{
							return BooleanValue.FALSE;
						}

						argIndex++;
					}
				}

				if (indeterminateException != null)
				{
					// there was at least one indeterminate arg that could have been TRUE or FALSE ->
					// indeterminate result
					throw indeterminateException;
				}

				return BooleanValue.TRUE;
			}

		}

		private final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, BooleanValue> funcSig;

		private CallFactory(final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, BooleanValue> functionSignature)
		{
			this.funcSig = functionSignature;
		}

		private FirstOrderFunctionCall<BooleanValue> getInstance(final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			/*
			 * Optimize this function call by checking the following:
			 * <ol>
			 * <li>If any argument expression is constant BooleanValue True, remove it from the arguments, as it has no effect on the final result. Indeed, and function is commutative and
			 * and(true, x, y...) = and(x, y...).</li>
			 * </ol>
			 * Other optimizations are already achieved by ApplyExpression pre-evaluating the function call with context = null and check the result if no IndeterminateEvaluationException is thrown.
			 */
			final List<Expression<?>> optimizedArgExprs = argExpressions.stream().filter(argExpr ->
					argExpr.getValue().map(v -> !(v instanceof BooleanValue) || !((BooleanValue) v).getUnderlyingValue()).orElse(true)
			).collect(Collectors.toUnmodifiableList());
			return new Call(funcSig, optimizedArgExprs, remainingArgTypes);
		}

	}

	private final CallFactory funcCallFactory;

	LogicalAndFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.BOOLEAN, true, Collections.singletonList(StandardDatatypes.BOOLEAN));
		this.funcCallFactory = new CallFactory(this.functionSignature);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}
}
