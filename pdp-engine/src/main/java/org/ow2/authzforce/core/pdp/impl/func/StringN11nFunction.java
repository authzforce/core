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
import java.util.Locale;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
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
		super(functionId, StandardDatatypes.STRING, false, Collections.singletonList(StandardDatatypes.STRING));
		this.funcCallFactory = new CallFactory(functionSignature, stringNormalizer);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

}
