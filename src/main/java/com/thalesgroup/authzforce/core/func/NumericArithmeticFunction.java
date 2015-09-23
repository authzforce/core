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

import java.util.Arrays;
import java.util.List;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.NumericAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

/**
 * A class that implements all the numeric *-add functions (as opposed to date/time *-add-*
 * functions).
 * 
 * @param <AV>
 *            return and parameter type
 * 
 */
public abstract class NumericArithmeticFunction<AV extends NumericAttributeValue<?, AV>> extends FirstOrderFunction<AV>
{
	/**
	 * Standard integer-abs function URI
	 */
	public static final String NAME_INTEGER_ABS = FUNCTION_NS_1 + "integer-abs";

	/**
	 * Standard double-abs function URI
	 */
	public static final String NAME_DOUBLE_ABS = FUNCTION_NS_1 + "double-abs";

	/**
	 * Standard URI of function integer-add
	 */
	public static final String NAME_INTEGER_ADD = FUNCTION_NS_1 + "integer-add";

	/**
	 * Standard URI of function double-add
	 */
	public static final String NAME_DOUBLE_ADD = FUNCTION_NS_1 + "double-add";

	/**
	 * Standard URI for the integer-multiply function.
	 */
	public static final String NAME_INTEGER_MULTIPLY = FUNCTION_NS_1 + "integer-multiply";

	/**
	 * Standard URI for the double-multiply function.
	 */
	public static final String NAME_DOUBLE_MULTIPLY = FUNCTION_NS_1 + "double-multiply";

	/**
	 * Standard URI for the integer-subtract function.
	 */
	public static final String NAME_INTEGER_SUBTRACT = FUNCTION_NS_1 + "integer-subtract";

	/**
	 * Standard URI for the integer-subtract function.
	 */
	public static final String NAME_DOUBLE_SUBTRACT = FUNCTION_NS_1 + "double-subtract";

	/**
	 * Standard URI for the integer-divide function.
	 */
	public static final String NAME_INTEGER_DIVIDE = FUNCTION_NS_1 + "integer-divide";

	/**
	 * Standard URI for the double-divide function.
	 */
	public static final String NAME_DOUBLE_DIVIDE = FUNCTION_NS_1 + "double-divide";

	/**
	 * Standard URI for the integer-mod function.
	 */
	public static final String NAME_INTEGER_MOD = FUNCTION_NS_1 + "integer-mod";

	/**
	 * Standard URI for the round function.
	 */
	public static final String NAME_ROUND = FUNCTION_NS_1 + "round";

	/**
	 * Standard URI for the floor function.
	 */
	public static final String NAME_FLOOR = FUNCTION_NS_1 + "floor";

	protected final IndeterminateEvaluationException divideByZeroIndeterminateException = new IndeterminateEvaluationException("Function " + functionId + " : divisor is zero", Status.STATUS_PROCESSING_ERROR);

	private final Class<AV[]> parameterArrayClass;

	private static final Datatype<?>[] createGenericTypeArray(Datatype<?> paramType, int numOfRepetitions)
	{
		final Datatype<?>[] generics = new Datatype<?>[numOfRepetitions];
		Arrays.fill(generics, paramType);
		return generics;
	}

	/**
	 * Creates a new Numeric Arithmetic function.
	 * 
	 * @param funcURI
	 *            function URI
	 * 
	 * @param paramArrayType
	 *            function parameter array type
	 * @param paramType
	 *            parameter/return type
	 * @param arity
	 *            number of arguments including the variable-length argument if {@code varArgs}.
	 *            Remember that to define a function that takes N or more args, you define a varargs
	 *            function of N+1 args where the (N+1-arg) is the variable-length parameter, as it
	 *            can have a length of 0 or more. Therefore, the arity is N+1 in this case.
	 * @param varArgs
	 *            whether this is a varargs function (like Java varargs method), i.e. last arg has
	 *            variable-length
	 * 
	 */
	public NumericArithmeticFunction(String funcURI, Datatype<AV> paramType, int arity, boolean varArgs, Class<AV[]> paramArrayType)
	{
		super(funcURI, paramType, varArgs, createGenericTypeArray(paramType, arity));
		this.parameterArrayClass = paramArrayType;
	}

	abstract protected AV eval(AV[] args) throws IndeterminateEvaluationException;

	@Override
	protected final FirstOrderFunctionCall<AV> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/**
		 * TODO: optimize call to "add" (resp. "multiply") function call by checking all
		 * static/constant arguments and if there are more than one, pre-compute their sum (resp.
		 * product) and replace these arguments with one argument that is this sum (resp. product)
		 * in the function call. Indeed, 'add' function is commutative and (constant in upper case,
		 * variables in lower case): add(C1, C2, x, y...) = add(C1+C2, x, y...). Similarly,
		 * multiply(C1, C2, x, y...) = multiply(C1*C2, x, y...)
		 * 
		 */

