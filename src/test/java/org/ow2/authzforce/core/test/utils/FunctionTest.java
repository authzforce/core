/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.test.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionCall;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.impl.expression.ExpressionFactoryImpl;
import org.ow2.authzforce.core.pdp.impl.expression.PrimitiveValueExpression;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.value.StandardDatatypeFactoryRegistry;

import com.sun.xacml.UnknownIdentifierException;

/**
 * An abstract class to easily test a function evaluation, according to a given function name, a list of arguments, and expected result. In order to perform a function test, simply extend this class
 * and give the test values on construction.
 * 
 */
public abstract class FunctionTest
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(GeneralFunctionTest.class);

	/**
	 * XACML standard Expression factory/parser
	 */
	private static final ExpressionFactory STD_EXPRESSION_FACTORY;
	static
	{
		try
		{
			STD_EXPRESSION_FACTORY = new ExpressionFactoryImpl(StandardDatatypeFactoryRegistry.MANDATORY_DATATYPES, StandardFunctionRegistry.getInstance(true), null, 0, false, false);
		} catch (IllegalArgumentException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private final FunctionCall<?> funcCall;
	private final Value expectedResult;
	private final String toString;
	private final boolean areBagsComparedAsSets;

	/**
	 * Creates instance
	 * 
	 * @param functionName
	 *            The fully qualified name of the function to be tested. The function must be supported by the StandardFunctionRegistry.
	 * @param inputs
	 *            The list of the function arguments as expressions, in order.
	 * @param expectedResult
	 *            The expected function evaluation result, according to the given inputs; null if evaluation expected to throw an error (IndeterminateEvaluationException)
	 * @param compareBagsAsSets
	 *            true iff result bags should be compared as sets for equality check
	 * @throws UnknownIdentifierException
	 */
	private FunctionTest(final String functionName, final List<Expression<?>> inputs, boolean compareBagsAsSets, final Value expectedResult)
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

		final Function<?> function = STD_EXPRESSION_FACTORY.getFunction(functionName, subFuncReturnType);
		if (function == null)
		{
			throw new IllegalArgumentException("Function " + functionName + (subFuncReturnType == null ? "" : "(sub-function return type = " + subFuncReturnType + ")") + " not valid/supported");
		}

		funcCall = function.newCall(inputs);

		this.expectedResult = expectedResult;
		this.toString = function + "( " + inputs + " )";

		this.areBagsComparedAsSets = compareBagsAsSets;
	}

	// @Before
	// public void skipIfFunctionNotSupported()
	// {
	// // assume test OK if function not supported -> skip it
	// org.junit.Assume.assumeTrue(function == null);
	// }

	private static <V extends AttributeValue> Expression<?> createValueExpression(Datatype<V> datatype, AttributeValue rawValue)
	{
		// static expression only if not xpathExpression
		return new PrimitiveValueExpression<>(datatype, datatype.cast(rawValue), datatype != StandardDatatypes.XPATH_FACTORY.getDatatype());
	}

	private static <V extends Bag<?>> Expression<?> createValueExpression(Datatype<V> datatype, Bag<?> rawValue)
	{
		return new BagValueExpression<>(datatype, datatype.cast(rawValue));
	}

	private static final class IndeterminateExpression<V extends Value> implements Expression<V>
	{
		private final Datatype<V> returnType;

		private IndeterminateExpression(Datatype<V> returnType)
		{
			this.returnType = returnType;
		}

		@Override
		public Datatype<V> getReturnType()
		{
			return returnType;
		}

		@Override
		public V evaluate(EvaluationContext context) throws IndeterminateEvaluationException
		{
			throw new IndeterminateEvaluationException("Missing attribute", StatusHelper.STATUS_MISSING_ATTRIBUTE);
		}

		@Override
		public boolean isStatic()
		{
			return true;
		}

		@Override
		public JAXBElement<? extends ExpressionType> getJAXBElement()
		{
			return null;
		}

	}

	private static final List<Expression<?>> toExpressions(String subFunctionName, List<Value> values)
	{
		final List<Expression<?>> inputExpressions = new ArrayList<>();
		if (subFunctionName != null)
		{
			// sub-function of higher-order function
			final Function<?> subFunc = STD_EXPRESSION_FACTORY.getFunction(subFunctionName);
			if (subFunc == null)
			{
				throw new UnsupportedOperationException("Function " + subFunctionName + " not valid/supported (as first-order function)");
			}

			inputExpressions.add(subFunc);
		}

		for (final Value val : values)
		{
			final Expression<?> valExpr;
			if (val instanceof NullValue)
			{
				/*
				 * Undefined arg -> wrap in a special expression that always return Indeterminate (useful for testing functions that do not need all arguments to return a result, such as logical
				 * or/and/n-o
				 */
				final NullValue nullVal = (NullValue) val;
				final DatatypeFactory<?> datatypeFactory = StandardDatatypeFactoryRegistry.ALL_DATATYPES.getExtension(nullVal.getDatatypeId());
				if (datatypeFactory == null)
				{
					throw new UnsupportedOperationException("Unsupported attribute datatype: '" + nullVal.getDatatypeId() + "'");
				}

				valExpr = new IndeterminateExpression<>(nullVal.isBag() ? datatypeFactory.getBagDatatype() : datatypeFactory.getDatatype());
			} else if (val instanceof AttributeValue)
			{
				final AttributeValue primVal = (AttributeValue) val;
				final DatatypeFactory<?> datatypeFactory = StandardDatatypeFactoryRegistry.ALL_DATATYPES.getExtension(primVal.getDataType());
				valExpr = createValueExpression(datatypeFactory.getDatatype(), primVal);
			} else if (val instanceof Bag)
			{
				final Bag<?> bagVal = (Bag<?>) val;
				final DatatypeFactory<?> datatypeFactory = StandardDatatypeFactoryRegistry.ALL_DATATYPES.getExtension(bagVal.getElementDatatype().getId());
				valExpr = createValueExpression(datatypeFactory.getBagDatatype(), bagVal);
			} else
			{
				throw new UnsupportedOperationException("Unsupported type of Value: " + val.getClass());
			}

			inputExpressions.add(valExpr);
		}

		return inputExpressions;
	}

	/**
	 * Creates instance
	 * 
	 * @param functionName
	 *            The fully qualified name of the function to be tested. The function must be supported by the StandardFunctionRegistry.
	 * @param subFunctionName
	 *            (optional) sub-function specified iff {@code functionName} corresponds to a higher-order function; else null
	 * @param inputs
	 *            The list of the function arguments as constant values, in order. Specify a null argument to indicate it is undefined. It will be considered as Indeterminate (wrapped in a Expression
	 *            that always evaluate to Indeterminate result). This is useful to test specific function behavior when one (or more) of the arguments is indeterminate; e.g. logical or/and/n-of
	 *            functions are able to return False/True even if some of the arguments are Indeterminate.
	 * @param expectedResult
	 *            The expected function evaluation result, according to the given inputs; null if evaluation expected to throw an error (IndeterminateEvaluationException)
	 * @param compareBagsAsSets
	 *            true iff result bags should be compared as sets for equality check
	 */
	public FunctionTest(String functionName, String subFunctionName, List<Value> inputs, boolean compareBagsAsSets, Value expectedResult)
	{
		this(functionName, toExpressions(subFunctionName, inputs), compareBagsAsSets, expectedResult);
	}

	/**
	 * Creates instance
	 * 
	 * @param functionName
	 *            The fully qualified name of the function to be tested. The function must be supported by the StandardFunctionRegistry.
	 * @param subFunctionName
	 *            (optional) sub-function specified iff {@code functionName} corresponds to a higher-order function; else null
	 * @param inputs
	 *            The list of the function arguments, as constant values, in order.
	 * @param expectedResult
	 *            The expected function evaluation result, according to the given inputs; null if evaluation expected to throw an error (IndeterminateEvaluationException)
	 */
	public FunctionTest(final String functionName, String subFunctionName, final List<Value> inputs, final Value expectedResult)
	{
		this(functionName, subFunctionName, inputs, false, expectedResult);
	}

	private static final Set<AttributeValue> bagToSet(Bag<?> bag)
	{
		final Set<AttributeValue> set = new HashSet<>();
		for (AttributeValue val : bag)
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
		 * Use null context as all inputs given as values in function tests, therefore already provided as inputs to function call
		 */
		try
		{
			Value actualResult = funcCall.evaluate(null);
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
