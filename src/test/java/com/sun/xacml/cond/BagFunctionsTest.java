/**
 * 
 */
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.Base64BinaryAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DNSNameAttribute;
import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.HexBinaryAttribute;
import com.sun.xacml.attr.IPAddressAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.TimeAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.attr.YearMonthDurationAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.Apply;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;

/**
 * @author Cyrille MARTINS (Thales)
 * 
 */
@RunWith(Parameterized.class)
public class BagFunctionsTest extends AbstractFunctionTest {

	private static final String NAME_STRING_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
	private static final String NAME_BOOLEAN_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only";
	private static final String NAME_INTEGER_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
	private static final String NAME_DOUBLE_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:double-one-and-only";
	private static final String NAME_TIME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:time-one-and-only";
	private static final String NAME_DATE_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:date-one-and-only";
	private static final String NAME_DATETIME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only";
	private static final String NAME_ANYURI_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only";
	private static final String NAME_HEXBINARY_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only";
	private static final String NAME_BASE64BINARY_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only";
	private static final String NAME_DAYTIMEDURATION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only";
	private static final String NAME_YEARMONTHDURATION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only";
	private static final String NAME_X500NAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only";
	private static final String NAME_RFC822NAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only";
	private static final String NAME_IPADDRESS_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only";
	private static final String NAME_DNSNAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only";
	private static final String NAME_STRING_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:string-bag-size";
	private static final String NAME_BOOLEAN_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size";
	private static final String NAME_INTEGER_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:integer-bag-size";
	private static final String NAME_DOUBLE_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:double-bag-size";
	private static final String NAME_TIME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:time-bag-size";
	private static final String NAME_DATE_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:date-bag-size";
	private static final String NAME_DATETIME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size";
	private static final String NAME_ANYURI_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size";
	private static final String NAME_HEXBINARY_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size";
	private static final String NAME_BASE64BINARY_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size";
	private static final String NAME_DAYTIMEDURATION_BAG_SIZE = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size";
	private static final String NAME_YEARMONTHDURATION_BAG_SIZE = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size";
	private static final String NAME_X500NAME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size";
	private static final String NAME_RFC822NAME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size";
	private static final String NAME_IPADDRESS_BAG_SIZE = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size";
	private static final String NAME_DNSNAME_BAG_SIZE = "urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size";
	private static final String NAME_STRING_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:string-is-in";
	private static final String NAME_BOOLEAN_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:boolean-is-in";
	private static final String NAME_INTEGER_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:integer-is-in";
	private static final String NAME_DOUBLE_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:double-is-in";
	private static final String NAME_TIME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:time-is-in";
	private static final String NAME_DATE_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:date-is-in";
	private static final String NAME_DATETIME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in";
	private static final String NAME_ANYURI_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in";
	private static final String NAME_HEXBINARY_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in";
	private static final String NAME_BASE64BINARY_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in";
	private static final String NAME_DAYTIMEDURATION_IS_IN = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in";
	private static final String NAME_YEARMONTHDURATION_IS_IN = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in";
	private static final String NAME_X500NAME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in";
	private static final String NAME_RFC822NAME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in";
	private static final String NAME_STRING_BAG = "urn:oasis:names:tc:xacml:1.0:function:string-bag";
	private static final String NAME_BOOLEAN_BAG = "urn:oasis:names:tc:xacml:1.0:function:boolean-bag";
	private static final String NAME_INTEGER_BAG = "urn:oasis:names:tc:xacml:1.0:function:integer-bag";
	private static final String NAME_DOUBLE_BAG = "urn:oasis:names:tc:xacml:1.0:function:double-bag";
	private static final String NAME_TIME_BAG = "urn:oasis:names:tc:xacml:1.0:function:time-bag";
	private static final String NAME_DATE_BAG = "urn:oasis:names:tc:xacml:1.0:function:date-bag";
	private static final String NAME_DATETIME_BAG = "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag";
	private static final String NAME_ANYURI_BAG = "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag";
	private static final String NAME_HEXBINARY_BAG = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag";
	private static final String NAME_BASE64BINARY_BAG = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag";
	private static final String NAME_DAYTIMEDURATION_BAG = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag";
	private static final String NAME_YEARMONTHDURATION_BAG = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag";
	private static final String NAME_X500NAME_BAG = "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag";
	private static final String NAME_RFC822NAME_BAG = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag";
	private static final String NAME_IPADDRESS_BAG = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag";
	private static final String NAME_DNSNAME_BAG = "urn:oasis:names:tc:xacml:2.0:function:dnsName-bag";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-one-and-only
				new Object[] {
						NAME_STRING_ONE_AND_ONLY,
						Arrays.asList(new Apply(BagFunction.getBagInstance(
								NAME_STRING_BAG, StringAttribute.identifier),
								Arrays.asList((ExpressionType) StringAttribute
										.getInstance("Test")))),
						new EvaluationResult(StringAttribute
								.getInstance("Test")) },
						new Object[] {
								NAME_STRING_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_STRING_BAG,
												StringAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_STRING_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_STRING_BAG,
												StringAttribute.identifier),
										Arrays.asList(
												(ExpressionType) StringAttribute
														.getInstance("Test"),
												(ExpressionType) StringAttribute
														.getInstance("Test")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only
						new Object[] {
								NAME_BOOLEAN_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_BOOLEAN_BAG,
												BooleanAttribute.identifier),
										Arrays.asList((ExpressionType) BooleanAttribute
												.getInstance(false)))),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_BOOLEAN_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_BOOLEAN_BAG,
												BooleanAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_BOOLEAN_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_BOOLEAN_BAG,
												BooleanAttribute.identifier),
										Arrays.asList(
												(ExpressionType) BooleanAttribute
														.getInstance(false),
												(ExpressionType) BooleanAttribute
														.getInstance(false)))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
						new Object[] {
								NAME_INTEGER_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_INTEGER_BAG,
												IntegerAttribute.identifier),
										Arrays.asList((ExpressionType) IntegerAttribute
												.getInstance("3")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },
						new Object[] {
								NAME_INTEGER_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_INTEGER_BAG,
												IntegerAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_INTEGER_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_INTEGER_BAG,
												IntegerAttribute.identifier),
										Arrays.asList(
												(ExpressionType) IntegerAttribute
														.getInstance("3"),
												(ExpressionType) IntegerAttribute
														.getInstance("3")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:double-one-and-only
						new Object[] {
								NAME_DOUBLE_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DOUBLE_BAG,
												DoubleAttribute.identifier),
										Arrays.asList((ExpressionType) DoubleAttribute
												.getInstance("3.14")))),
								new EvaluationResult(DoubleAttribute
										.getInstance("3.14")) },
						new Object[] {
								NAME_DOUBLE_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_DOUBLE_BAG,
												DoubleAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_DOUBLE_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DOUBLE_BAG,
												DoubleAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DoubleAttribute
														.getInstance("3.14"),
												(ExpressionType) DoubleAttribute
														.getInstance("3.14")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:time-one-and-only
						new Object[] {
								NAME_TIME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_TIME_BAG,
												TimeAttribute.identifier),
										Arrays.asList((ExpressionType) TimeAttribute
												.getInstance("09:30:15")))),
								new EvaluationResult(TimeAttribute
										.getInstance("09:30:15")) },
						new Object[] {
								NAME_TIME_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_TIME_BAG,
												TimeAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_TIME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_TIME_BAG,
												TimeAttribute.identifier),
										Arrays.asList(
												(ExpressionType) TimeAttribute
														.getInstance("09:30:15"),
												(ExpressionType) TimeAttribute
														.getInstance("09:30:15")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:date-one-and-only
						new Object[] {
								NAME_DATE_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DATE_BAG,
												DateAttribute.identifier),
										Arrays.asList((ExpressionType) DateAttribute
												.getInstance("2002-09-24")))),
								new EvaluationResult(DateAttribute
										.getInstance("2002-09-24")) },
						new Object[] {
								NAME_DATE_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_DATE_BAG,
												DateAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_DATE_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DATE_BAG,
												DateAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DateAttribute
														.getInstance("2002-09-24"),
												(ExpressionType) DateAttribute
														.getInstance("2002-09-24")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only
						new Object[] {
								NAME_DATETIME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DATETIME_BAG,
												DateTimeAttribute.identifier),
										Arrays.asList((ExpressionType) DateTimeAttribute
												.getInstance("2002-09-24T09:30:15")))),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15")) },
						new Object[] {
								NAME_DATETIME_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_DATETIME_BAG,
												DateTimeAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_DATETIME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DATETIME_BAG,
												DateTimeAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DateTimeAttribute
														.getInstance("2002-09-24T09:30:15"),
												(ExpressionType) DateTimeAttribute
														.getInstance("2002-09-24T09:30:15")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only
						new Object[] {
								NAME_ANYURI_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_ANYURI_BAG,
												AnyURIAttribute.identifier),
										Arrays.asList((ExpressionType) AnyURIAttribute
												.getInstance("http://www.example.com")))),
								new EvaluationResult(AnyURIAttribute
										.getInstance("http://www.example.com")) },
						new Object[] {
								NAME_ANYURI_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_ANYURI_BAG,
												AnyURIAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_ANYURI_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_ANYURI_BAG,
												AnyURIAttribute.identifier),
										Arrays.asList(
												(ExpressionType) AnyURIAttribute
														.getInstance("http://www.example.com"),
												(ExpressionType) AnyURIAttribute
														.getInstance("http://www.example.com")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only
						new Object[] {
								NAME_HEXBINARY_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_HEXBINARY_BAG,
												HexBinaryAttribute.identifier),
										Arrays.asList((ExpressionType) HexBinaryAttribute
												.getInstance("0FB7")))),
								new EvaluationResult(HexBinaryAttribute
										.getInstance("0FB7")) },
						new Object[] {
								NAME_HEXBINARY_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_HEXBINARY_BAG,
												HexBinaryAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_HEXBINARY_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_HEXBINARY_BAG,
												HexBinaryAttribute.identifier),
										Arrays.asList(
												(ExpressionType) HexBinaryAttribute
														.getInstance("0FB7"),
												(ExpressionType) HexBinaryAttribute
														.getInstance("0FB7")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only
						new Object[] {
								NAME_BASE64BINARY_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_BASE64BINARY_BAG,
														Base64BinaryAttribute.identifier),
										Arrays.asList((ExpressionType) Base64BinaryAttribute
												.getInstance("RXhhbXBsZQ==")))),
								new EvaluationResult(Base64BinaryAttribute
										.getInstance("RXhhbXBsZQ==")) },
						new Object[] {
								NAME_BASE64BINARY_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_BASE64BINARY_BAG,
														Base64BinaryAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_BASE64BINARY_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_BASE64BINARY_BAG,
														Base64BinaryAttribute.identifier),
										Arrays.asList(
												(ExpressionType) Base64BinaryAttribute
														.getInstance("RXhhbXBsZQ=="),
												(ExpressionType) Base64BinaryAttribute
														.getInstance("RXhhbXBsZQ==")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only
						new Object[] {
								NAME_DAYTIMEDURATION_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_DAYTIMEDURATION_BAG,
														DayTimeDurationAttribute.identifier),
										Arrays.asList((ExpressionType) DayTimeDurationAttribute
												.getInstance("P1DT2H")))),
								new EvaluationResult(DayTimeDurationAttribute
										.getInstance("P1DT2H")) },
						new Object[] {
								NAME_DAYTIMEDURATION_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_DAYTIMEDURATION_BAG,
														DayTimeDurationAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_DAYTIMEDURATION_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_DAYTIMEDURATION_BAG,
														DayTimeDurationAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DayTimeDurationAttribute
														.getInstance("P1DT2H"),
												(ExpressionType) DayTimeDurationAttribute
														.getInstance("P1DT2H")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only
						new Object[] {
								NAME_YEARMONTHDURATION_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_YEARMONTHDURATION_BAG,
														YearMonthDurationAttribute.identifier),
										Arrays.asList((ExpressionType) YearMonthDurationAttribute
												.getInstance("P1Y2M")))),
								new EvaluationResult(YearMonthDurationAttribute
										.getInstance("P1Y2M")) },
						new Object[] {
								NAME_YEARMONTHDURATION_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_YEARMONTHDURATION_BAG,
														YearMonthDurationAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_YEARMONTHDURATION_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_YEARMONTHDURATION_BAG,
														YearMonthDurationAttribute.identifier),
										Arrays.asList(
												(ExpressionType) YearMonthDurationAttribute
														.getInstance("P1Y2M"),
												(ExpressionType) YearMonthDurationAttribute
														.getInstance("P1Y2M")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only
						new Object[] {
								NAME_X500NAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_X500NAME_BAG,
												X500NameAttribute.identifier),
										Arrays.asList((ExpressionType) X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US")))),
								new EvaluationResult(
										X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US")) },
						new Object[] {
								NAME_X500NAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_X500NAME_BAG,
												X500NameAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_X500NAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_X500NAME_BAG,
												X500NameAttribute.identifier),
										Arrays.asList(
												(ExpressionType) X500NameAttribute
														.getInstance("cn=John Smith, o=Medico Corp, c=US"),
												(ExpressionType) X500NameAttribute
														.getInstance("cn=John Smith, o=Medico Corp, c=US")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only
						new Object[] {
								NAME_RFC822NAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_RFC822NAME_BAG,
												RFC822NameAttribute.identifier),
										Arrays.asList((ExpressionType) RFC822NameAttribute
												.getInstance("Anderson@sun.com")))),
								new EvaluationResult(RFC822NameAttribute
										.getInstance("Anderson@sun.com")) },
						new Object[] {
								NAME_RFC822NAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_RFC822NAME_BAG,
												RFC822NameAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_RFC822NAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_RFC822NAME_BAG,
												RFC822NameAttribute.identifier),
										Arrays.asList(
												(ExpressionType) RFC822NameAttribute
														.getInstance("Anderson@sun.com"),
												(ExpressionType) RFC822NameAttribute
														.getInstance("Anderson@sun.com")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only
						new Object[] {
								NAME_IPADDRESS_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_IPADDRESS_BAG,
												IPAddressAttribute.identifier),
										Arrays.asList((ExpressionType) IPAddressAttribute
												.getInstance("192.168.1.10")))),
								new EvaluationResult(IPAddressAttribute
										.getInstance("192.168.1.10")) },
						new Object[] {
								NAME_IPADDRESS_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_IPADDRESS_BAG,
												IPAddressAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_IPADDRESS_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_IPADDRESS_BAG,
												IPAddressAttribute.identifier),
										Arrays.asList(
												(ExpressionType) IPAddressAttribute
														.getInstance("192.168.1.10"),
												(ExpressionType) IPAddressAttribute
														.getInstance("192.168.1.10")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only
						new Object[] {
								NAME_DNSNAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DNSNAME_BAG,
												DNSNameAttribute.identifier),
										Arrays.asList((ExpressionType) DNSNameAttribute
												.getInstance("example.com")))),
								new EvaluationResult(DNSNameAttribute
										.getInstance("example.com")) },
						new Object[] {
								NAME_DNSNAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(BagFunction
										.getBagInstance(NAME_DNSNAME_BAG,
												DNSNameAttribute.identifier),
										Collections
												.<ExpressionType> emptyList())),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },
						new Object[] {
								NAME_DNSNAME_ONE_AND_ONLY,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DNSNAME_BAG,
												DNSNameAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DNSNameAttribute
														.getInstance("example.com"),
												(ExpressionType) DNSNameAttribute
														.getInstance("example.com")))),
								new EvaluationResult(
										new Status(
												Arrays.asList(Status.STATUS_PROCESSING_ERROR))) },

						// urn:oasis:names:tc:xacml:1.0:function:string-bag-size
						new Object[] {
								NAME_STRING_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_STRING_BAG,
												StringAttribute.identifier),
										Arrays.asList(
												(ExpressionType) StringAttribute
														.getInstance("A test"),
												(ExpressionType) StringAttribute
														.getInstance("Another test"),
												(ExpressionType) StringAttribute
														.getInstance("Another test")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size
						new Object[] {
								NAME_BOOLEAN_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_BOOLEAN_BAG,
												BooleanAttribute.identifier),
										Arrays.asList(
												(ExpressionType) BooleanAttribute
														.getInstance(false),
												(ExpressionType) BooleanAttribute
														.getInstance(false),
												(ExpressionType) BooleanAttribute
														.getInstance(true)))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-bag-size
						new Object[] {
								NAME_INTEGER_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_INTEGER_BAG,
												IntegerAttribute.identifier),
										Arrays.asList(
												(ExpressionType) IntegerAttribute
														.getInstance("4"),
												(ExpressionType) IntegerAttribute
														.getInstance("4"),
												(ExpressionType) IntegerAttribute
														.getInstance("2")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:double-bag-size
						new Object[] {
								NAME_DOUBLE_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DOUBLE_BAG,
												DoubleAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DoubleAttribute
														.getInstance("3.14"),
												(ExpressionType) DoubleAttribute
														.getInstance("3.14"),
												(ExpressionType) DoubleAttribute
														.getInstance("-4.21")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:time-bag-size
						new Object[] {
								NAME_TIME_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_TIME_BAG,
												TimeAttribute.identifier),
										Arrays.asList(
												(ExpressionType) TimeAttribute
														.getInstance("09:30:15"),
												(ExpressionType) TimeAttribute
														.getInstance("09:30:15"),
												(ExpressionType) TimeAttribute
														.getInstance("17:18:19")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:date-bag-size
						new Object[] {
								NAME_DATE_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DATE_BAG,
												DateAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DateAttribute
														.getInstance("2002-09-24"),
												(ExpressionType) DateAttribute
														.getInstance("2002-09-24"),
												(ExpressionType) DateAttribute
														.getInstance("2003-10-25")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size
						new Object[] {
								NAME_DATETIME_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DATETIME_BAG,
												DateTimeAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DateTimeAttribute
														.getInstance("2002-09-24T09:30:15"),
												(ExpressionType) DateTimeAttribute
														.getInstance("2002-09-24T09:30:15"),
												(ExpressionType) DateTimeAttribute
														.getInstance("2003-10-25T17:18:19")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size
						new Object[] {
								NAME_ANYURI_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_ANYURI_BAG,
												AnyURIAttribute.identifier),
										Arrays.asList(
												(ExpressionType) AnyURIAttribute
														.getInstance("http://www.example.com"),
												(ExpressionType) AnyURIAttribute
														.getInstance("http://www.example.com"),
												(ExpressionType) AnyURIAttribute
														.getInstance("https://www.thalesgroup.com")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size
						new Object[] {
								NAME_HEXBINARY_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_HEXBINARY_BAG,
												HexBinaryAttribute.identifier),
										Arrays.asList(
												(ExpressionType) HexBinaryAttribute
														.getInstance("0FB7"),
												(ExpressionType) HexBinaryAttribute
														.getInstance("0FB7"),
												(ExpressionType) HexBinaryAttribute
														.getInstance("0FB8")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size
						new Object[] {
								NAME_BASE64BINARY_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_BASE64BINARY_BAG,
														Base64BinaryAttribute.identifier),
										Arrays.asList(
												(ExpressionType) Base64BinaryAttribute
														.getInstance("RXhhbXBsZQ=="),
												(ExpressionType) Base64BinaryAttribute
														.getInstance("RXhhbXBsZQ=="),
												(ExpressionType) Base64BinaryAttribute
														.getInstance("T3RoZXI=")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size
						new Object[] {
								NAME_DAYTIMEDURATION_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_DAYTIMEDURATION_BAG,
														DayTimeDurationAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DayTimeDurationAttribute
														.getInstance("P1DT2H"),
												(ExpressionType) DayTimeDurationAttribute
														.getInstance("P1DT2H"),
												(ExpressionType) DayTimeDurationAttribute
														.getInstance("-P1DT3H")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size
						new Object[] {
								NAME_YEARMONTHDURATION_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction
												.getBagInstance(
														NAME_YEARMONTHDURATION_BAG,
														YearMonthDurationAttribute.identifier),
										Arrays.asList(
												(ExpressionType) YearMonthDurationAttribute
														.getInstance("P1Y2M"),
												(ExpressionType) YearMonthDurationAttribute
														.getInstance("P1Y2M"),
												(ExpressionType) YearMonthDurationAttribute
														.getInstance("-P1Y3M")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size
						new Object[] {
								NAME_X500NAME_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_X500NAME_BAG,
												X500NameAttribute.identifier),
										Arrays.asList(
												(ExpressionType) X500NameAttribute
														.getInstance("cn=John Smith, o=Medico Corp, c=US"),
												(ExpressionType) X500NameAttribute
														.getInstance("cn=John Smith, o=Medico Corp, c=US"),
												(ExpressionType) X500NameAttribute
														.getInstance("cn=John Smith, o=Other Corp, c=US")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size
						new Object[] {
								NAME_RFC822NAME_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_RFC822NAME_BAG,
												RFC822NameAttribute.identifier),
										Arrays.asList(
												(ExpressionType) RFC822NameAttribute
														.getInstance("Anderson@sun.com"),
												(ExpressionType) RFC822NameAttribute
														.getInstance("Anderson@sun.com"),
												(ExpressionType) RFC822NameAttribute
														.getInstance("Smith@sun.com")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size
						new Object[] {
								NAME_IPADDRESS_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_IPADDRESS_BAG,
												IPAddressAttribute.identifier),
										Arrays.asList(
												(ExpressionType) IPAddressAttribute
														.getInstance("192.168.1.10"),
												(ExpressionType) IPAddressAttribute
														.getInstance("192.168.1.10"),
												(ExpressionType) IPAddressAttribute
														.getInstance("[1fff:0:a88:85a5::ac1f]")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size
						new Object[] {
								NAME_DNSNAME_BAG_SIZE,
								Arrays.asList(new Apply(
										BagFunction.getBagInstance(
												NAME_DNSNAME_BAG,
												DNSNameAttribute.identifier),
										Arrays.asList(
												(ExpressionType) DNSNameAttribute
														.getInstance("example.com"),
												(ExpressionType) DNSNameAttribute
														.getInstance("example.com"),
												(ExpressionType) DNSNameAttribute
														.getInstance("thalesgroup.com")))),
								new EvaluationResult(IntegerAttribute
										.getInstance("3")) },

						// urn:oasis:names:tc:xacml:1.0:function:string-is-in
						new Object[] {
								NAME_STRING_IS_IN,
								Arrays.asList(
										StringAttribute.getInstance("A test"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_STRING_BAG,
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("A test"),
														(ExpressionType) StringAttribute
																.getInstance("Another test")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_IS_IN,
								Arrays.asList(
										StringAttribute
												.getInstance("A different test"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_STRING_BAG,
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("A test"),
														(ExpressionType) StringAttribute
																.getInstance("Another test")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-is-in
						new Object[] {
								NAME_BOOLEAN_IS_IN,
								Arrays.asList(
										BooleanAttribute.getInstance(false),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_BOOLEAN_BAG,
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_IS_IN,
								Arrays.asList(
										BooleanAttribute.getInstance(true),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_BOOLEAN_BAG,
																BooleanAttribute.identifier),
												Arrays.asList(
														(ExpressionType) BooleanAttribute
																.getInstance(false),
														(ExpressionType) BooleanAttribute
																.getInstance(false)))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-is-in
						new Object[] {
								NAME_INTEGER_IS_IN,
								Arrays.asList(
										IntegerAttribute.getInstance("3"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_INTEGER_BAG,
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_INTEGER_IS_IN,
								Arrays.asList(
										IntegerAttribute.getInstance("5"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_INTEGER_BAG,
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("3")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-is-in
						new Object[] {
								NAME_DOUBLE_IS_IN,
								Arrays.asList(
										DoubleAttribute.getInstance("3.14"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DOUBLE_BAG,
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_IS_IN,
								Arrays.asList(
										DoubleAttribute.getInstance("-4.21"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DOUBLE_BAG,
																DoubleAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DoubleAttribute
																.getInstance("3.14"),
														(ExpressionType) DoubleAttribute
																.getInstance("3.14")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:time-is-in
						new Object[] {
								NAME_TIME_IS_IN,
								Arrays.asList(
										TimeAttribute.getInstance("09:30:15"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_TIME_BAG,
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_TIME_IS_IN,
								Arrays.asList(
										TimeAttribute.getInstance("17:18:19"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_TIME_BAG,
																TimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15"),
														(ExpressionType) TimeAttribute
																.getInstance("09:30:15")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:date-is-in
						new Object[] {
								NAME_DATE_IS_IN,
								Arrays.asList(
										DateAttribute.getInstance("2002-09-24"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DATE_BAG,
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATE_IS_IN,
								Arrays.asList(
										DateAttribute.getInstance("2003-10-25"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DATE_BAG,
																DateAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24"),
														(ExpressionType) DateAttribute
																.getInstance("2002-09-24")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in
						new Object[] {
								NAME_DATETIME_IS_IN,
								Arrays.asList(
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:15"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DATETIME_BAG,
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATETIME_IS_IN,
								Arrays.asList(
										DateTimeAttribute
												.getInstance("2003-10-25T17:18:19"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DATETIME_BAG,
																DateTimeAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(ExpressionType) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in
						new Object[] {
								NAME_ANYURI_IS_IN,
								Arrays.asList(
										AnyURIAttribute
												.getInstance("http://www.example.com"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_ANYURI_BAG,
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_IS_IN,
								Arrays.asList(
										AnyURIAttribute
												.getInstance("https://www.thalesgroup.com"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_ANYURI_BAG,
																AnyURIAttribute.identifier),
												Arrays.asList(
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(ExpressionType) AnyURIAttribute
																.getInstance("http://www.example.com")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in
						new Object[] {
								NAME_HEXBINARY_IS_IN,
								Arrays.asList(
										HexBinaryAttribute.getInstance("0FB7"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_HEXBINARY_BAG,
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_HEXBINARY_IS_IN,
								Arrays.asList(
										HexBinaryAttribute.getInstance("0FB8"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_HEXBINARY_BAG,
																HexBinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7"),
														(ExpressionType) HexBinaryAttribute
																.getInstance("0FB7")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in
						new Object[] {
								NAME_BASE64BINARY_IS_IN,
								Arrays.asList(
										Base64BinaryAttribute
												.getInstance("RXhhbXBsZQ=="),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_BASE64BINARY_BAG,
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ==")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BASE64BINARY_IS_IN,
								Arrays.asList(
										Base64BinaryAttribute
												.getInstance("T3RoZXI="),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_BASE64BINARY_BAG,
																Base64BinaryAttribute.identifier),
												Arrays.asList(
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(ExpressionType) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ==")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in
						new Object[] {
								NAME_DAYTIMEDURATION_IS_IN,
								Arrays.asList(
										DayTimeDurationAttribute
												.getInstance("P1DT2H"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DAYTIMEDURATION_BAG,
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DAYTIMEDURATION_IS_IN,
								Arrays.asList(
										DayTimeDurationAttribute
												.getInstance("-P1DT3H"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_DAYTIMEDURATION_BAG,
																DayTimeDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(ExpressionType) DayTimeDurationAttribute
																.getInstance("P1DT2H")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in
						new Object[] {
								NAME_YEARMONTHDURATION_IS_IN,
								Arrays.asList(
										YearMonthDurationAttribute
												.getInstance("P1Y2M"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_YEARMONTHDURATION_BAG,
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_YEARMONTHDURATION_IS_IN,
								Arrays.asList(
										YearMonthDurationAttribute
												.getInstance("-P1Y3M"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_YEARMONTHDURATION_BAG,
																YearMonthDurationAttribute.identifier),
												Arrays.asList(
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(ExpressionType) YearMonthDurationAttribute
																.getInstance("P1Y2M")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in
						new Object[] {
								NAME_X500NAME_IS_IN,
								Arrays.asList(
										X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_X500NAME_BAG,
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_IS_IN,
								Arrays.asList(
										X500NameAttribute
												.getInstance("cn=John Smith, o=Other Corp, c=US"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_X500NAME_BAG,
																X500NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(ExpressionType) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in
						new Object[] {
								NAME_RFC822NAME_IS_IN,
								Arrays.asList(
										RFC822NameAttribute
												.getInstance("Anderson@sun.com"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_RFC822NAME_BAG,
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_IS_IN,
								Arrays.asList(
										RFC822NameAttribute
												.getInstance("Smith@sun.com"),
										new Apply(
												BagFunction
														.getBagInstance(
																NAME_RFC822NAME_BAG,
																RFC822NameAttribute.identifier),
												Arrays.asList(
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(ExpressionType) RFC822NameAttribute
																.getInstance("Anderson@sun.com")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:string-bag
						new Object[] {
								NAME_STRING_BAG,
								Arrays.asList(StringAttribute
										.getInstance("A test"), StringAttribute
										.getInstance("Another test")),
								new EvaluationResult(
										new BagAttribute(
												StringAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) StringAttribute
																.getInstance("A test"),
														(AttributeValue) StringAttribute
																.getInstance("Another test")))) },
						new Object[] {
								NAME_STRING_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										StringAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-bag
						new Object[] {
								NAME_BOOLEAN_BAG,
								Arrays.asList(
										BooleanAttribute.getInstance(true),
										BooleanAttribute.getInstance(false)),
								new EvaluationResult(
										new BagAttribute(
												BooleanAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) BooleanAttribute
																.getInstance(true),
														(AttributeValue) BooleanAttribute
																.getInstance(false)))) },
						new Object[] {
								NAME_BOOLEAN_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										BooleanAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-bag
						new Object[] {
								NAME_INTEGER_BAG,
								Arrays.asList(
										IntegerAttribute.getInstance("1"),
										IntegerAttribute.getInstance("3")),
								new EvaluationResult(
										new BagAttribute(
												IntegerAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) IntegerAttribute
																.getInstance("1"),
														(AttributeValue) IntegerAttribute
																.getInstance("3")))) },
						new Object[] {
								NAME_INTEGER_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										IntegerAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:double-bag
						new Object[] {
								NAME_DOUBLE_BAG,
								Arrays.asList(
										DoubleAttribute.getInstance("3.14"),
										DoubleAttribute.getInstance("-4.21")),
								new EvaluationResult(
										new BagAttribute(
												DoubleAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) DoubleAttribute
																.getInstance("3.14"),
														(AttributeValue) DoubleAttribute
																.getInstance("-4.21")))) },
						new Object[] {
								NAME_DOUBLE_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										DoubleAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:time-bag
						new Object[] {
								NAME_TIME_BAG,
								Arrays.asList(
										TimeAttribute.getInstance("09:30:15"),
										TimeAttribute.getInstance("17:18:19")),
								new EvaluationResult(
										new BagAttribute(
												TimeAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) TimeAttribute
																.getInstance("09:30:15"),
														(AttributeValue) TimeAttribute
																.getInstance("17:18:19")))) },
						new Object[] {
								NAME_TIME_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										TimeAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:date-bag
						new Object[] {
								NAME_DATE_BAG,
								Arrays.asList(
										DateAttribute.getInstance("2002-09-24"),
										DateAttribute.getInstance("2003-10-25")),
								new EvaluationResult(
										new BagAttribute(
												DateAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) DateAttribute
																.getInstance("2002-09-24"),
														(AttributeValue) DateAttribute
																.getInstance("2003-10-25")))) },
						new Object[] {
								NAME_DATE_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										DateAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-bag
						new Object[] {
								NAME_DATETIME_BAG,
								Arrays.asList(
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:15"),
										DateTimeAttribute
												.getInstance("2003-10-25T17:18:19")),
								new EvaluationResult(
										new BagAttribute(
												DateTimeAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) DateTimeAttribute
																.getInstance("2002-09-24T09:30:15"),
														(AttributeValue) DateTimeAttribute
																.getInstance("2003-10-25T17:18:19")))) },
						new Object[] {
								NAME_DATETIME_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										DateTimeAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-bag
						new Object[] {
								NAME_ANYURI_BAG,
								Arrays.asList(
										AnyURIAttribute
												.getInstance("http://www.example.com"),
										AnyURIAttribute
												.getInstance("https://www.thalesgroup.com")),
								new EvaluationResult(
										new BagAttribute(
												AnyURIAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) AnyURIAttribute
																.getInstance("http://www.example.com"),
														(AttributeValue) AnyURIAttribute
																.getInstance("https://www.thalesgroup.com")))) },
						new Object[] {
								NAME_ANYURI_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										AnyURIAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag
						new Object[] {
								NAME_HEXBINARY_BAG,
								Arrays.asList(
										HexBinaryAttribute.getInstance("0FB7"),
										HexBinaryAttribute.getInstance("0FB8")),
								new EvaluationResult(
										new BagAttribute(
												HexBinaryAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB7"),
														(AttributeValue) HexBinaryAttribute
																.getInstance("0FB8")))) },
						new Object[] {
								NAME_HEXBINARY_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										HexBinaryAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag
						new Object[] {
								NAME_BASE64BINARY_BAG,
								Arrays.asList(Base64BinaryAttribute
										.getInstance("RXhhbXBsZQ=="),
										Base64BinaryAttribute
												.getInstance("T3RoZXI=")),
								new EvaluationResult(
										new BagAttribute(
												Base64BinaryAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) Base64BinaryAttribute
																.getInstance("RXhhbXBsZQ=="),
														(AttributeValue) Base64BinaryAttribute
																.getInstance("T3RoZXI=")))) },
						new Object[] {
								NAME_BASE64BINARY_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										Base64BinaryAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag
						new Object[] {
								NAME_DAYTIMEDURATION_BAG,
								Arrays.asList(DayTimeDurationAttribute
										.getInstance("P1DT2H"),
										DayTimeDurationAttribute
												.getInstance("-P1DT3H")),
								new EvaluationResult(
										new BagAttribute(
												DayTimeDurationAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("P1DT2H"),
														(AttributeValue) DayTimeDurationAttribute
																.getInstance("-P1DT3H")))) },
						new Object[] {
								NAME_DAYTIMEDURATION_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										DayTimeDurationAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag
						new Object[] {
								NAME_YEARMONTHDURATION_BAG,
								Arrays.asList(YearMonthDurationAttribute
										.getInstance("P1Y2M"),
										YearMonthDurationAttribute
												.getInstance("-P1Y3M")),
								new EvaluationResult(
										new BagAttribute(
												YearMonthDurationAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("P1Y2M"),
														(AttributeValue) YearMonthDurationAttribute
																.getInstance("-P1Y3M")))) },
						new Object[] {
								NAME_YEARMONTHDURATION_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(
										new BagAttribute(
												YearMonthDurationAttribute.identifierURI,
												Collections
														.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-bag
						new Object[] {
								NAME_X500NAME_BAG,
								Arrays.asList(
										X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US"),
										X500NameAttribute
												.getInstance("cn=John Smith, o=Other Corp, c=US")),
								new EvaluationResult(
										new BagAttribute(
												X500NameAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Medico Corp, c=US"),
														(AttributeValue) X500NameAttribute
																.getInstance("cn=John Smith, o=Other Corp, c=US")))) },
						new Object[] {
								NAME_X500NAME_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										X500NameAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag
						new Object[] {
								NAME_RFC822NAME_BAG,
								Arrays.asList(RFC822NameAttribute
										.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Smith@sun.com")),
								new EvaluationResult(
										new BagAttribute(
												RFC822NameAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) RFC822NameAttribute
																.getInstance("Anderson@sun.com"),
														(AttributeValue) RFC822NameAttribute
																.getInstance("Smith@sun.com")))) },
						new Object[] {
								NAME_RFC822NAME_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										RFC822NameAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag
						new Object[] {
								NAME_IPADDRESS_BAG,
								Arrays.asList(
										IPAddressAttribute
												.getInstance("192.168.1.10"),
										IPAddressAttribute
												.getInstance("[1fff:0:a88:85a5::ac1f]")),
								new EvaluationResult(
										new BagAttribute(
												IPAddressAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) IPAddressAttribute
																.getInstance("192.168.1.10"),
														(AttributeValue) IPAddressAttribute
																.getInstance("[1fff:0:a88:85a5::ac1f]")))) },
						new Object[] {
								NAME_IPADDRESS_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										IPAddressAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) },

						// urn:oasis:names:tc:xacml:2.0:function:dnsName-bag
						new Object[] {
								NAME_DNSNAME_BAG,
								Arrays.asList(DNSNameAttribute
										.getInstance("example.com"),
										DNSNameAttribute
												.getInstance("thalesgroup.com")),
								new EvaluationResult(
										new BagAttribute(
												DNSNameAttribute.identifierURI,
												Arrays.asList(
														(AttributeValue) DNSNameAttribute
																.getInstance("example.com"),
														(AttributeValue) DNSNameAttribute
																.getInstance("thalesgroup.com")))) },
						new Object[] {
								NAME_DNSNAME_BAG,
								Collections.<Evaluatable> emptyList(),
								new EvaluationResult(new BagAttribute(
										DNSNameAttribute.identifierURI,
										Collections
												.<AttributeValue> emptyList())) });
	}

	public BagFunctionsTest(String functionName, List<Evaluatable> inputs,
			EvaluationResult expectedResult) throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
