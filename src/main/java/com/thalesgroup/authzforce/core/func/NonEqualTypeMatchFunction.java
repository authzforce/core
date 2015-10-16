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

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.saxon.Configuration;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

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
	 * Standard identifier for the string-regexp-match function.
	 */
	public static final String NAME_STRING_REGEXP_MATCH = FUNCTION_NS_1 + "string-regexp-match";

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
	 * Standard identifier for the string-starts-with function.
	 */
	public static final String NAME_STRING_STARTS_WITH = FUNCTION_NS_3 + "string-starts-with";

	/**
	 * Standard identifier for the string-ends-with function.
	 */
	public static final String NAME_STRING_ENDS_WITH = FUNCTION_NS_3 + "string-ends-with";

	/**
	 * Standard identifier for the string-contains-with function.
	 */
	public static final String NAME_STRING_CONTAINS = FUNCTION_NS_3 + "string-contains";

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

	protected final Class<T0> paramClass0;
	protected final Class<T1> paramClass1;

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
		this.paramClass1 = paramType1.getValueClass();
		invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + paramClass0.getSimpleName() + "," + paramClass1.getSimpleName() + "): ";
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
		return new EagerPrimitiveEval<BooleanAttributeValue, AttributeValue>(signature, AttributeValue[].class, argExpressions, remainingArgTypes)
		{

			@Override
			protected final BooleanAttributeValue evaluate(AttributeValue[] args) throws IndeterminateEvaluationException
			{
				final T0 arg0;
				final T1 arg1;
				try
				{
					arg0 = paramClass0.cast(args[0]);
					arg1 = paramClass1.cast(args[1]);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + args[0].getClass().getSimpleName() + "," + args[1].getClass().getSimpleName(), Status.STATUS_PROCESSING_ERROR, e);
				}

				final boolean isMatched;
				try
				{
					isMatched = match(arg0, arg1);
				} catch (PatternSyntaxException e)
				{
					throw new IndeterminateEvaluationException(invalidRegexErrorMsg, Status.STATUS_PROCESSING_ERROR, e);
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
	 * <p>
	 * WARNING: the regular expression syntax required by XACML refers to the
	 * <code>xf:matches</code> function from [XF] (see the XACML core spec for this reference). This
	 * function and associated syntax differ from {@link Pattern} (Java 7) in several ways.
	 * Therefore, we cannot use {@link Pattern} directly. Find examples of differences below:
	 * <ul>
	 * <li>{@link Pattern} matches the entire string against the pattern always, whereas
	 * <code>xf:matches</code> considers the string to match the pattern if any substring matches
	 * the pattern.</li>
	 * <li><code>xf:matches</code> regular expression syntax is based on XML schema which defines
	 * character class substraction using '-' character, whereas {@link Pattern} does not support
	 * this syntax but <code>&&[^</code> instead.</li>
	 * <li>
	 * Category escape: can be done in XML SCHEMA with: <code>[\P{X}]</code>. {@link Pattern} only
	 * supports this form: <code>[^\p{X}]</code>.</li>
	 * <li>
	 * Character classes: XML schema define categories <code>\c</code> and <code>\C</code>.
	 * {@link Pattern} does not support them.</li>
	 * </ul>
	 * EXAMPLE: this regex from XML schema spec uses character class substraction. It is valid for
	 * <code>xf:matches</code> but does not compile with {@link Pattern}:
	 * 
	 * <pre>
	 * [\i-[:]][\c-[:]]*
	 * </pre>
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	private static class RegexpMatch<AV extends SimpleAttributeValue<String, AV>> extends NonEqualTypeMatchFunction<StringAttributeValue, AV>
	{

		private final String indeterminateArg1TypeMessage = "Function " + functionId + ": Invalid type (expected = " + paramClass1.getName() + ") of arg#1: ";

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
			// check if first arg = regex is constant value, in which case pre-compile the regex
			final RegularExpression compiledRegex;
			if (argExpressions.isEmpty())
			{
				compiledRegex = null;
			} else
			{
				final Expression<?> input0 = argExpressions.get(0);
				if (input0.isStatic())
				{
					final StringAttributeValue input0Val;
					try
					{
						input0Val = Utils.evalSingle(input0, null, StringAttributeValue.class);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IllegalArgumentException("Function " + functionId + ": Error pre-evaluating static expression of arg #0 (in null context): " + input0, e);
					}
					final String regex = input0Val.getUnderlyingValue();
					try
					{
						/*
						 * From Saxon xf:matches() implementation: Matches#evaluateItem() /
						 * evalMatches()
						 */
						compiledRegex = Configuration.getPlatform().compileRegularExpression(regex, "", "XP20", null);
					} catch (XPathException e)
					{
						throw new IllegalArgumentException("Function " + functionId + ": Invalid regular expression in arg #0 (evaluated as static expression): '" + regex + "'", e);
					}
				} else
				{
					compiledRegex = null;
				}
			}

			if (compiledRegex == null)
			{
				/*
				 * No optimization: use super.newCall() as usual, which will call match() down
				 * below, which compiles the regex on the fly for each evaluation.
				 */
				return super.newCall(argExpressions, remainingArgTypes);
			}

			/*
			 * Else we could pre-compiled first arg as regex, so we can optimize: make a new
			 * FunctionCall that reuses the compiled regex (so remove first arg from argExpressions
			 * because it is already the compiledRegex)
			 */
			final List<Expression<?>> argExpressionsAfterRegex = argExpressions.subList(1, argExpressions.size());
			/*
			 * We still need to pass original argExpressions to FirstOrderFunctionCall because it
			 * checks all arguments datatypes and so on first
			 */
			return new FirstOrderFunctionCall<BooleanAttributeValue>(signature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanAttributeValue evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
				{
					final AV arg1;
					if (argExpressionsAfterRegex.isEmpty())
					{
						// no more arg in argExpressions, so next arg is in remainingArgs
						try
						{
							arg1 = paramClass1.cast(remainingArgs[0]);
						} catch (ClassCastException e)
						{
							throw new IndeterminateEvaluationException(indeterminateArg1TypeMessage, Status.STATUS_PROCESSING_ERROR, e);
						}
					} else
					{
						try
						{
							arg1 = Utils.evalSingle(argExpressionsAfterRegex.get(0), context, paramClass1);
						} catch (IndeterminateEvaluationException e)
						{
							throw new IndeterminateEvaluationException(getIndeterminateArgMessage(1), Status.STATUS_PROCESSING_ERROR, e);
						}
					}

					return BooleanAttributeValue.valueOf(eval(compiledRegex, arg1));

				}
			};
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

		private boolean eval(RegularExpression compiledRegex, AV arg1)
		{
			// from Matches#evaluateItem() / evalMatches():
			return compiledRegex.containsMatch(arg1.getUnderlyingValue());
		}

		@Override
		public final boolean match(StringAttributeValue regex, AV arg1) throws IllegalArgumentException
		{
			/*
			 * From Saxon xf:matches() implementation: Matches#evaluateItem() / evalMatches()
			 */
			final RegularExpression compiledRegex;
			try
			{
				compiledRegex = Configuration.getPlatform().compileRegularExpression(regex.getUnderlyingValue(), "", "XP20", null);
			} catch (XPathException e)
			{
				throw new PatternSyntaxException("Invalid regular expression arg", regex.getUnderlyingValue(), -1);
			}

			return eval(compiledRegex, arg1);
		}
	}

	protected static void main(String... args) throws XPathException
	{
		String input = "zzztesting";
		String regex = "^test.*";
		String flags = "";
		String xpathlang = "XP20";
		//
		RegularExpression compiledRegex = Configuration.getPlatform().compileRegularExpression(regex, flags, xpathlang, null);
		boolean isMatched = compiledRegex.containsMatch(input);
		System.out.println(isMatched);
	}

	/**
	 * *-starts-with function
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	private static class StartsWith<AV extends SimpleAttributeValue<String, AV>> extends NonEqualTypeMatchFunction<StringAttributeValue, AV>
	{
		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamType
		 *            second parameter type
		 */
		public StartsWith(String functionName, Datatype<AV> secondParamType)
		{
			super(functionName, DatatypeConstants.STRING.TYPE, secondParamType);
		}

		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public final boolean match(StringAttributeValue prefix, AV arg1)
		{
			return arg1.getUnderlyingValue().startsWith(prefix.getUnderlyingValue());
		}
	}

	/**
	 * *-ends-with function
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	private static class EndsWith<AV extends SimpleAttributeValue<String, AV>> extends NonEqualTypeMatchFunction<StringAttributeValue, AV>
	{
		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamType
		 *            second parameter type
		 */
		public EndsWith(String functionName, Datatype<AV> secondParamType)
		{
			super(functionName, DatatypeConstants.STRING.TYPE, secondParamType);
		}

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(StringAttributeValue suffix, AV arg1)
		{
			return arg1.getUnderlyingValue().endsWith(suffix.getUnderlyingValue());
		}
	}

	/**
	 * *-contains function
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	private static class Contains<AV extends SimpleAttributeValue<String, AV>> extends NonEqualTypeMatchFunction<StringAttributeValue, AV>
	{

		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamType
		 *            second parameter type
		 */
		public Contains(String functionName, Datatype<AV> secondParamType)
		{
			super(functionName, DatatypeConstants.STRING.TYPE, secondParamType);
		}

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the
		 * other
		 */
		@Override
		public final boolean match(StringAttributeValue contained, AV arg1)
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
			new RegexpMatch<>(NAME_STRING_REGEXP_MATCH, DatatypeConstants.STRING.TYPE),
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
			new StartsWith<>(NAME_STRING_STARTS_WITH, DatatypeConstants.STRING.TYPE),
			//
			new EndsWith<>(NAME_STRING_ENDS_WITH, DatatypeConstants.STRING.TYPE),
			//
			new Contains<>(NAME_STRING_CONTAINS, DatatypeConstants.STRING.TYPE),
			//
			new StartsWith<>(NAME_ANYURI_STARTS_WITH, DatatypeConstants.ANYURI.TYPE),
			//
			new EndsWith<>(NAME_ANYURI_ENDS_WITH, DatatypeConstants.ANYURI.TYPE),
			//
			new Contains<>(NAME_ANYURI_CONTAINS, DatatypeConstants.ANYURI.TYPE));

}
