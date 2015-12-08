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

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.DecisionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.VersionConstraints;

/**
 * This class is responsible for evaluating XACML Policy(Set)IdReferences.
 * 
 * @param <T>
 *            type of referred element: Policy, PolicySet...
 * 
 */
public abstract class PolicyReferenceEvaluator<T extends IPolicyEvaluator> implements IPolicyEvaluator
{
	static final Logger LOGGER = LoggerFactory.getLogger(PolicyReferenceEvaluator.class);

	protected final String refPolicyId;
	// and version constraints on this reference
	protected final VersionConstraints versionConstraints;
	protected final Class<T> referredPolicyClass;
	private final String toString;

	/**
	 * Get Policy reference description
	 * 
	 * @param policyReferenceType
	 *            type of Policy reference (PolicySetIdReference or PolicyIdReference)
	 * @param policyRefId
	 *            referenced policy ID
	 * @param versionConstraints
	 *            referenced policy version constraints
	 * @return description
	 */
	public static String toString(Class<? extends IPolicyEvaluator> policyReferenceType, String policyRefId, VersionConstraints versionConstraints)
	{
		return (policyReferenceType == PolicyEvaluator.class ? "PolicyIdReference" : "PolicySetIdReference") + "[Id=" + policyRefId + ", " + versionConstraints
				+ "]";
	}

	protected PolicyReferenceEvaluator(String idRef, VersionConstraints versionConstraints, Class<T> policyReferenceType)
	{
		this.refPolicyId = idRef;
		this.versionConstraints = versionConstraints;
		this.referredPolicyClass = policyReferenceType;
		this.toString = toString(referredPolicyClass, idRef, versionConstraints);
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
		return this.refPolicyId;
	}

}
