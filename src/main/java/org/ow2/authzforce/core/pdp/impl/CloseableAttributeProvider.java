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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.AttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.CloseableAttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;

/**
 * Closeable AttributeProvider
 * <p>
 * The sub-modules may very likely hold resources such as network resources to get attributes remotely, or attribute caches to speed up finding, etc. Therefore, you are required to call
 * {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with different modules) - in order to make sure these resources are released properly by each
 * underlying module (e.g. close the attribute caches).
 *
 * @version $Id: $
 */
public final class CloseableAttributeProvider extends ModularAttributeProvider implements Closeable
{

	private static final class ModuleAdapter
	{
		private final CloseableAttributeProviderModule module;

		private ModuleAdapter(final CloseableAttributeProviderModule module) throws IOException
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

	private static void close(final Set<ModuleAdapter> moduleClosers) throws IOException
	{
		// An error occuring on closing one module should not stop from closing
		// the others
		// But we keep the exception in memory if any, to throw it at the end as
		// we do not want to hide that an error occurred
		IOException latestEx = null;
		for (final ModuleAdapter mod : moduleClosers)
		{
			try
			{
				mod.close();
			}
			catch (final IOException e)
			{
				latestEx = e;
			}
		}

		if (latestEx != null)
		{
			throw latestEx;
		}
	}

	// not-null
	private final Set<ModuleAdapter> moduleClosers;

	private CloseableAttributeProvider(final Map<AttributeGUID, AttributeProviderModule> modulesByAttributeId, final Set<ModuleAdapter> moduleClosers, final boolean strictAttributeIssuerMatch)
	{
		super(modulesByAttributeId, null, strictAttributeIssuerMatch);
		assert moduleClosers != null;
		this.moduleClosers = moduleClosers;
	}

	private static final CloseableAttributeProvider EVALUATION_CONTEXT_ONLY_SCOPED_CLOSEABLE_ATTRIBUTE_PROVIDER = new CloseableAttributeProvider(
			Collections.<AttributeGUID, AttributeProviderModule> emptyMap(), Collections.<ModuleAdapter> emptySet(), true);

	/**
	 * Instantiates attribute Provider that tries to find attribute values in evaluation context, then, if not there, query the {@code module} providing the requested attribute ID, if any.
	 *
	 * @param attributeFactory
	 *            (mandatory) attribute value factory
	 * @param jaxbAttributeProviderConfs
	 *            (optional) XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static expression evaluation (out of context), in
	 *            which case AttributeSelectors/AttributeDesignators are not supported
	 * @param environmentProperties
	 *            global PDP configuration environment properties
	 * @return instance of this class
	 * @param strictAttributeIssuerMatch
	 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case that
	 *            the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full compliance with
	 *            the XACML 3.0 Attribute Evaluation: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute SHALL be governed by
	 *            AttributeId and DataType attributes alone."
	 * @throws java.lang.IllegalArgumentException
	 *             If any of attribute Provider modules created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in conflict with another one already registered to
	 *             provide the same or part of the same attributes.
	 * @throws java.io.IOException
	 *             error closing the attribute Provider modules created from {@code jaxbAttributeProviderConfs}, when and before an {@link IllegalArgumentException} is raised
	 */
	public static CloseableAttributeProvider getInstance(final List<AbstractAttributeProvider> jaxbAttributeProviderConfs, final DatatypeFactoryRegistry attributeFactory,
			final boolean strictAttributeIssuerMatch, final EnvironmentProperties environmentProperties) throws IOException
	{
		if (jaxbAttributeProviderConfs == null)
		{
			return EVALUATION_CONTEXT_ONLY_SCOPED_CLOSEABLE_ATTRIBUTE_PROVIDER;
		}

		final Map<AttributeGUID, AttributeProviderModule> modulesByAttributeId = HashCollections.newUpdatableMap();
		final int moduleCount = jaxbAttributeProviderConfs.size();
		final Set<ModuleAdapter> mutableModuleCloserSet = HashCollections.newUpdatableSet(moduleCount);
		for (final AbstractAttributeProvider jaxbAttributeProviderConf : jaxbAttributeProviderConfs)
		{
			try
			{
				final CloseableAttributeProviderModule.FactoryBuilder<AbstractAttributeProvider> attrProviderModBuilder = PdpExtensionLoader.getJaxbBoundExtension(
						CloseableAttributeProviderModule.FactoryBuilder.class, jaxbAttributeProviderConf.getClass());
				final CloseableAttributeProviderModule.DependencyAwareFactory depAwareAttrProviderModBuilder = attrProviderModBuilder.getInstance(jaxbAttributeProviderConf, environmentProperties);
				final Set<AttributeDesignatorType> requiredAttrs = depAwareAttrProviderModBuilder.getDependencies();
				/*
				 * Each AttributeProviderModule is given a read-only AttributeProvider - aka "dependency attribute Provider" - to find any attribute they require (dependency), based on the attribute
				 * Provider modules that provide these required attributes (set above); read-only so that modules use this attribute Provider only to get required attributes, nothing else. Create this
				 * dependency attribute Provider.
				 */
				final AttributeProvider depAttrProvider;
				if (requiredAttrs == null)
				{
					depAttrProvider = ModularAttributeProvider.EVALUATION_CONTEXT_ONLY_SCOPED_ATTRIBUTE_PROVIDER;
				}
				else
				{
					final Map<AttributeGUID, AttributeProviderModule> immutableCopyOfAttrProviderModsByAttrId = Collections
							.<AttributeGUID, AttributeProviderModule> unmodifiableMap(modulesByAttributeId);
					depAttrProvider = new ModularAttributeProvider(immutableCopyOfAttrProviderModsByAttrId, requiredAttrs, strictAttributeIssuerMatch);
				}

				// attrProviderMod closing isn't done in this method but
				// handled in close() method when closing all modules
				final ModuleAdapter moduleAdapter = new ModuleAdapter(depAwareAttrProviderModBuilder.getInstance(attributeFactory, depAttrProvider));
				mutableModuleCloserSet.add(moduleAdapter);

				for (final AttributeDesignatorType attrDesignator : moduleAdapter.getProvidedAttributes())
				{
					final AttributeGUID attrGUID = new AttributeGUID(attrDesignator);
					if (modulesByAttributeId.containsKey(attrGUID))
					{
						moduleAdapter.close();
						throw new IllegalArgumentException("Conflict: " + moduleAdapter + " providing the same AttributeDesignator (" + attrGUID + ") as another already registered.");
					}

					modulesByAttributeId.put(attrGUID, moduleAdapter.getAdaptedModule());
				}
			}
			catch (final IllegalArgumentException e)
			{
				close(mutableModuleCloserSet);
				throw e;
			}
		}

		if (modulesByAttributeId.isEmpty())
		{
			return EVALUATION_CONTEXT_ONLY_SCOPED_CLOSEABLE_ATTRIBUTE_PROVIDER;
		}

		return new CloseableAttributeProvider(modulesByAttributeId, HashCollections.newImmutableSet(mutableModuleCloserSet), strictAttributeIssuerMatch);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		close(this.moduleClosers);
	}
}
