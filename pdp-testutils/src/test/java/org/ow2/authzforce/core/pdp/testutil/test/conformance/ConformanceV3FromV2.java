/*
 * Copyright 2012-2022 THALES.
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
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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
	public final static String POLICIES_DIRNAME_SUFFIX = "Policies";

	/**
	 * Suffix of filename of an AttributeProvider configuration. The actual filename is the concatenation of the test ID and this suffix.
	 */
	public static final String ATTRIBUTE_PROVIDER_FILENAME_SUFFIX = "AttributeProvider.xml";

	/**
	 * PDP Configuration file name
	 */
	public final static String PDP_CONF_FILENAME = "pdp.xml";

	/**
	 * PDP extensions schema
	 */
	public final static String PDP_EXTENSION_XSD_LOCATION = "classpath:pdp-ext.xsd";

	/**
	 * Spring-supported location to XML catalog (may be prefixed with classpath:, etc.)
	 */
	public final static String XML_CATALOG_LOCATION = "classpath:catalog.xml";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceV3FromV2.class);

	protected static void setUp(final String testRootDirectoryLocation)
	{
		LOGGER.debug("Launching conformance tests for features in directory: {}", testRootDirectoryLocation);
	}

	/**
	 * For each test folder {@code testRootDir}/{@code testSubDirectoryName}, it creates the test parameters: path to the test directory containing Request.xml, Response.xml, etc., and the ID of request filter to be applied (null means the default lax variant of the single-decision request preprocessor
	 * 
	 * @param testRootDir
	 *            path to root directory of all test data
	 * @param requestFilterId
	 *            PDP request filter ID to be used for the tests
	 * @return test data
	 */
	public static Collection<Object[]> getTestData(final String testRootDir, DirectoryStream.Filter<? super Path> fileFilter, final String requestFilterId) throws IOException
	{
		final Collection<Object[]> testData = new ArrayList<>();
		/*
		 * Each sub-directory of the root directory is data for a specific test. So we configure a test for each directory
		 */
		final Path testRootDirPath = Paths.get(testRootDir);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testRootDirPath, fileFilter))
		{
			for (final Path testDirPath: stream)
			{
				// specific test's resources directory location and request filter ID, used as parameters to test(...)
				testData.add(new Object[]{testDirPath, requestFilterId });
			}
		}

		return testData;
	}

	private final Path testDirectoryPath;

	private final boolean enableXPath;

	private final XmlnsFilteringParserFactory xacmlParserFactory;

	private final String reqFilterId;

	public ConformanceV3FromV2(final Path testDir, final boolean enableXPath, final String requestFilter)
	{
		this.testDirectoryPath = testDir;
		this.enableXPath = enableXPath;
		this.reqFilterId = requestFilter;
		this.xacmlParserFactory = XacmlJaxbParsingUtils.getXacmlParserFactory(enableXPath);
	}

	@Test
	public void test() throws Exception
	{
		LOGGER.debug("******************************");
		LOGGER.debug("Starting PDP test in directory: '{}'", testDirectoryPath);

		// Response file
		final XmlnsFilteringParser respUnmarshaller = xacmlParserFactory.getInstance();
		final Path expectedRespFilepath = testDirectoryPath.resolve(EXPECTED_RESPONSE_FILENAME_SUFFIX);
		// If no Response file, it is just a static policy or request syntax error check
		final Response expectedResponse;
		if (Files.exists(expectedRespFilepath))
		{
			expectedResponse = TestUtils.createResponse(expectedRespFilepath, respUnmarshaller);
		}
		else
		{
			expectedResponse = null;
			// Do nothing except logging -> request = null
			LOGGER.debug("Response file '{}' does not exist -> Static Policy/Request syntax error check", expectedRespFilepath);
		}

		// Request file
		final XmlnsFilteringParser reqUnmarshaller = xacmlParserFactory.getInstance();
		final Path reqFilepath =  testDirectoryPath.resolve(REQUEST_FILENAME_SUFFIX);
		// If no Request file, it is just a static policy syntax error check
		final Request request;
		if (Files.exists(reqFilepath))
		{
			try
			{
				request = TestUtils.createRequest(reqFilepath, reqUnmarshaller);
			}
			catch (final JAXBException e)
			{
				// we found a syntax error in request
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
		}
		else
		{
			request = null;
			// do nothing except logging -> request = null
			LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", reqFilepath);
		}

		/*
		 * Create PDP
		 */
		final PdpEngineConfiguration pdpEngineConf;
		final Path pdpConfFile = testDirectoryPath.resolve(PDP_CONF_FILENAME);
		if (Files.notExists(pdpConfFile))
		{
			/*
			 * Policies directory. If it exists, root Policy file is expected to be in there. This is the case for IIE*** conformance tests
			 */
			final Path policiesDir = testDirectoryPath.resolve(POLICIES_DIRNAME_SUFFIX);
			/*
			Attribute Provider config
			 */
			final Path attributeProviderConfFile = testDirectoryPath.resolve(ATTRIBUTE_PROVIDER_FILENAME_SUFFIX);
			final Optional<Path> optAttributeProviderConfFile = Files.isRegularFile(attributeProviderConfFile) ? Optional.of(attributeProviderConfFile) : Optional.empty();

			try
			{
				if (Files.isDirectory(policiesDir))
				{
					final Path rootPolicyFile = policiesDir.resolve(ROOT_POLICY_FILENAME_SUFFIX);
					pdpEngineConf = TestUtils.newPdpEngineConfiguration(TestUtils.getPolicyRef(rootPolicyFile), policiesDir, enableXPath, optAttributeProviderConfFile, this.reqFilterId, null);
				} else
				{
					final Path rootPolicyFile = testDirectoryPath.resolve(ROOT_POLICY_FILENAME_SUFFIX);
					pdpEngineConf = TestUtils.newPdpEngineConfiguration(rootPolicyFile, enableXPath, optAttributeProviderConfFile, this.reqFilterId, null);
				}
			} catch (final IllegalArgumentException e)
			{
				// we found syntax error in policy
				if (request == null)
				{
					// this is a policy syntax error check and we found the syntax error as
					// expected -> success
					LOGGER.debug("Successfully found syntax error as expected in policy(ies) with path: {}*", testDirectoryPath);
					return;
				}

				// Unexpected error
				throw e;
			}
		} else
		{
			/*
			 * PDP configuration filename found in test directory -> create PDP from it
			 */
			// final String pdpExtXsdLocation = testResourceLocationPrefix + PDP_EXTENSION_XSD_FILENAME;
			File pdpExtXsdFile = null;
			try
			{
				pdpExtXsdFile = ResourceUtils.getFile(PDP_EXTENSION_XSD_LOCATION);
			} catch (final FileNotFoundException e)
			{
				LOGGER.debug("No PDP extension configuration file '{}' found -> JAXB-bound PDP extensions not allowed.", PDP_EXTENSION_XSD_LOCATION);
			}

			try
			{
				/*
				 * Load the PDP configuration from the configuration, and optionally, the PDP extension XSD if this file exists, and the XML catalog required to resolve these extension XSDs
				 */
				pdpEngineConf = pdpExtXsdFile == null ? PdpEngineConfiguration.getInstance(pdpConfFile.toString())
						: PdpEngineConfiguration.getInstance(pdpConfFile.toString(), XML_CATALOG_LOCATION, PDP_EXTENSION_XSD_LOCATION);
			} catch (final IOException e)
			{
				throw new RuntimeException("Error parsing PDP configuration from file '" + pdpConfFile + "' with extension XSD '" + PDP_EXTENSION_XSD_LOCATION + "' and XML catalog file '"
						+ XML_CATALOG_LOCATION + "'", e);
			}
		}

		try (PdpEngineInoutAdapter<Request, Response> pdp = PdpEngineAdapters.newXacmlJaxbInoutAdapter(pdpEngineConf))
		{
			if (request == null)
			{
				// this is a policy syntax error check and we didn't found the syntax error as
				// expected
				Assert.fail("Failed to find syntax error as expected in policy(ies)  with path: " + testDirectoryPath + "*");
			}
			else if (expectedResponse == null)
			{
				/*
				 * No expected response, so it is not a PDP evaluation test, but request or policy syntax error check. We got here, so request and policy OK. This is unexpected.
				 */
				Assert.fail("Missing response file '" + expectedRespFilepath + "' or failed to find syntax error as expected in either request located at '" + reqFilepath
				        + "' or policy(ies) with path '" + testDirectoryPath + "*'");

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

				TestUtils.assertNormalizedEquals("Test failed for directory "+ testDirectoryPath, expectedResponse, actualResponse);
			}
		}
		catch (final IllegalArgumentException e)
		{
			// we found syntax error in policy
			if (request == null)
			{
				// this is a policy syntax error check and we found the syntax error as
				// expected -> success
				LOGGER.debug("Successfully found syntax error as expected in policy(ies) with path: {}*", testDirectoryPath);
				return;
			}

			// Unexpected error
			throw e;
		}
	}


/*	public static void main(String[] args) throws IOException
	{
		final String testsDir = "pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/optional/xml";
		final Path testsDirPath = Paths.get(testsDir);
		for(final Path testdir: Files.newDirectoryStream(testsDirPath)) {
			if(Files.isDirectory(testdir)) {
				for(final Path testFile: Files.newDirectoryStream(testdir)) {
					final String testFilename = testFile.getFileName().toString();
					String prefix = testFilename.substring(0, 6);
					Path newTestDirPath = testsDirPath.resolve(prefix);
					if(Files.notExists(newTestDirPath)) {
						System.out.println("Creating directory: " + newTestDirPath);
						Files.createDirectory(newTestDirPath);
					}
					String suffix = testFilename.substring(6);
					Path newFilepath = newTestDirPath.resolve(suffix);
					System.out.println("Moving file: " + testFile + " -> "+ newFilepath);
					Files.move(testFile, newFilepath);
				}
			}
		}
	}*/
}