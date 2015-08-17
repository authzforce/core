package com.thalesgroup.authzforce.core.func;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;

/**
 * A class that implements the n-of function. From the XACML spec
 * (urn:oasis:names:tc:xacml:1.0:function:n-of): the first argument to this function SHALL be of
 * data-type http://www.w3.org/2001/XMLSchema#integer. The remaining arguments SHALL be of data-type
 * http://www.w3.org/2001/XMLSchema#boolean. The first argument specifies the minimum number of the
 * remaining arguments that MUST evaluate to "True" for the expression to be considered "True". If
 * the first argument is 0, the result SHALL be "True". If the number of arguments after the first
 * one is less than the value of the first argument, then the expression SHALL result in
 * "Indeterminate". The order of evaluation SHALL be: first evaluate the integer value, and then
 * evaluate each subsequent argument. The evaluation SHALL stop and return "True" if the specified
 * number of arguments evaluate to "True". The evaluation of arguments SHALL stop if it is
 * determined that evaluating the remaining arguments will not satisfy the requirement.
 * <p>
 * This function evaluates the arguments one at a time, starting with the first one. As soon as the
 * result of the function can be determined, evaluation stops and that result is returned. During
 * this process, if any argument evaluates to indeterminate, an indeterminate result is returned.
 */
public class LogicalNOfFunction extends BaseFunction<PrimitiveResult<BooleanAttributeValue>>
{

	/**
	 * Standard identifier for the n-of function.
	 */
	public static final String NAME_N_OF = FUNCTION_NS_1 + "n-of";

	/**
	 * Creates a new <code>LogicalNOfFunction</code> object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the function is unknown
	 */
	public LogicalNOfFunction()
	{
		super(NAME_N_OF, BooleanAttributeValue.TYPE, true, IntegerAttributeValue.TYPE, BooleanAttributeValue.TYPE);
	}

	/**
	 * Returns a <code>Set</code> containing all the function identifiers supported by this class.
	 * 
	 * @return a <code>Set</code> of <code>String</code>s
	 */
	public static Set<String> getSupportedIdentifiers()
	{
		return Collections.singleton(NAME_N_OF);
	}

	private static final String INVALID_ARG_TYPE_MESSAGE_PREFIX = "Function " + NAME_N_OF + ": Invalid type (expected = " + BooleanAttributeValue.class.getName() + ") of arg#";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.BaseFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected Call getFunctionCall(final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes)
	{
		/*
		 * Arg datatypes and number is already checked in superclass but we need to do further
		 * checks specific to this function such as the first argument which must be a positive
		 * integer
		 */

		/**
		 * TODO: optimize this function call by checking the following:
		 * <ol>
		 * <li>If eval(null, checkedArgExpressions, null) returns a result (no
		 * IndeterminateEvaluationException thrown), then it will always return this result</li>
		 * <li>
		 * Else If any argument expression is constant BooleanAttributeValue False, remove it from
		 * the arguments and always decrement the first integer argument by one, as it has no effect
		 * on the final result. Indeed, n-of function is commutative except for the first argument,
		 * and n-of(N, false, x, y...) = n-of(N, x, y...).</li>
		 * </ol>
		 */

		return new Call(checkedRemainingArgTypes)
		{

			@Override
			protected PrimitiveResult<BooleanAttributeValue> evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
			{
				return eval(context, checkedArgExpressions, remainingArgs);
			}
		};
	}

