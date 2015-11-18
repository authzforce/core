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
package org.ow2.authzforce.core.test.conformance;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.authzforce.core.test.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This would test multiple decision profile that is introduced with XACML 3.0 (support is partial and experimental).
 */
public class MultipleDecisionProfileV3
{

	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "classpath:conformance/experimental/MultipleDecisionProfile/";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MultipleDecisionProfileV3.class);

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.info("Launching multi requests tests");
	}

	@AfterClass
	public static void tearDown() throws Exception
	{
	}

	@Test
	public void testBasicTest0001() throws Exception
	{

		String reqResNo;
		String policyFilename = "TestPolicy_0014.xml";
		// PDP pdp = getPDPNewInstance(policies);
		LOGGER.info("Basic Test 0014 is started");
		Response response = null;
		Response expectedResponse = null;
		Request request = null;

		// FIXME: fix test 2, i.e. with MultiRequests (only test1, i.e. without MultiRequests
		// succeeds so far)
		for (int i = 1; i < 3; i++)
		{
			reqResNo = "0" + i;
			request = TestUtils.createRequest(ROOT_DIRECTORY + "request_0014_" + reqResNo + ".xml");
			LOGGER.debug("Request that is sent to the PDP :  {}", request);
			response = TestUtils.getPDPNewInstance(ROOT_DIRECTORY + policyFilename, null).evaluate(request);
			LOGGER.debug("Response that is received from the PDP :  {}", response);
			expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY + "response_0014_" + reqResNo + ".xml");
			LOGGER.debug("Response expected:  {}", expectedResponse);
			TestUtils.assertNormalizedEquals("Response to " + ROOT_DIRECTORY + "request_0014_" + reqResNo + ".xml", expectedResponse, response);
			LOGGER.debug("Basic Test 0014 is finished");
		}
	}
}