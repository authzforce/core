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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.func.HigherOrderBagFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.impl.func.StandardHigherOrderBagFunctions.OneBagOnlyHigherOrderFunction;

/**
 *
 * Map function factory
 *
 * 
 * @version $Id: $
 */
final class MapFunctionFactory extends GenericHigherOrderFunctionFactory
{

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

			private Call(final String functionId, final Datatype<Bag<SUB_RETURN>> returnType, final FirstOrderFunction<SUB_RETURN> subFunction, final List<Expression<?>> primitiveInputs,
					final Expression<?> lastInputBag)
			{
				super(functionId, returnType, subFunction, primitiveInputs, lastInputBag);
				this.returnBagElementType = subFunction.getReturnType();
				this.indeterminateSubFuncEvalMessagePrefix = "Function " + functionId + ": Error calling sub-function (first argument) with last arg=";
			}

			@Override
			protected Bag<SUB_RETURN> evaluate(final Bag<?> lastArgBag, final EvaluationContext context) throws IndeterminateEvaluationException
			{
				final Collection<SUB_RETURN> results = new ArrayDeque<>(lastArgBag.size());
				for (final AttributeValue lastArgBagVal : lastArgBag)
				{
					final SUB_RETURN subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, lastArgBagVal);
					}
					catch (final IndeterminateEvaluationException e)
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
		private MapFunction(final String functionId, final BagDatatype<SUB_RETURN_T> returnType)
		{
			super(functionId, returnType, returnType.getElementType());
		}

		@Override
		protected OneBagOnlyHigherOrderFunction.Call<Bag<SUB_RETURN_T>, SUB_RETURN_T> newFunctionCall(final FirstOrderFunction<SUB_RETURN_T> subFunc, final List<Expression<?>> primitiveInputs,
				final Expression<?> lastInputBag)
		{
			return new Call<>(this.getId(), this.getReturnType(), subFunc, primitiveInputs, lastInputBag);
		}

	}

	private final String functionId;

	MapFunctionFactory(final String functionId)
	{
		this.functionId = functionId;
	}

	@Override
	public final String getId()
	{
		return functionId;
	}

	@Override
	public final <SUB_RETURN extends AttributeValue> HigherOrderBagFunction<?, SUB_RETURN> getInstance(final DatatypeFactory<SUB_RETURN> subFunctionReturnTypeFactory)
	{
		return new MapFunction<>(functionId, subFunctionReturnTypeFactory.getBagDatatype());
	}

}
