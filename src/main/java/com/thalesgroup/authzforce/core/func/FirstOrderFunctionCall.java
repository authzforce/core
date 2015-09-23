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

import java.lang.reflect.Array;
import java.util.List;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.Expression.Utils;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Function call, made of a function definition and given arguments to be passed to the function. It
 * is the recommended way of calling any {@link FirstOrderFunction} instance.
 * <p>
 * Some of the arguments (expressions) may not be known in advance, but only at evaluation time
 * (when calling {@link #evaluate(EvaluationContext, AttributeValue...)}). For example, when using a
 * FirstOrderFunction as a sub-function of the Higher-Order function 'any-of', the last arguments of
 * the sub-function are determined during evaluation, after evaluating the expression of the last
 * input in the context, and getting the various values in the result bag.
 * <p>
 * In the case of such evaluation-time args, you must pass their types (the datatype of the last
 * input bag in the previous example) as the <code>remainingArgTypes</code> parameters to
 * {@link FirstOrderFunctionCall#FirstOrderFunctionCall(FunctionSignature, List, Datatype...)} , and
 * correspond to the types of the <code>remainingArgs</code> passed later as parameters to
 * {@link #evaluate(EvaluationContext, AttributeValue...)}.
 * 
 * @param <RETURN>
 *            function return type
 * 
 * 
 */
public abstract class FirstOrderFunctionCall<RETURN extends Expression.Value<?, RETURN>> implements FunctionCall<RETURN>
{
	private static final IllegalArgumentException EVAL_ARGS_NULL_INPUT_ARRAY_EXCEPTION = new IllegalArgumentException("Input array to store evaluation results is NULL");

	/**
	 * Evaluates primitive argument expressions in the given context, and stores all result values
	 * in a given array of a specific datatype.
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
	private final static <AV extends AttributeValue<?>> AV[] evalPrimitiveArgs(List<? extends Expression<?>> args, EvaluationContext context, AV[] results) throws IndeterminateEvaluationException
	{
		if (results == null)
		{
			throw EVAL_ARGS_NULL_INPUT_ARRAY_EXCEPTION;
		}

		if (results.length < args.size())
		{
			throw new IllegalArgumentException("Invalid size of input array to store Expression evaluation results: " + results.length + ". Required (>= number of input Expressions): >= " + args.size());
		}

		final Class<AV> expectedResultType = (Class<AV>) results.getClass().getComponentType();
		int resultIndex = 0;
		for (final Expression<?> arg : args)
		{
			// get and evaluate the next parameter
			/*
			 * The types of arguments have already been checked with checkInputs(), so casting to
			 * returnType should work.
			 */
			final AV argVal;
			try
			{
				argVal = Utils.evalSingle(arg, context, expectedResultType);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Indeterminate arg #" + resultIndex, Status.STATUS_PROCESSING_ERROR, e);
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
	private final static <T extends AttributeValue<T>> T[] evalPrimitiveArgs(List<? extends Expression<?>> args, EvaluationContext context, Class<T> returnType) throws IndeterminateEvaluationException
	{
		final T[] results = (T[]) Array.newInstance(returnType, args.size());
		return evalPrimitiveArgs(args, context, results);
	}

	private final static <T extends AttributeValue<T>> T[] evalBagArg(Expression<?> arg, EvaluationContext context, Class<T[]> resultArrayType) throws IndeterminateEvaluationException
	{
		final AttributeValue<?>[] attrVals = arg.evaluate(context).all();
		try
		{
			return resultArrayType.cast(attrVals);
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid arg evaluation result's bag value datatype: " + attrVals.getClass().getComponentType().getName() + ". Expected: " + resultArrayType.getClass().getComponentType().getName(), Status.STATUS_PROCESSING_ERROR, e);
		}
	}

	private final static <T extends AttributeValue<T>> T[][] evalBagArgs(List<Expression<?>> args, EvaluationContext context, T[][] results, Class<T[]> resultArrayType) throws IndeterminateEvaluationException
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
		for (final Expression<?> arg : args)
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

	private final static <T extends AttributeValue<T>> T[][] evalBagArgs(List<Expression<?>> args, EvaluationContext context, Class<T[]> resultArrayType) throws IndeterminateEvaluationException
	{
		final T[][] results = (T[][]) Array.newInstance(resultArrayType, args.size());
		return evalBagArgs(args, context, results, resultArrayType);
	}

	private final void checkArgType(Datatype<?> argType, int argIndex, Datatype<?> expectedType) throws IllegalArgumentException
	{
		if (!argType.equals(expectedType))
		{
			throw new IllegalArgumentException("Function " + funcSig.getName() + ": type of arg #" + argIndex + " not valid: " + argType + ". Required: " + expectedType + ".");
		}
	}

	/**
	 * Check number of arguments (arity) and their types against the function parameter types
	 * 
	 * @param inputTypes
	 *            argument types
	 * @param offset
	 *            index of parameter type in {@code funcSig.getParameterTypes()} expected to match
	 *            the first element in {@code inputTypes} . The validation starts there:
	 *            parameterTypes[offset + n] matched against inputTypes[n] for n=0 to
	 *            inputTypes.length
	 * 
	 * @throws IllegalArgumentException
	 *             if the number of arguments or argument types are invalid
	 */
	private final void validateArgs(Datatype<?>[] inputTypes, int offset) throws IllegalArgumentException
	{
		final Datatype<?>[] paramTypes = funcSig.getParameterTypes();
		assert 0 <= offset && offset < paramTypes.length;

		final int numOfInputs = inputTypes.length;
		if (funcSig.isVarArgs())
		{
			/*
			 * The last parameter type (last item in paramTypes) of a varargs function can occur 0
			 * or more times in arguments, so total number of function arguments (arity) can be
			 * (paramTypes.length - 1) or more.
			 */
			final int varArgIndex = paramTypes.length - 1; // = minimum arity
			if (offset + numOfInputs < varArgIndex)
			{
				throw new IllegalArgumentException("Wrong number of args for varargs function: " + numOfInputs + ". Required: >= " + varArgIndex);
			}

			int paramIndex = offset;
			for (final Datatype<?> input : inputTypes)
			{
				final Datatype<?> expectedType;
				// if number of inputs exceeds size of paramTypes, input types must be of type of
				// vararg parameter
				if (paramIndex < paramTypes.length)
				{
					expectedType = paramTypes[paramIndex];
				} else
				{
					expectedType = paramTypes[varArgIndex];

				}

				checkArgType(input, paramIndex, expectedType);
				paramIndex++;
			}
		} else
		{
			// Fixed number of arguments
			final int expectedNumOfInputs = paramTypes.length - offset;
			if (numOfInputs != expectedNumOfInputs)
			{
				throw new IllegalArgumentException("Wrong number of " + (offset > 0 ? "remaining args (starting at #" + offset + "): " : "args: ") + numOfInputs + ". Required: " + expectedNumOfInputs);
			}

			// now, make sure every input type is of the correct type
			int paramIndex = offset;
			for (final Datatype<?> input : inputTypes)
			{
				checkArgType(input, paramIndex, paramTypes[paramIndex]);
				paramIndex++;
			}
		}
	}

	protected final FunctionSignature<RETURN> funcSig;
	/*
	 * Number of initial arguments (excluding remainingArgs passed at evaluation time). This is also
	 * the index in function parameter array where the first item in remainingArgs start, if there
	 * is any
	 */
	protected final int initialArgCount;

	/**
	 * Instantiates a function call, including the validation of arguments ({@code inputExpressions}
	 * ) according to the function definition.
	 * 
	 * @param function
	 *            (first-order) function to which this call applies
	 * @param argExpressions
	 *            function arguments (expressions)
	 * 
	 * @param remainingArgTypes
	 *            types of arguments of which the actual Expressions are unknown at this point, but
	 *            will be known and passed at evaluation time as <code>remainingArgs</code>
	 *            parameter to {@link #evaluate(EvaluationContext, boolean, AttributeValue...)},
	 *            then {@link #evaluate(EvaluationContext, AttributeValue...)}. Only
	 *            non-bag/primitive values are valid <code>remainingArgs</code> to prevent varargs
	 *            warning in {@link #evaluate(EvaluationContext, AttributeValue...)} (potential heap
	 *            pollution via varargs parameter) that would be caused by using a parameterized
	 *            type such as Value/Collection to represent both bags and primitives.
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function or one of <code>remainingArgTypes</code>
	 *             is a bag type.
	 */
	protected FirstOrderFunctionCall(FunctionSignature<RETURN> functionSig, List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		this.funcSig = functionSig;
		this.initialArgCount = argExpressions.size();
		final Datatype<?>[] argTypes = new Datatype<?>[initialArgCount + remainingArgTypes.length];
		int i = 0;
		for (final Expression<?> argExpr : argExpressions)
		{
			argTypes[i] = argExpr.getReturnType();
			i++;
		}

		for (final Datatype<?> remainingArgType : remainingArgTypes)
		{
			if (remainingArgType.isBag())
			{
				throw new IllegalArgumentException("Invalid evaluation-time arg type: remainingArgTypes[" + i + "] is a bag type. Only primitive types are allowed.");
			}
			argTypes[i] = remainingArgType;
			i++;
		}

		validateArgs(argTypes, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FunctionCall#evaluate(com.thalesgroup.authzforce
	 * .core.test.EvaluationCtx)
	 */
	@Override
	public final RETURN evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return evaluate(context, (AttributeValue[]) null);
	}

	/**
	 * Make the call in a given evaluation context and argument values resolved at evaluation time.
	 * This method is called by {@link #evaluate(EvaluationContext, boolean, AttributeValue...)}
	 * after checking evaluation-time args.
	 * 
	 * @param context
	 *            evaluation context
	 * @param remainingArgs
	 *            remaining args corresponding to <code>remainingArgTypes</code> parameters passed
	 *            to {@link #FirstOrderFunctionCall(DatatypeDef, boolean, DatatypeDef...)}. Null if
	 *            none. Only non-bag/primitive values are valid <code>remainingArgs</code> to
	 *            prevent varargs warning in {@link #evaluate(EvaluationContext, AttributeValue...)}
	 *            (potential heap pollution via varargs parameter) that would be caused by using a
	 *            parameterized type such as Value/Collection to represent both bags and primitives.
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if any error evaluating the function
	 */
	protected abstract RETURN evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException;

	/**
	 * Make the call in a given evaluation context. This method calls
	 * {@link #evaluate(EvaluationContext, AttributeValue...)} after checking
	 * <code>remainingArgTypes</code> if <code>checkremainingArgTypes = true</code>
	 * 
	 * @param context
	 *            evaluation context
	 * @param checkRemainingArgTypes
	 *            whether to check <code>remainingArgs</code> against <code>remainingArgTypes</code>
	 *            passed as last parameters to
	 *            {@link FirstOrderFunctionCall#FirstOrderFunctionCall(FunctionSignature, List,Datatype...)}
	 *            . It is strongly recommended to set this to <code>true</code> always, unless you
	 *            have already checked the types are OK before calling this method and want to skip
	 *            re-checking for efficiency.
	 * 
	 * @param remainingArgs
	 *            remaining args corresponding to <code>remainingArgTypes</code> parameters passed
	 *            as last parameters to
	 *            {@link FirstOrderFunctionCall#FirstOrderFunctionCall(FunctionSignature, List, Datatype...)}
	 *            .
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if <code>checkremainingArgTypes = true</code> and <code>remainingArgs</code> do
	 *             not check OK, or if they do and
	 *             {@link #evaluate(EvaluationContext, AttributeValue...)} throws an exception
	 */
	public final RETURN evaluate(EvaluationContext context, boolean checkRemainingArgTypes, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
	{
		if (checkRemainingArgTypes)
		{
			final Datatype<?>[] remainingArgTypes = new Datatype<?>[remainingArgs.length];
			for (int i = 0; i < remainingArgs.length; i++)
			{
				remainingArgTypes[i] = new Datatype<>(remainingArgs[i].getClass(), remainingArgs[i].getDataType());
				i++;
			}

			/*
			 * Offset where to start validation of arguments is where remainingArgs are supposed to
			 * start, i.e. just after all the $initialArgCount initial arguments passed to
			 * FirstOrderFunctionCall constructor , i.e. starting at index initialArgCount
			 */
			validateArgs(remainingArgTypes, initialArgCount);
		}

		return evaluate(context, remainingArgs);
	}

	@Override
	public final Datatype<RETURN> getReturnType()
	{
		return funcSig.getReturnType();
	}

	/**
	 * Function call, for {@link FirstOrderFunction}s requiring <i>eager</i> (aka <i>greedy</i>)
	 * evaluation of ALL their arguments' expressions to actual values, before the function can be
	 * evaluated. This is the case of most functions in XACML. Exceptions (functions not using eager
	 * evaluation) are logical functions for instance, such as 'or', 'and', 'n-of'. Indeed, these
	 * functions can return the final result before all arguments have been evaluated, e.g. the 'or'
	 * function returns True as soon as one of the arguments return True, regardless of the
	 * remaining arguments.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PARAM_T>
	 *            arg values' supertype. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	public static abstract class EagerEval<RETURN_T extends Expression.Value<?, RETURN_T>, PARAM_T extends AttributeValue<?>> extends FirstOrderFunctionCall<RETURN_T>
	{
		protected final Class<PARAM_T[]> parameterArrayClass;
		protected final Class<PARAM_T> parameterClass;
		protected final List<Expression<?>> argExpressions;
		protected final String indeterminateArgMessage;
		// number of initial arguments (expressions) + number of remaining args if any
		protected final int totalArgCount;
		protected final int numOfSameTypePrimitiveParamsBeforeBag;

		/**
		 * Instantiates Function FirstOrderFunctionCall
		 * 
		 * @param functionSignature
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
		protected EagerEval(FunctionSignature<RETURN_T> functionSignature, Class<PARAM_T[]> argArrayType, List<Expression<?>> args, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSignature, args, remainingArgTypes);
			final Datatype<?>[] paramTypes = functionSignature.getParameterTypes();
			final String funcId = functionSignature.getName();

			/*
			 * Determine compatible eager-eval function call if any, depending on number of
			 * primitive parameters against total number of parameters. (We do not check here
			 * whether all parameters have same primitive datatype in the function signature, as you
			 * can always use the EagerPrimitiveEval with supertype AttributeValue.)
			 */
			int primParamCount = 0;
			for (final Datatype<?> paramType : paramTypes)
			{
				if (!paramType.isBag())
				{
					primParamCount++;
				}
			}

			// parameters have same primitive datatype
			if (primParamCount == paramTypes.length)
			{
				// All parameters are primitive -> use EagerPrimitiveEval class
				if (!EagerPrimitiveEval.class.isAssignableFrom(this.getClass()))
				{
					throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerPrimitiveEval.class + " or any subclass instead when all parameters are primitive.");
				}
			} else if (primParamCount == 0)
			{
				// no primitive parameters -> all parameters are bag -> use EagerBagEval.class
				if (!EagerBagEval.class.isAssignableFrom(this.getClass()))
				{
					throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerBagEval.class + " or any subclass instead when all parameters are bag.");
				}
			} else
			{
				// parly primitive, partly bag -> use EagerPartlyBagEval
				/*
				 * For anonymous class used often to instantiate function call, call
				 * Class#getSuperClass() to get actual FunctionCall class implemented.
				 */
				if (!EagerPartlyBagEval.class.isAssignableFrom(this.getClass()))
				{
					throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerPartlyBagEval.class + " or any subclass instead when there are both primitive and bag parameters.");
				}
			}
			// END OF determining type of eager-eval function call
			this.numOfSameTypePrimitiveParamsBeforeBag = primParamCount;

			if (argArrayType == null)
			{
				throw new IllegalArgumentException("Function " + funcId + ": Undefined parameter array type for eager-evaluation function call");
			}

			this.parameterArrayClass = argArrayType;
			this.parameterClass = (Class<PARAM_T>) parameterArrayClass.getComponentType();
			this.argExpressions = args;
			this.indeterminateArgMessage = "Function " + funcId + ": indeterminate arg";
			// total number of arguments to the function
			this.totalArgCount = initialArgCount + remainingArgTypes.length;
		}
	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL
	 * their arguments' expressions to actual values, before the function can be evaluated. All
	 * arguments must be primitive values (primitive type).
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PARAM_T>
	 *            arg values' supertype. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	public static abstract class EagerPrimitiveEval<RETURN_T extends Expression.Value<?, RETURN_T>, PARAM_T extends AttributeValue<?>> extends EagerEval<RETURN_T, PARAM_T>
	{
		/**
		 * Instantiates Function call
		 * 
		 * @param functionSig
		 *            function signature
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
		protected EagerPrimitiveEval(FunctionSignature<RETURN_T> functionSig, Class<PARAM_T[]> argArrayType, List<Expression<?>> args, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, argArrayType, args, remainingArgTypes);
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
		protected abstract RETURN_T evaluate(PARAM_T[] args) throws IndeterminateEvaluationException;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall#evaluate(com.thalesgroup.
		 * authzforce .core.test.EvaluationCtx ,
		 * com.thalesgroup.authzforce.core.attr.AttributeValue[])
		 */
		@Override
		protected final RETURN_T evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
		{
			final PARAM_T[] finalArgs = parameterArrayClass.cast(Array.newInstance(parameterClass, totalArgCount));
			if (argExpressions != null)
			{
				try
				{
					evalPrimitiveArgs(argExpressions, context, finalArgs);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this.indeterminateArgMessage, Status.STATUS_PROCESSING_ERROR, e);
				}
			}

			if (remainingArgs != null)
			{
				/*
				 * remainingArgs (following the initial args, therefore starting at index =
				 * initialArgCount)
				 */
				for (int i = 0; i < remainingArgs.length; i++)
				{
					try
					{
						finalArgs[initialArgCount + i] = parameterClass.cast(remainingArgs[i]);
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException("Function " + this.funcSig.getName() + ": Type of arg #" + (initialArgCount + i) + " not valid: " + remainingArgs[i].getClass() + ". Required: " + parameterClass + ".", Status.STATUS_PROCESSING_ERROR);
					}
				}
			}

			return evaluate(finalArgs);
		}
	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL
	 * their arguments' expressions to actual values, before the function can be evaluated. All
	 * arguments must be bags, therefore no support for primitive values resolved at evaluation time
	 * (i.e. remaining args / evaluation-time args are not supported). If some ending parameters are
	 * primitive, use
	 * {@link com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPartlyBagEval}
	 * instead.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PARAM_T>
	 *            supertype of all primitive values in the bag(s) used as parameter(s). If these
	 *            parameter bags have different primitive datatypes, the supertype of all -
	 *            {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	public static abstract class EagerBagEval<RETURN_T extends Expression.Value<?, RETURN_T>, PARAM_T extends AttributeValue<PARAM_T>> extends EagerEval<RETURN_T, PARAM_T>
	{
		/**
		 * Instantiates Function call
		 * 
		 * @param functionSig
		 *            function signature
		 * 
		 * @param argArrayType
		 *            array class of which element type is <code>PARAM_T</code> (arg value
		 *            super-datatype). If argument expressions return different datatypes, the
		 *            supertype of all - {@link AttributeValue} - may be specified.
		 * @param args
		 *            arguments' Expressions
		 */
		protected EagerBagEval(FunctionSignature<RETURN_T> functionSig, Class<PARAM_T[]> argArrayType, List<Expression<?>> args) throws IllegalArgumentException
		{
			super(functionSig, argArrayType, args);
			if (argExpressions == null)
			{
				// all arg expressions are primitive
				throw new IllegalArgumentException("Function " + functionSig.getName() + ": no bag expression in arguments. At least one bag expression is required to use this type of FunctionCall: " + this.getClass());
			}

		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument
		 * expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param bagArgs
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract RETURN_T evaluate(PARAM_T[][] bagArgs) throws IndeterminateEvaluationException;

		@Override
		protected RETURN_T evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
		{

			/*
			 * No support for remainingArgs which would be primitive values, where as all arguments
			 * for EagerBagEval are supposed to be bags. Otherwise use EagerPartlyBagEval.
			 */
			assert remainingArgs == null;

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

			return evaluate(bagArgs);
		}

	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL
	 * their arguments' expressions to actual values, before the function can be evaluated. To be
	 * used only if there is a mix of primitive and bag arguments.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PARAM_T>
	 *            arg values' supertype. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	public static abstract class EagerPartlyBagEval<RETURN_T extends Expression.Value<?, RETURN_T>, PARAM_T extends AttributeValue<PARAM_T>> extends EagerEval<RETURN_T, PARAM_T>
	{
		private final int numOfArgExpressions;

		protected EagerPartlyBagEval(FunctionSignature<RETURN_T> functionSig, Class<PARAM_T[]> argArrayType, List<Expression<?>> args, Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, argArrayType, args, remainingArgTypes);
			if (argExpressions == null || (numOfArgExpressions = argExpressions.size()) <= numOfSameTypePrimitiveParamsBeforeBag)
			{
				// all arg expressions are primitive
				throw new IllegalArgumentException("Function " + funcSig.getName() + ": no bag expression in arguments. At least one bag expression is required to use this type of FunctionCall: " + this.getClass());
			}
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
		protected abstract RETURN_T evaluate(PARAM_T[] primArgsBeforeBag, PARAM_T[][] bagArgs, PARAM_T[] remainingArgs) throws IndeterminateEvaluationException;

		@Override
		protected final RETURN_T evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
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
			if (remainingArgs == null || remainingArgs.length == 0)
			{
				castRemainingArgs = null;
			} else
			{
				try
				{
					castRemainingArgs = parameterArrayClass.cast(remainingArgs);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException("Function " + funcSig.getName() + ": Type of remaining args (# >= " + initialArgCount + ") not valid: " + remainingArgs.getClass().getComponentType() + ". Required: " + parameterClass + ".", Status.STATUS_PROCESSING_ERROR);
				}
			}

			return evaluate(primArgsBeforeBag, bagArgs, castRemainingArgs);
		}

	}

}
