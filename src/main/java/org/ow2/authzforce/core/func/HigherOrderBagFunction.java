package org.ow2.authzforce.core.func;

import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.expression.VariableReference;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;

import com.sun.xacml.Function;

/**
 * Higher-order bag function
 * 
 * @param <RETURN_T>
 *            return type
 * @param <SUB_RETURN_PRIMITIVE_T>
 *            sub-function's return (primitive) type. Only functions returning primitive type of result are compatible with higher-order functions here.
 */
public abstract class HigherOrderBagFunction<RETURN_T extends Value, SUB_RETURN_PRIMITIVE_T extends AttributeValue> extends Function<RETURN_T>
{

	private final Datatype<RETURN_T> returnType;

	private final Datatype<?> subFuncReturnType;

	/**
	 * Instantiates higher-order bag function
	 * 
	 * @param functionId
	 *            function ID
	 * @param returnType
	 *            function's return type
	 * @param subFunctionReturnType
	 *            sub-function's return datatype; may be null to indicate any datatype (e.g. map function's sub-function return datatype can be any primitive
	 *            type)
	 */
	HigherOrderBagFunction(String functionId, Datatype<RETURN_T> returnType, Datatype<?> subFunctionReturnType)
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
	 * Creates function call from sub-function definition and all inputs to higher-order function To be overriden by OneBagOnlyFunctions (any-of/all-of)
	 * 
	 * @param boolSubFunc
	 *            boolean sub-function
	 * @param subFuncArgTypes
	 *            sub-function argument types
	 * @param inputs
	 *            all inputs
	 * @return function call
	 */
	protected abstract FunctionCall<RETURN_T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc,
			List<Expression<?>> inputsAfterSubFunc);

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
		 * Check whether it is a FirstOrderFunction because it is the only type of function for which we have a generic way to validate argument types as done
		 * later below
		 */
		if (!(inputFunc instanceof FirstOrderFunction))
		{
			throw new IllegalArgumentException(this + ": Invalid function in first argument: " + inputFunc + " is not supported as such argument");
		}

		final Datatype<?> inputFuncReturnType = inputFunc.getReturnType();
		if (subFuncReturnType == null)
		{
			/*
			 * sub-function's return type can be any primitive datatype; check at least it is primitive
			 */
			if (inputFuncReturnType.getTypeParameter() != null)
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFuncReturnType
						+ " (bag type). Required: any primitive type");
			}
		} else
		{
			if (!inputFuncReturnType.equals(subFuncReturnType))
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFuncReturnType + ". Required: "
						+ subFuncReturnType);
			}
		}

		// so now we know we have a boolean FirstOrderFunction
		@SuppressWarnings("unchecked")
		final FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc = (FirstOrderFunction<SUB_RETURN_PRIMITIVE_T>) inputFunc;

		return createFunctionCallFromSubFunction(subFunc, inputs.subList(1, numInputs));
	}

	protected abstract void checkNumberOfArgs(int numInputs);
}