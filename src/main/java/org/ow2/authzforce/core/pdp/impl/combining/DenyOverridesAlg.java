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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgSet;
import org.ow2.authzforce.core.pdp.impl.BaseDecisionResult;

/**
 * This is the standard XACML 3.0 Deny-Overrides policy/rule combining algorithm. It allows a single evaluation of Deny to take precedence over any number of permit, not applicable or indeterminate
 * results. Note that since this implementation does an ordered evaluation, this class also supports the Ordered-Deny-Overrides-algorithm.
 *
 * @version $Id: $
 */
public final class DenyOverridesAlg extends BaseCombiningAlg<Decidable>
{
	private static class Evaluator implements CombiningAlg.Evaluator
	{

		private final List<? extends Decidable> combinedElements;

		private Evaluator(List<? extends Decidable> combinedElements)
		{
			this.combinedElements = combinedElements;
		}

		@Override
		public DecisionResult eval(EvaluationContext context)
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
			 * Replaces atLeastOnePermit from XACML spec. atLeastOnePermit == true <=> combinedPermitResult != null
			 */
			DecisionResult combinedPermitResult = null;

			for (final Decidable combinedElement : combinedElements)
			{
				// evaluate the policy
				final DecisionResult result = combinedElement.evaluate(context);
				switch (result.getDecision())
				{
				case DENY:
					return result;
				case PERMIT:
					if (combinedPermitResult == null)
					{
						combinedPermitResult = result;
					} else
					{
						combinedPermitResult.merge(result.getPepActions(), result.getApplicablePolicyIdList());
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
				return new BaseDecisionResult(firstIndeterminateDResult.getStatus(), firstIndeterminatePResult != null || combinedPermitResult != null ? DecisionType.INDETERMINATE : DecisionType.DENY);
			}

			// if we got a PERMIT or Indeterminate{P}, return it, otherwise it's NOT_APPLICABLE
			if (combinedPermitResult != null)
			{
				return combinedPermitResult;
			}

			if (firstIndeterminatePResult != null)
			{
				return firstIndeterminatePResult;
			}

			return BaseDecisionResult.NOT_APPLICABLE;
		}
	}

	/**
	 * The standard URIs used to identify this algorithm
	 */
	private static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides",
			"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides",
			"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgSet SET;
	static
	{
		final Set<CombiningAlg<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new DenyOverridesAlg(algId));
		}

		SET = new CombiningAlgSet(algSet);
	}

	private DenyOverridesAlg(String algId)
	{
		super(algId, Decidable.class);
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(List<CombiningAlgParameter<? extends Decidable>> params, List<? extends Decidable> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		return new Evaluator(combinedElements);
	}

}
