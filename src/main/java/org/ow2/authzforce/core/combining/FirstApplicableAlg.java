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
package org.ow2.authzforce.core.combining;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.Decidable;
import org.ow2.authzforce.core.PolicyDecisionResult;
import org.ow2.authzforce.core.EvaluationContext;

/**
 * This is the standard First-Applicable policy/rule combining algorithm. It looks through the set of policies/rules, finds the first one that applies, and
 * returns that evaluation result.
 * 
 */
public final class FirstApplicableAlg extends CombiningAlg<Decidable>
{
	private static class Evaluator implements CombiningAlg.Evaluator
	{

		private final List<? extends Decidable> combinedElements;

		private Evaluator(List<? extends Decidable> combinedElements)
		{
			this.combinedElements = combinedElements;
		}

		@Override
		public PolicyDecisionResult eval(EvaluationContext context)
		{
			for (final Decidable combinedElement : combinedElements)
			{
				// evaluate the policy
				final PolicyDecisionResult result = combinedElement.evaluate(context);
				final DecisionType decision = result.getDecision();

				// in the case of PERMIT, DENY, or INDETERMINATE, we always
				// just return that result, so only on a rule that doesn't
				// apply do we keep going...
				if (decision != DecisionType.NOT_APPLICABLE)
				{
					return result;
				}
			}

			// if we got here, then none of the rules applied
			return PolicyDecisionResult.NOT_APPLICABLE;
		}

	}

	@Override
	public CombiningAlg.Evaluator getInstance(List<CombiningAlgParameter<? extends Decidable>> params, List<? extends Decidable> combinedElements)
	{
		return new Evaluator(combinedElements);
	}

	/**
	 * The standard URIs used to identify this algorithm
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable",
			"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgSet SET = new CombiningAlgSet(new FirstApplicableAlg(SUPPORTED_IDENTIFIERS[0]), new FirstApplicableAlg(
			SUPPORTED_IDENTIFIERS[1]));

	private FirstApplicableAlg(String algId)
	{
		super(algId, false, Decidable.class);
	}

}
