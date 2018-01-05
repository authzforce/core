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
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

@RunWith(Parameterized.class)
public class HigherOrderFunctionsTest extends StandardFunctionTest
{
	public HigherOrderFunctionsTest(final String functionName, final String subFunctionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, subFunctionName, inputs, expectedResult);
	}

	private static final String NAME_ANY_OF = "urn:oasis:names:tc:xacml:3.0:function:any-of";
	private static final String NAME_ALL_OF = "urn:oasis:names:tc:xacml:3.0:function:all-of";
	private static final String NAME_ANY_OF_ANY = "urn:oasis:names:tc:xacml:3.0:function:any-of-any";
	private static final String NAME_ALL_OF_ANY = "urn:oasis:names:tc:xacml:1.0:function:all-of-any";
	private static final String NAME_ANY_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:any-of-all";
	private static final String NAME_ALL_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:all-of-all";
	private static final String NAME_MAP = "urn:oasis:names:tc:xacml:3.0:function:map";

	private static final String STRING_EQUAL_FUNCTION_ID = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	private static final String INTEGER_GREATER_THAN_FUNCTION_ID = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than";
	private static final String STRING_NORMALIZE_TO_LC_FUNCTION_ID = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{

		return Arrays.asList(
		// urn:oasis:names:tc:xacml:3.0:function:any-of
				new Object[] { NAME_ANY_OF,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(new StringValue("Paul"), //
								Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("George"), new StringValue("Ringo")))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ANY_OF,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(new StringValue("Paul"), //
								Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("John"), new StringValue("George"), new StringValue("Ringo")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:all-of
				new Object[] { NAME_ALL_OF,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(IntegerValue.valueOf(10), //
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(9), IntegerValue.valueOf(3), IntegerValue.valueOf(4), IntegerValue.valueOf(2)))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ALL_OF,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(IntegerValue.valueOf(10), //
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(9), IntegerValue.valueOf(3), IntegerValue.valueOf(14), IntegerValue.valueOf(2)))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:any-of-any
				new Object[] { NAME_ANY_OF_ANY,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("Ringo"), new StringValue("Mary"))),//
								Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("George"), new StringValue("Ringo")))),//
						BooleanValue.TRUE },//
				// Example with matching string in last position in first bag
				new Object[] { NAME_ANY_OF_ANY,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("Ringo"), new StringValue("Mary"))),//
								Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("Mary"), new StringValue("Ringo")))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ANY_OF_ANY,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("Ringo"), new StringValue("Mary"))),//
								Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("George")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-any
				new Object[] { NAME_ALL_OF_ANY,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(10), IntegerValue.valueOf(20))),//
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(1), IntegerValue.valueOf(3)))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ALL_OF_ANY,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(10), IntegerValue.valueOf(20))),//
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(11), IntegerValue.valueOf(13), IntegerValue.valueOf(15), IntegerValue.valueOf(19)))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:any-of-all
				new Object[] { NAME_ANY_OF_ALL,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//

						Arrays.asList(Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(3), IntegerValue.valueOf(5))),//
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(1), IntegerValue.valueOf(2), IntegerValue.valueOf(3), IntegerValue.valueOf(4)))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ANY_OF_ALL,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(3), IntegerValue.valueOf(4))),//
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(1), IntegerValue.valueOf(2), IntegerValue.valueOf(3), IntegerValue.valueOf(4)))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-all
				new Object[] { NAME_ALL_OF_ALL,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(6), IntegerValue.valueOf(5))),//
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(1), IntegerValue.valueOf(2), IntegerValue.valueOf(3), IntegerValue.valueOf(4)))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ALL_OF_ALL, INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(3), IntegerValue.valueOf(5))),//
								Bags.newBag(StandardDatatypes.INTEGER, Arrays.asList(IntegerValue.valueOf(1), IntegerValue.valueOf(2), IntegerValue.valueOf(3), IntegerValue.valueOf(4)))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:map
				new Object[] { NAME_MAP, //
						STRING_NORMALIZE_TO_LC_FUNCTION_ID,//
						Arrays.asList(Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("Hello"), new StringValue("World")))),//
						Bags.newBag(StandardDatatypes.STRING, Arrays.asList(new StringValue("hello"), new StringValue("world"))) }//
				);
	}

}
