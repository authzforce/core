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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.test.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.PDP;

/**
 * XACML 3.0 conformance tests published by OASIS
 */
@RunWith(value = Parameterized.class)
public class ConformanceV3FromV2
{
	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "classpath:conformance/xacml-3.0-from-2.0-ct";

	private static enum TestParameters
	{
		// enum constant name gives the sub-directory with all test files
		// first param is the file prefix (before number) if different from enum constant name, then
		// the start number and end number corresponding to last files in the sub-folder
		IIB001("IIB", 1, 53), IIB300("IIB", 300, 301), IIC001("IIC", 1, 22), IIC024("IIC", 24, 53), IIC056("IIC", 56, 87), IIC090("IIC", 90, 91), IIC094("IIC",
				94, 97), IIC100("IIC", 100, 232), IIC300("IIC", 300, 303), IIC310("IIC", 310, 313), IIC320("IIC", 320, 323), IIC330("IIC", 330, 335), IIC340(
				"IIC", 340, 359), IIIA001("IIIA", 1, 32), IIIA301("IIIA", 301, 329), IIIA340("IIIA", 340, 340);

		private final String filenamePrefixBeforeNum;
		private final int startNum;
		private final int endNum;

		private TestParameters(int startNum, int endNum)
		{
			this(null, startNum, endNum);
		}

		private TestParameters(String filenamePrefix, int startNum, int endNum)
		{
			this.filenamePrefixBeforeNum = filenamePrefix == null ? this.name() : filenamePrefix;
			this.startNum = startNum;
			this.endNum = endNum;
		}
	}

	/**
	 * directory name that states XACML version
	 */
	// private final static String VERSION_DIRECTORY = "3";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceV3FromV2.class);

	private final String testFilePathPrefix;

	public ConformanceV3FromV2(String filePathPrefix)
	{
		this.testFilePathPrefix = filePathPrefix;
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.debug("Launching conformance tests in directory: {}", ROOT_DIRECTORY);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data()
	{
		final Collection<Object[]> testData = new ArrayList<>();
		for (TestParameters testParams : TestParameters.values())
		{
			for (int testNum = testParams.startNum; testNum <= testParams.endNum; testNum++)
			{
				final String paddedTestNumber;
				if (testNum < 10)
				{
					paddedTestNumber = "00" + testNum;
				} else if (testNum < 100)
				{
					paddedTestNumber = "0" + testNum;
				} else
				{
					paddedTestNumber = Integer.toString(testNum);
				}

				testData.add(new Object[] { ROOT_DIRECTORY + "/" + testParams.name() + "/" + testParams.filenamePrefixBeforeNum + paddedTestNumber });
			}
		}

		return testData;
	}

	@Test
	public void testConformance() throws Exception
	{
		LOGGER.debug("Starting conformance test with files '{}(Policy|Request|Response).xml'", testFilePathPrefix);
		Request request = null;
		// if no Request file, it is just a static policy syntax error check
		String expectedReqFilepath = testFilePathPrefix + "Request.xml";
		try
		{
			request = TestUtils.createRequest(expectedReqFilepath);
		} catch (FileNotFoundException notFoundErr)
		{
			// do nothing except logging -> request = null
			LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", expectedReqFilepath);
		}

		String policyFilepath = testFilePathPrefix + "Policy.xml";
		final PDP pdp;
		try
		{
			pdp = TestUtils.getPDPNewInstance(policyFilepath, null);
			if (request == null)
			{
				// this is a policy syntax error check and we didn't found the syntax error as
				// expected
				Assert.fail("Failed to find syntax error as expected in policy located at: " + policyFilepath);
			} else
			{
				// this is an evaluation test with request/response (not a policy syntax check)
				LOGGER.debug("Request that is sent to the PDP: {}", request);
				Response response = pdp.evaluate(request);
				Response expectedResponse = TestUtils.createResponse(testFilePathPrefix + "Response.xml");
				LOGGER.debug("Response that is received from the PDP :  {}", response);
				TestUtils.assertNormalizedEquals(testFilePathPrefix, expectedResponse, response);
			}
		} catch (IllegalArgumentException e)
		{
			// we found syntax error in policy
			if (request == null)
			{
				// this is a policy syntax error check and we found the syntax error as
				// expected -> success
				LOGGER.debug("Successfully found syntax error as expected in policy located at: {}", policyFilepath);
			} else
			{
				// Unexpected error
				throw e;
			}
		}

		LOGGER.debug("Finished conformance test with files '{}(Policy|Request|Response).xml'", testFilePathPrefix);
	}
}