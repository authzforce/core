package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.List;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.NumericAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;

/**
 * A class that implements all the numeric *-add functions (as opposed to date/time *-add-*
 * functions).
 * 
 * @param <T>
 *            type of returned and input attribute values
 * 
 */
public abstract class NumericArithmeticFunction<T extends NumericAttributeValue<?, T>> extends BaseFunction<PrimitiveResult<T>>
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
	 * Standard identifier for the integer-multiply function.
	 */
	public static final String NAME_INTEGER_MULTIPLY = FUNCTION_NS_1 + "integer-multiply";

	/**
	 * Standard identifier for the double-multiply function.
	 */
	public static final String NAME_DOUBLE_MULTIPLY = FUNCTION_NS_1 + "double-multiply";

	/**
	 * Standard identifier for the integer-subtract function.
	 */
	public static final String NAME_INTEGER_SUBTRACT = FUNCTION_NS_1 + "integer-subtract";

	/**
	 * Standard identifier for the integer-subtract function.
	 */
	public static final String NAME_DOUBLE_SUBTRACT = FUNCTION_NS_1 + "double-subtract";

	/**
	 * Standard identifier for the integer-divide function.
	 */
	public static final String NAME_INTEGER_DIVIDE = FUNCTION_NS_1 + "integer-divide";

	/**
	 * Standard identifier for the double-divide function.
	 */
	public static final String NAME_DOUBLE_DIVIDE = FUNCTION_NS_1 + "double-divide";

	/**
	 * Standard identifier for the integer-mod function.
	 */
	public static final String NAME_INTEGER_MOD = FUNCTION_NS_1 + "integer-mod";

	/**
	 * Standard identifier for the round function.
	 */
	public static final String NAME_ROUND = FUNCTION_NS_1 + "round";

	/**
	 * Standard identifier for the floor function.
	 */
	public static final String NAME_FLOOR = FUNCTION_NS_1 + "floor";

	protected final IndeterminateEvaluationException divideByZeroIndeterminateException = new IndeterminateEvaluationException("Function " + functionId + " : divisor is zero", Status.STATUS_PROCESSING_ERROR);

	/**
	 * Numeric arithmetic function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "numeric-arithmetic",
	//
			new Abs<>(NAME_INTEGER_ABS, IntegerAttributeValue.identifier, IntegerAttributeValue[].class),
			//
			new Abs<>(NAME_DOUBLE_ABS, DoubleAttributeValue.identifier, DoubleAttributeValue[].class),
			//
			new Add<>(NAME_INTEGER_ADD, IntegerAttributeValue.identifier, IntegerAttributeValue[].class),
			//
			new Add<>(NAME_DOUBLE_ADD, DoubleAttributeValue.identifier, DoubleAttributeValue[].class),
			//
			new Multiply<>(NAME_INTEGER_MULTIPLY, IntegerAttributeValue.identifier, IntegerAttributeValue[].class),
			//
			new Multiply<>(NAME_DOUBLE_MULTIPLY, DoubleAttributeValue.identifier, DoubleAttributeValue[].class),
			//
			new Subtract<>(NAME_INTEGER_SUBTRACT, IntegerAttributeValue.identifier, IntegerAttributeValue[].class),
			//
			new Subtract<>(NAME_DOUBLE_SUBTRACT, DoubleAttributeValue.identifier, DoubleAttributeValue[].class),
			//
			new Divide<>(NAME_INTEGER_DIVIDE, IntegerAttributeValue.identifier, IntegerAttributeValue[].class),
			//
			new Divide<>(NAME_DOUBLE_DIVIDE, DoubleAttributeValue.identifier, DoubleAttributeValue[].class),
			//
			new IntegerMod(),
			//
			new Floor(),
			//
			new Round());

	private final Class<T[]> parameterArrayClass;

	private static final DatatypeDef[] createGenericTypeArray(DatatypeDef paramTypeURI, int numOfRepetitions)
	{
		final DatatypeDef[] generics = new DatatypeDef[numOfRepetitions];
		Arrays.fill(generics, paramTypeURI);
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
	public NumericArithmeticFunction(String funcURI, DatatypeDef paramType, int arity, boolean varArgs, Class<T[]> paramArrayType)
	{
		super(funcURI, paramType, varArgs, createGenericTypeArray(paramType, arity));
		this.parameterArrayClass = paramArrayType;
	}

	abstract protected T eval(T[] args) throws IndeterminateEvaluationException;

	@Override
	protected final Call getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes) throws IllegalArgumentException
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

		return new EagerPrimitiveEvalCall<T>(parameterArrayClass, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected final PrimitiveResult<T> evaluate(T[] args) throws IndeterminateEvaluationException
			{
				return new PrimitiveResult<>(eval(args), returnType);
			}

		};
	}

	private static class Abs<T extends NumericAttributeValue<?, T>> extends NumericArithmeticFunction<T>
	{

		private Abs(String funcURI, String paramTypeURI, Class<T[]> paramArrayType)
		{
			super(funcURI, new DatatypeDef(paramTypeURI), 1, false, paramArrayType);
		}

		@Override
		protected final T eval(T[] args)
		{
			return args[0].abs();
		}

	}

	private static class Add<T extends NumericAttributeValue<?, T>> extends NumericArithmeticFunction<T>
	{

		private Add(String funcURI, String paramTypeURI, Class<T[]> paramArrayType)
		{
			super(funcURI, new DatatypeDef(paramTypeURI), 3, true, paramArrayType);
		}

		@Override
		protected final T eval(T[] args)
		{
			args[0].add(args, 1);
			return args[0];
		}

	}

	private static class Multiply<T extends NumericAttributeValue<?, T>> extends NumericArithmeticFunction<T>
	{

		private Multiply(String funcURI, String paramTypeURI, Class<T[]> paramArrayType)
		{
			super(funcURI, new DatatypeDef(paramTypeURI), 3, true, paramArrayType);
		}

		@Override
		protected final T eval(T[] args)
		{
			args[0].multiply(args, 1);
			return args[0];
		}

	}

	private static class Subtract<T extends NumericAttributeValue<?, T>> extends NumericArithmeticFunction<T>
	{

		private Subtract(String funcURI, String paramTypeURI, Class<T[]> paramArrayType)
		{
			super(funcURI, new DatatypeDef(paramTypeURI), 2, false, paramArrayType);
		}

		@Override
		protected final T eval(T[] args)
		{
			args[0].subtract(args[1]);
			return args[0];
		}

	}

	private static class Divide<T extends NumericAttributeValue<?, T>> extends NumericArithmeticFunction<T>
	{

		private Divide(String funcURI, String paramTypeURI, Class<T[]> paramArrayType)
		{
			super(funcURI, new DatatypeDef(paramTypeURI), 2, false, paramArrayType);
		}

		@Override
		protected final T eval(T[] args) throws IndeterminateEvaluationException
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
			super(NAME_INTEGER_MOD, IntegerAttributeValue.TYPE, 2, false, IntegerAttributeValue[].class);
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
			super(NAME_FLOOR, DoubleAttributeValue.TYPE, 1, false, DoubleAttributeValue[].class);
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
			super(NAME_ROUND, DoubleAttributeValue.TYPE, 1, false, DoubleAttributeValue[].class);
		}

		@Override
		protected final DoubleAttributeValue eval(DoubleAttributeValue[] args) throws IndeterminateEvaluationException
		{
			return args[0].roundIEEE754Default();
		}
	}

}
