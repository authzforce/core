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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Bag;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.Expression.Value;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FunctionCall;
import com.thalesgroup.authzforce.core.test.utils.TestUtils;

/**
 * An abstract class to easily test a function evaluation, according to a given function name, a
 * list of arguments, and expected result. In order to perform a function test, simply extend this
 * class and give the test values on construction.
 * 
 */
public abstract class GeneralFunctionTest
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(GeneralFunctionTest.class);

	private final FunctionCall<?> funcCall;
	private final Value<?> expectedResult;
	private final String toString;
	private final boolean areBagsComparedAsSets;

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
	 * @throws UnknownIdentifierException
	 */
	protected GeneralFunctionTest(final String functionName, final List<Expression<?>> inputs, final Value<?> expectedResult)
	{
		this(functionName, inputs, expectedResult, false);
	}

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
	 * @param compareBagsAsSets
	 *            true iff result bags should be compared as sets for equality check
	 * @throws UnknownIdentifierException
	 */
	protected GeneralFunctionTest(final String functionName, final List<Expression<?>> inputs, final Value<?> expectedResult, boolean compareBagsAsSets)
	{
		// Determine whether this is a higher-order function, i.e. first parameter is a sub-function
		final Datatype<?> subFuncReturnType;
		if (inputs.isEmpty())
		{
			subFuncReturnType = null;
		} else
		{
			final Expression<?> xpr0 = inputs.get(0);
			if (xpr0 instanceof Function<?>)
			{
				subFuncReturnType = xpr0.getReturnType();
			} else
			{
				subFuncReturnType = null;
			}
		}

		try
		{
			final Function<?> function = TestUtils.STD_EXPRESSION_FACTORY.getFunction(functionName, subFuncReturnType);
			funcCall = function.newCall(inputs);

			this.expectedResult = expectedResult;
			this.toString = function + "( " + inputs + " )";
		} catch (UnknownIdentifierException e)
		{
			throw new RuntimeException(e);
		}

		this.areBagsComparedAsSets = compareBagsAsSets;
	}

	// @Before
	// public void skipIfFunctionNotSupported()
	// {
	// // assume test OK if function not supported -> skip it
	// org.junit.Assume.assumeTrue(function == null);
	// }

	private static final Set<AttributeValue<?>> bagToSet(Bag<?> bag)
	{
		final Set<AttributeValue<?>> set = new HashSet<>();
		for (AttributeValue<?> val : bag)
		{
			set.add(val);
		}

		return set;
	}

	@Test
	public void testEvaluate() throws IndeterminateEvaluationException
	{
		// Validate inputs and create function call
		/*
		 * Use null context as all inputs given as values in function tests, therefore already
		 * provided as inputs to function call
		 */
		try
		{
			Value<?> actualResult = funcCall.evaluate(null);
			if (expectedResult instanceof Bag && actualResult instanceof Bag && areBagsComparedAsSets)
			{
				Set<?> expectedSet = bagToSet((Bag<?>) expectedResult);
				Set<?> actualSet = bagToSet((Bag<?>) actualResult);
				Assert.assertEquals(toString, expectedSet, actualSet);
			} else if (expectedResult != null)
			{
				Assert.assertEquals(toString, expectedResult, actualResult);
			}
		} catch (IndeterminateEvaluationException e)
		{
			if (expectedResult != null)
			{
				// unexpected error
				throw e;
			}
		}
	}
}
