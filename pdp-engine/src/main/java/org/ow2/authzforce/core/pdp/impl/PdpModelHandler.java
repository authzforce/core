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

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPdpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PDP Engine XML configuration handler
 *
 * @version $Id: $
 */
public final class PdpModelHandler
{
	/**
	 * Location of PDP configuration schema
	 */
	public final static String CORE_XSD_LOCATION = "classpath:pdp.xsd";

	/**
	 * Default location of XML catalog to resolve imported XML schemas in {@value #CORE_XSD_LOCATION}
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
	 * Load Configuration model handler. Parameters here are locations to XSD files. Locations can be any resource string supported by Spring ResourceLoader. More info:
	 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
	 *
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 *
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
	 *            location of XML catalog for resolving XSDs imported by the pdp.xsd (PDP configuration schema) and the extensions XSD specified as 'extensionXsdLocation' argument (may be null)
	 */
	@ConstructorProperties({ "catalogLocation", "extensionXsdLocation" })
	public PdpModelHandler(final String catalogLocation, final String extensionXsdLocation)
	{
		final List<String> schemaLocations;
		if (extensionXsdLocation == null)
		{
			schemaLocations = Collections.singletonList(CORE_XSD_LOCATION);
		}
		else
		{
			schemaLocations = Arrays.asList(extensionXsdLocation, CORE_XSD_LOCATION);
		}

		/*
		 * JAXB classes of extensions are generated separately from the extension base type XSD. Therefore no @XmlSeeAlso to link to the base type. Therefore any JAXB provider cannot (un)marshall
		 * documents using the extension base type XSD, unless it is provided with the list of the extra JAXB classes based on the new extension XSD. For instance, this is the case for JAXB providers
		 * used by REST/SOAP frameworks: Apache CXF, Metro, etc. So we need to add to the JAXBContext all the extensions' model (JAXB-generated) classes. These have been collected by the
		 * PdpExtensionLoader.
		 */
		final Set<Class<? extends AbstractPdpExtension>> extJaxbClasses = PdpExtensions.getExtensionJaxbClasses();
		final Set<Class<?>> jaxbBoundClassSet = HashCollections.<Class<?>> newUpdatableSet(extJaxbClasses.size() + 1);
		jaxbBoundClassSet.addAll(extJaxbClasses);
		LOGGER.debug("Final list of loaded extension models (JAXB classes): {}", jaxbBoundClassSet);

		// Classes to be bound when creating new instance of JAXB context
		jaxbBoundClassSet.add(SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE);
		try
		{
			confJaxbCtx = JAXBContext.newInstance(jaxbBoundClassSet.toArray(new Class<?>[jaxbBoundClassSet.size()]));
			LOGGER.debug("JAXB context for PDP configuration (un)marshalling: {}", confJaxbCtx);
		}
		catch (final JAXBException e)
		{
			throw new RuntimeException("Failed to initialize configuration unmarshaller", e);
		}

		// Load schema for validating XML configurations
		final String schemaHandlerCatalogLocation;
		if (catalogLocation == null)
		{
			LOGGER.debug("No XML catalog location specified for PDP schema handler, using default: {}", DEFAULT_CATALOG_LOCATION);
			schemaHandlerCatalogLocation = DEFAULT_CATALOG_LOCATION;
		}
		else
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
	 *            Class of object to be unmarshalled, must be a subclass (or the class itself) of the one defined by {@link #SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE}, i.e. {@link Pdp}
	 * @return object of class clazz
	 * @throws javax.xml.bind.JAXBException
	 *             if an error was encountered while unmarshalling the XML document in {@code src} into an instance of {@code clazz}
	 * @param <T>
	 *            a T object.
	 */
	public <T> T unmarshal(final Source src, final Class<T> clazz) throws JAXBException
	{
		if (!SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE.isAssignableFrom(clazz))
		{
			throw new UnsupportedOperationException("XML configuration unmarshalling is not supported for " + clazz + "; supported JAXB type for root configuration elements is: "
					+ SUPPORTED_ROOT_CONF_ELEMENT_JAXB_TYPE);
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
	 * @throws javax.xml.bind.JAXBException
	 *             error when marshalling the XML configuration to the output stream
	 */
	public void marshal(final Pdp conf, final OutputStream os) throws JAXBException
	{
		final Marshaller marshaller = confJaxbCtx.createMarshaller();
		marshaller.setSchema(confSchema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(conf, os);
	}

	/**
	 * Saves full configuration (XML)
	 *
	 * @param conf
	 *            configuration
	 * @param f
	 *            output file where to save
	 * @throws javax.xml.bind.JAXBException
	 *             error when marshalling the XML configuration to file
	 */
	public void marshal(final Pdp conf, final File f) throws JAXBException
	{
		final Marshaller marshaller = confJaxbCtx.createMarshaller();
		marshaller.setSchema(confSchema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(conf, f);
	}

}
