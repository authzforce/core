/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml;

import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.expression.Expression;
import org.ow2.authzforce.core.func.FirstOrderFunction;
import org.ow2.authzforce.core.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.value.BooleanValue;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeConstants;

/**
 * A class that implements the not function. This function takes one boolean argument and returns the logical negation of that value. If the argument evaluates
 * to indeterminate, an indeterminate result is returned.
 * 
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public final class NotFunction extends FirstOrderFunction.SingleParameterTyped<BooleanValue, BooleanValue>
{

	/**
	 * Standard identifier for the not function.
	 */
	public static final String NAME_NOT = XACML_NS_1_0 + "not";

	/**
	 * Creates a new <code>NotFunction</code> object.
	 */
	public NotFunction()
	{
		super(NAME_NOT, DatatypeConstants.BOOLEAN.TYPE, false, Collections.singletonList(DatatypeConstants.BOOLEAN.TYPE));
	}

	@Override
	protected FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
			throws IllegalArgumentException
	{
		return new FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval<BooleanValue, BooleanValue>(functionSignature, argExpressions, remainingArgTypes)
		{
			@Override
			protected BooleanValue evaluate(Deque<BooleanValue> args) throws IndeterminateEvaluationException
			{
				return args.getFirst().not();
			}

		};
	}

}
