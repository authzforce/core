/**
 *
 */
package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * A class implementing comparison of {@link Comparable} attribute values, i.e. imposing total
 * ordering of compared objects. In particular, this applies to all numeric types (integers,
 * doubles...) and string, but not to XML schema date/times that may have indeterminate relationship to each
 * other.
 * 
 * @param <T>
 *            function parameter type
 */
public class TotalComparisonFunction<T extends AttributeValue & Comparable<T>> extends BaseComparisonFunction<T>
{

	/**
	 * @see BaseComparisonFunction#BaseComparisonFunction(String, String, Class, PostCondition)
	 */
	public TotalComparisonFunction(String funcIdPrefixBeforeHyphen, String paramTypeURI, Class<T[]> paramArrayType, PostCondition condition)
	{
		super(funcIdPrefixBeforeHyphen, paramTypeURI, paramArrayType, condition);
	}

	@Override
	public int compare(T attrVal1, T attrVal2) throws IndeterminateEvaluationException
	{
		return attrVal1.compareTo(attrVal2);
	}

	/**
	 * All comparison functions requiring total order on compared objects
	 */
	private static final Set<Function<?>> INSTANCES = new HashSet<>();
	static
	{
		for (final PostCondition condition : PostCondition.values())
		{
			INSTANCES.addAll(Arrays
					.asList(
					//
					new TotalComparisonFunction<>(FUNCTION_NS_1 + "integer", IntegerAttributeValue.TYPE_URI, IntegerAttributeValue[].class,
							condition),
					//
							new TotalComparisonFunction<>(FUNCTION_NS_1 + "double", DoubleAttributeValue.TYPE_URI, DoubleAttributeValue[].class,
									condition),
							//
							new TotalComparisonFunction<>(FUNCTION_NS_1 + "string", StringAttributeValue.TYPE_URI, StringAttributeValue[].class,
									condition)));
		}
	}

	/**
	 * function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "total-comparison", INSTANCES);

}
