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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.impl.MutableDecisionResult;

/**
 * This is the standard Permit-Overrides policy/rule combining algorithm. It allows a single evaluation of Permit to take precedence over any number of deny, not applicable or indeterminate results.
 * Note that since this implementation does an ordered evaluation, this class also supports the Ordered-Permit-Overrides algorithm.
 *
 * 
 * @version $Id: $
 */
final class PermitOverridesAlg extends BaseCombiningAlg<Decidable>
{

	private static class Evaluator implements CombiningAlg.Evaluator
	{

		private final List<? extends Decidable> combinedElements;

		private Evaluator(final List<? extends Decidable> combinedElements)
		{
			this.combinedElements = combinedElements;
		}

		@Override
		public DecisionResult eval(final EvaluationContext context)
		{
			/*
			 * Replaces atLeastOneErrorDP from XACML spec. atLeastOneErrorDP == true <=> firstIndeterminateDPResult != null
			 */
			DecisionResult firstIndeterminateDPResult = null;
			/*
			 * Replaces atLeastOneErrorD from XACML spec. atLeastOneErrorD == true <=> firstIndeterminateDResult != null
			 */
			DecisionResult firstIndeterminateDResult = null;
			/*
			 * Replaces atLeastOneErrorP from XACML spec. atLeastOneErrorP == true <=> firstIndeterminatePResult != null
			 */
			DecisionResult firstIndeterminatePResult = null;

			/*
			 * Replaces and enhances atLeastOneDeny from XACML spec. atLeastOneDeny == true <=> combinedDenyResult != null
			 */
			DecisionResult combinedDenyResult = null;

			for (final Decidable combinedElement : combinedElements)
			{
				// evaluate the policy
				final DecisionResult result = combinedElement.evaluate(context);
				switch (result.getDecision())
				{
				case PERMIT:
					return result;
				case DENY:
					// merge the obligations, etc. in case the final result is Deny
					if (combinedDenyResult == null)
					{
						combinedDenyResult = result;
					} else
					{
						combinedDenyResult.merge(result.getPepActions(), result.getApplicablePolicyIdList());
					}
					break;
				case INDETERMINATE:
					/*
					 * Save Extended Indeterminate value if this is a new type of such value, till the end because needed to compute final Extended Indeterminate value
					 */
					switch (result.getExtendedIndeterminate())
					{
					case INDETERMINATE:
						if (firstIndeterminateDPResult == null)
						{
							firstIndeterminateDPResult = result;
						}
						break;
					case DENY:
						if (firstIndeterminateDResult == null)
						{
							firstIndeterminateDResult = result;
						}
						break;
					case PERMIT:
						if (firstIndeterminatePResult == null)
						{
							firstIndeterminatePResult = result;
						}
						break;
					default:

					}
					break;
				default:
					break;
				}
			}

			/*
			 * There was no Permit, else: if any Indeterminate{DP}, then Indeterminate{DP}
			 */
			if (firstIndeterminateDPResult != null)
			{
				// at least one Indeterminate{DP}
				return firstIndeterminateDPResult;
			}

			/*
			 * Else if any Indeterminate{P}, then: if ( any Indeterminate{D} or any Deny ) -> Indeterminate{DP}; else -> Indeterminate{P} (this is a simplified equivalent of the algo in the spec)
			 */
			if (firstIndeterminatePResult != null)
			{
				return new MutableDecisionResult(firstIndeterminatePResult.getStatus(), firstIndeterminateDResult != null || combinedDenyResult != null ? DecisionType.INDETERMINATE : DecisionType.PERMIT);
			}

			/*
			 * atLeastOneDeny == true <=> combinedDenyResult != null
			 */
			if (combinedDenyResult != null)
			{
				return combinedDenyResult;
			}

			if (firstIndeterminateDResult != null)
			{
				return firstIndeterminateDResult;
			}

			return MutableDecisionResult.NOT_APPLICABLE;
		}

	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(final List<CombiningAlgParameter<? extends Decidable>> params, final List<? extends Decidable> combinedElements)
	{
		return new Evaluator(combinedElements);
	}

	PermitOverridesAlg(final String algId)
	{
		super(algId, Decidable.class);
	}
}
