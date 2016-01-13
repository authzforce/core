/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Bag;
import org.ow2.authzforce.core.pdp.api.BagDatatype;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.Expressions;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.FunctionCall;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;

/**
 * Set of higher-order bag functions
 *
 */
public final class HigherOrderBagFunctionSet
{
	/**
	 * Standard identifier for the any-of function. WARNING: XACML 1.0 any-of planned for deprecation as of XACML 3.0. Only 3.0 version supported henceforth.
	 */
	public static final String NAME_ANY_OF = Function.XACML_NS_3_0 + "any-of";

	/**
	 * Standard identifier for the all-of function.
	 */
	public static final String NAME_ALL_OF = Function.XACML_NS_3_0 + "all-of";

	/**
	 * Standard identifier for the any-of-any function.
	 */
	public static final String NAME_ANY_OF_ANY = Function.XACML_NS_3_0 + "any-of-any";

	/**
	 * Standard identifier for the all-of-any function.
	 */
	public static final String NAME_ALL_OF_ANY = Function.XACML_NS_1_0 + "all-of-any";

	/**
	 * Standard identifier for the any-of-all function.
	 */
	public static final String NAME_ANY_OF_ALL = Function.XACML_NS_1_0 + "any-of-all";

	/**
	 * Standard identifier for the all-of-all function.
	 */
	public static final String NAME_ALL_OF_ALL = Function.XACML_NS_1_0 + "all-of-all";

	private static abstract class BooleanHigherOrderBagFunction extends HigherOrderBagFunction<BooleanValue, BooleanValue>
	{
		private BooleanHigherOrderBagFunction(String functionId)
		{
			super(functionId, DatatypeConstants.BOOLEAN.TYPE, DatatypeConstants.BOOLEAN.TYPE);
		}
	}

	/**
	 * Higher-order boolean function taking three arguments: sub-function and two bags
	 * 
	 */
	private static abstract class BooleanHigherOrderTwoBagFunction extends BooleanHigherOrderBagFunction
	{
		private final IllegalArgumentException invalidLastArgTypeException = new IllegalArgumentException("Function" + this
				+ ": Invalid last argument type: primitive (not a bag). Required: a bag");

		private BooleanHigherOrderTwoBagFunction(String functionName)
		{
			super(functionName);
		}

		@Override
		protected final void checkNumberOfArgs(int numInputs)
		{
			if (numInputs != 3)
			{
				throw new IllegalArgumentException("Function " + this + ": Invalid number of arguments (" + numInputs + "). Required: 3");
			}
		}

		private static abstract class Call implements FunctionCall<BooleanValue>
		{
			protected final FirstOrderFunctionCall<BooleanValue> subFuncCall;
			private final String errorEvalArg1Message;
			private final String errorEvalArg2Message;
			private final Expression<? extends Bag<?>> bagArgExpr0;
			private final Expression<? extends Bag<?>> bagArgExpr1;

			private Call(String functionId, FirstOrderFunction<BooleanValue> subFunc, Expression<? extends Bag<?>> input0, Expression<? extends Bag<?>> input1)
			{
				final Datatype<?>[] subFuncArgTypes = { input0.getReturnType().getTypeParameter(), input1.getReturnType().getTypeParameter() };
				this.subFuncCall = subFunc.newCall(Collections.<Expression<?>> emptyList(), subFuncArgTypes);
				this.bagArgExpr0 = input0;
				this.bagArgExpr1 = input1;
				this.errorEvalArg1Message = "Function " + functionId + ": Error evaluating second arg #1";
				this.errorEvalArg2Message = "Function " + functionId + ": Error evaluating arg #2";
			}

