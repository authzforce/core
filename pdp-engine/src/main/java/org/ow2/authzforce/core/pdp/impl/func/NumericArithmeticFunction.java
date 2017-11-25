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
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ConstantPrimitiveAttributeValueExpression;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.NumericValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that implements all the numeric *-add functions (as opposed to date/time *-add-* functions).
 *
 * @param <AV>
 *            return and parameter type
 * 
 * @version $Id: $
 */
final class NumericArithmeticFunction<AV extends NumericValue<?, AV>> extends SingleParameterTypedFirstOrderFunction<AV, AV>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NumericArithmeticFunction.class);

	private static final IllegalArgumentException UNDEF_PARAMETER_TYPES_EXCEPTION = new IllegalArgumentException("Undefined function parameter types");

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

	/**
	 * Multary/Multiary/Polyadic operator
	 * 
	 * @see "https://en.wikipedia.org/wiki/Arity#Other_names"
	 *
	 * @param <V>
	 */
	interface MultaryOperation<V extends NumericValue<?, V>> extends StaticOperation<V>
	{
		boolean isCommutative();
	}

	private static final class Call<V extends NumericValue<?, V>> extends EagerSinglePrimitiveTypeEval<V, V>
	{
		private final String invalidArgsErrMsg;
		private final StaticOperation<V> op;

		private Call(final SingleParameterTypedFirstOrderFunctionSignature<V, V> functionSig, final StaticOperation<V> op, final List<Expression<?>> args, final Datatype<?>[] remainingArgTypes)
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
				throw new IndeterminateEvaluationException(invalidArgsErrMsg, XacmlStatusCode.PROCESSING_ERROR.value(), e);
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
	NumericArithmeticFunction(final String funcURI, final boolean varArgs, final List<Datatype<AV>> paramTypes, final StaticOperation<AV> op) throws IllegalArgumentException
	{
		super(funcURI, validate(paramTypes).get(0), varArgs, paramTypes);
		this.op = op;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<AV> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/**
		 * If this.op is a commutative function (e.g. add or multiply function), we can simplify arguments if there are multiple constants. Indeed, if C1,...Cm are constants, then:
		 * <p>
		 * op(x1,..., x_{n1-1}, C1, x_n1, ..., x_{n2-1} C2, x_n2, ..., Cm, x_nm...) = op( C, x1.., x_{n1-1}, x_n1, x_{n2-2}, x_n2...), where C (constant) = op(C1, C2..., Cm)
		 * </p>
		 * In this case, we can pre-compute constant C and replace all constant args with one: C
		 * 
		 */
		if (op instanceof MultaryOperation && ((MultaryOperation<AV>) op).isCommutative())
		{
			/*
			 * Constant argExpressions
			 */
			final Deque<AV> constants = new ArrayDeque<>(argExpressions.size());
			/*
			 * Remaining variable argExpressions
			 */
			final List<Expression<?>> finalArgExpressions = new ArrayList<>(argExpressions.size());
			final Datatype<AV> paramType = this.functionSignature.getParameterType();
			final Iterator<Expression<?>> argExpIterator = argExpressions.iterator();
			int argIndex = 0;
			while (argExpIterator.hasNext())
			{
				final Expression<?> argExp = argExpIterator.next();
				final Optional<? extends Value> v = argExp.getValue();
				if (!v.isPresent())
				{
					// variable
					finalArgExpressions.add(argExp);
				}
				else
				{
					// constant
					try
					{
						constants.add(paramType.cast(v.get()));
					}
					catch (final ClassCastException e)
					{
						throw new IllegalArgumentException("Function " + this.functionSignature + ": invalid arg #" + argIndex + ": bad type: " + argExp.getReturnType() + ". Expected type: "
								+ paramType, e);
					}
				}

				argIndex += 1;
			}

			if (constants.size() > 1)
			{
				/*
				 * we can replace all constant args C1, C2... with one constant C = op(C1, C2...)
				 */
				LOGGER.warn("Function {}: simplifying args to this commutative function (f): replacing all constant args {} with one that is the constant result of f(constant_args)",
						this.functionSignature, constants);
				final AV constantResult = op.eval(constants);
				if (finalArgExpressions.isEmpty())
				{
					/*
					 * There aren't any other args, i.e. all are constant. The result is constantResult.
					 */
					return new ConstantResultFirstOrderFunctionCall<>(constantResult, paramType);
				}

				/*
				 * finalArgExpressions is not empty. There is at least one variable arg.
				 */
				finalArgExpressions.add(new ConstantPrimitiveAttributeValueExpression<>(paramType, constantResult));
				return new Call<>(functionSignature, op, finalArgExpressions, remainingArgTypes);
			}

		}

		return new Call<>(functionSignature, op, argExpressions, remainingArgTypes);
	}

}
