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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * permit-unless-deny policy algorithm
 *
 * 
 * @version $Id: $
 */
public final class PermitUnlessDenyAlg extends BaseCombiningAlg<Decidable>
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
			DecisionResult combinedPermitResult = null;

			for (Decidable combinedElement : combinedElements)
			{
				final DecisionResult result = combinedElement.evaluate(context);
				final DecisionType decision = result.getDecision();
				switch (decision)
				{
				case DENY:
					return result;
				case PERMIT:
					// merge the obligations, etc. in case the final result is Permit
					if (combinedPermitResult == null)
					{
						combinedPermitResult = result;
					} else
					{
						combinedPermitResult.merge(result.getPepActions(), result.getApplicablePolicyIdList());
					}
					break;
				default:
					continue;
				}
			}

			return combinedPermitResult == null ? BaseDecisionResult.PERMIT : combinedPermitResult;
		}

	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(List<CombiningAlgParameter<? extends Decidable>> params, List<? extends Decidable> combinedElements)
	{
		return new Evaluator(combinedElements);
	}

	/**
	 * The standard URN used to identify this algorithm
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny",
			"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgSet SET;
	static
	{
		final Set<CombiningAlg<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new PermitUnlessDenyAlg(algId));
		}

		SET = new CombiningAlgSet(algSet);
	}

	private PermitUnlessDenyAlg(String algId)
	{
		super(algId, Decidable.class);
	}

}
