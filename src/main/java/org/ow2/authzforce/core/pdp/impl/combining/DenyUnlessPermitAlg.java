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
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Deny-unless-permit combining algorithm
 *
 * @version $Id: $
 */
final class DenyUnlessPermitAlg extends BaseCombiningAlg<Decidable>
{

	private static final class Evaluator extends BaseCombiningAlg.Evaluator<Decidable>
	{
		private Evaluator(final List<? extends Decidable> combinedElements)
		{
			super(combinedElements);
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList)
		{
			assert outPepActions != null;
			/*
			 * The final decision cannot be NotApplicable so we can add all applicable policies straight to
			 * outApplicablePolicyIdList
			 */

			UpdatablePepActions denyPepActions = null;

			for (final Decidable combinedElement : getCombinedElements())
			{
				final DecisionResult result = combinedElement.evaluate(context);
				final DecisionType decision = result.getDecision();
				/*
				 * XACML §7.18: Obligations & Advice: do not return obligations/Advice of the rule, policy, or policy
				 * set that does not match the decision resulting from evaluating the enclosing policy set.
				 * 
				 * So if we return Deny, we should add to outPepActions only the PEP actions from Deny decisions
				 */
				switch (decision) {
					case PERMIT:
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						outPepActions.add(result.getPepActions());
						return ExtendedDecisions.SIMPLE_PERMIT;
					case DENY:
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						if (denyPepActions == null)
						{
							denyPepActions = new UpdatablePepActions();
						}

						denyPepActions.add(result.getPepActions());
						break;
					default:
						break;
				}
			}

			/*
			 * All applicable policies are already in outApplicablePolicyIdList at this point, so nothing else to do
			 * with it
			 */

			outPepActions.add(denyPepActions);
			return ExtendedDecisions.SIMPLE_DENY;
		}

	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final List<CombiningAlgParameter<? extends Decidable>> params,
			final List<? extends Decidable> combinedElements)
			throws UnsupportedOperationException, IllegalArgumentException
	{
		return new Evaluator(combinedElements);
	}

	DenyUnlessPermitAlg(final String algId)
	{
		super(algId, Decidable.class);
	}

}
