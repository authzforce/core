/**
 * Copyright 2012-2019 THALES.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ConstantPrimitiveAttributeValueExpression;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.FunctionExpression;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionCall;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.PrimitiveValue;
import org.ow2.authzforce.core.pdp.api.value.StandardAttributeValueFactories;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.impl.expression.DepthLimitingExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * An abstract class to easily test a function evaluation, according to a given function name, a list of arguments, and expected result. In order to perform a function test, simply extend this class
 * and give the test values on construction.
 * 
 */
public abstract class StandardFunctionTest
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
			STD_EXPRESSION_FACTORY = new DepthLimitingExpressionFactory(StandardAttributeValueFactories.getRegistry(true, Optional.empty()),
			        StandardFunction.getRegistry(true, StandardAttributeValueFactories.BIG_INTEGER), null, 0, false, false);
		} catch (IllegalArgumentException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final Map<Class<?>, AttributeDatatype<?>> JAVA_CLASS_TO_DATATYPE_MAP = StandardDatatypes.MANDATORY_SET.stream().collect(Collectors.toMap(dt -> dt.getInstanceClass(), dt -> dt));

	private FunctionCall<?> funcCall;
	private final Value expectedResult;
	private final String toString;
	private final boolean areBagsComparedAsSets;
	private boolean isTestOkBeforeFuncCall = false;

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
	private StandardFunctionTest(final String functionName, final List<Expression<?>> inputs, final boolean compareBagsAsSets, final Value expectedResult)
	{
		// Determine whether this is a higher-order function, i.e. first parameter is a sub-function
		final Datatype<? extends AttributeValue> subFuncReturnType;
		if (inputs.isEmpty())
		{
			subFuncReturnType = null;
		} else
		{
			final Expression<?> xpr0 = inputs.get(0);
			if (xpr0 instanceof FunctionExpression)
			{
				subFuncReturnType = ((FunctionExpression) xpr0).getValue().get().getReturnType();
			} else
			{
				subFuncReturnType = null;
			}
		}

		final FunctionExpression functionExp = STD_EXPRESSION_FACTORY.getFunction(functionName, subFuncReturnType);
		if (functionExp == null)
		{
			throw new IllegalArgumentException("Function " + functionName + " not valid/supported "
			        + (subFuncReturnType == null ? "as first-order function" : "as higher-order function with sub-function return type = " + subFuncReturnType));
		}

		final Function<?> function = functionExp.getValue().get();

		try
		{
			funcCall = function.newCall(inputs);
		} catch (final IllegalArgumentException e)
		{
			/*
			 * Some syntax errors might be caught at initialization time, which is expected if expectedResult == null
			 */
			if (expectedResult != null)
			{
				/*
				 * IllegalArgumentException should not have been thrown, since we expect a result of the function call
				 */
				throw new RuntimeException("expectedResult != null but invalid args in test definition prevented the function call", e);
			}

			funcCall = null;
			// expectedResult == null
			isTestOkBeforeFuncCall = true;
		}

		/*
		 * If test not yet OK, we need to run the function call (funcCall.evaluate(...)), so funcCall must be defined
		 */
		if (!isTestOkBeforeFuncCall && funcCall == null)
		{
			throw new RuntimeException("Failed to initialize function call for unknown reason");
		}

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

	private static <V extends AttributeValue> Expression<?> createValueExpression(final Datatype<V> datatype, final AttributeValue rawValue)
	{
		// static expression only if not xpathExpression
		return new ConstantPrimitiveAttributeValueExpression<>(datatype, datatype.cast(rawValue));
	}

	private static <V extends Bag<?>> Expression<?> createValueExpression(final Datatype<V> datatype, final Bag<?> rawValue)
	{
		return new BagValueExpression<>(datatype, datatype.cast(rawValue));
	}

	private static final class IndeterminateExpression<V extends Value> implements Expression<V>
	{
		private final Datatype<V> returnType;

		private IndeterminateExpression(final Datatype<V> returnType)
		{
			this.returnType = returnType;
		}

		@Override
		public Datatype<V> getReturnType()
		{
			return returnType;
		}

		@Override
		public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			throw new IndeterminateEvaluationException("Missing attribute", XacmlStatusCode.MISSING_ATTRIBUTE.value());
		}

		@Override
		public Optional<V> getValue()
		{
			throw new UnsupportedOperationException("No constant defined for Indeterminate expression");
		}

	}

	// private static <V extends Value> IndeterminateExpression<V> newIndeterminateExpression

	private static final List<Expression<?>> toExpressions(final String subFunctionName, final List<Value> values)
	{
		final List<Expression<?>> inputExpressions = new ArrayList<>();
		if (subFunctionName != null)
		{
			// sub-function of higher-order function
			final FunctionExpression subFuncExp = STD_EXPRESSION_FACTORY.getFunction(subFunctionName);
			if (subFuncExp == null)
			{
				throw new UnsupportedOperationException("Function " + subFunctionName + " not valid/supported (as first-order function)");
			}

			inputExpressions.add(subFuncExp);
		}

		final AttributeValueFactoryRegistry stdDatatypeFactoryRegistry = StandardAttributeValueFactories.getRegistry(true, Optional.empty());
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
				final AttributeValueFactory<?> datatypeFactory = stdDatatypeFactoryRegistry.getExtension(nullVal.getDatatypeId());
				if (datatypeFactory == null)
				{
					throw new UnsupportedOperationException("Unsupported attribute datatype: '" + nullVal.getDatatypeId() + "'");
				}

				valExpr = nullVal.isBag() ? new IndeterminateExpression<>(datatypeFactory.getDatatype().getBagDatatype()) : new IndeterminateExpression<>(datatypeFactory.getDatatype());
			} else if (val instanceof AttributeValue)
			{
				final AttributeValue primVal = (AttributeValue) val;
				final AttributeValueFactory<?> datatypeFactory = stdDatatypeFactoryRegistry.getExtension(JAVA_CLASS_TO_DATATYPE_MAP.get(primVal.getClass()).getId());
				valExpr = createValueExpression(datatypeFactory.getDatatype(), primVal);
			} else if (val instanceof Bag)
			{
				final Bag<?> bagVal = (Bag<?>) val;
				final AttributeValueFactory<?> datatypeFactory = stdDatatypeFactoryRegistry.getExtension(bagVal.getElementDatatype().getId());
				valExpr = createValueExpression(datatypeFactory.getDatatype().getBagDatatype(), bagVal);
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
	public StandardFunctionTest(final String functionName, final String subFunctionName, final List<Value> inputs, final boolean compareBagsAsSets, final Value expectedResult)
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
	public StandardFunctionTest(final String functionName, final String subFunctionName, final List<Value> inputs, final Value expectedResult)
	{
		this(functionName, subFunctionName, inputs, false, expectedResult);
	}

	private static final Set<PrimitiveValue> bagToSet(final Bag<?> bag)
	{
		final Set<PrimitiveValue> set = new HashSet<>();
		for (final PrimitiveValue val : bag)
		{
			set.add(val);
		}

		return set;
	}

	@Test
	public void testEvaluate() throws IndeterminateEvaluationException
	{
		if (isTestOkBeforeFuncCall)
		{
			/*
			 * Test already OK (syntax error was expected and occured when creating the function call already), no need to carry on the function call
			 */
			return;
		}

		/*
		 * Use null context as all inputs given as values in function tests, therefore already provided as inputs to function call
		 */
		try
		{
			/*
			 * funcCall != null (see constructor)
			 */
			final Value actualResult = funcCall.evaluate(null);
			if (expectedResult instanceof Bag && actualResult instanceof Bag && areBagsComparedAsSets)
			{
				final Set<?> expectedSet = bagToSet((Bag<?>) expectedResult);
				final Set<?> actualSet = bagToSet((Bag<?>) actualResult);
				Assert.assertEquals(toString, expectedSet, actualSet);
			} else if (expectedResult != null)
			{
				Assert.assertEquals(toString, expectedResult, actualResult);
			}
		} catch (final IndeterminateEvaluationException e)
		{
			if (expectedResult != null)
			{
				// unexpected error
				throw e;
			}
		}
	}
}
