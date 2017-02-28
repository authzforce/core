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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.impl.MultiDecisionRequestFilter;

/**
 * XACML 3.0 conformance tests for optional features.
 * 
 * @see ConformanceV3FromV2
 */
public class ConformanceV3FromV2Optional extends ConformanceV3FromV2
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
		IIA002("IIA", 2, 2), IIA022_23_FIXED_WITH_XPATH("IIA", 22, 23), IIF300_301_FIXED_WITH_XPATH("IIF", 300, 301), IIF310_FIXED_WITH_XPATH("IIF", 310, 310), IIIA030("IIIA", 30, 30), IIIA330("IIIA", 330, 330), IIIE302("IIIE", 302, 302, MultiDecisionRequestFilter.LaxFilterFactory.ID), IIIF001("IIIF", 1, 1), IIIG001("IIIG", 1, 1), IIIG301("IIIG", 301, 302);

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

	public ConformanceV3FromV2Optional(String filePathPrefix, String requestFilterId)
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