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
package org.ow2.authzforce.core.pdp.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a com.thalesgroup.authzforce.core.test.basic implementation of <code>PdpExtensionRegistry</code>.
 *
 * @param <T>
 *            type of extension in this registry
 * @version $Id: $
 */
public class BasePdpExtensionRegistry<T extends PdpExtension> implements PdpExtensionRegistry<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BasePdpExtensionRegistry.class);

	private static final IllegalArgumentException NULL_EXTENSION_CLASS_EXCEPTION = new IllegalArgumentException("Extension class arg undefined");
	private static final IllegalArgumentException NULL_EXTENSIONS_EXCEPTION = new IllegalArgumentException("ExtensionsById arg undefined");

	private final Class<? super T> extClass;

	// the backing maps for the Function objects
	private final Map<String, T> extensionsById;

	/**
	 * Instantiates registry from a map (id -> extension)
	 * 
	 * @param extensionClass
	 *            extension class
	 * 
	 * @param extensionsById
	 *            extensions indexed by ID
	 */
	private BasePdpExtensionRegistry(Class<? super T> extensionClass, Map<String, T> extensionsById)
	{
		if (extensionClass == null)
		{
			throw NULL_EXTENSION_CLASS_EXCEPTION;
		}

		if (extensionsById == null)
		{
			throw NULL_EXTENSIONS_EXCEPTION;
		}

		this.extClass = extensionClass;
		this.extensionsById = extensionsById;
	}

	/**
	 * Instantiates immutable registry from a set of extensions
	 *
	 * @param extensionClass
	 *            extension class
	 * @param extensions
	 *            extensions
	 */
	public BasePdpExtensionRegistry(Class<? super T> extensionClass, Set<T> extensions)
	{
		if (extensionClass == null)
		{
			throw NULL_EXTENSION_CLASS_EXCEPTION;
		}

		if (extensions == null)
		{
			throw NULL_EXTENSIONS_EXCEPTION;
		}

		this.extClass = extensionClass;

		this.extensionsById = new HashMap<>();
		for (final T extension : extensions)
		{
			final String id = extension.getId();

			this.extensionsById.put(id, extension);
		}
	}

	/**
	 * Default constructor. No superset factory is used.
	 *
	 * @param extensionClass
	 *            extension class
	 */
	public BasePdpExtensionRegistry(Class<? super T> extensionClass)
	{
		this(extensionClass, new HashMap<String, T>());
	}

	/**
	 * Constructor that sets a "base registry" from which this inherits all the extensions. Used for instance to build a new registry based on a standard one like the StandardFunctionRegistry for
	 * standard functions).
	 *
	 * @param baseRegistry
	 *            the base/parent registry on which this one is based or null
	 * @param extensionClass
	 *            extension class
	 */
	public BasePdpExtensionRegistry(Class<? super T> extensionClass, BasePdpExtensionRegistry<T> baseRegistry)
	{
		this(extensionClass, baseRegistry == null ? new HashMap<String, T>() : new HashMap<>(baseRegistry.extensionsById));
	}

	/** {@inheritDoc} */
	@Override
	public void addExtension(T extension) throws IllegalArgumentException
	{
		final String id = extension.getId();
		// make sure nothing already registered with same ID
		if (extensionsById.containsKey(id))
		{
			throw new IllegalArgumentException("Conflict: extension (id=" + id + ") already registered");
		}

		extensionsById.put(id, extension);
		LOGGER.debug("Added PDP extension of {} to registry: {}", extClass, extension);
	}

	/** {@inheritDoc} */
	@Override
	public T getExtension(String identity)
	{
		return extensionsById.get(identity);
	}

}
