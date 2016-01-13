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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Bag;
import org.ow2.authzforce.core.pdp.api.BagDatatype;
import org.ow2.authzforce.core.pdp.api.Bags;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.impl.func.HigherOrderBagFunctionSet.OneBagOnlyHigherOrderFunction;

/**
 * 
 * Map function factory
 * 
 */
public final class MapFunctionFactory
{

	/**
	 * Standard identifier for the map function.
	 */
	public static final String NAME_MAP = Function.XACML_NS_3_0 + "map";

	/**
	 * 
	 * map function
	 * 
	 * @param <SUB_RETURN_T>
	 *            subfunction return type
	 * 
	 */
	private static final class MapFunction<SUB_RETURN_T extends AttributeValue> extends OneBagOnlyHigherOrderFunction<Bag<SUB_RETURN_T>, SUB_RETURN_T>
	{

		private static final class Call<SUB_RETURN extends AttributeValue> extends OneBagOnlyHigherOrderFunction.Call<Bag<SUB_RETURN>, SUB_RETURN>
		{
			private final Datatype<SUB_RETURN> returnBagElementType;
			private final String indeterminateSubFuncEvalMessagePrefix;

			private Call(Datatype<Bag<SUB_RETURN>> returnType, FirstOrderFunction<SUB_RETURN> subFunction, List<Expression<?>> primitiveInputs,
					Expression<?> lastInputBag)
			{
				super(NAME_MAP, returnType, subFunction, primitiveInputs, lastInputBag);
				this.returnBagElementType = subFunction.getReturnType();
				this.indeterminateSubFuncEvalMessagePrefix = "Function " + NAME_MAP + ": Error calling sub-function (first argument) with last arg=";
			}

			@Override
			protected Bag<SUB_RETURN> evaluate(Bag<?> lastArgBag, EvaluationContext context) throws IndeterminateEvaluationException
			{
				final Collection<SUB_RETURN> results = new ArrayDeque<>(lastArgBag.size());
				for (final AttributeValue lastArgBagVal : lastArgBag)
				{
					final SUB_RETURN subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, lastArgBagVal);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(indeterminateSubFuncEvalMessagePrefix + lastArgBagVal, e.getStatusCode(), e);
					}

					results.add(subResult);
				}

				return Bags.getInstance(returnBagElementType, results);
			}
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
		}

		@Override
		protected OneBagOnlyHigherOrderFunction.Call<Bag<SUB_RETURN_T>, SUB_RETURN_T> newFunctionCall(FirstOrderFunction<SUB_RETURN_T> subFunc,
				List<Expression<?>> primitiveInputs, Expression<?> lastInputBag)
		{
			return new Call<>(this.getReturnType(), subFunc, primitiveInputs, lastInputBag);
		}

	}

	/**
	 * Instance of generic factory for standard 'map' function (singleton)
	 */
	public static final GenericHigherOrderFunctionFactory INSTANCE = new GenericHigherOrderFunctionFactory()
	{

		@Override
		public final String getId()
		{
			return NAME_MAP;
		}

		@Override
		public final <SUB_RETURN extends AttributeValue> HigherOrderBagFunction<?, SUB_RETURN> getInstance(
				DatatypeFactory<SUB_RETURN> subFunctionReturnTypeFactory)
		{
			return new MapFunction<>(subFunctionReturnTypeFactory.getBagDatatype());
		}

	};

}
