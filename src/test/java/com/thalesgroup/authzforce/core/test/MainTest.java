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
package com.thalesgroup.authzforce.core.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thalesgroup.authzforce.core.test.basic.BasicFunctionV3;
import com.thalesgroup.authzforce.core.test.basic.BasicV3_1;
import com.thalesgroup.authzforce.core.test.basic.BasicV3_2;
import com.thalesgroup.authzforce.core.test.basic.BasicV3_3;
import com.thalesgroup.authzforce.core.test.basic.BasicV3_4;
import com.thalesgroup.authzforce.core.test.basic.BasicV3_5;
import com.thalesgroup.authzforce.core.test.conformance.ConformanceV3;
import com.thalesgroup.authzforce.core.test.custom.TestApply;
import com.thalesgroup.authzforce.core.test.custom.TestMatchAlg;
import com.thalesgroup.authzforce.core.test.func.BagFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.DateTimeArithmeticFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.EqualityFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.HigherOrderFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.LogicalFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.NonNumericComparisonFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.NumericArithmeticFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.NumericComparisonFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.NumericConversionFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.RegExpBasedFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.SetFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.SpecialMatchFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.StringConversionFunctionsTest;
import com.thalesgroup.authzforce.core.test.func.StringFunctionsTest;
import com.thalesgroup.authzforce.core.test.nonregression.NonRegression;

/**
 * 
 * class to use for the testSuite MatchTest.class, ConformanceV3.class, BasicV3_1.class,
 * BasicV3_2.class, BasicV3_3.class, BasicV3_4.class, BasicV3_5.class, BasicFunctionV3.class
 */
@RunWith(Suite.class)
@SuiteClasses(value = { EqualityFunctionsTest.class, NumericArithmeticFunctionsTest.class, StringConversionFunctionsTest.class, NumericConversionFunctionsTest.class, LogicalFunctionsTest.class, NumericComparisonFunctionsTest.class, DateTimeArithmeticFunctionsTest.class,
		NonNumericComparisonFunctionsTest.class, StringFunctionsTest.class, BagFunctionsTest.class, SetFunctionsTest.class, HigherOrderFunctionsTest.class, RegExpBasedFunctionsTest.class, SpecialMatchFunctionsTest.class, TestMatchAlg.class, ConformanceV3.class, BasicV3_1.class, BasicV3_2.class,
		BasicV3_3.class, BasicV3_4.class, BasicV3_5.class, BasicFunctionV3.class, TestApply.class, NonRegression.class /*
																														 * ,
																														 * BasicMultipleRequestV3
																														 * .
																														 * class
																														 */})
public class MainTest
{
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

	@BeforeClass
	public static void setUpClass()
	{
		LOGGER.info("Beginning Tests");

	}

	@AfterClass
	public static void tearDownClass()
	{
		LOGGER.info("Finishing Tests");
	}
}
