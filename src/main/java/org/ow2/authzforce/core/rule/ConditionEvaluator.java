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
package com.thalesgroup.authzforce.core.rule;

import javax.xml.bind.JAXBElement;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;

/**
 * Evaluates a XACML ConditionEvaluator. It contains exactly one child expression that is boolean
 * and returns a single value.
 * 
 */
public class ConditionEvaluator extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition
{
	// the condition's evaluatable expression
	private transient final Expression<BooleanAttributeValue> evaluatableExpression;

	/**
	 * Logger used for all classes
	 */
	// private static final Logger LOGGER = LoggerFactory.getLogger(ConditionEvaluator.class);

	private static final UnsupportedOperationException UNSUPPORTED_SET_EXPRESSION_OPERATION = new UnsupportedOperationException("ConditionEvaluator.setExpression() not allowed");

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition#getExpression()
	 */
	@Override
	public final JAXBElement<? extends ExpressionType> getExpression()
	{
		return evaluatableExpression.getJAXBElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition#setExpression(javax.xml.bind.JAXBElement
	 * )
	 */
	@Override
	public final void setExpression(JAXBElement<? extends ExpressionType> value)
	{
		throw UNSUPPORTED_SET_EXPRESSION_OPERATION;
	}

	/**
	 * Constructs a Condition evaluator
	 * 
	 * @param condition
	 *            Condition in JAXB model
	 * @param expFactory
	 *            expression factory
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * 
	 * @throws IllegalArgumentException
	 *             if the expression is not boolean or returns a bag
	 * @throws ParsingException
	 *             error parsing the expression in condition
	 */
	public ConditionEvaluator(oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition condition, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws IllegalArgumentException, ParsingException
	{
		final ExpressionType exprElt = condition.getExpression().getValue();
		final Expression<?> expr = expFactory.getInstance(exprElt, xPathCompiler, null);

		// make sure it's a boolean expression...
		if (!(expr.getReturnType().equals(DatatypeConstants.BOOLEAN.TYPE)))
		{
			throw new IllegalArgumentException("Invalid return datatype (" + expr.getReturnType() + ") for Expression (" + expr.getClass().getSimpleName() + ") in Condition. Expected: Boolean.");
		}

		this.evaluatableExpression = (Expression<BooleanAttributeValue>) expr;

		/*
		 * Set JAXB expression field to null, getExpression() overridden instead to make sure
		 * evaluatableExpression is always consistent/synchronized with condition.getExpression()
		 */
		this.expression = null;
	}

	/**
	 * Evaluates the <code>Condition</code> to boolean by evaluating its child boolean
	 * <code>Expression</code>.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return true if and only if condition is true, i.e. its expression evaluates to True
	 * @throws IndeterminateEvaluationException
	 *             if error evaluating the condition
	 */
	public boolean evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		final BooleanAttributeValue boolVal = evaluatableExpression.evaluate(context);
		return boolVal.getUnderlyingValue();
	}

}
