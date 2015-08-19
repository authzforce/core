package com.thalesgroup.authzforce.core.func;

import java.util.List;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.ExpressionResult;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

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
public abstract class DatatypeConversionFunction<PARAM_T extends PrimitiveAttributeValue<?>, RETURN_T extends PrimitiveAttributeValue<?>> extends FirstOrderFunction<RETURN_T>
{

	/**
	 * Standard TYPE_URI for the double-to-integer function.
	 */
	public static final String NAME_DOUBLE_TO_INTEGER = FUNCTION_NS_1 + "double-to-integer";

	/**
	 * Standard TYPE_URI for the integer-to-double function.
	 */
	public static final String NAME_INTEGER_TO_DOUBLE = FUNCTION_NS_1 + "integer-to-double";

	/**
	 * Standard TYPE_URI for the boolean-from-string function.
	 */
	public static final String NAME_BOOLEAN_FROM_STRING = FUNCTION_NS_3 + "boolean-from-string";

	/**
	 * Standard TYPE_URI for the string-from-boolean function.
	 */
	public static final String NAME_STRING_FROM_BOOLEAN = FUNCTION_NS_3 + "string-from-boolean";

	/**
	 * Standard TYPE_URI for the integer-from-string function.
	 */
	public static final String NAME_INTEGER_FROM_STRING = FUNCTION_NS_3 + "integer-from-string";

	/**
	 * Standard TYPE_URI for the string-from-integer function.
	 */
	public static final String NAME_STRING_FROM_INTEGER = FUNCTION_NS_3 + "string-from-integer";

	/**
	 * Standard TYPE_URI for the double-from-string function.
	 */
	public static final String NAME_DOUBLE_FROM_STRING = FUNCTION_NS_3 + "double-from-string";

	/**
	 * Standard TYPE_URI for the string-from-double function.
	 */
	public static final String NAME_STRING_FROM_DOUBLE = FUNCTION_NS_3 + "string-from-double";

	/**
	 * Standard TYPE_URI for the time-from-string function.
	 */
	public static final String NAME_TIME_FROM_STRING = FUNCTION_NS_3 + "time-from-string";

	/**
	 * Standard TYPE_URI for the string-from-time function.
	 */
	public static final String NAME_STRING_FROM_TIME = FUNCTION_NS_3 + "string-from-time";

	/**
	 * Standard TYPE_URI for the date-from-string function.
	 */
	public static final String NAME_DATE_FROM_STRING = FUNCTION_NS_3 + "date-from-string";

	/**
	 * Standard TYPE_URI for the string-from-date function.
	 */
	public static final String NAME_STRING_FROM_DATE = FUNCTION_NS_3 + "string-from-date";

	/**
	 * Standard TYPE_URI for the dateTime-from-string function.
	 */
	public static final String NAME_DATETIME_FROM_STRING = FUNCTION_NS_3 + "dateTime-from-string";

	/**
	 * Standard TYPE_URI for the string-from-dateTime function.
	 */
	public static final String NAME_STRING_FROM_DATETIME = FUNCTION_NS_3 + "string-from-dateTime";

	/**
	 * Standard TYPE_URI for the anyURI-from-string function.
	 */
	public static final String NAME_ANYURI_FROM_STRING = FUNCTION_NS_3 + "anyURI-from-string";

	/**
	 * Standard TYPE_URI for the string-from-anyURI function.
	 */
	public static final String NAME_STRING_FROM_ANYURI = FUNCTION_NS_3 + "string-from-anyURI";

	/**
	 * Standard TYPE_URI for the dayTimeDuration-from-string function.
	 */
	public static final String NAME_DAYTIMEDURATION_FROM_STRING = FUNCTION_NS_3 + "dayTimeDuration-from-string";

	/**
	 * Standard TYPE_URI for the string-from-dayTimeDuration function.
	 */
	public static final String NAME_STRING_FROM_DAYTIMEDURATION = FUNCTION_NS_3 + "string-from-dayTimeDuration";

	/**
	 * Standard TYPE_URI for the yearMonthDuration-from-string function.
	 */
	public static final String NAME_YEARMONTHDURATION_FROM_STRING = FUNCTION_NS_3 + "yearMonthDuration-from-string";

	/**
	 * Standard TYPE_URI for the string-from-yearMonthDuration function.
	 */
	public static final String NAME_STRING_FROM_YEARMONTHDURATION = FUNCTION_NS_3 + "string-from-yearMonthDuration";

	/**
	 * Standard TYPE_URI for the x500Name-from-string function.
	 */
	public static final String NAME_X500NAME_FROM_STRING = FUNCTION_NS_3 + "x500Name-from-string";

