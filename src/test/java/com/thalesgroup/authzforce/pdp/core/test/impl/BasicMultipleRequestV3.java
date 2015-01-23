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
package com.thalesgroup.authzforce.pdp.core.test.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ctx.ResponseCtx;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * This would test multiple decision profile that is introduced with XACML 3.0.
 */
public class BasicMultipleRequestV3 {

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
			.getLogger(BasicMultipleRequestV3.class);

	/**
	 * The map of results
	 */
	private static Map<String, String> results = new TreeMap<String, String>();

	@BeforeClass
	public static void setUp() throws Exception {
		LOGGER.info("Launching multi requests tests");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		showResults();
	}

	@Test
	public void testBasicTest0001() throws Exception {

		String reqResNo;
		Set<String> policies = new HashSet<String>();
		policies.add("TestPolicy_0014.xml");
		// PDP pdp = getPDPNewInstance(policies);
		LOGGER.info("Basic Test 0014 is started");
		ResponseCtx response = null;
		Response expectedResponse = null;
		Request request = null;

		for (int i = 1; i < 3; i++) {
			if (i < 10) {
				reqResNo = "0" + i;
			} else {
				reqResNo = Integer.toString(i);
			}

			request = TestUtils.createRequest(ROOT_DIRECTORY,
					VERSION_DIRECTORY, "request_0014_" + reqResNo + ".xml");
			if (request != null) {
				LOGGER.debug("Request that is sent to the PDP :  "
						+ TestUtils.printRequest(request));
				response = TestUtils.getPDPNewInstance(ROOT_DIRECTORY, VERSION_DIRECTORY, policies).evaluate(request);
				if (response != null) {
					LOGGER.info("Response that is received from the PDP :  "
							+ response.getEncoded());
					expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY,
							VERSION_DIRECTORY, "response_0014_" + reqResNo
									+ ".xml");
					LOGGER.info("Response expected:  "
							+ TestUtils.printResponse(expectedResponse));
					if (expectedResponse != null) {
						boolean assertion = TestUtils.match(response,
								expectedResponse);
						if (assertion) {
							LOGGER.debug("Assertion SUCCESS for: IIIA"
									+ "response_0014_" + reqResNo);
							results.put("response_0014_" + reqResNo, "SUCCESS");
						} else {
							LOGGER.error("Assertion FAILED for: TestPolicy_0014.xml and response_0014_"
									+ reqResNo);
							results.put("response_0014_" + reqResNo, "FAILED");
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
			LOGGER.info("Basic Test 0014 is finished");
		}
	}

	private static void showResults() throws Exception {
		for (String key : results.keySet()) {
			LOGGER.info(key + ":" + results.get(key));
		}
	}
}