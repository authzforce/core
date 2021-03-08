/*
 * Copyright 2012-2021 THALES.
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
package org.ow2.authzforce.core.pdp.testutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

/**
 * PDP test class. There should be a folder for test data of each issue. Each test folder is expected to be in one of these two configurations:
 * <p>
 * Configuration 1 for minimal/basic PDP configuration:
 * <ul>
 * <li>{@value #ROOT_POLICY_FILENAME}: root policy filename used by the PDP</li>
 * <li>{@value #POLICIES_DIR_NAME}: (optional) directory containing files of XACML Policy(Set) that can be referred to from root policy {@value #ROOT_POLICY_FILENAME} via Policy(Set)IdReference;
 * required only if there is any Policy(Set)IdReference in {@value #ROOT_POLICY_FILENAME} to resolve.</li>
 * <li>{@value #REQUEST_FILENAME}: (optional) XACML request file sent to the PDP for evaluation. If not present, the test is considered as a static policy test, i.e. test for invalid policy detection,
 * such as invalid syntax, circular reference, etc.</li>
 * <li>{@value #EXPECTED_RESPONSE_FILENAME}: (optional) expected XACML response from the PDP, to be compared with the actual response. Required only if {@value #REQUEST_FILENAME} is present.</li>
 * </ul>
 * </p>
 * <p>
 * Configuration 2 for advanced/custom PDP configuration:
 * <ul>
 * <li>{@value #PDP_CONF_FILENAME}: PDP configuration file</li>
 * <li>{@value #PDP_EXTENSION_XSD_LOCATION}: (optional) PDP extensions schema location, required iff custom PDP extensions are required</li>
 * <li>{@value #REQUEST_FILENAME}: (optional) XACML request file sent to the PDP for evaluation. If not present, the test is considered as a static policy test, i.e. test for invalid policy detection,
 * such as invalid syntax, circular reference, etc.</li>
 * <li>{@value #EXPECTED_RESPONSE_FILENAME}: (optional) expected XACML response from the PDP, to be compared with the actual response. Required only if {@value #REQUEST_FILENAME} is present.</li>
 * <li>{@value #POLICIES_DIR_NAME}: (optional) directory containing files of XACML Policy(Set) that can be referred to from root policy {@value #ROOT_POLICY_FILENAME} via Policy(Set)IdReference;
 * required only if there is any Policy(Set)IdReference in {@value #ROOT_POLICY_FILENAME} to resolve.</li>
 * <li>Policy files matching locations defined in {@value #PDP_CONF_FILENAME}.</li>
 * </ul>
 * </p>
 * 
 */
public abstract class XacmlXmlPdpTest
{

	/**
	 * PDP Configuration file name
	 */
	public final static String PDP_CONF_FILENAME = "pdp.xml";

	/**
	 * XACML policy filename used by default when no PDP configuration file found, i.e. no file named {@value #PDP_CONF_FILENAME} exists in the test directory
	 */
	public final static String ROOT_POLICY_FILENAME = "policy.xml";

	/**
	 * Name of directory containing files of XACML Policy(Set) that can be referred to from root policy {@value #ROOT_POLICY_FILENAME} via Policy(Set)IdReference
	 */
	public final static String POLICIES_DIR_NAME = "policies";

	/**
	 * XACML request filename
	 */
	public final static String REQUEST_FILENAME = "request.xml";

	/**
	 * Expected XACML response filename
	 */
	public final static String EXPECTED_RESPONSE_FILENAME = "response.xml";

	/**
	 * Spring-supported location to XML catalog (may be prefixed with classpath:, etc.)
	 */
	public final static String XML_CATALOG_LOCATION = "classpath:catalog.xml";

	/**
	 * PDP extensions schema
	 */
	public final static String PDP_EXTENSION_XSD_LOCATION = "classpath:pdp-ext.xsd";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(XacmlXmlPdpTest.class);

	private static final XmlnsFilteringParserFactory XACML_PARSER_FACTORY = XacmlJaxbParsingUtils.getXacmlParserFactory(false);

	private final String testDirPath;

	/**
	 * 
	 * @param testDir
	 *            directory where test data are located
	 */
	public XacmlXmlPdpTest(final String testDir)
	{
		this.testDirPath = testDir;
	}

