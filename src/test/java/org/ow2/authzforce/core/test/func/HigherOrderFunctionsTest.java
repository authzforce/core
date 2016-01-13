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
import org.ow2.authzforce.core.pdp.api.Bags;
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.IntegerValue;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;
import org.ow2.authzforce.core.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class HigherOrderFunctionsTest extends FunctionTest
{
	public HigherOrderFunctionsTest(String functionName, String subFunctionName, List<Value> inputs, Value expectedResult)
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
				new Object[] {
						NAME_ANY_OF,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(
								new StringValue("Paul"), //
								Bags.getInstance(DatatypeConstants.STRING.TYPE,
										Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("George"), new StringValue("Ringo")))),//
						BooleanValue.TRUE },

				new Object[] {
						NAME_ANY_OF,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(
								new StringValue("Paul"), //
								Bags.getInstance(DatatypeConstants.STRING.TYPE,
										Arrays.asList(new StringValue("John"), new StringValue("George"), new StringValue("Ringo")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:all-of
				new Object[] {
						NAME_ALL_OF,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(
								new IntegerValue("10"), //
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("9"), new IntegerValue("3"), new IntegerValue("4"), new IntegerValue("2")))),//
						BooleanValue.TRUE },

				new Object[] {
						NAME_ALL_OF,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(
								new IntegerValue("10"), //
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("9"), new IntegerValue("3"), new IntegerValue("14"), new IntegerValue("2")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:any-of-any
				new Object[] {
						NAME_ANY_OF_ANY,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("Ringo"), new StringValue("Mary"))),//
								Bags.getInstance(DatatypeConstants.STRING.TYPE,
										Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("George"), new StringValue("Ringo")))),//
						BooleanValue.TRUE },//
				// Example with matching string in last position in first bag
				new Object[] {
						NAME_ANY_OF_ANY,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("Ringo"), new StringValue("Mary"))),//
								Bags.getInstance(DatatypeConstants.STRING.TYPE,
										Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("Mary"), new StringValue("Ringo")))),//
						BooleanValue.TRUE },

				new Object[] {
						NAME_ANY_OF_ANY,//
						STRING_EQUAL_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("Ringo"), new StringValue("Mary"))),//
								Bags.getInstance(DatatypeConstants.STRING.TYPE,
										Arrays.asList(new StringValue("John"), new StringValue("Paul"), new StringValue("George")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-any
				new Object[] { NAME_ALL_OF_ANY,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("10"), new IntegerValue("20"))),//
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("1"), new IntegerValue("3")))),//
						BooleanValue.TRUE },

				new Object[] {
						NAME_ALL_OF_ANY,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("10"), new IntegerValue("20"))),//
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("11"), new IntegerValue("13"), new IntegerValue("15"), new IntegerValue("19")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:any-of-all
				new Object[] {
						NAME_ANY_OF_ALL,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//

						Arrays.asList(
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("3"), new IntegerValue("5"))),//
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3"), new IntegerValue("4")))),//
						BooleanValue.TRUE },

				new Object[] {
						NAME_ANY_OF_ALL,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("3"), new IntegerValue("4"))),//
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3"), new IntegerValue("4")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-all
				new Object[] {
						NAME_ALL_OF_ALL,//
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("6"), new IntegerValue("5"))),//
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3"), new IntegerValue("4")))),//
						BooleanValue.TRUE },

				new Object[] { NAME_ALL_OF_ALL,
						INTEGER_GREATER_THAN_FUNCTION_ID,//
						Arrays.asList(
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE, Arrays.asList(new IntegerValue("3"), new IntegerValue("5"))),//
								Bags.getInstance(DatatypeConstants.INTEGER.TYPE,
										Arrays.asList(new IntegerValue("1"), new IntegerValue("2"), new IntegerValue("3"), new IntegerValue("4")))),//
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:map
				new Object[] { NAME_MAP, //
						STRING_NORMALIZE_TO_LC_FUNCTION_ID,//
						Arrays.asList(Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("Hello"), new StringValue("World")))),//
						Bags.getInstance(DatatypeConstants.STRING.TYPE, Arrays.asList(new StringValue("hello"), new StringValue("world"))) }//
				);
	}

}
