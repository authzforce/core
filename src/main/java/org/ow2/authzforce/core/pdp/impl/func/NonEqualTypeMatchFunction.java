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
import java.util.regex.PatternSyntaxException;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.impl.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DNSNameValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.impl.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.impl.value.SimpleValue;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.value.X500NameValue;

/**
 * Implements generic match functions taking two parameters of possibly different types, e.g. a string and a URI.
 *
 * @param <T0>
 *            Type of the first parameter of this function.
 * @param <T1>
 *            Type of the second parameter of this function.
 * 
 * @version $Id: $
 */
public final class NonEqualTypeMatchFunction<T0 extends AttributeValue, T1 extends AttributeValue> extends FirstOrderFunction.MultiParameterTyped<BooleanValue>
{
	/**
	 * Standard identifier for the rfc822Name-match function (different from rfc822Name-regexp-match down below).
	 */
	public static final String NAME_RFC822NAME_MATCH = XACML_NS_1_0 + "rfc822Name-match";

	/**
	 * Standard identifier for the anyURI-regexp-match function.
	 */
	public static final String NAME_ANYURI_REGEXP_MATCH = XACML_NS_2_0 + "anyURI-regexp-match";

	/**
	 * Standard identifier for the ipAddress-regexp-match function.
	 */
	public static final String NAME_IPADDRESS_REGEXP_MATCH = XACML_NS_2_0 + "ipAddress-regexp-match";

	/**
	 * Standard identifier for the dnsName-regexp-match function.
	 */
	public static final String NAME_DNSNAME_REGEXP_MATCH = XACML_NS_2_0 + "dnsName-regexp-match";

	/**
	 * Standard identifier for the rfc822Name-regexp-match function.
	 */
	public static final String NAME_RFC822NAME_REGEXP_MATCH = XACML_NS_2_0 + "rfc822Name-regexp-match";

	/**
	 * Standard identifier for the x500Name-regexp-match function.
	 */
	public static final String NAME_X500NAME_REGEXP_MATCH = XACML_NS_2_0 + "x500Name-regexp-match";

	/**
	 * Standard identifier for the anyURI-starts-with function.
	 */
	public static final String NAME_ANYURI_STARTS_WITH = XACML_NS_3_0 + "anyURI-starts-with";

	/**
	 * Standard identifier for the anyURI-ends-with function.
	 */
	public static final String NAME_ANYURI_ENDS_WITH = XACML_NS_3_0 + "anyURI-ends-with";

	/**
	 * Standard identifier for the anyURI-contains-with function.
	 */
	public static final String NAME_ANYURI_CONTAINS = XACML_NS_3_0 + "anyURI-contains";

	private interface Matcher<T0 extends AttributeValue, T1 extends AttributeValue>
	{
		/**
		 * Evaluate function with second parameter as string
		 * 
		 * @param arg0
		 *            first function parameter
		 * @param arg1
		 *            second function parameter
		 * @return true if and only if both arguments match according to the matcher definition
		 * @throws IllegalArgumentException
		 *             if one of the arguments is not valid for this matcher
		 */
		boolean match(T0 arg0, T1 arg1) throws IllegalArgumentException;
	}

	private static class CallFactory<T0 extends AttributeValue, T1 extends AttributeValue>
	{
		private final String invalidArgTypesErrorMsg;
		private final String invalidRegexErrorMsg;
		private final Class<T0> paramClass0;
		private final Class<T1> paramClass1;
		private final Matcher<T0, T1> matcher;
		private final FunctionSignature<BooleanValue> funcSig;

		private CallFactory(FunctionSignature<BooleanValue> functionSig, Datatype<T0> paramType0, Datatype<T1> paramType1, Matcher<T0, T1> matcher)
		{

			this.invalidArgTypesErrorMsg = "Function " + functionSig.getName() + ": Invalid arg types: expected: " + paramType0 + "," + paramType1
					+ "; actual: ";
			this.invalidRegexErrorMsg = "Function " + functionSig.getName() + ": Invalid regular expression in arg#0";
			this.paramClass0 = paramType0.getValueClass();
			this.paramClass1 = paramType1.getValueClass();
			this.matcher = matcher;
			this.funcSig = functionSig;
		}

		protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			return new EagerMultiPrimitiveTypeEval<BooleanValue>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected final BooleanValue evaluate(Deque<AttributeValue> args) throws IndeterminateEvaluationException
				{
					final AttributeValue rawArg0 = args.poll();
					final AttributeValue rawArg1 = args.poll();

					final T0 arg0;
					final T1 arg1;
					try
					{
						arg0 = paramClass0.cast(rawArg0);
						arg1 = paramClass1.cast(rawArg1);
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + ", " + rawArg1.getDataType(),
								StatusHelper.STATUS_PROCESSING_ERROR, e);
					}

