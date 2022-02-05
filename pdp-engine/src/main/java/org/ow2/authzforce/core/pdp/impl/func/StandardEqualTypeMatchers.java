/*
 * Copyright 2012-2022 THALES.
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
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.CallFactory;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.CallFactoryBuilder;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.Matcher;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.RegexpMatchFunctionHelper;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.*;

import java.util.List;

/**
 * Standard match functions taking parameters of same/equal type, i.e. standard (A.3.1) Equality predicates, special match function x500Name-match, string-starts-with/contains/ends-with.
 * <p>
 * Note that there are no such functions as ipAddress-equal and dnsName-equal functions in the XACML core specification. Regexp-match alternatives should be used instead. More info:
 * https://lists.oasis-open.org/archives/xacml-comment/200411/msg00002.html
 *
 * @version $Id: $
 */
final class StandardEqualTypeMatchers
{

	/**
	 * x500Name-match function matcher
	 * 
	 */
	static final Matcher<X500NameValue> X500NAME_MATCHER = X500NameValue::match;

	/**
	 * string-starts-with function matcher. For other *-starts-with functions, see {@link org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction} class.
	 */
	static final Matcher<StringValue> STRING_STARTS_WITH_MATCHER = new Matcher<>()
	{
		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public boolean match(final StringValue prefix, final StringValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	};

	/**
	 * string-ends-with function matcher
	 */
	static final Matcher<StringValue> STRING_ENDS_WITH_MATCHER = new Matcher<>()
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public boolean match(final StringValue suffix, final StringValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	};

	/**
	 * string-contains function matcher
	 * 
	 */
	static final Matcher<StringValue> STRING_CONTAINS_MATCHER = new Matcher<>()
	{

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the other
		 */
		@Override
		public boolean match(final StringValue contained, final StringValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	};

	private static final class StringRegexpMatchCallFactory extends CallFactory<StringValue>
	{
		private static final Matcher<StringValue> STRING_REGEXP_MATCHER = RegexpMatchFunctionHelper::match;

		private final RegexpMatchFunctionHelper regexFuncHelper;

		private StringRegexpMatchCallFactory(final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, StringValue> functionSignature)
		{
			super(functionSignature, STRING_REGEXP_MATCHER);
			regexFuncHelper = new RegexpMatchFunctionHelper(functionSignature, StandardDatatypes.STRING);
		}

		@Override
		protected FirstOrderFunctionCall<BooleanValue> getInstance(final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes)
		{
			final FirstOrderFunctionCall<BooleanValue> compiledRegexFuncCall = regexFuncHelper.getCompiledRegexMatchCall(argExpressions, remainingArgTypes);
			/*
			 * compiledRegexFuncCall == null means no optimization using a pre-compiled regex could be done; in this case, use super.newCall() as usual, which will call match() down below, compiling
			 * the regex on-the-fly for each evaluation.
			 */
			return compiledRegexFuncCall == null ? super.getInstance(argExpressions, remainingArgTypes) : compiledRegexFuncCall;
		}

	}

	static final CallFactoryBuilder<StringValue> STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER = StringRegexpMatchCallFactory::new;

	private StandardEqualTypeMatchers()
	{
		// empty private constructor to prevent instantiation
	}

}
