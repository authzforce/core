/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.NotFunction;
import com.sun.xacml.StringNormalizeFunction;
import com.sun.xacml.TimeInRangeFunction;

/**
 * This factory supports the standard set of functions specified in XACML 1.x and 2.0 and 3.0.
 * <p>
 * Note that because this supports only the standard functions, this factory does not allow the addition of any other functions. If you call {@link #addFunction(Function)} on an instance of this
 * class, an exception will be thrown. If you need a standard factory that is modifiable, you can either create a new <code>BasePdpExtensionRegistry</code> (or some other implementation of
 * <code>PdpExtensionRegistry</code>) populated with the standard functions from {@code this#getSupportedFunctions()}
 *
 * 
 * @version $Id: $
 */
public final class StandardFunctionRegistry extends FunctionRegistry
{

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardFunctionRegistry.class);

	private static final PdpExtensionComparator<Function<?>> FUNCTION_COMPARATOR = new PdpExtensionComparator<>();
	private static final PdpExtensionComparator<GenericHigherOrderFunctionFactory> FUNCTION_FACTORY_COMPARATOR = new PdpExtensionComparator<>();

	private static final StandardFunctionRegistry NON_XPATH_FUNCTIONS;
	private static final StandardFunctionRegistry ALL_FUNCTIONS;

	static
	{
		final Set<Function<?>> nonGenericFunctions = new HashSet<>();
		/*
		 * Add standard functions in an order as close as possible to the order of declaration in the XACML spec (A.3).
		 */

		/*
		 * Match functions taking only one type of parameter: Equality predicates (A.3.1) + special match function 'x500Name-match' (A.3.14)
		 */
		nonGenericFunctions.addAll(StandardEqualTypeMatchFunctions.SET.getSupportedFunctions());

		/*
		 * Numeric Arithmetic functions (A.3.2)
		 */
		nonGenericFunctions.addAll(NumericArithmeticFunction.SET.getSupportedFunctions());

		/*
		 * String-normalize functions (= A.3.3 String conversion functions in the spec)
		 */
		nonGenericFunctions.addAll(StringNormalizeFunction.SET.getSupportedFunctions());

		/*
		 * Primitive data-type conversion functions: A.3.4 Numeric data-type conversion functions, and all {type}-from-string / string-from-{type} functions from A.3.9 (other parts of A.3.9 addressed
		 * below by StringConcatenateFunction, NonEqualTypeMatchFunction, SubstringFunction)
		 */
		nonGenericFunctions.addAll(StandardDatatypeConversionFunctions.SET.getSupportedFunctions());

		/*
		 * Logical functions (A.3.5)
		 */
		nonGenericFunctions.add(LogicalOrFunction.INSTANCE);
		nonGenericFunctions.add(LogicalAndFunction.INSTANCE);
		nonGenericFunctions.add(LogicalNOfFunction.INSTANCE);
		nonGenericFunctions.add(NotFunction.INSTANCE);

		/*
		 * Total-ordering comparison functions (all elements of a given type can be compared to each other), i.e. numeric (A.3.6) and string comparison functions (first part of A.3.8)
		 */
		nonGenericFunctions.addAll(StandardComparisonFunctions.TOTAL_ORDER_SET.getSupportedFunctions());

		/*
		 * Date and time arithmetic functions (A.3.7)
		 */
		nonGenericFunctions.addAll(TemporalArithmeticFunction.SET.getSupportedFunctions());

		/*
		 * Date and time comparison functions (second part of A.3.8, first part already addressed above by TotalComparisonFunction)
		 */
		nonGenericFunctions.addAll(StandardComparisonFunctions.TEMPORAL_SET.getSupportedFunctions());
		nonGenericFunctions.add(TimeInRangeFunction.INSTANCE);

		/*
		 * String-concatenate function (start of A.3.9, other parts addressed above by DatatypeConversionFunction, and below by NonEqualTypeMatchFunction and SubstringFunction)
		 */
		nonGenericFunctions.add(StringConcatenateFunction.INSTANCE);

		/*
		 * Match functions taking parameters of possibly different types, i.e. *-contains / *-starts-with / *-ends-with (second before last part of A.3.9, other parts addressed above by
		 * DatatypeConversionFunction, StringConcatenateFunction, and below by SubstringFunction), regexp-match (A.3.13) and special match 'rfc822Name-match' (part of A.3.14, other part addressed
		 * above by EqualTypeMatchFunction)
		 */
		nonGenericFunctions.addAll(StandardNonEqualTypeMatchFunctions.SET.getSupportedFunctions());

		/*
		 * Substring functions (last part of A.3.9, other parts addressed above by DatatypeConversionFunction, StringConcatenateFunction, NonEqualTypeMatchFunction)
		 */
		nonGenericFunctions.addAll(SubstringFunction.SET.getSupportedFunctions());

		/*
		 * First-order bag functions (A.3.10, A.3.11)
		 */
		nonGenericFunctions.addAll(StandardFirstOrderBagFunctions.SET.getSupportedFunctions());

		/*
		 * Higher-order bag functions (A.3.12)
		 */
		nonGenericFunctions.addAll(StandardHigherOrderBagFunctions.SET.getSupportedFunctions());

		/*
		 * A.3.13 already addressed above by NonEqualTypeMatchFunction
		 */
		/*
		 * A.3.14 already addressed above by EqualTypeMatchFunction and NonEqualTypeMatchFunction
		 */
		final BasePdpExtensionRegistry<Function<?>> nonGenericFuncRegistry = new BasePdpExtensionRegistry<>(Function.class, Collections.unmodifiableSet(nonGenericFunctions));

		// Generic functions, e.g. map function
		final Set<GenericHigherOrderFunctionFactory> genericFuncFactories = Collections.singleton(MapFunctionFactory.INSTANCE);
		final BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFuncFactoryRegistry = new BasePdpExtensionRegistry<>(GenericHigherOrderFunctionFactory.class, genericFuncFactories);

		NON_XPATH_FUNCTIONS = new StandardFunctionRegistry(nonGenericFuncRegistry, genericFuncFactoryRegistry);

		/*
		 * Optional functions
		 */
		/*
		 * A.3.15 functions only xpath-node-count supported
		 */
		nonGenericFuncRegistry.addExtension(XPathNodeCountFunction.INSTANCE);
		ALL_FUNCTIONS = new StandardFunctionRegistry(nonGenericFuncRegistry, genericFuncFactoryRegistry);

		/*
		 * A.3.16 not supported
		 */
		if (LOGGER.isDebugEnabled())
		{
			// TreeSet for sorting functions, easier to read
			final TreeSet<Function<?>> sortedFunctions = new TreeSet<>(FUNCTION_COMPARATOR);
			sortedFunctions.addAll(nonGenericFunctions);
			LOGGER.debug("Loaded XACML standard non-generic functions: {}", sortedFunctions);

			final TreeSet<GenericHigherOrderFunctionFactory> sortedFunctionFactories = new TreeSet<>(FUNCTION_FACTORY_COMPARATOR);
			sortedFunctionFactories.addAll(genericFuncFactories);
			LOGGER.debug("Loaded XACML standard generic functions: {}", sortedFunctionFactories);
		}
	}

	/**
	 * Creates a new StandardFunctionRegistry, making sure that the default maps are initialized correctly. Standard factories can't be modified, so there is no notion of supersetting since that's
	 * only used for correctly propagating new functions.
	 * 
	 * @param functionRegistry
	 * @param genericFunctionFactoryRegistry
	 */
	private StandardFunctionRegistry(BasePdpExtensionRegistry<Function<?>> functionRegistry, BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFunctionFactoryRegistry)
	{
		super(functionRegistry, genericFunctionFactoryRegistry);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Always throws an exception, since support for new functions may not be added to a standard factory.
	 */
	@Override
	public void addFunction(Function<?> function) throws IllegalArgumentException
	{
		throw new UnsupportedOperationException("A standard factory cannot be modified");
	}

	/**
	 * Get standard function registry
	 *
	 * @param enableXPath
	 *            true iff XPath-based function(s) support enabled
	 * @return standard function registry
	 */
	public static FunctionRegistry getInstance(boolean enableXPath)
	{
		return enableXPath ? ALL_FUNCTIONS : NON_XPATH_FUNCTIONS;
	}

}
