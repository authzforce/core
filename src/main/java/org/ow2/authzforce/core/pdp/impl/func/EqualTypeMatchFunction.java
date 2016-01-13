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

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.impl.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.impl.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DNSNameValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DateValue;
import org.ow2.authzforce.core.pdp.impl.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.impl.value.DoubleValue;
import org.ow2.authzforce.core.pdp.impl.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.impl.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.impl.value.IntegerValue;
import org.ow2.authzforce.core.pdp.impl.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.impl.value.SimpleValue;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.value.TimeValue;
import org.ow2.authzforce.core.pdp.impl.value.X500NameValue;
import org.ow2.authzforce.core.pdp.impl.value.YearMonthDurationValue;

/**
 * Implements generic match functions taking parameters of same/equal type, i.e. standard (A.3.1) Equality predicates and special match function x500Name-match
 * 
 * @param <PARAM>
 *            type of compared parameters
 */
public class EqualTypeMatchFunction<PARAM extends AttributeValue> extends FirstOrderFunction.SingleParameterTyped<BooleanValue, PARAM>
{

	/**
	 * Standard identifier for the string-equal function.
	 */
	public static final String NAME_STRING_EQUAL = XACML_NS_1_0 + "string-equal";

	/**
	 * Standard identifier for the boolean-equal function.
	 */
	public static final String NAME_BOOLEAN_EQUAL = XACML_NS_1_0 + "boolean-equal";

	/**
	 * Standard identifier for the integer-equal function.
	 */
	public static final String NAME_INTEGER_EQUAL = XACML_NS_1_0 + "integer-equal";

	/**
	 * Standard identifier for the double-equal function.
	 */
	public static final String NAME_DOUBLE_EQUAL = XACML_NS_1_0 + "double-equal";

	/**
	 * Standard identifier for the date-equal function.
	 */
	public static final String NAME_DATE_EQUAL = XACML_NS_1_0 + "date-equal";

	/**
	 * Standard identifier for the time-equal function.
	 */
	public static final String NAME_TIME_EQUAL = XACML_NS_1_0 + "time-equal";

	/**
	 * Standard identifier for the dateTime-equal function.
	 */
	public static final String NAME_DATETIME_EQUAL = XACML_NS_1_0 + "dateTime-equal";

	/**
	 * Standard identifier for the dayTimeDuration-equal function.
	 */
	public static final String NAME_DAYTIME_DURATION_EQUAL = XACML_NS_3_0 + "dayTimeDuration-equal";

	/**
	 * Standard identifier for the yearMonthDuration-equal function.
	 */
	public static final String NAME_YEARMONTH_DURATION_EQUAL = XACML_NS_3_0 + "yearMonthDuration-equal";

	/**
	 * Standard identifier for the anyURI-equal function.
	 */
	public static final String NAME_ANYURI_EQUAL = XACML_NS_1_0 + "anyURI-equal";

	/**
	 * Standard identifier for the x500Name-equal function.
	 */
	public static final String NAME_X500NAME_EQUAL = XACML_NS_1_0 + "x500Name-equal";

	/**
	 * Standard identifier for the rfc822Name-equal function.
	 */
	public static final String NAME_RFC822NAME_EQUAL = XACML_NS_1_0 + "rfc822Name-equal";

	/**
	 * Standard identifier for the hexBinary-equal function.
	 */
	public static final String NAME_HEXBINARY_EQUAL = XACML_NS_1_0 + "hexBinary-equal";

	/**
	 * Standard identifier for the base64Binary-equal function.
	 */
	public static final String NAME_BASE64BINARY_EQUAL = XACML_NS_1_0 + "base64Binary-equal";

	/**
	 * Standard identifier for the ipAddress-equal function.
	 */
	public static final String NAME_IPADDRESS_EQUAL = XACML_NS_2_0 + "ipAddress-equal";

	/**
	 * Standard identifier for the dnsName-equal function.
	 */
	public static final String NAME_DNSNAME_EQUAL = XACML_NS_2_0 + "dnsName-equal";

	/**
	 * Standard identifier for the string-equal-ignore-case function.
	 */
	private static final String NAME_STRING_EQUAL_IGNORE_CASE = XACML_NS_3_0 + "string-equal-ignore-case";

	/**
	 * Standard identifier for the x500Name-match function (different from x500Name-regexp-match down below).
	 */
	public static final String NAME_X500NAME_MATCH = XACML_NS_1_0 + "x500Name-match";

