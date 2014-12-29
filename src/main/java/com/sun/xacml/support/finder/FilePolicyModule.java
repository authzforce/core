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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.ProcessingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.xacmlv3.IPolicy;
import com.thalesgroup.authz.model.ext._3.AbstractPolicyFinder;

/**
 * This module represents a collection of files containing polices, each of which will be searched
 * through when trying to find a policy that is applicable to a specific request. It does not
 * support policy references.
 * <p>
 * Note that this class used to be provided in the <code>com.sun.xacml.finder.impl</code> package
 * with a warning that it would move out of the core packages eventually. This is partly because
 * this class doesn't represent standard functionality, and partly because it isn't designed to be
 * generally useful as anything more than an example. Because so many people have used this class,
 * however, it stayed in place until the 2.0 release.
 * <p>
 * As of the 2.0 release, you may still use this class (in its new location), but you are encouraged
 * to migrate to the new support modules that are much richer and designed for general-purpose use.
 * Also, note that the <code>loadPolicy</code> methods that used to be available from this class
 * have been removed. That functionality has been replaced by the much more useful
 * <code>PolicyReader</code> class. If you need to load policies directly, you should consider that
 * new class.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class FilePolicyModule extends PolicyFinderModule<AbstractPolicyFinder>
{

	// the schema file we're using, if any
	private File schemaFile = null;

	// the filenames for the files we'll load
	private Set<String> fileNames;

	// the actual loaded policies
	private PolicyCollection policies;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(FilePolicyModule.class);

	/**
	 * Constructor which retrieves the schema file to validate policies against from the
	 * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved property is null, then no
	 * schema validation will occur.
	 */
	public FilePolicyModule()
	{
		fileNames = new HashSet<>();
		policies = new PolicyCollection();

		String schemaName = System.getProperty(PolicyReader.POLICY_SCHEMA_PROPERTY);

		if (schemaName != null)
			schemaFile = new File(schemaName);
	}

	/**
	 * Constructor that uses the specified <code>File</code> as the schema file for XML validation.
	 * If schema validation is not desired, a null value should be used.
	 * 
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired.
	 */
	public FilePolicyModule(File schemaFile)
	{
		fileNames = new HashSet<>();
		policies = new PolicyCollection();

		this.schemaFile = schemaFile;
	}

	/**
	 * Constructor that uses the specified <code>String</code> as the schema file for XML
	 * validation. If schema validation is not desired, a null value should be used.
	 * 
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired.
	 */
	public FilePolicyModule(String schemaFile)
	{
		this((schemaFile != null) ? new File(schemaFile) : null);
	}

	/**
	 * Constructor that specifies a set of initial policy files to use. This retrieves the schema
	 * file to validate policies against from the <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>.
	 * If the retrieved property is null, then no schema validation will occur.
	 * 
	 * @param fileNames
	 *            a <code>List</code> of <code>String</code>s that identify policy files
	 */
	public FilePolicyModule(List<String> fileNames)
	{
		this();

		if (fileNames != null)
			this.fileNames.addAll(fileNames);
	}

	/**
	 * Constructor that specifies a set of initial policy files to use and the schema file used to
	 * validate the policies. If schema validation is not desired, a null value should be used.
	 * 
	 * @param fileNames
	 *            a <code>List</code> of <code>String</code>s that identify policy files
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if schema validation is not
	 *            desired.
	 */
	public FilePolicyModule(List<String> fileNames, String schemaFile)
	{
		this(schemaFile);

		if (fileNames != null)
			this.fileNames.addAll(fileNames);
	}

	/**
	 * Adds a file (containing a policy) to the collection of filenames associated with this module.
	 * Note that this doesn't actually load the policy file. Policies aren't loaded from their files
	 * until the module is initialized through the <code>init</code> method (which is called
	 * automatically by the <code>PolicyFinder</code> when the system comes up).
	 * 
	 * @param filename
	 *            the file to add to this module's collection of files
	 * @return true iff filename was not already in the list
	 */
	public boolean addPolicy(String filename)
	{
		return fileNames.add(filename);
	}

	/**
	 * Indicates whether this module supports finding policies based on a request (target matching).
	 * Since this module does support finding policies based on requests, it returns true.
	 * 
	 * @return true, since finding policies based on requests is supported
	 */
	@Override
	public boolean isRequestSupported()
	{
		return true;
	}

	/**
	 * Initializes the <code>FilePolicyModule</code> by loading the policies contained in the
	 * collection of files associated with this module. This method also uses the specified
	 * <code>PolicyFinder</code> to help in instantiating PolicySets.
	 * 
	 * @param finder
	 *            a PolicyFinder used to help in instantiating PolicySets
	 */
	@Override
	public void init(PolicyFinder finder)
	{
		PolicyReader reader = new PolicyReader(finder, LOGGER, schemaFile);

		for(String fname: fileNames) {
			final IPolicy policy;
			try(final InputStream fileIn = new FileInputStream(fname))
			{
				String typePolicy = reader.getType(fileIn);
				if (typePolicy.equals("Policy"))
				{
					policy = reader.readPolicy(fileIn);
				} else if (typePolicy.equals("PolicySet"))
				{
					policy = reader.readPolicySet(fileIn);
				} else
				{
					throw new IllegalArgumentException("Invalid type of Policy Element: " + typePolicy);
				}

				if (policy != null)
				{
					this.policies.addPolicy(policy);
				}
			} catch (FileNotFoundException fnfe)
			{
				throw new ProcessingException("Policy file '"+fname+"' not found", fnfe);
			} catch (ParsingException pe)
			{
				throw new ProcessingException("Policy file '"+fname+"' couldn't be parsed into XACML",  pe);
			} catch (SAXException e)
			{
				throw new ProcessingException("Policy file '"+fname+"' couldn't be parsed into XML", e);
			} catch (IOException e)
			{
				throw new ProcessingException("Policy file '"+fname+"' couldn't be read", e);
			} catch (UnknownIdentifierException e)
			{
				throw new ProcessingException("Invalid combining algorithm ID found in policy file "+fname, e);
			}
		}
	}

	/**
	 * Finds a policy based on a request's context. If more than one applicable policy is found,
	 * this will return an error. Note that this is basically just a subset of the OnlyOneApplicable
	 * Policy Combining Alg that skips the evaluation step. See comments in there for details on
	 * this algorithm.
	 * 
	 * @param context
	 *            the representation of the request data
	 * 
	 * @return the result of trying to find an applicable policy
	 */
	@Override
	public PolicyFinderResult findPolicy(EvaluationCtx context)
	{
//		try
//		{
//			final IPolicy policyElement;
//			policyElement = this.policies.getPolicy(context);
//			if (policyElement instanceof PolicySet)
//			{
//				PolicySet policySet = (PolicySet) policyElement;
//				// Retrieving combining algorithm
//				PolicyCombiningAlgorithm myCombiningAlg = (PolicyCombiningAlgorithm) policySet.getCombiningAlg();
//				PolicyCollection myPolcollection = new PolicyCollection(myCombiningAlg, URI.create(policySet.getPolicySetId()));
//				for (Object elt : policySet.getPolicySetsAndPoliciesAndPolicySetIdReferences())
//				{
//					if (elt instanceof PolicyCombinerElement)
//					{
//						final Object combinedElt = ((PolicyCombinerElement) elt).getElement();
//						if (combinedElt instanceof IPolicy)
//						{
//							myPolcollection.addPolicy((IPolicy) combinedElt);
//						} else
//						{
//							continue;
//						}
//					}
//				}
//
//				final IPolicy subpolicy = myPolcollection.getPolicy(context);
//				// The finder found more than one applicable policy so it build a new PolicySet
//				if (subpolicy instanceof PolicySet)
//				{
//					if (policySet != null)
//					{
//						((PolicySet) subpolicy).setObligationExpressions(policySet.getObligationExpressions());
//						((PolicySet) subpolicy).setAdviceExpressions(policySet.getAdviceExpressions());
//					}
//
//					return new PolicyFinderResult(subpolicy);
//				}
//
//				// The finder found only one applicable policy
//				else if (subpolicy instanceof Policy)
//				{
//					List<IPolicy> matchedPolicies = Arrays.asList(subpolicy);
//					final PolicySet finalPolicySet;
//					finalPolicySet = new PolicySet(policySet.getId(), policySet.getVersion(), myCombiningAlg, policySet.getDescription(),
//							(Target) policySet.getTarget(), matchedPolicies, policySet.getDefaultVersion(), policySet.getObligationExpressions(),
//							policySet.getAdviceExpressions());
//					return new PolicyFinderResult(finalPolicySet);
//				}
//			} else if (policyElement instanceof Policy)
//			{
//				Policy policies = (Policy) policyElement;
//				return new PolicyFinderResult((Policy) policies);
//			}
//			// None of the policies/policySets matched
//			return new PolicyFinderResult();
//		} catch (TopLevelPolicyException tlpe)
//		{
//			return new PolicyFinderResult(tlpe.getStatus());
//		}
		
		try {
            final IPolicy policy = policies.getPolicy(context);
            if (policy == null) {
                return new PolicyFinderResult();
            } 
            
			return new PolicyFinderResult(policy);
        } catch (TopLevelPolicyException tlpe) {
            return new PolicyFinderResult(tlpe.getStatus());
        }
	}

	@Override
	public void init(AbstractPolicyFinder conf)
	{
		throw new UnsupportedOperationException("Initialization method not supported. Use the constructors instead.");
	}

}
