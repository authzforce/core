/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.combining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.Decidable;
import com.thalesgroup.authzforce.core.DecisionResult;
import com.thalesgroup.authzforce.core.EvaluationContext;

/**
 * permit-unless-deny policy algorithm
 * 
 */
public final class PermitUnlessDenyAlg extends CombiningAlgorithm<Decidable>
{

	/**
	 * The standard URN used to identify this algorithm
	 */
	static final String[] SUPPORTED_IDENTIFIERS = { "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny" };

	/**
	 * Supported algorithms
	 */
	public static final CombiningAlgorithmSet SET;
	static
	{
		final Set<CombiningAlgorithm<?>> algSet = new HashSet<>();
		for (final String algId : SUPPORTED_IDENTIFIERS)
		{
			algSet.add(new PermitUnlessDenyAlg(algId));
		}

		SET = new CombiningAlgorithmSet(algSet);
	}

	private PermitUnlessDenyAlg(String algId)
	{
		super(algId, false, Decidable.class);
	}

	@Override
	public DecisionResult combine(EvaluationContext context, List<CombinerElement<? extends Decidable>> parameters, List<? extends Decidable> combinedElements)
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
					// merge the obligations/advice in case the final result is Permit
					combinedPermitResult = result.merge(combinedPermitResult);
					break;
				default:
					continue;
			}
		}

		return combinedPermitResult == null ? DecisionResult.PERMIT : combinedPermitResult;
	}

}
