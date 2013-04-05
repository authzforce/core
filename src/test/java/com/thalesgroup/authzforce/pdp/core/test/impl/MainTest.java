package com.thalesgroup.authzforce.pdp.core.test.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Romain Ferrari
 * 
 */
public class MainTest extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite testSuite = new TestSuite();
		// conformance test for XACML 3.0
		testSuite.addTestSuite(ConformanceV3.class);
		// Basic test suite for xacml 3.0
		testSuite.addTestSuite(BasicV3.class);
		// Extended test suite for XACML 3.0 function (Not suppported yet)
//		 testSuite.addTestSuite(BasicFunctionV3.class);
		//Multiple Requests for XACML 3.0
//		testSuite.addTestSuite(BasicMultipleRequestV3.class);

		return testSuite;
	}
}
