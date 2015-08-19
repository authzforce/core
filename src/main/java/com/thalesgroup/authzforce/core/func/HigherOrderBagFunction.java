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
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.VariableReference;

/**
 * Implements all of the higher-order bag functions.
 * 
 * @param <T>
 *            return type
 * @param <SUB_RETURN_PRIMITIVE_T>
 *            sub-function's return (primitive) type. Only functions returning primitive type of
 *            result are compatible with higher-order functions here.
 */
public abstract class HigherOrderBagFunction<T extends ExpressionResult<? extends AttributeValue>, SUB_RETURN_PRIMITIVE_T extends AttributeValue> extends Function<T>
{

	/**
	 * Standard TYPE_URI for the any-of function.
	 */
	public static final String NAME_ANY_OF = FUNCTION_NS_1 + "any-of";

	/**
	 * Standard TYPE_URI for the all-of function.
	 */
	public static final String NAME_ALL_OF = FUNCTION_NS_3 + "all-of";

	/**
	 * Standard TYPE_URI for the any-of-any function.
	 */
	public static final String NAME_ANY_OF_ANY = FUNCTION_NS_3 + "any-of-any";

	/**
	 * Standard TYPE_URI for the all-of-any function.
	 */
	public static final String NAME_ALL_OF_ANY = FUNCTION_NS_1 + "all-of-any";

	/**
	 * Standard TYPE_URI for the any-of-all function.
	 */
	public static final String NAME_ANY_OF_ALL = FUNCTION_NS_1 + "any-of-all";

	/**
	 * Standard TYPE_URI for the all-of-all function.
	 */
	public static final String NAME_ALL_OF_ALL = FUNCTION_NS_1 + "all-of-all";