		return new EagerPrimitiveEval<AV, AV>(signature, parameterArrayClass, argExpressions, remainingArgTypes)
		{

			@Override
			protected final AV evaluate(AV[] args) throws IndeterminateEvaluationException
			{
				return eval(args);
			}

		};
	}

	private static class Abs<NAV extends NumericAttributeValue<?, NAV>> extends NumericArithmeticFunction<NAV>
	{

		private Abs(String funcURI, Datatype<NAV> paramType, Class<NAV[]> paramArrayType)
		{
			super(funcURI, paramType, 1, false, paramArrayType);
		}

		@Override
		protected final NAV eval(NAV[] args)
		{
			return args[0].abs();
		}

	}

	private static class Add<NAV extends NumericAttributeValue<?, NAV>> extends NumericArithmeticFunction<NAV>
	{

		private Add(String funcURI, Datatype<NAV> paramType, Class<NAV[]> paramArrayType)
		{
			super(funcURI, paramType, 3, true, paramArrayType);
		}

		@Override
		protected final NAV eval(NAV[] args)
		{
			args[0].add(args, 1);
			return args[0];
		}

	}

	private static class Multiply<NAV extends NumericAttributeValue<?, NAV>> extends NumericArithmeticFunction<NAV>
	{

		private Multiply(String funcURI, Datatype<NAV> paramType, Class<NAV[]> paramArrayType)
		{
			super(funcURI, paramType, 3, true, paramArrayType);
		}

		@Override
		protected final NAV eval(NAV[] args)
		{
			args[0].multiply(args, 1);
			return args[0];
		}

	}

	private static class Subtract<NAV extends NumericAttributeValue<?, NAV>> extends NumericArithmeticFunction<NAV>
	{

		private Subtract(String funcURI, Datatype<NAV> paramType, Class<NAV[]> paramArrayType)
		{
			super(funcURI, paramType, 2, false, paramArrayType);
		}

		@Override
		protected final NAV eval(NAV[] args)
		{
			args[0].subtract(args[1]);
			return args[0];
		}

	}

	private static class Divide<NAV extends NumericAttributeValue<?, NAV>> extends NumericArithmeticFunction<NAV>
	{

		private Divide(String funcURI, Datatype<NAV> paramType, Class<NAV[]> paramArrayType)
		{
			super(funcURI, paramType, 2, false, paramArrayType);
		}

		@Override
		protected final NAV eval(NAV[] args) throws IndeterminateEvaluationException
		{
			try
			{
				return args[0].divide(args[1]);
			} catch (ArithmeticException e)
			{
				throw divideByZeroIndeterminateException;
			}
		}

	}

	private static class IntegerMod extends NumericArithmeticFunction<IntegerAttributeValue>
	{

		public IntegerMod()
		{
			super(NAME_INTEGER_MOD, DatatypeConstants.INTEGER.TYPE, 2, false, IntegerAttributeValue[].class);
		}

		@Override
		protected final IntegerAttributeValue eval(IntegerAttributeValue[] args) throws IndeterminateEvaluationException
		{
			final IntegerAttributeValue remainder;
			try
			{
				remainder = args[0].remainder(args[1]);
			} catch (ArithmeticException e)
			{
				throw divideByZeroIndeterminateException;
			}

			return remainder;
		}
	}

	private static class Floor extends NumericArithmeticFunction<DoubleAttributeValue>
	{

		private Floor()
		{
			super(NAME_FLOOR, DatatypeConstants.DOUBLE.TYPE, 1, false, DoubleAttributeValue[].class);
		}

		@Override
		protected final DoubleAttributeValue eval(DoubleAttributeValue[] args)
		{
			return args[0].floor();
		}

	}

	private static class Round extends NumericArithmeticFunction<DoubleAttributeValue>
	{
		private Round()
		{
			super(NAME_ROUND, DatatypeConstants.DOUBLE.TYPE, 1, false, DoubleAttributeValue[].class);
		}

		@Override
		protected final DoubleAttributeValue eval(DoubleAttributeValue[] args) throws IndeterminateEvaluationException
		{
			return args[0].roundIEEE754Default();
		}
	}

	/**
	 * Numeric arithmetic function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "numeric-arithmetic",
	//
			new Abs<>(NAME_INTEGER_ABS, DatatypeConstants.INTEGER.TYPE, IntegerAttributeValue[].class),
			//
			new Abs<>(NAME_DOUBLE_ABS, DatatypeConstants.DOUBLE.TYPE, DoubleAttributeValue[].class),
			//
			new Add<>(NAME_INTEGER_ADD, DatatypeConstants.INTEGER.TYPE, IntegerAttributeValue[].class),
			//
			new Add<>(NAME_DOUBLE_ADD, DatatypeConstants.DOUBLE.TYPE, DoubleAttributeValue[].class),
			//
			new Multiply<>(NAME_INTEGER_MULTIPLY, DatatypeConstants.INTEGER.TYPE, IntegerAttributeValue[].class),
			//
			new Multiply<>(NAME_DOUBLE_MULTIPLY, DatatypeConstants.DOUBLE.TYPE, DoubleAttributeValue[].class),
			//
			new Subtract<>(NAME_INTEGER_SUBTRACT, DatatypeConstants.INTEGER.TYPE, IntegerAttributeValue[].class),
			//
			new Subtract<>(NAME_DOUBLE_SUBTRACT, DatatypeConstants.DOUBLE.TYPE, DoubleAttributeValue[].class),
			//
			new Divide<>(NAME_INTEGER_DIVIDE, DatatypeConstants.INTEGER.TYPE, IntegerAttributeValue[].class),
			//
			new Divide<>(NAME_DOUBLE_DIVIDE, DatatypeConstants.DOUBLE.TYPE, DoubleAttributeValue[].class),
			//
			new IntegerMod(),
			//
			new Floor(),
			//
			new Round());

}
