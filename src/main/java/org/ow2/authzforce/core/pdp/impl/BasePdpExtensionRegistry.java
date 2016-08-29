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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Map;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;

import com.koloboke.collect.map.ObjObjMap;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import com.koloboke.collect.set.hash.HashObjSets;

/**
 * This is a base implementation of <code>PdpExtensionRegistry</code>. This should be used as basis to implement (in a
 * final class) an immutable PDP extension registry of a specific type. If you need a generic immutable PDP extension
 * registry, see {
 *
 * @param <T>
 *            type of extension in this registry
 * @version $Id: $
 */
public abstract class BasePdpExtensionRegistry<T extends PdpExtension> implements PdpExtensionRegistry<T>
{

	private final Class<? super T> extClass;
	private final Map<String, T> extensionsById;
	private final transient String toString;

	/**
	 * Instantiates immutable registry from a map.
	 *
	 * @param extensionClass
	 *            extension class
	 * @param extensions
	 *            extensions input map; the registry actually creates and uses an immutable copy of this map internally
	 *            to avoid external modifications on the internal map
	 */
	protected BasePdpExtensionRegistry(Class<? super T> extensionClass, Map<String, T> extensionsById)
	{
		assert extensionClass != null && extensionsById != null;

		this.extClass = extensionClass;
		this.extensionsById = HashObjObjMaps.newImmutableMap(extensionsById);
		this.toString = this + "( extensionClass= " + extClass.getCanonicalName() + " )";
	}

	/** {@inheritDoc} */
	@Override
	public final T getExtension(String identity)
	{
		return extensionsById.get(identity);
	}

	/** {@inheritDoc} */
	@Override
	public final Set<T> getExtensions()
	{
		return HashObjSets.newImmutableSet(extensionsById.values());
	}

	private static <E extends PdpExtension> Map<String, E> newImmutableMap(Set<E> extensions)
	{
		final ObjObjMap<String, E> mutableMap = HashObjObjMaps.newUpdatableMap(extensions.size());
		for (final E extension : extensions)
		{
			mutableMap.put(extension.getId(), extension);
		}

		return HashObjObjMaps.newImmutableMap(mutableMap);
	}

	/**
	 * Instantiates immutable registry from a set of extensions
	 *
	 * @param extensionClass
	 *            extension class (required not null)
	 * @param extensions
	 *            extensions (required not null)
	 */
	protected BasePdpExtensionRegistry(Class<? super T> extensionClass, Set<T> extensions)
	{
		this(extensionClass, newImmutableMap(extensions));
	}

	@Override
	public String toString()
	{
		return toString;
	}

}
