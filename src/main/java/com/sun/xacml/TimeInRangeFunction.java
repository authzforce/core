/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;
import java.util.TimeZone;

import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.TimeValue;

/**
 * This class implements the time-in-range function, which takes three time values and returns true if the first value falls between the second and the third value. This function was introduced in
 * XACML 2.0.
 * <p>
 * Note that this function allows any time ranges less than 24 hours. In other words, it is not bound by normal day boundries (midnight GMT), but by the minimum time in the range. This means that
 * ranges like 9am-5pm are supported, as are ranges like 5pm-9am.
 *
 * @since 2.0
 * @author seth proctor
 * @version $Id: $
 */
public final class TimeInRangeFunction extends FirstOrderFunction.SingleParameterTyped<BooleanValue, TimeValue>
{

	/**
	 * The identifier for this function
	 */
	public static final String NAME = XACML_NS_2_0 + "time-in-range";

	/**
	 * Singleton instance: Effective Java, item 3
	 */
	public static final TimeInRangeFunction INSTANCE = new TimeInRangeFunction();

	/**
	 * Default constructor.
	 */
	private TimeInRangeFunction()
	{
		/**
		 * boolean timeInRange(time,time,time)
		 */
		super(NAME, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(DatatypeConstants.TIME.TYPE, DatatypeConstants.TIME.TYPE, DatatypeConstants.TIME.TYPE));
	}

	private static final class Call extends FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval<BooleanValue, TimeValue>
	{
		private static final TimeZone DEFAULT_TZ = TimeZone.getDefault();

		/**
		 * Set {@code cal}'s date to the same as {@code ref}'s date
		 * 
		 * @param cal
		 * @param ref
		 */
		private static void setSameDate(Calendar cal, Calendar ref)
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
		public static boolean eval(TimeValue arg, TimeValue lowerBound, TimeValue upperBound)
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

		private Call(FunctionSignature.SingleParameterTyped<BooleanValue, TimeValue> functionSignature, List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
		{
			super(functionSignature, argExpressions, remainingArgTypes);
		}

		@Override
		protected BooleanValue evaluate(Deque<TimeValue> argStack) throws IndeterminateEvaluationException
		{
			/*
			 * args.poll() returns the first element and remove it from the stack, so that next poll() returns the next element (and removes it from the stack), etc.
			 */
			return BooleanValue.valueOf(eval(argStack.poll(), argStack.poll(), argStack.poll()));
		}
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new Call(functionSignature, argExpressions, remainingArgTypes);
	}
}
