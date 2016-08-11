/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.List;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.MutableDecisionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the standard only-one-applicable policy combining algorithm.
 *
 * @version $Id: $
 */
final class OnlyOneApplicableAlg extends BaseCombiningAlg<PolicyEvaluator>
{

	private static class Evaluator implements CombiningAlg.Evaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

		private final MutableDecisionResult tooManyApplicablePoliciesIndeterminateResult;

		private final List<? extends PolicyEvaluator> policyElements;

		private Evaluator(final String algId, final List<? extends PolicyEvaluator> policyElements)
		{
			this.policyElements = policyElements;
			this.tooManyApplicablePoliciesIndeterminateResult = new MutableDecisionResult(new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR,
					"Too many (more than one) applicable policies for algorithm: " + algId));
		}

		@Override
		public DecisionResult eval(final EvaluationContext context)
		{
			// atLeastOne == true iff selectedPolicy != null
			PolicyEvaluator selectedPolicy = null;

			for (final PolicyEvaluator policy : policyElements)
			{
				// see if the policy applies to the context
				final boolean isApplicable;
				try
				{
					isApplicable = policy.isApplicable(context);
				} catch (final IndeterminateEvaluationException e)
				{
					LOGGER.info("Error checking whether {} is applicable", policy, e);
					return new MutableDecisionResult(e.getStatus());
				}

				if (isApplicable)
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

			// if we got through the loop, it means we found at most one match, then
			// we return the evaluation result of that policy if there is a match
			if (selectedPolicy != null)
			{
				return selectedPolicy.evaluate(context, true);
			}

			return MutableDecisionResult.NOT_APPLICABLE;
		}

	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(final List<CombiningAlgParameter<? extends PolicyEvaluator>> params, final List<? extends PolicyEvaluator> combinedElements)
	{
		return new Evaluator(this.getId(), combinedElements);
	}

	/**
	 * Standard constructor.
	 */
	OnlyOneApplicableAlg(final String algId)
	{
		super(algId, PolicyEvaluator.class);
	}

}
