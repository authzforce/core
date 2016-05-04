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
package org.ow2.authzforce.core.pdp.impl.expression;

import java.util.Deque;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.api.VariableReference;

/**
 * This class defines a VariableReference built from VariableReference after the referenced VariableDefinition has been resolved and therefore its expression.
 * As a result, Variables are simply Expressions identified by an ID (VariableId) and replace original XACML VariableReferences for actual evaluation.
 *
 * @param <V>
 *            evaluation's return type
 * 
 * @version $Id: $
 */
public class BaseVariableReference<V extends Value> implements VariableReference<V>
{
	private final transient Expression<V> expression;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isStatic()
	{
		return expression.isStatic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.impl.expression.VariableReference#getReferencedExpression()
	 */
	/** {@inheritDoc} */
	@Override
	public Expression<?> getReferencedExpression()
	{
		return expression;
	}

	private final String variableId;

	private final transient Deque<String> longestVariableReferenceChain;

	/**
	 * Constructor that takes a variable identifier
	 *
	 * @param varId
	 *            input VariableReference from XACML model
	 * @param varExpr
	 *            Expression of referenced VariableDefinition
	 * @param longestVarRefChain
	 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's
	 *            expression contains one or more VariableReferences to V2)
	 */
	public BaseVariableReference(String varId, Expression<V> varExpr, Deque<String> longestVarRefChain)
	{
		this.variableId = varId;
		this.expression = varExpr;
		this.longestVariableReferenceChain = longestVarRefChain;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the referenced expression using the given context, and either returns an error or a resulting value. If this doesn't reference an evaluatable
	 * expression (eg, a single Function) then this will throw an exception.
	 */
	@Override
	public V evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		/*
		 * Even if context == null, evaluation may work because expression may be static/constant (e.g. AttributeValue or Apply on AttributeValues). This is
		 * called static evaluation and is used for pre-evaluating/optimizing certain function calls.
		 */
		if (context == null)
		{
			return expression.evaluate(null);
		}

		final V ctxVal = context.getVariableValue(this.variableId, expression.getReturnType());
		if (ctxVal != null)
		{
			return ctxVal;
		}

		// ctxVal == null: not evaluated yet in this context -> evaluate now
		final V result = expression.evaluate(context);
		context.putVariableIfAbsent(this.variableId, result);
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns the type of the referenced expression.
	 */
	@Override
	public Datatype<V> getReturnType()
	{
		return expression.getReturnType();
	}

	/** {@inheritDoc} */
	@Override
	public String getVariableId()
	{
		return this.variableId;
	}

	/**
	 * <p>Getter for the field <code>longestVariableReferenceChain</code>.</p>
	 *
	 * @return the longestVariableReferenceChain
	 */
	public Deque<String> getLongestVariableReferenceChain()
	{
		return longestVariableReferenceChain;
	}

	/** {@inheritDoc} */
	@Override
	public JAXBElement<VariableReferenceType> getJAXBElement()
	{
		return JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createVariableReference(new VariableReferenceType(variableId));
	}

}
