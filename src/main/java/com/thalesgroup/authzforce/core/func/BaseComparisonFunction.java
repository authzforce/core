/**
 *
 */
package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;

/**
 * A superclass of all the standard comparison functions (return a boolean).
 * 
 * @param <T>
 *            function parameter type
 */
public abstract class BaseComparisonFunction<T extends AttributeValue> extends BaseFunction<PrimitiveResult<BooleanAttributeValue>>
{
	private static interface PostConditionChecker
	{
		public boolean check(int comparisonResult);
	}

	protected static enum PostCondition
	{
		/**
		 * 
		 */
		GREATER_THAN("-greater-than", new PostConditionChecker()
		{
			@Override
			public boolean check(int comparisonResult)
			{
				return comparisonResult > 0;
			}
		}),
		/**
		 * 
		 */
		GREATER_THAN_OR_EQUAL("-greater-than-or-equal", new PostConditionChecker()
		{
			@Override
			public boolean check(int comparisonResult)
			{
				return comparisonResult >= 0;
			}
		}),
		/**
		 * 
		 */
		LESS_THAN("-less-than", new PostConditionChecker()
		{
			@Override
			public boolean check(int comparisonResult)
			{
				return comparisonResult < 0;
			}
		}),
		/**
		 * 
		 */
		LESS_THAN_OR_EQUAL("-less-than-or-equal", new PostConditionChecker()
		{
			@Override
			public boolean check(int comparisonResult)
			{
				return comparisonResult <= 0;
			}
		});

		private final String functionSuffix;
		private final PostConditionChecker checker;

		private PostCondition(String funcSuffix, PostConditionChecker checker)
		{
			this.functionSuffix = funcSuffix;
			this.checker = checker;
		}

		private boolean isTrue(int comparisonResult)
		{
			return checker.check(comparisonResult);
		}
	}

	private final Class<T[]> paramArrayClass;
	private final PostCondition postCondition;

	/**
	 * Creates a new <code>BaseComparisonFunction</code> object.
	 * 
	 * @param funcIdPrefix
	 *            function ID prefix up to the first hyphen (not included) or full ID if no hyphen
	 * 
	 * @param paramTypeURI
	 *            parameter type URI
	 * @param paramArrayType
	 *            parameter array type
	 * @param condition
	 *            post-condition to hold true when comparing the result of
	 *            {@link #compare(AttributeValue, AttributeValue)} to zero
	 * 
	 * @throws IllegalArgumentException
	 *             if the function isn't known
	 */
	public BaseComparisonFunction(String funcIdPrefix, String paramTypeURI, Class<T[]> paramArrayType, PostCondition condition)
	{
		super(funcIdPrefix + condition.functionSuffix, BooleanAttributeValue.TYPE, false, new DatatypeDef(paramTypeURI), new DatatypeDef(paramTypeURI));
		this.paramArrayClass = paramArrayType;
		this.postCondition = condition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.BaseFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected final Call getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes)
	{
		return new EagerPrimitiveEvalCall<T>(paramArrayClass, checkedArgExpressions, checkedRemainingArgTypes)
		{
			@Override
			protected PrimitiveResult<BooleanAttributeValue> evaluate(T[] args) throws IndeterminateEvaluationException
			{
				return PrimitiveResult.getInstance(eval(args[0], args[1]));
			}

		};
	}

	/**
	 * Function comparing two arguments like <code>arg0.compareTo(arg1)</code> on
	 * {@link Comparable#compareTo(Object)} objects. and checking {@link #postCondition} on the
	 * comparison result
	 * 
	 * @param arg0
	 * @param arg1
	 * @return result
	 * @throws IndeterminateEvaluationException
	 *             thrown when relationship between the arguments is indeterminate
	 */
	public final boolean eval(T arg0, T arg1) throws IndeterminateEvaluationException
	{

		// Now that we have real values, perform the comparison operation
		final int comparResult = compare(arg0, arg1);
		// Return the result as a BooleanAttributeValue.
		return postCondition.isTrue(comparResult);
	}

	/**
	 * Compare two arguments in the same way as {@link Comparable#compareTo(Object)}:
	 * <code>arg0.compareTo(arg1)</code>. Except the relationship between attrVal0 and attrVal1 may
	 * be indeterminate, like for XML schema date/time types.
	 * 
	 * @param arg0
	 * @param arg1
	 * @return same as result as
	 * @throws IndeterminateEvaluationException
	 *             thrown when relationship between the arguments is indeterminate
	 */
	protected abstract int compare(T arg0, T arg1) throws IndeterminateEvaluationException;
}
