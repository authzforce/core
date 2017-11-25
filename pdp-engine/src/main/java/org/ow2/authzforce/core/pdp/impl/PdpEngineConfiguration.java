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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.ow2.authzforce.core.pdp.api.CloseableDesignatedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.DecisionCache;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EnvironmentPropertyName;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue.StringParseableValueFactory;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.combining.ImmutableCombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.pdp.impl.expression.DepthLimitingExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.func.ImmutableFunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;
import org.ow2.authzforce.core.pdp.impl.value.ImmutableAttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.impl.value.StandardAttributeValueFactories;
import org.ow2.authzforce.core.xmlns.pdp.InOutProcChain;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.core.xmlns.pdp.StandardEnvironmentAttributeSource;
import org.ow2.authzforce.xacml.identifiers.XacmlDatatypeId;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.ImmutableMap;

/**
 * PDP engine configuration
 *
 */
public final class PdpEngineConfiguration
{
	private static final IllegalArgumentException NULL_REQPREPROC_EXCEPTION = new IllegalArgumentException(
			"Undefined request preprocessor ('requestPreproc' element) in I/O processing chain ('ioProcChain' element)");

	private static final IllegalArgumentException ILLEGAL_USE_STD_FUNCTIONS_ARGUMENT_EXCEPTION = new IllegalArgumentException("useStandardFunctions = true not allowed if useStandardDatatypes = false");

	private static final IllegalArgumentException NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined rootPolicyProvider");

	// the logger we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(BasePdpEngine.class);

	private static final IllegalArgumentException NULL_PDP_MODEL_HANDLER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined PDP configuration model handler");

	private static boolean isXpathBased(final Function<?> function)
	{
		/*
		 * A function is said "XPath-based" iff it takes at least one (XACML) xpathExpression parameter. Regarding higher-order function, as of now, we only provide higher-order functions defined in
		 * the XACML (3.0) Core specification, which are not XPath-based, or if a higher-order function happens to take a XPathExpression parameter, it is actually a parameter to the first-order
		 * sub-function. Plus it is not possible to add extensions that are higher-order functions in this PDP implementation. Therefore, it is enough to check first-order functions (class
		 * FirstOrderFunction) only. (Remember that such functions may be used as parameter to a higher-order function.)
		 */
		if (function instanceof FirstOrderFunction)
		{
			final List<? extends Datatype<?>> paramTypes = ((FirstOrderFunction<?>) function).getParameterTypes();
			for (final Datatype<?> paramType : paramTypes)
			{
				if (paramType.getId().equals(XacmlDatatypeId.XPATH_EXPRESSION.value()))
				{
					return true;
				}
			}
		}

		return false;
	}

	private static <JAXB_CONF extends AbstractAttributeProvider> CloseableDesignatedAttributeProvider.DependencyAwareFactory newAttributeProviderProviderFactory(final JAXB_CONF jaxbConf,
			final EnvironmentProperties envProps)
	{
		final CloseableDesignatedAttributeProvider.FactoryBuilder<JAXB_CONF> attrProviderModBuilder = PdpExtensions.getAttributeProviderFactoryBuilder((Class<JAXB_CONF>) jaxbConf.getClass());
		return attrProviderModBuilder.getInstance(jaxbConf, envProps);
	}

	private static <JAXB_CONF extends AbstractPolicyProvider> CloseableRefPolicyProvider newRefPolicyProvider(final JAXB_CONF jaxbConf, final XmlnsFilteringParserFactory xacmlParserFactory,
			final int maxPolicySetRefDepth, final ExpressionFactory xacmlExprFactory, final CombiningAlgRegistry combiningAlgRegistry, final EnvironmentProperties envProps)
	{
		final CloseableRefPolicyProvider.Factory<JAXB_CONF> refPolicyProviderModFactory = PdpExtensions.getRefPolicyProviderFactory((Class<JAXB_CONF>) jaxbConf.getClass());
		return refPolicyProviderModFactory.getInstance(jaxbConf, xacmlParserFactory, maxPolicySetRefDepth, xacmlExprFactory, combiningAlgRegistry, envProps);
	}