	/**
	 * Standard TYPE_URI for the map function.
	 */
	public static final String NAME_MAP = FUNCTION_NS_3 + "map";

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "higher-order-bag",
	//
			new AnyOf(), new AllOf(), new AnyOfAny(), new AllOfAny(), new AnyOfAll(), new AllOfAll()/*
																									 * ,
																									 * new
																									 * Map
																									 * (
																									 * )
																									 */);

	private final DatatypeDef returnType;

	protected final DatatypeDef subFuncReturnType;

	/**
	 * Instantiates higher-order bag function
	 * 
	 * @param functionId
	 *            function ID
	 * @param returnType
	 *            function's return type
	 * @param subFunctionReturnTypeURI
	 *            sub-function's return datatype URI; may be null to indicate any datatype (e.g. map
	 *            function's sub-function return datatype can be any primitive type)
	 */
	protected HigherOrderBagFunction(String functionId, DatatypeDef returnType, String subFunctionReturnTypeURI)
	{
		super(functionId);
		this.returnType = returnType;
		this.subFuncReturnType = new DatatypeDef(subFunctionReturnTypeURI);
	}

	/**
	 * Returns the type of attribute value that will be returned by this function.
	 * 
	 * @return the return type
	 */
	@Override
	public DatatypeDef getReturnType()
	{
		return returnType;
	}

	@Override
	public final FunctionCall<T> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputs) throws IllegalArgumentException
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
			final Expression<?> varRefExp = ((VariableReference) input0).getReferencedExpression();
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

		if (subFuncReturnType == null)
		{
			/*
			 * sub-function's return type can be any primitive datatype; check at least it is
			 * primitive
			 */
			if (inputFunc.getReturnType().isBag())
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFunc.getReturnType() + " (bag type). Required: any primitive type");
			}
		} else
		{
			if (!inputFunc.getReturnType().equals(subFuncReturnType))
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFunc.getReturnType() + ". Required: " + BooleanAttributeValue.TYPE);
			}
		}

		// so now we know we have a boolean FirstOrderFunction
		@SuppressWarnings("unchecked")
		final FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc = (FirstOrderFunction<SUB_RETURN_PRIMITIVE_T>) inputFunc;

		return createFunctionCallFromSubFunction(subFunc, inputs.subList(1, numInputs));
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
	protected abstract FunctionCall<T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFunc);

	protected abstract void checkNumberOfArgs(int numInputs);

	/**
	 * on-bag-only functions (only last arg is bag): any-of, all-of
	 * 
	 */
	static abstract class OneBagOnlyFunction<RETURN_T extends ExpressionResult<? extends AttributeValue>, SUB_RETURN_T extends AttributeValue> extends HigherOrderBagFunction<RETURN_T, SUB_RETURN_T>
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
		 * @param lastArgBag
		 *            the bag of which each value is used as the last argument in a combination with
		 *            <code>subFuncArgsBeforeLast</code>
		 * @param context
		 *            evaluation context
		 * @return final result
		 * @throws IndeterminateEvaluationException
		 */
		protected abstract RETURN_T evaluate(FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall, AttributeValue[] lastArgBag, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException;

		private final String errorEvalLastArgMsg = "Function " + this + ": Error evaluating last arg (bag)";

		public final RETURN_T evaluate(FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall, Expression<? extends ExpressionResult<? extends AttributeValue>> lastArgBagExp, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
		{
			final AttributeValue[] lastArgBagVals;
			try
			{
				lastArgBagVals = lastArgBagExp.evaluate(context).values();
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(errorEvalLastArgMsg, e.getStatusCode(), e);
			}

			return evaluate(subFuncCall, lastArgBagVals, lastArgIndex, context);
		}

		private class OneBagOnlyFunctionCall implements FunctionCall<RETURN_T>
		{

			private final FirstOrderFunctionCall<SUB_RETURN_T> subFuncCall;
			private final Expression<? extends ExpressionResult<? extends AttributeValue>> lastArgBag;
			private final int lastArgIndex;

			protected OneBagOnlyFunctionCall(FirstOrderFunction<SUB_RETURN_T> subFunction, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> primitiveInputs, Expression<? extends ExpressionResult<? extends AttributeValue>> lastInputBag)
			{
				this.subFuncCall = subFunction.newCall(primitiveInputs, new DatatypeDef(lastInputBag.getReturnType().datatypeURI()));
				this.lastArgBag = lastInputBag;
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
				return OneBagOnlyFunction.this.evaluate(subFuncCall, lastArgBag, lastArgIndex, context);
			}

			@Override
			public final DatatypeDef getReturnType()
			{
				return BooleanAttributeValue.TYPE;
			}

		}

		protected OneBagOnlyFunction(String functionName, DatatypeDef returnType, String subFunctionReturnTypeURI)
		{
			super(functionName, returnType, subFunctionReturnTypeURI);
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
		protected final FunctionCall<RETURN_T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_T> subFunc, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFunc)
		{
			final Iterator<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubfuncIterator = inputsAfterSubFunc.iterator();
			// inputs that we can parse/validate for the sub-function are the primitive inputs, i.e.
			// all except last one which is a bag
			final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> primitiveInputs = new ArrayList<>();
			Expression<? extends ExpressionResult<? extends AttributeValue>> lastInputBag = null;
			boolean hasNextInput = true;
			while (hasNextInput)
			{
				final Expression<? extends ExpressionResult<? extends AttributeValue>> input = inputsAfterSubfuncIterator.next();
				hasNextInput = inputsAfterSubfuncIterator.hasNext();
				final DatatypeDef inputType = input.getReturnType();
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
			super(functionId, BooleanAttributeValue.TYPE, BooleanAttributeValue.TYPE_URI);
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
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, AttributeValue[] lastArgBagVals, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
		{
			for (final AttributeValue attrVal : lastArgBagVals)
			{
				final BooleanAttributeValue subResult;
				try
				{
					subResult = subFuncCall.evaluate(context, attrVal);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error calling sub-function (specified as first argument) with last arg=" + attrVal, e.getStatusCode(), e);
				}

				if (subResult.getValue())
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
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall,
		/* Collection<? extends AttributeValue> */AttributeValue[] lastArgBagVals, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
		{
			for (final AttributeValue attrVal : lastArgBagVals)
			{
				final BooleanAttributeValue subResult;
				try
				{
					subResult = subFuncCall.evaluate(context, attrVal);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error calling sub-function (specified as first argument) with last arg=" + attrVal, e.getStatusCode(), e);
				}

				if (!subResult.getValue())
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
			super(functionId, BooleanAttributeValue.TYPE, BooleanAttributeValue.TYPE_URI);
		}
	}

	/**
	 * any-of-any function
	 * 
	 */
	private static class AnyOfAny extends BooleanHigherOrderBagFunction
	{

		private static final String errorEvalArgAfterFirst = "Function " + NAME_ANY_OF_ANY + ": Error evaluating one of the arguments after sub-function (first arg)";

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
		protected final FunctionCall<BooleanAttributeValue> createFunctionCallFromSubFunction(FirstOrderFunction<BooleanAttributeValue> subFunc, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFunc)
		{
			return new AnyOfAnyFunctionCall(subFunc, inputsAfterSubFunc);
		}

		private class AnyOfAnyFunctionCall implements FunctionCall<BooleanAttributeValue>
		{
			private final FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall;
			private final int subFuncArity;
			private final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFunc;

			protected AnyOfAnyFunctionCall(FirstOrderFunction<BooleanAttributeValue> subFunc, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFunc)
			{
				this.subFuncArity = inputsAfterSubFunc.size();
				final DatatypeDef[] subFuncArgTypes = new DatatypeDef[subFuncArity];
				int i = 0;
				for (final Expression<? extends ExpressionResult<? extends AttributeValue>> input : inputsAfterSubFunc)
				{
					subFuncArgTypes[i] = new DatatypeDef(input.getReturnType().datatypeURI());
					i++;
				}

				this.subFuncCall = subFunc.newCall(Collections.EMPTY_LIST, subFuncArgTypes);
				this.inputsAfterSubFunc = inputsAfterSubFunc;
			}

			private final BooleanAttributeValue eval(Iterator<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFuncIterator, ListIterator<AttributeValue[]> nonFirstArgEvalResultsIterator, Deque<AttributeValue> subFuncArgsStack, EvaluationContext context)
					throws IndeterminateEvaluationException
			{
				final AttributeValue[] argVals;
				if (inputsAfterSubFuncIterator.hasNext())
				{
					// we are still evaluating argument expressions for the first time
					try
					{
						argVals = inputsAfterSubFuncIterator.next().evaluate(context).values();
						/*
						 * If result bag empty, returns False as there will be no possibility for a
						 * Predicate that is "True", in particular if
						 * AttributeDesignator/AttributeSelector with MustBePresent=False evaluates
						 * to empty bag.
						 */
						if (argVals.length == 0)
						{
							return BooleanAttributeValue.FALSE;
						}

					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(errorEvalArgAfterFirst, e.getStatusCode(), e);
					}
					// save the result for reuse when building the next list of sub-function
					// arguments to avoid re-evaluation
					nonFirstArgEvalResultsIterator.add(argVals);
				} else
				/*
				 * No more arg expression to evaluate, but we may have evaluated them all with
				 * results put in nonFirstArgEvalResultsIterator, then started a new combination of
				 * arguments from the start, working with nonFirstArgEvalResultsIterator only after
				 * that So check where we are with nonFirstArgEvalResultsIterator
				 */
				if (nonFirstArgEvalResultsIterator.hasNext())
				{
					argVals = nonFirstArgEvalResultsIterator.next();
				} else
				{
					// no more argument to add to the list of sub-function arguments
					argVals = null;
				}

				if (argVals == null)
				{
					// we finished a list of sub-function arguments, so we can call the sub-function
					// with it
					final AttributeValue[] subFuncArgValues = subFuncArgsStack.toArray(new AttributeValue[subFuncArity]);
					try
					{
						return subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFuncCallErrorMsgPrefix + subFuncArgsStack, e.getStatusCode(), e);
					}
				}

				/*
				 * For each value in the arg bag, add this extra value to the sub-function argument
				 * stack and call eval()
				 */
				for (final AttributeValue argVal : argVals)
				{
					subFuncArgsStack.add(argVal);
					final BooleanAttributeValue subResult = eval(inputsAfterSubFuncIterator, nonFirstArgEvalResultsIterator, subFuncArgsStack, context);
					/*
					 * On each call to eval, nonFirstArgEvalResultsIterator.next()/add() is called,
					 * so we need to go backwards, when going back to the caller
					 */
					nonFirstArgEvalResultsIterator.previous();
					if (subResult.getValue())
					{
						return BooleanAttributeValue.TRUE;
					}

					/*
					 * Remove the arg we just added at the start of the iteration, to leave the
					 * place for the new arg in the next iteration
					 */
					subFuncArgsStack.removeLast();
				}

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
				final List<AttributeValue[]> inputsAfterSubFuncEvalResults = new ArrayList<>();

				/*
				 * We build the list of sub-function argument values (extracted progressively from
				 * inputsAfterSubFuncEvalResults) in a Stack. Stack is convenient for managing the
				 * sub-function arguments because we will be pushing/popping for each value in each
				 * bag argument, to make the list of the sub-function's arguments before we can make
				 * each sub-function call.
				 */
				final Deque<AttributeValue> subFuncArgsStack = new ArrayDeque<>(subFuncArity);

				// the subsequent logic is put in separated method because we need to call it
				// recursively over nonFirstArgExpsIterator
				return eval(inputsAfterSubFunc.iterator(), inputsAfterSubFuncEvalResults.listIterator(), subFuncArgsStack, context);
			}

			@Override
			public final DatatypeDef getReturnType()
			{
				return BooleanAttributeValue.TYPE;
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

		protected abstract BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, BagResult<? extends AttributeValue> bag0Vals, BagResult<? extends AttributeValue> bag1Vals, EvaluationContext context) throws IndeterminateEvaluationException;

		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, Expression<BagResult<? extends AttributeValue>> inputBag0, Expression<BagResult<? extends AttributeValue>> inputBag1, EvaluationContext context) throws IndeterminateEvaluationException
		{
			final BagResult<? extends AttributeValue> bag0Vals;
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

			final BagResult<? extends AttributeValue> bag1Vals;
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
			private final Expression<BagResult<? extends AttributeValue>> input0;
			private final Expression<BagResult<? extends AttributeValue>> input1;

			protected TwoBagFunctionCall(FirstOrderFunction<BooleanAttributeValue> subFunc, Expression<BagResult<? extends AttributeValue>> input0, Expression<BagResult<? extends AttributeValue>> input1)
			{
				final DatatypeDef[] subFuncArgTypes = { new DatatypeDef(input0.getReturnType().datatypeURI()), new DatatypeDef(input1.getReturnType().datatypeURI()) };
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
			public final DatatypeDef getReturnType()
			{
				return BooleanAttributeValue.TYPE;
			}

		}

		@Override
		protected final FunctionCall<BooleanAttributeValue> createFunctionCallFromSubFunction(FirstOrderFunction<BooleanAttributeValue> subFunc, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubFunc)
		{

			final Iterator<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputsAfterSubfuncIterator = inputsAfterSubFunc.iterator();

			while (inputsAfterSubfuncIterator.hasNext())
			{
				// all must be bag
				if (!inputsAfterSubfuncIterator.next().getReturnType().isBag())
				{
					throw new IllegalArgumentException(this + ": Invalid last argument type: primitive (not a bag). Required: a bag");
				}
			}

			final Expression<BagResult<? extends AttributeValue>> input0 = (Expression<BagResult<? extends AttributeValue>>) inputsAfterSubFunc.get(0);
			final Expression<BagResult<? extends AttributeValue>> input1 = (Expression<BagResult<? extends AttributeValue>>) inputsAfterSubFunc.get(1);
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
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, BagResult<? extends AttributeValue> bag0, BagResult<? extends AttributeValue> bag1, EvaluationContext context) throws IndeterminateEvaluationException

		{
			final Deque<Expression<? extends ExpressionResult<? extends AttributeValue>>> subFuncArgStack = new ArrayDeque<>(2);
			for (final AttributeValue bag0Val : bag0.values())
			{
				subFuncArgStack.add(bag0Val);
				boolean isAnyTrue = false;
				for (final AttributeValue bag1Val : bag1.values())
				{
					subFuncArgStack.add(bag1Val);
					final AttributeValue[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
					final BooleanAttributeValue subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFunctionCallErrMsgPrefix + subFuncArgStack, Status.STATUS_PROCESSING_ERROR);
					}

					subFuncArgStack.removeLast();
					if (subResult.getValue())
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
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, BagResult<? extends AttributeValue> bag0, BagResult<? extends AttributeValue> bag1, EvaluationContext context) throws IndeterminateEvaluationException

		{
			final Deque<Expression<? extends ExpressionResult<? extends AttributeValue>>> subFuncArgStack = new ArrayDeque<>(2);

			// same as all-of-any but in reverse order of bag0 and bag1
			for (final AttributeValue bag1Val : bag1.values())
			{
				boolean isAnyTrue = false;
				for (final AttributeValue bag0Val : bag0.values())
				{
					subFuncArgStack.add(bag0Val);
					subFuncArgStack.add(bag1Val);
					final AttributeValue[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
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
					if (subResult.getValue())
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
		protected final BooleanAttributeValue evaluate(FirstOrderFunctionCall<BooleanAttributeValue> subFuncCall, BagResult<? extends AttributeValue> bag0, BagResult<? extends AttributeValue> bag1, EvaluationContext context) throws IndeterminateEvaluationException

		{
			final Deque<Expression<? extends ExpressionResult<? extends AttributeValue>>> subFuncArgStack = new ArrayDeque<>(2);
			for (final AttributeValue bag0Val : bag0.values())
			{
				subFuncArgStack.add(bag0Val);
				boolean areAllTrue = true;
				for (final AttributeValue bag1Val : bag1.values())
				{
					subFuncArgStack.add(bag1Val);
					final AttributeValue[] subFuncArgValues = subFuncArgStack.toArray(new AttributeValue[2]);
					final BooleanAttributeValue subResult;
					try
					{
						subResult = subFuncCall.evaluate(context, subFuncArgValues);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException(subFunctionCallErrMsgPrefix + subFuncArgStack, Status.STATUS_PROCESSING_ERROR);
					}

					subFuncArgStack.removeLast();
					if (!subResult.getValue())
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
