package org.ow2.authzforce.core.policy;

import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;

import com.sun.xacml.VersionConstraints;

class StaticPolicyRefEvaluator<P extends IPolicyEvaluator> extends PolicyReferenceEvaluator<P>
{
	private final transient P referredPolicy;

	StaticPolicyRefEvaluator(String policyIdRef, VersionConstraints versionConstraints, P referredPolicy)
	{
		super(policyIdRef, versionConstraints, (Class<P>) referredPolicy.getClass());
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
			throw new IndeterminateEvaluationException("Error checking whether Policy(Set) referenced by " + this, e.getStatusCode()
					+ " is applicable to the request context", e);
		}
	}

	@Override
	public String getCombiningAlgId()
	{
		return referredPolicy.getCombiningAlgId();
	}

}