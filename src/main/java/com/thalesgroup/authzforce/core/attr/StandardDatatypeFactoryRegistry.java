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
package com.thalesgroup.authzforce.core.attr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue.Factory;

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
public final class StandardDatatypeFactoryRegistry extends BaseDatatypeFactoryRegistry
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
			IPAddressAttributeValue.FACTORY,
			// 3.x datatype(s)
			// Support of this datatype is optional in the spec
			XPathAttributeValue.FACTORY };

	/**
	 * Singleton instance of standard datatype factory.
	 */
	public static final StandardDatatypeFactoryRegistry INSTANCE;

	static
	{
		final Map<String, AttributeValue.Factory<?>> attrDatatypeUriToFactoryMap = new HashMap<>();
		for (final AttributeValue.Factory<?> datatypeFactory : STD_DATATYPE_FACTORIES)
		{
			attrDatatypeUriToFactoryMap.put(datatypeFactory.getId(), datatypeFactory);
		}

		INSTANCE = new StandardDatatypeFactoryRegistry(attrDatatypeUriToFactoryMap);
		LOGGER.debug("Loaded XACML standard datatypes: {}", attrDatatypeUriToFactoryMap.keySet());
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
	}

	/**
	 * Throws an <code>UnsupportedOperationException</code> since you are not allowed to modify what
	 * a standard factory supports.
	 */
	@Override
	public void addExtension(AttributeValue.Factory<?> attrDatatypeFactory)
	{
		throw new UnsupportedOperationException("This factory does not support custom datatypes but only standard ones.");
	}

}
