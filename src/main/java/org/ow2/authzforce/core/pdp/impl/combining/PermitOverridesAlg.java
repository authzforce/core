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
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;

import com.google.common.base.Preconditions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * This is the standard Permit-Overrides policy/rule combining algorithm. It allows a single evaluation of Permit to
 * take precedence over any number of deny, not applicable or indeterminate results. Note that since this implementation
 * does an ordered evaluation, this class also supports the Ordered-Permit-Overrides algorithm.
 * 
 * @version $Id: $
 */
final class PermitOverridesAlg extends BaseCombiningAlg<Decidable>
{
	private static final class Evaluator extends DPOverridesAlgEvaluator
	{
		private Evaluator(final List<? extends Decidable> combinedElements)
		{
			super(combinedElements);
		}

		@Override
		protected ExtendedDecision getOverridingDPResult(final DecisionResult result,
				final UpdatablePepActions outPepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
				final DPOverridesAlgResultCombiner resultHelper)
		{
			switch (result.getDecision()) {
				case PERMIT:
					if (outApplicablePolicyIdList != null)
					{
						outApplicablePolicyIdList.addAll(resultHelper.getApplicablePolicies(result));
					}

					outPepActions.add(result.getPepActions());
					return ExtendedDecisions.SIMPLE_PERMIT;
				case DENY:
					resultHelper.addSubResultDP(result);
					break;
				case INDETERMINATE:
					resultHelper.addSubResultIndeterminate(result);
					break;
				default:
					break;
			}

			return null;
		}

		@Override
		protected ExtendedDecision getFinalResult(final PepActions combinedDenyPepActions,
				final UpdatablePepActions outPepActions,
				final List<JAXBElement<IdReferenceType>> combinedApplicablePolicies,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
				final ExtendedDecision firstIndeterminateD, final ExtendedDecision firstIndeterminateP)
		{
			/*
			 * If any Indeterminate{P}, then: if ( any Indeterminate{D} or any Deny ) -> Indeterminate{DP}; else ->
			 * Indeterminate{P} (this is a simplified equivalent of the algo in the spec)
			 */
			/*
			 * atLeastOneDeny == true <=> denyPepActions != null
			 */
			if (firstIndeterminateP != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return ExtendedDecisions.newIndeterminate(firstIndeterminateD != null || combinedDenyPepActions != null
						? DecisionType.INDETERMINATE : DecisionType.PERMIT, firstIndeterminateP.getStatus());
			}

			if (combinedDenyPepActions != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				outPepActions.add(combinedDenyPepActions);
				return ExtendedDecisions.SIMPLE_DENY;
			}

			if (firstIndeterminateD != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return firstIndeterminateD;
			}

			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}

	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(final List<CombiningAlgParameter<? extends Decidable>> params,
			final List<? extends Decidable> combinedElements)
	{
		return new Evaluator(Preconditions.checkNotNull(combinedElements));
	}

	PermitOverridesAlg(final String algId)
	{
		super(algId, Decidable.class);
	}
}
