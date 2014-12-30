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

import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.YearMonthDurationAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

@RunWith(Parameterized.class)
public class DateTimeArithmeticFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_DATETIME_ADD_DAYTIMEDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration";
	private static final String NAME_DATETIME_ADD_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration";
	private static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration";
	private static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration";
	private static final String NAME_DATE_ADD_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration";
	private static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION = "urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration
				new Object[] {
						NAME_DATETIME_ADD_DAYTIMEDURATION,
						Arrays.asList(DateTimeAttribute
								.getInstance("2002-09-24T09:30:15"),
								DayTimeDurationAttribute.getInstance("P1DT2H")),
						new EvaluationResult(DateTimeAttribute
								.getInstance("2002-09-25T11:30:15")) },
						new Object[] {
								NAME_DATETIME_ADD_DAYTIMEDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										DayTimeDurationAttribute
												.getInstance("-P1DT2H")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2002-09-23T07:30:15")) },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
						new Object[] {
								NAME_DATETIME_ADD_YEARMONTHDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										YearMonthDurationAttribute
												.getInstance("P1Y2M")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2003-11-24T09:30:15")) },
						new Object[] {
								NAME_DATETIME_ADD_YEARMONTHDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										YearMonthDurationAttribute
												.getInstance("-P1Y2M")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2001-07-24T09:30:15")) },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
						new Object[] {
								NAME_DATETIME_SUBTRACT_DAYTIMEDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										DayTimeDurationAttribute
												.getInstance("P1DT2H")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2002-09-23T07:30:15")) },
						new Object[] {
								NAME_DATETIME_SUBTRACT_DAYTIMEDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										DayTimeDurationAttribute
												.getInstance("-P1DT2H")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2002-09-25T11:30:15")) },

						// urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
						new Object[] {
								NAME_DATETIME_SUBTRACT_YEARMONTHDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										YearMonthDurationAttribute
												.getInstance("P1Y2M")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2001-07-24T09:30:15")) },
						new Object[] {
								NAME_DATETIME_SUBTRACT_YEARMONTHDURATION,
								Arrays.asList(DateTimeAttribute
										.getInstance("2002-09-24T09:30:15"),
										YearMonthDurationAttribute
												.getInstance("-P1Y2M")),
								new EvaluationResult(DateTimeAttribute
										.getInstance("2003-11-24T09:30:15")) },

						// urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
						new Object[] {
								NAME_DATE_ADD_YEARMONTHDURATION,
								Arrays.asList(DateAttribute
										.getInstance("2002-09-24"),
										YearMonthDurationAttribute
												.getInstance("P1Y2M")),
								new EvaluationResult(DateAttribute
										.getInstance("2003-11-24")) },
						new Object[] {
								NAME_DATE_ADD_YEARMONTHDURATION,
								Arrays.asList(DateAttribute
										.getInstance("2002-09-24"),
										YearMonthDurationAttribute
												.getInstance("-P1Y2M")),
								new EvaluationResult(DateAttribute
										.getInstance("2001-07-24")) },

						// urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
						new Object[] {
								NAME_DATE_SUBTRACT_YEARMONTHDURATION,
								Arrays.asList(DateAttribute
										.getInstance("2002-09-24"),
										YearMonthDurationAttribute
												.getInstance("P1Y2M")),
								new EvaluationResult(DateAttribute
										.getInstance("2001-07-24")) },
						new Object[] {
								NAME_DATE_SUBTRACT_YEARMONTHDURATION,
								Arrays.asList(DateAttribute
										.getInstance("2002-09-24"),
										YearMonthDurationAttribute
												.getInstance("-P1Y2M")),
								new EvaluationResult(DateAttribute
										.getInstance("2003-11-24")) });
	}

	public DateTimeArithmeticFunctionsTest(String functionName,
			List<ExpressionType> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
