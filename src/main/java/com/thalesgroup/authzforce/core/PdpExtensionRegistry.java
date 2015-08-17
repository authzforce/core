package com.thalesgroup.authzforce.core;


/**
 * Registry of extensions of specific type. 
 * @param <T> type of extension in this registry
 */
public interface PdpExtensionRegistry<T extends PdpExtension>
{
	/**
	 * Adds the extension to the registry
	 * @param extension extension
	 * 
	 * @throws IllegalArgumentException
	 *             if an extension with same ID is already registered
	 */
	void addExtension(T extension) throws IllegalArgumentException;

//	/**
//	 * Get all the registered extensions.
//	 * @return registered extensions
//	 */
//	Set<T> getExtensions();

	/**
	 * Get an extension by ID.
	 * 
	 * @param identity
	 *            ID of extension to loop up
	 *            
	 * @return extension, null if none with such ID in the registry
	 */
	T getExtension(String identity);

}
