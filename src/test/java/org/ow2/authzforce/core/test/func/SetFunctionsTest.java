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
import org.ow2.authzforce.core.pdp.api.Bags;
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.impl.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.impl.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DateValue;
import org.ow2.authzforce.core.pdp.impl.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.impl.value.DoubleValue;
import org.ow2.authzforce.core.pdp.impl.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.impl.value.IntegerValue;
import org.ow2.authzforce.core.pdp.impl.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.value.TimeValue;
import org.ow2.authzforce.core.pdp.impl.value.X500NameValue;
import org.ow2.authzforce.core.pdp.impl.value.YearMonthDurationValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class SetFunctionsTest extends FunctionTest
{

	public SetFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, true, expectedResult);
	}

	private static final String NAME_STRING_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:string-intersection";
	private static final String NAME_BOOLEAN_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:boolean-intersection";
	private static final String NAME_INTEGER_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:integer-intersection";
	private static final String NAME_DOUBLE_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:double-intersection";
	private static final String NAME_TIME_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:time-intersection";
	private static final String NAME_DATE_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:date-intersection";
	private static final String NAME_DATETIME_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection";
	private static final String NAME_ANYURI_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection";
	private static final String NAME_HEXBINARY_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection";
	private static final String NAME_BASE64BINARY_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection";
	private static final String NAME_DAYTIMEDURATION_INTERSECTION = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection";
	private static final String NAME_YEARMONTHDURATION_INTERSECTION = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection";
	private static final String NAME_X500NAME_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection";
	private static final String NAME_RFC822NAME_INTERSECTION = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection";
	private static final String NAME_STRING_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of";
	private static final String NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of";
	private static final String NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of";
	private static final String NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of";
	private static final String NAME_TIME_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of";
	private static final String NAME_DATE_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of";
	private static final String NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of";
	private static final String NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of";
	private static final String NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of";
	private static final String NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of";
	private static final String NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-one-member-of";
	private static final String NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-one-member-of";
	private static final String NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of";
	private static final String NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of";
	private static final String NAME_STRING_UNION = "urn:oasis:names:tc:xacml:1.0:function:string-union";
	private static final String NAME_BOOLEAN_UNION = "urn:oasis:names:tc:xacml:1.0:function:boolean-union";
	private static final String NAME_INTEGER_UNION = "urn:oasis:names:tc:xacml:1.0:function:integer-union";
	private static final String NAME_DOUBLE_UNION = "urn:oasis:names:tc:xacml:1.0:function:double-union";
	private static final String NAME_TIME_UNION = "urn:oasis:names:tc:xacml:1.0:function:time-union";
	private static final String NAME_DATE_UNION = "urn:oasis:names:tc:xacml:1.0:function:date-union";
	private static final String NAME_DATETIME_UNION = "urn:oasis:names:tc:xacml:1.0:function:dateTime-union";
	private static final String NAME_ANYURI_UNION = "urn:oasis:names:tc:xacml:1.0:function:anyURI-union";
	private static final String NAME_HEXBINARY_UNION = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-union";
	private static final String NAME_BASE64BINARY_UNION = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-union";
	private static final String NAME_DAYTIMEDURATION_UNION = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union";
	private static final String NAME_YEARMONTHDURATION_UNION = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union";
	private static final String NAME_X500NAME_UNION = "urn:oasis:names:tc:xacml:1.0:function:x500Name-union";
	private static final String NAME_RFC822NAME_UNION = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union";
	private static final String NAME_STRING_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:string-subset";
	private static final String NAME_BOOLEAN_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:boolean-subset";
	private static final String NAME_INTEGER_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:integer-subset";
	private static final String NAME_DOUBLE_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:double-subset";
	private static final String NAME_TIME_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:time-subset";
	private static final String NAME_DATE_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:date-subset";
	private static final String NAME_DATETIME_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:dateTime-subset";
	private static final String NAME_ANYURI_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:anyURI-subset";
	private static final String NAME_HEXBINARY_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset";
	private static final String NAME_BASE64BINARY_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset";
	private static final String NAME_DAYTIMEDURATION_SUBSET = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset";
	private static final String NAME_YEARMONTHDURATION_SUBSET = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset";
	private static final String NAME_X500NAME_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:x500Name-subset";
	private static final String NAME_RFC822NAME_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset";
	private static final String NAME_STRING_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:string-set-equals";
	private static final String NAME_BOOLEAN_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals";
	private static final String NAME_INTEGER_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:integer-set-equals";
	private static final String NAME_DOUBLE_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:double-set-equals";
	private static final String NAME_TIME_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:time-set-equals";
	private static final String NAME_DATE_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:date-set-equals";
	private static final String NAME_DATETIME_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals";
	private static final String NAME_ANYURI_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals";
	private static final String NAME_HEXBINARY_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals";
	private static final String NAME_BASE64BINARY_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals";
	private static final String NAME_DAYTIMEDURATION_SET_EQUALS = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals";
	private static final String NAME_YEARMONTHDURATION_SET_EQUALS = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals";
	private static final String NAME_X500NAME_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals";
	private static final String NAME_RFC822NAME_SET_EQUALS = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-intersection
				new Object[] {
						NAME_STRING_INTERSECTION,
						Arrays.asList(Bags.getInstance(DatatypeConstants.STRING.TYPE,
								Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String2"), new StringValue("String3"))), //
								Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String2"), new StringValue("String3"),
										new StringValue("String3"), new StringValue("String4")))),//
						Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String2"), new StringValue("String3"))) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-intersection
						new Object[] {
								NAME_BOOLEAN_INTERSECTION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE,
												Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE, BooleanValue.FALSE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE, BooleanValue.FALSE))),//
								Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE)) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-intersection
						new Object[] {
								NAME_INTEGER_INTERSECTION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"), new IntegerValue("3"))),
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("2"), new IntegerValue("3"), new IntegerValue("3"), new IntegerValue("4")))),
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("2"), new IntegerValue("3"))) },

						// urn:oasis:names:tc:xacml:1.0:function:double-intersection
						new Object[] {
								NAME_DOUBLE_INTERSECTION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."), new DoubleValue("3.14"))),
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("2."), new DoubleValue("3.14"), new DoubleValue("3.14"), new DoubleValue("4.")))),//
								Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("2."), new DoubleValue("3.14"))) },

						// urn:oasis:names:tc:xacml:1.0:function:time-intersection
						new Object[] {
								NAME_TIME_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"),
										new TimeValue("09:30:15"), new TimeValue("17:18:19"))), Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(
										new TimeValue("09:30:15"), new TimeValue("17:18:19"), new TimeValue("17:18:19"), new TimeValue("03:56:12")))),
								Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("17:18:19"))) },

						// urn:oasis:names:tc:xacml:1.0:function:date-intersection
						new Object[] {
								NAME_DATE_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue(
										"2002-09-24"), new DateValue("2002-09-24"), new DateValue("2003-10-25"))), //
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2003-10-25"),
												new DateValue("2003-10-25"), new DateValue("1970-01-01")))),//
								Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2003-10-25"))) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection
						new Object[] {
								NAME_DATETIME_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue(
												"2003-10-25T17:18:19"))), //
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"),
												new DateTimeValue("2003-10-25T17:18:19"), new DateTimeValue("2003-10-25T17:18:19"), new DateTimeValue(
														"1970-01-01T03:56:12")))),
								Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
										Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19"))) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection
						new Object[] {
								NAME_ANYURI_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"),
										new AnyURIValue("http://www.example.com/videos/sample.avi"))), Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays
										.asList(new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue(
												"http://www.example.com/videos/sample.avi"), new AnyURIValue("http://www.example.com/videos/sample.avi"),
												new AnyURIValue("https://www.thalesgroup.com")))),
								Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com/images/logo.gif"),
										new AnyURIValue("http://www.example.com/videos/sample.avi"))) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection
						new Object[] {
								NAME_HEXBINARY_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue(
										"0FB7"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))), //
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB7"),
												new HexBinaryValue("0FB8"), new HexBinaryValue("0FB8"), new HexBinaryValue("0FB9")))),//
								Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection
						new Object[] {
								NAME_BASE64BINARY_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))), //
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="),
												new Base64BinaryValue("T3RoZXI="), new Base64BinaryValue("T3RoZXI="), new Base64BinaryValue("VGVzdA==")))),
								Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
										Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection
						new Object[] {
								NAME_DAYTIMEDURATION_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("-P0D"))), //
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT26H"),
												new DayTimeDurationValue("P0D"), new DayTimeDurationValue("P0D"), new DayTimeDurationValue("-PT1M30.5S")))),
								Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
										Arrays.asList(new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P0D"))) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection
						new Object[] {
								NAME_YEARMONTHDURATION_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("-P0Y"))), Bags
										.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P14M"),
												new YearMonthDurationValue("P0Y"), new YearMonthDurationValue("P0Y"), new YearMonthDurationValue("-P60Y")))),
								Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
										Arrays.asList(new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P0Y"))) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection
						new Object[] {
								NAME_X500NAME_INTERSECTION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))), Bags
										.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
												new X500NameValue("cn=John Smith, o=Other Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"),
												new X500NameValue("cn=Mark Anderson, o=Thales, c=FR")))),
								Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Other Corp, c=US"))) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection
						new Object[] {
								NAME_RFC822NAME_INTERSECTION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
												new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue(
														"Smith@sun.com"))), //
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("Anderson@sun.com"),
												new RFC822NameValue("Smith@sun.com"), new RFC822NameValue("Smith@sun.com"), new RFC822NameValue(
														"john.doe@example.com")))),
								Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
										Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com"))) },

						// urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of
						new Object[] {
								NAME_STRING_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue("String2"))),
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String2"), new StringValue("String2")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_STRING_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue("String1"))),
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String2"), new StringValue("String2")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of
						new Object[] { NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE, BooleanValue.FALSE))),
								BooleanValue.TRUE },
						new Object[] { NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.TRUE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE, BooleanValue.FALSE))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of
						new Object[] { NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("1"), new IntegerValue("2"))),//
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("2"), new IntegerValue("2")))),
								BooleanValue.TRUE },
						new Object[] { NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("1"), new IntegerValue("1"))),//
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("2"), new IntegerValue("2")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of
						new Object[] {
								NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("2."), new DoubleValue("3.14"))),
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("3.14"), new DoubleValue("3.14")))),
								BooleanValue.TRUE },
						new Object[] { NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("2."), new DoubleValue("2."))),//
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("3.14"), new DoubleValue("3.14")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of
						new Object[] {
								NAME_TIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("17:18:19"))),
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("17:18:19"), new TimeValue("17:18:19")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_TIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("09:30:15"))),
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("17:18:19"), new TimeValue("17:18:19")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of
						new Object[] {
								NAME_DATE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2003-10-25"))),
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2003-10-25"), new DateValue("2003-10-25")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DATE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2002-09-24"))),
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2003-10-25"), new DateValue("2003-10-25")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of
						new Object[] {
								NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
												Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19"))),
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
												Arrays.asList(new DateTimeValue("2003-10-25T17:18:19"), new DateTimeValue("2003-10-25T17:18:19")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
												Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"))),
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
												Arrays.asList(new DateTimeValue("2003-10-25T17:18:19"), new DateTimeValue("2003-10-25T17:18:19")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of
						new Object[] {
								NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue(
										"http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/videos/sample.avi"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue(
												"http://www.example.com/videos/sample.avi"), new AnyURIValue("http://www.example.com/videos/sample.avi")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue(
										"http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue(
												"http://www.example.com/videos/sample.avi"), new AnyURIValue("http://www.example.com/videos/sample.avi")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of
						new Object[] {
								NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))),
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB8"), new HexBinaryValue("0FB8")))), BooleanValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB7"), new HexBinaryValue("0FB7"))),
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB8"), new HexBinaryValue("0FB8")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-o
						new Object[] {
								NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))),//
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("T3RoZXI="), new Base64BinaryValue("T3RoZXI=")))), BooleanValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="))),
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("T3RoZXI="), new Base64BinaryValue("T3RoZXI=")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-one-member-of
						new Object[] {
								NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("-P0D"))),
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("PT26H")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("-P0D"), new DayTimeDurationValue("-P0D"))),//
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("PT26H")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-one-member-of
						new Object[] {
								NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("-P0Y"))),
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("P14M"), new YearMonthDurationValue("P14M")))), BooleanValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("-P0Y"), new YearMonthDurationValue("-P0Y"))),
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("P14M"), new YearMonthDurationValue("P14M")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of
						new Object[] {
								NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue(
										"cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Other Corp, c=US"),
												new X500NameValue("cn=John Smith, o=Other Corp, c=US")))), BooleanValue.TRUE },
						new Object[] {
								NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue(
										"cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Other Corp, c=US"),
												new X500NameValue("cn=John Smith, o=Other Corp, c=US")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of
						new Object[] {
								NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com"))),
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("Smith@sun.com"), new RFC822NameValue("Smith@sun.com")))), BooleanValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"))),//
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("Smith@sun.com"), new RFC822NameValue("Smith@sun.com")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:string-union
						new Object[] {
								NAME_STRING_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String2"))),
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String2"), new StringValue("String3")))),
								Bags.getInstance(DatatypeConstants.STRING.TYPE,
										Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String3"))) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-union
						new Object[] {
								NAME_BOOLEAN_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE,
												Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE, BooleanValue.FALSE)),
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE, BooleanValue.FALSE))),
								Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE)) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-union
						new Object[] {
								NAME_INTEGER_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"))),//
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("2"), new IntegerValue("3")))),
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3"))) },

						// urn:oasis:names:tc:xacml:1.0:function:double-union
						new Object[] {
								NAME_DOUBLE_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."))),//
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("2."), new DoubleValue("3.14")))),
								Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
										Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("3.14"))) },

						// urn:oasis:names:tc:xacml:1.0:function:time-union
						new Object[] {
								NAME_TIME_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("09:30:15"))),//
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("09:30:15"), new TimeValue("17:18:19")))),
								Bags.getInstance(DatatypeConstants.TIME.TYPE,
										Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("17:18:19"))) },

						// urn:oasis:names:tc:xacml:1.0:function:date-union
						new Object[] {
								NAME_DATE_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2002-09-24"))),
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2002-09-24"), new DateValue("2003-10-25")))),
								Bags.getInstance(DatatypeConstants.DATE.TYPE,
										Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2003-10-25"))) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-union
						new Object[] {
								NAME_DATETIME_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"))), //
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
												Arrays.asList(new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")))),
								Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"), new DateTimeValue(
										"2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19"))) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-union
						new Object[] {
								NAME_ANYURI_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(
												new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue(
														"http://www.example.com/videos/sample.avi")))),
								Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"), new AnyURIValue(
										"http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/videos/sample.avi"))) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-union
						new Object[] {
								NAME_HEXBINARY_UNION,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB7"))),
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")))),
								Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
										Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-union
						new Object[] {
								NAME_BASE64BINARY_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="))), //
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")))),
								Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union
						new Object[] {
								NAME_DAYTIMEDURATION_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"))), //
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("P0D")))),
								Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
										Arrays.asList(new DayTimeDurationValue("PT20M"), new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P0D"))) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union
						new Object[] {
								NAME_YEARMONTHDURATION_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"))), //
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("P14M"), new YearMonthDurationValue("P0Y")))),
								Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P0Y"))) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-union
						new Object[] {
								NAME_X500NAME_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays
												.asList(new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue(
														"cn=John Smith, o=Other Corp, c=US")))),
								Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union
						new Object[] {
								NAME_RFC822NAME_UNION,
								Arrays.asList(Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
										new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"))), //
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")))),
								Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"), new RFC822NameValue(
										"Anderson@sun.com"), new RFC822NameValue("Smith@sun.com"))) },

						// urn:oasis:names:tc:xacml:1.0:function:string-subset
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String2"))),
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String3")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue(
										"String2"), new StringValue("String2"), new StringValue("String3"))),//
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String3")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue(
										"String2"), new StringValue("String2"), new StringValue("String3"))), //
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue("String2")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-subset
						new Object[] { NAME_BOOLEAN_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.TRUE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE))),
								BooleanValue.TRUE },
						new Object[] { NAME_BOOLEAN_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE, BooleanValue.TRUE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE))),
								BooleanValue.TRUE },
						new Object[] { NAME_BOOLEAN_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.TRUE))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:integer-subset
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"))),//
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3")))), BooleanValue.TRUE },
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"), new IntegerValue("3"))),
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3")))), BooleanValue.TRUE },
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"), new IntegerValue("3"))),
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("1"), new IntegerValue("2")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:double-subset
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."))),//
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("3.14")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."), new DoubleValue("3.14"))),
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("3.14")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."), new DoubleValue("3.14"))),
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2.")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:time-subset
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("09:30:15"))),//
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("17:18:19")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"),
										new TimeValue("09:30:15"), new TimeValue("17:18:19"))), //
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("17:18:19")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"),
										new TimeValue("09:30:15"), new TimeValue("17:18:19"))), //
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:date-subset
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2002-09-24"))),
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2003-10-25")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue(
										"2002-09-24"), new DateValue("2002-09-24"), new DateValue("2003-10-25"))), //
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2003-10-25")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue(
										"2002-09-24"), new DateValue("2002-09-24"), new DateValue("2003-10-25"))), //
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-subset
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"))), Bags.getInstance(
										DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"), new DateTimeValue(
												"2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue(
												"2003-10-25T17:18:19"))), //
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
												new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue(
												"2003-10-25T17:18:19"))), //
										Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
												Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"), new DateTimeValue("2002-09-24T09:30:15")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-subset
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
												new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue(
														"http://www.example.com/videos/sample.avi")))), BooleanValue.TRUE },
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"),
										new AnyURIValue("http://www.example.com/videos/sample.avi"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
												new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue(
														"http://www.example.com/videos/sample.avi")))), BooleanValue.TRUE },
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"),
										new AnyURIValue("http://www.example.com/videos/sample.avi"))), Bags.getInstance(DatatypeConstants.ANYURI.TYPE,
										Arrays.asList(new AnyURIValue("http://www.example.com"), new AnyURIValue("http://www.example.com/images/logo.gif")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB7"))),
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue(
										"0FB7"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))), //
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue(
										"0FB7"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))), //
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="))), //
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
												new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")))), BooleanValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))), Bags
										.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
												new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")))), BooleanValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))), Bags
										.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="), new Base64BinaryValue("RXhhbXBsZQ==")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"))), Bags.getInstance(
										DatatypeConstants.DAYTIMEDURATION.TYPE,
										Arrays.asList(new DayTimeDurationValue("PT20M"), new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("P0D")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P0D"))), Bags
										.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
												new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("P0D")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P0D"))), Bags
										.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("PT20M"), new DayTimeDurationValue("PT26H")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"))), Bags.getInstance(
										DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"), new YearMonthDurationValue(
												"P14M"), new YearMonthDurationValue("P0Y")))), BooleanValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P0Y"))), Bags
										.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
												new YearMonthDurationValue("P14M"), new YearMonthDurationValue("P0Y")))), BooleanValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P0Y"))), Bags
										.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("P20M"), new YearMonthDurationValue("P14M")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-subset
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays
												.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue(
														"cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays
												.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue(
														"cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"),
												new X500NameValue("cn=John Smith, o=Medico Corp, c=US")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
										new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"))), Bags.getInstance(
										DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"), new RFC822NameValue(
												"Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")))), BooleanValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
												new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue(
														"Smith@sun.com"))), Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(
												new RFC822NameValue("toto@example.com"), new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue(
														"Smith@sun.com")))), BooleanValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
												new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue(
														"Smith@sun.com"))), Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("toto@example.com"), new RFC822NameValue("Anderson@sun.com")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:string-set-equals
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String2"))),
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String3")))),
								BooleanValue.FALSE },
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue(
										"String2"), new StringValue("String2"), new StringValue("String3"))), //
										Bags.getInstance(DatatypeConstants.STRING.TYPE,
												Arrays.asList(new StringValue("String1"), new StringValue("String2"), new StringValue("String3")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue(
										"String2"), new StringValue("String2"), new StringValue("String3"))), //
										Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("String1"), new StringValue("String2")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals
						new Object[] { NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.TRUE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE))),
								BooleanValue.FALSE },
						new Object[] { NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.FALSE, BooleanValue.TRUE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE))),
								BooleanValue.TRUE },
						new Object[] { NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE)),//
										Bags.getInstance(DatatypeConstants.BOOLEAN.TYPE, Arrays.asList(BooleanValue.TRUE, BooleanValue.TRUE))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:integer-set-equals
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"))),//
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3")))), BooleanValue.FALSE },
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"), new IntegerValue("3"))),
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3")))), BooleanValue.TRUE },
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
												Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("2"), new IntegerValue("3"))),
										Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("1"), new IntegerValue("2")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:double-set-equals
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."))),//
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("3.14")))), BooleanValue.FALSE },
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."), new DoubleValue("3.14"))),
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("3.14")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE,
												Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2."), new DoubleValue("2."), new DoubleValue("3.14"))),
										Bags.getInstance(DatatypeConstants.DOUBLE.TYPE, Arrays.asList(new DoubleValue("1.23"), new DoubleValue("2.")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:time-set-equals
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("09:30:15"))),//
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("17:18:19")))),
								BooleanValue.FALSE },
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"),
										new TimeValue("09:30:15"), new TimeValue("17:18:19"))), //
										Bags.getInstance(DatatypeConstants.TIME.TYPE,
												Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"), new TimeValue("17:18:19")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15"),
										new TimeValue("09:30:15"), new TimeValue("17:18:19"))), //
										Bags.getInstance(DatatypeConstants.TIME.TYPE, Arrays.asList(new TimeValue("08:15:56"), new TimeValue("09:30:15")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:date-set-equals
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2002-09-24"))),
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2003-10-25")))),
								BooleanValue.FALSE },
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue(
										"2002-09-24"), new DateValue("2002-09-24"), new DateValue("2003-10-25"))), //
										Bags.getInstance(DatatypeConstants.DATE.TYPE,
												Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24"), new DateValue("2003-10-25")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue(
										"2002-09-24"), new DateValue("2002-09-24"), new DateValue("2003-10-25"))), //
										Bags.getInstance(DatatypeConstants.DATE.TYPE, Arrays.asList(new DateValue("2012-01-01"), new DateValue("2002-09-24")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"))), Bags.getInstance(
										DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"), new DateTimeValue(
												"2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")))), BooleanValue.FALSE },
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue(
												"2003-10-25T17:18:19"))), Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue(
										"2012-01-01T08:15:56"), new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2003-10-25T17:18:19")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DATETIME.TYPE, Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"),
										new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue("2002-09-24T09:30:15"), new DateTimeValue(
												"2003-10-25T17:18:19"))), Bags.getInstance(DatatypeConstants.DATETIME.TYPE,
										Arrays.asList(new DateTimeValue("2012-01-01T08:15:56"), new DateTimeValue("2002-09-24T09:30:15")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
												new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue(
														"http://www.example.com/videos/sample.avi")))), BooleanValue.FALSE },
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"),
										new AnyURIValue("http://www.example.com/videos/sample.avi"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
												new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue(
														"http://www.example.com/videos/sample.avi")))), BooleanValue.TRUE },
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
										new AnyURIValue("http://www.example.com/images/logo.gif"), new AnyURIValue("http://www.example.com/images/logo.gif"),
										new AnyURIValue("http://www.example.com/videos/sample.avi"))), //
										Bags.getInstance(DatatypeConstants.ANYURI.TYPE, Arrays.asList(new AnyURIValue("http://www.example.com"),
												new AnyURIValue("http://www.example.com/images/logo.gif")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB7"))),
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")))),
								BooleanValue.FALSE },
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue(
										"0FB7"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))), //
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE, Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue(
										"0FB7"), new HexBinaryValue("0FB7"), new HexBinaryValue("0FB8"))), //
										Bags.getInstance(DatatypeConstants.HEXBINARY.TYPE,
												Arrays.asList(new HexBinaryValue("0FB6"), new HexBinaryValue("0FB7")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="))), //
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
												new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")))), BooleanValue.FALSE },
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))), //
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
												new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI=")))), BooleanValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE, Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="),
										new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("RXhhbXBsZQ=="), new Base64BinaryValue("T3RoZXI="))), //
										Bags.getInstance(DatatypeConstants.BASE64BINARY.TYPE,
												Arrays.asList(new Base64BinaryValue("UGFyaXNTRw=="), new Base64BinaryValue("RXhhbXBsZQ==")))),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"))), Bags.getInstance(
										DatatypeConstants.DAYTIMEDURATION.TYPE,
										Arrays.asList(new DayTimeDurationValue("PT20M"), new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("P0D")))),
								BooleanValue.FALSE },
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P0D"))), //
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
												new DayTimeDurationValue("PT26H"), new DayTimeDurationValue("P0D")))), BooleanValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE, Arrays.asList(new DayTimeDurationValue("PT20M"),
										new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P1DT2H"), new DayTimeDurationValue("P0D"))),//
										Bags.getInstance(DatatypeConstants.DAYTIMEDURATION.TYPE,
												Arrays.asList(new DayTimeDurationValue("PT20M"), new DayTimeDurationValue("PT26H")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"))), Bags.getInstance(
										DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"), new YearMonthDurationValue(
												"P14M"), new YearMonthDurationValue("P0Y")))), BooleanValue.FALSE },
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P0Y"))), //
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
												new YearMonthDurationValue("P14M"), new YearMonthDurationValue("P0Y")))), BooleanValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE, Arrays.asList(new YearMonthDurationValue("P20M"),
										new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P1Y2M"), new YearMonthDurationValue("P0Y"))),//
										Bags.getInstance(DatatypeConstants.YEARMONTHDURATION.TYPE,
												Arrays.asList(new YearMonthDurationValue("P20M"), new YearMonthDurationValue("P14M")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays
												.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue(
														"cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US")))),
								BooleanValue.FALSE },
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays
												.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue(
														"cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US")))),
								BooleanValue.TRUE },
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(
										new X500NameValue("cn=John Smith, o=Thales, c=FR"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US"),
										new X500NameValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameValue("cn=John Smith, o=Other Corp, c=US"))), //
										Bags.getInstance(DatatypeConstants.X500NAME.TYPE, Arrays.asList(new X500NameValue("cn=John Smith, o=Thales, c=FR"),
												new X500NameValue("cn=John Smith, o=Medico Corp, c=US")))), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
										new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"))), Bags.getInstance(
										DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"), new RFC822NameValue(
												"Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")))), BooleanValue.FALSE },
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
												new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue(
														"Smith@sun.com"))), //
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
												new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Smith@sun.com")))), BooleanValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE, Arrays.asList(new RFC822NameValue("toto@example.com"),
												new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com"), new RFC822NameValue(
														"Smith@sun.com"))), //
										Bags.getInstance(DatatypeConstants.RFC822NAME.TYPE,
												Arrays.asList(new RFC822NameValue("toto@example.com"), new RFC822NameValue("Anderson@sun.com")))),
								BooleanValue.FALSE });
	}

}
