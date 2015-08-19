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

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.test.utils.TestUtils;

@RunWith(Parameterized.class)
public class HigherOrderFunctionsTest extends GeneralFunctionTest
{
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
		return Arrays.asList(
		// urn:oasis:names:tc:xacml:3.0:function:any-of
				new Object[] { NAME_ANY_OF,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),//
								new StringAttributeValue("Paul"), //
								new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("George"), new StringAttributeValue("Ringo") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ANY_OF,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),//
								new StringAttributeValue("Paul"), //
								new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("John"), new StringAttributeValue("George"), new StringAttributeValue("Ringo") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:all-of
				new Object[] { NAME_ALL_OF,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new IntegerAttributeValue("10"), //
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("9"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4"), new IntegerAttributeValue("2") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ALL_OF,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new IntegerAttributeValue("10"), //
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("9"), new IntegerAttributeValue("3"), new IntegerAttributeValue("14"), new IntegerAttributeValue("2") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:any-of-any
				new Object[] { NAME_ANY_OF_ANY,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),//
								new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("Ringo"), new StringAttributeValue("Mary") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE),//
								new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("George"), new StringAttributeValue("Ringo") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ANY_OF_ANY,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),//
								new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("Ringo"), new StringAttributeValue("Mary") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE),//
								new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("John"), new StringAttributeValue("Paul"), new StringAttributeValue("George") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-any
				new Object[] { NAME_ALL_OF_ANY,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("10"), new IntegerAttributeValue("20") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("1"), new IntegerAttributeValue("3") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ALL_OF_ANY,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("10"), new IntegerAttributeValue("20") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("11"), new IntegerAttributeValue("13"), new IntegerAttributeValue("15"), new IntegerAttributeValue("19") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:any-of-all
				new Object[] { NAME_ANY_OF_ALL,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("3"), new IntegerAttributeValue("5") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ANY_OF_ALL,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("3"), new IntegerAttributeValue("4") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:all-of-all
				new Object[] { NAME_ALL_OF_ALL,//
						Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("6"), new IntegerAttributeValue("5") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
								new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.TRUE },

				new Object[] { NAME_ALL_OF_ALL, Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),//
						new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("3"), new IntegerAttributeValue("5") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE),//
						new BagResult<>(new IntegerAttributeValue[] { new IntegerAttributeValue("1"), new IntegerAttributeValue("2"), new IntegerAttributeValue("3"), new IntegerAttributeValue("4") }, IntegerAttributeValue.class, IntegerAttributeValue.BAG_TYPE)),//
						BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:3.0:function:map
				new Object[] { NAME_MAP, Arrays.asList(TestUtils.STD_EXPRESSION_FACTORY.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case"),//
						new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("Hello"), new StringAttributeValue("World") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE)),//
						new BagResult<>(new StringAttributeValue[] { new StringAttributeValue("hello"), new StringAttributeValue("world") }, StringAttributeValue.class, StringAttributeValue.BAG_TYPE) }//
				);
	}

	protected HigherOrderFunctionsTest(String functionName, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs, ExpressionResult<? extends AttributeValue> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

}