	/**
	 * Standard TYPE_URI for the string-from-x500Name function.
	 */
	public static final String NAME_STRING_FROM_X500NAME = FUNCTION_NS_3 + "string-from-x500Name";

	/**
	 * Standard TYPE_URI for the rfc822Name-from-string function.
	 */
	public static final String NAME_RFC822NAME_FROM_STRING = FUNCTION_NS_3 + "rfc822Name-from-string";

	/**
	 * Standard TYPE_URI for the string-from-rfc822Name function.
	 */
	public static final String NAME_STRING_FROM_RFC822NAME = FUNCTION_NS_3 + "string-from-rfc822Name";

	/**
	 * Standard TYPE_URI for the ipAddress-from-string function.
	 */
	public static final String NAME_IPADDRESS_FROM_STRING = FUNCTION_NS_3 + "ipAddress-from-string";

	/**
	 * Standard TYPE_URI for the string-from-ipAddress function.
	 */
	public static final String NAME_STRING_FROM_IPADDRESS = FUNCTION_NS_3 + "string-from-ipAddress";

	/**
	 * Standard TYPE_URI for the dnsName-from-string function.
	 */
	public static final String NAME_DNSNAME_FROM_STRING = FUNCTION_NS_3 + "dnsName-from-string";

	/**
	 * Standard TYPE_URI for the string-from-dnsName function.
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

	protected final Class<PARAM_T[]> parameterArrayClass;

	/**
	 * Creates a new <code>DatatypeConversionFunction</code> object.
	 * 
	 * @param funcURI
	 *            function URI
	 * 
	 * @param paramArrayType
	 *            function parameter array type
	 * @param paramTypeURI
	 *            parameter datatype URI
	 * @param returnTypeURI
	 *            parameter datatype URI
	 * 
	 */
	protected DatatypeConversionFunction(String funcURI, String paramTypeURI, Class<PARAM_T[]> paramArrayType, String returnTypeURI)
	{
		super(funcURI, new DatatypeDef(returnTypeURI), false, new DatatypeDef(paramTypeURI));
		this.parameterArrayClass = paramArrayType;

	}

	protected abstract RETURN_T convert(PARAM_T arg) throws IndeterminateEvaluationException;

	@Override
	protected final FirstOrderFunctionCall<RETURN_T> newCall(List<Expression<? extends ExpressionResult<? extends AttributeValue>>> argExpressions, DatatypeDef... remainingArgTypes) throws IllegalArgumentException
	{
		return new EagerPrimitiveEval<RETURN_T, PARAM_T>(signature, parameterArrayClass, argExpressions, remainingArgTypes)
		{
			@Override
			protected RETURN_T evaluate(PARAM_T[] args) throws IndeterminateEvaluationException
			{
				return convert(args[0]);
			}

		};
	}

	private static class DoubleToInteger extends DatatypeConversionFunction<DoubleAttributeValue, IntegerAttributeValue>
	{

		protected DoubleToInteger()
		{
			super(NAME_DOUBLE_TO_INTEGER, DoubleAttributeValue.TYPE_URI, DoubleAttributeValue[].class, IntegerAttributeValue.TYPE_URI);
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
			super(NAME_INTEGER_TO_DOUBLE, IntegerAttributeValue.TYPE_URI, IntegerAttributeValue[].class, DoubleAttributeValue.TYPE_URI);
		}

		private static final IndeterminateEvaluationException INTEGER_OUT_OF_RANGE_EXCEPTION = new IndeterminateEvaluationException("Function " + NAME_INTEGER_TO_DOUBLE + ": integer argument is outside the range which can be represented by a double", Status.STATUS_PROCESSING_ERROR);

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

	private static abstract class FromString<RETURN extends PrimitiveAttributeValue<?>> extends DatatypeConversionFunction<StringAttributeValue, RETURN>
	{

		private final String invalidStringArgErrMessage;

		protected FromString(String funcURI, String returnTypeURI, Class<RETURN> returnClass)
		{
			super(funcURI, StringAttributeValue.TYPE_URI, StringAttributeValue[].class, returnTypeURI);
			this.invalidStringArgErrMessage = "Function " + functionId + ": Invalid string arg (not a valid lexical representation of a " + returnClass.getSimpleName() + "): ";
		}

		@Override
		protected final RETURN convert(StringAttributeValue arg) throws IndeterminateEvaluationException
		{
			try
			{
				return convert(arg.getValue());
			} catch (IllegalArgumentException e)
			{
				throw new IndeterminateEvaluationException(invalidStringArgErrMessage + arg, Status.STATUS_SYNTAX_ERROR);
			}

		}

		abstract protected RETURN convert(String value) throws IllegalArgumentException;

	}

	private static abstract class ToString<PARAM extends PrimitiveAttributeValue<?>> extends DatatypeConversionFunction<PARAM, StringAttributeValue>
	{

