package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * A class that implements the logical function "and".
 * <p>
 * From XACML core specification of function 'urn:oasis:names:tc:xacml:1.0:function:and': This
 * function SHALL return "True" if it has no arguments and SHALL return "False" if one of its
 * arguments evaluates to "False". The order of evaluation SHALL be from first argument to last. The
 * evaluation SHALL stop with a result of "False" if any argument evaluates to "False", leaving the
 * rest of the arguments unevaluated.
 * 
 */
public class LogicalAndFunction extends FirstOrderFunction<BooleanAttributeValue>
{
	/**
	 * XACML standard TYPE_URI for the "and" logical function
	 */
	public static final String NAME_AND = FUNCTION_NS_1 + "and";

	/**
	 * Instantiates the function
	 * 
	 */
	public LogicalAndFunction()
	{
		super(NAME_AND, BooleanAttributeValue.TYPE, true, BooleanAttributeValue.TYPE);
	}

	private static final String INVALID_ARG_TYPE_MESSAGE_PREFIX = "Function " + NAME_AND + ": Invalid type (expected = " + BooleanAttributeValue.class.getName() + ") of arg#";

	/**
	 * Logical 'and' evaluation method
	 * 
	 * @param context
	 * @param checkedArgExpressions
	 *            arg expression whose return type is assumed valid (already checked) for this
	 *            function
	 * @param checkedRemainingArgs
	 *            remaining arg values, whose datatype is assumed valid (already checked) for this
	 *            function
	 * @return true iff all checkedArgExpressions return True and all remainingArgs are True
	 * @throws IndeterminateEvaluationException
	 */
	public BooleanAttributeValue eval(EvaluationContext context, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, AttributeValue[] checkedRemainingArgs) throws IndeterminateEvaluationException
	{
		int argIndex = 0;
		for (final Expression<? extends ExpressionResult<? extends AttributeValue>> arg : checkedArgExpressions)
		{
			// Evaluate the argument
			final BooleanAttributeValue attrVal;
			try
			{
				attrVal = Utils.evalPrimitiveArg(arg, context, BooleanAttributeValue.class);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(getIndeterminateArgMessage(argIndex), Status.STATUS_PROCESSING_ERROR, e);
			}

			if (!attrVal.getValue())
			{
				return BooleanAttributeValue.FALSE;
			}

			argIndex++;
		}

		// do the same with remaining arg values
		for (final AttributeValue arg : checkedRemainingArgs)
		{
			// Evaluate the argument
			final BooleanAttributeValue attrVal;
			try
			{
				attrVal = BooleanAttributeValue.class.cast(arg);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException(INVALID_ARG_TYPE_MESSAGE_PREFIX + argIndex + ": " + arg.getClass().getName(), Status.STATUS_PROCESSING_ERROR, e);
			}

			if (!attrVal.getValue())
			{
				return BooleanAttributeValue.FALSE;
			}

			argIndex++;
		}

		return BooleanAttributeValue.TRUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes)
	{
		/**
		 * TODO: optimize this function call by checking the following:
		 * <ol>
		 * <li>If any argument expression is constant BooleanAttributeValue False, return always
		 * False.</li>
		 * <li>Else If all argument expressions are constant BooleanAttributeValue True, return
		 * always True.</li>
		 * <li>
		 * Else If any argument expression is constant BooleanAttributeValue True, remove it from
		 * the arguments, as it has no effect on the final result. Indeed, and function is
		 * commutative and and(true, x, y...) = and(x, y...).</li>
		 * </ol>
		 * The first two optimizations can be achieved by pre-evaluating the function call with
		 * context = null and check the result if no IndeterminateEvaluationException is thrown.
		 */

		return new FirstOrderFunctionCall<BooleanAttributeValue>(signature, argExpressions, remainingArgTypes)
		{

			@Override
			protected BooleanAttributeValue evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
			{
				return eval(context, argExpressions, remainingArgs);
			}
		};
	}
}
