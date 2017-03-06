/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
