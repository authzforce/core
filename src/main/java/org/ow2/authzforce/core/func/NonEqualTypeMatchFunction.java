/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.func;

import java.util.Deque;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;
import com.thalesgroup.authzforce.core.datatypes.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;
import com.thalesgroup.authzforce.core.datatypes.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.StringAttributeValue;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;

/**
 * Implements generic match functions taking two parameters of possibly different types, e.g. a
 * string and a URI.
 * 
 * @param <T0>
 *            Type of the first parameter of this function.
 * @param <T1>
 *            Type of the second parameter of this function.
 * 
 */
public abstract class NonEqualTypeMatchFunction<T0 extends AttributeValue<T0>, T1 extends AttributeValue<T1>> extends FirstOrderFunction<BooleanAttributeValue>
{
	/**
	 * Standard identifier for the rfc822Name-match function (different from rfc822Name-regexp-match
	 * down below).
	 */
	public static final String NAME_RFC822NAME_MATCH = FUNCTION_NS_1 + "rfc822Name-match";

	/**
	 * Standard identifier for the anyURI-regexp-match function.
	 */
	public static final String NAME_ANYURI_REGEXP_MATCH = FUNCTION_NS_2 + "anyURI-regexp-match";

	/**
	 * Standard identifier for the ipAddress-regexp-match function.
	 */
	public static final String NAME_IPADDRESS_REGEXP_MATCH = FUNCTION_NS_2 + "ipAddress-regexp-match";

	/**
	 * Standard identifier for the dnsName-regexp-match function.
	 */
	public static final String NAME_DNSNAME_REGEXP_MATCH = FUNCTION_NS_2 + "dnsName-regexp-match";

	/**
	 * Standard identifier for the rfc822Name-regexp-match function.
	 */
	public static final String NAME_RFC822NAME_REGEXP_MATCH = FUNCTION_NS_2 + "rfc822Name-regexp-match";

	/**
	 * Standard identifier for the x500Name-regexp-match function.
	 */
	public static final String NAME_X500NAME_REGEXP_MATCH = FUNCTION_NS_2 + "x500Name-regexp-match";

	/**
	 * Standard identifier for the anyURI-starts-with function.
	 */
	public static final String NAME_ANYURI_STARTS_WITH = FUNCTION_NS_3 + "anyURI-starts-with";

	/**
	 * Standard identifier for the anyURI-ends-with function.
	 */
	public static final String NAME_ANYURI_ENDS_WITH = FUNCTION_NS_3 + "anyURI-ends-with";

	/**
	 * Standard identifier for the anyURI-contains-with function.
	 */
	public static final String NAME_ANYURI_CONTAINS = FUNCTION_NS_3 + "anyURI-contains";

	private final Class<T0> paramClass0;
	protected final Class<T1> paramClass1;
	protected final Datatype<T1> paramType1;

	private final String invalidArgTypesErrorMsg;
	private final String invalidRegexErrorMsg = "Function " + this.functionId + ": Invalid regular expression in arg#0";

	/**
	 * Creates a new <code>NonEqualTypeMatchFunction</code> based on the given name.
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param paramType0
	 *            first parameter type
	 * @param paramType1
	 *            second parameter type
	 * 
	 */
	public NonEqualTypeMatchFunction(String functionName, Datatype<T0> paramType0, Datatype<T1> paramType1)
	{
		super(functionName, DatatypeConstants.BOOLEAN.TYPE, false, paramType0, paramType1);
		this.paramClass0 = paramType0.getValueClass();
		this.paramType1 = paramType1;
		this.paramClass1 = paramType1.getValueClass();
		invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + paramType0 + "," + paramType1 + "): ";
	}

	/**
	 * Evaluate function with second parameter as string
	 * 
	 * @param arg0
	 *            first function parameter
	 * @param arg1
	 *            second function parameter
	 * @return true if and only if both arguments match according to the function definition
	 * @throws IllegalArgumentException
	 *             if one of the arguments is not valid for this function
	 */
	protected abstract boolean match(T0 arg0, T1 arg1) throws IllegalArgumentException;

