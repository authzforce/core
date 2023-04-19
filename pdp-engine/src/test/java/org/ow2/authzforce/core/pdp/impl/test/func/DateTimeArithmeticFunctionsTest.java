/*
 * Copyright 2012-2023 THALES.
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
package org.ow2.authzforce.core.pdp.impl.test.func;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class DateTimeArithmeticFunctionsTest extends StandardFunctionTest
{

	public DateTimeArithmeticFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
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
	public static Collection<Object[]> params()
	{
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration
				new Object[] { NAME_DATETIME_ADD_DAYTIMEDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("P1DT2H")), new DateTimeValue("2002-09-25T11:30:15") }, //
						new Object[] { NAME_DATETIME_ADD_DAYTIMEDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("-P1DT2H")),
								new DateTimeValue("2002-09-23T07:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
						new Object[] { NAME_DATETIME_ADD_YEARMONTHDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("P1Y2M")),
								new DateTimeValue("2003-11-24T09:30:15") },//
						new Object[] { NAME_DATETIME_ADD_YEARMONTHDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("-P1Y2M")),
								new DateTimeValue("2001-07-24T09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
						new Object[] { NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("P1DT2H")),
								new DateTimeValue("2002-09-23T07:30:15") },//
						new Object[] { NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DayTimeDurationValue("-P1DT2H")),
								new DateTimeValue("2002-09-25T11:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
						new Object[] { NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("P1Y2M")),
								new DateTimeValue("2001-07-24T09:30:15") },//
						new Object[] { NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new YearMonthDurationValue("-P1Y2M")),
								new DateTimeValue("2003-11-24T09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
						new Object[] { NAME_DATE_ADD_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("P1Y2M")), new DateValue("2003-11-24") }, new Object[] {
								NAME_DATE_ADD_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("-P1Y2M")), new DateValue("2001-07-24") },

						// urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
						new Object[] { NAME_DATE_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("P1Y2M")), new DateValue("2001-07-24") },//
						new Object[] { NAME_DATE_SUBTRACT_YEARMONTHDURATION, Arrays.asList(new DateValue("2002-09-24"), new YearMonthDurationValue("-P1Y2M")), new DateValue("2003-11-24") });
	}

}
