/**
 * Copyright (C) 2012-2015 Thales Services SAS.
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
package org.ow2.authzforce.core.test.custom;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.test.utils.PdpTest;

/**
 * AuthZForce-specific PDP tests
 *
 */
@RunWith(value = Parameterized.class)
public class CustomPdpTest extends PdpTest
{
	/**
	 * Name of root directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "classpath:custom";

	// private static final Logger LOGGER = LoggerFactory.getLogger(CustomPdpTest.class);

	/**
	 * 
	 * @param testDir
	 *            subdirectory of {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} where test data are located
	 */
	public CustomPdpTest(String testDir)
	{
		super(testDir);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> params() throws URISyntaxException, IOException
	{
		return PdpTest.params(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
	}

}