	private static <JAXB_CONF extends AbstractPolicyProvider> RootPolicyProvider newRootPolicyProvider(final JAXB_CONF jaxbConf, final XmlnsFilteringParserFactory xacmlParserFactory,
			final ExpressionFactory xacmlExprFactory, final CombiningAlgRegistry combiningAlgRegistry, final Optional<CloseableRefPolicyProvider> refPolicyProvider,
			final EnvironmentProperties envProps)
	{
		final RootPolicyProvider.Factory<JAXB_CONF> rootPolicyProviderFactory = PdpExtensions.getRootPolicyProviderFactory((Class<JAXB_CONF>) jaxbConf.getClass());
		return rootPolicyProviderFactory.getInstance(jaxbConf, xacmlParserFactory, xacmlExprFactory, combiningAlgRegistry, refPolicyProvider, envProps);
	}

	private final boolean enableXPath;
	private final AttributeValueFactoryRegistry attValFactoryRegistry;

	private final ExpressionFactory xacmlExpressionFactory;

	private final RootPolicyProvider rootPolicyProvider;

	private final boolean strictAttributeIssuerMatch;

	private final StandardEnvironmentAttributeSource stdEnvAttributeSource;

	private final Optional<DecisionCache> decisionCache;

	private final Map<Class<?>, Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>>> ioProcChainsByInputType;

	private final int clientReqErrVerbosityLevel;

