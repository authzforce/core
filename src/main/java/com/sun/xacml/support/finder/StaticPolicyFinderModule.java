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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicySet;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.xacmlv3.IPolicy;
import com.sun.xacml.xacmlv3.Policy;
import com.thalesgroup.authz.model.ext._3.AbstractPolicyFinder;
import com.thalesgroup.authzforce.core.PdpModelHandler;
import com.thalesgroup.authzforce.core.ResourceUtils;

/**
 * This is a simple implementation of <code>PolicyFinderModule</code> that supports retrieval based
 * on context, and is designed for use with a run-time configuration. Its constructor accepts a
 * <code>List</code> of <code>String</code>s that represent Spring-like URLs or files, and these are
 * resolved to policies when the module is initialized. Beyond this, there is no modifying or
 * re-loading the policies represented by this class. This class will optionally wrap multiple
 * applicable policies into a dynamic PolicySet.
 * <p>
 * Note that this class is designed to complement <code>StaticRefPolicyFinderModule</code>. It would
 * be easy to support both kinds of policy retrieval in a single class, but the functionality is
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
public class StaticPolicyFinderModule extends PolicyFinderModule<AbstractPolicyFinder>
{

	// the list of policy URLs/filenames passed to the constructor
	private List<String> policyList;

	// the map of policies
	private PolicyCollection policies;

	// the optional schema
	private final Schema schema;

	// the policy identifier for any policy sets we dynamically create
	private static final String POLICY_ID = "urn:com:sun:xacml:support:finder:dynamic-policy-set";
	private static URI policyId = null;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(StaticPolicyFinderModule.class);

	static
	{
		try
		{
			policyId = new URI(POLICY_ID);
		} catch (Exception e)
		{
			// this can't actually happen, but just in case...

			LOGGER.error("couldn't assign default policy id", e);
		}
	}

	/**
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to the given collection
	 * of policies and returns an error when more than one policy matches a given context. Any
	 * policy that cannot be loaded will be noted in the log, but will not cause an error. The
	 * schema file used to validate policies is defined by the property
	 * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved property is null, then no
	 * schema validation will occur.
	 * 
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent URLs or files pointing
	 *            to XACML policies
	 */
	public StaticPolicyFinderModule(List<String> policyList)
	{
		this.policyList = policyList;
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
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to the given collection
	 * of policies and returns an error when more than one policy matches a given context. Any
	 * policy that cannot be loaded will be noted in the log, but will not cause an error.
	 * 
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent URLs or files pointing
	 *            to XACML policies
	 * @param schemaFilename
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired
	 */
	public StaticPolicyFinderModule(List<String> policyList, String schemaFilename)
	{
		this.policyList = policyList;
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
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to the given collection
	 * of policies. The given combining algorithm is used to create new PolicySets when more than
	 * one policy applies. Any policy that cannot be loaded will be noted in the log, but will not
	 * cause an error. The schema file used to validate policies is defined by the property
	 * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved property is null, then no
	 * schema validation will occur.
	 * 
	 * @param combiningAlg
	 *            the algorithm to use in a new PolicySet when more than one policy applies
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent URLs or files pointing
	 *            to XACML policies
	 * 
	 * @throws URISyntaxException
	 *             if the combining algorithm is not a well-formed URI
	 * @throws UnknownIdentifierException
	 *             if the combining algorithm identifier isn't known
	 */
	public StaticPolicyFinderModule(String combiningAlg, List<String> policyList) throws URISyntaxException, UnknownIdentifierException
	{
		PolicyCombiningAlgorithm alg = (PolicyCombiningAlgorithm) (CombiningAlgFactory.getInstance().createAlgorithm(new URI(combiningAlg)));

		this.policyList = policyList;
		this.policies = new PolicyCollection(alg, policyId);

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
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to the given collection
	 * of policies. The given combining algorithm is used to create new PolicySets when more than
	 * one policy applies. Any policy that cannot be loaded will be noted in the log, but will not
	 * cause an error.
	 * 
	 * @param combiningAlg
	 *            the algorithm to use in a new PolicySet when more than one policy applies
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent URLs or files pointing
	 *            to XACML policies
	 * @param schemaFilename
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired
	 * 
	 * @throws URISyntaxException
	 *             if the combining algorithm is not a well-formed URI
	 * @throws UnknownIdentifierException
	 *             if the combining algorithm identifier isn't known
	 */
	public StaticPolicyFinderModule(String combiningAlg, List<String> policyList, String schemaFilename) throws URISyntaxException,
			UnknownIdentifierException
	{
		PolicyCombiningAlgorithm alg = (PolicyCombiningAlgorithm) (CombiningAlgFactory.getInstance().createAlgorithm(new URI(combiningAlg)));

		this.policyList = policyList;
		this.policies = new PolicyCollection(alg, policyId);

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

	@Override
	public boolean isRequestSupported()
	{
		return true;
	}

	@Override
	public void init(PolicyFinder finder)
	{
		// now that we have the PolicyFinder, we can load the policies
		// PolicyReader reader = new PolicyReader(finder, LOGGER, schemaFile);
		final File baseDir = finder.getBaseDirectory();
		for (final String policyLocation : policyList)
		{
			Object jaxbObj;
			final Unmarshaller unmarshaller;
			try
			{
				unmarshaller = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			} catch (JAXBException e1)
			{
				throw new IllegalArgumentException("Failed to create JAXB marshaller for unmarshalling Policy XML document", e1);
			}

			unmarshaller.setSchema(schema);
			try
			{
				// first try to load it as a Spring resource

				final URL url = ResourceUtils.getResourceURL(policyLocation);
				if(url == null) {
					throw new IOException("Invalid Spring-supported URL: " + policyLocation);
				}
				
				jaxbObj = unmarshaller.unmarshal(url);
			} catch (IOException ioe)
			{
				LOGGER.info("Failed to load policy location {} as Spring resource. Loading as file relative to PDP configuration directory");
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
				} catch (ParsingException | UnknownIdentifierException e)
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

	@Override
	public PolicyFinderResult findPolicy(EvaluationCtx context)
	{
		try
		{
			final IPolicy policy = policies.getPolicy(context);
			if (policy == null) {
				return new PolicyFinderResult();
			}
			
			return new PolicyFinderResult(policy);
		} catch (TopLevelPolicyException tlpe)
		{
			return new PolicyFinderResult(tlpe.getStatus());
		}
	}

	@Override
	public void init(AbstractPolicyFinder conf)
	{
		throw new UnsupportedOperationException("Initialization method not supported. Use the constructors instead.");

	}

}
