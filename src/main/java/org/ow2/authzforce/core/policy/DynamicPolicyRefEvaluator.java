package org.ow2.authzforce.core.policy;

import java.util.Deque;

import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;

class DynamicPolicyRefEvaluator<T extends IPolicyEvaluator> extends PolicyReferenceEvaluator<T>
{

	private static final UnsupportedOperationException UNSUPPORTED_DYNAMIC_GET_COMBINING_ALG_ID = new UnsupportedOperationException(
			"Unable to get Combining algorithm ID out of context for a dynamic PolicyReference");

	// this policyProvider to use in finding the referenced policy
	private final transient RefPolicyProvider refPolicyProvider;

	/*
	 * (Do not use a Queue as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 */
	private final transient Deque<String> policySetRefChain;

	DynamicPolicyRefEvaluator(String policyIdRef, VersionConstraints versionConstraints, Class<T> policyReferenceType, RefPolicyProvider refPolicyProvider,
			Deque<String> policyRefChain)
	{
		super(policyIdRef, versionConstraints, policyReferenceType);
		if (refPolicyProvider == null)
		{
			throw new IllegalArgumentException("Undefined policy policyProvider");
		}

		this.refPolicyProvider = refPolicyProvider;
		this.policySetRefChain = policyRefChain;
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

		return refPolicyProvider.get(this.value, this.versionConstraints, this.referredPolicyClass, policySetRefChain);
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
			throw new IndeterminateEvaluationException("Error resolving " + this
					+ " to check whether the referenced policy is applicable to the request context", StatusHelper.STATUS_SYNTAX_ERROR, e);
		}
	}

	@Override
	public String getCombiningAlgId()
	{
		throw UNSUPPORTED_DYNAMIC_GET_COMBINING_ALG_ID;
	}
}