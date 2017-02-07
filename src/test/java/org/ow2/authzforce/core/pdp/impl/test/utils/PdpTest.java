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
package org.ow2.authzforce.core.pdp.impl.test.utils;

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

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * PDP test class. There should be a folder for test data of each issue. Each test folder is expected to be in one of these two configurations:
 * <p>
 * Configuration 1 for minimal/basic PDP configuration:
 * <ul>
 * <li>{@value #POLICY_FILENAME}: root policy filename used by the PDP</li>
 * <li>{@value #REF_POLICIES_DIR_NAME}: (optional) directory containing files of XACML Policy(Set) that can be referred to from root policy {@value #POLICY_FILENAME} via Policy(Set)IdReference;
 * required only if there is any Policy(Set)IdReference in {@value #POLICY_FILENAME} to resolve.</li>
 * <li>{@value #REQUEST_FILENAME}: (optional) XACML request file sent to the PDP for evaluation. If not present, the test is considered as a static policy test, i.e. test for invalid policy detection,
 * such as invalid syntax, circular reference, etc.</li>
 * <li>{@value #EXPECTED_RESPONSE_FILENAME}: (optional) expected XACML response from the PDP, to be compared with the actual response. Required only if {@value #REQUEST_FILENAME} is present.</li>
 * </ul>
 * </p>
 * <p>
 * Configuration 2 for advanced/custom PDP configuration:
 * <ul>
 * <li>{@value #PDP_CONF_FILENAME}: PDP configuration file</li>
 * <li>{@value #PDP_EXTENSION_XSD}: (optional) PDP extensions schema, required iff custom PDP extensions are required</li>
 * <li>{@value #REQUEST_FILENAME}: (optional) XACML request file sent to the PDP for evaluation. If not present, the test is considered as a static policy test, i.e. test for invalid policy detection,
 * such as invalid syntax, circular reference, etc.</li>
 * <li>{@value #EXPECTED_RESPONSE_FILENAME}: (optional) expected XACML response from the PDP, to be compared with the actual response. Required only if {@value #REQUEST_FILENAME} is present.</li>
 * <li>{@value #REF_POLICIES_DIR_NAME}: (optional) directory containing files of XACML Policy(Set) that can be referred to from root policy {@value #POLICY_FILENAME} via Policy(Set)IdReference;
 * required only if there is any Policy(Set)IdReference in {@value #POLICY_FILENAME} to resolve.</li>
 * <li>Policy files matching locations defined in {@value #PDP_CONF_FILENAME}.</li>
 * </ul>
 * </p>
 * 
 */
public abstract class PdpTest
{

	/**
	 * PDP Configuration file name
	 */
	public final static String PDP_CONF_FILENAME = "pdp.xml";

	/**
	 * PDP extensions schema
	 */
	public final static String PDP_EXTENSION_XSD = "pdp-ext.xsd";

	/**
	 * XACML policy filename used by default when no PDP configuration file found, i.e. no file named {@value #PDP_CONF_FILENAME} exists in the test directory
	 */
	public final static String POLICY_FILENAME = "policy.xml";

	/**
	 * Name of directory containing files of XACML Policy(Set) that can be referred to from root policy {@value #POLICY_FILENAME} via Policy(Set)IdReference
	 */
	public final static String REF_POLICIES_DIR_NAME = "refPolicies";

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
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PdpTest.class);

	private static final XACMLParserFactory XACML_PARSER_FACTORY = JaxbXACMLUtils.getXACMLParserFactory(false);

	private final String testDirPath;

	/**
	 * 
	 * @param testDir
	 *            directory where test data are located
	 */
	public PdpTest(final String testDir)
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
	 * @throws IOException
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
			// I/O error encounted during the iteration, the cause is an IOException
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
		Request request = null;
		// if no Request file, it is just a static policy syntax error check
		final String reqFilepath = testResourceLocationPrefix + REQUEST_FILENAME;
		final NamespaceFilteringParser unmarshaller = XACML_PARSER_FACTORY.getInstance();
		try
		{
			request = TestUtils.createRequest(reqFilepath, unmarshaller);
			LOGGER.debug("XACML Request sent to the PDP: {}", request);
		}
		catch (final FileNotFoundException notFoundErr)
		{
			// do nothing except logging -> request = null
			LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", reqFilepath);
		}

		// Create PDP
		BasePdpEngine pdp = null;
		final String pdpConfLocation = testResourceLocationPrefix + PDP_CONF_FILENAME;
		File pdpConfFile = null;
		try
		{
			pdpConfFile = ResourceUtils.getFile(pdpConfLocation);
		}
		catch (final FileNotFoundException e)
		{
			LOGGER.debug("No PDP configuration file found at location: '{}'. Using minimal PDP instead (returned by TestUtils.getPDPNewInstance(policy) ).", pdpConfLocation);
		}

		try
		{
			if (pdpConfFile == null)
			{
				/*
				 * PDP configuration filename NOT found in test directory -> create minimal PDP using TestUtils.getPDPNewInstance(policy)
				 */
				pdp = TestUtils.getPDPNewInstance(testResourceLocationPrefix + POLICY_FILENAME, testResourceLocationPrefix + REF_POLICIES_DIR_NAME, false, null, null);

			}
			else
			{
				// PDP configuration filename found in test directory -> create PDP from it
				final String pdpExtXsdLocation = testResourceLocationPrefix + PDP_EXTENSION_XSD;
				File pdpExtXsdFile = null;
				try
				{
					pdpExtXsdFile = ResourceUtils.getFile(pdpExtXsdLocation);
				}
				catch (final FileNotFoundException e)
				{
					LOGGER.debug("No PDP extension configuration file '{}' found -> JAXB-bound PDP extensions not allowed.", pdpExtXsdLocation);
				}

				try
				{
					/*
					 * Load the PDP configuration from the configuration, and optionally, the PDP extension XSD if this file exists, and the XML catalog required to resolve these extension XSDs
					 */
					pdp = pdpExtXsdFile == null ? BasePdpEngine.getInstance(pdpConfLocation) : BasePdpEngine.getInstance(pdpConfFile, XML_CATALOG_LOCATION, pdpExtXsdLocation);
				}
				catch (final IOException e)
				{
					throw new RuntimeException("Error parsing PDP configuration from file '" + pdpConfLocation + "' with extension XSD '" + pdpExtXsdLocation + "' and XML catalog file '"
							+ XML_CATALOG_LOCATION + "'", e);
				}
			}

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
				final Response expectedResponse = TestUtils.createResponse(testResourceLocationPrefix + EXPECTED_RESPONSE_FILENAME, unmarshaller);

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