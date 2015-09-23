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

import java.util.List;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.Base64BinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.HexBinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.SimpleAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.func.FirstOrderFunctionCall.EagerPrimitiveEval;

/**
 * Implements generic match functions taking parameters of same/equal type, i.e. standard (A.3.1)
 * Equality predicates and special match function x500Name-match
 * 
 * @param <PARAM>
 *            type of compared parameters
 */
public abstract class EqualTypeMatchFunction<PARAM extends AttributeValue<PARAM>> extends FirstOrderFunction<BooleanAttributeValue>
{

	/**
	 * Standard identifier for the string-equal function.
	 */
	public static final String NAME_STRING_EQUAL = FUNCTION_NS_1 + "string-equal";

	/**
	 * Standard identifier for the boolean-equal function.
	 */
	public static final String NAME_BOOLEAN_EQUAL = FUNCTION_NS_1 + "boolean-equal";

	/**
	 * Standard identifier for the integer-equal function.
	 */
	public static final String NAME_INTEGER_EQUAL = FUNCTION_NS_1 + "integer-equal";

	/**
	 * Standard identifier for the double-equal function.
	 */
	public static final String NAME_DOUBLE_EQUAL = FUNCTION_NS_1 + "double-equal";

	/**
	 * Standard identifier for the date-equal function.
	 */
	public static final String NAME_DATE_EQUAL = FUNCTION_NS_1 + "date-equal";

	/**
	 * Standard identifier for the time-equal function.
	 */
	public static final String NAME_TIME_EQUAL = FUNCTION_NS_1 + "time-equal";

	/**
	 * Standard identifier for the dateTime-equal function.
	 */
	public static final String NAME_DATETIME_EQUAL = FUNCTION_NS_1 + "dateTime-equal";

	/**
	 * Standard identifier for the dayTimeDuration-equal function.
	 */
	public static final String NAME_DAYTIME_DURATION_EQUAL = FUNCTION_NS_3 + "dayTimeDuration-equal";

	/**
	 * Standard identifier for the yearMonthDuration-equal function.
	 */
	public static final String NAME_YEARMONTH_DURATION_EQUAL = FUNCTION_NS_3 + "yearMonthDuration-equal";

	/**
	 * Standard identifier for the anyURI-equal function.
	 */
	public static final String NAME_ANYURI_EQUAL = FUNCTION_NS_1 + "anyURI-equal";

	/**
	 * Standard identifier for the x500Name-equal function.
	 */
	public static final String NAME_X500NAME_EQUAL = FUNCTION_NS_1 + "x500Name-equal";

	/**
	 * Standard identifier for the rfc822Name-equal function.
	 */
	public static final String NAME_RFC822NAME_EQUAL = FUNCTION_NS_1 + "rfc822Name-equal";

	/**
	 * Standard identifier for the hexBinary-equal function.
	 */
	public static final String NAME_HEXBINARY_EQUAL = FUNCTION_NS_1 + "hexBinary-equal";

	/**
	 * Standard identifier for the base64Binary-equal function.
	 */
	public static final String NAME_BASE64BINARY_EQUAL = FUNCTION_NS_1 + "base64Binary-equal";

	/**
	 * Standard identifier for the ipAddress-equal function.
	 */
	public static final String NAME_IPADDRESS_EQUAL = FUNCTION_NS_2 + "ipAddress-equal";

	/**
	 * Standard identifier for the dnsName-equal function.
	 */
	public static final String NAME_DNSNAME_EQUAL = FUNCTION_NS_2 + "dnsName-equal";

	/**
	 * Standard identifier for the string-equal-ignore-case function.
	 */
	private static final String NAME_STRING_EQUAL_IGNORE_CASE = FUNCTION_NS_3 + "string-equal-ignore-case";

	/**
	 * Standard identifier for the x500Name-match function (different from x500Name-regexp-match
	 * down below).
	 */
	public static final String NAME_X500NAME_MATCH = FUNCTION_NS_1 + "x500Name-match";

	// /**
	// * Logger used for all classes
	// */
	// private static final Logger LOGGER = LoggerFactory
	// .getLogger(EqualTypeMatchFunction.class);

	private final Class<PARAM[]> parameterArrayClass;

