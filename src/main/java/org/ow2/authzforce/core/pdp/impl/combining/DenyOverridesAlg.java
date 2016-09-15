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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;

import com.google.common.base.Preconditions;

/**
 * This is the standard XACML 3.0 Deny-Overrides policy/rule combining algorithm. It allows a single evaluation of Deny to take precedence over any number of permit, not applicable or indeterminate
 * results. Note that since this implementation does an ordered evaluation, this class also supports the Ordered-Deny-Overrides-algorithm.
 *
 * @version $Id: $
 */
final class DenyOverridesAlg extends BaseCombiningAlg<Decidable>
{

	private static final class Evaluator extends DPOverridesAlgEvaluator
	{
		private Evaluator(final Iterable<? extends Decidable> combinedElements)
		{
			super(combinedElements);
		}

		@Override
		protected ExtendedDecision getOverridingDPResult(final DecisionResult result, final UpdatablePepActions outPepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList, final DPOverridesAlgResultCombiner resultHelper)
		{
			switch (result.getDecision())
			{
				case DENY:
					if (outApplicablePolicyIdList != null)
					{
						outApplicablePolicyIdList.addAll(resultHelper.getApplicablePolicies(result));
					}

					outPepActions.add(result.getPepActions());
					return ExtendedDecisions.SIMPLE_DENY;
				case PERMIT:
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
		protected ExtendedDecision getFinalResult(final PepActions combinedPermitPepActions, final UpdatablePepActions outPepActions,
				final List<JAXBElement<IdReferenceType>> combinedApplicablePolicies, final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
				final ExtendedDecision firstIndeterminateD, final ExtendedDecision firstIndeterminateP)
		{
			/*
			 * If any Indeterminate{D}, then: if ( any Indeterminate{P} or any Permit ) -> Indeterminate{DP}; else -> Indeterminate{D} (this is a simplified equivalent of the algo in the spec)
			 */
			/*
			 * atLeastOnePermit == true <=> permitPepActions != null
			 */
			if (firstIndeterminateD != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return ExtendedDecisions.newIndeterminate(firstIndeterminateP != null || combinedPermitPepActions != null ? DecisionType.INDETERMINATE : DecisionType.DENY,
						firstIndeterminateD.getStatus());
			}

			// if we got a PERMIT or Indeterminate{P}, return it, otherwise it's NOT_APPLICABLE
			if (combinedPermitPepActions != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				outPepActions.add(combinedPermitPepActions);
				return ExtendedDecisions.SIMPLE_PERMIT;
			}

			if (firstIndeterminateP != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return firstIndeterminateP;
			}

			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	}

	DenyOverridesAlg(final String algId)
	{
		super(algId, Decidable.class);
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends Decidable>> params, final Iterable<? extends Decidable> combinedElements)
			throws UnsupportedOperationException, IllegalArgumentException
	{
		return new Evaluator(Preconditions.checkNotNull(combinedElements));
	}

}
