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

import java.util.Deque;

import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.NumericValue;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticFunction.MultaryOperation;
import org.ow2.authzforce.core.pdp.impl.func.NumericArithmeticFunction.StaticOperation;

final class NumericArithmeticOperators
{
	private NumericArithmeticOperators()
	{
		// empty private constructor to prevent instantiation
	}

	static final class AbsOperator<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{

		@Override
		public NAV eval(final Deque<NAV> args)
		{
			return args.getFirst().abs();
		}

	}

	static final class AddOperator<NAV extends NumericValue<?, NAV>> implements MultaryOperation<NAV>
	{
		@Override
		public boolean isCommutative()
		{
			return true;
		}

		@Override
		public NAV eval(final Deque<NAV> args) throws ArithmeticException
		{
			final NAV arg0 = args.poll();
			return arg0.add(args);
		}

	}

	static final class MultiplyOperator<NAV extends NumericValue<?, NAV>> implements MultaryOperation<NAV>
	{

		@Override
		public boolean isCommutative()
		{
			return true;
		}

		@Override
		public NAV eval(final Deque<NAV> args) throws ArithmeticException
		{
			final NAV arg0 = args.poll();
			return arg0.multiply(args);
		}

	}

	static final class SubtractOperator<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{
		@Override
		public NAV eval(final Deque<NAV> args) throws ArithmeticException
		{
			final NAV arg0 = args.poll();
			final NAV arg1 = args.poll();
			return arg0.subtract(arg1);
		}

	}

	static final class DivideOperator<NAV extends NumericValue<?, NAV>> implements StaticOperation<NAV>
	{
		@Override
		public NAV eval(final Deque<NAV> args) throws ArithmeticException
		{
			final NAV arg0 = args.poll();
			final NAV arg1 = args.poll();
			return arg0.divide(arg1);
		}

	}

	static final StaticOperation<IntegerValue> INTEGER_MOD_OPERATOR = new StaticOperation<IntegerValue>()
	{
		@Override
		public IntegerValue eval(final Deque<IntegerValue> args) throws ArithmeticException
		{
			final IntegerValue arg0 = args.poll();
			final IntegerValue arg1 = args.poll();
			return arg0.remainder(arg1);
		}
	};

	static final StaticOperation<DoubleValue> FLOOR_OPERATOR = new StaticOperation<DoubleValue>()
	{

		@Override
		public DoubleValue eval(final Deque<DoubleValue> args)
		{
			return args.getFirst().floor();
		}

	};

	static final StaticOperation<DoubleValue> ROUND_OPERATOR = new StaticOperation<DoubleValue>()
	{
		@Override
		public DoubleValue eval(final Deque<DoubleValue> args)
		{
			return args.getFirst().roundIEEE754Default();
		}
	};
}