	/**
	 * Logical 'n-of' evaluation method
	 * 
	 * @param context
	 * @param checkedArgExpressions
	 *            arg expression whose return type is assumed valid (already checked) for this
	 *            function
	 * @param checkedRemainingArgValues
	 *            remaining arg values, whose datatype is assumed valid (already checked) for this
	 *            function; may be null if none
	 * @return true iff all checkedArgExpressions return True and all remainingArgs are True
	 * @throws IndeterminateEvaluationException
	 */
	public PrimitiveResult<BooleanAttributeValue> eval(EvaluationContext context, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, AttributeValue[] checkedRemainingArgValues) throws IndeterminateEvaluationException
	{

		// Evaluate the arguments one by one. As soon as we can return
		// a result, do so. Return Indeterminate if any argument
		// evaluated is indeterminate.
		final Iterator<? extends Expression<?>> argExpsIterator = checkedArgExpressions.iterator();

		// Evaluate the first argument
		final Expression<?> input0 = argExpsIterator.next();
		final IntegerAttributeValue intAttrVal;
		try
		{
			intAttrVal = evalPrimitiveArg(input0, context, IntegerAttributeValue.class);
		} catch (IndeterminateEvaluationException e)
		{
			throw new IndeterminateEvaluationException(getIndeterminateArgMessage(0), Status.STATUS_PROCESSING_ERROR, e);
		}

		// intAttrVal is 'n' (number of Trues to reach)

		// We downsize the BigInteger value to int right away, because anyway inputs.size() is an
		// int, so we cannot do better and don't need to.
		int nOfRequiredTrues = intAttrVal.getValue().intValue();

		// If the number of trues needed is less than zero, report an error.
		if (nOfRequiredTrues < 0)
		{
			throw new IndeterminateEvaluationException("Function " + NAME_N_OF + ": Invalid arg #0: " + nOfRequiredTrues + ". Expected: (integer) >= 0", Status.STATUS_PROCESSING_ERROR);
		}

		// If the number of trues needed is zero, return true.
		if (nOfRequiredTrues == 0)
		{
			return PrimitiveResult.TRUE;
		}

		// else nOfRequiredTrues > 0
		// make sure it's possible to find n true values in the remaining arguments
		int nOfRemainingArgs = checkedArgExpressions.size() + (checkedRemainingArgValues == null ? 0 : checkedRemainingArgValues.length) - 1;
		if (nOfRequiredTrues > nOfRemainingArgs)
		{
			throw new IndeterminateEvaluationException("Function " + NAME_N_OF + ": Invalid arguments to n-of function: value of arg #0 (i.e. number of required Trues = " + nOfRequiredTrues + ") > value of arg #1 (i.e. number of remaining args = " + nOfRemainingArgs + ")",
					Status.STATUS_PROCESSING_ERROR);
		}

		// loop through the inputs, trying to find at least n trues
		int argIndex = 1;
		while (argExpsIterator.hasNext())
		{
			// evaluate the next argument
			final Expression<?> input = argExpsIterator.next();
			final BooleanAttributeValue attrVal;
			try
			{
				attrVal = evalPrimitiveArg(input, context, BooleanAttributeValue.class);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(getIndeterminateArgMessage(argIndex), Status.STATUS_PROCESSING_ERROR, e);
			}

			if (attrVal.getValue())
			{
				// we're one closer to our goal...see if we met it
				nOfRequiredTrues--;
				if (nOfRequiredTrues == 0)
				{
					return PrimitiveResult.TRUE;
				}

				if (nOfRequiredTrues > nOfRemainingArgs)
				{
					// check whether we have enough remaining args
					return PrimitiveResult.FALSE;
				}
			}

			nOfRemainingArgs--;
			argIndex++;
		}

		// do the same loop with remaining arg values
		if (checkedRemainingArgValues != null)
		{
			for (final AttributeValue arg : checkedRemainingArgValues)
			{
				final BooleanAttributeValue attrVal;
				try
				{
					attrVal = BooleanAttributeValue.class.cast(arg);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE_PREFIX + argIndex + ": " + arg.getClass().getName(), Status.STATUS_PROCESSING_ERROR, e);
				}

				if (attrVal.getValue())
				{
					// we're one closer to our goal...see if we met it
					nOfRequiredTrues--;
					if (nOfRequiredTrues == 0)
					{
						return PrimitiveResult.TRUE;
					}

					if (nOfRequiredTrues > nOfRemainingArgs)
					{
						// check whether we have enough remaining args
						return PrimitiveResult.FALSE;
					}
				}

				nOfRemainingArgs--;
				argIndex++;
			}
		}

		// if we got here then we didn't meet our quota
		return PrimitiveResult.FALSE;
	}

}
