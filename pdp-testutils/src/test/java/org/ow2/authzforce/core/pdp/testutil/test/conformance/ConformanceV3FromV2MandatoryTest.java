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
package org.ow2.authzforce.core.pdp.testutil.test.conformance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

/**
 * XACML 3.0 conformance tests for mandatory features.
 * 
 * @see ConformanceV3FromV2
 */
public class ConformanceV3FromV2MandatoryTest extends ConformanceV3FromV2
{
	/**
	 * test root directory location, where each subdirectories contains a category of tests
	 */
	private final static String ROOT_DIRECTORY = "target/test-classes/conformance/xacml-3.0-from-2.0-ct/mandatory";

	@BeforeClass
	public static void setUp()
	{
		setUp(ROOT_DIRECTORY);
	}

	public ConformanceV3FromV2MandatoryTest(final Path testDir, final String requestFilterId)
	{
		super(testDir, false, requestFilterId);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws IOException
	{
		return getTestData(ROOT_DIRECTORY, Files::isDirectory,null);
	}
}