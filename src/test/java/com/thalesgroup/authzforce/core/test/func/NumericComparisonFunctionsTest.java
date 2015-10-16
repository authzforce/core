/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class NumericComparisonFunctionsTest extends GeneralFunctionTest
{

	public NumericComparisonFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
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
				new Object[] { NAME_INTEGER_GREATER_THAN, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("4")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_INTEGER_GREATER_THAN, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("6")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_INTEGER_GREATER_THAN, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("5")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
				new Object[] { NAME_INTEGER_GREATER_THAN_OR_EQUAL, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("4")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_INTEGER_GREATER_THAN_OR_EQUAL, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("6")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_INTEGER_GREATER_THAN_OR_EQUAL, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("5")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than
				new Object[] { NAME_INTEGER_LESS_THAN, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("4")), BooleanAttributeValue.FALSE }, new Object[] { NAME_INTEGER_LESS_THAN, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("6")),
						BooleanAttributeValue.TRUE },
				new Object[] { NAME_INTEGER_LESS_THAN, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("5")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
				new Object[] { NAME_INTEGER_LESS_THAN_OR_EQUAL, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("4")), BooleanAttributeValue.FALSE }, new Object[] { NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("6")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_INTEGER_LESS_THAN_OR_EQUAL, Arrays.asList(new IntegerAttributeValue("5"), new IntegerAttributeValue("5")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than
				new Object[] { NAME_DOUBLE_GREATER_THAN, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.4")), BooleanAttributeValue.TRUE }, new Object[] { NAME_DOUBLE_GREATER_THAN, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.6")),
						BooleanAttributeValue.FALSE },
				new Object[] { NAME_DOUBLE_GREATER_THAN, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.5")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
				new Object[] { NAME_DOUBLE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.4")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DOUBLE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.6")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_DOUBLE_GREATER_THAN_OR_EQUAL, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.5")), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than
				new Object[] { NAME_DOUBLE_LESS_THAN, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.4")), BooleanAttributeValue.FALSE },//
				new Object[] { NAME_DOUBLE_LESS_THAN, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.6")), BooleanAttributeValue.TRUE }, new Object[] { NAME_DOUBLE_LESS_THAN, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.5")),
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal
				new Object[] { NAME_DOUBLE_LESS_THAN_OR_EQUAL, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.4")), BooleanAttributeValue.FALSE },//
				new Object[] { NAME_DOUBLE_LESS_THAN_OR_EQUAL, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.6")), BooleanAttributeValue.TRUE }, //
				new Object[] { NAME_DOUBLE_LESS_THAN_OR_EQUAL, Arrays.asList(new DoubleAttributeValue("5.5"), new DoubleAttributeValue("5.5")), BooleanAttributeValue.TRUE });
	}

}
