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

import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * Implements the XACML not function
 *
 * @version $Id: $
 */
final class LogicalNotFunction extends SingleParameterTypedFirstOrderFunction<BooleanValue, BooleanValue>
{

	LogicalNotFunction(final String functionId)
	{
		super(functionId, StandardDatatypes.BOOLEAN, false, Collections.singletonList(StandardDatatypes.BOOLEAN));
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval<BooleanValue, BooleanValue>(functionSignature, argExpressions, remainingArgTypes)
		{
			@Override
			protected BooleanValue evaluate(final Deque<BooleanValue> args) throws IndeterminateEvaluationException
			{
				return args.getFirst().not();
			}

		};
	}

}
