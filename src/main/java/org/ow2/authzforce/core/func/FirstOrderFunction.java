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
import java.util.List;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.value.BagDatatype;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;

import com.sun.xacml.Function;

/**
 * Superclass of "first-order" functions, "first-order" as opposed to "higher-order". (Higher-order functions are implemented in separate classes.) Supplies
 * several useful methods, making it easier to implement a "first-order" function.
 * 
 * @param <RETURN>
 *            function return type
 */
public abstract class FirstOrderFunction<RETURN extends Value> extends Function<RETURN>
{
	private static final Datatype<?>[] EMPTY_DATATYPE_DEF_ARRAY = new Datatype<?>[] {};

	private FirstOrderFunction(String name)
	{
		super(name);
	}

	/**
	 * Get parameter types
	 * 
	 * @return parameter types
	 */
	public abstract List<? extends Datatype<?>> getParameterTypes();

	/**
	 * Returns a function call for calling this function.
	 * 
	 * @param functionSignature
	 *            function signature
	 * 
	 * @param argExpressions
	 *            function arguments (expressions)
	 * 
	 * @param remainingArgTypes
	 *            types of remaining inputs to be passed only at request evaluation time, if not all arguments are specified in <code>argExpressions</code>.
	 *            Therefore, only their type is checked at this point. The actual argument values will be passed as last parameters when calling
	 *            {@link FirstOrderFunctionCall#evaluate(EvaluationContext, boolean, com.thalesgroup.authzforce.core.AttributeValue...)} at request evaluation
	 *            time, via the returned <code>FunctionCall</code>.
	 * @return Function call handle for calling this function which such inputs (with possible changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	protected abstract FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
			throws IllegalArgumentException;

	@Override
	public final FunctionCall<RETURN> newCall(List<Expression<?>> argExpressions) throws IllegalArgumentException
	{
		return newCall(argExpressions, EMPTY_DATATYPE_DEF_ARRAY);
	}

	/**
	 * Superclass of "first-order" functions of single-type parameters, i.e. whose all parameters have the same datatype. Supplies several useful methods,
	 * making it easier to implement such "first-order" function.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * @param <PARAM_T>
	 *            single/common parameter type
	 */
	public static abstract class SingleParameterTyped<RETURN_T extends Value, PARAM_T extends Value> extends FirstOrderFunction<RETURN_T>
	{
		protected FunctionSignature.SingleParameterTyped<RETURN_T, PARAM_T> functionSignature;

		/**
		 * Constructor that creates a function from its signature definition
		 * 
		 * @param name
		 *            function name
		 * @param returnType
		 *            function return type
		 * @param varargs
		 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}
		 * @param parameterTypes
		 *            function parameter types. Note: the "? extends" allows to use {@link BagDatatype}.
		 * @throws IllegalArgumentException
		 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 1 })
		 * 
		 */
		public SingleParameterTyped(String name, Datatype<RETURN_T> returnType, boolean varargs, List<? extends Datatype<PARAM_T>> parameterTypes)
				throws IllegalArgumentException
		{
			super(name);
			this.functionSignature = new FunctionSignature.SingleParameterTyped<>(name, returnType, varargs, parameterTypes);
		}

		@Override
		public final Datatype<RETURN_T> getReturnType()
		{
			return functionSignature.getReturnType();
		}

		/**
		 * Get parameter types
		 * 
		 * @return parameter types
		 */
		@Override
		public final List<? extends Datatype<?>> getParameterTypes()
		{
			return functionSignature.getParameterTypes();
		}
	}

	/**
	 * Superclass of "first-order" functions of multi-type parameters, i.e. whose parameters have different datatypes (at least two different). Supplies several
	 * useful methods, making it easier to implement such "first-order" function.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 */
	public static abstract class MultiParameterTyped<RETURN_T extends Value> extends FirstOrderFunction<RETURN_T>
	{
		protected final FunctionSignature<RETURN_T> functionSignature;

		/**
		 * Constructor that creates a function from its signature definition
		 * 
		 * @param name
		 *            function name
		 * @param returnType
		 *            function return type
		 * @param varargs
		 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}
		 * @param parameterTypes
		 *            function parameter types
		 * @throws IllegalArgumentException
		 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 2 })
		 * 
		 */
		public MultiParameterTyped(String name, Datatype<RETURN_T> returnType, boolean varargs, List<? extends Datatype<?>> parameterTypes)
		{
			super(name);
			this.functionSignature = new FunctionSignature.MultiParameterTyped<>(name, returnType, varargs, parameterTypes);
		}

		@Override
		public final Datatype<RETURN_T> getReturnType()
		{
			return functionSignature.getReturnType();
		}

		/**
		 * Get parameter types
		 * 
		 * @return parameter types
		 */
		@Override
		public final List<? extends Datatype<?>> getParameterTypes()
		{
			return functionSignature.getParameterTypes();
		}
	}
}
