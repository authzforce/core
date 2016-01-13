/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
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
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.impl.value.DoubleValue;
import org.ow2.authzforce.core.pdp.impl.value.IntegerValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class NumericArithmeticFunctionsTest extends FunctionTest
{

	public NumericArithmeticFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
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
				new Object[] { NAME_INTEGER_ADD, Arrays.asList(new IntegerValue("2"), new IntegerValue("1")), new IntegerValue("3") },
				//
				new Object[] { NAME_INTEGER_ADD, Arrays.asList(new IntegerValue("2"), new IntegerValue("-1")), new IntegerValue("1") }, new Object[] {
						NAME_INTEGER_ADD, Arrays.asList(new IntegerValue("2"), new IntegerValue("-1"), new IntegerValue("0"), new IntegerValue("3")),
						new IntegerValue("4") },

				// urn:oasis:names:tc:xacml:1.0:function:double-add
				new Object[] { NAME_DOUBLE_ADD, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("2.5")), new DoubleValue("4.0") },
				//
				new Object[] { NAME_DOUBLE_ADD, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("-2.5")), new DoubleValue("-1.") }, new Object[] {
						NAME_DOUBLE_ADD, Arrays.asList(new DoubleValue("1.25"), new DoubleValue("-2.75"), new DoubleValue("0.0"), new DoubleValue("4.0")),
						new DoubleValue("2.5") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-subtract
				new Object[] { NAME_INTEGER_SUBTRACT, Arrays.asList(new IntegerValue("2"), new IntegerValue("1")), new IntegerValue("1") },
				//
				new Object[] { NAME_INTEGER_SUBTRACT, Arrays.asList(new IntegerValue("2"), new IntegerValue("-1")), new IntegerValue("3") },

				// urn:oasis:names:tc:xacml:1.0:function:double-subtract
				new Object[] { NAME_DOUBLE_SUBTRACT, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("2.5")), new DoubleValue("-1.") },
				//
				new Object[] { NAME_DOUBLE_SUBTRACT, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("-2.5")), new DoubleValue("4.0") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-multiply
				new Object[] { NAME_INTEGER_MULTIPLY, Arrays.asList(new IntegerValue("2"), new IntegerValue("3")), new IntegerValue("6") },//
				new Object[] { NAME_INTEGER_MULTIPLY, Arrays.asList(new IntegerValue("2"), new IntegerValue("0")), new IntegerValue("0") }, new Object[] {
						NAME_INTEGER_MULTIPLY, Arrays.asList(new IntegerValue("2"), new IntegerValue("-1"), new IntegerValue("3")), new IntegerValue("-6") },

				// urn:oasis:names:tc:xacml:1.0:function:double-multiply
				new Object[] { NAME_DOUBLE_MULTIPLY, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("2.5")), new DoubleValue("3.75") },//
				new Object[] { NAME_DOUBLE_MULTIPLY, Arrays.asList(new DoubleValue("1.5"), new DoubleValue("0.0")), new DoubleValue("0.0") }, new Object[] {
						NAME_DOUBLE_MULTIPLY, Arrays.asList(new DoubleValue("1.25"), new DoubleValue("-2.75"), new DoubleValue("1.5")),
						new DoubleValue("-5.15625") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-divide
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(new IntegerValue("6"), new IntegerValue("3")), new IntegerValue("2") },
				//
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(new IntegerValue("7"), new IntegerValue("-3")), new IntegerValue("-2") },
				//
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(new IntegerValue("0"), new IntegerValue("-3")), new IntegerValue("0") },
				//
				new Object[] { NAME_INTEGER_DIVIDE, Arrays.asList(new IntegerValue("-3"), new IntegerValue("0")), null },

				// urn:oasis:names:tc:xacml:1.0:function:double-divide
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("6.5"), new DoubleValue("2.5")), new DoubleValue("2.6") },
				//
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("7.0"), new DoubleValue("-2.")), new DoubleValue("-3.5") }, //
				// According to IEEE Standard for Floating-Point Arithmetic (IEEE 754), division
				// below returns -0.0
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("0.0"), new DoubleValue("-3.14")), new DoubleValue("-0.0") }, //
				new Object[] { NAME_DOUBLE_DIVIDE, Arrays.asList(new DoubleValue("-3.14"), new DoubleValue("0.0")), null },

				// urn:oasis:names:tc:xacml:1.0:function:integer-mod
				new Object[] { NAME_INTEGER_MOD, Arrays.asList(new IntegerValue("6"), new IntegerValue("3")), new IntegerValue("0") },
				//
				new Object[] { NAME_INTEGER_MOD, Arrays.asList(new IntegerValue("7"), new IntegerValue("3")), new IntegerValue("1") },
				//
				new Object[] { NAME_INTEGER_MOD, Arrays.asList(new IntegerValue("0"), new IntegerValue("-3")), new IntegerValue("0") },

				// urn:oasis:names:tc:xacml:1.0:function:integer-abs
				new Object[] { NAME_INTEGER_ABS, Arrays.asList(new IntegerValue("5")), new IntegerValue("5") },//
				new Object[] { NAME_INTEGER_ABS, Arrays.asList(new IntegerValue("-5")), new IntegerValue("5") },

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
