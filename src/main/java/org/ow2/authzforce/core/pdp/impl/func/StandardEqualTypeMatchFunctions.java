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
import org.ow2.authzforce.core.pdp.api.func.BaseFunctionSet;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.CallFactory;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.CallFactoryBuilder;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.EqualIgnoreCaseMatcher;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.EqualMatcher;
import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction.Matcher;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionSet;
import org.ow2.authzforce.core.pdp.api.func.RegexpMatchFunctionHelper;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunctionSignature;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;

/**
 * Standard match functions taking parameters of same/equal type, i.e. standard (A.3.1) Equality predicates, special match function x500Name-match, string-starts-with/contains/ends-with.
 * <p>
 * Note that there are no such functions as ipAddress-equal and dnsName-equal functions in the XACML core specification. Regexp-match alternatives should be used intead. More info:
 * https://lists.oasis-open.org/archives/xacml-comment/200411/msg00002.html
 *
 * @version $Id: $
 */
public final class StandardEqualTypeMatchFunctions
{
	/**
	 * Standard identifier for the string-equal function.
	 */
	public static final String NAME_STRING_EQUAL = Function.XACML_NS_1_0 + "string-equal";

	/**
	 * Standard identifier for the boolean-equal function.
	 */
	public static final String NAME_BOOLEAN_EQUAL = Function.XACML_NS_1_0 + "boolean-equal";

	/**
	 * Standard identifier for the integer-equal function.
	 */
	public static final String NAME_INTEGER_EQUAL = Function.XACML_NS_1_0 + "integer-equal";

	/**
	 * Standard identifier for the double-equal function.
	 */
	public static final String NAME_DOUBLE_EQUAL = Function.XACML_NS_1_0 + "double-equal";

	/**
	 * Standard identifier for the date-equal function.
	 */
	public static final String NAME_DATE_EQUAL = Function.XACML_NS_1_0 + "date-equal";

	/**
	 * Standard identifier for the time-equal function.
	 */
	public static final String NAME_TIME_EQUAL = Function.XACML_NS_1_0 + "time-equal";

	/**
	 * Standard identifier for the dateTime-equal function.
	 */
	public static final String NAME_DATETIME_EQUAL = Function.XACML_NS_1_0 + "dateTime-equal";

	/**
	 * Standard identifier for the dayTimeDuration-equal function.
	 */
	public static final String NAME_DAYTIME_DURATION_EQUAL = Function.XACML_NS_3_0 + "dayTimeDuration-equal";

	/**
	 * Standard identifier for the yearMonthDuration-equal function.
	 */
	public static final String NAME_YEARMONTH_DURATION_EQUAL = Function.XACML_NS_3_0 + "yearMonthDuration-equal";

	/**
	 * Standard identifier for the anyURI-equal function.
	 */
	public static final String NAME_ANYURI_EQUAL = Function.XACML_NS_1_0 + "anyURI-equal";

	/**
	 * Standard identifier for the x500Name-equal function.
	 */
	public static final String NAME_X500NAME_EQUAL = Function.XACML_NS_1_0 + "x500Name-equal";

	/**
	 * Standard identifier for the rfc822Name-equal function.
	 */
	public static final String NAME_RFC822NAME_EQUAL = Function.XACML_NS_1_0 + "rfc822Name-equal";

	/**
	 * Standard identifier for the hexBinary-equal function.
	 */
	public static final String NAME_HEXBINARY_EQUAL = Function.XACML_NS_1_0 + "hexBinary-equal";

	/**
	 * Standard identifier for the base64Binary-equal function.
	 */
	public static final String NAME_BASE64BINARY_EQUAL = Function.XACML_NS_1_0 + "base64Binary-equal";

	/**
	 * Standard identifier for the string-equal-ignore-case function.
	 */
	private static final String NAME_STRING_EQUAL_IGNORE_CASE = Function.XACML_NS_3_0 + "string-equal-ignore-case";

	/**
	 * Standard identifier for the x500Name-match function (different from x500Name-regexp-match down below).
	 */
	public static final String NAME_X500NAME_MATCH = Function.XACML_NS_1_0 + "x500Name-match";

	/**
	 * Standard identifier for the string-starts-with function.
	 */
	public static final String NAME_STRING_STARTS_WITH = Function.XACML_NS_3_0 + "string-starts-with";

	/**
	 * Standard identifier for the string-ends-with function.
	 */
	public static final String NAME_STRING_ENDS_WITH = Function.XACML_NS_3_0 + "string-ends-with";

	/**
	 * Standard identifier for the string-contains-with function.
	 */
	public static final String NAME_STRING_CONTAINS = Function.XACML_NS_3_0 + "string-contains";

	/**
	 * Standard identifier for the string-regexp-match function.
	 */
	public static final String NAME_STRING_REGEXP_MATCH = Function.XACML_NS_1_0 + "string-regexp-match";

	/**
	 * x500Name-match function matcher
	 * 
	 */
	private static final Matcher<X500NameValue> X500NAME_MATCHER = new Matcher<X500NameValue>()
	{
		@Override
		public boolean match(X500NameValue arg0, X500NameValue arg1)
		{
			return arg0.match(arg1);
		}
	};