			protected abstract BooleanValue evaluate(Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException;

			@Override
			public final BooleanValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
			{
				final Bag<?> bag0;
				try
				{
					bag0 = bagArgExpr0.evaluate(context);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(errorEvalArg1Message, StatusHelper.STATUS_PROCESSING_ERROR);
				}

				/*
				 * If result bag empty, returns False as there will be no possibility for a Predicate that is "True". AttributeDesignator/AttributeSelector with
				 * MustBePresent=False may evaluate to empty bags (Indeterminate Exception if MustBePresent=True). empty bag.
				 */
				if (bag0.isEmpty())
				{
					return BooleanValue.FALSE;
				}

				final Bag<?> bag1;
				try
				{
					bag1 = bagArgExpr1.evaluate(context);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(errorEvalArg2Message, StatusHelper.STATUS_PROCESSING_ERROR);
				}

				if (bag1.isEmpty())
				{
					return BooleanValue.FALSE;
				}

				return evaluate(bag0, bag1, context);
			}

			@Override
			public final Datatype<BooleanValue> getReturnType()
			{
				return DatatypeConstants.BOOLEAN.TYPE;
			}

		}

		/**
		 * Creates function call
		 * 
		 * @param subFunc
		 *            sub-function
		 * @param arg0
		 *            first input expression returning a bag
		 * @param arg1
		 *            second input expression returning a bag
		 * @return function call
		 */
		protected abstract Call newFunctionCall(FirstOrderFunction<BooleanValue> subFunc, Expression<? extends Bag<?>> arg0, Expression<? extends Bag<?>> arg1);

		@Override
		protected final FunctionCall<BooleanValue> createFunctionCallFromSubFunction(FirstOrderFunction<BooleanValue> subFunc,
				List<Expression<?>> inputsAfterSubFunc)
		{

			final Iterator<Expression<?>> inputsAfterSubfuncIterator = inputsAfterSubFunc.iterator();

			while (inputsAfterSubfuncIterator.hasNext())
			{
				// all must be bag
				if (inputsAfterSubfuncIterator.next().getReturnType().getTypeParameter() == null)
				{
					throw invalidLastArgTypeException;
				}
			}

			final Expression<? extends Bag<?>> input0 = (Expression<? extends Bag<?>>) inputsAfterSubFunc.get(0);
			final Expression<? extends Bag<?>> input1 = (Expression<? extends Bag<?>>) inputsAfterSubFunc.get(1);
			return newFunctionCall(subFunc, input0, input1);
		}
	}

