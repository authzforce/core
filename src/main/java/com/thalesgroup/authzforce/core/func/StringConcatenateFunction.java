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

import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

/**
 * Implements string-concatenate function
 * 
 */
public class StringConcatenateFunction extends FirstOrderFunction<StringAttributeValue>
{

	/**
	 * Standard identifier for the string-concatenate function.
	 */
	public static final String NAME_STRING_CONCATENATE = FUNCTION_NS_2 + "string-concatenate";

	/**
	 * Instantiates function. Takes two or more arguments, i.e. third is varargs
	 */
	public StringConcatenateFunction()
	{
		super(NAME_STRING_CONCATENATE, DatatypeConstants.STRING.TYPE, true, DatatypeConstants.STRING.TYPE, DatatypeConstants.STRING.TYPE, DatatypeConstants.STRING.TYPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<StringAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return new EagerPrimitiveEval<StringAttributeValue, StringAttributeValue>(signature, StringAttributeValue[].class, argExpressions, remainingArgTypes)
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
