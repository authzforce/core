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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.UpdatableCollections;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.impl.combining.CombiningAlgEvaluators.RulesWithSameEffectEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * 
 * Standard *-overrides combining algorithm.
 *
 */
final class DPOverridesCombiningAlg<T extends Decidable> extends BaseCombiningAlg<T>
{
	/**
	 * "Ordered" Deny/Permit-overrides combining algorithm evaluator; "ordered" means combined elements are evaluated in the same order as in the input collection (constructor parameter
	 * 'combinedElements'), i.e. no re-ordering.
	 */
	private static final class OrderPreservingCombiningAlgEvaluator extends BaseCombiningAlg.Evaluator<Decidable>
	{
		/**
		 * Helper to combine (not-overriding) decision results until a decision overrides or there is no more element to combine
		 */
		private static final class DecisionResultCollector
		{
			private final UpdatableList<PrimaryPolicyMetadata> combinedApplicablePolicyIdList;
			/*
			 * Replaces atLeastOneErrorDP from XACML spec. atLeastOneErrorDP == true <=> firstIndeterminateDPResult != null
			 */
			private ExtendedDecision firstIndeterminateDPResult = null;
			/*
			 * Replaces atLeastOneError${overriding_effect} from XACML spec. atLeastOneError${overriding_effect} == true <=> firstIndeterminate${overriding_effect} != null
			 */
			private ExtendedDecision firstIndeterminateOverridingEffect = null;
			/*
			 * Replaces atLeastOneError${overridden_effect} from XACML spec. atLeastOneError${overridden_effect} == true <=> firstIndeterminate${overridden_effect} != null
			 */
			private ExtendedDecision firstIndeterminateOverriddenEffect = null;

			/**
			 * Replaces atLeastOnePermit (resp. atLeastOneDeny) from description of permit-overrides (resp. deny-overrides) in the XACML spec.
			 * <p>
			 * atLeastOnePermit (resp. atLeastOneDeny) == false <=> combinedPepActions == null.
			 * <p>
			 * At this point, we don't know yet whether the PEP actions of combined/children's Permit/Deny decisions will be added to the final result's PEP actions, since we don't know yet whether
			 * the final decision is Permit/Deny.
			 */
			private UpdatablePepActions combinedPepActions = null;

			DecisionResultCollector(final boolean returnApplicablePolicyIdList)
			{
				/*
				 * Since we may combine multiple elements before returning a final decision, we have to collect them in a list; and since we don't know yet whether the final decision is NotApplicable,
				 * we cannot add collected applicable policies straight to outApplicablePolicyIdList. So we create a temporary list until we know the final decision applies.
				 */
				combinedApplicablePolicyIdList = returnApplicablePolicyIdList ? UpdatableCollections.<PrimaryPolicyMetadata> newUpdatableList() : UpdatableCollections.<PrimaryPolicyMetadata> emptyList();
			}

			/**
			 * Return new result's applicable policies combined (added last) with the ones previously found, or only the ones combined so far if result == null
			 * 
			 */
			List<PrimaryPolicyMetadata> getApplicablePolicies(final DecisionResult result)
			{
				if (result != null)
				{
					combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
				}
				return combinedApplicablePolicyIdList.copy();
			}

			/**
			 * Add intermediate (not final a priori) Deny/Permit result (update applicable policies and PEP actions), i.e. a Permit (resp. Deny) result for deny-overrides (resp. permit-overrides)
			 */
			void addSubResultDP(final DecisionResult result)
			{
				combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
				if (combinedPepActions == null)
				{
					// first Permit
					combinedPepActions = new UpdatablePepActions();
				}

				combinedPepActions.add(result.getPepActions());
			}

			/**
			 * Add intermediate (not final a priori) Indeterminate${overriding_effect} result (update applicable policies, etc.)
			 */
			void addSubResultIndeterminateInOverridingEffect(final DecisionResult result)
			{
				combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
				if (firstIndeterminateOverridingEffect == null)
				{
					firstIndeterminateOverridingEffect = result;
				}
			}

			/**
			 * Add intermediate (not final a priori) Indeterminate${overridden_effect} result (update applicable policies, etc.)
			 */
			void addSubResultIndeterminateInOverriddenEffect(final DecisionResult result)
			{
				combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
				if (firstIndeterminateOverriddenEffect == null)
				{
					firstIndeterminateOverriddenEffect = result;
				}
			}

			/**
			 * Add intermediate (not final a priori) IndeterminateDP result (update applicable policies, etc.)
			 */
			void addSubResultIndeterminateDP(final DecisionResult result)
			{
				combinedApplicablePolicyIdList.addAll(result.getApplicablePolicies());
				if (firstIndeterminateDPResult == null)
				{
					firstIndeterminateDPResult = result;
				}
			}

			/**
			 * Get any occurred IndeterminateDP result
			 */
			ExtendedDecision getFirstIndeterminateDP()
			{
				return firstIndeterminateDPResult;
			}

