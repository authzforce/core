package com.thalesgroup.authzforce.core.func;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;

/**
 * An abstract utility superclass for "simple" functions, "simple" as opposed to "higher-order".
 * (High-order functions are implemented in separate classes.) Supplies several useful methods,
 * making it easier to implement a "simple" <code>Function</code>. You can extend this class or
 * implement <code>Function</code> directly, depending on your needs.
 * 
 * @param <T>
 *            type of function result, i.e. single-valued V or bag of Vs
 */
public abstract class BaseFunction<T extends ExpressionResult<? extends AttributeValue>> extends Function<T>
{

	private static final IllegalArgumentException INVALID_VARARG_METHOD_PARAMETER_COUNT_EXCEPTION = new IllegalArgumentException("Invalid number of parameter types (0) for a varargs function. Such function requires at least one type for the final variable-length argument.");

	// the return type of this function, and whether it's a bag
	protected final DatatypeDef returnType;

	// parameter types
	protected final DatatypeDef[] paramTypes;

	/**
	 * Is the last parameter specified in <code>paramTypes</code> considered as variable-length
	 * (like Java {@link Method#isVarArgs()}), i.e. taking a variable number of arguments (0 or
	 * more) of the specified paramTypes[n-1] with n the size of paramTypes). In the following
	 * examples, '...' means varargs like in Java:
	 * <p>
	 * Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string},
	 * isVarargs=true
	 * </p>
	 * <p>
	 * Example 2: or(boolean...) -> paramTypes={boolean}, isVarargs=true (As you can see,
	 * isVarargs=true really means 0 or more args; indeed, the or function can take 0 parameter
	 * according to spec)
	 * </p>
	 * <p>
	 * Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}, isVarargs=true
	 * </p>
	 * <p>
	 * Example 4: abs(integer) -> paramTypes={integer}, isVarargs=false
	 * </p>
	 * <p>
	 * Example 5: string-equal(string, string) -> paramTypes={string, string}, isVarargs=false
	 * </p>
	 * <p>
	 * Example 6: date-add-yearMonthDuration(date, yearMonthDuration) -> paramTypes={date,
	 * yearMonthDuration}, isVarargs=false
	 * </p>
	 */
	protected final boolean isVarargs;

	private final Class<? extends EagerEvalCall> compatibleEagerEvalCallClass;

	private final int numOfSameTypePrimitiveParamsBeforeBag;

	/**
	 * Constructor that sets up the function as having different types for each given parameter and
	 * possibly a final variable-length argument.
	 * 
	 * @param functionId
	 *            the URI of this function as used by the factory and any XACML policies
	 * @param paramTypes
	 *            the type of each parameter, in order, required by this function, as used by the
	 *            factory and any XACML documents
	 * @param varArgs
	 *            true if and only if function takes a variable number of arguments (like Java
	 *            {@link Method#isVarArgs()}, i.e. the final type in <code>paramTypes</code> can be
	 *            repeated 0 or more times to match a variable-length argument
	 *            <p>
	 *            Examples with varargs=true ('...' means varargs like in Java):
	 *            </p>
	 *            <p>
	 *            Example 1: string-concat(string, string, string...) -> paramTypes={string, string,
	 *            string}
	 *            </p>
	 *            <p>
	 *            Example 2: or(boolean...) -> paramTypes={boolean} (As you can see, isVarargs=true
	 *            really means 0 or more args; indeed, the or function can take 0 parameter
	 *            according to spec)
	 *            </p>
	 *            <p>
	 *            Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}
	 *            </p>
	 * 
	 * @param returnType
	 *            the type returned by this function, as used by the factory and any XACML documents
	 * 
	 */
	public BaseFunction(String functionId, DatatypeDef returnType, boolean varArgs, DatatypeDef... paramTypes)
	{
		super(functionId);
		if (varArgs && paramTypes.length == 0)
		{
			throw INVALID_VARARG_METHOD_PARAMETER_COUNT_EXCEPTION;
		}

		this.paramTypes = paramTypes;

		/*
		 * Determine compatible eager-eval function call if any (all parameters must have same
		 * primitive datatype), depending on number of primitive parameters against total number of
		 * parameters
		 */
		String commonParamTypeURI = null;
		int primParamCount = 0;
		for (final DatatypeDef paramType : paramTypes)
		{
			final String paramTypeURI = paramType.datatypeURI();
			if (paramTypeURI == null)
			{
				commonParamTypeURI = null;
				break;
			}

			if (commonParamTypeURI == null)
			{
				commonParamTypeURI = paramTypeURI;
			} else if (!paramTypeURI.equals(commonParamTypeURI))
			{
				commonParamTypeURI = null;
				break;
			}

			if (!paramType.isBag())
			{
				primParamCount++;
			}
		}

		this.numOfSameTypePrimitiveParamsBeforeBag = primParamCount;
		if (commonParamTypeURI == null)
		{
			/*
			 * Parameters do not have same primitive datatype -> no EagerEvalCall is compatible
			 */
			this.compatibleEagerEvalCallClass = null;
		} else
		{
			// parameters have same primitive datatype
			if (numOfSameTypePrimitiveParamsBeforeBag == paramTypes.length)
			{
				// all parameters are primitive
				this.compatibleEagerEvalCallClass = EagerPrimitiveEvalCall.class;
			} else if (numOfSameTypePrimitiveParamsBeforeBag == 0)
			{
				// no primitive parameters -> all parameters are bag
				this.compatibleEagerEvalCallClass = EagerBagEvalCall.class;
			} else
			{
				// parly primitive, partly bag
				this.compatibleEagerEvalCallClass = EagerPartlyBagEvalCall.class;
			}
		}

		this.isVarargs = varArgs;
		this.returnType = returnType;
	}

