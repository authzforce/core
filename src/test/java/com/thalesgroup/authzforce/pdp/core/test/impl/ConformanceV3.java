package com.thalesgroup.authzforce.pdp.core.test.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.ResponseCtx;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * XACML 3.0 conformance tests published by OASIS
 */
@RunWith(value = Parameterized.class)
public class ConformanceV3 {

	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "conformance";

	/**
	 * directory name that states XACML version
	 */
	private final static String VERSION_DIRECTORY = "3";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConformanceV3.class);

	private static final int NB_TESTS = 32;
	
	private int numTest;

	public ConformanceV3(int numTest) {
		this.numTest = numTest;
	}

	@BeforeClass
	public static void setUp() throws Exception {
		LOGGER.info("Launching conformance tests");
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[NB_TESTS][1];
		for (int i = 0; i < NB_TESTS; i++) {
			data[i][0] = i+1;
		}
		return Arrays.asList(data);
	}

	@Test
	public void testConformanceTestA() throws Exception {

		String policyNumber;
		ResponseCtx response = null;
		Response expectedResponse = null;
		Request request = null;
		Map<String, String> results = new TreeMap<String, String>();

			if (numTest < 10) {
				policyNumber = "00" + numTest;
			} else if (9 < numTest && numTest < 100) {
				policyNumber = "0" + numTest;
			} else {
				policyNumber = Integer.toString(numTest);
			}

			LOGGER.debug("Conformance Test IIIA{} is started", policyNumber);

			request = TestUtils.createRequest(ROOT_DIRECTORY,
					VERSION_DIRECTORY, "IIIA" + policyNumber
							+ "Request.xacml3.xml");
			if (request != null) {
				LOGGER.debug("Request that is sent to the PDP :  "
						+ TestUtils.printRequest(request));
				Set<String> policies = new HashSet<String>();
				policies.add("IIIA" + policyNumber + "Policy.xacml3.xml");
				response = TestUtils.getPDPNewInstance(ROOT_DIRECTORY, VERSION_DIRECTORY, policies).evaluate(request);
				if (response != null) {
					expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY,
							VERSION_DIRECTORY, "IIIA" + policyNumber
									+ "Response.xacml3.xml");
					LOGGER.debug("Response that is received from the PDP :  "
							+ response.getEncoded());
					LOGGER.debug("Going to assert it");
					if (expectedResponse != null) {
						boolean assertion = TestUtils.match(response,
								expectedResponse);
						if (assertion) {
							LOGGER.info("Assertion SUCCESS for: IIIA"
									+ policyNumber);
							results.put(policyNumber, "SUCCESS");
						} else {
							LOGGER.error("Assertion FAILED for: IIIA"
									+ policyNumber);
						}
						assertTrue(assertion);
					} else {
						LOGGER.error("Assertion FAILED for: IIIA"
								+ policyNumber);
						assertTrue("Response read from file is Null", false);
					}
				} else {
					LOGGER.error("Assertion FAILED for: IIIA" + policyNumber);
					assertFalse("Response received PDP is Null", false);
				}
			} else {
				LOGGER.error("Assertion FAILED for: IIIA" + policyNumber);
				assertTrue("Request read from file is Null", false);
			}

			LOGGER.info("Conformance Test IIIA" + policyNumber + " is finished");
		for (String key : results.keySet()) {
			LOGGER.debug(key + ":" + results.get(key));
		}
	}
}