/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
package org.ow2.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class NumericConversionFunctionsTest extends FunctionTest
{

	public NumericConversionFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
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
				new Object[] { NAME_DOUBLE_TO_INTEGER, Arrays.asList(new DoubleValue("5.25")), new IntegerValue("5") },//
				new Object[] { NAME_DOUBLE_TO_INTEGER, Arrays.asList(new DoubleValue("5.75")), new IntegerValue("5") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-to-double
				new Object[] { NAME_INTEGER_TO_DOUBLE, Arrays.asList(new IntegerValue("5")), new DoubleValue("5.") });
	}

}
