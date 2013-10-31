/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.support.finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

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

	private static final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	// the finder, which is used by PolicySets (actually never used in this class!)
//	private PolicyFinder finder;

	// the builders used to create DOM documents
	/**
	 * DocumentBuilder is not thread-safe and cannot be used multiple times without calling reset().
	 * So we need to make sure a PolicyReader instance cannot be used by multiple threads, and
	 * reset() called if multiple parsing with same DocumentBuilder.
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
		this.logger = LoggerFactory.getLogger(PolicyReader.class);
//		this.finder = finder;

		// Create the Policy document builder factory
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(true);
		
		/*
		 * Create the XML schema object for validating the document if schemaFile not null. As
		 * said in Javadoc for javax.xml.parsers.DocumentBuilderFactory.setSchema(Schema
		 * schema), it is an error to use the
		 * http://java.sun.com/xml/jaxp/properties/schemaSource property and/or the
		 * http://java.sun.com/xml/jaxp/properties/schemaLanguage property in conjunction with a
		 * Schema object; and factory schema is null by default, and #isValidating() returns
		 * false, i.e. no validation.
		 */
		if (schemaFile != null)
		{

			try
			{
				final Schema schema = schemaFactory.newSchema(schemaFile);
				factory.setSchema(schema);
			} catch (SAXException e)
			{
				throw new IllegalArgumentException("Failed to load schema from file '"+schemaFile.getAbsolutePath()+"' for validating policy documents: ", e);
			}
		}

		try
		{
			

			// now use the factory to create the document builder
			builder = factory.newDocumentBuilder();
			builder.setErrorHandler(this);
		} catch (ParserConfigurationException pce)
		{
			throw new IllegalArgumentException("Failed to setup policy reader: ", pce);
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
		} finally
		{
			builder.reset();
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
	public Policy readPolicy(URL url) throws ParsingException
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
	private static Policy handleDocument(Document doc) throws ParsingException
	{
		// handle the policy, if it's a known type
		Element root = doc.getDocumentElement();
		String name = root.getTagName();

		// see what type of policy this is
		if (name.equals("Policy"))
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
	private static PolicySet handlePolicySetDocument(Document doc) throws ParsingException
	{
		// handle the policy, if it's a known type
		Element root = doc.getDocumentElement();
		String name = root.getTagName();

		// see what type of policy this is
		if (name.equals("PolicySet"))
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
