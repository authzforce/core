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
package org.ow2.authzforce.core;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Registry of extensions of specific type.
 * 
 * @param <T>
 *            type of extension in this registry
 */
public interface PdpExtensionRegistry<T extends PdpExtension>
{
	/**
	 * Adds the extension to the registry
	 * 
	 * @param extension
	 *            extension
	 * 
	 * @throws IllegalArgumentException
	 *             if an extension with same ID is already registered
	 */
	void addExtension(T extension) throws IllegalArgumentException;

	/**
	 * Get an extension by ID.
	 * 
	 * @param identity
	 *            ID of extension to loop up
	 * 
	 * @return extension, null if none with such ID in the registry
	 */
	T getExtension(String identity);

	/**
	 * PDP extension comparator (compares IDs)
	 *
	 * @param <E>
	 *            extension type
	 */
	class PdpExtensionComparator<E extends PdpExtension> implements Comparator<E>, Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(E e1, E e2)
		{
			return e1.getId().compareTo(e2.getId());
		}

	}

}
