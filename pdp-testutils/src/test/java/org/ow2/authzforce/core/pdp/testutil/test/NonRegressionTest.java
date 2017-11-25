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
package org.ow2.authzforce.core.pdp.testutil.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.testutil.PdpTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-regression testing. Each test addresses a bug reported in the issue management system (e.g. Gitlab). There should be a folder for test data of each issue in folder:
 * src/test/resources/NonRegression.
 */
@RunWith(value = Parameterized.class)
public class NonRegressionTest extends PdpTest
{
	/**
	 * Name of root directory that contains test resources for each non-regression test
	 */
	public final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "classpath:NonRegression";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(NonRegressionTest.class);

	/**
	 * Initialize test parameters for each test
	 * 
	 * @return collection of test dataset
	 * @throws URISyntaxException
	 *             if {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} is not a valid location
	 * @throws IOException
	 *             if {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} location could not be accessed
	 */
	@Parameters(name = "{0}")
	public static Collection<Object[]> params() throws URISyntaxException, IOException
	{
		return PdpTest.params(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}

	/**
	 * 
	 * @param testDir
	 *            subdirectory of {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} where test data are located
	 */
	public NonRegressionTest(final String testDir)
	{
		super(testDir);
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.debug("Launching tests in '{}'", TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}
}