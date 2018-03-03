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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
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

import com.google.common.collect.ImmutableList;

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
	public static final SimpleValue.StringContentOnlyFactory<StringValue> STRING = new SimpleValue.StringContentOnlyFactory<StringValue>(StandardDatatypes.STRING)
	{

		@Override
		public StringValue parse(final String val) {
			return StringValue.parse(val);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_BOOLEAN_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(Boolean.class, String.class));

	/**
	 * boolean
	 */
	public static final SimpleValue.StringParseableValueFactory<BooleanValue> BOOLEAN = new SimpleValue.StringParseableValueFactory<BooleanValue>(StandardDatatypes.BOOLEAN)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_BOOLEAN_FACTORY_INPUT_TYPES;
		}

		@Override
		public BooleanValue parse(final String val) {
			return BooleanValue.getInstance(val);
		}

		@Override
		public BooleanValue getInstance(final Serializable value) {
			if (value instanceof Boolean)
			{
				return new BooleanValue((Boolean) value);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_INTEGER_FACTORY_INPUT_TYPES = HashCollections
			.newImmutableSet(Arrays.asList(Short.class, Integer.class, Long.class, BigInteger.class, String.class));

	private static abstract class IntegerValueFactory extends SimpleValue.StringParseableValueFactory<IntegerValue>
	{

		private IntegerValueFactory()
		{
			super(StandardDatatypes.INTEGER);
		}

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_INTEGER_FACTORY_INPUT_TYPES;
		}

	}

	/**
	 * integer parsed into {@link Integer}, therefore supports medium-size integers (representing xsd:int)
	 */
	public static final SimpleValue.StringParseableValueFactory<IntegerValue> MEDIUM_INTEGER = new IntegerValueFactory()
	{

		@Override
		public IntegerValue parse(final String val) throws IllegalArgumentException {
			final int i;
			try
			{
				i = DatatypeConverter.parseInt(val);
			} catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException(this + ": input value not valid or too big for Java int: " + val);
			}

			return IntegerValue.valueOf(i);
		}

		@Override
		public IntegerValue getInstance(final Serializable value) throws IllegalArgumentException {
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
				final Long l = (Long) value;
				final int i;
				try
				{
					i = Math.toIntExact(l.longValue());
				} catch (final ArithmeticException e)
				{
					throw new IllegalArgumentException(this + ": input value not supported (too big for Java int): " + value);
				}

				return IntegerValue.valueOf(i);
			}

			if (value instanceof BigInteger)
			{
				final BigInteger bigInt = (BigInteger) value;
				final int i;
				try
				{
					i = bigInt.intValueExact();
				} catch (final ArithmeticException e)
				{
					throw new IllegalArgumentException(this + ": input value not supported (too big for Java int): " + value);
				}

				return IntegerValue.valueOf(i);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	private static IntegerValue longOrSmallerIntToIntegerValue(final Serializable value) {
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

		return null;
	}

	/**
	 * integer parsed into {@link Long}, therefore supports long integers (representing xsd:long)
	 */
	public static final SimpleValue.StringParseableValueFactory<IntegerValue> LONG_INTEGER = new IntegerValueFactory()
	{

		@Override
		public IntegerValue parse(final String val) throws IllegalArgumentException {
			final long i;
			try
			{
				i = DatatypeConverter.parseLong(val);
			} catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException(this + ": input value not valid or too big for Java long: " + val);
			}

			return IntegerValue.valueOf(i);
		}

		@Override
		public IntegerValue getInstance(final Serializable value) throws IllegalArgumentException {
			final IntegerValue smallIntegerValue = longOrSmallerIntToIntegerValue(value);
			if (smallIntegerValue != null)
			{
				return smallIntegerValue;
			}

			if (value instanceof BigInteger)
			{
				final BigInteger bigInt = (BigInteger) value;
				final long i;
				try
				{
					i = bigInt.longValueExact();
				} catch (final ArithmeticException e)
				{
					throw new IllegalArgumentException(this + ": input value not supported (too big for Java long): " + bigInt);
				}

				return IntegerValue.valueOf(i);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	/**
	 * integer parsed into {@link BigInteger}, therefore supports arbitrary-precision integers (i.e. any xsd:integer)
	 */
	public static final SimpleValue.StringParseableValueFactory<IntegerValue> BIG_INTEGER = new IntegerValueFactory()
	{

		private IntegerValue getInstance(final BigInteger bigi) {
			final long i;
			try
			{
				i = bigi.longValueExact();
				return IntegerValue.valueOf(i);
			} catch (final ArithmeticException e)
			{
				LOGGER.debug("Input integer too big to fit in a long: {}", bigi);
			}

			return new IntegerValue(new ArbitrarilyBigInteger(bigi));
		}

		@Override
		public IntegerValue parse(final String val) throws IllegalArgumentException {
			final BigInteger bigInt;
			try
			{
				bigInt = DatatypeConverter.parseInteger(val);
			} catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException(this + ": input value not valid: " + val);
			}

			return getInstance(bigInt);
		}

		@Override
		public IntegerValue getInstance(final Serializable value) throws IllegalArgumentException {
			final IntegerValue smallIntegerValue = longOrSmallerIntToIntegerValue(value);
			if (smallIntegerValue != null)
			{
				return smallIntegerValue;
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

			throw newInvalidInputTypeException(value);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_DOUBLE_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(Float.class, Double.class, String.class));

	/**
	 * double
	 */
	public static final SimpleValue.StringParseableValueFactory<DoubleValue> DOUBLE = new SimpleValue.StringParseableValueFactory<DoubleValue>(StandardDatatypes.DOUBLE)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_DOUBLE_FACTORY_INPUT_TYPES;
		}

		@Override
		public DoubleValue parse(final String val) {
			return new DoubleValue(val);
		}

		@Override
		public DoubleValue getInstance(final Serializable value) {
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

			throw newInvalidInputTypeException(value);
		}

	};

	private static TimeValue newUtcTimeValue(final int hours, final int minutes, final int seconds, final int nanoOfSec) {
		return TimeValue.getInstance(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendarTime(hours, minutes, seconds, BigDecimal.valueOf(nanoOfSec).movePointLeft(9), 0));
	}

	private static final Set<Class<? extends Serializable>> SUPPORTED_TIME_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(LocalTime.class, OffsetTime.class, String.class));

	/**
	 * time
	 */
	public static final SimpleValue.StringParseableValueFactory<TimeValue> TIME = new SimpleValue.StringParseableValueFactory<TimeValue>(StandardDatatypes.TIME)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_TIME_FACTORY_INPUT_TYPES;
		}

		@Override
		public TimeValue parse(final String val) {
			return new TimeValue(val);
		}

		@Override
		public TimeValue getInstance(final Serializable value) throws IllegalArgumentException {
			if (value instanceof LocalTime)
			{
				final LocalTime time = (LocalTime) value;
				// We set time in UTC, so timezone offset is 0
				return newUtcTimeValue(time.getHour(), time.getMinute(), time.getSecond(), time.getNano());
			}

			if (value instanceof OffsetTime)
			{
				final OffsetTime time = (OffsetTime) value;
				// We set time in UTC, so timezone offset is 0
				return newUtcTimeValue(time.getHour(), time.getMinute(), time.getSecond(), time.getNano());
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_DATE_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(LocalDate.class, String.class));

	/**
	 * date
	 */
	public static final SimpleValue.StringParseableValueFactory<DateValue> DATE = new SimpleValue.StringParseableValueFactory<DateValue>(StandardDatatypes.DATE)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_DATE_FACTORY_INPUT_TYPES;
		}

		@Override
		public DateValue parse(final String val) {
			return new DateValue(val);
		}

		@Override
		public DateValue getInstance(final Serializable value) throws IllegalArgumentException {
			if (value instanceof LocalDate)
			{
				final LocalDate date = (LocalDate) value;
				// We set date in UTC, so timezone offset is 0
				return DateValue.getInstance(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendarDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0));
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	private static DateTimeValue newDateTimeValue(final int year, final int month, final int dayOfMonth, final int hours, final int minutes, final int seconds, final int nanoOfSec,
			final int timezoneOffsetMinutes) {
		return new DateTimeValue(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(BigInteger.valueOf(year), month, dayOfMonth, hours, minutes, seconds,
				BigDecimal.valueOf(nanoOfSec).movePointLeft(9), timezoneOffsetMinutes));
	}

	private static final Set<Class<? extends Serializable>> SUPPORTED_DATETIME_FACTORY_INPUT_TYPES = HashCollections
			.newImmutableSet(Arrays.asList(LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, Instant.class, String.class));

	/**
	 * dateTime
	 */
	public static final SimpleValue.StringParseableValueFactory<DateTimeValue> DATETIME = new SimpleValue.StringParseableValueFactory<DateTimeValue>(StandardDatatypes.DATETIME)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_DATETIME_FACTORY_INPUT_TYPES;
		}

		@Override
		public DateTimeValue parse(final String val) {
			return new DateTimeValue(val);
		}

		@Override
		public DateTimeValue getInstance(final Serializable value) throws IllegalArgumentException {
			if (value instanceof LocalDateTime)
			{
				final LocalDateTime dateTime = (LocalDateTime) value;
				// We set time in UTC, so timezone offset is 0
				return newDateTimeValue(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano(), 0);
			}

			if (value instanceof OffsetDateTime)
			{
				final OffsetDateTime dateTime = (OffsetDateTime) value;
				return newDateTimeValue(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano(),
						dateTime.getOffset().getTotalSeconds() / 60);
			}

			if (value instanceof ZonedDateTime)
			{
				final ZonedDateTime dateTime = (ZonedDateTime) value;
				return newDateTimeValue(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano(),
						dateTime.getOffset().getTotalSeconds() / 60);
			}

			if (value instanceof Instant)
			{
				final Instant instant = (Instant) value;
				// We set time in UTC
				final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
				return newDateTimeValue(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano(), 0);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_ANYURI_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(URI.class, String.class));

	/**
	 * anyURI
	 */
	public static final SimpleValue.StringParseableValueFactory<AnyUriValue> ANYURI = new SimpleValue.StringParseableValueFactory<AnyUriValue>(StandardDatatypes.ANYURI)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_ANYURI_FACTORY_INPUT_TYPES;
		}

		@Override
		public AnyUriValue parse(final String val) {
			return new AnyUriValue(val);
		}

		@Override
		public AnyUriValue getInstance(final Serializable value) throws IllegalArgumentException {
			if (value instanceof URI)
			{
				final URI uri = (URI) value;
				return new AnyUriValue(uri.toString());
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_HEXBINARY_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(byte[].class, String.class));

	/**
	 * hexBinary
	 */
	public static final SimpleValue.StringParseableValueFactory<HexBinaryValue> HEXBINARY = new SimpleValue.StringParseableValueFactory<HexBinaryValue>(StandardDatatypes.HEXBINARY)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_HEXBINARY_FACTORY_INPUT_TYPES;
		}

		@Override
		public HexBinaryValue parse(final String val) {
			return new HexBinaryValue(val);
		}

		@Override
		public HexBinaryValue getInstance(final Serializable value) throws IllegalArgumentException {
			if (value instanceof byte[])
			{
				final byte[] bytes = (byte[]) value;
				return new HexBinaryValue(bytes);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}

	};

	/**
	 * base64Binary
	 */
	public static final SimpleValue.StringParseableValueFactory<Base64BinaryValue> BASE64BINARY = new SimpleValue.StringContentOnlyFactory<Base64BinaryValue>(StandardDatatypes.BASE64BINARY)
	{
		@Override
		public Base64BinaryValue parse(final String val) {
			return new Base64BinaryValue(val);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_X500NAME_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(Arrays.asList(X500Principal.class, String.class));

	/**
	 * x500Name
	 */
	public static final SimpleValue.StringParseableValueFactory<X500NameValue> X500NAME = new SimpleValue.StringParseableValueFactory<X500NameValue>(StandardDatatypes.X500NAME)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_X500NAME_FACTORY_INPUT_TYPES;
		}

		@Override
		public X500NameValue parse(final String val) {
			return new X500NameValue(val);
		}

		@Override
		public X500NameValue getInstance(final Serializable value) throws IllegalArgumentException {
			if (value instanceof X500Principal)
			{
				final X500Principal principal = (X500Principal) value;
				return new X500NameValue(principal);
			}

			if (value instanceof String)
			{
				return parse((String) value);
			}

			throw newInvalidInputTypeException(value);
		}
	};

	/**
	 * rfc822Name
	 */
	public static final SimpleValue.StringContentOnlyFactory<Rfc822NameValue> RFC822NAME = new SimpleValue.StringContentOnlyFactory<Rfc822NameValue>(StandardDatatypes.RFC822NAME)
	{

		@Override
		public Rfc822NameValue parse(final String val) {
			return new Rfc822NameValue(val);
		}
	};

	/**
	 * ipAddress
	 */
	public static final SimpleValue.StringContentOnlyFactory<IpAddressValue> IPADDRESS = new SimpleValue.StringContentOnlyFactory<IpAddressValue>(StandardDatatypes.IPADDRESS)
	{
		@Override
		public IpAddressValue parse(final String value) {
			return new IpAddressValue(value);
		}

	};

	/**
	 * dnsName
	 */
	public static final SimpleValue.StringContentOnlyFactory<DnsNameWithPortRangeValue> DNSNAME = new SimpleValue.StringContentOnlyFactory<DnsNameWithPortRangeValue>(StandardDatatypes.DNSNAME)
	{

		@Override
		public DnsNameWithPortRangeValue parse(final String value) {
			return new DnsNameWithPortRangeValue(value);
		}

	};

	/**
	 * dayTimeDuration
	 */
	public static final SimpleValue.StringContentOnlyFactory<DayTimeDurationValue> DAYTIMEDURATION = new SimpleValue.StringContentOnlyFactory<DayTimeDurationValue>(StandardDatatypes.DAYTIMEDURATION)
	{

		@Override
		public DayTimeDurationValue parse(final String val) {
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
		public YearMonthDurationValue parse(final String val) {
			return new YearMonthDurationValue(val);
		}

	};

	private static final Set<Class<? extends Serializable>> SUPPORTED_XPATH_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(String.class);

	/**
	 * xpathExpression
	 */
	public static final SimpleValue.BaseFactory<XPathValue> XPATH = new SimpleValue.BaseFactory<XPathValue>(StandardDatatypes.XPATH)
	{

		@Override
		public Set<Class<? extends Serializable>> getSupportedInputTypes() {
			return SUPPORTED_XPATH_FACTORY_INPUT_TYPES;
		}

		@Override
		public XPathValue getInstance(final Serializable value, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException {
			if (!(value instanceof String))
			{
				throw new IllegalArgumentException("Invalid primitive AttributeValueType: content contains instance of " + value.getClass().getName() + ". Expected: " + String.class);
			}

			return new XPathValue((String) value, otherXmlAttributes, xPathCompiler);
		}
	};

	private static final PdpExtensionComparator<AttributeValueFactory<?>> DATATYPE_EXTENSION_COMPARATOR = new PdpExtensionComparator<>();

	/**
	 * List of attribute value factories for standard mandatory datatypes (xpathExpression is optional, therefore excluded), ordered by preference; i.e. if two factories support a common input type,
	 * the first one in the list is always used, when no specific output XACML datatype is requested. For example, all factories support String type, but STRING factory is first in the list, so this
	 * is the one that is used for creating attribute values from String when no specific output XACML datatype (different from string) is requested.
	 */
	public static final List<SimpleValue.StringParseableValueFactory<? extends SimpleValue<? extends Object>>> MANDATORY_SET_EXCEPT_INTEGER = ImmutableList.of(STRING, BOOLEAN, DOUBLE, TIME, DATE,
			DATETIME, ANYURI, HEXBINARY, BASE64BINARY, X500NAME, RFC822NAME, IPADDRESS, DNSNAME, DAYTIMEDURATION, YEARMONTHDURATION);

	// private static BigInteger BYTE_MAX_AS_BIG_INT = BigInteger.valueOf(Byte.valueOf(Byte.MAX_VALUE).longValue());
	// private static BigInteger SHORT_MAX_AS_BIG_INT = BigInteger.valueOf(Short.valueOf(Short.MAX_VALUE).longValue());
	private static final BigInteger INT_MAX_AS_BIG_INT = BigInteger.valueOf(Integer.valueOf(Integer.MAX_VALUE).longValue());

	private static final BigInteger LONG_MAX_AS_BIG_INT = BigInteger.valueOf(Long.MAX_VALUE);

	/**
	 * Get standard registry of (datatype-specific) attribute value parsers/factories
	 *
	 * @param enableXPath
	 *            true iff XPath-based function(s) support enabled
	 * @param maxIntegerValue
	 *            Maximum integer value. This is the expected maximum value for XACML attributes of standard type 'http://www.w3.org/2001/XMLSchema#integer'. Decreasing this value as much as possible
	 *            helps the PDP engine optimize the processing of integer values (lower memory consumption, faster computations). By default, the Java class used to represent an integer value is:
	 *            <ul>
	 *            <li>{@link Integer}</li>
	 *            </ul>
	 * @return standard registry of attribute value factories
	 */
	public static AttributeValueFactoryRegistry getRegistry(final boolean enableXPath, final Optional<BigInteger> maxIntegerValue) {
		/*
		 * Ordered in which factories are added to the registry must be preserved to indicate order of preference; i.e. if two factories support a common input type, the first one in the list is
		 * always used by default, unless a specific output XACML datatype is requested. For example, all factories support String type, but STRING factory must be first in the list, so that it is the
		 * one that is used for creating attribute values from String by default when no specific output XACML datatype (different from string) is requested.
		 */
		/*
		 * There is one more factory for XPathExpression if XPath support is requested
		 */
		final List<SimpleValue.BaseFactory<?>> attValFactories = new ArrayList<>(StandardDatatypes.MANDATORY_SET.size() + (enableXPath ? 1 : 0));
		for (final SimpleValue.StringParseableValueFactory<? extends SimpleValue<? extends Object>> typeFactory : MANDATORY_SET_EXCEPT_INTEGER)
		{
			attValFactories.add(typeFactory);
		}

		final SimpleValue.BaseFactory<?> integerValFactory;
		if (!maxIntegerValue.isPresent())
		{
			integerValFactory = MEDIUM_INTEGER;
		} else
		{

			final BigInteger nonNullMaxInt = maxIntegerValue.get();
			// else if(maxIntegerValue.compareTo(BYTE_MAX_AS_BIG_INT) == -1) {
			// integerValFactory = BYTE_INTEGER;
			// }
			// else if(maxIntegerValue.compareTo(SHORT_MAX_AS_BIG_INT) == -1) {
			// integerValFactory = SHORT_INTEGER;
			// }
			if (nonNullMaxInt.compareTo(INT_MAX_AS_BIG_INT) <= 0)
			{
				integerValFactory = MEDIUM_INTEGER;
			} else if (nonNullMaxInt.compareTo(LONG_MAX_AS_BIG_INT) <= 0)
			{
				integerValFactory = LONG_INTEGER;
			} else
			{
				integerValFactory = BIG_INTEGER;
			}
		}

		attValFactories.add(integerValFactory);
		if (enableXPath)
		{

			attValFactories.add(StandardAttributeValueFactories.XPATH);
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
