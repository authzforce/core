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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;

/**
 * Function registry
 */
public interface FunctionRegistry
{

	/**
	 * Get a (non-generic) function by ID. 'Non-generic' here means the function is either first-order, or higher-order
	 * but does not need the specific sub-function parameter to be instantiated.
	 *
	 * @param functionId
	 *            ID of function to loop up
	 * @return function instance, null if none with such ID in the registry of non-generic functions, in which case it
	 *         may be a generic function and you should try {@link #getFunction(String, DatatypeFactory)} instead.
	 */
	Function<?> getFunction(String functionId);

	/**
	 * Get any function including generic ones. 'Generic' here means the function is a higher-order function that is
	 * instantiated for a specific sub-function. For instance, the XACML 'map' function function class takes the
	 * sub-function's return type as type parameter and therefore it needs this sub-function's return type to be
	 * instantiated.
	 *
	 * @param functionId
	 *            function ID
	 * @param subFunctionReturnTypeFactory
	 *            sub-function return datatype factory
	 * @return function instance
	 */
	<SUB_RETURN_T extends AttributeValue> Function<?> getFunction(String functionId,
			DatatypeFactory<SUB_RETURN_T> subFunctionReturnTypeFactory);

	/**
	 * Get currently registered non-generic function
	 * 
	 * @return non-generic functions
	 */
	Set<Function<?>> getNonGenericFunctions();

	/**
	 * Get currently registered generic function factories
	 * 
	 * @return generic function factories
	 */
	Set<GenericHigherOrderFunctionFactory> getGenericFunctionFactories();

}