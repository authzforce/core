/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Map;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * This is a base implementation of <code>PdpExtensionRegistry</code>. This should be used as basis to implement (in a final class) an immutable PDP extension registry of a specific type. If you need
 * a generic immutable PDP extension registry, see {
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
	 * @param extensionsById
	 *            extensions input map; the registry actually creates and uses an immutable copy of this map internally to avoid external modifications on the internal map
	 */
	protected BasePdpExtensionRegistry(final Class<? super T> extensionClass, final Map<String, ? extends T> extensionsById)
	{
		assert extensionClass != null && extensionsById != null;

		this.extClass = extensionClass;
		this.extensionsById = HashCollections.newImmutableMap(extensionsById);
		this.toString = this + "( extensionClass= " + extClass.getCanonicalName() + " )";
	}

	/** {@inheritDoc} */
	@Override
	public final T getExtension(final String identity)
	{
		return extensionsById.get(identity);
	}

	/** {@inheritDoc} */
	@Override
	public final Set<T> getExtensions()
	{
		return HashCollections.newImmutableSet(extensionsById.values());
	}

	private static final class ExtensionToIdFunction<E extends PdpExtension> implements Function<E, String>
	{

		@Override
		public String apply(final E extension) throws NullPointerException
		{
			assert extension != null;
			return Preconditions.checkNotNull(extension, "One of the input extensions is invalid (null)").getId();
		}

	}

	private static final Function<? extends PdpExtension, String> EXTENSION_TO_ID_FUNCTION = new ExtensionToIdFunction<>();

	@SuppressWarnings("unchecked")
	private static <E extends PdpExtension> Map<String, E> newImmutableMap(final Set<E> extensions)
	{
		return Maps.uniqueIndex(extensions, (Function<E, String>) EXTENSION_TO_ID_FUNCTION);
	}

	/**
	 * Instantiates immutable registry from a set of extensions
	 *
	 * @param extensionClass
	 *            extension class (required not null)
	 * @param extensions
	 *            extensions (required not null)
	 */
	protected BasePdpExtensionRegistry(final Class<? super T> extensionClass, final Set<? extends T> extensions)
	{
		this(extensionClass, newImmutableMap(extensions));
	}

	@Override
	public String toString()
	{
		return toString;
	}

}
