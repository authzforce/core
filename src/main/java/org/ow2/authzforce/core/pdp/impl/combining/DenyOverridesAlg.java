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
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.MutablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.impl.MutableDecisionResult;

/**
 * This is the standard XACML 3.0 Deny-Overrides policy/rule combining algorithm. It allows a single evaluation of Deny to take precedence over any number of permit, not applicable or indeterminate
 * results. Note that since this implementation does an ordered evaluation, this class also supports the Ordered-Deny-Overrides-algorithm.
 *
 * @version $Id: $
 */
final class DenyOverridesAlg extends BaseCombiningAlg<Decidable>
{

	private static class Evaluator implements CombiningAlg.Evaluator
	{

		private final List<? extends Decidable> combinedElements;

		private Evaluator(final List<? extends Decidable> combinedElements)
		{
			this.combinedElements = combinedElements;
		}

		@Override
		public DecisionResult eval(final EvaluationContext context, final MutablePepActions mutablePepActions)
		{
			assert mutablePepActions != null;

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

			boolean atLeastOnePermit = false;
			MutablePepActions permitPepActions = null;

			for (final Decidable combinedElement : combinedElements)
			{
				// evaluate the policy
				final DecisionResult result = combinedElement.evaluate(context);

				/*
				 * XACML §7.18: Obligations & Advice: do not return obligations/Advice of the rule, policy, or policy set that does not match the decision resulting from evaluating the enclosing
				 * policy set
				 */

				switch (result.getDecision())
				{
					case DENY:
						mutablePepActions.add(result.getPepActions());
						return DecisionResults.SIMPLE_DENY;
					case PERMIT:
						if (atLeastOnePermit)
						{
							assert permitPepActions != null;
							// not the first permit, pepActionsList already initialized
							permitPepActions.add(result.getPepActions());
							break;
						}

						atLeastOnePermit = true;
						permitPepActions = new MutablePepActions();
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
			 * There was no Deny, else: if any Indeterminate{DP}, then Indeterminate{DP}
			 */
			if (firstIndeterminateDPResult != null)
			{
				// at least one Indeterminate{DP}
				return firstIndeterminateDPResult;
			}

			/*
			 * Else if any Indeterminate{D}, then: if ( any Indeterminate{P} or any Permit ) -> Indeterminate{DP}; else -> Indeterminate{D} (this is a simplified equivalent of the algo in the spec)
			 */
			if (firstIndeterminateDResult != null)
			{
				return new MutableDecisionResult(firstIndeterminateDResult.getStatus(), firstIndeterminatePResult != null || atLeastOnePermit ? DecisionType.INDETERMINATE : DecisionType.DENY);
			}

			// if we got a PERMIT or Indeterminate{P}, return it, otherwise it's NOT_APPLICABLE
			if (atLeastOnePermit)
			{
				mutablePepActions.add(permitPepActions);
				return DecisionResults.SIMPLE_PERMIT;
			}

			if (firstIndeterminatePResult != null)
			{
				return firstIndeterminatePResult;
			}

			return DecisionResults.SIMPLE_NOT_APPLICABLE;
		}
	}

	DenyOverridesAlg(final String algId)
	{
		super(algId, Decidable.class);
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final List<CombiningAlgParameter<? extends Decidable>> params, final List<? extends Decidable> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		return new Evaluator(combinedElements);
	}

}
