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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Arrays;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Match;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.Function;
import org.ow2.authzforce.core.pdp.api.FunctionCall;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.impl.func.HigherOrderBagFunctionSet;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;

/**
 * XACML Match evaluator. This is the part of the Target that actually evaluates whether the specified attribute values in the Target match the corresponding attribute values in the request context.
 *
 * @version $Id: $
 */
public class MatchEvaluator
{

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
	 *             invalid <code>jaxbMatch</code>
	 */
	public MatchEvaluator(Match jaxbMatch, XPathCompiler xPathCompiler, ExpressionFactory expFactory) throws IllegalArgumentException
	{
		// get the matchFunction type, making sure that it's really a correct
		// Target matchFunction
		final String matchId = jaxbMatch.getMatchId();
		final Function<?> matchFunction = expFactory.getFunction(matchId);
		if (matchFunction == null)
		{
			throw new IllegalArgumentException("Unsupported function for MatchId: " + matchId);
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
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid <Match>'s <AttributeValue>", e);
		}

		// Match(matchFunction, attributeValue, bagExpression) = anyOf(matchFunction,
		// attributeValue, bagExpression)
		final Function<BooleanValue> anyOfFunc = (Function<BooleanValue>) expFactory.getFunction(HigherOrderBagFunctionSet.NAME_ANY_OF);
		if (anyOfFunc == null)
		{
			throw new IllegalArgumentException("Unsupported function '" + HigherOrderBagFunctionSet.NAME_ANY_OF + "' required for Match evaluation");
		}

		final List<Expression<?>> anyOfFuncInputs = Arrays.<Expression<?>> asList(matchFunction, attrValueExpr, bagExpression);
		try
		{
			this.anyOfFuncCall = anyOfFunc.newCall(anyOfFuncInputs);
		} catch (IllegalArgumentException e)
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
	public boolean match(EvaluationContext context) throws IndeterminateEvaluationException
	{
		final BooleanValue anyOfFuncCallResult;
		try
		{
			anyOfFuncCallResult = anyOfFuncCall.evaluate(context);
		} catch (IndeterminateEvaluationException e)
		{
			throw new IndeterminateEvaluationException("Error evaluating Match (with equivalent 'any-of' function)", e.getStatusCode(), e);
		}

		return anyOfFuncCallResult.getUnderlyingValue();
	}

}
