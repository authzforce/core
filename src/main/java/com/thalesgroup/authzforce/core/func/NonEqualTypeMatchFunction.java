package com.thalesgroup.authzforce.core.func;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.saxon.Configuration;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Implements generic match functions taking parameters of possibly different types.
 * 
 * @param <T>
 *            Type of the second parameter of this function. The first one being always a String.
 * 
 */
public abstract class NonEqualTypeMatchFunction<T extends PrimitiveAttributeValue<String>> extends FirstOrderFunction<BooleanAttributeValue>
{
	/**
	 * Standard TYPE_URI for the rfc822Name-match function (different from rfc822Name-regexp-match
	 * down below).
	 */
	public static final String NAME_RFC822NAME_MATCH = FUNCTION_NS_1 + "rfc822Name-match";

	/**
	 * Standard TYPE_URI for the string-regexp-match function.
	 */
	public static final String NAME_STRING_REGEXP_MATCH = FUNCTION_NS_1 + "string-regexp-match";

	/**
	 * Standard TYPE_URI for the anyURI-regexp-match function.
	 */
	public static final String NAME_ANYURI_REGEXP_MATCH = FUNCTION_NS_2 + "anyURI-regexp-match";

	/**
	 * Standard TYPE_URI for the ipAddress-regexp-match function.
	 */
	public static final String NAME_IPADDRESS_REGEXP_MATCH = FUNCTION_NS_2 + "ipAddress-regexp-match";

	/**
	 * Standard TYPE_URI for the dnsName-regexp-match function.
	 */
	public static final String NAME_DNSNAME_REGEXP_MATCH = FUNCTION_NS_2 + "dnsName-regexp-match";

	/**
	 * Standard TYPE_URI for the rfc822Name-regexp-match function.
	 */
	public static final String NAME_RFC822NAME_REGEXP_MATCH = FUNCTION_NS_2 + "rfc822Name-regexp-match";

	/**
	 * Standard TYPE_URI for the x500Name-regexp-match function.
	 */
	public static final String NAME_X500NAME_REGEXP_MATCH = FUNCTION_NS_2 + "x500Name-regexp-match";

	/**
	 * Standard TYPE_URI for the string-starts-with function.
	 */
	public static final String NAME_STRING_STARTS_WITH = FUNCTION_NS_3 + "string-starts-with";

	/**
	 * Standard TYPE_URI for the string-ends-with function.
	 */
	public static final String NAME_STRING_ENDS_WITH = FUNCTION_NS_3 + "string-ends-with";

	/**
	 * Standard TYPE_URI for the string-contains-with function.
	 */
	public static final String NAME_STRING_CONTAINS = FUNCTION_NS_3 + "string-contains";

	/**
	 * Standard TYPE_URI for the anyURI-starts-with function.
	 */
	public static final String NAME_ANYURI_STARTS_WITH = FUNCTION_NS_3 + "anyURI-starts-with";

	/**
	 * Standard TYPE_URI for the anyURI-ends-with function.
	 */
	public static final String NAME_ANYURI_ENDS_WITH = FUNCTION_NS_3 + "anyURI-ends-with";

