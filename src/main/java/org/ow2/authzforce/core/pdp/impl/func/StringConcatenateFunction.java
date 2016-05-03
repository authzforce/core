/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;

/**
 * Implements string-concatenate function
 *
 * 
 * @version $Id: $
 */
public final class StringConcatenateFunction
{

	/**
	 * Standard identifier for the string-concatenate function.
	 */
	public static final String NAME_STRING_CONCATENATE = Function.XACML_NS_2_0 + "string-concatenate";

	private StringConcatenateFunction()
	{
	}

	/**
	 * Instance of string-concatenate function (singleton)
	 */
	public static final FirstOrderFunction.SingleParameterTyped<StringValue, StringValue> INSTANCE = new FirstOrderFunction.SingleParameterTyped<StringValue, StringValue>(
			NAME_STRING_CONCATENATE, DatatypeConstants.STRING.TYPE, true, Arrays.asList(DatatypeConstants.STRING.TYPE, DatatypeConstants.STRING.TYPE,
					DatatypeConstants.STRING.TYPE))
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
		 */
		@Override
		public FirstOrderFunctionCall<StringValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<StringValue, StringValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(Deque<StringValue> args) throws IndeterminateEvaluationException
				{
					// string-concatenate(str1, str2, str3, ...)
					final StringBuilder strBuilder = new StringBuilder();
					while (!args.isEmpty())
					{
						strBuilder.append(args.poll().getUnderlyingValue());
					}

					return new StringValue(strBuilder.toString());
				}

			};
		}
	};

}
