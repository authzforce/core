package com.thalesgroup.authzforce.core.func;

import java.lang.reflect.Array;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * 
 * map function
 * 
 * @param <SUB_RETURN_T>
 *            subfunction return type
 * 
 */
public class MapFunction<SUB_RETURN_T extends AttributeValue> extends HigherOrderBagFunction.OneBagOnlyFunction<BagResult<SUB_RETURN_T>, SUB_RETURN_T>
{

	/**
	 * RefPolicyFinderModuleFactory for map function
	 * 
	 */
	public static class Factory implements GenericHigherOrderFunctionFactory
	{

		@Override
		public final String getId()
		{
			return HigherOrderBagFunction.NAME_MAP;
		}

		@Override
		public final <SUB_RETURN_T extends AttributeValue> HigherOrderBagFunction<? extends ExpressionResult<? extends AttributeValue>, SUB_RETURN_T> getInstance(FirstOrderFunction<? extends ExpressionResult<? extends AttributeValue>> subFunction, Class<SUB_RETURN_T> subFunctionReturnType)
		{
			return new MapFunction<>(subFunction.getReturnType().datatypeURI(), subFunctionReturnType);
		}

	}

	private final Class<SUB_RETURN_T> datatypeClass;

	/**
	 * Creates Map function for specific sub-function's return type
	 * 
	 * @param subFunctionReturnTypeURI
	 *            sub-function return type URI (primitive)
	 * @param subFunctionReturnType
	 *            sub-function return type
	 */
	public MapFunction(String subFunctionReturnTypeURI, Class<SUB_RETURN_T> subFunctionReturnType)
	{
		super(NAME_MAP, new DatatypeDef(subFunctionReturnTypeURI, true), subFunctionReturnTypeURI);
		this.datatypeClass = subFunctionReturnType;
	}

	@Override
	protected final BagResult<SUB_RETURN_T> evaluate(FirstOrderFunction<SUB_RETURN_T>.FirstOrderFunctionCall subFuncCall, AttributeValue[] lastArgBag, int lastArgIndex, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final SUB_RETURN_T[] results = (SUB_RETURN_T[]) Array.newInstance(datatypeClass, lastArgBag.length);
		for (int valIndex = 0; valIndex < lastArgBag.length; valIndex++)
		{
			final SUB_RETURN_T subResult;
			try
			{
				subResult = subFuncCall.evaluate(context, lastArgBag[valIndex]);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this + ": Error calling sub-function (specified as first argument) with last arg=" + lastArgBag[valIndex], e.getStatusCode(), e);
			}

			if (subResult == null)
			{
				throw getIndeterminateArgException(lastArgIndex);
			}

			results[valIndex] = subResult;
		}

		return new BagResult<>(results, datatypeClass, subFuncReturnType);
	}

}
