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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 *
 * XML schema handler that can load schema file(s) from location(s) supported by {@link ResourceUtils} using any OASIS catalog at any location supported by {@link ResourceUtils} as well.
 *
 * @version $Id: $
 */
public final class SchemaHandler
{
	private static final class XmlSchemaResourceResolver implements LSResourceResolver
	{
		private final static Logger _LOGGER = LoggerFactory.getLogger(XmlSchemaResourceResolver.class);

		private final String catalogLocation;
		private final OASISCatalogManager catalogResolver;

		private XmlSchemaResourceResolver(final String catalogLocation, final OASISCatalogManager catalogResolver)
		{
			this.catalogLocation = catalogLocation;
			this.catalogResolver = catalogResolver;
		}

		@Override
		public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId, final String baseURI)
		{
			_LOGGER.debug("resolveResource(type = {}, namespaceURI = {}, publicId = {}, systemId = {}, baseURI = {}) -> {}", type, namespaceURI, publicId, systemId, baseURI);
			try
			{
				String resolvedLocation = null;
				if (systemId != null)
				{
					resolvedLocation = catalogResolver.resolveSystem(systemId);
					_LOGGER.debug("resolveSystem(systemId = {}) -> {}", systemId, resolvedLocation);
				}

				if (resolvedLocation == null && namespaceURI != null)
				{
					resolvedLocation = catalogResolver.resolveURI(namespaceURI);
					_LOGGER.debug("resolveURI(namespaceURI = {}) -> {}", namespaceURI, resolvedLocation);
				}
				if (resolvedLocation == null && publicId != null)
				{
					resolvedLocation = catalogResolver.resolvePublic(publicId, systemId);
					_LOGGER.debug("resolvePublic(publicId = {}, systemId = {}) -> {}", publicId, systemId, resolvedLocation);
				}
				if (resolvedLocation != null)
				{
					final URL resourceURL = ResourceUtils.getURL(resolvedLocation);
					if (resourceURL != null)
					{
						return new LSInputImpl(publicId, systemId, resourceURL.openStream());
					}
				}
			}
			catch (final IOException ex)
			{
				final String errMsg = "Unable to resolve schema-required entity with XML catalog (location='" + catalogLocation + "'): type=" + type + ", namespaceURI=" + namespaceURI
						+ ", publicId='" + publicId + "', systemId='" + systemId + "', baseURI='" + baseURI + "'";
				throw new RuntimeException(errMsg, ex);
			}

			return null;
		}
	}

	private static final ErrorHandler SCHEMA_PARSING_ERROR_HANDLER = new ErrorHandler()
	{

		@Override
		public final void warning(final SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public final void error(final SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public final void fatalError(final SAXParseException exception) throws SAXException
		{
			throw exception;
		}
	};

	/**
	 * This is quite similar to org.apache.cxf.catalog.OASISCatalogManager, except it is much simplified as we don't need as many features. We are not using CXF's OASISCatalogManager class directly
	 * because it is part of cxf-core which drags many classes and dependencies on CXF we don't need. It would make more sense if OASISCatalogManager was part of a cxf common utility package, but it
	 * is not the case as of writing (December 2014).
	 * <p>
	 * WARNING: this is not immutable since getCatalog() gives access to internal catalog which is mutable.
	 * </p>
	 */
	private static final class OASISCatalogManager
	{
		private static final IllegalArgumentException ERROR_CREATING_CATALOG_RESOLVER_EXCEPTION = new IllegalArgumentException(
				"Error creating org.apache.xml.resolver.tools.CatalogResolver for OASIS CatalogManager");

		private static final Logger _LOGGER = LoggerFactory.getLogger(OASISCatalogManager.class);

		private static CatalogResolver getResolver()
		{
			try
			{
				final CatalogManager catalogManager = new CatalogManager();
				if (_LOGGER.isDebugEnabled())
				{
					// lowest debug level for logging all messages
					catalogManager.debug.setDebug(0);
				}

				catalogManager.setUseStaticCatalog(false);
				catalogManager.setIgnoreMissingProperties(true);
				final CatalogResolver catalogResolver = new CatalogResolver(catalogManager)
				{
					@Override
					public String getResolvedEntity(final String publicId, final String systemId)
					{
						final String s = super.getResolvedEntity(publicId, systemId);
						if (s != null && s.startsWith("classpath:"))
						{
							try
							{
								final URL resourceURL = ResourceUtils.getURL(s);
								if (resourceURL != null)
								{
									return resourceURL.toExternalForm();
								}
							}
							catch (final IOException e)
							{
								_LOGGER.warn("Error resolving resource needed by org.apache.xml.resolver.CatalogResolver for OASIS CatalogManager with URL: {}", e);
							}
						}
						return s;
					}
				};
				return catalogResolver;
			}
			catch (final Throwable t)
			{
				_LOGGER.error("Error getting org.apache.xml.resolver.CatalogResolver for OASIS CatalogManager", t);
			}
			return null;
		}

		private final CatalogResolver resolver;
		private final Catalog catalog;
		private final Set<String> loadedCatalogs = new CopyOnWriteArraySet<>();

		private OASISCatalogManager()
		{
			resolver = getResolver();
			if (resolver == null)
			{
				throw ERROR_CREATING_CATALOG_RESOLVER_EXCEPTION;
			}

			catalog = resolver.getCatalog();
		}

		private void loadCatalog(final URL catalogURL) throws IOException
		{
			assert catalogURL != null;
			if (!loadedCatalogs.contains(catalogURL.toString()))
			{
				if ("file".equals(catalogURL.getProtocol()))
				{
					try
					{
						final Path filePath = Paths.get(catalogURL.toURI());
						if (!Files.exists(filePath))
						{
							throw new FileNotFoundException(filePath.toString());
						}
					}
					catch (final URISyntaxException e)
					{
						_LOGGER.warn("Error resolving XML catalog URL ({}) to a file", catalogURL, e);
					}
				}

				if (catalog == null)
				{
					_LOGGER.warn("Catalog found at {} but no org.apache.xml.resolver.CatalogManager was found. Check the classpatch for an xmlresolver jar.", catalogURL);
				}
				else
				{
					catalog.parseCatalog(catalogURL);
					loadedCatalogs.add(catalogURL.toString());
				}
			}
		}

		private String resolveSystem(final String sys) throws MalformedURLException, IOException
		{
			assert sys != null;
			if (catalog == null)
			{
				return null;
			}
			return catalog.resolveSystem(sys);
		}

		private String resolveURI(final String uri) throws MalformedURLException, IOException
		{
			assert uri != null;
			if (catalog == null)
			{
				return null;
			}
			return catalog.resolveURI(uri);
		}

		private String resolvePublic(final String uri, final String parent) throws MalformedURLException, IOException
		{
			assert uri != null;
			if (resolver == null)
			{
				return null;
			}
			return catalog.resolvePublic(uri, parent);
		}
	}

	private static final class LSInputImpl implements LSInput
	{

		private static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();
		private final String publicId;
		private final String systemId;
		private final InputStream byteStream;

		private LSInputImpl(final String publicId, final String systemId, final InputStream byteStream)
		{
			this.publicId = publicId;
			this.systemId = systemId;
			this.byteStream = byteStream;
		}

		@Override
		public InputStream getByteStream()
		{
			return byteStream;
		}

		@Override
		public String getSystemId()
		{
			return systemId;
		}

		@Override
		public String getPublicId()
		{
			return publicId;
		}

		@Override
		public Reader getCharacterStream()
		{
			/*
			 * No character stream, only byte streams are allowed. Do not throw exception, otherwise the resolution of the resource fails, even if byte stream OK
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public void setCharacterStream(final Reader characterStream)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public void setByteStream(final InputStream byteStream)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public String getStringData()
		{
			/*
			 * Not supported. Do not throw exception, otherwise the resolution of the resource fails.
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public void setStringData(final String stringData)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public void setSystemId(final String systemId)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public void setPublicId(final String publicId)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public String getBaseURI()
		{
			/*
			 * No base URI, only absolute URIs are allowed. Do not throw exception if no base URI, otherwise the resolution of the resource fails, even for absolute URIs
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public void setBaseURI(final String baseURI)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public String getEncoding()
		{
			/*
			 * No encoding override, only absolute URIs are allowed. Do not throw exception if no base URI, otherwise the resolution of the resource fails, even if encoding specified in other way
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public void setEncoding(final String encoding)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public boolean getCertifiedText()
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

		@Override
		public void setCertifiedText(final boolean certifiedText)
		{
			throw UNSUPPORTED_OPERATION_EXCEPTION;
		}

	}

	private Schema schema;
	private String catalogLocation;

	/**
	 * Sets (Spring-supported) locations to XML schema files
	 *
	 * @param locations
	 *            XML schema locations
	 */
	public void setSchemaLocations(final List<String> locations)
	{
		schema = createSchema(locations, catalogLocation);
	}

	/**
	 * Sets (Spring-supported) locations to XML catalog files
	 *
	 * @param location
	 *            XML catalog location
	 */
	public void setCatalogLocation(final String location)
	{
		this.catalogLocation = location;
	}

	/**
	 * Get schema used by this handler
	 *
	 * @return XML schema
	 */
	public Schema getSchema()
	{
		return schema;
	}

	/**
	 * Creates schema from locations to XML schema files and catalog file
	 *
	 * @param schemaLocations
	 *            XML schema locations
	 * @param catalogLocation
	 *            XML catalog location
	 * @return XML validation schema
	 */
	public static Schema createSchema(final List<String> schemaLocations, final String catalogLocation)
	{
		/*
		 * This is mostly similar to org.apache.cxf.jaxrs.utils.schemas.SchemaHandler#createSchema(), except we are using Spring ResourceUtils class to get Resource URLs and we don't use any Bus
		 * object. We are not using CXF's SchemaHandler class directly because it is part of cxf-rt-frontend-jaxrs which drags many dependencies on CXF we don't need, the full CXF JAX-RS framework
		 * actually. It would make more sense if SchemaHandler was part of some cxf common utility package, but it is not the case as of writing (December 2014).
		 */

		final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try
		{
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		}
		catch (final SAXNotRecognizedException e)
		{
			throw new RuntimeException("Error configuring the XML schema factory for secure processing", e);
		}
		catch (final SAXNotSupportedException e)
		{
			throw new RuntimeException("Error configuring the XML schema factory for secure processing", e);
		}

		factory.setErrorHandler(SCHEMA_PARSING_ERROR_HANDLER);
		final List<Source> sources = new ArrayList<>(schemaLocations.size());
		try
		{
			for (final String schemaLocation : schemaLocations)
			{
				final URL schemaURL;
				try
				{
					schemaURL = ResourceUtils.getURL(schemaLocation);
				}
				catch (final FileNotFoundException e)
				{
					throw new RuntimeException("No resource found for XML schema location: " + schemaLocation, e);
				}

				final Reader r = new BufferedReader(new InputStreamReader(schemaURL.openStream(), "UTF-8"));
				final StreamSource source = new StreamSource(r);
				source.setSystemId(schemaURL.toString());
				sources.add(source);
			}
		}
		catch (final IOException ex)
		{
			throw new RuntimeException("Failed to load XML schemas: " + schemaLocations, ex);
		}

		if (sources.isEmpty())
		{
			return null;
		}

		if (catalogLocation != null)
		{
			final OASISCatalogManager catalogResolver = new OASISCatalogManager();
			final URL catalogURL;
			try
			{
				catalogURL = ResourceUtils.getURL(catalogLocation);
			}
			catch (final FileNotFoundException e)
			{
				throw new RuntimeException("No resource found for XML catalog file location: " + catalogLocation, e);
			}

			try
			{
				catalogResolver.loadCatalog(catalogURL);
				factory.setResourceResolver(new XmlSchemaResourceResolver(catalogLocation, catalogResolver));
			}
			catch (final IOException ex)
			{
				throw new RuntimeException("Catalog located at '" + catalogLocation + "' can not be loaded", ex);
			}
		}

		final Schema s;
		try
		{
			s = factory.newSchema(sources.toArray(new Source[sources.size()]));
		}
		catch (final SAXException e)
		{
			throw new RuntimeException("Failed to load XML schemas: " + schemaLocations, e);
		}

		return s;

	}
}
