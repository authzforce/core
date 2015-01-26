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
package com.sun.xacml.xacmlv3.function;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.YearMonthDurationAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.DateMathFunction;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

public class TestDateMathFunction {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestDateMathFunction.class);

	private static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";

	private static final EvaluationCtx globalContext = TestUtils
			.createContext(new Request());

	/**
	 * Standard identifier for the dateTime-add-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_ADD_DAYTIMEDURATION = FUNCTION_NS_3
			+ "dateTime-add-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-subtract-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION = FUNCTION_NS_3
			+ "dateTime-subtract-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-add-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_ADD_YEARMONTHDURATION = FUNCTION_NS_3
			+ "dateTime-add-yearMonthDuration";

	/**
	 * Standard identifier for the dateTime-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION = FUNCTION_NS_3
			+ "dateTime-subtract-yearMonthDuration";

	/**
	 * Standard identifier for the date-add-yearMonthDuration function.
	 */
	public static final String NAME_DATE_ADD_YEARMONTHDURATION = FUNCTION_NS_3
			+ "date-add-yearMonthDuration";

	/**
	 * Standard identifier for the date-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION = FUNCTION_NS_3
			+ "date-subtract-yearMonthDuration";

	@BeforeClass
	public static void setUp() {

		LOGGER.info("Begining testing for DateMathFunctions");

		Set<String> testFunctions = new HashSet<String>();
		testFunctions.add(NAME_DATETIME_ADD_DAYTIMEDURATION);
		testFunctions.add(NAME_DATETIME_SUBTRACT_DAYTIMEDURATION);
		testFunctions.add(NAME_DATETIME_ADD_YEARMONTHDURATION);
		testFunctions.add(NAME_DATETIME_SUBTRACT_YEARMONTHDURATION);
		testFunctions.add(NAME_DATE_ADD_YEARMONTHDURATION);
		testFunctions.add(NAME_DATE_SUBTRACT_YEARMONTHDURATION);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Function to be tested");
			for (String functionToBeTested : testFunctions) {
				LOGGER.debug(functionToBeTested);
			}
		}
	}

	@Test
	public final void testNameDateTimeAddDaytimeDuration() {
		LOGGER.info("Testing function: " + NAME_DATETIME_ADD_DAYTIMEDURATION);
		DateMathFunction testNameDateTimeAddDaytimeDurationFunction = new DateMathFunction(
				NAME_DATETIME_ADD_DAYTIMEDURATION);

		List<AttributeValue> goodInputs;
		DateTimeAttribute result;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		Date currentDate = new Date();

		Date plusOneDay = new Date();
		Date plusOneHour = new Date();
		Date plusOneMin = new Date();
		Date plusOneSecond = new Date();
		Date minusOneDay = new Date();
		Date minusOneHour = new Date();
		Date minusOneMin = new Date();
		Date minusOneSecond = new Date();

		DateTimeAttribute dateTimeAttr = null;
		DateTimeAttribute dateTimeExpected = null;
		try {
			dateTimeAttr = new DateTimeAttribute(df.parse(df
					.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		DayTimeDurationAttribute plusOneDayDuration = new DayTimeDurationAttribute(
				false, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute plusOneHourDuration = new DayTimeDurationAttribute(
				false, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute plusOneMinDuration = new DayTimeDurationAttribute(
				false, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute plusOneSecondDuration = new DayTimeDurationAttribute(
				false, 0, 0, 0, 1, 0);

		DayTimeDurationAttribute minusOneDayDuration = new DayTimeDurationAttribute(
				true, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute minusOneHourDuration = new DayTimeDurationAttribute(
				true, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute minusOneMinDuration = new DayTimeDurationAttribute(
				true, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute minusOneSecondDuration = new DayTimeDurationAttribute(
				true, 0, 0, 0, 1, 0);

		/*
		 * Testing +1 day
		 */
		c.setTime(plusOneDay);
		c.add(Calendar.DATE, 1);
		plusOneDay = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneDay)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneDayDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 hour
		 */
		c.setTime(plusOneHour);
		c.add(Calendar.HOUR, 1);
		plusOneHour = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneHour)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneHourDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 minute
		 */
		c.setTime(plusOneMin);
		c.add(Calendar.MINUTE, 1);
		plusOneMin = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneMin)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneMinDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 second
		 */
		c.setTime(plusOneSecond);
		c.add(Calendar.SECOND, 1);
		plusOneSecond = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneSecond)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneSecondDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 day
		 */
		c.setTime(minusOneDay);
		c.add(Calendar.DATE, -1);
		plusOneDay = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneDay)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneDayDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 hour
		 */
		c.setTime(minusOneHour);
		c.add(Calendar.HOUR, -1);
		plusOneHour = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneHour)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneHourDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 minute
		 */
		c.setTime(minusOneMin);
		c.add(Calendar.MINUTE, -1);
		plusOneMin = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneMin)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneMinDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 second
		 */
		c.setTime(minusOneSecond);
		c.add(Calendar.SECOND, -1);
		plusOneSecond = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneSecond)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneSecondDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		LOGGER.debug("Testing borderline case. Like the new year");

		/*
		 * Testing -11 second
		 */
		DayTimeDurationAttribute yearChange = new DayTimeDurationAttribute(true, 0, 0, 0, 11, 0);
		try {
			c.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-01-01 00:00:01"));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		dateTimeAttr = new DateTimeAttribute(c.getTime());
		c.add(Calendar.SECOND, -11);
		dateTimeExpected = new DateTimeAttribute(c.getTime());
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				yearChange));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		LOGGER.info("Function: " + NAME_DATETIME_ADD_DAYTIMEDURATION + ": OK");
	}

	@Test
	public final void testNameDateTimeSubstractDaytimeDuration() {
		LOGGER.info("Testing function: "
				+ NAME_DATETIME_SUBTRACT_DAYTIMEDURATION);

		DateMathFunction testNameDateTimeSubtractDaytimeDurationFunction = new DateMathFunction(
				NAME_DATETIME_SUBTRACT_DAYTIMEDURATION);

		List<AttributeValue> goodInputs;
		DateTimeAttribute result;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		Date currentDate = new Date();

		Date plusOneDay = new Date();
		Date plusOneHour = new Date();
		Date plusOneMin = new Date();
		Date plusOneSecond = new Date();
		Date minusOneDay = new Date();
		Date minusOneHour = new Date();
		Date minusOneMin = new Date();
		Date minusOneSecond = new Date();

		DateTimeAttribute dateTimeAttr = null;
		DateTimeAttribute dateTimeExpected = null;
		try {
			dateTimeAttr = new DateTimeAttribute(df.parse(df
					.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		DayTimeDurationAttribute plusOneDayDuration = new DayTimeDurationAttribute(
				true, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute plusOneHourDuration = new DayTimeDurationAttribute(
				true, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute plusOneMinDuration = new DayTimeDurationAttribute(
				true, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute plusOneSecondDuration = new DayTimeDurationAttribute(
				true, 0, 0, 0, 1, 0);

		DayTimeDurationAttribute minusOneDayDuration = new DayTimeDurationAttribute(
				false, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute minusOneHourDuration = new DayTimeDurationAttribute(
				false, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute minusOneMinDuration = new DayTimeDurationAttribute(
				false, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute minusOneSecondDuration = new DayTimeDurationAttribute(
				false, 0, 0, 0, 1, 0);

		/*
		 * Testing +1 day
		 */
		c.setTime(plusOneDay);
		c.add(Calendar.DATE, -1);
		plusOneDay = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneDay)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneDayDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 hour
		 */
		c.setTime(plusOneHour);
		c.add(Calendar.HOUR, -1);
		plusOneHour = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneHour)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneHourDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 minute
		 */
		c.setTime(plusOneMin);
		c.add(Calendar.MINUTE, -1);
		plusOneMin = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneMin)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneMinDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 second
		 */
		c.setTime(plusOneSecond);
		c.add(Calendar.SECOND, -1);
		plusOneSecond = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneSecond)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneSecondDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 day
		 */
		c.setTime(minusOneDay);
		c.add(Calendar.DATE, 1);
		minusOneDay = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneDay)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneDayDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 hour
		 */
		c.setTime(minusOneHour);
		c.add(Calendar.HOUR, 1);
		minusOneHour = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneHour)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneHourDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 minute
		 */
		c.setTime(minusOneMin);
		c.add(Calendar.MINUTE, 1);
		minusOneMin = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneMin)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneMinDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 second
		 */
		c.setTime(minusOneSecond);
		c.add(Calendar.SECOND, 1);
		minusOneSecond = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneSecond)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneSecondDuration));
		result = (DateTimeAttribute) testNameDateTimeSubtractDaytimeDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		LOGGER.info("Function: " + NAME_DATETIME_SUBTRACT_DAYTIMEDURATION
				+ ": OK");
	}

	@Test
	public final void testNameDateTimeAddYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATETIME_ADD_YEARMONTHDURATION);
		DateMathFunction testNameDateTimeAddYearMonthDurationFunction = new DateMathFunction(
				NAME_DATETIME_ADD_YEARMONTHDURATION);

		List<AttributeValue> goodInputs;
		DateTimeAttribute result;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		Date currentDate = new Date();

		Date plusOneYear = new Date();
		Date plusOneMonth = new Date();
		Date minusOneYear = new Date();
		Date minusOneMonth = new Date();

		DateTimeAttribute dateTimeAttr = null;
		DateTimeAttribute dateTimeExpected = null;
		try {
			dateTimeAttr = new DateTimeAttribute(df.parse(df
					.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		YearMonthDurationAttribute plusOneYearAttribute = new YearMonthDurationAttribute(
				false, 1, 0);
		YearMonthDurationAttribute plusOneMonthAttribute = new YearMonthDurationAttribute(
				false, 0, 1);
		YearMonthDurationAttribute minusOneYearAttribute = new YearMonthDurationAttribute(
				true, 1, 0);
		YearMonthDurationAttribute minusOneMonthAttribute = new YearMonthDurationAttribute(
				true, 0, 1);

		/*
		 * Testing +1 Year
		 */
		c.setTime(plusOneYear);
		c.add(Calendar.YEAR, 1);
		plusOneYear = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneYearAttribute));
		result = (DateTimeAttribute) testNameDateTimeAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 Month
		 */
		c.setTime(plusOneMonth);
		c.add(Calendar.MONTH, 1);
		plusOneMonth = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneMonthAttribute));
		result = (DateTimeAttribute) testNameDateTimeAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 Year
		 */
		c.setTime(minusOneYear);
		c.add(Calendar.YEAR, -1);
		minusOneYear = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneYearAttribute));
		result = (DateTimeAttribute) testNameDateTimeAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 Month
		 */
		c.setTime(minusOneMonth);
		c.add(Calendar.MONTH, -1);
		minusOneMonth = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneMonthAttribute));
		result = (DateTimeAttribute) testNameDateTimeAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		LOGGER.info("Function: " + NAME_DATETIME_ADD_YEARMONTHDURATION + ": OK");
	}

	@Test
	public final void testNameDateTimeSubstractYearMonthDuration() {
		LOGGER.info("Testing function: "
				+ NAME_DATETIME_SUBTRACT_YEARMONTHDURATION);
		DateMathFunction testNameDateTimeSubtractYearMonthDurationFunction = new DateMathFunction(
				NAME_DATETIME_SUBTRACT_YEARMONTHDURATION);

		List<AttributeValue> goodInputs;
		DateTimeAttribute result;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		Date currentDate = new Date();

		Date plusOneYear = new Date();
		Date plusOneMonth = new Date();
		Date minusOneYear = new Date();
		Date minusOneMonth = new Date();

		DateTimeAttribute dateTimeAttr = null;
		DateTimeAttribute dateTimeExpected = null;
		try {
			dateTimeAttr = new DateTimeAttribute(df.parse(df
					.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		YearMonthDurationAttribute plusOneYearAttribute = new YearMonthDurationAttribute(
				false, 1, 0);
		YearMonthDurationAttribute plusOneMonthAttribute = new YearMonthDurationAttribute(
				false, 0, 1);
		YearMonthDurationAttribute minusOneYearAttribute = new YearMonthDurationAttribute(
				true, 1, 0);
		YearMonthDurationAttribute minusOneMonthAttribute = new YearMonthDurationAttribute(
				true, 0, 1);

		/*
		 * Testing -1 Year
		 */
		c.setTime(minusOneYear);
		c.add(Calendar.YEAR, -1);
		minusOneYear = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneYearAttribute));
		result = (DateTimeAttribute) testNameDateTimeSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing -1 Month
		 */
		c.setTime(minusOneMonth);
		c.add(Calendar.MONTH, -1);
		minusOneMonth = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(minusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				minusOneMonthAttribute));
		result = (DateTimeAttribute) testNameDateTimeSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 Year
		 */
		c.setTime(plusOneYear);
		c.add(Calendar.YEAR, 1);
		plusOneYear = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneYearAttribute));
		result = (DateTimeAttribute) testNameDateTimeSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		/*
		 * Testing +1 Month
		 */
		c.setTime(plusOneMonth);
		c.add(Calendar.MONTH, 1);
		plusOneMonth = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df
					.format(plusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr,
				plusOneMonthAttribute));
		result = (DateTimeAttribute) testNameDateTimeSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());

		LOGGER.info("Function: " + NAME_DATETIME_SUBTRACT_YEARMONTHDURATION
				+ ": OK");
	}

	@Test
	public final void testNameDateAddYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATE_ADD_YEARMONTHDURATION);
		DateMathFunction testNameDateAddYearMonthDurationFunction = new DateMathFunction(
				NAME_DATE_ADD_YEARMONTHDURATION);

		List<AttributeValue> goodInputs;
		DateAttribute result;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		Date currentDate = new Date();

		Date plusOneYear = new Date();
		Date plusOneMonth = new Date();
		Date minusOneYear = new Date();
		Date minusOneMonth = new Date();

		DateAttribute dateAttr = null;
		DateAttribute dateExpected = null;
		try {
			dateAttr = new DateAttribute(df.parse(df.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		YearMonthDurationAttribute plusOneYearAttribute = new YearMonthDurationAttribute(
				false, 1, 0);
		YearMonthDurationAttribute plusOneMonthAttribute = new YearMonthDurationAttribute(
				false, 0, 1);
		YearMonthDurationAttribute minusOneYearAttribute = new YearMonthDurationAttribute(
				true, 1, 0);
		YearMonthDurationAttribute minusOneMonthAttribute = new YearMonthDurationAttribute(
				true, 0, 1);

		/*
		 * Testing +1 Year
		 */
		c.setTime(plusOneYear);
		c.add(Calendar.YEAR, 1);
		plusOneYear = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(plusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				plusOneYearAttribute));
		result = (DateAttribute) testNameDateAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		/*
		 * Testing +1 Month
		 */
		c.setTime(plusOneMonth);
		c.add(Calendar.MONTH, 1);
		plusOneMonth = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(plusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				plusOneMonthAttribute));
		result = (DateAttribute) testNameDateAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		/*
		 * Testing -1 Year
		 */
		c.setTime(minusOneYear);
		c.add(Calendar.YEAR, -1);
		minusOneYear = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(minusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				minusOneYearAttribute));
		result = (DateAttribute) testNameDateAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		/*
		 * Testing -1 Month
		 */
		c.setTime(minusOneMonth);
		c.add(Calendar.MONTH, -1);
		minusOneMonth = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(minusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				minusOneMonthAttribute));
		result = (DateAttribute) testNameDateAddYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		LOGGER.info("Function: " + NAME_DATE_ADD_YEARMONTHDURATION + ": OK");
	}

	@Test
	public final void testNameDateSubstractYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATE_SUBTRACT_YEARMONTHDURATION);
		DateMathFunction testNameDateSubtractYearMonthDurationFunction = new DateMathFunction(
				NAME_DATE_SUBTRACT_YEARMONTHDURATION);

		List<AttributeValue> goodInputs;
		DateAttribute result;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		Date currentDate = new Date();

		Date plusOneYear = new Date();
		Date plusOneMonth = new Date();
		Date minusOneYear = new Date();
		Date minusOneMonth = new Date();

		DateAttribute dateAttr = null;
		DateAttribute dateExpected = null;
		try {
			dateAttr = new DateAttribute(df.parse(df.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		YearMonthDurationAttribute plusOneYearAttribute = new YearMonthDurationAttribute(
				false, 1, 0);
		YearMonthDurationAttribute plusOneMonthAttribute = new YearMonthDurationAttribute(
				false, 0, 1);
		YearMonthDurationAttribute minusOneYearAttribute = new YearMonthDurationAttribute(
				true, 1, 0);
		YearMonthDurationAttribute minusOneMonthAttribute = new YearMonthDurationAttribute(
				true, 0, 1);

		/*
		 * Testing -1 Year
		 */
		c.setTime(minusOneYear);
		c.add(Calendar.YEAR, -1);
		minusOneYear = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(minusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				minusOneYearAttribute));
		result = (DateAttribute) testNameDateSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		/*
		 * Testing -1 Month
		 */
		c.setTime(minusOneMonth);
		c.add(Calendar.MONTH, -1);
		minusOneMonth = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(minusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				minusOneMonthAttribute));
		result = (DateAttribute) testNameDateSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		/*
		 * Testing +1 Year
		 */
		c.setTime(plusOneYear);
		c.add(Calendar.YEAR, 1);
		plusOneYear = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(plusOneYear)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				plusOneYearAttribute));
		result = (DateAttribute) testNameDateSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		/*
		 * Testing +1 Month
		 */
		c.setTime(plusOneMonth);
		c.add(Calendar.MONTH, 1);
		plusOneMonth = c.getTime();
		try {
			dateExpected = new DateAttribute(df.parse(df.format(plusOneMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateAttr,
				plusOneMonthAttribute));
		result = (DateAttribute) testNameDateSubtractYearMonthDurationFunction
				.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateExpected.encode(), result.encode());

		LOGGER.info("Function: " + NAME_DATE_SUBTRACT_YEARMONTHDURATION
				+ ": OK");
	}
}
