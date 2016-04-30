/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.Decidable;

/**
 * This implements the standard Permit-Overrides and Ordered-Permit-Overrides policy/rule combining algorithm. It allows a single evaluation of Permit to take
 * precedence over any number of deny, not applicable or indeterminate results. Note that since this implementation does an ordered evaluation, this class also
 * supports the Ordered Permit Overrides algorithm.
 *
 * @author cdangerv
 * @version $Id: $
 */
public final class LegacyPermitOverridesAlg extends BaseCombiningAlg<Decidable>
{
	private static final String LEGACY_ALG_WARNING = "%s is a legacy combining algorithm defined in XACML versions earlier than 3.0. This implementation does not support such legacy algorithms. Use the new XACML 3.0 versions of these combining algorithms instead.";

	/**
	 * The standard URIs used to identify this algorithm
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides",
			"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides",
			"urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides",
			"urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgSet SET;
	static
	{
		final Set<CombiningAlg<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new LegacyPermitOverridesAlg(algId));
		}

		SET = new CombiningAlgSet(algSet);
	}

	private final UnsupportedOperationException unsupportedLegacyAlgorithmException;

	private LegacyPermitOverridesAlg(String algId)
	{
		super(algId, Decidable.class);
		this.unsupportedLegacyAlgorithmException = new UnsupportedOperationException(String.format(LEGACY_ALG_WARNING, this));
	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(List<CombiningAlgParameter<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
	{
		throw this.unsupportedLegacyAlgorithmException;
		/*
		 * boolean atLeastOneError = false; boolean atLeastOneDeny = false; Obligations combinedDenyObligations = null; AssociatedAdvice combinedDenyAdvice =
		 * null; StatusHelper firstIndeterminateStatus = null;
		 * 
		 * // List<MatchPolicies> policiesList = new ArrayList<MatchPolicies>(); for (final IPolicy policyElement : policyElements) { // make sure that the
		 * policy matches the context final MatchResult match = policyElement.match(context); LOGGER.debug("{} - {}", policyElement, match); if (match == null)
		 * { atLeastOneError = true; } else if (match.getResult() == MatchResult.INDETERMINATE) { atLeastOneError = true;
		 * 
		 * // keep track of the first error, regardless of cause if (firstIndeterminateStatus == null) { firstIndeterminateStatus = match.getStatus(); } } else
		 * if (match.getResult() == MatchResult.MATCH) { // now we evaluate the policy final Result result = policyElement.evaluate(context); final DecisionType
		 * effect = result.getDecision();
		 * 
		 * if (effect == DecisionType.PERMIT) { return result; } if (effect == DecisionType.DENY) { atLeastOneDeny = true;
		 * 
		 * // copy the obligations/advice in case the final result is Deny combinedDenyObligations = CombiningAlgorithm.addResultObligations(result,
		 * combinedDenyObligations); combinedDenyAdvice = CombiningAlgorithm.addResultAdvice(result, combinedDenyAdvice); } else if (effect ==
		 * DecisionType.INDETERMINATE) { atLeastOneError = true; // keep track of the first error, regardless of cause if (firstIndeterminateStatus == null) {
		 * firstIndeterminateStatus = result.getStatus(); } } } }
		 * 
		 * // if we got a DENY, return it if (atLeastOneDeny) { return new Result(DecisionType.DENY, null, combinedDenyObligations, combinedDenyAdvice, null,
		 * null); }
		 * 
		 * // if we got an INDETERMINATE, return it if (atLeastOneError) { return new Result(DecisionType.INDETERMINATE, firstIndeterminateStatus, null, null,
		 * null, null); }
		 * 
		 * // if we got here, then nothing applied to us return new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		 */
	}
}
