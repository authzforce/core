package com.thalesgroup.authzforce.core.func;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

/**
 * Superclass of "first-order" functions, "first-order" as opposed to "higher-order". (Higher-order
 * functions are implemented in separate classes.) Supplies several useful methods, making it easier
 * to implement a "first-order" function.
 * 
 * @param <T>
 *            type of function result, i.e. single-valued V or bag of Vs, where V extends
 *            AttributeValue
 */
public abstract class FirstOrderFunction<T extends ExpressionResult<? extends AttributeValue>> extends Function<T>
{
	protected final FunctionSignature signature;

	/**
	 * Constructor that creates a function from its signature definition
	 * 
	 * @param name
	 *            function name
	 * @param returnType
	 *            function return type
	 * @param varargs
	 *            true iff the function takes a variable number of arguments (like Java
	 *            {@link Method#isVarArgs()}
	 * @param parameterTypes
	 *            function parameter types
	 * 
	 * 
	 */
	public FirstOrderFunction(String name, DatatypeDef returnType, boolean varargs, DatatypeDef... parameterTypes)
	{
		super(name);
		this.signature = new FunctionSignature(name, returnType, varargs, parameterTypes);
	}

	@Override
	public final DatatypeDef getReturnType()
	{
		return signature.getReturnType();
	}

	/**
	 * Get parameter types
	 * 
	 * @return parameter types
	 */
	public final DatatypeDef[] getParameterTypes()
	{
		return signature.getParameterTypes();
	}

	// /**
	// * Evaluate primitive (single-valued) argument expression
	// *
	// * @param arg
	// * argument expression
	// * @param context
	// * context in which argument expression is evaluated
	// * @return result of evaluation
	// * @throws IndeterminateEvaluationException
	// * if no value returned from evaluation
	// */
	// protected final static <T extends AttributeValue> T eval(Expression<T> arg, EvaluationContext
	// context) throws IndeterminateEvaluationException
	// {
	// final T attrVal = arg.evaluate(context);
	// if (attrVal == null)
	// {
	// throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
	// }
	//
	// return attrVal;
	// }

	// /**
	// * Evaluates primitive (single-valued result) expressions in a given context and return a
	// array
	// * of the results.
	// *
	// * @param args
	// * expressions to be evaluated in <code>context</code>
	// * @param context
	// * the evaluation context
	// *
	// * @return array of results; each element is the result of the evaluation of the expression at
	// * the same position in <code>args</code>. Therefore the size of the array and
	// * <code>args</code> are the same.
	// * @throws IndeterminateEvaluationException
	// * if evaluation of one of the arg failed
	// */
	// public final static AttributeValue[] evalPrimitiveArgs(List<? extends Expression<?>> args,
	// EvaluationContext context) throws IndeterminateEvaluationException
	// {
	// return evalPrimitiveArgs(args, context, AttributeValue.class);
	// }

	/**
	 * Returns a function call for calling this function.
	 * 
	 * @param inputExpressions
	 *            function arguments (expressions)
	 * 
	 * @param evalTimeInputTypes
	 *            types of remaining inputs, if not all arguments could be specified in
	 *            <code>inputExpressions</code> because only the type is known. Therefore, only
	 *            their type is checked, and the actual expression may be specified later as last
	 *            parameter when calling
	 *            {@link FirstOrderFunctionCall#evaluate(EvaluationContext, boolean, AttributeValue...)}
	 *            at evaluation time, via the returned <code>FunctionCall</code>.
	 * @return Function call handle for calling this function which such inputs (with possible
	 *         changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	protected abstract FirstOrderFunctionCall<T> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputExpressions, DatatypeDef... evalTimeInputTypes) throws IllegalArgumentException;

	private static final DatatypeDef[] EMPTY_DATATYPE_DEF_ARRAY = new DatatypeDef[] {};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.test.func.Function#parseInputs(java.util.List)
	 */
	@Override
	public final FunctionCall<T> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputExpressions) throws IllegalArgumentException
	{
		return newCall(inputExpressions, EMPTY_DATATYPE_DEF_ARRAY);
	}
}