	/**
	 * one-bag-only functions (only last arg is bag): any-of, all-of, map.
	 * 
	 */
	static abstract class OneBagOnlyHigherOrderFunction<RETURN_T extends Value, SUB_RETURN_T extends AttributeValue> extends
			HigherOrderBagFunction<RETURN_T, SUB_RETURN_T>
	{
		private final String invalidArityMsgPrefix = "Function " + this + ": Invalid number of arguments: expected: >= 2; actual: ";
		private final String unexpectedBagInputErrorMsg = " Function " + this + ": Invalid type (expected: primitive, actual: bag) of argument #";
		private final IllegalArgumentException invalidLastArgTypeException = new IllegalArgumentException(this
				+ ": Invalid last argument type: expected: bag; actual: primitive");

		static abstract class Call<RETURN extends Value, SUB_RETURN extends AttributeValue> implements FunctionCall<RETURN>
		{
			private final String errorEvalLastArgMsg;
			protected final FirstOrderFunctionCall<SUB_RETURN> subFuncCall;
			private final Expression<?> lastArgBagExpr;
			private final BagDatatype<?> lastArgBagDatatype;
			private final Datatype<RETURN> returnType;

			protected Call(String functionId, Datatype<RETURN> returnType, FirstOrderFunction<SUB_RETURN> subFunction, List<Expression<?>> primitiveInputs,
					Expression<?> lastInputBag)
			{
				final Datatype<?> lastArgExpDatatype = lastInputBag.getReturnType();
				if (lastArgExpDatatype.getTypeParameter() == null)
				{
					throw new IllegalArgumentException("Function " + functionId + ": last argument expression's return type: expected: Bag; actual: "
							+ lastArgExpDatatype);
				}

				lastArgBagExpr = lastInputBag;
				// Bag.Datatype is the only Datatype implementation for Datatype<Bag<?>>
				lastArgBagDatatype = (BagDatatype<?>) lastArgExpDatatype;

				/*
				 * The actual expression passed as last argument to the sub-function is not yet known; but we know the expected datatype is the type of each
				 * element lastInputBag's evaluation result bag, therefore the element datatype, i.e. type parameter to the returned bag datatype
				 */

				this.subFuncCall = subFunction.newCall(primitiveInputs, lastInputBag.getReturnType().getTypeParameter());
				this.errorEvalLastArgMsg = "Function " + functionId + ": Error evaluating last arg (bag)";
				this.returnType = returnType;
			}

			/**
			 * Combines the results of evaluations of a sub-function with the following combination of arguments: (arg_0,..., arg_n-1, bag[i]), for all values
			 * bag[i] of a bag (i=0..bag.size()-1), and (arg_0,... arg_n-1) a constant list
			 * 
			 * @param subFunc
			 *            the sub-function to be called with the various combination of arguments
			 * @param subFuncArgsBeforeLast
			 *            the (arg_0,... arg_n-1) constant list
			 * @param lastArgBagExpr
			 *            the bag of which each value is used as the last argument in a combination with <code>subFuncArgsBeforeLast</code>
			 * @param context
			 *            evaluation context
			 * @return final result
			 * @throws IndeterminateEvaluationException
			 */
			protected abstract RETURN evaluate(Bag<?> lastArgBag, EvaluationContext context) throws IndeterminateEvaluationException;

			@Override
			public final RETURN evaluate(EvaluationContext context) throws IndeterminateEvaluationException
			{
				final Bag<?> lastArgBag;
				try
				{
					lastArgBag = Expressions.eval(lastArgBagExpr, context, lastArgBagDatatype);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(errorEvalLastArgMsg, e.getStatusCode(), e);
				}

				return evaluate(lastArgBag, context);
			}

			@Override
			public final Datatype<RETURN> getReturnType()
			{
				return returnType;
			}
		}

		/*
		 * 'protected' because used by separate MapFunction class
		 */
		protected OneBagOnlyHigherOrderFunction(String functionName, Datatype<RETURN_T> returnType, Datatype<SUB_RETURN_T> subFunctionReturnType)
		{
			super(functionName, returnType, subFunctionReturnType);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.HigherOrderBagFunction#checkNumberOfArgs(int)
		 */
		@Override
		protected final void checkNumberOfArgs(int numInputs)
		{
			if (numInputs < 2)
			{
				throw new IllegalArgumentException(invalidArityMsgPrefix + numInputs);
			}
		}

		/**
		 * Creates function call
		 * 
		 * @param subFunc
		 *            sub-function
		 * @param primitiveInputs
		 *            all arguments before last, all primitive (datatype already checked).
		 * @param lastInputBag
		 *            last argument - bag (datatype already checked)
		 * @return function call
		 */
		protected abstract Call<RETURN_T, SUB_RETURN_T> newFunctionCall(FirstOrderFunction<SUB_RETURN_T> subFunc, List<Expression<?>> primitiveInputs,
				Expression<?> lastInputBag);

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.HigherOrderBagFunction# createFunctionCallFromSubFunction
		 * (com.thalesgroup.authzforce.core.func.FirstOrderFunction, java.util.List, java.util.List)
		 */
		@Override
		protected final FunctionCall<RETURN_T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_T> subFunc,
				List<Expression<?>> inputsAfterSubFunc)
		{
			final Iterator<Expression<?>> inputsAfterSubfuncIterator = inputsAfterSubFunc.iterator();
			// inputs that we can parse/validate for the sub-function are the primitive inputs, i.e.
			// all except last one which is a bag
			final List<Expression<?>> primitiveInputs = new ArrayList<>();
			int argIndex = 0;
			Expression<?> lastInputBag = null;
			boolean hasNextInput = true;
			while (hasNextInput)
			{
				final Expression<?> input = inputsAfterSubfuncIterator.next();
				argIndex++;
				hasNextInput = inputsAfterSubfuncIterator.hasNext();
				final Datatype<?> inputType = input.getReturnType();
				if (hasNextInput)
				{
					// not the last input, must be primitive
					if (inputType.getTypeParameter() != null)
					{
						throw new IllegalArgumentException(unexpectedBagInputErrorMsg + argIndex);
					}

					primitiveInputs.add(input);
				} else
				{
					// last input, must be a bag
					if (inputType.getTypeParameter() == null)
					{
						throw invalidLastArgTypeException;
					}

					lastInputBag = input;
				}
			}

			return newFunctionCall(subFunc, primitiveInputs, lastInputBag);
		}

	}

