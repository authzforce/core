package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.List;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.PrimitiveResult;

/**
 * Implements string-concatenate function
 * 
 */
public class StringConcatenateFunction extends BaseFunction<PrimitiveResult<StringAttributeValue>>
{

	/**
	 * Standard identifier for the string-concatenate function.
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
	 * @see com.thalesgroup.authzforce.core.func.BaseFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected Call getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes)
	{
		return new EagerPrimitiveEvalCall<StringAttributeValue>(StringAttributeValue[].class, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected PrimitiveResult<StringAttributeValue> evaluate(StringAttributeValue[] args) throws IndeterminateEvaluationException
			{
				return new PrimitiveResult<>(eval(args), returnType);
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
