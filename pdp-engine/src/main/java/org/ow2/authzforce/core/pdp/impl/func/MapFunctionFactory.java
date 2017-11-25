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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.GenericHigherOrderFunctionFactory;
import org.ow2.authzforce.core.pdp.api.func.HigherOrderBagFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
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
	private static final IllegalArgumentException NULL_SUB_FUNCTION_RETURN_TYPE_ARG_EXCEPTION = new IllegalArgumentException(
			"Cannot create generic function with null subFunctionReturnTypeFactory (sub-function return type factory) arg");

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
					final Expression<? extends Bag<?>> lastInputBag)
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

				return Bags.newBag(returnBagElementType, results);
			}
		}

		/**
		 * Creates Map function for specific sub-function's return type
		 * 
		 * @param subFunctionReturnType
		 *            sub-function return type
		 */
		private MapFunction(final String functionId, final AttributeDatatype<SUB_RETURN_T> returnType)
		{
			super(functionId, returnType.getBagDatatype(), returnType);
		}

		@Override
		protected OneBagOnlyHigherOrderFunction.Call<Bag<SUB_RETURN_T>, SUB_RETURN_T> newFunctionCall(final FirstOrderFunction<SUB_RETURN_T> subFunc, final List<Expression<?>> primitiveInputs,
				final Expression<? extends Bag<?>> lastInputBag)
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
	public String getId()
	{
		return functionId;
	}

	@Override
	public <SUB_RETURN extends AttributeValue> HigherOrderBagFunction<?, SUB_RETURN> getInstance(final Datatype<SUB_RETURN> subFunctionReturnType) throws IllegalArgumentException
	{
		if (subFunctionReturnType == null)
		{
			throw NULL_SUB_FUNCTION_RETURN_TYPE_ARG_EXCEPTION;
		}

		if (!(subFunctionReturnType instanceof AttributeDatatype<?>))
		{
			throw new IllegalArgumentException("Invalid sub-function's return type specified for function '" + functionId + "': " + subFunctionReturnType
					+ ". Expected: any primitive attribute datatype.");
		}

		return new MapFunction<>(functionId, (AttributeDatatype<SUB_RETURN>) subFunctionReturnType);
	}

}
