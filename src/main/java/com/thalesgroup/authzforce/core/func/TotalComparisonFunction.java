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
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;

/**
 * A class implementing comparison of {@link Comparable} attribute values, i.e. imposing total
 * ordering of compared objects. In particular, this applies to all numeric types (integers,
 * doubles...) and string, but not to XML schema date/times that may have indeterminate relationship
 * to each other.
 * 
 * @param <AV>
 *            function parameter type
 */
public class TotalComparisonFunction<AV extends AttributeValue<AV> & Comparable<AV>> extends BaseComparisonFunction<AV>
{

	/**
	 * @see BaseComparisonFunction#BaseComparisonFunction(DatatypeConstants, PostCondition)
	 */
	public TotalComparisonFunction(DatatypeConstants<AV> paramTypeDef, PostCondition condition)
	{
		super(paramTypeDef, condition);
	}

	@Override
	public int compare(AV attrVal1, AV attrVal2) throws IndeterminateEvaluationException
	{
		return attrVal1.compareTo(attrVal2);
	}

	/**
	 * All comparison functions requiring total order on compared objects
	 */
	private static final Set<Function<?>> INSTANCES = new HashSet<>();
	static
	{
		for (final PostCondition condition : PostCondition.values())
		{
			INSTANCES.addAll(Arrays.asList(
			//
					new TotalComparisonFunction<>(DatatypeConstants.INTEGER, condition),
					//
					new TotalComparisonFunction<>(DatatypeConstants.DOUBLE, condition),
					//
					new TotalComparisonFunction<>(DatatypeConstants.STRING, condition)));
		}
	}

	/**
	 * function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "total-comparison", INSTANCES);

}
