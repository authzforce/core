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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.PolicySet;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.VersionConstraints;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.xacmlv3.IPolicy;
import com.sun.xacml.xacmlv3.Policy;
import com.thalesgroup.authzforce.BindingUtility;

/**
 * This is a simple implementation of <code>PolicyFinderModule</code> that supports retrieval based
 * on reference, and is designed for use with a run-time configuration. Its constructor accepts a
 * <code>List</code> of <code>String</code>s that represent URLs or files, and these are resolved to
 * policies when the module is initialized. Beyond this, there is no modifying or re-loading the
 * policies represented by this class. The policy's identifiers are used for reference resolution.
 * <p>
 * Note that this class is designed to complement <code>StaticPolicyFinderModule</code>. It would be
 * easy to support both kinds of policy retrieval in a single class, but the functionality is
 * instead split between two classes. The reason is that when you define a configuration for your
 * PDP, it's easier to specify the two sets of policies by using two different finder modules.
 * Typically, there aren't many policies that exist in both sets, so loading the sets separately
 * isn't a problem. If this is a concern to you, simply create your own class and merge the two
 * existing classes.
 * <p>
 * This module is provided as an example, but is still fully functional, and should be useful for
 * many simple applications. This is provided in the <code>support</code> package rather than the
 * core codebase because it implements non-standard behavior.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class StaticRefPolicyFinderModule extends PolicyFinderModule
{

	// the list of policy URLs passed to the constructor
	private List<String> policyLocationList;

	// the map of policies
	private PolicyCollection policies;

	// the optional schema 
	private Schema schema = null;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(StaticRefPolicyFinderModule.class);

	/**
	 * Creates a <code>StaticRefPolicyFinderModule</code> that provides access to the given
	 * collection of policies. Any policy that cannot be loaded will be noted in the log, but will
	 * not cause an error. The schema file used to validate policies is defined by the property
	 * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved property is null, then no
	 * schema validation will occur.
	 * 
	 * @param policyLocations
	 *            a <code>List</code> of <code>String</code>s that represent URLs or files pointing
	 *            to XACML policies
	 */
	public StaticRefPolicyFinderModule(List<String> policyLocations)
	{
		this.policyLocationList = policyLocations;
		this.policies = new PolicyCollection();

		final String schemaFilename = System.getProperty(PolicyReader.POLICY_SCHEMA_PROPERTY);
		if (schemaFilename == null)
		{
			schema = null;
		} else
		{
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try
			{
				schema = schemaFactory.newSchema(new File(schemaFilename));
			} catch (SAXException e)
			{
				throw new IllegalArgumentException("Unable to load policy validation schema from file defined by system property '"
						+ PolicyReader.POLICY_SCHEMA_PROPERTY + "': '" + schemaFilename + "'", e);
			}
		}
	}

	/**
	 * Creates a <code>StaticRefPolicyFinderModule</code> that provides access to the given
	 * collection of policyLocations.
	 * 
	 * @param policyLocations
	 *            a <code>List</code> of <code>String</code>s that represent URLs or files pointing
	 *            to XACML policies
	 * @param schemaFilename
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired
	 */
	public StaticRefPolicyFinderModule(List<String> policyLocations, String schemaFilename)
	{
		this.policyLocationList = policyLocations;
		this.policies = new PolicyCollection();

		if (schemaFilename == null)
		{
			schema = null;
		} else
		{
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try
			{
				schema = schemaFactory.newSchema(new File(schemaFilename));
			} catch (SAXException e)
			{
				throw new IllegalArgumentException("Unable to load policy validation schema from file: '" + schemaFilename + "'", e);
			}
		}
	}

	/**
	 * Always returns <code>true</code> since this module does support finding policies based on
	 * reference.
	 * 
	 * @return true
	 */
	public boolean isIdReferenceSupported()
	{
		return true;
	}

	/**
	 * Initialize this module. Typically this is called by <code>PolicyFinder</code> when a PDP is
	 * created. This method is where the policies are actually loaded.
	 * 
	 * @param finder
	 *            the <code>PolicyFinder</code> using this module
	 */
	public void init(PolicyFinder finder)
	{
		// now that we have the PolicyFinder, we can load the policies
		// PolicyReader reader = new PolicyReader(finder, LOGGER, schemaFile);
		final File baseDir = finder.getBaseDirectory();
		for (final String policyLocation : policyLocationList)
		{
			Object jaxbObj;
			final Unmarshaller unmarshaller;
			try
			{
				unmarshaller = BindingUtility.XACML3_0_JAXB_CONTEXT.createUnmarshaller();
			}
				catch (JAXBException e1)
				{
					throw new IllegalArgumentException("Failed to create JAXB marshaller for unmarshalling Policy XML document", e1);
				}
			
				unmarshaller.setSchema(schema);
			try {
				// first try to load it as a URL
				final URL url = new URL(policyLocation);
				jaxbObj = unmarshaller.unmarshal(url);
			} catch (MalformedURLException murle)
			{
				// assume that this is a filename, and try again
				final File file = new File(policyLocation);
				final File policyFile;
				if (!file.isAbsolute() && baseDir != null)
				{
					policyFile = new File(baseDir, policyLocation);
				} else
				{
					policyFile = file;
				}
				
				try
				{
					jaxbObj = unmarshaller.unmarshal(policyFile);
				} catch (JAXBException e)
				{
					throw new IllegalArgumentException("Failed to unmarshall Policy XML document from policy location: " + policyLocation, e);
				}
			} catch (JAXBException e1)
			{
				throw new IllegalArgumentException("Failed to unmarshall Policy XML document from policy location: " + policyLocation, e1);
			}

			final IPolicy policyInstance;
			if (jaxbObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy)
			{
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy policyElement = (oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) jaxbObj;
				try
				{
					policyInstance = Policy.getInstance(policyElement);
				} catch (ParsingException|UnknownIdentifierException e)
				{
					throw new IllegalArgumentException("Error parsing Policy: " + policyElement.getPolicyId(), e);
				} 
			} else if (jaxbObj instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
			{
				final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet policySetElement = (oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) jaxbObj;
				try
				{
					policyInstance = PolicySet.getInstance(policySetElement, finder);
				} catch (UnknownIdentifierException e)
				{
					throw new IllegalArgumentException("Unknown policy combining algorithm ID", e);
				} catch (ParsingException e)
				{
					throw new IllegalArgumentException("Error parsing PolicySet: " + policySetElement.getPolicySetId(), e);
				}
			} else
			{
				throw new IllegalArgumentException("Unexpected element found as root of the policy document: " + jaxbObj.getClass().getSimpleName());
			}

			// we loaded the policy, so try putting it in the collection
			if (!policies.addPolicy(policyInstance))
			{
				LOGGER.warn("Tried to load the same policy multiple times: {}", policyLocation);
			}
		}
	}

	/**
	 * Attempts to find a policy by reference, based on the provided parameters.
	 * 
	 * @param idReference
	 *            an identifier specifying some policy
	 * @param type
	 *            type of reference (policy or policySet) as identified by the fields in
	 *            <code>PolicyReference</code>
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy (this will never
	 *            be null, but it may impose no constraints, and in fact will never impose
	 *            constraints when used from a pre-2.0 XACML policy)
	 * @param parentMetaData
	 *            the meta-data from the parent policy, which provides XACML version, factories,
	 *            etc.
	 * 
	 * @return the result of looking for a matching policy
	 */
	public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints, PolicyMetaData parentMetaData)
	{
		final IPolicy policyInstance = policies.getPolicy(idReference.toString(), type, constraints);

		if (policyInstance == null)
		{
			return new PolicyFinderResult();
		}

		return new PolicyFinderResult(policyInstance);
	}

}
