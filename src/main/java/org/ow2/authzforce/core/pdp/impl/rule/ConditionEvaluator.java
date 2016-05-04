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
package org.ow2.authzforce.core.pdp.impl.rule;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;

/**
 * Evaluates a XACML ConditionEvaluator. It contains exactly one child expression that is boolean and returns a single value.
 *
 * 
 * @version $Id: $
 */
public class ConditionEvaluator
{
	// the condition's evaluatable expression
	private transient final Expression<BooleanValue> evaluatableExpression;

	/**
	 * Constructs a Condition evaluator
	 *
	 * @param condition
	 *            Condition in JAXB model
	 * @param expFactory
	 *            expression factory
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @throws java.lang.IllegalArgumentException
	 *             if the expression is not a valid boolean expression
	 */
	public ConditionEvaluator(Condition condition, XPathCompiler xPathCompiler, ExpressionFactory expFactory) throws IllegalArgumentException
	{
		final ExpressionType exprElt = condition.getExpression().getValue();
		final Expression<?> expr = expFactory.getInstance(exprElt, xPathCompiler, null);

		// make sure it's a boolean expression...
		if (!(expr.getReturnType().equals(DatatypeConstants.BOOLEAN.TYPE)))
		{
			throw new IllegalArgumentException("Invalid return datatype (" + expr.getReturnType() + ") for Expression (" + expr.getClass().getSimpleName()
					+ ") in Condition. Expected: Boolean.");
		}

		this.evaluatableExpression = (Expression<BooleanValue>) expr;
	}

	/**
	 * Evaluates the <code>Condition</code> to boolean by evaluating its child boolean <code>Expression</code>.
	 *
	 * @param context
	 *            the representation of the request
	 * @return true if and only if condition is true, i.e. its expression evaluates to True
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if error evaluating the condition
	 */
	public boolean evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		final BooleanValue boolVal = evaluatableExpression.evaluate(context);
		return boolVal.getUnderlyingValue();
	}

}
