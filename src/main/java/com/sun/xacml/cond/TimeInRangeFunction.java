/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.cond;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;
import com.thalesgroup.authzforce.core.func.BaseFunction;

/**
 * This class implements the time-in-range function, which takes three time values and returns true
 * if the first value falls between the second and the third value. This function was introduced in
 * XACML 2.0.
 * <p>
 * Note that this function allows any time ranges less than 24 hours. In other words, it is not
 * bound by normal day boundries (midnight GMT), but by the minimum time in the range. This means
 * that ranges like 9am-5pm are supported, as are ranges like 5pm-9am.
 * 
 * @since 2.0
 * @author seth proctor
 */
public class TimeInRangeFunction extends BaseFunction<PrimitiveResult<BooleanAttributeValue>>
{

	/**
	 * The identifier for this function
	 */
	public static final String NAME = FUNCTION_NS_2 + "time-in-range";

	/**
	 * Default constructor.
	 */
	public TimeInRangeFunction()
	{
		/**
		 * boolean timeInRange(time,time,time)
		 */
		super(NAME, BooleanAttributeValue.TYPE, false, TimeAttributeValue.TYPE, TimeAttributeValue.TYPE, TimeAttributeValue.TYPE);
	}

	private static final TimeZone DEFAULT_TZ = TimeZone.getDefault();

	private static void setSameDate(Calendar cal, Calendar ref)
	{
		cal.set(Calendar.YEAR, ref.get(Calendar.YEAR));
		cal.set(Calendar.DAY_OF_YEAR, ref.get(Calendar.DAY_OF_YEAR));
	}

	/**
	 * Evaluates the time-in-range function, which takes three <code>TimeAttributeValue</code>
	 * values. This function return true if the first value falls between the second and third
	 * values (ie., on or after the second time and on or before the third time). If no time zone is
	 * specified for the second and/or third time value, then the timezone from the first time value
	 * is used. This lets you say time-in-range(current-time, 9am, 5pm) and always have the
	 * evaluation happen in your current-time timezone.
	 * 
	 * @param arg
	 * @param lowerBound
	 * @param upperBound
	 * @return true iff arg is in range [lowerBound, upperBound]
	 * 
	 * 
	 */
	public static boolean eval(TimeAttributeValue arg, TimeAttributeValue lowerBound, TimeAttributeValue upperBound)
	{
		// get the three time values
		final Calendar calCheckedWhetherInRange = arg.getValue().toGregorianCalendar();
		if (calCheckedWhetherInRange.getTimeZone() == null)
		{
			calCheckedWhetherInRange.setTimeZone(DEFAULT_TZ);
		}

		final Calendar startCal = lowerBound.getValue().toGregorianCalendar();
		if (startCal.getTimeZone() == null)
		{
			startCal.setTimeZone(calCheckedWhetherInRange.getTimeZone());
		}
		final Calendar endCal = upperBound.getValue().toGregorianCalendar();
		if (endCal.getTimeZone() == null)
		{
			endCal.setTimeZone(calCheckedWhetherInRange.getTimeZone());
		}

		/*
		 * Use start time as reference for the day in time comparisons, so set the timeChecked day
		 * to the one of the start time
		 */
		setSameDate(calCheckedWhetherInRange, startCal);
		final boolean isStrictlyBeforeStart = calCheckedWhetherInRange.before(startCal);
		if (startCal.after(endCal))
		{
			/**
			 * start time > end time, for instance 02:00:00 > 01:00:00 so we consider the end time
			 * (01:00:00) a day later (later than the second argument - end time - by less than 24h,
			 * the spec says) So we interpret the time interval as the date interval [startTime on
			 * day 1, endTime on day 2] So we check the two possibilities considering:
			 * <ol>
			 * <li>Time checked assumed on day 1 (same as startCal) -> it is in range if and only if
			 * after or equals startCal, i.e. not before 2.</li>
			 * <li>(If not 1.) Time checked assumed on day 2 -> it is in range if and only if before
			 * or equals endTime, i.e. not after.</li>
			 * </ol>
			 */
			if (isStrictlyBeforeStart)
			{
				// time checked is strictly before start time so not in range on day 1; try day 2
				calCheckedWhetherInRange.add(Calendar.DAY_OF_YEAR, 1);
				// set end time on the same day
				setSameDate(endCal, calCheckedWhetherInRange);
				// time checked is in range if and only if before or equals end time (on day 2),
				// i.e. not strictly after
				return !calCheckedWhetherInRange.after(startCal);
			}

			// time checked is after or equal to start time, so it is in range on day1
			return true;
		}

		// start time <= end time, then all considered on the same day
		if (isStrictlyBeforeStart)
		{
			return false;
		}

		// set end time on the same day
		setSameDate(endCal, calCheckedWhetherInRange);
		// time checked is in range if and only if before or equals end time, so not strictly after
		return !calCheckedWhetherInRange.after(startCal);
	}

	@Override
	protected Call getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes) throws IllegalArgumentException
	{
		return new EagerPrimitiveEvalCall<TimeAttributeValue>(TimeAttributeValue[].class, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected PrimitiveResult<BooleanAttributeValue> evaluate(TimeAttributeValue[] args) throws IndeterminateEvaluationException
			{
				return PrimitiveResult.getInstance(eval(args[0], args[1], args[2]));
			}
		};
	}

}
