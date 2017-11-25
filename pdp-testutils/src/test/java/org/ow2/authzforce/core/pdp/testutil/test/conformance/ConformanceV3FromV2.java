/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.testutil.test.conformance;

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
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XACML 3.0 conformance tests (published on OASIS xacml-comments mailing list). For tests testing validation of XACML policy syntax, the PDP is expected to reject the policy before receiving any
 * Request. For these tests, the Request.xml and Response.xml are absent to indicate to this test class, that an invalid policy syntax is expected.
 * <p>
 * For tests testing validation of XACML Request syntax, the PDP is expected to reject the request before evaluation. For these tests, the Response.xml is absent to indicate to this test class, that
 * an invalid Request syntax is expected.
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

	private final XmlnsFilteringParserFactory xacmlParserFactory;

	private final String reqFilter;

	public ConformanceV3FromV2(final String filePathPrefix, final boolean enableXPath, final String requestFilter)
	{
		this.testFilePathPrefix = filePathPrefix;
		this.enableXPath = enableXPath;
		this.reqFilter = requestFilter;
		this.xacmlParserFactory = XacmlJaxbParsingUtils.getXacmlParserFactory(enableXPath);
	}

	@Test
	public void testConformance() throws Exception
	{
		LOGGER.debug("Starting conformance test with files '{}*.xml'", testFilePathPrefix);

		final XmlnsFilteringParser respUnmarshaller = xacmlParserFactory.getInstance();
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

		final XmlnsFilteringParser reqUnmarshaller = xacmlParserFactory.getInstance();
		Request request = null;
		// if no Request file, it is just a static policy syntax error check
		final String reqFilepath = testFilePathPrefix + REQUEST_FILENAME_SUFFIX;
		try
		{
			request = TestUtils.createRequest(reqFilepath, reqUnmarshaller);
		}
		catch (final FileNotFoundException notFoundErr)
		{
			// do nothing except logging -> request = null
			LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", reqFilepath);
		}
		catch (final JAXBException e)
		{
			// we found syntax error in request
			if (expectedResponse == null)
			{
				// this is a Request syntax error check and we found the syntax error as
				// expected -> success
				LOGGER.debug("Successfully found syntax error as expected in Request located at: {}", reqFilepath);
				return;
			}

			// Unexpected error
			throw e;
		}

		final String rootPolicyFilepath = testFilePathPrefix + ROOT_POLICY_FILENAME_SUFFIX;
		// referenced policies if any
		final String refPoliciesDirLocation = testFilePathPrefix + REF_POLICIES_DIRNAME_SUFFIX;

		final String attributeProviderConfLocation = testFilePathPrefix + ATTRIBUTE_PROVIDER_FILENAME_SUFFIX;
		final PdpEngineConfiguration pdpEngineConf;
		try
		{
			pdpEngineConf = TestUtils.newPdpEngineConfiguration(rootPolicyFilepath, refPoliciesDirLocation, enableXPath, attributeProviderConfLocation, this.reqFilter, null);
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

		try (PdpEngineInoutAdapter<Request, Response> pdp = PdpEngineAdapters.newXacmlJaxbInoutAdapter(pdpEngineConf))
		{
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
				Assert.fail("Missing response file '" + expectedRespFilepath + "' or failed to find syntax error as expected in either request located at '" + reqFilepath + "' or policy located at '"
						+ rootPolicyFilepath + "'");

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
	}
}