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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.func.MultiParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements *-substring functions
 *
 * @param <AV>
 *            parameter type
 * 
 * @version $Id: $
 */
final class SubstringFunction<AV extends SimpleValue<String>> extends MultiParameterTypedFirstOrderFunction<StringValue>
{

	private static final Logger LOGGER = LoggerFactory.getLogger(SubstringFunction.class);

	private static final class Call extends EagerMultiPrimitiveTypeEval<StringValue>
	{

		private final String invalidArgTypesErrorMsg;
		private final String argsOutOfBoundsErrorMessage;
		private final Datatype<? extends SimpleValue<String>> param0Type;

		private Call(final FirstOrderFunctionSignature<StringValue> functionSig, final Datatype<? extends SimpleValue<String>> param0Type, final List<Expression<?>> args,
				final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			this.invalidArgTypesErrorMsg = "Function " + functionId + ": Invalid arg types: expected: " + param0Type + ", " + StandardDatatypes.INTEGER + ", " + StandardDatatypes.INTEGER
					+ "; actual: ";
			this.argsOutOfBoundsErrorMessage = "Function " + functionId + ": either beginIndex is out of bounds, or endIndex =/= -1 and out of bounds";
			this.param0Type = param0Type;
		}

		@Override
		protected StringValue evaluate(final Deque<AttributeValue> args) throws IndeterminateEvaluationException
		{
			final AttributeValue rawArg0 = args.poll();
			final AttributeValue rawArg1 = args.poll();
			final AttributeValue rawArg2 = args.poll();

			final SimpleValue<String> arg0;
			final IntegerValue beginIndex;
			final IntegerValue endIndex;
			try
			{
				arg0 = param0Type.cast(rawArg0);
				beginIndex = (IntegerValue) rawArg1;
				endIndex = (IntegerValue) rawArg2;
			}
			catch (final ClassCastException e)
			{
				throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + "," + rawArg1.getDataType() + "," + rawArg2.getDataType(),
						XacmlStatusCode.PROCESSING_ERROR.value(), e);
			}

			/**
			 * string-susbtring(str1, beginIndex, endIndex)
			 * <p>
			 * The result SHALL be the substring of <code>arg0</code> at the position given by <code>beginIndex</code> and ending at <code>endIndex</code>. The first character of <code>arg0</code> has
			 * position zero. The negative integer value -1 given for <code>endIndex</code> indicates the end of the string. If <code>beginIndex</code> or <code>endIndex</code> are out of bounds, then
			 * the function MUST evaluate to Indeterminate with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error
			 * 
			 * @param arg0
			 *            value from which to extract the substring
			 * @param beginIndex
			 *            position in this string where to begin the substring
			 * @param endIndex
			 *            the position in this string just before which to end the substring
			 * @return the substring
			 * @throws IndeterminateXacmlJaxbResult
			 *             if {@code beginIndex} or {@code endIndex} are out of bounds
			 */
			final String substring;
			try
			{
				final int beginIndexInt = beginIndex.intValueExact();
				final int endIndexInt = endIndex.intValueExact();
				substring = endIndexInt == -1 ? arg0.getUnderlyingValue().substring(beginIndexInt) : arg0.getUnderlyingValue().substring(beginIndexInt, endIndexInt);
			}
			catch (ArithmeticException | IndexOutOfBoundsException e)
			{
				throw new IndeterminateEvaluationException(argsOutOfBoundsErrorMessage, XacmlStatusCode.PROCESSING_ERROR.value(), e);
			}

			return new StringValue(substring);
		}
	}

	private final Datatype<AV> param0Type;

	/**
	 * Instantiates function
	 * 
	 * @param functionId
	 *            function ID
	 * @param param0Type
	 *            First parameter type
	 */
	SubstringFunction(final String functionId, final Datatype<AV> param0Type)
	{
		super(functionId, StandardDatatypes.STRING, false, Arrays.asList(param0Type, StandardDatatypes.INTEGER, StandardDatatypes.INTEGER));
		this.param0Type = param0Type;
	}

	private static String getInvalidArg1MessagePrefix(final FirstOrderFunctionSignature<?> funcsig)
	{
		return "Function " + funcsig.getName() + ": Invalid arg #1 (beginIndex): expected: positive integer; actual: ";
	}

	private static String getInvalidArg2MessagePrefix(final FirstOrderFunctionSignature<?> funcsig)
	{
		return "Function " + funcsig.getName() + ": Invalid arg #2 (endIndex): expected: -1 or positive integer >= beginIndex; actual: ";
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	{
		if (argExpressions.size() != 3)
		{
			throw new IllegalArgumentException("Function " + functionSignature.getName() + ": Invalid number of args: expected: 3; actual: " + argExpressions.size());
		}

		/*
		 * Datatypes will be checked by Call class but they are specific constraints: arg0 >= 0 && (arg1 == -1 || arg0 <= arg1). Note that if arg0 == arg1, result may be '' or raise out-of-bounds
		 * error
		 */
		final Iterator<? extends Expression<?>> argExpsIterator = argExpressions.iterator();
		// Skip the first argument which is the string
		argExpsIterator.next();

		// Second arg
		final Expression<?> arg1Exp = argExpsIterator.next();
		final Optional<? extends Value> arg1 = arg1Exp.getValue();
		final int beginIndex;
		if (arg1.isPresent())
		{
			final Value arg1Value = arg1.get();
			if (!(arg1Value instanceof IntegerValue))
			{
				throw new IllegalArgumentException(getInvalidArg1MessagePrefix(functionSignature) + arg1Value + " (type: " + arg1Exp.getReturnType() + ")");
			}

			beginIndex = IntegerValue.class.cast(arg1Value).getUnderlyingValue().intValueExact();
			if (beginIndex < 0)
			{
				throw new IllegalArgumentException(getInvalidArg1MessagePrefix(functionSignature) + beginIndex);
			}
		}
		else
		{
			beginIndex = -1; // undefined
		}

		// Third arg
		final Expression<?> arg2Exp = argExpsIterator.next();
		final Optional<? extends Value> arg2 = arg2Exp.getValue();
		if (arg2.isPresent())
		{
			final Value arg2Value = arg2.get();
			if (!(arg2Value instanceof IntegerValue))
			{
				throw new IllegalArgumentException(getInvalidArg2MessagePrefix(functionSignature) + arg2Value + " (type: " + arg2Exp.getReturnType() + ")");
			}

			final int endIndex = IntegerValue.class.cast(arg2Value).getUnderlyingValue().intValueExact();
			if (endIndex != -1)
			{
				if (endIndex < 0)
				{
					throw new IllegalArgumentException(getInvalidArg2MessagePrefix(functionSignature) + endIndex);
				}

				if (beginIndex != -1)
				{
					// beginIndex defined
					if (endIndex < beginIndex)
					{
						// and endIndex strictly smaller than beginIndex!
						throw new IllegalArgumentException(getInvalidArg2MessagePrefix(functionSignature) + endIndex + " < beginIndex (" + beginIndex + ")");
					}

					// endIndex >= beginIndex
					if (beginIndex == 0 && endIndex == 0)
					{
						// constant empty string
						LOGGER.warn("{} used with arg0 (beginIndex) = arg1 (endIndex) = 0 resulting in constant empty string. This is useless!", this.functionSignature);
						return new ConstantResultFirstOrderFunctionCall<>(StringValue.EMPTY, StandardDatatypes.STRING);
					}
				}

			}
		}

		return new Call(functionSignature, param0Type, argExpressions, remainingArgTypes);
	}
}
