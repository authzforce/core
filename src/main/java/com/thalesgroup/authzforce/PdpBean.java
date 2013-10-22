/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * 
 */
package com.thalesgroup.authzforce;

/*
 * #%L
 * Thales AuthzForce-FIWARE
 * %%
 * Copyright (C) 2012 - 2013 Thales
 * %%
 * All rights reserved.
 * #L%
 */

import java.io.File;
import java.io.IOException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.SystemPropertyUtils;

import com.sun.xacml.ConfigurationStore;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.ctx.ResponseCtx;

/**
 * JavaBean for the PDP to be used/called as JNDI resource.
 * 
 * In JEE application servers such as Glassfish, you could use class
 * org.glassfish.resources.custom.factory.JavaBeanFactory for registering the custom JNDI resource.
 * More info: http://docs.oracle.com/cd/E26576_01/doc.312/e24930/jndi.htm#giywi
 * 
 * For Tomcat, see http://tomcat.apache.org/tomcat-7.0-doc/jndi-resources-howto.html#
 * Adding_Custom_Resource_Factories.
 * 
 * @author Cyril DANGERVILLE
 * 
 */
public class PdpBean
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdpBean.class);

	private PDP pdp;

	/**
	 * @param request
	 *            XACML Request
	 * @return XACML Response
	 */
	public Response evaluate(Request request)
	{
		final ResponseCtx responseCtx = pdp.evaluate(request);
		// convert sunxacmlResp to JAXB Response type
		final Response jaxbResponse = new Response();
		jaxbResponse.getResults().addAll(responseCtx.getResults());
		return jaxbResponse;
	}

	/**
	 * Configuration file. Only the 'defaultPDP' configuration will be loaded, i.e. 'pdp' element
	 * with 'name' matching the 'defaultPDP' attribute of the root 'config' element
	 * 
	 * @param filePath
	 *            configuration file path used as argument to
	 *            {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to
	 *            resolve the resource; any placeholder ${...} in the path will be replaced with the
	 *            corresponding system property value
	 */
	public void setConfigFile(String filePath)
	{
		final ResourceLoader resLoader = new DefaultResourceLoader();
		final Resource confRes = resLoader.getResource(SystemPropertyUtils.resolvePlaceholders(filePath));
		if (!confRes.exists())
		{
			throw new IllegalArgumentException("Invalid PDP configuration file path: resource '" + confRes.getDescription() + "' does not exist");
		}

		if (!confRes.isReadable())
		{
			throw new IllegalArgumentException("Invalid PDP configuration file path: resource '" + confRes.getDescription() + "' cannot be read");
		}

		File confFile = null;
		try
		{
			confFile = confRes.getFile();
			LOGGER.info("Loading PDP configuration from file {}", confFile.getAbsolutePath());
			final ConfigurationStore confStore = new ConfigurationStore(confFile);
			final PDPConfig conf = confStore.getDefaultPDPConfig();
			pdp = new PDP(conf);
		} catch (IOException e1)
		{
			throw new IllegalArgumentException("PDP configuration resource '" + confRes.getDescription()
					+ "' cannot be resolved as a valid path on the filesystem", e1);
		} catch (ParsingException e)
		{
			throw new IllegalArgumentException("Error parsing PDP configuration from resource '" + confRes.getDescription() + "'", e);
		} catch (UnknownIdentifierException e)
		{
			throw new IllegalArgumentException("No default PDP configuration defined in resource '" + confRes.getDescription()
					+ "' (there should be one 'pdp' element with 'name' matching the 'defaultPDP' attribute of the root 'config' element)");
		}
	}

}
