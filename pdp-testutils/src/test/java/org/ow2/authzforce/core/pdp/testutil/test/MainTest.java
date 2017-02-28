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
package org.ow2.authzforce.core.pdp.testutil.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ow2.authzforce.core.pdp.impl.test.value.AnyURIAttributeTest;
import org.ow2.authzforce.core.pdp.testutil.test.pep.cxf.EmbeddedPdpBasedAuthzInterceptorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Main PDP core implementation test suite.
 * 
 * NB: {@link AnyURIAttributeTest} no longer useful and removed because we now refer to the definition of anyURI datatype given in XSD 1.1, which has the same value space as the string datatype. More
 * info in the XSD 1.1 datatypes document and SAXON documentation: http://www.saxonica.com/html/documentation9.4/changes/intro93/xsd11-93.html. Although XACML 3.0 still refers to XSD 1.0 and its
 * stricter definition of anyURI, we prefer to anticipate and use the definition from XSD 1.1 for XACML AttributeValues of datatype anyURI. However, this does not affect XACML schema validation of
 * Policy/PolicySet/Request documents, where the XSD 1.0 definition of anyURI still applies.
 */
@RunWith(Suite.class)
@SuiteClasses(value = { CustomPdpTest.class, TestPdpGetStaticApplicablePolicies.class, NonRegression.class, EmbeddedPdpBasedAuthzInterceptorTest.class })
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
