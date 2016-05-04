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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.func;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.impl.BasePdpExtensionRegistry;

/**
 * <p>FunctionRegistry class.</p>
 *
 * 
 * @version $Id: $
 */
public class FunctionRegistry
{

	private final BasePdpExtensionRegistry<Function<?>> nonGenericFunctionRegistry;
	private final BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericHigherOrderFunctionFactoryRegistry;

	/**
	 * Low-level constructor
	 *
	 * @param nonGenericFunctionRegistry
	 *            (mandatory) non-generic function registry
	 * @param genericFunctionFactoryRegistry
	 *            (optional) generic function factory registry
	 */
	protected FunctionRegistry(BasePdpExtensionRegistry<Function<?>> nonGenericFunctionRegistry, BasePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericFunctionFactoryRegistry)
	{
		this.nonGenericFunctionRegistry = new BasePdpExtensionRegistry<>(Function.class, nonGenericFunctionRegistry);
		this.genericHigherOrderFunctionFactoryRegistry = genericFunctionFactoryRegistry == null ? null : new BasePdpExtensionRegistry<>(GenericHigherOrderFunctionFactory.class,
				genericFunctionFactoryRegistry);
	}

	/**
	 * Constructor that sets a "base registry" from which this inherits all the extensions. Used for instance to build a new registry based on a standard one (e.g. {@link StandardFunctionRegistry} for
	 * standard functions).
	 *
	 * @param baseRegistry
	 *            the base/parent registry on which this one is based or null
	 */
	public FunctionRegistry(FunctionRegistry baseRegistry)
	{
		this(baseRegistry == null ? new BasePdpExtensionRegistry<Function<?>>(Function.class) : new BasePdpExtensionRegistry<>(Function.class, baseRegistry.nonGenericFunctionRegistry),
				baseRegistry == null ? new BasePdpExtensionRegistry<>(GenericHigherOrderFunctionFactory.class) : new BasePdpExtensionRegistry<>(GenericHigherOrderFunctionFactory.class,
						baseRegistry.genericHigherOrderFunctionFactoryRegistry));
	}

	/**
	 * Adds (non-generic) function
	 *
	 * @param function
	 *            function
	 */
	public void addFunction(Function<?> function)
	{
		nonGenericFunctionRegistry.addExtension(function);

	}

	/**
	 * Get a (non-generic) function by ID. 'Non-generic' here means the function is either first-order, or higher-order but does not need the specific sub-function parameter to be instantiated.
	 *
	 * @param functionId
	 *            ID of function to loop up
	 * @return function instance, null if none with such ID in the registry of non-generic functions, in which case it may be a generic function and you should try
	 *         {@link #getFunction(String, DatatypeFactory)} instead.
	 */
	public Function<?> getFunction(String functionId)
	{
		return nonGenericFunctionRegistry.getExtension(functionId);
	}

	/**
	 * Get any function including generic ones. 'Generic' here means the function is a higher-order function that is instantiated for a specific sub-function. For instance, the XACML 'map' function (
	 * {@link MapFunctionFactory}) function class takes the sub-function's return type as type parameter and therefore it needs this sub-function's return type to be instantiated (this is done via the
	 * {@link MapFunctionFactory}).
	 *
	 * @param functionId
	 *            function ID
	 * @param subFunctionReturnTypeFactory
	 *            sub-function return datatype factory
	 * @return function instance
	 */
	public <SUB_RETURN_T extends AttributeValue> Function<?> getFunction(String functionId, DatatypeFactory<SUB_RETURN_T> subFunctionReturnTypeFactory)
	{
		final Function<?> nonGenericFunc = nonGenericFunctionRegistry.getExtension(functionId);
		if (nonGenericFunc != null)
		{
			return nonGenericFunc;
		}

		if (genericHigherOrderFunctionFactoryRegistry == null)
		{
			return null;
		}

		final GenericHigherOrderFunctionFactory funcFactory = genericHigherOrderFunctionFactoryRegistry.getExtension(functionId);
		return funcFactory.getInstance(subFunctionReturnTypeFactory);
	}

}
