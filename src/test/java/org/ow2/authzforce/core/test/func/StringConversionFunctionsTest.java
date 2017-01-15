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
/**
 * 
 */
package org.ow2.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class StringConversionFunctionsTest extends FunctionTest
{

	public StringConversionFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_STRING_NORMALIZE_SPACE = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-space";
	private static final String NAME_STRING_NORMALIZE_TO_LOWER_CASE = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
		// urn:oasis:names:tc:xacml:1.0:function:string-normalize-space
				new Object[] { NAME_STRING_NORMALIZE_SPACE, Arrays.asList(new StringValue("test")), new StringValue("test") },//
				new Object[] { NAME_STRING_NORMALIZE_SPACE, Arrays.asList(new StringValue("   test   ")), new StringValue("test") },

				// urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case
				new Object[] { NAME_STRING_NORMALIZE_TO_LOWER_CASE, Arrays.asList(new StringValue("test")), new StringValue("test") },//
				new Object[] { NAME_STRING_NORMALIZE_TO_LOWER_CASE, Arrays.asList(new StringValue("TeST")), new StringValue("test") });
	}

}
