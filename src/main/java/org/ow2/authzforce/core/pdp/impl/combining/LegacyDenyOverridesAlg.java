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
 * This is the standard Deny-Overrides and Ordered-Deny-Overrides combining algorithm. It allows a single evaluation of Deny to take precedence over any number
 * of permit, not applicable or indeterminate results. Note that since this implementation does an ordered evaluation, this class also supports the
 * Ordered-Deny-Overrides algorithm.
 * 
 */
public final class LegacyDenyOverridesAlg extends BaseCombiningAlg<Decidable>
{
	private static final String LEGACY_ALG_WARNING = "%s is a legacy combining algorithm defined in XACML versions earlier than 3.0. This implementation does not support such legacy algorithms. Use the new XACML 3.0 versions of these combining algorithms instead.";

	/**
	 * The standard URIs used to identify this algorithm
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides",
			"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides",
			"urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides",
			"urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgSet SET;
	static
	{
		final Set<CombiningAlg<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new LegacyDenyOverridesAlg(algId));
		}

		SET = new CombiningAlgSet(algSet);
	}

	private final UnsupportedOperationException unsupportedLegacyAlgorithmException;

	private LegacyDenyOverridesAlg(String algId)
	{
		super(algId, Decidable.class);
		this.unsupportedLegacyAlgorithmException = new UnsupportedOperationException(String.format(LEGACY_ALG_WARNING, this));
	}

	@Override
	public CombiningAlg.Evaluator getInstance(List<CombiningAlgParameter<? extends Decidable>> params, List<? extends Decidable> combinedElements)
	{
		throw this.unsupportedLegacyAlgorithmException;
		/*
		 * boolean atLeastOnePermit = false; Obligations combinedPermitObligations = null; AssociatedAdvice combinedPermitAdvice = null; for (final IPolicy
		 * policy : policyElements) { // evaluate the policy final DecisionResult result = policy.evaluate(context); final DecisionType effect =
		 * result.getDecision();
		 * 
		 * // unlike in the RuleCombining version of this alg, we always // return DENY if any Policy returns DENY or INDETERMINATE if ((effect ==
		 * DecisionType.DENY) || (effect == DecisionType.INDETERMINATE)) { return new DecisionResult(DecisionType.DENY, result.getPepActions()); }
		 * 
		 * // remember if at least one Policy said PERMIT if (effect == DecisionType.PERMIT) { atLeastOnePermit = true;
		 * 
		 * // copy the obligations/advice in case the final result is Permit combinedPermitObligations = CombiningAlgorithm.addResultObligations(result,
		 * combinedPermitObligations); combinedPermitAdvice = CombiningAlgorithm.addResultAdvice(result, combinedPermitAdvice); } }
		 * 
		 * // if we got a PERMIT, return it, otherwise it's NOT_APPLICABLE if (atLeastOnePermit) { return new Result(DecisionType.PERMIT, null,
		 * combinedPermitObligations, combinedPermitAdvice, null, null); }
		 * 
		 * return new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		 */
	}

}
