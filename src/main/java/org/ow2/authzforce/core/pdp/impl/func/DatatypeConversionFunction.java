/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Expression;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.FunctionSet;
import org.ow2.authzforce.core.pdp.api.FunctionSignature;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.Value;
import org.ow2.authzforce.core.pdp.impl.value.AnyURIValue;
import org.ow2.authzforce.core.pdp.impl.value.BooleanValue;
import org.ow2.authzforce.core.pdp.impl.value.DNSNameValue;
import org.ow2.authzforce.core.pdp.impl.value.DatatypeConstants;
import org.ow2.authzforce.core.pdp.impl.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.impl.value.DateValue;
import org.ow2.authzforce.core.pdp.impl.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.impl.value.DoubleValue;
import org.ow2.authzforce.core.pdp.impl.value.IPAddressValue;
import org.ow2.authzforce.core.pdp.impl.value.IntegerValue;
import org.ow2.authzforce.core.pdp.impl.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.impl.value.SimpleValue;
import org.ow2.authzforce.core.pdp.impl.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.value.TimeValue;
import org.ow2.authzforce.core.pdp.impl.value.X500NameValue;
import org.ow2.authzforce.core.pdp.impl.value.YearMonthDurationValue;

/**
 * A class that implements all the primitive datatype conversion functions: double-to-integer, integer-to-double, *-from-string, *-to-string, etc. It takes one
 * argument of the appropriate type, converts that argument to the other type, and returns the result.
 *
 * @param <PARAM_T>
 *            parameter/input type
 * @param <RETURN_T>
 *            return/output type
 * @author cdangerv
 * @version $Id: $
 */
