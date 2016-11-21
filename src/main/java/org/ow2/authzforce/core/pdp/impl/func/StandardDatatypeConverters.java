/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
			return new IntegerValue(arg.longValue());
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
				return new DoubleValue(arg.doubleValue());
			}
			catch (final IllegalArgumentException e)
			{
				throw INTEGER_OUT_OF_RANGE_EXCEPTION;
			}
		}
	};

	static class FromStringConverter<RETURN extends SimpleValue<?>> implements TypeConverter<RETURN, StringValue>
	{
		private final SimpleValue.StringContentOnlyFactory<RETURN> returnTypeFactory;

		FromStringConverter(final SimpleValue.StringContentOnlyFactory<RETURN> returnTypeFactory)
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
