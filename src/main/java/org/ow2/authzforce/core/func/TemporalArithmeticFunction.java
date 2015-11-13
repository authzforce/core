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

import java.util.Deque;
import java.util.List;

import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.BaseTimeAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;
import com.thalesgroup.authzforce.core.datatypes.DurationAttributeValue;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;

/**
 * Implements generic match functions taking parameters of possibly different types.
 * 
 * @param <T>
 *            type of first parameter and returned value (date/time)
 * @param <D>
 *            type of second parameter (duration)
 * 
 */
public abstract class TemporalArithmeticFunction<T extends BaseTimeAttributeValue<T>, D extends DurationAttributeValue<D>> extends FirstOrderFunction<T>
{
	/**
	 * Standard identifier for the dateTime-add-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_ADD_DAYTIMEDURATION = FUNCTION_NS_3 + "dateTime-add-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-subtract-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION = FUNCTION_NS_3 + "dateTime-subtract-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-add-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_ADD_YEARMONTHDURATION = FUNCTION_NS_3 + "dateTime-add-yearMonthDuration";

	/**
	 * Standard identifier for the dateTime-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION = FUNCTION_NS_3 + "dateTime-subtract-yearMonthDuration";

	/**
	 * Standard identifier for the date-add-yearMonthDuration function.
	 */
	public static final String NAME_DATE_ADD_YEARMONTHDURATION = FUNCTION_NS_3 + "date-add-yearMonthDuration";

	/**
	 * Standard identifier for the date-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION = FUNCTION_NS_3 + "date-subtract-yearMonthDuration";

	private final String invalidArgTypesErrorMsg;

	private final Class<T> firstParamClass;

	private final Class<D> secondParamClass;

	/**
	 * Creates a new Date-time arithmetic function
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param durationParamType
	 *            second parameter type (duration)
	 * @param timeParamType
	 *            first parameter type (date/time)
	 */
	public TemporalArithmeticFunction(String functionName, Datatype<T> timeParamType, Datatype<D> durationParamType)
	{
		super(functionName, timeParamType, false, timeParamType, durationParamType);
		this.firstParamClass = timeParamType.getValueClass();
		this.secondParamClass = durationParamType.getValueClass();
		invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + firstParamClass.getSimpleName() + "," + secondParamClass.getSimpleName() + "): ";
	}

	@Override
	protected final FirstOrderFunctionCall<T> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new EagerMultiPrimitiveTypeEval<T>(signature, argExpressions, remainingArgTypes)
		{

			@Override
			protected T evaluate(Deque<AttributeValue<?>> args) throws IndeterminateEvaluationException
			{
				final AttributeValue<?> rawArg0 = args.poll();
				final AttributeValue<?> rawArg1 = args.poll();

				final T arg0;
				final D arg1;
				try
				{
					arg0 = firstParamClass.cast(rawArg0);
					arg1 = secondParamClass.cast(rawArg1);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getReturnType() + "," + rawArg1.getReturnType(), StatusHelper.STATUS_PROCESSING_ERROR, e);
				}

				return eval(arg0, arg1);
			}

		};
	}

	protected abstract T eval(T time, D duration);

	private static class TimeAddDuration<T extends BaseTimeAttributeValue<T>, D extends DurationAttributeValue<D>> extends TemporalArithmeticFunction<T, D>
	{

		protected TimeAddDuration(String functionName, Datatype<T> timeParamType, Datatype<D> durationParamType)
		{
			super(functionName, timeParamType, durationParamType);
		}

		@Override
		protected final T eval(T time, D duration)
		{
			return time.add(duration);

		}

	}

	private static class TimeSubtractDuration<T extends BaseTimeAttributeValue<T>, D extends DurationAttributeValue<D>> extends TemporalArithmeticFunction<T, D>
	{

		protected TimeSubtractDuration(String functionName, Datatype<T> timeParamType, Datatype<D> durationParamType)
		{
			super(functionName, timeParamType, durationParamType);
		}

		@Override
		protected final T eval(T time, D duration)
		{
			return time.subtract(duration);
		}

	}

	/**
	 * Temporal arithmetic function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-arithmetic",
	//
			new TimeAddDuration<>(NAME_DATETIME_ADD_DAYTIMEDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.DAYTIMEDURATION.TYPE),
			//
			new TimeSubtractDuration<>(NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.DAYTIMEDURATION.TYPE),
			//
			new TimeAddDuration<>(NAME_DATETIME_ADD_YEARMONTHDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE),
			//
			new TimeSubtractDuration<>(NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE),
			//
			new TimeAddDuration<>(NAME_DATE_ADD_YEARMONTHDURATION, DatatypeConstants.DATE.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE),
			//
			new TimeSubtractDuration<>(NAME_DATE_SUBTRACT_YEARMONTHDURATION, DatatypeConstants.DATE.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE));

}
