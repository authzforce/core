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

import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction.Matcher;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
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
	static final Matcher<StringValue, RFC822NameValue> RFC822NAME_MATCHER = new Matcher<StringValue, RFC822NameValue>()
	{

		@Override
		public final boolean match(final StringValue arg0, final RFC822NameValue arg1)
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
	static final Matcher<StringValue, AnyURIValue> ANYURI_STARTS_WITH_MATCHER = new Matcher<StringValue, AnyURIValue>()
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public final boolean match(final StringValue prefix, final AnyURIValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	};

	/**
	 * anyURI-ends-with matcher
	 */
	static final Matcher<StringValue, AnyURIValue> ANYURI_ENDS_WITH_MATCHER = new Matcher<StringValue, AnyURIValue>()
	{
		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(final StringValue suffix, final AnyURIValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	};

	/**
	 * anyURI-contains matcher
	 * 
	 */
	static final Matcher<StringValue, AnyURIValue> ANYURI_CONTAINS_MATCHER = new Matcher<StringValue, AnyURIValue>()
	{

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the other
		 */
		@Override
		public final boolean match(final StringValue contained, final AnyURIValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	};

	private StandardNonEqualTypeMatchers()
	{
		// empty private constructor to prevent instantiation
	}

}
