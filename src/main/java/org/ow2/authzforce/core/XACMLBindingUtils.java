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

import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;

/**
 * Utilities/constants for XACML-to-Java binding
 * 
 */
public final class XACMLBindingUtils
{

	private final static String[] XACML_3_0_SCHEMA_LOCATIONS = { "classpath:xml.xsd", "classpath:xacml-core-v3-schema-wd-17.xsd" };

	/**
	 * Name of Java package of XACML-3.0-schema-derived classes
	 */
	private static final String XACML_3_0_JAXB_PACKAGE_NAME = "oasis.names.tc.xacml._3_0.core.schema.wd_17";

	/**
	 * XACML 3.0 schema
	 */
	private static final Schema XACML_3_0_SCHEMA = SchemaHandler.createSchema(Arrays.asList(XACML_3_0_SCHEMA_LOCATIONS), null);

	/**
	 * JAXB context for (un)marshalling from/to JAXB objects derived from XACML 3.0 schema
	 */
	public static final JAXBContext XACML_3_0_JAXB_CONTEXT;
	static
	{
		try
		{
			XACML_3_0_JAXB_CONTEXT = JAXBContext.newInstance(XACML_3_0_JAXB_PACKAGE_NAME, XACMLBindingUtils.class.getClassLoader());
		} catch (JAXBException e)
		{
			throw new RuntimeException("Error instantiating JAXB context for (un)marshalling from/to XACML 3.0 objects", e);
		}
	}

	/**
	 * XACML 3.0 JAXB ObjectFactory
	 */
	public static final ObjectFactory XACML_3_0_OBJECT_FACTORY = new ObjectFactory();

	/**
	 * Creates XACML 3.0/JAXB unmarshaller with XACML 3.0 schema validation
	 * 
	 * @return unmarshaller
	 * @throws JAXBException
	 */
	public static Unmarshaller createXacml3Unmarshaller() throws JAXBException
	{
		final Unmarshaller unmarshaller = XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
		unmarshaller.setSchema(XACML_3_0_SCHEMA);
		return unmarshaller;
	}

	/**
	 * Creates XACML 3.0/JAXB marshaller with XACML 3.0 schema validation
	 * 
	 * @return marshaller
	 * @throws JAXBException
	 */
	public static Marshaller createXacml3Marshaller() throws JAXBException
	{
		final Marshaller marshaller = XACML_3_0_JAXB_CONTEXT.createMarshaller();
		marshaller.setSchema(XACML_3_0_SCHEMA);
		return marshaller;
	}

	private XACMLBindingUtils()
	{
	}

}
