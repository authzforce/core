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
/**
 *
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;

/**
 * A superclass of all the standard comparison functions (return a boolean).
 *
 * @param <AV>
 *            function parameter type
 * 
 * @version $Id: $
 */
public final class ComparisonFunction<AV extends AttributeValue & Comparable<AV>> extends FirstOrderFunction.SingleParameterTyped<BooleanValue, AV>
{
	enum PostCondition
	{

		/**
		 * 
		 */
		GREATER_THAN("-greater-than", new Checker()
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
		GREATER_THAN_OR_EQUAL("-greater-than-or-equal", new Checker()
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
		LESS_THAN("-less-than", new Checker()
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
		LESS_THAN_OR_EQUAL("-less-than-or-equal", new Checker()
		{
			@Override
			public boolean check(int comparisonResult)
			{
				return comparisonResult <= 0;
			}
		});

		private final String functionSuffix;
		private final Checker checker;

		private PostCondition(String funcSuffix, Checker checker)
		{
			this.functionSuffix = funcSuffix;
			this.checker = checker;
		}

		boolean isTrue(int comparisonResult)
		{
			return checker.check(comparisonResult);
		}

		private interface Checker
		{
			boolean check(int comparisonResult);
		}
	}

	private static final class CallFactory<V extends AttributeValue & Comparable<V>>
	{
		private final PostCondition postCondition;
		private final FunctionSignature.SingleParameterTyped<BooleanValue, V> funcSig;
		private final String illegalComparisonMsgPrefix;

		/**
		 * Creates comparison function call factory
		 * 
		 * @param condition
		 *            post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compateTo() function is similar
		 *            to {@link Comparable#compareTo(Object)}.
		 */
		private CallFactory(FunctionSignature.SingleParameterTyped<BooleanValue, V> functionSig, PostCondition postCondition)
		{
			this.funcSig = functionSig;
			this.postCondition = postCondition;
			illegalComparisonMsgPrefix = "Function " + funcSig.getName() + ": cannot compare arguments: ";
		}

		private FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
				throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<BooleanValue, V>(funcSig, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(Deque<V> args) throws IndeterminateEvaluationException
				{
					// Now that we have real values, perform the comparison operation
					final V arg0 = args.poll();
					final V arg1 = args.poll();
					final int comparResult;
					try
					{
						comparResult = arg0.compareTo(arg1);
					} catch (IllegalArgumentException e)
					{
						// See BaseTimeValue#compareTo() for example of comparison throwing such exception
						throw new IndeterminateEvaluationException(illegalComparisonMsgPrefix + arg0.getContent() + ", " + arg1.getContent(),
								StatusHelper.STATUS_PROCESSING_ERROR, e);
					}
					// Return the result as a BooleanAttributeValue.
					return BooleanValue.valueOf(postCondition.isTrue(comparResult));
				}
			};
		}

	}

	private final CallFactory<AV> funcCallFactory;

	/**
	 * Creates a new <code>BaseComparisonFunction</code> object.
	 * 
	 * @param paramTypeDef
	 *            parameter type
	 * @param postCondition
	 *            post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compateTo() function is similar to
	 *            {@link Comparable#compareTo(Object)}
	 * 
	 * @throws IllegalArgumentException
	 *             if the function is unknown
	 */
	private ComparisonFunction(DatatypeConstants<AV> paramTypeDef, PostCondition postCondition)
	{
		super(paramTypeDef.FUNCTION_ID_PREFIX + postCondition.functionSuffix, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(paramTypeDef.TYPE,
				paramTypeDef.TYPE));
		this.funcCallFactory = new CallFactory<>(functionSignature, postCondition);
	}

	private static Set<Function<?>> getTotalComparisonFunctions()
	{
		final Set<Function<?>> mutableSet = new HashSet<>();
		for (final PostCondition condition : PostCondition.values())
		{
			mutableSet.add(new ComparisonFunction<>(DatatypeConstants.INTEGER, condition));
			mutableSet.add(new ComparisonFunction<>(DatatypeConstants.DOUBLE, condition));
			mutableSet.add(new ComparisonFunction<>(DatatypeConstants.STRING, condition));
		}

		return mutableSet;
	}

	/**
	 * Set of functions implementing comparison of {@link Comparable} values, i.e. imposing total ordering of compared objects. In particular, this applies to
	 * all numeric types (integers, doubles...) and string, but not to XML schema date/times that may have indeterminate relationship to each other (see
	 * {@link #TEMPORAL_SET}).
	 */
	public static final FunctionSet TOTAL_ORDER_SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "total-order-comparison",
			getTotalComparisonFunctions());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

	private static Set<Function<?>> getTemporalFunctions()
	{
		final Set<Function<?>> mutableSet = new HashSet<>();
		for (final PostCondition condition : PostCondition.values())
		{
			mutableSet.add(new ComparisonFunction<>(DatatypeConstants.TIME, condition));
			mutableSet.add(new ComparisonFunction<>(DatatypeConstants.DATE, condition));
			mutableSet.add(new ComparisonFunction<>(DatatypeConstants.DATETIME, condition));
		}

		return mutableSet;
	}

	/**
	 * Set of functions comparing XML schema date/time values, i.e. not imposing total ordering of compared objects, as such date/times may have indeterminate
	 * relationship to each other.
	 * 
	 */
	public static final FunctionSet TEMPORAL_SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-comparison", getTemporalFunctions());
}
