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
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.MultiParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BaseTimeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DurationValue;

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
final class TemporalArithmeticFunction<T extends BaseTimeValue<T>, D extends DurationValue<D>>
		extends MultiParameterTypedFirstOrderFunction<T>
{
	interface StaticOperation<TV extends BaseTimeValue<TV>, DV extends DurationValue<DV>>
	{

		TV eval(TV time, DV duration);
	}

	private static final class Call<TV extends BaseTimeValue<TV>, DV extends DurationValue<DV>>
			extends EagerMultiPrimitiveTypeEval<TV>
	{
		private final String invalidArgTypesErrorMsg;
		private final Class<DV> durationParamClass;
		private final Class<TV> timeParamClass;
		private final StaticOperation<TV, DV> op;

		private Call(final FirstOrderFunctionSignature<TV> functionSig, final Datatype<TV> timeParamType,
				final Datatype<DV> durationParamType, final StaticOperation<TV, DV> op, final List<Expression<?>> args,
				final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + timeParamType
					+ "," + durationParamType + "): ";
			this.timeParamClass = timeParamType.getValueClass();
			this.durationParamClass = durationParamType.getValueClass();
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
				arg0 = timeParamClass.cast(rawArg0);
				arg1 = durationParamClass.cast(rawArg1);
			}
			catch (final ClassCastException e)
			{
				throw new IndeterminateEvaluationException(
						invalidArgTypesErrorMsg + rawArg0.getDataType() + "," + rawArg1.getDataType(),
						StatusHelper.STATUS_PROCESSING_ERROR, e);
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
	TemporalArithmeticFunction(final String functionName, final Datatype<T> timeParamType,
			final Datatype<D> durationParamType, final StaticOperation<T, D> op)
	{
		super(functionName, timeParamType, false, Arrays.asList(timeParamType, durationParamType));
		this.timeParamType = timeParamType;
		this.durationParamType = durationParamType;

		this.op = op;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<T> newCall(final List<Expression<?>> argExpressions,
			final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new Call<>(functionSignature, timeParamType, durationParamType, op, argExpressions, remainingArgTypes);
	}

}
