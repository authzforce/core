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

import java.beans.ConstructorProperties;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final static Logger LOGGER = LoggerFactory.getLogger(PdpModelHandler.class);

	/**
	 * Supported JAXB type for root elements of XML configuration documents (e.g. files)
	 */
	public final static Class<?> SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE = Pdp.class;

	private final Schema confSchema;
	private final JAXBContext confJaxbCtx;

	/**
	 * Load Configuration model handler. Parameters here are locations to XSD files. Locations can be any resource string supported by Spring ResourceLoader.
	 * More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 * 
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 * 
	 * 
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD import for each
	 *            extension schema. Only import the namespace, do not define the actual schema location here. Use the catalog specified by the
	 *            <code>catalogLocation</code> parameter to specify the schema location. For example:
	 * 
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?> 
	 * 		  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
	 *            targetNamespace="http://thalesgroup.com/authzforce/model/3.0"
	 *            xmlns:tns="http://thalesgroup.com/authzforce/model/3.0"
	 *            elementFormDefault="qualified" attributeFormDefault="unqualified">
	 *            <xs:import
	 *            namespace="http://thalesgroup.com/authzforce/model/3.0/finder/attribute/rest" />
	 *            </xs:schema>
	 * 			}
	 * </pre>
	 * 
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the pdp.xsd (PDP configuration schema) and the extensions XSD specified as
	 *            'extensionXsdLocation' argument (may be null)
	 * 
	 */
	@ConstructorProperties({ "catalogLocation", "extensionXsdLocation" })
	public PdpModelHandler(String catalogLocation, String extensionXsdLocation)
	{
		final List<String> schemaLocations;
		if (extensionXsdLocation == null)
		{
			schemaLocations = Collections.singletonList(CORE_XSD_LOCATION);
		} else
		{
			schemaLocations = Arrays.asList(extensionXsdLocation, CORE_XSD_LOCATION);
		}

		/*
		 * JAXB classes of extensions are generated separately from the extension base type XSD. Therefore no @XmlSeeAlso to link to the base type. Therefore
		 * any JAXB provider cannot (un)marshall documents using the extension base type XSD, unless it is provided with the list of the extra JAXB classes
		 * based on the new extension XSD. For instance, this is the case for JAXB providers used by REST/SOAP frameworks: Apache CXF, Metro, etc. So we need to
		 * add to the JAXBContext all the extensions' model (JAXB-generated) classes. These have been collected by the PdpExtensionLoader.
		 */
		final Set<Class<?>> jaxbBoundClassList = new HashSet<Class<?>>(PdpExtensionLoader.getExtensionJaxbClasses());
		LOGGER.debug("Final list of loaded extension models (JAXB classes): {}", jaxbBoundClassList);

		// Classes to be bound when creating new instance of JAXB context
		jaxbBoundClassList.add(SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE);
		try
		{
			confJaxbCtx = JAXBContext.newInstance(jaxbBoundClassList.toArray(new Class<?>[jaxbBoundClassList.size()]));
			LOGGER.debug("JAXB context for PDP configuration (un)marshalling: {}", confJaxbCtx);
		} catch (JAXBException e)
		{
			throw new RuntimeException("Failed to initialize configuration unmarshaller", e);
		}

		// Load schema for validating XML configurations
		final String schemaHandlerCatalogLocation;
		if (catalogLocation == null)
		{
			LOGGER.debug("No XML catalog location specified for PDP schema handler, using default: {}", DEFAULT_CATALOG_LOCATION);
			schemaHandlerCatalogLocation = DEFAULT_CATALOG_LOCATION;
		} else
		{
			LOGGER.debug("XML catalog location specified for PDP schema handler: {}", catalogLocation);
			schemaHandlerCatalogLocation = catalogLocation;
		}

		confSchema = SchemaHandler.createSchema(schemaLocations, schemaHandlerCatalogLocation);
	}

	/**
	 * Unmarshall object from XML source
	 * 
	 * @param src
	 *            XML source
	 * @param clazz
	 *            Class of object to be unmarshalled, must be a subclass (or the class itself) of {@value #SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE}
	 * @return object of class clazz
	 * @throws JAXBException
	 */
	public <T> T unmarshal(Source src, Class<T> clazz) throws JAXBException
	{
		if (!SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE.isAssignableFrom(clazz))
		{
			throw new UnsupportedOperationException("XML configuration unmarshalling is not supported for " + clazz
					+ "; supported JAXB type for root configuration elements is: " + SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE);
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
	public void marshal(Pdp conf, OutputStream os) throws JAXBException
	{
		final Marshaller marshaller = confJaxbCtx.createMarshaller();
		marshaller.setSchema(confSchema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(conf, os);
	}

}
