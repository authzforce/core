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
/**
 * 
 */
package org.ow2.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.impl.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DateValue;
import org.ow2.authzforce.core.pdp.impl.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.impl.value.YearMonthDurationValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class DateTimeArithmeticFunctionsTest extends FunctionTest
{

	public DateTimeArithmeticFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
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
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration
				new Object[] { NAME_DATETIME_ADD_DAYTIMEDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("P1DT2H")),
						new DateTimeValue("2002-09-25T11:30:15") }, //
						new Object[] { NAME_DATETIME_ADD_DAYTIMEDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("-P1DT2H")),
								new DateTimeValue("2002-09-23T07:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
						new Object[] { NAME_DATETIME_ADD_YEARMONTHDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("P1Y2M")),
								new DateTimeValue("2003-11-24T09:30:15") },//
						new Object[] { NAME_DATETIME_ADD_YEARMONTHDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("-P1Y2M")),
								new DateTimeValue("2001-07-24T09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
						new Object[] { NAME_DATETIME_SUBTRACT_DAYTIMEDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("P1DT2H")),
								new DateTimeValue("2002-09-23T07:30:15") },//
						new Object[] { NAME_DATETIME_SUBTRACT_DAYTIMEDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("-P1DT2H")),
								new DateTimeValue("2002-09-25T11:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
						new Object[] { NAME_DATETIME_SUBTRACT_YEARMONTHDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("P1Y2M")),
								new DateTimeValue("2001-07-24T09:30:15") },//
						new Object[] { NAME_DATETIME_SUBTRACT_YEARMONTHDURATION,
								Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("-P1Y2M")),
								new DateTimeValue("2003-11-24T09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
						new Object[] { NAME_DATE_ADD_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("P1Y2M")),
								new DateValue("2003-11-24") },
						new Object[] { NAME_DATE_ADD_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("-P1Y2M")),
								new DateValue("2001-07-24") },

						// urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
						new Object[] { NAME_DATE_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("P1Y2M")),
								new DateValue("2001-07-24") },//
						new Object[] { NAME_DATE_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("-P1Y2M")),
								new DateValue("2003-11-24") });
	}

}