		protected ToString(String funcURI, String paramTypeURI, Class<PARAM[]> paramArrayType)
		{
			super(funcURI, paramTypeURI, paramArrayType, StringAttributeValue.TYPE_URI);
		}
	}

	private static class BooleanFromString extends FromString<BooleanAttributeValue>
	{

		private BooleanFromString()
		{
			super(NAME_BOOLEAN_FROM_STRING, BooleanAttributeValue.TYPE_URI, BooleanAttributeValue.class);
		}

		@Override
		protected final BooleanAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return BooleanAttributeValue.getInstance(arg);

		}
	}

	private static class BooleanToString extends ToString<BooleanAttributeValue>
	{
		private BooleanToString()
		{
			super(NAME_STRING_FROM_BOOLEAN, BooleanAttributeValue.TYPE_URI, BooleanAttributeValue[].class);
		}

		@Override
		protected final StringAttributeValue convert(BooleanAttributeValue arg)
		{
			return StringAttributeValue.getInstance(arg.getValue());
		}

	}

	private static abstract class NonBooleanToString<PARAM extends PrimitiveAttributeValue<?>> extends ToString<PARAM>
	{

		protected NonBooleanToString(String funcURI, String paramTypeURI, Class<PARAM[]> paramArrayType)
		{
			super(funcURI, paramTypeURI, paramArrayType);
		}

		@Override
		protected StringAttributeValue convert(PARAM arg)
		{
			return new StringAttributeValue(arg.toString());
		}

	}

	private static class IntegerFromString extends FromString<IntegerAttributeValue>
	{

		private IntegerFromString()
		{
			super(NAME_INTEGER_FROM_STRING, IntegerAttributeValue.TYPE_URI, IntegerAttributeValue.class);
		}

