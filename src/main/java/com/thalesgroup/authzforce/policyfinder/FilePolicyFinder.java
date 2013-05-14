/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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
package com.thalesgroup.authzforce.policyfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicySet;
import com.sun.xacml.combine.PolicyCombinerElement;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.support.finder.PolicyCollection;
import com.sun.xacml.support.finder.PolicyReader;
import com.sun.xacml.support.finder.TopLevelPolicyException;
import com.sun.xacml.xacmlv3.Policy;

/**
 * This module represents a collection of files containing polices, each of
 * which will be searched through when trying to find a policy that is
 * applicable to a specific request. It does not support policy references.
 * <p>
 * Note that this class used to be provided in the
 * <code>com.sun.xacml.finder.impl</code> package with a warning that it would
 * move out of the core packages eventually. This is partly because this class
 * doesn't represent standard functionality, and partly because it isn't
 * designed to be generally useful as anything more than an example. Because so
 * many people have used this class, however, it stayed in place until the 2.0
 * release.
 * <p>
 * As of the 2.0 release, you may still use this class (in its new location),
 * but you are encouraged to migrate to the new support modules that are much
 * richer and designed for general-purpose use. Also, note that the
 * <code>loadPolicy</code> methods that used to be available from this class
 * have been removed. That functionality has been replaced by the much more
 * useful <code>PolicyReader</code> class. If you need to load policies
 * directly, you should consider that new class.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class FilePolicyFinder extends PolicyFinderModule {

	// the schema file we're using, if any
	private File schemaFile = null;

	// the filenames for the files we'll load
	private Set fileNames;

	// the actual loaded policies
	private PolicyCollection policies;

	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(FilePolicyFinder.class);

	/**
	 * Constructor which retrieves the schema file to validate policies against
	 * from the <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the
	 * retrieved property is null, then no schema validation will occur.
	 */
	public FilePolicyFinder() {
		this.fileNames = new HashSet();
		this.policies = new PolicyCollection();

		String schemaName = System
				.getProperty(PolicyReader.POLICY_SCHEMA_PROPERTY);

		if (schemaName != null) {
			this.schemaFile = new File(schemaName);
		}
	}

	/**
	 * Constructor that uses the specified <code>File</code> as the schema file
	 * for XML validation. If schema validation is not desired, a null value
	 * should be used.
	 * 
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if
	 *            schema validation is not desired.
	 */
	public FilePolicyFinder(File schemaFile) {
		this.fileNames = new HashSet();
		this.policies = new PolicyCollection();

		this.schemaFile = schemaFile;
	}

	/**
	 * Constructor that uses the specified <code>String</code> as the schema
	 * file for XML validation. If schema validation is not desired, a null
	 * value should be used.
	 * 
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if
	 *            schema validation is not desired.
	 */
	public FilePolicyFinder(String schemaFile) {
		this((schemaFile != null) ? new File(schemaFile) : null);
	}

	/**
	 * Constructor that specifies a set of initial policy files to use. This
	 * retrieves the schema file to validate policies against from the
	 * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved
	 * property is null, then no schema validation will occur.
	 * 
	 * @param fileNames
	 *            a <code>List</code> of <code>String</code>s that identify
	 *            policy files
	 */
	public FilePolicyFinder(List fileNames) {
		this();

		if (fileNames != null) {
			this.fileNames.addAll(fileNames);
		}
	}

	/**
	 * Constructor that specifies a set of initial policy files to use and the
	 * schema file used to validate the policies. If schema validation is not
	 * desired, a null value should be used.
	 * 
	 * @param fileNames
	 *            a <code>List</code> of <code>String</code>s that identify
	 *            policy files
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if
	 *            schema validation is not desired.
	 */
	public FilePolicyFinder(List fileNames, String schemaFile) {
		this(schemaFile);

		if (fileNames != null) {
			this.fileNames.addAll(fileNames);
		}
	}

	/**
	 * Adds a file (containing a policy) to the collection of filenames
	 * associated with this module. Note that this doesn't actually load the
	 * policy file. Policies aren't loaded from their files until the module is
	 * initialized through the <code>init</code> method (which is called
	 * automatically by the <code>PolicyFinder</code> when the system comes up).
	 * 
	 * @param filename
	 *            the file to add to this module's collection of files
	 * 
	 * @return true or false depending on the success of this operation.
	 */
	public boolean addPolicy(String filename) {
		return this.fileNames.add(filename);
	}

	/**
	 * Indicates whether this module supports finding policies based on a
	 * request (target matching). Since this module does support finding
	 * policies based on requests, it returns true.
	 * 
	 * @return true, since finding policies based on requests is supported
	 */
	public boolean isRequestSupported() {
		return true;
	}

	/**
	 * Initializes the <code>FilePolicyModule</code> by loading the policies
	 * contained in the collection of files associated with this module. This
	 * method also uses the specified <code>PolicyFinder</code> to help in
	 * instantiating PolicySets.
	 * 
	 * @param finder
	 *            a PolicyFinder used to help in instantiating PolicySets
	 */
	public void init(PolicyFinder finder) {
		PolicyReader reader = new PolicyReader(finder, null, this.schemaFile);

		// Deprecated method
		// PolicyReader(finder, logger, this.schemaFile);

		Iterator it = this.fileNames.iterator();
		while (it.hasNext()) {
			String fname = (String) (it.next());
			Policy policy = null;
			PolicySet policySet = null;
			try {
				String typePolicy = reader.getType(new FileInputStream(fname));
				if (typePolicy.equals("Policy")) {
					policy = reader.readPolicy(new FileInputStream(fname));
				} else if (typePolicy.equals("PolicySet")) {
					policySet = reader.readPolicySet(new FileInputStream(fname));
				}
				if (policy != null) {
					this.policies.addPolicy(policy);
				} else if (policySet != null) {
					this.policies.addPolicySet(policySet);
				}
			} catch (FileNotFoundException fnfe) {
				LOGGER.warn("File couldn't be read: " + fname, fnfe);
			} catch (ParsingException pe) {
				LOGGER.warn("Error reading policy from file " + fname, pe);
			}
		}
	}

	/**
	 * Finds a policy based on a request's context. If more than one applicable
	 * policy is found, this will return an error. Note that this is basically
	 * just a subset of the OnlyOneApplicable Policy Combining Alg that skips
	 * the evaluation step. See comments in there for details on this algorithm.
	 * 
	 * @param context
	 *            the representation of the request data
	 * 
	 * @return the result of trying to find an applicable policy
	 */
	public PolicyFinderResult findPolicy(EvaluationCtx context) {
		try {
			Object myPolicies = this.policies.getPolicy(context);
			if(myPolicies == null) {
				myPolicies = this.policies.getPolicySet(context);
			}
			if(myPolicies instanceof PolicySet) {
				PolicySet policySet = (PolicySet)myPolicies;
				// Retrieving combining algorithm
				PolicyCombiningAlgorithm myCombiningAlg = (PolicyCombiningAlgorithm) policySet.getCombiningAlg();
				PolicyCollection myPolcollection = new PolicyCollection(myCombiningAlg, URI.create(policySet.getPolicySetId()));
				for (Object elt : policySet.getPolicySetOrPolicyOrPolicySetIdReference()) {
					if (elt instanceof PolicyCombinerElement) {
						if((((PolicyCombinerElement) elt).getElement()) instanceof Policy) {
							myPolcollection.addPolicy((Policy) ((PolicyCombinerElement) elt).getElement());
						} else if((((PolicyCombinerElement) elt).getElement()) instanceof PolicySet) {
							myPolcollection.addPolicySet((PolicySet) ((PolicyCombinerElement) elt).getElement());
						}
					}
				}
				Object policy = myPolcollection.getPolicySet(context);
				if(policy == null) {
					policy = myPolcollection.getPolicy(context);
					
				}
				// The finder found more than one applicable policy so it build a new PolicySet
				if(policy instanceof PolicySet) {
					if(policySet != null) {
						((PolicySet)policy).setObligationExpressions(policySet.getObligationExpressions());
						((PolicySet)policy).setAdviceExpressions(policySet.getAdviceExpressions());
					}
					return new PolicyFinderResult((PolicySet)policy, myCombiningAlg);	
				}
				// The finder found only one applicable policy 
				else if(policy instanceof Policy) {
					List matchedPolicies = Arrays.asList(policy);
					PolicySet finalPolicySet = new PolicySet(policySet.getId(), policySet.getVersion(), myCombiningAlg, policySet.getDescription(), policySet.getTarget(), matchedPolicies, policySet.getDefaultVersion(), policySet.getObligationExpressions(), policySet.getAdviceExpressions());
					
					return new PolicyFinderResult(finalPolicySet, myCombiningAlg);
				}
			} else if (myPolicies instanceof Policy) {
				Policy policies = (Policy)myPolicies;
				return new PolicyFinderResult((Policy)policies);
			}
			// None of the policies/policySets matched 
			return new PolicyFinderResult();
		} catch (TopLevelPolicyException tlpe) {
			return new PolicyFinderResult(tlpe.getStatus());
		}
	}

}
