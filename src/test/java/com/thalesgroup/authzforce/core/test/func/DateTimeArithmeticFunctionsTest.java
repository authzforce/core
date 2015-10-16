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
/**
 * 
 */
package com.thalesgroup.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class DateTimeArithmeticFunctionsTest extends GeneralFunctionTest
{

	public DateTimeArithmeticFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_DATETIME_ADD_DAYTIMEDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration";
	private static final String NAME_DATETIME_ADD_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration";
	private static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration";
	private static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration";
	private static final String NAME_DATE_ADD_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration";
	private static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration
				new Object[] { NAME_DATETIME_ADD_DAYTIMEDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DayTimeDurationAttributeValue("P1DT2H")), new DateTimeAttributeValue("2002-09-25T11:30:15") }, //
				new Object[] { NAME_DATETIME_ADD_DAYTIMEDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DayTimeDurationAttributeValue("-P1DT2H")), new DateTimeAttributeValue("2002-09-23T07:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
				new Object[] { NAME_DATETIME_ADD_YEARMONTHDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new YearMonthDurationAttributeValue("P1Y2M")), new DateTimeAttributeValue("2003-11-24T09:30:15") },//
				new Object[] { NAME_DATETIME_ADD_YEARMONTHDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new YearMonthDurationAttributeValue("-P1Y2M")), new DateTimeAttributeValue("2001-07-24T09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
				new Object[] { NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DayTimeDurationAttributeValue("P1DT2H")), new DateTimeAttributeValue("2002-09-23T07:30:15") },//
				new Object[] { NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DayTimeDurationAttributeValue("-P1DT2H")), new DateTimeAttributeValue("2002-09-25T11:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
				new Object[] { NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new YearMonthDurationAttributeValue("P1Y2M")), new DateTimeAttributeValue("2001-07-24T09:30:15") },//
				new Object[] { NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new YearMonthDurationAttributeValue("-P1Y2M")), new DateTimeAttributeValue("2003-11-24T09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
				new Object[] { NAME_DATE_ADD_YEARMONTHDURATION, Arrays.asList(new DateAttributeValue("2002-09-24"), new YearMonthDurationAttributeValue("P1Y2M")), new DateAttributeValue("2003-11-24") },
				new Object[] { NAME_DATE_ADD_YEARMONTHDURATION, Arrays.asList(new DateAttributeValue("2002-09-24"), new YearMonthDurationAttributeValue("-P1Y2M")), new DateAttributeValue("2001-07-24") },

				// urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
				new Object[] { NAME_DATE_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateAttributeValue("2002-09-24"), new YearMonthDurationAttributeValue("P1Y2M")), new DateAttributeValue("2001-07-24") },//
				new Object[] { NAME_DATE_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateAttributeValue("2002-09-24"), new YearMonthDurationAttributeValue("-P1Y2M")), new DateAttributeValue("2003-11-24") });
	}

}
