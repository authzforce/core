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
package com.thalesgroup.authzforce.core.func;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.eval.Bag;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.VariableReference;

/**
 * Implements all of the higher-order bag functions.
 * 
 * @param <RETURN_T>
 *            return type
 * @param <SUB_RETURN_PRIMITIVE_T>
 *            sub-function's return (primitive) type. Only functions returning primitive type of
 *            result are compatible with higher-order functions here.
 */
public abstract class HigherOrderBagFunction<RETURN_T extends Expression.Value<RETURN_T>, SUB_RETURN_PRIMITIVE_T extends AttributeValue<SUB_RETURN_PRIMITIVE_T>> extends Function<RETURN_T>
{

	/**
	 * Standard identifier for the any-of function. WARNING: XACML 1.0 any-of planned for
	 * deprecation as of XACML 3.0. Only 3.0 version supported henceforth.
	 */
	public static final String NAME_ANY_OF = FUNCTION_NS_3 + "any-of";

	/**
	 * Standard identifier for the all-of function.
	 */
	public static final String NAME_ALL_OF = FUNCTION_NS_3 + "all-of";

	/**
	 * Standard identifier for the any-of-any function.
	 */
	public static final String NAME_ANY_OF_ANY = FUNCTION_NS_3 + "any-of-any";

	/**
	 * Standard identifier for the all-of-any function.
	 */
	public static final String NAME_ALL_OF_ANY = FUNCTION_NS_1 + "all-of-any";

	/**
	 * Standard identifier for the any-of-all function.
	 */
	public static final String NAME_ANY_OF_ALL = FUNCTION_NS_1 + "any-of-all";

