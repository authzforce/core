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

import java.beans.ConstructorProperties;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.blogspot.illegalargumentexception.NamespaceContextMap;
import com.thalesgroup.appsec.util.Utils;
import com.thalesgroup.authzforce.pdp.model._2014._12.AttributeSelectorXPathFinder;
import com.thalesgroup.authzforce.pdp.model._2014._12.CurrentDateTimeFinder;
import com.thalesgroup.authzforce.pdp.model._2014._12.Pdps;

/**
 * PDP Engine XML configuration handler
 * 
 */
public class PdpModelHandler
{
	/**
	 * Location of PDP configuration schema
	 */
	public final static String CORE_XSD_LOCATION = "classpath:pdp.xsd";
	
	/**
	 * Default location of XML catalog to resolve imported XML schemas in {@value PdpModelHandler#CORE_XSD_LOCATION} 
	 */
	public final static String DEFAULT_CATALOG_LOCATION = "classpath:catalog.xml";
	
	private final static String[] XACML_3_0_SCHEMA_LOCATIONS = {"classpath:xml.xsd", "classpath:xacml-core-v3-schema-wd-17.xsd"};

	/**
	 * XPath to schema locations in XML schema imports
	 */
	public static final String XSD_IMPORT_SCHEMA_LOCATIONS_XPATH = "/xs:schema/xs:import/@schemaLocation";
	private final static XPathExpression W3C_XML_SCHEMA_LOCATION_XPATHEXPR;
	static
	{
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final NamespaceContext xsnsContext = new NamespaceContextMap("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xpath.setNamespaceContext(xsnsContext);
		try
		{
			W3C_XML_SCHEMA_LOCATION_XPATHEXPR = xpath.compile(XSD_IMPORT_SCHEMA_LOCATIONS_XPATH);
		} catch (XPathExpressionException e)
		{
			throw new RuntimeException("Invalid XPath to XSD import schemaLocation values: " + XSD_IMPORT_SCHEMA_LOCATIONS_XPATH, e);
		}
	}
	
	/**
	 * XACML 3.0 XML schema namespace
	 */
	private static final String XACML_3_0_XMLNS = "oasis.names.tc.xacml._3_0.core.schema.wd_17";
	
	/**
	 * XACML JAXB object factory
	 */
	public static final ObjectFactory XACML_OBJECT_FACTORY = new ObjectFactory();

	/**
	 * JAXB context for (un)marshalling from/to JAXB objects derived from XACML 3.0 schema
	 */
	public static final JAXBContext XACML_3_0_JAXB_CONTEXT;
	static
	{
		try
		{
			XACML_3_0_JAXB_CONTEXT = JAXBContext.newInstance(XACML_3_0_XMLNS, PdpModelHandler.class.getClassLoader());
		} catch (JAXBException e)
		{
			throw new RuntimeException("Error instantiating JAXB context for (un)marshalling from/to XACML 3.0 objects", e);
		}
	}
	
	/**
	 * XACML 3.0 schema
	 */
	public static final Schema XACML_3_0_SCHEMA = SchemaHandler.createSchema(Arrays.asList(XACML_3_0_SCHEMA_LOCATIONS),null);

	private final static Logger LOGGER = LoggerFactory.getLogger(PdpModelHandler.class);

	/**
	 * Supported JAXB types for root elements of XML configuration documents (e.g. files)
	 */
	private final static Class<?>[] SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPES = { Pdps.class };
	
	/**
	 * JAXB types of default extensions already provided by authzforce-core and defined in PDP core XSD.
	 */
	private final static Class<?>[] DEFAULT_EXTENSION_JAXB_TYPES = { CurrentDateTimeFinder.class, AttributeSelectorXPathFinder.class};

	private Schema confSchema;
	private final Class<?>[] extJaxbBoundClasses;
	// list of schemaLocations found in extensions XSD (must be names of
	// files at the root of the classpath)
	// private final List<String> extSchemaLocations = new ArrayList<>();
	private final JAXBContext confJaxbCtx;

	/**
	 * Load Configuration model handler. Parameters here are locations to XSD files. Locations can
	 * be any resource string supported by Spring ResourceLoader. More info:
	 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 * 
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 * 
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
	 * 
	 */
	@ConstructorProperties({ "catalogLocation", "extensionXsdLocation" })
	public PdpModelHandler(String catalogLocation, String extensionXsdLocation)
	{
		/*
		 * JAXB classes of extensions are generated separately from the extension base type XSD.
		 * Therefore no @XmlSeeAlso to link to the base type. Therefore any JAXB provider cannot
		 * (un)marshall documents using the extension base type XSD, unless it is provided with the
		 * list of the extra JAXB classes based on the new extension XSD. For instance, this is the
		 * case for JAXB providers used by REST/SOAP frameworks: Apache CXF, Metro, etc. So we
		 * create such a list for the extension schema. and we keep the list in case it is needed.
		 */
		final List<Class<?>> jaxbBoundClassList = new ArrayList<>(Arrays.asList(DEFAULT_EXTENSION_JAXB_TYPES));
		final List<String> schemaLocations;
		if (extensionXsdLocation == null)
		{
			schemaLocations = Collections.singletonList(CORE_XSD_LOCATION);
		} else
		{
			schemaLocations = Arrays.asList(extensionXsdLocation, CORE_XSD_LOCATION);
			final Document extXsDoc;
			final DocumentBuilder threadLocalDocBuilder = Utils.THREAD_LOCAL_NS_AWARE_DOC_BUILDER.get();
			try (final InputStream extXsdIn = SchemaHandler.getResourceStream(extensionXsdLocation))
			{
				if (extXsdIn == null)
				{
					throw new IllegalArgumentException("Invalid extension schema location (not found): " + extensionXsdLocation);
				}

				extXsDoc = threadLocalDocBuilder.parse(extXsdIn);
				// DEBUGGING
				// NodeList nodeList = extXsDoc.getChildNodes();
				// for(int k=0; k < nodeList.getLength(); k++) {
				// Node node = nodeList.item(k);
				// String nodeName = node.getNodeName();
				// String nodeValue = node.getNodeValue();
				// System.out.println("Node in ext schema: " + nodeName + " = " + nodeValue);
				// }
				//
			} catch (Exception e1)
			{
				throw new RuntimeException("Error parsing extension XSD given location: " + extensionXsdLocation, e1);
			} finally
			{
				threadLocalDocBuilder.reset();
			}

			// load all user-defined imported extension schemas
			// (schemaLocation must be a filename in the classpath)
			final NodeList xpathResults;
			try
			{
				xpathResults = (NodeList) W3C_XML_SCHEMA_LOCATION_XPATHEXPR.evaluate(extXsDoc, XPathConstants.NODESET);
			} catch (XPathExpressionException e)
			{
				throw new RuntimeException(String.format("Error evaluating xpath='%s' ('xs' as prefix of namespace '%s') on extension XSD: %s",
						XSD_IMPORT_SCHEMA_LOCATIONS_XPATH, XMLConstants.W3C_XML_SCHEMA_NS_URI, extXsDoc), e);
			}

			if (xpathResults == null)
			{
				LOGGER.info("No extension to load (no result for evaluation of XPath: {})", XSD_IMPORT_SCHEMA_LOCATIONS_XPATH);
			} else
			{
				LOGGER.info("{} extension(s) to load (results of evaluation of XPath: {})", xpathResults.getLength(),
						XSD_IMPORT_SCHEMA_LOCATIONS_XPATH);
				for (int i = 0; i < xpathResults.getLength(); i++)
				{
					final Node result = xpathResults.item(i);
					final String schemaLocation = result.getNodeValue();
					LOGGER.info("Loading extension model (JAXB class obtained by removing '.xsd' extension) from 'schemaLocation': {}",
							schemaLocation);
					/*
					 * The convention for extension schemas says the extension schemaLocation (XSD
					 * filename) must be the extension's fully qualified Java class (to be bound)
					 * name with '.xsd' extension. Remove .xsd file extension to get the class name.
					 */

					final String extJaxbClassName;
					try
					{
						extJaxbClassName = schemaLocation.substring(0, schemaLocation.length() - 4);
						LOGGER.info("Found extension model (JAXB class): {}", extJaxbClassName);
					} catch (IndexOutOfBoundsException e)
					{
						throw new IllegalArgumentException("Invalid schemaLocation in extension XSD: '" + schemaLocation
								+ "'. Must be ${fully_qualified_jaxb_class_name}.xsd", e);
					}
					try
					{
						jaxbBoundClassList.add(Class.forName(extJaxbClassName));
					} catch (ClassNotFoundException e)
					{
						throw new RuntimeException("Extension JAXB-annotated class '" + extJaxbClassName + "' as specified by schemaLocation='"
								+ schemaLocation + "' in extension XSD (without '.xsd' extension) not found in classpath", e);
					}
				}
			}
		}

		LOGGER.info("Final list of loaded extension models (JAXB classes): {}", jaxbBoundClassList);
		this.extJaxbBoundClasses = jaxbBoundClassList.toArray(new Class<?>[jaxbBoundClassList.size()]);

		// Classes to be bound when creating new instance of JAXB context
		jaxbBoundClassList.addAll(Arrays.asList(SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPES));
		try
		{
			confJaxbCtx = JAXBContext.newInstance(jaxbBoundClassList.toArray(new Class<?>[jaxbBoundClassList.size()]));
		} catch (JAXBException e)
		{
			throw new RuntimeException("Failed to initialize configuration unmarshaller", e);
		}

		// Load schema for validating XML configurations
		final String schemaHandlerCatalogLocation;
		if(catalogLocation == null) {
			LOGGER.info("No XML catalog location specified for PDP schema handler, using default: {}", DEFAULT_CATALOG_LOCATION);
			schemaHandlerCatalogLocation = DEFAULT_CATALOG_LOCATION;
		} else {
			LOGGER.info("XML catalog location specified for PDP schema handler: {}", catalogLocation);
			schemaHandlerCatalogLocation = catalogLocation;
		}
		
		confSchema = SchemaHandler.createSchema(schemaLocations, schemaHandlerCatalogLocation);
	}

	/**
	 * Get extension classes bound to the JAXB context
	 * 
	 * @return extension classes
	 */
	public Class<?>[] getExtensionJaxbBoundClasses()
	{
		return extJaxbBoundClasses;
	}

	/**
	 * Unmarshall object from XML source
	 * 
	 * @param src
	 *            XML source
	 * @param clazz
	 *            Class of object to be unmarshalled, must be a subclass (or the class itself) of
	 *            one of the following: {@value #SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPES}
	 * @return object of class clazz
	 * @throws JAXBException
	 */
	public <T> T unmarshal(Source src, Class<T> clazz) throws JAXBException
	{
		boolean isSupported = false;
		for (final Class<?> supportedClass : SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPES)
		{
			if (supportedClass.isAssignableFrom(clazz))
			{
				isSupported = true;
				break;
			}
		}

		if (!isSupported)
		{
			throw new UnsupportedOperationException("XML configuration unmarshalling is not supported for " + clazz
					+ "; supported JAXB types for root configuration elements are: " + Arrays.asList(SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPES));
		}

		final Unmarshaller unmarshaller = confJaxbCtx.createUnmarshaller();
		unmarshaller.setSchema(confSchema);
		final JAXBElement<T> confRootElt = unmarshaller.unmarshal(src, clazz);
		return confRootElt.getValue();
	}

	/**
	 * Saves full configuration (XML)
	 * 
	 * @param conf
	 *            configuration
	 * @param os
	 *            output stream where to save
	 * @throws JAXBException
	 *             error when marshalling the XML configuration to the output stream
	 */
	public void marshal(Pdps conf, OutputStream os) throws JAXBException
	{
		final Marshaller marshaller = confJaxbCtx.createMarshaller();
		marshaller.setSchema(confSchema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(conf, os);
	}

}
