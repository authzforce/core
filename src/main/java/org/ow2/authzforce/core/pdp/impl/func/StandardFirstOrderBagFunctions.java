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

import java.util.HashSet;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.func.BaseFunctionSet;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderBagFunctions;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionSet;
import org.ow2.authzforce.core.pdp.api.func.HigherOrderBagFunction;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * Standard first-order bag functions, as opposed to the higher-order bag functions (see {@link HigherOrderBagFunction}); standard first-order bag functions are
 * the Bag functions of section A.3.10, and the Set functions of A.3.11 of the XACML spec.
 * 
 * @version $Id: $
 */
public final class StandardFirstOrderBagFunctions
{
	private StandardFirstOrderBagFunctions()
	{
		// empty private constructor to prevent instantiation
	}

	private static Set<Function<?>> getFunctions()
	{
		final Set<Function<?>> mutableSet = new HashSet<>();
		for (final SimpleValue.Factory<? extends SimpleValue<? extends Object>> typeFactory : StandardDatatypes.MANDATORY_DATATYPE_SET)
		{
			mutableSet.addAll(FirstOrderBagFunctions.getFunctions(typeFactory));
		}

		return mutableSet;

	}

	/**
	 * FirstOrderBagFunctionSet instance (singleton)
	 */
	public static final FunctionSet SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "first-order-bag", getFunctions());

}
