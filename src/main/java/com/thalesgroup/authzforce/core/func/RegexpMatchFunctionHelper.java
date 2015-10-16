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
import java.util.regex.PatternSyntaxException;

import net.sf.saxon.Configuration;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.Expression.Utils;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * *-regexp-match function helper
 * <p>
 * WARNING: the regular expression syntax required by XACML refers to the <code>xf:matches</code>
 * function from [XF] (see the XACML core spec for this reference). This function and associated
 * syntax differ from {@link Pattern} (Java 7) in several ways. Therefore, we cannot use
 * {@link Pattern} directly. Find examples of differences below:
 * <ul>
 * <li>{@link Pattern} matches the entire string against the pattern always, whereas
 * <code>xf:matches</code> considers the string to match the pattern if any substring matches the
 * pattern.</li>
 * <li><code>xf:matches</code> regular expression syntax is based on XML schema which defines
 * character class substraction using '-' character, whereas {@link Pattern} does not support this
 * syntax but <code>&&[^</code> instead.</li>
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
 */
final class RegexpMatchFunctionHelper
{
	private final String indeterminateArg1TypeMessage;
	private final FunctionSignature<BooleanAttributeValue> funcSig;
	private final Datatype<? extends SimpleAttributeValue<String, ?>> matchedValueType;
	private final Class<? extends SimpleAttributeValue<String, ?>> matchedValueClass;

	RegexpMatchFunctionHelper(FunctionSignature<BooleanAttributeValue> matchFunctionSignature, Datatype<? extends SimpleAttributeValue<String, ?>> matchedDatatype)
	{
		this.funcSig = matchFunctionSignature;
		this.matchedValueType = matchedDatatype;
		this.matchedValueClass = matchedDatatype.getValueClass();
		indeterminateArg1TypeMessage = "Function " + funcSig.getName() + ": Invalid type (expected = " + matchedDatatype + ") of arg#1: ";
	}

	private static boolean eval(RegularExpression compiledRegex, SimpleAttributeValue<String, ?> matchedValue)
	{
		return compiledRegex.containsMatch(matchedValue.getUnderlyingValue());
	}

	/**
	 * 
	 * @param argExpressions
	 * @param remainingArgTypes
	 * @return function call using compiled regex from first argument if constant value; or null if
	 *         first argument is not constant
	 */
	FirstOrderFunctionCall<BooleanAttributeValue> getCompiledRegexMatchCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
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
					input0Val = Utils.evalSingle(input0, null, DatatypeConstants.STRING.TYPE);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IllegalArgumentException("Function " + funcSig.getName() + ": Error pre-evaluating static expression of arg #0 (in null context): " + input0, e);
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
					throw new IllegalArgumentException("Function " + funcSig.getName() + ": Invalid regular expression in arg #0 (evaluated as static expression): '" + regex + "'", e);
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
		 * Else compiledRegex != null, so we can optimize: make a new FunctionCall that reuses the
		 * compiled regex (so remove first arg from argExpressions because it is already the
		 * compiledRegex)
		 */
		final List<Expression<?>> argExpressionsAfterRegex = argExpressions.subList(1, argExpressions.size());
		/*
		 * We still need to pass original argExpressions to FirstOrderFunctionCall because it checks
		 * all arguments datatypes and so on first
		 */
		return new FirstOrderFunctionCall<BooleanAttributeValue>(funcSig, argExpressions, remainingArgTypes)
		{

			@Override
			protected BooleanAttributeValue evaluate(EvaluationContext context, AttributeValue<?>... remainingArgs) throws IndeterminateEvaluationException
			{
				final SimpleAttributeValue<String, ?> arg1;
				if (argExpressionsAfterRegex.isEmpty())
				{
					// no more arg in argExpressions, so next arg is in remainingArgs
					try
					{
						arg1 = matchedValueClass.cast(remainingArgs[0]);
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException(indeterminateArg1TypeMessage, Status.STATUS_PROCESSING_ERROR, e);
					}
				} else
				{
					try
					{
						arg1 = Utils.evalSingle(argExpressionsAfterRegex.get(0), context, matchedValueType);
					} catch (IndeterminateEvaluationException e)
					{
						throw new IndeterminateEvaluationException("Function " + funcSig.getName() + ": Indeterminate arg #1", Status.STATUS_PROCESSING_ERROR, e);
					}
				}

				return BooleanAttributeValue.valueOf(eval(compiledRegex, arg1));

			}
		};
	}

	static boolean match(StringAttributeValue regex, SimpleAttributeValue<String, ?> arg1) throws IllegalArgumentException
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