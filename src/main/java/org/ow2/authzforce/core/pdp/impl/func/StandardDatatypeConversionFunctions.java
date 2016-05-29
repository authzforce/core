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

import org.ow2.authzforce.core.pdp.api.func.BaseFunctionSet;
import org.ow2.authzforce.core.pdp.api.func.DatatypeConversionFunction;
import org.ow2.authzforce.core.pdp.api.func.DatatypeConversionFunction.TypeConverter;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.func.FunctionSet;
import org.ow2.authzforce.core.pdp.api.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DNSNameWithPortRangeValue;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;

/**
 * Standard primitive datatype conversion functions: double-to-integer, integer-to-double, *-from-string, *-to-string, etc.
 * 
 * @version $Id: $
 */
public final class StandardDatatypeConversionFunctions
{
	private StandardDatatypeConversionFunctions()
	{
		// empty private constructor to prevent instantiation
	}

	/**
	 * Standard identifier for the double-to-integer function.
	 */
	public static final String NAME_DOUBLE_TO_INTEGER = Function.XACML_NS_1_0 + "double-to-integer";

	/**
	 * Standard identifier for the integer-to-double function.
	 */
	public static final String NAME_INTEGER_TO_DOUBLE = Function.XACML_NS_1_0 + "integer-to-double";

	/**
	 * Standard identifier for the boolean-from-string function.
	 */
	public static final String NAME_BOOLEAN_FROM_STRING = Function.XACML_NS_3_0 + "boolean-from-string";

	/**
	 * Standard identifier for the string-from-boolean function.
	 */
	public static final String NAME_STRING_FROM_BOOLEAN = Function.XACML_NS_3_0 + "string-from-boolean";

	/**
	 * Standard identifier for the integer-from-string function.
	 */
	public static final String NAME_INTEGER_FROM_STRING = Function.XACML_NS_3_0 + "integer-from-string";

	/**
	 * Standard identifier for the string-from-integer function.
	 */
	public static final String NAME_STRING_FROM_INTEGER = Function.XACML_NS_3_0 + "string-from-integer";

	/**
	 * Standard identifier for the double-from-string function.
	 */
	public static final String NAME_DOUBLE_FROM_STRING = Function.XACML_NS_3_0 + "double-from-string";

	/**
	 * Standard identifier for the string-from-double function.
	 */
	public static final String NAME_STRING_FROM_DOUBLE = Function.XACML_NS_3_0 + "string-from-double";

	/**
	 * Standard identifier for the time-from-string function.
	 */
	public static final String NAME_TIME_FROM_STRING = Function.XACML_NS_3_0 + "time-from-string";

	/**
	 * Standard identifier for the string-from-time function.
	 */
	public static final String NAME_STRING_FROM_TIME = Function.XACML_NS_3_0 + "string-from-time";

	/**
	 * Standard identifier for the date-from-string function.
	 */
	public static final String NAME_DATE_FROM_STRING = Function.XACML_NS_3_0 + "date-from-string";

	/**
	 * Standard identifier for the string-from-date function.
	 */
	public static final String NAME_STRING_FROM_DATE = Function.XACML_NS_3_0 + "string-from-date";

	/**
	 * Standard identifier for the dateTime-from-string function.
	 */
	public static final String NAME_DATETIME_FROM_STRING = Function.XACML_NS_3_0 + "dateTime-from-string";

	/**
	 * Standard identifier for the string-from-dateTime function.
	 */
	public static final String NAME_STRING_FROM_DATETIME = Function.XACML_NS_3_0 + "string-from-dateTime";

	/**
	 * Standard identifier for the anyURI-from-string function.
	 */
	public static final String NAME_ANYURI_FROM_STRING = Function.XACML_NS_3_0 + "anyURI-from-string";

	/**
	 * Standard identifier for the string-from-anyURI function.
	 */
	public static final String NAME_STRING_FROM_ANYURI = Function.XACML_NS_3_0 + "string-from-anyURI";

