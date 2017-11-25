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

import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction.Matcher;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.Rfc822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

/**
 * Standard match functions taking two parameters of possibly different types, e.g. a string and a URI.
 * 
 * @version $Id: $
 */
final class StandardNonEqualTypeMatchers
{
	/**
	 * rfc822Name-match function
	 * 
	 */
	static final Matcher<StringValue, Rfc822NameValue> RFC822NAME_MATCHER = new Matcher<StringValue, Rfc822NameValue>()
	{

		@Override
		public final boolean match(final StringValue arg0, final Rfc822NameValue arg1)
		{
			return arg1.match(arg0.getUnderlyingValue());
		}
	};

	// public static void main(String... args) throws XPathException
	// {
	// String input = "zzztesting";
	// String regex = "^test.*";
	// String flags = "";
	// String xpathlang = "XP20";
	// //
	// RegularExpression compiledRegex = Configuration.getPlatform().compileRegularExpression(regex,
	// flags, xpathlang, null);
	// boolean isMatched = compiledRegex.containsMatch(input);
	// System.out.println(isMatched);
	// }

	/**
	 * anyURI-starts-with matcher. For string-starts-with, see {@link EqualTypeMatchFunction} class.
	 * 
	 */
	static final Matcher<StringValue, AnyUriValue> ANYURI_STARTS_WITH_MATCHER = new Matcher<StringValue, AnyUriValue>()
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public final boolean match(final StringValue prefix, final AnyUriValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	};

	/**
	 * anyURI-ends-with matcher
	 */
	static final Matcher<StringValue, AnyUriValue> ANYURI_ENDS_WITH_MATCHER = new Matcher<StringValue, AnyUriValue>()
	{
		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(final StringValue suffix, final AnyUriValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	};

	/**
	 * anyURI-contains matcher
	 * 
	 */
	static final Matcher<StringValue, AnyUriValue> ANYURI_CONTAINS_MATCHER = new Matcher<StringValue, AnyUriValue>()
	{

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the other
		 */
		@Override
		public final boolean match(final StringValue contained, final AnyUriValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	};

	private StandardNonEqualTypeMatchers()
	{
		// empty private constructor to prevent instantiation
	}

}
