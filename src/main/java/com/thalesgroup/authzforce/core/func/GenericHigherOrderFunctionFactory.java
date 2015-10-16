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
package com.thalesgroup.authzforce.core.func;

import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;

/**
 * Interface for generic higher-order function factories, e.g. {@link MapFunction}. A generic
 * function is a function class with a type parameter depending on the sub-function's return type,
 * e.g. {@link MapFunction.Factory}, therefore the function is instantiated for a specific
 * sub-function's return type.
 * 
 */
public interface GenericHigherOrderFunctionFactory extends PdpExtension
{
	/**
	 * Returns instance of the Higher-order function
	 * 
	 * @param subFunctionReturnType
	 *            sub-function's return class
	 * @return higher-order function instance
	 */
	<SUB_RETURN_T extends AttributeValue<SUB_RETURN_T>> HigherOrderBagFunction<?, SUB_RETURN_T> getInstance(Datatype<SUB_RETURN_T> subFunctionReturnType);
}
