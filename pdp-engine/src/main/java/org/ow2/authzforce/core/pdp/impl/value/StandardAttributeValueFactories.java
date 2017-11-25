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
package org.ow2.authzforce.core.pdp.impl.value;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.ArbitrarilyBigInteger;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.Base64BinaryValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.DayTimeDurationValue;
import org.ow2.authzforce.core.pdp.api.value.DnsNameWithPortRangeValue;
import org.ow2.authzforce.core.pdp.api.value.DoubleValue;
import org.ow2.authzforce.core.pdp.api.value.HexBinaryValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.IpAddressValue;
import org.ow2.authzforce.core.pdp.api.value.Rfc822NameValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XACML standard datatypes
 *
 * 
 * @version $Id: $
 */
public final class StandardAttributeValueFactories
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StandardAttributeValueFactories.class);

	/**
	 * string
	 */
	public static final SimpleValue.StringContentOnlyFactory<StringValue> STRING = new SimpleValue.StringContentOnlyFactory<StringValue>(StandardDatatypes.STRING)
	{

		@Override
		public StringValue parse(final String val)
		{
			return StringValue.parse(val);
		}

	};

	/**
	 * boolean
	 */
	public static final SimpleValue.StringParseableValueFactory<BooleanValue> BOOLEAN = new SimpleValue.StringParseableValueFactory<BooleanValue>(StandardDatatypes.BOOLEAN)
	{

		@Override
		public BooleanValue parse(final String val)
		{
			return BooleanValue.getInstance(val);
		}

		@Override
		public BooleanValue getInstance(final Serializable value)
		{
			if (value instanceof Boolean)
			{
				return new BooleanValue((Boolean) value);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw new IllegalArgumentException("Invalid input type to Boolean AttributeValue factory: " + value.getClass().getName() + ". Expected one of: " + Boolean.class + "," + String.class);
		}

	};

	private static abstract class IntegerValueFactory extends SimpleValue.StringParseableValueFactory<IntegerValue>
	{

		private IntegerValueFactory()
		{
			super(StandardDatatypes.INTEGER);
		}

	}

	/**
	 * integer parsed into {@link Integer}, therefore supports medium-size integers (representing xsd:int)
	 */
	public static final SimpleValue.StringParseableValueFactory<IntegerValue> MEDIUM_INTEGER = new IntegerValueFactory()
	{

		@Override
		public IntegerValue parse(final String val) throws IllegalArgumentException
		{
			final int i;
			try
			{
				i = DatatypeConverter.parseInt(val);
			}
			catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException(this + ": input value not valid or too big: " + val);
			}

			return IntegerValue.valueOf(i);
		}

		@Override
		public IntegerValue getInstance(final Serializable value) throws IllegalArgumentException
		{
			if (value instanceof Integer)
			{
				return IntegerValue.valueOf(((Integer) value).intValue());
			}

			if (value instanceof BigInteger)
			{
				final BigInteger bigInt = (BigInteger) value;
				final int i;
				try
				{
					i = bigInt.intValueExact();
				}
				catch (final ArithmeticException e)
				{
					throw new IllegalArgumentException(this + ": input value not supported (too big): " + bigInt);
				}

				return IntegerValue.valueOf(i);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw new IllegalArgumentException("Invalid input type to Integer AttributeValue factory: " + value.getClass().getName() + ". Expected one of: " + Integer.class + "," + BigInteger.class
					+ "," + String.class);
		}

	};

	/**
	 * integer parsed into {@link Long}, therefore supports long integers (representing xsd:long)
	 */
	public static final SimpleValue.StringParseableValueFactory<IntegerValue> LONG_INTEGER = new IntegerValueFactory()
	{

		@Override
		public IntegerValue parse(final String val) throws IllegalArgumentException
		{
			final long i;
			try
			{
				i = DatatypeConverter.parseLong(val);
			}
			catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException(this + ": input value not valid or too big: " + val);
			}

			return IntegerValue.valueOf(i);
		}

		@Override
		public IntegerValue getInstance(final Serializable value) throws IllegalArgumentException
		{
			if (value instanceof Integer)
			{
				return IntegerValue.valueOf(((Integer) value).intValue());
			}

			if (value instanceof BigInteger)
			{
				final BigInteger bigInt = (BigInteger) value;
				final long i;
				try
				{
					i = bigInt.longValueExact();
				}
				catch (final ArithmeticException e)
				{
					throw new IllegalArgumentException(this + ": input value not supported (too big): " + bigInt);
				}

				return IntegerValue.valueOf(i);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw new IllegalArgumentException("Invalid input type to Integer AttributeValue factory: " + value.getClass().getName() + ". Expected one of: " + Integer.class + "," + BigInteger.class
					+ "," + String.class);
		}

	};

	/**
	 * integer parsed into {@link BigInteger}, therefore supports arbitrary-precision integers (i.e. any xsd:integer)
	 */
	public static final SimpleValue.StringParseableValueFactory<IntegerValue> BIG_INTEGER = new IntegerValueFactory()
	{

		private IntegerValue getInstance(final BigInteger bigi)
		{
			final long i;
			try
			{
				i = bigi.longValueExact();
				return IntegerValue.valueOf(i);
			}
			catch (final ArithmeticException e)
			{
				LOGGER.debug("Input integer too big to fit in a long: {}", bigi);
			}

			/*
			 * TODO: if it can fit in a long, use IntegerValue.valueOf(long l) -> new LongInteger class
			 */
			return new IntegerValue(new ArbitrarilyBigInteger(bigi));
		}

		@Override
		public IntegerValue parse(final String val) throws IllegalArgumentException
		{
			final BigInteger bigInt;
			try
			{
				bigInt = DatatypeConverter.parseInteger(val);
			}
			catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException(this + ": input value not valid: " + val);
			}

			return getInstance(bigInt);
		}

		@Override
		public IntegerValue getInstance(final Serializable value) throws IllegalArgumentException
		{
			if (value instanceof Integer)
			{
				return IntegerValue.valueOf(((Integer) value).intValue());
			}

			if (value instanceof BigInteger)
			{
				final BigInteger bigInt = (BigInteger) value;
				return getInstance(bigInt);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw new IllegalArgumentException("Invalid input type to Integer AttributeValue factory: " + value.getClass().getName() + ". Expected one of: " + Integer.class + "," + BigInteger.class
					+ "," + String.class);
		}

	};

	/**
	 * double
	 */
	public static final SimpleValue.StringParseableValueFactory<DoubleValue> DOUBLE = new SimpleValue.StringParseableValueFactory<DoubleValue>(StandardDatatypes.DOUBLE)
	{

		@Override
		public DoubleValue parse(final String val)
		{
			return new DoubleValue(val);
		}

		@Override
		public DoubleValue getInstance(final Serializable value)
		{
			if (value instanceof Double)
			{
				return new DoubleValue((Double) value);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw new IllegalArgumentException("Invalid input type to Double AttributeValue factory: " + value.getClass().getName() + ". Expected one of: " + Double.class + "," + String.class);
		}

	};

	/**
	 * time
	 */
	public static final SimpleValue.StringContentOnlyFactory<TimeValue> TIME = new SimpleValue.StringContentOnlyFactory<TimeValue>(StandardDatatypes.TIME)
	{

		@Override
		public TimeValue parse(final String val)
		{
			return new TimeValue(val);
		}

	};

	/**
	 * date
	 */
	public static final SimpleValue.StringContentOnlyFactory<DateValue> DATE = new SimpleValue.StringContentOnlyFactory<DateValue>(StandardDatatypes.DATE)
	{

		@Override
		public DateValue parse(final String val)
		{
			return new DateValue(val);
		}

	};

	/**
	 * dateTime
	 */
	public static final SimpleValue.StringContentOnlyFactory<DateTimeValue> DATETIME = new SimpleValue.StringContentOnlyFactory<DateTimeValue>(StandardDatatypes.DATETIME)
	{

		@Override
		public DateTimeValue parse(final String val)
		{
			return new DateTimeValue(val);
		}

	};

	/**
	 * anyURI
	 */
	public static final SimpleValue.StringContentOnlyFactory<AnyUriValue> ANYURI = new SimpleValue.StringContentOnlyFactory<AnyUriValue>(StandardDatatypes.ANYURI)
	{

		@Override
		public AnyUriValue parse(final String val)
		{
			return new AnyUriValue(val);
		}

	};

	/**
	 * hexBinary
	 */
	public static final SimpleValue.StringContentOnlyFactory<HexBinaryValue> HEXBINARY = new SimpleValue.StringContentOnlyFactory<HexBinaryValue>(StandardDatatypes.HEXBINARY)
	{

		@Override
		public HexBinaryValue parse(final String val)
		{
			return new HexBinaryValue(val);
		}

	};

	/**
	 * base64Binary
	 */
	public static final SimpleValue.StringContentOnlyFactory<Base64BinaryValue> BASE64BINARY = new SimpleValue.StringContentOnlyFactory<Base64BinaryValue>(StandardDatatypes.BASE64BINARY)
	{
		@Override
		public Base64BinaryValue parse(final String val)
		{
			return new Base64BinaryValue(val);
		}

	};

	/**
	 * x500Name
	 */
	public static final SimpleValue.StringContentOnlyFactory<X500NameValue> X500NAME = new SimpleValue.StringContentOnlyFactory<X500NameValue>(StandardDatatypes.X500NAME)
	{

		@Override
		public X500NameValue parse(final String val)
		{
			return new X500NameValue(val);
		}

	};

	/**
	 * rfc822Name
	 */
	public static final SimpleValue.StringContentOnlyFactory<Rfc822NameValue> RFC822NAME = new SimpleValue.StringContentOnlyFactory<Rfc822NameValue>(StandardDatatypes.RFC822NAME)
	{

		@Override
		public Rfc822NameValue parse(final String val)
		{
			return new Rfc822NameValue(val);
		}
	};

	/**
	 * ipAddress
	 */
	public static final SimpleValue.StringContentOnlyFactory<IpAddressValue> IPADDRESS = new SimpleValue.StringContentOnlyFactory<IpAddressValue>(StandardDatatypes.IPADDRESS)
	{
		@Override
		public IpAddressValue parse(final String value)
		{
			return new IpAddressValue(value);
		}

	};

	/**
	 * dnsName
	 */
	public static final SimpleValue.StringContentOnlyFactory<DnsNameWithPortRangeValue> DNSNAME = new SimpleValue.StringContentOnlyFactory<DnsNameWithPortRangeValue>(StandardDatatypes.DNSNAME)
	{

		@Override
		public DnsNameWithPortRangeValue parse(final String value)
		{
			return new DnsNameWithPortRangeValue(value);
		}

	};

	/**
	 * dayTimeDuration
	 */
	public static final SimpleValue.StringContentOnlyFactory<DayTimeDurationValue> DAYTIMEDURATION = new SimpleValue.StringContentOnlyFactory<DayTimeDurationValue>(StandardDatatypes.DAYTIMEDURATION)
	{

		@Override
		public DayTimeDurationValue parse(final String val)
		{
			return new DayTimeDurationValue(val);
		}

	};

	/**
	 * yearMonthDuration
	 */
	public static final SimpleValue.StringContentOnlyFactory<YearMonthDurationValue> YEARMONTHDURATION = new SimpleValue.StringContentOnlyFactory<YearMonthDurationValue>(
			StandardDatatypes.YEARMONTHDURATION)
	{

		@Override
		public YearMonthDurationValue parse(final String val)
		{
			return new YearMonthDurationValue(val);
		}

	};

	/**
	 * xpathExpression
	 */
	public static final SimpleValue.BaseFactory<XPathValue> XPATH = new SimpleValue.BaseFactory<XPathValue>(StandardDatatypes.XPATH)
	{

		@Override
		public XPathValue getInstance(final Serializable value, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			if (!(value instanceof String))
			{
				throw new IllegalArgumentException("Invalid primitive AttributeValueType: content contains instance of " + value.getClass().getName() + ". Expected: " + String.class);
			}

			return new XPathValue((String) value, otherXmlAttributes, xPathCompiler);
		}

	};

	private static final PdpExtensionComparator<AttributeValueFactory<?>> DATATYPE_EXTENSION_COMPARATOR = new PdpExtensionComparator<>();

	/**
	 * Set of attribute value factories for standard mandatory datatypes (xpathExpression is optional, therefore excluded)
	 */
	public static final Set<SimpleValue.StringParseableValueFactory<? extends SimpleValue<? extends Object>>> MANDATORY_SET_EXCEPT_INTEGER = HashCollections.newImmutableSet(Arrays.asList(STRING,
			BOOLEAN, DOUBLE, TIME, DATE, DATETIME, ANYURI, HEXBINARY, BASE64BINARY, X500NAME, RFC822NAME, IPADDRESS, DNSNAME, DAYTIMEDURATION, YEARMONTHDURATION));

	// private static BigInteger BYTE_MAX_AS_BIG_INT = BigInteger.valueOf(Byte.valueOf(Byte.MAX_VALUE).longValue());
	// private static BigInteger SHORT_MAX_AS_BIG_INT = BigInteger.valueOf(Short.valueOf(Short.MAX_VALUE).longValue());
	private static BigInteger INT_MAX_AS_BIG_INT = BigInteger.valueOf(Integer.valueOf(Integer.MAX_VALUE).longValue());

	private static BigInteger LONG_MAX_AS_BIG_INT = BigInteger.valueOf(Long.valueOf(Long.MAX_VALUE).longValue());

	/**
	 * Get standard registry of (datatype-specific) attribute value parsers/factories
	 *
	 * @param enableXPath
	 *            true iff XPath-based function(s) support enabled
	 * @param maxIntegerValue
	 *            Maximum integer value. This is the expected maximum value for XACML attributes of standard type 'http://www.w3.org/2001/XMLSchema#integer'. Decreasing this value as much as possible
	 *            helps the PDP engine optimize the processing of integer values (lower memory consumption, faster computations). In particular, the Java class used to represent an integer value is:
	 *            <ul>
	 *            <li>{@code Byte}</li>
	 *            </ul>
	 * @return standard registry of attribute value factories
	 */
	public static AttributeValueFactoryRegistry getRegistry(final boolean enableXPath, final Optional<BigInteger> maxIntegerValue)
	{
		final Set<SimpleValue.BaseFactory<?>> attValFactories;
		if (enableXPath)
		{
			attValFactories = HashCollections.newUpdatableSet(StandardDatatypes.MANDATORY_SET.size() + 1);
			attValFactories.add(StandardAttributeValueFactories.XPATH);
		}
		else
		{
			attValFactories = HashCollections.newUpdatableSet(StandardDatatypes.MANDATORY_SET.size());
		}

		final SimpleValue.BaseFactory<?> integerValFactory;
		if (!maxIntegerValue.isPresent())
		{
			integerValFactory = BIG_INTEGER;
		}
		else
		{

			final BigInteger nonNullMaxInt = maxIntegerValue.get();
			// else if(maxIntegerValue.compareTo(BYTE_MAX_AS_BIG_INT) == -1) {
			// integerValFactory = BYTE_INTEGER;
			// }
			// else if(maxIntegerValue.compareTo(SHORT_MAX_AS_BIG_INT) == -1) {
			// integerValFactory = SHORT_INTEGER;
			// }
			if (nonNullMaxInt.compareTo(INT_MAX_AS_BIG_INT) == -1)
			{
				integerValFactory = MEDIUM_INTEGER;
			}
			else if (nonNullMaxInt.compareTo(LONG_MAX_AS_BIG_INT) == -1)
			{
				integerValFactory = LONG_INTEGER;
			}
			else
			{
				integerValFactory = BIG_INTEGER;
			}
		}

		attValFactories.add(integerValFactory);

		for (final SimpleValue.StringParseableValueFactory<? extends SimpleValue<? extends Object>> typeFactory : MANDATORY_SET_EXCEPT_INTEGER)
		{
			attValFactories.add(typeFactory);
		}

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Supported XACML standard datatypes: {}", attValFactories.stream().sorted(DATATYPE_EXTENSION_COMPARATOR).map(AttributeValueFactory::getDatatype).collect(Collectors.toSet()));
		}

		return new ImmutableAttributeValueFactoryRegistry(attValFactories);
	}

	private StandardAttributeValueFactories()
	{
		// private empty constructor
	}

}
