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
package com.sun.xacml.finder;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.VersionConstraints;
import com.sun.xacml.ctx.Status;

/**
 * This class is used by the PDP to find all policies used in evaluation. A PDP is given a
 * pre-configured <code>PolicyFinder</code> on construction. The <code>PolicyFinder</code> provides
 * the functionality both to find policies based on a request (ie, retrieve policies and match
 * against the target) and based on an idReference (as can be included in a PolicySet).
 * <p>
 * While this class is typically used by the PDP, it is intentionally designed to support
 * stand-alone use, so it could be the base for a distributed service, or for some application that
 * needs just this functionality. There is nothing in the <code>PolicyFinder</code that relies on
 * the functionality in the PDP. An example of this is a PDP that offloads all policy work by
 * passing requests to another server that does all the retrieval, and passes back the applicable
 * policy. This would require custom code undefined in the XACML spec, but it would free up the
 * server to focus on core policy processing.
 * <p>
 * Note that it is an error to have more than one top-level policy (as explained in the
 * OnlyOneApplicable combining algorithm), so any module that is added to this finder will be
 * evaluated each time a policy is requested. This means that you should think carefully about how
 * many modules you include, and how they can cacheManager policy data.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class PolicyFinder
{
	/**
	 * Base directory for finder modules to resolve relative policy file paths as relative to this
	 */
	private final File baseDirectory;
	
	/**
	 *  All modules in this finder (List type is used because order matters, for instance, referenceModules should be loaded before other modules if the latter load policies making PolicyReference to policies that have to be loaded before by referenceModules
	 *  If not, we would fail or would not be able to check resoved PolicyReference when loading the modules.
	 *  Besides, using Set type makes no sense since PolicyFinderModule class does not implement equals().
	 */
	private List<PolicyFinderModule> allModules;

	// all the request modules
	private List<PolicyFinderModule> requestModules;

	// all the reference modules
	private List<PolicyFinderModule> referenceModules;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyFinder.class);
	
	/**
	 * Creates PolicyFinder instance
	 * 
	 * @param base
	 *            Base directory for finder modules to resolve relative policy file paths as
	 *            relative to this
	 */
	public PolicyFinder(File base)
	{
		if (base != null)
		{
			if (!base.exists())
			{
				throw new IllegalArgumentException("File specified as PolicyFinder base arg does not exist: " + base.getAbsolutePath());
			}

			if (!base.isDirectory())
			{
				throw new IllegalArgumentException("File specified as PolicyFinder base arg is not a directory: " + base.getAbsolutePath());
			}

			if (!base.canRead())
			{
				throw new IllegalArgumentException("File specified as PolicyFinder base arg cannot be read: " + base.getAbsolutePath());
			}
		}

		this.baseDirectory = base;
	}
	
	/**
	 * Creates PolicyFinder instance
	 */
	public PolicyFinder()
	{
		this.baseDirectory = null;
	}
	
	/**
	 * Returns the ordered <code>List</code> of <code>PolicyFinderModule</code>s used by this class
	 * to find policies.
	 * 
	 * @return a <code>List</code> of <code>PolicyFinderModule</code>s in order of declaration/registration
	 */
	public List<PolicyFinderModule> getModules()
	{
		return new ArrayList<>(allModules);
	}

	/**
	 * Sets the ordered <code>List</code> of <code>PolicyFinderModule</code>s used by this class to
	 * find policies.
	 * 
	 * @param modules
	 *            a <code>List</code> of <code>PolicyFinderModule</code>s
	 */
	public void setModules(List<PolicyFinderModule> modules)
	{
		allModules = new ArrayList<>(modules);
		requestModules = new ArrayList<>();
		referenceModules = new ArrayList<>();

		for (PolicyFinderModule module: modules)
		{
			if (module.isRequestSupported())
				requestModules.add(module);

			if (module.isIdReferenceSupported())
				referenceModules.add(module);
		}
	}

	/**
	 * Initializes all modules in this finder.
	 */
	public void init()
	{
		LOGGER.debug("Initializing PolicyFinder");
		
		for (PolicyFinderModule module: allModules)
		{
			module.init(this);
		}
	}

	/**
	 * Finds a policy based on a request's context. This may involve using the request data as
	 * indexing data to lookup a policy. This will always do a Target match to make sure that the
	 * given policy applies. If more than one applicable policy is found, this will return an error.
	 * 
	 * @param context
	 *            the representation of the request data
	 * 
	 * @return the result of trying to find an applicable policy
	 */
	public PolicyFinderResult findPolicy(EvaluationCtx context)
	{
		PolicyFinderResult result = null;
		
		// look through all of the modules
		for (PolicyFinderModule module: requestModules)
		{
			PolicyFinderResult newResult = module.findPolicy(context);

			// if there was an error, we stop right away
			if (newResult.indeterminate())
			{
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info("An error occured while trying to find a single applicable policy for a request: {}", newResult.getStatus()
							.getMessage());
				}

				return newResult;
			}

			// if we found a policy...
			if (!newResult.notApplicable())
			{
				// ...if we already had found a policy, this is an error...
				if (result != null)
				{
					if(LOGGER.isInfoEnabled()) {
						LOGGER.info("More than one top-level applicable policy found for the request: {}, {}...", result.getPolicy(), newResult.getPolicy());
					}
					
					List<String> code = new ArrayList<>();
					code.add(Status.STATUS_PROCESSING_ERROR);
					Status status = new Status(code, "too many applicable top-level policies");
					return new PolicyFinderResult(status);
				}

				// ...otherwise we remember the result
				result = newResult;
			}
		}

		// if we got here then we didn't have any errors, so the only
		// question is whether or not we found anything
		if (result != null)
		{
			return result;
		}
		
		LOGGER.info("No applicable policies were found for the request");

		return new PolicyFinderResult();
	}

	/**
	 * Finds a policy based on an id reference. This may involve using the reference as indexing
	 * data to lookup a policy. This will always do a Target match to make sure that the given
	 * policy applies. If more than one applicable policy is found, this will return an error.
	 * 
	 * @param idReference
	 *            the identifier used to resolve a policy
	 * @param type
	 *            type of reference (policy or policySet) as identified by the fields in
	 *            <code>PolicyReference</code>
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy
	 * @param parentMetaData
	 *            the meta-data from the parent policy, which provides XACML version, factories,
	 *            etc.
	 * 
	 * @return the result of trying to find an applicable policy
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>type</code> is invalid
	 */
	public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints, PolicyMetaData parentMetaData)
			throws IllegalArgumentException
	{
		PolicyFinderResult result = null;

		if ((type != PolicyReference.POLICY_REFERENCE) && (type != PolicyReference.POLICYSET_REFERENCE))
			throw new IllegalArgumentException("Unknown reference type");

		// look through all of the modules
		for (PolicyFinderModule module: referenceModules)
		{
			PolicyFinderResult newResult = module.findPolicy(idReference, type, constraints, parentMetaData);

			// if there was an error, we stop right away
			if (newResult.indeterminate())
			{
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info("An error occured while trying to find the referenced policy '{}': {}", idReference, newResult.getStatus().getMessage());
				}

				return newResult;
			}

			// if we found a policy...
			if (!newResult.notApplicable())
			{
				// ...if we already had found a policy, this is an error...
				if (result != null)
				{
					LOGGER.info("More than one policy applies for the reference: {}", idReference);
					List<String> code = new ArrayList<>();
					code.add(Status.STATUS_PROCESSING_ERROR);
					Status status = new Status(code, "too many applicable top-level policies");
					return new PolicyFinderResult(status);
				}

				// ...otherwise we remember the result
				result = newResult;
			}
		}

		// if we got here then we didn't have any errors, so the only
		// question is whether or not we found anything
		if (result != null)
		{
			return result;
		}
		
		LOGGER.info("No policies were resolved for the reference: {}", idReference);
		return new PolicyFinderResult();
	}
	
	/**
	 * @return Base directory for finder modules to resolve relative policy file paths as relative
	 *         to this
	 */
	public File getBaseDirectory()
	{
		return this.baseDirectory;
	}

}
