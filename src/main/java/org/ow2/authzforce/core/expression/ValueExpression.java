/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.expression;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.Value;

/**
 * 
 * Expression wrapper for constant values to be used as Expression, e.g. to be used as function argument. This is an alternative to {@link Value} extending
 * {@link Expression} directly, which would break the Acyclic Dependency principle since {@link Expression} already has a reference to {@link Value}.
 * 
 * @param <V>
 *            concrete value type
 *
 */
public abstract class ValueExpression<V extends Value> implements Expression<V>
{
	private static final IllegalArgumentException UNDEF_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined expression return type");
	private static final IllegalArgumentException UNDEF_VALUE_EXCEPTION = new IllegalArgumentException("Undefined value");
	private final Datatype<V> datatype;
	private final boolean isStatic;
	protected final V value;

	/**
	 * Creates instance of constant value expression
	 * 
	 * @param datatype
	 *            value datatype
	 * @param v
	 *            constant value
	 * @param isStatic
	 *            true iff the expression based on this value always evaluate to the same constant (not the case for xpathExpressions for instance)
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null || v == null}
	 * 
	 */
	protected ValueExpression(Datatype<V> datatype, V v, boolean isStatic) throws IllegalArgumentException
	{
		if (datatype == null)
		{
			throw UNDEF_DATATYPE_EXCEPTION;
		}

		if (v == null)
		{
			throw UNDEF_VALUE_EXCEPTION;
		}

		this.datatype = datatype;
		this.value = v;
		this.isStatic = isStatic;
	}

	@Override
	public final Datatype<V> getReturnType()
	{
		return this.datatype;
	}

	/**
	 * Returns the value itself
	 */
	@Override
	public final V evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this.value;
	}

	@Override
	public final boolean isStatic()
	{
		return isStatic;
	}

	/**
	 * Gets underlying value
	 * 
	 * @return value wrapped in this expression
	 */
	public V getValue()
	{
		return this.value;
	}

	@Override
	public String toString()
	{
		return value.toString();
	}

}
