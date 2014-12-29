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

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.thalesgroup.authz.model.ext._3.AbstractPolicyFinder;
import com.thalesgroup.authzforce.core.PdpModelHandler;

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
public class StaticRefPolicyFinderModule extends PolicyFinderModule<AbstractPolicyFinder>
{

	/*
	 * The list of policy URLs passed to the constructor. Order matters because one PolicySet can
	 * make reference to another one which has to be previously defined. Not null only if the finder
	 * module is initialized from locations of XACML <PolicySet>s.
	 */
	private List<URL> policyLocationList = null;

	/*
	 * List of <PolicySet>s. Not null only if the finder module is initialized directly from XACML
	 * <PolicySet>s (JAXB).
	 */
	private List<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> policySetList = null;

	// the map of policies
	private PolicyCollection policies;

	// the optional schema
	private Schema schema = null;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(StaticRefPolicyFinderModule.class);

	/**
	 * Creates policy by-reference finder module based on list of XACML PolicySets as JAXB elements
	 * 
	 * @param policySets
	 *            a <code>List</code> of XACML <PolicySet>s
	 */
	public StaticRefPolicyFinderModule(List<oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet> policySets)
	{
		this.policySetList = policySets;
	}

	/**
	 * Creates a <code>StaticRefPolicyFinderModule</code> that provides access to the given
	 * collection of policyLocations.
	 * 
	 * @param policyLocations
	 *            a <code>List</code> of <code>URL</code>s that represent URLs or files pointing to
	 *            XACML policies
	 * @param xacmlSchema
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired
	 */
	public StaticRefPolicyFinderModule(List<URL> policyLocations, Schema xacmlSchema)
	{
		this.policyLocationList = policyLocations;
		this.policies = new PolicyCollection();
		this.schema = xacmlSchema;
	}

	/**
	 * Always returns <code>true</code> since this module does support finding policies based on
	 * reference.
	 * 
	 * @return true
	 */
	@Override
	public boolean isIdReferenceSupported()
	{
		return true;
	}

	private static IPolicy getPolicyInstanceFromJaxb(Object policyOrPolicySetJaxbObject, PolicyFinder finder)
	{
		final IPolicy policyInstance;
		if (policyOrPolicySetJaxbObject instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy)
		{
			final oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy policyElement = (oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy) policyOrPolicySetJaxbObject;
			try
			{
				policyInstance = Policy.getInstance(policyElement);
			} catch (ParsingException | UnknownIdentifierException e)
			{
				throw new IllegalArgumentException("Error parsing Policy: " + policyElement.getPolicyId(), e);
			}
		} else if (policyOrPolicySetJaxbObject instanceof oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet)
		{
			final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet policySetElement = (oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet) policyOrPolicySetJaxbObject;
			try
			{
				policyInstance = PolicySet.getInstance(policySetElement, finder);
			} catch (ParsingException | UnknownIdentifierException e)
			{
				throw new IllegalArgumentException("Error parsing PolicySet: " + policySetElement.getPolicySetId(), e);
			}
		} else
		{
			throw new IllegalArgumentException("Unexpected element found as root of the policy document: "
					+ policyOrPolicySetJaxbObject.getClass().getSimpleName());
		}

		return policyInstance;
	}

	/**
	 * Initialize this module. Typically this is called by <code>PolicyFinder</code> when a PDP is
	 * created. This method is where the policies are actually loaded.
	 * 
	 * @param finder
	 *            the <code>PolicyFinder</code> using this module
	 */
	@Override
	public void init(PolicyFinder finder)
	{
		// now that we have the PolicyFinder, we can load the policies
		// PolicyReader reader = new PolicyReader(finder, LOGGER, schemaFile);

		/*
		 * Try loading from JAXB <PolicySet>s first.
		 */
		if (policySetList != null)
		{
			for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet policySet : policySetList)
			{
				final IPolicy policyInstance = getPolicyInstanceFromJaxb(policySet, finder);
				if (!policies.addPolicy(policyInstance))
				{
					LOGGER.warn("Tried to load the same policy multiple times: id={}", policyInstance.getId());
				}
			}

			return;
		}

		// Else load from list of policy locations
		for (final URL policyLocation : policyLocationList)
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
				jaxbObj = unmarshaller.unmarshal(policyLocation);
			} catch (JAXBException e)
			{
				throw new IllegalArgumentException("Failed to unmarshall Policy XML document from policy location: " + policyLocation, e);
			}

			final IPolicy policyInstance = getPolicyInstanceFromJaxb(jaxbObj, finder);
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
	@Override
	public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints, PolicyMetaData parentMetaData)
	{
		final IPolicy policyInstance = policies.getPolicy(idReference.toString(), type, constraints);

		if (policyInstance == null)
		{
			return new PolicyFinderResult();
		}

		return new PolicyFinderResult(policyInstance);
	}

	@Override
	public void init(AbstractPolicyFinder conf)
	{
		throw new UnsupportedOperationException("Initialization method not supported. Use the constructors instead.");
		
	}

}
