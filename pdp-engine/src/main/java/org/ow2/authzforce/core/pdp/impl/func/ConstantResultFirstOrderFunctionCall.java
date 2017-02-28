/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * First-order function call with constant result
 *
 * @param <RETURN_T>
 *            return type
 */
public final class ConstantResultFirstOrderFunctionCall<RETURN_T extends Value> implements FirstOrderFunctionCall<RETURN_T>
{
	private final RETURN_T constant;
	private final Datatype<RETURN_T> constantDatatype;

	/**
	 * Constructor
	 * 
	 * @param constant
	 *            constant result
	 * @param constantDatatype
	 *            constant/return datatype
	 */
	public ConstantResultFirstOrderFunctionCall(final RETURN_T constant, final Datatype<RETURN_T> constantDatatype)
	{
		this.constant = constant;
		this.constantDatatype = constantDatatype;
	}

	@Override
	public RETURN_T evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		return constant;
	}

	@Override
	public Datatype<RETURN_T> getReturnType()
	{
		return constantDatatype;
	}

	@Override
	public RETURN_T evaluate(final EvaluationContext context, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
	{
		return constant;
	}

	@Override
	public RETURN_T evaluate(final EvaluationContext context, final boolean checkRemainingArgTypes, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
	{
		return constant;
	}
}