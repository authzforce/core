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

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

abstract class DPOverridesAlgEvaluator extends BaseCombiningAlg.Evaluator<Decidable>
{

	DPOverridesAlgEvaluator(final List<? extends Decidable> combinedElements)
	{
		super(combinedElements);
	}

	@Override
	public final ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions,
			final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList)
	{
		assert outPepActions != null;
		final DPOverridesAlgResultCombiner resultHelper = new DPOverridesAlgResultCombiner(
				outApplicablePolicyIdList != null);
		for (final Decidable combinedElement : getCombinedElements())
		{
			// evaluate the policy
			final DecisionResult result = combinedElement.evaluate(context);
			/*
			 * XACML §7.18: Obligations & Advice: do not return obligations/Advice of the rule, policy, or policy
			 * set that does not match the decision resulting from evaluating the enclosing policy set. For example,
			 * if the final decision is Permit, we should add to outPepActions only the PEP actions from Permit
			 * decisions (permitPepActions)
			 */
			final ExtendedDecision finalResult = getOverridingDPResult(result, outPepActions,
					outApplicablePolicyIdList, resultHelper);
			if (finalResult != null)
			{
				return finalResult;
			}
		}

		/*
		 * There was no overriding Deny/Permit decision, i.e. Deny (resp. Permit) in case of deny-overrides (resp.
		 * permit-overrides) alg, else: if any Indeterminate{DP}, then Indeterminate{DP}
		 */
		final ExtendedDecision firstIndeterminateDP = resultHelper.getFirstIndeterminateDP();
		if (firstIndeterminateDP != null)
		{
			// at least one Indeterminate{DP}
			if (outApplicablePolicyIdList != null)
			{
				outApplicablePolicyIdList.addAll(resultHelper.getApplicablePolicies(null));
			}

			return firstIndeterminateDP;
		}

		return getFinalResult(resultHelper.getPepActions(), outPepActions, resultHelper.getApplicablePolicies(null),
				outApplicablePolicyIdList, resultHelper.getFirstIndeterminateD(),
				resultHelper.getFirstIndeterminateP());
	}

	/**
	 * Get overriding Deny/Permit decision, e.g. first Deny (resp. Permit) returned by a combined element in
	 * deny-overrides (resp. permit-overrides) algorithm, resulting in the algorithm to return it as final result
	 * immediately. (This corresponds to the for-loop in XACML spec's pseudo-code describing the algorithm.) Or null
	 * if no such case occurred (and algorithm must go on, i.e. part after the for-loop in XACML spec)
	 */
	protected abstract ExtendedDecision getOverridingDPResult(DecisionResult result,
			UpdatablePepActions outPepActions,
			UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
			DPOverridesAlgResultCombiner resultHelper);

	/**
	 * Finish the algorithm based on all PEP actions and applicable policy lists from all combined elements, and
	 * previously returned Indeterminate{D}/Indeterminate{P} if any
	 */
	protected abstract ExtendedDecision getFinalResult(final PepActions combinedPepActions,
			final UpdatablePepActions outPepActions,
			final List<JAXBElement<IdReferenceType>> combinedApplicablePolicies,
			final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
			final ExtendedDecision firstIndeterminateD, final ExtendedDecision firstIndeterminateP);

}