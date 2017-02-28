/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
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
