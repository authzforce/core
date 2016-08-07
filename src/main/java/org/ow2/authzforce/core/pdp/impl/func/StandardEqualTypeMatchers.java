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

import java.util.List;

import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.CallFactory;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.CallFactoryBuilder;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.Matcher;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.RegexpMatchFunctionHelper;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;

/**
 * Standard match functions taking parameters of same/equal type, i.e. standard (A.3.1) Equality predicates, special match function x500Name-match, string-starts-with/contains/ends-with.
 * <p>
 * Note that there are no such functions as ipAddress-equal and dnsName-equal functions in the XACML core specification. Regexp-match alternatives should be used intead. More info:
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
	static final Matcher<X500NameValue> X500NAME_MATCHER = new Matcher<X500NameValue>()
	{
		@Override
		public boolean match(final X500NameValue arg0, final X500NameValue arg1)
		{
			return arg0.match(arg1);
		}
	};

	/**
	 * string-starts-with function matcher. For other *-starts-with functions, see {@link org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction} class.
	 */
	static final Matcher<StringValue> STRING_STARTS_WITH_MATCHER = new Matcher<StringValue>()
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
	static final Matcher<StringValue> STRING_ENDS_WITH_MATCHER = new Matcher<StringValue>()
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(final StringValue suffix, final StringValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	};

	/**
	 * string-contains function matcher
	 * 
	 */
	static final Matcher<StringValue> STRING_CONTAINS_MATCHER = new Matcher<StringValue>()
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
		private static final Matcher<StringValue> STRING_REGEXP_MATCHER = new Matcher<StringValue>()
		{
			@Override
			public boolean match(final StringValue regex, final StringValue arg1)
			{
				return RegexpMatchFunctionHelper.match(regex, arg1);
			}
		};

		private final RegexpMatchFunctionHelper regexFuncHelper;

		private StringRegexpMatchCallFactory(final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, StringValue> functionSignature)
		{
			super(functionSignature, STRING_REGEXP_MATCHER);
			regexFuncHelper = new RegexpMatchFunctionHelper(functionSignature, StandardDatatypes.STRING_FACTORY.getDatatype());
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

	static final CallFactoryBuilder<StringValue> STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER = new CallFactoryBuilder<StringValue>()
	{

		@Override
		public CallFactory<StringValue> build(final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, StringValue> functionSignature)
		{
			return new StringRegexpMatchCallFactory(functionSignature);
		}

	};

	private StandardEqualTypeMatchers()
	{
		// empty private constructor to prevent instantiation
	}

}
