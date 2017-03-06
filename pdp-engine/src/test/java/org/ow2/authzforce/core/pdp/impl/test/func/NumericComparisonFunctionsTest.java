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
