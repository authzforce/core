/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.func;

import java.util.Deque;
import java.util.List;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.Expression;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;
import com.thalesgroup.authzforce.core.datatypes.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.AttributeValue;
import com.thalesgroup.authzforce.core.datatypes.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DatatypeConstants;
import com.thalesgroup.authzforce.core.datatypes.DateAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.StringAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.TimeAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.datatypes.SimpleAttributeValue.StringContentOnlyFactory;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;

/**
 * A class that implements all the primitive datatype conversion functions: double-to-integer,
 * integer-to-double, *-from-string, *-to-string, etc. It takes one argument of the appropriate
 * type, converts that argument to the other type, and returns the result.
 * 
 * @param <PARAM_T>
 *            parameter/input type
 * @param <RETURN_T>
 *            return/output type
 */
public abstract class DatatypeConversionFunction<PARAM_T extends SimpleAttributeValue<?, PARAM_T>, RETURN_T extends SimpleAttributeValue<?, RETURN_T>> extends FirstOrderFunction<RETURN_T>
{

	/**
	 * Standard identifier for the double-to-integer function.
	 */
	public static final String NAME_DOUBLE_TO_INTEGER = FUNCTION_NS_1 + "double-to-integer";

	/**
	 * Standard identifier for the integer-to-double function.
	 */
	public static final String NAME_INTEGER_TO_DOUBLE = FUNCTION_NS_1 + "integer-to-double";

	/**
	 * Standard identifier for the boolean-from-string function.
	 */
	public static final String NAME_BOOLEAN_FROM_STRING = FUNCTION_NS_3 + "boolean-from-string";

	/**
	 * Standard identifier for the string-from-boolean function.
	 */
	public static final String NAME_STRING_FROM_BOOLEAN = FUNCTION_NS_3 + "string-from-boolean";

	/**
	 * Standard identifier for the integer-from-string function.
	 */
	public static final String NAME_INTEGER_FROM_STRING = FUNCTION_NS_3 + "integer-from-string";

	/**
	 * Standard identifier for the string-from-integer function.
	 */
	public static final String NAME_STRING_FROM_INTEGER = FUNCTION_NS_3 + "string-from-integer";

	/**
	 * Standard identifier for the double-from-string function.
	 */
	public static final String NAME_DOUBLE_FROM_STRING = FUNCTION_NS_3 + "double-from-string";

	/**
	 * Standard identifier for the string-from-double function.
	 */
	public static final String NAME_STRING_FROM_DOUBLE = FUNCTION_NS_3 + "string-from-double";

	/**
	 * Standard identifier for the time-from-string function.
	 */
	public static final String NAME_TIME_FROM_STRING = FUNCTION_NS_3 + "time-from-string";

	/**
	 * Standard identifier for the string-from-time function.
	 */
	public static final String NAME_STRING_FROM_TIME = FUNCTION_NS_3 + "string-from-time";

	/**
	 * Standard identifier for the date-from-string function.
	 */
	public static final String NAME_DATE_FROM_STRING = FUNCTION_NS_3 + "date-from-string";

	/**
	 * Standard identifier for the string-from-date function.
	 */
	public static final String NAME_STRING_FROM_DATE = FUNCTION_NS_3 + "string-from-date";

	/**
	 * Standard identifier for the dateTime-from-string function.
	 */
	public static final String NAME_DATETIME_FROM_STRING = FUNCTION_NS_3 + "dateTime-from-string";

	/**
	 * Standard identifier for the string-from-dateTime function.
	 */
	public static final String NAME_STRING_FROM_DATETIME = FUNCTION_NS_3 + "string-from-dateTime";

	/**
	 * Standard identifier for the anyURI-from-string function.
	 */
	public static final String NAME_ANYURI_FROM_STRING = FUNCTION_NS_3 + "anyURI-from-string";

	/**
	 * Standard identifier for the string-from-anyURI function.
	 */
	public static final String NAME_STRING_FROM_ANYURI = FUNCTION_NS_3 + "string-from-anyURI";

	/**
	 * Standard identifier for the dayTimeDuration-from-string function.
	 */
	public static final String NAME_DAYTIMEDURATION_FROM_STRING = FUNCTION_NS_3 + "dayTimeDuration-from-string";

	/**
	 * Standard identifier for the string-from-dayTimeDuration function.
	 */
	public static final String NAME_STRING_FROM_DAYTIMEDURATION = FUNCTION_NS_3 + "string-from-dayTimeDuration";

	/**
	 * Standard identifier for the yearMonthDuration-from-string function.
	 */
	public static final String NAME_YEARMONTHDURATION_FROM_STRING = FUNCTION_NS_3 + "yearMonthDuration-from-string";

