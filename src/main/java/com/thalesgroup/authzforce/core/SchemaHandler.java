/**
 * Copyright (C) 2011-2014 Thales Services SAS.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.cxf.catalog.OASISCatalogManager;
import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This is mostly similar to org.apache.cxf.jaxrs.utils.schemas.SchemaHandler#createSchema(), except
 * we are using Spring DefaultResourceLoader to get Resource URLs and we don't use any Bus object.
 * We are not using CXF's SchemaHandler class directly because it is part of cxf-rt-frontend-jaxrs
 * which drags many dependencies on CXF we don't need, the full CXF JAX-RS framework actually. It
 * would make more sense if SchemaHandler was part of cxf-rt-core or other common utility package,
 * but it is not the case as of writing (December 2014).
 */
public class SchemaHandler
{

	private final static Logger LOGGER = LoggerFactory.getLogger(SchemaHandler.class);
	private static final DefaultResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

	private Schema schema;
	private String catalogLocation;

	public SchemaHandler()
	{
	}

	public void setSchemaLocations(List<String> locations)
	{
		schema = createSchema(locations, catalogLocation);
	}

	public void setCatalogLocation(String name)
	{
		this.catalogLocation = name;
	}

	public Schema getSchema()
	{
		return schema;
	}

	public static Schema createSchema(List<String> locations, String catalogLocation)
	{

		final SchemaFactory factory = SchemaFactory.newInstance(Constants.URI_2001_SCHEMA_XSD);
		try
		{
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (SAXNotRecognizedException e)
		{
			throw new RuntimeException("Error configuring the XML schema factory for secure processing", e);
		} catch (SAXNotSupportedException e)
		{
			throw new RuntimeException("Error configuring the XML schema factory for secure processing", e);
		}

		final ErrorHandler schemaErrorHandler = new SchemaParsingErrorHandler();
		factory.setErrorHandler(schemaErrorHandler);
		final Schema s;
		try
		{
			final List<Source> sources = new ArrayList<>();
			for (String loc : locations)
			{
				final List<URL> schemaURLs = new LinkedList<>();

				// if (loc.lastIndexOf(".") == -1 || loc.lastIndexOf('*') != -1) {
				// schemaURLs = ClasspathScanner.findResources(loc, "xsd");
				// } else {
				final URL url = getResourceURL(loc);
				if (url != null)
				{
					schemaURLs.add(url);
				}
				// }
				if (schemaURLs.isEmpty())
				{
					throw new IllegalArgumentException("Cannot find XML schema location: " + loc);
				}
				for (URL schemaURL : schemaURLs)
				{
					final Reader r = new BufferedReader(new InputStreamReader(schemaURL.openStream(), "UTF-8"));
					final StreamSource source = new StreamSource(r);
					source.setSystemId(schemaURL.toString());
					sources.add(source);
				}
			}
			if (sources.isEmpty())
			{
				return null;
			}

			if (catalogLocation != null)
			{
				final OASISCatalogManager catalogResolver = OASISCatalogManager.getCatalogManager(null);
				if (catalogResolver != null)
				{
					// catalogLocation = catalogLocation == null
					// ? SchemaHandler.DEFAULT_CATALOG_LOCATION : catalogLocation;
					final URL catalogURL = getResourceURL(catalogLocation);
					if (catalogURL != null)
					{
						try
						{
							catalogResolver.loadCatalog(catalogURL);
							factory.setResourceResolver(new LSResourceResolver()
							{

								@Override
								public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
								{
									try
									{
										String resolvedLocation = catalogResolver.resolveSystem(systemId);

										if (resolvedLocation == null)
										{
											resolvedLocation = catalogResolver.resolveURI(namespaceURI);
										}
										if (resolvedLocation == null)
										{
											resolvedLocation = catalogResolver.resolvePublic(publicId, systemId);
										}
										if (resolvedLocation != null)
										{
											final InputStream resourceStream = getResourceStream(resolvedLocation);
											if (resourceStream != null)
											{
												return new LSInputImpl(publicId, systemId, resourceStream);
											}
										}
									} catch (Exception ex)
									{
										// ignore
									}
									return null;
								}

							});
						} catch (IOException ex)
						{
							throw new IllegalArgumentException("Catalog " + catalogLocation + " can not be loaded", ex);
						}
					}
				}
			}
			
			s = factory.newSchema(sources.toArray(new Source[sources.size()]));
		} catch (Exception ex)
		{
			throw new IllegalArgumentException("Failed to load XML schemas: " + locations, ex);
		}
		return s;

	}

	public static URL getResourceURL(String loc) throws IOException
	{
		final Resource resource = RESOURCE_LOADER.getResource(loc);
		if (resource == null || !resource.exists())
		{
			LOGGER.warn("No resource '" + loc + "' is available");
			return null;
		}

		final URL url = resource.getURL();
		if (url == null)
		{
			LOGGER.warn("Resource " + loc + " could not be resolved to a URL");
		}

		return url;
	}

	//
	// public static URL getClasspathResourceURL(String path, Class<?> callingClass)
	// {
	// return ClassLoaderUtils.getResource(path, callingClass);
	// }
	//
	public static InputStream getResourceStream(String loc) throws Exception
	{
		final URL url = getResourceURL(loc);
		return url == null ? null : url.openStream();
	}
}
