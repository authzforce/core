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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.CloseableAttributeProviderModule;
import org.ow2.authzforce.core.pdp.api.DecisionCache;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPdpExtension;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Loads PDP extensions (implementing {@link PdpExtension}) from classpath using {@link ServiceLoader}.
 *
 * @version $Id: $
 */
public final class PdpExtensionLoader
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(PdpExtensionLoader.class);

	/*
	 * For each type of extension, we build the maps allowing to get the compatible/supporting extension class, using {@link ServiceLoader} API, to discover these classes from files
	 * 'META-INF/services/org.ow2.authzforce.core.pdp.api.PdpExtension' on the classpath, in the format described by {@link ServiceLoader} API documentation.
	 */

	/**
	 * Types of zero-conf (non-JAXB-bound) extension
	 */
	private static final Set<Class<? extends PdpExtension>> NON_JAXB_BOUND_EXTENSION_CLASSES = HashCollections.newImmutableSet(Arrays.asList(DatatypeFactory.class, Function.class, CombiningAlg.class,
			RequestFilter.Factory.class, DecisionResultFilter.class));

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
		final Table<Class<? extends PdpExtension>, String, PdpExtension> mutableNonJaxbBoundExtMapByClassAndId = HashBasedTable.create();
		final Map<Class<? extends AbstractPdpExtension>, JaxbBoundPdpExtension<? extends AbstractPdpExtension>> mutableJaxbBoundExtMapByClass = HashCollections.newUpdatableMap();

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
				final JaxbBoundPdpExtension<?> duplicate = mutableJaxbBoundExtMapByClass.putIfAbsent(jaxbBoundExt.getJaxbClass(), jaxbBoundExt);
				if (duplicate != null)
				{
					throw new IllegalArgumentException("Extension " + jaxbBoundExt + " (" + jaxbBoundExt.getClass() + ") is conflicting with " + duplicate + "(" + duplicate.getClass()
							+ ") for the same XML/JAXB configuration class: " + jaxbBoundExt.getJaxbClass());
				}

				isValidExt = true;
			}
			else
			{
				for (final Class<? extends PdpExtension> extClass : NON_JAXB_BOUND_EXTENSION_CLASSES)
				{
					if (extClass.isInstance(extension))
					{
						final PdpExtension duplicate = mutableNonJaxbBoundExtMapByClassAndId.put(extClass, extension.getId(), extension);
						if (duplicate != null)
						{
							throw new IllegalArgumentException("Extension " + extension + " is conflicting with " + duplicate + " registered with same ID: " + extension.getId());
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

		NON_JAXB_BOUND_EXTENSIONS_BY_CLASS_AND_ID = HashCollections.newImmutableMap(mutableNonJaxbBoundExtMapByClassAndId.rowMap());
		JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS = HashCollections.newImmutableMap(mutableJaxbBoundExtMapByClass);
	}

	/**
	 * Get PDP extension configuration classes (JAXB-generated from XML schema)
	 *
	 * @return classes representing datamodels of configurations of all PDP extensions
	 */
	public static Set<Class<? extends AbstractPdpExtension>> getExtensionJaxbClasses()
	{
		return HashCollections.newImmutableSet(JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
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

		return HashCollections.newImmutableSet(typeSpecificExtsById.keySet());
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
	 * Create RootPolicyProviderModule
	 * 
	 * @param jaxbRootPolicyProviderConf
	 *            module configuration (instance of JAXB-annotated class derived from XML instance)
	 * @param enableNsAwarePolicyParsing
	 *            true iff namespace-aware policy parsing must be enabled, e.g. for namespace-aware XPath evaluation of AttributeSelectors, etc.
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions in the root policy(set)
	 * @param combiningAlgRegistry
	 *            registry of combining algorithms for instantiating algorithms used in the root policy(set) *
	 * @param jaxbRefPolicyProviderConf
	 *            (optional) XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in root policy; may be null if support of PolicyReferences is disabled or
	 *            this RootPolicyProvider module already supports these. Used as argument to {@code refPolicyProviderModuleFactory.getInstance(REF_POLICY_PROVIDER_CONF)}
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2
	 *            -> ...; a strictly negative value means no limit. If and only if {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param refPolicyProviderModuleFactory
	 *            (optional) refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf} . May be null iff
	 *            {@code jaxbRefPolicyProviderConf == null}. If not null, it is the responsibility of the root Policy Provider implementation to use this and {@code jaxbRefPolicyProviderConf} as
	 *            argument to instantiate the ref Policy Provider, and close it with (@link RefPolicyProviderModule#close()) when it is done using it, in particular when closing the root policy
	 *            provider created by this factory (with {@link RootPolicyProviderModule#close()}).
	 * @param environmentProperties
	 *            PDP configuration environment properties
	 * 
	 *
	 * @return Root Policy Provider Module
	 * @throws java.lang.IllegalArgumentException
	 *             if there is no extension of type {@link org.ow2.authzforce.core.pdp.api.policy.RootPolicyProviderModule.Factory} supporting {@code jaxbPdpExtensionClass}
	 */

	public static <ROOT_POLICY_PROVIDER_CONF extends AbstractPolicyProvider, REF_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> RootPolicyProviderModule getRootPolicyProviderModule(
			final ROOT_POLICY_PROVIDER_CONF jaxbRootPolicyProviderConf, final boolean enableNsAwarePolicyParsing, final ExpressionFactory expressionFactory,
			final CombiningAlgRegistry combiningAlgRegistry, final REF_POLICY_PROVIDER_CONF jaxbRefPolicyProviderConf,
			final RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF> refPolicyProviderModuleFactory, final int maxPolicySetRefDepth, final EnvironmentProperties environmentProperties)
			throws IllegalArgumentException
	{
		final Class<ROOT_POLICY_PROVIDER_CONF> jaxbPolicyProviderConfClass = (Class<ROOT_POLICY_PROVIDER_CONF>) jaxbRootPolicyProviderConf.getClass();
		final JaxbBoundPdpExtension<ROOT_POLICY_PROVIDER_CONF> ext = (JaxbBoundPdpExtension<ROOT_POLICY_PROVIDER_CONF>) JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.get(jaxbPolicyProviderConfClass);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension found supporting JAXB (configuration) type: " + jaxbPolicyProviderConfClass + ". Expected types: "
					+ JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
		}

		if (!(ext instanceof RootPolicyProviderModule.Factory))
		{
			throw new IllegalArgumentException("No PDP extension of type " + RootPolicyProviderModule.Factory.class
					+ " (Root Policy Provider Module factory) supporting JAXB/XML (configuration) type: " + jaxbPolicyProviderConfClass);
		}

		return ((RootPolicyProviderModule.Factory<ROOT_POLICY_PROVIDER_CONF>) ext).getInstance(jaxbRootPolicyProviderConf, JaxbXACMLUtils.getXACMLParserFactory(enableNsAwarePolicyParsing),
				expressionFactory, combiningAlgRegistry, jaxbRefPolicyProviderConf, refPolicyProviderModuleFactory, maxPolicySetRefDepth, environmentProperties);
	}

	/**
	 * Get Reference-based Policy Provider Module factory
	 * 
	 * @param jaxbRefPolicyProviderConf
	 *            module configuration (instance of JAXB-annotated class derived from XML instance)
	 * @return Reference-based Policy Provider Module
	 * @throws java.lang.IllegalArgumentException
	 *             if there is no extension of type {@link org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule.Factory} supporting {@code jaxbPdpExtensionClass}
	 */
	public static <REF_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF> getRefPolicyProviderModuleFactory(
			final REF_POLICY_PROVIDER_CONF jaxbRefPolicyProviderConf) throws IllegalArgumentException
	{
		final Class<REF_POLICY_PROVIDER_CONF> jaxbPolicyProviderConfClass = (Class<REF_POLICY_PROVIDER_CONF>) jaxbRefPolicyProviderConf.getClass();
		final JaxbBoundPdpExtension<REF_POLICY_PROVIDER_CONF> ext = (JaxbBoundPdpExtension<REF_POLICY_PROVIDER_CONF>) JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.get(jaxbPolicyProviderConfClass);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension found supporting JAXB (configuration) type: " + jaxbPolicyProviderConfClass + ". Expected types: "
					+ JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
		}

		if (!(ext instanceof RefPolicyProviderModule.Factory))
		{
			throw new IllegalArgumentException("No PDP extension of type " + RefPolicyProviderModule.Factory.class
					+ " (Reference-based Policy Provider Module factory) supporting JAXB/XML (configuration) type: " + jaxbPolicyProviderConfClass);
		}

		return (RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF>) ext;
	}

	/**
	 * Get Attribute Provider Module factory builder
	 * 
	 * @param jaxbAttributeProviderConf
	 *            module configuration (instance of JAXB-annotated class derived from XML instance)
	 * @return Attribute Provider Module factory builder
	 * @throws java.lang.IllegalArgumentException
	 *             if there is no extension of type {@link org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule.Factory} supporting {@code jaxbPdpExtensionClass}
	 */
	public static <ATTRIBUTE_PROVIDER_CONF extends AbstractAttributeProvider> CloseableAttributeProviderModule.FactoryBuilder<ATTRIBUTE_PROVIDER_CONF> getAttributeProviderModuleFactoryBuilder(
			final ATTRIBUTE_PROVIDER_CONF jaxbAttributeProviderConf)
	{
		final Class<ATTRIBUTE_PROVIDER_CONF> jaxbAttributeProviderConfClass = (Class<ATTRIBUTE_PROVIDER_CONF>) jaxbAttributeProviderConf.getClass();
		final JaxbBoundPdpExtension<ATTRIBUTE_PROVIDER_CONF> ext = (JaxbBoundPdpExtension<ATTRIBUTE_PROVIDER_CONF>) JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.get(jaxbAttributeProviderConfClass);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension found supporting JAXB (configuration) type: " + jaxbAttributeProviderConfClass + ". Expected types: "
					+ JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
		}

		if (!(ext instanceof CloseableAttributeProviderModule.FactoryBuilder))
		{
			throw new IllegalArgumentException("No PDP extension of type " + CloseableAttributeProviderModule.FactoryBuilder.class
					+ " (Attribute Provider Module factory builder) supporting JAXB/XML (configuration) type: " + jaxbAttributeProviderConfClass);
		}

		return (CloseableAttributeProviderModule.FactoryBuilder<ATTRIBUTE_PROVIDER_CONF>) ext;
	}

	/**
	 * Get Attribute Provider Module factory builder
	 * 
	 * @param jaxbAttributeProviderConf
	 *            module configuration (instance of JAXB-annotated class derived from XML instance)
	 * @return Attribute Provider Module factory builder
	 * @throws java.lang.IllegalArgumentException
	 *             if there is no extension of type {@link org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule.Factory} supporting {@code jaxbPdpExtensionClass}
	 */
	public static <DECISION_CACHE_CONF extends AbstractDecisionCache> DecisionCache.Factory<DECISION_CACHE_CONF> getDecisionCacheFactory(final DECISION_CACHE_CONF jaxbDecisionCacheConf)
	{
		final Class<DECISION_CACHE_CONF> jaxbDecisionCacheConfClass = (Class<DECISION_CACHE_CONF>) jaxbDecisionCacheConf.getClass();
		final JaxbBoundPdpExtension<DECISION_CACHE_CONF> ext = (JaxbBoundPdpExtension<DECISION_CACHE_CONF>) JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.get(jaxbDecisionCacheConfClass);
		if (ext == null)
		{
			throw new IllegalArgumentException("No PDP extension found supporting JAXB (configuration) type: " + jaxbDecisionCacheConfClass + ". Expected types: "
					+ JAXB_BOUND_EXTENSIONS_BY_JAXB_CLASS.keySet());
		}

		if (!(ext instanceof DecisionCache.Factory))
		{
			throw new IllegalArgumentException("No PDP extension of type " + DecisionCache.Factory.class + " (Decision Cache factory) supporting JAXB/XML (configuration) type: "
					+ jaxbDecisionCacheConfClass);
		}

		return (DecisionCache.Factory<DECISION_CACHE_CONF>) ext;
	}

}
