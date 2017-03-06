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
 * This implements the standard Permit-Overrides and Ordered-Permit-Overrides policy/rule combining algorithm. It allows a single evaluation of Permit to take precedence over any number of deny, not
 * applicable or indeterminate results. Note that since this implementation does an ordered evaluation, this class also supports the Ordered Permit Overrides algorithm.
 *
 * @version $Id: $
 */
final class LegacyPermitOverridesCombiningAlg extends BaseCombiningAlg<Decidable>
{
	private static final String LEGACY_ALG_WARNING = "%s is a legacy combining algorithm defined in XACML versions earlier than 3.0. This implementation does not support such legacy algorithms. Use the new XACML 3.0 versions of these combining algorithms instead.";

	private final UnsupportedOperationException unsupportedLegacyAlgorithmException;

	LegacyPermitOverridesCombiningAlg(final String algId)
	{
		super(algId, Decidable.class);
		this.unsupportedLegacyAlgorithmException = new UnsupportedOperationException(String.format(LEGACY_ALG_WARNING, this));
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends Decidable>> parameters, final Iterable<? extends Decidable> combinedElements)
	{
		throw this.unsupportedLegacyAlgorithmException;
		/*
		 * boolean atLeastOneError = false; boolean atLeastOneDeny = false; Obligations combinedDenyObligations = null; AssociatedAdvice combinedDenyAdvice = null; StatusHelper
		 * firstIndeterminateStatus = null;
		 * 
		 * // List<MatchPolicies> policiesList = new ArrayList<MatchPolicies>(); for (final IPolicy policyElement : policyElements) { // make sure that the policy matches the context final MatchResult
		 * match = policyElement.match(context); LOGGER.debug("{} - {}", policyElement, match); if (match == null) { atLeastOneError = true; } else if (match.getResult() == MatchResult.INDETERMINATE)
		 * { atLeastOneError = true;
		 * 
		 * // keep track of the first error, regardless of cause if (firstIndeterminateStatus == null) { firstIndeterminateStatus = match.getStatus(); } } else if (match.getResult() ==
		 * MatchResult.MATCH) { // now we evaluate the policy final Result result = policyElement.evaluate(context); final DecisionType effect = result.getDecision();
		 * 
		 * if (effect == DecisionType.PERMIT) { return result; } if (effect == DecisionType.DENY) { atLeastOneDeny = true;
		 * 
		 * // copy the obligations/advice in case the final result is Deny combinedDenyObligations = CombiningAlgorithm.addResultObligations(result, combinedDenyObligations); combinedDenyAdvice =
		 * CombiningAlgorithm.addResultAdvice(result, combinedDenyAdvice); } else if (effect == DecisionType.INDETERMINATE) { atLeastOneError = true; // keep track of the first error, regardless of
		 * cause if (firstIndeterminateStatus == null) { firstIndeterminateStatus = result.getStatus(); } } } }
		 * 
		 * // if we got a DENY, return it if (atLeastOneDeny) { return new Result(DecisionType.DENY, null, combinedDenyObligations, combinedDenyAdvice, null, null); }
		 * 
		 * // if we got an INDETERMINATE, return it if (atLeastOneError) { return new Result(DecisionType.INDETERMINATE, firstIndeterminateStatus, null, null, null, null); }
		 * 
		 * // if we got here, then nothing applied to us return new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		 */
	}
}
