/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.test.conformance;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.test.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XACML 3.0 conformance tests (published on OASIS xacml-comments mailing list). For tests testing validation of XACML policy syntax, the PDP is expected to reject the policy before receiving any
 * Request. For these tests, the original Request.xml and Response.xml must be renamed to Request.xml.ignore and Response.xml.ignore to indicate to this test class, that an invalid policy syntax is
 * expected.
 * <p>
 * For tests testing validation of XACML Request syntax, the PDP is expected to reject the request before evaluation. For these tests, the original Policy.xml and Response.xml must be renamed to
 * Policy.xml.ignore and Response.xml.ignore to indicate to this test class, that an invalid Request syntax is expected.
 */
@RunWith(value = Parameterized.class)
public class ConformanceV3FromV2
{
	/**
	 * Suffix of filename of root XACML Policy(Set). The actual filename is the concatenation of the test ID and this suffix.
	 */
	public static final String ROOT_POLICY_FILENAME_SUFFIX = "Policy.xml";

	/**
	 * Suffix of filename of XACML request to send to the PDP. The actual filename is the concatenation of the test ID and this suffix.
	 */
	public static final String REQUEST_FILENAME_SUFFIX = "Request.xml";

	/**
	 * Suffix of filename of the expected XACML response from the PDP. The actual filename is the concatenation of the test ID and this suffix.
	 */
	public static final String EXPECTED_RESPONSE_FILENAME_SUFFIX = "Response.xml";
	/**
	 * Suffix of name of directory containing files of XACML Policy(Set) that can be referenced from root policy via Policy(Set)IdReference. The actual directory name is the concatenation of the test
	 * ID and this suffix.
	 */
	public final static String REF_POLICIES_DIRNAME_SUFFIX = "Repository";

