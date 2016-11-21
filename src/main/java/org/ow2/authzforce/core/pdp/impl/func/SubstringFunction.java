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
import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
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
		private final Class<? extends SimpleValue<String>> firstParamClass;

		private Call(final FirstOrderFunctionSignature<StringValue> functionSig, final Datatype<? extends SimpleValue<String>> param0Type, final List<Expression<?>> args,
				final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			this.invalidArgTypesErrorMsg = "Function " + functionId + ": Invalid arg types: expected: " + param0Type + ", " + StandardDatatypes.INTEGER_FACTORY.getDatatype() + ", "
					+ StandardDatatypes.INTEGER_FACTORY.getDatatype() + "; actual: ";
			this.argsOutOfBoundsErrorMessage = "Function " + functionId + ": either beginIndex is out of bounds, or endIndex =/= -1 and out of bounds";
			this.firstParamClass = param0Type.getValueClass();
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
				arg0 = firstParamClass.cast(rawArg0);
				beginIndex = (IntegerValue) rawArg1;
				endIndex = (IntegerValue) rawArg2;
			}
			catch (final ClassCastException e)
			{
				throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + "," + rawArg1.getDataType() + "," + rawArg2.getDataType(),
						StatusHelper.STATUS_PROCESSING_ERROR, e);
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
			 * @throws IndeterminateEvaluationException
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
				throw new IndeterminateEvaluationException(argsOutOfBoundsErrorMessage, StatusHelper.STATUS_PROCESSING_ERROR, e);
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
		super(functionId, StandardDatatypes.STRING_FACTORY.getDatatype(), false, Arrays.asList(param0Type, StandardDatatypes.INTEGER_FACTORY.getDatatype(),
				StandardDatatypes.INTEGER_FACTORY.getDatatype()));
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
		final Value arg1 = arg1Exp.getValue();
		final int beginIndex;
		if (arg1 != null)
		{
			if (!(arg1 instanceof IntegerValue))
			{
				throw new IllegalArgumentException(getInvalidArg1MessagePrefix(functionSignature) + arg1 + " (type: " + arg1Exp.getReturnType() + ")");
			}

			beginIndex = IntegerValue.class.cast(arg1).getUnderlyingValue().intValueExact();
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
		final Value arg2 = arg2Exp.getValue();
		if (arg2 != null)
		{
			if (!(arg2 instanceof IntegerValue))
			{
				throw new IllegalArgumentException(getInvalidArg2MessagePrefix(functionSignature) + arg2 + " (type: " + arg2Exp.getReturnType() + ")");
			}

			final int endIndex = IntegerValue.class.cast(arg2).getUnderlyingValue().intValueExact();
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
						return new ConstantResultFirstOrderFunctionCall<>(StringValue.EMPTY, StandardDatatypes.STRING_FACTORY.getDatatype());
					}
				}

			}
		}

		return new Call(functionSignature, param0Type, argExpressions, remainingArgTypes);
	}
}