	/**
	 * Standard identifier for the dayTimeDuration-from-string function.
	 */
	public static final String NAME_DAYTIMEDURATION_FROM_STRING = Function.XACML_NS_3_0 + "dayTimeDuration-from-string";

	/**
	 * Standard identifier for the string-from-dayTimeDuration function.
	 */
	public static final String NAME_STRING_FROM_DAYTIMEDURATION = Function.XACML_NS_3_0 + "string-from-dayTimeDuration";

	/**
	 * Standard identifier for the yearMonthDuration-from-string function.
	 */
	public static final String NAME_YEARMONTHDURATION_FROM_STRING = Function.XACML_NS_3_0 + "yearMonthDuration-from-string";

	/**
	 * Standard identifier for the string-from-yearMonthDuration function.
	 */
	public static final String NAME_STRING_FROM_YEARMONTHDURATION = Function.XACML_NS_3_0 + "string-from-yearMonthDuration";

	/**
	 * Standard identifier for the x500Name-from-string function.
	 */
	public static final String NAME_X500NAME_FROM_STRING = Function.XACML_NS_3_0 + "x500Name-from-string";

	/**
	 * Standard identifier for the string-from-x500Name function.
	 */
	public static final String NAME_STRING_FROM_X500NAME = Function.XACML_NS_3_0 + "string-from-x500Name";

	/**
	 * Standard identifier for the rfc822Name-from-string function.
	 */
	public static final String NAME_RFC822NAME_FROM_STRING = Function.XACML_NS_3_0 + "rfc822Name-from-string";

	/**
	 * Standard identifier for the string-from-rfc822Name function.
	 */
	public static final String NAME_STRING_FROM_RFC822NAME = Function.XACML_NS_3_0 + "string-from-rfc822Name";

	/**
	 * Standard identifier for the ipAddress-from-string function.
	 */
	public static final String NAME_IPADDRESS_FROM_STRING = Function.XACML_NS_3_0 + "ipAddress-from-string";

	/**
	 * Standard identifier for the string-from-ipAddress function.
	 */
	public static final String NAME_STRING_FROM_IPADDRESS = Function.XACML_NS_3_0 + "string-from-ipAddress";

	/**
	 * Standard identifier for the dnsName-from-string function.
	 */
	public static final String NAME_DNSNAME_FROM_STRING = Function.XACML_NS_3_0 + "dnsName-from-string";

	/**
	 * Standard identifier for the string-from-dnsName function.
	 */
	public static final String NAME_STRING_FROM_DNSNAME = Function.XACML_NS_3_0 + "string-from-dnsName";

	private static final TypeConverter<IntegerValue, DoubleValue> DOUBLE_TO_INTEGER_CONVERTER = new TypeConverter<IntegerValue, DoubleValue>()
	{

		@Override
		public final IntegerValue convert(DoubleValue arg)
		{
			return new IntegerValue(arg.longValue());
		}

	};

	private static class IntegerToDoubleConverter implements TypeConverter<DoubleValue, IntegerValue>
	{
		private static final IllegalArgumentException INTEGER_OUT_OF_RANGE_EXCEPTION = new IllegalArgumentException("Integer argument is outside the range which can be represented by a double");

		@Override
		public final DoubleValue convert(IntegerValue arg)
		{
			try
			{
				return new DoubleValue(arg.doubleValue());
			} catch (IllegalArgumentException e)
			{
				throw INTEGER_OUT_OF_RANGE_EXCEPTION;
			}
		}
	}

	private static class FromStringConverter<RETURN extends SimpleValue<?>> implements TypeConverter<RETURN, StringValue>
	{
		private final SimpleValue.Factory<RETURN> returnTypeFactory;

		private FromStringConverter(SimpleValue.Factory<RETURN> returnTypeFactory)
		{
			this.returnTypeFactory = returnTypeFactory;
		}

		@Override
		public final RETURN convert(StringValue arg)
		{
			return returnTypeFactory.getInstance(arg.getUnderlyingValue(), null, null);

		}

	}

