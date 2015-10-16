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
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;

/**
 * This is the standard Deny-Overrides and Ordered-Deny-Overrides combining algorithm. It allows a
 * single evaluation of Deny to take precedence over any number of permit, not applicable or
 * indeterminate results. Note that since this implementation does an ordered evaluation, this class
 * also supports the Ordered-Deny-Overrides algorithm.
 * 
 */
public final class LegacyDenyOverridesAlg extends CombiningAlgorithm<Decidable>
{

	/**
	 * The standard URIs used to identify this algorithm
	 */
	public static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides", "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides",
			"urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET;
	static
	{
		final Set<CombiningAlgorithm<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new LegacyDenyOverridesAlg(algId));
		}

		SET = new CombiningAlgorithmSet(algSet);
	}

	private LegacyDenyOverridesAlg(String algId)
	{
		super(algId, true, Decidable.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		throw this.unsupportedLegacyAlgorithmException;
		/*
		 * boolean atLeastOnePermit = false; Obligations combinedPermitObligations = null;
		 * AssociatedAdvice combinedPermitAdvice = null; for (final IPolicy policy : policyElements)
		 * { // evaluate the policy final DecisionResult result = policy.evaluate(context); final
		 * DecisionType effect = result.getDecision();
		 * 
		 * // unlike in the RuleCombining version of this alg, we always // return DENY if any
		 * Policy returns DENY or INDETERMINATE if ((effect == DecisionType.DENY) || (effect ==
		 * DecisionType.INDETERMINATE)) { return new DecisionResult(DecisionType.DENY,
		 * result.getPepActions()); }
		 * 
		 * // remember if at least one Policy said PERMIT if (effect == DecisionType.PERMIT) {
		 * atLeastOnePermit = true;
		 * 
		 * // copy the obligations/advice in case the final result is Permit
		 * combinedPermitObligations = CombiningAlgorithm.addResultObligations(result,
		 * combinedPermitObligations); combinedPermitAdvice =
		 * CombiningAlgorithm.addResultAdvice(result, combinedPermitAdvice); } }
		 * 
		 * // if we got a PERMIT, return it, otherwise it's NOT_APPLICABLE if (atLeastOnePermit) {
		 * return new Result(DecisionType.PERMIT, null, combinedPermitObligations,
		 * combinedPermitAdvice, null, null); }
		 * 
		 * return new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		 */
	}

}
