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
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.DateMathFunction;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

public class TestDateMathFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDateMathFunction.class);

	private static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";

	private static final EvaluationCtx globalContext = TestUtils
			.createContext(new Request());
	
	/**
     * Standard identifier for the dateTime-add-dayTimeDuration function.
     */
    public static final String NAME_DATETIME_ADD_DAYTIMEDURATION =
        FUNCTION_NS_3 + "dateTime-add-dayTimeDuration";

    /**
     * Standard identifier for the dateTime-subtract-dayTimeDuration function.
     */
    public static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION =
        FUNCTION_NS_3 + "dateTime-subtract-dayTimeDuration";

    /**
     * Standard identifier for the dateTime-add-yearMonthDuration function.
     */
    public static final String NAME_DATETIME_ADD_YEARMONTHDURATION =
        FUNCTION_NS_3 + "dateTime-add-yearMonthDuration";

    /**
     * Standard identifier for the dateTime-subtract-yearMonthDuration
     * function.
     */
    public static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION =
        FUNCTION_NS_3 + "dateTime-subtract-yearMonthDuration";

    /**
     * Standard identifier for the date-add-yearMonthDuration function.
     */
    public static final String NAME_DATE_ADD_YEARMONTHDURATION =
        FUNCTION_NS_3 + "date-add-yearMonthDuration";

    /**
     * Standard identifier for the date-subtract-yearMonthDuration function.
     */
    public static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION =
        FUNCTION_NS_3 + "date-subtract-yearMonthDuration";

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
		DateMathFunction testNameDateTimeAddDaytimeDurationFunction = new DateMathFunction(NAME_DATETIME_ADD_DAYTIMEDURATION);
		
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
			dateTimeAttr = new DateTimeAttribute(df.parse(df.format(currentDate)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Duration of one day
		DayTimeDurationAttribute plusOneDayDuration = new DayTimeDurationAttribute(false, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute plusOneHourDuration = new DayTimeDurationAttribute(false, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute plusOneMinDuration = new DayTimeDurationAttribute(false, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute plusOneSecondDuration = new DayTimeDurationAttribute(false, 0, 0, 0, 1, 0);
		
		DayTimeDurationAttribute minusOneDayDuration = new DayTimeDurationAttribute(true, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute minusOneHourDuration = new DayTimeDurationAttribute(true, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute minusOneMinDuration = new DayTimeDurationAttribute(true, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute minusOneSecondDuration = new DayTimeDurationAttribute(true, 0, 0, 0, 1, 0);
		
		/*
		 * Testing +1 day
		 */
		c.setTime(plusOneDay);
		c.add(Calendar.DATE, 1);
		plusOneDay = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneDay)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, plusOneDayDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing +1 hour
		 */
		c.setTime(plusOneHour);
		c.add(Calendar.HOUR, 1);
		plusOneHour = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneHour)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, plusOneHourDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing +1 minute
		 */
		c.setTime(plusOneMin);
		c.add(Calendar.MINUTE, 1);
		plusOneMin = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneMin)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, plusOneMinDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing +1 second
		 */
		c.setTime(plusOneSecond);
		c.add(Calendar.SECOND, 1);
		plusOneSecond = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneSecond)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, plusOneSecondDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing -1 day
		 */
		c.setTime(minusOneDay);
		c.add(Calendar.DATE, -1);
		plusOneDay = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneDay)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, minusOneDayDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing -1 hour
		 */
		c.setTime(minusOneHour);
		c.add(Calendar.HOUR, -1);
		plusOneHour = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneHour)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, minusOneHourDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing -1 minute
		 */
		c.setTime(minusOneMin);
		c.add(Calendar.MINUTE, -1);
		plusOneMin = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneMin)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, minusOneMinDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		/*
		 * Testing -1 second
		 */
		c.setTime(minusOneSecond);
		c.add(Calendar.SECOND, -1);
		plusOneSecond = c.getTime();
		try {
			dateTimeExpected = new DateTimeAttribute(df.parse(df.format(plusOneSecond)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		goodInputs = new ArrayList<AttributeValue>(Arrays.asList(dateTimeAttr, minusOneSecondDuration));
		result = (DateTimeAttribute) testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext).getAttributeValue();
		Assert.assertEquals(dateTimeExpected.encode(), result.encode());
		
		LOGGER.info("Function: " + NAME_DATETIME_ADD_DAYTIMEDURATION + ": OK");
	}
	@Test
	public final void NameDateTimeSubstractDaytimeDuration() {
		LOGGER.info("Testing function: " + NAME_DATETIME_SUBTRACT_DAYTIMEDURATION);
		Assert.fail("Test not implemented");
		LOGGER.info("Function: " + NAME_DATETIME_SUBTRACT_DAYTIMEDURATION + ": OK");
	}
	
	@Test
	public final void NameDateTimeAddYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATETIME_ADD_YEARMONTHDURATION);
		Assert.fail("Test not implemented");
		LOGGER.info("Function: " + NAME_DATETIME_ADD_YEARMONTHDURATION + ": OK");
	}

	@Test
	public final void NameDateTimeSubstractYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATETIME_SUBTRACT_YEARMONTHDURATION);
		Assert.fail("Test not implemented");
		LOGGER.info("Function: " + NAME_DATETIME_SUBTRACT_YEARMONTHDURATION + ": OK");
	}
	
	@Test
	public final void NameDateAddYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATE_ADD_YEARMONTHDURATION);
		Assert.fail("Test not implemented");
		LOGGER.info("Function: " + NAME_DATE_ADD_YEARMONTHDURATION + ": OK");
	}

	@Test
	public final void NameDateSubstractYearMonthDuration() {
		LOGGER.info("Testing function: " + NAME_DATE_SUBTRACT_YEARMONTHDURATION);
		Assert.fail("Test not implemented");
		LOGGER.info("Function: " + NAME_DATE_SUBTRACT_YEARMONTHDURATION + ": OK");
	}
}
