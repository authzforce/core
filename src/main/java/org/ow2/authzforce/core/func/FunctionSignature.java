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
package org.ow2.authzforce.core.func;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.ow2.authzforce.core.value.BagDatatype;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;

/**
 * First-order function signature (name, return type, arity, parameter types)
 * 
 * @param <RETURN_T>
 *            function's return type
 */
public abstract class FunctionSignature<RETURN_T extends Value>
{
	private static final IllegalArgumentException NULL_NAME_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined function name arg");
	private static final IllegalArgumentException NULL_RETURN_TYPE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined function return type arg");
	private static final IllegalArgumentException UNDEF_PARAMETER_TYPES_EXCEPTION = new IllegalArgumentException("Undefined function parameter types");

	// function name
	protected final String name;

	// the return type of the function
	protected final Datatype<RETURN_T> returnType;

	/**
	 * Is the last parameter specified in <code>paramTypes</code> considered as variable-length (like Java {@link Method#isVarArgs()}), i.e. taking a variable
	 * number of arguments (0 or more) of the specified paramTypes[n-1] with n the size of paramTypes). In the following examples, '...' means varargs like in
	 * Java:
	 * <p>
	 * Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}, isVarargs=true
	 * </p>
	 * <p>
	 * Example 2: or(boolean...) -> paramTypes={boolean}, isVarargs=true (As you can see, isVarargs=true really means 0 or more args; indeed, the or function
	 * can take 0 parameter according to spec)
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
	 * Example 6: date-add-yearMonthDuration(date, yearMonthDuration) -> paramTypes={date, yearMonthDuration}, isVarargs=false
	 * </p>
	 */
	protected final boolean isVarArgs;