	/**
	 * Constructs configuration from PDP XML-schema-derived JAXB model (usually 'unmarshaled' from XML configuration file)
	 *
	 * @param pdpJaxbConf
	 *            (JAXB-bound) PDP configuration
	 * @param envProps
	 *            PDP configuration environment properties (e.g. PARENT_DIR)
	 * @throws java.lang.IllegalArgumentException
	 *             invalid PDP configuration
	 * @throws java.io.IOException
	 *             if any error occurred closing already created {@link Closeable} modules (policy Providers, attribute Providers, decision cache)
	 */
	public PdpEngineConfiguration(final Pdp pdpJaxbConf, final EnvironmentProperties envProps) throws IllegalArgumentException, IOException
	{
		/*
		 * Check required args
		 */
		/*
		 * Root policy provider
		 */
		final AbstractPolicyProvider rootPolicyProviderJaxbConf = pdpJaxbConf.getRootPolicyProvider();
		if (rootPolicyProviderJaxbConf == null)
		{
			throw NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION;
		}

		/*
		 * Enable support for XPath expressions, XPath functions, etc.
		 */
		enableXPath = pdpJaxbConf.isEnableXPath();

		// Attribute datatypes (primitive)
		final List<String> datatypeExtensionIdentifiers = pdpJaxbConf.getAttributeDatatypes();
		final Set<AttributeValueFactory<?>> datatypeExtensions = HashCollections.newUpdatableSet(datatypeExtensionIdentifiers.size());
		for (final String datatypeId : datatypeExtensionIdentifiers)
		{
			final AttributeValueFactory<?> datatypeFactory = PdpExtensions.getExtension(AttributeValueFactory.class, datatypeId);
			datatypeExtensions.add(datatypeFactory);
		}

		/*
		 * Merge with standards if required, or use the standards as is if no extension
		 */
		final boolean enableStdDatatypes = pdpJaxbConf.isUseStandardDatatypes();
		if (enableStdDatatypes)
		{
			final AttributeValueFactoryRegistry stdRegistry = StandardAttributeValueFactories.getRegistry(enableXPath, Optional.ofNullable(pdpJaxbConf.getMaxIntegerValue()));
			if (datatypeExtensionIdentifiers.isEmpty())
			{
				attValFactoryRegistry = stdRegistry;
			}
			else
			{
				attValFactoryRegistry = new ImmutableAttributeValueFactoryRegistry(HashCollections.newImmutableSet(stdRegistry.getExtensions(), datatypeExtensions));
			}
		}
		else
		{
			attValFactoryRegistry = new ImmutableAttributeValueFactoryRegistry(datatypeExtensions);
		}

		// Standard Environment Attribute source
		final StandardEnvironmentAttributeSource stdEnvAttSourceFromJaxbConf = pdpJaxbConf.getStandardEnvAttributeSource();
		/*
		 * The default behavior for getting the standard environment attributes (current date/time) is the one complying strictly with the XACML spec: if request does not have values for these
		 * attributes, the "context handler" (PDP in this case) must provide them. So we use PDP values if it does not override any existing value in the request.
		 */
		stdEnvAttributeSource = stdEnvAttSourceFromJaxbConf == null ? StandardEnvironmentAttributeSource.REQUEST_ELSE_PDP : stdEnvAttSourceFromJaxbConf;

		// Extra Attribute Providers
		final List<AbstractAttributeProvider> attProviderJaxbConfs = pdpJaxbConf.getAttributeProviders();
		final List<CloseableDesignatedAttributeProvider.DependencyAwareFactory> attProviderFactories = new ArrayList<>(attProviderJaxbConfs.size());
		for (final AbstractAttributeProvider attProviderJaxbConf : attProviderJaxbConfs)
		{
			final CloseableDesignatedAttributeProvider.DependencyAwareFactory depAwareAttrProviderModFactory = newAttributeProviderProviderFactory(attProviderJaxbConf, envProps);
			attProviderFactories.add(depAwareAttrProviderModFactory);
		}

		/*
		 * Variable processing - max Variable reference depth
		 */
		final BigInteger bigMaxVarRefDepth = pdpJaxbConf.getMaxVariableRefDepth();
		final int maxVarRefDepth;
		try
		{
			maxVarRefDepth = bigMaxVarRefDepth == null ? -1 : bigMaxVarRefDepth.intValueExact();
		}
		catch (final ArithmeticException e)
		{
			throw new IllegalArgumentException("Invalid maxVariableRefDepth: " + bigMaxVarRefDepth, e);
		}

		// Functions (only non-generic functions supported in configuration)
		final List<String> nonGenericFunctionExtensionIdentifiers = pdpJaxbConf.getFunctions();
		final Set<Function<?>> nonGenericFunctionExtensions = HashCollections.newUpdatableSet(nonGenericFunctionExtensionIdentifiers.size());
		for (final String funcId : nonGenericFunctionExtensionIdentifiers)
		{
			final Function<?> function = PdpExtensions.getExtension(Function.class, funcId);
			if (!enableXPath && isXpathBased(function))
			{
				throw new IllegalArgumentException("XPath-based function not allowed (because configuration parameter 'enableXPath' = false): " + function);
			}

			nonGenericFunctionExtensions.add(function);
		}

		/*
		 * Merge with standards if required, or use the standards as is if no extension
		 */
		final FunctionRegistry functionRegistry;
		if (pdpJaxbConf.isUseStandardFunctions())
		{
			if (!enableStdDatatypes)
			{
				throw ILLEGAL_USE_STD_FUNCTIONS_ARGUMENT_EXCEPTION;
			}

			final AttributeValueFactory<?> intValFactory = attValFactoryRegistry.getExtension(StandardDatatypes.INTEGER.getId());
			assert intValFactory != null && intValFactory.getDatatype() == StandardDatatypes.INTEGER && intValFactory instanceof StringParseableValueFactory;

			final FunctionRegistry stdRegistry = StandardFunction.getRegistry(enableXPath, (StringParseableValueFactory<IntegerValue>) intValFactory);
			if (nonGenericFunctionExtensionIdentifiers.isEmpty())
			{
				functionRegistry = stdRegistry;
			}
			else
			{
				functionRegistry = new ImmutableFunctionRegistry(HashCollections.newImmutableSet(stdRegistry.getNonGenericFunctions(), nonGenericFunctionExtensions),
						stdRegistry.getGenericFunctionFactories());
			}
		}
		else
		{
			functionRegistry = new ImmutableFunctionRegistry(nonGenericFunctionExtensions, null);
		}

		/*
		 * XACML element (Policies, etc.) parser factory
		 */
		final XmlnsFilteringParserFactory xacmlParserFactory = XacmlJaxbParsingUtils.getXacmlParserFactory(enableXPath);

		/*
		 * Strict Attribute Issuer match
		 */
		strictAttributeIssuerMatch = pdpJaxbConf.isStrictAttributeIssuerMatch();

		// Policy/Rule Combining Algorithms
		// Extensions
		final List<String> algExtensionIdentifiers = pdpJaxbConf.getCombiningAlgorithms();
		final Set<CombiningAlg<?>> algExtensions = HashCollections.newUpdatableSet(algExtensionIdentifiers.size());
		for (final String algId : algExtensionIdentifiers)
		{
			final CombiningAlg<?> alg = PdpExtensions.getExtension(CombiningAlg.class, algId);
			algExtensions.add(alg);
		}

		/*
		 * Merge with standards if required, or use the standards as is if no extension
		 */
		final CombiningAlgRegistry combiningAlgRegistry;
		if (pdpJaxbConf.isUseStandardCombiningAlgorithms())
		{
			if (algExtensions.isEmpty())
			{
				combiningAlgRegistry = StandardCombiningAlgorithm.REGISTRY;
			}
			else
			{
				combiningAlgRegistry = new ImmutableCombiningAlgRegistry(HashCollections.newImmutableSet(StandardCombiningAlgorithm.REGISTRY.getExtensions(), algExtensions));
			}
		}
		else
		{
			combiningAlgRegistry = new ImmutableCombiningAlgRegistry(algExtensions);
		}

		/*
		 * Policy Reference processing - Max PolicySet reference depth
		 */
		final BigInteger bigMaxPolicyRefDepth = pdpJaxbConf.getMaxPolicyRefDepth();
		final int maxPolicySetRefDepth;
		try
		{
			maxPolicySetRefDepth = bigMaxPolicyRefDepth == null ? -1 : bigMaxPolicyRefDepth.intValueExact();
		}
		catch (final ArithmeticException e)
		{
			throw new IllegalArgumentException("Invalid maxPolicyRefDepth: " + bigMaxPolicyRefDepth, e);
		}

		/*
		 * XACML Expression factory/parser
		 */
		xacmlExpressionFactory = new DepthLimitingExpressionFactory(attValFactoryRegistry, functionRegistry, attProviderFactories, maxVarRefDepth, enableXPath, strictAttributeIssuerMatch);

		/*
		 * Policy Reference processing - Policy-by-reference Provider
		 */
		final AbstractPolicyProvider refPolicyProviderJaxbConf = pdpJaxbConf.getRefPolicyProvider();
		final Optional<CloseableRefPolicyProvider> refPolicyProvider;
		if (refPolicyProviderJaxbConf == null)
		{
			refPolicyProvider = Optional.empty();
		}
		else
		{
			refPolicyProvider = Optional.of(newRefPolicyProvider(refPolicyProviderJaxbConf, xacmlParserFactory, maxPolicySetRefDepth, xacmlExpressionFactory, combiningAlgRegistry, envProps));
		}

		/*
		 * Root Policy Provider
		 */
		rootPolicyProvider = newRootPolicyProvider(rootPolicyProviderJaxbConf, xacmlParserFactory, xacmlExpressionFactory, combiningAlgRegistry, refPolicyProvider, envProps);

		// Decision cache
		final AbstractDecisionCache decisionCacheJaxbConf = pdpJaxbConf.getDecisionCache();
		if (decisionCacheJaxbConf == null)
		{
			decisionCache = Optional.empty();
		}
		else
		{
			final DecisionCache.Factory<AbstractDecisionCache> responseCacheStoreFactory = PdpExtensions.getDecisionCacheFactory(decisionCacheJaxbConf);
			decisionCache = Optional.of(responseCacheStoreFactory.getInstance(decisionCacheJaxbConf));
		}

		// Decision Result postprocessor
		final BigInteger clientReqErrVerbosityBigInt = pdpJaxbConf.getClientRequestErrorVerbosityLevel();
		try
		{
			this.clientReqErrVerbosityLevel = clientReqErrVerbosityBigInt == null ? 0 : clientReqErrVerbosityBigInt.intValueExact();
		}
		catch (final ArithmeticException e)
		{
			throw new IllegalArgumentException("Invalid clientRequestErrorVerbosityLevel: " + clientReqErrVerbosityBigInt, e);
		}

		final List<InOutProcChain> inoutProcChains = pdpJaxbConf.getIoProcChains();

		if (inoutProcChains.isEmpty())
		{
			this.ioProcChainsByInputType = Collections.emptyMap();
		}
		else
		{
			final Map<Class<?>, Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>>> mutableInoutProcChainsByInputType = HashCollections
					.newUpdatableMap(inoutProcChains.size());
			final Map<Class<?>, String> reqProcIdentifiersByInputType = HashCollections.newUpdatableMap(inoutProcChains.size());
			for (final InOutProcChain chain : inoutProcChains)
			{
				// Decision Result postprocessor
				final String resultPostprocId = chain.getResultPostproc();
				final DecisionResultPostprocessor<?, ?> decisionResultPostproc;
				if (resultPostprocId == null)
				{
					decisionResultPostproc = null;
				}
				else
				{
					final DecisionResultPostprocessor.Factory<?, ?> resultPostprocFactory = PdpExtensions.getExtension(DecisionResultPostprocessor.Factory.class, resultPostprocId);
					decisionResultPostproc = resultPostprocFactory.getInstance(clientReqErrVerbosityLevel);
				}

				// Decision Request preprocessor
				final String reqPreprocId = chain.getRequestPreproc();

				if (reqPreprocId == null)
				{
					throw NULL_REQPREPROC_EXCEPTION;
				}

				final DecisionRequestPreprocessor.Factory<?, ?> requestPreprocFactory = PdpExtensions.getExtension(DecisionRequestPreprocessor.Factory.class, reqPreprocId);
				final DecisionRequestPreprocessor<?, ?> decisionRequestPreproc = requestPreprocFactory.getInstance(attValFactoryRegistry, strictAttributeIssuerMatch, enableXPath,
						XmlUtils.SAXON_PROCESSOR, decisionResultPostproc == null ? Collections.emptySet() : decisionResultPostproc.getFeatures());
				if (decisionResultPostproc != null && decisionRequestPreproc.getOutputRequestType() != decisionResultPostproc.getRequestType())
				{
					throw new IllegalArgumentException("Invalid 'ioProcChain': request pre-processor's output request type (requestPreproc.getOutputRequestType() = "
							+ decisionRequestPreproc.getOutputRequestType() + ") and result post-processor's request type (resultPostproc.getRequestType() = "
							+ decisionResultPostproc.getRequestType() + ") do not match");
				}

				final Class<?> inputType = decisionRequestPreproc.getInputRequestType();
				final Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>> oldEntry = mutableInoutProcChainsByInputType.put(inputType, new AbstractMap.SimpleImmutableEntry<>(
						decisionRequestPreproc, decisionResultPostproc));
				if (oldEntry != null)
				{
					throw new IllegalArgumentException("Conflicting 'ioProcChain' (I/O processing chain) elements: request preprocessors '" + reqProcIdentifiersByInputType.get(inputType)
							+ "' in one chain and '" + reqPreprocId + " in another handle the same input type (only one 'ioProcChain', i.e. 'requestPreproc', per input type is allowed)");
				}

				reqProcIdentifiersByInputType.put(inputType, reqPreprocId);
			}

			this.ioProcChainsByInputType = ImmutableMap.copyOf(mutableInoutProcChainsByInputType);
		}

	}