	/**
	 * Initialize test parameters for each test. To be called by method with Parameters annotation in subclasses.
	 * 
	 * @param testResourcesRootDirectory
	 *            Spring-resolvable location (e.g. classpath:...) of root directory that contains test resources for each test
	 * 
	 * @return collection of test dataset
	 * @throws URISyntaxException
	 *             if <code>testResourcesRootDirectory</code> is no valid URI
	 * @throws IOException
	 *             if <code>testResourcesRootDirectory</code> is no valid file path
	 */
	public static Collection<Object[]> params(final String testResourcesRootDirectory) throws URISyntaxException, IOException
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		/*
		 * Each sub-directory of the root directory is data for a specific test. So we configure a test for each directory
		 */
		final URL testRootDir = ResourceUtils.getURL(testResourcesRootDirectory);
		final Path testRootPath = Paths.get(testRootDir.toURI());
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testRootPath))
		{
			for (final Path path : stream)
			{
				if (Files.isDirectory(path))
				{
					final Path lastPathElement = path.getFileName();
					if (lastPathElement == null)
					{
						// skip
						continue;
					}

					// specific test's resources directory location, used as parameter to PdpTest(String)
					testParams.add(new Object[] { testResourcesRootDirectory + "/" + lastPathElement.toString() });
				}
			}
		}
		catch (final DirectoryIteratorException ex)
		{
			// I/O error encountered during the iteration, the cause is an IOException
			throw ex.getCause();
		}

		return testParams;
	}

	@Test
	public void test() throws IllegalArgumentException, IOException, URISyntaxException, JAXBException
	{
		LOGGER.debug("******************************");
		LOGGER.debug("Starting PDP test of directory '{}'", testDirPath);
		final String testResourceLocationPrefix = testDirPath + "/";
		// Parse request
		final Request request;
		// if no Request file, it is just a static policy syntax error check
		final Path reqFilepath = Paths.get(testResourceLocationPrefix + REQUEST_FILENAME);
		final XmlnsFilteringParser unmarshaller = XACML_PARSER_FACTORY.getInstance();
		if (Files.exists(reqFilepath))
		{
			request = TestUtils.createRequest(reqFilepath, unmarshaller);
			LOGGER.debug("XACML Request sent to the PDP: {}", request);
		}
		else
		{
			request = null;
			// do nothing except logging -> request = null
			LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", reqFilepath);
		}

		/*
		 * Policies
		 * 
		 * If there is a "$TEST_DIR/$POLICIES_DIR_NAME" directory, then load all policies from there, including root policy from "$TEST_DIR/$POLICIES_DIR_NAME/$ROOT_POLICY_FILENAME" Else load only the
		 * root policy from "$TEST_DIR/$ROOT_POLICY_FILENAME"
		 */
		final Path policiesDir = Paths.get(testResourceLocationPrefix + POLICIES_DIR_NAME);
		final Optional<Path> optPoliciesDir;
		final Path rootPolicyFile;
		if (Files.isDirectory(policiesDir))
		{
			optPoliciesDir = Optional.of(policiesDir);
			rootPolicyFile = policiesDir.resolve(ROOT_POLICY_FILENAME);
		}
		else
		{
			optPoliciesDir = Optional.empty();
			rootPolicyFile = Paths.get(testResourceLocationPrefix + ROOT_POLICY_FILENAME);
		}

		/*
		 * Create PDP
		 */
		PdpEngineInoutAdapter<Request, Response> pdp = null;
		final Path pdpConfFile = Paths.get(testResourceLocationPrefix + PDP_CONF_FILENAME);
		try
		{
			final PdpEngineConfiguration pdpEngineConf;
			if (Files.notExists(pdpConfFile))
			{
				LOGGER.debug("No PDP configuration file found at location: '{}'. Using minimal PDP instead (returned by TestUtils.getPDPNewInstance(policy) ).", pdpConfFile);
				pdpEngineConf = optPoliciesDir.isPresent()
				        ? TestUtils.newPdpEngineConfiguration(TestUtils.getPolicyRef(rootPolicyFile), optPoliciesDir.get(), false, Optional.empty(), null, null)
				        : TestUtils.newPdpEngineConfiguration(rootPolicyFile, false, Optional.empty(), null, null);
			}
			else
			{
				/*
				 * PDP configuration filename found in test directory -> create PDP from it
				 */
				// final String pdpExtXsdLocation = testResourceLocationPrefix + PDP_EXTENSION_XSD_FILENAME;
				File pdpExtXsdFile = null;
				try
				{
					pdpExtXsdFile = ResourceUtils.getFile(PDP_EXTENSION_XSD_LOCATION);
				}
				catch (final FileNotFoundException e)
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
				}
				catch (final IOException e)
				{
					throw new RuntimeException("Error parsing PDP configuration from file '" + pdpConfFile + "' with extension XSD '" + PDP_EXTENSION_XSD_LOCATION + "' and XML catalog file '"
					        + XML_CATALOG_LOCATION + "'", e);
				}
			}

			pdp = PdpEngineAdapters.newXacmlJaxbInoutAdapter(pdpEngineConf);

			if (request == null)
			{
				/*
				 * This is a policy syntax error check and we didn't found the syntax error as expected
				 */
				Assert.fail("Failed to find syntax error as expected in policy(ies) located in directory: " + testDirPath);
			}
			else
			{
				// Parse expected response
				final Response expectedResponse = TestUtils.createResponse(Paths.get(testResourceLocationPrefix + EXPECTED_RESPONSE_FILENAME), unmarshaller);

				final Response response = pdp.evaluate(request, null);
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("XACML Response received from the PDP: {}", TestUtils.printResponse(response));
				}
				TestUtils.assertNormalizedEquals(testResourceLocationPrefix, expectedResponse, response);
				LOGGER.debug("Finished PDP test of directory '{}'", testDirPath);
			}
		}
		catch (final IllegalArgumentException e)
		{
			// we found syntax error in policy
			if (request == null)
			{
				// this is a policy syntax error check and we found the syntax error as
				// expected -> success
				LOGGER.debug("Successfully found syntax error as expected in policy(ies) located in directory: {}", testDirPath, e);
			}
			else
			{
				throw e;
			}
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