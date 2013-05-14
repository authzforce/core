/**
 * 
 */
package com.sun.xacml.combine;

import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;

/**
 * @author Romain Ferrari
 * 
 */
public class PermitUnlessDenyRuleAlg extends RuleCombiningAlgorithm {

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:"
			+ "permit-unless-deny";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);

	/**
	 * Standard constructor
	 */
	public PermitUnlessDenyRuleAlg() {
		super(identifierURI);
	}

	/**
	 * @param identifier
	 */
	public PermitUnlessDenyRuleAlg(URI identifier) {
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
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List ruleElements) {
		for (RuleCombinerElement myRuleCombinerElt : (List<RuleCombinerElement>) ruleElements) {
			Rule rule = (Rule)myRuleCombinerElt.getRule();
			Result result = rule.evaluate(context);
            int value = result.getDecision().ordinal();
			if (value == DecisionType.DENY.ordinal()) {
				return result;
			}
		}

		return new Result(DecisionType.PERMIT, context.getResourceId()
				.encode());
	}

}
