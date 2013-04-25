/**
 * 
 */
package com.sun.xacml.combine;

import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;

/**
 * @author Romain Ferrari
 * 
 */
public class DenyUnlessPermitPolicyAlg extends PolicyCombiningAlgorithm {

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:"
			+ "deny-unless-permit";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);
	
	/**
	 * Standard constructor
	 */
	public DenyUnlessPermitPolicyAlg() {
		super(identifierURI);
	}

	/**
	 * @param identifier
	 */
	public DenyUnlessPermitPolicyAlg(URI identifier) {
		super(identifier);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.xacml.combine.PolicyCombiningAlgorithm#combine(com.sun.xacml.
	 * EvaluationCtx, java.util.List, java.util.List)
	 */
	@Override
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List policyElements) {
		Result result = null;
		for (PolicyCombinerElement myPolicyCombinerElt : (List<PolicyCombinerElement>) policyElements) {
			Policy policy = myPolicyCombinerElt.getPolicy();
			// make sure that the policy matches the context
			MatchResult match = policy.match(context);
			if (match.getResult() == MatchResult.MATCH) {
				result = policy.evaluate(context);
				int value = result.getDecision().ordinal();
				if (value == Result.DECISION_PERMIT) {
					return result;
				} 
			}
		}
		return new Result(DecisionType.DENY, context
				.getResourceId().encode(), result.getObligations());
	}

}