	/**
	 * Standard TYPE_URI for the anyURI-contains-with function.
	 */
	public static final String NAME_ANYURI_CONTAINS = FUNCTION_NS_3 + "anyURI-contains";

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "non-equal-type-match",
	//
			new RFC822NameMatch(),
			//
			new RegexpMatch<>(NAME_STRING_REGEXP_MATCH, StringAttributeValue.TYPE_URI, StringAttributeValue.class),
			//
			new RegexpMatch<>(NAME_ANYURI_REGEXP_MATCH, AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue.class),
			//
			new RegexpMatch<>(NAME_IPADDRESS_REGEXP_MATCH, IPAddressAttributeValue.identifier, IPAddressAttributeValue.class),
			//
			new RegexpMatch<>(NAME_DNSNAME_REGEXP_MATCH, DNSNameAttributeValue.identifier, DNSNameAttributeValue.class),
			//
			new RegexpMatch<>(NAME_RFC822NAME_REGEXP_MATCH, RFC822NameAttributeValue.TYPE_URI, RFC822NameAttributeValue.class),
			//
			new RegexpMatch<>(NAME_X500NAME_REGEXP_MATCH, X500NameAttributeValue.TYPE_URI, X500NameAttributeValue.class),
			//
			new StartsWith<>(NAME_STRING_STARTS_WITH, StringAttributeValue.TYPE_URI, StringAttributeValue.class),
			//
			new EndsWith<>(NAME_STRING_ENDS_WITH, StringAttributeValue.TYPE_URI, StringAttributeValue.class),
			//
			new Contains<>(NAME_STRING_CONTAINS, StringAttributeValue.TYPE_URI, StringAttributeValue.class),
			//
			new StartsWith<>(NAME_ANYURI_STARTS_WITH, AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue.class),
			//
			new EndsWith<>(NAME_ANYURI_ENDS_WITH, AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue.class),
			//
			new Contains<>(NAME_ANYURI_CONTAINS, AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue.class));

	protected final Class<T> secondParamClass;

	private final String invalidArgTypesErrorMsg;
	private final String invalidRegexErrorMsg = "Function " + this.functionId + ": Invalid regular expression in arg#0";

	/**
	 * Creates a new <code>NonEqualTypeMatchFunction</code> based on the given name.
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param secondParamType
	 *            second parameter type URI
	 * @param secondParamClass
	 *            second parameter type
	 * 
	 */
	protected NonEqualTypeMatchFunction(String functionName, String secondParamTypeURI, Class<T> secondParamType)
	{
		super(functionName, BooleanAttributeValue.TYPE, false, BooleanAttributeValue.TYPE, new DatatypeDef(secondParamTypeURI));
		this.secondParamClass = secondParamType;
		invalidArgTypesErrorMsg = "Function " + this.functionId + ": Invalid arg types (expected: " + StringAttributeValue.class.getSimpleName() + "," + secondParamClass.getSimpleName() + "): ";
	}

	@Override
	protected FirstOrderFunctionCall getFunctionCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes) throws IllegalArgumentException
	{
		return new EagerPrimitiveEvalCall<AttributeValue>(AttributeValue[].class, checkedArgExpressions, checkedRemainingArgTypes)
		{

			@Override
			protected final BooleanAttributeValue evaluate(AttributeValue[] args) throws IndeterminateEvaluationException
			{
				final String arg0;
				final T arg1;
				try
				{
					arg0 = ((StringAttributeValue) args[0]).getValue();
					arg1 = secondParamClass.cast(args[1]);
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
	 * Evaluate function with second parameter as string
	 * 
	 * @param arg0
	 *            first function parameter
	 * @param arg1
	 *            second function parameter
	 * @return true if and only if both arguments match according to the function definition
	 * @throws PatternSyntaxException
	 *             if the function is a *-regexp-match function and <code>arg0</code> is not a valid
	 *             regular expression
	 */
	protected abstract boolean match(String arg0, T arg1) throws PatternSyntaxException;

	/**
	 * rfc822Name-match function
	 * 
	 */
	private static class RFC822NameMatch extends NonEqualTypeMatchFunction<RFC822NameAttributeValue>
	{

		/**
		 * Instantiates the function
		 */
		public RFC822NameMatch()
		{
			super(NAME_RFC822NAME_MATCH, RFC822NameAttributeValue.TYPE_URI, RFC822NameAttributeValue.class);
		}

		@Override
		public final boolean match(String arg0, RFC822NameAttributeValue arg1)
		{
			return arg1.match(arg0);
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
	 * @param <T>
	 *            second parameter type
	 */
	private static class RegexpMatch<T extends PrimitiveAttributeValue<String>> extends NonEqualTypeMatchFunction<T>
	{

		private final String indeterminateArg1TypeMessage = "Function " + functionId + ": Invalid type (expected = " + secondParamClass.getName() + ") of arg#1: ";

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.thalesgroup.authzforce.core.func.NonEqualTypeMatchFunction#getFunctionCall(java.util
		 * .List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
		 */
		@Override
		protected final FirstOrderFunctionCall getFunctionCall(final List<Expression<? extends ExpressionResult<? extends AttributeValue>>> checkedArgExpressions, DatatypeDef[] checkedRemainingArgTypes) throws IllegalArgumentException
		{
			// check if first arg = regex is constant value, in which case pre-compile the regex
			final RegularExpression compiledRegex;
			if (checkedArgExpressions.isEmpty())
			{
				compiledRegex = null;
			} else
			{
				final Expression<? extends ExpressionResult<? extends AttributeValue>> input0 = checkedArgExpressions.get(0);
				if (input0.isStatic())
				{
					final StringAttributeValue input0Val;
					try
					{
						input0Val = evalPrimitiveArg(input0, null, StringAttributeValue.class);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IllegalArgumentException("Function " + functionId + ": Error pre-evaluating static expression of arg #0 (in null context): " + input0, e);
					}
					final String regex = input0Val.getValue();
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
				// no optimization
				return super.getFunctionCall(checkedArgExpressions, checkedRemainingArgTypes);
			}

			// make a new FunctionCall that reuses the compiled regex
			return new FirstOrderFunctionCall(checkedRemainingArgTypes)
			{

				@Override
				protected BooleanAttributeValue evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
				{
					final T arg1;
					if (checkedArgExpressions.isEmpty())
					{
						try
						{
							arg1 = secondParamClass.cast(remainingArgs[0]);
						} catch (ClassCastException e)
						{
							throw new IndeterminateEvaluationException(indeterminateArg1TypeMessage, Status.STATUS_PROCESSING_ERROR, e);
						}
					} else
					{
						try
						{
							arg1 = evalPrimitiveArg(checkedArgExpressions.get(0), context, secondParamClass);
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
		 * @param secondParamTypeURI
		 *            second parameter's datatype URI
		 * @param secondParamType
		 *            second parameter class
		 */
		public RegexpMatch(String functionName, String secondParamTypeURI, Class<T> secondParamType)
		{
			super(functionName, secondParamTypeURI, secondParamType);
		}

		private boolean eval(RegularExpression compiledRegex, T arg1)
		{
			// from Matches#evaluateItem() / evalMatches():
			return compiledRegex.containsMatch(arg1.getValue());
		}

		@Override
		public final boolean match(String regex, T arg1) throws PatternSyntaxException
		{
			/*
			 * From Saxon xf:matches() implementation: Matches#evaluateItem() / evalMatches()
			 */
			final RegularExpression compiledRegex;
			try
			{
				compiledRegex = Configuration.getPlatform().compileRegularExpression(regex, "", "XP20", null);
			} catch (XPathException e)
			{
				throw new PatternSyntaxException("Invalid regular expression arg", regex, -1);
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
	 * @param <T>
	 *            second parameter type
	 */
	private static class StartsWith<T extends PrimitiveAttributeValue<String>> extends NonEqualTypeMatchFunction<T>
	{
		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamTypeURI
		 *            second parameter's datatype URI
		 * @param secondParamType
		 *            second parameter class
		 */
		public StartsWith(String functionName, String secondParamTypeURI, Class<T> secondParamType)
		{
			super(functionName, secondParamTypeURI, secondParamType);
		}

		/**
		 * WARNING: the XACML spec defines the first argument as the prefix
		 */
		@Override
		public final boolean match(String prefix, T arg1)
		{
			return arg1.getValue().startsWith(prefix);
		}
	}

	/**
	 * *-ends-with function
	 * 
	 * @param <T>
	 *            second parameter type
	 */
	private static class EndsWith<T extends PrimitiveAttributeValue<String>> extends NonEqualTypeMatchFunction<T>
	{
		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamTypeURI
		 *            second parameter's datatype URI
		 * @param secondParamType
		 *            second parameter class
		 */
		public EndsWith(String functionName, String secondParamTypeURI, Class<T> secondParamType)
		{
			super(functionName, secondParamTypeURI, secondParamType);
		}

		/**
		 * WARNING: the XACML spec defines the first argument as the suffix
		 */
		@Override
		public final boolean match(String suffix, T arg1)
		{
			return arg1.getValue().endsWith(suffix);
		}
	}

	/**
	 * *-contains function
	 * 
	 * @param <T>
	 *            second parameter type
	 */
	private static class Contains<T extends PrimitiveAttributeValue<String>> extends NonEqualTypeMatchFunction<T>
	{

		/**
		 * Instantiates the function
		 * 
		 * @param functionName
		 *            function ID
		 * @param secondParamTypeURI
		 *            second parameter's datatype URI
		 * @param secondParamType
		 *            second parameter class
		 */
		public Contains(String functionName, String secondParamTypeURI, Class<T> secondParamType)
		{
			super(functionName, secondParamTypeURI, secondParamType);
		}

		/**
		 * WARNING: the XACML spec defines the second argument as the string that must contain the
		 * other
		 */
		@Override
		public final boolean match(String contained, T arg1)
		{
			return arg1.getValue().contains(contained);
		}
	}

}
