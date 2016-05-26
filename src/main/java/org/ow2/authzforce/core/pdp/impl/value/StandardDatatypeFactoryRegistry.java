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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registry provides the factories for standard attribute datatypes specified in XACML.
 * <p>
 * Note that because this supports only the standard datatypes, this factory does not allow the addition of any other datatypes. If you call <code>addDatatype</code> on an instance of this class, an
 * exception will be thrown. If you need a standard factory that is modifiable, you should create a new <code>BaseDatatypeFactoryRegistry</code> (or some other <code>DatatypeFactoryRegistry</code>)
 * and pass this to {@link BaseDatatypeFactoryRegistry#BaseDatatypeFactoryRegistry(BaseDatatypeFactoryRegistry)}.
 *
 * 
 * @version $Id: $
 */
public final class StandardDatatypeFactoryRegistry extends BaseDatatypeFactoryRegistry
{
	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(StandardDatatypeFactoryRegistry.class);

	/**
	 * Singleton instance of Standard mandatory datatype registry
	 */
	public static final StandardDatatypeFactoryRegistry MANDATORY_DATATYPES;

	/**
	 * Singleton instance of registry of all standard datatypes
	 */
	public static final StandardDatatypeFactoryRegistry ALL_DATATYPES;

	static
	{
		final Set<DatatypeFactory<?>> datatypeFactories = new HashSet<>();
		for (final SimpleValue.Factory<? extends SimpleValue<? extends Object>> typeFactory : StandardDatatypes.MANDATORY_DATATYPE_SET)
		{
			datatypeFactories.add(typeFactory);
		}

		MANDATORY_DATATYPES = new StandardDatatypeFactoryRegistry(Collections.unmodifiableSet(datatypeFactories));

		// create another instance with optional xpathExpression datatype, to be used only if XPath support enabled, see getInstance(boolean) method.
		datatypeFactories.add(StandardDatatypes.XPATH_FACTORY);
		ALL_DATATYPES = new StandardDatatypeFactoryRegistry(Collections.unmodifiableSet(datatypeFactories));

		if (LOGGER.isDebugEnabled())
		{
			final TreeSet<DatatypeFactory<?>> sortedFactories = new TreeSet<>(COMPARATOR);
			sortedFactories.addAll(datatypeFactories);
			LOGGER.debug("Supported XACML standard datatypes: {}", sortedFactories);
		}
	}

	/**
	 * Private constructor
	 * 
	 * @param stdDatatypeFactories
	 *            standard datatype factories
	 */
	private StandardDatatypeFactoryRegistry(Set<DatatypeFactory<?>> stdDatatypeFactories)
	{
		super(stdDatatypeFactories);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Throws an <code>UnsupportedOperationException</code> since you are not allowed to modify what a standard factory supports.
	 */
	@Override
	public void addExtension(DatatypeFactory<?> attrDatatypeFactory)
	{
		throw new UnsupportedOperationException("This factory does not support custom datatypes but only standard ones.");
	}

}
