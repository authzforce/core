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
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.Base64BinaryAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.HexBinaryAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.TimeAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.attr.YearMonthDurationAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.Apply;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

@RunWith(Parameterized.class)
public class SetFunctionsTest extends GeneralFunctionTest {

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
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-intersection
				new Object[] {
						NAME_STRING_INTERSECTION,
						Arrays.asList(
								new Apply(
										BagFunction
												.getBagInstance(
														"urn:oasis:names:tc:xacml:1.0:function:string-bag",
														StringAttribute.identifier),
										Arrays.asList(
												(ExpressionType) StringAttribute
														.getInstance("String1"),
												(ExpressionType) StringAttribute
														.getInstance("String2"),
												(ExpressionType) StringAttribute
														.getInstance("String2"),
												(ExpressionType) StringAttribute
														.getInstance("String3"))),
								new Apply(
										BagFunction
												.getBagInstance(
														"urn:oasis:names:tc:xacml:1.0:function:string-bag",
														StringAttribute.identifier),
										Arrays.asList(
												(ExpressionType) StringAttribute
														.getInstance("String2"),
												(ExpressionType) StringAttribute
														.getInstance("String3"),
												(ExpressionType) StringAttribute
														.getInstance("String3"),
												(ExpressionType) StringAttribute
														.getInstance("String4")))),
						new EvaluationResult(new BagAttribute(
								StringAttribute.identifier, Arrays.asList(
										(AttributeValue) StringAttribute
												.getInstance("String2"),
										(AttributeValue) StringAttribute
												.getInstance("String3")))) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-intersection
						new Object[] {
								NAME_BOOLEAN_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								new EvaluationResult(
										new BagAttribute(
												BooleanAttribute.identifier,
												Arrays.asList((AttributeValue) BooleanAttribute
														.getInstance(false)))) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-intersection
						new Object[] {
								NAME_INTEGER_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4")))),
								new EvaluationResult(
										new BagAttribute(
												IntegerAttribute.identifier,
												Arrays.asList(
														(AttributeValue) IntegerAttribute
																.getInstance("2"),
														(AttributeValue) IntegerAttribute
																.getInstance("3")))) },

						// urn:oasis:names:tc:xacml:1.0:function:double-intersection
						new Object[] {
								NAME_DOUBLE_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"),
														(ExpressionType) DoubleAttribute
																.getInstance("4.")))),
								new EvaluationResult(
										new BagAttribute(
												DoubleAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DoubleAttribute
																.getInstance("2."),
														(AttributeValue) DoubleAttribute
																.getInstance("3.14")))) },

						// urn:oasis:names:tc:xacml:1.0:function:time-intersection
						new Object[] {
								NAME_TIME_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"),
														(ExpressionType) TimeAttribute
																.getInstance("03:56:12")))),
								new EvaluationResult(
										new BagAttribute(
												TimeAttribute.identifier,
												Arrays.asList(
														(AttributeValue) TimeAttribute
																.getInstance("09:30:15"),
														(AttributeValue) TimeAttribute
																.getInstance("17:18:19")))) },

						// urn:oasis:names:tc:xacml:1.0:function:date-intersection
						new Object[] {
								NAME_DATE_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"),
														(ExpressionType) DateAttribute
																.getInstance("1970-01-01")))),
								new EvaluationResult(
										new BagAttribute(
												DateAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DateAttribute
																.getInstance("2002-09-24"),
														(AttributeValue) DateAttribute
																.getInstance("2003-10-25")))) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection
						new Object[] {
								NAME_DATETIME_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"),
														(ExpressionType) DateTimeAttribute
																.getInstance("1970-01-01T03:56:12")))),
								new EvaluationResult(
										new BagAttribute(
												DateTimeAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(AttributeValue) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection
						new Object[] {
								NAME_ANYURI_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"),
														(ExpressionType) AnyURIAttribute
																.getInstance("https://www.thalesgroup.com")))),
								new EvaluationResult(
										new BagAttribute(
												AnyURIAttribute.identifier,
												Arrays.asList(
														(AttributeValue) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(AttributeValue) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection
						new Object[] {
								NAME_HEXBINARY_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB9")))),
								new EvaluationResult(
										new BagAttribute(
												HexBinaryAttribute.identifier,
												Arrays.asList(
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB7"),
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB8")))) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection
						new Object[] {
								NAME_BASE64BINARY_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("VGVzdA==")))),
								new EvaluationResult(
										new BagAttribute(
												Base64BinaryAttribute.identifier,
												Arrays.asList(
														(AttributeValue) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(AttributeValue) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection
						new Object[] {
								NAME_DAYTIMEDURATION_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("-P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("-PT1M30.5S")))),
								new EvaluationResult(
										new BagAttribute(
												DayTimeDurationAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("P0D")))) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection
						new Object[] {
								NAME_YEARMONTHDURATION_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("-P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("-P60Y")))),
								new EvaluationResult(
										new BagAttribute(
												YearMonthDurationAttribute.identifier,
												Arrays.asList(
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("P0Y")))) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection
						new Object[] {
								NAME_X500NAME_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=Mark Anderson, o=Thales, c=FR")))),
								new EvaluationResult(
										new BagAttribute(
												X500NameAttribute.identifier,
												Arrays.asList(
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection
						new Object[] {
								NAME_RFC822NAME_INTERSECTION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("john.doe@example.com")))),
								new EvaluationResult(
										new BagAttribute(
												RFC822NameAttribute.identifier,
												Arrays.asList(
														(AttributeValue) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(AttributeValue) RFC822NameAttribute
																.getInstance("Smith@sun.com")))) },

						// urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of
						new Object[] {
								NAME_STRING_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String1"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of
						new Object[] {
								NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(true))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of
						new Object[] {
								NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_INTEGER_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("1"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of
						new Object[] {
								NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of
						new Object[] {
								NAME_TIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_TIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of
						new Object[] {
								NAME_DATE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATE_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of
						new Object[] {
								NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATETIME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of
						new Object[] {
								NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of
						new Object[] {
								NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_HEXBINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-o
						new Object[] {
								NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-one-member-of
						new Object[] {
								NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("-P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("-P0D"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("-P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-one-member-of
						new Object[] {
								NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("-P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("-P0Y"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("-P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of
						new Object[] {
								NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of
						new Object[] {
								NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_AT_LEAST_ONE_MEMBER_OF,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:string-union
						new Object[] {
								NAME_STRING_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3")))),
								new EvaluationResult(
										new BagAttribute(
												StringAttribute.identifier,
												Arrays.asList(
														(AttributeValue) StringAttribute
																.getInstance("String1"),
														(AttributeValue) StringAttribute
																.getInstance("String2"),
														(AttributeValue) StringAttribute
																.getInstance("String3")))) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-union
						new Object[] {
								NAME_BOOLEAN_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								new EvaluationResult(
										new BagAttribute(
												BooleanAttribute.identifier,
												Arrays.asList(
														(AttributeValue) BooleanAttribute
																.getInstance(true),
														(AttributeValue) BooleanAttribute
																.getInstance(false)))) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-union
						new Object[] {
								NAME_INTEGER_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								new EvaluationResult(
										new BagAttribute(
												IntegerAttribute.identifier,
												Arrays.asList(
														(AttributeValue) IntegerAttribute
																.getInstance("1"),
														(AttributeValue) IntegerAttribute
																.getInstance("2"),
														(AttributeValue) IntegerAttribute
																.getInstance("3")))) },

						// urn:oasis:names:tc:xacml:1.0:function:double-union
						new Object[] {
								NAME_DOUBLE_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								new EvaluationResult(
										new BagAttribute(
												DoubleAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DoubleAttribute
																.getInstance("1.23"),
														(AttributeValue) DoubleAttribute
																.getInstance("2."),
														(AttributeValue) DoubleAttribute
																.getInstance("3.14")))) },

						// urn:oasis:names:tc:xacml:1.0:function:time-union
						new Object[] {
								NAME_TIME_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								new EvaluationResult(
										new BagAttribute(
												TimeAttribute.identifier,
												Arrays.asList(
														(AttributeValue) TimeAttribute
																.getInstance("08:15:56"),
														(AttributeValue) TimeAttribute
																.getInstance("09:30:15"),
														(AttributeValue) TimeAttribute
																.getInstance("17:18:19")))) },

						// urn:oasis:names:tc:xacml:1.0:function:date-union
						new Object[] {
								NAME_DATE_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								new EvaluationResult(
										new BagAttribute(
												DateAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DateAttribute
																.getInstance("2012-01-01"),
														(AttributeValue) DateAttribute
																.getInstance("2002-09-24"),
														(AttributeValue) DateAttribute
																.getInstance("2003-10-25")))) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-union
						new Object[] {
								NAME_DATETIME_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								new EvaluationResult(
										new BagAttribute(
												DateTimeAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(AttributeValue) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(AttributeValue) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-union
						new Object[] {
								NAME_ANYURI_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								new EvaluationResult(
										new BagAttribute(
												AnyURIAttribute.identifier,
												Arrays.asList(
														(AttributeValue) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(AttributeValue) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(AttributeValue) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-union
						new Object[] {
								NAME_HEXBINARY_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								new EvaluationResult(
										new BagAttribute(
												HexBinaryAttribute.identifier,
												Arrays.asList(
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB6"),
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB7"),
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB8")))) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-union
						new Object[] {
								NAME_BASE64BINARY_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								new EvaluationResult(
										new BagAttribute(
												Base64BinaryAttribute.identifier,
												Arrays.asList(
														(AttributeValue) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(AttributeValue) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(AttributeValue) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union
						new Object[] {
								NAME_DAYTIMEDURATION_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D")))),
								new EvaluationResult(
										new BagAttribute(
												DayTimeDurationAttribute.identifier,
												Arrays.asList(
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("P0D")))) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union
						new Object[] {
								NAME_YEARMONTHDURATION_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y")))),
								new EvaluationResult(
										new BagAttribute(
												YearMonthDurationAttribute.identifier,
												Arrays.asList(
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("P20M"),
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("P0Y")))) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-union
						new Object[] {
								NAME_X500NAME_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								new EvaluationResult(
										new BagAttribute(
												X500NameAttribute.identifier,
												Arrays.asList(
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union
						new Object[] {
								NAME_RFC822NAME_UNION,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								new EvaluationResult(
										new BagAttribute(
												RFC822NameAttribute.identifier,
												Arrays.asList(
														(AttributeValue) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(AttributeValue) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(AttributeValue) RFC822NameAttribute
																.getInstance("Smith@sun.com")))) },

						// urn:oasis:names:tc:xacml:1.0:function:string-subset
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-subset
						new Object[] {
								NAME_BOOLEAN_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(true))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(true))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(true)))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-subset
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_INTEGER_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-subset
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2.")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:time-subset
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_TIME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:date-subset
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATE_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-subset
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATETIME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-subset
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_HEXBINARY_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BASE64BINARY_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ==")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DAYTIMEDURATION_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_YEARMONTHDURATION_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-subset
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_SUBSET,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:string-set-equals
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String2"),
														(ExpressionType) StringAttribute
																.getInstance("String3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("String1"),
														(ExpressionType) StringAttribute
																.getInstance("String2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals
						new Object[] {
								NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(true))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(true))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(false))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(true),
														(ExpressionType) BooleanAttribute
																.getInstance(true)))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-set-equals
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_INTEGER_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-set-equals
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("2."),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:double-bag",
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("1.23"),
														(ExpressionType) DoubleAttribute
																.getInstance("2.")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:time-set-equals
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_TIME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:time-bag",
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("08:15:56"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:date-set-equals
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATE_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2003-10-25"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:date-bag",
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2012-01-01"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATETIME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2012-01-01T08:15:56"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/videos/sample.avi"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com/images/logo.gif")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_HEXBINARY_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB8"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB6"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BASE64BINARY_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("T3RoZXI="))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("UGFyaXNTRw=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ==")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DAYTIMEDURATION_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P0D"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT20M"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("PT26H")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_YEARMONTHDURATION_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P0Y"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P20M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P14M")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Thales, c=FR"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_SET_EQUALS,
								Arrays.asList(
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Smith@sun.com"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("toto@example.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com")))),
								EvaluationResult.getInstance(false) });
	}

	public SetFunctionsTest(String functionName, List<ExpressionType> inputs,
			EvaluationResult expectedResult) throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
