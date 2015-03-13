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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * (Spring-like) Resource loading utils (simplified org.apache.cxf.jaxrs.utils.ResourceUtils to
 * avoid using this class directly which would require the full cxf-rt-frontend-jaxrs dependency,
 * which we want to avoid. Maybe one day this class will be moved out of the jaxrs module, into the
 * core module, this would be much better.)
 * 
 */
public class ResourceUtils
{
	private static final DefaultResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);

	/**
	 * Get resource URL from Spring-supported resource location
	 * 
	 * @param loc
	 * @return resource URL
	 * @throws IOException
	 */
	public static URL getResourceURL(String loc) throws IOException
	{
		final Resource resource = RESOURCE_LOADER.getResource(loc);
		if (resource == null || !resource.exists())
		{
			LOGGER.info("Resource '" + loc + "' null or does not exist");
			return null;
		}

		final URL url = resource.getURL();
		if (url == null)
		{
			LOGGER.info("Resource " + loc + " could not be resolved to a URL");
		}

		return url;
	}

	//
	// public static URL getClasspathResourceURL(String path, Class<?> callingClass)
	// {
	// return ClassLoaderUtils.getResource(path, callingClass);
	// }
	//
	/**
	 * Get resource stream from Spring-supported resource location
	 * 
	 * @param loc
	 * @return resource stream
	 * @throws Exception
	 */
	public static InputStream getResourceStream(String loc) throws Exception
	{
		final URL url = getResourceURL(loc);
		return url == null ? null : url.openStream();
	}

	/**
	 * Same as {@link DefaultResourceLoader#getResource(String)}
	 * @param location 
	 * @return resource handle
	 */
	public static Resource getResource(String location)
	{
		return RESOURCE_LOADER.getResource(location);
	}
}
