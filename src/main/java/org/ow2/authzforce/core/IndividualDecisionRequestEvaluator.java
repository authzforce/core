package org.ow2.authzforce.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.value.Bag;

/**
 * Individual decision request evaluator
 *
 */
public class IndividualDecisionRequestEvaluator
{
	private final RootPolicyEvaluator rootPolicyEvaluator;

	/**
	 * Creates an evaluator
	 * 
	 * @param rootPolicyEvaluator
	 *            root policy evaluator that this request evaluator uses to evaluate individual decision request
	 */
	public IndividualDecisionRequestEvaluator(RootPolicyEvaluator rootPolicyEvaluator)
	{
		assert rootPolicyEvaluator != null;
		this.rootPolicyEvaluator = rootPolicyEvaluator;
	}

	protected final Result evaluate(IndividualDecisionRequest request, Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
	{
		// convert to EvaluationContext
		final Map<AttributeGUID, Bag<?>> namedAttributes = request.getNamedAttributes();
		namedAttributes.putAll(pdpIssuedAttributes);
		final EvaluationContext ctx = new IndividualDecisionRequestContext(namedAttributes, request.getExtraContentsByCategory(),
				request.getDefaultXPathCompiler());
		final DecisionResult result = rootPolicyEvaluator.findAndEvaluate(ctx);
		result.setAttributes(request.getAttributesIncludedInResult());
		return result;
	}

	/**
	 * Evaluates individual decision requests
	 * 
	 * @param individualDecisionRequests
	 *            individual decision requests
	 * @param pdpIssuedAttributes
	 *            PDP-issued attributes to be combined with the attributes of each individual decision request (e.g. date/time)
	 * @return evaluation results, one per individual decision request
	 */
	public List<Result> evaluate(List<IndividualDecisionRequest> individualDecisionRequests, Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
	{
		final List<Result> results = new ArrayList<>(individualDecisionRequests.size());
		for (final IndividualDecisionRequest request : individualDecisionRequests)
		{
			final Result result = evaluate(request, pdpIssuedAttributes);
			results.add(result);
		}

		return results;
	}
}