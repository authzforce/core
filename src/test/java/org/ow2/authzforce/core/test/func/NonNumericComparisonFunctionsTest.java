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
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DateValue;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.value.TimeValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class NonNumericComparisonFunctionsTest extends FunctionTest
{

	public NonNumericComparisonFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_STRING_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:string-greater-than";
	private static final String NAME_STRING_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal";
	private static final String NAME_STRING_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:string-less-than";
	private static final String NAME_STRING_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal";
	private static final String NAME_TIME_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:time-greater-than";
	private static final String NAME_TIME_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal";
	private static final String NAME_TIME_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:time-less-than";
	private static final String NAME_TIME_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal";
	private static final String NAME_TIME_IN_RANGE = "urn:oasis:names:tc:xacml:2.0:function:time-in-range";
	private static final String NAME_DATETIME_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than";
	private static final String NAME_DATETIME_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal";
	private static final String NAME_DATETIME_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than";
	private static final String NAME_DATETIME_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal";
	private static final String NAME_DATE_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:date-greater-than";
	private static final String NAME_DATE_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal";
	private static final String NAME_DATE_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:date-less-than";
	private static final String NAME_DATE_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-greater-than
				new Object[] { NAME_STRING_GREATER_THAN, Arrays.asList(new StringValue("First"), new StringValue("Second")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_GREATER_THAN, Arrays.asList(new StringValue("Third"), new StringValue("Fourth")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_GREATER_THAN, Arrays.asList(new StringValue("Fifth"), new StringValue("Fifth")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal
				new Object[] { NAME_STRING_GREATER_THAN_OR_EQUAL, Arrays.asList(new StringValue("First"), new StringValue("Second")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_GREATER_THAN_OR_EQUAL, Arrays.asList(new StringValue("Third"), new StringValue("Fourth")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_GREATER_THAN_OR_EQUAL, Arrays.asList(new StringValue("Fifth"), new StringValue("Fifth")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:string-less-than
				new Object[] { NAME_STRING_LESS_THAN, Arrays.asList(new StringValue("First"), new StringValue("Second")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_LESS_THAN, Arrays.asList(new StringValue("Third"), new StringValue("Fourth")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_LESS_THAN, Arrays.asList(new StringValue("Fifth"), new StringValue("Fifth")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal
				new Object[] { NAME_STRING_LESS_THAN_OR_EQUAL, Arrays.asList(new StringValue("First"), new StringValue("Second")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_LESS_THAN_OR_EQUAL, Arrays.asList(new StringValue("Third"), new StringValue("Fourth")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_LESS_THAN_OR_EQUAL, Arrays.asList(new StringValue("Fifth"), new StringValue("Fifth")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:time-greater-than
				new Object[] { NAME_TIME_GREATER_THAN, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:44:22")), BooleanValue.FALSE },
				new Object[] { NAME_TIME_GREATER_THAN, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("08:50:48")), BooleanValue.TRUE },
				new Object[] { NAME_TIME_GREATER_THAN, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:15")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal
				new Object[] { NAME_TIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:44:22")), BooleanValue.FALSE },
				new Object[] { NAME_TIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("08:50:48")), BooleanValue.TRUE },
				new Object[] { NAME_TIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:15")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:time-less-than
				new Object[] { NAME_TIME_LESS_THAN, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:44:22")), BooleanValue.TRUE },
				new Object[] { NAME_TIME_LESS_THAN, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("08:50:48")), BooleanValue.FALSE },
				new Object[] { NAME_TIME_LESS_THAN, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:15")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal
				new Object[] { NAME_TIME_LESS_THAN_OR_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:44:22")), BooleanValue.TRUE },
				new Object[] { NAME_TIME_LESS_THAN_OR_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("08:50:48")), BooleanValue.FALSE },
				new Object[] { NAME_TIME_LESS_THAN_OR_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:15")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:2.0:function:time-in-range
				/*
				 * Time interval lower bound and upper bound are the same (lower bound = upper bound)
				 */
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:00"), new TimeValue("09:30:00")),
						BooleanValue.FALSE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:30:00"), new TimeValue("09:30:00"), new TimeValue("09:30:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:28:15"), new TimeValue("09:30:00"), new TimeValue("09:30:00")),
						BooleanValue.FALSE },
				/*
				 * Time interval lower bound and upper bound on the same day (lower bound < upper bound)
				 */
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:28:15"), new TimeValue("09:30:00"), new TimeValue("09:45:00")),
						BooleanValue.FALSE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:30:00"), new TimeValue("09:30:00"), new TimeValue("09:45:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:00"), new TimeValue("09:45:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:45:00"), new TimeValue("09:30:00"), new TimeValue("09:45:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:47:15"), new TimeValue("09:30:00"), new TimeValue("09:45:00")),
						BooleanValue.FALSE },
				/*
				 * Time interval upper bound on the day after (lower bound > upper bound upper bound)
				 */
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:28:15"), new TimeValue("09:30:00"), new TimeValue("02:45:00")),
						BooleanValue.FALSE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:30:00"), new TimeValue("09:30:00"), new TimeValue("02:45:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:00"), new TimeValue("02:45:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("01:30:15"), new TimeValue("09:30:00"), new TimeValue("02:45:00")),
						BooleanValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeValue("02:45:00"), new TimeValue("09:30:00"), new TimeValue("02:45:00")),
						BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than
				new Object[] { NAME_DATETIME_GREATER_THAN, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:44:22")),
						BooleanValue.FALSE },
				new Object[] { NAME_DATETIME_GREATER_THAN, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-23T23:50:48")),
						BooleanValue.TRUE },
				new Object[] { NAME_DATETIME_GREATER_THAN, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal
				new Object[] { NAME_DATETIME_GREATER_THAN_OR_EQUAL,
						Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:44:22")), BooleanValue.FALSE },
				new Object[] { NAME_DATETIME_GREATER_THAN_OR_EQUAL,
						Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-23T23:50:48")), BooleanValue.TRUE },
				new Object[] { NAME_DATETIME_GREATER_THAN_OR_EQUAL,
						Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than
				new Object[] { NAME_DATETIME_LESS_THAN, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:44:22")),
						BooleanValue.TRUE },
				new Object[] { NAME_DATETIME_LESS_THAN, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-23T23:50:48")),
						BooleanValue.FALSE },
				new Object[] { NAME_DATETIME_LESS_THAN, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal
				new Object[] { NAME_DATETIME_LESS_THAN_OR_EQUAL,
						Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:44:22")), BooleanValue.TRUE },
				new Object[] { NAME_DATETIME_LESS_THAN_OR_EQUAL,
						Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-23T23:50:48")), BooleanValue.FALSE },
				new Object[] { NAME_DATETIME_LESS_THAN_OR_EQUAL,
						Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:date-greater-than
				new Object[] { NAME_DATE_GREATER_THAN, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-25")), BooleanValue.FALSE },
				new Object[] { NAME_DATE_GREATER_THAN, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-23")), BooleanValue.TRUE },
				new Object[] { NAME_DATE_GREATER_THAN, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-24")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal
				new Object[] { NAME_DATE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-25")), BooleanValue.FALSE },
				new Object[] { NAME_DATE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-23")), BooleanValue.TRUE },
				new Object[] { NAME_DATE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-24")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:date-less-than
				new Object[] { NAME_DATE_LESS_THAN, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-25")), BooleanValue.TRUE },//
				new Object[] { NAME_DATE_LESS_THAN, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-23")), BooleanValue.FALSE },
				new Object[] { NAME_DATE_LESS_THAN, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-24")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal
				new Object[] { NAME_DATE_LESS_THAN_OR_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-25")), BooleanValue.TRUE },
				new Object[] { NAME_DATE_LESS_THAN_OR_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-23")), BooleanValue.FALSE },
				new Object[] { NAME_DATE_LESS_THAN_OR_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-24")), BooleanValue.TRUE });
	}

}
