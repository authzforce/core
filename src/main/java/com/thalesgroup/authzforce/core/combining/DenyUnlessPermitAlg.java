package com.thalesgroup.authzforce.core.combining;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;

/**
 * Deny-unless-permit combining algorithm
 * 
 */
public class DenyUnlessPermitAlg extends CombiningAlgorithm<Decidable>
{

	/**
	 * The standard URIs used to identify this algorithm
	 */
	public static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET = new CombiningAlgorithmSet(new DenyUnlessPermitAlg(SUPPORTED_IDENTIFIERS[0]), new DenyUnlessPermitAlg(SUPPORTED_IDENTIFIERS[1]));

	private DenyUnlessPermitAlg(String algId)
	{
		super(algId, Decidable.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.test.combine.PolicyCombiningAlgorithm#combine(com.thalesgroup.authzforce.core.test. EvaluationContext,
	 * java.util.List, java.util.List)
	 */
	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		DecisionResult combinedDenyResult = null;

		for (Decidable combinedElement : combinedElements)
		{
			// make sure that the policy matches the context
			final DecisionResult policyResult = combinedElement.evaluate(context);
			final DecisionType decision = policyResult.getDecision();
			switch (decision)
			{
				case PERMIT:
					return policyResult;
				case DENY:
					// merge result (obligations/advice)
					combinedDenyResult = policyResult.merge(combinedDenyResult);
					break;
				default:
					continue;
			}
		}

		return combinedDenyResult == null ? DecisionResult.DENY : combinedDenyResult;
	}

}