	/**
	 * Create PDP instance
	 * <p>
	 * To allow using file paths relative to the parent folder of the configuration file (located at confLocation) anywhere in this configuration file (including in PDP extensions'), we define a
	 * property 'PARENT_DIR', so that the placeholder ${PARENT_DIR} can be used as prefix for file paths in the configuration file. E.g. if confLocation = 'file:///path/to/configurationfile', then
	 * ${PARENT_DIR} will be replaced by 'file:///path/to'. If confLocation is not a file on the filesystem, then ${PARENT_DIR} is undefined.
	 *
	 * @param confFile
	 *            PDP configuration file
	 * @param modelHandler
	 *            PDP configuration model handler
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confFile}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration in {@code confFile}
	 */
	public static PdpEngineConfiguration getInstance(final File confFile, final PdpModelHandler modelHandler) throws IOException, IllegalArgumentException
	{
		if (confFile == null || !confFile.exists())
		{
			// no property replacement of PARENT_DIR
			throw new IllegalArgumentException("Invalid configuration file location: No file exists at: " + confFile);
		}

		if (modelHandler == null)
		{
			throw NULL_PDP_MODEL_HANDLER_ARGUMENT_EXCEPTION;
		}

		// configuration file exists
		final Pdp pdpJaxbConf;
		try
		{
			pdpJaxbConf = modelHandler.unmarshal(new StreamSource(confFile), Pdp.class);
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Invalid PDP configuration file", e);
		}

		// Set property PARENT_DIR in environment properties for future
		// replacement in configuration strings by PDP extensions using file
		// paths
		final String propVal = confFile.getParentFile().toURI().toString();
		LOGGER.debug("Property {} = {}", EnvironmentPropertyName.PARENT_DIR, propVal);
		final EnvironmentProperties envProps = new DefaultEnvironmentProperties(Collections.singletonMap(EnvironmentPropertyName.PARENT_DIR, propVal));
		return new PdpEngineConfiguration(pdpJaxbConf, envProps);
	}

