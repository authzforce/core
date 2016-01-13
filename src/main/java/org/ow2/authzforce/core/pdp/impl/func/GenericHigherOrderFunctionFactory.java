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

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.PdpExtension;

/**
 * Interface for generic higher-order function factories, e.g. {@link MapFunctionFactory}. A generic function is a function class with a type parameter
 * depending on the sub-function's return type, e.g. {@link MapFunctionFactory}, therefore the function is instantiated for a specific sub-function's return
 * type.
 * 
 */
public abstract class GenericHigherOrderFunctionFactory implements PdpExtension
{
	/**
	 * Returns instance of the Higher-order function
	 * 
	 * @param subFunctionReturnTypeFactory
	 *            sub-function's return datatype factory
	 * @return higher-order function instance
	 */
	public abstract <SUB_RETURN_T extends AttributeValue> HigherOrderBagFunction<?, SUB_RETURN_T> getInstance(
			DatatypeFactory<SUB_RETURN_T> subFunctionReturnTypeFactory);

	@Override
	public String toString()
	{
		return this.getId();
	}

}
