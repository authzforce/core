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
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

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
	public static final SingleParameterTypedFirstOrderFunction<StringValue, StringValue> INSTANCE = new SingleParameterTypedFirstOrderFunction<StringValue, StringValue>(NAME_STRING_CONCATENATE,
			StandardDatatypes.STRING_FACTORY.getDatatype(), true, Arrays.asList(StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					StandardDatatypes.STRING_FACTORY.getDatatype()))
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
