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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * A class that implements the logical functions "or"
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:1.0:function:or': This function SHALL return "False" if it has no arguments and SHALL return "True" if at least one of its
 * arguments evaluates to "True". The order of evaluation SHALL be from first argument to last. The evaluation SHALL stop with a result of "True" if any argument evaluates to "True", leaving the rest
 * of the arguments unevaluated.
 *
 * 
 * @version $Id: $
 */
final class LogicalOrFunction extends SingleParameterTypedFirstOrderFunction<BooleanValue, BooleanValue>
{

	private static final class Call extends BaseFirstOrderFunctionCall<BooleanValue>
	{
		private final String indeterminateArgMsgPrefix;
		private final String invalidArgTypeMsgPrefix;

		private final List<Expression<?>> checkedArgExpressions;

		private Call(final FirstOrderFunctionSignature<BooleanValue> functionSig, final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, argExpressions, remainingArgTypes);
			this.checkedArgExpressions = argExpressions;
			indeterminateArgMsgPrefix = "Function " + functionSig.getName() + ": Indeterminate arg #";
			invalidArgTypeMsgPrefix = "Function " + functionSig.getName() + ": Invalid type (expected = " + StandardDatatypes.BOOLEAN + ") of arg#";
		}

		@Override
		public BooleanValue evaluate(final EvaluationContext context, final AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException
		{
			IndeterminateEvaluationException indeterminateException = null;
			int argIndex = 0;
			for (final Expression<?> arg : checkedArgExpressions)
			{
				// Evaluate the argument
				final BooleanValue attrVal;
				try
				{
					attrVal = Expressions.eval(arg, context, StandardDatatypes.BOOLEAN);
					if (attrVal.getUnderlyingValue().booleanValue())
					{
						return BooleanValue.TRUE;
					}
				}
				catch (final IndeterminateEvaluationException e)
				{
					// save the indeterminate to throw later only if there was not any TRUE in remaining
					// args
					indeterminateException = new IndeterminateEvaluationException(indeterminateArgMsgPrefix + argIndex, e.getStatusCode(), e);
				}

				argIndex++;
			}

			// do the same with remaining arg values
			if (checkedRemainingArgs != null)
			{

				for (final AttributeValue arg : checkedRemainingArgs)
				{
					// Evaluate the argument
					final BooleanValue attrVal;
					try
					{
						attrVal = BooleanValue.class.cast(arg);
					}
					catch (final ClassCastException e)
					{
						throw new IndeterminateEvaluationException(invalidArgTypeMsgPrefix + argIndex + ": " + arg.getClass().getName(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
					}

					if (attrVal.getUnderlyingValue().booleanValue())
					{
						return BooleanValue.TRUE;
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

			return BooleanValue.FALSE;
		}
	}

	LogicalOrFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.BOOLEAN, true, Arrays.asList(StandardDatatypes.BOOLEAN));
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	{
		/**
		 * TODO: optimize this function call by checking the following:
		 * <ol>
		 * <li>If any argument expression is constant BooleanAttributeValue False, remove it from the arguments, as it has no effect on the final result. Indeed, or function is commutative and
		 * or(false, x, y...) = or(x, y...).</li>
		 * </ol>
		 * Other optimizations are already achieved by ApplyExpression pre-evaluating the function call with context = null and check the result if no IndeterminateEvaluationException is thrown.
		 */
		return new Call(functionSignature, argExpressions, remainingArgTypes);
	}

}
