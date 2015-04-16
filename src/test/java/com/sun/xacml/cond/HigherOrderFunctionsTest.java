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
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.Apply;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

@RunWith(Parameterized.class)
public class HigherOrderFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_ANY_OF = "urn:oasis:names:tc:xacml:3.0:function:any-of";
	private static final String NAME_ALL_OF = "urn:oasis:names:tc:xacml:3.0:function:all-of";
	private static final String NAME_ANY_OF_ANY = "urn:oasis:names:tc:xacml:3.0:function:any-of-any";
	private static final String NAME_ALL_OF_ANY = "urn:oasis:names:tc:xacml:1.0:function:all-of-any";
	private static final String NAME_ANY_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:any-of-all";
	private static final String NAME_ALL_OF_ALL = "urn:oasis:names:tc:xacml:1.0:function:all-of-all";
	private static final String NAME_MAP = "urn:oasis:names:tc:xacml:3.0:function:map";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:3.0:function:any-of
				new Object[] {
						NAME_ANY_OF,
						Arrays.asList(
								FUNCTION_FACTORY
										.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
								StringAttribute.getInstance("Paul"),
								new Apply(
										BagFunction
												.getBagInstance(
														"urn:oasis:names:tc:xacml:1.0:function:string-bag",
														StringAttribute.identifier),
										Arrays.asList(
												(ExpressionType) StringAttribute
														.getInstance("John"),
												(ExpressionType) StringAttribute
														.getInstance("Paul"),
												(ExpressionType) StringAttribute
														.getInstance("George"),
												(ExpressionType) StringAttribute
														.getInstance("Ringo")))),
						EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANY_OF,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
										StringAttribute.getInstance("Paul"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("John"),
														(ExpressionType) StringAttribute
																.getInstance("George"),
														(ExpressionType) StringAttribute
																.getInstance("Ringo")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:all-of
						new Object[] {
								NAME_ALL_OF,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										IntegerAttribute.getInstance("10"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("9"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4"),
														(ExpressionType) IntegerAttribute
																.getInstance("2")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ALL_OF,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										IntegerAttribute.getInstance("10"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("9"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("14"),
														(ExpressionType) IntegerAttribute
																.getInstance("2")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:any-of-any
						new Object[] {
								NAME_ANY_OF_ANY,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("Ringo"),
														(ExpressionType) StringAttribute
																.getInstance("Mary"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("John"),
														(ExpressionType) StringAttribute
																.getInstance("Paul"),
														(ExpressionType) StringAttribute
																.getInstance("George"),
														(ExpressionType) StringAttribute
																.getInstance("Ringo")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANY_OF_ANY,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("Ringo"),
														(ExpressionType) StringAttribute
																.getInstance("Mary"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("John"),
														(ExpressionType) StringAttribute
																.getInstance("Paul"),
														(ExpressionType) StringAttribute
																.getInstance("George")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:all-of-any
						new Object[] {
								NAME_ALL_OF_ANY,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("10"),
														(ExpressionType) IntegerAttribute
																.getInstance("20"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("5"),
														(ExpressionType) IntegerAttribute
																.getInstance("19")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ALL_OF_ANY,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("10"),
														(ExpressionType) IntegerAttribute
																.getInstance("20"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("11"),
														(ExpressionType) IntegerAttribute
																.getInstance("13"),
														(ExpressionType) IntegerAttribute
																.getInstance("15"),
														(ExpressionType) IntegerAttribute
																.getInstance("19")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:any-of-all
						new Object[] {
								NAME_ANY_OF_ALL,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("5"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ANY_OF_ALL,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:1.0:function:all-of-all
						new Object[] {
								NAME_ALL_OF_ALL,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("6"),
														(ExpressionType) IntegerAttribute
																.getInstance("5"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4")))),
								EvaluationResult.getInstance(true) },
						new Object[] {
								NAME_ALL_OF_ALL,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("5"))),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
																IntegerAttribute.identifier),
												Arrays.asList(
														(ExpressionType) IntegerAttribute
																.getInstance("1"),
														(ExpressionType) IntegerAttribute
																.getInstance("2"),
														(ExpressionType) IntegerAttribute
																.getInstance("3"),
														(ExpressionType) IntegerAttribute
																.getInstance("4")))),
								EvaluationResult.getInstance(false) },

						// urn:oasis:names:tc:xacml:3.0:function:map
						new Object[] {
								NAME_MAP,
								Arrays.asList(
										FUNCTION_FACTORY
												.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case"),
										new Apply(
												BagFunction
														.getBagInstance(
																"urn:oasis:names:tc:xacml:1.0:function:string-bag",
																StringAttribute.identifier),
												Arrays.asList(
														(ExpressionType) StringAttribute
																.getInstance("Hello"),
														(ExpressionType) StringAttribute
																.getInstance("World!")))),
								new EvaluationResult(
										new BagAttribute(
												StringAttribute.identifier,
												Arrays.asList(
														(AttributeValue) StringAttribute
																.getInstance("hello"),
														(AttributeValue) StringAttribute
																.getInstance("world!")))) });
	}

	public HigherOrderFunctionsTest(String functionName,
			List<ExpressionType> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