	/**
	 * Standard identifier for the string-from-yearMonthDuration function.
	 */
	public static final String NAME_STRING_FROM_YEARMONTHDURATION = FUNCTION_NS_3 + "string-from-yearMonthDuration";

	/**
	 * Standard identifier for the x500Name-from-string function.
	 */
	public static final String NAME_X500NAME_FROM_STRING = FUNCTION_NS_3 + "x500Name-from-string";

	/**
	 * Standard identifier for the string-from-x500Name function.
	 */
	public static final String NAME_STRING_FROM_X500NAME = FUNCTION_NS_3 + "string-from-x500Name";

	/**
	 * Standard identifier for the rfc822Name-from-string function.
	 */
	public static final String NAME_RFC822NAME_FROM_STRING = FUNCTION_NS_3 + "rfc822Name-from-string";

	/**
	 * Standard identifier for the string-from-rfc822Name function.
	 */
	public static final String NAME_STRING_FROM_RFC822NAME = FUNCTION_NS_3 + "string-from-rfc822Name";

	/**
	 * Standard identifier for the ipAddress-from-string function.
	 */
	public static final String NAME_IPADDRESS_FROM_STRING = FUNCTION_NS_3 + "ipAddress-from-string";

	/**
	 * Standard identifier for the string-from-ipAddress function.
	 */
	public static final String NAME_STRING_FROM_IPADDRESS = FUNCTION_NS_3 + "string-from-ipAddress";

	/**
	 * Standard identifier for the dnsName-from-string function.
	 */
	public static final String NAME_DNSNAME_FROM_STRING = FUNCTION_NS_3 + "dnsName-from-string";

	/**
	 * Standard identifier for the string-from-dnsName function.
	 */
	public static final String NAME_STRING_FROM_DNSNAME = FUNCTION_NS_3 + "string-from-dnsName";

	/**
	 * Datatype-conversion function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "type-conversion",
	//
			new DoubleToInteger(),
			//
			new IntegerToDouble(),
			//
			new BooleanFromString(),
			//
			new BooleanToString(),
			//
			new IntegerFromString(),
			//
			new IntegerToString(),
			//
			new DoubleFromString(),
			//
			new DoubleToString(),
			//
			new TimeFromString(),
			//
			new TimeToString(),
			//
			new DateFromString(),
			//
			new DateToString(),
			//
			new DateTimeFromString(),
			//
			new DateTimeToString(),
			//
			new AnyUriFromString(),
			//
			new AnyUriToString(),
			//
			new DayTimeDurationFromString(),
			//
			new DayTimeDurationToString(),
			//
			new YearMonthDurationFromString(),
			//
			new YearMonthDurationToString(),
			//
			new X500NameFromString(),
			//
			new X500NameToString(),
			//
			new RFC822NameFromString(),
			//
			new RFC822NameToString(),
			//
			new IpAddressFromString(),
			//
			new IpAddressToString(),
			//
			new DnsNameFromString(),
			//
			new DnsNameToString());

	protected final Datatype<PARAM_T> paramType;

	/**
	 * Creates a new <code>DatatypeConversionFunction</code> object.
	 * 
	 * @param funcURI
	 *            function URI
	 * 
	 * @param paramArrayType
	 *            function parameter array type
	 * @param paramTypeDef
	 *            parameter type definition
	 * @param returnType
	 *            return type
	 * 
	 */
	protected DatatypeConversionFunction(String funcURI, DatatypeConstants<PARAM_T> paramTypeDef, Datatype<RETURN_T> returnType)
	{
		super(funcURI, returnType, false, paramTypeDef.TYPE);
		this.paramType = paramTypeDef.TYPE;

	}

	protected abstract RETURN_T convert(PARAM_T arg) throws IndeterminateEvaluationException;

