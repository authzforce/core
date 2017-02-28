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
package org.ow2.authzforce.core.pdp.testutil.test.conformance;

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
 * "Various" XACML 3.0 conformance tests that are not in the official set of conformance tests already addressed by {@link ConformanceV3FromV2Mandatory}.
 */
@RunWith(value = Parameterized.class)
public class ConformanceV3Others extends PdpTest
{

	/**
	 * Name of root directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "classpath:conformance/others";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceV3Others.class);

	/**
	 * 
	 * @param testDir
	 *            subdirectory of {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} where test data are located
	 */
	public ConformanceV3Others(final String testDir)
	{
		super(testDir);
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		LOGGER.debug("Launching tests in '{}'", TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> params() throws URISyntaxException, IOException
	{
		return PdpTest.params(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}
}