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

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;

/**
 * This is the standard Deny-Overrides and Ordered-Deny-Overrides combining algorithm. It allows a single evaluation of Deny to take precedence over any number of permit, not applicable or
 * indeterminate results. Note that since this implementation does an ordered evaluation, this class also supports the Ordered-Deny-Overrides algorithm.
 *
 * @version $Id: $
 */
final class LegacyDenyOverridesCombiningAlg extends BaseCombiningAlg<Decidable>
{
	private static final String LEGACY_ALG_WARNING = "%s is a legacy combining algorithm defined in XACML versions earlier than 3.0. This implementation does not support such legacy algorithms. Use the new XACML 3.0 versions of these combining algorithms instead.";

	private final UnsupportedOperationException unsupportedLegacyAlgorithmException;

	LegacyDenyOverridesCombiningAlg(final String algId)
	{
		super(algId, Decidable.class);
		this.unsupportedLegacyAlgorithmException = new UnsupportedOperationException(String.format(LEGACY_ALG_WARNING, this));
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends Decidable>> params, final Iterable<? extends Decidable> combinedElements)
	{
		throw this.unsupportedLegacyAlgorithmException;
		/*
		 * boolean atLeastOnePermit = false; Obligations combinedPermitObligations = null; AssociatedAdvice combinedPermitAdvice = null; for (final IPolicy policy : policyElements) { // evaluate the
		 * policy final DecisionResult result = policy.evaluate(context); final DecisionType effect = result.getDecision();
		 * 
		 * // unlike in the RuleCombining version of this alg, we always // return DENY if any Policy returns DENY or INDETERMINATE if ((effect == DecisionType.DENY) || (effect ==
		 * DecisionType.INDETERMINATE)) { return new DecisionResult(DecisionType.DENY, result.getPepActions()); }
		 * 
		 * // remember if at least one Policy said PERMIT if (effect == DecisionType.PERMIT) { atLeastOnePermit = true;
		 * 
		 * // copy the obligations/advice in case the final result is Permit combinedPermitObligations = CombiningAlgorithm.addResultObligations(result, combinedPermitObligations);
		 * combinedPermitAdvice = CombiningAlgorithm.addResultAdvice(result, combinedPermitAdvice); } }
		 * 
		 * // if we got a PERMIT, return it, otherwise it's NOT_APPLICABLE if (atLeastOnePermit) { return new Result(DecisionType.PERMIT, null, combinedPermitObligations, combinedPermitAdvice, null,
		 * null); }
		 * 
		 * return new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		 */
	}

}
