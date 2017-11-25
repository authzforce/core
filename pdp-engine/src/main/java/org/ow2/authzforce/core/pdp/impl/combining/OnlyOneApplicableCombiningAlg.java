/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.combining;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the standard only-one-applicable policy combining algorithm.
 *
 * @version $Id: $
 */
final class OnlyOneApplicableCombiningAlg extends BaseCombiningAlg<PolicyEvaluator>
{

	private static final class Evaluator extends BaseCombiningAlg.Evaluator<PolicyEvaluator>
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

		private final ExtendedDecision tooManyApplicablePoliciesIndeterminateResult;

		private Evaluator(final String algId, final Iterable<? extends PolicyEvaluator> policyElements)
		{
			super(policyElements);
			this.tooManyApplicablePoliciesIndeterminateResult = ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE, new IndeterminateEvaluationException(
					"Too many (more than one) applicable policies for algorithm: " + algId, XacmlStatusCode.PROCESSING_ERROR.value()));
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
		{
			assert outPepActions != null;

			// atLeastOne == true iff selectedPolicy != null
			PolicyEvaluator selectedPolicy = null;

			for (final PolicyEvaluator policy : getCombinedElements())
			{
				// see if the policy applies to the context
				final boolean isApplicableByTarget;
				try
				{
					isApplicableByTarget = policy.isApplicableByTarget(context);
				}
				catch (final IndeterminateEvaluationException e)
				{
					LOGGER.info("Error checking whether {} is applicable", policy, e);
					return ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE, e);
				}

				if (isApplicableByTarget)
				{
					// if one selected (found applicable) already
					if (selectedPolicy != null)
					{
						return tooManyApplicablePoliciesIndeterminateResult;
					}

					// if this was the first applicable policy in the set, then
					// remember it for later
					selectedPolicy = policy;
				}
			}

			/*
			 * If we got through the loop, it means we found at most one match, then we return the evaluation result of that policy if there is a match
			 */
			if (selectedPolicy != null)
			{
				final DecisionResult result = selectedPolicy.evaluate(context, true);
				switch (result.getDecision())
				{
					case PERMIT:
					case DENY:
						outPepActions.add(result.getPepActions());
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						break;
					case INDETERMINATE:
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						break;
					default: // NotApplicable
						break;
				}

				return result;
			}

			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends PolicyEvaluator>> params, final Iterable<? extends PolicyEvaluator> combinedElements)
	{
		return new Evaluator(this.getId(), combinedElements);
	}

	/**
	 * Standard constructor.
	 */
	OnlyOneApplicableCombiningAlg(final String algId)
	{
		super(algId, PolicyEvaluator.class);
	}

}