	private static final class BooleanOneBagOnlyFunction extends OneBagOnlyHigherOrderFunction<BooleanValue, BooleanValue>
	{
		private interface CallFactory
		{
			OneBagOnlyHigherOrderFunction.Call<BooleanValue, BooleanValue> getInstance(FirstOrderFunction<BooleanValue> subFunc,
					List<Expression<?>> primitiveInputs, Expression<?> lastInputBag);
		}

		private final CallFactory funcCallFactory;

		protected BooleanOneBagOnlyFunction(String functionId, CallFactory functionCallFactory)
		{
			super(functionId, DatatypeConstants.BOOLEAN.TYPE, DatatypeConstants.BOOLEAN.TYPE);
			this.funcCallFactory = functionCallFactory;
		}

		@Override
		protected OneBagOnlyHigherOrderFunction.Call<BooleanValue, BooleanValue> newFunctionCall(FirstOrderFunction<BooleanValue> subFunc,
				List<Expression<?>> primitiveInputs, Expression<?> lastInputBag)
		{
			return funcCallFactory.getInstance(subFunc, primitiveInputs, lastInputBag);
		}

	}

	/**
	 * any-of function
	 * 
	 */
	private static final class AnyOfCallFactory implements BooleanOneBagOnlyFunction.CallFactory
	{
		private static final String SUBFUNC_CALL_WITH_LAST_ARG_ERROR_MSG_PREFIX = "Function " + NAME_ANY_OF
				+ ": Error calling sub-function (specified as first argument) with last arg=";

		@Override
		public OneBagOnlyHigherOrderFunction.Call<BooleanValue, BooleanValue> getInstance(FirstOrderFunction<BooleanValue> subFunc,
				List<Expression<?>> primitiveInputs, Expression<?> lastInputBag)
		{
			return new OneBagOnlyHigherOrderFunction.Call<BooleanValue, BooleanValue>(NAME_ANY_OF, DatatypeConstants.BOOLEAN.TYPE, subFunc, primitiveInputs,
					lastInputBag)
			{

				@Override
				protected BooleanValue evaluate(Bag<?> lastArgBag, EvaluationContext context) throws IndeterminateEvaluationException
				{
					for (final AttributeValue attrVal : lastArgBag)
					{
						final BooleanValue subResult;
						try
						{
							subResult = subFuncCall.evaluate(context, attrVal);
						} catch (IndeterminateEvaluationException e)
						{
							throw new IndeterminateEvaluationException(SUBFUNC_CALL_WITH_LAST_ARG_ERROR_MSG_PREFIX + attrVal, e.getStatusCode(), e);
						}

						if (subResult.getUnderlyingValue())
						{
							return BooleanValue.TRUE;
						}
					}

					return BooleanValue.FALSE;
				}

			};
		}

	}

	/**
	 * all-of function
	 * 
	 */
	private static final class AllOfCallFactory implements BooleanOneBagOnlyFunction.CallFactory
	{
		private static final String SUBFUNC_CALL_WITH_LAST_ARG_ERROR_MSG_PREFIX = "Function " + NAME_ALL_OF
				+ ": Error calling sub-function (specified as first argument) with last arg=";

