package com.thalesgroup.authzforce.core.func;

import com.thalesgroup.authzforce.core.PdpExtension;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;

/**
 * Interface for generic higher-order function factories, e.g. {@link MapFunction}. A generic
 * function is a function class with a type parameter that is the sub-function's return type, e.g.
 * {@link MapFunction.Factory}, therefore the function is instantiated for a specific
 * sub-function's return type; the sub-function can be any first-order function (FirstOrderFunction).
 * 
 */
public interface GenericHigherOrderFunctionFactory extends PdpExtension
{
	/**
	 * Returns instance of the Higher-order function
	 * 
	 * @param subFunction
	 *            sub-function
	 * @param subFunctionReturnType sub-function's return type
	 * @return higher-order function instance
	 */
	public <SUB_RETURN_T extends AttributeValue> HigherOrderBagFunction<? extends ExpressionResult<? extends AttributeValue>, SUB_RETURN_T> getInstance(
			FirstOrderFunction<? extends ExpressionResult<? extends AttributeValue>> subFunction, Class<SUB_RETURN_T> subFunctionReturnType);
}
