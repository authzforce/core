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
package org.ow2.authzforce.core.pdp.impl.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

@RunWith(Parameterized.class)
public class NumericConversionFunctionsTest extends StandardFunctionTest
{

	public NumericConversionFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_DOUBLE_TO_INTEGER = "urn:oasis:names:tc:xacml:1.0:function:double-to-integer";
	private static final String NAME_INTEGER_TO_DOUBLE = "urn:oasis:names:tc:xacml:1.0:function:integer-to-double";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
		// urn:oasis:names:tc:xacml:1.0:function:double-to-integer
				new Object[] { NAME_DOUBLE_TO_INTEGER, Arrays.asList(new DoubleValue("5.25")), IntegerValue.valueOf(5) },//
				new Object[] { NAME_DOUBLE_TO_INTEGER, Arrays.asList(new DoubleValue("5.75")), IntegerValue.valueOf(5) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-to-double
				new Object[] { NAME_INTEGER_TO_DOUBLE, Arrays.asList(IntegerValue.valueOf(5)), new DoubleValue("5.") });
	}

}
