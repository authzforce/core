/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.combining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.Decidable;
import com.thalesgroup.authzforce.core.DecisionResult;
import com.thalesgroup.authzforce.core.EvaluationContext;

/**
 * This is the standard XACML 3.0 Deny-Overrides policy/rule combining algorithm. It allows a single
 * evaluation of Deny to take precedence over any number of permit, not applicable or indeterminate
 * results. Note that since this implementation does an ordered evaluation, this class also supports
 * the Ordered-Deny-Overrides-algorithm.
 */
public final class DenyOverridesAlg extends CombiningAlgorithm<Decidable>
{

	/**
	 * The standard URIs used to identify this algorithm
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides",
			"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET;
	static
	{
		final Set<CombiningAlgorithm<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new DenyOverridesAlg(algId));
		}

		SET = new CombiningAlgorithmSet(algSet);
	}

	private DenyOverridesAlg(String algId)
	{
		super(algId, false, Decidable.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		/*
		 * Replaces atLeastOneError from XACML spec. atLeastOneError == true <=>
		 * firstIndeterminateResult != null
		 */
		DecisionResult firstIndeterminateResult = null;

		/*
		 * Replaces atLeastOnePermit from XACML spec. atLeastOnePermit == true <=>
		 * combinedPermitResult != null
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
					// Get PEP actions from result in case the final decision is Permit
					combinedPermitResult = result.merge(combinedPermitResult);
					break;
				case INDETERMINATE:
					/*
					 * FIXME: implement extended Indeterminate decisions (result differs if
					 * Indeterminate{P} or Indeterminate{D})
					 */
					firstIndeterminateResult = result;
					break;
				default:
					break;
			}
		}

		/*
		 * FIXME: implement extended Indeterminate decisions as the algorithm distinguishes them.
		 */
		if (firstIndeterminateResult != null)
		{
			return firstIndeterminateResult;
		}

		// if we got a PERMIT, return it, otherwise it's NOT_APPLICABLE
		if (combinedPermitResult != null)
		{
			return combinedPermitResult;
		}

		return DecisionResult.NOT_APPLICABLE;
	}

}
