/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import org.ow2.authzforce.core.pdp.api.func.BaseFunctionSet;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionSet;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction.Matcher;
import org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction.RegexpMatchCallFactoryBuilder;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.DNSNameWithPortRangeValue;
import org.ow2.authzforce.core.pdp.api.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;

/**
 * Standard match functions taking two parameters of possibly different types, e.g. a string and a URI.
 * 
 * @version $Id: $
 */
public final class StandardNonEqualTypeMatchFunctions
{
	private StandardNonEqualTypeMatchFunctions()
	{
		// empty private constructor to prevent instantiation
	}

	/**
	 * Standard identifier for the rfc822Name-match function (different from rfc822Name-regexp-match down below).
	 */
	public static final String NAME_RFC822NAME_MATCH = Function.XACML_NS_1_0 + "rfc822Name-match";

	/**
	 * Standard identifier for the anyURI-regexp-match function.
	 */
	public static final String NAME_ANYURI_REGEXP_MATCH = Function.XACML_NS_2_0 + "anyURI-regexp-match";

	/**
	 * Standard identifier for the ipAddress-regexp-match function.
	 */
	public static final String NAME_IPADDRESS_REGEXP_MATCH = Function.XACML_NS_2_0 + "ipAddress-regexp-match";

	/**
	 * Standard identifier for the dnsName-regexp-match function.
	 */
	public static final String NAME_DNSNAME_REGEXP_MATCH = Function.XACML_NS_2_0 + "dnsName-regexp-match";

	/**
	 * Standard identifier for the rfc822Name-regexp-match function.
	 */
	public static final String NAME_RFC822NAME_REGEXP_MATCH = Function.XACML_NS_2_0 + "rfc822Name-regexp-match";

	/**
	 * Standard identifier for the x500Name-regexp-match function.
	 */
	public static final String NAME_X500NAME_REGEXP_MATCH = Function.XACML_NS_2_0 + "x500Name-regexp-match";

	/**
	 * Standard identifier for the anyURI-starts-with function.
	 */
	public static final String NAME_ANYURI_STARTS_WITH = Function.XACML_NS_3_0 + "anyURI-starts-with";

	/**
	 * Standard identifier for the anyURI-ends-with function.
	 */
	public static final String NAME_ANYURI_ENDS_WITH = Function.XACML_NS_3_0 + "anyURI-ends-with";

	/**
	 * Standard identifier for the anyURI-contains-with function.
	 */
	public static final String NAME_ANYURI_CONTAINS = Function.XACML_NS_3_0 + "anyURI-contains";

	/**
	 * rfc822Name-match function
	 * 
	 */
	private static final Matcher<StringValue, RFC822NameValue> RFC822NAME_MATCHER = new Matcher<StringValue, RFC822NameValue>()
	{

		@Override
		public final boolean match(StringValue arg0, RFC822NameValue arg1)
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
	private static final Matcher<StringValue, AnyURIValue> ANYURI_STARTS_WITH_MATCHER = new Matcher<StringValue, AnyURIValue>()
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public final boolean match(StringValue prefix, AnyURIValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	};

	/**
	 * anyURI-ends-with matcher
	 */
	private static final Matcher<StringValue, AnyURIValue> ANYURI_ENDS_WITH_MATCHER = new Matcher<StringValue, AnyURIValue>()
	{
		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(StringValue suffix, AnyURIValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	};

	/**
	 * anyURI-contains matcher
	 * 
	 */
	private static final Matcher<StringValue, AnyURIValue> ANYURI_CONTAINS_MATCHER = new Matcher<StringValue, AnyURIValue>()
	{

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the other
		 */
		@Override
		public final boolean match(StringValue contained, AnyURIValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	};

	/**
	 * Function cluster
	 */
	public static final FunctionSet SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "non-equal-type-match",
	//
			new NonEqualTypeMatchFunction<>(NAME_RFC822NAME_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.RFC822NAME_FACTORY.getDatatype(), RFC822NAME_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_STARTS_WITH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(), ANYURI_STARTS_WITH_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_ENDS_WITH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(), ANYURI_ENDS_WITH_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_CONTAINS, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(), ANYURI_CONTAINS_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_REGEXP_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(),
					new RegexpMatchCallFactoryBuilder<AnyURIValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_IPADDRESS_REGEXP_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.IPADDRESS_FACTORY.getDatatype(),
					new RegexpMatchCallFactoryBuilder<IPAddressValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_DNSNAME_REGEXP_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DNSNAME_FACTORY.getDatatype(),
					new RegexpMatchCallFactoryBuilder<DNSNameWithPortRangeValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_RFC822NAME_REGEXP_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.RFC822NAME_FACTORY.getDatatype(),
					new RegexpMatchCallFactoryBuilder<RFC822NameValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_X500NAME_REGEXP_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.X500NAME_FACTORY.getDatatype(),
					new RegexpMatchCallFactoryBuilder<X500NameValue>()));

}
