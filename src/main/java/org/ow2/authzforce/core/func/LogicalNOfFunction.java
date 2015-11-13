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
package org.ow2.authzforce.core.func;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.expression.Expressions;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.BooleanValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeConstants;
import org.ow2.authzforce.core.value.IntegerValue;

/**
 * A class that implements the n-of function. From the XACML spec (urn:oasis:names:tc:xacml:1.0:function:n-of): the first argument to this function SHALL be of
 * data-type http://www.w3.org/2001/XMLSchema#integer. The remaining arguments SHALL be of data-type http://www.w3.org/2001/XMLSchema#boolean. The first
 * argument specifies the minimum number of the remaining arguments that MUST evaluate to "True" for the expression to be considered "True". If the first
 * argument is 0, the result SHALL be "True". If the number of arguments after the first one is less than the value of the first argument, then the expression
 * SHALL result in "Indeterminate". The order of evaluation SHALL be: first evaluate the integer value, and then evaluate each subsequent argument. The
 * evaluation SHALL stop and return "True" if the specified number of arguments evaluate to "True". The evaluation of arguments SHALL stop if it is determined
 * that evaluating the remaining arguments will not satisfy the requirement.
 * <p>
 * This function evaluates the arguments one at a time, starting with the first one. As soon as the result of the function can be determined, evaluation stops
 * and that result is returned. During this process, if any argument evaluates to indeterminate, an indeterminate result is returned.
 */
public final class LogicalNOfFunction extends FirstOrderFunction.MultiParameterTyped<BooleanValue>
{

	private static final class Call extends FirstOrderFunctionCall<BooleanValue>
	{
		private static final String INVALID_ARG_TYPE_MESSAGE_PREFIX = "Function " + NAME_N_OF + ": Invalid type (expected = " + BooleanValue.class.getName()
				+ ") of arg#";
		private static final String INDETERMINATE_ARG_MSG_PREFIX = "Function " + NAME_N_OF + ": Indeterminate arg #";
		private static final String INVALID_ARG0_MSG_PREFIX = "Function " + NAME_N_OF
				+ ": Invalid arg #0 (number of required Trues): expected: (integer) >= 0; actual: ";
		private static final String INVALID_ARGS_MSG_PREFIX = "Function " + NAME_N_OF
				+ ": Invalid arguments to n-of function: value of arg #0 (number of required Trues) > number of boolean args: ";
		private static final IndeterminateEvaluationException INDETERMINATE_ARG_EXCEPTION = new IndeterminateEvaluationException("Function " + NAME_N_OF
				+ ": evaluation failed because of indeterminate arg", StatusHelper.STATUS_PROCESSING_ERROR);
		private final List<Expression<?>> checkedArgExpressions;

		private Call(FunctionSignature<BooleanValue> functionSig, List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes,
				List<Expression<?>> checkedArgExpressions) throws IllegalArgumentException
		{
			super(functionSig, argExpressions, remainingArgTypes);
			this.checkedArgExpressions = checkedArgExpressions;
		}

