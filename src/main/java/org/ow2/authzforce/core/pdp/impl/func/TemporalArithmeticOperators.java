package org.ow2.authzforce.core.pdp.impl.func;

import org.ow2.authzforce.core.pdp.api.value.BaseTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DurationValue;
import org.ow2.authzforce.core.pdp.impl.func.TemporalArithmeticFunction.StaticOperation;

final class TemporalArithmeticOperators
{
	private TemporalArithmeticOperators()
	{
		// empty private constructor to prevent instantiation
	}

	static final class TimeAddDurationOperator<T extends BaseTimeValue<T>, D extends DurationValue<D>> implements StaticOperation<T, D>
	{

		@Override
		public T eval(final T time, final D duration)
		{
			return time.add(duration);

		}

	}

	static final class TimeSubtractDurationOperator<T extends BaseTimeValue<T>, D extends DurationValue<D>> implements StaticOperation<T, D>
	{

		@Override
		public T eval(final T time, final D duration)
		{
			return time.subtract(duration);
		}

	}

}
