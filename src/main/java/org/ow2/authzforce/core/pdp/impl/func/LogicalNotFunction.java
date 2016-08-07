package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * Implements the XACML not function
 *
 * @version $Id: $
 */
public final class LogicalNotFunction extends SingleParameterTypedFirstOrderFunction<BooleanValue, BooleanValue>
{

	/**
	 * Standard identifier for the not function.
	 */
	public static final String NAME_NOT = XACML_NS_1_0 + "not";

	/**
	 * Singleton instance of "not" logical function
	 */
	public static final LogicalNotFunction INSTANCE = new LogicalNotFunction();

	private LogicalNotFunction()
	{
		super(NAME_NOT, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Collections.singletonList(StandardDatatypes.BOOLEAN_FACTORY.getDatatype()));
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval<BooleanValue, BooleanValue>(functionSignature, argExpressions, remainingArgTypes)
		{
			@Override
			protected BooleanValue evaluate(final Deque<BooleanValue> args) throws IndeterminateEvaluationException
			{
				return args.getFirst().not();
			}

		};
	}

}
