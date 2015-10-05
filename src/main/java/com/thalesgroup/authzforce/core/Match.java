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
package com.thalesgroup.authzforce.core;

import java.util.Arrays;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.ParsingException;
import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FunctionCall;
import com.thalesgroup.authzforce.core.func.HigherOrderBagFunction;

/**
 * XACML Match evaluator. This is the part of the Target that actually evaluates whether the
 * specified attribute values in the Target match the corresponding attribute values in the request
 * context.
 */
public class Match extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Match
{
	private static final UnsupportedOperationException UNSUPPORTED_SET_MATCH_ID_SELECTOR_OPERATION = new UnsupportedOperationException("Match/AttributeSelector read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION = new UnsupportedOperationException("Match/AttributeValue read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_ATTRIBUTE_DESIGNATOR_OPERATION = new UnsupportedOperationException("Match/AttributeDesignator read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_ATTRIBUTE_SELECTOR_OPERATION = new UnsupportedOperationException("Match/AttributeSelector read-only");

	/**
	 * Any-of function call equivalent to this Match:
	 * <p>
	 * Match(matchFunction, attributeValue, bagExpression) = anyOf(matchFunction, attributeValue,
	 * bagExpression)
	 */
	private final FunctionCall<BooleanAttributeValue> anyOfFuncCall;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Match#setAttributeValue(oasis.names.tc.xacml.
	 * _3_0.core.schema.wd_17.AttributeValueType)
	 */
	@Override
	public final void setAttributeValue(AttributeValueType value)
	{
		/*
		 * Disable this method to avoid inconsistency with anyOfFuncCall which is derived from this
		 * JAXB field
		 */
		throw UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Match#setAttributeSelector(oasis.names.tc.xacml
	 * ._3_0.core.schema.wd_17.AttributeSelectorType)
	 */
	@Override
	public final void setAttributeSelector(AttributeSelectorType value)
	{
		/*
		 * Disable this method to avoid inconsistency with anyOfFuncCall which is derived from this
		 * JAXB field
		 */
		throw UNSUPPORTED_SET_ATTRIBUTE_SELECTOR_OPERATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Match#setAttributeDesignator(oasis.names.tc.xacml
	 * ._3_0.core.schema.wd_17.AttributeDesignatorType)
	 */
	@Override
	public final void setAttributeDesignator(AttributeDesignatorType value)
	{
		/*
		 * Disable this method to avoid inconsistency with anyOfFuncCall which is derived from this
		 * JAXB field
		 */
		throw UNSUPPORTED_SET_ATTRIBUTE_DESIGNATOR_OPERATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.Match#setMatchId(java.lang.String)
	 */
	@Override
	public final void setMatchId(String value)
	{
		/*
		 * Disable this method to avoid inconsistency with anyOfFuncCall which is derived from this
		 * JAXB field
		 */
		throw UNSUPPORTED_SET_MATCH_ID_SELECTOR_OPERATION;
	}

	/**
	 * Instantiates Match evaluator from XACML-Schema-derived JAXB Match
	 * 
	 * @param jaxbMatch
	 *            XACML-Schema-derived JAXB Match
	 * @param expFactory
	 *            bagExpression factory
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @throws ParsingException
	 *             error parsing <code>jaxbMatch</code>
	 */
	public Match(oasis.names.tc.xacml._3_0.core.schema.wd_17.Match jaxbMatch, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws ParsingException
	{
		// get the matchFunction type, making sure that it's really a correct
		// Target matchFunction
		this.matchId = jaxbMatch.getMatchId();
		final Function<?> matchFunction = expFactory.getFunction(matchId);
		if (matchFunction == null)
		{
			throw new ParsingException("Unsupported function for MatchId: " + matchId);
		}

		// next, get the designator or selector being used, and the attribute
		// value paired with it
		this.attributeDesignator = jaxbMatch.getAttributeDesignator();
		this.attributeSelector = jaxbMatch.getAttributeSelector();
		final Expression<?> bagExpression = expFactory.getInstance(attributeDesignator == null ? attributeSelector : attributeDesignator, xPathCompiler, null);

		this.attributeValue = jaxbMatch.getAttributeValue();
		final AttributeValue<?> attrValueExpr;
		try
		{
			attrValueExpr = expFactory.createAttributeValue(attributeValue, xPathCompiler);
		} catch (ParsingException e)
		{
			throw new ParsingException("Error parsing <Match>'s <AttributeValue>", e);
		}

		// Match(matchFunction, attributeValue, bagExpression) = anyOf(matchFunction,
		// attributeValue, bagExpression)
		final Function<BooleanAttributeValue> anyOfFunc = (Function<BooleanAttributeValue>) expFactory.getFunction(HigherOrderBagFunction.NAME_ANY_OF);
		if (anyOfFunc == null)
		{
			throw new ParsingException("Unsupported function '" + HigherOrderBagFunction.NAME_ANY_OF + "' required for Match evaluation");
		}

		final List<Expression<?>> anyOfFuncInputs = Arrays.<Expression<?>> asList(matchFunction, attrValueExpr, bagExpression);
		try
		{
			anyOfFuncCall = anyOfFunc.newCall(anyOfFuncInputs);
		} catch (IllegalArgumentException e)
		{
			throw new ParsingException("Invalid inputs (Expressions) to the Match (validated using the equivalent standard 'any-of' function definition): " + anyOfFuncInputs, e);
		}
	}

	/**
	 * Determines whether this <code>Match</code> matches the input request (whether it is
	 * applicable)
	 * 
	 * @param context
	 *            the evaluation context
	 * 
	 * @return true iff the context matches
	 * @throws IndeterminateEvaluationException
	 */
	public boolean match(EvaluationContext context) throws IndeterminateEvaluationException
	{
		final BooleanAttributeValue anyOfFuncCallResult;
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
