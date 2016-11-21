/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.value;

import java.util.Set;
import java.util.TreeSet;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry.PdpExtensionComparator;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registry provides the factories for standard attribute datatypes specified in XACML.
 *
 * 
 * @version $Id: $
 */
public final class StandardDatatypeFactoryRegistry
{
	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(StandardDatatypeFactoryRegistry.class);

	private static final PdpExtensionComparator<DatatypeFactory<?>> DATATYPE_EXTENSION_COMPARATOR = new PdpExtensionComparator<>();

	/**
	 * Singleton instance of Standard mandatory datatype registry, i.e. not including XPath datatype
	 */
	private static final DatatypeFactoryRegistry NON_XPATH_DATATYPES;

	/**
	 * Singleton instance of registry of all standard datatypes, i.e. including XPath datatype
	 */
	private static final DatatypeFactoryRegistry ALL_DATATYPES;

	static
	{
		final Set<DatatypeFactory<?>> datatypeFactories = HashCollections.newUpdatableSet(StandardDatatypes.MANDATORY_DATATYPE_SET.size() + 1);
		for (final SimpleValue.Factory<? extends SimpleValue<? extends Object>> typeFactory : StandardDatatypes.MANDATORY_DATATYPE_SET)
		{
			datatypeFactories.add(typeFactory);
		}

		NON_XPATH_DATATYPES = new ImmutableDatatypeFactoryRegistry(datatypeFactories);

		// create another instance with optional xpathExpression datatype, to be used only if XPath support enabled, see
		// getInstance(boolean) method.
		datatypeFactories.add(StandardDatatypes.XPATH_FACTORY);
		ALL_DATATYPES = new ImmutableDatatypeFactoryRegistry(datatypeFactories);

		if (LOGGER.isDebugEnabled())
		{
			final TreeSet<DatatypeFactory<?>> sortedFactories = new TreeSet<>(DATATYPE_EXTENSION_COMPARATOR);
			sortedFactories.addAll(datatypeFactories);
			LOGGER.debug("Supported XACML standard datatypes: {}", sortedFactories);
		}

	}

	private StandardDatatypeFactoryRegistry()
	{
		// prevent instantiation
	}

	/**
	 * Get standard function registry
	 *
	 * @param enableXPath
	 *            true iff XPath-based function(s) support enabled
	 * @return standard function registry
	 */
	public static DatatypeFactoryRegistry getRegistry(final boolean enableXPath)
	{
		return enableXPath ? ALL_DATATYPES : NON_XPATH_DATATYPES;
	}

}
