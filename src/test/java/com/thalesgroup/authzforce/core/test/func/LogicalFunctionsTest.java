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
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class LogicalFunctionsTest extends GeneralFunctionTest
{

	public LogicalFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?, ?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_OR = "urn:oasis:names:tc:xacml:1.0:function:or";
	private static final String NAME_AND = "urn:oasis:names:tc:xacml:1.0:function:and";
	private static final String NAME_N_OF = "urn:oasis:names:tc:xacml:1.0:function:n-of";
	private static final String NAME_NOT = "urn:oasis:names:tc:xacml:1.0:function:not";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:or
				new Object[] { NAME_OR, Collections.EMPTY_LIST, BooleanAttributeValue.FALSE },
				new Object[] { NAME_OR, Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.FALSE },
				new Object[] { NAME_OR, Arrays.asList(BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE), BooleanAttributeValue.TRUE },

				// urn:oasis:names:tc:xacml:1.0:function:and
				new Object[] { NAME_AND, Collections.EMPTY_LIST, BooleanAttributeValue.TRUE },
				new Object[] { NAME_AND, Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), BooleanAttributeValue.TRUE },
				new Object[] { NAME_AND, Arrays.asList(BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:n-of
				new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("0")), BooleanAttributeValue.TRUE }, new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("0"), BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE),
						BooleanAttributeValue.TRUE }, new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("2"), BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE, BooleanAttributeValue.FALSE), BooleanAttributeValue.FALSE },
				new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("2"), BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE, BooleanAttributeValue.FALSE), BooleanAttributeValue.TRUE },
				new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("2"), BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), BooleanAttributeValue.TRUE },
				new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("4"), BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), null },
				/*
				 * This is not explicit in the XACML spec, but we expect the function to return an
				 * error if first argument is < 0.
				 */
				new Object[] { NAME_N_OF, Arrays.asList(new IntegerAttributeValue("-1"), BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE, BooleanAttributeValue.TRUE), null },

				// urn:oasis:names:tc:xacml:1.0:function:not
				new Object[] { NAME_NOT, Arrays.asList(BooleanAttributeValue.TRUE), BooleanAttributeValue.FALSE }, new Object[] { NAME_NOT, Arrays.asList(BooleanAttributeValue.FALSE), BooleanAttributeValue.TRUE });
	}

}
