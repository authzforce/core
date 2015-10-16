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

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class StringFunctionsTest extends GeneralFunctionTest
{
	public StringFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?, ?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_STRING_CONCATENATE = "urn:oasis:names:tc:xacml:2.0:function:string-concatenate";
	private static final String NAME_BOOLEAN_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:boolean-from-string";
	private static final String NAME_STRING_FROM_BOOLEAN = "urn:oasis:names:tc:xacml:3.0:function:string-from-boolean";
	private static final String NAME_INTEGER_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:integer-from-string";
	private static final String NAME_STRING_FROM_INTEGER = "urn:oasis:names:tc:xacml:3.0:function:string-from-integer";
	private static final String NAME_DOUBLE_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:double-from-string";
	private static final String NAME_STRING_FROM_DOUBLE = "urn:oasis:names:tc:xacml:3.0:function:string-from-double";
	private static final String NAME_TIME_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:time-from-string";
	private static final String NAME_STRING_FROM_TIME = "urn:oasis:names:tc:xacml:3.0:function:string-from-time";
	private static final String NAME_DATE_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:date-from-string";
	private static final String NAME_STRING_FROM_DATE = "urn:oasis:names:tc:xacml:3.0:function:string-from-date";
	private static final String NAME_DATETIME_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string";
	private static final String NAME_STRING_FROM_DATETIME = "urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime";
	private static final String NAME_ANYURI_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string";
	private static final String NAME_STRING_FROM_ANYURI = "urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI";
	private static final String NAME_DAYTIMEDURATION_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string";
	private static final String NAME_STRING_FROM_DAYTIMEDURATION = "urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration";
	private static final String NAME_YEARMONTHDURATION_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string";
	private static final String NAME_STRING_FROM_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration";
	private static final String NAME_X500NAME_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string";
	private static final String NAME_STRING_FROM_X500NAME = "urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name";
	private static final String NAME_RFC822NAME_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string";
	private static final String NAME_STRING_FROM_RFC822NAME = "urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name";
	private static final String NAME_IPADDRESS_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string";
	private static final String NAME_STRING_FROM_IPADDRESS = "urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress";
	private static final String NAME_DNSNAME_FROM_STRING = "urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string";
	private static final String NAME_STRING_FROM_DNSNAME = "urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName";
	private static final String NAME_STRING_STARTS_WITH = "urn:oasis:names:tc:xacml:3.0:function:string-starts-with";
	private static final String NAME_ANYURI_STARTS_WITH = "urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with";
	private static final String NAME_STRING_ENDS_WITH = "urn:oasis:names:tc:xacml:3.0:function:string-ends-with";
	private static final String NAME_ANYURI_ENDS_WITH = "urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with";
	private static final String NAME_STRING_CONTAINS = "urn:oasis:names:tc:xacml:3.0:function:string-contains";
	private static final String NAME_ANYURI_CONTAINS = "urn:oasis:names:tc:xacml:3.0:function:anyURI-contains";
	private static final String NAME_STRING_SUBSTRING = "urn:oasis:names:tc:xacml:3.0:function:string-substring";
	private static final String NAME_ANYURI_SUBSTRING = "urn:oasis:names:tc:xacml:3.0:function:anyURI-substring";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:2.0:function:string-concatenate
				new Object[] { NAME_STRING_CONCATENATE, Arrays.asList(new StringAttributeValue("foo"), new StringAttributeValue("bar")), new StringAttributeValue("foobar") },
				new Object[] { NAME_STRING_CONCATENATE, Arrays.asList(new StringAttributeValue("foo"), new StringAttributeValue(""), new StringAttributeValue("bar")), new StringAttributeValue("foobar") },

				// urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
				new Object[] { NAME_BOOLEAN_FROM_STRING, Arrays.asList(new StringAttributeValue("true")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_BOOLEAN_FROM_STRING, Arrays.asList(new StringAttributeValue("false")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_BOOLEAN_FROM_STRING, Arrays.asList(new StringAttributeValue("error")), null },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
				new Object[] { NAME_STRING_FROM_BOOLEAN, Arrays.asList(BooleanAttributeValue.FALSE), new StringAttributeValue("false") },
				new Object[] { NAME_STRING_FROM_BOOLEAN, Arrays.asList(BooleanAttributeValue.TRUE), new StringAttributeValue("true") },

				// urn:oasis:names:tc:xacml:3.0:function:integer-from-string
				new Object[] { NAME_INTEGER_FROM_STRING, Arrays.asList(new StringAttributeValue("5")), new IntegerAttributeValue("5") },
				new Object[] { NAME_INTEGER_FROM_STRING, Arrays.asList(new StringAttributeValue("-5")), new IntegerAttributeValue("-5") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-integer
				new Object[] { NAME_STRING_FROM_INTEGER, Arrays.asList(new IntegerAttributeValue("5")), new StringAttributeValue("5") },
				new Object[] { NAME_STRING_FROM_INTEGER, Arrays.asList(new IntegerAttributeValue("-5")), new StringAttributeValue("-5") },

				// urn:oasis:names:tc:xacml:3.0:function:double-from-string
				new Object[] { NAME_DOUBLE_FROM_STRING, Arrays.asList(new StringAttributeValue("5.2")), new DoubleAttributeValue("5.2") },
				new Object[] { NAME_DOUBLE_FROM_STRING, Arrays.asList(new StringAttributeValue("-5.2")), new DoubleAttributeValue("-5.2") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-double
				new Object[] { NAME_STRING_FROM_DOUBLE, Arrays.asList(new DoubleAttributeValue("5.2")), new StringAttributeValue("5.2") },
				new Object[] { NAME_STRING_FROM_DOUBLE, Arrays.asList(new DoubleAttributeValue("-5.2")), new StringAttributeValue("-5.2") },

				// urn:oasis:names:tc:xacml:3.0:function:time-from-string
				new Object[] { NAME_TIME_FROM_STRING, Arrays.asList(new StringAttributeValue("09:30:15")), new TimeAttributeValue("09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-time
				new Object[] { NAME_STRING_FROM_TIME, Arrays.asList(new TimeAttributeValue("09:30:15")), new StringAttributeValue("09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:date-from-string
				new Object[] { NAME_DATE_FROM_STRING, Arrays.asList(new StringAttributeValue("2002-09-24")), new DateAttributeValue("2002-09-24") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-date
				new Object[] { NAME_STRING_FROM_DATE, Arrays.asList(new DateAttributeValue("2002-09-24")), new StringAttributeValue("2002-09-24") },

				// urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
				new Object[] { NAME_DATETIME_FROM_STRING, Arrays.asList(new StringAttributeValue("2002-09-24T09:30:15")), new DateTimeAttributeValue("2002-09-24T09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
				new Object[] { NAME_STRING_FROM_DATETIME, Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15")), new StringAttributeValue("2002-09-24T09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
				new Object[] { NAME_ANYURI_FROM_STRING, Arrays.asList(new StringAttributeValue("http://www.example.com")), new AnyURIAttributeValue("http://www.example.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
				new Object[] { NAME_STRING_FROM_ANYURI, Arrays.asList(new AnyURIAttributeValue("http://www.example.com")), new StringAttributeValue("http://www.example.com") },

				// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
				new Object[] { NAME_DAYTIMEDURATION_FROM_STRING, Arrays.asList(new StringAttributeValue("P1DT2H")), new DayTimeDurationAttributeValue("P1DT2H") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
				new Object[] { NAME_STRING_FROM_DAYTIMEDURATION, Arrays.asList(new DayTimeDurationAttributeValue("P1DT2H")), new StringAttributeValue("P1DT2H") },

				// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
				new Object[] { NAME_YEARMONTHDURATION_FROM_STRING, Arrays.asList(new StringAttributeValue("P1Y2M")), new YearMonthDurationAttributeValue("P1Y2M") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
				new Object[] { NAME_STRING_FROM_YEARMONTHDURATION, Arrays.asList(new YearMonthDurationAttributeValue("P1Y2M")), new StringAttributeValue("P1Y2M") },

				// urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
				new Object[] { NAME_X500NAME_FROM_STRING, Arrays.asList(new StringAttributeValue("cn=John Smith, o=Medico Corp, c=US")), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
				new Object[] { NAME_STRING_FROM_X500NAME, Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), new StringAttributeValue("cn=John Smith, o=Medico Corp, c=US") },

				// urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
				new Object[] { NAME_RFC822NAME_FROM_STRING, Arrays.asList(new StringAttributeValue("Anderson@sun.com")), new RFC822NameAttributeValue("Anderson@sun.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
				new Object[] { NAME_STRING_FROM_RFC822NAME, Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com")), new StringAttributeValue("Anderson@sun.com") },

				// urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
				new Object[] { NAME_IPADDRESS_FROM_STRING, Arrays.asList(new StringAttributeValue("192.168.1.10/255.255.255.0:8080")), new IPAddressAttributeValue("192.168.1.10/255.255.255.0:8080") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
				new Object[] { NAME_STRING_FROM_IPADDRESS, Arrays.asList(new IPAddressAttributeValue("192.168.1.10/255.255.255.0:8080")), new StringAttributeValue("192.168.1.10/255.255.255.0:8080") },

				// urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
				new Object[] { NAME_DNSNAME_FROM_STRING, Arrays.asList(new StringAttributeValue("thalesgroup.com")), new DNSNameAttributeValue("thalesgroup.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
				new Object[] { NAME_STRING_FROM_DNSNAME, Arrays.asList(new DNSNameAttributeValue("thalesgroup.com")), new StringAttributeValue("thalesgroup.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-starts-with
				new Object[] { NAME_STRING_STARTS_WITH, Arrays.asList(new StringAttributeValue("First"), new StringAttributeValue("First test")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_STARTS_WITH, Arrays.asList(new StringAttributeValue("test"), new StringAttributeValue("First test")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
				new Object[] { NAME_ANYURI_STARTS_WITH, Arrays.asList(new StringAttributeValue("http"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_ANYURI_STARTS_WITH, Arrays.asList(new StringAttributeValue(".com"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:string-ends-with
				new Object[] { NAME_STRING_ENDS_WITH, Arrays.asList(new StringAttributeValue("First"), new StringAttributeValue("First test")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_STRING_ENDS_WITH, Arrays.asList(new StringAttributeValue("test"), new StringAttributeValue("First test")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
				new Object[] { NAME_ANYURI_ENDS_WITH, Arrays.asList(new StringAttributeValue("http"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_ANYURI_ENDS_WITH, Arrays.asList(new StringAttributeValue(".com"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:3.0:function:string-contains
				new Object[] { NAME_STRING_CONTAINS, Arrays.asList(new StringAttributeValue("test"), new StringAttributeValue("First test")), BooleanAttributeValue.TRUE },//
				new Object[] { NAME_STRING_CONTAINS, Arrays.asList(new StringAttributeValue("Error"), new StringAttributeValue("First test")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
				new Object[] { NAME_ANYURI_CONTAINS, Arrays.asList(new StringAttributeValue("example.com"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_ANYURI_CONTAINS, Arrays.asList(new StringAttributeValue("thalesgroup.com"), new AnyURIAttributeValue("http://www.example.com")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:string-substring
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringAttributeValue("First test"), new IntegerAttributeValue("0"), new IntegerAttributeValue("5")), new StringAttributeValue("First") },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringAttributeValue("First test"), new IntegerAttributeValue("6"), new IntegerAttributeValue("-1")), new StringAttributeValue("test") },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringAttributeValue("First test"), new IntegerAttributeValue("6"), new IntegerAttributeValue("106")), null },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringAttributeValue("First test"), new IntegerAttributeValue("106"), new IntegerAttributeValue("-1")), null },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringAttributeValue("First test"), new IntegerAttributeValue("-1"), new IntegerAttributeValue("-1")), null },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new IntegerAttributeValue("0"), new IntegerAttributeValue("7")), new StringAttributeValue("http://") },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new IntegerAttributeValue("11"), new IntegerAttributeValue("-1")), new StringAttributeValue("example.com") },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new IntegerAttributeValue("11"), new IntegerAttributeValue("106")), null },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new IntegerAttributeValue("-1"), new IntegerAttributeValue("7")), null },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new IntegerAttributeValue("-1"), new IntegerAttributeValue("-1")), null });
	}

}
