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
package com.sun.xacml.support.finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicySet;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.xacmlv3.Policy;

/**
 * This class is provided as a utility for reading policies from common, simple sources:
 * <code>InputStream</code>s, <code>File</code>s, and <code>URL</code>s. It can optionally schema
 * validate the policies.
 * <p>
 * Note: some of this functionality was previously provided in
 * <code>com.sun.xacml.finder.impl.FilePolicyModule</code>, but as of the 2.0 release, that class
 * has been removed. This new <code>PolicyReader</code> class provides much better functionality for
 * loading policies.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class PolicyReader implements ErrorHandler
{

	/**
	 * The property which is used to specify the schema file to validate against (if any). Note that
	 * this isn't used directly by <code>PolicyReader</code>, but is referenced by many classes that
	 * use this class to load policies.
	 */
	public static final String POLICY_SCHEMA_PROPERTY = "com.sun.xacml.PolicySchema";

	// the standard attribute for specifying the XML schema language
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	// the standard identifier for the XML schema specification
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	// the standard attribute for specifying schema source
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	// the finder, which is used by PolicySets
	private PolicyFinder finder;

	// the builders used to create DOM documents
	/**
	 * DocumentBuilder is not thread-safe and cannot be used multiple times without calling
	 * reset(). So we need to make sure a PolicyReader instance cannot be used by multiple threads,
	 * and reset() called if multiple parsing with same DocumentBuilder.
	 */
	private DocumentBuilder builder;

	// the optional logger used for error reporting
	private Logger logger;

	/**
	 * Creates a <code>PolicyReader</code> that does not schema-validate policies.
	 * 
	 * @param finder
	 *            a <code>PolicyFinder</code> that is used by policy sets, which may be null only if
	 *            no references are used
	 */
	public PolicyReader(PolicyFinder finder)
	{
		this(null, finder);
	}

	/**
	 * Creates a <code>PolicyReader</code> that may schema-validate policies.
	 * 
	 * @param finder
	 *            a <code>PolicyFinder</code> that is used by policy sets, which may be null only if
	 *            no references are used
	 * @param schemaFile
	 *            the schema file used to validate policies, or null if schema validation is not
	 *            desired
	 */
	public PolicyReader(File schemaFile, PolicyFinder finder)
	{
		this.logger = LoggerFactory.getLogger(PolicyReader.class.getName());
		this.finder = finder;

		// create the factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(true);

		// see if we want to schema-validate policies
		if (schemaFile == null)
		{
			factory.setValidating(false);
		} else
		{
			factory.setValidating(true);
			factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			factory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
		}

		// now use the factory to create the document builder
		try
		{
			builder = factory.newDocumentBuilder();
			builder.setErrorHandler(this);
		} catch (ParserConfigurationException pce)
		{
			throw new IllegalArgumentException("Filed to setup reader: " + pce.toString());
		}
	}

	@Deprecated
	public PolicyReader(PolicyFinder finder, Logger logger)
	{
		this(finder, logger, null);
	}

	@Deprecated
	public PolicyReader(PolicyFinder finder, Logger logger, File schemaFile)
	{
		this(schemaFile, finder);
		if (logger != null)
			this.logger = logger;
	}

	/**
	 * Tries to read an XACML policy or policy set from the given file.
	 * 
	 * @param file
	 *            the file containing the policy to read
	 * 
	 * @return a (potentially schema-validated) policy loaded from the given file
	 * 
	 * @throws ParsingException
	 *             if an error occurs while reading or parsing the policy
	 */
	public synchronized Policy readPolicy(File file) throws ParsingException
	{
		try
		{
			return handleDocument(builder.parse(file));
		} catch (IOException ioe)
		{
			throw new ParsingException("Failed to read the file", ioe);
		} catch (SAXException saxe)
		{
			throw new ParsingException("Failed to parse the file", saxe);
		}
	}

	/**
	 * Tries to read an XACML policy or policy set from the given stream.
	 * 
	 * @param input
	 *            the stream containing the policy to read
	 * 
	 * @return a (potentially schema-validated) policy loaded from the given file
	 * 
	 * @throws ParsingException
	 *             if an error occurs while reading or parsing the policy
	 */
	public synchronized Policy readPolicy(InputStream input) throws ParsingException
	{
		try
		{
			return handleDocument(builder.parse(input));
		} catch (IOException ioe)
		{
			throw new ParsingException("Failed to read the stream", ioe);
		} catch (SAXException saxe)
		{
			throw new ParsingException("Failed to parse the stream", saxe);
		}
	}

	public synchronized PolicySet readPolicySet(InputStream input) throws ParsingException
	{
		try
		{
			return handlePolicySetDocument(builder.parse(input));
		} catch (IOException ioe)
		{
			throw new ParsingException("Failed to read the stream", ioe);
		} catch (SAXException saxe)
		{
			throw new ParsingException("Failed to parse the stream", saxe);
		}
	}

	/**
	 * Tries to read an XACML policy or policy set based on the given URL. This may be any
	 * resolvable URL, like a file or http pointer.
	 * 
	 * @param url
	 *            a URL pointing to the policy to read
	 * 
	 * @return a (potentially schema-validated) policy loaded from the given file
	 * 
	 * @throws ParsingException
	 *             if an error occurs while reading or parsing the policy, or if the URL can't be
	 *             resolved
	 */
	public synchronized Policy readPolicy(URL url) throws ParsingException
	{
		try
		{
			return readPolicy(url.openStream());
		} catch (IOException ioe)
		{
			throw new ParsingException("Failed to resolve the URL: " + url.toString(), ioe);
		}
	}

	/**
	 * A private method that handles reading the policy and creates the correct kind of Policy.
	 */
	private Policy handleDocument(Document doc) throws ParsingException
	{
		// handle the policy, if it's a known type
		Element root = doc.getDocumentElement();
		String name = root.getTagName();

		// see what type of policy this is
		if (name.equals("Policy") || name.equals("PolicyType"))
		{
			return Policy.getInstance(root);
		} else
		{
			// this isn't a root type that we know how to handle
			throw new ParsingException("Unknown root document type: " + name);
		}
	}

	/**
	 * A private method that handles reading the policySet and creates the correct kind of
	 * PolicySet.
	 */
	private PolicySet handlePolicySetDocument(Document doc) throws ParsingException
	{
		// handle the policy, if it's a known type
		Element root = doc.getDocumentElement();
		String name = root.getTagName();

		// see what type of policy this is
		if (name.equals("PolicySet") || name.equals("PolicySetType"))
		{
			return PolicySet.getInstance(root);
		} else
		{
			// this isn't a root type that we know how to handle
			throw new ParsingException("Unknown root document type: " + name);
		}
	}

	/**
	 * Standard handler routine for the XML parsing.
	 * 
	 * @param exception
	 *            information on what caused the problem
	 */
	public void warning(SAXParseException exception) throws SAXException
	{
			logger.warn("Error reading policy", exception);
	}

	/**
	 * Standard handler routine for the XML parsing.
	 * 
	 * @param exception
	 *            information on what caused the problem
	 * 
	 * @throws SAXException
	 *             always to halt parsing on errors
	 */
	public void error(SAXParseException exception) throws SAXException
	{
			logger.warn("Error reading Policy ... Policy will not be available", exception);
		throw new SAXException("error parsing policy");
	}

	/**
	 * Standard handler routine for the XML parsing.
	 * 
	 * @param exception
	 *            information on what caused the problem
	 * 
	 * @throws SAXException
	 *             always to halt parsing on errors
	 */
	public void fatalError(SAXParseException exception) throws SAXException
	{
			logger.warn("Fatal error reading policy ... Policy will not be available", exception);
		
		throw new SAXException("fatal error parsing policy");
	}

	public String getType(FileInputStream input) throws ParsingException, SAXException, IOException
	{
		// handle the policy, if it's a known type
		Document doc = builder.parse(input);
		Element root = doc.getDocumentElement();
		String name = root.getTagName();
		// see what type of policy this is
		if ((name.equals("Policy") || name.equals("PolicyType")))
		{
			return "Policy";
		} else if (name.equals("PolicySet") || name.equals("PolicySetType"))
		{
			return "PolicySet";
		} else
		{
			throw new ParsingException("Unknown root document type: " + name);
		}
	}

}