	@Override
	protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/*
		 * Actual argument types are expected to be different, therefore we use the supertype
		 * AttributeValue as generic parameter type for all when creating the function call
		 */
		return new EagerMultiPrimitiveTypeEval<BooleanAttributeValue>(signature, argExpressions, remainingArgTypes)
		{

			@Override
			protected final BooleanAttributeValue evaluate(Deque<AttributeValue<?>> args) throws IndeterminateEvaluationException
			{
				final AttributeValue<?> rawArg0 = args.poll();
				final AttributeValue<?> rawArg1 = args.poll();

				final T0 arg0;
				final T1 arg1;
				try
				{
					arg0 = paramClass0.cast(rawArg0);
					arg1 = paramClass1.cast(rawArg1);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + "," + rawArg1.getDataType(), StatusHelper.STATUS_PROCESSING_ERROR, e);
				}

				final boolean isMatched;
				try
				{
					isMatched = match(arg0, arg1);
				} catch (PatternSyntaxException e)
				{
					throw new IndeterminateEvaluationException(invalidRegexErrorMsg, StatusHelper.STATUS_PROCESSING_ERROR, e);
				}

				return BooleanAttributeValue.valueOf(isMatched);
			}

		};
	}

	/**
	 * rfc822Name-match function
	 * 
	 */
	private static class RFC822NameMatch extends NonEqualTypeMatchFunction<StringAttributeValue, RFC822NameAttributeValue>
	{

		/**
		 * Instantiates the function
		 */
		public RFC822NameMatch()
		{
			super(NAME_RFC822NAME_MATCH, DatatypeConstants.STRING.TYPE, DatatypeConstants.RFC822NAME.TYPE);
		}

		@Override
		public final boolean match(StringAttributeValue arg0, RFC822NameAttributeValue arg1)
		{
			return arg1.match(arg0.getUnderlyingValue());
		}
	}

	/**
	 * *-regexp-match function
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	private static class RegexpMatch<AV extends SimpleAttributeValue<String, AV>> extends NonEqualTypeMatchFunction<StringAttributeValue, AV>
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.thalesgroup.authzforce.core.func.NonEqualTypeMatchFunction#getFunctionCall(java.util
		 * .List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
		 */
		@Override
		protected final FirstOrderFunctionCall<BooleanAttributeValue> newCall(final List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			final RegexpMatchFunctionHelper regexFuncHelper = new RegexpMatchFunctionHelper(signature, paramType1);
			final FirstOrderFunctionCall<BooleanAttributeValue> compiledRegexFuncCall = regexFuncHelper.getCompiledRegexMatchCall(argExpressions, remainingArgTypes);
			/*
			 * compiledRegexFuncCall == null means no optimization using a pre-compiled regex could
			 * be done; in this case, use super.newCall() as usual, which will call match() down
			 * below, compiling the regex on-the-fly for each evaluation.
			 */
			return compiledRegexFuncCall == null ? super.newCall(argExpressions, remainingArgTypes) : compiledRegexFuncCall;
		}

		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamType
		 *            second parameter type
		 */
		public RegexpMatch(String functionName, Datatype<AV> secondParamType)
		{
			super(functionName, DatatypeConstants.STRING.TYPE, secondParamType);
		}

		@Override
		public final boolean match(StringAttributeValue regex, AV arg1) throws IllegalArgumentException
		{
			return RegexpMatchFunctionHelper.match(regex, arg1);
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
	 * anyURI-starts-with function. For string-starts-with, see {@link EqualTypeMatchFunction}
	 * class.
	 * 
	 */
	private static class AnyURIStartsWith extends NonEqualTypeMatchFunction<StringAttributeValue, AnyURIAttributeValue>
	{
		/**
		 * Instantiates the function
		 * 
		 */
		public AnyURIStartsWith()
		{
			super(NAME_ANYURI_STARTS_WITH, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE);
		}

		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public final boolean match(StringAttributeValue prefix, AnyURIAttributeValue arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	}

	/**
	 * anyURI-ends-with function
	 */
	private static class AnyURIEndsWith extends NonEqualTypeMatchFunction<StringAttributeValue, AnyURIAttributeValue>
	{
		/**
		 * Instantiates the function
		 * 
		 */
		public AnyURIEndsWith()
		{
			super(NAME_ANYURI_ENDS_WITH, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE);
		}

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(StringAttributeValue suffix, AnyURIAttributeValue arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	}

	/**
	 * anyURI-contains function
	 * 
	 */
	private static class AnyURIContains extends NonEqualTypeMatchFunction<StringAttributeValue, AnyURIAttributeValue>
	{

		/**
		 * Instantiates the function
		 * 
		 */
		public AnyURIContains()
		{
			super(NAME_ANYURI_CONTAINS, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE);
		}

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the
		 * other
		 */
		@Override
		public final boolean match(StringAttributeValue contained, AnyURIAttributeValue arg1)
		{
			return arg1.getUnderlyingValue().contains(contained.getUnderlyingValue());
		}
	}

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "non-equal-type-match",
	//
			new RFC822NameMatch(),
			//
			new RegexpMatch<>(NAME_ANYURI_REGEXP_MATCH, DatatypeConstants.ANYURI.TYPE),
			//
			new RegexpMatch<>(NAME_IPADDRESS_REGEXP_MATCH, DatatypeConstants.IPADDRESS.TYPE),
			//
			new RegexpMatch<>(NAME_DNSNAME_REGEXP_MATCH, DatatypeConstants.DNSNAME.TYPE),
			//
			new RegexpMatch<>(NAME_RFC822NAME_REGEXP_MATCH, DatatypeConstants.RFC822NAME.TYPE),
			//
			new RegexpMatch<>(NAME_X500NAME_REGEXP_MATCH, DatatypeConstants.X500NAME.TYPE),
			//
			new AnyURIStartsWith(),
			//
			new AnyURIEndsWith(),
			//
			new AnyURIContains());

}