		@Override
		public OneBagOnlyHigherOrderFunction.Call<BooleanValue, BooleanValue> getInstance(FirstOrderFunction<BooleanValue> subFunc,
				List<Expression<?>> primitiveInputs, Expression<?> lastInputBag)
		{
			return new OneBagOnlyHigherOrderFunction.Call<BooleanValue, BooleanValue>(NAME_ALL_OF, DatatypeConstants.BOOLEAN.TYPE, subFunc, primitiveInputs,
					lastInputBag)
			{

				@Override
				protected BooleanValue evaluate(Bag<?> lastArgBag, EvaluationContext context) throws IndeterminateEvaluationException
				{
					for (final AttributeValue attrVal : lastArgBag)
					{
						final BooleanValue subResult;
						try
						{
							subResult = subFuncCall.evaluate(context, attrVal);
						} catch (IndeterminateEvaluationException e)
						{
							throw new IndeterminateEvaluationException(SUBFUNC_CALL_WITH_LAST_ARG_ERROR_MSG_PREFIX + attrVal, e.getStatusCode(), e);
						}

						if (!subResult.getUnderlyingValue())
						{
							return BooleanValue.FALSE;
						}
					}

					return BooleanValue.TRUE;
				}

			};
		}

	}

	/**
	 * any-of-any function
	 * 
	 */
	private static final class AnyOfAny extends BooleanHigherOrderBagFunction
	{

		private static final String SUB_FUNC_EVAL_ERROR_MSG = "Function " + NAME_ANY_OF_ANY
				+ ": Error evaluating one of the arguments after sub-function (first arg)";

		/**
		 * Default constructor
		 */
		private AnyOfAny()
		{
			super(NAME_ANY_OF_ANY);
		}

		@Override
		protected void checkNumberOfArgs(int numInputs)
		{
			if (numInputs < 2)
			{
				throw new IllegalArgumentException("Function " + this + ": Invalid number of arguments (" + numInputs + "). Required: >= 2");
			}
		}

		@Override
		protected FunctionCall<BooleanValue> createFunctionCallFromSubFunction(FirstOrderFunction<BooleanValue> subFunc, List<Expression<?>> inputsAfterSubFunc)
		{
			return new AnyOfAnyFunctionCall(subFunc, inputsAfterSubFunc);
		}

		private static final class AnyOfAnyFunctionCall implements FunctionCall<BooleanValue>
		{
			private final FirstOrderFunctionCall<BooleanValue> subFuncCall;
			private final int subFuncArity;
			private final List<Expression<?>> inputsAfterSubFunc;
			private final String subFunctionCallErrorMessagePrefix;

			protected AnyOfAnyFunctionCall(FirstOrderFunction<BooleanValue> subFunc, List<Expression<?>> inputsAfterSubFunc)
			{
				/*
				 * According to spec of an-of-any function, the remaining arguments (inputsAfterSubFunc here) are either primitive data types or bags of
				 * primitive types. The expression SHALL be evaluated as if the function named in the <Function> argument (subFunc here) was applied between
				 * every tuple of the cross product on all bags and the primitive values.
				 */
				this.subFuncArity = inputsAfterSubFunc.size();
				final Datatype<?>[] subFuncArgTypes = new Datatype<?>[subFuncArity];
				int i = 0;
				for (final Expression<?> input : inputsAfterSubFunc)
				{
					final Datatype<?> inputDatatype = input.getReturnType();
					/*
					 * Always primitive datatype used in the sub-function call (typeParameter of the datatype for a bag datatype, else the datatype itself
					 * (already primitive))
					 */
					subFuncArgTypes[i] = inputDatatype.getTypeParameter() == null ? inputDatatype : inputDatatype.getTypeParameter();
					i++;
				}

				this.subFuncCall = subFunc.newCall(Collections.<Expression<?>> emptyList(), subFuncArgTypes);
				this.inputsAfterSubFunc = inputsAfterSubFunc;
				this.subFunctionCallErrorMessagePrefix = "Function " + NAME_ANY_OF_ANY + ": Error evaluating sub-function with arguments (evaluated to): ";
			}