		@Override
		protected final IntegerAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new IntegerAttributeValue(arg);

		}

	}

	private static class IntegerToString extends NonBooleanToString<IntegerAttributeValue>
	{
		private IntegerToString()
		{
			super(NAME_STRING_FROM_INTEGER, IntegerAttributeValue.TYPE_URI, IntegerAttributeValue[].class);
		}

	}

	private static class DoubleFromString extends FromString<DoubleAttributeValue>
	{

		private DoubleFromString()
		{
			super(NAME_DOUBLE_FROM_STRING, DoubleAttributeValue.TYPE_URI, DoubleAttributeValue.class);
		}

		@Override
		protected final DoubleAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new DoubleAttributeValue(arg);

		}

	}

	private static class DoubleToString extends NonBooleanToString<DoubleAttributeValue>
	{
		private DoubleToString()
		{
			super(NAME_STRING_FROM_DOUBLE, DoubleAttributeValue.TYPE_URI, DoubleAttributeValue[].class);
		}

	}

	private static class TimeFromString extends FromString<TimeAttributeValue>
	{

		private TimeFromString()
		{
			super(NAME_TIME_FROM_STRING, TimeAttributeValue.TYPE_URI, TimeAttributeValue.class);
		}

		@Override
		protected final TimeAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new TimeAttributeValue(arg);

		}

	}

	private static class TimeToString extends NonBooleanToString<TimeAttributeValue>
	{
		private TimeToString()
		{
			super(NAME_STRING_FROM_TIME, TimeAttributeValue.TYPE_URI, TimeAttributeValue[].class);
		}

	}

	private static class DateFromString extends FromString<DateAttributeValue>
	{

		private DateFromString()
		{
			super(NAME_DATE_FROM_STRING, DateAttributeValue.TYPE_URI, DateAttributeValue.class);
		}

		@Override
		protected final DateAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new DateAttributeValue(arg);

		}

	}

	private static class DateToString extends NonBooleanToString<DateAttributeValue>
	{
		private DateToString()
		{
			super(NAME_STRING_FROM_DATE, DateAttributeValue.TYPE_URI, DateAttributeValue[].class);
		}

	}

	private static class DateTimeFromString extends FromString<DateTimeAttributeValue>
	{

		private DateTimeFromString()
		{
			super(NAME_DATETIME_FROM_STRING, DateTimeAttributeValue.TYPE_URI, DateTimeAttributeValue.class);
		}

		@Override
		protected final DateTimeAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new DateTimeAttributeValue(arg);

		}

	}

	private static class DateTimeToString extends NonBooleanToString<DateTimeAttributeValue>
	{
		private DateTimeToString()
		{
			super(NAME_STRING_FROM_DATETIME, DateTimeAttributeValue.TYPE_URI, DateTimeAttributeValue[].class);
		}

	}

	private static class AnyUriFromString extends FromString<AnyURIAttributeValue>
	{

		private AnyUriFromString()
		{
			super(NAME_ANYURI_FROM_STRING, AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue.class);
		}

		@Override
		protected final AnyURIAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new AnyURIAttributeValue(arg);

		}

	}

	private static class AnyUriToString extends NonBooleanToString<AnyURIAttributeValue>
	{
		private AnyUriToString()
		{
			super(NAME_STRING_FROM_ANYURI, AnyURIAttributeValue.TYPE_URI, AnyURIAttributeValue[].class);
		}

	}

	private static class DayTimeDurationFromString extends FromString<DayTimeDurationAttributeValue>
	{

		private DayTimeDurationFromString()
		{
			super(NAME_DAYTIMEDURATION_FROM_STRING, DayTimeDurationAttributeValue.TYPE_URI, DayTimeDurationAttributeValue.class);
		}

		@Override
		protected final DayTimeDurationAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new DayTimeDurationAttributeValue(arg);

		}

	}

	private static class DayTimeDurationToString extends NonBooleanToString<DayTimeDurationAttributeValue>
	{
		private DayTimeDurationToString()
		{
			super(NAME_STRING_FROM_DAYTIMEDURATION, DayTimeDurationAttributeValue.TYPE_URI, DayTimeDurationAttributeValue[].class);
		}

	}

	private static class YearMonthDurationFromString extends FromString<YearMonthDurationAttributeValue>
	{

		private YearMonthDurationFromString()
		{
			super(NAME_YEARMONTHDURATION_FROM_STRING, YearMonthDurationAttributeValue.TYPE_URI, YearMonthDurationAttributeValue.class);
		}

		@Override
		protected final YearMonthDurationAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new YearMonthDurationAttributeValue(arg);

		}

	}

	private static class YearMonthDurationToString extends NonBooleanToString<YearMonthDurationAttributeValue>
	{
		private YearMonthDurationToString()
		{
			super(NAME_STRING_FROM_YEARMONTHDURATION, YearMonthDurationAttributeValue.TYPE_URI, YearMonthDurationAttributeValue[].class);
		}

	}

	private static class X500NameFromString extends FromString<X500NameAttributeValue>
	{

		private X500NameFromString()
		{
			super(NAME_X500NAME_FROM_STRING, X500NameAttributeValue.TYPE_URI, X500NameAttributeValue.class);
		}

		@Override
		protected final X500NameAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new X500NameAttributeValue(arg);

		}

	}

	private static class X500NameToString extends NonBooleanToString<X500NameAttributeValue>
	{
		private X500NameToString()
		{
			super(NAME_STRING_FROM_X500NAME, X500NameAttributeValue.TYPE_URI, X500NameAttributeValue[].class);
		}
	}

	private static class RFC822NameFromString extends FromString<RFC822NameAttributeValue>
	{

		private RFC822NameFromString()
		{
			super(NAME_RFC822NAME_FROM_STRING, RFC822NameAttributeValue.TYPE_URI, RFC822NameAttributeValue.class);
		}

		@Override
		protected final RFC822NameAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new RFC822NameAttributeValue(arg);

		}

	}

	private static class RFC822NameToString extends NonBooleanToString<RFC822NameAttributeValue>
	{
		private RFC822NameToString()
		{
			super(NAME_STRING_FROM_RFC822NAME, RFC822NameAttributeValue.TYPE_URI, RFC822NameAttributeValue[].class);
		}
	}

	private static class IpAddressFromString extends FromString<IPAddressAttributeValue>
	{

		private IpAddressFromString()
		{
			super(NAME_IPADDRESS_FROM_STRING, IPAddressAttributeValue.identifier, IPAddressAttributeValue.class);
		}

		@Override
		protected final IPAddressAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new IPAddressAttributeValue(arg);

		}

	}

	private static class IpAddressToString extends NonBooleanToString<IPAddressAttributeValue>
	{
		private IpAddressToString()
		{
			super(NAME_STRING_FROM_IPADDRESS, IPAddressAttributeValue.identifier, IPAddressAttributeValue[].class);
		}
	}

	private static class DnsNameFromString extends FromString<DNSNameAttributeValue>
	{

		private DnsNameFromString()
		{
			super(NAME_DNSNAME_FROM_STRING, DNSNameAttributeValue.identifier, DNSNameAttributeValue.class);
		}

		@Override
		protected final DNSNameAttributeValue convert(String arg) throws IllegalArgumentException
		{
			return new DNSNameAttributeValue(arg);

		}

	}

	private static class DnsNameToString extends NonBooleanToString<DNSNameAttributeValue>
	{
		private DnsNameToString()
		{
			super(NAME_STRING_FROM_DNSNAME, DNSNameAttributeValue.identifier, DNSNameAttributeValue[].class);
		}
	}

}
