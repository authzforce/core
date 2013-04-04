package com.thalesgroup.authzforce.pdp.core.test.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Romain Ferrari
 *
 */
public class MainTest extends TestSuite{
	
	public static Test suite() throws Exception {
		TestSuite testSuite = new TestSuite();
        // conformance test for XACML 3.0
        testSuite.addTestSuite(ConformanceV3.class);
        testSuite.addTestSuite(BasicV3.class);

        return testSuite;
	}
}
