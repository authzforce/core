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
/**
 *
 */
package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.BaseTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * A class implementing comparison of XML schema date/time attribute values, i.e. not imposing total
 * ordering of compared objects, as such date/times may have indeterminate relationship to each
 * other.
 * 
 * @param <T>
 *            function parameter type
 */
public class TemporalComparisonFunction<T extends BaseTimeAttributeValue<T>> extends BaseComparisonFunction<T>
{

	/**
	 * @see BaseComparisonFunction#BaseComparisonFunction(DatatypeConstants,
	 *      BaseComparisonFunction.PostCondition)
	 */
	public TemporalComparisonFunction(DatatypeConstants<T> paramTypeDef, PostCondition condition)
	{
		super(paramTypeDef, condition);
	}

	@Override
	public int compare(T attrVal1, T attrVal2) throws IndeterminateEvaluationException
	{
		return attrVal1.compare(attrVal2);
	}

	/**
	 * All time-related less/greater-than(-or-equal) functions
	 */
	private static final Set<Function<?>> INSTANCES = new HashSet<>();
	static
	{
		for (final PostCondition condition : PostCondition.values())
		{
			INSTANCES.addAll(Arrays.asList(
			//
					new TemporalComparisonFunction<>(DatatypeConstants.TIME, condition),
					//
					new TemporalComparisonFunction<>(DatatypeConstants.DATE, condition),
					//
					new TemporalComparisonFunction<>(DatatypeConstants.DATETIME, condition)));
		}
	}

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-comparison", INSTANCES);

}
