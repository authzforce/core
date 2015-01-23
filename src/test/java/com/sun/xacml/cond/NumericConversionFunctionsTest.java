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
public class NumericConversionFunctionsTest extends GeneralFunctionTest {

	private static final String NAME_DOUBLE_TO_INTEGER = "urn:oasis:names:tc:xacml:1.0:function:double-to-integer";
	private static final String NAME_INTEGER_TO_DOUBLE = "urn:oasis:names:tc:xacml:1.0:function:integer-to-double";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception {
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:double-to-integer
				new Object[] { NAME_DOUBLE_TO_INTEGER,
						Arrays.asList(DoubleAttribute.getInstance("5.25")),
						new EvaluationResult(IntegerAttribute.getInstance("5")) },
						new Object[] {
								NAME_DOUBLE_TO_INTEGER,
								Arrays.asList(DoubleAttribute
										.getInstance("5.75")),
								new EvaluationResult(IntegerAttribute
										.getInstance("5")) },

						// urn:oasis:names:tc:xacml:1.0:function:integer-to-double
						new Object[] {
								NAME_INTEGER_TO_DOUBLE,
								Arrays.asList(IntegerAttribute.getInstance("5")),
								new EvaluationResult(DoubleAttribute
										.getInstance("5.")) });
	}

	public NumericConversionFunctionsTest(String functionName,
			List<ExpressionType> inputs, EvaluationResult expectedResult)
			throws Exception {
		super(functionName, inputs, expectedResult);
	}

}
