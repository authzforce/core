/*
 * Copyright 2012-2023 THALES.
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

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.MultiParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class that implements the n-of function. From the XACML spec (urn:oasis:names:tc:xacml:1.0:function:n-of): the first argument to this function SHALL be of data-type
 * <code>http://www.w3.org/2001/XMLSchema#integer</code>. The remaining arguments SHALL be of data-type <code>http://www.w3.org/2001/XMLSchema#boolean</code>. The first argument specifies the minimum number of the remaining
 * arguments that MUST evaluate to "True" for the expression to be considered "True". If the first argument is 0, the result SHALL be "True". If the number of arguments after the first one is less
 * than the value of the first argument, then the expression SHALL result in "Indeterminate". The order of evaluation SHALL be: first evaluate the integer value, and then evaluate each subsequent
 * argument. The evaluation SHALL stop and return "True" if the specified number of arguments evaluate to "True". The evaluation of arguments SHALL stop if it is determined that evaluating the
 * remaining arguments will not satisfy the requirement.
 * <p>
 * This function evaluates the arguments one at a time, starting with the first one. As soon as the result of the function can be determined, evaluation stops and that result is returned. During this
 * process, if any argument evaluates to indeterminate, an indeterminate result is returned.
 * 
 * @version $Id: $
 */
final class LogicalNOfFunction extends MultiParameterTypedFirstOrderFunction<BooleanValue>
{
	private static String getInvalidArg0MessagePrefix(final FirstOrderFunctionSignature<?> funcsig) {
		return "Function " + funcsig.getName() + ": Invalid arg #0 (number of required Trues): expected: 0 <= (integer) <= number_of_remaining_arguments; actual: ";
	}

	private static abstract class Call extends BaseFirstOrderFunctionCall<BooleanValue>
	{

		protected final int numOfArgsAfterFirst;
		protected final String indeterminateArgMsgPrefix;
		private final IndeterminateEvaluationException indeterminateArgException;
		private final String invalidArgTypeMsgPrefix;
		private final List<Expression<?>> checkedArgExpressionsAfterFirst;

		private Call(final FirstOrderFunctionSignature<BooleanValue> functionSig, final List<Expression<?>> args, final Datatype<?>... remainingArgTypes)
		{
			super(functionSig, args, remainingArgTypes);
			
			/*
			 * Number of remaining args, i.e. all (boolean) args after the first (integer) arg (excluded).
			 */
			numOfArgsAfterFirst = args.size() + remainingArgTypes.length - 1;
			indeterminateArgMsgPrefix = "Function " + functionSig.getName() + ": Indeterminate arg #";
			indeterminateArgException = new IndeterminateEvaluationException("Function " + functionSig.getName() + ": evaluation failed because of indeterminate arg",
					XacmlStatusCode.PROCESSING_ERROR.value());
			invalidArgTypeMsgPrefix = "Function " + functionSig.getName() + ": Invalid type (expected = " + StandardDatatypes.BOOLEAN + ") of arg#";

			/*
			 * Let us optimize by checking the following: If any argument expression is constant BooleanValue False, remove it from the arguments, as it has no effect on the final result. Indeed,
			 * n-of function is commutative except for the first argument, and n-of(N, false, x, y...) = n-of(N, x, y...).
			 */
			this.checkedArgExpressionsAfterFirst = args.subList(1, args.size()).stream().filter(argExpr ->
					argExpr.getValue().map(v -> !(v instanceof BooleanValue) || ((BooleanValue) v).getUnderlyingValue()).orElse(true)
			).collect(Collectors.toUnmodifiableList());
		}

