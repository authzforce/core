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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.MutablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;

/**
 * Deny-unless-permit combining algorithm
 *
 * @version $Id: $
 */
final class DenyUnlessPermitAlg extends BaseCombiningAlg<Decidable>
{

	private static class Evaluator implements CombiningAlg.Evaluator
	{

		private final List<? extends Decidable> combinedElements;

		private Evaluator(final List<? extends Decidable> combinedElements)
		{
			this.combinedElements = combinedElements;
		}

		@Override
		public DecisionResult eval(final EvaluationContext context, final MutablePepActions mutablePepActions)
		{
			boolean atLeastOneDeny = false;
			MutablePepActions denyPepActions = null;

			for (final Decidable combinedElement : combinedElements)
			{
				// make sure that the policy matches the context
				final DecisionResult policyResult = combinedElement.evaluate(context);
				final DecisionType decision = policyResult.getDecision();
				/*
				 * XACML §7.18: Obligations & Advice: do not return obligations/Advice of the rule, policy, or policy set that does not match the decision resulting from evaluating the enclosing
				 * policy set
				 */
				switch (decision)
				{
					case PERMIT:
						mutablePepActions.add(policyResult.getPepActions());
						return DecisionResults.SIMPLE_PERMIT;
					case DENY:
						// merge result (obligations/advice/mached policy IDs)
						if (atLeastOneDeny)
						{
							assert denyPepActions != null;
							denyPepActions.add(policyResult.getPepActions());
							break;
						}

						atLeastOneDeny = true;
						denyPepActions = new MutablePepActions();
						break;
					default:
						break;
				}
			}

			mutablePepActions.add(denyPepActions);
			return DecisionResults.SIMPLE_DENY;
		}

	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final List<CombiningAlgParameter<? extends Decidable>> params, final List<? extends Decidable> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		return new Evaluator(combinedElements);
	}

	DenyUnlessPermitAlg(final String algId)
	{
		super(algId, Decidable.class);
	}

}
