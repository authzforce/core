/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ow2.authzforce.core.test.conformance.ConformanceV3FromV2;
import org.ow2.authzforce.core.test.conformance.ConformanceV3Others;
import org.ow2.authzforce.core.test.custom.TestApplyMarshalling;
import org.ow2.authzforce.core.test.func.BagFunctionsTest;
import org.ow2.authzforce.core.test.func.DateTimeArithmeticFunctionsTest;
import org.ow2.authzforce.core.test.func.EqualityFunctionsTest;
import org.ow2.authzforce.core.test.func.HigherOrderFunctionsTest;
import org.ow2.authzforce.core.test.func.LogicalFunctionsTest;
import org.ow2.authzforce.core.test.func.NonNumericComparisonFunctionsTest;
import org.ow2.authzforce.core.test.func.NumericArithmeticFunctionsTest;
import org.ow2.authzforce.core.test.func.NumericComparisonFunctionsTest;
import org.ow2.authzforce.core.test.func.NumericConversionFunctionsTest;
import org.ow2.authzforce.core.test.func.RegExpBasedFunctionsTest;
import org.ow2.authzforce.core.test.func.SetFunctionsTest;
import org.ow2.authzforce.core.test.func.SpecialMatchFunctionsTest;
import org.ow2.authzforce.core.test.func.StringConversionFunctionsTest;
import org.ow2.authzforce.core.test.func.StringFunctionsTest;
import org.ow2.authzforce.core.test.value.AnyURIAttributeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * class to use for the testSuite MatchTest.class, ConformanceV3FromV2.class, BasicV3_1.class, BasicV3_2.class, BasicV3_3.class, BasicV3_4.class,
 * BasicV3_5.class, BasicFunctionV3.class
 */
@RunWith(Suite.class)
@SuiteClasses(value = { AnyURIAttributeTest.class, EqualityFunctionsTest.class, NumericArithmeticFunctionsTest.class, StringConversionFunctionsTest.class,
		NumericConversionFunctionsTest.class, LogicalFunctionsTest.class, NumericComparisonFunctionsTest.class, DateTimeArithmeticFunctionsTest.class,
		NonNumericComparisonFunctionsTest.class, StringFunctionsTest.class, BagFunctionsTest.class, SetFunctionsTest.class, HigherOrderFunctionsTest.class,
		RegExpBasedFunctionsTest.class, SpecialMatchFunctionsTest.class, ConformanceV3FromV2.class, ConformanceV3Others.class, TestApplyMarshalling.class,
		NonRegression.class })
public class MainTest
{
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

	@BeforeClass
	public static void setUpClass()
	{
		LOGGER.debug("Beginning Tests");

	}

	@AfterClass
	public static void tearDownClass()
	{
		LOGGER.debug("Finishing Tests");
	}

}