	/**
	 * Create PDP instance. Locations here can be any resource string supported by Spring ResourceLoader. More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html
	 * /resources.html.
	 * <p>
	 * To allow using file paths relative to the parent folder of the configuration file (located at confLocation) anywhere in this configuration file (including in PDP extensions'), we define a
	 * property 'PARENT_DIR', so that the placeholder ${PARENT_DIR} can be used as prefix for file paths in the configuration file. E.g. if confLocation = 'file:///path/to/configurationfile', then
	 * ${PARENT_DIR} will be replaced by 'file:///path/to'. If confLocation is not a file on the filesystem, then ${PARENT_DIR} is undefined.
	 *
	 * @param confLocation
	 *            location of PDP configuration file
	 * @param modelHandler
	 *            PDP configuration model handler
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static PdpEngineConfiguration getInstance(final String confLocation, final PdpModelHandler modelHandler) throws IOException, IllegalArgumentException
	{
		File confFile = null;
		try
		{
			confFile = ResourceUtils.getFile(confLocation);
		}
		catch (final FileNotFoundException e)
		{
			throw new IllegalArgumentException("Invalid PDP configuration location: " + confLocation, e);
		}

		return getInstance(confFile, modelHandler);
	}

	/**
	 * Create PDP instance. Locations here can be any resource string supported by Spring ResourceLoader. More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html
	 * /resources.html
	 *
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 *
	 * @param confFile
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD namespace import for each extension used in the PDP
	 *            configuration, for example:
	 *
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?>
	 * <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
	 * 	<xs:annotation>
	 * 		<xs:documentation xml:lang="en">
	 * 			Import here the schema(s) of any XSD-defined PDP extension that you want to use in a PDP configuration: attribute finders, policy finders, etc.
	 * 			Indicate only the namespace here and use the XML catalog to resolve the schema location.
	 * 		</xs:documentation>
	 * 	</xs:annotation>
	 * 	<!-- Do not specify schema locations here. Define the schema locations in the XML catalog instead (see file 'catalog.xml'). -->
	 * 	<!--  Adding TestAttributeProvider extension for example -->
	 * 	<xs:import namespace="http://authzforce.github.io/core/xmlns/test/3" />
	 * </xs:schema>
	 * 			}
	 * </pre>
	 *
	 *            In this example, the file at {@code catalogLocation} must define the schemaLocation for the imported namespace above using a line like this (for an XML-formatted catalog):
	 * 
	 *            <pre>
	 *            {@literal
	 *            <uri name="http://authzforce.github.io/core/xmlns/test/3" uri=
	 * 	"classpath:org.ow2.authzforce.core.test.xsd" />
	 *            }
	 * </pre>
	 * 
	 *            We assume that this XML type is an extension of one the PDP extension base types, 'AbstractAttributeProvider' (that extends 'AbstractPdpExtension' like all other extension base
	 *            types) in this case.
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the extension XSD specified as 'extensionXsdLocation' argument (may be null if 'extensionXsdLocation' is null)
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static PdpEngineConfiguration getInstance(final File confFile, final String catalogLocation, final String extensionXsdLocation) throws IOException, IllegalArgumentException
	{
		return getInstance(confFile, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	/**
	 * Create PDP instance. Locations here may be any resource string supported by Spring ResourceLoader. More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html
	 * /resources.html
	 *
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 *
	 * @param confLocation
	 *            location of PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD namespace import for each extension used in the PDP
	 *            configuration, for example:
	 *
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?>
	 * <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
	 * 	<xs:annotation>
	 * 		<xs:documentation xml:lang="en">
	 * 			Import here the schema(s) of any XSD-defined PDP extension that you want to use in a PDP configuration: attribute finders, policy finders, etc.
	 * 			Indicate only the namespace here and use the XML catalog to resolve the schema location.
	 * 		</xs:documentation>
	 * 	</xs:annotation>
	 * 	<!-- Do not specify schema locations here. Define the schema locations in the XML catalog instead (see file 'catalog.xml'). -->
	 * 	<!--  Adding TestAttributeProvider extension for example -->
	 * 	<xs:import namespace="http://authzforce.github.io/core/xmlns/test/3" />
	 * </xs:schema>
	 * 			}
	 * </pre>
	 *
	 *            In this example, the file at {@code catalogLocation} must define the schemaLocation for the imported namespace above using a line like this (for an XML-formatted catalog):
	 * 
	 *            <pre>
	 *            {@literal
	 *            <uri name="http://authzforce.github.io/core/xmlns/test/3" uri=
	 * 	"classpath:org.ow2.authzforce.core.test.xsd" />
	 *            }
	 * </pre>
	 * 
	 *            We assume that this XML type is an extension of one the PDP extension base types, 'AbstractAttributeProvider' (that extends 'AbstractPdpExtension' like all other extension base
	 *            types) in this case.
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the extension XSD specified as 'extensionXsdLocation' argument (may be null if 'extensionXsdLocation' is null)
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static PdpEngineConfiguration getInstance(final String confLocation, final String catalogLocation, final String extensionXsdLocation) throws IOException, IllegalArgumentException
	{
		return getInstance(confLocation, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	/**
	 * Create PDP instance.
	 *
	 * @param confLocation
	 *            location of PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd). This location may be any resource string supported by Spring ResourceLoader. For example:
	 *            classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd... More info: http://docs.spring.io/spring/docs/current/spring-framework- reference/html/resources.html
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static PdpEngineConfiguration getInstance(final String confLocation) throws IOException, IllegalArgumentException
	{
		return getInstance(confLocation, null, null);
	}

	/**
	 * Returns true iff XPath (AttributeSelectors, xpathExpression datatype and xpath functions) support is enabled. For your information, AttributeSelector and xpathExpression datatype support is
	 * marked as optional in XACML 3.0 core specification.
	 * 
	 * @return true iff XPath is supported.
	 */
	public boolean isXpathEnabled()
	{
		return this.enableXPath;
	}

