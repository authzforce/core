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