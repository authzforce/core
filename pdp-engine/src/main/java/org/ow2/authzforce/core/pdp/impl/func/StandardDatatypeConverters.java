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

import org.ow2.authzforce.core.pdp.api.func.DatatypeConversionFunction.TypeConverter;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

/**
 * Standard primitive datatype conversion functions: double-to-integer, integer-to-double, *-from-string, *-to-string, etc.
 * 
 * @version $Id: $
 */
final class StandardDatatypeConverters
{

	static final TypeConverter<IntegerValue, DoubleValue> DOUBLE_TO_INTEGER = new TypeConverter<IntegerValue, DoubleValue>()
	{

		@Override
		public final IntegerValue convert(final DoubleValue arg)
		{

			return IntegerValue.valueOf(arg.longValue());
		}

	};

	private static final IllegalArgumentException INTEGER_OUT_OF_RANGE_EXCEPTION = new IllegalArgumentException("Integer argument is outside the range which can be represented by a double");

	static final TypeConverter<DoubleValue, IntegerValue> INTEGER_TO_DOUBLE = new TypeConverter<DoubleValue, IntegerValue>()
	{

		@Override
		public final DoubleValue convert(final IntegerValue arg)
		{
			try
			{
				return new DoubleValue(Double.valueOf(arg.doubleValue()));
			}
			catch (final IllegalArgumentException e)
			{
				throw INTEGER_OUT_OF_RANGE_EXCEPTION;
			}
		}
	};

	static class FromStringConverter<RETURN extends SimpleValue<?>> implements TypeConverter<RETURN, StringValue>
	{
		private final SimpleValue.StringParseableValueFactory<RETURN> returnTypeFactory;

		FromStringConverter(final SimpleValue.StringParseableValueFactory<RETURN> returnTypeFactory)
		{
			this.returnTypeFactory = returnTypeFactory;
		}

		@Override
		public final RETURN convert(final StringValue arg)
		{
			return returnTypeFactory.getInstance(arg.getUnderlyingValue());

		}

	}

	static class ToStringConverter<PARAM extends SimpleValue<?>> implements TypeConverter<StringValue, PARAM>
	{
		// not final because overriden specially by BooleanToString
		@Override
		public StringValue convert(final PARAM arg)
		{
			return new StringValue(arg.toString());
		}

	}

	static final ToStringConverter<BooleanValue> BOOLEAN_TO_STRING = new ToStringConverter<BooleanValue>()
	{
		@Override
		public final StringValue convert(final BooleanValue arg)
		{
			return StringValue.getInstance(arg);
		}

	};

	private StandardDatatypeConverters()
	{
		// empty private constructor to prevent instantiation
	}
}
