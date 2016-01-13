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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.AttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.CloseableAttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.DatatypeFactoryRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;

/**
 * Closeable AttributeProvider
 * <p>
 * The sub-modules may very likely hold resources such as network resources to get attributes remotely, or attribute caches to speed up finding, etc. Therefore,
 * you are required to call {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with different modules) - in
 * order to make sure these resources are released properly by each underlying module (e.g. close the attribute caches).
 * 
 */
public final class CloseableAttributeProvider extends ModularAttributeProvider implements Closeable
{

	private static class ModuleAdapter
	{
		private CloseableAttributeProviderModule module;

		private ModuleAdapter(CloseableAttributeProviderModule module) throws IOException
		{
			final Set<AttributeDesignatorType> providedAttributes = module.getProvidedAttributes();
			if (providedAttributes == null || providedAttributes.isEmpty())
			{
				module.close();
				throw new IllegalArgumentException("Invalid " + module + " : list of supported AttributeDesignators is null or empty");
			}

			this.module = module;
		}

		private void close() throws IOException
		{
			this.module.close();
		}

		private Set<AttributeDesignatorType> getProvidedAttributes()
		{
			return this.module.getProvidedAttributes();
		}

		@Override
		public String toString()
		{
			return module.toString();
		}

		private AttributeProviderModule getAdaptedModule()
		{
			return this.module;
		}
	}

	private static void close(Set<ModuleAdapter> moduleClosers) throws IOException
	{
		// An error occuring on closing one module should not stop from closing the others
		// But we keep the exception in memory if any, to throw it at the end as we do not want to hide that an error occurred
		IOException latestEx = null;
		for (final ModuleAdapter mod : moduleClosers)
		{
			try
			{
				mod.close();
			} catch (IOException e)
			{
				latestEx = e;
			}
		}

		if (latestEx != null)
		{
			throw latestEx;
		}
	}

	private Set<ModuleAdapter> moduleClosers;

	/**
	 * Instantiates attribute Provider that tries to find attribute values in evaluation context, then, if not there, query the {@code module} providing the
	 * requested attribute ID, if any.
	 * 
	 * @param attributeFactory
	 *            (mandatory) attribute value factory
	 * 
	 * @param jaxbAttributeProviderConfs
	 *            (optional) XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static
	 *            expression evaluation (out of context), in which case AttributeSelectors/AttributeDesignators are not supported
	 * @throws IllegalArgumentException
	 *             If any of attribute Provider modules created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in conflict
	 *             with another one already registered to provide the same or part of the same attributes.
	 * @throws IOException
	 *             error closing the attribute Provider modules created from {@code jaxbAttributeProviderConfs}, when and before an
	 *             {@link IllegalArgumentException} is raised
	 */
	private CloseableAttributeProvider(Map<AttributeGUID, AttributeProviderModule> modulesByAttributeId, Set<ModuleAdapter> moduleClosers) throws IOException
	{
		super(modulesByAttributeId);
		this.moduleClosers = moduleClosers;
	}

	/**
	 * Instantiates attribute Provider that tries to find attribute values in evaluation context, then, if not there, query the {@code module} providing the
	 * requested attribute ID, if any.
	 * 
	 * @param attributeFactory
	 *            (mandatory) attribute value factory
	 * 
	 * @param jaxbAttributeProviderConfs
	 *            (optional) XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static
	 *            expression evaluation (out of context), in which case AttributeSelectors/AttributeDesignators are not supported
	 * @return instance of this class
	 * @throws IllegalArgumentException
	 *             If any of attribute Provider modules created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in conflict
	 *             with another one already registered to provide the same or part of the same attributes.
	 * @throws IOException
	 *             error closing the attribute Provider modules created from {@code jaxbAttributeProviderConfs}, when and before an
	 *             {@link IllegalArgumentException} is raised
	 */
	public static CloseableAttributeProvider getInstance(List<AbstractAttributeProvider> jaxbAttributeProviderConfs, DatatypeFactoryRegistry attributeFactory)
			throws IOException
	{
		final Map<AttributeGUID, AttributeProviderModule> modulesByAttributeId;
		final Set<ModuleAdapter> moduleCloserSet;
		if (jaxbAttributeProviderConfs == null)
		{
			modulesByAttributeId = null;
			moduleCloserSet = null;
		} else
		{
			final int moduleCount = jaxbAttributeProviderConfs.size();
			modulesByAttributeId = new HashMap<>(moduleCount);
			moduleCloserSet = new HashSet<>(moduleCount);
			for (final AbstractAttributeProvider jaxbAttributeProviderConf : jaxbAttributeProviderConfs)
			{
				try
				{
					final CloseableAttributeProviderModule.FactoryBuilder<AbstractAttributeProvider> attrProviderModBuilder = PdpExtensionLoader
							.getJaxbBoundExtension(CloseableAttributeProviderModule.FactoryBuilder.class, jaxbAttributeProviderConf.getClass());
					final CloseableAttributeProviderModule.DependencyAwareFactory depAwareAttrProviderModBuilder = attrProviderModBuilder
							.getInstance(jaxbAttributeProviderConf);
					final Set<AttributeDesignatorType> requiredAttrs = depAwareAttrProviderModBuilder.getDependencies();
					/*
					 * Each AttributeProviderModule is given a read-only AttributeProvider - aka "dependency attribute Provider" - to find any attribute they
					 * require (dependency), based on the attribute Provider modules that provide these required attributes (set above); read-only so that
					 * modules use this attribute Provider only to get required attributes, nothing else. Create this dependency attribute Provider.
					 */
					final AttributeProvider depAttrProvider;
					if (requiredAttrs == null)
					{
						depAttrProvider = new ModularAttributeProvider(null);
					} else
					{
						final Map<AttributeGUID, AttributeProviderModule> immutableCopyOfAttrProviderModsByAttrId = Collections
								.<AttributeGUID, AttributeProviderModule> unmodifiableMap(modulesByAttributeId);
						depAttrProvider = new ModularAttributeProvider(immutableCopyOfAttrProviderModsByAttrId, requiredAttrs);
					}

					// attrProviderMod closing isn't done in this method but handled in close() method when closing all modules
					final ModuleAdapter moduleAdapter = new ModuleAdapter(depAwareAttrProviderModBuilder.getInstance(attributeFactory, depAttrProvider));
					moduleCloserSet.add(moduleAdapter);

					for (final AttributeDesignatorType attrDesignator : moduleAdapter.getProvidedAttributes())
					{
						final AttributeGUID attrGUID = new AttributeGUID(attrDesignator);
						if (modulesByAttributeId.containsKey(attrGUID))
						{
							moduleAdapter.close();
							throw new IllegalArgumentException("Conflict: " + moduleAdapter + " providing the same AttributeDesignator (" + attrGUID
									+ ") as another already registered.");
						}

						modulesByAttributeId.put(attrGUID, moduleAdapter.getAdaptedModule());
					}
				} catch (IllegalArgumentException e)
				{
					close(moduleCloserSet);
					throw e;
				}
			}
		}

		return new CloseableAttributeProvider(modulesByAttributeId, moduleCloserSet);
	}

	@Override
	public void close() throws IOException
	{
		close(this.moduleClosers);
	}
}