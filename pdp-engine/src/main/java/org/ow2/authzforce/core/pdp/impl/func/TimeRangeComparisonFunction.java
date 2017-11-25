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
import java.util.Calendar;
import java.util.Deque;
import java.util.List;
import java.util.TimeZone;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;

/**
 * Time range comparison function (XACML 2.0: time-in-range), which takes three time values and returns true if the first value falls between the second and the third value
 * <p>
 * Note that this function allows any time ranges less than 24 hours. In other words, it is not bound by normal day boundaries (midnight GMT), but by the minimum time in the range. This means that
 * ranges like 9am-5pm are supported, as are ranges like 5pm-9am.
 * 
 * @version $Id: $
 */
final class TimeRangeComparisonFunction extends SingleParameterTypedFirstOrderFunction<BooleanValue, TimeValue>
{

	/**
	 * Default constructor.
	 * 
	 * @param functionId
	 *            function ID
	 */
	TimeRangeComparisonFunction(final String functionId)
	{
		/**
		 * boolean timeInRange(time,time,time)
		 */
		super(functionId, StandardDatatypes.BOOLEAN, false, Arrays.asList(StandardDatatypes.TIME, StandardDatatypes.TIME, StandardDatatypes.TIME));
	}

	private static final class Call extends BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval<BooleanValue, TimeValue>
	{
		/**
		 * XACML says: "If no time zone is provided for the first argument, it SHALL use the default time zone at the context handler."
		 */
		private static final TimeZone DEFAULT_TZ = TimeZone.getDefault();

		/**
		 * Set {@code cal}'s date to the same as {@code ref}'s date
		 * 
		 * @param cal
		 * @param ref
		 */
		private static void setSameDate(final Calendar cal, final Calendar ref)
		{
			cal.set(Calendar.YEAR, ref.get(Calendar.YEAR));
			cal.set(Calendar.DAY_OF_YEAR, ref.get(Calendar.DAY_OF_YEAR));
		}

		/**
		 * Evaluates the time-in-range function, which takes three <code>TimeAttributeValue</code> values. This function return true if the first value falls between the second and third values (ie.,
		 * on or after the second time and on or before the third time). If no time zone is specified for the second and/or third time value, then the timezone from the first time value is used. This
		 * lets you say time-in-range(current-time, 9am, 5pm) and always have the evaluation happen in your current-time timezone.
		 * 
		 * @param arg
		 *            time to be checked against the lower and upper bounds
		 * @param lowerBound
		 *            lower time bound
		 * @param upperBound
		 *            upper time bound
		 * @return true iff arg is in range [lowerBound, upperBound]
		 * 
		 * 
		 */
		public static boolean eval(final TimeValue arg, final TimeValue lowerBound, final TimeValue upperBound)
		{
			// get the three time values
			final Calendar calCheckedWhetherInRange = arg.getUnderlyingValue().toGregorianCalendar();
			if (calCheckedWhetherInRange.getTimeZone() == null)
			{
				calCheckedWhetherInRange.setTimeZone(DEFAULT_TZ);
			}

			final Calendar startCal = lowerBound.getUnderlyingValue().toGregorianCalendar();
			if (startCal.getTimeZone() == null)
			{
				startCal.setTimeZone(calCheckedWhetherInRange.getTimeZone());
			}
			final Calendar endCal = upperBound.getUnderlyingValue().toGregorianCalendar();
			if (endCal.getTimeZone() == null)
			{
				endCal.setTimeZone(calCheckedWhetherInRange.getTimeZone());
			}

			/*
			 * Use start time as reference for the day in time comparisons, so set the timeChecked day to the one of the start time
			 */
			setSameDate(calCheckedWhetherInRange, startCal);
			/*
			 * Now we date does not matter in calendar comparison, we only compare times of the day so ignoring the date, the checked time of the day might be before the lower time bound but still be
			 * in range if considered this is the time on the next day. In this case, startCal is on day N, and calCheckedWhetherInRange on day N+1.
			 */
			/*
			 * Boolean below says whether the checked time is strictly after the start time if considered on the *same day*, i.e. in terms of time of day.
			 */
			final boolean isCheckedDayTimeStrictlyBeforeStartDayTime = calCheckedWhetherInRange.before(startCal);
			if (startCal.after(endCal))
			{
				/**
				 * start time of the day > end time of the day, for instance 02:00:00 > 01:00:00 so we consider the end time (01:00:00) on the next day (later than the second argument - end time - by
				 * less than 24h, the spec says). So we interpret the time interval as the date interval [startTime on day N, endTime on day N+1]. If checked time of day < start time of day (compared
				 * on the same day), then checked time can only be on day after to be in range
				 */
				if (isCheckedDayTimeStrictlyBeforeStartDayTime)
				{
					/*
					 * time checked is strictly before start time if considered on the same day, so not in range unless considered on day N+1 So let's compared with end time after considering them on
					 * the same day
					 */
					// calCheckedWhetherInRange.add(Calendar.DAY_OF_YEAR, 1);
					// set checked time to same day as end time for comparison
					setSameDate(calCheckedWhetherInRange, endCal);
					// time checked is in range if and only if before or equals end time (on day N+1),
					// i.e. not strictly after
					return !calCheckedWhetherInRange.after(endCal);
				}

				/*
				 * Time checked is after or equal to start time, so it is in range (on day N), as we already consider end time to be on day N+1
				 */
				return true;
			}

			// start time <= end time -> all considered on the same day
			if (isCheckedDayTimeStrictlyBeforeStartDayTime)
			{
				// checked time < start time -> out of range
				return false;
			}

			// checked time >= start time

			// set checked time to same day as end time for comparison
			setSameDate(calCheckedWhetherInRange, endCal);
			// time checked is in range if and only if before or equals end time, so not strictly after
			return !calCheckedWhetherInRange.after(endCal);
		}

		private Call(final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, TimeValue> functionSignature, final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{
			super(functionSignature, argExpressions, remainingArgTypes);
		}

		@Override
		protected BooleanValue evaluate(final Deque<TimeValue> argStack) throws IndeterminateEvaluationException
		{
			/*
			 * args.poll() returns the first element and remove it from the stack, so that next poll() returns the next element (and removes it from the stack), etc.
			 */
			return BooleanValue.valueOf(eval(argStack.poll(), argStack.poll(), argStack.poll()));
		}
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new Call(functionSignature, argExpressions, remainingArgTypes);
	}
}
