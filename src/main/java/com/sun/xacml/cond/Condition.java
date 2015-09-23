/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.cond;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Represents the XACML ConditionType type. It contains exactly one child expression that is boolean
 * and returns a single value. This class was added in XACML 2.0
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class Condition extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition
{
	// the condition's evaluatable expression
	private final Expression<BooleanAttributeValue> evaluatableExpression;

	/**
	 * Logger used for all classes
	 */
	// private static final Logger LOGGER = LoggerFactory.getLogger(Condition.class);

	private static final UnsupportedOperationException UNSUPPORTED_SET_EXPRESSION_OPERATION = new UnsupportedOperationException("Condition.setExpression() not allowed");

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
	 * Constructs a <code>Condition</code> as used in XACML 2.0.
	 * 
	 * @param condition
	 *            Condition in JAXB model
	 * @param expFactory
	 *            expression factory
	 * @param policyDefaults
	 *            policy(set) default parameters, e.g. XPath version
	 * 
	 * @throws IllegalArgumentException
	 *             if the expression is not boolean or returns a bag
	 * @throws ParsingException
	 *             error parsing the expression in condition
	 */
	public Condition(oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition condition, DefaultsType policyDefaults, ExpressionFactory expFactory) throws IllegalArgumentException, ParsingException
	{
		final ExpressionType exprElt = condition.getExpression().getValue();
		final Expression<?> expr = expFactory.getInstance(exprElt, policyDefaults, null);

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
