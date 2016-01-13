/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.DecisionResult;

import com.sun.xacml.VersionConstraints;

class StaticPolicyRefEvaluator<P extends IPolicyEvaluator> extends PolicyReferenceEvaluator<P>
{
	private static final IllegalArgumentException UNDEF_POLICY_EXCEPTION = new IllegalArgumentException("undefined policy as target of static policy reference");
	private final transient P referredPolicy;
	private final List<String> longestPolicyRefChain;

	StaticPolicyRefEvaluator(String policyIdRef, VersionConstraints versionConstraints, P referredPolicy)
	{
		super(policyIdRef, versionConstraints, (Class<P>) validate(referredPolicy).getClass());
		this.referredPolicy = referredPolicy;
		final List<String> referredPolicyLongestRefChain = referredPolicy.getLongestPolicyReferenceChain();
		if (referredPolicyLongestRefChain == null)
		{
			longestPolicyRefChain = Collections.singletonList(this.refPolicyId);
		} else
		{
			final List<String> mutableChain = new ArrayList<>();
			mutableChain.add(this.refPolicyId);
			mutableChain.addAll(referredPolicyLongestRefChain);
			this.longestPolicyRefChain = Collections.unmodifiableList(mutableChain);
		}
	}

	private static <P extends IPolicyEvaluator> P validate(P referredPolicy)
	{
		if (referredPolicy == null)
		{
			throw UNDEF_POLICY_EXCEPTION;
		}

		return referredPolicy;
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
			throw new IndeterminateEvaluationException("Error checking whether Policy(Set) referenced by " + this, e.getStatusCode()
					+ " is applicable to the request context", e);
		}
	}

	@Override
	public String getCombiningAlgId()
	{
		return referredPolicy.getCombiningAlgId();
	}

	@Override
	public List<String> getLongestPolicyReferenceChain()
	{
		return this.longestPolicyRefChain;
	}

}