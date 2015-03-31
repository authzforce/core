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
package com.thalesgroup.authzforce.core.test.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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
import org.springframework.core.io.Resource;

import com.sun.xacml.PDP;
import com.sun.xacml.ctx.ResponseCtx;
import com.thalesgroup.authzforce.core.PdpConfigurationManager;
import com.thalesgroup.authzforce.core.PdpModelHandler;
import com.thalesgroup.authzforce.core.ResourceUtils;
import com.thalesgroup.authzforce.core.test.utils.TestUtils;

/**
 * Non-regression testing. Each test addresses a bug reported in the issue management system (e.g.
 * Gitlab). There should be a folder for test data of each issue in folder:
 * src/test/resources/NonRegression.
 */
@RunWith(value = Parameterized.class)
public class NonRegression
{	
	/**
	 * Name of root directory that contains test resources for each non-regression test
	 */
	public final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "classpath:NonRegression";

	/**
	 * PDP Configuration file name
	 */
	public final static String PDP_CONF_FILENAME = "pdp.xml";

	/**
	 * PDP extensions schema
	 */
	public final static String PDP_EXTENSION_XSD = "pdp-ext.xsd";

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
	private static final Logger LOGGER = LoggerFactory.getLogger(NonRegression.class);
	/**
	 * The map of results
	 */
	private static Map<String, String> results = new TreeMap<>();

	final String testDirName;

	/**
	 * 
	 * @param testDir
	 *            subdirectory of {@literal #ROOT_DIRECTORY} where test data are located
	 */
	public NonRegression(String testDir)
	{
		this.testDirName = testDir;
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.info("Launching tests in '{}'", TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}

	@AfterClass
	public static void tearDown() throws Exception
	{
		showResults();
	}

	/**
	 * Initialize test parameters for each test
	 * 
	 * @return collection of test dataset
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Parameters
	public static Collection<Object[]> params() throws URISyntaxException, IOException
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		/*
		 * Each sub-directory of the root directory is data for a specific test. So we configure a
		 * test for each directory
		 */
		final URL testRootDir = ResourceUtils.getResourceURL(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
		final Path testRootPath = Paths.get(testRootDir.toURI());
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testRootPath))
		{
			for (Path path : stream)
			{
				if (Files.isDirectory(path))
				{
					testParams.add(new Object[] { path.getFileName().toString() });
				}
			}
		} catch (DirectoryIteratorException ex)
		{
			// I/O error encounted during the iteration, the cause is an IOException
			throw ex.getCause();
		}

		return testParams;
	}

	@Test
	public void test() throws Exception
	{
		final String testResourceLocationPrefix = TEST_RESOURCES_ROOT_DIRECTORY_LOCATION + "/" + testDirName + "/";
		LOGGER.debug("Test '{}' is started", testResourceLocationPrefix);

		// Create PDP
		final PdpConfigurationManager pdpConfMgr;
		final String pdpConfLocation = testResourceLocationPrefix + PDP_CONF_FILENAME;
		final String pdpExtXsdLocation = testResourceLocationPrefix + PDP_EXTENSION_XSD;
		final Resource pdpExtXsdRes = ResourceUtils.getResource(pdpExtXsdLocation);
		try
		{
			/*
			 * Load the PDP configuration manager from the configuration, and optionally, the PDP
			 * extension XSD if this file exists, and the XML catalog required to resolve these
			 * extension XSDs
			 */
			pdpConfMgr = pdpExtXsdRes.exists() ? new PdpConfigurationManager(pdpConfLocation, XML_CATALOG_LOCATION, pdpExtXsdLocation)
					: new PdpConfigurationManager(pdpConfLocation);
		} catch (IOException | JAXBException e)
		{
			throw new RuntimeException("Error parsing PDP configuration from file '" + pdpConfLocation + "' with extension XSD '" + pdpExtXsdLocation
					+ "' and XML catalog file '" + XML_CATALOG_LOCATION + "'", e);
		}

		final PDP pdp = new PDP(pdpConfMgr.getDefaultPDPConfig());

		// Create request
		final URL reqFileURL = ResourceUtils.getResourceURL(testResourceLocationPrefix + REQUEST_FILENAME);
		final Unmarshaller xacmlUnmarshaller = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
		xacmlUnmarshaller.setSchema(PdpModelHandler.XACML_3_0_SCHEMA);
		final Request request = (Request) xacmlUnmarshaller.unmarshal(reqFileURL);
		if (request != null)
		{
			LOGGER.debug("XACML Request that is sent to the PDP: {}", TestUtils.printRequest(request));

			final ResponseCtx response = pdp.evaluate(request);
			if (response != null)
			{
				LOGGER.debug("XACML Response that is received from the PDP: {}", response.getEncoded());
				final URL expectedRespFileURL = ResourceUtils.getResourceURL(testResourceLocationPrefix + EXPECTED_RESPONSE_FILENAME);
				final Response expectedResponse = (Response) xacmlUnmarshaller.unmarshal(expectedRespFileURL);
				if (expectedResponse != null)
				{
					final boolean matched = TestUtils.match(response, expectedResponse);
					final String resultMsg = matched ? "SUCCESS" : "FAILED";
					LOGGER.info("Assertion {} for test: {}", new Object[] { resultMsg, testResourceLocationPrefix });
					results.put(testResourceLocationPrefix, resultMsg);
					assertTrue(matched);
				} else
				{
					fail("Expected XACML response read from location '" + expectedRespFileURL + "' is null");
				}
			} else
			{
				fail("Actual XACML response received from PDP is Null");
			}
		} else
		{
			fail("XACML Request read from location '" + reqFileURL + "' is null");
		}

		LOGGER.debug("Test '{}' is finished", testResourceLocationPrefix);
	}

	private static void showResults() throws Exception
	{
		for (String key : results.keySet())
		{
			LOGGER.debug(key + ":" + results.get(key));
		}
	}
}