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
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgSet;
import org.ow2.authzforce.core.pdp.impl.BaseDecisionResult;

/**
 * Deny-unless-permit combining algorithm
 *
 * @version $Id: $
 */
public final class DenyUnlessPermitAlg extends BaseCombiningAlg<Decidable>
{

	private static class Evaluator implements CombiningAlg.Evaluator
	{

		private final List<? extends Decidable> combinedElements;

		private Evaluator(List<? extends Decidable> combinedElements)
		{
			this.combinedElements = combinedElements;
		}

		@Override
		public DecisionResult eval(EvaluationContext context)
		{
			DecisionResult combinedDenyResult = null;
			for (Decidable combinedElement : combinedElements)
			{
				// make sure that the policy matches the context
				final DecisionResult policyResult = combinedElement.evaluate(context);
				final DecisionType decision = policyResult.getDecision();
				switch (decision)
				{
				case PERMIT:
					return policyResult;
				case DENY:
					// merge result (obligations/advice/mached policy IDs)
					if (combinedDenyResult == null)
					{
						combinedDenyResult = policyResult;
					} else
					{
						combinedDenyResult.merge(policyResult.getPepActions(), policyResult.getApplicablePolicyIdList());
					}
					break;
				default:
					continue;
				}
			}

			return combinedDenyResult == null ? BaseDecisionResult.DENY : combinedDenyResult;
		}

	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(List<CombiningAlgParameter<? extends Decidable>> params, List<? extends Decidable> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		return new Evaluator(combinedElements);
	}

	/**
	 * The standard URIs used to identify this algorithm; first one is for policy combinging, second one for rule combining.
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit",
			"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgSet SET = new CombiningAlgSet(new DenyUnlessPermitAlg(SUPPORTED_IDENTIFIERS[0]), new DenyUnlessPermitAlg(SUPPORTED_IDENTIFIERS[1]));

	private DenyUnlessPermitAlg(String algId)
	{
		super(algId, Decidable.class);
	}

}
