/**
 * Copyright 2012-2018 Thales Services SAS.
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
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

@RunWith(Parameterized.class)
public class NumericArithmeticFunctionsTest extends StandardFunctionTest
{

	public NumericArithmeticFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_INTEGER_ADD = "urn:oasis:names:tc:xacml:1.0:function:integer-add";
	private static final String NAME_DOUBLE_ADD = "urn:oasis:names:tc:xacml:1.0:function:double-add";
	private static final String NAME_INTEGER_SUBTRACT = "urn:oasis:names:tc:xacml:1.0:function:integer-subtract";
	private static final String NAME_DOUBLE_SUBTRACT = "urn:oasis:names:tc:xacml:1.0:function:double-subtract";
	private static final String NAME_INTEGER_MULTIPLY = "urn:oasis:names:tc:xacml:1.0:function:integer-multiply";
	private static final String NAME_DOUBLE_MULTIPLY = "urn:oasis:names:tc:xacml:1.0:function:double-multiply";
	private static final String NAME_INTEGER_DIVIDE = "urn:oasis:names:tc:xacml:1.0:function:integer-divide";
	private static final String NAME_DOUBLE_DIVIDE = "urn:oasis:names:tc:xacml:1.0:function:double-divide";
	private static final String NAME_INTEGER_MOD = "urn:oasis:names:tc:xacml:1.0:function:integer-mod";
	private static final String NAME_INTEGER_ABS = "urn:oasis:names:tc:xacml:1.0:function:integer-abs";
	private static final String NAME_DOUBLE_ABS = "urn:oasis:names:tc:xacml:1.0:function:double-abs";
	private static final String NAME_ROUND = "urn:oasis:names:tc:xacml:1.0:function:round";
	private static final String NAME_FLOOR = "urn:oasis:names:tc:xacml:1.0:function:floor";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-add
				new Object[] { NAME_INTEGER_ADD, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(1)), IntegerValue.valueOf(3) },
				//
				new Object[] { NAME_INTEGER_ADD, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(-1)), IntegerValue.valueOf(1) },
				new Object[] { NAME_INTEGER_ADD, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(-1), IntegerValue.valueOf(0), IntegerValue.valueOf(3)), IntegerValue.valueOf(4) },

				// urn:oasis:names:tc:xacml:1.0:function:double-add
				new Object[] { NAME_DOUBLE_ADD, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("2.5")), new DoubleValue("4.0") },
				//
				new Object[] { NAME_DOUBLE_ADD, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("-2.5")), new DoubleValue("-1.") },
				new Object[] { NAME_DOUBLE_ADD, Arrays.asList(new DoubleValue("1.25"), new DoubleValue("-2.75"), new DoubleValue("0.0"), new DoubleValue("4.0")), new DoubleValue("2.5") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-subtract
				new Object[] { NAME_INTEGER_SUBTRACT, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(1)), IntegerValue.valueOf(1) },
				//
				new Object[] { NAME_INTEGER_SUBTRACT, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(-1)), IntegerValue.valueOf(3) },

				// urn:oasis:names:tc:xacml:1.0:function:double-subtract
				new Object[] { NAME_DOUBLE_SUBTRACT, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("2.5")), new DoubleValue("-1.") },
				//
				new Object[] { NAME_DOUBLE_SUBTRACT, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("-2.5")), new DoubleValue("4.0") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-multiply
				new Object[] { NAME_INTEGER_MULTIPLY, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(3)), IntegerValue.valueOf(6) },//
				new Object[] { NAME_INTEGER_MULTIPLY, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(0)), IntegerValue.valueOf(0) },
				new Object[] { NAME_INTEGER_MULTIPLY, Arrays.asList(IntegerValue.valueOf(2), IntegerValue.valueOf(-1), IntegerValue.valueOf(3)), IntegerValue.valueOf(-6) },

				// urn:oasis:names:tc:xacml:1.0:function:double-multiply
				new Object[] { NAME_DOUBLE_MULTIPLY, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("2.5")), new DoubleValue("3.75") },//
				new Object[] { NAME_DOUBLE_MULTIPLY, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("0.0")), new DoubleValue("0.0") },
				new Object[] { NAME_DOUBLE_MULTIPLY, Arrays.asList(new DoubleValue("1.25"), new DoubleValue("-2.75"), new DoubleValue("1.5")), new DoubleValue("-5.15625") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-divide
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(IntegerValue.valueOf(6), IntegerValue.valueOf(3)), IntegerValue.valueOf(2) },
				//
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(IntegerValue.valueOf(7), IntegerValue.valueOf(-3)), IntegerValue.valueOf(-2) },
				//
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(IntegerValue.valueOf(0), IntegerValue.valueOf(-3)), IntegerValue.valueOf(0) },
				//
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(IntegerValue.valueOf(-3), IntegerValue.valueOf(0)), null },

				// urn:oasis:names:tc:xacml:1.0:function:double-divide
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("6.5"), new DoubleValue("2.5")), new DoubleValue("2.6") },
				//
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("7.0"), new DoubleValue("-2.")), new DoubleValue("-3.5") }, //
				// According to IEEE Standard for Floating-Point Arithmetic (IEEE 754), division
				// below returns -0.0
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("0.0"), new DoubleValue("-3.14")), new DoubleValue("-0.0") }, //
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("-3.14"), new DoubleValue("0.0")), null },

				// urn:oasis:names:tc:xacml:1.0:function:integer-mod
				new Object[] { NAME_INTEGER_MOD, Arrays.asList(IntegerValue.valueOf(6), IntegerValue.valueOf(3)), IntegerValue.valueOf(0) },
				//
				new Object[] { NAME_INTEGER_MOD, Arrays.asList(IntegerValue.valueOf(7), IntegerValue.valueOf(3)), IntegerValue.valueOf(1) },
				//
				new Object[] { NAME_INTEGER_MOD, Arrays.asList(IntegerValue.valueOf(0), IntegerValue.valueOf(-3)), IntegerValue.valueOf(0) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-abs
				new Object[] { NAME_INTEGER_ABS, Arrays.asList(IntegerValue.valueOf(5)), IntegerValue.valueOf(5) },//
				new Object[] { NAME_INTEGER_ABS, Arrays.asList(IntegerValue.valueOf(-5)), IntegerValue.valueOf(5) },

				// urn:oasis:names:tc:xacml:1.0:function:double-abs
				new Object[] { NAME_DOUBLE_ABS, Arrays.asList(new DoubleValue("5.25")), new DoubleValue("5.25") },//
				new Object[] { NAME_DOUBLE_ABS, Arrays.asList(new DoubleValue("-5.0")), new DoubleValue("5.0") },

				// urn:oasis:names:tc:xacml:1.0:function:round
				new Object[] { NAME_ROUND, Arrays.asList(new DoubleValue("5.25")), new DoubleValue("5.") },
				//
				new Object[] { NAME_ROUND, Arrays.asList(new DoubleValue("-5.75")), new DoubleValue("-6.") },//
				new Object[] { NAME_ROUND, Arrays.asList(new DoubleValue("5.5")), new DoubleValue("6.") },

				// urn:oasis:names:tc:xacml:1.0:function:floor
				new Object[] { NAME_FLOOR, Arrays.asList(new DoubleValue("5.25")), new DoubleValue("5.") },
				//
				new Object[] { NAME_FLOOR, Arrays.asList(new DoubleValue("-5.25")), new DoubleValue("-6.") },//
				new Object[] { NAME_FLOOR, Arrays.asList(new DoubleValue("5.5")), new DoubleValue("5.0") });
	}

}