			private BooleanValue eval(Iterator<Expression<?>> argExpressionsAfterSubFuncIterator, ListIterator<Value> argValuesAfterSubFuncIterator,
					Deque<AttributeValue> subFuncArgsStack, EvaluationContext context) throws IndeterminateEvaluationException
			{
				final Value argVal;
				if (argExpressionsAfterSubFuncIterator.hasNext())
				{
					// we are still evaluating argument expressions for the first time
					try
					{
						argVal = argExpressionsAfterSubFuncIterator.next().evaluate(context);

					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(SUB_FUNC_EVAL_ERROR_MSG, e.getStatusCode(), e);
					}
					// save the result for reuse when building the next list of sub-function
					// arguments to avoid re-evaluation
					argValuesAfterSubFuncIterator.add(argVal);
				} else
				{
					/*
					 * No more arg expression to evaluate, but we may have evaluated them all with results put in argValuesAfterSubFuncIterator, then started a
					 * new combination of arguments from the start, working with argValuesAfterSubFuncIterator only after that. So check where we are with
					 * argValuesAfterSubFuncIterator
					 */
					if (argValuesAfterSubFuncIterator.hasNext())
					{
						argVal = argValuesAfterSubFuncIterator.next();
					} else
					{
						// no more argument to add to the list of sub-function arguments
						argVal = null;
					}
				}

				if (argVal == null)
				{
					// we finished a list of sub-function arguments, so we can call the sub-function
					// with it
					final AttributeValue[] subFuncArgValues = subFuncArgsStack.toArray(new AttributeValue[subFuncArity]);
					try
					{
						return subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFunctionCallErrorMessagePrefix + subFuncArgsStack, e.getStatusCode(), e);
					}
				}

				// argVal != null
				if (argVal instanceof Bag)
				{
					// arg value is a bag
					/*
					 * If bag empty, returns False as there will be no possibility for a predicate to be "True"; in particular if
					 * AttributeDesignator/AttributeSelector with MustBePresent=False evaluates to empty bag.
					 */
					final Bag<?> argBag = (Bag<?>) argVal;
					if (argBag.isEmpty())
					{
						return BooleanValue.FALSE;
					}
					/*
					 * For each value in the arg bag, add it to the sub-function argument stack and call eval()
					 */

					for (final AttributeValue argBagVal : argBag)
					{
						subFuncArgsStack.add(argBagVal);
						final BooleanValue subResult = eval(argExpressionsAfterSubFuncIterator, argValuesAfterSubFuncIterator, subFuncArgsStack, context);
						if (subResult.getUnderlyingValue())
						{
							return BooleanValue.TRUE;
						}

						/*
						 * Remove the arg we just added at the start of the iteration, to leave the place for the new arg in the next iteration
						 */
						subFuncArgsStack.removeLast();
					}

				} else
				{
					// arg value is primitive
					// add it to the sub-function call's argument stack
					subFuncArgsStack.add((AttributeValue) argVal);
					// evaluate with the new arg stack
					final BooleanValue subResult = eval(argExpressionsAfterSubFuncIterator, argValuesAfterSubFuncIterator, subFuncArgsStack, context);
					if (subResult.getUnderlyingValue())
					{
						return BooleanValue.TRUE;
					}

					/*
					 * Remove the arg we just added at the start of the iteration, to leave the place for the new arg in the next iteration
					 */
					subFuncArgsStack.removeLast();
				}

				/*
				 * argVal != null and either argValuesAfterSubFuncIterator.next() or argValuesAfterSubFuncIterator.add(...) was called so we need to go
				 * backwards now to prepare next eval().
				 */
				argValuesAfterSubFuncIterator.previous();
				return BooleanValue.FALSE;

			}

