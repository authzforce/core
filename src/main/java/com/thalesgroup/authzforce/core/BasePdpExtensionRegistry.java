package com.thalesgroup.authzforce.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a com.thalesgroup.authzforce.core.test.basic implementation of <code>PdpExtensionRegistry</code>.
 * 
 * @param <T>
 *            type of extension in this registry
 */
public class BasePdpExtensionRegistry<T extends PdpExtension> implements PdpExtensionRegistry<T>
{
	private final Logger logger;

	// the backing maps for the Function objects
	private final Map<String, T> extensionsById;

	/**
	 * Instantiates registry from a map (id -> extension)
	 * 
	 * @param extensionsById
	 *            extensions indexed by ID
	 */
	public BasePdpExtensionRegistry(Map<String, T> extensionsById)
	{
		assert extensionsById != null;
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.extensionsById = extensionsById;
		if (logger.isDebugEnabled())
		{
			logger.debug("Added PDP extensions: {}", extensionsById.values());
		}
	}

	/**
	 * Default constructor. No superset factory is used.
	 */
	public BasePdpExtensionRegistry()
	{
		this(new HashMap<String, T>());
	}

	/**
	 * Constructor that sets a "base registry" from which this inherits all the extensions. Used for
	 * instance to build a new registry based on a standard one like the StandardFunctionRegistry
	 * for standard functions).
	 * 
	 * @param baseRegistry
	 *            the base/parent registry on which this one is based or null
	 */
	public BasePdpExtensionRegistry(BasePdpExtensionRegistry<T> baseRegistry)
	{
		this(baseRegistry == null ? new HashMap<String, T>() : new HashMap<>(baseRegistry.extensionsById));
	}

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

		logger.debug("Added PDP extension: {}", extension);
	}

	@Override
	public T getExtension(String identity)
	{
		return extensionsById.get(identity);
	}

}
