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

import java.lang.reflect.Array;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.BagDatatype;
import com.thalesgroup.authzforce.core.eval.Bags;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.Bag;

/**
 * 
 * map function
 * 
 * @param <SUB_RETURN_T>
 *            subfunction return type
 * 
 */
public class MapFunction<SUB_RETURN_T extends AttributeValue<SUB_RETURN_T>> extends HigherOrderBagFunction.OneBagOnlyFunction<Bag<SUB_RETURN_T>, SUB_RETURN_T>
{

	/**
	 * Standard identifier for the map function.
	 */
	public static final String NAME_MAP = FUNCTION_NS_3 + "map";

	/**
	 * Function Factory for map function
	 * 
	 */
	public static class Factory implements GenericHigherOrderFunctionFactory
	{

		@Override
		public final String getId()
		{
			return NAME_MAP;
		}

		@Override
		public final <SUB_RETURN extends AttributeValue<SUB_RETURN>> HigherOrderBagFunction<?, SUB_RETURN> getInstance(Datatype<SUB_RETURN> subFunctionReturnType)
		{
			return MapFunction.getInstance(subFunctionReturnType);
		}

	}

	private final Class<SUB_RETURN_T> subFuncReturnClass;
	private final BagDatatype<SUB_RETURN_T> returnBagType;

	/**
	 * Creates Map function for specific sub-function's return type
	 * 
	 * @param subFunctionReturnType
	 *            sub-function return type
	 * @return instance of Map function
	 */
	public static <SUB_RETURN extends AttributeValue<SUB_RETURN>> MapFunction<SUB_RETURN> getInstance(Datatype<SUB_RETURN> subFunctionReturnType)
	{
		final BagDatatype<SUB_RETURN> returnBagDatatype = Bags.empty(subFunctionReturnType).getDatatype();
		return new MapFunction<>(returnBagDatatype);
	}

	/**
	 * Creates Map function for specific sub-function's return type
	 * 
	 * @param subFunctionReturnType
	 *            sub-function return type
	 */
	private MapFunction(BagDatatype<SUB_RETURN_T> returnType)
	{
		super(NAME_MAP, returnType, returnType.getElementType());
		this.returnBagType = returnType;
		this.subFuncReturnClass = returnType.getElementType().getValueClass();
	}

	@Override
	protected final Bag<SUB_RETURN_T> evaluate(FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall, AttributeValue<?>[] lastArgBag, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final SUB_RETURN_T[] results = (SUB_RETURN_T[]) Array.newInstance(subFuncReturnClass, lastArgBag.length);
		for (int valIndex = 0; valIndex < lastArgBag.length; valIndex++)
		{
			final SUB_RETURN_T subResult;
			try
			{
				subResult = subFuncCall.evaluate(context, lastArgBag[valIndex]);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this + ": Error calling sub-function (specified as first argument) with last arg=" + lastArgBag[valIndex], e.getStatusCode(), e);
			}

			if (subResult == null)
			{
				throw getIndeterminateArgException(lastArgIndex);
			}

			results[valIndex] = subResult;
		}

		return Bags.getInstance(returnBagType, results);
	}

}