			@Override
			public BooleanValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
			{
				/*
				 * For each input expression coming from inputsAfterSubFunc, the evaluation result will be added to the following list, to avoid evaluating the
				 * same expression again as each one will be reused in multiple combination of arguments:
				 */
				final List<Value> inputsAfterSubFuncEvalResults = new ArrayList<>();

				/*
				 * We build the stack (Deque) of sub-function argument values (extracted progressively from inputsAfterSubFuncEvalResults). Deque provides LIFO
				 * stack which is convenient for managing the sub-function arguments because we will be pushing/popping for each value in each bag argument, to
				 * make the list of the sub-function's arguments before we can make each sub-function call.
				 */
				final Deque<AttributeValue> subFuncArgsStack = new ArrayDeque<>(subFuncArity);

				// the subsequent logic is put in separated method because we need to call it
				// recursively over nonFirstArgExpsIterator
				return eval(inputsAfterSubFunc.iterator(), inputsAfterSubFuncEvalResults.listIterator(), subFuncArgsStack, context);
			}

			@Override
			public Datatype<BooleanValue> getReturnType()
			{
				return DatatypeConstants.BOOLEAN.TYPE;
			}

		}

	}

	/**
	 * all-of-any function
	 * 
	 */
	private static final class AllOfAny extends BooleanHigherOrderTwoBagFunction
	{

		private final String subFunctionCallErrorMessagePrefix;

		/**
		 * Default constructor
		 */
		private AllOfAny()
		{
			super(NAME_ALL_OF_ANY);
			this.subFunctionCallErrorMessagePrefix = "Function " + NAME_ALL_OF_ANY + ": Error evaluating sub-function with arguments (evaluated to): ";
		}

