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
/**
 * 
 */
package com.thalesgroup.authzforce.core.eval;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeValue;

/**
 * Super interface of the internal model's expression that are "Evaluatable" by the PDP evaluation
 * engine, i.e. evaluation depends on the Evaluation Context:
 * <ul>
 * <li>AttributeValue</li>
 * <li>Apply</li>
 * <li>AttributeSelector</li>
 * <li>VariableReference</li>
 * <li>AttributeDesignator</li>
 * <li>Function</li>
 * </ul>
 * 
 * @param <T>
 *            type of result, i.e single-value or bag of values
 */

public interface Expression<T extends ExpressionResult<? extends AttributeValue, T>>
{
	/**
	 * Gets the expected return type of the expression if evaluated.
	 * 
	 * @return expression evaluation's return type
	 */
	DatatypeDef getReturnType();

	/**
	 * Evaluates the expression using the given context.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation that may be a single value T (e.g. function result,
	 *         AttributeValue, Condition, Match...) or bag of values (e.g. AttributeDesignator,
	 *         AttributeSelector)
	 * @throws IndeterminateEvaluationException
	 *             if evaluation "Indeterminate" (see XACML core specification)
	 */
	T evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Tells whether this expression is actually a static value, i.e. independent from the
	 * evaluation context (e.g. AttributeValue, VariableReference to AttributeValue...). This
	 * enables expression consumers to do optimizations, e.g. functions may pre-compile/pre-evaluate
	 * parts of their inputs knowing some are constant values.
	 * 
	 * @return true iff a static/fixed/constant value
	 */
	boolean isStatic();

	/**
	 * Gets the instance of the Java representation of the XACML-schema-defined Expression
	 * bound/equivalent to this expression
	 * 
	 * @return JAXB element equivalent
	 */
	JAXBElement<? extends ExpressionType> getJAXBElement();

	/**
	 * Utility class that provide functions to help evaluate Expressions
	 * 
	 */
	public static class Utils
	{
		private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException("No value returned by arg evaluation in the current context", Status.STATUS_PROCESSING_ERROR);

		/**
		 * Evaluate single-valued (primitive) argument expression
		 * 
		 * @param arg
		 *            argument expression
		 * @param context
		 *            context in which argument expression is evaluated
		 * @param returnType
		 *            type of returned attribute value
		 * @return result of evaluation
		 * @throws IndeterminateEvaluationException
		 *             if no value returned from evaluation, or <code>returnType</code> is not a
		 *             supertype of the result value datatype
		 */
		public static <T extends AttributeValue> T evalPrimitiveArg(Expression<?> arg, EvaluationContext context, Class<T> returnType) throws IndeterminateEvaluationException
		{
			final AttributeValue attrVal = arg.evaluate(context).value();
			if (attrVal == null)
			{
				throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
			}

			try
			{
				return returnType.cast(attrVal);
			} catch (ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Invalid arg evaluation result datatype: " + attrVal.getClass().getName() + ". Expected: " + returnType.getName(), Status.STATUS_PROCESSING_ERROR, e);
			}
		}
	}

}