	@Override
	public final DatatypeDef getReturnType()
	{
		return returnType;
	}

	/**
	 * Get parameter types
	 * 
	 * @return parameter types
	 */
	public final DatatypeDef[] getParameterTypes()
	{
		return this.paramTypes;
	}

	private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException("No value returned by arg evaluation in the current context", Status.STATUS_PROCESSING_ERROR);

	/**
	 * Evaluate single-valued (primitive) argument expression
	 * 
	 * @param arg
	 *            argument expression
	 * @param context
	 *            context in which argument expression is evaluated
	 * @param returnType
	 *            type of returned attribute value
	 * @return result of evaluation
	 * @throws IndeterminateEvaluationException
	 *             if no value returned from evaluation, or <code>returnType</code> is not a
	 *             supertype of the result value datatype
	 */
	protected final static <T extends AttributeValue> T evalPrimitiveArg(Expression<?> arg, EvaluationContext context, Class<T> returnType) throws IndeterminateEvaluationException
	{
		final AttributeValue attrVal = arg.evaluate(context).value();
		if (attrVal == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return returnType.cast(attrVal);
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid arg evaluation result datatype: " + attrVal.getClass().getName() + ". Expected: " + returnType.getName(), Status.STATUS_PROCESSING_ERROR, e);
		}
	}

