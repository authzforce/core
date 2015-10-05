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

import java.util.List;

import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

/**
 * Implements string-concatenate function
 * 
 * @param <AV>
 *            type of string-based primitive attribute values to concatenate
 * 
 */
public class StringConcatenateFunction<AV extends SimpleAttributeValue<String, AV>> extends FirstOrderFunction<StringAttributeValue>
{

	/**
	 * Standard identifier for the string-concatenate function.
	 */
	public static final String NAME_STRING_CONCATENATE = FUNCTION_NS_2 + "string-concatenate";
	private final Class<AV[]> paramArrayClass;

	/**
	 * Instantiates function. Takes two or more arguments, i.e. third is varargs
	 * 
	 * @param typeParameter
	 *            parameter type definition
	 */
	public StringConcatenateFunction(DatatypeConstants<AV> typeParameter)
	{
		super(NAME_STRING_CONCATENATE, DatatypeConstants.STRING.TYPE, true, typeParameter.TYPE, typeParameter.TYPE, typeParameter.TYPE);
		this.paramArrayClass = typeParameter.ARRAY_CLASS;
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

		return new EagerPrimitiveEval<StringAttributeValue, AV>(signature, paramArrayClass, argExpressions, remainingArgTypes)
		{

			@Override
			protected StringAttributeValue evaluate(AV[] args) throws IndeterminateEvaluationException
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
	public static <AV extends SimpleAttributeValue<String, AV>> StringAttributeValue eval(AV[] args)
	{
		final StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < args.length; i++)
		{
			strBuilder.append(args[i].getUnderlyingValue());
		}
		return new StringAttributeValue(strBuilder.toString());
	}

}
