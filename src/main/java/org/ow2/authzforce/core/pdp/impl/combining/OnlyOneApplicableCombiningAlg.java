/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
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
			this.tooManyApplicablePoliciesIndeterminateResult = ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE, new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR,
					"Too many (more than one) applicable policies for algorithm: " + algId));
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList)
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
					return ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE, e.getStatus());
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
