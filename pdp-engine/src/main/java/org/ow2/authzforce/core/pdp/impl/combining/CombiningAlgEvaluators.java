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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.Collection;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Common Combining Algorithm evaluators
 */
final class CombiningAlgEvaluators
{
	private CombiningAlgEvaluators()
	{
	}

	private static abstract class ConstantDecisionEvaluator implements CombiningAlg.Evaluator
	{

		private static final Logger LOGGER = LoggerFactory.getLogger(ConstantDecisionEvaluator.class);

		protected abstract ExtendedDecision getReturnedDecision();

		@Override
		public final ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions, final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
		{
			LOGGER.debug("This evaluator constantly returns the same decision, which results from an optimization of the combining algorithm and combined elements (if any) initially defined in the policy. Check the policy initialization logs for more information on this optimization.");
			return getReturnedDecision();
		}
	}

	static final CombiningAlg.Evaluator NOT_APPLICABLE_CONSTANT_EVALUATOR = new ConstantDecisionEvaluator()
	{

		@Override
		public ExtendedDecision getReturnedDecision()
		{
			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	};

	static final CombiningAlg.Evaluator DENY_CONSTANT_EVALUATOR = new ConstantDecisionEvaluator()
	{

		@Override
		public ExtendedDecision getReturnedDecision()
		{
			return ExtendedDecisions.SIMPLE_DENY;
		}
	};

	static final CombiningAlg.Evaluator PERMIT_CONSTANT_EVALUATOR = new ConstantDecisionEvaluator()
	{

		@Override
		public ExtendedDecision getReturnedDecision()
		{
			return ExtendedDecisions.SIMPLE_PERMIT;
		}
	};

	/*
	 * Rule combining algorithm evaluator where all rules must have the same Effect, and that returns NotApplicable if no rule applies, else Indeterminate if all rules result in Indeterminate or
	 * NotApplicable, else the Permit/Deny decision (corresponding to the common Effect) of the first rule that applies and successfully evaluates.
	 */
	static class RulesWithSameEffectEvaluator implements CombiningAlg.Evaluator
	{
		protected static boolean haveSameEffect(final EffectType expectedEffect, final Collection<? extends RuleEvaluator> rules)
		{
			for (final RuleEvaluator rule : rules)
			{
				if (rule.getEffect() != expectedEffect)
				{
					return false;
				}
			}

			return true;
		}

		private final ImmutableList<RuleEvaluator> rulesWithSameEffect;
		private final DecisionType commonDecision;

		RulesWithSameEffectEvaluator(final Collection<? extends RuleEvaluator> rulesWithSameEffect)
		{
			assert rulesWithSameEffect != null && !rulesWithSameEffect.isEmpty();
			// first rule's effect assumed the same for all
			final EffectType commonEffect = rulesWithSameEffect.iterator().next().getEffect();
			assert haveSameEffect(commonEffect, rulesWithSameEffect);
			this.commonDecision = commonEffect == EffectType.DENY ? DecisionType.DENY : DecisionType.PERMIT;
			this.rulesWithSameEffect = ImmutableList.copyOf(rulesWithSameEffect);
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions, final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
		{
			ExtendedDecision firstIndeterminate = null;
			for (final RuleEvaluator rule : rulesWithSameEffect)
			{
				final DecisionResult evalResult = rule.evaluate(context);
				final DecisionType decision = evalResult.getDecision();
				if (decision == commonDecision)
				{
					updatablePepActions.add(evalResult.getPepActions());
					return evalResult;
				}

				// Decision is NotApplicable or Indeterminate
				// If decision Indeterminate, evalResult is Indeterminate(P)
				if (decision == DecisionType.INDETERMINATE && firstIndeterminate == null)
				{
					// this is the first Indeterminate
					firstIndeterminate = evalResult;
				}
			}

			// No commonDecision (Permit/Deny) returned
			// If no Indeterminate -> all NotApplicable
			return firstIndeterminate == null ? ExtendedDecisions.SIMPLE_NOT_APPLICABLE : firstIndeterminate;
		}
	}
}
