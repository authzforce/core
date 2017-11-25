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
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Set;

import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * Function registry
 */
public interface FunctionRegistry
{

	/**
	 * Get a (non-generic) function by ID. 'Non-generic' here means the function is either first-order, or higher-order but does not need the specific sub-function parameter to be instantiated.
	 *
	 * @param functionId
	 *            ID of function to loop up
	 * @return function instance, null if none with such ID in the registry of non-generic functions, in which case it may be a generic function and you should try
	 *         {@link #getFunction(String, Datatype)} instead.
	 */
	Function<?> getFunction(String functionId);

	/**
	 * Get any function including generic ones. 'Generic' here means the function is a higher-order function that is instantiated for a specific sub-function. For instance, the XACML 'map' function
	 * function class takes the sub-function's return type as type parameter and therefore it needs this sub-function's return type to be instantiated.
	 *
	 * @param functionId
	 *            function ID
	 * @param subFunctionReturnType
	 *            sub-function return type
	 * @return function instance
	 */
	Function<?> getFunction(String functionId, Datatype<? extends AttributeValue> subFunctionReturnType);

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