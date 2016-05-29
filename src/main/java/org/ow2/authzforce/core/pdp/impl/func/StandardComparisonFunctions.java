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
/**
 *
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.HashSet;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.func.BaseFunctionSet;
import org.ow2.authzforce.core.pdp.api.func.ComparisonFunction;
import org.ow2.authzforce.core.pdp.api.func.ComparisonFunction.PostCondition;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionSet;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * Standard comparison functions
 * 
 * @version $Id: $
 */
public final class StandardComparisonFunctions
{
	private StandardComparisonFunctions()
	{
		// empty private constructor to prevent instantiation
	}

	private static Set<Function<?>> getTotalComparisonFunctions()
	{
		final Set<Function<?>> mutableSet = new HashSet<>();
		for (final PostCondition condition : PostCondition.values())
		{
			mutableSet.add(new ComparisonFunction<>(StandardDatatypes.INTEGER_FACTORY.getDatatype(), condition));
			mutableSet.add(new ComparisonFunction<>(StandardDatatypes.DOUBLE_FACTORY.getDatatype(), condition));
			mutableSet.add(new ComparisonFunction<>(StandardDatatypes.STRING_FACTORY.getDatatype(), condition));
		}

		return mutableSet;
	}

	/**
	 * Set of functions implementing comparison of {@link Comparable} values, i.e. imposing total ordering of compared objects. In particular, this applies to all numeric types (integers, doubles...)
	 * and string, but not to XML schema date/times that may have indeterminate relationship to each other (see {@link #TEMPORAL_SET}).
	 */
	public static final FunctionSet TOTAL_ORDER_SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "total-order-comparison", getTotalComparisonFunctions());

	private static Set<Function<?>> getTemporalFunctions()
	{
		final Set<Function<?>> mutableSet = new HashSet<>();
		for (final PostCondition condition : PostCondition.values())
		{
			mutableSet.add(new ComparisonFunction<>(StandardDatatypes.TIME_FACTORY.getDatatype(), condition));
			mutableSet.add(new ComparisonFunction<>(StandardDatatypes.DATE_FACTORY.getDatatype(), condition));
			mutableSet.add(new ComparisonFunction<>(StandardDatatypes.DATETIME_FACTORY.getDatatype(), condition));
		}

		return mutableSet;
	}

	/**
	 * Set of functions comparing XML schema date/time values, i.e. not imposing total ordering of compared objects, as such date/times may have indeterminate relationship to each other.
	 * 
	 */
	public static final FunctionSet TEMPORAL_SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-comparison", getTemporalFunctions());
}
