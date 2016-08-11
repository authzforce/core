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
package org.ow2.authzforce.core.test.custom;

import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.MutableDecisionResult;

/**
 * Implements "on-permit-apply-second" policy combining algorithm from <a href="http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html">XACML 3.0 Additional Combining
 * Algorithms Profile Version 1.0</a>. Edited by Erik Rissanen. 18 August 2014. OASIS Committee Specification 01. </>
 * <p>
 * Used here for testing Authzforce combining algorithm extension mechanism, i.e. plugging a custom policy/rule combining algorithm into the PDP engine.
 *
 * @version $Id: $
 */
public class TestOnPermitApplySecondCombiningAlg extends BaseCombiningAlg<PolicyEvaluator>
{

	/**
	 * The standard identifier used to identify this algorithm
	 */
	public static final String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";

	private static class Evaluator implements CombiningAlg.Evaluator
	{
		// private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

		private final List<? extends PolicyEvaluator> policyElements;

		private Evaluator(List<? extends PolicyEvaluator> policyElements) throws IllegalArgumentException
		{
			final int numOfCombinedElts = policyElements.size();
			if (numOfCombinedElts != 2 && numOfCombinedElts != 3)
			{
				throw new IllegalArgumentException("Invalid number of child policies (" + numOfCombinedElts + ") for combining algorithm '" + ID + "'. Expected: 2 or 3");
			}

			this.policyElements = policyElements;
		}

		@Override
		public DecisionResult eval(EvaluationContext context)
		{
			final Iterator<? extends PolicyEvaluator> policyIterator = policyElements.iterator();
			// constructor made sure there are 2 or three elements
			final DecisionResult decisionResult0 = policyIterator.next().evaluate(context);
			final DecisionType decision0 = decisionResult0.getDecision();
			switch (decision0)
			{
			case PERMIT:
				// result from the next child
				return policyIterator.next().evaluate(context);

			case INDETERMINATE:
				switch (decisionResult0.getExtendedIndeterminate())
				{
				case INDETERMINATE:
				case PERMIT:
					return decisionResult0;
				default:
					// from here, we know it is Indeterminate{D}
				}

			default:
				// decision0 is Indeterminate{D}, NotApplicable or Deny -> skip the next child
				policyIterator.next();
				// if there is no more child left, NotApplicable; else result from the next child
				if (policyIterator.hasNext())
				{
					return policyIterator.next().evaluate(context);
				}

				return MutableDecisionResult.NOT_APPLICABLE;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(List<CombiningAlgParameter<? extends PolicyEvaluator>> params, List<? extends PolicyEvaluator> combinedElements)
	{
		return new Evaluator(combinedElements);
	}

	/**
	 * No-arg constructor.
	 */
	public TestOnPermitApplySecondCombiningAlg()
	{
		super(ID, PolicyEvaluator.class);
	}

}