	/**
	 * Evaluate primitive (single-valued) argument expression
	 * 
	 * @param arg
	 *            argument expression
	 * @param context
	 *            context in which argument expression is evaluated
	 * @return result of evaluation
	 * @throws IndeterminateEvaluationException
	 *             if no value returned from evaluation
	 */
	protected final static <T extends AttributeValue> T eval(Expression<PrimitiveResult<T>> arg, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final T attrVal = arg.evaluate(context).value();
		if (attrVal == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		return attrVal;
	}

	/**
	 * Evaluates primitive (single-valued result) expressions in a given context and return a array
	 * of the results.
	 * 
	 * @param args
	 *            expressions to be evaluated in <code>context</code>
	 * @param context
	 *            the evaluation context
	 * 
	 * @return array of results; each element is the result of the evaluation of the expression at
	 *         the same position in <code>args</code>. Therefore the size of the array and
	 *         <code>args</code> are the same.
	 * @throws IndeterminateEvaluationException
	 *             if evaluation of one of the arg failed
	 */
	public final static AttributeValue[] evalPrimitiveArgs(List<? extends Expression<?>> args, EvaluationContext context) throws IndeterminateEvaluationException
	{
		return evalPrimitiveArgs(args, context, AttributeValue.class);
	}

	/**
	 * Same as {@link #evalPrimitiveArgs(List, EvaluationContext)}, except all result values are
	 * stored in a given array of a specific datatype.
	 * 
	 * @param args
	 *            function arguments
	 * @param context
	 *            evaluation context
	 * @param results
	 *            an array expected to be of greater or equal size to <code>args</code> that will,
	 *            on return, contain the attribute values generated from evaluating all
	 *            <code>args</code> in <code>context</code>; the specified type <code>T</code> of
	 *            array elements must be a supertype of any expected arg evalution result datatype.
	 * @return results containing all evaluation results. If its size is bigger than
	 *         <code>args</code>, extra elements are not modified by this method.
	 * @throws IndeterminateEvaluationException
	 *             if evaluation of one of the arg failed, or <code>T</code> is not a supertype of
	 *             the result value datatype
	 * @throws IllegalArgumentException
	 *             if <code>results == null || results.length < args.size()</code>
	 */
	public final static <T extends AttributeValue> T[] evalPrimitiveArgs(List<? extends Expression<?>> args, EvaluationContext context, T[] results) throws IndeterminateEvaluationException
	{
		if (results == null)
		{
			throw EVAL_ARGS_NULL_INPUT_ARRAY_EXCEPTION;
		}

		if (results.length < args.size())
		{
			throw new IllegalArgumentException("Invalid size of input array to store Expression evaluation results: " + results.length + ". Required (>= number of input Expressions): >= " + args.size());
		}

		final Class<T> expectedResultType = (Class<T>) results.getClass().getComponentType();
		int resultIndex = 0;
		for (final Expression<?> arg : args)
		{
			// get and evaluate the next parameter
			/*
			 * The types of arguments have already been checked with checkInputs(), so casting to
			 * returnType should work.
			 */
			final T argVal;
			try
			{
				argVal = evalPrimitiveArg(arg, context, expectedResultType);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Indeterminate arg #" + resultIndex, Status.STATUS_PROCESSING_ERROR);
			}

			results[resultIndex] = argVal;
			resultIndex++;
		}

		return results;
	}

	/**
	 * Same as {@link #evalPrimitiveArgs(List, EvaluationContext)}, except all result values (must)
	 * have a specific (data)type
	 * 
	 * @param args
	 *            function arguments
	 * @param context
	 *            evaluation context
	 * @param returnType
	 *            the one type that all result values must have
	 * @return values resulting from evaluation of each input
	 * @throws IndeterminateEvaluationException
	 *             if evaluation of one of the arg failed or <code>returnType</code> is not a
	 *             supertype of the result value datatype
	 */
	private final static <T extends AttributeValue> T[] evalPrimitiveArgs(List<? extends Expression<?>> args, EvaluationContext context, Class<T> returnType) throws IndeterminateEvaluationException
	{
		final T[] results = (T[]) Array.newInstance(returnType, args.size());
		return evalPrimitiveArgs(args, context, results);
	}

	private final static <T extends AttributeValue> T[] evalBagArg(Expression<? extends ExpressionResult<? extends AttributeValue>> arg, EvaluationContext context, Class<T[]> resultArrayType) throws IndeterminateEvaluationException
	{
		// assert emptyResultArray != null;

		// final Collection<? extends AttributeValue> attrVals =
		// arg.evaluate(context).getAttributeValues();
		final AttributeValue[] attrVals = arg.evaluate(context).values();
		// if (attrVals == null || attrVals.length == 0)
		// {
		// return emptyResultArray;
		// }

		try
		{
			return resultArrayType.cast(attrVals);
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid arg evaluation result's bag value datatype: " + attrVals.getClass().getComponentType().getName() + ". Expected: " + resultArrayType.getClass().getComponentType().getName(), Status.STATUS_PROCESSING_ERROR, e);
		}
	}

	private final static <T extends AttributeValue> T[][] evalBagArgs(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> args, EvaluationContext context, T[][] results, Class<T[]> resultArrayType) throws IndeterminateEvaluationException
	{
		if (results == null)
		{
			throw EVAL_ARGS_NULL_INPUT_ARRAY_EXCEPTION;
		}

		if (results.length < args.size())
		{
			throw new IllegalArgumentException("Invalid size of input array to store Expression evaluation results: " + results.length + ". Required (>= number of input Expressions): >= " + args.size());
		}

		int resultIndex = 0;
		for (final Expression<? extends ExpressionResult<? extends AttributeValue>> arg : args)
		{
			// get and evaluate the next parameter
			/*
			 * The types of arguments have already been checked with checkInputs(), so casting to
			 * returnType should work.
			 */
			final T[] argVals;
			try
			{
				argVals = evalBagArg(arg, context, resultArrayType);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Indeterminate arg #" + resultIndex, Status.STATUS_PROCESSING_ERROR);
			}

			results[resultIndex] = argVals;
			resultIndex++;
		}

		return results;
	}

	private final static <T extends AttributeValue> T[][] evalBagArgs(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> args, EvaluationContext context, Class<T[]> resultArrayType) throws IndeterminateEvaluationException
	{
		final T[][] results = (T[][]) Array.newInstance(resultArrayType, args.size());
		return evalBagArgs(args, context, results, resultArrayType);
	}

	private static final IllegalArgumentException EVAL_ARGS_NULL_INPUT_ARRAY_EXCEPTION = new IllegalArgumentException("Input array to store evaluation results is NULL");

	private final static void checkArgType(DatatypeDef argType, int argIndex, DatatypeDef expectedType) throws IllegalArgumentException
	{
		if (!argType.equals(expectedType))
		{
			throw new IllegalArgumentException("Type of arg #" + argIndex + " not valid: " + argType + ". Required: " + expectedType + ".");
		}
	}

	/**
	 * Check input types and number against expected types (according on a function's definition)
	 * 
	 * @param inputTypes
	 *            input types
	 * @param expectedTypes
	 * @param isVarArgs
	 *            whether the checked function is varargs, i.e. the last type in
	 *            <code>expectedTypes</code> is a varargs parameter type
	 * 
	 * @throws IllegalArgumentException
	 *             if the number of arguments or argument types are invalid
	 */
	private final void checkInputs(DatatypeDef[] inputTypes, DatatypeDef[] expectedTypes) throws IllegalArgumentException
	{
		final int numOfInputs = inputTypes.length;
		int argIndex = 0;
		if (isVarargs)
		{
			if (expectedTypes.length == 0)
			{
				throw INVALID_VARARG_METHOD_PARAMETER_COUNT_EXCEPTION;
			}

			/*
			 * The last parameter type (last item in paramTypes) of a varargs function can occur 0
			 * or more times in arguments, so total number of function arguments (arity) can be
			 * (paramTypes.length - 1) or more.
			 */
			final int varArgIndex = expectedTypes.length - 1; // = minimum arity
			if (numOfInputs < varArgIndex)
			{
				throw new IllegalArgumentException("Wrong number of args for varargs function: " + numOfInputs + ". Required: >= " + varArgIndex);
			}

			for (final DatatypeDef input : inputTypes)
			{
				final DatatypeDef expectedType;
				// if number of inputs exceeds size of paramTypes, input types must be varargType
				if (argIndex < inputTypes.length)
				{
					expectedType = expectedTypes[argIndex];
				} else
				{
					expectedType = expectedTypes[varArgIndex];

				}

				checkArgType(input, argIndex, expectedType);
				argIndex++;
			}
		} else
		{
			// Fixed number of arguments
			if (numOfInputs != expectedTypes.length)
			{
				throw new IllegalArgumentException("Wrong number of args: " + numOfInputs + ". Required: " + expectedTypes.length);
			}

			// now, make sure everything is of the same, correct type
			for (final DatatypeDef input : inputTypes)
			{
				checkArgType(input, argIndex, expectedTypes[argIndex]);
				argIndex++;
			}
		}
	}

	/**
	 * Parse/validate the function inputs and returns a FunctionCall (for calling this function )
	 * generated by {@link #getFunctionCall(List, DatatypeDef[])}. This method is called by
	 * {@link #parseInputs(List)} with empty varargs parameter.
	 * 
	 * @param inputExpressions
	 *            function arguments (expressions)
	 * 
	 * @param evalTimeInputTypes
	 *            types of remaining inputs, if not all arguments could be specified in
	 *            <code>inputExpressions</code> because only the type is known. Therefore, only
	 *            their type is checked, and the actual expression may be specified later as last
	 *            parameter when calling
	 *            {@link Call#evaluate(EvaluationContext, boolean, AttributeValue...)} at evaluation
	 *            time, via the returned <code>FunctionCall</code>.
	 * @return Function call handle for calling this function which such inputs (with possible
	 *         changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	public final Call parseInputs(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputExpressions, DatatypeDef... evalTimeInputTypes) throws IllegalArgumentException
	{
		final DatatypeDef[] inputTypes = new DatatypeDef[inputExpressions.size() + evalTimeInputTypes.length];
		int i = 0;
		for (final Expression<?> input : inputExpressions)
		{
			inputTypes[i] = input.getReturnType();
			i++;
		}

		for (final DatatypeDef evalTimeInputType : evalTimeInputTypes)
		{
			inputTypes[i] = evalTimeInputType;
			i++;
		}

		checkInputs(inputTypes, paramTypes);
		return getFunctionCall(inputExpressions, evalTimeInputTypes);
	}

	private static final DatatypeDef[] EMPTY_DATATYPE_DEF_ARRAY = new DatatypeDef[] {};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.test.func.Function#parseInputs(java.util.List)
	 */
	@Override
	public final FunctionCall<T> parseInputs(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputExpressions) throws IllegalArgumentException
	{
		return parseInputs(inputExpressions, EMPTY_DATATYPE_DEF_ARRAY);
	}

	/**
	 * Returns a function call for calling this function. This method is called by
	 * {@link #parseInputs(List, DatatypeDef...)} so argument types have been checked before calling
	 * this.
	 * 
	 * @param checkedArgExpressions
	 *            input expressions already checked
	 * @param checkedRemainingArgTypes
	 *            evaluation-time input types - already checked - completing
	 *            <code>checkedInputExpressions</code>, i.e. types of unknown arg expressions to be
	 *            used as the varargs parameter of
	 *            {@link BaseFunctionCall#evaluate(EvaluationContext, AttributeValue...)} and
	 *            {@link BaseFunctionCall#evaluate(EvaluationContext, boolean, AttributeValue...)}
	 * @return FunctionCall handle to be used for calling the function
	 * @throws IllegalArgumentException
	 *             if function-specific error parsing <code>checkedArgExpressions</code> or
	 *             <code>checkedRemainingArgTypes</code>
	 */
	protected abstract Call getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes) throws IllegalArgumentException;

