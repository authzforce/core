/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.Arrays;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Match;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.FunctionExpression;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionCall;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;

/**
 * XACML Match evaluator. This is the part of the Target that actually evaluates whether the specified attribute values in the Target match the corresponding attribute values in the request context.
 *
 * @version $Id: $
 */
public final class MatchEvaluator
{

	private static final IllegalArgumentException NULL_XACML_MATCH_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined input XACML Match element");
	private static final IllegalArgumentException NULL_XACML_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined input XACML Expression parser");

	/**
	 * Any-of function call equivalent to this Match:
	 * <p>
	 * Match(matchFunction, attributeValue, bagExpression) = anyOf(matchFunction, attributeValue, bagExpression)
	 */
	private final transient FunctionCall<BooleanValue> anyOfFuncCall;

	/**
	 * Instantiates Match evaluator from XACML-Schema-derived JAXB Match
	 *
	 * @param jaxbMatch
	 *            XACML-Schema-derived JAXB Match
	 * @param expFactory
	 *            bagExpression factory
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @throws java.lang.IllegalArgumentException
	 *             null {@code expFactory} or null/empty {@code jaxbMatch}
	 */
	public MatchEvaluator(final Match jaxbMatch, final XPathCompiler xPathCompiler, final ExpressionFactory expFactory) throws IllegalArgumentException
	{
		if (jaxbMatch == null)
		{
			throw NULL_XACML_MATCH_ARGUMENT_EXCEPTION;
		}

		if (expFactory == null)
		{
			throw NULL_XACML_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION;
		}

		// get the matchFunction type, making sure that it's really a correct
		// Target matchFunction
		final String matchId = jaxbMatch.getMatchId();
		final FunctionExpression matchFunction = expFactory.getFunction(matchId);
		if (matchFunction == null)
		{
			throw new IllegalArgumentException("Unsupported function for MatchId: '" + matchId + "'");
		}

		// next, get the designator or selector being used, and the attribute
		// value paired with it
		final AttributeDesignatorType attributeDesignator = jaxbMatch.getAttributeDesignator();
		final AttributeSelectorType attributeSelector = jaxbMatch.getAttributeSelector();
		final Expression<?> bagExpression = expFactory.getInstance(attributeDesignator == null ? attributeSelector : attributeDesignator, xPathCompiler, null);

		final AttributeValueType attributeValue = jaxbMatch.getAttributeValue();
		final Expression<? extends AttributeValue> attrValueExpr;
		try
		{
			attrValueExpr = expFactory.getInstance(attributeValue, xPathCompiler);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid <Match>'s <AttributeValue>", e);
		}

		/*
		 * Match(matchFunction, attributeValue, bagExpression) = anyOf(matchFunction, attributeValue, bagExpression)
		 */
		final FunctionExpression funcExp = expFactory.getFunction(StandardFunction.ANY_OF.getId());
		if (funcExp == null)
		{
			throw new IllegalArgumentException("Unsupported function '" + StandardFunction.ANY_OF.getId() + "' required for Match evaluation");
		}

		final Function<BooleanValue> anyOfFunc = funcExp.getValue().get();
		final List<Expression<?>> anyOfFuncInputs = Arrays.<Expression<?>> asList(matchFunction, attrValueExpr, bagExpression);
		try
		{
			this.anyOfFuncCall = anyOfFunc.newCall(anyOfFuncInputs);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid inputs (Expressions) to the Match (validated using the equivalent standard 'any-of' function definition): " + anyOfFuncInputs, e);
		}
	}

	/**
	 * Determines whether this <code>Match</code> matches the input request (whether it is applicable)
	 *
	 * @param context
	 *            the evaluation context
	 * @return true iff the context matches
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             error occurred evaluating the Match element in this evaluation {@code context}
	 */
	public boolean match(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		final BooleanValue anyOfFuncCallResult;
		try
		{
			anyOfFuncCallResult = anyOfFuncCall.evaluate(context);
		}
		catch (final IndeterminateEvaluationException e)
		{
			throw new IndeterminateEvaluationException("Error evaluating Match (with equivalent 'any-of' function)", e.getStatusCode(), e);
		}

		return anyOfFuncCallResult.getUnderlyingValue().booleanValue();
	}

}
