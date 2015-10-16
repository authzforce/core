/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 */
package com.thalesgroup.authzforce.core.func;

import java.util.Deque;
import java.util.List;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;

/**
 * A superclass of all the standard comparison functions (return a boolean).
 * 
 * @param <AV>
 *            function parameter type
 */
public abstract class BaseComparisonFunction<AV extends AttributeValue<AV>> extends FirstOrderFunction<BooleanAttributeValue>
{
	private interface PostConditionChecker
	{
		boolean check(int comparisonResult);
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

	private final Datatype<AV> paramType;
	private final PostCondition postCondition;

	/**
	 * Creates a new <code>BaseComparisonFunction</code> object.
	 * 
	 * @param paramTypeDef
	 *            parameter type
	 * @param condition
	 *            post-condition to hold true when comparing the result of
	 *            {@link #compare(AttributeValue, AttributeValue)} to zero
	 * 
	 * @throws IllegalArgumentException
	 *             if the function is unknown
	 */
	public BaseComparisonFunction(DatatypeConstants<AV> paramTypeDef, PostCondition condition)
	{
		super(paramTypeDef.FUNCTION_ID_PREFIX + condition.functionSuffix, DatatypeConstants.BOOLEAN.TYPE, false, paramTypeDef.TYPE, paramTypeDef.TYPE);
		this.paramType = paramTypeDef.TYPE;
		this.postCondition = condition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected final FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return new EagerSinglePrimitiveTypeEval<BooleanAttributeValue, AV>(signature, paramType, argExpressions, remainingArgTypes)
		{
			@Override
			protected BooleanAttributeValue evaluate(Deque<AV> args) throws IndeterminateEvaluationException
			{
				return BooleanAttributeValue.valueOf(eval(args.poll(), args.poll()));
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
	public final boolean eval(AV arg0, AV arg1) throws IndeterminateEvaluationException
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
	protected abstract int compare(AV arg0, AV arg1) throws IndeterminateEvaluationException;
}