			/**
			 * Get any occurred Indeterminate${overriding_effect} result
			 */
			ExtendedDecision getFirstIndeterminateWithOverridingEffect()
			{
				return firstIndeterminateOverridingEffect;
			}

			/**
			 * Get any occurred Indeterminate${overridden_effect} result
			 */
			ExtendedDecision getFirstIndeterminateWithOverriddenEffect()
			{
				return firstIndeterminateOverriddenEffect;
			}

			/**
			 * Get combined PEP actions of intermediate results
			 */
			PepActions getPepActions()
			{
				return combinedPepActions;
			}
		}

		private interface SubDecisionHandler
		{
			/**
			 * Handles the decision result of one of the combined elements
			 * 
			 * @param result
			 *            decision result of a combined element to handle
			 * @param resultCollector
			 *            used to collect the result (added/combined with previous ones), if the decision does not override, to be reused later at the end of the evaluation
			 * @return true iff the result overrides all others (is final), e.g. Deny for deny-overrides
			 */
			boolean handle(DecisionResult result, DecisionResultCollector resultCollector);
		}

		private static final SubDecisionHandler OVERRIDING_SUBDECISIONHANDLER = new SubDecisionHandler()
		{

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				return true;
			}

		};

		private static final SubDecisionHandler OVERRIDDEN_DP_SUBDECISIONHANDLER = new SubDecisionHandler()
		{

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				resultCollector.addSubResultDP(result);
				return false;
			}

		};

		private static final SubDecisionHandler NOT_APPLICABLE_SUBDECISIONHANDLER = new SubDecisionHandler()
		{

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				return false;
			}

		};

		private static final SubDecisionHandler INDETERMINATE_OVERRIDING_EFFECT_SUBDECISIONHANDLER = new SubDecisionHandler()
		{

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				resultCollector.addSubResultIndeterminateInOverridingEffect(result);
				return false;
			}

		};

		private static final SubDecisionHandler INDETERMINATE_OVERRIDDEN_EFFECT_SUBDECISIONHANDLER = new SubDecisionHandler()
		{

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				resultCollector.addSubResultIndeterminateInOverriddenEffect(result);
				return false;
			}

		};

		private static final SubDecisionHandler INDETERMINATE_DP_SUBDECISIONHANDLER = new SubDecisionHandler()
		{

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				resultCollector.addSubResultIndeterminateDP(result);
				return false;
			}

		};

		private static final class IndeterminateSubDecisionHandler implements SubDecisionHandler
		{
			private final Map<DecisionType, SubDecisionHandler> indeterminateResultHandlersByExtendedIndeterminateType;

			private IndeterminateSubDecisionHandler(final Map<DecisionType, SubDecisionHandler> indeterminateResultHandlersByExtendedIndeterminateType)
			{
				this.indeterminateResultHandlersByExtendedIndeterminateType = indeterminateResultHandlersByExtendedIndeterminateType;
			}

			@Override
			public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
			{
				final DecisionType extIndeterminate = result.getExtendedIndeterminate();
				assert extIndeterminate != null && extIndeterminate != DecisionType.NOT_APPLICABLE;
				return this.indeterminateResultHandlersByExtendedIndeterminateType.get(extIndeterminate).handle(result, resultCollector);
			}

		}

		private final ExtendedDecision decisionForOverridingEffect;
		private final ExtendedDecision decisionForOverriddenEffect;
		private final Map<DecisionType, SubDecisionHandler> resultHandlersByDecisionType = new EnumMap<>(DecisionType.class);

		OrderPreservingCombiningAlgEvaluator(final Iterable<? extends Decidable> combinedElements, final EffectType overridingEffect)
		{
			super(combinedElements);
			resultHandlersByDecisionType.put(DecisionType.NOT_APPLICABLE, NOT_APPLICABLE_SUBDECISIONHANDLER);
			final Map<DecisionType, SubDecisionHandler> indeterminateResultHandlersByExtendedIndeterminateType = new EnumMap<>(DecisionType.class);
			indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.INDETERMINATE, INDETERMINATE_DP_SUBDECISIONHANDLER);
			if (overridingEffect == EffectType.DENY)
			{
				// deny-overrides
				this.decisionForOverridingEffect = ExtendedDecisions.SIMPLE_DENY;
				this.decisionForOverriddenEffect = ExtendedDecisions.SIMPLE_PERMIT;
				resultHandlersByDecisionType.put(DecisionType.DENY, OVERRIDING_SUBDECISIONHANDLER);
				indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.DENY, INDETERMINATE_OVERRIDING_EFFECT_SUBDECISIONHANDLER);
				resultHandlersByDecisionType.put(DecisionType.PERMIT, OVERRIDDEN_DP_SUBDECISIONHANDLER);
				indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.PERMIT, INDETERMINATE_OVERRIDDEN_EFFECT_SUBDECISIONHANDLER);
			}
			else
			{
				this.decisionForOverridingEffect = ExtendedDecisions.SIMPLE_PERMIT;
				this.decisionForOverriddenEffect = ExtendedDecisions.SIMPLE_DENY;
				resultHandlersByDecisionType.put(DecisionType.PERMIT, OVERRIDING_SUBDECISIONHANDLER);
				indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.PERMIT, INDETERMINATE_OVERRIDING_EFFECT_SUBDECISIONHANDLER);
				resultHandlersByDecisionType.put(DecisionType.DENY, OVERRIDDEN_DP_SUBDECISIONHANDLER);
				indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.DENY, INDETERMINATE_OVERRIDDEN_EFFECT_SUBDECISIONHANDLER);
			}

			resultHandlersByDecisionType.put(DecisionType.INDETERMINATE, new IndeterminateSubDecisionHandler(indeterminateResultHandlersByExtendedIndeterminateType));
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
		{
			assert outPepActions != null;
			final DecisionResultCollector resultCollector = new DecisionResultCollector(outApplicablePolicyIdList != null);

			for (final Decidable combinedElement : getCombinedElements())
			{
				// evaluate the policy
				final DecisionResult result = combinedElement.evaluate(context);
				final boolean isResultOverriding = resultHandlersByDecisionType.get(result.getDecision()).handle(result, resultCollector);

				/*
				 * XACML §7.18: Obligations & Advice: do not return Obligations/Advice of the rule, policy, or policy set that does not match the decision resulting from evaluating the enclosing
				 * policy set. For example, if the final decision is Permit, we should add to outPepActions only the PEP actions from Permit decisions (permitPepActions)
				 */
				if (isResultOverriding)
				{
					/*
					 * result overrides all others, return it right away after updating output applicable policies and PEP actions
					 */
					if (outApplicablePolicyIdList != null)
					{
						outApplicablePolicyIdList.addAll(resultCollector.getApplicablePolicies(result));
					}

					outPepActions.add(result.getPepActions());
					return this.decisionForOverridingEffect;
				}

			}

			/*
			 * There was no overriding Deny/Permit decision, i.e. Deny (resp. Permit) in case of deny-overrides (resp. permit-overrides) alg, else: if any Indeterminate{DP}, then Indeterminate{DP}
			 */
			final ExtendedDecision firstIndeterminateDP = resultCollector.getFirstIndeterminateDP();
			if (firstIndeterminateDP != null)
			{
				// at least one Indeterminate{DP}
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(resultCollector.getApplicablePolicies(null));
				}

				return firstIndeterminateDP;
			}

			final List<PrimaryPolicyMetadata> combinedApplicablePolicies = resultCollector.getApplicablePolicies(null);
			final PepActions combinedPepActionsOfNotOverridingDP = resultCollector.getPepActions();
			final ExtendedDecision firstIndeterminateWithOverridingEffect = resultCollector.getFirstIndeterminateWithOverridingEffect();
			final ExtendedDecision firstIndeterminateWithOverriddenEffect = resultCollector.getFirstIndeterminateWithOverriddenEffect();

			/*
			 * ${overriding_effect} = Deny and ${overridden_effect} = Permit (resp. Deny) in case of deny-overrides (resp. permit-overrides) algorithm.
			 * 
			 * If any Indeterminate{overriding_effect}, then: if ( any Indeterminate{overridden_effect} or any decision with ${overridden_effect} ) -> Indeterminate{DP}; else ->
			 * Indeterminate{overriding_effect} (this is a simplified equivalent of the algo in the spec)
			 */
			/*
			 * atLeastOne${overridden_effect} == true <=> ${overridden_effect}PepActions != null
			 */
			if (firstIndeterminateWithOverridingEffect != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return ExtendedDecisions.newIndeterminate(firstIndeterminateWithOverriddenEffect != null || combinedPepActionsOfNotOverridingDP != null ? DecisionType.INDETERMINATE
						: decisionForOverridingEffect.getDecision(), firstIndeterminateWithOverridingEffect.getCauseForIndeterminate().get());
			}

			/*
			 * If we got a decision with overridden effect (e.g. Permit in case of deny-overrides algo) or Indeterminate{overridden_effect}, return it, otherwise it's NOT_APPLICABLE
			 */
			if (combinedPepActionsOfNotOverridingDP != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				outPepActions.add(combinedPepActionsOfNotOverridingDP);
				return decisionForOverriddenEffect;
			}

			if (firstIndeterminateWithOverriddenEffect != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return firstIndeterminateWithOverriddenEffect;
			}

			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	}

	/**
	 * Combining algorithm evaluator that evaluates rules in a specific order: 1) rules with overriding Effect (in same order as in the constructor argument 'rulesWithOverridingEffect'), 2) other
	 * rules (with overridden effect)
	 */
	private static final class OverridingEffectFirstRuleCombiningAlgEvaluator extends RulesWithSameEffectEvaluator
	{
		private final ImmutableList<RuleEvaluator> otherRules;
		private final DecisionType overriddenEffectAsDecision;
		private final ExtendedDecision overriddenEffectAsExtDecision;

		/**
		 * Constructor
		 * 
		 * @param rulesWithOverridingEffect
		 *            combined Rules with overriding Effect. Must be non-null and non-empty.
		 * @param otherRules
		 *            combined Rules with opposite/overridden Effect. Must be non-null and non-empty.
		 */
		OverridingEffectFirstRuleCombiningAlgEvaluator(final Collection<RuleEvaluator> rulesWithOverridingEffect, final Collection<RuleEvaluator> otherRules)
		{
			super(rulesWithOverridingEffect);

			assert otherRules != null && !otherRules.isEmpty();

			// first rule's effect assumed the same for all
			final EffectType overriddenEffect = otherRules.iterator().next().getEffect();

			assert rulesWithOverridingEffect.iterator().next().getEffect() != overriddenEffect && haveSameEffect(overriddenEffect, otherRules);

			if (overriddenEffect == EffectType.DENY)
			{
				this.overriddenEffectAsDecision = DecisionType.DENY;
				this.overriddenEffectAsExtDecision = ExtendedDecisions.SIMPLE_DENY;
			}
			else
			{
				this.overriddenEffectAsDecision = DecisionType.PERMIT;
				this.overriddenEffectAsExtDecision = ExtendedDecisions.SIMPLE_PERMIT;
			}

			this.otherRules = ImmutableList.copyOf(otherRules);
		}

		/**
		 * Evaluate rules with overridden Effect in the case when the evaluation of the rules with overriding Effect returned Indeterminate
		 * 
		 * @param indeterminateFromRulesWithOverridingEffect
		 *            Indeterminate result from previous evaluation of rules with overridING effect
		 * @return final decision
		 */
		private ExtendedDecision evaluateRulesWithOverriddenEffect(final EvaluationContext context, final ExtendedDecision indeterminateFromRulesWithOverridingEffect)
		{
			/*
			 * indeterminateFromRulesWithOverridingEffect's decision assumed Indeterminate{overriding_effect}, overriding_effect = D (resp. P) if overriding Effect is Deny (resp. Permit)
			 */
			assert indeterminateFromRulesWithOverridingEffect != null && indeterminateFromRulesWithOverridingEffect.getDecision() == DecisionType.INDETERMINATE;

			for (final RuleEvaluator rule : otherRules)
			{
				final DecisionResult evalResult = rule.evaluate(context);
				if (evalResult.getDecision() != DecisionType.NOT_APPLICABLE)
				{
					/**
					 * decision is the overridden Effect or Indeterminate{overridden_effect}, which we have to combine with previous result (from rules with overriding Effect)
					 * Indeterminate{overriding_effect}. For example,
					 * <p>
					 * IndeterminateD and (IndeterminateP or Permit)
					 * </p>
					 * <p>
					 * OR
					 * </p>
					 * <p>
					 * IndeterminateP and (IndeterminateD or Deny)
					 * </p>
					 * <p>
					 * => IndeterminateDP in both cases
					 * </p>
					 */
					return ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE, indeterminateFromRulesWithOverridingEffect.getCauseForIndeterminate().get());
				}

				// Else decision is NotApplicable, do nothing, continue
			}

			/*
			 * All other rules (with overridden effect) NotApplicable -> initial Indeterminate result unchanged
			 */
			return indeterminateFromRulesWithOverridingEffect;
		}

		/**
		 * Evaluate rules with overridden Effect in the case when the evaluation of the rules with overriding Effect returned NotApplicable
		 * 
		 * @return final decision
		 */
		private ExtendedDecision evaluateRulesWithOverriddenEffect(final EvaluationContext context, final UpdatablePepActions updatablePepActions)
		{
			/**
			 * Replaces atLeastOnePermit (resp. atLeastOneDeny) from description of deny-overrides (resp. permit-overrides) in the XACML spec.
			 * <p>
			 * atLeastOnePermit/atLeastOneDeny == false <=> combinedPepActions == null.
			 * <p>
			 * At this point, we don't know yet whether the PEP actions of combined/children's Permit/Deny decisions will be added to the final result's PEP actions, since we don't know yet whether
			 * the final decision is Permit/Deny.
			 */
			UpdatablePepActions combinedPepActions = null;

			ExtendedDecision firstIndeterminateInOverriddenEffect = null;
			for (final RuleEvaluator rule : otherRules)
			{
				final DecisionResult evalResult = rule.evaluate(context);
				final DecisionType decision = evalResult.getDecision();
				if (decision == overriddenEffectAsDecision)
				{
					// Permit/Deny
					if (combinedPepActions == null)
					{
						combinedPepActions = new UpdatablePepActions();
					}

					combinedPepActions.add(evalResult.getPepActions());
				}

				/*
				 * If the decision is Indeterminate, save the indeterminate cause for the final Indeterminate result (if first Indeterminate), only used if no other rule with determinate result
				 * checked above is found.
				 */
				if (decision == DecisionType.INDETERMINATE && firstIndeterminateInOverriddenEffect == null)
				{
					// first Indeterminate for overridden effect
					firstIndeterminateInOverriddenEffect = evalResult;
				}
			}

			// if(atLeastOnePermit/atLeastOneDeny)...
			if (combinedPepActions != null)
			{
				updatablePepActions.add(combinedPepActions);
				return this.overriddenEffectAsExtDecision;
			}

			/*
			 * All decisions were NotApplicable or Indeterminate{overridden_effect}
			 */
			// at Least One Indeterminate
			if (firstIndeterminateInOverriddenEffect != null)
			{
				return firstIndeterminateInOverriddenEffect;
			}

			// All decisions were NotApplicable -> NotApplicable
			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions, final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
		{
			final ExtendedDecision extDecisionFromRulesWithOverridingEffect = super.evaluate(context, updatablePepActions, updatableApplicablePolicyIdList);
			switch (extDecisionFromRulesWithOverridingEffect.getDecision())
			{
				case DENY:
				case PERMIT:
					return extDecisionFromRulesWithOverridingEffect;

				case INDETERMINATE:
					// Optimize
					return evaluateRulesWithOverriddenEffect(context, extDecisionFromRulesWithOverridingEffect);
				default:
					// NotApplicable
					// Optimize
					return evaluateRulesWithOverriddenEffect(context, updatablePepActions);
			}
		}

	}

	private interface RuleCollector
	{
		void addNonEmptyRuleWithOverridingEffect(RuleEvaluator rule);

		boolean hasRuleWithOverriddenEffectAndPepAction();

		void addFirstEmptyRuleWithOverriddenEffect(RuleEvaluator rule);

		void addNonEmptyRuleWithOverriddenEffectButNoPepAction(RuleEvaluator rule);

		void addRuleWithOverriddenEffectAndPepActions(RuleEvaluator rule);

		boolean hasRuleWithOverridingEffect();

		boolean hasRuleWithOverriddenEffect();

		CombiningAlg.Evaluator getRuleCombiningAlgEvaluatorAssumingAllWithOverridingEffect();

		CombiningAlg.Evaluator getRuleCombiningAlgEvaluatorAssumingAllWithOverriddenEffect();

		CombiningAlg.Evaluator getDPOverridesRuleCombiningAlgEvaluator(EffectType overridingEffect);
	}

	private interface RuleCollectorFactory
	{
		RuleCollector newInstance();
	}

	/**
	 * Rule collector that preserve the order of the rules, i.e. order of insertion/appending, as opposed to OverridingEffectFirstRuleCollector
	 */
	private static class OrderPreservingRuleCollector implements RuleCollector
	{
		boolean atLeastOneRuleWithOverridingEffect = false;
		boolean atLeastOneRuleWithOverriddenEffectButNoPepAction = false;
		boolean atLeastOneRuleWithOverriddenEffectAndPepAction = false;
		final List<RuleEvaluator> addedRules = new ArrayList<>();

		@Override
		public void addNonEmptyRuleWithOverridingEffect(final RuleEvaluator rule)
		{
			atLeastOneRuleWithOverridingEffect = true;
			addedRules.add(rule);
		}

		@Override
		public void addNonEmptyRuleWithOverriddenEffectButNoPepAction(final RuleEvaluator rule)
		{
			assert rule.hasNoPepAction();
			atLeastOneRuleWithOverriddenEffectButNoPepAction = true;
			addedRules.add(rule);
		}

		@Override
		public void addRuleWithOverriddenEffectAndPepActions(final RuleEvaluator rule)
		{
			assert !rule.hasNoPepAction();
			atLeastOneRuleWithOverriddenEffectAndPepAction = true;
			addedRules.add(rule);
		}

		@Override
		public void addFirstEmptyRuleWithOverriddenEffect(final RuleEvaluator rule)
		{
			atLeastOneRuleWithOverriddenEffectButNoPepAction = true;
			/*
			 * Remove all rules with overridden Effect but no PEP action, since they have no effect if there is an empty rule (that always successfully evaluate to overridden Effect)
			 */
			final Iterator<RuleEvaluator> it = addedRules.iterator();
			while (it.hasNext())
			{
				final RuleEvaluator addedRule = it.next();
				if (addedRule.hasNoPepAction())
				{
					it.remove();
					if (LOGGER.isWarnEnabled())
					{
						LOGGER.warn(
								"ordered-{}-overrides algorithm: Ignoring/removing {} (Effect={}) because it does not affect the result, since it does no have any PEP action and we already found an empty Rule ({}) with same Effect (always returns {}).",
								rule.getEffect() == EffectType.DENY ? "permit" : "deny", addedRule, addedRule.getEffect(), rule, addedRule.getEffect());
					}
				}
			}

			addedRules.add(rule);
		}

		@Override
		public boolean hasRuleWithOverridingEffect()
		{
			return atLeastOneRuleWithOverridingEffect;
		}

		@Override
		public boolean hasRuleWithOverriddenEffect()
		{
			return atLeastOneRuleWithOverriddenEffectButNoPepAction || atLeastOneRuleWithOverriddenEffectAndPepAction;
		}

		@Override
		public boolean hasRuleWithOverriddenEffectAndPepAction()
		{
			return atLeastOneRuleWithOverriddenEffectAndPepAction;
		}

		@Override
		public CombiningAlg.Evaluator getRuleCombiningAlgEvaluatorAssumingAllWithOverridingEffect()
		{
			assert !hasRuleWithOverriddenEffect();
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(addedRules);
		}

		@Override
		public CombiningAlg.Evaluator getRuleCombiningAlgEvaluatorAssumingAllWithOverriddenEffect()
		{
			assert !hasRuleWithOverridingEffect();
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(addedRules);
		}

		@Override
		public CombiningAlg.Evaluator getDPOverridesRuleCombiningAlgEvaluator(final EffectType overridingEffect)
		{
			return new OrderPreservingCombiningAlgEvaluator(addedRules, overridingEffect);
		}

	}

	/**
	 * 
	 * Rule collector that groups rules by Effect, with overridding Effect first (e.g. Deny rules before Permit rules if algorithm is deny-overrides)
	 *
	 */
	private static class OverridingEffectFirstRuleCollector implements RuleCollector
	{
		/*
		 * We will reorder rules with overriding Effect (e.g. Deny for deny-overrides algorithm) before rules with overridden Effect (e.g. Permit for deny-overrides algorithm) since order does not
		 * matter and overriding Effect overrides/prevails.
		 */
		final Deque<RuleEvaluator> nonEmptyRulesWithOverridingEffect = new ArrayDeque<>();
		final Deque<RuleEvaluator> rulesWithOverriddenEffectButNoPepAction = new ArrayDeque<>();
		final Deque<RuleEvaluator> rulesWithOverriddenEffectAndPepActions = new ArrayDeque<>();

		@Override
		public void addNonEmptyRuleWithOverridingEffect(final RuleEvaluator rule)
		{
			nonEmptyRulesWithOverridingEffect.addLast(rule);
		}

		@Override
		public void addNonEmptyRuleWithOverriddenEffectButNoPepAction(final RuleEvaluator rule)
		{
			assert rule.hasNoPepAction();
			rulesWithOverriddenEffectButNoPepAction.addLast(rule);
		}

		@Override
		public void addRuleWithOverriddenEffectAndPepActions(final RuleEvaluator rule)
		{
			assert !rule.hasNoPepAction();
			rulesWithOverriddenEffectAndPepActions.addLast(rule);
		}

		@Override
		public void addFirstEmptyRuleWithOverriddenEffect(final RuleEvaluator rule)
		{
			/*
			 * We may ignore all other rules with same overridden Effect and without PEP action
			 */
			if (LOGGER.isWarnEnabled())
			{
				LOGGER.warn(
						"{}-overrides algorithm: found empty rule {} (Effect = {}) -> Ignoring/removing all rules found so far with same Effect and without any PEP action ( {} ) because they do not affect the final result.",
						rule.getEffect() == EffectType.DENY ? "permit" : "deny", rule, rule.getEffect(), rulesWithOverriddenEffectButNoPepAction);
			}
			rulesWithOverriddenEffectButNoPepAction.clear();
			rulesWithOverriddenEffectButNoPepAction.addLast(rule);
		}

		@Override
		public boolean hasRuleWithOverridingEffect()
		{
			return !nonEmptyRulesWithOverridingEffect.isEmpty();
		}

		@Override
		public boolean hasRuleWithOverriddenEffect()
		{
			return !rulesWithOverriddenEffectButNoPepAction.isEmpty() || !rulesWithOverriddenEffectAndPepActions.isEmpty();
		}

		@Override
		public boolean hasRuleWithOverriddenEffectAndPepAction()
		{
			return !rulesWithOverriddenEffectAndPepActions.isEmpty();
		}

		@Override
		public CombiningAlg.Evaluator getRuleCombiningAlgEvaluatorAssumingAllWithOverridingEffect()
		{
			assert rulesWithOverriddenEffectButNoPepAction.isEmpty() && rulesWithOverriddenEffectAndPepActions.isEmpty();
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(nonEmptyRulesWithOverridingEffect);
		}

		private Deque<RuleEvaluator> getRulesWithOverriddenEffect()
		{
			if (rulesWithOverriddenEffectButNoPepAction.isEmpty())
			{
				return rulesWithOverriddenEffectAndPepActions;
			}

			if (rulesWithOverriddenEffectAndPepActions.isEmpty())
			{
				return rulesWithOverriddenEffectButNoPepAction;
			}

			final Deque<RuleEvaluator> rulesWithOverriddenEffect = new ArrayDeque<>(rulesWithOverriddenEffectButNoPepAction.size() + rulesWithOverriddenEffectAndPepActions.size());
			rulesWithOverriddenEffect.addAll(rulesWithOverriddenEffectButNoPepAction);
			rulesWithOverriddenEffect.addAll(rulesWithOverriddenEffectAndPepActions);
			return rulesWithOverriddenEffect;
		}

		@Override
		public CombiningAlg.Evaluator getRuleCombiningAlgEvaluatorAssumingAllWithOverriddenEffect()
		{
			assert nonEmptyRulesWithOverridingEffect.isEmpty();
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(getRulesWithOverriddenEffect());
		}

		@Override
		public CombiningAlg.Evaluator getDPOverridesRuleCombiningAlgEvaluator(final EffectType overridingEffect)
		{
			return new OverridingEffectFirstRuleCombiningAlgEvaluator(nonEmptyRulesWithOverridingEffect, getRulesWithOverriddenEffect());
		}
	}

	private static final RuleCollectorFactory OVERRIDING_EFFECT_FIRST_RULE_COLLECTOR_FACTORY = new RuleCollectorFactory()
	{

		@Override
		public RuleCollector newInstance()
		{
			LOGGER.debug("Rule combining algorithm is permit/deny-overrides: 'children may be processed in any order' (XACML). This implementation will process Rules with overriding Effect first, then the others without PEP action, and finally the others with PEP action(s)");
			return new OverridingEffectFirstRuleCollector();
		}

	};

	private static final RuleCollectorFactory ORDER_PRESERVING_RULE_COLLECTOR_FACTORY = new RuleCollectorFactory()
	{

		@Override
		public RuleCollector newInstance()
		{
			return new OrderPreservingRuleCollector();
		}

	};

	private static final Logger LOGGER = LoggerFactory.getLogger(DPOverridesCombiningAlg.class);

	private final EffectType overridingEffect;
	private final EffectType overriddenEffect;
	private final RuleCollectorFactory ruleCollectorFactory;
	private final CombiningAlg.Evaluator constantOverridingEffectDecisionEvaluator;
	private final CombiningAlg.Evaluator constantOverriddenEffectDecisionEvaluator;

	/**
	 * Constructor
	 * 
	 * @param algId
	 *            combining algorithm ID
	 * @param overridingEffect
	 *            overriding effect (e.g. Deny for deny-overrides algorithm)
	 * @param isOrdered
	 *            true iff combined elements must be evaluated in order of declaration, i.e. in same order as in 'combinedElements' argument of {@link #getInstance(Iterable, Iterable)}. If false, the
	 *            order is changed, in particular optimized by evaluating rules with overriding Effect first.
	 */
	DPOverridesCombiningAlg(final String algId, final Class<T> combinedType, final EffectType overridingEffect, final boolean isOrdered)
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

		this.ruleCollectorFactory = isOrdered ? ORDER_PRESERVING_RULE_COLLECTOR_FACTORY : OVERRIDING_EFFECT_FIRST_RULE_COLLECTOR_FACTORY;
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends T>> params, final Iterable<? extends T> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		// if no element -> NotApplicable
		if (combinedElements == null)
		{
			LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant NotApplicable decision", this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
		if (!combinedEltIterator.hasNext())
		{
			// empty (no element to combine)
			LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant NotApplicable decision", this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		/*
		 * If combined elements are Rules, we can optimize
		 */
		if (!RuleEvaluator.class.isAssignableFrom(getCombinedElementType()))
		{
			return new OrderPreservingCombiningAlgEvaluator(combinedElements, this.overridingEffect);
		}

		// combined elements are Rules, we can optimize
		/*
		 * There is at least one Rule. Prepare to iterate over Rules and collect them in a specific way (depending on whether it is "ordered-*-overrides" kind of algorithm or not).
		 */
		final RuleCollector ruleCollector = ruleCollectorFactory.newInstance();

		/*
		 * 
		 * If we find any empty Rule in overridden Effect (no target/condition/pep_action), we don't need to look at other rules with such Effect and no PEP action; because if there is no rule with
		 * overriding Effect, this rule is enough to return the overridden Effect as decision, and PEP actions come from all other rules with same Effect and PEP actions (e.g. if algorithm is
		 * deny-overrides, then if there is no applicable Deny rule, if there is any empty Permit rule, the result is Permit with PEP actions combined from all other Permit rules with PEP actions)
		 */
		RuleEvaluator firstEmptyRuleWithOverriddenEffect = null;

		while (combinedEltIterator.hasNext())
		{
			final RuleEvaluator rule = (RuleEvaluator) combinedEltIterator.next();
			if (rule.getEffect() == overridingEffect)
			{
				/*
				 * If rule's effect is the overriding Effect and it has no target/condition/pep_actions, then rule will always return this Effect -> {overriding_effect}-overrides alg always evaluates
				 * to ${overriding_effect} (ignore/remove all other rules). ({overriding_effect} = Permit if algorithm is Permit-overrides, or Deny if algorithm is Deny-overrides in this statement.)
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
				ruleCollector.addNonEmptyRuleWithOverridingEffect(rule);
				continue;
			}

			/*
			 * Rule Effect = {overridden_Effect} (e.g. Permit if algorithm is deny-overrides)
			 * 
			 * In the end, if there is no applicable rule with overridING Effect, and if there is an empty rule with such overridden Effect, we already know that the result is always the overridden
			 * Effect with PEP actions from all other rules with same Effect and PEP action(s).
			 */
			if (firstEmptyRuleWithOverriddenEffect != null)
			{
				if (rule.hasNoPepAction())
				{
					/*
					 * Ignore this new Rule with overridden Effect and no PEP action; it will have no effect since we are sure the empty rule (that we already found) with overridden Effect will
					 * successfully evaluate.
					 */
					LOGGER.warn(
							"{}: Ignoring/removing {} (Effect={}) because it does not affect the result, since it does no have any PEP action and we already found an empty Rule ({}) found with same Effect (always returns {}).",
							this, rule, overriddenEffect, firstEmptyRuleWithOverriddenEffect, overriddenEffect);
					// continue looking for rules with overriding Effect
					continue;
				}

				// rule has PEP action(s), therefore it matters if final result is overridden Effect
				ruleCollector.addRuleWithOverriddenEffectAndPepActions(rule);
				continue;
			}

			// No empty Rule with overridden Effect found yet (firstEmptyRuleWithOverriddenEffect == null)
			if (rule.isEmptyEquivalent())
			{
				// rule has no PEP action -> firstEmptyRuleWithOverriddenEffect == null
				/*
				 * This is the first declared empty Rule with overridden Effect -> always returns the overridden Effect as decision; we can ignore/remove other Rules with overridden Effect unless they
				 * have PEP actions (have no effect anymore)
				 */
				LOGGER.warn(
						"{}: {} (Effect={}) is empty (no target/condition/pep_actions) => always returns {} => algorithm will always return {} unless some {} rule applies => other combined {} rules without any PEP action have no effect => will be ignored/removed.",
						this, rule, overriddenEffect, overriddenEffect, overriddenEffect, overridingEffect, overriddenEffect);
				firstEmptyRuleWithOverriddenEffect = rule;
				ruleCollector.addFirstEmptyRuleWithOverriddenEffect(rule);
				// continue looking for rules with overriding Effect or with PEP actions
				continue;
			}

			/*
			 * Non-empty Rule with overridden Effect found yet and current rule is not empty
			 */
			if (rule.hasNoPepAction())
			{
				ruleCollector.addNonEmptyRuleWithOverriddenEffectButNoPepAction(rule);
			}
			else
			{
				ruleCollector.addRuleWithOverriddenEffectAndPepActions(rule);
			}

		} // END while

		/*
		 * There is at least one rule and there is no empty Rule with overriding Effect.
		 * 
		 * If there is no rule with overriding Effect...
		 */
		if (!ruleCollector.hasRuleWithOverridingEffect())
		{
			/*
			 * No Rule with overriding Effect (whether empty or not) -> at least one Rule with overridden Effect and all rules have this same overridden Effect. If we found an empty rule with
			 * overridden Effect and no other with PEP action, the decision is the constant overridden Effect without PEP action.
			 */
			if (firstEmptyRuleWithOverriddenEffect != null && !ruleCollector.hasRuleWithOverriddenEffectAndPepAction())
			{
				/*
				 * no Rule with overriding Effect or PEP action, but one empty rule with overridden Effect -> final result is the overridden Effect as simple decision (no PEP action) always
				 */
				LOGGER.warn(
						"{}: the only combined rule is empty {} Rule ({}) => algorithm will always return this {} => optimization: replacing with equivalent evaluator returning constant {} decision",
						this, this.overriddenEffect, firstEmptyRuleWithOverriddenEffect, this.overriddenEffect, this.overriddenEffect);
				return constantOverriddenEffectDecisionEvaluator;
			}

			/*
			 * (All rules have same overridden Effect, and) either there is no empty rule OR there is at least one with PEP action
			 */
			return ruleCollector.getRuleCombiningAlgEvaluatorAssumingAllWithOverriddenEffect();
		}

		/*
		 * There is at least one non-empty rule with overriding Effect.
		 * 
		 * If there is no rule with overridden Effect...
		 */

		if (!ruleCollector.hasRuleWithOverriddenEffect())
		{
			/*
			 * No rule with overridden Effect -> only non-empty rules with same overriding Effect
			 */
			return ruleCollector.getRuleCombiningAlgEvaluatorAssumingAllWithOverridingEffect();
		}

		/*
		 * At least one Rule with overridden Effect and only non-empty rules with overriding Effect
		 */
		return ruleCollector.getDPOverridesRuleCombiningAlgEvaluator(overridingEffect);
	}
}
