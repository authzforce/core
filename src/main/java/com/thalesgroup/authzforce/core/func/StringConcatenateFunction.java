package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.List;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Implements string-concatenate function
 * 
 */
public class StringConcatenateFunction extends FirstOrderFunction<StringAttributeValue>
{

	/**
	 * Standard TYPE_URI for the string-concatenate function.
	 */
	public static final String NAME_STRING_CONCATENATE = FUNCTION_NS_2 + "string-concatenate";

	/**
	 * Instantiates function
	 */
	public StringConcatenateFunction()
	{
		super(NAME_STRING_CONCATENATE, StringAttributeValue.TYPE, true, StringAttributeValue.TYPE, StringAttributeValue.TYPE, StringAttributeValue.TYPE);
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
		return new EagerPrimitiveEvalCall<StringAttributeValue>(StringAttributeValue[].class, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected StringAttributeValue evaluate(StringAttributeValue[] args) throws IndeterminateEvaluationException
			{
				return eval(args);
			}

		};
	}

	/**
	 * string-concatenate(str1, str2, str3, ...)
	 * 
	 * @param args
	 *            strings to concatenate
	 * @return concatenation of all args
	 */
	public static StringAttributeValue eval(StringAttributeValue[] args)
	{

		return new StringAttributeValue(Arrays.toString(args));
	}

}
