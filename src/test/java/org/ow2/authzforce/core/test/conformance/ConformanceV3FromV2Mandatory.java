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
package org.ow2.authzforce.core.test.conformance;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

/**
 * XACML 3.0 conformance tests for mandatory features.
 * 
 * @see ConformanceV3FromV2
 */
public class ConformanceV3FromV2Mandatory extends ConformanceV3FromV2
{
	/**
	 * test root directory location, where each subdirectories contains a category of tests
	 */
	private final static String ROOT_DIRECTORY = "classpath:conformance/xacml-3.0-from-2.0-ct/mandatory";

	private static enum TestParameters
	{
		// enum constant name gives the sub-directory with all test files
		// first param is the file prefix (before number) if different from enum constant name, then
		// the start number and end number corresponding to last files in the sub-folder
		IIA001("IIA", 1, 1), IIA003("IIA", 3, 9), IIA011("IIA", 11, 11), IIA013("IIA", 13, 15), IIA016_FIXED("IIA", 16, 16), IIA017("IIA", 17, 17), IIA018_FIXED(
				"IIA", 18, 18), IIA019("IIA", 19, 19), IIA020_FIXED("IIA", 20, 20), IIA021("IIA", 21, 21), IIA022_23_FIXED_NO_XPATH("IIA", 22, 23), IIB001(
				"IIB", 1, 53), IIB300("IIB", 300, 301), IIC001("IIC", 1, 22), IIC024("IIC", 24, 53), IIC056("IIC", 56, 87), IIC090("IIC", 90, 91), IIC094(
				"IIC", 94, 97), IIC100("IIC", 100, 232), IIC300("IIC", 300, 303), IIC310("IIC", 310, 313), IIC320("IIC", 320, 323), IIC330("IIC", 330, 335), IIC340(
				"IIC", 340, 359), IID001("IID", 1, 28), IID300("IID", 300, 320), IID330("IID", 330, 333), IID340("IID", 340, 343), IIE001("IIE", 1, 3), IIF301_FIXED_NO_XPATH(
				"IIF", 301, 301), IIF310_FIXED_NO_XPATH("IIF", 310, 310), IIF311("IIF", 311, 311), IIIA001("IIIA", 1, 28), IIIA301("IIIA", 301, 329), IIIA340(
				"IIIA", 340, 340), IIIC001("IIIC", 1, 1);

		private final String filenamePrefixBeforeNum;
		private final int startNum;
		private final int endNum;

		private TestParameters(int startNum, int endNum)
		{
			this(null, startNum, endNum);
		}

		private TestParameters(String filenamePrefix, int startNum, int endNum)
		{
			this.filenamePrefixBeforeNum = filenamePrefix == null ? this.name() : filenamePrefix;
			this.startNum = startNum;
			this.endNum = endNum;
		}
	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		setUp(ROOT_DIRECTORY);
	}

	public ConformanceV3FromV2Mandatory(String filePathPrefix, String requestFilterId)
	{
		super(filePathPrefix, false, requestFilterId);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data()
	{
		final Collection<Object[]> testData = new ArrayList<>();
		for (TestParameters testParams : TestParameters.values())
		{
			// no custom request filter
			testData.addAll(getTestData(ROOT_DIRECTORY, testParams.name(), testParams.filenamePrefixBeforeNum, testParams.startNum, testParams.endNum, null));
		}

		return testData;
	}
}