	private static final AttributeValue[] EMPTY_ARRAY = new AttributeValue[0];

	/**
	 * Function call, usually resulting {@link BaseFunction#parseInputs(List, DatatypeDef...)}. is
	 * the recommended way of calling any {@link BaseFunction} instance. This is quite similar to
	 * XACML Apply except it does not include the Description field; and arguments are optimized
	 * specifically for each function by extending this class accordinly, therefore they might be
	 * quite different from original input Expressions of the Apply. In particular, if some
	 * expressions are actually static values (e.g. AttributeValue, VariableReference to
	 * AttributeValue, function applied to static values...), these expressions might be
	 * pre-compiled/pre-evaluated. For instance, a static regex parameter to regexp-match function
	 * may be pre-compiled to a regex for re-use.
	 * <p>
	 * Some of the arguments (expressions) may not be known in advance, but only at evaluation time
	 * (when calling {@link #evaluate(EvaluationContext, AttributeValue...)}). For example, when
	 * using a BaseFunction as a sub-function of the Higher-Order function 'any-of', the last
	 * arguments of the sub-function are determined during evaluation, after evaluating the
	 * expression of the last input in the context, and getting the various values in the result
	 * bag.
	 * <p>
	 * In the case of such evaluation-time args, you must pass their types (the datatype of the last
	 * input bag in the previous example) as the <code>remainingArgTypes</code> parameters to
	 * {@link Call#Call(DatatypeDef...)}, and correspond to the types of the
	 * <code>remainingArgs</code> passed later as parameters to
	 * {@link #evaluate(EvaluationContext, AttributeValue...)}.
	 * 
	 * 
	 */
	public abstract class Call implements FunctionCall<T>
	{
		private final DatatypeDef[] expectedRemainingArgTypes;

