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
package org.ow2.authzforce.core.policy;

import java.util.Deque;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;

/**
 * This class is responsible for evaluating XACML Policy(Set)IdReferences.
 * 
 * @param <T>
 *            type of referred element: Policy, PolicySet...
 * 
 */
public abstract class PolicyReferenceEvaluator<T extends IPolicyEvaluator> extends IdReferenceType implements IPolicyEvaluator
{
	static final Logger LOGGER = LoggerFactory.getLogger(PolicyReferenceEvaluator.class);

	private static final UnsupportedOperationException UNSUPPORTED_SET_VALUE_EXCEPTION = new UnsupportedOperationException(
			"ID in Policy(Set)IdReference is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_VERSION_EXCEPTION = new UnsupportedOperationException(
			"Version in Policy(Set)IdReference is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_EARLIEST_VERSION_EXCEPTION = new UnsupportedOperationException(
			"EarliestVersion in Policy(Set)IdReference is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_SET_LATEST_VERSION_EXCEPTION = new UnsupportedOperationException(
			"LatestVersion in Policy(Set)IdReference is read-only");

	// and version constraints on this reference
	protected final VersionConstraints versionConstraints;

	protected final Class<T> referredPolicyClass;

	private transient volatile String toString = null;

	protected PolicyReferenceEvaluator(String idRef, VersionConstraints versionConstraints, Class<T> policyReferenceType)
	{
		this.versionConstraints = versionConstraints;
		this.referredPolicyClass = policyReferenceType;
		this.toString = toString(referredPolicyClass, idRef, versionConstraints);
	}

	private static String toString(Class<? extends IPolicyEvaluator> policyReferenceType, String policyRefId, VersionConstraints versionConstraints)
	{
		return (policyReferenceType == PolicyEvaluator.class ? "PolicyIdReference" : "PolicySetIdReference") + "[Id=" + policyRefId + ", " + versionConstraints
				+ "]";
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType#setEarliestVersion(java.lang. String)
	 */
	@Override
	public final void setEarliestVersion(String value)
	{
		throw UNSUPPORTED_SET_EARLIEST_VERSION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType#setLatestVersion(java.lang.String )
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

	@Override
	public String getPolicyId()
	{
		// IdReference
		return this.value;
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
	 *            chain of ancestor PolicySetIdReferences leading to the reference identified here by {@code idRef} (exclusive): PolicySet Ref 1 -> PolicySet
	 *            Ref 2 -> ... -> Ref n -> {@code idRef}. This allows to detect circular references and validate the size of the chain against the max depth
	 *            enforced by {@code policyFinder}. This may be null if no ancestor, e.g. a PolicySetIdReference in a top-level PolicySet. Beware that we only
	 *            keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what
	 *            the version is.
	 * @return instance instance of PolicyReference
	 * @throws ParsingException
	 *             if PolicySetIdReference loop detected or PolicySetIdReference depth exceeds the max enforced by {@code policyFinder}
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyFinder} undefined
	 */
	public static <T extends IPolicyEvaluator> PolicyReferenceEvaluator<T> getInstance(IdReferenceType idRef, RefPolicyFinder refPolicyFinder,
			Class<T> refPolicyType, Deque<String> parentPolicySetRefChain) throws ParsingException, IllegalArgumentException
	{
		if (refPolicyFinder == null)
		{
			throw new IllegalArgumentException("Policy(Set)IdReference resolver/finder undefined");
		}

		final VersionConstraints versionConstraints = new VersionConstraints(idRef.getVersion(), idRef.getEarliestVersion(), idRef.getLatestVersion());
		/*
		 * REMINDER: parentPolicySetRefChain is handled/updated by the refPolicyFinder. So do not modify it here, just pass the parameter. modify it here.
		 */
		if (refPolicyFinder.isStatic())
		{
			final T policy;
			try
			{
				policy = refPolicyFinder.findPolicy(idRef.getValue(), versionConstraints, refPolicyType, parentPolicySetRefChain);
			} catch (IndeterminateEvaluationException e)
			{
				throw new ParsingException("Error resolving statically or parsing " + toString(refPolicyType, idRef.getValue(), versionConstraints)
						+ " into its referenced policy (via static policy finder)", e);
			}

			return new StaticPolicyRefEvaluator<>(idRef.getValue(), versionConstraints, policy);
		}

		// dynamic reference resolution
		return new DynamicPolicyRefEvaluator<>(idRef.getValue(), versionConstraints, refPolicyType, refPolicyFinder, parentPolicySetRefChain);
	}

}
