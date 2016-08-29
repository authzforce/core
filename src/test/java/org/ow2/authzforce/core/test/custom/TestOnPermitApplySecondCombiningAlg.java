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

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Implements "on-permit-apply-second" policy combining algorithm from
 * <a href="http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html">XACML 3.0 Additional
 * Combining Algorithms Profile Version 1.0</a>. Edited by Erik Rissanen. 18 August 2014. OASIS Committee Specification
 * 01. </>
 * <p>
 * Used here for testing Authzforce combining algorithm extension mechanism, i.e. plugging a custom policy/rule
 * combining algorithm into the PDP engine.
 *
 * @version $Id: $
 */
public class TestOnPermitApplySecondCombiningAlg extends BaseCombiningAlg<PolicyEvaluator>
{

	/**
	 * The standard identifier used to identify this algorithm
	 */
	public static final String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";

	private static class Evaluator extends BaseCombiningAlg.Evaluator<PolicyEvaluator>
	{
		// private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

		private Evaluator(final List<? extends PolicyEvaluator> policyElements) throws IllegalArgumentException
		{
			super(policyElements);
			final int numOfCombinedElts = policyElements.size();
			if (numOfCombinedElts != 2 && numOfCombinedElts != 3)
			{
				throw new IllegalArgumentException("Invalid number of child policies (" + numOfCombinedElts
						+ ") for combining algorithm '" + ID + "'. Expected: 2 or 3");
			}
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList)
		{
			assert outPepActions != null;

			final Iterator<? extends PolicyEvaluator> policyIterator = getCombinedElements().iterator();
			// constructor made sure there are 2 or three elements
			// Use same variable names as in profile spec for decisions (decision0, decision1, decision2)
			final DecisionResult decisionResult0 = policyIterator.next().evaluate(context);
			final DecisionType decision0 = decisionResult0.getDecision();
			switch (decision0) {
				case PERMIT:
					// result from the next child
					final DecisionResult decisionResult1 = policyIterator.next().evaluate(context);
					switch (decisionResult1.getDecision()) {
						case PERMIT:
							/*
							 * final result is Permit like decision0, so add the obligation/advice from decisionResult0
							 * to outPepActions; obligation/advice from decision1 are added as well in case DENY below
							 * (fall through)
							 */
							outPepActions.add(decisionResult0.getPepActions());
						case DENY:
							// obligation/advice from decision1 are added
							outPepActions.add(decisionResult1.getPepActions());
						case INDETERMINATE: // all "applicable" cases (swith blocks fall trough from PERMIT case)
							if (outApplicablePolicyIdList != null)
							{
								outApplicablePolicyIdList.addAll(decisionResult0.getApplicablePolicies());
								outApplicablePolicyIdList.addAll(decisionResult1.getApplicablePolicies());
							}
							break;
						default: // NotApplicable
							break;
					}

					return decisionResult1;

				case INDETERMINATE:
					switch (decisionResult0.getExtendedIndeterminate()) {
						case PERMIT:
						case INDETERMINATE:
							if (outApplicablePolicyIdList != null)
							{
								outApplicablePolicyIdList.addAll(decisionResult0.getApplicablePolicies());
							}
							return decisionResult0;
						default:
							/*
							 * From here, we know decision0 is Indeterminate{D}. Fall through to default case below (in
							 * enclosing switch) The final decision may still be NotApplicable.
							 */
					}

				default:
					// decision0 is Indeterminate{D}, NotApplicable or Deny -> skip the next child
					policyIterator.next();
					// if there is no more child left, NotApplicable; else result from the next child
					if (policyIterator.hasNext())
					{
						// Final decision is decision2
						final DecisionResult decisionResult2 = policyIterator.next().evaluate(context);
						switch (decisionResult2.getDecision()) {
							case DENY:
								if (decision0 == DecisionType.DENY)
								{
									/*
									 * Final result is Deny like decision0, so add the obligation/advice from
									 * decisionResult0 to outPepActions; obligation/advice from decision2 are added as
									 * well in case PERMIT below (fall through)
									 */
									outPepActions.add(decisionResult0.getPepActions());
								}
							case PERMIT:
								// obligation/advice from decision1 are added
								outPepActions.add(decisionResult2.getPepActions());
							case INDETERMINATE: // all "applicable" cases (swith blocks fall trough from DENY case)
								if (outApplicablePolicyIdList != null)
								{
									if (decision0 != DecisionType.NOT_APPLICABLE)
									{
										outApplicablePolicyIdList.addAll(decisionResult0.getApplicablePolicies());
									}

									outApplicablePolicyIdList.addAll(decisionResult2.getApplicablePolicies());
								}
								break;
							default: // NotApplicable
								break;
						}

						return decisionResult2;
					}

					return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Evaluator getInstance(final List<CombiningAlgParameter<? extends PolicyEvaluator>> params,
			final List<? extends PolicyEvaluator> combinedElements)
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