		@Override
		protected BooleanValue evaluate(EvaluationContext context, AttributeValue... checkedRemainingArgs) throws IndeterminateEvaluationException
		{
			/*
			 * Arg datatypes and number is already checked in superclass but we need to do further checks specific to this function such as the first argument
			 * which must be a positive integer
			 */

			/**
			 * TODO: optimize this function call by checking the following:
			 * <ol>
			 * <li>If eval(null, checkedArgExpressions, null) returns a result (no IndeterminateEvaluationException thrown), then it will always return this
			 * result</li>
			 * <li>
			 * Else If any argument expression is constant BooleanAttributeValue False, remove it from the arguments and always decrement the first integer
			 * argument by one, as it has no effect on the final result. Indeed, n-of function is commutative except for the first argument, and n-of(N, false,
			 * x, y...) = n-of(N, x, y...).</li>
			 * </ol>
			 */

			// Evaluate the arguments one by one. As soon as we can return
			// a result, do so. Return Indeterminate if any argument
			// evaluated is indeterminate.
			final Iterator<? extends Expression<?>> argExpsIterator = checkedArgExpressions.iterator();

			// Evaluate the first argument
			final Expression<?> input0 = argExpsIterator.next();
			final IntegerValue intAttrVal;
			try
			{
				intAttrVal = Expressions.eval(input0, context, DatatypeConstants.INTEGER.TYPE);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(INDETERMINATE_ARG_MSG_PREFIX + 0, StatusHelper.STATUS_PROCESSING_ERROR, e);
			}

			// intAttrVal is 'n' (number of Trues to reach)

			// We downsize the BigInteger value to int right away, because anyway inputs.size() is an
			// int, so we cannot do better and don't need to.
			int nOfRequiredTrues = intAttrVal.getUnderlyingValue().intValue();

			// If the number of trues needed is less than zero, report an error.
			if (nOfRequiredTrues < 0)
			{
				throw new IndeterminateEvaluationException(INVALID_ARG0_MSG_PREFIX + nOfRequiredTrues, StatusHelper.STATUS_PROCESSING_ERROR);
			}

			// If the number of trues needed is zero, return true.
			if (nOfRequiredTrues == 0)
			{
				return BooleanValue.TRUE;
			}

			// else nOfRequiredTrues > 0
			// make sure it's possible to find n true values in the remaining arguments
			int nOfRemainingArgs = checkedArgExpressions.size() + (checkedRemainingArgs == null ? 0 : checkedRemainingArgs.length) - 1;
			if (nOfRequiredTrues > nOfRemainingArgs)
			{
				throw new IndeterminateEvaluationException(INVALID_ARGS_MSG_PREFIX + nOfRequiredTrues + " > " + nOfRemainingArgs,
						StatusHelper.STATUS_PROCESSING_ERROR);
			}

			IndeterminateEvaluationException lastIndeterminateException = null;
			int nOfIndeterminateArgs = 0;
			// loop through the inputs, trying to find at least n trues
			int argIndex = 1;
			while (argExpsIterator.hasNext())
			{
				// evaluate the next argument
				final Expression<?> input = argExpsIterator.next();
				final BooleanValue attrVal;
				try
				{
					attrVal = Expressions.eval(input, context, DatatypeConstants.BOOLEAN.TYPE);
					if (attrVal.getUnderlyingValue())
					{
						// we're one closer to our goal...see if we met it
						nOfRequiredTrues--;
						if (nOfRequiredTrues == 0)
						{
							return BooleanValue.TRUE;
						}

						// nOfRequiredTrues != 0
						if (nOfRequiredTrues == nOfIndeterminateArgs)
						{
							// nOfIndeterminateArgs != 0 as well
							// if all indeterminate args have been TRUE, result would be TRUE ->
							// indeterminate result
							if (lastIndeterminateException == null)
							{
								// this should not happen in theory as lastIndeterminateException !=
								// null when nOfIndeterminateArgs != 0
								throw INDETERMINATE_ARG_EXCEPTION;
							}

							throw lastIndeterminateException;
						}

						final int nOfPossibleTrues = nOfRemainingArgs + nOfIndeterminateArgs;
						if (nOfRequiredTrues > nOfPossibleTrues)
						{
							// check whether we have enough remaining args
							return BooleanValue.FALSE;
						}
					}
				} catch (IndeterminateEvaluationException e)
				{
					// keep the indeterminate arg error to throw later, in case there was not enough
					// TRUEs in the remaining args
					lastIndeterminateException = new IndeterminateEvaluationException(INDETERMINATE_ARG_MSG_PREFIX + argIndex,
							StatusHelper.STATUS_PROCESSING_ERROR, e);
					nOfIndeterminateArgs++;
				}

				nOfRemainingArgs--;
				argIndex++;
			}

			// do the same loop with remaining arg values
			if (checkedRemainingArgs != null)
			{
				for (final AttributeValue arg : checkedRemainingArgs)
				{
					final BooleanValue attrVal;
					try
					{
						attrVal = BooleanValue.class.cast(arg);
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE_PREFIX + argIndex + ": " + arg.getClass().getName(),
								StatusHelper.STATUS_PROCESSING_ERROR, e);
					}

					if (attrVal.getUnderlyingValue())
					{
						// we're one closer to our goal...see if we met it
						nOfRequiredTrues--;
						if (nOfRequiredTrues == 0)
						{
							return BooleanValue.TRUE;
						}

						if (nOfRequiredTrues > nOfRemainingArgs)
						{
							// check whether we have enough remaining args
							return BooleanValue.FALSE;
						}
					}

					nOfRemainingArgs--;
					argIndex++;
				}
			}

			// if we got here then we didn't meet our quota
			// nOfRequiredTrues != 0
			if (nOfRequiredTrues == nOfIndeterminateArgs)
			{
				// nOfIndeterminateArgs != 0 as well, so lastIndeterminateException != null
				// if all indeterminate args have been TRUE, result would be TRUE -> indeterminate
				// result
				if (lastIndeterminateException == null)
				{
					// this should not happen in theory as lastIndeterminateException !=
					// null when nOfIndeterminateArgs != 0
					throw INDETERMINATE_ARG_EXCEPTION;
				}

				throw lastIndeterminateException;
			}

			return BooleanValue.FALSE;
		}
	}

	/**
	 * Standard identifier for the n-of function.
	 */
	public static final String NAME_N_OF = XACML_NS_1_0 + "n-of";

	/**
	 * Creates a new <code>LogicalNOfFunction</code> object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the function is unknown
	 */
	public LogicalNOfFunction()
	{
		super(NAME_N_OF, DatatypeConstants.BOOLEAN.TYPE, true, Arrays.asList(DatatypeConstants.INTEGER.TYPE, DatatypeConstants.BOOLEAN.TYPE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> checkedArgExpressions, Datatype<?>... remainingArgTypes)
			throws IllegalArgumentException
	{
		return new Call(functionSignature, checkedArgExpressions, remainingArgTypes, checkedArgExpressions);
	}

}
