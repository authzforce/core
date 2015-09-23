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
 * This implements the standard Permit-Overrides and Ordered-Permit-Overrides policy/rule combining
 * algorithm. It allows a single evaluation of Permit to take precedence over any number of deny,
 * not applicable or indeterminate results. Note that since this implementation does an ordered
 * evaluation, this class also supports the Ordered Permit Overrides algorithm.
 * 
 */
public class LegacyPermitOverridesAlg extends CombiningAlgorithm<Decidable>
{
	/**
	 * The standard URIs used to identify this algorithm
	 */
	public static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides", "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides",
			"urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET;
	static
	{
		final Set<CombiningAlgorithm<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new LegacyPermitOverridesAlg(algId));
		}

		SET = new CombiningAlgorithmSet(algSet);
	}

	private LegacyPermitOverridesAlg(String algId)
	{
		super(algId, true, Decidable.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		throw this.unsupportedLegacyAlgorithmException;
		/*
		 * boolean atLeastOneError = false; boolean atLeastOneDeny = false; Obligations
		 * combinedDenyObligations = null; AssociatedAdvice combinedDenyAdvice = null; Status
		 * firstIndeterminateStatus = null;
		 * 
		 * // List<MatchPolicies> policiesList = new ArrayList<MatchPolicies>(); for (final IPolicy
		 * policyElement : policyElements) { // make sure that the policy matches the context final
		 * MatchResult match = policyElement.match(context); LOGGER.debug("{} - {}", policyElement,
		 * match); if (match == null) { atLeastOneError = true; } else if (match.getResult() ==
		 * MatchResult.INDETERMINATE) { atLeastOneError = true;
		 * 
		 * // keep track of the first error, regardless of cause if (firstIndeterminateStatus ==
		 * null) { firstIndeterminateStatus = match.getStatus(); } } else if (match.getResult() ==
		 * MatchResult.MATCH) { // now we evaluate the policy final Result result =
		 * policyElement.evaluate(context); final DecisionType effect = result.getDecision();
		 * 
		 * if (effect == DecisionType.PERMIT) { return result; } if (effect == DecisionType.DENY) {
		 * atLeastOneDeny = true;
		 * 
		 * // copy the obligations/advice in case the final result is Deny combinedDenyObligations =
		 * CombiningAlgorithm.addResultObligations(result, combinedDenyObligations);
		 * combinedDenyAdvice = CombiningAlgorithm.addResultAdvice(result, combinedDenyAdvice); }
		 * else if (effect == DecisionType.INDETERMINATE) { atLeastOneError = true; // keep track of
		 * the first error, regardless of cause if (firstIndeterminateStatus == null) {
		 * firstIndeterminateStatus = result.getStatus(); } } } }
		 * 
		 * // if we got a DENY, return it if (atLeastOneDeny) { return new Result(DecisionType.DENY,
		 * null, combinedDenyObligations, combinedDenyAdvice, null, null); }
		 * 
		 * // if we got an INDETERMINATE, return it if (atLeastOneError) { return new
		 * Result(DecisionType.INDETERMINATE, firstIndeterminateStatus, null, null, null, null); }
		 * 
		 * // if we got here, then nothing applied to us return new
		 * Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		 */
	}
}
