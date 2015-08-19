package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Implements string-substring function
 * 
 * @param <T>
 *            parameter type
 * 
 */
public class SubstringFunction<T extends PrimitiveAttributeValue<String>> extends FirstOrderFunction<StringAttributeValue>
{

	/**
	 * Standard TYPE_URI for the string-substring function.
	 */
	public static final String NAME_STRING_SUBSTRING = FUNCTION_NS_3 + "string-substring";

	/**
	 * Standard TYPE_URI for the anyURI-substring function.
	 */
	public static final String NAME_ANYURI_SUBSTRING = FUNCTION_NS_3 + "anyURI-substring";

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "substring",
	//
			new SubstringFunction<>(NAME_STRING_SUBSTRING, StringAttributeValue.TYPE, StringAttributeValue.class),
			//
			new SubstringFunction<>(NAME_ANYURI_SUBSTRING, AnyURIAttributeValue.TYPE, AnyURIAttributeValue.class));

	private final Class<T> firstParamClass;

	private final String invalidArgTypesErrorMsg;

	/**
	 * Instantiates function
	 * 
	 * @param functionId
	 *            function ID
	 * @param param0Type
	 *            First parameter type
	 * @param param0Class
	 *            First parameter class
	 */
	public SubstringFunction(String functionId, DatatypeDef param0Type, Class<T> param0Class)
	{
		super(functionId, StringAttributeValue.TYPE, false, param0Type, IntegerAttributeValue.TYPE, IntegerAttributeValue.TYPE);
		this.firstParamClass = param0Class;
		this.invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + firstParamClass.getSimpleName() + ", integer, integer): ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes)
	{
		return new EagerPrimitiveEvalCall<AttributeValue>(AttributeValue[].class, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected StringAttributeValue evaluate(AttributeValue[] args) throws IndeterminateEvaluationException
			{
				final T arg0;
				final IntegerAttributeValue beginIndex;
				final IntegerAttributeValue endIndex;
				try
				{
					arg0 = firstParamClass.cast(args[0]);
					beginIndex = (IntegerAttributeValue) args[1];
					endIndex = (IntegerAttributeValue) args[2];
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + args[0].getClass().getSimpleName() + "," + args[1].getClass().getSimpleName() + "," + args[2].getClass().getSimpleName(), Status.STATUS_PROCESSING_ERROR, e);
				}

				return eval(arg0, beginIndex, endIndex);
			}

		};
	}

	private final String argsOutOfBoundsErrorMessage = "FUNCTION " + functionId + ": args out of bounds";

	/**
	 * string-susbtring(str1, beginIndex, endIndex)
	 * <p>
	 * The result SHALL be the substring of <code>arg0</code> at the position given by
	 * <code>beginIndex</code> and ending at <code>endIndex</code>. The first character of
	 * <code>arg0</code> has position zero. The negative integer value -1 given for
	 * <code>endIndex</code> indicates the end of the string. If <code>beginIndex</code> or
	 * <code>endIndex</code> are out of bounds, then the function MUST evaluate to Indeterminate
	 * with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error
	 * 
	 * @param arg0
	 *            value from which to extract the substring
	 * @param beginIndex
	 *            position in this string where to begin the substring
	 * @param endIndex
	 *            the position in this string just before which to end the substring
	 * @return the substring
	 * @throws IndeterminateEvaluationException
	 *             if {@code beginIndex} or {@code endIndex} are out of bounds
	 */
	public StringAttributeValue eval(T arg0, IntegerAttributeValue beginIndex, IntegerAttributeValue endIndex) throws IndeterminateEvaluationException
	{
		final String substring;
		try
		{
			final int beginIndexInt = beginIndex.intValueExact();
			final int endIndexInt = endIndex.intValueExact();
			substring = endIndexInt == -1 ? arg0.getValue().substring(beginIndexInt) : arg0.getValue().substring(beginIndexInt, endIndexInt);
		} catch (ArithmeticException | IndexOutOfBoundsException e)
		{
			throw new IndeterminateEvaluationException(argsOutOfBoundsErrorMessage, Status.STATUS_PROCESSING_ERROR, e);
		}

		return new StringAttributeValue(substring);
	}

}
