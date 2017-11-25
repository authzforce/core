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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

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
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * *-unless-* combining algorithm (deny-unless-permit or permit-unless-deny)
 *
 * @version $Id: $
 */
final class DPUnlessPDCombiningAlg<T extends Decidable> extends BaseCombiningAlg<T>
{

	private static final class Evaluator extends BaseCombiningAlg.Evaluator<Decidable>
	{
		private final DecisionType overridingEffectAsDecision;
		private final ExtendedDecision overridingEffectAsExtDecision;
		private final DecisionType overriddenEffectAsDecision;
		private final ExtendedDecision overriddenEffectAsExtDecision;

		private Evaluator(final Iterable<? extends Decidable> combinedElements, final EffectType overridingEffect)
		{
			super(combinedElements);
			if (overridingEffect == EffectType.DENY)
			{
				// permit-unless-deny
				this.overridingEffectAsDecision = DecisionType.DENY;
				this.overridingEffectAsExtDecision = ExtendedDecisions.SIMPLE_DENY;
				this.overriddenEffectAsDecision = DecisionType.PERMIT;
				this.overriddenEffectAsExtDecision = ExtendedDecisions.SIMPLE_PERMIT;
			}
			else
			{
				// deny-unless-permit
				this.overridingEffectAsDecision = DecisionType.PERMIT;
				this.overridingEffectAsExtDecision = ExtendedDecisions.SIMPLE_PERMIT;
				this.overriddenEffectAsDecision = DecisionType.DENY;
				this.overriddenEffectAsExtDecision = ExtendedDecisions.SIMPLE_DENY;
			}
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
		{
			assert outPepActions != null;
			/*
			 * The final decision cannot be NotApplicable so we can add all applicable policies straight to outApplicablePolicyIdList
			 */

			UpdatablePepActions pepActionsInOverriddenEffect = null;
			for (final Decidable combinedElement : getCombinedElements())
			{
				final DecisionResult result = combinedElement.evaluate(context);
				final DecisionType decision = result.getDecision();
				if (decision != DecisionType.NOT_APPLICABLE && outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
				}

				/*
				 * XACML §7.18: Obligations & Advice: do not return obligations/Advice of the rule, policy, or policy set that does not match the decision resulting from evaluating the enclosing
				 * policy set.
				 * 
				 * For example, if we return Deny, we should add to outPepActions only the PEP actions from Deny decisions
				 */
				if (decision == this.overridingEffectAsDecision)
				{
					outPepActions.add(result.getPepActions());
					return this.overridingEffectAsExtDecision;
				}

				/*
				 * Decision is overridden Effect / NotApplicable / Indeterminate
				 */
				if (decision == this.overriddenEffectAsDecision)
				{
					if (pepActionsInOverriddenEffect == null)
					{
						pepActionsInOverriddenEffect = new UpdatablePepActions();
					}

					pepActionsInOverriddenEffect.add(result.getPepActions());
				}
			}

			/*
			 * All applicable policies are already in outApplicablePolicyIdList at this point, so nothing else to do with it
			 */
			outPepActions.add(pepActionsInOverriddenEffect);
			return this.overriddenEffectAsExtDecision;
		}

	}

	/**
	 * Combining algorithm evaluator that evaluates rules in a specific order: 1) rules with overriding Effect (in same order as in the constructor argument 'rulesWithOverridingEffect'), 2) other
	 * rules (with overridden effect)
	 */
	private static final class OverridingEffectFirstRuleCombiningAlgEvaluator implements CombiningAlg.Evaluator
	{
		private static boolean haveSameEffect(final EffectType expectedEffect, final Collection<? extends RuleEvaluator> rules, final boolean mustHavePepAction)
		{
			for (final RuleEvaluator rule : rules)
			{
				if (rule.getEffect() != expectedEffect || mustHavePepAction && rule.hasNoPepAction())
				{
					return false;
				}
			}

			return true;
		}

		private final ImmutableList<RuleEvaluator> rulesWithOverridingEffect;
		private final DecisionType overridingEffectAsDecision;
		private final ImmutableList<RuleEvaluator> otherRulesWithPepActions;
		private final DecisionType overriddenEffectAsDecision;
		private final ExtendedDecision overriddenEffectAsExtDecision;

