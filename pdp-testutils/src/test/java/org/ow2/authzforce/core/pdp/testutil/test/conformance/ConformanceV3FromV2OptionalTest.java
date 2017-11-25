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
package org.ow2.authzforce.core.pdp.testutil.test.conformance;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.impl.io.MultiDecisionXacmlJaxbRequestPreprocessor;

/**
 * XACML 3.0 conformance tests for optional features.
 * 
 * @see ConformanceV3FromV2
 */
public class ConformanceV3FromV2OptionalTest extends ConformanceV3FromV2
{
	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "classpath:conformance/xacml-3.0-from-2.0-ct/optional";

	private static enum TestParameters
	{
		// enum constant name gives the sub-directory with all test files
		// first param is the file prefix (before number) if different from enum constant name, then
		// the start number and end number corresponding to last files in the sub-folder
		IIA002("IIA", 2, 2), IIA022_23_FIXED_WITH_XPATH("IIA", 22, 23), IIF300_301_FIXED_WITH_XPATH("IIF", 300, 301), IIF310_FIXED_WITH_XPATH("IIF", 310, 310), IIIA030("IIIA", 30, 30), IIIA330("IIIA", 330, 330), IIIE302("IIIE", 302, 302, MultiDecisionXacmlJaxbRequestPreprocessor.LaxVariantFactory.ID), IIIF001("IIIF", 1, 1), IIIG001("IIIG", 1, 1), IIIG301("IIIG", 301, 302);

		private final String filenamePrefixBeforeNum;
		private final int startNum;
		private final int endNum;
		private final String requestFilterId;

		private TestParameters(int startNum, int endNum)
		{
			this(null, startNum, endNum);
		}

		private TestParameters(String filenamePrefix, int startNum, int endNum, String requestFilterId)
		{
			this.filenamePrefixBeforeNum = filenamePrefix == null ? this.name() : filenamePrefix;
			this.startNum = startNum;
			this.endNum = endNum;
			this.requestFilterId = requestFilterId;
		}

		private TestParameters(String filenamePrefix, int startNum, int endNum)
		{
			this(filenamePrefix, startNum, endNum, null);
		}
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		setUp(ROOT_DIRECTORY);
	}

	public ConformanceV3FromV2OptionalTest(String filePathPrefix, String requestFilterId)
	{
		super(filePathPrefix, true, requestFilterId);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data()
	{
		final Collection<Object[]> testData = new ArrayList<>();
		for (TestParameters testParams : TestParameters.values())
		{
			testData.addAll(getTestData(ROOT_DIRECTORY, testParams.name(), testParams.filenamePrefixBeforeNum, testParams.startNum, testParams.endNum, testParams.requestFilterId));
		}

		return testData;
	}
}