	private static class ToStringConverter<PARAM extends SimpleValue<?>> implements TypeConverter<StringValue, PARAM>
	{
		// not final because overriden specially by BooleanToString
		@Override
		public StringValue convert(PARAM arg)
		{
			return new StringValue(arg.toString());
		}

	}

	private static final ToStringConverter<BooleanValue> BOOLEAN_TO_STRING_CONVERTER = new ToStringConverter<BooleanValue>()
	{
		@Override
		public final StringValue convert(BooleanValue arg)
		{
			return StringValue.getInstance(arg);
		}

	};

	/**
	 * Datatype-conversion function cluster
	 */
	public static final FunctionSet SET = new BaseFunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "type-conversion",
	//
			new DatatypeConversionFunction<>(NAME_DOUBLE_TO_INTEGER, StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype(), DOUBLE_TO_INTEGER_CONVERTER),
			//
			new DatatypeConversionFunction<>(NAME_INTEGER_TO_DOUBLE, StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype(), new IntegerToDoubleConverter()),
			//
			new DatatypeConversionFunction<>(NAME_BOOLEAN_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.BOOLEAN_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_BOOLEAN, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(), BOOLEAN_TO_STRING_CONVERTER),
			//
			new DatatypeConversionFunction<>(NAME_INTEGER_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.INTEGER_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.INTEGER_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_INTEGER, StandardDatatypes.INTEGER_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<IntegerValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DOUBLE_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DOUBLE_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.DOUBLE_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DOUBLE, StandardDatatypes.DOUBLE_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<DoubleValue>()),
			//
			new DatatypeConversionFunction<>(NAME_TIME_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.TIME_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.TIME_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_TIME, StandardDatatypes.TIME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(), new ToStringConverter<TimeValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DATE_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DATE_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.DATE_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DATE, StandardDatatypes.DATE_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(), new ToStringConverter<DateValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DATETIME_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DATETIME_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.DATETIME_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DATETIME, StandardDatatypes.DATETIME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<DateTimeValue>()),
			//
			new DatatypeConversionFunction<>(NAME_ANYURI_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.ANYURI_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.ANYURI_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_ANYURI, StandardDatatypes.ANYURI_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<AnyURIValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DAYTIMEDURATION_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(),
					new FromStringConverter<>(StandardDatatypes.DAYTIMEDURATION_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DAYTIMEDURATION, StandardDatatypes.DAYTIMEDURATION_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<DayTimeDurationValue>()),
			//
			new DatatypeConversionFunction<>(NAME_YEARMONTHDURATION_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(),
					new FromStringConverter<>(StandardDatatypes.YEARMONTHDURATION_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_YEARMONTHDURATION, StandardDatatypes.YEARMONTHDURATION_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<YearMonthDurationValue>()),
			//
			new DatatypeConversionFunction<>(NAME_X500NAME_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.X500NAME_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.X500NAME_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_X500NAME, StandardDatatypes.X500NAME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<X500NameValue>()),
			//
			new DatatypeConversionFunction<>(NAME_RFC822NAME_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.RFC822NAME_FACTORY.getDatatype(),
					new FromStringConverter<>(StandardDatatypes.RFC822NAME_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_RFC822NAME, StandardDatatypes.RFC822NAME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<RFC822NameValue>()),
			//
			new DatatypeConversionFunction<>(NAME_IPADDRESS_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.IPADDRESS_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.IPADDRESS_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_IPADDRESS, StandardDatatypes.IPADDRESS_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<IPAddressValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DNSNAME_FROM_STRING, StandardDatatypes.STRING_FACTORY.getDatatype(), StandardDatatypes.DNSNAME_FACTORY.getDatatype(), new FromStringConverter<>(
					StandardDatatypes.DNSNAME_FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DNSNAME, StandardDatatypes.DNSNAME_FACTORY.getDatatype(), StandardDatatypes.STRING_FACTORY.getDatatype(),
					new ToStringConverter<DNSNameWithPortRangeValue>())
	//
	);

}
