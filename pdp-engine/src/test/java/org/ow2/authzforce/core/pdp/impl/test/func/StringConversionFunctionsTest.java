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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

@RunWith(Parameterized.class)
public class StringConversionFunctionsTest extends StandardFunctionTest
{

	public StringConversionFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
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
