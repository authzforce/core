/*
 * Copyright 2012-2024 THALES.
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
package org.ow2.authzforce.core.pdp.testutil.test.json;

import org.ow2.authzforce.core.pdp.io.xacml.json.SingleDecisionXacmlJsonRequestPreprocessor;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Miscellaneous tests
 */
public class XacmlJsonOtherTests extends XacmlJsonTest
{

	/**
	 * test root directory location, where each subdirectories contains a category of tests
	 */
	private final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "src/test/resources.json/other";

	@DataProvider
	public static Iterator<Object[]> getTestDirectories() throws URISyntaxException, IOException
	{
		return getTestData(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION, SingleDecisionXacmlJsonRequestPreprocessor.LaxVariantFactory.ID).iterator();
	}

}