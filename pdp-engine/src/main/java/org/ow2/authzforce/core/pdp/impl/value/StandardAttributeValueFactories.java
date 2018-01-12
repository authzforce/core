/**
 * Copyright 2012-2018 Thales Services SAS.
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
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.ArbitrarilyBigInteger;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.Bags;
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
import org.ow2.authzforce.core.pdp.api.value.SimpleValue.StringParseableValueFactory;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;
import org.ow2.authzforce.core.pdp.api.value.YearMonthDurationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.s9api.XPathCompiler;

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
	public static final SimpleValue.StringContentOnlyFactory<StringValue> STRING = new SimpleValue.StringContentOnlyFactory<StringValue>(
			StandardDatatypes.STRING)
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
	public static final SimpleValue.StringParseableValueFactory<BooleanValue> BOOLEAN = new SimpleValue.StringParseableValueFactory<BooleanValue>(
			StandardDatatypes.BOOLEAN)
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

			throw new IllegalArgumentException("Invalid input type to Boolean AttributeValue factory: "
					+ value.getClass().getName() + ". Expected one of: " + Boolean.class + "," + String.class);
		}

	};

	private static abstract class IntegerValueFactory extends SimpleValue.StringParseableValueFactory<IntegerValue>
	{

		private IntegerValueFactory()
		{
			super(StandardDatatypes.INTEGER);
		}

	}

	private static final String MEDIUM_INT_FACTORY_INPUT_TYPE_ERR_MSG_SUFFIX = ". Expected one of: " + Short.class + ","
			+ Integer.class + "," + BigInteger.class + "," + String.class;

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
			if (value instanceof Short)
			{
				return IntegerValue.valueOf(((Short) value).intValue());
			}

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

			throw new IllegalArgumentException("Invalid input type to Integer AttributeValue factory: "
					+ value.getClass().getName() + MEDIUM_INT_FACTORY_INPUT_TYPE_ERR_MSG_SUFFIX);
		}

	};

	private static final String LONG_INT_FACTORY_INPUT_TYPE_ERR_MSG_SUFFIX = ". Expected one of: " + Short.class + ","
			+ Integer.class + "," + Long.class + "," + BigInteger.class + "," + String.class;

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
			if (value instanceof Short)
			{
				return IntegerValue.valueOf(((Short) value).intValue());
			}

			if (value instanceof Integer)
			{
				return IntegerValue.valueOf(((Integer) value).intValue());
			}

			if (value instanceof Long)
			{
				return IntegerValue.valueOf(((Long) value).longValue());
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

			throw new IllegalArgumentException("Invalid input type to Integer AttributeValue factory: "
					+ value.getClass().getName() + LONG_INT_FACTORY_INPUT_TYPE_ERR_MSG_SUFFIX);
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
			if (value instanceof Short)
			{
				return IntegerValue.valueOf(((Short) value).intValue());
			}

			if (value instanceof Integer)
			{
				return IntegerValue.valueOf(((Integer) value).intValue());
			}

			if (value instanceof Long)
			{
				return IntegerValue.valueOf(((Long) value).longValue());
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

			throw new IllegalArgumentException("Invalid input type to Integer AttributeValue factory: "
					+ value.getClass().getName() + LONG_INT_FACTORY_INPUT_TYPE_ERR_MSG_SUFFIX);
		}

	};

	/**
	 * double
	 */
	public static final SimpleValue.StringParseableValueFactory<DoubleValue> DOUBLE = new SimpleValue.StringParseableValueFactory<DoubleValue>(
			StandardDatatypes.DOUBLE)
	{

		@Override
		public DoubleValue parse(final String val)
		{
			return new DoubleValue(val);
		}

		@Override
		public DoubleValue getInstance(final Serializable value)
		{
			if (value instanceof Float)
			{
				return new DoubleValue(Double.valueOf(((Float) value).doubleValue()));
			}

			if (value instanceof Double)
			{
				return new DoubleValue((Double) value);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw new IllegalArgumentException("Invalid input type to Double AttributeValue factory: "
					+ value.getClass().getName() + ". Expected one of: " + Double.class + "," + String.class);
		}

	};

	/**
	 * time
	 */
	public static final SimpleValue.StringContentOnlyFactory<TimeValue> TIME = new SimpleValue.StringContentOnlyFactory<TimeValue>(
			StandardDatatypes.TIME)
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
	public static final SimpleValue.StringContentOnlyFactory<DateValue> DATE = new SimpleValue.StringContentOnlyFactory<DateValue>(
			StandardDatatypes.DATE)
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
	public static final SimpleValue.StringContentOnlyFactory<DateTimeValue> DATETIME = new SimpleValue.StringContentOnlyFactory<DateTimeValue>(
			StandardDatatypes.DATETIME)
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
	public static final SimpleValue.StringContentOnlyFactory<AnyUriValue> ANYURI = new SimpleValue.StringContentOnlyFactory<AnyUriValue>(
			StandardDatatypes.ANYURI)
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
	public static final SimpleValue.StringContentOnlyFactory<HexBinaryValue> HEXBINARY = new SimpleValue.StringContentOnlyFactory<HexBinaryValue>(
			StandardDatatypes.HEXBINARY)
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
	public static final SimpleValue.StringContentOnlyFactory<Base64BinaryValue> BASE64BINARY = new SimpleValue.StringContentOnlyFactory<Base64BinaryValue>(
			StandardDatatypes.BASE64BINARY)
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
	public static final SimpleValue.StringContentOnlyFactory<X500NameValue> X500NAME = new SimpleValue.StringContentOnlyFactory<X500NameValue>(
			StandardDatatypes.X500NAME)
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
	public static final SimpleValue.StringContentOnlyFactory<Rfc822NameValue> RFC822NAME = new SimpleValue.StringContentOnlyFactory<Rfc822NameValue>(
			StandardDatatypes.RFC822NAME)
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
	public static final SimpleValue.StringContentOnlyFactory<IpAddressValue> IPADDRESS = new SimpleValue.StringContentOnlyFactory<IpAddressValue>(
			StandardDatatypes.IPADDRESS)
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
	public static final SimpleValue.StringContentOnlyFactory<DnsNameWithPortRangeValue> DNSNAME = new SimpleValue.StringContentOnlyFactory<DnsNameWithPortRangeValue>(
			StandardDatatypes.DNSNAME)
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
	public static final SimpleValue.StringContentOnlyFactory<DayTimeDurationValue> DAYTIMEDURATION = new SimpleValue.StringContentOnlyFactory<DayTimeDurationValue>(
			StandardDatatypes.DAYTIMEDURATION)
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
	public static final SimpleValue.BaseFactory<XPathValue> XPATH = new SimpleValue.BaseFactory<XPathValue>(
			StandardDatatypes.XPATH)
	{

		@Override
		public XPathValue getInstance(final Serializable value, final Map<QName, String> otherXmlAttributes,
				final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			if (!(value instanceof String))
			{
				throw new IllegalArgumentException("Invalid primitive AttributeValueType: content contains instance of "
						+ value.getClass().getName() + ". Expected: " + String.class);
			}

			return new XPathValue((String) value, otherXmlAttributes, xPathCompiler);
		}

	};

	private static final PdpExtensionComparator<AttributeValueFactory<?>> DATATYPE_EXTENSION_COMPARATOR = new PdpExtensionComparator<>();

	/**
	 * Set of attribute value factories for standard mandatory datatypes (xpathExpression is optional, therefore
	 * excluded)
	 */
	public static final Set<SimpleValue.StringParseableValueFactory<? extends SimpleValue<? extends Object>>> MANDATORY_SET_EXCEPT_INTEGER = HashCollections
			.newImmutableSet(Arrays.asList(STRING, BOOLEAN, DOUBLE, TIME, DATE, DATETIME, ANYURI, HEXBINARY,
					BASE64BINARY, X500NAME, RFC822NAME, IPADDRESS, DNSNAME, DAYTIMEDURATION, YEARMONTHDURATION));

	// private static BigInteger BYTE_MAX_AS_BIG_INT = BigInteger.valueOf(Byte.valueOf(Byte.MAX_VALUE).longValue());
	// private static BigInteger SHORT_MAX_AS_BIG_INT = BigInteger.valueOf(Short.valueOf(Short.MAX_VALUE).longValue());
	private static final BigInteger INT_MAX_AS_BIG_INT = BigInteger
			.valueOf(Integer.valueOf(Integer.MAX_VALUE).longValue());

	private static final BigInteger LONG_MAX_AS_BIG_INT = BigInteger.valueOf(Long.valueOf(Long.MAX_VALUE).longValue());

	/**
	 * Get standard registry of (datatype-specific) attribute value parsers/factories
	 *
	 * @param enableXPath
	 *            true iff XPath-based function(s) support enabled
	 * @param maxIntegerValue
	 *            Maximum integer value. This is the expected maximum value for XACML attributes of standard type
	 *            'http://www.w3.org/2001/XMLSchema#integer'. Decreasing this value as much as possible helps the PDP
	 *            engine optimize the processing of integer values (lower memory consumption, faster computations). In
	 *            particular, the Java class used to represent an integer value is:
	 *            <ul>
	 *            <li>{@code Byte}</li>
	 *            </ul>
	 * @return standard registry of attribute value factories
	 */
	public static AttributeValueFactoryRegistry getRegistry(final boolean enableXPath,
			final Optional<BigInteger> maxIntegerValue)
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
			LOGGER.debug("Supported XACML standard datatypes: {}",
					attValFactories.stream().sorted(DATATYPE_EXTENSION_COMPARATOR)
							.map(AttributeValueFactory::getDatatype).collect(Collectors.toSet()));
		}

		return new ImmutableAttributeValueFactoryRegistry(attValFactories);
	}

	private StandardAttributeValueFactories()
	{
		// private empty constructor
	}

	/*
	 * FIXME: move this to Bags class?
	 */
	private static final Map<Class<?>, StringParseableValueFactory<?>> JAVA_TYPE_TO_ATT_VALUE_FACTORY;
	private static final Set<Entry<Class<?>, StringParseableValueFactory<?>>> NON_FINAL_JAVA_TYPE_TO_ATT_VALUE_FACTORY;
	static
	{
		final Map<Class<?>, StringParseableValueFactory<?>> mutableMap = HashCollections.newUpdatableMap(14);
		mutableMap.put(String.class, STRING);
		mutableMap.put(Boolean.class, BOOLEAN);
		mutableMap.put(Short.class, MEDIUM_INTEGER);
		mutableMap.put(Integer.class, MEDIUM_INTEGER);
		mutableMap.put(Long.class, LONG_INTEGER);
		mutableMap.put(BigInteger.class, BIG_INTEGER); // non final
		mutableMap.put(Float.class, DOUBLE);
		mutableMap.put(Double.class, DOUBLE);
		mutableMap.put(LocalTime.class, TIME);
		mutableMap.put(OffsetTime.class, TIME);
		mutableMap.put(LocalDate.class, DATE);
		mutableMap.put(GregorianCalendar.class, DATETIME);// nonfinal
		mutableMap.put(LocalDateTime.class, DATETIME);
		mutableMap.put(OffsetDateTime.class, DATETIME);
		mutableMap.put(ZonedDateTime.class, DATETIME);
		mutableMap.put(Date.class, DATETIME); // non final, subclass java.sql.Date, etc.
		mutableMap.put(URI.class, ANYURI);
		mutableMap.put(byte[].class, HEXBINARY);
		mutableMap.put(X500Principal.class, X500NAME);

		JAVA_TYPE_TO_ATT_VALUE_FACTORY = HashCollections.newImmutableMap(mutableMap);

		/*
		 * Using JAVA_TYPE_TO_ATT_VALUE_FACTORY.get(instanceClass) to get the corresponding factory is faster that doing
		 * many instanceOf checks but only works for equal match. For non final classes, we still have to do the
		 * instanceOf check because the instance class might not be equal, i.e same class, but a subclass. So we gather
		 * the list of non-final classes for which instanceOf check is necessary iff no equal match.
		 */
		final Set<Entry<Class<?>, StringParseableValueFactory<?>>> mutableSet = JAVA_TYPE_TO_ATT_VALUE_FACTORY
				.entrySet().stream().filter(e -> !Modifier.isFinal(e.getKey().getModifiers()))
				.collect(Collectors.toSet());// HashCollections.newUpdatableSet(JAVA_TYPE_TO_ATT_VALUE_FACTORY.size());
		NON_FINAL_JAVA_TYPE_TO_ATT_VALUE_FACTORY = ImmutableSet.copyOf(mutableSet);
	}

	private static final StringParseableValueFactory<?> getAttributeValueFactory(final Serializable rawValue)
	{
		final StringParseableValueFactory<? extends AttributeValue> attValFactoryFromMap = JAVA_TYPE_TO_ATT_VALUE_FACTORY
				.get(rawValue.getClass());
		if (attValFactoryFromMap == null)
		{
			/*
			 * This may look like the collection is fully filtered before findfirst() is called but it is not the case.
			 * "All intermediate operations e.g. filter(), map() etc are lazy and they are only executed when a terminal
			 * operation like findFirst() or forEach() is called.
			 * 
			 * This also means, a lot of opportunity for optimization depending upon the size of the original list."
			 * (Quote from: http://javarevisited.blogspot.fr/2016/03/how-to-find-first-element-of-stream-in.html)
			 */
			final Optional<Entry<Class<?>, StringParseableValueFactory<?>>> optionalResult = NON_FINAL_JAVA_TYPE_TO_ATT_VALUE_FACTORY
					.stream().filter(e -> e.getKey().isInstance(rawValue)).findFirst();
			if (optionalResult.isPresent())
			{
				return optionalResult.get().getValue();
			}

			throw new UnsupportedOperationException("Unsupported input value type: '" + rawValue.getClass()
					+ "' (no suitable XACML datatype factory found)");
		}

		return attValFactoryFromMap;
	}

	public static AttributeValue newAttributeValue(final Serializable rawValue)
			throws IllegalArgumentException, UnsupportedOperationException
	{
		Preconditions.checkArgument(rawValue != null, "Null arg");
		final StringParseableValueFactory<?> factory = getAttributeValueFactory(rawValue);
		if (factory == null)
		{
			throw new UnsupportedOperationException("Unsupported input value type: '" + rawValue.getClass()
					+ "' (no suitable XACML datatype factory found)");
		}
		return factory.getInstance(rawValue);
	}

	/**
	 * TODO: document default mappings
	 * 
	 * @param rawVals
	 * @return
	 * @throws UnsupportedOperationException
	 *             if no suitable XACML datatype factory found for input raw value type
	 * @throws IllegalArgumentException
	 *             if {@code rawVals == null || rawVals.isEmpty()}
	 * 
	 */
	public static AttributeBag<?> newAttributeBag(final Collection<Serializable> rawVals)
			throws UnsupportedOperationException, IllegalArgumentException
	{
		Preconditions.checkArgument(rawVals != null && !rawVals.isEmpty(), "Null/empty arg");
		final Serializable rawVal0 = rawVals.iterator().next();
		final StringParseableValueFactory<?> factory = getAttributeValueFactory(rawVal0);
		if (factory == null)
		{
			throw new UnsupportedOperationException("Unsupported input value type: '" + rawVal0.getClass()
					+ "' (no suitable XACML datatype factory found)");
		}
		return Bags.newAttributeBag(factory, rawVals);
	}

	public static void main(final String... strings)
	{
		final Serializable javaValue = java.sql.Date.valueOf(LocalDate.now());
	}
}
