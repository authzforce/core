/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
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
	public Function<?> getFunction(final String functionId, final Datatype<? extends AttributeValue> subFunctionReturnType)
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

		return funcFactory.getInstance(subFunctionReturnType);
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
