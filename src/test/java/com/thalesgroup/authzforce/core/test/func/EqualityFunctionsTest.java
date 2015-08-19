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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.Base64BinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.HexBinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

@RunWith(Parameterized.class)
public class EqualityFunctionsTest extends GeneralFunctionTest
{

	private static final String NAME_STRING_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	private static final String NAME_STRING_EQUAL_IGNORE_CASE = "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case";
	private static final String NAME_BOOLEAN_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:boolean-equal";
	private static final String NAME_INTEGER_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
	private static final String NAME_DOUBLE_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-equal";
	private static final String NAME_DATE_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:date-equal";
	private static final String NAME_TIME_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:time-equal";
	private static final String NAME_DATETIME_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:dateTime-equal";
	private static final String NAME_DAYTIME_DURATION_EQUAL = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal";
	private static final String NAME_YEARMONTH_DURATION_EQUAL = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal";
	private static final String NAME_ANYURI_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:anyURI-equal";
	private static final String NAME_X500NAME_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:x500Name-equal";
	private static final String NAME_RFC822NAME_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal";
	private static final String NAME_HEXBINARY_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal";
	private static final String NAME_BASE64BINARY_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal";
	private static final Logger LOGGER = LoggerFactory.getLogger(EqualityFunctionsTest.class);

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-equal
				new Object[] { NAME_STRING_EQUAL, Arrays.asList(new StringAttributeValue("Test"), new StringAttributeValue("Test")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_EQUAL, Arrays.asList(new StringAttributeValue("Test"), new StringAttributeValue("Toast")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_EQUAL, Arrays.asList(new StringAttributeValue("Test"), new StringAttributeValue("TEST")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case
				new Object[] { NAME_STRING_EQUAL_IGNORE_CASE, Arrays.asList(new StringAttributeValue("Test"), new StringAttributeValue("Test")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_EQUAL_IGNORE_CASE, Arrays.asList(new StringAttributeValue("Test"), new StringAttributeValue("Toast")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_EQUAL_IGNORE_CASE, Arrays.asList(new StringAttributeValue("Test"), new StringAttributeValue("TEST")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:boolean-equal
				new Object[] { NAME_BOOLEAN_EQUAL, Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.TRUE },
				new Object[] { NAME_BOOLEAN_EQUAL, Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-equal
				new Object[] { NAME_INTEGER_EQUAL, Arrays.asList(new IntegerAttributeValue("42"), new IntegerAttributeValue("42")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_INTEGER_EQUAL, Arrays.asList(new IntegerAttributeValue("42"), new IntegerAttributeValue("24")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:double-equal
				new Object[] { NAME_DOUBLE_EQUAL, Arrays.asList(new DoubleAttributeValue("42.543"), new DoubleAttributeValue("42.543")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DOUBLE_EQUAL, Arrays.asList(new DoubleAttributeValue("42.543"), new DoubleAttributeValue("24.2")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:date-equal
				new Object[] { NAME_DATE_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATE_EQUAL, Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-04-29")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:time-equal
				new Object[] { NAME_TIME_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_TIME_EQUAL, Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:19")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
				new Object[] { NAME_DATETIME_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DATETIME_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-04-29T09:30:15")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DATETIME_EQUAL, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:19")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
				new Object[] { NAME_DAYTIME_DURATION_EQUAL, Arrays.asList(new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DAYTIME_DURATION_EQUAL, Arrays.asList(new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT3H")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DAYTIME_DURATION_EQUAL, Arrays.asList(new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("PT26H")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
				new Object[] { NAME_YEARMONTH_DURATION_EQUAL, Arrays.asList(new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_YEARMONTH_DURATION_EQUAL, Arrays.asList(new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y3M")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_YEARMONTH_DURATION_EQUAL, Arrays.asList(new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P14M")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
				new Object[] { NAME_ANYURI_EQUAL, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_ANYURI_EQUAL, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("https://www.example.com")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
				new Object[] { NAME_X500NAME_EQUAL, Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_X500NAME_EQUAL, Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@SUN.COM")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("ANDERSON@SUN.COM")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
				new Object[] { NAME_HEXBINARY_EQUAL, Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7")), BooleanAttributeValue.TRUE },//
				new Object[] { NAME_HEXBINARY_EQUAL, Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
				new Object[] { NAME_BASE64BINARY_EQUAL, Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_BASE64BINARY_EQUAL, Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), BooleanAttributeValue.FALSE });
	}

	public EqualityFunctionsTest(String functionName, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs, ExpressionResult<? extends AttributeValue> expectedResult)
	{
		super(functionName, inputs, expectedResult);
		LOGGER.debug(this + ": " + functionName + "(" + inputs + ")");
	}

}
