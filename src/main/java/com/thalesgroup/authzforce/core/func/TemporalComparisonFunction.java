/**
 *
 */
package com.thalesgroup.authzforce.core.func;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.cond.Function;
import com.thalesgroup.authzforce.core.attr.BaseTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * A class implementing comparison of XML schema date/time attribute values, i.e. not imposing total
 * ordering of compared objects, as such date/times may have indeterminate relationship to each
 * other.
 * 
 * @param <T>
 *            function parameter type
 */
public class TemporalComparisonFunction<T extends BaseTimeAttributeValue<T>> extends BaseComparisonFunction<T>
{
	/**
	 * All time-related less/greater-than(-or-equal) functions
	 */
	private static final Set<Function<?>> INSTANCES = new HashSet<>();
	static
	{
		for (final PostCondition condition : PostCondition.values())
		{
			INSTANCES.addAll(Arrays.asList(new TemporalComparisonFunction<>(FUNCTION_NS_1 + "time", TimeAttributeValue.identifier,
					TimeAttributeValue[].class, condition), new TemporalComparisonFunction<>(FUNCTION_NS_1 + "date", DateAttributeValue.identifier,
					DateAttributeValue[].class, condition), new TemporalComparisonFunction<>(FUNCTION_NS_1 + "dateTime",
					DateTimeAttributeValue.identifier, DateTimeAttributeValue[].class, condition)));
		}
	}

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "temporal-comparison", INSTANCES);

	/**
	 * @see BaseComparisonFunction#BaseComparisonFunction(String, String, Class,
	 *      BaseComparisonFunction.PostCondition)
	 */
	public TemporalComparisonFunction(String funcIdPrefixBeforeHyphen, String paramType, Class<T[]> paramArrayType, PostCondition condition)
	{
		super(funcIdPrefixBeforeHyphen, paramType, paramArrayType, condition);
	}

	@Override
	public int compare(T attrVal1, T attrVal2) throws IndeterminateEvaluationException
	{
		return attrVal1.compare(attrVal2);
	}

}
