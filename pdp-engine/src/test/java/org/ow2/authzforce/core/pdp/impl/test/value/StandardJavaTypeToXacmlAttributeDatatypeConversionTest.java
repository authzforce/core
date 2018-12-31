/**
 * Copyright 2012-2018 THALES.
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
package org.ow2.authzforce.core.pdp.impl.test.value;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.StandardAttributeValueFactories;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;

import com.google.common.collect.ImmutableMultiset;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

/**
 * 
 * Tests conversion from standard Java types to XACML datatype (attribute value)
 * <p>
 * Implements feature requested in <a href="https://github.com/authzforce/core/issues/10">GitHub issue #10</a>
 */
@RunWith(value = Parameterized.class)
public class StandardJavaTypeToXacmlAttributeDatatypeConversionTest
{
	private static final class MyBigInteger extends BigInteger
	{
		private static final long serialVersionUID = 1L;

		public MyBigInteger(final String val)
		{
			super(val);
		}

	}

	private static final BigInteger INTEGER_GREATER_THAN_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);

	private static final AttributeValueFactoryRegistry ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT = StandardAttributeValueFactories.getRegistry(false, Optional.empty());
	private static final AttributeValueFactoryRegistry ATT_VALUE_FACTORIES_WITH_LONG_INT_SUPPORT = StandardAttributeValueFactories.getRegistry(false, Optional.of(BigInteger.valueOf(Long.MAX_VALUE)));
	private static final AttributeValueFactoryRegistry ATT_VALUE_FACTORIES_WITH_BIG_INT_SUPPORT = StandardAttributeValueFactories.getRegistry(false, Optional.of(INTEGER_GREATER_THAN_MAX_LONG));

	private static volatile AtomicInteger testId = new AtomicInteger(0);

	@Parameters(name = "{index}: {1} -> {2}")
	public static Collection<Object[]> data()
	{
		final Object[][] data = new Object[][] {
		        /**
		         * each test input is: attributeValueFactories (standard if null), raw values, expected (XACML) datatype of attribute values created from them, exception iff error expected (null if
		         * none)
		         */
		        /* empty collection */
		        { null, Collections.emptyList(), StandardDatatypes.STRING.getId(), IllegalArgumentException.class },

		        /* string type */
		        { null, Arrays.asList("string"), StandardDatatypes.STRING.getId(), null },
		        /* multiple values (strings) */
		        { null, Arrays.asList("string1", "string2"), StandardDatatypes.STRING.getId(), null },

		        /* boolean type */
		        /* invalid */
		        /* invalid */
		        { null, Arrays.asList((Boolean) null), StandardDatatypes.BOOLEAN.getId(), IllegalArgumentException.class },

		        /*
		         * singleton
		         */
		        { null, Arrays.asList(true), StandardDatatypes.BOOLEAN.getId(), null },
		        /* multiple values of type boolean */
		        { null, Arrays.asList(true, false), StandardDatatypes.BOOLEAN.getId(), null },

		        /*
		         * integer
		         */
		        /* invalid */
		        { null, Arrays.asList((Integer) null), StandardDatatypes.INTEGER.getId(), IllegalArgumentException.class },

		        /* Short */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(Short.MIN_VALUE), StandardDatatypes.INTEGER.getId(), null },
		        /* Integer */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(Integer.MIN_VALUE), StandardDatatypes.INTEGER.getId(), null },

		        /* Long */
		        /* Using medium integer support (Long converted to Integer) */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(Long.valueOf(0)), StandardDatatypes.INTEGER.getId(), null },
		        /* Max long unsupported by attribute value factory for small/medium integers */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(Long.MAX_VALUE), StandardDatatypes.INTEGER.getId(), IllegalArgumentException.class },
		        /* Max long supported by attribute value factory for long integers */
		        { ATT_VALUE_FACTORIES_WITH_LONG_INT_SUPPORT, Arrays.asList(Long.MAX_VALUE), StandardDatatypes.INTEGER.getId(), null },

		        /* BigInteger (non-final!) */
		        /* Using medium integer support (BigInteger converted to Integer) */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(BigInteger.ZERO), StandardDatatypes.INTEGER.getId(), null },
		        /*
		         * Using long integer support (BigInteger converted to Long)
		         */
		        { ATT_VALUE_FACTORIES_WITH_LONG_INT_SUPPORT, Arrays.asList(BigInteger.ZERO), StandardDatatypes.INTEGER.getId(), null },
		        /* subtype of BigInteger */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(MyBigInteger.valueOf(0)), StandardDatatypes.INTEGER.getId(), null },
		        /* BigInteger too big to be supported by attribute value factory for small/medium integers */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(INTEGER_GREATER_THAN_MAX_LONG), StandardDatatypes.INTEGER.getId(), IllegalArgumentException.class },
		        /* BigInteger too big to be supported by attribute value factory for long integers */
		        { ATT_VALUE_FACTORIES_WITH_LONG_INT_SUPPORT, Arrays.asList(INTEGER_GREATER_THAN_MAX_LONG), StandardDatatypes.INTEGER.getId(), IllegalArgumentException.class },
		        /* BigInteger bigger than max long, supported by attribute value factory for big integers */
		        { ATT_VALUE_FACTORIES_WITH_BIG_INT_SUPPORT, Arrays.asList(INTEGER_GREATER_THAN_MAX_LONG), StandardDatatypes.INTEGER.getId(), null },

		        /* Mix of different integer types */
		        { ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT, Arrays.asList(Short.MAX_VALUE, Integer.MAX_VALUE, Long.valueOf(0), BigInteger.ZERO), StandardDatatypes.INTEGER.getId(), null },

		        { ATT_VALUE_FACTORIES_WITH_LONG_INT_SUPPORT, Arrays.asList(Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, BigInteger.ZERO), StandardDatatypes.INTEGER.getId(), null },

		        { ATT_VALUE_FACTORIES_WITH_BIG_INT_SUPPORT, Arrays.asList(Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, INTEGER_GREATER_THAN_MAX_LONG), StandardDatatypes.INTEGER.getId(), null },

		        /*
		         * double
		         */
		        /* invalid */
		        { null, Arrays.asList((Double) null), StandardDatatypes.DOUBLE.getId(), IllegalArgumentException.class },
		        /* from Double */
		        { null, Arrays.asList(Double.MIN_NORMAL), StandardDatatypes.DOUBLE.getId(), null },
		        /* from Float */
		        { null, Arrays.asList(Float.MIN_NORMAL), StandardDatatypes.DOUBLE.getId(), null },
		        /* Mix of float and double */
		        { null, Arrays.asList(Float.MIN_NORMAL, Double.MIN_NORMAL), StandardDatatypes.DOUBLE.getId(), null },

		        /*
		         * time
		         */
		        /* invalid */
		        { null, Arrays.asList((LocalTime) null), StandardDatatypes.TIME.getId(), IllegalArgumentException.class },
		        /* from LocalTime */
		        { null, Arrays.asList(LocalTime.now()), StandardDatatypes.TIME.getId(), null },
		        /* from OffsetTime */
		        { null, Arrays.asList(OffsetTime.now()), StandardDatatypes.TIME.getId(), null },
		        /* Mix of LocalTime and OffsetTime */
		        { null, Arrays.asList(LocalTime.now(), OffsetTime.now()), StandardDatatypes.TIME.getId(), null },

		        /*
		         * date
		         */
		        /* invalid */
		        { null, Arrays.asList((LocalDate) null), StandardDatatypes.DATE.getId(), IllegalArgumentException.class },
		        /*
		         * From LocalDate
		         */
		        { null, Arrays.asList(LocalDate.now()), StandardDatatypes.DATE.getId(), null },

		        /*
		         * date-time
		         */
		        /* invalid */
		        { null, Arrays.asList((LocalDateTime) null), StandardDatatypes.DATETIME.getId(), IllegalArgumentException.class },
		        /* from LocalDateTime */
		        { null, Arrays.asList(LocalDateTime.now()), StandardDatatypes.DATETIME.getId(), null },
		        /* from OffsetDateTime */
		        { null, Arrays.asList(OffsetDateTime.now()), StandardDatatypes.DATETIME.getId(), null },
		        /* from ZonedDateTime */
		        { null, Arrays.asList(ZonedDateTime.now()), StandardDatatypes.DATETIME.getId(), null },
		        /* from ZonedDateTime */
		        { null, Arrays.asList(Instant.now()), StandardDatatypes.DATETIME.getId(), null },
		        /* Mix of LocalDateTime, OffsetDateTime... */
		        { null, Arrays.asList(LocalDateTime.now(), OffsetDateTime.now(), ZonedDateTime.now(), Instant.now()), StandardDatatypes.DATETIME.getId(), null },
		        /* from LocalDateTime */
		        { null, Arrays.asList(LocalDateTime.now()), StandardDatatypes.DATETIME.getId(), null },

		        /*
		         * anyURI
		         */
		        /* from URI */
		        { null, Arrays.asList(URI.create("")), StandardDatatypes.ANYURI.getId(), null },

		        /*
		         * hexBinary
		         */
		        /* invalid: null */
		        { null, Arrays.asList((byte[]) null), StandardDatatypes.HEXBINARY.getId(), IllegalArgumentException.class },
		        /* Empty byte array is considered like empty XML value which is valid xs:hexBinary */
		        { null, Arrays.asList(new byte[] {}), StandardDatatypes.HEXBINARY.getId(), null },
		        /* singleton byte */
		        { null, Arrays.asList(new byte[] { 0x01 }), StandardDatatypes.HEXBINARY.getId(), null },
		        /* from bytes */
		        { null, Arrays.asList(new byte[] { 0x01, 0x23 }), StandardDatatypes.HEXBINARY.getId(), null },

		        /*
		         * x500Name
		         */
		        /* invalid */
		        { null, Arrays.asList((X500Principal) null), StandardDatatypes.X500NAME.getId(), IllegalArgumentException.class },
		        /* from bytes */
		        { null, Arrays.asList(new X500Principal("CN=someCN")), StandardDatatypes.X500NAME.getId(), null },

		        /* invalid mix of different datatypes */
		        { null, Arrays.asList(new Integer(0), LocalDate.now()), StandardDatatypes.INTEGER.getId(), IllegalArgumentException.class },

		        /*
		         * Unsupported java type
		         */
		        /*
		         * GregorianCalendar and Date classes not supported here because considered legacy code since Java 8. More info: https://docs.oracle.com/javase/tutorial/datetime/iso/legacy.html
		         */
		        { null, Arrays.asList(new Date()), StandardDatatypes.DATETIME.getId(), UnsupportedOperationException.class }
				/*
				 * 
				 */
		};

		return Arrays.asList(data);
	}

	private final AttributeValueFactoryRegistry attValFactories;
	private final Collection<? extends Serializable> rawValues;
	private final String expectedAttributeDatatypeId;
	private final Class<? extends Exception> expectedExceptionClass;

	public StandardJavaTypeToXacmlAttributeDatatypeConversionTest(final AttributeValueFactoryRegistry attValFactories, final Collection<? extends Serializable> rawValues,
	        final String expectedAttributeDatatypeId, final Class<? extends Exception> expectedExceptionClass)
	{
		this.attValFactories = attValFactories == null ? ATT_VALUE_FACTORIES_WITH_MEDIUM_INT_SUPPORT : attValFactories;
		this.rawValues = rawValues;
		this.expectedAttributeDatatypeId = expectedAttributeDatatypeId;
		this.expectedExceptionClass = expectedExceptionClass;
	}

	@Test
	public void test()
	{
		if (rawValues == null || rawValues.isEmpty())
		{
			try
			{
				attValFactories.newAttributeBag(rawValues);
				Assert.fail("Should have raised IllegalArgumentException because of invalid rawValues");
			} catch (final Exception e)
			{
				Assert.assertTrue("Unexpected error: " + e, expectedExceptionClass != null && expectedExceptionClass.isInstance(e));
			}
			return;
		}

		// rawValues has at least one value
		if (rawValues.size() == 1)
		{
			final Serializable rawVal = rawValues.iterator().next();
			if (rawVal == null)
			{
				/*
				 * Instantiate using expected datatype to check if null is spotted as invalid value
				 */
				try
				{
					attValFactories.newExpression(expectedAttributeDatatypeId, Collections.singletonList(rawVal), null, null);
					Assert.assertTrue("Parsing raw value into AttributeValue did not throw exception as expected", expectedExceptionClass == null);
				} catch (final Exception e)
				{
					Assert.assertTrue("Unexpected error: " + e, expectedExceptionClass != null && expectedExceptionClass.isInstance(e));
				}
			} else
			{
				try
				{
					final AttributeValueFactory<?> attValFactory = attValFactories.getCompatibleFactory(rawVal.getClass());
					final String actualDatatypeId = attValFactory.getDatatype().getId();
					Assert.assertEquals("Invalid datatype for created attribute value", actualDatatypeId, expectedAttributeDatatypeId);
					attValFactories.newAttributeValue(rawVal);
					Assert.assertTrue("Parsing raw value into AttributeValue did not throw exception as expected", expectedExceptionClass == null);
				} catch (final Exception e)
				{
					Assert.assertTrue("Unexpected error: " + e, expectedExceptionClass != null && expectedExceptionClass.isInstance(e));
				}
			}
		}

		try
		{
			final AttributeBag<?> attBag = attValFactories.newAttributeBag(rawValues);
			Assert.assertEquals("Invalid datatype for created attribute values", attBag.getElementDatatype().getId(), expectedAttributeDatatypeId);

			/*
			 * Marshall to XACML and try to unmarshall to original Java value to make sure marshalling is OK
			 */
			final List<AttributeValueType> outXacmlAttVals = attBag.elements().stream()
			        .map(attVal -> new AttributeValueType(attVal.getContent(), attBag.getElementDatatype().getId(), attVal.getXmlAttributes())).collect(Collectors.toList());
			final Attribute outXacmlAtt = new Attribute(outXacmlAttVals, testId.toString(), null, false);
			final Marshaller marshaller = Xacml3JaxbHelper.createXacml3Marshaller();
			final StringWriter strWriter = new StringWriter();
			marshaller.marshal(outXacmlAtt, strWriter);
			final String outStr = strWriter.toString();

			final Unmarshaller unmarshaller = Xacml3JaxbHelper.createXacml3Unmarshaller();
			final Attribute inXacmlAtt = (Attribute) unmarshaller.unmarshal(new StringReader(outStr));
			final List<AttributeValueType> inXacmlAttVals = inXacmlAtt.getAttributeValues();
			if (inXacmlAttVals.isEmpty())
			{
				Assert.fail("Marshalling/unmarshalling failed: no AttributeValue after unmarshalling: " + outStr);
				return;
			}

			final AttributeValueType inXacmlAttVal0 = inXacmlAttVals.get(0);
			final AttributeValueFactory<?> attValFactory = this.attValFactories.getExtension(inXacmlAttVal0.getDataType());
			final List<AttributeValue> inAttVals = inXacmlAttVals.stream()
			        .map(inputXacmlAttValue -> attValFactory.getInstance(inputXacmlAttValue.getContent(), inputXacmlAttValue.getOtherAttributes(), null)).collect(Collectors.toList());

			Assert.assertEquals("AttributeValues after unmarshalling do not match original AttributeValues before marshalling: " + outStr, attBag.elements(), ImmutableMultiset.copyOf(inAttVals));
			Assert.assertTrue("Parsing raw value into AttributeValue did not throw exception as expected", expectedExceptionClass == null);
		} catch (final Exception e)
		{
			Assert.assertTrue("Unexpected error: " + e, expectedExceptionClass != null && expectedExceptionClass.isInstance(e));
		}
	}

	public static void main(String[] args)
	{
		final byte[] hex = DatatypeConverter.parseHexBinary("01");
		System.out.println(hex);
	}

}
