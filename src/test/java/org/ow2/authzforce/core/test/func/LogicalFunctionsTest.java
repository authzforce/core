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
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.test.utils.FunctionTest;
import org.ow2.authzforce.core.test.utils.NullValue;

@RunWith(Parameterized.class)
public class LogicalFunctionsTest extends FunctionTest
{

	public LogicalFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_OR = "urn:oasis:names:tc:xacml:1.0:function:or";
	private static final String NAME_AND = "urn:oasis:names:tc:xacml:1.0:function:and";
	private static final String NAME_N_OF = "urn:oasis:names:tc:xacml:1.0:function:n-of";
	private static final String NAME_NOT = "urn:oasis:names:tc:xacml:1.0:function:not";

	private static final NullValue NULL_BOOLEAN_VALUE = new NullValue(BooleanValue.TYPE_URI);

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:or
				new Object[] { NAME_OR, Collections.EMPTY_LIST, BooleanValue.FALSE },
						new Object[] { NAME_OR, Arrays.asList(BooleanValue.FALSE, BooleanValue.FALSE, BooleanValue.FALSE), BooleanValue.FALSE },
						new Object[] { NAME_OR, Arrays.asList(BooleanValue.FALSE, BooleanValue.TRUE, BooleanValue.FALSE), BooleanValue.TRUE },
						// with indeterminate argument in first position
						new Object[] { NAME_OR, Arrays.asList(NULL_BOOLEAN_VALUE, BooleanValue.TRUE, BooleanValue.FALSE), BooleanValue.TRUE },
						// with indeterminate argument after a TRUE
						new Object[] { NAME_OR, Arrays.asList(BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE), BooleanValue.TRUE },
						// with indeterminate argument after a FALSE with a TRUE
						new Object[] { NAME_OR, Arrays.asList(BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE), BooleanValue.TRUE },
						// with indeterminate argument after a FALSE with only FALSEs or indeterminate ->
						// INDETERMINATE
						new Object[] { NAME_OR, Arrays.asList(BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE), null },

						// urn:oasis:names:tc:xacml:1.0:function:and
						new Object[] { NAME_AND, Collections.EMPTY_LIST, BooleanValue.TRUE },
						new Object[] { NAME_AND, Arrays.asList(BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE), BooleanValue.TRUE },
						new Object[] { NAME_AND, Arrays.asList(BooleanValue.TRUE, BooleanValue.FALSE, BooleanValue.TRUE), BooleanValue.FALSE },
						// with indeterminate argument in first position
						new Object[] { NAME_AND, Arrays.asList(NULL_BOOLEAN_VALUE, BooleanValue.TRUE, BooleanValue.FALSE), BooleanValue.FALSE },
						// with indeterminate argument after a FALSE
						new Object[] { NAME_AND, Arrays.asList(BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE), BooleanValue.FALSE },
						// with indeterminate argument after a TRUE with a FALSE
						new Object[] { NAME_AND, Arrays.asList(BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE), BooleanValue.FALSE },
						// with indeterminate argument after a TRUE with only TRUEs or Indeterminate ->
						// INDETERMINATE
						new Object[] { NAME_AND, Arrays.asList(BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE), null },

						// urn:oasis:names:tc:xacml:1.0:function:n-of
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("0")), BooleanValue.TRUE },//
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("0"), BooleanValue.FALSE, BooleanValue.FALSE, BooleanValue.FALSE),
								BooleanValue.TRUE },//
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), BooleanValue.TRUE, BooleanValue.FALSE, BooleanValue.FALSE),
								BooleanValue.FALSE },
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.FALSE),
								BooleanValue.TRUE },
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE),
								BooleanValue.TRUE },
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("4"), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE), null },

						/*
						 * This is not explicit in the XACML spec, but we expect the function to return an error if first argument is < 0.
						 */
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("-1"), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE), null },

						// with indeterminate argument in first position
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), NULL_BOOLEAN_VALUE, BooleanValue.TRUE, BooleanValue.TRUE),
								BooleanValue.TRUE },
						// with indeterminate argument after a TRUE
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE),
								BooleanValue.TRUE },
						// with indeterminate argument after a FALSE with a TRUE
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("1"), BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE),
								BooleanValue.TRUE },
						// 2 TRUES required: 1 indeterminate and all other FALSE args -> FALSE (2 TRUES not
						// possible)
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE),
								BooleanValue.FALSE },
						// 2 TRUEs required: 1 TRUE and one indeterminate args -> indeterminate
						new Object[] { NAME_N_OF, Arrays.asList(new IntegerValue("2"), BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE), null },

						// urn:oasis:names:tc:xacml:1.0:function:not
						new Object[] { NAME_NOT, Arrays.asList(BooleanValue.TRUE), BooleanValue.FALSE },
						new Object[] { NAME_NOT, Arrays.asList(BooleanValue.FALSE), BooleanValue.TRUE });
	}

}
