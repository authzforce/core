/**
 * Copyright 2012-2020 THALES.
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
package org.ow2.authzforce.core.pdp.impl.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ow2.authzforce.core.pdp.impl.test.func.BagFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.DateTimeArithmeticFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.EqualityFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.HigherOrderFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.LogicalFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.NonNumericComparisonFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.NumericArithmeticFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.NumericComparisonFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.NumericConversionFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.RegExpBasedFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.SetFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.SpecialMatchFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.StringConversionFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.func.StringFunctionsTest;
import org.ow2.authzforce.core.pdp.impl.test.value.AnyURIAttributeTest;
import org.ow2.authzforce.core.pdp.impl.test.value.StandardJavaTypeToXacmlAttributeDatatypeConversionTest;
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
@SuiteClasses(value = { EqualityFunctionsTest.class, NumericArithmeticFunctionsTest.class, StringConversionFunctionsTest.class, NumericConversionFunctionsTest.class, LogicalFunctionsTest.class,
		NumericComparisonFunctionsTest.class, DateTimeArithmeticFunctionsTest.class, NonNumericComparisonFunctionsTest.class, StringFunctionsTest.class, BagFunctionsTest.class,
		SetFunctionsTest.class, HigherOrderFunctionsTest.class, RegExpBasedFunctionsTest.class, SpecialMatchFunctionsTest.class, StandardJavaTypeToXacmlAttributeDatatypeConversionTest.class })
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