		/**
		 * Instantiates Function Call
		 * 
		 * @param remainingArgTypes
		 *            types of arguments of which the actual Expressions are unknown at this point,
		 *            but will be known and passed at evaluation time as <code>remainingArgs</code>
		 *            parameter to {@link #evaluate(EvaluationContext, boolean, AttributeValue...)},
		 *            then {@link #evaluate(EvaluationContext, AttributeValue...)}. Only
		 *            non-bag/primitive values are valid <code>remainingArgs</code> to prevent
		 *            varargs warning in {@link #evaluate(EvaluationContext, AttributeValue...)}
		 *            (potential heap pollution via varargs parameter) that would be caused by using
		 *            a parameterized type such as ExpressionResult/Collection to represent both
		 *            bags and primitives.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected Call(DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			for (int i = 0; i < remainingArgTypes.length; i++)
			{
				if (remainingArgTypes[i].isBag())
				{
					throw new IllegalArgumentException("Invalid evaluation-time arg type: remainingArgTypes[" + i + "] is a bag type. Only primitive types are allowed.");
				}

				i++;
			}

			this.expectedRemainingArgTypes = remainingArgTypes;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.thalesgroup.authzforce.core.func.FunctionCall#evaluate(com.thalesgroup.authzforce.core.test.EvaluationCtx)
		 */
		@Override
		public final T evaluate(EvaluationContext context) throws IndeterminateEvaluationException
		{
			return evaluate(context, EMPTY_ARRAY);
		}

