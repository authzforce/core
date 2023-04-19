/*
 * Copyright 2012-2023 THALES.
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

import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

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

	static final StringNormalizer STRING_NORMALIZE_SPACE_FUNCTION_CALL_FACTORY = StringValue::trim;

	/*
	 * Specified by fn:lower-case function in [XF]. Looking at Saxon HE as our reference for Java open source implementation of XPath functions, we can check in Saxon implementation of
	 * fn:lower-case (LowerCase class), that this is equivalent to String#toLowerCase(); English locale to be used for Locale-insensitive strings, see String.toLowerCase()
	 */
	static final StringNormalizer STRING_NORMALIZE_TO_LOWER_CASE_FUNCTION_CALL_FACTORY = value -> value.toLowerCase(Locale.ENGLISH);

	private final StringNormalizer strNormalizer;

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
		this.strNormalizer = stringNormalizer;
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
			return new EagerSinglePrimitiveTypeEval<>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(final Deque<StringValue> argStack)
				{
					return strNormalizer.normalize(argStack.getFirst());
				}

			};
	}

}
