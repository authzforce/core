/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.xml.transform.ResourceSource;

import com.sun.xacml.PDP;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.cond.Function;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.thalesgroup.authz.model.ext._3.AbstractAttributeFinder;
import com.thalesgroup.authz.model.ext._3.AbstractDecisionCache;
import com.thalesgroup.authz.model.ext._3.AbstractPolicyFinder;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.BaseDatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinder;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinderImpl;
import com.thalesgroup.authzforce.core.attr.DatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.attr.StandardDatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.combining.BaseCombiningAlgRegistry;
import com.thalesgroup.authzforce.core.combining.CombiningAlgRegistry;
import com.thalesgroup.authzforce.core.combining.StandardCombiningAlgRegistry;
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;
import com.thalesgroup.authzforce.core.eval.ExpressionFactoryImpl;
import com.thalesgroup.authzforce.core.func.FirstOrderFunction;
import com.thalesgroup.authzforce.core.func.FunctionRegistry;
import com.thalesgroup.authzforce.core.func.FunctionSet;
import com.thalesgroup.authzforce.core.func.StandardFunctionRegistry;
import com.thalesgroup.authzforce.core.policy.RefPolicyFinder;
import com.thalesgroup.authzforce.core.policy.RefPolicyFinderModule;
import com.thalesgroup.authzforce.core.policy.RootPolicyFinder;
import com.thalesgroup.authzforce.core.policy.RootPolicyFinderModule;
import com.thalesgroup.authzforce.pdp.model._2015._06.Pdp;
import com.thalesgroup.authzforce.xacml.schema.XACMLDatatypeId;
import com.thalesgroup.authzforce.xacml.schema.XPATHVersion;

/**
 * XML-based PDP Configuration parser
 * 
 */