	/**
	 * Returns the registry of attribute value parsers/factories
	 * 
	 * @return the registry of attribute value parsers/factories
	 */
	public AttributeValueFactoryRegistry getAttributeValueFactoryRegistry()
	{
		return this.attValFactoryRegistry;
	}

	/**
	 * Returns the XACML Expression parser/factory
	 * 
	 * @return the XACML expression factory
	 */
	public ExpressionFactory getXacmlExpressionFactory()
	{
		return xacmlExpressionFactory;
	}

	/**
	 * Returns the Root Policy Provider in charge of providing the root policy where the PDP starts evaluation
	 * 
	 * @return the Root Policy Provider
	 */
	public RootPolicyProvider getRootPolicyProvider()
	{
		return rootPolicyProvider;
	}

	/**
	 * Returns true iff strict Attribute Issuer matching is enabled, in which case we require that all AttributeDesignators set the Issuer field.
	 * <p>
	 * "Strict Attribute Issuer matching" means that an AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the
	 * case that the Issuer is not present in the Attribute Designator, but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Indeed, the XACML 3.0
	 * Attribute Evaluation section §5.29 says: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and
	 * DataType attributes alone." Therefore, if {@code strictAttributeIssuerMatch} is false, since policies may use AttributeDesignators without Issuer, if the requests are using matching Attributes
	 * but with none, one or more different Issuers, this PDP engine has to gather all the values from all the attributes with matching Category/AttributeId but with any Issuer or no Issuer.
	 * Therefore, in order to stay compliant with §5.29 and still enforce best practice, when {@code strictAttributeIssuerMatch} is true, we also require that all AttributeDesignators set the Issuer
	 * field.
	 * 
	 * @return the strictAttributeIssuerMatch
	 */
	public boolean isStrictAttributeIssuerMatchEnabled()
	{
		return strictAttributeIssuerMatch;
	}

