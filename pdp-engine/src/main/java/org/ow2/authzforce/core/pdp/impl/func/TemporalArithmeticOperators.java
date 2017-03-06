/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