		/**
		 * Make the call in a given evaluation context and argument values resolved at evaluation
		 * time. This method is called by
		 * {@link #evaluate(EvaluationContext, boolean, AttributeValue...)} after checking
		 * evaluation-time args.
		 * 
		 * @param context
		 *            evaluation context
		 * @param remainingArgs
		 *            remaining args corresponding to <code>remainingArgTypes</code> parameters
		 *            passed to {@link #BaseFunctionCall(DatatypeDef, boolean, DatatypeDef...)}.
		 *            Only non-bag/primitive values are valid <code>remainingArgs</code> to prevent
		 *            varargs warning in {@link #evaluate(EvaluationContext, AttributeValue...)}
		 *            (potential heap pollution via varargs parameter) that would be caused by using
		 *            a parameterized type such as ExpressionResult/Collection to represent both
		 *            bags and primitives.
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract T evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException;

		/**
		 * Make the call in a given evaluation context. This method calls
		 * {@link #evaluate(EvaluationContext, AttributeValue...)} after checking
		 * <code>remainingArgTypes</code> if <code>checkremainingArgTypes = true</code>
		 * 
		 * @param context
		 *            evaluation context
		 * @param checkRemainingArgTypes
		 *            whether to check <code>remainingArgs</code> against
		 *            <code>remainingArgTypes</code> passed to {@link Call#Call(DatatypeDef...)}. It
		 *            is strongly recommended to set this to <code>true</code> always, unless you
		 *            have already checked the types are OK before calling this method and want to
		 *            skip re-checking for efficiency.
		 * 
		 * @param remainingArgs
		 *            remaining args corresponding to <code>remainingArgTypes</code> parameters
		 *            passed to {@link Call#Call(DatatypeDef...)}.
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if <code>checkremainingArgTypes = true</code> and <code>remainingArgs</code>
		 *             do not check OK, or if they do and
		 *             {@link #evaluate(EvaluationContext, AttributeValue...)} throws an exception
		 */
		public final T evaluate(EvaluationContext context, boolean checkRemainingArgTypes, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			if (checkRemainingArgTypes)
			{
				final DatatypeDef[] remainingArgTypes = new DatatypeDef[remainingArgs.length];
				for (int i = 0; i < remainingArgs.length; i++)
				{
					remainingArgTypes[i] = new DatatypeDef(remainingArgs[i].getDataType());
					i++;
				}

				checkInputs(remainingArgTypes, expectedRemainingArgTypes);
			}

			return evaluate(context, remainingArgs);
		}

		@Override
		public final DatatypeDef getReturnType()
		{
			return returnType;
		}

	}

	private abstract class EagerEvalCall<PARAM_T extends AttributeValue> extends Call
	{
		protected final Class<PARAM_T[]> parameterArrayClass;
		protected final Class<PARAM_T> parameterClass;
		protected final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions;
		protected final String indeterminateArgMessage;
		protected final int argCount;
		protected final int remainingArgsStartIndex;

