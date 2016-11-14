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

import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.NumericValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * A class that implements all the numeric *-add functions (as opposed to date/time *-add-* functions).
 *
 * @param <AV>
 *            return and parameter type
 * 
 * @version $Id: $
 */
final class NumericArithmeticFunction<AV extends NumericValue<?, AV>>
		extends SingleParameterTypedFirstOrderFunction<AV, AV>
{

	private static final IllegalArgumentException UNDEF_PARAMETER_TYPES_EXCEPTION = new IllegalArgumentException(
			"Undefined function parameter types");

	private static <AV extends Value> List<Datatype<AV>> validate(final List<Datatype<AV>> paramTypes)
	{
		if (paramTypes == null || paramTypes.isEmpty())
		{
			throw UNDEF_PARAMETER_TYPES_EXCEPTION;
		}

		return paramTypes;
	}

	interface StaticOperation<V extends NumericValue<?, V>>
	{
		V eval(Deque<V> args) throws IllegalArgumentException, ArithmeticException;
	}

	private static final class Call<V extends NumericValue<?, V>> extends EagerSinglePrimitiveTypeEval<V, V>
	{
		private final String invalidArgsErrMsg;
		private final StaticOperation<V> op;

		private Call(final SingleParameterTypedFirstOrderFunctionSignature<V, V> functionSig,
				final StaticOperation<V> op, final List<Expression<?>> args, final Datatype<?>[] remainingArgTypes)
				throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			this.op = op;
			this.invalidArgsErrMsg = "Function " + this.functionId + ": invalid argument(s)";
		}

		@Override
		protected V evaluate(final Deque<V> args) throws IndeterminateEvaluationException
		{
			try
			{
				return op.eval(args);
			}
			catch (IllegalArgumentException | ArithmeticException e)
			{
				throw new IndeterminateEvaluationException(invalidArgsErrMsg, StatusHelper.STATUS_PROCESSING_ERROR, e);
			}
		}
	}

	private final StaticOperation<AV> op;

	/**
	 * Creates a new Numeric Arithmetic function.
	 * 
	 * @param funcURI
	 *            function URI
	 * 
	 * @param paramTypes
	 *            parameter/return types (all the same)
	 * @param varArgs
	 *            whether this is a varargs function (like Java varargs method), i.e. last arg has variable-length
	 * 
	 */
	NumericArithmeticFunction(final String funcURI, final boolean varArgs, final List<Datatype<AV>> paramTypes,
			final StaticOperation<AV> op) throws IllegalArgumentException
	{
		super(funcURI, validate(paramTypes).get(0), varArgs, paramTypes);
		this.op = op;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<AV> newCall(final List<Expression<?>> argExpressions,
			final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/**
		 * TODO: optimize call to "add" (resp. "multiply") function call by checking all static/constant arguments and
		 * if there are more than one, pre-compute their sum (resp. product) and replace these arguments with one
		 * argument that is this sum (resp. product) in the function call. Indeed, 'add' function is commutative and
		 * (constant in upper case, variables in lower case): add(C1, C2, x, y...) = add(C1+C2, x, y...). Similarly,
		 * multiply(C1, C2, x, y...) = multiply(C1*C2, x, y...)
		 * 
		 */

		return new Call<>(functionSignature, op, argExpressions, remainingArgTypes);
	}

}
