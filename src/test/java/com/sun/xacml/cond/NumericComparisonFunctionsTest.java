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
package com.sun.xacml.cond;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;

@RunWith(Parameterized.class)
public class NumericComparisonFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_INTEGER_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than";
	private static final String NAME_INTEGER_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal";
	private static final String NAME_INTEGER_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:integer-less-than";
	private static final String NAME_INTEGER_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal";
	private static final String NAME_DOUBLE_GREATER_THAN = "urn:oasis:names:tc:xacml:1.0:function:double-greater-than";
	private static final String NAME_DOUBLE_GREATER_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal";
	private static final String NAME_DOUBLE_LESS_THAN = "urn:oasis:names:tc:xacml:1.0:function:double-less-than";
	private static final String NAME_DOUBLE_LESS_THAN_OR_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
				new Object[] {
						NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_GREATER_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than
				new Object[] {
						NAME_INTEGER_LESS_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_LESS_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_LESS_THAN,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
				new Object[] {
						NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_INTEGER_LESS_THAN_OR_EQUAL,
						Arrays.asList(IntegerAttribute.getInstance("5"),
								IntegerAttribute.getInstance("5")),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than
				new Object[] {
						NAME_DOUBLE_GREATER_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
				new Object[] {
						NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_GREATER_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(true) },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than
				new Object[] {
						NAME_DOUBLE_LESS_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(false) },

				// urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal
				new Object[] {
						NAME_DOUBLE_LESS_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.4")),
						EvaluationResult.getInstance(false) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.6")),
						EvaluationResult.getInstance(true) },
				new Object[] {
						NAME_DOUBLE_LESS_THAN_OR_EQUAL,
						Arrays.asList(DoubleAttribute.getInstance("5.5"),
								DoubleAttribute.getInstance("5.5")),
						EvaluationResult.getInstance(true) });
	}

	public NumericComparisonFunctionsTest(String functionName,
			List<ExpressionType> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