public final class DatatypeConversionFunction<PARAM_T extends AttributeValue, RETURN_T extends AttributeValue> extends
		FirstOrderFunction.SingleParameterTyped<RETURN_T, PARAM_T>
{

	/**
	 * Standard identifier for the double-to-integer function.
	 */
	public static final String NAME_DOUBLE_TO_INTEGER = XACML_NS_1_0 + "double-to-integer";

	/**
	 * Standard identifier for the integer-to-double function.
	 */
	public static final String NAME_INTEGER_TO_DOUBLE = XACML_NS_1_0 + "integer-to-double";

	/**
	 * Standard identifier for the boolean-from-string function.
	 */
	public static final String NAME_BOOLEAN_FROM_STRING = XACML_NS_3_0 + "boolean-from-string";

	/**
	 * Standard identifier for the string-from-boolean function.
	 */
	public static final String NAME_STRING_FROM_BOOLEAN = XACML_NS_3_0 + "string-from-boolean";

	/**
	 * Standard identifier for the integer-from-string function.
	 */
	public static final String NAME_INTEGER_FROM_STRING = XACML_NS_3_0 + "integer-from-string";

	/**
	 * Standard identifier for the string-from-integer function.
	 */
	public static final String NAME_STRING_FROM_INTEGER = XACML_NS_3_0 + "string-from-integer";

	/**
	 * Standard identifier for the double-from-string function.
	 */
	public static final String NAME_DOUBLE_FROM_STRING = XACML_NS_3_0 + "double-from-string";

	/**
	 * Standard identifier for the string-from-double function.
	 */
	public static final String NAME_STRING_FROM_DOUBLE = XACML_NS_3_0 + "string-from-double";

	/**
	 * Standard identifier for the time-from-string function.
	 */
	public static final String NAME_TIME_FROM_STRING = XACML_NS_3_0 + "time-from-string";

	/**
	 * Standard identifier for the string-from-time function.
	 */
	public static final String NAME_STRING_FROM_TIME = XACML_NS_3_0 + "string-from-time";

	/**
	 * Standard identifier for the date-from-string function.
	 */
	public static final String NAME_DATE_FROM_STRING = XACML_NS_3_0 + "date-from-string";

	/**
	 * Standard identifier for the string-from-date function.
	 */
	public static final String NAME_STRING_FROM_DATE = XACML_NS_3_0 + "string-from-date";

	/**
	 * Standard identifier for the dateTime-from-string function.
	 */
	public static final String NAME_DATETIME_FROM_STRING = XACML_NS_3_0 + "dateTime-from-string";

	/**
	 * Standard identifier for the string-from-dateTime function.
	 */
	public static final String NAME_STRING_FROM_DATETIME = XACML_NS_3_0 + "string-from-dateTime";

	/**
	 * Standard identifier for the anyURI-from-string function.
	 */
	public static final String NAME_ANYURI_FROM_STRING = XACML_NS_3_0 + "anyURI-from-string";

	/**
	 * Standard identifier for the string-from-anyURI function.
	 */
	public static final String NAME_STRING_FROM_ANYURI = XACML_NS_3_0 + "string-from-anyURI";

	/**
	 * Standard identifier for the dayTimeDuration-from-string function.
	 */
	public static final String NAME_DAYTIMEDURATION_FROM_STRING = XACML_NS_3_0 + "dayTimeDuration-from-string";

	/**
	 * Standard identifier for the string-from-dayTimeDuration function.
	 */
	public static final String NAME_STRING_FROM_DAYTIMEDURATION = XACML_NS_3_0 + "string-from-dayTimeDuration";

	/**
	 * Standard identifier for the yearMonthDuration-from-string function.
	 */
	public static final String NAME_YEARMONTHDURATION_FROM_STRING = XACML_NS_3_0 + "yearMonthDuration-from-string";

	/**
	 * Standard identifier for the string-from-yearMonthDuration function.
	 */
	public static final String NAME_STRING_FROM_YEARMONTHDURATION = XACML_NS_3_0 + "string-from-yearMonthDuration";

	/**
	 * Standard identifier for the x500Name-from-string function.
	 */
	public static final String NAME_X500NAME_FROM_STRING = XACML_NS_3_0 + "x500Name-from-string";

	/**
	 * Standard identifier for the string-from-x500Name function.
	 */
	public static final String NAME_STRING_FROM_X500NAME = XACML_NS_3_0 + "string-from-x500Name";

	/**
	 * Standard identifier for the rfc822Name-from-string function.
	 */
	public static final String NAME_RFC822NAME_FROM_STRING = XACML_NS_3_0 + "rfc822Name-from-string";

	/**
	 * Standard identifier for the string-from-rfc822Name function.
	 */
	public static final String NAME_STRING_FROM_RFC822NAME = XACML_NS_3_0 + "string-from-rfc822Name";

	/**
	 * Standard identifier for the ipAddress-from-string function.
	 */
	public static final String NAME_IPADDRESS_FROM_STRING = XACML_NS_3_0 + "ipAddress-from-string";

	/**
	 * Standard identifier for the string-from-ipAddress function.
	 */
	public static final String NAME_STRING_FROM_IPADDRESS = XACML_NS_3_0 + "string-from-ipAddress";

	/**
	 * Standard identifier for the dnsName-from-string function.
	 */
	public static final String NAME_DNSNAME_FROM_STRING = XACML_NS_3_0 + "dnsName-from-string";

	/**
	 * Standard identifier for the string-from-dnsName function.
	 */
	public static final String NAME_STRING_FROM_DNSNAME = XACML_NS_3_0 + "string-from-dnsName";

	private interface TypeConverter<RETURN, PARAM>
	{

		RETURN convert(PARAM arg) throws IllegalArgumentException;
	}

	private static final class CallFactory<RETURN extends Value, PARAM extends AttributeValue>
	{
		private final TypeConverter<RETURN, PARAM> converter;
		private final FunctionSignature.SingleParameterTyped<RETURN, PARAM> funcSig;
		private final String invalidArgMsgPrefix;

		private CallFactory(FunctionSignature.SingleParameterTyped<RETURN, PARAM> functionSignature, TypeConverter<RETURN, PARAM> converter)
		{
			this.funcSig = functionSignature;
			this.converter = converter;
			this.invalidArgMsgPrefix = "Function " + functionSignature.getName() + ": invalid arg: ";
		}

		public FirstOrderFunctionCall<RETURN> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			return new EagerSinglePrimitiveTypeEval<RETURN, PARAM>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected RETURN evaluate(Deque<PARAM> args) throws IndeterminateEvaluationException
				{
					final PARAM arg0 = args.getFirst();
					try
					{
						return converter.convert(arg0);
					} catch (IllegalArgumentException e)
					{
						throw new IndeterminateEvaluationException(invalidArgMsgPrefix + arg0, StatusHelper.STATUS_PROCESSING_ERROR, e);
					}
				}

			};
		}
	}

	private final CallFactory<RETURN_T, PARAM_T> funcCallFactory;

	/**
	 * Creates a new <code>DatatypeConversionFunction</code> object.
	 * 
	 * @param funcURI
	 *            function URI
	 * 
	 * @param paramArrayType
	 *            function parameter array type
	 * @param paramType
	 *            parameter type
	 * @param returnType
	 *            return type
	 * 
	 */
	private DatatypeConversionFunction(String funcURI, Datatype<PARAM_T> paramType, Datatype<RETURN_T> returnType, TypeConverter<RETURN_T, PARAM_T> converter)
	{
		super(funcURI, returnType, false, Arrays.asList(paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, converter);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<RETURN_T> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

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
		private static final IllegalArgumentException INTEGER_OUT_OF_RANGE_EXCEPTION = new IllegalArgumentException(
				"Integer argument is outside the range which can be represented by a double");

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
	public static final FunctionSet CLUSTER = new BaseFunctionSet(
			FunctionSet.DEFAULT_ID_NAMESPACE + "type-conversion",
			//
			new DatatypeConversionFunction<>(NAME_DOUBLE_TO_INTEGER, DatatypeConstants.DOUBLE.TYPE, DatatypeConstants.INTEGER.TYPE, DOUBLE_TO_INTEGER_CONVERTER),
			//
			new DatatypeConversionFunction<>(NAME_INTEGER_TO_DOUBLE, DatatypeConstants.INTEGER.TYPE, DatatypeConstants.DOUBLE.TYPE,
					new IntegerToDoubleConverter()),
			//
			new DatatypeConversionFunction<>(NAME_BOOLEAN_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.BOOLEAN.TYPE,
					new FromStringConverter<>(DatatypeConstants.BOOLEAN.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_BOOLEAN, DatatypeConstants.BOOLEAN.TYPE, DatatypeConstants.STRING.TYPE,
					BOOLEAN_TO_STRING_CONVERTER),
			//
			new DatatypeConversionFunction<>(NAME_INTEGER_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.INTEGER.TYPE,
					new FromStringConverter<>(DatatypeConstants.INTEGER.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_INTEGER, DatatypeConstants.INTEGER.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<IntegerValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DOUBLE_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.DOUBLE.TYPE, new FromStringConverter<>(
					DatatypeConstants.DOUBLE.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DOUBLE, DatatypeConstants.DOUBLE.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<DoubleValue>()),
			//
			new DatatypeConversionFunction<>(NAME_TIME_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.TIME.TYPE, new FromStringConverter<>(
					DatatypeConstants.TIME.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_TIME, DatatypeConstants.TIME.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<TimeValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DATE_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.DATE.TYPE, new FromStringConverter<>(
					DatatypeConstants.DATE.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DATE, DatatypeConstants.DATE.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<DateValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DATETIME_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.DATETIME.TYPE,
					new FromStringConverter<>(DatatypeConstants.DATETIME.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DATETIME, DatatypeConstants.DATETIME.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<DateTimeValue>()),
			//
			new DatatypeConversionFunction<>(NAME_ANYURI_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.ANYURI.TYPE, new FromStringConverter<>(
					DatatypeConstants.ANYURI.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_ANYURI, DatatypeConstants.ANYURI.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<AnyURIValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DAYTIMEDURATION_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.DAYTIMEDURATION.TYPE,
					new FromStringConverter<>(DatatypeConstants.DAYTIMEDURATION.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DAYTIMEDURATION, DatatypeConstants.DAYTIMEDURATION.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<DayTimeDurationValue>()),
			//
			new DatatypeConversionFunction<>(NAME_YEARMONTHDURATION_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.YEARMONTHDURATION.TYPE,
					new FromStringConverter<>(DatatypeConstants.YEARMONTHDURATION.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_YEARMONTHDURATION, DatatypeConstants.YEARMONTHDURATION.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<YearMonthDurationValue>()),
			//
			new DatatypeConversionFunction<>(NAME_X500NAME_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.X500NAME.TYPE,
					new FromStringConverter<>(DatatypeConstants.X500NAME.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_X500NAME, DatatypeConstants.X500NAME.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<X500NameValue>()),
			//
			new DatatypeConversionFunction<>(NAME_RFC822NAME_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.RFC822NAME.TYPE,
					new FromStringConverter<>(DatatypeConstants.RFC822NAME.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_RFC822NAME, DatatypeConstants.RFC822NAME.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<RFC822NameValue>()),
			//
			new DatatypeConversionFunction<>(NAME_IPADDRESS_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.IPADDRESS.TYPE,
					new FromStringConverter<>(DatatypeConstants.IPADDRESS.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_IPADDRESS, DatatypeConstants.IPADDRESS.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<IPAddressValue>()),
			//
			new DatatypeConversionFunction<>(NAME_DNSNAME_FROM_STRING, DatatypeConstants.STRING.TYPE, DatatypeConstants.DNSNAME.TYPE,
					new FromStringConverter<>(DatatypeConstants.DNSNAME.FACTORY)),
			//
			new DatatypeConversionFunction<>(NAME_STRING_FROM_DNSNAME, DatatypeConstants.DNSNAME.TYPE, DatatypeConstants.STRING.TYPE,
					new ToStringConverter<DNSNameValue>())
	//
	);

}
