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

import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class NonNumericComparisonFunctionsTest extends GeneralFunctionTest
{

	public NonNumericComparisonFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?, ?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
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
				new Object[] { NAME_STRING_GREATER_THAN, Arrays.asList(new StringAttributeValue("First"), new StringAttributeValue("Second")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_GREATER_THAN, Arrays.asList(new StringAttributeValue("Third"), new StringAttributeValue("Fourth")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_GREATER_THAN, Arrays.asList(new StringAttributeValue("Fifth"), new StringAttributeValue("Fifth")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal
				new Object[] { NAME_STRING_GREATER_THAN_OR_EQUAL, Arrays.asList(new StringAttributeValue("First"), new StringAttributeValue("Second")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_GREATER_THAN_OR_EQUAL, Arrays.asList(new StringAttributeValue("Third"), new StringAttributeValue("Fourth")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_GREATER_THAN_OR_EQUAL, Arrays.asList(new StringAttributeValue("Fifth"), new StringAttributeValue("Fifth")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:string-less-than
				new Object[] { NAME_STRING_LESS_THAN, Arrays.asList(new StringAttributeValue("First"), new StringAttributeValue("Second")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_LESS_THAN, Arrays.asList(new StringAttributeValue("Third"), new StringAttributeValue("Fourth")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_LESS_THAN, Arrays.asList(new StringAttributeValue("Fifth"), new StringAttributeValue("Fifth")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal
				new Object[] { NAME_STRING_LESS_THAN_OR_EQUAL, Arrays.asList(new StringAttributeValue("First"), new StringAttributeValue("Second")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_LESS_THAN_OR_EQUAL, Arrays.asList(new StringAttributeValue("Third"), new StringAttributeValue("Fourth")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_LESS_THAN_OR_EQUAL, Arrays.asList(new StringAttributeValue("Fifth"), new StringAttributeValue("Fifth")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:time-greater-than
				new Object[] { NAME_TIME_GREATER_THAN, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:44:22")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_GREATER_THAN, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("08:50:48")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_GREATER_THAN, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal
				new Object[] { NAME_TIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:44:22")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("08:50:48")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:time-less-than
				new Object[] { NAME_TIME_LESS_THAN, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:44:22")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_LESS_THAN, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("08:50:48")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_LESS_THAN, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal
				new Object[] { NAME_TIME_LESS_THAN_OR_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:44:22")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_LESS_THAN_OR_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("08:50:48")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_LESS_THAN_OR_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:2.0:function:time-in-range
				/*
				 * Time interval lower bound and upper bound are the same (lower bound = upper
				 * bound)
				 */
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:30:00")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:30:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:28:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:30:00")), BooleanAttributeValue.FALSE },
				/*
				 * Time interval lower bound and upper bound on the same day (lower bound < upper
				 * bound)
				 */
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:28:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:45:00")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:45:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:45:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:45:00"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:45:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:47:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:45:00")), BooleanAttributeValue.FALSE },
				/*
				 * Time interval upper bound on the day after (lower bound > upper bound upper
				 * bound)
				 */
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:28:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("02:45:00")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:30:00"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("02:45:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("02:45:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("01:30:15"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("02:45:00")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_IN_RANGE, Arrays.asList(new TimeAttributeValue("02:45:00"), new TimeAttributeValue("09:30:00"), new TimeAttributeValue("02:45:00")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than
				new Object[] { NAME_DATETIME_GREATER_THAN, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:44:22")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATETIME_GREATER_THAN, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-23T23:50:48")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATETIME_GREATER_THAN, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal
				new Object[] { NAME_DATETIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:44:22")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATETIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-23T23:50:48")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATETIME_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than
				new Object[] { NAME_DATETIME_LESS_THAN, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:44:22")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATETIME_LESS_THAN, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-23T23:50:48")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATETIME_LESS_THAN, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal
				new Object[] { NAME_DATETIME_LESS_THAN_OR_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:44:22")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATETIME_LESS_THAN_OR_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-23T23:50:48")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATETIME_LESS_THAN_OR_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:date-greater-than
				new Object[] { NAME_DATE_GREATER_THAN, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-25")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATE_GREATER_THAN, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-23")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATE_GREATER_THAN, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal
				new Object[] { NAME_DATE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-25")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-23")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:date-less-than
				new Object[] { NAME_DATE_LESS_THAN, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-25")), BooleanAttributeValue.TRUE },//
				new Object[] { NAME_DATE_LESS_THAN, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-23")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATE_LESS_THAN, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal
				new Object[] { NAME_DATE_LESS_THAN_OR_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-25")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATE_LESS_THAN_OR_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-23")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATE_LESS_THAN_OR_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), BooleanAttributeValue.TRUE });
	}

}
