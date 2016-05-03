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
package org.ow2.authzforce.core.pdp.impl.value;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.AttributeValue;
import org.ow2.authzforce.core.pdp.api.BagDatatype;
import org.ow2.authzforce.core.pdp.api.Datatype;
import org.ow2.authzforce.core.pdp.api.Function;

/**
 * XACML standard datatype constants, i.e. constants related to XACML standard datatypes
 *
 * @param <AV>
 *            Concrete Java type actually implementing the datatype
 * 
 * @version $Id: $
 */
public final class DatatypeConstants<AV extends AttributeValue>
{

	private static final SimpleValue.Factory<StringValue> STRING_FACTORY = new SimpleValue.StringContentOnlyFactory<StringValue>(StringValue.class,
			StringValue.TYPE_URI)
	{

		@Override
		public StringValue getInstance(String val)
		{
			return StringValue.parse(val);
		}

	};

	/**
	 * string
	 */
	public static final DatatypeConstants<StringValue> STRING = new DatatypeConstants<>(STRING_FACTORY, StringValue[].class, Function.XACML_NS_1_0 + "string");

	private static final SimpleValue.Factory<BooleanValue> BOOLEAN_FACTORY = new SimpleValue.StringContentOnlyFactory<BooleanValue>(BooleanValue.class,
			BooleanValue.TYPE_URI)
	{

		@Override
		public BooleanValue getInstance(String val)
		{
			return BooleanValue.getInstance(val);
		}

	};
	/**
	 * boolean
	 */
	public static final DatatypeConstants<BooleanValue> BOOLEAN = new DatatypeConstants<>(BOOLEAN_FACTORY, BooleanValue[].class, Function.XACML_NS_1_0
			+ "boolean");

	private static final SimpleValue.Factory<IntegerValue> INTEGER_FACTORY = new SimpleValue.StringContentOnlyFactory<IntegerValue>(IntegerValue.class,
			IntegerValue.TYPE_URI)
	{

		@Override
		public IntegerValue getInstance(String val)
		{
			return new IntegerValue(val);
		}

	};

	/**
	 * integer
	 */
	public static final DatatypeConstants<IntegerValue> INTEGER = new DatatypeConstants<>(INTEGER_FACTORY, IntegerValue[].class, Function.XACML_NS_1_0
			+ "integer");

	private static final SimpleValue.Factory<DoubleValue> DOUBLE_FACTORY = new SimpleValue.StringContentOnlyFactory<DoubleValue>(DoubleValue.class,
			DoubleValue.TYPE_URI)
	{

		@Override
		public DoubleValue getInstance(String val)
		{
			return new DoubleValue(val);
		}

	};

	/**
	 * double
	 */
	public static final DatatypeConstants<DoubleValue> DOUBLE = new DatatypeConstants<>(DOUBLE_FACTORY, DoubleValue[].class, Function.XACML_NS_1_0 + "double");

	private static final SimpleValue.Factory<TimeValue> TIME_FACTORY = new SimpleValue.StringContentOnlyFactory<TimeValue>(TimeValue.class, TimeValue.TYPE_URI)
	{

		@Override
		public TimeValue getInstance(String val)
		{
			return new TimeValue(val);
		}

	};

	/**
	 * time
	 */
	public static final DatatypeConstants<TimeValue> TIME = new DatatypeConstants<>(TIME_FACTORY, TimeValue[].class, Function.XACML_NS_1_0 + "time");

	private static final SimpleValue.Factory<DateValue> DATE_FACTORY = new SimpleValue.StringContentOnlyFactory<DateValue>(DateValue.class, DateValue.TYPE_URI)
	{

		@Override
		public DateValue getInstance(String val)
		{
			return new DateValue(val);
		}

	};

	/**
	 * date
	 */
	public static final DatatypeConstants<DateValue> DATE = new DatatypeConstants<>(DATE_FACTORY, DateValue[].class, Function.XACML_NS_1_0 + "date");

	private static final SimpleValue.Factory<DateTimeValue> DATETIME_FACTORY = new SimpleValue.StringContentOnlyFactory<DateTimeValue>(DateTimeValue.class,
			DateTimeValue.TYPE_URI)
	{

		@Override
		public DateTimeValue getInstance(String val)
		{
			return new DateTimeValue(val);
		}

	};

