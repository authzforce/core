package com.thalesgroup.authzforce.pdp.core.test.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.cond.ArithmeticFunctionsTest;
import com.sun.xacml.cond.BagFunctionsTest;
import com.sun.xacml.cond.DateTimeArithmeticFunctionsTest;
import com.sun.xacml.cond.EqualityFunctionsTest;
import com.sun.xacml.cond.HigherOrderFunctionsTest;
import com.sun.xacml.cond.LogicalFunctionsTest;
import com.sun.xacml.cond.NonNumericComparisonFunctionsTest;
import com.sun.xacml.cond.NumericComparisonFunctionsTest;
import com.sun.xacml.cond.NumericConversionFunctionsTest;
import com.sun.xacml.cond.RegExpBasedFunctionsTest;
import com.sun.xacml.cond.SetFunctionsTest;
import com.sun.xacml.cond.SpecialMatchFunctionsTest;
import com.sun.xacml.cond.StringConversionFunctionsTest;
import com.sun.xacml.cond.StringFunctionsTest;
import com.sun.xacml.xacmlv3.TestMatchAlg;
import com.sun.xacml.xacmlv3.function.TestDateMathFunction;
import com.sun.xacml.xacmlv3.function.TestMatchFunction;
import com.sun.xacml.xacmlv3.function.TestStringFunction;

/**
 * 
 *         class to use for the testSuite MatchTest.class, ConformanceV3.class,
 *         BasicV3_1.class, BasicV3_2.class, BasicV3_3.class, BasicV3_4.class,
 *         BasicV3_5.class, BasicFunctionV3.class
 */
@RunWith(Suite.class)
@SuiteClasses(value = { EqualityFunctionsTest.class,
		ArithmeticFunctionsTest.class, StringConversionFunctionsTest.class,
		NumericConversionFunctionsTest.class, LogicalFunctionsTest.class,
		NumericComparisonFunctionsTest.class,
		DateTimeArithmeticFunctionsTest.class,
		NonNumericComparisonFunctionsTest.class, StringFunctionsTest.class,
		BagFunctionsTest.class, SetFunctionsTest.class,
		HigherOrderFunctionsTest.class, RegExpBasedFunctionsTest.class,
		SpecialMatchFunctionsTest.class, TestMatchAlg.class,
		TestMatchFunction.class, TestStringFunction.class,
		/* TestDateMathFunction.class, */ ConformanceV3.class, BasicV3_1.class,
		BasicV3_2.class, BasicV3_3.class, BasicV3_4.class, BasicV3_5.class,
		BasicFunctionV3.class/*, BasicMultipleRequestV3.class */ })
public class MainTest {
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MainTest.class);

	@BeforeClass
	public static void setUpClass() {
		LOGGER.info("Beginning Tests");

	}

	@AfterClass
	public static void tearDownClass() {
		LOGGER.info("Finishing Tests");
	}
}
