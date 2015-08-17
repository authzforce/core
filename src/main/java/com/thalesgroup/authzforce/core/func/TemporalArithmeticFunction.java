package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BaseTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;

/**
 * Implements generic match functions taking parameters of possibly different types.
 * 
 * @param <T>
 *            type of first parameter and returned value (date/time)
 * @param <D>
 *            type of second parameter (duration)
 * 
 */
public abstract class TemporalArithmeticFunction<T extends BaseTimeAttributeValue<T>, D extends DurationAttributeValue> extends BaseFunction<PrimitiveResult<T>>
{
	/**
	 * Standard identifier for the dateTime-add-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_ADD_DAYTIMEDURATION = FUNCTION_NS_3 + "dateTime-add-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-subtract-dayTimeDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_DAYTIMEDURATION = FUNCTION_NS_3 + "dateTime-subtract-dayTimeDuration";

	/**
	 * Standard identifier for the dateTime-add-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_ADD_YEARMONTHDURATION = FUNCTION_NS_3 + "dateTime-add-yearMonthDuration";

	/**
	 * Standard identifier for the dateTime-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATETIME_SUBTRACT_YEARMONTHDURATION = FUNCTION_NS_3 + "dateTime-subtract-yearMonthDuration";

	/**
	 * Standard identifier for the date-add-yearMonthDuration function.
	 */
	public static final String NAME_DATE_ADD_YEARMONTHDURATION = FUNCTION_NS_3 + "date-add-yearMonthDuration";

	/**
	 * Standard identifier for the date-subtract-yearMonthDuration function.
	 */
	public static final String NAME_DATE_SUBTRACT_YEARMONTHDURATION = FUNCTION_NS_3 + "date-subtract-yearMonthDuration";

	/**
	 * Temporal arithmetic function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-arithmetic",
	//
			new TimeAddDuration<>(NAME_DATETIME_ADD_DAYTIMEDURATION, DateTimeAttributeValue.identifier, DateTimeAttributeValue.class, DayTimeDurationAttributeValue.identifier, DayTimeDurationAttributeValue.class),
			//
			new TimeSubtractDuration<>(NAME_DATETIME_SUBTRACT_DAYTIMEDURATION, DateTimeAttributeValue.identifier, DateTimeAttributeValue.class, DayTimeDurationAttributeValue.identifier, DayTimeDurationAttributeValue.class),
			//
			new TimeAddDuration<>(NAME_DATETIME_ADD_YEARMONTHDURATION, DateTimeAttributeValue.identifier, DateTimeAttributeValue.class, YearMonthDurationAttributeValue.identifier, YearMonthDurationAttributeValue.class),
			//
			new TimeSubtractDuration<>(NAME_DATETIME_SUBTRACT_YEARMONTHDURATION, DateTimeAttributeValue.identifier, DateTimeAttributeValue.class, YearMonthDurationAttributeValue.identifier, YearMonthDurationAttributeValue.class),
			//
			new TimeAddDuration<>(NAME_DATE_ADD_YEARMONTHDURATION, DateAttributeValue.identifier, DateAttributeValue.class, YearMonthDurationAttributeValue.identifier, YearMonthDurationAttributeValue.class),
			//
			new TimeSubtractDuration<>(NAME_DATE_SUBTRACT_YEARMONTHDURATION, DateAttributeValue.identifier, DateAttributeValue.class, YearMonthDurationAttributeValue.identifier, YearMonthDurationAttributeValue.class));

	private final String invalidArgTypesErrorMsg;

	private final Class<T> firstParamClass;

	private final Class<D> secondParamClass;

	/**
	 * Creates a new Date-time arithmetic function
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param secondParamTypeURI
	 *            second parameter type URI
	 * @param secondParamClass
	 *            second parameter type
	 * @param firstParamTypeURI
	 *            first parameter type URI
	 * @param firstParamType
	 *            first parameter type
	 */
	protected TemporalArithmeticFunction(String functionName, String firstParamTypeURI, Class<T> firstParamType, String secondParamTypeURI, Class<D> secondParamType)
	{
		super(functionName, new DatatypeDef(firstParamTypeURI), false, new DatatypeDef(firstParamTypeURI), new DatatypeDef(secondParamTypeURI));
		this.firstParamClass = firstParamType;
		this.secondParamClass = secondParamType;
		invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + firstParamClass.getSimpleName() + "," + secondParamClass.getSimpleName() + "): ";
	}

	@Override
	protected final Call getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes) throws IllegalArgumentException
	{
		return new EagerPrimitiveEvalCall<AttributeValue>(AttributeValue[].class, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected PrimitiveResult<T> evaluate(AttributeValue[] args) throws IndeterminateEvaluationException
			{
				final T arg0;
				final D arg1;
				try
				{
					arg0 = firstParamClass.cast(args[0]);
					arg1 = secondParamClass.cast(args[1]);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + args[0].getClass().getSimpleName() + "," + args[1].getClass().getSimpleName(), Status.STATUS_PROCESSING_ERROR, e);
				}

				return new PrimitiveResult<>(eval(arg0, arg1), returnType);
			}

		};
	}

	protected abstract T eval(T time, D duration);

	private static class TimeAddDuration<T extends BaseTimeAttributeValue<T>, D extends DurationAttributeValue> extends TemporalArithmeticFunction<T, D>
	{

		protected TimeAddDuration(String functionName, String firstParamTypeURI, Class<T> firstParamType, String secondParamTypeURI, Class<D> secondParamType)
		{
			super(functionName, firstParamTypeURI, firstParamType, secondParamTypeURI, secondParamType);
		}

		@Override
		protected final T eval(T time, D duration)
		{
			time.add(duration);
			return time;

		}

	}

	private static class TimeSubtractDuration<T extends BaseTimeAttributeValue<T>, D extends DurationAttributeValue> extends TemporalArithmeticFunction<T, D>
	{

		protected TimeSubtractDuration(String functionName, String firstParamTypeURI, Class<T> firstParamType, String secondParamTypeURI, Class<D> secondParamType)
		{
			super(functionName, firstParamTypeURI, firstParamType, secondParamTypeURI, secondParamType);
		}

		@Override
		protected final T eval(T time, D duration)
		{
			time.subtract(duration);
			return time;

		}

	}

}
