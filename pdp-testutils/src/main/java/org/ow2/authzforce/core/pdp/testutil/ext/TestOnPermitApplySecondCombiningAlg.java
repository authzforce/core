/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.testutil.ext;

import java.util.Iterator;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;

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
		private interface ThirdPolicyEvaluator
		{
			ExtendedDecision evaluate(EvaluationContext ctx, final DecisionResult decisionResult0, final UpdatablePepActions outPepActions,
					final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList);
		}

		private static class NonNullThirdPolicyEvaluator implements ThirdPolicyEvaluator
		{
			private final PolicyEvaluator policyEvaluator;

			private NonNullThirdPolicyEvaluator(final PolicyEvaluator policyEvaluator)
			{
				this.policyEvaluator = policyEvaluator;
			}

			@Override
			public ExtendedDecision evaluate(final EvaluationContext ctx, final DecisionResult decisionResult0, final UpdatablePepActions outPepActions,
					final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
			{
				final DecisionResult decisionResult2 = policyEvaluator.evaluate(ctx);
				final DecisionType decision0 = decisionResult0.getDecision();
				switch (decisionResult2.getDecision())
				{
					case DENY:
						if (decision0 == DecisionType.DENY)
						{
							/*
							 * Final result is Deny like decision0, so add the obligation/advice from decisionResult0 to outPepActions; obligation/advice from decision2 are added as well in case
							 * PERMIT below (fall through)
							 */
							outPepActions.add(decisionResult0.getPepActions());
						}
					case PERMIT:
						/*
						 * Obligation/advice from decision2 are added whether it is Permit/Deny (switch fall-through)
						 */
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
		}

		private static final ThirdPolicyEvaluator NO_THIRD_POLICY_EVALUATOR = new ThirdPolicyEvaluator()
		{

			@Override
			public ExtendedDecision evaluate(final EvaluationContext ctx, final DecisionResult decisionResult0, final UpdatablePepActions outPepActions,
					final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
			{
				// When there is no third policy -> NotApplicable
				return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
			}

		};

		private final PolicyEvaluator policyEvaluator0;
		private final PolicyEvaluator policyEvaluator1;
		private final ThirdPolicyEvaluator thirdPolicyEvaluator;

		private Evaluator(final Iterable<? extends PolicyEvaluator> policyElements) throws IllegalArgumentException
		{
			final Iterator<? extends PolicyEvaluator> policyIterator = policyElements.iterator();
			if (!policyIterator.hasNext())
			{
				throw new IllegalArgumentException("Invalid number of child policies (0) for combining algorithm '" + ID + "'. Expected: 2 or 3");
			}

			this.policyEvaluator0 = policyIterator.next();
			if (!policyIterator.hasNext())
			{
				throw new IllegalArgumentException("Invalid number of child policies (1) for combining algorithm '" + ID + "'. Expected: 2 or 3");
			}

			this.policyEvaluator1 = policyIterator.next();
			/*
			 * When decision0 (decision from PolicyEvaluator0) is Indeterminate{D}, NotApplicable or Deny, we skip policyEvaluator1 (second policy), and return result (decision2) of evaluating the
			 * third policy if any, or return NotApplicable
			 * 
			 * NonNullThirdPolicyEvaluator returns result of next input policy evaluator NO_THIRD_POLICY_EVALUATOR always return NotApplicable.
			 */
			this.thirdPolicyEvaluator = policyIterator.hasNext() ? new NonNullThirdPolicyEvaluator(policyIterator.next()) : NO_THIRD_POLICY_EVALUATOR;
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
		{
			assert outPepActions != null;
			// Use same variable names as in profile spec for decisions (decision0, decision1, decision2)
			final DecisionResult decisionResult0 = policyEvaluator0.evaluate(context);
			final DecisionType decision0 = decisionResult0.getDecision();
			switch (decision0)
			{
				case PERMIT:
					// result from the next child
					final DecisionResult decisionResult1 = policyEvaluator1.evaluate(context);
					switch (decisionResult1.getDecision())
					{
						case PERMIT:
							/*
							 * final result is Permit like decision0, so add the obligation/advice from decisionResult0 to outPepActions; obligation/advice from decision1 are added as well in case
							 * DENY below (fall through)
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
					switch (decisionResult0.getExtendedIndeterminate())
					{
						case PERMIT:
						case INDETERMINATE:
							if (outApplicablePolicyIdList != null)
							{
								outApplicablePolicyIdList.addAll(decisionResult0.getApplicablePolicies());
							}
							return decisionResult0;
						default:
							/*
							 * From here, we know decision0 is Indeterminate{D}. Fall through to default case below (in enclosing switch) The final decision may still be NotApplicable.
							 */
					}

				default:
					return thirdPolicyEvaluator.evaluate(context, decisionResult0, outPepActions, outApplicablePolicyIdList);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends PolicyEvaluator>> params, final Iterable<? extends PolicyEvaluator> combinedElements)
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
