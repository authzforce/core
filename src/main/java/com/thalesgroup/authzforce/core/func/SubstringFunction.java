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

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;

/**
 * Implements *-substring functions
 * 
 * @param <AV>
 *            parameter type
 * 
 */
public class SubstringFunction<AV extends SimpleAttributeValue<String, AV>> extends FirstOrderFunction<StringAttributeValue>
{

	/**
	 * Standard identifier for the string-substring function.
	 */
	public static final String NAME_STRING_SUBSTRING = FUNCTION_NS_3 + "string-substring";

	/**
	 * Standard identifier for the anyURI-substring function.
	 */
	public static final String NAME_ANYURI_SUBSTRING = FUNCTION_NS_3 + "anyURI-substring";

	private final Class<AV> firstParamClass;

	private final String invalidArgTypesErrorMsg;

	/**
	 * Instantiates function
	 * 
	 * @param functionId
	 *            function ID
	 * @param param0Type
	 *            First parameter type
	 */
	public SubstringFunction(String functionId, Datatype<AV> param0Type)
	{
		super(functionId, DatatypeConstants.STRING.TYPE, false, param0Type, DatatypeConstants.INTEGER.TYPE, DatatypeConstants.INTEGER.TYPE);
		this.firstParamClass = param0Type.getValueClass();
		this.invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + firstParamClass.getSimpleName() + ", integer, integer): ";
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
		return new EagerMultiPrimitiveTypeEval<StringAttributeValue>(signature, argExpressions, remainingArgTypes)
		{

			@Override
			protected StringAttributeValue evaluate(AttributeValue<?>[] args) throws IndeterminateEvaluationException
			{
				final AV arg0;
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
	public StringAttributeValue eval(AV arg0, IntegerAttributeValue beginIndex, IntegerAttributeValue endIndex) throws IndeterminateEvaluationException
	{
		final String substring;
		try
		{
			final int beginIndexInt = beginIndex.intValueExact();
			final int endIndexInt = endIndex.intValueExact();
			substring = endIndexInt == -1 ? arg0.getUnderlyingValue().substring(beginIndexInt) : arg0.getUnderlyingValue().substring(beginIndexInt, endIndexInt);
		} catch (ArithmeticException | IndexOutOfBoundsException e)
		{
			throw new IndeterminateEvaluationException(argsOutOfBoundsErrorMessage, Status.STATUS_PROCESSING_ERROR, e);
		}

		return new StringAttributeValue(substring);
	}

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "substring",
	//
			new SubstringFunction<>(NAME_STRING_SUBSTRING, DatatypeConstants.STRING.TYPE),
			//
			new SubstringFunction<>(NAME_ANYURI_SUBSTRING, DatatypeConstants.ANYURI.TYPE));

}
