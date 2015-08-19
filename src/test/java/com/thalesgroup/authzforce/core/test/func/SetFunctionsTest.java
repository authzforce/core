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
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

@RunWith(Parameterized.class)
public class SetFunctionsTest extends GeneralFunctionTest
{

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
						Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
								new BagResult<>(Arrays.asList(new StringAttributeValue("String2"), new StringAttributeValue("String3"), new StringAttributeValue("String3"), new StringAttributeValue("String4")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),
						new BagResult<>(Arrays.asList(new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-intersection
						new Object[] {
								NAME_BOOLEAN_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-intersection
						new Object[] {
								NAME_INTEGER_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:double-intersection
						new Object[] {
								NAME_DOUBLE_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14"), new DoubleAttributeValue("3.14"), new DoubleAttributeValue("4.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)),//
								new BagResult<>(Arrays.asList(new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:time-intersection
						new Object[] {
								NAME_TIME_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19"), new TimeAttributeValue("17:18:19"), new TimeAttributeValue("03:56:12")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:date-intersection
						new Object[] {
								NAME_DATE_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25"), new DateAttributeValue("2003-10-25"), new DateAttributeValue("1970-01-01")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)),//
								new BagResult<>(Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection
						new Object[] {
								NAME_DATETIME_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19"), new DateTimeAttributeValue("2003-10-25T17:18:19"), new DateTimeAttributeValue("1970-01-01T03:56:12")),
												DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection
						new Object[] {
								NAME_ANYURI_INTERSECTION,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue(
												"http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi"),
												new AnyURIAttributeValue("https://www.thalesgroup.com")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection
						new Object[] {
								NAME_HEXBINARY_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8"), new HexBinaryAttributeValue("0FB8"), new HexBinaryAttributeValue("0FB9")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)),//
								new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection
						new Object[] {
								NAME_BASE64BINARY_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI="), new Base64BinaryAttributeValue("T3RoZXI="), new Base64BinaryAttributeValue("VGVzdA==")),
										Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection
						new Object[] {
								NAME_DAYTIMEDURATION_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("-P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("P0D"), new DayTimeDurationAttributeValue("P0D"), new DayTimeDurationAttributeValue("-PT1M30.5S")),
										DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection
						new Object[] {
								NAME_YEARMONTHDURATION_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("-P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P0Y"), new YearMonthDurationAttributeValue("P0Y"), new YearMonthDurationAttributeValue("-P60Y")),
										YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection
						new Object[] {
								NAME_X500NAME_INTERSECTION,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue(
												"cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US"), new X500NameAttributeValue(
												"cn=Mark Anderson, o=Thales, c=FR")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection
						new Object[] {
								NAME_RFC822NAME_INTERSECTION,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com"), new RFC822NameAttributeValue("Smith@sun.com"), new RFC822NameAttributeValue("john.doe@example.com")),
												RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of
						new Object[] { NAME_STRING_AT_LEAST_ONE_MEMBER_OF, new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
								new BagResult<>(Arrays.asList(new StringAttributeValue("String2"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE), BooleanAttributeValue.TRUE },
						new Object[] { NAME_STRING_AT_LEAST_ONE_MEMBER_OF, new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String1")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
								new BagResult<>(Arrays.asList(new StringAttributeValue("String2"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of
						new Object[] { NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF, new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE),
								new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of
						new Object[] {
								NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new IntegerAttributeValue("2"), new IntegerAttributeValue("2")),
										IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("1")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new IntegerAttributeValue("2"), new IntegerAttributeValue("2")),
										IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of
						new Object[] {
								NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DoubleAttributeValue("3.14"), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("2."), new DoubleAttributeValue("2.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DoubleAttributeValue("3.14"), new DoubleAttributeValue("3.14")),
										DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of
						new Object[] {
								NAME_TIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("17:18:19"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_TIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("17:18:19"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of
						new Object[] {
								NAME_DATE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2003-10-25"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2003-10-25"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of
						new Object[] {
								NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2003-10-25T17:18:19"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2003-10-25T17:18:19"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of
						new Object[] {
								NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/videos/sample.avi"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/videos/sample.avi"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of
						new Object[] {
								NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB8"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB8"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-o
						new Object[] { NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF, Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("T3RoZXI="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("T3RoZXI="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-one-member-of
						new Object[] {
								NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("-P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("PT26H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] { NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF, Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("-P0D"), new DayTimeDurationAttributeValue("-P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("PT26H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-one-member-of
						new Object[] {
								NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("-P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P14M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("-P0Y"), new YearMonthDurationAttributeValue("-P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P14M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of
						new Object[] {
								NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of
						new Object[] {
								NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Smith@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Smith@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:string-union
						new Object[] {
								NAME_STRING_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-union
						new Object[] {
								NAME_BOOLEAN_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE),
								new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-union
						new Object[] { NAME_INTEGER_UNION, Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:double-union
						new Object[] { NAME_DOUBLE_UNION, Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:time-union
						new Object[] { NAME_TIME_UNION, Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:date-union
						new Object[] {
								NAME_DATE_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-union
						new Object[] {
								NAME_DATETIME_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-union
						new Object[] {
								NAME_ANYURI_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif")), AnyURIAttributeValue.class,
										AnyURIAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class,
										AnyURIAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-union
						new Object[] {
								NAME_HEXBINARY_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-union
						new Object[] {
								NAME_BASE64BINARY_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union
						new Object[] {
								NAME_DAYTIMEDURATION_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union
						new Object[] {
								NAME_YEARMONTHDURATION_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-union
						new Object[] {
								NAME_X500NAME_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), X500NameAttributeValue.class,
										X500NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class,
										X500NameAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union
						new Object[] {
								NAME_RFC822NAME_UNION,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)),
								new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE) },

						// urn:oasis:names:tc:xacml:1.0:function:string-subset
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-subset
						new Object[] {
								NAME_BOOLEAN_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BOOLEAN_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BOOLEAN_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:integer-subset
						new Object[] { NAME_INTEGER_SUBSET, Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:double-subset
						new Object[] { NAME_DOUBLE_SUBSET, Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:time-subset
						new Object[] { NAME_TIME_SUBSET, Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:date-subset
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-subset
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-subset
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif")), AnyURIAttributeValue.class,
										AnyURIAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")),
										AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue(
												"http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class,
												AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue(
												"http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("PT26H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P14M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-subset
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), X500NameAttributeValue.class,
										X500NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")),
										X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue(
												"cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class,
												X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue(
												"cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com")), RFC822NameAttributeValue.class,
										RFC822NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:string-set-equals
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2"), new StringAttributeValue("String2"), new StringAttributeValue("String3")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new StringAttributeValue("String1"), new StringAttributeValue("String2")), StringAttributeValue.class, StringAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals
						new Object[] {
								NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE),
										BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.class, BooleanAttributeValue.BAG_TYPE), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:integer-set-equals
						new Object[] { NAME_INTEGER_SET_EQUALS, Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2")), IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:double-set-equals
						new Object[] { NAME_DOUBLE_SET_EQUALS, Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2."), new DoubleAttributeValue("2."), new DoubleAttributeValue("3.14")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DoubleAttributeValue("1.23"), new DoubleAttributeValue("2.")), DoubleAttributeValue.class, DoubleAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:time-set-equals
						new Object[] { NAME_TIME_SET_EQUALS, Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),//
								new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new TimeAttributeValue("08:15:56"), new TimeAttributeValue("09:30:15")), TimeAttributeValue.class, TimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:date-set-equals
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateAttributeValue("2012-01-01"), new DateAttributeValue("2002-09-24")), DateAttributeValue.class, DateAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")),
										DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DateTimeAttributeValue("2012-01-01T08:15:56"), new DateTimeAttributeValue("2002-09-24T09:30:15")), DateTimeAttributeValue.class, DateTimeAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif")), AnyURIAttributeValue.class,
										AnyURIAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")),
										AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue(
												"http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class,
												AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif"), new AnyURIAttributeValue(
												"http://www.example.com/videos/sample.avi")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example.com/images/logo.gif")), AnyURIAttributeValue.class, AnyURIAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new HexBinaryAttributeValue("0FB6"), new HexBinaryAttributeValue("0FB7")), HexBinaryAttributeValue.class, HexBinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")), Base64BinaryAttributeValue.class,
										Base64BinaryAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new Base64BinaryAttributeValue("UGFyaXNTRw=="), new Base64BinaryAttributeValue("RXhhbXBsZQ==")), Base64BinaryAttributeValue.class, Base64BinaryAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("PT26H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("P0D")), DayTimeDurationAttributeValue.class,
										DayTimeDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new DayTimeDurationAttributeValue("PT20M"), new DayTimeDurationAttributeValue("PT26H")), DayTimeDurationAttributeValue.class, DayTimeDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P14M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("P0Y")), YearMonthDurationAttributeValue.class,
										YearMonthDurationAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new YearMonthDurationAttributeValue("P20M"), new YearMonthDurationAttributeValue("P14M")), YearMonthDurationAttributeValue.class, YearMonthDurationAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), X500NameAttributeValue.class,
										X500NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")),
										X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue(
												"cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class,
												X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue(
												"cn=John Smith, o=Other Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new X500NameAttributeValue("cn=John Smith, o=Thales, c=FR"), new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")), X500NameAttributeValue.class, X500NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE),
										new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")), RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)),
								BooleanAttributeValue.FALSE },
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.TRUE },
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")),
										RFC822NameAttributeValue.class, RFC822NameAttributeValue.BAG_TYPE), new BagResult<>(Arrays.asList(new RFC822NameAttributeValue("toto@example.com"), new RFC822NameAttributeValue("Anderson@sun.com")), RFC822NameAttributeValue.class,
										RFC822NameAttributeValue.BAG_TYPE)), BooleanAttributeValue.FALSE });
	}

	protected SetFunctionsTest(String functionName, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs, ExpressionResult<? extends AttributeValue> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

}
