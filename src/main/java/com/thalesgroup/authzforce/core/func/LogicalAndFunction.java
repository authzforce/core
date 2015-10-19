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
package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;

/**
 * A class that implements the logical function "and".
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:1.0:function:and': This
 * function SHALL return "True" if it has no arguments and SHALL return "False" if one of its
 * arguments evaluates to "False". The order of evaluation SHALL be from first argument to last. The
 * evaluation SHALL stop with a result of "False" if any argument evaluates to "False", leaving the
 * rest of the arguments unevaluated.
 * 
 */
public class LogicalAndFunction extends FirstOrderFunction<BooleanAttributeValue>
{
	/**
	 * XACML standard identifier for the "and" logical function
	 */
	public static final String NAME_AND = FUNCTION_NS_1 + "and";

	/**
	 * Instantiates the function
	 * 
	 */
	public LogicalAndFunction()
	{
		super(NAME_AND, DatatypeConstants.BOOLEAN.TYPE, true, DatatypeConstants.BOOLEAN.TYPE);
	}

	private static final String INVALID_ARG_TYPE_MESSAGE_PREFIX = "Function " + NAME_AND + ": Invalid type (expected = " + BooleanAttributeValue.class.getName() + ") of arg#";

	/**
	 * Logical 'and' evaluation method
	 * 
	 * @param context
	 * @param checkedArgExpressions
	 *            arg expression whose return type is assumed valid (already checked) for this
	 *            function
	 * @param checkedRemainingArgs
	 *            remaining arg values, whose datatype is assumed valid (already checked) for this
	 *            function
	 * @return true iff all checkedArgExpressions return True and all remainingArgs are True
	 * @throws IndeterminateEvaluationException
	 */
	public BooleanAttributeValue eval(EvaluationContext context, List<Expression<?>> checkedArgExpressions, AttributeValue<?>[] checkedRemainingArgs) throws IndeterminateEvaluationException
	{
		IndeterminateEvaluationException indeterminateException = null;
		int argIndex = 0;
		for (final Expression<?> arg : checkedArgExpressions)
		{
			// Evaluate the argument
			final BooleanAttributeValue attrVal;
			try
			{
				attrVal = Utils.evalSingle(arg, context, DatatypeConstants.BOOLEAN.TYPE);
				if (!attrVal.getUnderlyingValue())
				{
					return BooleanAttributeValue.FALSE;
				}
			} catch (IndeterminateEvaluationException e)
			{
				// keep the indeterminate error to throw later if there was not any FALSE in
				// remaining args
				indeterminateException = new IndeterminateEvaluationException(getIndeterminateArgMessage(argIndex), StatusHelper.STATUS_PROCESSING_ERROR, e);
			}

			argIndex++;
		}

		// do the same with remaining arg values
		if (checkedRemainingArgs != null)
		{

			for (final AttributeValue<?> arg : checkedRemainingArgs)
			{
				// Evaluate the argument
				final BooleanAttributeValue attrVal;
				try
				{
					attrVal = BooleanAttributeValue.class.cast(arg);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE_PREFIX + argIndex + ": " + arg.getClass().getName(), StatusHelper.STATUS_PROCESSING_ERROR, e);
				}

				if (!attrVal.getUnderlyingValue())
				{
					return BooleanAttributeValue.FALSE;
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

		return BooleanAttributeValue.TRUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(final List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		/**
		 * TODO: optimize this function call by checking the following:
		 * <ol>
		 * <li>If any argument expression is constant BooleanAttributeValue False, return always
		 * False.</li>
		 * <li>Else If all argument expressions are constant BooleanAttributeValue True, return
		 * always True.</li>
		 * <li>
		 * Else If any argument expression is constant BooleanAttributeValue True, remove it from
		 * the arguments, as it has no effect on the final result. Indeed, and function is
		 * commutative and and(true, x, y...) = and(x, y...).</li>
		 * </ol>
		 * The first two optimizations can be achieved by pre-evaluating the function call with
		 * context = null and check the result if no IndeterminateEvaluationException is thrown.
		 */

		return new FirstOrderFunctionCall<BooleanAttributeValue>(signature, argExpressions, remainingArgTypes)
		{

			@Override
			protected BooleanAttributeValue evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
			{
				return eval(context, argExpressions, remainingArgs);
			}
		};
	}
}
