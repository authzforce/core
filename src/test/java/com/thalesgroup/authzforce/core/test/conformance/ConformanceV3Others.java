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
package com.thalesgroup.authzforce.core.test.conformance;

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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.sun.xacml.PDP;
import com.thalesgroup.authzforce.core.test.utils.TestUtils;

/**
 * XACML 3.0 conformance tests published by OASIS
 */
@RunWith(value = Parameterized.class)
public class ConformanceV3Others
{

	/**
	 * Name of root directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "classpath:conformance/others";

	/**
	 * XACML policy filename
	 */
	public final static String POLICY_FILENAME = "policy.xml";

	/**
	 * XACML request filename
	 */
	public final static String REQUEST_FILENAME = "request.xml";

	/**
	 * Expected XACML response filename
	 */
	public final static String EXPECTED_RESPONSE_FILENAME = "response.xml";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceV3Others.class);

	final String testDirPath;

	/**
	 * 
	 * @param testDir
	 *            subdirectory of {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} where test data
	 *            are located
	 */
	public ConformanceV3Others(String testDir)
	{
		this.testDirPath = testDir;
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.info("Launching tests in '{}'", TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}

	@Parameters
	public static Collection<Object[]> params() throws URISyntaxException, IOException
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		/*
		 * Each sub-directory of the root directory is data for a specific test. So we configure a
		 * test for each directory
		 */
		final URL testRootDir = ResourceUtils.getURL(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
		final Path testRootPath = Paths.get(testRootDir.toURI());
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testRootPath))
		{
			for (Path path : stream)
			{
				if (Files.isDirectory(path))
				{
					// add the test directory path used as arg to constructor
					// ConformanceV3Others(String
					// testDir)
					testParams.add(new Object[] { TEST_RESOURCES_ROOT_DIRECTORY_LOCATION + "/" + path.getFileName().toString() });
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
	public void testConformance() throws Exception
	{
		final String testResourceLocationPrefix = testDirPath + "/";
		LOGGER.debug("Starting conformance test of directory '{}'", testResourceLocationPrefix);
		Request request = TestUtils.createRequest(testResourceLocationPrefix + REQUEST_FILENAME);
		LOGGER.debug("Request that is sent to the PDP :  " + TestUtils.printRequest(request));
		PDP pdp = TestUtils.getPDPNewInstance(testResourceLocationPrefix + POLICY_FILENAME);
		Response response = pdp.evaluate(request);
		Response expectedResponse = TestUtils.createResponse(testResourceLocationPrefix + EXPECTED_RESPONSE_FILENAME);
		LOGGER.debug("Response that is received from the PDP :  " + response);
		TestUtils.assertNormalizedEquals(testResourceLocationPrefix, expectedResponse, response);
		LOGGER.debug("Finished conformance test of directory '{}", testResourceLocationPrefix);
	}
}