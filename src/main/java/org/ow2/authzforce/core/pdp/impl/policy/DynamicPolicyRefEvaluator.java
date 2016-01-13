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
package org.ow2.authzforce.core.pdp.impl.policy;

import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.RefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.VersionPatterns;
import org.ow2.authzforce.core.pdp.impl.BaseDecisionResult;

import com.sun.xacml.ParsingException;

class DynamicPolicyRefEvaluator<T extends IPolicyEvaluator> extends PolicyReferenceEvaluator<T>
{

	private static final UnsupportedOperationException UNSUPPORTED_DYNAMIC_GET_COMBINING_ALG_ID = new UnsupportedOperationException(
			"Unable to get Combining algorithm ID out of context for a dynamic PolicyReference");

	// this policyProvider to use in finding the referenced policy
	private final transient RefPolicyProvider refPolicyProvider;

	/*
	 * Chain of Policy Reference leading from root policy down to this reference (excluded) (Do not use a Queue as it is FIFO, and we need LIFO and iteration in
	 * order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 */
	private final transient Deque<String> ancestorPolicyRefChain;

	DynamicPolicyRefEvaluator(String policyIdRef, VersionPatterns versionConstraints, Class<T> policyReferenceType, RefPolicyProvider refPolicyProvider,
			Deque<String> ancestorPolicyRefChain)
	{
		super(policyIdRef, versionConstraints, policyReferenceType);
		if (refPolicyProvider == null)
		{
			throw new IllegalArgumentException("Undefined policy policyProvider");
		}

		this.refPolicyProvider = refPolicyProvider;
		this.ancestorPolicyRefChain = ancestorPolicyRefChain;
	}

	/**
	 * Resolves this to the actual Policy
	 * 
	 * @throws ParsingException
	 *             Error parsing the policy referenced by this. The referenced policy may be parsed on the fly, when calling this method.
	 * @throws IndeterminateEvaluationException
	 *             if error determining the policy referenced by this, e.g. if more than one policy is found
	 */
	private T resolve() throws ParsingException, IndeterminateEvaluationException
	{

		return refPolicyProvider.get(this.referredPolicyClass, this.refPolicyId, this.versionConstraints, ancestorPolicyRefChain);
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
			return new BaseDecisionResult(e.getStatus());
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
			throw new IndeterminateEvaluationException("Error resolving " + this
					+ " to check whether the referenced policy is applicable to the request context", StatusHelper.STATUS_SYNTAX_ERROR, e);
		}
	}

	@Override
	public String getCombiningAlgId()
	{
		throw UNSUPPORTED_DYNAMIC_GET_COMBINING_ALG_ID;
	}

	@Override
	public List<String> getLongestPolicyReferenceChain()
	{
		// computed dynamically at evaluation time, see resolve() method
		return null;
	}
}