	/**
	 * Standard identifier for the all-of-all function.
	 */
	public static final String NAME_ALL_OF_ALL = FUNCTION_NS_1 + "all-of-all";

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "higher-order-bag",
	//
			new AnyOf(), new AllOf(), new AnyOfAny(), new AllOfAny(), new AnyOfAll(), new AllOfAll());

	protected final Datatype<RETURN_T> returnType;

	protected final Datatype<?> subFuncReturnType;

	/**
	 * Instantiates higher-order bag function
	 * 
	 * @param functionId
	 *            function ID
	 * @param returnType
	 *            function's return type
	 * @param subFunctionReturnType
	 *            sub-function's return datatype; may be null to indicate any datatype (e.g. map
	 *            function's sub-function return datatype can be any primitive type)
	 */
	protected HigherOrderBagFunction(String functionId, Datatype<RETURN_T> returnType, Datatype<?> subFunctionReturnType)
	{
		super(functionId);
		this.returnType = returnType;
		this.subFuncReturnType = subFunctionReturnType;
	}

	/**
	 * Returns the type of attribute value that will be returned by this function.
	 * 
	 * @return the return type
	 */
	@Override
	public Datatype<RETURN_T> getReturnType()
	{
		return returnType;
	}

	/**
	 * Creates function call from sub-function definition and all inputs to higher-order function To
	 * be overriden by OneBagOnlyFunctions (any-of/all-of)
	 * 
	 * @param boolSubFunc
	 *            boolean sub-function
	 * @param subFuncArgTypes
	 *            sub-function argument types
	 * @param inputs
	 *            all inputs
	 * @return function call
	 */
	protected abstract FunctionCall<RETURN_T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc, List<Expression<?>> inputsAfterSubFunc);

	@Override
	public final FunctionCall<RETURN_T> newCall(List<Expression<?>> inputs) throws IllegalArgumentException
	{
		final int numInputs = inputs.size();
		checkNumberOfArgs(numInputs);

		final Iterator<? extends Expression<?>> inputsIterator = inputs.iterator();
		final Expression<?> input0 = inputsIterator.next();
		// first arg must be a boolean function
		final Function<?> inputFunc;
		if (input0 instanceof Function)
		{
			inputFunc = (Function<?>) input0;
		} else if (input0 instanceof VariableReference)
		{
			final Expression<?> varRefExp = ((VariableReference<?>) input0).getReferencedExpression();
			if (!(varRefExp instanceof Function))
			{
				throw new IllegalArgumentException(this + ": Invalid type of first argument: " + varRefExp.getClass().getSimpleName() + ". Required: Function");
			}

			inputFunc = (Function<?>) varRefExp;
		} else
		{
			throw new IllegalArgumentException(this + ": Invalid type of first argument: " + input0.getClass().getSimpleName() + ". Required: Function");
		}

		/*
		 * Check whether it is a FirstOrderFunction because it is the only type of function for
		 * which we have a generic way to validate argument types as done later below
		 */
		if (!(inputFunc instanceof FirstOrderFunction))
		{
			throw new IllegalArgumentException(this + ": Invalid function in first argument: " + inputFunc + " is not supported as such argument");
		}

		final Datatype<?> inputFuncReturnType = inputFunc.getReturnType();
		if (subFuncReturnType == null)
		{
			/*
			 * sub-function's return type can be any primitive datatype; check at least it is
			 * primitive
			 */
			if (inputFuncReturnType.isBag())
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFuncReturnType + " (bag type). Required: any primitive type");
			}
		} else
		{
			if (!inputFuncReturnType.equals(subFuncReturnType))
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFuncReturnType + ". Required: " + subFuncReturnType);
			}
		}

		// so now we know we have a boolean FirstOrderFunction
		@SuppressWarnings("unchecked")
		final FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc = (FirstOrderFunction<SUB_RETURN_PRIMITIVE_T>) inputFunc;

		return createFunctionCallFromSubFunction(subFunc, inputs.subList(1, numInputs));
	}

	protected abstract void checkNumberOfArgs(int numInputs);

	/**
	 * on-bag-only functions (only last arg is bag): any-of, all-of
	 * 
	 */
	static abstract class OneBagOnlyFunction<RETURN_T extends Value<RETURN_T>, SUB_RETURN_T extends AttributeValue<SUB_RETURN_T>> extends HigherOrderBagFunction<RETURN_T, SUB_RETURN_T>
	{
		/**
		 * Combines the results of evaluations of a sub-function with the following combination of
		 * arguments: (arg_0,..., arg_n-1, bag[i]), for all values bag[i] of a bag
		 * (i=0..bag.size()-1), and (arg_0,... arg_n-1) a constant list
		 * 
		 * @param subFunc
		 *            the sub-function to be called with the various combination of arguments
		 * @param subFuncArgsBeforeLast
		 *            the (arg_0,... arg_n-1) constant list
		 * @param lastArgBagExpr
		 *            the bag of which each value is used as the last argument in a combination with
		 *            <code>subFuncArgsBeforeLast</code>
		 * @param context
		 *            evaluation context
		 * @return final result
		 * @throws IndeterminateEvaluationException
		 */
		protected abstract RETURN_T evaluate(FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall, Bag<?> lastArgBag, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException;

		private final String errorEvalLastArgMsg = "Function " + this + ": Error evaluating last arg (bag)";

		public final RETURN_T evaluate(FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall, Expression<?> lastArgBagExpr, Bag.Datatype<?> lastArgBagDatatype, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
		{
			final Bag<?> lastArgBag;
			try
			{
				lastArgBag = Utils.evalBagArg(lastArgBagExpr, context, lastArgBagDatatype);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(errorEvalLastArgMsg, e.getStatusCode(), e);
			}

			return evaluate(subFuncCall, lastArgBag, lastArgIndex, context);
		}

		private class OneBagOnlyFunctionCall implements FunctionCall<RETURN_T>
		{

			private final FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall;
			private final Expression<?> lastArgBagExpr;
			private final int lastArgIndex;
			private final Bag.Datatype<?> lastArgBagDatatype;

			protected OneBagOnlyFunctionCall(FirstOrderFunction<SUB_RETURN_T> subFunction, List<Expression<?>> primitiveInputs, Expression<?> lastInputBag)
			{
				final Datatype<?> lastArgExpDatatype = lastInputBag.getReturnType();
				if (!lastArgExpDatatype.isBag())
				{
					throw new IllegalArgumentException("Function " + this + ": last argument expression's return type (" + lastArgExpDatatype + ") is not a Bag datatype as expected");
				}

				lastArgBagExpr = lastInputBag;
				// Bag.Datatype is the only Datatype implementation for Datatype<Bag<?>>
				lastArgBagDatatype = (Bag.Datatype<?>) lastArgExpDatatype;

				/*
				 * The actual expression passed as last argument to the sub-function is not yet
				 * known; but we know the expected datatype is the type of each element
				 * lastInputBag's evaluation result bag, therefore the element datatype, i.e. type
				 * parameter to the returned bag datatype
				 */

				this.subFuncCall = subFunction.newCall(primitiveInputs, lastInputBag.getReturnType().getTypeParameter());
				/*
				 * Total number of args to the higher-order function (including sub-function and
				 * last bag arg) = primitiveInputs.size() + 2 Last arg (bag) index = total number of
				 * args - 1
				 */
				this.lastArgIndex = primitiveInputs.size() + 1;
			}

			@Override
			public final RETURN_T evaluate(EvaluationContext context) throws IndeterminateEvaluationException
			{
				return OneBagOnlyFunction.this.evaluate(subFuncCall, lastArgBagExpr, lastArgBagDatatype, lastArgIndex, context);
			}

			@Override
			public final Datatype<RETURN_T> getReturnType()
			{
				return returnType;
			}
		}

		protected OneBagOnlyFunction(String functionName, Datatype<RETURN_T> returnType, Datatype<SUB_RETURN_T> subFunctionReturnType)
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
				throw new IllegalArgumentException("Function " + this.functionId + ": Invalid number of arguments (" + numInputs + "). Required: >= 2");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.HigherOrderBagFunction#
		 * createFunctionCallFromSubFunction
		 * (com.thalesgroup.authzforce.core.func.FirstOrderFunction, java.util.List, java.util.List)
		 */
		@Override
		protected final FunctionCall<RETURN_T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_T> subFunc, List<Expression<?>> inputsAfterSubFunc)
		{
			final Iterator<Expression<?>> inputsAfterSubfuncIterator = inputsAfterSubFunc.iterator();
			// inputs that we can parse/validate for the sub-function are the primitive inputs, i.e.
			// all except last one which is a bag
			final List<Expression<?>> primitiveInputs = new ArrayList<>();
			Expression<?> lastInputBag = null;
			boolean hasNextInput = true;
			while (hasNextInput)
			{
				final Expression<?> input = inputsAfterSubfuncIterator.next();
				hasNextInput = inputsAfterSubfuncIterator.hasNext();
				final Datatype<?> inputType = input.getReturnType();
				if (hasNextInput)
				{
					// not the last input, must be primitive
					if (inputType.isBag())
					{
						throw new IllegalArgumentException(this + ": Invalid type of argument between second and second to last (inclusive): a bag. Required: primitive (not a bag)");
					}

					primitiveInputs.add(input);
				} else
				{
					// last input, must be a bag
					if (!inputType.isBag())
					{
						throw new IllegalArgumentException(this + ": Invalid last argument type: primitive (not a bag). Required: a bag");
					}

					lastInputBag = input;
				}
			}

			return new OneBagOnlyFunctionCall(subFunc, primitiveInputs, lastInputBag);
		}

	}

	private static abstract class BooleanOneBagOnlyFunction extends OneBagOnlyFunction<BooleanAttributeValue, BooleanAttributeValue>
	{
		protected BooleanOneBagOnlyFunction(String functionId)
		{
			super(functionId, DatatypeConstants.BOOLEAN.TYPE, DatatypeConstants.BOOLEAN.TYPE);
		}
	}

	/**
	 * any-of function
	 * 
	 */
	private static class AnyOf extends BooleanOneBagOnlyFunction
	{
		protected AnyOf()
		{
			super(NAME_ANY_OF);
		}

		@Override
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Bag<?> lastArgBag, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
		{
			for (final AttributeValue<?> attrVal : lastArgBag)
			{
				final BooleanAttributeValue subResult;
				try
				{
					subResult = subFuncCall.evaluate(context, attrVal);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error calling sub-function (specified as first argument) with last arg=" + attrVal, e.getStatusCode(), e);
				}

				if (subResult.getUnderlyingValue())
				{
					return BooleanAttributeValue.TRUE;
				}
			}

			return BooleanAttributeValue.FALSE;
		}

	}

	/**
	 * all-of function
	 * 
	 */
	private static class AllOf extends BooleanOneBagOnlyFunction
	{

		/**
		 * Default constructor with standard function URI
		 */
		public AllOf()
		{
			super(NAME_ALL_OF);
		}

		@Override
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Bag<?> lastArgBag, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
		{
			for (final AttributeValue<?> attrVal : lastArgBag)
			{
				final BooleanAttributeValue subResult;
				try
				{
					subResult = subFuncCall.evaluate(context, attrVal);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error calling sub-function (specified as first argument) with last arg=" + attrVal, e.getStatusCode(), e);
				}

				if (!subResult.getUnderlyingValue())
				{
					return BooleanAttributeValue.FALSE;
				}
			}

			return BooleanAttributeValue.TRUE;
		}
	}

	private static abstract class BooleanHigherOrderBagFunction extends HigherOrderBagFunction<BooleanAttributeValue, BooleanAttributeValue>
	{
		protected BooleanHigherOrderBagFunction(String functionId)
		{
			super(functionId, DatatypeConstants.BOOLEAN.TYPE, DatatypeConstants.BOOLEAN.TYPE);
		}
	}

	/**
	 * any-of-any function
	 * 
	 */
	private static class AnyOfAny extends BooleanHigherOrderBagFunction
	{

		private static final String SUB_FUNC_EVAL_ERROR_MSG = "Function " + NAME_ANY_OF_ANY + ": Error evaluating one of the arguments after sub-function (first arg)";

		/**
		 * Default constructor
		 */
		public AnyOfAny()
		{
			super(NAME_ANY_OF_ANY);
		}

		@Override
		protected final void checkNumberOfArgs(int numInputs)
		{
			if (numInputs < 2)
			{
				throw new IllegalArgumentException("Function " + this.functionId + ": Invalid number of arguments (" + numInputs + "). Required: >= 2");
			}
		}

		@Override
		protected final FunctionCall<BooleanAttributeValue> createFunctionCallFromSubFunction(FirstOrderFunction<BooleanAttributeValue> subFunc, List<Expression<?>> inputsAfterSubFunc)
		{
			return new AnyOfAnyFunctionCall(subFunc, inputsAfterSubFunc);
		}

		private class AnyOfAnyFunctionCall implements FunctionCall<BooleanAttributeValue>
		{
			private final FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall;
			private final int subFuncArity;
			private final List<Expression<?>> inputsAfterSubFunc;

			protected AnyOfAnyFunctionCall(FirstOrderFunction<BooleanAttributeValue> subFunc, List<Expression<?>> inputsAfterSubFunc)
			{
				/*
				 * According to spec of an-of-any function, the remaining arguments
				 * (inputsAfterSubFunc here) are either primitive data types or bags of primitive
				 * types. The expression SHALL be evaluated as if the function named in the
				 * <Function> argument (subFunc here) was applied between every tuple of the cross
				 * product on all bags and the primitive values.
				 */
				this.subFuncArity = inputsAfterSubFunc.size();
				final Datatype<?>[] subFuncArgTypes = new Datatype<?>[subFuncArity];
				int i = 0;
				for (final Expression<?> input : inputsAfterSubFunc)
				{
					final Datatype<?> inputDatatype = input.getReturnType();
					/*
					 * Always primitive datatype used in the sub-function call (typeParameter of the
					 * datatype for a bag datatype, else the datatype itself (already primitive))
					 */
					subFuncArgTypes[i] = inputDatatype.isBag() ? inputDatatype.getTypeParameter() : inputDatatype;
					i++;
				}

				this.subFuncCall = subFunc.newCall(Collections.EMPTY_LIST, subFuncArgTypes);
				this.inputsAfterSubFunc = inputsAfterSubFunc;
			}

			private final BooleanAttributeValue eval(Iterator<Expression<?>> argExpressionsAfterSubFuncIterator, ListIterator<Value<?>> argValuesAfterSubFuncIterator, Deque<AttributeValue<?>> subFuncArgsStack, EvaluationContext context) throws IndeterminateEvaluationException
			{
				final Value<?> argVal;
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
					 * No more arg expression to evaluate, but we may have evaluated them all with
					 * results put in argValuesAfterSubFuncIterator, then started a new combination
					 * of arguments from the start, working with argValuesAfterSubFuncIterator only
					 * after that. So check where we are with argValuesAfterSubFuncIterator
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
					final AttributeValue<?>[] subFuncArgValues = subFuncArgsStack.toArray(new AttributeValue[subFuncArity]);
					try
					{
						return subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFuncCallErrorMsgPrefix + subFuncArgsStack, e.getStatusCode(), e);
					}
				}

				// argVal != null
				if (argVal.getReturnType().isBag())
				{
					// arg value is a bag
					/*
					 * If bag empty, returns False as there will be no possibility for a predicate
					 * to be "True"; in particular if AttributeDesignator/AttributeSelector with
					 * MustBePresent=False evaluates to empty bag.
					 */
					final Bag<?> argBag = (Bag<?>) argVal;
					if (argBag.isEmpty())
					{
						return BooleanAttributeValue.FALSE;
					}
					/*
					 * For each value in the arg bag, add it to the sub-function argument stack and
					 * call eval()
					 */

					for (final AttributeValue<?> argBagVal : argBag)
					{
						subFuncArgsStack.add(argBagVal);
						final BooleanAttributeValue subResult = eval(argExpressionsAfterSubFuncIterator, argValuesAfterSubFuncIterator, subFuncArgsStack, context);
						if (subResult.getUnderlyingValue())
						{
							return BooleanAttributeValue.TRUE;
						}

						/*
						 * Remove the arg we just added at the start of the iteration, to leave the
						 * place for the new arg in the next iteration
						 */
						subFuncArgsStack.removeLast();
					}

				} else
				{
					// arg value is primitive
					// add it to the sub-function call's argument stack
					subFuncArgsStack.add((AttributeValue<?>) argVal);
					// evaluate with the new arg stack
					final BooleanAttributeValue subResult = eval(argExpressionsAfterSubFuncIterator, argValuesAfterSubFuncIterator, subFuncArgsStack, context);
					if (subResult.getUnderlyingValue())
					{
						return BooleanAttributeValue.TRUE;
					}

					/*
					 * Remove the arg we just added at the start of the iteration, to leave the
					 * place for the new arg in the next iteration
					 */
					subFuncArgsStack.removeLast();
				}

				/*
				 * argVal != null and either argValuesAfterSubFuncIterator.next() or
				 * argValuesAfterSubFuncIterator.add(...) was called so we need to go backwards now
				 * to prepare next eval().
				 */
				argValuesAfterSubFuncIterator.previous();
				return BooleanAttributeValue.FALSE;

			}

			@Override
			public final BooleanAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
			{
				/*
				 * For each input expression coming from inputsAfterSubFunc, the evaluation result
				 * will be added to the following list, to avoid evaluating the same expression
				 * again as each one will be reused in multiple combination of arguments:
				 */
				final List<Value<?>> inputsAfterSubFuncEvalResults = new ArrayList<>();

				/*
				 * We build the stack (Deque) of sub-function argument values (extracted
				 * progressively from inputsAfterSubFuncEvalResults). Deque provides LIFO stack
				 * which is convenient for managing the sub-function arguments because we will be
				 * pushing/popping for each value in each bag argument, to make the list of the
				 * sub-function's arguments before we can make each sub-function call.
				 */
				final Deque<AttributeValue<?>> subFuncArgsStack = new ArrayDeque<>(subFuncArity);

				// the subsequent logic is put in separated method because we need to call it
				// recursively over nonFirstArgExpsIterator
				return eval(inputsAfterSubFunc.iterator(), inputsAfterSubFuncEvalResults.listIterator(), subFuncArgsStack, context);
			}

			@Override
			public final Datatype<BooleanAttributeValue> getReturnType()
			{
				return DatatypeConstants.BOOLEAN.TYPE;
			}

		}

		private final String subFuncCallErrorMsgPrefix = "Function " + functionId + ": Error calling sub-function (specified as first argument) with arguments: ";

	}

	/**
	 * Higher-order boolean function taking three arguments: sub-function and two bags
	 * 
	 */
	private static abstract class TwoBagFunction extends BooleanHigherOrderBagFunction
	{

		private final String errorEvalArg1Message = "Function " + this + ": Error evaluating second arg #1";
		private final String errorEvalArg2Message = "Function " + this + ": Error evaluating arg #2";
		protected final String subFunctionCallErrMsgPrefix = "Function " + functionId + ": Error evaluating sub-function with arguments (evaluated to): ";

		protected TwoBagFunction(String functionName)
		{
			super(functionName);
		}

		@Override
		protected final void checkNumberOfArgs(int numInputs)
		{
			if (numInputs != 3)
			{
				throw new IllegalArgumentException("Function " + this.functionId + ": Invalid number of arguments (" + numInputs + "). Required: 3");
			}
		}

		protected abstract BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Bag<?> bag0Vals, Bag<?> bag1Vals, EvaluationContext context) throws IndeterminateEvaluationException;

		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Expression<Bag<?>> inputBag0, Expression<Bag<?>> inputBag1, EvaluationContext context) throws IndeterminateEvaluationException
		{
			final Bag<?> bag0Vals;
			try
			{
				bag0Vals = inputBag0.evaluate(context);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(errorEvalArg1Message, Status.STATUS_PROCESSING_ERROR);
			}

			/*
			 * If result bag empty, returns False as there will be no possibility for a Predicate
			 * that is "True". AttributeDesignator/AttributeSelector with MustBePresent=False may
			 * evaluate to empty bags (Indeterminate Exception if MustBePresent=True). empty bag.
			 */
			if (bag0Vals.isEmpty())
			{
				return BooleanAttributeValue.FALSE;
			}

			final Bag<?> bag1Vals;
			try
			{
				bag1Vals = inputBag1.evaluate(context);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(errorEvalArg2Message, Status.STATUS_PROCESSING_ERROR);
			}

			if (bag1Vals.isEmpty())
			{
				return BooleanAttributeValue.FALSE;
			}

			return evaluate(subFuncCall, bag0Vals, bag1Vals, context);
		}

		private class TwoBagFunctionCall implements FunctionCall<BooleanAttributeValue>
		{
			private final FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall;
			private final Expression<Bag<?>> input0;
			private final Expression<Bag<?>> input1;

			protected TwoBagFunctionCall(FirstOrderFunction<BooleanAttributeValue> subFunc, Expression<Bag<?>> input0, Expression<Bag<?>> input1)
			{
				final Datatype<?>[] subFuncArgTypes = { input0.getReturnType().getTypeParameter(), input1.getReturnType().getTypeParameter() };
				this.subFuncCall = subFunc.newCall(Collections.EMPTY_LIST, subFuncArgTypes);
				this.input0 = input0;
				this.input1 = input1;
			}

			@Override
			public final BooleanAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
			{
				return TwoBagFunction.this.evaluate(subFuncCall, input0, input1, context);
			}

			@Override
			public final Datatype<BooleanAttributeValue> getReturnType()
			{
				return DatatypeConstants.BOOLEAN.TYPE;
			}

		}

		@Override
		protected final FunctionCall<BooleanAttributeValue> createFunctionCallFromSubFunction(FirstOrderFunction<BooleanAttributeValue> subFunc, List<Expression<?>> inputsAfterSubFunc)
		{

			final Iterator<Expression<?>> inputsAfterSubfuncIterator = inputsAfterSubFunc.iterator();

			while (inputsAfterSubfuncIterator.hasNext())
			{
				// all must be bag
				if (!inputsAfterSubfuncIterator.next().getReturnType().isBag())
				{
					throw new IllegalArgumentException(this + ": Invalid last argument type: primitive (not a bag). Required: a bag");
				}
			}

			final Expression<Bag<?>> input0 = (Expression<Bag<?>>) inputsAfterSubFunc.get(0);
			final Expression<Bag<?>> input1 = (Expression<Bag<?>>) inputsAfterSubFunc.get(1);
			return new TwoBagFunctionCall(subFunc, input0, input1);
		}
	}

	/**
	 * all-of-any function
	 * 
	 */
	private static class AllOfAny extends TwoBagFunction
	{

		/**
		 * Default constructor
		 */
		public AllOfAny()
		{
			super(NAME_ALL_OF_ANY);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.HigherOrderBagFunction#evaluate(com.thalesgroup
		 * .authzforce.core.func.BaseFunction, java.util.List,
		 * com.thalesgroup.authzforce.core.test.EvaluationCtx)
		 */
		@Override
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException

		{
			final Deque<Expression<?>> subFuncArgStack = new ArrayDeque<>(2);
			for (final AttributeValue<?> bag0Val : bag0)
			{
				subFuncArgStack.add(bag0Val);
				boolean isAnyTrue = false;
				for (final AttributeValue<?> bag1Val : bag1)
				{
					subFuncArgStack.add(bag1Val);
					final AttributeValue<?>[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
					final BooleanAttributeValue subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFunctionCallErrMsgPrefix + subFuncArgStack, Status.STATUS_PROCESSING_ERROR);
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
					return BooleanAttributeValue.FALSE;
				}

				subFuncArgStack.removeLast();
			}

			return BooleanAttributeValue.TRUE;
		}
	}

	/**
	 * any-of-all function
	 * 
	 */
	private static class AnyOfAll extends TwoBagFunction
	{

		/**
		 * Default constructor
		 */
		public AnyOfAll()
		{
			super(NAME_ANY_OF_ALL);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.HigherOrderBagFunction#evaluate(com.thalesgroup
		 * .authzforce.core.func.BaseFunction, java.util.List,
		 * com.thalesgroup.authzforce.core.test.EvaluationCtx)
		 */
		@Override
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException

		{
			final Deque<Expression<?>> subFuncArgStack = new ArrayDeque<>(2);

			// same as all-of-any but in reverse order of bag0 and bag1
			for (final AttributeValue<?> bag1Val : bag1)
			{
				boolean isAnyTrue = false;
				for (final AttributeValue<?> bag0Val : bag0)
				{
					subFuncArgStack.add(bag0Val);
					subFuncArgStack.add(bag1Val);
					final AttributeValue<?>[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
					final BooleanAttributeValue subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFunctionCallErrMsgPrefix + subFuncArgStack, Status.STATUS_PROCESSING_ERROR);
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
					return BooleanAttributeValue.FALSE;
				}
			}

			return BooleanAttributeValue.TRUE;
		}
	}

	/**
	 * any-of-all function
	 * 
	 */
	private static class AllOfAll extends TwoBagFunction
	{

		/**
		 * Default constructor
		 */
		public AllOfAll()
		{
			super(NAME_ALL_OF_ALL);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.HigherOrderBagFunction#evaluate(com.thalesgroup
		 * .authzforce.core.func.BaseFunction, java.util.List,
		 * com.thalesgroup.authzforce.core.test.EvaluationCtx)
		 */
		@Override
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Bag<?> bag0, Bag<?> bag1, EvaluationContext context) throws IndeterminateEvaluationException

		{
			final Deque<Expression<?>> subFuncArgStack = new ArrayDeque<>(2);
			for (final AttributeValue<?> bag0Val : bag0)
			{
				subFuncArgStack.add(bag0Val);
				boolean areAllTrue = true;
				for (final AttributeValue<?> bag1Val : bag1)
				{
					subFuncArgStack.add(bag1Val);
					final AttributeValue<?>[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
					final BooleanAttributeValue subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFunctionCallErrMsgPrefix + subFuncArgStack, Status.STATUS_PROCESSING_ERROR);
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
					return BooleanAttributeValue.FALSE;
				}

				subFuncArgStack.removeLast();
			}

			return BooleanAttributeValue.TRUE;
		}
	}

}
