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

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

/**
 * String normalization (n11n) function (XACML 1.0: string-normalize-*)
 *
 * @version $Id: $
 */
final class StringN11nFunction extends SingleParameterTypedFirstOrderFunction<StringValue, StringValue>
{

	private interface StringNormalizer
	{
		StringValue normalize(StringValue value);
	}

	private static final class CallFactory
	{

		private final StringNormalizer strNormalizer;
		private final SingleParameterTypedFirstOrderFunctionSignature<StringValue, StringValue> funcSig;

		public CallFactory(final SingleParameterTypedFirstOrderFunctionSignature<StringValue, StringValue> functionSignature, final StringNormalizer stringNormalizer)
		{
			this.funcSig = functionSignature;
			this.strNormalizer = stringNormalizer;
		}

		private FirstOrderFunctionCall<StringValue> getInstance(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<StringValue, StringValue>(funcSig, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(final Deque<StringValue> argStack) throws IndeterminateEvaluationException
				{
					return strNormalizer.normalize(argStack.getFirst());
				}

			};
		}
	}

	static final StringNormalizer STRING_NORMALIZE_SPACE_FUNCTION_CALL_FACTORY = new StringNormalizer()
	{
		@Override
		public StringValue normalize(final StringValue value)
		{
			return value.trim();
		}

	};

	static final StringNormalizer STRING_NORMALIZE_TO_LOWER_CASE_FUNCTION_CALL_FACTORY = new StringNormalizer()
	{
		@Override
		public StringValue normalize(final StringValue value)
		{
			/*
			 * Specified by fn:lower-case function in [XF]. Looking at Saxon HE as our reference for Java open source implementation of XPath functions, we can check in Saxon implementation of
			 * fn:lower-case (LowerCase class), that this is equivalent to String#toLowerCase(); English locale to be used for Locale-insensitive strings, see String.toLowerCase()
			 */
			return value.toLowerCase(Locale.ENGLISH);
		}

	};

	private final CallFactory funcCallFactory;

	/**
	 * Creates a new <code>StringNormalizeFunction</code> object.
	 * 
	 * @param functionId
	 *            function ID
	 * 
	 */
	StringN11nFunction(final String functionId, final StringNormalizer stringNormalizer)
	{
		super(functionId, StandardDatatypes.STRING_FACTORY.getDatatype(), false, Collections.singletonList(StandardDatatypes.STRING_FACTORY.getDatatype()));
		this.funcCallFactory = new CallFactory(functionSignature, stringNormalizer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

}
