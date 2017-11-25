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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.CloseableDesignatedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.DesignatedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Closeable AttributeProvider
 * <p>
 * The sub-providers may very likely hold resources such as network resources to get attributes remotely, or attribute caches to speed up finding, etc. Therefore, you are required to call
 * {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with different modules) - in order to make sure these resources are released properly by each
 * underlying module (e.g. close the attribute caches).
 *
 * @version $Id: $
 */
public final class CloseableAttributeProvider extends ModularAttributeProvider implements Closeable
{

	private static final class ModuleAdapter
	{
		private final CloseableDesignatedAttributeProvider module;

		private ModuleAdapter(final CloseableDesignatedAttributeProvider module) throws IOException
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

		private DesignatedAttributeProvider getAdaptedModule()
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

	private CloseableAttributeProvider(final ImmutableListMultimap<AttributeFqn, DesignatedAttributeProvider> modulesByAttributeId, final Set<ModuleAdapter> moduleClosers,
			final boolean strictAttributeIssuerMatch)
	{
		super(modulesByAttributeId, null, strictAttributeIssuerMatch);
		assert moduleClosers != null;
		this.moduleClosers = moduleClosers;
	}

	private static final CloseableAttributeProvider EVALUATION_CONTEXT_ONLY_SCOPED_CLOSEABLE_ATTRIBUTE_PROVIDER = new CloseableAttributeProvider(ImmutableListMultimap.of(),
			Collections.<ModuleAdapter> emptySet(), true);

	/**
	 * Instantiates attribute Provider that tries to find attribute values in evaluation context, then, if not there, query the {@code module} providing the requested attribute ID, if any.
	 *
	 * @param attributeFactory
	 *            (mandatory) attribute value factory
	 * @param attributeProviderFactories
	 *            Attribute Provider factories (Attribute Providers resolve values of attributes absent from the request context). Empty if none.
	 * @return instance of this class
	 * @param strictAttributeIssuerMatch
	 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case that
	 *            the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full compliance with
	 *            the XACML 3.0 Attribute Evaluation: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute SHALL be governed by
	 *            AttributeId and DataType attributes alone."
	 * @throws java.lang.IllegalArgumentException
	 *             If any Attribute Provider created from {@code attributeProviderFactories} does not provide any attribute.
	 * @throws java.io.IOException
	 *             error closing the Attribute Providers created from {@code attributeProviderFactories}, when a {@link IllegalArgumentException} is raised
	 */
	public static CloseableAttributeProvider getInstance(final List<CloseableDesignatedAttributeProvider.DependencyAwareFactory> attributeProviderFactories,
			final AttributeValueFactoryRegistry attributeFactory, final boolean strictAttributeIssuerMatch) throws IOException
	{
		if (attributeProviderFactories == null || attributeProviderFactories.isEmpty())
		{
			return EVALUATION_CONTEXT_ONLY_SCOPED_CLOSEABLE_ATTRIBUTE_PROVIDER;
		}

		final ListMultimap<AttributeFqn, DesignatedAttributeProvider> modulesByAttributeId = ArrayListMultimap.create();
		final int moduleCount = attributeProviderFactories.size();
		final Set<ModuleAdapter> mutableModuleCloserSet = HashCollections.newUpdatableSet(moduleCount);
		for (final CloseableDesignatedAttributeProvider.DependencyAwareFactory attProviderFactory : attributeProviderFactories)
		{
			try
			{
				final Set<AttributeDesignatorType> requiredAttrs = attProviderFactory.getDependencies();
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
					final ImmutableListMultimap<AttributeFqn, DesignatedAttributeProvider> immutableCopyOfAttrProviderModsByAttrId = ImmutableListMultimap.copyOf(modulesByAttributeId);
					depAttrProvider = new ModularAttributeProvider(immutableCopyOfAttrProviderModsByAttrId, requiredAttrs, strictAttributeIssuerMatch);
				}

				/*
				 * attrProviderMod closing isn't done in this method but handled in close() method when closing all modules
				 */
				final ModuleAdapter moduleAdapter = new ModuleAdapter(attProviderFactory.getInstance(attributeFactory, depAttrProvider));
				mutableModuleCloserSet.add(moduleAdapter);

				for (final AttributeDesignatorType attrDesignator : moduleAdapter.getProvidedAttributes())
				{
					final AttributeFqn attrGUID = AttributeFqns.newInstance(attrDesignator);
					/*
					 * We allow multiple modules supporting the same attribute designator (as fall-back: if one does not find any value, the next one comes in)
					 */
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

		return new CloseableAttributeProvider(ImmutableListMultimap.copyOf(modulesByAttributeId), HashCollections.newImmutableSet(mutableModuleCloserSet), strictAttributeIssuerMatch);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		close(this.moduleClosers);
	}
}
