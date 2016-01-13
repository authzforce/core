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
package org.ow2.authzforce.core.pdp.impl;

import java.util.HashMap;
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

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.Bag;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;

/**
 * Individual decision request evaluator
 *
 */
public abstract class IndividualDecisionRequestEvaluator
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
	protected IndividualDecisionRequestEvaluator(RootPolicyEvaluator rootPolicyEvaluator)
	{
		assert rootPolicyEvaluator != null;
		this.rootPolicyEvaluator = rootPolicyEvaluator;
	}

	protected final Result evaluate(IndividualDecisionRequest request, Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
	{
		assert request != null;

		// convert to EvaluationContext
		/*
		 * The pdpIssuedAttributes may be re-used for many individual requests, so we must not modify it but clone it before individual decision request
		 * processing
		 */
		final Map<AttributeGUID, Bag<?>> pdpEnhancedNamedAttributes = pdpIssuedAttributes == null ? new HashMap<AttributeGUID, Bag<?>>() : new HashMap<>(
				pdpIssuedAttributes);
		final Map<AttributeGUID, Bag<?>> reqNamedAttributes = request.getNamedAttributes();
		if (reqNamedAttributes != null)
		{
			pdpEnhancedNamedAttributes.putAll(reqNamedAttributes);
		}

		final EvaluationContext ctx = new IndividualDecisionRequestContext(pdpEnhancedNamedAttributes, request.getExtraContentsByCategory(),
				request.isApplicablePolicyIdentifiersReturned());
		final DecisionResult result = rootPolicyEvaluator.findAndEvaluate(ctx);
		if (result == BaseDecisionResult.PERMIT)
		{
			return PERMIT;
		}

		if (result == BaseDecisionResult.DENY)
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

	protected abstract List<Result> evaluate(List<? extends IndividualDecisionRequest> individualDecisionRequests,
			Map<AttributeGUID, Bag<?>> pdpIssuedAttributes);
}