	/**
	 * dateTime
	 */
	public static final DatatypeConstants<DateTimeValue> DATETIME = new DatatypeConstants<>(DATETIME_FACTORY, DateTimeValue[].class, Function.XACML_NS_1_0
			+ "dateTime");

	private static final SimpleValue.Factory<AnyURIValue> ANYURI_FACTORY = new SimpleValue.StringContentOnlyFactory<AnyURIValue>(AnyURIValue.class,
			AnyURIValue.TYPE_URI)
	{

		@Override
		public AnyURIValue getInstance(String val)
		{
			return new AnyURIValue(val);
		}

	};

	/**
	 * anyURI
	 */
	public static final DatatypeConstants<AnyURIValue> ANYURI = new DatatypeConstants<>(ANYURI_FACTORY, AnyURIValue[].class, Function.XACML_NS_1_0 + "anyURI");

	private static final SimpleValue.Factory<HexBinaryValue> HEXBINARY_FACTORY = new SimpleValue.StringContentOnlyFactory<HexBinaryValue>(HexBinaryValue.class,
			HexBinaryValue.TYPE_URI)
	{

		@Override
		public HexBinaryValue getInstance(String val)
		{
			return new HexBinaryValue(val);
		}

	};

	/**
	 * hexBinary
	 */
	public static final DatatypeConstants<HexBinaryValue> HEXBINARY = new DatatypeConstants<>(HEXBINARY_FACTORY, HexBinaryValue[].class, Function.XACML_NS_1_0
			+ "hexBinary");

	private static final SimpleValue.Factory<Base64BinaryValue> BASE64BINARY_FACTORY = new SimpleValue.StringContentOnlyFactory<Base64BinaryValue>(
			Base64BinaryValue.class, Base64BinaryValue.TYPE_URI)
	{
		@Override
		public Base64BinaryValue getInstance(String val)
		{
			return new Base64BinaryValue(val);
		}

	};

	/**
	 * base64Binary
	 */
	public static final DatatypeConstants<Base64BinaryValue> BASE64BINARY = new DatatypeConstants<>(BASE64BINARY_FACTORY, Base64BinaryValue[].class,
			Function.XACML_NS_1_0 + "base64Binary");

	private static final SimpleValue.Factory<X500NameValue> X500NAME_FACTORY = new SimpleValue.StringContentOnlyFactory<X500NameValue>(X500NameValue.class,
			X500NameValue.TYPE_URI)
	{

		@Override
		public X500NameValue getInstance(String val)
		{
			return new X500NameValue(val);
		}

	};

	/**
	 * x500Name
	 */
	public static final DatatypeConstants<X500NameValue> X500NAME = new DatatypeConstants<>(X500NAME_FACTORY, X500NameValue[].class, Function.XACML_NS_1_0
			+ "x500Name");

	private static final SimpleValue.Factory<RFC822NameValue> RFC822NAME_FACTORY = new SimpleValue.StringContentOnlyFactory<RFC822NameValue>(
			RFC822NameValue.class, RFC822NameValue.TYPE_URI)
	{

		@Override
		public RFC822NameValue getInstance(String val)
		{
			return new RFC822NameValue(val);
		}
	};

	/**
	 * rfc822Name
	 */
	public static final DatatypeConstants<RFC822NameValue> RFC822NAME = new DatatypeConstants<>(RFC822NAME_FACTORY, RFC822NameValue[].class,
			Function.XACML_NS_1_0 + "rfc822Name");

	private static final SimpleValue.Factory<IPAddressValue> IPADDRESS_FACTORY = new SimpleValue.StringContentOnlyFactory<IPAddressValue>(IPAddressValue.class,
			IPAddressValue.TYPE_URI)
	{
		@Override
		public IPAddressValue getInstance(String value)
		{
			return new IPAddressValue(value);
		}

	};

	/**
	 * ipAddress
	 */
	public static final DatatypeConstants<IPAddressValue> IPADDRESS = new DatatypeConstants<>(IPADDRESS_FACTORY, IPAddressValue[].class, Function.XACML_NS_2_0
			+ "ipAddress");

	private static final SimpleValue.Factory<DNSNameValue> DNSNAME_FACTORY = new SimpleValue.StringContentOnlyFactory<DNSNameValue>(DNSNameValue.class,
			DNSNameValue.TYPE_URI)
	{

		@Override
		public DNSNameValue getInstance(String value)
		{
			return new DNSNameValue(value);
		}

	};

