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
package org.ow2.authzforce.core.pdp.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
 */
public class SchemaHandler
{
	private static final class XmlSchemaResourceResolver implements LSResourceResolver
	{
		private final static Logger _LOGGER = LoggerFactory.getLogger(XmlSchemaResourceResolver.class);

		private final String catalogLocation;
		private final OASISCatalogManager catalogResolver;

		private XmlSchemaResourceResolver(String catalogLocation, OASISCatalogManager catalogResolver)
		{
			this.catalogLocation = catalogLocation;
			this.catalogResolver = catalogResolver;
		}

		@Override
		public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
		{
			try
			{
				String resolvedLocation = catalogResolver.resolveSystem(systemId);
				_LOGGER.debug("resolveSystem(systemId = {}) -> {}", systemId, resolvedLocation);

				if (resolvedLocation == null)
				{
					resolvedLocation = catalogResolver.resolveURI(namespaceURI);
					_LOGGER.debug("resolveURI(namespaceURI = {}) -> {}", namespaceURI, resolvedLocation);
				}
				if (resolvedLocation == null)
				{
					resolvedLocation = catalogResolver.resolvePublic(publicId, systemId);
					if (_LOGGER.isDebugEnabled())
					{
						_LOGGER.debug("resolvePublic(publicId = {}, systemId = {}) -> {}", new Object[] { publicId, systemId, resolvedLocation });
					}
				}
				if (resolvedLocation != null)
				{
					final URL resourceURL = ResourceUtils.getURL(resolvedLocation);
					if (resourceURL != null)
					{
						return new LSInputImpl(publicId, systemId, resourceURL.openStream());
					}
				}
			} catch (IOException ex)
			{
				final String errMsg = "Unable to resolve schema-required entity with XML catalog (location='" + catalogLocation + "'): type=" + type + ", namespaceURI=" + namespaceURI
						+ ", publicId='" + publicId + "', systemId='" + systemId + "', baseURI='" + baseURI + "'";
				throw new RuntimeException(errMsg, ex);
			}

			return null;
		}
	}

	private static class SchemaParsingErrorHandler implements ErrorHandler
	{

