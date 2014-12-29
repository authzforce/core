package com.sun.xacml.combine;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;

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
	 *            combined rules
	 * 
	 * @return a single unified result based on the combining logic
	 */
	@Override
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List<Rule> ruleElements) {
		
		final Obligations combinedObligations = new Obligations();
		final AssociatedAdvice combinedAssociatedAdvice = new AssociatedAdvice();
		for (Rule rule: ruleElements) {
			final Result result = rule.evaluate(context);
			if(result.getDecision() == DecisionType.PERMIT) {
				return result;
			}
			
			final Obligations resultObligations = result.getObligations();
			if(resultObligations != null) {
				combinedObligations.getObligations().addAll(resultObligations.getObligations());
			}
			
			final AssociatedAdvice resultAssociatedAdvice = result.getAssociatedAdvice();
			if(resultAssociatedAdvice != null) {
				combinedAssociatedAdvice.getAdvices().addAll(resultAssociatedAdvice.getAdvices());
			}
		}
		
		return new Result(DecisionType.DENY, null, context.getResourceId().encode(), combinedObligations, combinedAssociatedAdvice, null);
	}
	

}