	/**
	 * Standard identifier for the string-starts-with function.
	 */
	public static final String NAME_STRING_STARTS_WITH = XACML_NS_3_0 + "string-starts-with";

	/**
	 * Standard identifier for the string-ends-with function.
	 */
	public static final String NAME_STRING_ENDS_WITH = XACML_NS_3_0 + "string-ends-with";

	/**
	 * Standard identifier for the string-contains-with function.
	 */
	public static final String NAME_STRING_CONTAINS = XACML_NS_3_0 + "string-contains";

	/**
	 * Standard identifier for the string-regexp-match function.
	 */
	public static final String NAME_STRING_REGEXP_MATCH = XACML_NS_1_0 + "string-regexp-match";

	// /**
	// * Logger used for all classes
	// */
	// private static final Logger LOGGER = LoggerFactory
	// .getLogger(EqualTypeMatchFunction.class);

	private static class CallFactory<PARAM_T extends AttributeValue>
	{

		private final FunctionSignature.SingleParameterTyped<BooleanValue, PARAM_T> funcSig;
		private final Matcher<PARAM_T> matcher;

		private CallFactory(FunctionSignature.SingleParameterTyped<BooleanValue, PARAM_T> functionSignature, Matcher<PARAM_T> matcher)
		{
			this.funcSig = functionSignature;
			this.matcher = matcher;
		}

		protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
				throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<BooleanValue, PARAM_T>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected final BooleanValue evaluate(Deque<PARAM_T> args) throws IndeterminateEvaluationException
				{
					return BooleanValue.valueOf(matcher.match(args.poll(), args.poll()));
				}

			};
		}

	}

	private interface CallFactoryBuilder<PARAM_T extends AttributeValue>
	{
		CallFactory<PARAM_T> build(FunctionSignature.SingleParameterTyped<BooleanValue, PARAM_T> functionSignature);
	}

	private interface Matcher<PARAM_T extends AttributeValue>
	{
		boolean match(PARAM_T arg0, PARAM_T arg1);
	}

	private final CallFactory<PARAM> funcCallFactory;

	/**
	 * Creates a new <code>EqualTypeMatchFunction</code> object.
	 * 
	 * @param functionName
	 *            the standard XACML name of the function to be handled by this object, including the full namespace
	 * @param paramType
	 *            parameter type
	 */
	private EqualTypeMatchFunction(String functionName, Datatype<PARAM> paramType, Matcher<PARAM> matcher)
	{
		super(functionName, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, matcher);
	}

	private EqualTypeMatchFunction(String functionName, Datatype<PARAM> paramType, CallFactoryBuilder<PARAM> callFactoryBuilder)
	{
		super(functionName, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = callFactoryBuilder.build(functionSignature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

	/**
	 * *-equal function matcher
	 * 
	 * @param <PARAM>
	 *            parameter type
	 */
	private static final class EqualMatcher<PARAM extends AttributeValue> implements Matcher<PARAM>
	{
		@Override
		public boolean match(PARAM arg0, PARAM arg1)
		{
			return arg0.equals(arg1);
		}
	}

	/**
	 * *-equal-ignore-case function matcher
	 * 
	 * @param <PARAM>
	 *            parameter type
	 */
	private static final class EqualIgnoreCaseMatcher<PARAM extends SimpleValue<String>> implements Matcher<PARAM>
	{
		@Override
		public boolean match(PARAM arg0, PARAM arg1)
		{
			return arg0.getUnderlyingValue().equalsIgnoreCase(arg1.getUnderlyingValue());
		}
	}

	/**
	 * x500Name-match function matcher
	 * 
	 */
	private static final class X500NameMatcher implements Matcher<X500NameValue>
	{
		@Override
		public boolean match(X500NameValue arg0, X500NameValue arg1)
		{
			return arg0.match(arg1);
		}
	}

	/**
	 * string-starts-with function matcher. For other *-starts-with functions, see {@link NonEqualTypeMatchFunction} class.
	 */
	private static final class StringStartsWithMatcher implements Matcher<StringValue>
	{
		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public boolean match(StringValue prefix, StringValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	}

	/**
	 * string-ends-with function matcher
	 */
	private static class StringEndsWithMatcher implements Matcher<StringValue>
	{

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(StringValue suffix, StringValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	}

	/**
	 * string-contains function matcher
	 * 
	 */
	private static final class StringContainsMatcher implements Matcher<StringValue>
	{

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the other
		 */
		@Override
		public boolean match(StringValue contained, StringValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	}

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

		private StringRegexpMatchCallFactory(FunctionSignature.SingleParameterTyped<BooleanValue, StringValue> functionSignature)
		{
			super(functionSignature, STRING_REGEXP_MATCHER);
			regexFuncHelper = new RegexpMatchFunctionHelper(functionSignature, DatatypeConstants.STRING.TYPE);
		}

		@Override
		protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			final FirstOrderFunctionCall<BooleanValue> compiledRegexFuncCall = regexFuncHelper.getCompiledRegexMatchCall(argExpressions, remainingArgTypes);
			/*
			 * compiledRegexFuncCall == null means no optimization using a pre-compiled regex could be done; in this case, use super.newCall() as usual, which
			 * will call match() down below, compiling the regex on-the-fly for each evaluation.
			 */
			return compiledRegexFuncCall == null ? super.getInstance(argExpressions, remainingArgTypes) : compiledRegexFuncCall;
		}

	}

	private static final CallFactoryBuilder<StringValue> STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER = new CallFactoryBuilder<StringValue>()
	{

		@Override
		public CallFactory<StringValue> build(FunctionSignature.SingleParameterTyped<BooleanValue, StringValue> functionSignature)
		{
			return new StringRegexpMatchCallFactory(functionSignature);
		}

	};

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "equal-type-match",
	//
			new EqualTypeMatchFunction<>(NAME_STRING_EQUAL, DatatypeConstants.STRING.TYPE, new EqualMatcher<StringValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_BOOLEAN_EQUAL, DatatypeConstants.BOOLEAN.TYPE, new EqualMatcher<BooleanValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_INTEGER_EQUAL, DatatypeConstants.INTEGER.TYPE, new EqualMatcher<IntegerValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DOUBLE_EQUAL, DatatypeConstants.DOUBLE.TYPE, new EqualMatcher<DoubleValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DATE_EQUAL, DatatypeConstants.DATE.TYPE, new EqualMatcher<DateValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_TIME_EQUAL, DatatypeConstants.TIME.TYPE, new EqualMatcher<TimeValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DATETIME_EQUAL, DatatypeConstants.DATETIME.TYPE, new EqualMatcher<DateTimeValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DAYTIME_DURATION_EQUAL, DatatypeConstants.DAYTIMEDURATION.TYPE, new EqualMatcher<DayTimeDurationValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_YEARMONTH_DURATION_EQUAL, DatatypeConstants.YEARMONTHDURATION.TYPE, new EqualMatcher<YearMonthDurationValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_ANYURI_EQUAL, DatatypeConstants.ANYURI.TYPE, new EqualMatcher<AnyURIValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_X500NAME_EQUAL, DatatypeConstants.X500NAME.TYPE, new EqualMatcher<X500NameValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_RFC822NAME_EQUAL, DatatypeConstants.RFC822NAME.TYPE, new EqualMatcher<RFC822NameValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_HEXBINARY_EQUAL, DatatypeConstants.HEXBINARY.TYPE, new EqualMatcher<HexBinaryValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_BASE64BINARY_EQUAL, DatatypeConstants.BASE64BINARY.TYPE, new EqualMatcher<Base64BinaryValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_IPADDRESS_EQUAL, DatatypeConstants.IPADDRESS.TYPE, new EqualMatcher<IPAddressValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_DNSNAME_EQUAL, DatatypeConstants.DNSNAME.TYPE, new EqualMatcher<DNSNameValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_EQUAL_IGNORE_CASE, DatatypeConstants.STRING.TYPE, new EqualIgnoreCaseMatcher<StringValue>()),
			//
			new EqualTypeMatchFunction<>(NAME_X500NAME_MATCH, DatatypeConstants.X500NAME.TYPE, new X500NameMatcher()),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_STARTS_WITH, DatatypeConstants.STRING.TYPE, new StringStartsWithMatcher()),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_ENDS_WITH, DatatypeConstants.STRING.TYPE, new StringEndsWithMatcher()),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_CONTAINS, DatatypeConstants.STRING.TYPE, new StringContainsMatcher()),
			//
			new EqualTypeMatchFunction<>(NAME_STRING_REGEXP_MATCH, DatatypeConstants.STRING.TYPE, STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER));

}
