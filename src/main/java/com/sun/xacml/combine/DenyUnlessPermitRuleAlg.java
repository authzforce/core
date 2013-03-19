/**
 * 
 */
package com.sun.xacml.combine;

import java.net.URI;
import java.util.List;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;

/**
 * @author Romain Ferrari
 * 
 */
public class DenyUnlessPermitRuleAlg extends RuleCombiningAlgorithm {

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:"
			+ "deny-unless-permit";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);
	
	/**
	 * Standard constructor
	 */
	public DenyUnlessPermitRuleAlg() {
		super(identifierURI);
	}

	/**
	 * @param identifier
	 */
	public DenyUnlessPermitRuleAlg(URI identifier) {
		super(identifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.xacml.combine.RuleCombiningAlgorithm#combine(com.sun.xacml.
	 * EvaluationCtx, java.util.List, java.util.List)
	 */
	/**
	 * Combines the rules based on the context to produce some unified result.
	 * This is the one function of a combining algorithm.
	 * 
	 * @param context
	 *            the representation of the request
	 * @param parameters
	 *            a (possibly empty) non-null <code>List</code> of
	 *            <code>CombinerParameter<code>s
	 * @param ruleElements
	 *            a <code>List</code> of <code>CombinerElement<code>s
	 * 
	 * @return a single unified result based on the combining logic
	 */
	@Override
	public Result combine(EvaluationCtx context, List parameters,
			List ruleElements) {
		for (RuleCombinerElement myRuleCombinerElt : (List<RuleCombinerElement>)ruleElements) {
			Rule rule = myRuleCombinerElt.getRule();
			Result result = rule.evaluate(context);
            int value = result.getDecision();
			if (value == Result.DECISION_PERMIT) {
				return result;
			}
		}

		return new Result(Result.DECISION_DENY, context.getResourceId().encode());
	}
	

}
