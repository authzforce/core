/*
 * Copyright 2012-2022 THALES.
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
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class StringFunctionsTest extends StandardFunctionTest
{
	public StringFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
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
	public static Collection<Object[]> params()
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:2.0:function:string-concatenate
				new Object[] { NAME_STRING_CONCATENATE, Arrays.asList(new StringValue("foo"), new StringValue("bar")), new StringValue("foobar") },
				new Object[] { NAME_STRING_CONCATENATE, Arrays.asList(new StringValue("foo"), new StringValue(""), new StringValue("bar")), new StringValue("foobar") },

				// urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
				new Object[] { NAME_BOOLEAN_FROM_STRING, Collections.singletonList(new StringValue("true")), BooleanValue.TRUE },
				new Object[] { NAME_BOOLEAN_FROM_STRING, Collections.singletonList(new StringValue("false")), BooleanValue.FALSE },
				new Object[] { NAME_BOOLEAN_FROM_STRING, Collections.singletonList(new StringValue("error")), null },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
				new Object[] { NAME_STRING_FROM_BOOLEAN, Collections.singletonList(BooleanValue.FALSE), new StringValue("false") },
				new Object[] { NAME_STRING_FROM_BOOLEAN, Collections.singletonList(BooleanValue.TRUE), new StringValue("true") },

				// urn:oasis:names:tc:xacml:3.0:function:integer-from-string
				new Object[] { NAME_INTEGER_FROM_STRING, Collections.singletonList(new StringValue("5")), IntegerValue.valueOf(5) },
				new Object[] { NAME_INTEGER_FROM_STRING, Collections.singletonList(new StringValue("-5")), IntegerValue.valueOf(-5) },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-integer
				new Object[] { NAME_STRING_FROM_INTEGER, Collections.singletonList(IntegerValue.valueOf(5)), new StringValue("5") },
				new Object[] { NAME_STRING_FROM_INTEGER, Collections.singletonList(IntegerValue.valueOf(-5)), new StringValue("-5") },

				// urn:oasis:names:tc:xacml:3.0:function:double-from-string
				new Object[] { NAME_DOUBLE_FROM_STRING, Collections.singletonList(new StringValue("5.2")), new DoubleValue("5.2") },
				new Object[] { NAME_DOUBLE_FROM_STRING, Collections.singletonList(new StringValue("-5.2")), new DoubleValue("-5.2") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-double
				new Object[] { NAME_STRING_FROM_DOUBLE, Collections.singletonList(new DoubleValue("5.2")), new StringValue("5.2") },
				new Object[] { NAME_STRING_FROM_DOUBLE, Collections.singletonList(new DoubleValue("-5.2")), new StringValue("-5.2") },

				// urn:oasis:names:tc:xacml:3.0:function:time-from-string
				new Object[] { NAME_TIME_FROM_STRING, Collections.singletonList(new StringValue("09:30:15")), new TimeValue("09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-time
				new Object[] { NAME_STRING_FROM_TIME, Collections.singletonList(new TimeValue("09:30:15")), new StringValue("09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:date-from-string
				new Object[] { NAME_DATE_FROM_STRING, Collections.singletonList(new StringValue("2002-09-24")), new DateValue("2002-09-24") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-date
				new Object[] { NAME_STRING_FROM_DATE, Collections.singletonList(new DateValue("2002-09-24")), new StringValue("2002-09-24") },

				// urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
				new Object[] { NAME_DATETIME_FROM_STRING, Collections.singletonList(new StringValue("2002-09-24T09:30:15")), new DateTimeValue("2002-09-24T09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
				new Object[] { NAME_STRING_FROM_DATETIME, Collections.singletonList(new DateTimeValue("2002-09-24T09:30:15")), new StringValue("2002-09-24T09:30:15") },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
				new Object[] { NAME_ANYURI_FROM_STRING, Collections.singletonList(new StringValue("http://www.example.com")), new AnyUriValue("http://www.example.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
				new Object[] { NAME_STRING_FROM_ANYURI, Collections.singletonList(new AnyUriValue("http://www.example.com")), new StringValue("http://www.example.com") },

				// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
				new Object[] { NAME_DAYTIMEDURATION_FROM_STRING, Collections.singletonList(new StringValue("P1DT2H")), new DayTimeDurationValue("P1DT2H") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
				new Object[] { NAME_STRING_FROM_DAYTIMEDURATION, Collections.singletonList(new DayTimeDurationValue("P1DT2H")), new StringValue("P1DT2H") },

				// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
				new Object[] { NAME_YEARMONTHDURATION_FROM_STRING, Collections.singletonList(new StringValue("P1Y2M")), new YearMonthDurationValue("P1Y2M") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
				new Object[] { NAME_STRING_FROM_YEARMONTHDURATION, Collections.singletonList(new YearMonthDurationValue("P1Y2M")), new StringValue("P1Y2M") },

				// urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
				new Object[] { NAME_X500NAME_FROM_STRING, Collections.singletonList(new StringValue("cn=John Smith, o=Medico Corp, c=US")), new X500NameValue("cn=John Smith, o=Medico Corp, c=US") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
				new Object[] { NAME_STRING_FROM_X500NAME, Collections.singletonList(new X500NameValue("cn=John Smith, o=Medico Corp, c=US")), new StringValue("cn=John Smith, o=Medico Corp, c=US") },

				// urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
				new Object[] { NAME_RFC822NAME_FROM_STRING, Collections.singletonList(new StringValue("Anderson@sun.com")), new Rfc822NameValue("Anderson@sun.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
				new Object[] { NAME_STRING_FROM_RFC822NAME, Collections.singletonList(new Rfc822NameValue("Anderson@sun.com")), new StringValue("Anderson@sun.com") },

				// urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
				new Object[] { NAME_IPADDRESS_FROM_STRING, Collections.singletonList(new StringValue("192.168.1.10/255.255.255.0:8080")), IpAddressValue.valueOf("192.168.1.10/255.255.255.0:8080") },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
				new Object[] { NAME_STRING_FROM_IPADDRESS, Collections.singletonList(IpAddressValue.valueOf("192.168.1.10/255.255.255.0:8080")), new StringValue("192.168.1.10/255.255.255.0:8080") },

				// urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com")), new DnsNameWithPortRangeValue("example.com") },
				// with a wildcard in the left-most
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("*.example.com")), new DnsNameWithPortRangeValue("*.example.com") },
				// wildcard at the end or in the middle (WRONG)
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.*")), null },
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("www.*.com")), null },
				// with ':' but missing port/port range
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:")), null },
				// with port number
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:123")), new DnsNameWithPortRangeValue("example.com:123") },
				// with bounded port range
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:123-456")), new DnsNameWithPortRangeValue("example.com:123-456") },
				// with unbounded port range
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:123-")), new DnsNameWithPortRangeValue("example.com:123-") },
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:-456")), new DnsNameWithPortRangeValue("example.com:-456") },
				// with invalid port ranges
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:-456-")), null },
				new Object[] { NAME_DNSNAME_FROM_STRING, Collections.singletonList(new StringValue("example.com:123--456")), null },

				// urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
				new Object[] { NAME_STRING_FROM_DNSNAME, Collections.singletonList(new DnsNameWithPortRangeValue("example.com")), new StringValue("example.com") },

				// urn:oasis:names:tc:xacml:3.0:function:string-starts-with
				new Object[] { NAME_STRING_STARTS_WITH, Arrays.asList(new StringValue("First"), new StringValue("First test")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_STARTS_WITH, Arrays.asList(new StringValue("test"), new StringValue("First test")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
				new Object[] { NAME_ANYURI_STARTS_WITH, Arrays.asList(new StringValue("http"), new AnyUriValue("http://www.example.com")), BooleanValue.TRUE }, new Object[] { NAME_ANYURI_STARTS_WITH,
						Arrays.asList(new StringValue(".com"), new AnyUriValue("http://www.example.com")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:string-ends-with
				new Object[] { NAME_STRING_ENDS_WITH, Arrays.asList(new StringValue("First"), new StringValue("First test")), BooleanValue.FALSE },
				new Object[] { NAME_STRING_ENDS_WITH, Arrays.asList(new StringValue("test"), new StringValue("First test")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
				new Object[] { NAME_ANYURI_ENDS_WITH, Arrays.asList(new StringValue("http"), new AnyUriValue("http://www.example.com")), BooleanValue.FALSE }, new Object[] { NAME_ANYURI_ENDS_WITH,
						Arrays.asList(new StringValue(".com"), new AnyUriValue("http://www.example.com")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:3.0:function:string-contains
				new Object[] { NAME_STRING_CONTAINS, Arrays.asList(new StringValue("test"), new StringValue("First test")), BooleanValue.TRUE },//
				new Object[] { NAME_STRING_CONTAINS, Arrays.asList(new StringValue("Error"), new StringValue("First test")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
				new Object[] { NAME_ANYURI_CONTAINS, Arrays.asList(new StringValue("example.com"), new AnyUriValue("http://www.example.com")), BooleanValue.TRUE }, new Object[] {
						NAME_ANYURI_CONTAINS, Arrays.asList(new StringValue("acme.com"), new AnyUriValue("http://www.example.com")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:string-substring
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringValue("First test"), IntegerValue.valueOf(0), IntegerValue.valueOf(5)), new StringValue("First") },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringValue("First test"), IntegerValue.valueOf(6), IntegerValue.valueOf(-1)), new StringValue("test") },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringValue("First test"), IntegerValue.valueOf(6), IntegerValue.valueOf(106)), null },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringValue("First test"), IntegerValue.valueOf(106), IntegerValue.valueOf(-1)), null },//
				new Object[] { NAME_STRING_SUBSTRING, Arrays.asList(new StringValue("First test"), IntegerValue.valueOf(-1), IntegerValue.valueOf(-1)), null },

				// urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyUriValue("http://www.example.com"), IntegerValue.valueOf(0), IntegerValue.valueOf(7)), new StringValue("http://") },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyUriValue("http://www.example.com"), IntegerValue.valueOf(11), IntegerValue.valueOf(-1)), new StringValue("example.com") },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyUriValue("http://www.example.com"), IntegerValue.valueOf(11), IntegerValue.valueOf(106)), null },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyUriValue("http://www.example.com"), IntegerValue.valueOf(-1), IntegerValue.valueOf(7)), null },//
				new Object[] { NAME_ANYURI_SUBSTRING, Arrays.asList(new AnyUriValue("http://www.example.com"), IntegerValue.valueOf(-1), IntegerValue.valueOf(-1)), null });
	}

}