	@Override
	protected final FirstOrderFunctionCall<RETURN_T> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return new EagerSinglePrimitiveTypeEval<RETURN_T, PARAM_T>(signature, paramType, argExpressions, remainingArgTypes)
		{
			@Override
			protected RETURN_T evaluate(Deque<PARAM_T> args) throws IndeterminateEvaluationException
			{
				return convert(args.getFirst());
			}

		};
	}

	private static class DoubleToInteger extends DatatypeConversionFunction<DoubleAttributeValue, IntegerAttributeValue>
	{

		protected DoubleToInteger()
		{
			super(NAME_DOUBLE_TO_INTEGER, DatatypeConstants.DOUBLE, DatatypeConstants.INTEGER.TYPE);
		}

		@Override
		protected final IntegerAttributeValue convert(DoubleAttributeValue arg)
		{
			return arg.toInteger();
		}

	}

	private static class IntegerToDouble extends DatatypeConversionFunction<IntegerAttributeValue, DoubleAttributeValue>
	{

		protected IntegerToDouble()
		{
			super(NAME_INTEGER_TO_DOUBLE, DatatypeConstants.INTEGER, DatatypeConstants.DOUBLE.TYPE);
		}

		private static final IndeterminateEvaluationException INTEGER_OUT_OF_RANGE_EXCEPTION = new IndeterminateEvaluationException("Function " + NAME_INTEGER_TO_DOUBLE + ": integer argument is outside the range which can be represented by a double", StatusHelper.STATUS_PROCESSING_ERROR);

		@Override
		protected final DoubleAttributeValue convert(IntegerAttributeValue arg) throws IndeterminateEvaluationException
		{
			try
			{
				return arg.toDouble();
			} catch (IllegalArgumentException e)
			{
				throw INTEGER_OUT_OF_RANGE_EXCEPTION;
			}
		}

	}

	private static abstract class FromString<RETURN extends SimpleAttributeValue<?, RETURN>> extends DatatypeConversionFunction<StringAttributeValue, RETURN>
	{

		private final String invalidStringArgErrMessage;
		private final StringContentOnlyFactory<RETURN> returnTypeFactory;

		protected FromString(String funcURI, DatatypeConstants<RETURN> returnTypeDef)
		{
			super(funcURI, DatatypeConstants.STRING, returnTypeDef.TYPE);
			this.invalidStringArgErrMessage = "Function " + functionId + ": Invalid string arg: not a valid lexical representation of XACML datatype '" + returnTypeDef.TYPE + "': ";
			final AttributeValue.Factory<RETURN> fac = returnTypeDef.FACTORY;
			if (!(fac instanceof StringContentOnlyFactory))
			{
				throw new UnsupportedOperationException("Function '" + funcURI + "' not supported: incompatible return datatype: " + returnTypeDef.TYPE);
			}

			returnTypeFactory = (StringContentOnlyFactory<RETURN>) fac;
		}

		@Override
		protected final RETURN convert(StringAttributeValue arg) throws IndeterminateEvaluationException
		{
			try
			{
				return returnTypeFactory.getInstance(arg.getUnderlyingValue());
			} catch (IllegalArgumentException e)
			{
				throw new IndeterminateEvaluationException(invalidStringArgErrMessage + arg, StatusHelper.STATUS_SYNTAX_ERROR);
			}

		}

	}

	private static abstract class ToString<PARAM extends SimpleAttributeValue<?, PARAM>> extends DatatypeConversionFunction<PARAM, StringAttributeValue>
	{

		protected ToString(String funcURI, DatatypeConstants<PARAM> paramTypeDef)
		{
			super(funcURI, paramTypeDef, DatatypeConstants.STRING.TYPE);
		}

		@Override
		protected final StringAttributeValue convert(PARAM arg) throws IndeterminateEvaluationException
		{
			return arg.toStringAttributeValue();
		}

	}

	private static class BooleanToString extends ToString<BooleanAttributeValue>
	{

		private BooleanToString()
		{
			super(NAME_STRING_FROM_BOOLEAN, DatatypeConstants.BOOLEAN);
		}

	}

	private static class BooleanFromString extends FromString<BooleanAttributeValue>
	{

		private BooleanFromString()
		{
			super(NAME_BOOLEAN_FROM_STRING, DatatypeConstants.BOOLEAN);
		}
	}

	private static class IntegerFromString extends FromString<IntegerAttributeValue>
	{

		private IntegerFromString()
		{
			super(NAME_INTEGER_FROM_STRING, DatatypeConstants.INTEGER);
		}
	}

	private static class IntegerToString extends ToString<IntegerAttributeValue>
	{
		private IntegerToString()
		{
			super(NAME_STRING_FROM_INTEGER, DatatypeConstants.INTEGER);
		}

	}

	private static class DoubleFromString extends FromString<DoubleAttributeValue>
	{

		private DoubleFromString()
		{
			super(NAME_DOUBLE_FROM_STRING, DatatypeConstants.DOUBLE);
		}
	}

	private static class DoubleToString extends ToString<DoubleAttributeValue>
	{
		private DoubleToString()
		{
			super(NAME_STRING_FROM_DOUBLE, DatatypeConstants.DOUBLE);
		}

	}

	private static class TimeFromString extends FromString<TimeAttributeValue>
	{

		private TimeFromString()
		{
			super(NAME_TIME_FROM_STRING, DatatypeConstants.TIME);
		}
	}

	private static class TimeToString extends ToString<TimeAttributeValue>
	{
		private TimeToString()
		{
			super(NAME_STRING_FROM_TIME, DatatypeConstants.TIME);
		}

	}

	private static class DateFromString extends FromString<DateAttributeValue>
	{

		private DateFromString()
		{
			super(NAME_DATE_FROM_STRING, DatatypeConstants.DATE);
		}
	}

	private static class DateToString extends ToString<DateAttributeValue>
	{
		private DateToString()
		{
			super(NAME_STRING_FROM_DATE, DatatypeConstants.DATE);
		}

	}

	private static class DateTimeFromString extends FromString<DateTimeAttributeValue>
	{

		private DateTimeFromString()
		{
			super(NAME_DATETIME_FROM_STRING, DatatypeConstants.DATETIME);
		}

	}

	private static class DateTimeToString extends ToString<DateTimeAttributeValue>
	{
		private DateTimeToString()
		{
			super(NAME_STRING_FROM_DATETIME, DatatypeConstants.DATETIME);
		}

	}

	private static class AnyUriFromString extends FromString<AnyURIAttributeValue>
	{

		private AnyUriFromString()
		{
			super(NAME_ANYURI_FROM_STRING, DatatypeConstants.ANYURI);
		}

	}

	private static class AnyUriToString extends ToString<AnyURIAttributeValue>
	{
		private AnyUriToString()
		{
			super(NAME_STRING_FROM_ANYURI, DatatypeConstants.ANYURI);
		}

	}

	private static class DayTimeDurationFromString extends FromString<DayTimeDurationAttributeValue>
	{

		private DayTimeDurationFromString()
		{
			super(NAME_DAYTIMEDURATION_FROM_STRING, DatatypeConstants.DAYTIMEDURATION);
		}

	}

	private static class DayTimeDurationToString extends ToString<DayTimeDurationAttributeValue>
	{
		private DayTimeDurationToString()
		{
			super(NAME_STRING_FROM_DAYTIMEDURATION, DatatypeConstants.DAYTIMEDURATION);
		}

	}

	private static class YearMonthDurationFromString extends FromString<YearMonthDurationAttributeValue>
	{

		private YearMonthDurationFromString()
		{
			super(NAME_YEARMONTHDURATION_FROM_STRING, DatatypeConstants.YEARMONTHDURATION);
		}

	}

	private static class YearMonthDurationToString extends ToString<YearMonthDurationAttributeValue>
	{
		private YearMonthDurationToString()
		{
			super(NAME_STRING_FROM_YEARMONTHDURATION, DatatypeConstants.YEARMONTHDURATION);
		}

	}

	private static class X500NameFromString extends FromString<X500NameAttributeValue>
	{

		private X500NameFromString()
		{
			super(NAME_X500NAME_FROM_STRING, DatatypeConstants.X500NAME);
		}

	}

	private static class X500NameToString extends ToString<X500NameAttributeValue>
	{
		private X500NameToString()
		{
			super(NAME_STRING_FROM_X500NAME, DatatypeConstants.X500NAME);
		}
	}

	private static class RFC822NameFromString extends FromString<RFC822NameAttributeValue>
	{

		private RFC822NameFromString()
		{
			super(NAME_RFC822NAME_FROM_STRING, DatatypeConstants.RFC822NAME);
		}

	}

	private static class RFC822NameToString extends ToString<RFC822NameAttributeValue>
	{
		private RFC822NameToString()
		{
			super(NAME_STRING_FROM_RFC822NAME, DatatypeConstants.RFC822NAME);
		}
	}

	private static class IpAddressFromString extends FromString<IPAddressAttributeValue>
	{

		private IpAddressFromString()
		{
			super(NAME_IPADDRESS_FROM_STRING, DatatypeConstants.IPADDRESS);
		}

	}

	private static class IpAddressToString extends ToString<IPAddressAttributeValue>
	{
		private IpAddressToString()
		{
			super(NAME_STRING_FROM_IPADDRESS, DatatypeConstants.IPADDRESS);
		}
	}

	private static class DnsNameFromString extends FromString<DNSNameAttributeValue>
	{

		private DnsNameFromString()
		{
			super(NAME_DNSNAME_FROM_STRING, DatatypeConstants.DNSNAME);
		}

	}

	private static class DnsNameToString extends ToString<DNSNameAttributeValue>
	{
		private DnsNameToString()
		{
			super(NAME_STRING_FROM_DNSNAME, DatatypeConstants.DNSNAME);
		}
	}

}
