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

import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.JAXBBoundExpression;
import com.thalesgroup.authzforce.core.func.FunctionCall;

/**
 * Interface that all functions in the system must implement.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @param <T>
 *            return type of this function, i.e. single-valued V or bag of Vs
 */
public abstract class Function<T extends ExpressionResult<? extends AttributeValue>> extends FunctionType implements JAXBBoundExpression<FunctionType, T>, PdpExtension
{
	/**
	 * Returns the function ID (as PDP extension ID)
	 * 
	 * @see com.thalesgroup.authzforce.core.PdpExtension#getId()
	 */
	@Override
	public final String getId()
	{
		return this.functionId;
	}

	/**
	 * The standard namespace where all XACML 1.0 spec-defined functions live
	 */
	public static final String FUNCTION_NS_1 = "urn:oasis:names:tc:xacml:1.0:function:";

	/**
	 * The standard namespace where all XACML 2.0 spec-defined functions live
	 */
	public static final String FUNCTION_NS_2 = "urn:oasis:names:tc:xacml:2.0:function:";

	/**
	 * The standard namespace where all XACML 3.0 spec-defined functions live
	 */
	public static final String FUNCTION_NS_3 = "urn:oasis:names:tc:xacml:3.0:function:";

	private static final UnsupportedOperationException UNSUPPORTED_SET_FUNCTION_ID_OPERATION_EXCEPTION = new UnsupportedOperationException("Function.setFunctionId() not allowed");

	protected Function(String functionId)
	{
		this.functionId = functionId;
	}

	/**
	 * Parse/validate the function inputs.
	 * 
	 * @param inputExpressions
	 *            function arguments (expressions)
	 * 
	 * @return Function call handle for calling this function which such inputs (with possible
	 *         changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	public abstract FunctionCall<T> parseInputs(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> inputExpressions) throws IllegalArgumentException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
	 */
	@Override
	public final boolean isStatic()
	{
		// the function itself is static: constant identified by its ID
		return true;
	}

	@Override
	public final T evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// Expression#evaluate()
		/*
		 * The static function instance itself (as an expression, without any parameter) evaluates
		 * to nothing, it is just a function ID
		 */
		return null;
	}

	@Override
	public final String toString()
	{
		return this.getFunctionId();
	}

	@Override
	public final int hashCode()
	{
		return Objects.hash(this.functionId);
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		// if (!super.equals(obj))
		// return false;

		if (getClass() != obj.getClass())
			return false;
		final Function<?> other = (Function<?>) obj;
		if (this.functionId == null)
		{
			if (other.functionId != null)
				return false;
		} else if (!functionId.equals(other.functionId))
			return false;
		return true;
	}

	private final String indeterminateArgMessagePrefix = "Function " + functionId + ": Indeterminate arg #";

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType#setFunctionId(java.lang.String)
	 */
	@Override
	public final void setFunctionId(String value)
	{
		// disallow this method to avoid inconsistency with indeterminateArgMessagePrefix
		throw UNSUPPORTED_SET_FUNCTION_ID_OPERATION_EXCEPTION;
	}

	/**
	 * Get Indeterminate arg message
	 * 
	 * @param argIndex
	 *            function argument index (#x) that could not be determined
	 * @return "Indeterminate arg#x" exception
	 */
	public final String getIndeterminateArgMessage(int argIndex)
	{
		return indeterminateArgMessagePrefix + argIndex;
	}

	/**
	 * Get Indeterminate arg exception
	 * 
	 * @param argIndex
	 *            function argument index (#x) that could not be determined
	 * @return "Indeterminate arg#x" exception
	 */
	public final IndeterminateEvaluationException getIndeterminateArgException(int argIndex)
	{
		return new IndeterminateEvaluationException(getIndeterminateArgMessage(argIndex), Status.STATUS_PROCESSING_ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#createJAXBElement()
	 */
	@Override
	public JAXBElement<FunctionType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createFunction(this);
	}

}
