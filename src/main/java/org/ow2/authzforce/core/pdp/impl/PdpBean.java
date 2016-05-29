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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PDP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SystemPropertyUtils;

/**
 * JavaBean for the PDP to be used/called as JNDI resource.
 *
 * In JEE application servers such as Glassfish, you could use class org.glassfish.resources.custom.factory.JavaBeanFactory for registering the custom JNDI resource. More info:
 * http://docs.oracle.com/cd/E26576_01/doc.312/e24930/jndi.htm#giywi
 *
 * For Tomcat, see http://tomcat.apache.org/tomcat-7.0-doc/jndi-resources-howto.html# Adding_Custom_Resource_Factories.
 *
 * @version $Id: $
 */
public final class PdpBean implements PDP
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdpBean.class);

	private PDP pdp;

	private String confLocation = null;

	private boolean initialized = false;

	private String extSchemaLocation = null;

	private String catalogLocation = null;

	/**
	 * Configuration file. Only the 'defaultPDP' configuration will be loaded, i.e. 'pdp' element with 'name' matching the 'defaultPDP' attribute of the root 'config' element
	 *
	 * @param filePath
	 *            configuration file path used as argument to {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to resolve the resource; any placeholder ${...} in the path
	 *            will be replaced with the corresponding system property value
	 * @throws java.lang.IllegalArgumentException
	 *             if there is an unresolvable placeholder in {@code filePath}
	 */
	public void setConfigFile(String filePath) throws IllegalArgumentException
	{
		confLocation = SystemPropertyUtils.resolvePlaceholders(filePath);
		init();
	}

	/**
	 * Configuration schema file. Used only for validating XML configurations (enclosed with 'xml' tag) of PDP extension modules in PDP configuration file set with {@link #setConfigFile(String)}
	 *
	 * @param filePath
	 *            configuration file path used as argument to {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to resolve the resource; any placeholder ${...} in the path
	 *            will be replaced with the corresponding system property value
	 * @throws java.lang.IllegalArgumentException
	 *             if there is an unresolvable placeholder in {@code filePath}
	 */
	public void setSchemaFile(String filePath) throws IllegalArgumentException
	{
		extSchemaLocation = SystemPropertyUtils.resolvePlaceholders(filePath);
		init();
	}

	/**
	 * Set XML catalog for resolving XML entities used in XML schema
	 *
	 * @param filePath
	 *            configuration file path used as argument to {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to resolve the resource; any placeholder ${...} in the path
	 *            will be replaced with the corresponding system property value
	 * @throws java.lang.IllegalArgumentException
	 *             if there is an unresolvable placeholder in {@code filePath}
	 */
	public void setCatalogFile(String filePath) throws IllegalArgumentException
	{
		catalogLocation = SystemPropertyUtils.resolvePlaceholders(filePath);
		init();
	}

	private boolean init()
	{
		if (!initialized && catalogLocation != null && extSchemaLocation != null && confLocation != null)
		{
			LOGGER.info("Loading PDP configuration from file {} with extension schema location '{}' and XML catalog location '{}'", new Object[] { confLocation, extSchemaLocation, catalogLocation });
			try
			{
				pdp = PdpConfigurationParser.getPDP(confLocation, catalogLocation, extSchemaLocation);
			} catch (IOException | IllegalArgumentException e)
			{
				throw new RuntimeException("Error parsing PDP configuration from location: " + confLocation, e);
			}

			initialized = true;
		}

		return initialized;
	}

	/** {@inheritDoc} */
	@Override
	public Response evaluate(Request request)
	{
		return evaluate(request, null);
	}

	private void checkInit()
	{
		if (!initialized)
		{
			final String cause;
			if (confLocation == null)
			{
				cause = "Missing parameter: configuration file";
			} else if (extSchemaLocation == null)
			{
				cause = "Missing parameter: extension schema file";
			} else if (catalogLocation == null)
			{
				cause = "Missing parameter: XML catalog file";
			} else
			{
				cause = "Check previous errors.";
			}

			throw new RuntimeException("PDP not initialized: " + cause);
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Result> evaluate(List<? extends IndividualDecisionRequest> individualDecisionRequests)
	{
		checkInit();
		return pdp.evaluate(individualDecisionRequests);
	}

	/** {@inheritDoc} */
	@Override
	public Response evaluate(Request request, Map<String, String> namespaceURIsByPrefix)
	{
		checkInit();
		return pdp.evaluate(request, namespaceURIsByPrefix);
	}

}
