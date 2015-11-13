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
/**
 * 
 */
package com.thalesgroup.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.Expression.Value;
import com.thalesgroup.authzforce.core.datatypes.Bag;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;
import com.thalesgroup.authzforce.core.datatypes.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.StringAttributeValue;
import com.thalesgroup.authzforce.core.test.utils.TestUtils;

@RunWith(Parameterized.class)
public class HigherOrderFunctionsTest extends GeneralFunctionTest
{
	public HigherOrderFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_ANY_OF = "urn:oasis:names:tc:xacml:3.0:function:any-of";
	private static final String NAME_ALL_OF = "urn:oasis:names:tc:xacml:3.0:function:all-of";
	private static final String NAME_ANY_OF_ANY = "urn:oasis:names:tc:xacml:3.0:function:any-of-any";
	private static final String NAME_ALL_OF_ANY = "urn:oasis:names:tc:xacml:1.0:function:all-of-any";
	private static final String NAME_ANY_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:any-of-all";
	private static final String NAME_ALL_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:all-of-all";
	private static final String NAME_MAP = "urn:oasis:names:tc:xacml:3.0:function:map";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		Function<?> stringEqualFunc = TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal");
		Function<?> integerEqualFunc = TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than");

		return Arrays.asList(
		// urn:oasis:names:tc:xacml:3.0:function:any-of
				new Object[] { NAME_ANY_OF,//
						Arrays.asList(stringEqualFunc,//
								new StringAttributeValue("Paul"), //
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("George"), new StringAttributeValue("Ringo")))),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ANY_OF,//
						Arrays.asList(stringEqualFunc,//
								new StringAttributeValue("Paul"), //
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("John"), new StringAttributeValue("George"), new StringAttributeValue("Ringo")))),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:all-of
				new Object[] { NAME_ALL_OF,//
						Arrays.asList(integerEqualFunc,//
								new IntegerAttributeValue("10"), //
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("9"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4"), new IntegerAttributeValue("2")))),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ALL_OF,//
						Arrays.asList(integerEqualFunc,//
								new IntegerAttributeValue("10"), //
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("9"), new IntegerAttributeValue("3"), new IntegerAttributeValue("14"), new IntegerAttributeValue("2")))),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:any-of-any
				new Object[] { NAME_ANY_OF_ANY,//
						Arrays.asList(stringEqualFunc,//
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("Ringo"), new StringAttributeValue("Mary"))),//
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("George"), new StringAttributeValue("Ringo")))),//
						BooleanAttributeValue.TRUE },//
				// Example with matching string in last position in first bag
				new Object[] { NAME_ANY_OF_ANY,//
						Arrays.asList(stringEqualFunc,//
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("Ringo"), new StringAttributeValue("Mary"))),//
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("Mary"), new StringAttributeValue("Ringo")))),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ANY_OF_ANY,//
						Arrays.asList(stringEqualFunc,//
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("Ringo"), new StringAttributeValue("Mary"))),//
								Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("George")))),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-any
				new Object[] { NAME_ALL_OF_ANY,//
						Arrays.asList(integerEqualFunc,//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("10"), new IntegerAttributeValue("20"))),//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("3")))),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ALL_OF_ANY,//
						Arrays.asList(integerEqualFunc,//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("10"), new IntegerAttributeValue("20"))),//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("11"), new IntegerAttributeValue("13"), new IntegerAttributeValue("15"), new IntegerAttributeValue("19")))),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:any-of-all
				new Object[] { NAME_ANY_OF_ALL,//
						Arrays.asList(integerEqualFunc,//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("3"), new IntegerAttributeValue("5"))),//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4")))),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ANY_OF_ALL,//
						Arrays.asList(integerEqualFunc,//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("3"), new IntegerAttributeValue("4"))),//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4")))),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-all
				new Object[] { NAME_ALL_OF_ALL,//
						Arrays.asList(integerEqualFunc,//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("6"), new IntegerAttributeValue("5"))),//
								Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4")))),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ALL_OF_ALL, Arrays.asList(integerEqualFunc,//
						Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("3"), new IntegerAttributeValue("5"))),//
						Bag.getInstance(DatatypeConstants.INTEGER.BAG_TYPE, Arrays.asList(new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4")))),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:map
				new Object[] { NAME_MAP, Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case"),//
						Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("Hello"), new StringAttributeValue("World")))),//
						Bag.getInstance(DatatypeConstants.STRING.BAG_TYPE, Arrays.asList(new StringAttributeValue("hello"), new StringAttributeValue("world"))) }//
				);
	}

}
