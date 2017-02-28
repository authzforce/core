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
package org.ow2.authzforce.core.pdp.impl.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

@RunWith(Parameterized.class)
public class NumericComparisonFunctionsTest extends StandardFunctionTest
{

	public NumericComparisonFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_INTEGER_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than";
	private static final String NAME_INTEGER_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal";
	private static final String NAME_INTEGER_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:integer-less-than";
	private static final String NAME_INTEGER_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal";
	private static final String NAME_DOUBLE_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:double-greater-than";
	private static final String NAME_DOUBLE_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal";
	private static final String NAME_DOUBLE_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:double-less-than";
	private static final String NAME_DOUBLE_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
				new Object[] { NAME_INTEGER_GREATER_THAN, Arrays.asList(new IntegerValue("5"), new IntegerValue("4")), BooleanValue.TRUE },
				new Object[] { NAME_INTEGER_GREATER_THAN, Arrays.asList(new IntegerValue("5"), new IntegerValue("6")), BooleanValue.FALSE },
				new Object[] { NAME_INTEGER_GREATER_THAN, Arrays.asList(new IntegerValue("5"), new IntegerValue("5")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
				new Object[] { NAME_INTEGER_GREATER_THAN_OR_EQUAL, Arrays.asList(new IntegerValue("5"), new IntegerValue("4")), BooleanValue.TRUE },
				new Object[] { NAME_INTEGER_GREATER_THAN_OR_EQUAL, Arrays.asList(new IntegerValue("5"), new IntegerValue("6")), BooleanValue.FALSE },
				new Object[] { NAME_INTEGER_GREATER_THAN_OR_EQUAL, Arrays.asList(new IntegerValue("5"), new IntegerValue("5")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than
				new Object[] { NAME_INTEGER_LESS_THAN, Arrays.asList(new IntegerValue("5"), new IntegerValue("4")), BooleanValue.FALSE },
				new Object[] { NAME_INTEGER_LESS_THAN, Arrays.asList(new IntegerValue("5"), new IntegerValue("6")), BooleanValue.TRUE },
				new Object[] { NAME_INTEGER_LESS_THAN, Arrays.asList(new IntegerValue("5"), new IntegerValue("5")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
				new Object[] { NAME_INTEGER_LESS_THAN_OR_EQUAL, Arrays.asList(new IntegerValue("5"), new IntegerValue("4")), BooleanValue.FALSE },
				new Object[] { NAME_INTEGER_LESS_THAN_OR_EQUAL, Arrays.asList(new IntegerValue("5"), new IntegerValue("6")), BooleanValue.TRUE },
				new Object[] { NAME_INTEGER_LESS_THAN_OR_EQUAL, Arrays.asList(new IntegerValue("5"), new IntegerValue("5")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than
				new Object[] { NAME_DOUBLE_GREATER_THAN, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.4")), BooleanValue.TRUE },
				new Object[] { NAME_DOUBLE_GREATER_THAN, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.6")), BooleanValue.FALSE },
				new Object[] { NAME_DOUBLE_GREATER_THAN, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.5")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
				new Object[] { NAME_DOUBLE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.4")), BooleanValue.TRUE },
				new Object[] { NAME_DOUBLE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.6")), BooleanValue.FALSE },
				new Object[] { NAME_DOUBLE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.5")), BooleanValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than
				new Object[] { NAME_DOUBLE_LESS_THAN, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.4")), BooleanValue.FALSE },//
				new Object[] { NAME_DOUBLE_LESS_THAN, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.6")), BooleanValue.TRUE },
				new Object[] { NAME_DOUBLE_LESS_THAN, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.5")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal
				new Object[] { NAME_DOUBLE_LESS_THAN_OR_EQUAL, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.4")), BooleanValue.FALSE },//
				new Object[] { NAME_DOUBLE_LESS_THAN_OR_EQUAL, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.6")), BooleanValue.TRUE }, //
				new Object[] { NAME_DOUBLE_LESS_THAN_OR_EQUAL, Arrays.asList(new DoubleValue("5.5"), new DoubleValue("5.5")), BooleanValue.TRUE });
	}

}
