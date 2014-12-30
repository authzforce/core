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
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

@RunWith(Parameterized.class)
public class EqualityFunctionsTest extends GeneralFunctionTest {

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
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-equal
				new Object[] {
						NAME_STRING_EQUAL,
						Arrays.asList(StringAttribute.getInstance("Test"),
								StringAttribute.getInstance("Test")),
						EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_EQUAL,
								Arrays.asList(
										StringAttribute.getInstance("Test"),
										StringAttribute.getInstance("Toast")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_STRING_EQUAL,
								Arrays.asList(
										StringAttribute.getInstance("Test"),
										StringAttribute.getInstance("TEST")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case
						new Object[] {
								NAME_STRING_EQUAL_IGNORE_CASE,
								Arrays.asList(
										StringAttribute.getInstance("Test"),
										StringAttribute.getInstance("Test")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_STRING_EQUAL_IGNORE_CASE,
								Arrays.asList(
										StringAttribute.getInstance("Test"),
										StringAttribute.getInstance("Toast")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_STRING_EQUAL_IGNORE_CASE,
								Arrays.asList(
										StringAttribute.getInstance("Test"),
										StringAttribute.getInstance("TEST")),
								EvaluationResult.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:boolean-equal
						new Object[] {
								NAME_BOOLEAN_EQUAL,
								Arrays.asList(
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(false)),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BOOLEAN_EQUAL,
								Arrays.asList(
										BooleanAttribute.getInstance(false),
										BooleanAttribute.getInstance(true)),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-equal
						new Object[] {
								NAME_INTEGER_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("42"),
										IntegerAttribute.getInstance("42")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_INTEGER_EQUAL,
								Arrays.asList(
										IntegerAttribute.getInstance("42"),
										IntegerAttribute.getInstance("24")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:double-equal
						new Object[] {
								NAME_DOUBLE_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("42.543"),
										DoubleAttribute.getInstance("42.543")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DOUBLE_EQUAL,
								Arrays.asList(
										DoubleAttribute.getInstance("42.543"),
										DoubleAttribute.getInstance("24.2")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:date-equal
						new Object[] {
								NAME_DATE_EQUAL,
								Arrays.asList(
										DateAttribute.getInstance("2002-09-24"),
										DateAttribute.getInstance("2002-09-24")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATE_EQUAL,
								Arrays.asList(
										DateAttribute.getInstance("2002-09-24"),
										DateAttribute.getInstance("2002-04-29")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:time-equal
						new Object[] {
								NAME_TIME_EQUAL,
								Arrays.asList(
										TimeAttribute.getInstance("09:30:15"),
										TimeAttribute.getInstance("09:30:15")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_TIME_EQUAL,
								Arrays.asList(
										TimeAttribute.getInstance("09:30:15"),
										TimeAttribute.getInstance("09:30:19")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
						new Object[] {
								NAME_DATETIME_EQUAL,
								Arrays.asList(
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:15"),
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:15")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DATETIME_EQUAL,
								Arrays.asList(
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:15"),
										DateTimeAttribute
												.getInstance("2002-04-29T09:30:15")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_DATETIME_EQUAL,
								Arrays.asList(
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:15"),
										DateTimeAttribute
												.getInstance("2002-09-24T09:30:19")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
						new Object[] {
								NAME_DAYTIME_DURATION_EQUAL,
								Arrays.asList(DayTimeDurationAttribute
										.getInstance("P1DT2H"),
										DayTimeDurationAttribute
												.getInstance("P1DT2H")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_DAYTIME_DURATION_EQUAL,
								Arrays.asList(DayTimeDurationAttribute
										.getInstance("P1DT2H"),
										DayTimeDurationAttribute
												.getInstance("P1DT3H")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_DAYTIME_DURATION_EQUAL,
								Arrays.asList(DayTimeDurationAttribute
										.getInstance("P1DT2H"),
										DayTimeDurationAttribute
												.getInstance("PT26H")),
								EvaluationResult.getInstance(true) },

						// urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
						new Object[] {
								NAME_YEARMONTH_DURATION_EQUAL,
								Arrays.asList(YearMonthDurationAttribute
										.getInstance("P1Y2M"),
										YearMonthDurationAttribute
												.getInstance("P1Y2M")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_YEARMONTH_DURATION_EQUAL,
								Arrays.asList(YearMonthDurationAttribute
										.getInstance("P1Y2M"),
										YearMonthDurationAttribute
												.getInstance("P1Y3M")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_YEARMONTH_DURATION_EQUAL,
								Arrays.asList(YearMonthDurationAttribute
										.getInstance("P1Y2M"),
										YearMonthDurationAttribute
												.getInstance("P14M")),
								EvaluationResult.getInstance(true) },

						// urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
						new Object[] {
								NAME_ANYURI_EQUAL,
								Arrays.asList(
										AnyURIAttribute
												.getInstance("http://www.example.com"),
										AnyURIAttribute
												.getInstance("http://www.example.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANYURI_EQUAL,
								Arrays.asList(
										AnyURIAttribute
												.getInstance("http://www.example.com"),
										AnyURIAttribute
												.getInstance("https://www.example.com")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
						new Object[] {
								NAME_X500NAME_EQUAL,
								Arrays.asList(
										X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US"),
										X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_X500NAME_EQUAL,
								Arrays.asList(
										X500NameAttribute
												.getInstance("cn=John Smith, o=Medico Corp, c=US"),
										X500NameAttribute
												.getInstance("cn=John Smith, o=Other Corp, c=US")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
						new Object[] {
								NAME_RFC822NAME_EQUAL,
								Arrays.asList(
										RFC822NameAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@sun.com")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_EQUAL,
								Arrays.asList(RFC822NameAttribute
										.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Smith@sun.com")),
								EvaluationResult.getInstance(false) },
						new Object[] {
								NAME_RFC822NAME_EQUAL,
								Arrays.asList(
										RFC822NameAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("Anderson@SUN.COM")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_RFC822NAME_EQUAL,
								Arrays.asList(
										RFC822NameAttribute
												.getInstance("Anderson@sun.com"),
										RFC822NameAttribute
												.getInstance("ANDERSON@SUN.COM")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
						new Object[] {
								NAME_HEXBINARY_EQUAL,
								Arrays.asList(
										HexBinaryAttribute.getInstance("0FB7"),
										HexBinaryAttribute.getInstance("0FB7")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_HEXBINARY_EQUAL,
								Arrays.asList(
										HexBinaryAttribute.getInstance("0FB7"),
										HexBinaryAttribute.getInstance("0FB8")),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
						new Object[] {
								NAME_BASE64BINARY_EQUAL,
								Arrays.asList(Base64BinaryAttribute
										.getInstance("RXhhbXBsZQ=="),
										Base64BinaryAttribute
												.getInstance("RXhhbXBsZQ==")),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_BASE64BINARY_EQUAL,
								Arrays.asList(Base64BinaryAttribute
										.getInstance("RXhhbXBsZQ=="),
										Base64BinaryAttribute
												.getInstance("T3RoZXI=")),
								EvaluationResult.getInstance(false) });
	}

	public EqualityFunctionsTest(String functionName, List<Evaluatable> inputs,
			EvaluationResult expectedResult) throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