	/**
	 * Suffix of filename of an AttributeProvider configuration. The actual filename is the concatenation of the test ID and this suffix.
	 */
	public static final String ATTRIBUTE_PROVIDER_FILENAME_SUFFIX = "AttributeProvider.xml";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceV3FromV2.class);

	protected static void setUp(final String testRootDirectoryLocation) throws Exception
	{
		LOGGER.debug("Launching conformance tests for features in directory: {}", testRootDirectoryLocation);
	}

	/**
	 * For each test folder {@code rootDirectoryPath}/{@code testSubDirectoryName}/{@code tesFilenamePrefixBeforeNum}NUM, where NUM is in range [ {@code startTestNum}, {@code endTestNum}] padded with
	 * leading zeros if needed to form a 3-digit number, it creates the following data: path prefix to the test files (Request.xml, Response.xml, etc.), and the input request filter ID
	 * 
	 * @param rootDirectoryPath
	 *            path to root directory of all test data
	 * @param testSubDirectoryName
	 *            name of a specific test subfolder
	 * @param testFilenamePrefixBeforeNum
	 *            prefix in test filename, before the test number part
	 * @param startTestNum
	 *            starting number of tests in the test subdirectory
	 * @param endTestNum
	 *            ending number of tests in the test subdirectory
	 * @param requestFilterId
	 *            PDP request filter ID to be used for the tests
	 * @return test data
	 */
	protected static Collection<? extends Object[]> getTestData(final String rootDirectoryPath, final String testSubDirectoryName, final String testFilenamePrefixBeforeNum, final int startTestNum,
			final int endTestNum, final String requestFilterId)
	{
		final Collection<Object[]> testData = new ArrayList<>();
		for (int testNum = startTestNum; testNum <= endTestNum; testNum++)
		{
			final String paddedTestNumber;
			if (testNum < 10)
			{
				paddedTestNumber = "00" + testNum;
			}
			else if (testNum < 100)
			{
				paddedTestNumber = "0" + testNum;
			}
			else
			{
				paddedTestNumber = Integer.toString(testNum);
			}

			testData.add(new Object[] { rootDirectoryPath + "/" + testSubDirectoryName + "/" + testFilenamePrefixBeforeNum + paddedTestNumber, requestFilterId });
		}

		return testData;
	}

	private final String testFilePathPrefix;

	private final boolean enableXPath;

	private final XACMLParserFactory xacmlParserFactory;

	private final String reqFilter;

	public ConformanceV3FromV2(final String filePathPrefix, final boolean enableXPath, final String requestFilter)
	{
		this.testFilePathPrefix = filePathPrefix;
		this.enableXPath = enableXPath;
		this.reqFilter = requestFilter;
		this.xacmlParserFactory = JaxbXACMLUtils.getXACMLParserFactory(enableXPath);
	}

	@Test
	public void testConformance() throws Exception
	{
		LOGGER.debug("Starting conformance test with files '{}*.xml'", testFilePathPrefix);

		final NamespaceFilteringParser respUnmarshaller = xacmlParserFactory.getInstance();
		Response expectedResponse = null;
		final String expectedRespFilepath = testFilePathPrefix + EXPECTED_RESPONSE_FILENAME_SUFFIX;
		try
		{
			expectedResponse = TestUtils.createResponse(expectedRespFilepath, respUnmarshaller);
		}
		catch (final FileNotFoundException notFoundErr)
		{
			// do nothing except logging -> request = null
			LOGGER.debug("Response file '{}' does not exist -> Static Policy/Request syntax error check", expectedRespFilepath);
		}

		final NamespaceFilteringParser reqUnmarshaller = xacmlParserFactory.getInstance();
		Request request = null;
		// if no Request file, it is just a static policy syntax error check
		final String expectedReqFilepath = testFilePathPrefix + REQUEST_FILENAME_SUFFIX;
		try
		{
			request = TestUtils.createRequest(expectedReqFilepath, reqUnmarshaller);
		}
		catch (final FileNotFoundException notFoundErr)
		{
			// do nothing except logging -> request = null
			LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", expectedReqFilepath);
		}
		catch (final JAXBException e)
		{
			// we found syntax error in request
			if (expectedResponse == null)
			{
				// this is a Request syntax error check and we found the syntax error as
				// expected -> success
				LOGGER.debug("Successfully found syntax error as expected in Request located at: {}", expectedReqFilepath);
				return;
			}

			// Unexpected error
			throw e;
		}

		final String rootPolicyFilepath = testFilePathPrefix + ROOT_POLICY_FILENAME_SUFFIX;
		// referenced policies if any
		final String refPoliciesDirLocation = testFilePathPrefix + REF_POLICIES_DIRNAME_SUFFIX;

		final String attributeProviderConfLocation = testFilePathPrefix + ATTRIBUTE_PROVIDER_FILENAME_SUFFIX;

		BasePdpEngine pdp = null;
		try
		{
			pdp = TestUtils.getPDPNewInstance(rootPolicyFilepath, refPoliciesDirLocation, enableXPath, attributeProviderConfLocation, this.reqFilter);
			if (request == null)
			{
				// this is a policy syntax error check and we didn't found the syntax error as
				// expected
				Assert.fail("Failed to find syntax error as expected in policy located at: " + rootPolicyFilepath);
			}
			else if (expectedResponse == null)
			{
				/*
				 * No expected response, so it is not a PDP evaluation test, but request or policy syntax error check. We got here, so request and policy OK. This is unexpected.
				 */
				Assert.fail("Missing response file '" + expectedRespFilepath + "' or failed to find syntax error as expected in either request located at '" + expectedReqFilepath
						+ "' or policy located at '" + rootPolicyFilepath + "'");

			}
			else
			{
				// this is an evaluation test with request/response (not a policy syntax check)
				LOGGER.debug("Request that is sent to the PDP: {}", request);
				final Response actualResponse = pdp.evaluate(request, reqUnmarshaller.getNamespacePrefixUriMap());
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("Response that is received from the PDP :  {}", TestUtils.printResponse(actualResponse));
				}

				TestUtils.assertNormalizedEquals(testFilePathPrefix, expectedResponse, actualResponse);
			}
		}
		catch (final IllegalArgumentException e)
		{
			// we found syntax error in policy
			if (request == null)
			{
				// this is a policy syntax error check and we found the syntax error as
				// expected -> success
				LOGGER.debug("Successfully found syntax error as expected in policy located at: {}", rootPolicyFilepath);
				return;
			}

			// Unexpected error
			throw e;
		}
		finally
		{
			if (pdp != null)
			{
				pdp.close();
			}
		}
	}
}