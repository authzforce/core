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
package com.thalesgroup.authzforce.core.test.basic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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

import com.thalesgroup.authzforce.core.test.utils.TestUtils;

/**
 * This XACML 3.0 com.thalesgroup.authzforce.core.test.basic policy test. This would test a
 * com.thalesgroup.authzforce.core.test.basic policy, com.thalesgroup.authzforce.core.test.basic
 * policy with obligations and com.thalesgroup.authzforce.core.test.basic policy with advices.
 */
@RunWith(value = Parameterized.class)
public class BasicFunctionV3
{

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
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicFunctionV3.class);

	private static final int NB_TESTS = 3;

	/**
	 * The map of results
	 */
	private static Map<String, String> results = new TreeMap<String, String>();

	private final int numTest;

	public BasicFunctionV3(int numTest)
	{
		this.numTest = numTest;
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.info("Launching com.thalesgroup.authzforce.core.test.basic function tests");
	}

	@AfterClass
	public static void tearDown() throws Exception
	{
		showResults();
	}

	@Parameters
	public static Collection<Object[]> data()
	{
		Object[][] data = new Object[NB_TESTS][1];
		for (int i = 0; i < NB_TESTS; i++)
		{
			data[i][0] = i + 1;
		}
		return Arrays.asList(data);
	}

	@Test
	public void testBasicTest0006() throws Exception
	{

		String reqResNo;
		String policyFilename = "TestPolicy_0006.xml";
		// PDP pdp = getPDPNewInstance(policies);
		LOGGER.debug("Basic function v3 Test " + numTest + " is started");
		final Response response;
		final Response expectedResponse;
		final Request request;

		if (numTest < 10)
		{
			reqResNo = "0" + numTest;
		} else
		{
			reqResNo = Integer.toString(numTest);
		}

		request = TestUtils.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY, "request_0006_" + reqResNo + ".xml");
		LOGGER.debug("Request that is sent to the PDP :  " + TestUtils.printRequest(request));
		response = TestUtils.getPDPNewInstance(ROOT_DIRECTORY, VERSION_DIRECTORY, policyFilename).evaluate(request);
		LOGGER.debug("Response that is received from the PDP :  " + response);
		expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY, VERSION_DIRECTORY, "response_0006_" + reqResNo + ".xml");
		TestUtils.assertNormalizedEquals(ROOT_DIRECTORY + "/" + VERSION_DIRECTORY + "/request_0006_" + reqResNo, expectedResponse, response);
		LOGGER.debug("Basic function v3 Test " + numTest + " is finished");
	}

	private static void showResults() throws Exception
	{
		for (String key : results.keySet())
		{
			LOGGER.debug(key + ":" + results.get(key));
		}
	}
}