		protected BooleanValue evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final int requiredMinOfTrues,
				final AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException {
			assert requiredMinOfTrues <= this.numOfArgsAfterFirst;

			int nOfRequiredTrues = requiredMinOfTrues;
			int nOfRemainingArgs = this.numOfArgsAfterFirst;
			IndeterminateEvaluationException lastIndeterminateException = null;
			int nOfIndeterminateArgs = 0;

			// loop through the inputs, trying to find at least n trues
			for (Expression<?> input : checkedArgExpressionsAfterFirst)
			{
				// evaluate the next argument
				nOfRemainingArgs--;
				final BooleanValue attrVal;
				try
				{
					attrVal = Expressions.eval(input, context, mdpContext, StandardDatatypes.BOOLEAN);
					if (attrVal.getUnderlyingValue())
					{
						/*
						 * Arg evaluation result = TRUE -> We're one step closer to our goal...check if we met it
						 */
						nOfRequiredTrues--;
						if (nOfRequiredTrues == 0)
						{
							return BooleanValue.TRUE;
						}
					}
				} catch (final IndeterminateEvaluationException e)
				{
					/*
					 * Keep the indeterminate arg error to throw it later in exception, in case there was not enough TRUEs in the remaining args.
					 *
					 * Indicate arg index to help troubleshooting: argIndex (starting at 0) = max_index - number_of_args_remaining_to_evaluate = (totalArgCount - 1) - nOfRemainingArgs =
					 * numOfArgsAfterFirst - nOfRemainingArgs
					 */
					final int argIndex = numOfArgsAfterFirst - nOfRemainingArgs;
					lastIndeterminateException = new IndeterminateEvaluationException(indeterminateArgMsgPrefix + argIndex, e);
					nOfIndeterminateArgs++;
				}

				// nOfRequiredTrues != 0
				/*
				 * If number of required TRUEs > number of remaining args, the result is definitely not TRUE (but FALSE or Indeterminate)
				 */
				if (nOfRequiredTrues > nOfRemainingArgs)
				{
					/*
					 * The final result is either False or Indeterminate. If we already have nOfIndeterminateArgs >= nOfRequiredTrues, the final result would be TRUE if all Indeterminate where TRUE,
					 * so the final result is Indeterminate.
					 */
					if (nOfRequiredTrues <= nOfIndeterminateArgs)
					{
						/*
						 * nOfIndeterminateArgs (= nOfRequiredTrues) != 0 as well if all indeterminate args have been TRUE, result would be TRUE -> indeterminate result
						 */
						if (lastIndeterminateException == null)
						{
							/*
							 * This should not happen in theory as lastIndeterminateException != null when nOfIndeterminateArgs != 0
							 */
							throw indeterminateArgException;
						}

						throw lastIndeterminateException;
					}

					/*
					 * If number of required TRUEs > number of possible TRUEs (= nOfRemainingArgs + nOfIndeterminateArgs), then the result is definitely FALSE. (Else it can still be Indeterminate or
					 * True.)
					 */
					if (nOfRequiredTrues > nOfRemainingArgs + nOfIndeterminateArgs)
					{
						return BooleanValue.FALSE;
					}
				}
			}

			/*
			 * Do the same logic with remaining arg values (except they are constants)
			 */
			if (checkedRemainingArgs != null)
			{
				for (final AttributeValue arg : checkedRemainingArgs)
				{
					nOfRemainingArgs--;
					final BooleanValue attrVal;
					try
					{
						attrVal = (BooleanValue) arg;
					} catch (final ClassCastException e)
					{
						/*
						 * Indicate arg index to help troubleshooting: argIndex (starting at 0) = max_index - number_of_args_remaining_to_evaluate = (totalArgCount - 1) - nOfRemainingArgs =
						 * numOfArgsAfterFirst - nOfRemainingArgs
						 */
						final int argIndex = numOfArgsAfterFirst - nOfRemainingArgs;
						throw new IndeterminateEvaluationException(invalidArgTypeMsgPrefix + argIndex + ": " + arg.getClass().getName(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
					}

					if (attrVal.getUnderlyingValue())
					{
						/*
						 * Arg = TRUE -> We're one step closer to our goal...check if we met it
						 */
						nOfRequiredTrues--;
						if (nOfRequiredTrues == 0)
						{
							return BooleanValue.TRUE;
						}
					}

					/*
					 * If number of required TRUEs > number of remaining args, the result is definitely not TRUE (but FALSE or Indeterminate)
					 */
					if (nOfRequiredTrues > nOfRemainingArgs)
					{
						/*
						 * The final result is either False or Indeterminate. If we already have nOfIndeterminateArgs >= nOfRequiredTrues, the final result would be TRUE if all Indeterminate where
						 * TRUE, so the final result is Indeterminate.
						 */
						if (nOfRequiredTrues <= nOfIndeterminateArgs)
						{
							/*
							 * nOfIndeterminateArgs (= nOfRequiredTrues) != 0 as well if all indeterminate args have been TRUE, result would be TRUE -> indeterminate result
							 */
							if (lastIndeterminateException == null)
							{
								/*
								 * This should not happen in theory as lastIndeterminateException != null when nOfIndeterminateArgs != 0
								 */
								throw indeterminateArgException;
							}

							throw lastIndeterminateException;
						}

						/*
						 * If number of required TRUEs > number of possible TRUEs (= nOfRemainingArgs + nOfIndeterminateArgs), then the result is definitely FALSE. (Else it can still be Indeterminate
						 * or True.)
						 */
						if (nOfRequiredTrues > nOfRemainingArgs + nOfIndeterminateArgs)
						{
							return BooleanValue.FALSE;
						}
					}
				}
			}

			return BooleanValue.FALSE;
		}

	}

	private static final class CallWithFixedArg0 extends Call
	{
		private final int requiredMinOfTrues;

		public CallWithFixedArg0(final FirstOrderFunctionSignature<BooleanValue> functionSignature, final int checkedArg0, final List<Expression<?>> allArgExpressions, final Datatype<?>... remainingArgTypes)
		{
			super(functionSignature, allArgExpressions, remainingArgTypes);
			requiredMinOfTrues = checkedArg0;
		}

		@Override
		public BooleanValue evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException {
			return super.evaluate(context, mdpContext, requiredMinOfTrues, remainingArgs);
		}

	}

	private static final class CallWithVarArg0 extends Call
	{
		private final String invalidArg0MsgPrefix;
		private final String indeterminateArg0MsgPrefix;
		private final Expression<? extends IntegerValue> nOfRequiredTruesExpr;

		private CallWithVarArg0(final FirstOrderFunctionSignature<BooleanValue> functionSig, final Expression<? extends IntegerValue> arg0, final List<Expression<?>> allArgExpressions, final Datatype<?>... remainingArgTypes)
				throws IllegalArgumentException
		{
			super(functionSig, allArgExpressions, remainingArgTypes);
			invalidArg0MsgPrefix = getInvalidArg0MessagePrefix(functionSig);
			indeterminateArg0MsgPrefix = indeterminateArgMsgPrefix + 0;
			nOfRequiredTruesExpr = arg0;
		}

		@Override
		public BooleanValue evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException {
			/*
			 * Arg datatypes and number are already checked in superclass, but we need to do further checks specific to this function such as the first argument which must be a positive integer
			 */
			/*
			 * Evaluate the arguments one by one. As soon as we can return a result, do so. Return Indeterminate if any argument evaluated is indeterminate.
			 */
			// Evaluate the first argument
			final IntegerValue intAttrVal;
			try
			{
				intAttrVal = nOfRequiredTruesExpr.evaluate(context, mdpContext);
			} catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(indeterminateArg0MsgPrefix, e);
			}

			/*
			 * intAttrVal is 'n' (number of Trues to reach).
			 * 
			 * We downsize the BigInteger value to int right away, because anyway inputs.size() is an int, so we cannot do better and don't need to.
			 */
			final int nOfRequiredTrues = intAttrVal.getUnderlyingValue().intValue();

			// If the number of trues needed is less than zero, report an error.
			if (nOfRequiredTrues < 0)
			{
				throw new IndeterminateEvaluationException(invalidArg0MsgPrefix + nOfRequiredTrues, XacmlStatusCode.PROCESSING_ERROR.value());
			}

			// If the number of trues needed is zero, return true.
			if (nOfRequiredTrues == 0)
			{
				return BooleanValue.TRUE;
			}

			// else nOfRequiredTrues > 0
			// make sure it's possible to find n true values in the remaining arguments
			if (nOfRequiredTrues > numOfArgsAfterFirst)
			{
				throw new IndeterminateEvaluationException(invalidArg0MsgPrefix + nOfRequiredTrues + " > number_of_remaining_args (" + numOfArgsAfterFirst + ")",
						XacmlStatusCode.PROCESSING_ERROR.value());
			}

			return evaluate(context, mdpContext, nOfRequiredTrues, checkedRemainingArgs);
		}
	}

	LogicalNOfFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.BOOLEAN, true, Arrays.asList(StandardDatatypes.INTEGER, StandardDatatypes.BOOLEAN));
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException {
		/*
		 * Optimization: check whether the first arg ('n' = number of Trues to reach) is constant
		 */
		final Iterator<? extends Expression<?>> argExpsIterator = argExpressions.iterator();
		final String invalidArg0MsgPrefix = getInvalidArg0MessagePrefix(functionSignature);
		/*
		 * Evaluate the first argument if not in remainingArgTypes
		 */
		if (!argExpsIterator.hasNext())
		{
			throw new IllegalArgumentException(invalidArg0MsgPrefix + "<undefined> (no arg)");
		}

		final Expression<?> arg0Expr = argExpsIterator.next();
		final Optional<? extends Value> arg0 = arg0Expr.getValue();
		if (arg0.isPresent())
		{
			// arg0 is constant
			// We downsize the BigInteger value to int right away, because anyway inputs.size() is an
			// int, so we cannot do better and don't need to.
			final int nOfRequiredTrues = ((IntegerValue) arg0.get()).getUnderlyingValue().intValueExact();
			if (nOfRequiredTrues < 0)
			{
				throw new IllegalArgumentException(getInvalidArg0MessagePrefix(functionSignature) + nOfRequiredTrues);
			}

			// If the number of trues needed is zero, return true.
			if (nOfRequiredTrues == 0)
			{
				return new ConstantResultFirstOrderFunctionCall<>(BooleanValue.TRUE, StandardDatatypes.BOOLEAN);
			}

			// else nOfRequiredTrues > 0
			// make sure it's possible to find n true values in the remaining arguments
			// Total number of args to the n-of function:
			final int totalArgCount = argExpressions.size() + remainingArgTypes.length;
			/*
			 * Number of remaining args, i.e. all (boolean) args after the first (integer) arg (excluded).
			 */
			final int nOfRemainingArgs = totalArgCount - 1;
			if (nOfRequiredTrues > nOfRemainingArgs)
			{
				throw new IllegalArgumentException(getInvalidArg0MessagePrefix(functionSignature) + nOfRequiredTrues + " > number_of_remaining args (" + nOfRemainingArgs + ")");
			}

			return new CallWithFixedArg0(functionSignature, nOfRequiredTrues, argExpressions, remainingArgTypes);
		}

		// arg0 is no constant expression
		assert arg0Expr.getReturnType() == StandardDatatypes.INTEGER;
		return new CallWithVarArg0(functionSignature, (Expression<? extends  IntegerValue>) arg0Expr, argExpressions, remainingArgTypes);
	}

}
