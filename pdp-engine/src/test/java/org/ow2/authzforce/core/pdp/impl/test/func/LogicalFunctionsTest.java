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
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlDatatypeId;

@RunWith(Parameterized.class)
public class LogicalFunctionsTest extends StandardFunctionTest
{

	public LogicalFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_OR = "urn:oasis:names:tc:xacml:1.0:function:or";
	private static final String NAME_AND = "urn:oasis:names:tc:xacml:1.0:function:and";
	private static final String NAME_N_OF = "urn:oasis:names:tc:xacml:1.0:function:n-of";
	private static final String NAME_NOT = "urn:oasis:names:tc:xacml:1.0:function:not";

	private static final NullValue NULL_BOOLEAN_VALUE = new NullValue(XacmlDatatypeId.BOOLEAN.value());

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
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
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(0)), BooleanValue.TRUE },//
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(0), BooleanValue.FALSE, BooleanValue.FALSE, BooleanValue.FALSE), BooleanValue.TRUE },//
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), BooleanValue.TRUE, BooleanValue.FALSE, BooleanValue.FALSE), BooleanValue.FALSE },
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.FALSE), BooleanValue.TRUE },
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE), BooleanValue.TRUE },
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(4), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE), null },

				/*
				 * This is not explicit in the XACML spec, but we expect the function to return an error if first argument is < 0.
				 */
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(-1), BooleanValue.TRUE, BooleanValue.TRUE, BooleanValue.TRUE), null },

				// with indeterminate argument in first position
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), NULL_BOOLEAN_VALUE, BooleanValue.TRUE, BooleanValue.TRUE), BooleanValue.TRUE },
				// with indeterminate argument after a TRUE
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE), BooleanValue.TRUE },
				// with indeterminate argument after a FALSE with a TRUE
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(1), BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.TRUE), BooleanValue.TRUE },
				// 2 TRUES required: 1 indeterminate and all other FALSE args -> FALSE (2 TRUES not
				// possible)
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), BooleanValue.FALSE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE), BooleanValue.FALSE },
				// 2 TRUEs required: 1 TRUE and one indeterminate args -> indeterminate
				new Object[] { NAME_N_OF, Arrays.asList(IntegerValue.valueOf(2), BooleanValue.TRUE, NULL_BOOLEAN_VALUE, BooleanValue.FALSE), null },

				// urn:oasis:names:tc:xacml:1.0:function:not
				new Object[] { NAME_NOT, Arrays.asList(BooleanValue.TRUE), BooleanValue.FALSE }, new Object[] { NAME_NOT, Arrays.asList(BooleanValue.FALSE), BooleanValue.TRUE });
	}

}
