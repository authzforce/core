/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.NumericValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * A class that implements all the numeric *-add functions (as opposed to date/time *-add-* functions).
 *
 * @param <AV>
 *            return and parameter type
 * 
 * @version $Id: $
 */
public final class NumericArithmeticFunction<AV extends NumericValue<?, AV>> extends SingleParameterTypedFirstOrderFunction<AV, AV>
{
	/**
	 * Standard integer-abs function URI
	 */
	public static final String NAME_INTEGER_ABS = XACML_NS_1_0 + "integer-abs";

	/**
	 * Standard double-abs function URI
	 */
	public static final String NAME_DOUBLE_ABS = XACML_NS_1_0 + "double-abs";

	/**
	 * Standard URI of function integer-add
	 */
	public static final String NAME_INTEGER_ADD = XACML_NS_1_0 + "integer-add";

	/**
	 * Standard URI of function double-add
	 */
	public static final String NAME_DOUBLE_ADD = XACML_NS_1_0 + "double-add";

	/**
	 * Standard URI for the integer-multiply function.
	 */
	public static final String NAME_INTEGER_MULTIPLY = XACML_NS_1_0 + "integer-multiply";

	/**
	 * Standard URI for the double-multiply function.
	 */
	public static final String NAME_DOUBLE_MULTIPLY = XACML_NS_1_0 + "double-multiply";

	/**
	 * Standard URI for the integer-subtract function.
	 */
	public static final String NAME_INTEGER_SUBTRACT = XACML_NS_1_0 + "integer-subtract";

	/**
	 * Standard URI for the integer-subtract function.
	 */
	public static final String NAME_DOUBLE_SUBTRACT = XACML_NS_1_0 + "double-subtract";

	/**
	 * Standard URI for the integer-divide function.
	 */
	public static final String NAME_INTEGER_DIVIDE = XACML_NS_1_0 + "integer-divide";

	/**
	 * Standard URI for the double-divide function.
	 */
	public static final String NAME_DOUBLE_DIVIDE = XACML_NS_1_0 + "double-divide";

	/**
	 * Standard URI for the integer-mod function.
	 */
	public static final String NAME_INTEGER_MOD = XACML_NS_1_0 + "integer-mod";

	/**
	 * Standard URI for the round function.
	 */
	public static final String NAME_ROUND = XACML_NS_1_0 + "round";

	/**
	 * Standard URI for the floor function.
	 */
	public static final String NAME_FLOOR = XACML_NS_1_0 + "floor";

	private static final IllegalArgumentException UNDEF_PARAMETER_TYPES_EXCEPTION = new IllegalArgumentException("Undefined function parameter types");

	private static <AV extends Value> List<Datatype<AV>> validate(final List<Datatype<AV>> paramTypes)
	{
		if (paramTypes == null || paramTypes.isEmpty())
		{
			throw UNDEF_PARAMETER_TYPES_EXCEPTION;
		}

		return paramTypes;
	}

	private interface StaticOperation<V extends NumericValue<?, V>>
	{
		V eval(Deque<V> args) throws IllegalArgumentException, ArithmeticException;
	}

	private static final class Call<V extends NumericValue<?, V>> extends EagerSinglePrimitiveTypeEval<V, V>
	{
		private final String invalidArgsErrMsg;
		private final StaticOperation<V> op;

		private Call(final SingleParameterTypedFirstOrderFunctionSignature<V, V> functionSig, final StaticOperation<V> op, final List<Expression<?>> args, final Datatype<?>[] remainingArgTypes)
				throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			this.op = op;
			this.invalidArgsErrMsg = "Function " + this.functionId + ": invalid argument(s)";
		}

