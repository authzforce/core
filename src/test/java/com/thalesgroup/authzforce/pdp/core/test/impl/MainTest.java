/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.thalesgroup.authzforce.pdp.core.test.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Romain Ferrari
 * 
 * class to use for the testSuite
 		ConformanceV3.class,
		BasicV3.class,
		BasicFunctionV3.class,
		BasicMultipleRequestV3.class
 */
@RunWith(Suite.class)
@SuiteClasses(value={
		ConformanceV3.class,
		BasicV3.class,
		BasicFunctionV3.class
})
public class MainTest {
	/**
	 * the logger we'll use for all messages
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(MainTest.class);
	
	@BeforeClass 
    public static void setUpClass() {      
        LOGGER.info("Beginning Tests");

    }

    @AfterClass public static void tearDownClass() { 
    	LOGGER.info("Finishing Tests");
    }
}
