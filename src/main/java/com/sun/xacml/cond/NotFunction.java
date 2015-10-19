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

import java.util.Deque;
import java.util.List;

import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;
import com.thalesgroup.authzforce.core.func.FirstOrderFunction;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;

/**
 * A class that implements the not function. This function takes one boolean argument and returns
 * the logical negation of that value. If the argument evaluates to indeterminate, an indeterminate
 * result is returned.
 * 
 * @since 1.0
 * @author Steve Hanna
 * @author Seth Proctor
 */
public class NotFunction extends FirstOrderFunction<BooleanAttributeValue>
{

	/**
	 * Standard identifier for the not function.
	 */
	public static final String NAME_NOT = FUNCTION_NS_1 + "not";

	/**
	 * Creates a new <code>NotFunction</code> object.
	 */
	public NotFunction()
	{
		super(NAME_NOT, DatatypeConstants.BOOLEAN.TYPE, false, DatatypeConstants.BOOLEAN.TYPE);
	}

	@Override
	protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new EagerSinglePrimitiveTypeEval<BooleanAttributeValue, BooleanAttributeValue>(signature, DatatypeConstants.BOOLEAN.TYPE, argExpressions, remainingArgTypes)
		{
			@Override
			protected BooleanAttributeValue evaluate(Deque<BooleanAttributeValue> args) throws IndeterminateEvaluationException
			{
				return eval(args.getFirst());
			}

		};
	}

	/**
	 * not(arg)
	 * 
	 * @param arg
	 *            boolean
	 * @return not(<code>arg</code>)
	 */
	public static BooleanAttributeValue eval(BooleanAttributeValue arg)
	{
		return arg.not();
	}

}
