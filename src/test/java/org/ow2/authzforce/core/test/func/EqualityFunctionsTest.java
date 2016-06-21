/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
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
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class EqualityFunctionsTest extends FunctionTest
{

	public EqualityFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

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

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-equal
				new Object[] { NAME_STRING_EQUAL, Arrays.asList(new StringValue("Test"), new StringValue("Test")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_EQUAL, Arrays.asList(new StringValue("Test"), new StringValue("Toast")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_EQUAL, Arrays.asList(new StringValue("Test"), new StringValue("TEST")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case
				new Object[] { NAME_STRING_EQUAL_IGNORE_CASE, Arrays.asList(new StringValue("Test"), new StringValue("Test")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_EQUAL_IGNORE_CASE, Arrays.asList(new StringValue("Test"), new StringValue("Toast")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_EQUAL_IGNORE_CASE, Arrays.asList(new StringValue("Test"), new StringValue("TEST")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:boolean-equal
				new Object[] { NAME_BOOLEAN_EQUAL, Arrays.asList(BooleanValue.FALSE, BooleanValue.FALSE), BooleanValue.TRUE },
				new Object[] { NAME_BOOLEAN_EQUAL, Arrays.asList(BooleanValue.FALSE, BooleanValue.TRUE), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-equal
				new Object[] { NAME_INTEGER_EQUAL, Arrays.asList(new IntegerValue("42"), new IntegerValue("42")), BooleanValue.TRUE },
				new Object[] { NAME_INTEGER_EQUAL, Arrays.asList(new IntegerValue("42"), new IntegerValue("24")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:double-equal
				new Object[] { NAME_DOUBLE_EQUAL, Arrays.asList(new DoubleValue("42.543"), new DoubleValue("42.543")), BooleanValue.TRUE },
				new Object[] { NAME_DOUBLE_EQUAL, Arrays.asList(new DoubleValue("42.543"), new DoubleValue("24.2")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:date-equal
				new Object[] { NAME_DATE_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-24")), BooleanValue.TRUE },
				new Object[] { NAME_DATE_EQUAL, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-04-29")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:time-equal
				new Object[] { NAME_TIME_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:15")), BooleanValue.TRUE },
				new Object[] { NAME_TIME_EQUAL, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:19")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
				new Object[] { NAME_DATETIME_EQUAL, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15")),
						BooleanValue.TRUE },
				new Object[] { NAME_DATETIME_EQUAL, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-04-29T09:30:15")),
						BooleanValue.FALSE },
				new Object[] { NAME_DATETIME_EQUAL, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:19")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
				new Object[] { NAME_DAYTIME_DURATION_EQUAL, Arrays.asList(new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H")),
						BooleanValue.TRUE },
				new Object[] { NAME_DAYTIME_DURATION_EQUAL, Arrays.asList(new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT3H")),
						BooleanValue.FALSE },
				new Object[] { NAME_DAYTIME_DURATION_EQUAL, Arrays.asList(new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("PT26H")),
						BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
				new Object[] { NAME_YEARMONTH_DURATION_EQUAL, Arrays.asList(new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M")),
						BooleanValue.TRUE },
				new Object[] { NAME_YEARMONTH_DURATION_EQUAL, Arrays.asList(new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y3M")),
						BooleanValue.FALSE },
				new Object[] { NAME_YEARMONTH_DURATION_EQUAL, Arrays.asList(new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P14M")),
						BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
				new Object[] { NAME_ANYURI_EQUAL, Arrays.asList(new AnyURIValue("http://www.example.com"), new AnyURIValue("http://www.example.com")),
						BooleanValue.TRUE },
				new Object[] { NAME_ANYURI_EQUAL, Arrays.asList(new AnyURIValue("http://www.example.com"), new AnyURIValue("https://www.example.com")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
				new Object[] { NAME_X500NAME_EQUAL,
						Arrays.asList(new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn= John Smith,o =Medico Corp, C=US")),
						BooleanValue.TRUE },
				new Object[] { NAME_X500NAME_EQUAL,
						Arrays.asList(new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US")),
						BooleanValue.FALSE },
				// If RDN contains multiple attributeTypeAndValue pairs
				new Object[] { NAME_X500NAME_EQUAL, Arrays.asList(new X500NameValue("cn=John+o=Medico, c=US"), new X500NameValue("o=Medico+cn=John, c=US")),
						BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com")),
						BooleanValue.TRUE },
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")),
						BooleanValue.FALSE },
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@SUN.COM")),
						BooleanValue.TRUE },
				new Object[] { NAME_RFC822NAME_EQUAL, Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("ANDERSON@SUN.COM")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
				new Object[] { NAME_HEXBINARY_EQUAL, Arrays.asList(new HexBinaryValue("0FB7"), new HexBinaryValue("0FB7")), BooleanValue.TRUE },//
				new Object[] { NAME_HEXBINARY_EQUAL, Arrays.asList(new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
				new Object[] { NAME_BASE64BINARY_EQUAL, Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ==")),
						BooleanValue.TRUE },
				new Object[] { NAME_BASE64BINARY_EQUAL, Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")),
						BooleanValue.FALSE });
	}

}