		@Override
		protected BooleanHigherOrderTwoBagFunction.Call newFunctionCall(FirstOrderFunction<BooleanValue> subFunc, Expression<? extends Bag<?>> arg0,
				Expression<? extends Bag<?>> arg1)
		{
			return new BooleanHigherOrderTwoBagFunction.Call(NAME_ALL_OF_ANY, subFunc, arg0, arg1)
			{

				@Override
				protected BooleanValue evaluate(Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException
				{
					final Deque<AttributeValue> subFuncArgStack = new ArrayDeque<>(2);
					for (final AttributeValue bag0Val : bag0)
					{
						subFuncArgStack.add(bag0Val);
						boolean isAnyTrue = false;
						for (final AttributeValue bag1Val : bag1)
						{
							subFuncArgStack.add(bag1Val);
							final AttributeValue[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
							final BooleanValue subResult;
							try
							{
								subResult = subFuncCall.evaluate(context, subFuncArgValues);
							} catch (IndeterminateEvaluationException e)
							{
								throw new IndeterminateEvaluationException(subFunctionCallErrorMessagePrefix + subFuncArgStack,
										StatusHelper.STATUS_PROCESSING_ERROR);
							}

							subFuncArgStack.removeLast();
							if (subResult.getUnderlyingValue())
							{
								isAnyTrue = true;
								break;
							}
						}

						if (!isAnyTrue)
						{
							return BooleanValue.FALSE;
						}

						subFuncArgStack.removeLast();
					}

					return BooleanValue.TRUE;
				}

			};
		}
	}

	/**
	 * any-of-all function
	 * 
	 */
	private static final class AnyOfAll extends BooleanHigherOrderTwoBagFunction
	{

		private final String subFunctionCallErrorMessagePrefix;

		/**
		 * Default constructor
		 */
		private AnyOfAll()
		{
			super(NAME_ANY_OF_ALL);
			this.subFunctionCallErrorMessagePrefix = "Function " + NAME_ANY_OF_ALL + ": Error evaluating sub-function with arguments (evaluated to): ";
		}

		@Override
		protected BooleanHigherOrderTwoBagFunction.Call newFunctionCall(FirstOrderFunction<BooleanValue> subFunc, Expression<? extends Bag<?>> arg0,
				Expression<? extends Bag<?>> arg1)
		{
			return new BooleanHigherOrderTwoBagFunction.Call(NAME_ANY_OF_ALL, subFunc, arg0, arg1)
			{

				@Override
				protected BooleanValue evaluate(Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException
				{
					final Deque<AttributeValue> subFuncArgStack = new ArrayDeque<>(2);

					// same as all-of-any but in reverse order of bag0 and bag1
					for (final AttributeValue bag1Val : bag1)
					{
						boolean isAnyTrue = false;
						for (final AttributeValue bag0Val : bag0)
						{
							subFuncArgStack.add(bag0Val);
							subFuncArgStack.add(bag1Val);
							final AttributeValue[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
							final BooleanValue subResult;
							try
							{
								subResult = subFuncCall.evaluate(context, subFuncArgValues);
							} catch (IndeterminateEvaluationException e)
							{
								throw new IndeterminateEvaluationException(subFunctionCallErrorMessagePrefix + subFuncArgStack,
										StatusHelper.STATUS_PROCESSING_ERROR);
							}

							subFuncArgStack.removeLast();// remove bag1val
							subFuncArgStack.removeLast();// remove bag0val
							if (subResult.getUnderlyingValue())
							{
								isAnyTrue = true;
								break;
							}
						}

						if (!isAnyTrue)
						{
							return BooleanValue.FALSE;
						}
					}

					return BooleanValue.TRUE;
				}
			};
		}
	}

	/**
	 * any-of-all function
	 * 
	 */
	private static final class AllOfAll extends BooleanHigherOrderTwoBagFunction
	{

		private final String subFunctionCallErrorMessagePrefix;

		/**
		 * Default constructor
		 */
		private AllOfAll()
		{
			super(NAME_ALL_OF_ALL);
			this.subFunctionCallErrorMessagePrefix = "Function " + NAME_ALL_OF_ALL + ": Error evaluating sub-function with arguments (evaluated to): ";

		}

		@Override
		protected BooleanHigherOrderTwoBagFunction.Call newFunctionCall(FirstOrderFunction<BooleanValue> subFunc, Expression<? extends Bag<?>> arg0,
				Expression<? extends Bag<?>> arg1)
		{
			return new BooleanHigherOrderTwoBagFunction.Call(NAME_ALL_OF_ALL, subFunc, arg0, arg1)
			{

				@Override
				protected BooleanValue evaluate(Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException
				{
					final Deque<AttributeValue> subFuncArgStack = new ArrayDeque<>(2);
					for (final AttributeValue bag0Val : bag0)
					{
						subFuncArgStack.add(bag0Val);
						boolean areAllTrue = true;
						for (final AttributeValue bag1Val : bag1)
						{
							subFuncArgStack.add(bag1Val);
							final AttributeValue[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
							final BooleanValue subResult;
							try
							{
								subResult = subFuncCall.evaluate(context, subFuncArgValues);
							} catch (IndeterminateEvaluationException e)
							{
								throw new IndeterminateEvaluationException(subFunctionCallErrorMessagePrefix + subFuncArgStack,
										StatusHelper.STATUS_PROCESSING_ERROR);
							}

							subFuncArgStack.removeLast();
							if (!subResult.getUnderlyingValue())
							{
								areAllTrue = false;
								break;
							}
						}

						if (!areAllTrue)
						{
							return BooleanValue.FALSE;
						}

						subFuncArgStack.removeLast();
					}

					return BooleanValue.TRUE;
				}
			};
		}
	}

	private HigherOrderBagFunctionSet()
	{

	}

	/**
	 * Function cluster
	 */
	public static final FunctionSet INSTANCE = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "higher-order-bag",
	//
			new BooleanOneBagOnlyFunction(NAME_ANY_OF, new AnyOfCallFactory()),//
			new BooleanOneBagOnlyFunction(NAME_ALL_OF, new AllOfCallFactory()),//
			new AnyOfAny(), new AllOfAny(), new AnyOfAll(), new AllOfAll());

}
