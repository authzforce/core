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
package org.ow2.authzforce.core.pdp.testutil.test;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.testutil.XacmlXmlPdpTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Non-regression testing. Each test addresses a bug reported in the issue management system (e.g. Gitlab). There should be a folder for test data of each issue in folder:
 * src/test/resources/NonRegression.
 */
@RunWith(value = Parameterized.class)
public class NonRegressionTest extends XacmlXmlPdpTest
{
	/**
	 * Name of root directory that contains test resources for each non-regression test
	 */
	public final static Path TEST_RESOURCES_ROOT_DIRECTORY_PATH = Paths.get("target", "test-classes", "NonRegression").toAbsolutePath();

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(NonRegressionTest.class);

	/**
	 * Initialize test parameters for each test
	 * 
	 * @return collection of test dataset
	 * @throws IOException
	 *             if TEST_RESOURCES_ROOT_DIRECTORY_PATH location could not be accessed
	 */
	@Parameters(name = "{0}")
	public static Collection<Object[]> params() throws IOException
	{
		return XacmlXmlPdpTest.params(TEST_RESOURCES_ROOT_DIRECTORY_PATH);
	}

	/**
	 * 
	 * @param testDirPath
	 *            subdirectory of TEST_RESOURCES_ROOT_DIRECTORY_PATH where test data are located
	 */
	public NonRegressionTest(final Path testDirPath)
	{
		super(testDirPath);
	}

	@BeforeClass
	public static void setUp()
    {
		LOGGER.debug("Launching tests in '{}'", TEST_RESOURCES_ROOT_DIRECTORY_PATH);
	}
}