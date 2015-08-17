/**
 * 
 */
package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Function call, usually resulting {@link Function#parseInputs(List)}. This is the recommended way
 * of calling any {@link Function}. This is quite similar to XACML Apply except it does not include
 * the Description field; and arguments are optimized specifically for each function by extending
 * this class accordinly, therefore they might be quite different from original input Expressions of
 * the Apply. In particular, if some expressions are actually static values (e.g. AttributeValue,
 * VariableReference to AttributeValue, function applied to static values...), these expressions
 * might be pre-compiled/pre-evaluated. For instance, a static regex parameter to regexp-match
 * function may be pre-compiled to a regex for re-use.
 * 
 * @param <T>
 *            call's return type (typically the same as the internal function's)
 * 
 */
public interface FunctionCall<T extends ExpressionResult<? extends AttributeValue>>
{

	/**
	 * Make the call in a given evaluation context
	 * 
	 * @param context
	 *            evaluation context
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if any evaluation error
	 */
	T evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Get the actual return type of this call (same as the internal function's return type), used
	 * as return type for XACML Apply in PDP.
	 * 
	 * @return return type
	 */
	DatatypeDef getReturnType();

}
