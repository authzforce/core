/*
 * Copyright 2012-2026 THALES.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/** Resolves classpath resources, URLs and file-system paths without a framework dependency. */
public final class ResourceLocationResolver
{
	public static final String CLASSPATH_URL_PREFIX = "classpath:";
	public static final String FILE_URL_PREFIX = "file:";

	private ResourceLocationResolver()
	{
		// Utility class.
	}

	public static URL getUrl(final String location) throws FileNotFoundException
	{
		if (location == null)
		{
			throw new IllegalArgumentException("Resource location must not be null");
		}

		if (location.startsWith(CLASSPATH_URL_PREFIX))
		{
			String resourcePath = location.substring(CLASSPATH_URL_PREFIX.length());
			while (resourcePath.startsWith("/"))
			{
				resourcePath = resourcePath.substring(1);
			}

			final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			final URL resourceUrl = contextClassLoader == null ? ClassLoader.getSystemResource(resourcePath) : contextClassLoader.getResource(resourcePath);
			if (resourceUrl == null)
			{
				throw new FileNotFoundException("Class path resource [" + resourcePath + "] cannot be found");
			}
			return resourceUrl;
		}

		try
		{
			return new URL(location);
		}
		catch (final MalformedURLException e)
		{
			try
			{
				return new File(location).toURI().toURL();
			}
			catch (final MalformedURLException impossible)
			{
				throw new IllegalArgumentException("Invalid resource location: " + location, impossible);
			}
		}
	}

	public static File getFile(final String location) throws FileNotFoundException
	{
		if (location == null)
		{
			throw new IllegalArgumentException("Resource location must not be null");
		}

		if (!location.startsWith(CLASSPATH_URL_PREFIX))
		{
			try
			{
				return getFile(new URL(location), location);
			}
			catch (final MalformedURLException e)
			{
				return new File(location);
			}
		}

		return getFile(getUrl(location), location);
	}

	private static File getFile(final URL resourceUrl, final String description) throws FileNotFoundException
	{
		if (!"file".equals(resourceUrl.getProtocol()))
		{
			throw new FileNotFoundException(description + " cannot be resolved to an absolute file path because it does not reside in the file system");
		}

		try
		{
			return new File(resourceUrl.toURI());
		}
		catch (final URISyntaxException e)
		{
			return new File(resourceUrl.getFile());
		}
	}
}