		/**
		 * Constructor
		 * 
		 * @param rulesWithOverridingEffect
		 *            combined Rules with overriding Effect. Must be non-null and non-empty.
		 * @param otherRulesWithPepActions
		 *            combined Rules with opposite/overridden Effect and PEP actions. Must be non-null and non-empty.
		 */
		OverridingEffectFirstRuleCombiningAlgEvaluator(final Collection<RuleEvaluator> rulesWithOverridingEffect, final Collection<RuleEvaluator> otherRulesWithPepActions)
		{
			assert rulesWithOverridingEffect != null && !rulesWithOverridingEffect.isEmpty() && otherRulesWithPepActions != null;

			// first rule's effect assumed the same for all
			final EffectType overridingEffect = rulesWithOverridingEffect.iterator().next().getEffect();
			assert haveSameEffect(overridingEffect, rulesWithOverridingEffect, false);

			final EffectType overriddenEffect;
			if (overridingEffect == EffectType.DENY)
			{
				overriddenEffect = EffectType.PERMIT;
				this.overridingEffectAsDecision = DecisionType.DENY;
				this.overriddenEffectAsDecision = DecisionType.PERMIT;
				this.overriddenEffectAsExtDecision = ExtendedDecisions.SIMPLE_PERMIT;
			}
			else
			{
				overriddenEffect = EffectType.DENY;
				this.overridingEffectAsDecision = DecisionType.PERMIT;
				this.overriddenEffectAsDecision = DecisionType.DENY;
				this.overriddenEffectAsExtDecision = ExtendedDecisions.SIMPLE_DENY;

			}

			assert haveSameEffect(overriddenEffect, otherRulesWithPepActions, true);

			this.rulesWithOverridingEffect = ImmutableList.copyOf(rulesWithOverridingEffect);
			this.otherRulesWithPepActions = ImmutableList.copyOf(otherRulesWithPepActions);
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions, final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
		{
			for (final RuleEvaluator rule : rulesWithOverridingEffect)
			{
				final DecisionResult evalResult = rule.evaluate(context);
				final DecisionType decision = evalResult.getDecision();
				if (decision == this.overridingEffectAsDecision)
				{
					updatablePepActions.add(evalResult.getPepActions());
					return evalResult;
				}

				// Decision is NotApplicable or Indeterminate -> ignore
			}

			/*
			 * Decision is not the overriding Effect -> final decision will be the opposite/overridden Effect. Before returning the final result, we need to collect PEP actions
			 */
			for (final RuleEvaluator rule : otherRulesWithPepActions)
			{
				final DecisionResult evalResult = rule.evaluate(context);
				final DecisionType decision = evalResult.getDecision();
				if (decision == overriddenEffectAsDecision)
				{
					// Permit/Deny
					updatablePepActions.add(evalResult.getPepActions());
				}
			}

			return this.overriddenEffectAsExtDecision;
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DPUnlessPDCombiningAlg.class);

	private final EffectType overridingEffect;
	private final EffectType overriddenEffect;
	private final CombiningAlg.Evaluator constantOverridingEffectDecisionEvaluator;
	private final CombiningAlg.Evaluator constantOverriddenEffectDecisionEvaluator;

	/**
	 * Constructor
	 * 
	 * @param algId
	 *            combining algorithm ID
	 * @param overridingEffect
	 *            overriding Effect, e.g. Permit if algId is "deny-unless-permit"
	 */
	DPUnlessPDCombiningAlg(final String algId, final Class<T> combinedType, final EffectType overridingEffect)
	{
		super(algId, combinedType);
		this.overridingEffect = overridingEffect;
		if (overridingEffect == EffectType.DENY)
		{
			overriddenEffect = EffectType.PERMIT;
			constantOverridingEffectDecisionEvaluator = CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
			constantOverriddenEffectDecisionEvaluator = CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
		}
		else
		{
			// Overriding Effect is Permit
			overriddenEffect = EffectType.DENY;
			constantOverridingEffectDecisionEvaluator = CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
			constantOverriddenEffectDecisionEvaluator = CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
		}
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends T>> params, final Iterable<? extends T> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		// if no element combined -> decision is overridden Effect
		if (combinedElements == null)
		{
			LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant decision {}", this, this.overriddenEffect);
			return this.constantOverriddenEffectDecisionEvaluator;
		}

		final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
		if (!combinedEltIterator.hasNext())
		{
			// empty (no element to combine)
			LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant decision {}", this, this.overriddenEffect);
			return this.constantOverriddenEffectDecisionEvaluator;
		}

		/*
		 * If combined elements are Rules, we can optimize
		 */
		if (!RuleEvaluator.class.isAssignableFrom(getCombinedElementType()))
		{
			return new Evaluator(combinedElements, this.overridingEffect);
		}

		// combined elements are Rules, we can optimize
		/*
		 * There is at least one Rule. Prepare to iterate over Rules.
		 */

		/*
		 * If we found any empty rule with overriding Effect, all others do not matter since the algorithm ends there with overriding Effect as decision -> ignore other rules. If there are non-empty
		 * rules with overriding Effect, for optimization, we separate them from others. If the overriding Effect is not returned as decision, the overridden Effect is always returned as decision,
		 * therefore the other rules (with overridden Effect) affect the decision result only if they have PEP action(s).
		 */
		final Deque<RuleEvaluator> nonEmptyRulesWithOverridingEffect = new ArrayDeque<>();
		final Deque<RuleEvaluator> rulesWithOverriddenEffectAndPepActions = new ArrayDeque<>();

		while (combinedEltIterator.hasNext())
		{
			final RuleEvaluator rule = (RuleEvaluator) combinedEltIterator.next();
			if (rule.getEffect() == overridingEffect)
			{
				/*
				 * If rule's effect is the overriding Effect and it has no target/condition/pep_actions, then rule will always return this Effect -> {overriding_effect}-overrides alg always evaluates
				 * to ${overriding_effect} (ignore/remove all other rules). ({overriding_effect} = Permit if algorithm is deny-unless-permit, or Deny if algorithm is permit-unless-deny in this
				 * statement.)
				 */
				if (rule.isEmptyEquivalent())
				{
					LOGGER.warn(
							"{}: {} with Effect={} is empty (no target/condition/pep_actions) => always returns {} => algorithm will always return {} => other combined rules have no effect => will be ignored/removed.",
							this, rule, this.overridingEffect, this.overridingEffect, this.overridingEffect);
					return constantOverridingEffectDecisionEvaluator;
				}

				/*
				 * Rule is not empty, i.e. has a target/condition/actions, therefore may not necessarily return its (overriding) Effect as decision
				 */
				nonEmptyRulesWithOverridingEffect.addLast(rule);
				continue;
			}

			/*
			 * Rule Effect = {overridden_Effect} (e.g. Permit if algorithm is deny-unless-permit)
			 * 
			 * In the end, if there is no applicable rule with overridING Effect, we already know that the result is always the overridden Effect with PEP actions from all other rules with same
			 * (overridden) Effect and PEP action(s).
			 */
			if (rule.hasNoPepAction())
			{
				/*
				 * Ignore this new Rule with overridden Effect and no PEP action; it will have no effect.
				 */
				LOGGER.warn("{}: Ignoring/removing {} (Effect={}, no PEP action) because it does not affect the result.", this, rule, overriddenEffect);
				// continue looking for rules with overriding Effect or with PEP actions
				continue;
			}

			// rule has PEP action(s)
			rulesWithOverriddenEffectAndPepActions.addLast(rule);
		} // END while

		/*
		 * There is at least one rule and there is no empty Rule with overriding Effect.
		 * 
		 * If there is no rule with overriding Effect and no rule with PEP action -> final result is always simple overridden Effect as decision.
		 */
		if (nonEmptyRulesWithOverridingEffect.isEmpty() && rulesWithOverriddenEffectAndPepActions.isEmpty())
		{
			LOGGER.warn(
					"{}: the only combined rule(s) is/are {} Rule(s) without PEP action => algorithm will always return {} => optimization: replacing with equivalent evaluator returning constant {} decision",
					this, this.overriddenEffect, this.overriddenEffect, this.overriddenEffect);
			return constantOverriddenEffectDecisionEvaluator;
		}

		/*
		 * (All rules have same overridden Effect, and) either there is no empty rule OR there is at least one with PEP action
		 */
		LOGGER.debug(
				"{}: 'children may be processed in any order' (XACML). This implementation will process Rules with overriding Effect first, then the others (with PEP actions only, others without are ignored)",
				this);
		return new OverridingEffectFirstRuleCombiningAlgEvaluator(nonEmptyRulesWithOverridingEffect, rulesWithOverriddenEffectAndPepActions);
	}

}
