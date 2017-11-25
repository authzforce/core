/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
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
final class StringConcatenateFunction extends SingleParameterTypedFirstOrderFunction<StringValue, StringValue>
{

	StringConcatenateFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.STRING, true, Arrays.asList(StandardDatatypes.STRING, StandardDatatypes.STRING, StandardDatatypes.STRING));
	}

	@Override
	public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	{

		return new EagerSinglePrimitiveTypeEval<StringValue, StringValue>(functionSignature, argExpressions, remainingArgTypes)
		{

			@Override
			protected StringValue evaluate(final Deque<StringValue> args) throws IndeterminateEvaluationException
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

}
