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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FunctionCall;
import com.thalesgroup.authzforce.core.func.StandardFunctionRegistry;

/**
 * An abstract class to easily test a function evaluation, according to a given function name, a
 * list of arguments, and expected result. In order to perform a function test, simply extend this
 * class and give the test values on construction.
 * 
 */
public abstract class GeneralFunctionTest
{
	private final Function<? extends ExpressionResult<? extends AttributeValue>> function;
	private final FunctionCall<? extends ExpressionResult<? extends AttributeValue>> funcCall;
	private final ExpressionResult<? extends AttributeValue> expectedResult;

	/**
	 * 
	 * @param functionName
	 *            The fully qualified name of the function to be tested. The function must be
	 *            supported by the StandardFunctionRegistry.
	 * @param inputs
	 *            The list of the function arguments, in order.
	 * @param expectedResult
	 *            The expected function evaluation result, according to the given inputs; null if
	 *            evaluation expected to throw an error (IndeterminateEvaluationException)
	 */
	protected GeneralFunctionTest(final String functionName, final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs, final ExpressionResult<? extends AttributeValue> expectedResult)
	{
		this.function = StandardFunctionRegistry.INSTANCE.getFunction(functionName);
		// -> function is null if not supported

		if (function == null)
		{
			funcCall = null;
		} else
		{
			funcCall = function.newCall(inputs);
		}

		this.expectedResult = expectedResult;
	}

	@Before
	public void skipIfFunctionNotSupported()
	{
		// assume test OK if function not supported -> skip it
		org.junit.Assume.assumeTrue(function == null);
	}

	@Test
	public void testEvaluate()
	{
		// Validate inputs and create function call
		/*
		 * Use null context as all inputs given as values in function tests, therefore already
		 * provided as inputs to function call
		 */
		try
		{
			ExpressionResult<? extends AttributeValue> actualResult = funcCall.evaluate(null);
			Assert.assertEquals(expectedResult, actualResult);
		} catch (IndeterminateEvaluationException e)
		{
			// expectedResult must be null to indicate we expect an evaluation error
			Assert.assertNull("Expected evaluation error", expectedResult);

		}
	}
}
