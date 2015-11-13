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
 * A class that implements the logical function "and".
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:1.0:function:and': This function SHALL return "True" if it has no arguments and SHALL
 * return "False" if one of its arguments evaluates to "False". The order of evaluation SHALL be from first argument to last. The evaluation SHALL stop with a
 * result of "False" if any argument evaluates to "False", leaving the rest of the arguments unevaluated.
 * 
 */
public final class LogicalAndFunction extends FirstOrderFunction.SingleParameterTyped<BooleanValue, BooleanValue>
{
	/**
	 * XACML standard identifier for the "and" logical function
	 */
	public static final String NAME_AND = XACML_NS_1_0 + "and";

	private static final class CallFactory
	{

		private static final String INVALID_ARG_TYPE_MESSAGE_PREFIX = "Function " + NAME_AND + ": Invalid type (expected = " + BooleanValue.class.getName()
				+ ") of arg#";
		private static final String INDETERMINATE_ARG_MESSAGE_PREFIX = "Function " + NAME_AND + ": Indeterminate arg #";

		private final FunctionSignature.SingleParameterTyped<BooleanValue, BooleanValue> funcSig;

		private CallFactory(FunctionSignature.SingleParameterTyped<BooleanValue, BooleanValue> functionSignature)
		{
			this.funcSig = functionSignature;
		}

		protected FirstOrderFunctionCall<BooleanValue> getInstance(final List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			/**
			 * TODO: optimize this function call by checking the following:
			 * <ol>
			 * <li>If any argument expression is constant BooleanAttributeValue False, return always False.</li>
			 * <li>Else If all argument expressions are constant BooleanAttributeValue True, return always True.</li>
			 * <li>
			 * Else If any argument expression is constant BooleanAttributeValue True, remove it from the arguments, as it has no effect on the final result.
			 * Indeed, and function is commutative and and(true, x, y...) = and(x, y...).</li>
			 * </ol>
			 * The first two optimizations can be achieved by pre-evaluating the function call with context = null and check the result if no
			 * IndeterminateEvaluationException is thrown.
			 */
			return new FirstOrderFunctionCall<BooleanValue>(funcSig, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
				{
					IndeterminateEvaluationException indeterminateException = null;
					int argIndex = 0;
					for (final Expression<?> arg : argExpressions)
					{
						// Evaluate the argument
						final BooleanValue attrVal;
						try
						{
							attrVal = Expressions.eval(arg, context, DatatypeConstants.BOOLEAN.TYPE);
							if (!attrVal.getUnderlyingValue())
							{
								return BooleanValue.FALSE;
							}
						} catch (IndeterminateEvaluationException e)
						{
							// keep the indeterminate error to throw later if there was not any FALSE in
							// remaining args
							indeterminateException = new IndeterminateEvaluationException(INDETERMINATE_ARG_MESSAGE_PREFIX + argIndex,
									StatusHelper.STATUS_PROCESSING_ERROR, e);
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
								attrVal = BooleanValue.class.cast(arg);
							} catch (ClassCastException e)
							{
								throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE_PREFIX + argIndex + ": " + arg.getClass().getName(),
										StatusHelper.STATUS_PROCESSING_ERROR, e);
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
			};
		}

	}

	private final CallFactory funcCallFactory;

	/**
	 * Instantiates the function
	 * 
	 */
	public LogicalAndFunction()
	{
		super(NAME_AND, DatatypeConstants.BOOLEAN.TYPE, true, Arrays.asList(DatatypeConstants.BOOLEAN.TYPE));
		this.funcCallFactory = new CallFactory(this.functionSignature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
			throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}
}