		/**
		 * Instantiates Function Call
		 * 
		 * @param argArrayType
		 *            array class of which element type is <code>PARAM_T</code> (arg value
		 *            super-datatype). If argument expressions return different datatypes, the
		 *            supertype of all - {@link AttributeValue} - may be specified.
		 * @param numOfPrimArgsBeforeBag
		 *            number of primitive args before first bag arg
		 * @param args
		 *            arguments' Expressions
		 * @param remainingArgTypes
		 *            types of arguments following <code>args</code>, and of which the actual
		 *            Expression is unknown at this point, but will be known and passed at
		 *            evaluation time as <code>remainingArgs</code> parameter to
		 *            {@link #evaluate(EvaluationContext, boolean, AttributeValue...)}, then
		 *            {@link #evaluate(EvaluationContext, AttributeValue...)}.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected EagerEvalCall(Class<PARAM_T[]> argArrayType, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> args, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			super(remainingArgTypes);
			// Check whether the selected EagerEvalClass is compatible with this function (i.e. its
			// function parameters)
			if (this.getClass() != compatibleEagerEvalCallClass)
			{
				throw new IllegalArgumentException("Function " + functionId + ": incompatible EagerEvalClass specified: " + this.getClass() + ". Compatible: " + (compatibleEagerEvalCallClass == null ? "none" : compatibleEagerEvalCallClass));
			}

			if (argArrayType == null)
			{
				throw new IllegalArgumentException("Function " + functionId + ": Undefined parameter array type for eager-evaluation function call");
			}

			this.parameterArrayClass = argArrayType;
			this.parameterClass = (Class<PARAM_T>) parameterArrayClass.getComponentType();
			this.argExpressions = args;
			this.indeterminateArgMessage = "Function " + functionId + ": indeterminate arg";
			// total number of arguments to the function
			if (args == null)
			{
				this.argCount = remainingArgTypes.length;
				this.remainingArgsStartIndex = 0;
			} else
			{
				this.argCount = args.size() + remainingArgTypes.length;
				this.remainingArgsStartIndex = args.size();
			}
		}
	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL
	 * their arguments' expressions to actual values, before the function can be evaluated. This is
	 * the case of most functions in XACML. Examples of functions that do NOT use eager evaluation
	 * are logical functions such as 'or', 'and', 'n-of'. Indeed, these functions can return the
	 * final result before all arguments have been evaluated, e.g. the 'or' function returns True as
	 * soon as one of the arguments return True, regardless of the remaining arguments.
	 * 
	 * @param <PARAM_T>
	 *            arg values' supertype. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	protected abstract class EagerPartlyBagEvalCall<PARAM_T extends AttributeValue> extends EagerEvalCall<PARAM_T>
	{
		private final int numOfArgExpressions;

		protected EagerPartlyBagEvalCall(Class<PARAM_T[]> argArrayType, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> args, DatatypeDef[] remainingArgTypes) throws IllegalArgumentException
		{
			super(argArrayType, args, remainingArgTypes);
			if (argExpressions == null || (numOfArgExpressions = argExpressions.size()) <= numOfSameTypePrimitiveParamsBeforeBag)
			{
				// all arg expressions are primitive
				throw new IllegalArgumentException("Function " + functionId + ": no bag expression in arguments. At least one bag expression is required to use this type of FunctionCall: " + this.getClass());
			}
		}

		@Override
		protected final T evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			/*
			 * We checked in constructor that argExpressions.size >
			 * numOfSameTypePrimitiveParamsBeforeBag
			 */
			final PARAM_T[] primArgsBeforeBag;
			final PARAM_T[][] bagArgs;
			try
			{
				primArgsBeforeBag = evalPrimitiveArgs(argExpressions.subList(0, numOfSameTypePrimitiveParamsBeforeBag), context, parameterClass);
				bagArgs = evalBagArgs(argExpressions.subList(numOfSameTypePrimitiveParamsBeforeBag, numOfArgExpressions), context, parameterArrayClass);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this.indeterminateArgMessage, Status.STATUS_PROCESSING_ERROR, e);
			}

