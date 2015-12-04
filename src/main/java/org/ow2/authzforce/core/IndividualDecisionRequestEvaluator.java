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
package org.ow2.authzforce.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
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
	private static final Result PERMIT = new Result(DecisionType.PERMIT, null, null, null, null, null);
	private static final Result DENY = new Result(DecisionType.DENY, null, null, null, null, null);

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
				request.isApplicablePolicyIdListReturned());
		final PolicyDecisionResult result = rootPolicyEvaluator.findAndEvaluate(ctx);
		if (result == PolicyDecisionResult.PERMIT)
		{
			return PERMIT;
		}

		if (result == PolicyDecisionResult.DENY)
		{
			return DENY;
		}

		final List<Obligation> obligationList = result.getPepActions().getObligations();
		final List<Advice> adviceList = result.getPepActions().getAdvices();
		final List<JAXBElement<IdReferenceType>> applicablePolicyIdList = result.getApplicablePolicyIdList();

		return new Result(result.getDecision(), result.getStatus(),
				obligationList == null || obligationList.isEmpty() ? null : new Obligations(obligationList), adviceList == null || adviceList.isEmpty() ? null
						: new AssociatedAdvice(adviceList), request.getReturnedAttributes(),
				applicablePolicyIdList == null || applicablePolicyIdList.isEmpty() ? null : new PolicyIdentifierList(applicablePolicyIdList));
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
	public List<Result> evaluate(List<? extends IndividualDecisionRequest> individualDecisionRequests, Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
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