	/**
	 * Returns the type of source for standard Environment attributes specified in §10.2.5: current-time, current-date and current-dateTime. If not defined in original PDP configuration,
	 * {@link StandardEnvironmentAttributeSource#REQUEST_ELSE_PDP} is returned by default.
	 * 
	 * @return the source type
	 */
	public StandardEnvironmentAttributeSource getStdEnvAttributeSource()
	{
		return stdEnvAttributeSource;
	}

	/**
	 * Returns the level of verbosity of the error message trace returned in case of client request errors, e.g. invalid requests. Increasing this value usually helps the clients better pinpoint the
	 * issue with their Requests. This parameter is relevant to the Result postprocessor ('resultPostproc' parameter) which is expected to enforce this verbosity level when returning Indeterminate
	 * Results due to client request errors. The Result postprocessor must return all error messages in the Java stacktrace up to the same level as this parameter's value if the stacktrace is bigger,
	 * else the full stacktrace.
	 * 
	 * @return client request error verbosity
	 */
	public int getClientRequestErrorVerbosityLevel()
	{
		return this.clientReqErrVerbosityLevel;
	}

	/**
	 * Returns the Decision Result cache that, for a given request, provides the XACML policy evaluation result from a cache, if there is a cached Result for the given request.
	 * 
	 * @return the decision result cache
	 */
	public Optional<DecisionCache> getDecisionCache()
	{
		return decisionCache;
	}

	/**
	 * Returns the processor chains that can be applied to PDP engine input/output, by input type
	 * 
	 * @return the non-null PDP engine input/ouput processor chains by input type (empty if none)
	 */
	public Map<Class<?>, Entry<DecisionRequestPreprocessor<?, ?>, DecisionResultPostprocessor<?, ?>>> getInOutProcChains()
	{
		return this.ioProcChainsByInputType;
	}

}