	/**
	 * string-starts-with function matcher. For other *-starts-with functions, see {@link org.ow2.authzforce.core.pdp.api.func.NonEqualTypeMatchFunction} class.
	 */
	private static final Matcher<StringValue> STRING_STARTS_WITH_MATCHER = new Matcher<StringValue>()
	{
		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public boolean match(StringValue prefix, StringValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	};

	/**
	 * string-ends-with function matcher
	 */
	private static final Matcher<StringValue> STRING_ENDS_WITH_MATCHER = new Matcher<StringValue>()
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(StringValue suffix, StringValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	};

	/**
	 * string-contains function matcher
	 * 
	 */
	private static final Matcher<StringValue> STRING_CONTAINS_MATCHER = new Matcher<StringValue>()
	{

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the other
		 */
		@Override
		public boolean match(StringValue contained, StringValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	};

	private static final class StringRegexpMatchCallFactory extends CallFactory<StringValue>
	{
		private static final Matcher<StringValue> STRING_REGEXP_MATCHER = new Matcher<StringValue>()
		{
			@Override
			public boolean match(StringValue regex, StringValue arg1)
			{
				return RegexpMatchFunctionHelper.match(regex, arg1);
			}
		};

		private final RegexpMatchFunctionHelper regexFuncHelper;

		private StringRegexpMatchCallFactory(SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, StringValue> functionSignature)
		{
			super(functionSignature, STRING_REGEXP_MATCHER);
			regexFuncHelper = new RegexpMatchFunctionHelper(functionSignature, StandardDatatypes.STRING_FACTORY.getDatatype());
		}

		@Override
		protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			final FirstOrderFunctionCall<BooleanValue> compiledRegexFuncCall = regexFuncHelper.getCompiledRegexMatchCall(argExpressions, remainingArgTypes);
			/*
			 * compiledRegexFuncCall == null means no optimization using a pre-compiled regex could be done; in this case, use super.newCall() as usual, which will call match() down below, compiling
			 * the regex on-the-fly for each evaluation.
			 */
			return compiledRegexFuncCall == null ? super.getInstance(argExpressions, remainingArgTypes) : compiledRegexFuncCall;
		}

	}

	private static final CallFactoryBuilder<StringValue> STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER = new CallFactoryBuilder<StringValue>()
	{

		@Override
		public CallFactory<StringValue> build(SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, StringValue> functionSignature)
		{
			return new StringRegexpMatchCallFactory(functionSignature);
		}

	};

	/**
	 * Function set
	 */
	public static final FunctionSet SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "equal-type-match",
	//
			new EqualTypeMatchFunction<>(NAME_STRING_EQUAL, StandardDatatypes.STRING_FACTORY.getDatatype(), new EqualMatcher<StringValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_BOOLEAN_EQUAL, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), new EqualMatcher<BooleanValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_INTEGER_EQUAL, StandardDatatypes.INTEGER_FACTORY.getDatatype(), new EqualMatcher<IntegerValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DOUBLE_EQUAL, StandardDatatypes.DOUBLE_FACTORY.getDatatype(), new EqualMatcher<DoubleValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DATE_EQUAL, StandardDatatypes.DATE_FACTORY.getDatatype(), new EqualMatcher<DateValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_TIME_EQUAL, StandardDatatypes.TIME_FACTORY.getDatatype(), new EqualMatcher<TimeValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DATETIME_EQUAL, StandardDatatypes.DATETIME_FACTORY.getDatatype(), new EqualMatcher<DateTimeValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DAYTIME_DURATION_EQUAL, StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(), new EqualMatcher<DayTimeDurationValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_YEARMONTH_DURATION_EQUAL, StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(), new EqualMatcher<YearMonthDurationValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_ANYURI_EQUAL, StandardDatatypes.ANYURI_FACTORY.getDatatype(), new EqualMatcher<AnyURIValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_X500NAME_EQUAL, StandardDatatypes.X500NAME_FACTORY.getDatatype(), new EqualMatcher<X500NameValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_RFC822NAME_EQUAL, StandardDatatypes.RFC822NAME_FACTORY.getDatatype(), new EqualMatcher<RFC822NameValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_HEXBINARY_EQUAL, StandardDatatypes.HEXBINARY_FACTORY.getDatatype(), new EqualMatcher<HexBinaryValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_BASE64BINARY_EQUAL, StandardDatatypes.BASE64BINARY_FACTORY.getDatatype(), new EqualMatcher<Base64BinaryValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_EQUAL_IGNORE_CASE, StandardDatatypes.STRING_FACTORY.getDatatype(), new EqualIgnoreCaseMatcher<StringValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_X500NAME_MATCH, StandardDatatypes.X500NAME_FACTORY.getDatatype(), X500NAME_MATCHER),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_STARTS_WITH, StandardDatatypes.STRING_FACTORY.getDatatype(), STRING_STARTS_WITH_MATCHER),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_ENDS_WITH, StandardDatatypes.STRING_FACTORY.getDatatype(), STRING_ENDS_WITH_MATCHER),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_CONTAINS, StandardDatatypes.STRING_FACTORY.getDatatype(), STRING_CONTAINS_MATCHER),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_REGEXP_MATCH, StandardDatatypes.STRING_FACTORY.getDatatype(), STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER));

	private StandardEqualTypeMatchFunctions()
	{
		// empty private constructor to prevent instantiation
	}

}
