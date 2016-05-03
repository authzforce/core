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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.impl.value.BaseTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DateValue;
import org.ow2.authzforce.core.pdp.impl.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.impl.value.DurationValue;
import org.ow2.authzforce.core.pdp.impl.value.YearMonthDurationValue;

/**
 * Implements generic match functions taking parameters of possibly different types.
 *
 * @param <T>
 *            type of first parameter and returned value (date/time)
 * @param <D>
 *            type of second parameter (duration)
 * 
 * @version $Id: $
 */
public final class TemporalArithmeticFunction<T extends BaseTimeValue<T>, D extends DurationValue<D>> extends FirstOrderFunction.MultiParameterTyped<T>
{
	/**
	 * Standard identifier for the dateTime-add-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_ADD_DAYTIMEDURATION = XACML_NS_3_0 + "dateTime-add-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-subtract-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION = XACML_NS_3_0 + "dateTime-subtract-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-add-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_ADD_YEARMONTHDURATION = XACML_NS_3_0 + "dateTime-add-yearMonthDuration";

	/**
	 * Standard identifier for the dateTime-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION = XACML_NS_3_0 + "dateTime-subtract-yearMonthDuration";

	/**
	 * Standard identifier for the date-add-yearMonthDuration function.
	 */
	public static final String NAME_DATE_ADD_YEARMONTHDURATION = XACML_NS_3_0 + "date-add-yearMonthDuration";

	/**
	 * Standard identifier for the date-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION = XACML_NS_3_0 + "date-subtract-yearMonthDuration";

	private interface StaticOperation<TV extends BaseTimeValue<TV>, DV extends DurationValue<DV>>
	{

		TV eval(TV time, DV duration);
	}

	private static final class Call<TV extends BaseTimeValue<TV>, DV extends DurationValue<DV>> extends EagerMultiPrimitiveTypeEval<TV>
	{
		private final String invalidArgTypesErrorMsg;
		private final Class<DV> durationParamClass;
		private final Class<TV> timeParamClass;
		private final StaticOperation<TV, DV> op;

		private Call(FunctionSignature<TV> functionSig, Datatype<TV> timeParamType, Datatype<DV> durationParamType, StaticOperation<TV, DV> op,
				List<Expression<?>> args, Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + timeParamType + "," + durationParamType + "): ";
			this.timeParamClass = timeParamType.getValueClass();
			this.durationParamClass = durationParamType.getValueClass();
			this.op = op;
		}

		@Override
		protected TV evaluate(Deque<AttributeValue> args) throws IndeterminateEvaluationException
		{
			final AttributeValue rawArg0 = args.poll();
			final AttributeValue rawArg1 = args.poll();

			final TV arg0;
			final DV arg1;
			try
			{
				arg0 = timeParamClass.cast(rawArg0);
				arg1 = durationParamClass.cast(rawArg1);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + "," + rawArg1.getDataType(),
						StatusHelper.STATUS_PROCESSING_ERROR, e);
			}

			return op.eval(arg0, arg1);
		}
	}

	private final StaticOperation<T, D> op;

	private final Datatype<T> timeParamType;

	private final Datatype<D> durationParamType;

	/**
	 * Creates a new Date-time arithmetic function
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param durationParamType
	 *            second parameter type (duration)
	 * @param timeParamType
	 *            first parameter type (date/time)
	 * @param op
	 *            temporal arithmetic operation
	 */
	private TemporalArithmeticFunction(String functionName, Datatype<T> timeParamType, Datatype<D> durationParamType, StaticOperation<T, D> op)
	{
		super(functionName, timeParamType, false, Arrays.asList(timeParamType, durationParamType));
		this.timeParamType = timeParamType;
		this.durationParamType = durationParamType;

		this.op = op;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<T> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new Call<>(functionSignature, timeParamType, durationParamType, op, argExpressions, remainingArgTypes);
	}

	private static final class TimeAddDurationOperation<T extends BaseTimeValue<T>, D extends DurationValue<D>> implements StaticOperation<T, D>
	{

		@Override
		public T eval(T time, D duration)
		{
			return time.add(duration);

		}

	}

	private static final class TimeSubtractDurationOperation<T extends BaseTimeValue<T>, D extends DurationValue<D>> implements StaticOperation<T, D>
	{

		@Override
		public T eval(T time, D duration)
		{
			return time.subtract(duration);
		}

	}

	/**
	 * Temporal arithmetic function cluster
	 */
	public static final FunctionSet CLUSTER = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-arithmetic",
	//
			new TemporalArithmeticFunction<>(NAME_DATETIME_ADD_DAYTIMEDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.DAYTIMEDURATION.TYPE,
					new TimeAddDurationOperation<DateTimeValue, DayTimeDurationValue>()),
			//
			new TemporalArithmeticFunction<>(NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.DAYTIMEDURATION.TYPE,
					new TimeSubtractDurationOperation<DateTimeValue, DayTimeDurationValue>()),
			//
			new TemporalArithmeticFunction<>(NAME_DATETIME_ADD_YEARMONTHDURATION, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE,
					new TimeAddDurationOperation<DateTimeValue, YearMonthDurationValue>()),
			//
			new TemporalArithmeticFunction<>(NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, DatatypeConstants.DATETIME.TYPE,
					DatatypeConstants.YEARMONTHDURATION.TYPE, new TimeSubtractDurationOperation<DateTimeValue, YearMonthDurationValue>()),
			//
			new TemporalArithmeticFunction<>(NAME_DATE_ADD_YEARMONTHDURATION, DatatypeConstants.DATE.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE,
					new TimeAddDurationOperation<DateValue, YearMonthDurationValue>()),
			//
			new TemporalArithmeticFunction<>(NAME_DATE_SUBTRACT_YEARMONTHDURATION, DatatypeConstants.DATE.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE,
					new TimeSubtractDurationOperation<DateValue, YearMonthDurationValue>()));

}