	/**
	 * dnsName
	 */
	public static final DatatypeConstants<DNSNameValue> DNSNAME = new DatatypeConstants<>(DNSNAME_FACTORY, DNSNameValue[].class, Function.XACML_NS_2_0
			+ "dnsName");

	private static final SimpleValue.Factory<DayTimeDurationValue> DAYTIMEDURATION_FACTORY = new SimpleValue.StringContentOnlyFactory<DayTimeDurationValue>(
			DayTimeDurationValue.class, DayTimeDurationValue.TYPE_URI)
	{

		@Override
		public DayTimeDurationValue getInstance(String val)
		{
			return new DayTimeDurationValue(val);
		}

	};

	/**
	 * dayTimeDuration
	 */
	public static final DatatypeConstants<DayTimeDurationValue> DAYTIMEDURATION = new DatatypeConstants<>(DAYTIMEDURATION_FACTORY,
			DayTimeDurationValue[].class, Function.XACML_NS_3_0 + "dayTimeDuration");

	/**
	 * Datatype factory instance
	 */
	private static final SimpleValue.Factory<YearMonthDurationValue> YEARMONTHDURATION_FACTORY = new SimpleValue.StringContentOnlyFactory<YearMonthDurationValue>(
			YearMonthDurationValue.class, YearMonthDurationValue.TYPE_URI)
	{

		@Override
		public YearMonthDurationValue getInstance(String val)
		{
			return new YearMonthDurationValue(val);
		}

	};

	/**
	 * yearMonthDuration
	 */
	public static final DatatypeConstants<YearMonthDurationValue> YEARMONTHDURATION = new DatatypeConstants<>(YEARMONTHDURATION_FACTORY,
			YearMonthDurationValue[].class, Function.XACML_NS_3_0 + "yearMonthDuration");

	private static final SimpleValue.Factory<XPathValue> XPATH_FACTORY = new SimpleValue.Factory<XPathValue>(XPathValue.class, XPathValue.TYPE_URI)
	{
		@Override
		public XPathValue getInstance(String value, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			return new XPathValue(value, otherXmlAttributes, xPathCompiler);
		}

		@Override
		public boolean isExpressionStatic()
		{
			// xpathExpression evaluation result depends on the context (request Content node)
			return false;
		}

	};

	/**
	 * xpathExpression
	 */
	public static final DatatypeConstants<XPathValue> XPATH = new DatatypeConstants<>(XPATH_FACTORY, XPathValue[].class, Function.XACML_NS_3_0 + "xpath");

	/**
	 * Constants for standard mandatory datatype (xpathExpression is optional, therefore excluded)
	 */
	public static final Set<DatatypeConstants<? extends SimpleValue<? extends Object>>> MANDATORY_DATATYPE_SET = Collections.unmodifiableSet(new HashSet<>(
			Arrays.asList(STRING, BOOLEAN, INTEGER, DOUBLE, TIME, DATE, DATETIME, ANYURI, HEXBINARY, BASE64BINARY, X500NAME, RFC822NAME, IPADDRESS, DNSNAME,
					DAYTIMEDURATION, YEARMONTHDURATION)));

	/**
	 * (Primitive) Datatype
	 */
	public final Datatype<AV> TYPE;

	/**
	 * Bag datatype based on the primitive datatype {@link #TYPE}
	 */
	public final BagDatatype<AV> BAG_TYPE;

	/**
	 * class of array of instances of this datatype
	 */
	public final Class<AV[]> ARRAY_CLASS;

	/**
	 * Prefix of URI of standard functions taking parameters of this datatype (and this datatype only)
	 */
	public final String FUNCTION_ID_PREFIX;

	/**
	 * Datatype factory
	 */
	public final SimpleValue.Factory<AV> FACTORY;

	private DatatypeConstants(SimpleValue.Factory<AV> valueFactory, Class<AV[]> valueArrayClass, String functionIdPrefix)
	{
		this.TYPE = valueFactory.getDatatype();
		this.FACTORY = valueFactory;
		this.BAG_TYPE = valueFactory.getBagDatatype();
		this.ARRAY_CLASS = valueArrayClass;
		this.FUNCTION_ID_PREFIX = functionIdPrefix;
	}
}
