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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.MultiParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * A class that implements the n-of function. From the XACML spec (urn:oasis:names:tc:xacml:1.0:function:n-of): the first argument to this function SHALL be of data-type
 * http://www.w3.org/2001/XMLSchema#integer. The remaining arguments SHALL be of data-type http://www.w3.org/2001/XMLSchema#boolean. The first argument specifies the minimum number of the remaining
 * arguments that MUST evaluate to "True" for the expression to be considered "True". If the first argument is 0, the result SHALL be "True". If the number of arguments after the first one is less
 * than the value of the first argument, then the expression SHALL result in "Indeterminate". The order of evaluation SHALL be: first evaluate the integer value, and then evaluate each subsequent
 * argument. The evaluation SHALL stop and return "True" if the specified number of arguments evaluate to "True". The evaluation of arguments SHALL stop if it is determined that evaluating the
 * remaining arguments will not satisfy the requirement.
 * <p>
 * This function evaluates the arguments one at a time, starting with the first one. As soon as the result of the function can be determined, evaluation stops and that result is returned. During this
 * process, if any argument evaluates to indeterminate, an indeterminate result is returned.
 *
 * 
 * @version $Id: $
 */
final class LogicalNOfFunction extends MultiParameterTypedFirstOrderFunction<BooleanValue>
{
	private static String getInvalidArg0MessagePrefix(final FirstOrderFunctionSignature<?> funcsig)
	{
		return "Function " + funcsig.getName() + ": Invalid arg #0 (number of required Trues): expected: 0 <= (integer) <= number_of_remaining_arguments; actual: ";
	}

	private static abstract class Call extends BaseFirstOrderFunctionCall<BooleanValue>
	{

		protected final int numOfArgsAfterFirst;
		protected final String indeterminateArgMsgPrefix;
		private final IndeterminateEvaluationException indeterminateArgException;
		private final String invalidArgTypeMsgPrefix;

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
		}

		protected BooleanValue evaluate(final EvaluationContext context, final int requiredMinOfTrues, final Iterator<? extends Expression<?>> remainingArgExpsIterator,
				final AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException
		{
			assert requiredMinOfTrues <= this.numOfArgsAfterFirst;

			int nOfRequiredTrues = requiredMinOfTrues;
			int nOfRemainingArgs = this.numOfArgsAfterFirst;
			IndeterminateEvaluationException lastIndeterminateException = null;
			int nOfIndeterminateArgs = 0;

			// loop through the inputs, trying to find at least n trues
			while (remainingArgExpsIterator.hasNext())
			{
				// evaluate the next argument
				final Expression<?> input = remainingArgExpsIterator.next();
				nOfRemainingArgs--;
				final BooleanValue attrVal;
				try
				{
					attrVal = Expressions.eval(input, context, StandardDatatypes.BOOLEAN);
					if (attrVal.getUnderlyingValue().booleanValue())
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
				}
				catch (final IndeterminateEvaluationException e)
				{
					/*
					 * Keep the indeterminate arg error to throw it later in exception, in case there was not enough TRUEs in the remaining args.
					 * 
					 * Indicate arg index to help troubleshooting: argIndex (starting at 0) = max_index - number_of_args_remaining_to_evaluate = (totalArgCount - 1) - nOfRemainingArgs =
					 * numOfArgsAfterFirst - nOfRemainingArgs
					 */
					final int argIndex = numOfArgsAfterFirst - nOfRemainingArgs;
					lastIndeterminateException = new IndeterminateEvaluationException(indeterminateArgMsgPrefix + argIndex, e.getStatusCode(), e);
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
						attrVal = BooleanValue.class.cast(arg);
					}
					catch (final ClassCastException e)
					{
						/*
						 * Indicate arg index to help troubleshooting: argIndex (starting at 0) = max_index - number_of_args_remaining_to_evaluate = (totalArgCount - 1) - nOfRemainingArgs =
						 * numOfArgsAfterFirst - nOfRemainingArgs
						 */
						final int argIndex = numOfArgsAfterFirst - nOfRemainingArgs;
						throw new IndeterminateEvaluationException(invalidArgTypeMsgPrefix + argIndex + ": " + arg.getClass().getName(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
					}

					if (attrVal.getUnderlyingValue().booleanValue())
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
		private final List<Expression<?>> checkedArgExpressionsAfterFirst;

		public CallWithFixedArg0(final FirstOrderFunctionSignature<BooleanValue> functionSignature, final int arg0, final List<Expression<?>> allArgExpressions, final Datatype<?>... remainingArgTypes)
		{
			super(functionSignature, allArgExpressions, remainingArgTypes);
			requiredMinOfTrues = arg0;
			checkedArgExpressionsAfterFirst = allArgExpressions.subList(1, allArgExpressions.size());
		}

		@Override
		public BooleanValue evaluate(final EvaluationContext context, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			return super.evaluate(context, requiredMinOfTrues, checkedArgExpressionsAfterFirst.iterator(), remainingArgs);
		}

	}

	private static final class CallWithVarArg0 extends Call
	{
		private final String invalidArg0MsgPrefix;
		private final List<Expression<?>> checkedArgExpressions;

		private CallWithVarArg0(final FirstOrderFunctionSignature<BooleanValue> functionSig, final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
				throws IllegalArgumentException
		{
			super(functionSig, argExpressions, remainingArgTypes);
			this.checkedArgExpressions = argExpressions;
			invalidArg0MsgPrefix = getInvalidArg0MessagePrefix(functionSig);
		}

		@Override
		public BooleanValue evaluate(final EvaluationContext context, final AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException
		{
			/*
			 * Arg datatypes and number is already checked in superclass but we need to do further checks specific to this function such as the first argument which must be a positive integer
			 */
			/*
			 * Evaluate the arguments one by one. As soon as we can return a result, do so. Return Indeterminate if any argument evaluated is indeterminate.
			 */
			final Iterator<? extends Expression<?>> argExpsIterator = checkedArgExpressions.iterator();

			// Evaluate the first argument
			final Expression<?> input0 = argExpsIterator.next();
			final IntegerValue intAttrVal;
			try
			{
				intAttrVal = Expressions.eval(input0, context, StandardDatatypes.INTEGER);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(indeterminateArgMsgPrefix + 0, e.getStatusCode(), e);
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

			return evaluate(context, nOfRequiredTrues, argExpsIterator, checkedRemainingArgs);
		}
	}

	LogicalNOfFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.BOOLEAN, true, Arrays.asList(StandardDatatypes.INTEGER, StandardDatatypes.BOOLEAN));
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/*
		 * Optimization: check whether the first arg ('n' = number of Trues to reach) is constant
		 */
		final Iterator<? extends Expression<?>> argExpsIterator = argExpressions.iterator();
		// Evaluate the first argument
		final Optional<? extends Value> arg0 = argExpsIterator.next().getValue();
		if (arg0.isPresent())
		{
			// arg0 is constant
			// We downsize the BigInteger value to int right away, because anyway inputs.size() is an
			// int, so we cannot do better and don't need to.
			final int nOfRequiredTrues = IntegerValue.class.cast(arg0.get()).getUnderlyingValue().intValueExact();
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

		/**
		 * TODO: optimize by checking the following: If any argument expression is constant BooleanAttributeValue False, remove it from the arguments, as it has no effect on the final result. Indeed,
		 * n-of function is commutative except for the first argument, and n-of(N, false, x, y...) = n-of(N, x, y...).
		 */
		return new CallWithVarArg0(functionSignature, argExpressions, remainingArgTypes);
	}

}