public class PdpConfigurationParser
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdpConfigurationParser.class);

	/**
	 * Saxon configuration file for AttributeSelector's XPath evaluation
	 */
	public static final String SAXON_CONFIGURATION_PATH = "classpath:saxon.xml";
	/**
	 * SAXON XML/XPath Processor
	 */
	private static final Processor SAXON_PROCESSOR;
	static
	{
		final ResourceLoader resLoader = new DefaultResourceLoader();
		final Resource saxonConfRes = resLoader.getResource(SAXON_CONFIGURATION_PATH);
		if (!saxonConfRes.exists())
		{
			throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_PATH);
		}

		final File saxonConfFile;
		try
		{
			saxonConfFile = saxonConfRes.getFile();
		} catch (IOException e)
		{
			throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_PATH, e);
		}

		try
		{
			SAXON_PROCESSOR = new Processor(new StreamSource(saxonConfFile));
		} catch (SaxonApiException e)
		{
			throw new RuntimeException("Error loading Saxon processor from configuration file at this location: " + SAXON_CONFIGURATION_PATH, e);
		}
	}

	/**
	 * Load PDP configuration handler.
	 * 
	 * @param confLocation
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * @return PDP instance
	 * 
	 * @throws IOException
	 *             I/O error reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * 
	 */
	public static PDP getPDP(String confLocation) throws IOException, JAXBException
	{
		return getPDP(confLocation, null, null);
	}

	/**
	 * Load PDP configuration handler. Locations here can be any resource string supported by Spring
	 * ResourceLoader. More info:
	 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 * 
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 * 
	 * @param confLocation
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * 
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if
	 *            exists; in such XSD, there must be a XSD import for each extension, where the
	 *            'schemaLocation' attribute value must be
	 *            ${fully_qualidifed_jaxb_class_bound_to_extension_XML_type}.xsd, for example:
	 * 
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?> 
	 * 		  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
	 *            targetNamespace="http://thalesgroup.com/authzforce/model/3.0"
	 *            xmlns:tns="http://thalesgroup.com/authzforce/model/3.0"
	 *            elementFormDefault="qualified" attributeFormDefault="unqualified">
	 * 
	 *            <xs:import
	 *            namespace="http://thalesgroup.com/authzforce/model/3.0/finder/attribute/rest"
	 *            schemaLocation=
	 *            "com.thalesgroup.authzforce.model._3_0.finder.attribute.rest.RESTfulAttributeFinder.xsd"
	 *            />
	 * 
	 *            </xs:schema>
	 * 			}
	 * </pre>
	 * 
	 *            In this example,
	 *            'com.thalesgroup.authzforce.model._3_0.finder.attribute.rest.RESTfulAttributeFinde
	 *            r ' is the JAXB-annotated class bound to XML type 'RESTfulAttributeFinder'. We
	 *            assume that this XML type is an extension of one the PDP extension base types,
	 *            'AbstractAttributeFinder' (that extends 'AbstractPdpExtension' like all other
	 *            extension base types) in this case.
	 * 
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the pdp.xsd (PDP
	 *            configuration schema) and the extension XSD specified as 'extensionXsdLocation'
	 *            argument (may be null)
	 * @return PDP instance
	 * @throws IOException
	 *             I/O error reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * 
	 */
	public static PDP getPDP(String confLocation, String catalogLocation, String extensionXsdLocation) throws IOException, JAXBException
	{
		return getPDP(confLocation, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	private static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
	private static final String PROPERTY_PLACEHOLDER_SUFFIX = "}";
	private static final String PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR = ":";
	private static final String PARENT_DIRECTORY_PROPERTY_NAME = "PARENT_DIR";

	private static XPathCompiler newXPathCompiler(XPATHVersion xpathVersion)
	{
		final XPathCompiler xpathCompiler = SAXON_PROCESSOR.newXPathCompiler();
		final String versionString;
		switch (xpathVersion)
		{
			case V1_0:
				versionString = "1.0";
				break;
			case V2_0:
				versionString = "2.0";
				break;
			default:
				throw new UnsupportedOperationException("Unsupported XPath version: " + xpathVersion + ". Versions supported: " + Arrays.asList(XPATHVersion.values()));

		}

		xpathCompiler.setLanguageVersion(versionString);
		xpathCompiler.setSchemaAware(false);

		/*
		 * TODO: we could enable caching of XPATH compiled queries but only once we have implemented
		 * a way to clear the cache periodically, otherwise it grows indefinitely.
		 */
		// xpathCompiler.setCaching(true);
		return xpathCompiler;
	}

	/**
	 * @param confLocation
	 *            location of PDP configuration file
	 * @param modelhandler
	 *            PDP configuration model handler
	 * @throws IOException
	 *             I/O error occurred reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * @throws IllegalArgumentException
	 *             invalid configuration file
	 */
	private static PDP getPDP(String confLocation, PdpModelHandler modelhandler) throws IOException, JAXBException, IllegalArgumentException
	{
		final Resource confResource = ResourceUtils.getResource(confLocation);
		if (confResource == null || !confResource.exists())
		{
			throw new IllegalArgumentException("No resource available at this location: " + confLocation);
		}

		/*
		 * To allow using file paths relative to the parent folder of the configuration file
		 * (located at confLocation) anywhere in this configuration file (including in PDP
		 * extensions'), we define a property 'PARENT_DIRECTORY', so that the placeholder
		 * ${PARENT_DIRECTORY} can be used as prefix for file paths in the configuration file. E.g.
		 * if confLocation = 'file:///path/to/configurationfile', then ${PARENT_DIRECTORY} will be
		 * replaced by 'file:///path/to'. If confLocation is not a file on the filesystem, then
		 * ${PARENT_DIRECTORY} is undefined.
		 */
		File confFile = null;
		try
		{
			confFile = confResource.getFile();
		} catch (IOException e)
		{
			LOGGER.warn("Property '" + PARENT_DIRECTORY_PROPERTY_NAME + " undefined because location of PDP configuration could not be resolved to a file on the filesystem");
		}

		final Source xmlSrc;
		if (confFile == null)
		{
			// no property replacement of PARENT_DIRECTORY
			try
			{
				xmlSrc = new ResourceSource(confResource);
			} catch (IOException e)
			{
				throw new IOException("Error reading from configuration location: " + confLocation, e);
			}
		} else
		{
			// property replacement of PARENT_DIRECTORY
			final File parentDir = confFile.getParentFile();
			final Properties props = new Properties();
			final URI propVal = parentDir.toURI();
			/*
			 * Property value must be a String! Using props.put(Object,Object) is misleading here as
			 * it makes falsely believe other datatypes would work
			 */
			props.setProperty(PARENT_DIRECTORY_PROPERTY_NAME, propVal.toString());
			LOGGER.debug("Property {} = {}", PARENT_DIRECTORY_PROPERTY_NAME, propVal);
			final PropertyPlaceholderHelper propPlaceholderHelper = new PropertyPlaceholderHelper(PROPERTY_PLACEHOLDER_PREFIX, PROPERTY_PLACEHOLDER_SUFFIX, PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR, false);
			final String confString = new String(FileCopyUtils.copyToByteArray(confFile), StandardCharsets.UTF_8);
			final String newConfString = propPlaceholderHelper.replacePlaceholders(confString, props);
			xmlSrc = new StreamSource(new StringReader(newConfString));
		}

		final Pdp pdpJaxbConf = modelhandler.unmarshal(xmlSrc, Pdp.class);
		return getPDP(pdpJaxbConf);
	}

	/**
	 * Get PDP instance
	 * 
	 * @param pdpJaxbConf
	 *            (JAXB-bound) PDP configuration
	 * @return PDP instance
	 * @throws IllegalArgumentException
	 *             invalid PDP configuration
	 */
	public static PDP getPDP(Pdp pdpJaxbConf) throws IllegalArgumentException
	{
		/*
		 * Initialize all parameters of ExpressionFactoryImpl: attribute datatype factories,
		 * functions, etc.
		 */

		// Attribute datatypes
		final DatatypeFactoryRegistry attributeFactory = new BaseDatatypeFactoryRegistry(pdpJaxbConf.isUseStandardDatatypes() ? StandardDatatypeFactoryRegistry.INSTANCE : null);
		for (final String attrDatatypeURI : pdpJaxbConf.getAttributeDatatypes())
		{
			final AttributeValue.Factory<?> datatypeFactory = PdpExtensionLoader.getExtension(AttributeValue.Factory.class, attrDatatypeURI);
			attributeFactory.addExtension(datatypeFactory);
		}

		// Functions
		/*
		 * For each function, check whether it is XPath-based (takes any XPathExpression as
		 * argument). Based on this and whether AttributeSelector evaluation is enabled, request
		 * Attributes/Content needs to be parsed specifically for XPath evaluation
		 */
		boolean isAnyFuncXPathBased = false;
		final FunctionRegistry functionRegistry = new FunctionRegistry(pdpJaxbConf.isUseStandardFunctions() ? StandardFunctionRegistry.INSTANCE : null);
		for (final String funcId : pdpJaxbConf.getFunctions())
		{
			final Function<?> function = PdpExtensionLoader.getExtension(Function.class, funcId);
			isAnyFuncXPathBased = isXpathBased(function);
			functionRegistry.addFunction(function);
		}

		for (final String funcSetId : pdpJaxbConf.getFunctionSets())
		{
			final FunctionSet functionSet = PdpExtensionLoader.getExtension(FunctionSet.class, funcSetId);
			for (final Function<?> function : functionSet.getSupportedFunctions())
			{
				isAnyFuncXPathBased = isXpathBased(function);
				functionRegistry.addFunction(function);
			}
		}

		// Attribute finder modules
		final Map<AttributeGUID, AttributeFinderModule> attrFinderModsByAttrId = new HashMap<>();
		for (final AbstractAttributeFinder jaxbAttrFinder : pdpJaxbConf.getAttributeFinders())
		{
			// FIXME: unchecked call
			final AttributeFinderModule.Factory<AbstractAttributeFinder> attrFinderModBuilder = PdpExtensionLoader.getJaxbBoundExtension(AttributeFinderModule.Factory.class, jaxbAttrFinder.getClass());
			final AttributeFinderModule.DependencyAwareFactory<AbstractAttributeFinder> depAwareAttrFinderModBuilder = attrFinderModBuilder.parseDependencies(jaxbAttrFinder);
			final Set<AttributeDesignatorType> requiredAttrs = depAwareAttrFinderModBuilder.getDependencies();
			final Map<AttributeGUID, AttributeFinderModule> requiredAttrFinderModsByAttrId = new HashMap<>();
			if (requiredAttrs != null)
			{
				for (final AttributeDesignatorType requiredAttr : requiredAttrs)
				{
					final AttributeGUID requiredAttrGUID = new AttributeGUID(requiredAttr);
					final AttributeFinderModule requiredAttrFinderMod = attrFinderModsByAttrId.get(requiredAttrGUID);
					// requiredAttrFinderMod = null means it should be provided by the request
					// context (in the initial request from PEP)
					if (requiredAttrFinderMod != null)
					{
						requiredAttrFinderModsByAttrId.put(requiredAttrGUID, requiredAttrFinderMod);
					}
				}
			}

			/*
			 * Each AttributeFinderModule is given a read-only AttributeFinder to find any attribute
			 * they require (dependency), based on the attribute finder modules that provide these
			 * required attributes (set above); read-only so that modules use this attribute finder
			 * only to get required attributes, nothing else.
			 */
			final AttributeFinder depAttrFinder = new CloseableAttributeFinderImpl(requiredAttrFinderModsByAttrId).getReadOnlyView();
			final AttributeFinderModule attrFinderMod = depAwareAttrFinderModBuilder.getInstance(attributeFactory, depAttrFinder);
			final Set<AttributeDesignatorType> providedAttributes = attrFinderMod.getProvidedAttributes();
			if (providedAttributes == null || providedAttributes.isEmpty())
			{
				throw new IllegalArgumentException("Invalid AttributeFinder[ID=" + attrFinderMod.getInstanceID() + "]: list of supported AttributeDesignators is null or empty");
			}

			for (final AttributeDesignatorType attrDesignator : providedAttributes)
			{
				final AttributeGUID attrGUID = new AttributeGUID(attrDesignator);
				final AttributeFinderModule conflictingMod = attrFinderModsByAttrId.put(attrGUID, attrFinderMod);
				if (conflictingMod != null)
				{
					throw new IllegalArgumentException("Conflict: AttributeFinder modules #" + conflictingMod.getInstanceID() + " and #" + attrFinderMod.getInstanceID() + " provide the same AttributeDesignator" + attrGUID);
				}
			}
		}

		// finally create the root/top-level attribute finder used to resolve AttributeDesignators
		final CloseableAttributeFinder rootAttrFinder = new CloseableAttributeFinderImpl(attrFinderModsByAttrId);

		// Initialize ExpressionFactoryImpl
		final Map<String, XPathCompiler> xpathCompilersByVersionURI = new HashMap<>();
		// XPATH 1.0 compiler
		xpathCompilersByVersionURI.put(XPATHVersion.V1_0.getURI(), newXPathCompiler(XPATHVersion.V1_0));
		// XPATH 2.0 compiler
		xpathCompilersByVersionURI.put(XPATHVersion.V2_0.getURI(), newXPathCompiler(XPATHVersion.V2_0));

		final ExpressionFactoryImpl expressionFactoryImpl = new ExpressionFactoryImpl(attributeFactory, functionRegistry, rootAttrFinder, pdpJaxbConf.getMaxVariableRefDepth(), pdpJaxbConf.isEnableAttributeSelectors(), xpathCompilersByVersionURI);

		// Combining Algorithms
		final CombiningAlgRegistry combiningAlgRegistry = new BaseCombiningAlgRegistry(pdpJaxbConf.isUseStandardCombiningAlgorithms() ? StandardCombiningAlgRegistry.INSTANCE : null);
		for (final String algId : pdpJaxbConf.getCombiningAlgorithms())
		{
			final CombiningAlgorithm<? extends Decidable> alg = PdpExtensionLoader.getExtension(CombiningAlgorithm.class, algId);
			combiningAlgRegistry.addExtension(alg);
		}

		// policy finder modules
		final AbstractPolicyFinder jaxbRootPolicyFinder = pdpJaxbConf.getRootPolicyFinder();
		final RootPolicyFinderModule.Factory<AbstractPolicyFinder> rootPolicyFinderModFactory = PdpExtensionLoader.<AbstractPolicyFinder, RootPolicyFinderModule.Factory> getJaxbBoundExtension(RootPolicyFinderModule.Factory.class, jaxbRootPolicyFinder.getClass());

		final AbstractPolicyFinder jaxbRefPolicyFinder = pdpJaxbConf.getRefPolicyFinder();
		final RefPolicyFinder refPolicyFinder;
		final RootPolicyFinderModule rootPolicyFinderMod;
		if (jaxbRefPolicyFinder == null)
		{
			refPolicyFinder = null;
		} else
		{
			final RefPolicyFinderModule.Factory<AbstractPolicyFinder> refPolicyFinderModFactory = PdpExtensionLoader.<AbstractPolicyFinder, RefPolicyFinderModule.Factory> getJaxbBoundExtension(RefPolicyFinderModule.Factory.class, jaxbRefPolicyFinder.getClass());
			final int maxPolicySetRefDepth = pdpJaxbConf.getMaxPolicySetRefDepth();
			final RefPolicyFinderModule refPolicyFinderMod = refPolicyFinderModFactory.getInstance(jaxbRefPolicyFinder, maxPolicySetRefDepth, expressionFactoryImpl, combiningAlgRegistry);
			refPolicyFinder = new RefPolicyFinder(refPolicyFinderMod, maxPolicySetRefDepth);
		}

		rootPolicyFinderMod = rootPolicyFinderModFactory.getInstance(jaxbRootPolicyFinder, expressionFactoryImpl, combiningAlgRegistry, refPolicyFinder);
		final RootPolicyFinder rootPolicyFinder = RootPolicyFinder.getInstance(rootPolicyFinderMod);

		// Request preprocessor
		final String requesFilterId = pdpJaxbConf.getRequestFilter();
		final RequestFilter.Factory requestFilterFactory;
		if (requesFilterId == null)
		{
			requestFilterFactory = DefaultRequestFilter.FACTORY;
		} else
		{
			requestFilterFactory = PdpExtensionLoader.getExtension(RequestFilter.Factory.class, requesFilterId);
		}

		/*
		 * Is the request filter required to parse/prepare Attributes/Content element for XPath
		 * evaluation (AttributeSelector, XPath-based function...)?
		 */
		final boolean isContentRequiredForXPathEval = pdpJaxbConf.isEnableAttributeSelectors() || isAnyFuncXPathBased;
		final RequestFilter requestFilter = requestFilterFactory.getInstance(attributeFactory, isContentRequiredForXPathEval, XACMLBindingUtils.XACML_3_0_JAXB_CONTEXT, SAXON_PROCESSOR);

		// Decision combiner
		final String resultFilterId = pdpJaxbConf.getResultFilter();
		final DecisionResultFilter decisionResultFilter;
		if (resultFilterId == null)
		{
			decisionResultFilter = null;
		} else
		{
			decisionResultFilter = PdpExtensionLoader.getExtension(DecisionResultFilter.class, resultFilterId);
		}

		// responseCacheStore and cache key generator
		final AbstractDecisionCache jaxbDecisionCache = pdpJaxbConf.getDecisionCache();
		final DecisionCache decisionCache;
		if (jaxbDecisionCache == null)
		{
			decisionCache = null;
		} else
		{
			final DecisionCache.Factory<AbstractDecisionCache> responseCacheStoreFactory = PdpExtensionLoader.<AbstractDecisionCache, DecisionCache.Factory> getJaxbBoundExtension(DecisionCache.Factory.class, jaxbDecisionCache.getClass());
			decisionCache = responseCacheStoreFactory.getInstance(jaxbDecisionCache);
		}

		return new PDP(rootPolicyFinder, requestFilter, decisionResultFilter, decisionCache);
	}

	private static boolean isXpathBased(Function<?> function)
	{
		/*
		 * A function is said "XPath-based" iff it takes at least one XPathExpression parameter.
		 * Regarding higher-order function, as of now, we only provide higher-order functions
		 * defined in the XACML (3.0) Core specification, which are not XPath-based, or if a
		 * higher-order function happens to take a XPathExpression parameter, it is actually a
		 * parameter to the first-order sub-function. Plus it is not possible to add extensions that
		 * are higher-order functions in this PDP implementation. Therefore, it is enough to check
		 * first-order functions (class FirstOrderFunction) only. (Remember that such functions may
		 * be used as parameter to a higher-order function.)
		 */
		if (function instanceof FirstOrderFunction)
		{
			final Datatype<?>[] paramTypes = ((FirstOrderFunction<?>) function).getParameterTypes();
			for (final Datatype<?> paramType : paramTypes)
			{
				if (paramType.getId().equals(XACMLDatatypeId.XPATH_EXPRESSION.value()))
				{
					return true;
				}
			}
		}

		return false;
	}
}
