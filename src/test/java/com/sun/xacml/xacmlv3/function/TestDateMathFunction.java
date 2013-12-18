package com.sun.xacml.xacmlv3.function;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.cond.DateMathFunction;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

public class TestDateMathFunction {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestDateMathFunction.class);

	private static final String FUNCTION_NS = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String FUNCTION_NS_2 = "urn:oasis:names:tc:xacml:2.0:function:";
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

	@Before
	public void setUp() throws Exception {

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
		Date currentDate = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	    df.setTimeZone(TimeZone.getTimeZone("UTC"));
		DateTimeAttribute dateTimeAttr = new DateTimeAttribute(currentDate);
		// Duration of one day
		DayTimeDurationAttribute plusOneDayDuration = new DayTimeDurationAttribute(false, 1, 0, 0, 0, 0);
		DayTimeDurationAttribute plusOneHourDuration = new DayTimeDurationAttribute(false, 0, 1, 0, 0, 0);
		DayTimeDurationAttribute plusOneMinDuration = new DayTimeDurationAttribute(false, 0, 0, 1, 0, 0);
		DayTimeDurationAttribute plusOneSecondDuration = new DayTimeDurationAttribute(false, 0, 0, 0, 1, 0);
		DayTimeDurationAttribute plusOneNanoSecondDuration = new DayTimeDurationAttribute(false, 0, 0, 0, 0, 1);
		
		List goodInputs = new ArrayList(Arrays.asList(dateTimeAttr, plusOneDayDuration));
		EvaluationResult attr = testNameDateTimeAddDaytimeDurationFunction.evaluate(goodInputs, globalContext);
				
		LOGGER.info("Function: " + NAME_DATETIME_ADD_DAYTIMEDURATION + ": OK");
	}

}
