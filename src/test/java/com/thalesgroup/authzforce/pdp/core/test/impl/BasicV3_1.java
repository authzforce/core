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

import org.junit.AfterClass;
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
 * This XACML 3.0 basic policy test. This would test a basic policy, basic
 * policy with obligations and basic policy with advices.
 */
@RunWith(value = Parameterized.class)
public class BasicV3_1 {

	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "basic";

	/**
	 * directory name that states XACML version
	 */
	private final static String VERSION_DIRECTORY = "3";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BasicV3_1.class);
	/**
	 * The map of results
	 */
	private static Map<String, String> results = new TreeMap<String, String>();
	
	int numTest;
	
	public BasicV3_1(int numTest) {
		this.numTest = numTest;
	}

	@BeforeClass
	public static void setUp() throws Exception {
		LOGGER.info("Launching Basic tests v1");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		showResults();
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 }, { 7 } };
		   return Arrays.asList(data);
	}

	@Test
	public void testBasicTest0001() throws Exception {

		String reqResNo;
		Set<String> policies = new HashSet<String>();
		policies.add("TestPolicy_0001.xml");
		LOGGER.debug("Basic Test V3 v1 "+numTest+" is started");
		ResponseCtx response = null;
		Response expectedResponse = null;
		Request request = null;

			if (numTest < 10) {
				reqResNo = "0" + numTest;
			} else {
				reqResNo = Integer.toString(numTest);
			}

			request = TestUtils.createRequest(ROOT_DIRECTORY,
					VERSION_DIRECTORY, "request_0001_" + reqResNo + ".xml");
			if (request != null) {
				LOGGER.debug("Request that is sent to the PDP :  "
						+ TestUtils.printRequest(request));
				response = TestUtils.getPDPNewInstance(ROOT_DIRECTORY, VERSION_DIRECTORY, policies).evaluate(request);
				if (response != null) {
					LOGGER.debug("Response that is received from the PDP :  "
							+ response.getEncoded());
					expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY,
							VERSION_DIRECTORY, "response_0001_" + reqResNo
									+ ".xml");
					if (expectedResponse != null) {
						boolean assertion = TestUtils.match(response,
								expectedResponse);
						if (assertion) {
							LOGGER.debug("Assertion SUCCESS for: IIIA"
									+ "response_0001_" + reqResNo);
							results.put("response_0001_" + reqResNo, "SUCCESS");
						} else {
							LOGGER.error("Assertion FAILED for: TestPolicy_0001.xml and response_0001_"
									+ reqResNo);
							results.put("response_0001_" + reqResNo, "FAILED");
						}
						assertTrue(assertion);
					} else {
						assertTrue("Response read from file is Null", false);
					}
				} else {
					assertFalse("Response received PDP is Null", false);
				}
			} else {
				assertTrue("Request read from file is Null", false);
			}

			LOGGER.debug("Basic Test V3 v1 "+numTest+" is finished");
	}

	private static void showResults() throws Exception {
		for (String key : results.keySet()) {
			LOGGER.debug(key + ":" + results.get(key));
		}
	}
}