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
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.MultiParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BaseTimeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DurationValue;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * Implements generic match functions taking parameters of possibly different types.
 *
 * @param <T>
 *            type of first parameter and returned value (date/time)
 * @param <D>
 *            type of second parameter (duration)
 * 
 * @version $Id: $
 */
final class TemporalArithmeticFunction<T extends BaseTimeValue<T>, D extends DurationValue<D>> extends MultiParameterTypedFirstOrderFunction<T>
{
	interface StaticOperation<TV extends BaseTimeValue<TV>, DV extends DurationValue<DV>>
	{

		TV eval(TV time, DV duration);
	}

	private static final class Call<TV extends BaseTimeValue<TV>, DV extends DurationValue<DV>> extends EagerMultiPrimitiveTypeEval<TV>
	{
		private final String invalidArgTypesErrorMsg;
		private final Datatype<DV> durationParamType;
		private final Datatype<TV> timeParamType;
		private final StaticOperation<TV, DV> op;

		private Call(final FirstOrderFunctionSignature<TV> functionSig, final Datatype<TV> timeParamType, final Datatype<DV> durationParamType, final StaticOperation<TV, DV> op,
				final List<Expression<?>> args, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + timeParamType + "," + durationParamType + "): ";
			this.timeParamType = timeParamType;
			this.durationParamType = durationParamType;
			this.op = op;
		}

		@Override
		protected TV evaluate(final Deque<AttributeValue> args) throws IndeterminateEvaluationException
		{
			final AttributeValue rawArg0 = args.poll();
			final AttributeValue rawArg1 = args.poll();

			final TV arg0;
			final DV arg1;
			try
			{
				arg0 = timeParamType.cast(rawArg0);
				arg1 = durationParamType.cast(rawArg1);
			}
			catch (final ClassCastException e)
			{
				throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + "," + rawArg1.getDataType(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
			}

			return op.eval(arg0, arg1);
		}
	}

	private final StaticOperation<T, D> op;

	private final Datatype<T> timeParamType;

	private final Datatype<D> durationParamType;

	/**
	 * Creates a new Date-time arithmetic function
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param durationParamType
	 *            second parameter type (duration)
	 * @param timeParamType
	 *            first parameter type (date/time)
	 * @param op
	 *            temporal arithmetic operation
	 */
	TemporalArithmeticFunction(final String functionName, final Datatype<T> timeParamType, final Datatype<D> durationParamType, final StaticOperation<T, D> op)
	{
		super(functionName, timeParamType, false, Arrays.asList(timeParamType, durationParamType));
		this.timeParamType = timeParamType;
		this.durationParamType = durationParamType;

		this.op = op;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<T> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new Call<>(functionSignature, timeParamType, durationParamType, op, argExpressions, remainingArgTypes);
	}

}
