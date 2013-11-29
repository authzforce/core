package com.thalesgroup.authzforce.pdp.core.test.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.xacmlv3.MatchTest;

/**
 * @author Romain Ferrari
 * 
 * class to use for the testSuite
 		MatchTest.class,
 		ConformanceV3.class,
		BasicV3_1.class,
		BasicV3_2.class,
		BasicV3_3.class,
		BasicV3_4.class,
		BasicV3_5.class,
		BasicFunctionV3.class
 */
@RunWith(Suite.class)
@SuiteClasses(value={
		MatchTest.class,
		ConformanceV3.class,
		BasicV3_1.class,
		BasicV3_2.class,
		BasicV3_3.class,
		BasicV3_4.class,
		BasicV3_5.class,
		BasicFunctionV3.class
})
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

    @AfterClass public static void tearDownClass() { 
    	LOGGER.info("Finishing Tests");
    }
}