	// cached method results
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
	 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}, i.e. the final type in
	 *            <code>paramTypes</code> can be repeated 0 or more times to match a variable-length argument
	 *            <p>
	 *            Examples with varargs=true ('...' means varargs like in Java):
	 *            </p>
	 *            <p>
	 *            Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}
	 *            </p>
	 *            <p>
	 *            Example 2: or(boolean...) -> paramTypes={boolean} (As you can see, isVarargs=true really means 0 or more args; indeed, the or function can
	 *            take 0 parameter according to spec)
	 *            </p>
	 *            <p>
	 *            Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}
	 *            </p>
	 * @throws IllegalArgumentException
	 *             if ( {@code name == null || returnType == null })
	 */
	private FunctionSignature(String name, Datatype<RETURN_T> returnType, boolean varArgs) throws IllegalArgumentException
	{
		if (name == null)
		{
			throw NULL_NAME_ARGUMENT_EXCEPTION;
		}

		if (returnType == null)
		{
			throw NULL_RETURN_TYPE_ARGUMENT_EXCEPTION;
		}

		this.name = name;
		this.returnType = returnType;
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
	 * Returns {@code true} if this method was declared to take a variable number of arguments; returns {@code false} otherwise.
	 * 
	 * @return {@code true} iff this method was declared to take a variable number of arguments.
	 */
	public boolean isVarArgs()
	{
		return isVarArgs;
	}

	/**
	 * Get function parameter types
	 * 
	 * @return function parameter types
	 */
	public abstract List<? extends Datatype<?>> getParameterTypes();

	@Override
	public String toString()
	{
		// immutable class -> cache result
		if (toString == null)
		{
			final StringBuffer strBuf = new StringBuffer(returnType + " " + name + "(");
			final Iterator<? extends Datatype<?>> paramTypesIterator = this.getParameterTypes().iterator();
			// at least one parameter, we make sure of that in the constructor
			strBuf.append(paramTypesIterator.next());
			while (paramTypesIterator.hasNext())
			{
				strBuf.append(',').append(paramTypesIterator.next());
			}

			if (isVarArgs)
			{
				strBuf.append("...");
			}

			strBuf.append(')');
			toString = strBuf.toString();
		}

		return toString;
	}

	/**
	 * First-order function signature whose every parameters has the same datatype
	 * 
	 * @param <RETURN>
	 *            function's return type
	 * @param <PARAM>
	 *            common parameter type
	 */
	public static class SingleParameterTyped<RETURN extends Value, PARAM extends Value> extends FunctionSignature<RETURN>
	{
		private transient volatile int hashCode = 0; // Effective Java - Item 9

		private final List<? extends Datatype<PARAM>> paramTypes;

		/**
		 * Creates function signature
		 * 
		 * @param name
		 *            function name (e.g. XACML-defined URI)
		 * 
		 * @param returnType
		 *            function's return type
		 * @param parameterTypes
		 *            function parameter types. Note: the "? extends" allows to use {@link BagDatatype} as parameterType
		 * @param varArgs
		 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}, i.e. the final type in
		 *            <code>paramTypes</code> can be repeated 0 or more times to match a variable-length argument
		 *            <p>
		 *            Examples with varargs=true ('...' means varargs like in Java):
		 *            </p>
		 *            <p>
		 *            Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}
		 *            </p>
		 *            <p>
		 *            Example 2: or(boolean...) -> paramTypes={boolean} (As you can see, isVarargs=true really means 0 or more args; indeed, the or function can
		 *            take 0 parameter according to spec)
		 *            </p>
		 *            <p>
		 *            Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}
		 *            </p>
		 * @throws IllegalArgumentException
		 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.isEmpty()})
		 */
		SingleParameterTyped(String name, Datatype<RETURN> returnType, boolean varArgs, List<? extends Datatype<PARAM>> parameterTypes)
				throws IllegalArgumentException
		{
			super(name, returnType, varArgs);
			if (parameterTypes == null)
			{
				throw UNDEF_PARAMETER_TYPES_EXCEPTION;
			}

			if (parameterTypes.isEmpty())
			{
				throw new IllegalArgumentException("Invalid number of function parameters (" + parameterTypes.size() + ") for first-order function (" + name
						+ "). Required: >= 1.");
			}

			this.paramTypes = Collections.unmodifiableList(parameterTypes);
		}

		/**
		 * Get single/common parameter datatype
		 * 
		 * @return parameter datatype
		 */
		public Datatype<PARAM> getParameterType()
		{
			// the constructor made sure that paramTypes is not empty
			return this.paramTypes.get(0);
		}

		/**
		 * Get function parameter types
		 * 
		 * @return function parameter types
		 */
		@Override
		public List<? extends Datatype<?>> getParameterTypes()
		{
			return this.paramTypes;
		}

		@Override
		public int hashCode()
		{
			// immutable class -> cache hashCode
			if (hashCode == 0)
			{
				hashCode = Objects.hash(name, returnType, isVarArgs, paramTypes.get(0));
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

			if (!(obj instanceof SingleParameterTyped))
			{
				return false;
			}

			final SingleParameterTyped<?, ?> other = (SingleParameterTyped<?, ?>) obj;
			return isVarArgs == other.isVarArgs && name.equals(other.name) && returnType.equals(other.returnType)
					&& this.paramTypes.get(0).equals(other.paramTypes.get(0));
		}
	}

	/**
	 * First-order function signature whose parameters have (at least two) different datatypes
	 * 
	 * @param <RETURN>
	 *            function's return type
	 */
	public static class MultiParameterTyped<RETURN extends Value> extends FunctionSignature<RETURN>
	{

		private transient volatile int hashCode = 0; // Effective Java - Item 9

		private final List<? extends Datatype<?>> paramTypes;

		/**
		 * 
		 * @param name
		 * @param returnType
		 * @param varArgs
		 * @param parameterTypes
		 * @throws IllegalArgumentException
		 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 2 })
		 */
		MultiParameterTyped(String name, Datatype<RETURN> returnType, boolean varArgs, List<? extends Datatype<?>> parameterTypes)
				throws IllegalArgumentException
		{
			super(name, returnType, varArgs);
			if (parameterTypes == null)
			{
				throw UNDEF_PARAMETER_TYPES_EXCEPTION;
			}

			if (parameterTypes.size() < 2)
			{
				throw new IllegalArgumentException("Invalid number of function parameters (" + parameterTypes.size() + ") for multi-parameter-typed function ("
						+ name + "). Required: >= " + 2 + ".");
			}
			this.paramTypes = Collections.unmodifiableList(parameterTypes);
		}

		/**
		 * Get function parameter types
		 * 
		 * @return function parameter types
		 */
		@Override
		public List<? extends Datatype<?>> getParameterTypes()
		{
			return this.paramTypes;
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

			if (!(obj instanceof MultiParameterTyped))
			{
				return false;
			}

			final MultiParameterTyped<?> other = (MultiParameterTyped<?>) obj;
			return isVarArgs == other.isVarArgs && name.equals(other.name) && returnType.equals(other.returnType) && this.paramTypes.equals(other.paramTypes);
		}
	}
}
