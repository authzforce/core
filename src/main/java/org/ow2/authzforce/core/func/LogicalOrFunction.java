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
package org.ow2.authzforce.core.func;

import java.util.Arrays;
import java.util.List;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.expression.Expressions;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.BooleanValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeConstants;

/**
 * A class that implements the logical functions "or" and "and".
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:1.0:function:or': This function SHALL return "False" if it has no arguments and SHALL
 * return "True" if at least one of its arguments evaluates to "True". The order of evaluation SHALL be from first argument to last. The evaluation SHALL stop
 * with a result of "True" if any argument evaluates to "True", leaving the rest of the arguments unevaluated.
 * 
 */
public final class LogicalOrFunction extends FirstOrderFunction.SingleParameterTyped<BooleanValue, BooleanValue>
{
	private static final class Call extends FirstOrderFunctionCall<BooleanValue>
	{
		private static final String INDETERMINATE_ARG_MESSAGE_PREFIX = "Function " + NAME_OR + ": Indeterminate arg #";
		private static final String INVALID_ARG_TYPE_MESSAGE_PREFIX = "Function " + NAME_OR + ": Invalid type (expected = " + DatatypeConstants.BOOLEAN.TYPE
				+ ") of arg#";

		private final List<Expression<?>> checkedArgExpressions;

		private Call(FunctionSignature<BooleanValue> functionSig, List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
				throws IllegalArgumentException
		{
			super(functionSig, argExpressions, remainingArgTypes);
			this.checkedArgExpressions = argExpressions;
		}

		@Override
		protected BooleanValue evaluate(EvaluationContext context, AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException
		{
			/**
			 * TODO: optimize this function call by checking the following:
			 * <ol>
			 * <li>If any argument expression is constant BooleanAttributeValue True, return always true.</li>
			 * <li>Else If all argument expressions are constant BooleanAttributeValue False, return always false.</li>
			 * <li>
			 * Else If any argument expression is constant BooleanAttributeValue False, remove it from the arguments, as it has no effect on the final result.
			 * Indeed, or function is commutative and or(false, x, y...) = or(x, y...).</li>
			 * </ol>
			 * The first two optimizations can be achieved by pre-evaluating the function call with context = null and check the result if no
			 * IndeterminateEvaluationException is thrown.
			 */

			IndeterminateEvaluationException indeterminateException = null;
			int argIndex = 0;
			for (final Expression<?> arg : checkedArgExpressions)
			{
				// Evaluate the argument
				final BooleanValue attrVal;
				try
				{
					attrVal = Expressions.eval(arg, context, DatatypeConstants.BOOLEAN.TYPE);
					if (attrVal.getUnderlyingValue())
					{
						return BooleanValue.TRUE;
					}
				} catch (IndeterminateEvaluationException e)
				{
					// save the indeterminate to throw later only if there was not any TRUE in remaining
					// args
					indeterminateException = new IndeterminateEvaluationException(INDETERMINATE_ARG_MESSAGE_PREFIX + argIndex,
							StatusHelper.STATUS_PROCESSING_ERROR, e);
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
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE_PREFIX + argIndex + ": " + arg.getClass().getName(),
								StatusHelper.STATUS_PROCESSING_ERROR, e);
					}

					if (attrVal.getUnderlyingValue())
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

	/**
	 * XACML standard identifier for the "or" logical function
	 */
	public static final String NAME_OR = XACML_NS_1_0 + "or";

	private LogicalOrFunction()
	{
		super(NAME_OR, DatatypeConstants.BOOLEAN.TYPE, true, Arrays.asList(DatatypeConstants.BOOLEAN.TYPE));
	}

	/**
	 * Singleton instance of "or" logical function
	 */
	public static final LogicalOrFunction INSTANCE = new LogicalOrFunction();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return new Call(functionSignature, argExpressions, remainingArgTypes);
	}

}