	/**
	 * Function cluster
	 */
	public static final FunctionSet CLUSTER = new FunctionSet(FunctionSet.DEFAULT_ID_NAMESPACE + "equal-type-match",
	//
			new Equal<>(NAME_STRING_EQUAL, DatatypeConstants.STRING.TYPE, StringAttributeValue[].class),
			//
			new Equal<>(NAME_BOOLEAN_EQUAL, DatatypeConstants.BOOLEAN.TYPE, BooleanAttributeValue[].class),
			//
			new Equal<>(NAME_INTEGER_EQUAL, DatatypeConstants.INTEGER.TYPE, IntegerAttributeValue[].class),
			//
			new Equal<>(NAME_DOUBLE_EQUAL, DatatypeConstants.DOUBLE.TYPE, DoubleAttributeValue[].class),
			//
			new Equal<>(NAME_DATE_EQUAL, DatatypeConstants.DATE.TYPE, DateAttributeValue[].class),
			//
			new Equal<>(NAME_TIME_EQUAL, DatatypeConstants.TIME.TYPE, TimeAttributeValue[].class),
			//
			new Equal<>(NAME_DATETIME_EQUAL, DatatypeConstants.DATETIME.TYPE, DateTimeAttributeValue[].class),
			//
			new Equal<>(NAME_DAYTIME_DURATION_EQUAL, DatatypeConstants.DAYTIMEDURATION.TYPE, DayTimeDurationAttributeValue[].class),
			//
			new Equal<>(NAME_YEARMONTH_DURATION_EQUAL, DatatypeConstants.YEARMONTHDURATION.TYPE, YearMonthDurationAttributeValue[].class),
			//
			new Equal<>(NAME_ANYURI_EQUAL, DatatypeConstants.ANYURI.TYPE, AnyURIAttributeValue[].class),
			//
			new Equal<>(NAME_X500NAME_EQUAL, DatatypeConstants.X500NAME.TYPE, X500NameAttributeValue[].class),
			//
			new Equal<>(NAME_RFC822NAME_EQUAL, DatatypeConstants.RFC822NAME.TYPE, RFC822NameAttributeValue[].class),
			//
			new Equal<>(NAME_HEXBINARY_EQUAL, DatatypeConstants.HEXBINARY.TYPE, HexBinaryAttributeValue[].class),
			//
			new Equal<>(NAME_BASE64BINARY_EQUAL, DatatypeConstants.BASE64BINARY.TYPE, Base64BinaryAttributeValue[].class),
			//
			new Equal<>(NAME_IPADDRESS_EQUAL, DatatypeConstants.IPADDRESS.TYPE, IPAddressAttributeValue[].class),
			//
			new Equal<>(NAME_DNSNAME_EQUAL, DatatypeConstants.DNSNAME.TYPE, DNSNameAttributeValue[].class),
			//
			new EqualIgnoreCase<>(NAME_STRING_EQUAL_IGNORE_CASE, DatatypeConstants.STRING.TYPE, StringAttributeValue[].class),
			//
			new X500NameMatch());

	/**
	 * Creates a new <code>EqualTypeMatchFunction</code> object.
	 * 
	 * @param functionName
	 *            the standard XACML name of the function to be handled by this object, including
	 *            the full namespace
	 * @param paramType
	 *            parameter type
	 * @param paramArrayType
	 *            parameter array type
	 */
	public EqualTypeMatchFunction(String functionName, Datatype<PARAM> paramType, Class<PARAM[]> paramArrayType)
	{
		super(functionName, DatatypeConstants.BOOLEAN.TYPE, false, paramType, paramType);
		this.parameterArrayClass = paramArrayType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List,
	 * com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	@Override
	protected FirstOrderFunctionCall<BooleanAttributeValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return new EagerPrimitiveEval<BooleanAttributeValue, PARAM>(signature, parameterArrayClass, argExpressions, remainingArgTypes)
		{
			@Override
			protected final BooleanAttributeValue evaluate(PARAM[] args) throws IndeterminateEvaluationException
			{
				return BooleanAttributeValue.valueOf(match(args[0], args[1]));
			}

		};
	}

	protected abstract boolean match(PARAM arg0, PARAM arg1);

	/**
	 * *-equal function
	 * 
	 * @param <PARAM>
	 *            parameter type
	 */
	private static class Equal<PARAM extends AttributeValue<PARAM>> extends EqualTypeMatchFunction<PARAM>
	{

		/**
		 * Instantiates *-equal function
		 * 
		 * @param functionName
		 *            function ID
		 * @param paramType
		 *            datatype of parameters
		 * @param paramArrayType
		 *            parameter array type
		 */
		public Equal(String functionName, Datatype<PARAM> paramType, Class<PARAM[]> paramArrayType)
		{
			super(functionName, paramType, paramArrayType);
		}

		@Override
		public final boolean match(PARAM arg0, PARAM arg1)
		{
			return arg0.equals(arg1);
		}
	}

	/**
	 * *-equal-ignore-case function
	 * 
	 * @param <PARAM>
	 *            parameter type
	 */
	private static class EqualIgnoreCase<PARAM extends SimpleAttributeValue<String, PARAM>> extends EqualTypeMatchFunction<PARAM>
	{

		/**
		 * Instantiates *-equal-ignore-case function
		 * 
		 * @param functionName
		 *            function ID
		 * @param paramType
		 *            datatype of parameters
		 * @param paramArrayType
		 *            parameter class
		 */
		public EqualIgnoreCase(String functionName, Datatype<PARAM> paramType, Class<PARAM[]> paramArrayType)
		{
			super(functionName, paramType, paramArrayType);
		}

		@Override
		public final boolean match(PARAM arg0, PARAM arg1)
		{
			return arg0.getUnderlyingValue().equalsIgnoreCase(arg1.getUnderlyingValue());
		}
	}

	/**
	 * x500Name-match function
	 * 
	 */
	private static class X500NameMatch extends EqualTypeMatchFunction<X500NameAttributeValue>
	{

		/**
		 * Instantiates the function
		 */
		public X500NameMatch()
		{
			super(NAME_X500NAME_MATCH, DatatypeConstants.X500NAME.TYPE, X500NameAttributeValue[].class);
		}

		@Override
		public final boolean match(X500NameAttributeValue arg0, X500NameAttributeValue arg1)
		{
			return arg0.match(arg1);
		}
	}

}
