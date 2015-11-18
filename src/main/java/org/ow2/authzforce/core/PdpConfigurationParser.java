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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.ow2.authzforce.core.combining.BaseCombiningAlgRegistry;
import org.ow2.authzforce.core.combining.CombiningAlg;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.combining.StandardCombiningAlgRegistry;
import org.ow2.authzforce.core.expression.Expressions;
import org.ow2.authzforce.core.func.FirstOrderFunction;
import org.ow2.authzforce.core.func.FunctionRegistry;
import org.ow2.authzforce.core.func.FunctionSet;
import org.ow2.authzforce.core.func.StandardFunctionRegistry;
import org.ow2.authzforce.core.value.BaseDatatypeFactoryRegistry;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeFactory;
import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.value.StandardDatatypeFactoryRegistry;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.xacml.identifiers.XACMLDatatypeId;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.ResourceUtils;

import com.sun.xacml.Function;
import com.sun.xacml.PDP;

/**
 * XML-based PDP Configuration parser
 * 
 */
public class PdpConfigurationParser
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdpConfigurationParser.class);

	/**
	 * Create PDP instance.
	 * 
	 * @param confLocation
	 *            location of PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd). This location may be any resource string supported by
	 *            Spring ResourceLoader. For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd... More info:
	 *            http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
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
	 * Create PDP instance. Locations here may be any resource string supported by Spring ResourceLoader. More info:
	 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 * 
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 * 
	 * @param confLocation
	 *            location of PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * 
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD import for each
	 *            extension, where the 'schemaLocation' attribute value must be ${fully_qualidifed_jaxb_class_bound_to_extension_XML_type}.xsd, for example:
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
	 *            namespace="http://thalesgroup.com/authzforce/model/3.0/Provider/attribute/rest"
	 *            schemaLocation=
	 *            "com.thalesgroup.authzforce.model._3_0.Provider.attribute.rest.RESTfulAttributeProvider.xsd"
	 *            />
	 * 
	 *            </xs:schema>
	 * 			}
	 * </pre>
	 * 
	 *            In this example, 'com.thalesgroup.authzforce.model._3_0.Provider.attribute.rest.RESTfulAttributeFinde r ' is the JAXB-annotated class bound to
	 *            XML type 'RESTfulAttributeProvider'. We assume that this XML type is an extension of one the PDP extension base types, 'AbstractAttributeProvider'
	 *            (that extends 'AbstractPdpExtension' like all other extension base types) in this case.
	 * 
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the pdp.xsd (PDP configuration schema) and the extension XSD specified as
	 *            'extensionXsdLocation' argument (may be null)
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

	/**
	 * Create PDP instance. Locations here can be any resource string supported by Spring ResourceLoader. More info:
	 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 * 
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 * 
	 * @param confFile
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * 
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD import for each
	 *            extension, where the 'schemaLocation' attribute value must be ${fully_qualidifed_jaxb_class_bound_to_extension_XML_type}.xsd, for example:
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
	 *            namespace="http://thalesgroup.com/authzforce/model/3.0/Provider/attribute/rest"
	 *            schemaLocation=
	 *            "com.thalesgroup.authzforce.model._3_0.Provider.attribute.rest.RESTfulAttributeProvider.xsd"
	 *            />
	 * 
	 *            </xs:schema>
	 * 			}
	 * </pre>
	 * 
	 *            In this example, 'com.thalesgroup.authzforce.model._3_0.Provider.attribute.rest.RESTfulAttributeFinde r ' is the JAXB-annotated class bound to
	 *            XML type 'RESTfulAttributeProvider'. We assume that this XML type is an extension of one the PDP extension base types, 'AbstractAttributeProvider'
	 *            (that extends 'AbstractPdpExtension' like all other extension base types) in this case.
	 * 
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the pdp.xsd (PDP configuration schema) and the extension XSD specified as
	 *            'extensionXsdLocation' argument (may be null)
	 * @return PDP instance
	 * @throws IOException
	 *             I/O error reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * 
	 */
	public static PDP getPDP(File confFile, String catalogLocation, String extensionXsdLocation) throws IOException, JAXBException
	{
		return getPDP(confFile, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	private static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
	private static final String PROPERTY_PLACEHOLDER_SUFFIX = "}";
	private static final String PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR = ":";
	private static final String PARENT_DIRECTORY_PROPERTY_NAME = "PARENT_DIR";
	private static final String PARENT_DIRECTORY_PROPERTY_UNDEFINED_ERROR_MESSAGE = "Property '" + PARENT_DIRECTORY_PROPERTY_NAME
			+ "' undefined because location of PDP configuration could not be resolved to a file on the filesystem: ";

	/**
	 * @param confLocation
	 *            location of PDP configuration file
	 * @param modelHandler
	 *            PDP configuration model handler
	 * @throws IOException
	 *             I/O error occurred reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * @throws IllegalArgumentException
	 *             invalid configuration file
	 */
	private static PDP getPDP(String confLocation, PdpModelHandler modelHandler) throws IOException, JAXBException, IllegalArgumentException
	{
		/*
		 * To allow using file paths relative to the parent folder of the configuration file (located at confLocation) anywhere in this configuration file
		 * (including in PDP extensions'), we define a property 'PARENT_DIRECTORY', so that the placeholder ${PARENT_DIRECTORY} can be used as prefix for file
		 * paths in the configuration file. E.g. if confLocation = 'file:///path/to/configurationfile', then ${PARENT_DIRECTORY} will be replaced by
		 * 'file:///path/to'. If confLocation is not a file on the filesystem, then ${PARENT_DIRECTORY} is undefined.
		 */
		File confFile = null;
		try
		{
			confFile = ResourceUtils.getFile(confLocation);
		} catch (FileNotFoundException e)
		{
			throw new IllegalArgumentException(PARENT_DIRECTORY_PROPERTY_UNDEFINED_ERROR_MESSAGE + confLocation, e);
		}

		return getPDP(confFile, modelHandler);
	}

	/**
	 * @param confFile
	 *            PDP configuration file
	 * @param modelhandler
	 *            PDP configuration model handler
	 * @throws IOException
	 *             I/O error occurred reading from confLocation
	 * @throws JAXBException
	 *             Error unmarshalling to Pdps instance from confLocation
	 * @throws IllegalArgumentException
	 *             invalid configuration file
	 */
	private static PDP getPDP(File confFile, PdpModelHandler modelhandler) throws IOException, JAXBException, IllegalArgumentException
	{
		if (confFile == null || !confFile.exists())
		{
			// no property replacement of PARENT_DIRECTORY
			throw new IllegalArgumentException("Invalid configuration file location: No file exists at: " + confFile);
		}

		// configuration file exists
		// property replacement of PARENT_DIRECTORY
		final File parentDir = confFile.getParentFile();
		final Properties props = new Properties();
		final URI propVal = parentDir.toURI();
		/*
		 * Property value must be a String! Using props.put(Object,Object) is misleading here as it makes falsely believe other datatypes would work
		 */
		props.setProperty(PARENT_DIRECTORY_PROPERTY_NAME, propVal.toString());
		LOGGER.debug("Property {} = {}", PARENT_DIRECTORY_PROPERTY_NAME, propVal);
		final PropertyPlaceholderHelper propPlaceholderHelper = new PropertyPlaceholderHelper(PROPERTY_PLACEHOLDER_PREFIX, PROPERTY_PLACEHOLDER_SUFFIX,
				PROPERTY_PLACEHOLDER_DEFAULT_VALUE_SEPARATOR, false);
		final String confString = new String(FileCopyUtils.copyToByteArray(confFile), StandardCharsets.UTF_8);
		final String newConfString = propPlaceholderHelper.replacePlaceholders(confString, props);
		final Source xmlSrc = new StreamSource(new StringReader(newConfString));

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
	 * @throws IOException
	 *             if any error occurred closing already created {@link Closeable} modules (policy Providers, attribute Providers, decision cache)
	 */
	public static PDP getPDP(Pdp pdpJaxbConf) throws IllegalArgumentException, IOException
	{
		/*
		 * Initialize all parameters of ExpressionFactoryImpl: attribute datatype factories, functions, etc.
		 */

		// Attribute datatypes
		final DatatypeFactoryRegistry attributeFactory = new BaseDatatypeFactoryRegistry(
				pdpJaxbConf.isUseStandardDatatypes() ? StandardDatatypeFactoryRegistry.INSTANCE : null);
		for (final String attrDatatypeURI : pdpJaxbConf.getAttributeDatatypes())
		{
			final DatatypeFactory<?> datatypeFactory = PdpExtensionLoader.getExtension(DatatypeFactory.class, attrDatatypeURI);
			attributeFactory.addExtension(datatypeFactory);
		}

		// Functions
		/*
		 * For each function, check whether it is XPath-based (takes any XPathExpression as argument). Based on this and whether AttributeSelector evaluation is
		 * enabled, request Attributes/Content needs to be parsed specifically for XPath evaluation
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

		// Combining Algorithms
		final CombiningAlgRegistry combiningAlgRegistry = new BaseCombiningAlgRegistry(
				pdpJaxbConf.isUseStandardCombiningAlgorithms() ? StandardCombiningAlgRegistry.INSTANCE : null);
		for (final String algId : pdpJaxbConf.getCombiningAlgorithms())
		{
			final CombiningAlg<?> alg = PdpExtensionLoader.getExtension(CombiningAlg.class, algId);
			combiningAlgRegistry.addExtension(alg);
		}

		// Request preprocessor
		final String requesFilterId = pdpJaxbConf.getRequestFilter();
		final RequestFilter.Factory requestFilterFactory = requesFilterId == null ? DefaultRequestFilterFactory.INSTANCE : PdpExtensionLoader.getExtension(
				RequestFilter.Factory.class, requesFilterId);

		/*
		 * Is the request filter required to parse/prepare Attributes/Content element for XPath evaluation (AttributeSelector, XPath-based function...)?
		 */
		final boolean isContentRequiredForXPathEval = pdpJaxbConf.isEnableAttributeSelectors() || isAnyFuncXPathBased;
		final RequestFilter requestFilter = requestFilterFactory.getInstance(attributeFactory, isContentRequiredForXPathEval,
				XACMLBindingUtils.XACML_3_0_JAXB_CONTEXT, Expressions.SAXON_PROCESSOR);

		// Decision combiner
		final String resultFilterId = pdpJaxbConf.getResultFilter();
		final DecisionResultFilter decisionResultFilter = resultFilterId == null ? null : PdpExtensionLoader.getExtension(DecisionResultFilter.class,
				resultFilterId);

		// decision cache
		final AbstractDecisionCache jaxbDecisionCache = pdpJaxbConf.getDecisionCache();

		return new PDP(attributeFactory, functionRegistry, pdpJaxbConf.getAttributeProviders(), pdpJaxbConf.getMaxVariableRefDepth(),
				pdpJaxbConf.isEnableAttributeSelectors(), combiningAlgRegistry, pdpJaxbConf.getRootPolicyProvider(), pdpJaxbConf.getRefPolicyProvider(),
				pdpJaxbConf.getMaxPolicySetRefDepth(), requestFilter, decisionResultFilter, jaxbDecisionCache);
	}

	private static boolean isXpathBased(Function<?> function)
	{
		/*
		 * A function is said "XPath-based" iff it takes at least one XPathExpression parameter. Regarding higher-order function, as of now, we only provide
		 * higher-order functions defined in the XACML (3.0) Core specification, which are not XPath-based, or if a higher-order function happens to take a
		 * XPathExpression parameter, it is actually a parameter to the first-order sub-function. Plus it is not possible to add extensions that are
		 * higher-order functions in this PDP implementation. Therefore, it is enough to check first-order functions (class FirstOrderFunction) only. (Remember
		 * that such functions may be used as parameter to a higher-order function.)
		 */
		if (function instanceof FirstOrderFunction)
		{
			final List<? extends Datatype<?>> paramTypes = ((FirstOrderFunction<?>) function).getParameterTypes();
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
