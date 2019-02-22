/**
 * Copyright 2012-2019 THALES.
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ow2.authzforce.core.pdp.testutil.test.conformance.ConformanceV3FromV2MandatoryTest;
import org.ow2.authzforce.core.pdp.testutil.test.conformance.ConformanceV3FromV2OptionalTest;
import org.ow2.authzforce.core.pdp.testutil.test.conformance.ConformanceV3OthersTest;
import org.ow2.authzforce.core.pdp.testutil.test.pep.cxf.EmbeddedPdpBasedAuthzInterceptorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Main PDP core implementation test suite.
 * 
 */
@RunWith(Suite.class)
@SuiteClasses(value = { ConformanceV3FromV2MandatoryTest.class, ConformanceV3FromV2OptionalTest.class, ConformanceV3OthersTest.class, PdpGetStaticApplicablePoliciesTest.class, CustomPdpTest.class,
		MongoDBRefPolicyProviderTest.class, EmbeddedPdpBasedAuthzInterceptorTest.class, NonRegressionTest.class })
public class MainTest
{
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

	@BeforeClass
	public static void setUpClass()
	{
		LOGGER.debug("Beginning Tests");

	}

	@AfterClass
	public static void tearDownClass()
	{
		LOGGER.debug("Finishing Tests");
	}

}
