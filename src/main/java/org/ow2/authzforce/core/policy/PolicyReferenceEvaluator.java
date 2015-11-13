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
package com.sun.xacml;

import java.util.Deque;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thalesgroup.authzforce.core.DecisionResult;
import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;
import com.thalesgroup.authzforce.core.policy.IPolicy;
import com.thalesgroup.authzforce.core.policy.PolicyEvaluator;
import com.thalesgroup.authzforce.core.policy.RefPolicyFinder;

/**
 * This class is responsible for evaluating XACML Policy(Set)IdReferences.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @param <T>
 *            type of referred element: Policy, PolicySet...
 * 
 */
public abstract class PolicyReference<T extends IPolicy> extends IdReferenceType implements IPolicy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyReference.class);

	private static final UnsupportedOperationException UNSUPPORTED_SET_VALUE_EXCEPTION = new UnsupportedOperationException("ID in Policy(Set)IdReference is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_VERSION_EXCEPTION = new UnsupportedOperationException("Version in Policy(Set)IdReference is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_EARLIEST_VERSION_EXCEPTION = new UnsupportedOperationException("EarliestVersion in Policy(Set)IdReference is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_LATEST_VERSION_EXCEPTION = new UnsupportedOperationException("LatestVersion in Policy(Set)IdReference is read-only");

	// and version constraints on this reference
	protected final VersionConstraints versionConstraints;

	protected final Class<T> referredPolicyClass;

	private transient volatile String toString = null;

	private PolicyReference(String idRef, VersionConstraints versionConstraints, Class<T> policyReferenceType)
	{
		this.versionConstraints = versionConstraints;
		this.referredPolicyClass = policyReferenceType;
		this.toString = toString(referredPolicyClass, idRef, versionConstraints);
	}

	private static String toString(Class<? extends IPolicy> policyReferenceType, String policyRefId, VersionConstraints versionConstraints)
	{
		return (policyReferenceType == PolicyEvaluator.class ? "PolicyIdReference" : "PolicySetIdReference") + "[Id=" + policyRefId + ", " + versionConstraints + "]";
	}

	// Make all super fields final
	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType#setValue(java.lang.String)
	 */
	@Override
	public final void setValue(String value)
	{
		throw UNSUPPORTED_SET_VALUE_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType#setVersion(java.lang.String)
	 */
	@Override
	public final void setVersion(String value)
	{
		throw UNSUPPORTED_SET_VERSION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType#setEarliestVersion(java.lang.
	 * String)
	 */
	@Override
	public final void setEarliestVersion(String value)
	{
		throw UNSUPPORTED_SET_EARLIEST_VERSION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType#setLatestVersion(java.lang.String
	 * )
	 */
	@Override
	public final void setLatestVersion(String value)
	{
		throw UNSUPPORTED_SET_LATEST_VERSION_EXCEPTION;
	}

	@Override
	public final DecisionResult evaluate(EvaluationContext context)
	{
		return evaluate(context, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return toString;
	}

	private static class Static<T extends IPolicy> extends PolicyReference<T>
	{

		private final transient T referredPolicy;

		private Static(String policyIdRef, VersionConstraints versionConstraints, T referredPolicy)
		{
			super(policyIdRef, versionConstraints, (Class<T>) referredPolicy.getClass());
			this.referredPolicy = referredPolicy;
		}

		@Override
		public final DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
		{
			return referredPolicy.evaluate(context, skipTarget);
		}

		@Override
		public final boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
		{
			try
			{
				return referredPolicy.isApplicable(context);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Error checking whether Policy(Set) referenced by " + this, e.getStatusCode() + " is applicable to the request context", e);
			}
		}

	}

	private static class Dynamic<T extends IPolicy> extends PolicyReference<T>
	{

		// this policyFinder to use in finding the referenced policy
		private final transient RefPolicyFinder refPolicyFinder;

		/*
		 * (Do not use a Queue as it is FIFO, and we need LIFO and iteration in order of insertion,
		 * so different from Collections.asLifoQueue(Deque) as well.)
		 */
		private final transient Deque<String> policySetRefChain;

		private Dynamic(String policyIdRef, VersionConstraints versionConstraints, Class<T> policyReferenceType, RefPolicyFinder refPolicyFinder, Deque<String> policyRefChain)
		{
			super(policyIdRef, versionConstraints, policyReferenceType);
			if (refPolicyFinder == null)
			{
				throw new IllegalArgumentException("Undefined policy policyFinder");
			}

			this.refPolicyFinder = refPolicyFinder;
			this.policySetRefChain = policyRefChain;
		}

		/**
		 * Resolves this to the actual Policy
		 * 
		 * @throws ParsingException
		 *             Error parsing the policy referenced by this. The referenced policy may be
		 *             parsed on the fly, when calling this method.
		 * @throws IndeterminateEvaluationException
		 *             if error determining the policy referenced by this, e.g. if more than one
		 *             policy is found
		 */
		private T resolve() throws ParsingException, IndeterminateEvaluationException
		{

			return refPolicyFinder.findPolicy(this.value, this.versionConstraints, this.referredPolicyClass, policySetRefChain);
		}

		@Override
		public final DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
		{
			// we must have found a policy
			try
			{
				return resolve().evaluate(context, skipTarget);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.info("Error resolving {} to the policy to evaluate in the request context", this, e);
				return new DecisionResult(e.getStatus());
			} catch (ParsingException e)
			{
				LOGGER.info("Error resolving {} to the policy to evaluate in the request context", this, e);
				return e.getIndeterminateResult();
			}
		}

		@Override
		public final boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
		{
			try
			{
				return resolve().isApplicable(context);
			} catch (ParsingException e)
			{
				throw new IndeterminateEvaluationException("Error resolving " + this + " to check whether the referenced policy is applicable to the request context", StatusHelper.STATUS_SYNTAX_ERROR, e);
			}
		}
	}

	/**
	 * Instantiates Policy(Set) Reference evaluator
	 * 
	 * @param idRef
	 *            Policy(Set)IdReference
	 * @param refPolicyFinder
	 *            Policy(Set)IdReference resolver/finder
	 * @param refPolicyType
	 *            type of policy referenced, i.e. whether it refers to Policy or PolicySet
	 * @param parentPolicySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the reference identified here
	 *            by {@code idRef} (exclusive): PolicySet Ref 1 -> PolicySet Ref 2 -> ... -> Ref n
	 *            -> {@code idRef}. This allows to detect circular references and validate the size
	 *            of the chain against the max depth enforced by {@code policyFinder}. This may be
	 *            null if no ancestor, e.g. a PolicySetIdReference in a top-level PolicySet. Beware
	 *            that we only keep the IDs in the chain, and not the version, because we consider
	 *            that a reference loop on the same policy ID is not allowed, no matter what the
	 *            version is.
	 * @return instance instance of PolicyReference
	 * @throws ParsingException
	 *             if PolicySetIdReference loop detected or PolicySetIdReference depth exceeds the
	 *             max enforced by {@code policyFinder}
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyFinder} undefined
	 */
	public static <T extends IPolicy> PolicyReference<T> getInstance(IdReferenceType idRef, RefPolicyFinder refPolicyFinder, Class<T> refPolicyType, Deque<String> parentPolicySetRefChain) throws ParsingException, IllegalArgumentException
	{
		if (refPolicyFinder == null)
		{
			throw new IllegalArgumentException("Policy(Set)IdReference resolver/finder undefined");
		}

		final VersionConstraints versionConstraints = new VersionConstraints(idRef.getVersion(), idRef.getEarliestVersion(), idRef.getLatestVersion());
		/*
		 * REMINDER: parentPolicySetRefChain is handled/updated by the refPolicyFinder. So do not
		 * modify it here, just pass the parameter. modify it here.
		 */
		if (refPolicyFinder.isStatic())
		{
			final T policy;
			try
			{
				policy = refPolicyFinder.findPolicy(idRef.getValue(), versionConstraints, refPolicyType, parentPolicySetRefChain);
			} catch (IndeterminateEvaluationException e)
			{
				throw new ParsingException("Error resolving statically or parsing " + toString(refPolicyType, idRef.getValue(), versionConstraints) + " into its referenced policy (via static policy finder)", e);
			}

			return new Static<>(idRef.getValue(), versionConstraints, policy);
		}

		// dynamic reference resolution
		return new Dynamic<>(idRef.getValue(), versionConstraints, refPolicyType, refPolicyFinder, parentPolicySetRefChain);
	}

}