					final boolean isMatched;
					try
					{
						isMatched = matcher.match(arg0, arg1);
					} catch (PatternSyntaxException e)
					{
						throw new IndeterminateEvaluationException(invalidRegexErrorMsg, StatusHelper.STATUS_PROCESSING_ERROR, e);
					}

					return BooleanValue.valueOf(isMatched);
				}
			};
		}

	}

	private interface CallFactoryBuilder<T0 extends AttributeValue, T1 extends AttributeValue>
	{
		CallFactory<T0, T1> build(FunctionSignature<BooleanValue> functionSignature, Datatype<T0> paramType0, Datatype<T1> paramType1);
	}

	private final CallFactory<T0, T1> funcCallFactory;

	/**
	 * Creates a new <code>NonEqualTypeMatchFunction</code> based on the given name.
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param paramType0
	 *            first parameter type
	 * @param paramType1
	 *            second parameter type
	 * @param matcher
	 *            matching algorithm
	 * 
	 */
	private NonEqualTypeMatchFunction(String functionName, Datatype<T0> paramType0, Datatype<T1> paramType1, Matcher<T0, T1> matcher)
	{
		super(functionName, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(paramType0, paramType1));
		this.funcCallFactory = new CallFactory<>(this.functionSignature, paramType0, paramType1, matcher);
	}

	private NonEqualTypeMatchFunction(String functionName, Datatype<T0> paramType0, Datatype<T1> paramType1, CallFactoryBuilder<T0, T1> callFactoryBuilder)
	{
		super(functionName, DatatypeConstants.BOOLEAN.TYPE, false, Arrays.asList(paramType0, paramType1));
		this.funcCallFactory = callFactoryBuilder.build(functionSignature, paramType0, paramType1);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/*
		 * Actual argument types are expected to be different, therefore we use the supertype AttributeValue as generic parameter type for all when creating the
		 * function call
		 */
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

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

	/**
	 * *-regexp-match function
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	private static class RegexpMatchCallFactoryBuilder<AV extends SimpleValue<String>> implements CallFactoryBuilder<StringValue, AV>
	{

		private final Matcher<StringValue, AV> regexMatcher = new Matcher<StringValue, AV>()
		{
			@Override
			public boolean match(StringValue regex, AV arg1)
			{
				return RegexpMatchFunctionHelper.match(regex, arg1);
			}
		};

		private class RegexpMatchCallFactory extends CallFactory<StringValue, AV>
		{
			private final RegexpMatchFunctionHelper regexFuncHelper;

			private RegexpMatchCallFactory(FunctionSignature<BooleanValue> functionSignature, Datatype<AV> secondParamType)
			{
				super(functionSignature, DatatypeConstants.STRING.TYPE, secondParamType, regexMatcher);
				regexFuncHelper = new RegexpMatchFunctionHelper(functionSignature, secondParamType);
			}

			@Override
			protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
			{
				final FirstOrderFunctionCall<BooleanValue> compiledRegexFuncCall = regexFuncHelper.getCompiledRegexMatchCall(argExpressions, remainingArgTypes);
				/*
				 * compiledRegexFuncCall == null means no optimization using a pre-compiled regex could be done; in this case, use super.newCall() as usual,
				 * which will call match() down below, compiling the regex on-the-fly for each evaluation.
				 */
				return compiledRegexFuncCall == null ? super.getInstance(argExpressions, remainingArgTypes) : compiledRegexFuncCall;
			}
		}

		@Override
		public CallFactory<StringValue, AV> build(FunctionSignature<BooleanValue> functionSignature, Datatype<StringValue> paramType0, Datatype<AV> paramType1)
		{
			return new RegexpMatchCallFactory(functionSignature, paramType1);
		}

	}

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
	public static final FunctionSet CLUSTER = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "non-equal-type-match",
	//
			new NonEqualTypeMatchFunction<>(NAME_RFC822NAME_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.RFC822NAME.TYPE, RFC822NAME_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_STARTS_WITH, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE, ANYURI_STARTS_WITH_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_ENDS_WITH, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE, ANYURI_ENDS_WITH_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_CONTAINS, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE, ANYURI_CONTAINS_MATCHER),
			//
			new NonEqualTypeMatchFunction<>(NAME_ANYURI_REGEXP_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE,
					new RegexpMatchCallFactoryBuilder<AnyURIValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_IPADDRESS_REGEXP_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.IPADDRESS.TYPE,
					new RegexpMatchCallFactoryBuilder<IPAddressValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_DNSNAME_REGEXP_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.DNSNAME.TYPE,
					new RegexpMatchCallFactoryBuilder<DNSNameValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_RFC822NAME_REGEXP_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.RFC822NAME.TYPE,
					new RegexpMatchCallFactoryBuilder<RFC822NameValue>()),
			//
			new NonEqualTypeMatchFunction<>(NAME_X500NAME_REGEXP_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.X500NAME.TYPE,
					new RegexpMatchCallFactoryBuilder<X500NameValue>()));

}
