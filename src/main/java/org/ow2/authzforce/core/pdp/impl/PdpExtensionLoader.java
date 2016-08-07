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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPdpExtension;

/**
 * Loads PDP extensions (implementing {@link PdpExtension}) from classpath using {@link ServiceLoader}.
 *
 * @version $Id: $
 */
public class PdpExtensionLoader
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(PdpExtensionLoader.class);

	/*
	 * For each type of extension, we build the maps allowing to get the compatible/supporting extension class, using {@link ServiceLoader} API, to discover these classes from files
	 * 'META-INF/services/org.ow2.authzforce.core.pdp.api.PdpExtension' on the classpath, in the format described by {@link ServiceLoader} API documentation.
	 */

	/**
	 * Types of zero-conf (non-JAXB-bound) extension
	 */
	private static final Set<Class<? extends PdpExtension>> NON_JAXB_BOUND_EXTENSION_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DatatypeFactory.class, Function.class,
			CombiningAlg.class, RequestFilter.Factory.class, DecisionResultFilter.class)));

	/*
	 * For each type of zero-conf (non-JAXB-bound) extension, have a map (extension ID -> extension instance), so that the extension ID is scoped to the extension type among the ones listed in
	 * NON_JAXB_BOUND_EXTENSION_CLASSES (you can have same ID but for different types of extensions).
	 */
	private final static Map<Class<? extends PdpExtension>, Map<String, PdpExtension>> NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID;

	/*
	 * For each type of XML/JAXB-bound extension, map XML/JAXB configuration class to corresponding extension (we assume a one-to-one relationship between the XML/JAXB type and the extension class)
	 */
	private final static Map<Class<? extends AbstractPdpExtension>, JaxbBoundPdpExtension<? extends AbstractPdpExtension>> JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS;

	static
	{
		final Map<Class<? extends PdpExtension>, Map<String, PdpExtension>> mutableNonJaxbBoundExtMapByClassAndId = new HashMap<>();
		final Map<Class<? extends AbstractPdpExtension>, JaxbBoundPdpExtension<? extends AbstractPdpExtension>> mutableJaxbBoundExtMapByClass = new HashMap<>();

		/*
		 * REMINDER: every service provider (implementation class) loaded by ServiceLoader MUST HAVE a ZERO-ARGUMENT CONSTRUCTOR.
		 */
		final ServiceLoader<PdpExtension> extensionLoader = ServiceLoader.load(PdpExtension.class);
		for (final PdpExtension extension : extensionLoader)
		{
			boolean isValidExt = false;
			if (extension instanceof JaxbBoundPdpExtension<?>)
			{
				final JaxbBoundPdpExtension<?> jaxbBoundExt = (JaxbBoundPdpExtension<?>) extension;
				final JaxbBoundPdpExtension<?> conflictingExt = mutableJaxbBoundExtMapByClass.put(jaxbBoundExt.getJaxbClass(), jaxbBoundExt);
				if (conflictingExt != null)
				{
					throw new IllegalArgumentException("Extension " + jaxbBoundExt + " (" + jaxbBoundExt.getClass() + ") is conflicting with " + conflictingExt + "(" + conflictingExt.getClass()
							+ ") for the same XML/JAXB configuration class: " + jaxbBoundExt.getJaxbClass());
				}

				isValidExt = true;
			} else
			{
				for (final Class<? extends PdpExtension> extClass : NON_JAXB_BOUND_EXTENSION_CLASSES)
				{
					if (extClass.isInstance(extension))
					{
						final Map<String, PdpExtension> oldMap = mutableNonJaxbBoundExtMapByClassAndId.get(extClass);
						final Map<String, PdpExtension> newMap;
						if (oldMap == null)
						{
							newMap = new HashMap<>();
							mutableNonJaxbBoundExtMapByClassAndId.put(extClass, newMap);
						} else
						{
							newMap = oldMap;
						}

						final PdpExtension conflictingExt = newMap.put(extension.getId(), extension);
						if (conflictingExt != null)
						{
							throw new IllegalArgumentException("Extension " + extension + " is conflicting with " + conflictingExt + " registered with same ID: " + extension.getId());
						}

						isValidExt = true;
						break;
					}
				}
			}

			if (!isValidExt)
			{
				throw new UnsupportedOperationException("Unsupported/invalid type of PDP extension: " + extension.getClass() + " (extension ID = " + extension.getId() + ")");
			}
		}

		NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID = Collections.unmodifiableMap(mutableNonJaxbBoundExtMapByClassAndId);
		JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS = Collections.unmodifiableMap(mutableJaxbBoundExtMapByClass);
	}

	/**
	 * Get PDP extension configuration classes (JAXB-generated from XML schema)
	 *
	 * @return classes representing datamodels of configurations of all PDP extensions
	 */
	public static Set<Class<? extends AbstractPdpExtension>> getExtensionJaxbClasses()
	{
		return Collections.unmodifiableSet(JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
	}

	/**
	 * Get non-JAXB-bound (aka zero-configuration) extension identifiers. Used by PAPs for instance, to get the list of extensions supported by the PDP before modifying PDP's configuration
	 *
	 * @param extensionType
	 *            type of extension: {@link DatatypeFactory }, {@link Function}, {@link CombiningAlg}, etc.
	 * @return unmodifiable set of supported non-JAXB bound extension IDs; may be empty (not null) if no extension available for this type
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code extensionType} is not a valid extension type
	 */
	public static <T extends PdpExtension> Set<String> getNonJaxbBoundExtensionIDs(final Class<T> extensionType) throws IllegalArgumentException
	{
		if (!NON_JAXB_BOUND_EXTENSION_CLASSES.contains(extensionType))
		{
			throw new IllegalArgumentException("Invalid (non-JAXB-bound) PDP extension type: " + extensionType + ". Expected types: " + NON_JAXB_BOUND_EXTENSION_CLASSES);
		}

		final Map<String, PdpExtension> typeSpecificExtsById = NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID.get(extensionType);
		if (typeSpecificExtsById == null)
		{
			return Collections.emptySet();
		}

		return Collections.unmodifiableSet(typeSpecificExtsById.keySet());
	}

	/**
	 * Get non-JAXB-bound (aka zero-configuration) extension
	 *
	 * @param extensionType
	 *            type of extension: {@link DatatypeFactory}, {@link Function}, etc.
	 * @param id
	 *            extension ID
	 * @return PDP extension instance of class {@code extensionType} and such that its method {@link PdpExtension#getId()} returns {@code id}
	 * @throws java.lang.IllegalArgumentException
	 *             if there is not any extension found for type {@code extensionType} with ID {@code id}
	 */
	public static <T extends PdpExtension> T getExtension(final Class<T> extensionType, final String id) throws IllegalArgumentException
	{
		if (!NON_JAXB_BOUND_EXTENSION_CLASSES.contains(extensionType))
		{
			throw new IllegalArgumentException("Invalid (non-JAXB-bound) PDP extension type: " + extensionType + ". Expected types: " + NON_JAXB_BOUND_EXTENSION_CLASSES);
		}

		final Map<String, PdpExtension> typeSpecificExtsById = NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID.get(extensionType);
		if (typeSpecificExtsById == null)
		{
			throw new IllegalArgumentException("No PDP extension of type '" + extensionType + "' found");
		}

		final PdpExtension ext = typeSpecificExtsById.get(id);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension of type '" + extensionType + "' found with ID: " + id + ". Expected IDs: " + typeSpecificExtsById.keySet());
		}

		return extensionType.cast(ext);
	}

	/**
	 * Get XML/JAXB-bound extension
	 *
	 * @param extensionType
	 *            type of extension, e.g. {@link org.ow2.authzforce.core.pdp.api.policy.RootPolicyProviderModule.Factory}, etc.
	 * @param jaxbPdpExtensionClass
	 *            JAXB class representing XML configuration type that the extension must support
	 * @return PDP extension instance of class {@code extensionType} and such that its method {@link JaxbBoundPdpExtension#getClass()} returns {@code jaxbPdpExtensionClass}
	 * @throws java.lang.IllegalArgumentException
	 *             if there is no extension supporting {@code jaxbPdpExtensionClass}
	 */
	public static <JAXB_T extends AbstractPdpExtension, T extends JaxbBoundPdpExtension<JAXB_T>> T getJaxbBoundExtension(final Class<T> extensionType, final Class<JAXB_T> jaxbPdpExtensionClass)
			throws IllegalArgumentException
	{
		final JaxbBoundPdpExtension<?> ext = JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.get(jaxbPdpExtensionClass);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension found supporting JAXB (configuration) type: " + jaxbPdpExtensionClass + ". Expected types: "
					+ JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
		}

		return extensionType.cast(ext);
	}

}