			final PARAM_T[] castRemainingArgs;
			try
			{
				castRemainingArgs = parameterArrayClass.cast(remainingArgs);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Function " + functionId + ": Type of remaining args (# > " + remainingArgsStartIndex + ") not valid: " + remainingArgs.getClass().getComponentType() + ". Required: " + parameterClass + ".", Status.STATUS_PROCESSING_ERROR);
			}

			return evaluate(primArgsBeforeBag, bagArgs, castRemainingArgs);
		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument
		 * expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param args
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract T evaluate(PARAM_T[] primArgsBeforeBag, PARAM_T[][] bagArgs, PARAM_T[] remainingArgs) throws IndeterminateEvaluationException;

	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL
	 * their arguments' expressions to actual values, before the function can be evaluated. This is
	 * the case of most functions in XACML. Examples of functions that do NOT use eager evaluation
	 * are logical functions such as 'or', 'and', 'n-of'. Indeed, these functions can return the
	 * final result before all arguments have been evaluated, e.g. the 'or' function returns True as
	 * soon as one of the arguments return True, regardless of the remaining arguments.
	 * 
	 * @param <PARAM_T>
	 *            arg values' supertype. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	protected abstract class EagerPrimitiveEvalCall<PARAM_T extends AttributeValue> extends EagerEvalCall<PARAM_T>
	{
		/**
		 * Instantiates Function Call
		 * 
		 * @param argArrayType
		 *            array class of which element type is <code>PARAM_T</code> (arg value
		 *            super-datatype). If argument expressions return different datatypes, the
		 *            supertype of all - {@link AttributeValue} - may be specified.
		 * @param args
		 *            arguments' Expressions
		 * @param remainingArgTypes
		 *            types of arguments following <code>args</code>, and of which the actual
		 *            Expression is unknown at this point, but will be known and passed at
		 *            evaluation time as <code>remainingArgs</code> parameter to
		 *            {@link #evaluate(EvaluationContext, boolean, AttributeValue...)}, then
		 *            {@link #evaluate(EvaluationContext, AttributeValue...)}.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected EagerPrimitiveEvalCall(Class<PARAM_T[]> argArrayType, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> args, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			super(argArrayType, args, remainingArgTypes);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.thalesgroup.authzforce.core.func.BaseFunctionCall#evaluate(com.thalesgroup.authzforce.core.test.EvaluationCtx
		 * , com.thalesgroup.authzforce.core.attr.AttributeValue[])
		 */
		@Override
		protected final T evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			final PARAM_T[] finalArgs = parameterArrayClass.cast(Array.newInstance(parameterClass, argCount));
			if (argExpressions != null)
			{
				try
				{
					BaseFunction.evalPrimitiveArgs(argExpressions, context, finalArgs);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this.indeterminateArgMessage, Status.STATUS_PROCESSING_ERROR, e);
				}
			}

			for (int i = 0; i < remainingArgs.length; i++)
			{
				try
				{
					finalArgs[remainingArgsStartIndex + i] = parameterClass.cast(remainingArgs[i]);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException("Function " + functionId + ": Type of arg #" + (remainingArgsStartIndex + i) + " not valid: " + remainingArgs[i].getClass() + ". Required: " + parameterClass + ".", Status.STATUS_PROCESSING_ERROR);
				}
			}

			return evaluate(finalArgs);
		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument
		 * expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param args
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract T evaluate(PARAM_T[] args) throws IndeterminateEvaluationException;

	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL
	 * their arguments' expressions to actual values, before the function can be evaluated. This is
	 * the case of most functions in XACML. Examples of functions that do NOT use eager evaluation
	 * are logical functions such as 'or', 'and', 'n-of'. Indeed, these functions can return the
	 * final result before all arguments have been evaluated, e.g. the 'or' function returns True as
	 * soon as one of the arguments return True, regardless of the remaining arguments.
	 * 
	 * @param <PARAM_T>
	 *            arg values' supertype. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	public abstract class EagerBagEvalCall<PARAM_T extends AttributeValue> extends EagerEvalCall<PARAM_T>
	{
		/**
		 * Instantiates Function Call
		 * 
		 * @param argArrayType
		 *            array class of which element type is <code>PARAM_T</code> (arg value
		 *            super-datatype). If argument expressions return different datatypes, the
		 *            supertype of all - {@link AttributeValue} - may be specified.
		 * @param args
		 *            arguments' Expressions
		 * @param remainingArgTypes
		 *            types of arguments following <code>args</code>, and of which the actual
		 *            Expression is unknown at this point, but will be known and passed at
		 *            evaluation time as <code>remainingArgs</code> parameter to
		 *            {@link #evaluate(EvaluationContext, boolean, AttributeValue...)}, then
		 *            {@link #evaluate(EvaluationContext, AttributeValue...)}.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected EagerBagEvalCall(Class<PARAM_T[]> argArrayType, List<Expression<? extends ExpressionResult<? extends AttributeValue>>> args, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
		{
			super(argArrayType, args, remainingArgTypes);
			if (argExpressions == null)
			{
				// all arg expressions are primitive
				throw new IllegalArgumentException("Function " + functionId + ": no bag expression in arguments. At least one bag expression is required to use this type of FunctionCall: " + this.getClass());
			}

		}

		@Override
		protected T evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{

			/*
			 * We checked in constructor that argExpressions != null
			 */
			final PARAM_T[][] bagArgs;
			try
			{
				bagArgs = evalBagArgs(argExpressions, context, parameterArrayClass);

			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this.indeterminateArgMessage, Status.STATUS_PROCESSING_ERROR, e);
			}

			final PARAM_T[] castRemainingArgs;
			try
			{
				castRemainingArgs = parameterArrayClass.cast(remainingArgs);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Function " + functionId + ": Type of remaining args (# > " + remainingArgsStartIndex + ") not valid: " + remainingArgs.getClass().getComponentType() + ". Required: " + parameterClass + ".", Status.STATUS_PROCESSING_ERROR);
			}

			return evaluate(bagArgs, castRemainingArgs);
		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument
		 * expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param args
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract T evaluate(PARAM_T[][] bagArgs, PARAM_T[] remainingArgs) throws IndeterminateEvaluationException;

	}
}