		@Override
		public final void warning(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public final void error(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public final void fatalError(SAXParseException exception) throws SAXException
		{
			throw exception;
		}
	}

	/**
	 * This is quite similar to org.apache.cxf.catalog.OASISCatalogManager, except it is much simplified as we don't need as many features. We are not using CXF's OASISCatalogManager class directly
	 * because it is part of cxf-core which drags many classes and dependencies on CXF we don't need. It would make more sense if OASISCatalogManager was part of a cxf common utility package, but it
	 * is not the case as of writing (December 2014).
	 */
	private static class OASISCatalogManager
	{
		private final static Logger _LOGGER = LoggerFactory.getLogger(OASISCatalogManager.class);

		private final CatalogResolver resolver;
		private final Catalog catalog;
		private final Set<String> loadedCatalogs = new CopyOnWriteArraySet<>();

		private OASISCatalogManager()
		{
			resolver = getResolver();
			if (resolver == null)
			{
				throw new IllegalArgumentException("Error creating org.apache.xml.resolver.tools.CatalogResolver for OASIS CatalogManager");
			}

			catalog = getCatalog(resolver);
		}

		private static Catalog getCatalog(CatalogResolver resolver)
		{
			assert resolver != null;
			try
			{
				return resolver.getCatalog();
			} catch (Throwable t)
			{
				_LOGGER.error("Error getting OASIS org.apache.xml.resolver.Catalog from CatalogResolver", t);
			}

			return null;
		}

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
					public String getResolvedEntity(String publicId, String systemId)
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
							} catch (IOException e)
							{
								_LOGGER.warn("Error resolving resource needed by org.apache.xml.resolver.CatalogResolver for OASIS CatalogManager with URL: {}", e);
							}
						}
						return s;
					}
				};
				return catalogResolver;
			} catch (Throwable t)
			{
				_LOGGER.error("Error getting org.apache.xml.resolver.CatalogResolver for OASIS CatalogManager", t);
			}
			return null;
		}

		private final void loadCatalog(URL catalogURL) throws IOException
		{
			if (!loadedCatalogs.contains(catalogURL.toString()))
			{
				if ("file".equals(catalogURL.getProtocol()))
				{
					try
					{
						final File file = new File(catalogURL.toURI());
						if (!file.exists())
						{
							throw new FileNotFoundException(file.getAbsolutePath());
						}
					} catch (URISyntaxException e)
					{
						_LOGGER.warn("Error resolving XML catalog URL ({}) to a file", catalogURL, e);
					}
				}

				if (catalog == null)
				{
					_LOGGER.warn("Catalog found at {} but no org.apache.xml.resolver.CatalogManager was found. Check the classpatch for an xmlresolver jar.", catalogURL);
				} else
				{
					catalog.parseCatalog(catalogURL);

					loadedCatalogs.add(catalogURL.toString());
				}
			}
		}

		/**
		 * Creates new {@link OASISCatalogManager} with default constructor
		 * 
		 * @return new instance of {@link OASISCatalogManager} (so not null)
		 */
		private static OASISCatalogManager getContextCatalog()
		{
			return new OASISCatalogManager();
		}

		/**
		 * Creates new {@link OASISCatalogManager}
		 * 
		 * @return new instance of {@link OASISCatalogManager} (so not null)
		 */
		private static OASISCatalogManager getCatalogManager()
		{
			return getContextCatalog();
		}

		private String resolveSystem(String sys) throws MalformedURLException, IOException
		{
			if (catalog == null)
			{
				return null;
			}
			return catalog.resolveSystem(sys);
		}

		private String resolveURI(String uri) throws MalformedURLException, IOException
		{
			if (catalog == null)
			{
				return null;
			}
			return catalog.resolveURI(uri);
		}

		private String resolvePublic(String uri, String parent) throws MalformedURLException, IOException
		{
			if (resolver == null)
			{
				return null;
			}
			return catalog.resolvePublic(uri, parent);
		}
	}

	private static class LSInputImpl implements LSInput
	{

		private final String publicId;
		private final String systemId;
		private final InputStream byteStream;

		private LSInputImpl(String publicId, String systemId, InputStream byteStream)
		{
			this.publicId = publicId;
			this.systemId = systemId;
			this.byteStream = byteStream;
		}

		@Override
		public final InputStream getByteStream()
		{
			return byteStream;
		}

		@Override
		public final String getSystemId()
		{
			return systemId;
		}

		@Override
		public final String getPublicId()
		{
			return publicId;
		}

		@Override
		public final Reader getCharacterStream()
		{
			/*
			 * No character stream, only byte streams are allowed. Do not throw exception, otherwise the resolution of the resource fails, even if byte stream OK
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public final void setCharacterStream(Reader characterStream)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void setByteStream(InputStream byteStream)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final String getStringData()
		{
			/*
			 * Not supported. Do not throw exception, otherwise the resolution of the resource fails.
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public final void setStringData(String stringData)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void setSystemId(String systemId)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void setPublicId(String publicId)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final String getBaseURI()
		{
			/*
			 * No base URI, only absolute URIs are allowed. Do not throw exception if no base URI, otherwise the resolution of the resource fails, even for absolute URIs
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public final void setBaseURI(String baseURI)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final String getEncoding()
		{
			/*
			 * No encoding override, only absolute URIs are allowed. Do not throw exception if no base URI, otherwise the resolution of the resource fails, even if encoding specified in other way
			 */
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public final void setEncoding(String encoding)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final boolean getCertifiedText()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void setCertifiedText(boolean certifiedText)
		{
			throw new UnsupportedOperationException();
		}

	}

	private Schema schema;
	private String catalogLocation;

	/**
	 * Default empty constructor, needed for instantiation by Spring framework
	 */
	public SchemaHandler()
	{
	}

	/**
	 * Sets (Spring-supported) locations to XML schema files
	 * 
	 * @param locations
	 *            XML schema locations
	 */
	public void setSchemaLocations(List<String> locations)
	{
		schema = createSchema(locations, catalogLocation);
	}

	/**
	 * Sets (Spring-supported) locations to XML catalog files
	 * 
	 * @param location
	 *            XML catalog location
	 */
	public void setCatalogLocation(String location)
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
	 * 
	 * @param schemaLocations
	 *            XML schema locations
	 * @param catalogLocation
	 *            XML catalog location
	 * @return XML validation schema
	 */
	public static Schema createSchema(List<String> schemaLocations, final String catalogLocation)
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
		} catch (SAXNotRecognizedException e)
		{
			throw new RuntimeException("Error configuring the XML schema factory for secure processing", e);
		} catch (SAXNotSupportedException e)
		{
			throw new RuntimeException("Error configuring the XML schema factory for secure processing", e);
		}

		final ErrorHandler schemaErrorHandler = new SchemaParsingErrorHandler();
		factory.setErrorHandler(schemaErrorHandler);
		final List<Source> sources = new ArrayList<>(schemaLocations.size());
		try
		{
			for (String schemaLocation : schemaLocations)
			{
				final URL schemaURL;
				try
				{
					schemaURL = ResourceUtils.getURL(schemaLocation);
				} catch (FileNotFoundException e)
				{
					throw new RuntimeException("No resource found for XML schema location: " + schemaLocation, e);
				}

				final Reader r = new BufferedReader(new InputStreamReader(schemaURL.openStream(), "UTF-8"));
				final StreamSource source = new StreamSource(r);
				source.setSystemId(schemaURL.toString());
				sources.add(source);
			}
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load XML schemas: " + schemaLocations, ex);
		}

		if (sources.isEmpty())
		{
			return null;
		}

		if (catalogLocation != null)
		{
			final OASISCatalogManager catalogResolver = OASISCatalogManager.getCatalogManager();
			final URL catalogURL;
			try
			{
				catalogURL = ResourceUtils.getURL(catalogLocation);
			} catch (FileNotFoundException e)
			{
				throw new RuntimeException("No resource found for XML catalog file location: " + catalogLocation, e);
			}

			try
			{
				catalogResolver.loadCatalog(catalogURL);
				factory.setResourceResolver(new XmlSchemaResourceResolver(catalogLocation, catalogResolver));
			} catch (IOException ex)
			{
				throw new RuntimeException("Catalog located at '" + catalogLocation + "' can not be loaded", ex);
			}
		}

		final Schema s;
		try
		{
			s = factory.newSchema(sources.toArray(new Source[sources.size()]));
		} catch (SAXException e)
		{
			throw new RuntimeException("Failed to load XML schemas: " + schemaLocations, e);
		}

		return s;

	}
}
