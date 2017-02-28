/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.impl.ImmutablePdpExtensionRegistry;

/**
 * <p>
 * Immutable function registry
 * </p>
 *
 * 
 * @version $Id: $
 */
public final class ImmutableFunctionRegistry implements FunctionRegistry
{

	private final ImmutablePdpExtensionRegistry<Function<?>> nonGenericFunctionRegistry;
	private final ImmutablePdpExtensionRegistry<GenericHigherOrderFunctionFactory> genericHigherOrderFunctionFactoryRegistry;

	/**
	 * Constructor
	 *
	 * @param nonGenericFunctions
	 *            (mandatory) non-generic functions
	 * @param genericFunctionFactories
	 *            (optional) generic function factories
	 */
	public ImmutableFunctionRegistry(final Set<Function<?>> nonGenericFunctions, final Set<GenericHigherOrderFunctionFactory> genericFunctionFactories)
	{
		this.nonGenericFunctionRegistry = new ImmutablePdpExtensionRegistry<>(Function.class, nonGenericFunctions);
		this.genericHigherOrderFunctionFactoryRegistry = genericFunctionFactories == null ? null : new ImmutablePdpExtensionRegistry<>(GenericHigherOrderFunctionFactory.class,
				genericFunctionFactories);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry#getFunction(java.lang.String)
	 */
	@Override
	public Function<?> getFunction(final String functionId)
	{
		return nonGenericFunctionRegistry.getExtension(functionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry#getFunction(java.lang.String, org.ow2.authzforce.core.pdp.api.value.DatatypeFactory)
	 */
	@Override
	public <SUB_RETURN_T extends AttributeValue> Function<?> getFunction(final String functionId, final DatatypeFactory<SUB_RETURN_T> subFunctionReturnTypeFactory)
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
		if (funcFactory == null)
		{
			return null;
		}

		return funcFactory.getInstance(subFunctionReturnTypeFactory);
	}

	@Override
	public Set<Function<?>> getNonGenericFunctions()
	{
		return this.nonGenericFunctionRegistry.getExtensions();
	}

	@Override
	public Set<GenericHigherOrderFunctionFactory> getGenericFunctionFactories()
	{
		return this.genericHigherOrderFunctionFactoryRegistry.getExtensions();
	}

}
