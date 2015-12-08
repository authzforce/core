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
package org.ow2.authzforce.core.func;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.saxon.Configuration;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.expression.Expressions;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.BooleanValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeConstants;
import org.ow2.authzforce.core.value.SimpleValue;
import org.ow2.authzforce.core.value.StringValue;

/**
 * *-regexp-match function helper
 * <p>
 * WARNING: the regular expression syntax required by XACML refers to the <code>xf:matches</code> function from [XF] (see the XACML core spec for this
 * reference). This function and associated syntax differ from {@link Pattern} (Java 7) in several ways. Therefore, we cannot use {@link Pattern} directly. Find
 * examples of differences below:
 * <ul>
 * <li>{@link Pattern} matches the entire string against the pattern always, whereas <code>xf:matches</code> considers the string to match the pattern if any
 * substring matches the pattern.</li>
 * <li><code>xf:matches</code> regular expression syntax is based on XML schema which defines character class substraction using '-' character, whereas
 * {@link Pattern} does not support this syntax but <code>&&[^</code> instead.</li>
 * <li>
 * Category escape: can be done in XML SCHEMA with: <code>[\P{X}]</code>. {@link Pattern} only supports this form: <code>[^\p{X}]</code>.</li>
 * <li>
 * Character classes: XML schema define categories <code>\c</code> and <code>\C</code>. {@link Pattern} does not support them.</li>
 * </ul>
 * EXAMPLE: this regex from XML schema spec uses character class substraction. It is valid for <code>xf:matches</code> but does not compile with {@link Pattern}:
 * 
 * <pre>
 * [\i-[:]][\c-[:]]*
 * </pre>
 * 
 */
final class RegexpMatchFunctionHelper
{
	private static final class CompiledRegexMatchFunctionCall extends FirstOrderFunctionCall<BooleanValue>
	{
		private final RegularExpression compiledRegex;
		private final List<Expression<?>> argExpressionsAfterRegex;
		private final Datatype<? extends SimpleValue<String>> matchedValType;
		private final Class<? extends SimpleValue<String>> matchedValClass;
		private final String invalidRemainingArg1TypeMsg;
		private final String funcId;

		private CompiledRegexMatchFunctionCall(FunctionSignature<BooleanValue> functionSig, List<Expression<?>> argExpressions,
				Datatype<?>[] remainingArgTypes, RegularExpression compiledRegex, Datatype<? extends SimpleValue<String>> matchedValueType,
				String invalidRemainingArg1TypeMsg) throws IllegalArgumentException
		{
			super(functionSig, argExpressions, remainingArgTypes);
			this.funcId = functionSig.getName();
			this.compiledRegex = compiledRegex;
			/*
			 * We can remove the first arg from argExpressions since it is already the compiledRegex.
			 */
			this.argExpressionsAfterRegex = argExpressions.subList(1, argExpressions.size());
			this.matchedValType = matchedValueType;
			this.matchedValClass = matchedValueType.getValueClass();
			this.invalidRemainingArg1TypeMsg = invalidRemainingArg1TypeMsg;
		}

		@Override
		protected BooleanValue evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			final SimpleValue<String> arg1;
			if (argExpressionsAfterRegex.isEmpty())
			{
				// no more arg in argExpressions, so next arg is in remainingArgs
				try
				{
					arg1 = matchedValClass.cast(remainingArgs[0]);
				} catch (ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidRemainingArg1TypeMsg, StatusHelper.STATUS_PROCESSING_ERROR, e);
				}
			} else
			{
				try
				{
					arg1 = Expressions.eval(argExpressionsAfterRegex.get(0), context, matchedValType);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException("Function " + this.funcId + ": Indeterminate arg #1", StatusHelper.STATUS_PROCESSING_ERROR, e);
				}
			}

			return BooleanValue.valueOf(compiledRegex.containsMatch(arg1.getUnderlyingValue()));

		}
	}

	private final String indeterminateArg1TypeMessage;
	private final FunctionSignature<BooleanValue> funcSig;
	private final Datatype<? extends SimpleValue<String>> matchedValueType;
	private final String indeterminateArg0StaticPreEvalMsg;
	private final String invalidRegexMsg;

	RegexpMatchFunctionHelper(FunctionSignature<BooleanValue> matchFunctionSignature, Datatype<? extends SimpleValue<String>> matchedDatatype)
	{
		this.funcSig = matchFunctionSignature;
		this.matchedValueType = matchedDatatype;
		this.indeterminateArg1TypeMessage = "Function " + funcSig.getName() + ": Invalid type (expected = " + matchedDatatype + ") of arg #1: ";
		this.indeterminateArg0StaticPreEvalMsg = "Function " + funcSig.getName() + ": Error pre-evaluating static expression of arg #0 (in null context): ";
		this.invalidRegexMsg = "Function " + funcSig.getName() + ": Invalid regular expression in arg #0 (evaluated as static expression): '";
	}

	/**
	 * Creates regex-match function call using pre-compiled regex
	 * 
	 * @param argExpressions
	 * @param remainingArgTypes
	 * @return function call using compiled regex from first argument if constant value; or null if first argument is not constant
	 */
	FirstOrderFunctionCall<BooleanValue> getCompiledRegexMatchCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
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
				final StringValue input0Val;
				try
				{
					input0Val = Expressions.eval(input0, null, DatatypeConstants.STRING.TYPE);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IllegalArgumentException(indeterminateArg0StaticPreEvalMsg + input0, e);
				}
				final String regex = input0Val.getUnderlyingValue();
				try
				{
					/*
					 * From Saxon xf:matches() implementation: Matches#evaluateItem() / evalMatches()
					 */
					compiledRegex = Configuration.getPlatform().compileRegularExpression(regex, "", "XP20", null);
				} catch (XPathException e)
				{
					throw new IllegalArgumentException(invalidRegexMsg + regex + "'", e);
				}
			} else
			{
				compiledRegex = null;
			}
		}

		if (compiledRegex == null)
		{
			return null;
		}

		/*
		 * Else compiledRegex != null, so we can optimize: make a new FunctionCall that reuses the compiled regex Although we could remove the first arg from
		 * argExpressions since it is already the compiledRegex, we still need to pass original argExpressions to any subclass of FirstOrderFunctionCall (like
		 * below) because it checks all arguments datatypes and so on first.
		 */
		return new CompiledRegexMatchFunctionCall(funcSig, argExpressions, remainingArgTypes, compiledRegex, matchedValueType, indeterminateArg1TypeMessage);
	}

	static boolean match(StringValue regex, SimpleValue<String> arg1) throws IllegalArgumentException
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

		return compiledRegex.containsMatch(arg1.getUnderlyingValue());
	}
}