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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.thalesgroup.authzforce.core.Expression.Datatype;
import com.thalesgroup.authzforce.core.Expression.Value;

/**
 * First-order function signature (name, return type, arity, parameter types)
 * 
 * @param <RETURN_T>
 *            function's return type
 */
public class FunctionSignature<RETURN_T extends Value<RETURN_T>>
{
	private static final IllegalArgumentException NULL_NAME_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined function name arg");
	private static final IllegalArgumentException NULL_RETURN_TYPE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined function return type arg");
	private static final IllegalArgumentException INVALID_VARARG_METHOD_PARAMETER_COUNT_EXCEPTION = new IllegalArgumentException("Invalid number of parameter types (0) for a varargs function. Such function requires at least one type for the final variable-length argument.");
	// function name
	private final String name;

	// the return type of the function
	private final Datatype<RETURN_T> returnType;

	// parameter types
	private final List<Datatype<?>> paramTypes;

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
	private final boolean isVarArgs;

	// cached method results
	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * Creates function signature
	 * 
	 * @param name
	 *            function name (e.g. XACML-defined URI)
	 * 
	 * @param returnType
	 *            function's return type
	 * @param parameterTypes
	 *            function parameter types, in order of parameter declaration
	 * @param varArgs
	 *            true iff the function takes a variable number of arguments (like Java
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
	 * @throws IllegalArgumentException
	 *             if function is Varargs but not parameter specified (
	 *             {@code varArgs == true && parameterTypes.length == 0})
	 */
	public FunctionSignature(String name, Datatype<RETURN_T> returnType, boolean varArgs, Datatype<?>... parameterTypes) throws IllegalArgumentException
	{
		if (name == null)
		{
			throw NULL_NAME_ARGUMENT_EXCEPTION;
		}

		if (returnType == null)
		{
			throw NULL_RETURN_TYPE_ARGUMENT_EXCEPTION;
		}

		if (varArgs && parameterTypes.length == 0)
		{
			throw INVALID_VARARG_METHOD_PARAMETER_COUNT_EXCEPTION;
		}

		this.name = name;
		this.returnType = returnType;
		this.paramTypes = Collections.unmodifiableList(Arrays.asList(parameterTypes));
		this.isVarArgs = varArgs;
	}

	/**
	 * Get function name
	 * 
	 * @return function name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get function return type
	 * 
	 * @return function return type
	 */
	public Datatype<RETURN_T> getReturnType()
	{
		return returnType;
	}

	/**
	 * Get function parameter types
	 * 
	 * @return function parameter types
	 */
	public List<Datatype<?>> getParameterTypes()
	{
		return paramTypes;
	}

	/**
	 * Returns {@code true} if this method was declared to take a variable number of arguments;
	 * returns {@code false} otherwise.
	 * 
	 * @return {@code true} iff this method was declared to take a variable number of arguments.
	 */
	public boolean isVarArgs()
	{
		return isVarArgs;
	}

	@Override
	public int hashCode()
	{
		// immutable class -> cache hashCode
		if (hashCode == 0)
		{
			hashCode = Objects.hash(name, returnType, isVarArgs, paramTypes);
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof FunctionSignature))
		{
			return false;
		}

		final FunctionSignature<?> other = (FunctionSignature<?>) obj;
		return isVarArgs == other.isVarArgs && name.equals(other.name) && this.paramTypes.equals(other.paramTypes) && returnType.equals(other.returnType);
	}

	@Override
	public String toString()
	{
		// immutable class -> cache result
		if (toString == null)
		{
			toString = "FunctionSignature [name=" + name + ", returnType=" + returnType + ", isVarArgs=" + isVarArgs + ", paramTypes=" + paramTypes + "]";
		}

		return toString;
	}

}
