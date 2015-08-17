package com.thalesgroup.authzforce.core.attr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue.Factory;
import com.thalesgroup.authzforce.core.attr.Base64BinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.BaseDatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.HexBinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;

/**
 * This registry provides the factories for standard attribute datatypes specified in XACML.
 * <p>
 * Note that because this supports only the standard datatypes, this factory does not allow the
 * addition of any other datatypes. If you call <code>addDatatype</code> on an instance of this
 * class, an exception will be thrown. If you need a standard factory that is modifiable, you should
 * create a new <code>BaseDatatypeFactoryRegistry</code> (or some other
 * <code>DatatypeFactoryRegistry</code>) and pass this to
 * {@link BaseDatatypeFactoryRegistry#BaseDatatypeFactoryRegistry(BaseDatatypeFactoryRegistry)}.
 */
public class StandardDatatypeFactoryRegistry extends BaseDatatypeFactoryRegistry
{
	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(StandardDatatypeFactoryRegistry.class);

	private static final AttributeValue.Factory<?>[] STD_DATATYPE_FACTORIES = {
			// 1.x datatypes
			BooleanAttributeValue.FACTORY,
			//
			StringAttributeValue.FACTORY,
			//
			DateAttributeValue.FACTORY,
			//
			TimeAttributeValue.FACTORY,
			//
			DateTimeAttributeValue.FACTORY,
			//
			DayTimeDurationAttributeValue.FACTORY,
			//
			YearMonthDurationAttributeValue.FACTORY,
			//
			DoubleAttributeValue.FACTORY,
			//
			IntegerAttributeValue.FACTORY,
			//
			AnyURIAttributeValue.FACTORY,
			//
			HexBinaryAttributeValue.FACTORY,
			//
			Base64BinaryAttributeValue.FACTORY,
			//
			X500NameAttributeValue.FACTORY,
			//
			RFC822NameAttributeValue.FACTORY,
			// 2.x datatypes
			DNSNameAttributeValue.FACTORY,
			//
			IPAddressAttributeValue.FACTORY
	//
	};

	/**
	 * Singleton instance of standard datatype factory.
	 */
	public static StandardDatatypeFactoryRegistry INSTANCE;

	static
	{
		final Map<String, AttributeValue.Factory<?>> attrDatatypeUriToFactoryMap = new HashMap<>();
		for (final AttributeValue.Factory<?> datatypeFactory : STD_DATATYPE_FACTORIES)
		{
			attrDatatypeUriToFactoryMap.put(datatypeFactory.getId(), datatypeFactory);
		}

		INSTANCE = new StandardDatatypeFactoryRegistry(attrDatatypeUriToFactoryMap);
	}

	/**
	 * Private constructor
	 * 
	 * @param stdDatatypeUriToFactoryMap
	 *            standard datatype URI to factory mapping
	 */
	private StandardDatatypeFactoryRegistry(Map<String, Factory<?>> stdDatatypeUriToFactoryMap)
	{
		super(Collections.unmodifiableMap(stdDatatypeUriToFactoryMap));
		LOGGER.info("Loaded standard datatypes");
	}

	/**
	 * Throws an <code>UnsupportedOperationException</code> since you are not allowed to modify what
	 * a standard factory supports.
	 */
	@Override
	public void addExtension(AttributeValue.Factory<? extends AttributeValue> attrDatatypeFactory)
	{
		throw new UnsupportedOperationException("This factory does not support custom datatypes but only standard ones.");
	}

}
