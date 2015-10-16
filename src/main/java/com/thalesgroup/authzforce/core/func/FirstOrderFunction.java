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

import java.lang.reflect.Method;
import java.util.List;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;

/**
 * Superclass of "first-order" functions, "first-order" as opposed to "higher-order". (Higher-order
 * functions are implemented in separate classes.) Supplies several useful methods, making it easier
 * to implement a "first-order" function.
 * 
 * @param <RETURN>
 *            function return type
 */
public abstract class FirstOrderFunction<RETURN extends Expression.Value<RETURN>> extends Function<RETURN>
{
	protected final FunctionSignature<RETURN> signature;

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
	public FirstOrderFunction(String name, Datatype<RETURN> returnType, boolean varargs, Datatype<?>... parameterTypes)
	{
		super(name);
		this.signature = new FunctionSignature<>(name, returnType, varargs, parameterTypes);
	}

	@Override
	public final Datatype<RETURN> getReturnType()
	{
		return signature.getReturnType();
	}

	/**
	 * Get parameter types
	 * 
	 * @return parameter types
	 */
	public final Datatype<?>[] getParameterTypes()
	{
		return signature.getParameterTypes();
	}

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
	 *            {@link FirstOrderFunctionCall#evaluate(EvaluationContext, boolean, com.thalesgroup.authzforce.core.AttributeValue...)}
	 *            at evaluation time, via the returned <code>FunctionCall</code>.
	 * @return Function call handle for calling this function which such inputs (with possible
	 *         changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	protected abstract FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> inputExpressions, Datatype<?>... evalTimeInputTypes) throws IllegalArgumentException;

	private static final Datatype<?>[] EMPTY_DATATYPE_DEF_ARRAY = new Datatype<?>[] {};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.test.func.Function#parseInputs(java.util.List)
	 */
	@Override
	public final FunctionCall<RETURN> newCall(List<Expression<?>> inputExpressions) throws IllegalArgumentException
	{
		return newCall(inputExpressions, EMPTY_DATATYPE_DEF_ARRAY);
	}
}
