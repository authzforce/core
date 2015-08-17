package com.thalesgroup.authzforce.core.func;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.NotFunction;
import com.sun.xacml.cond.StringNormalizeFunction;
import com.sun.xacml.cond.TimeInRangeFunction;
import com.thalesgroup.authzforce.core.BasePdpExtensionRegistry;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

/**
 * This factory supports the standard set of functions specified in XACML 1.x and 2.0 and 3.0.
 * <p>
 * Note that because this supports only the standard functions, this factory does not allow the
 * addition of any other functions. If you call <code>addFunction</code> on an instance of this
 * class, an exception will be thrown. If you need a standard factory that is modifiable, you can
 * either create a new <code>BasePdpExtensionRegistry</code> (or some other implementation of
 * <code>PdpExtensionRegistry</code>) populated with the standard functions from
 * {@code this#getSupportedFunctions()}
 */
public class StandardFunctionRegistry extends FunctionRegistry
{

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardFunctionRegistry.class);

	/**
	 * Singleton function registry instance for standard functions
	 */
	public static StandardFunctionRegistry INSTANCE;
	static
	{
		final Set<Function<? extends ExpressionResult<? extends AttributeValue>>> standardExtensions = new HashSet<>();
		/*
		 * Add standard functions in an order as close as possible to the order of declaration in
		 * the XACML spec (A.3).
		 */

		/*
		 * Match functions taking only one type of parameter: Equality predicates (A.3.1) + special
		 * match function 'x500Name-match' (A.3.14)
		 */
		standardExtensions.addAll(EqualTypeMatchFunction.CLUSTER.getSupportedFunctions());

		/*
		 * Numeric Arithmetic functions (A.3.2)
		 */
		standardExtensions.addAll(NumericArithmeticFunction.CLUSTER.getSupportedFunctions());

		/*
		 * String-normalize functions (= A.3.3 String conversion functions in the spec)
		 */
		standardExtensions.addAll(StringNormalizeFunction.CLUSTER.getSupportedFunctions());

		/*
		 * Primitive data-type conversion functions: A.3.4 Numeric data-type conversion functions,
		 * and all {type}-from-string / string-from-{type} functions from A.3.9 (other parts of
		 * A.3.9 addressed below by StringConcatenateFunction, NonEqualTypeMatchFunction,
		 * SubstringFunction)
		 */
		standardExtensions.addAll(DatatypeConversionFunction.CLUSTER.getSupportedFunctions());

		/*
		 * Logical functions (A.3.5)
		 */
		standardExtensions.add(new LogicalOrFunction());
		standardExtensions.add(new LogicalAndFunction());
		standardExtensions.add(new LogicalNOfFunction());
		standardExtensions.add(new NotFunction());

		/*
		 * Total-ordering comparison functions (all elements of a given type can be compared to each
		 * other), i.e. numeric (A.3.6) and string comparison functions (first part of A.3.8)
		 */
		standardExtensions.addAll(TotalComparisonFunction.CLUSTER.getSupportedFunctions());

		/*
		 * Date and time arithmetic functions (A.3.7)
		 */
		standardExtensions.addAll(TemporalArithmeticFunction.CLUSTER.getSupportedFunctions());

		/*
		 * Date and time comparison functions (second part of A.3.8, first part already addressed
		 * above by TotalComparisonFunction)
		 */
		standardExtensions.addAll(TemporalComparisonFunction.CLUSTER.getSupportedFunctions());
		standardExtensions.add(new TimeInRangeFunction());

		/*
		 * String-concatenate function (start of A.3.9, other parts addressed above by
		 * DatatypeConversionFunction, and below by NonEqualTypeMatchFunction and SubstringFunction)
		 */
		standardExtensions.add(new StringConcatenateFunction());

		/*
		 * Match functions taking parameters of possibly different types, i.e. *-contains /
		 * *-starts-with / *-ends-with (second before last part of A.3.9, other parts addressed
		 * above by DatatypeConversionFunction, StringConcatenateFunction, and below by
		 * SubstringFunction), regexp-match (A.3.13) and special match 'rfc822Name-match' (part of
		 * A.3.14, other part addressed above by EqualTypeMatchFunction)
		 */
		standardExtensions.addAll(NonEqualTypeMatchFunction.CLUSTER.getSupportedFunctions());

		/*
		 * Substring functions (last part of A.3.9, other parts addressed above by
		 * DatatypeConversionFunction, StringConcatenateFunction, NonEqualTypeMatchFunction)
		 */
		standardExtensions.addAll(SubstringFunction.CLUSTER.getSupportedFunctions());

		/*
		 * (First-order single-)bag functions (A.3.10)
		 */
		standardExtensions.addAll(new FirstOrderBagFunctionSet.SingleBagFunctionSet().getSupportedFunctions());

		/*
		 * Set functions (A.3.11)
		 */
		standardExtensions.addAll(new FirstOrderBagFunctionSet.SetFunctionSet().getSupportedFunctions());

		/*
		 * Higher-order bag functions (A.3.12)
		 */
		standardExtensions.addAll(HigherOrderBagFunction.CLUSTER.getSupportedFunctions());

		/**
		 * A.3.13 already addressed above by NonEqualTypeMatchFunction
		 * <p>
		 * A.3.14 already addressed above by EqualTypeMatchFunction and NonEqualTypeMatchFunction
		 * <p>
		 * A.3.15 and A.3.16 (optional) not supported
		 */

		final Map<String, Function<? extends ExpressionResult<? extends AttributeValue>>> stdExtMap = new HashMap<>();
		for (final Function<? extends ExpressionResult<? extends AttributeValue>> stdExt : standardExtensions)
		{
			stdExtMap.put(stdExt.getId(), stdExt);
		}

		final BasePdpExtensionRegistry<Function<? extends ExpressionResult<? extends AttributeValue>>> nonGenericFuncRegistry = new  BasePdpExtensionRegistry<>(Collections.unmodifiableMap(stdExtMap));
		
		// Generic functions
		final GenericHigherOrderFunctionFactory mapFuncFactory = new MapFunction.Factory();
		final BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFuncFactoryRegistry = new BasePdpExtensionRegistry<>(Collections.singletonMap(mapFuncFactory.getId(), mapFuncFactory));

		INSTANCE = new StandardFunctionRegistry(nonGenericFuncRegistry, genericFuncFactoryRegistry);
	}

	/**
	 * Creates a new StandardFunctionRegistry, making sure that the default maps are initialized
	 * correctly. Standard factories can't be modified, so there is no notion of supersetting since
	 * that's only used for correctly propagating new functions.
	 * 
	 * @param functionRegistry
	 * @param genericFunctionFactoryRegistry
	 */
	private StandardFunctionRegistry(BasePdpExtensionRegistry<Function<? extends ExpressionResult<? extends AttributeValue>>> functionRegistry,
			BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFunctionFactoryRegistry)
	{
		super(functionRegistry, genericFunctionFactoryRegistry);
		LOGGER.info("Loaded standard functions");
	}

	/**
	 * Always throws an exception, since support for new functions may not be added to a standard
	 * factory.
	 * 
	 * @param function
	 *            the <code>Function</code> to add to the factory
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void addFunction(Function<? extends ExpressionResult<? extends AttributeValue>> function) throws IllegalArgumentException
	{
		throw new UnsupportedOperationException("a standard factory cannot be modified");
	}

}
