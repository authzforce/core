/**
 * 
 */
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DNSNameAttribute;
import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.IPAddressAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.TimeAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.attr.YearMonthDurationAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class StringFunctionsTest extends AbstractFunctionTest {

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
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:2.0:function:string-concatenate
				new Object[] {
						NAME_STRING_CONCATENATE,
						Arrays.asList(StringAttribute.getInstance("foo"),
								StringAttribute.getInstance("bar")),
						Status.STATUS_OK, StringAttribute.getInstance("foobar") },
						new Object[] {
								NAME_STRING_CONCATENATE,
								Arrays.asList(
										StringAttribute.getInstance("foo"),
										StringAttribute.getInstance(""),
										StringAttribute.getInstance("bar")),
								Status.STATUS_OK,
								StringAttribute.getInstance("foobar") },

						// urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
						new Object[] {
								NAME_BOOLEAN_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("true")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("false")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_BOOLEAN_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("error")),
								Status.STATUS_SYNTAX_ERROR, null },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
						new Object[] {
								NAME_STRING_FROM_BOOLEAN,
								Arrays.asList(BooleanAttribute
										.getInstance(false)), Status.STATUS_OK,
								StringAttribute.getInstance("false") },
						new Object[] {
								NAME_STRING_FROM_BOOLEAN,
								Arrays.asList(BooleanAttribute
										.getInstance(true)), Status.STATUS_OK,
								StringAttribute.getInstance("true") },

						// urn:oasis:names:tc:xacml:3.0:function:integer-from-string
						new Object[] {
								NAME_INTEGER_FROM_STRING,
								Arrays.asList(StringAttribute.getInstance("5")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("5") },
						new Object[] {
								NAME_INTEGER_FROM_STRING,
								Arrays.asList(StringAttribute.getInstance("-5")),
								Status.STATUS_OK,
								IntegerAttribute.getInstance("-5") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-integer
						new Object[] {
								NAME_STRING_FROM_INTEGER,
								Arrays.asList(IntegerAttribute.getInstance("5")),
								Status.STATUS_OK,
								StringAttribute.getInstance("5") },
						new Object[] {
								NAME_STRING_FROM_INTEGER,
								Arrays.asList(IntegerAttribute
										.getInstance("-5")), Status.STATUS_OK,
								StringAttribute.getInstance("-5") },

						// urn:oasis:names:tc:xacml:3.0:function:double-from-string
						new Object[] {
								NAME_DOUBLE_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("5.2")), Status.STATUS_OK,
								DoubleAttribute.getInstance("5.2") },
						new Object[] {
								NAME_DOUBLE_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("-5.2")),
								Status.STATUS_OK,
								DoubleAttribute.getInstance("-5.2") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-double
						new Object[] {
								NAME_STRING_FROM_DOUBLE,
								Arrays.asList(DoubleAttribute
										.getInstance("5.2")), Status.STATUS_OK,
								StringAttribute.getInstance("5.2") },
						new Object[] {
								NAME_STRING_FROM_DOUBLE,
								Arrays.asList(DoubleAttribute
										.getInstance("-5.2")),
								Status.STATUS_OK,
								StringAttribute.getInstance("-5.2") },

						// urn:oasis:names:tc:xacml:3.0:function:time-from-string
						new Object[] {
								NAME_TIME_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("09:30:15")),
								Status.STATUS_OK,
								TimeAttribute.getInstance("09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-time
						new Object[] {
								NAME_STRING_FROM_TIME,
								Arrays.asList(TimeAttribute
										.getInstance("09:30:15")),
								Status.STATUS_OK,
								StringAttribute.getInstance("09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:date-from-string
						new Object[] {
								NAME_DATE_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("2002-09-24")),
								Status.STATUS_OK,
								DateAttribute.getInstance("2002-09-24") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-date
						new Object[] {
								NAME_STRING_FROM_DATE,
								Arrays.asList(DateAttribute
										.getInstance("2002-09-24")),
								Status.STATUS_OK,
								StringAttribute.getInstance("2002-09-24") },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
						new Object[] {
								NAME_DATETIME_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("2002-09-24T09:30:15")),
								Status.STATUS_OK,
								DateTimeAttribute
										.getInstance("2002-09-24T09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
						new Object[] {
								NAME_STRING_FROM_DATETIME,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15")),
								Status.STATUS_OK,
								StringAttribute
										.getInstance("2002-09-24T09:30:15") },

						// urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
						new Object[] {
								NAME_ANYURI_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								AnyURIAttribute
										.getInstance("http://www.example.com") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
						new Object[] {
								NAME_STRING_FROM_ANYURI,
								Arrays.asList(AnyURIAttribute
										.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								StringAttribute
										.getInstance("http://www.example.com") },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
						new Object[] {
								NAME_DAYTIMEDURATION_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("P1DT2H")),
								Status.STATUS_OK,
								DayTimeDurationAttribute.getInstance("P1DT2H") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
						new Object[] {
								NAME_STRING_FROM_DAYTIMEDURATION,
								Arrays.asList(DayTimeDurationAttribute
										.getInstance("P1DT2H")),
								Status.STATUS_OK,
								StringAttribute.getInstance("P1DT2H") },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
						new Object[] {
								NAME_YEARMONTHDURATION_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("P1Y2M")),
								Status.STATUS_OK,
								YearMonthDurationAttribute.getInstance("P1Y2M") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
						new Object[] {
								NAME_STRING_FROM_YEARMONTHDURATION,
								Arrays.asList(YearMonthDurationAttribute
										.getInstance("P1Y2M")),
								Status.STATUS_OK,
								StringAttribute.getInstance("P1Y2M") },

						// urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
						new Object[] {
								NAME_X500NAME_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("cn=John Smith, o=Medico Corp, c=US")),
								Status.STATUS_OK,
								X500NameAttribute
										.getInstance("cn=John Smith, o=Medico Corp, c=US") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
						new Object[] {
								NAME_STRING_FROM_X500NAME,
								Arrays.asList(X500NameAttribute
										.getInstance("cn=John Smith, o=Medico Corp, c=US")),
								Status.STATUS_OK,
								StringAttribute
										.getInstance("cn=John Smith, o=Medico Corp, c=US") },

						// urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
						new Object[] {
								NAME_RFC822NAME_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("Anderson@sun.com")),
								Status.STATUS_OK,
								RFC822NameAttribute
										.getInstance("Anderson@sun.com") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
						new Object[] {
								NAME_STRING_FROM_RFC822NAME,
								Arrays.asList(RFC822NameAttribute
										.getInstance("Anderson@sun.com")),
								Status.STATUS_OK,
								StringAttribute.getInstance("Anderson@sun.com") },

						// urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
						new Object[] {
								NAME_IPADDRESS_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("192.168.1.10/255.255.255.0:8080")),
								Status.STATUS_OK,
								IPAddressAttribute
										.getInstance("192.168.1.10/255.255.255.0:8080") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
						new Object[] {
								NAME_STRING_FROM_IPADDRESS,
								Arrays.asList(IPAddressAttribute
										.getInstance("192.168.1.10/255.255.255.0:8080")),
								Status.STATUS_OK,
								StringAttribute
										.getInstance("192.168.1.10/255.255.255.0:8080") },

						// urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
						new Object[] {
								NAME_DNSNAME_FROM_STRING,
								Arrays.asList(StringAttribute
										.getInstance("thalesgroup.com")),
								Status.STATUS_OK,
								DNSNameAttribute.getInstance("thalesgroup.com") },

						// urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
						new Object[] {
								NAME_STRING_FROM_DNSNAME,
								Arrays.asList(DNSNameAttribute
										.getInstance("thalesgroup.com")),
								Status.STATUS_OK,
								StringAttribute.getInstance("thalesgroup.com") },

						// urn:oasis:names:tc:xacml:3.0:function:string-starts-with
						new Object[] {
								NAME_STRING_STARTS_WITH,
								Arrays.asList(StringAttribute
										.getInstance("First"), StringAttribute
										.getInstance("First test")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_STRING_STARTS_WITH,
								Arrays.asList(StringAttribute
										.getInstance("test"), StringAttribute
										.getInstance("First test")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
						new Object[] {
								NAME_ANYURI_STARTS_WITH,
								Arrays.asList(StringAttribute
										.getInstance("http"), AnyURIAttribute
										.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_ANYURI_STARTS_WITH,
								Arrays.asList(StringAttribute
										.getInstance(".com"), AnyURIAttribute
										.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:string-ends-with
						new Object[] {
								NAME_STRING_ENDS_WITH,
								Arrays.asList(StringAttribute
										.getInstance("First"), StringAttribute
										.getInstance("First test")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_STRING_ENDS_WITH,
								Arrays.asList(StringAttribute
										.getInstance("test"), StringAttribute
										.getInstance("First test")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
						new Object[] {
								NAME_ANYURI_ENDS_WITH,
								Arrays.asList(StringAttribute
										.getInstance("http"), AnyURIAttribute
										.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },
						new Object[] {
								NAME_ANYURI_ENDS_WITH,
								Arrays.asList(StringAttribute
										.getInstance(".com"), AnyURIAttribute
										.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },

						// urn:oasis:names:tc:xacml:3.0:function:string-contains
						new Object[] {
								NAME_STRING_CONTAINS,
								Arrays.asList(StringAttribute
										.getInstance("test"), StringAttribute
										.getInstance("First test")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_STRING_CONTAINS,
								Arrays.asList(StringAttribute
										.getInstance("Error"), StringAttribute
										.getInstance("First test")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
						new Object[] {
								NAME_ANYURI_CONTAINS,
								Arrays.asList(
										StringAttribute
												.getInstance("example.com"),
										AnyURIAttribute
												.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(true) },
						new Object[] {
								NAME_ANYURI_CONTAINS,
								Arrays.asList(
										StringAttribute
												.getInstance("thalesgroup.com"),
										AnyURIAttribute
												.getInstance("http://www.example.com")),
								Status.STATUS_OK,
								BooleanAttribute.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:string-substring
						// TODO: error cases
						new Object[] {
								NAME_STRING_SUBSTRING,
								Arrays.asList(StringAttribute
										.getInstance("First test"),
										IntegerAttribute.getInstance("0"),
										IntegerAttribute.getInstance("5")),
								Status.STATUS_OK,
								StringAttribute.getInstance("First") },
						new Object[] {
								NAME_STRING_SUBSTRING,
								Arrays.asList(StringAttribute
										.getInstance("First test"),
										IntegerAttribute.getInstance("6"),
										IntegerAttribute.getInstance("-1")),
								Status.STATUS_OK,
								StringAttribute.getInstance("test") },

						// urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
						// TODO: error cases
						new Object[] {
								NAME_ANYURI_SUBSTRING,
								Arrays.asList(AnyURIAttribute
										.getInstance("http://www.example.com"),
										IntegerAttribute.getInstance("0"),
										IntegerAttribute.getInstance("7")),
								Status.STATUS_OK,
								StringAttribute.getInstance("http://") },
						new Object[] {
								NAME_ANYURI_SUBSTRING,
								Arrays.asList(AnyURIAttribute
										.getInstance("http://www.example.com"),
										IntegerAttribute.getInstance("11"),
										IntegerAttribute.getInstance("-1")),
								Status.STATUS_OK,
								StringAttribute.getInstance("example.com") });
	}

	public StringFunctionsTest(final String functionName,
			final List<Evaluatable> inputs, final String expectedStatus,
			final AttributeValue expectedValue) {
		super(functionName, inputs, expectedStatus, expectedValue);
	}

}