		@Override
		protected V evaluate(final Deque<V> args) throws IndeterminateEvaluationException
		{
			try
			{
				return op.eval(args);
			} catch (IllegalArgumentException | ArithmeticException e)
			{
				throw new IndeterminateEvaluationException(invalidArgsErrMsg, StatusHelper.STATUS_PROCESSING_ERROR, e);
			}
		}
	}

	private final StaticOperation<AV> op;

	/**
	 * Creates a new Numeric Arithmetic function.
	 * 
	 * @param funcURI
	 *            function URI
	 * 
	 * @param paramTypes
	 *            parameter/return types (all the same)
	 * @param varArgs
	 *            whether this is a varargs function (like Java varargs method), i.e. last arg has variable-length
	 * 
	 */
	private NumericArithmeticFunction(final String funcURI, final boolean varArgs, final List<Datatype<AV>> paramTypes, final StaticOperation<AV> op) throws IllegalArgumentException
	{
		super(funcURI, validate(paramTypes).get(0), varArgs, paramTypes);
		this.op = op;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<AV> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/**
		 * TODO: optimize call to "add" (resp. "multiply") function call by checking all static/constant arguments and if there are more than one, pre-compute their sum (resp. product) and replace
		 * these arguments with one argument that is this sum (resp. product) in the function call. Indeed, 'add' function is commutative and (constant in upper case, variables in lower case): add(C1,
		 * C2, x, y...) = add(C1+C2, x, y...). Similarly, multiply(C1, C2, x, y...) = multiply(C1*C2, x, y...)
		 * 
		 */

		return new Call<>(functionSignature, op, argExpressions, remainingArgTypes);
	}

	private static final class AbsOperation<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{

		@Override
		public NAV eval(final Deque<NAV> args)
		{
			return args.getFirst().abs();
		}

	}

	private static final class AddOperation<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{
		@Override
		public NAV eval(final Deque<NAV> args)
		{
			final NAV arg0 = args.poll();
			return arg0.add(args);
		}

	}

	private static final class MultiplyOperation<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{

		@Override
		public NAV eval(final Deque<NAV> args)
		{
			final NAV arg0 = args.poll();
			return arg0.multiply(args);
		}

	}

	private static final class SubtractOperation<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{
		@Override
		public NAV eval(final Deque<NAV> args)
		{
			final NAV arg0 = args.poll();
			final NAV arg1 = args.poll();
			return arg0.subtract(arg1);
		}

	}

	private static final class DivideOperation<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{
		@Override
		public NAV eval(final Deque<NAV> args) throws ArithmeticException
		{
			final NAV arg0 = args.poll();
			final NAV arg1 = args.poll();
			return arg0.divide(arg1);
		}

	}

	private static final class IntegerModOperation implements StaticOperation<IntegerValue>
	{
		@Override
		public IntegerValue eval(final Deque<IntegerValue> args) throws ArithmeticException
		{
			final IntegerValue arg0 = args.poll();
			final IntegerValue arg1 = args.poll();
			return arg0.remainder(arg1);
		}
	}

	private static final StaticOperation<DoubleValue> FLOOR_OPERATION = new StaticOperation<DoubleValue>()
	{

		@Override
		public DoubleValue eval(final Deque<DoubleValue> args)
		{
			return args.getFirst().floor();
		}

	};

	private static final StaticOperation<DoubleValue> ROUND_OPERATION = new StaticOperation<DoubleValue>()
	{
		@Override
		public DoubleValue eval(final Deque<DoubleValue> args)
		{
			return args.getFirst().roundIEEE754Default();
		}
	};

	/**
	 * Numeric arithmetic function cluster
	 */
	public static final FunctionSet SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "numeric-arithmetic",
	//
			new NumericArithmeticFunction<>(NAME_INTEGER_ABS, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype()), new AbsOperation<IntegerValue>()),
			//
			new NumericArithmeticFunction<>(NAME_DOUBLE_ABS, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new AbsOperation<DoubleValue>()),
			//
			new NumericArithmeticFunction<>(NAME_INTEGER_ADD, true, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype(),
					StandardDatatypes.INTEGER_FACTORY.getDatatype()), new AddOperation<IntegerValue>()),
			//
			new NumericArithmeticFunction<>(NAME_DOUBLE_ADD, true, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
					StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new AddOperation<DoubleValue>()),
			//
			new NumericArithmeticFunction<>(NAME_INTEGER_MULTIPLY, true, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype(),
					StandardDatatypes.INTEGER_FACTORY.getDatatype()), new MultiplyOperation<IntegerValue>()),
			//
			new NumericArithmeticFunction<>(NAME_DOUBLE_MULTIPLY, true, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype(),
					StandardDatatypes.DOUBLE_FACTORY.getDatatype()), new MultiplyOperation<DoubleValue>()),
			//
			new NumericArithmeticFunction<>(NAME_INTEGER_SUBTRACT, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype()),
					new SubtractOperation<IntegerValue>()),
			//
			new NumericArithmeticFunction<>(NAME_DOUBLE_SUBTRACT, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype()),
					new SubtractOperation<DoubleValue>()),
			//
			new NumericArithmeticFunction<>(NAME_INTEGER_DIVIDE, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype()),
					new DivideOperation<IntegerValue>()),
			//
			new NumericArithmeticFunction<>(NAME_DOUBLE_DIVIDE, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype()),
					new DivideOperation<DoubleValue>()),
			//
			new NumericArithmeticFunction<>(NAME_INTEGER_MOD, false, Arrays.asList(StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype()),
					new IntegerModOperation()),
			//
			new NumericArithmeticFunction<>(NAME_FLOOR, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype()), FLOOR_OPERATION),
			//
			new NumericArithmeticFunction<>(NAME_ROUND, false, Arrays.asList(StandardDatatypes.DOUBLE_FACTORY.getDatatype()), ROUND_OPERATION));

}
