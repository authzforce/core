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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

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
import org.ow2.authzforce.core.pdp.impl.combining.CombiningAlgEvaluators.RulesWithSameEffectEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * 
 * Standard ordered-*-overrides combining algorithm
 *
 */
final class OrderedDPOverridesCombiningAlg extends BaseCombiningAlg<Decidable>
{
	private static final class OverridingEffectFirstRuleCombiningAlgEvaluator extends RulesWithSameEffectEvaluator
	{
		private final ImmutableList<RuleEvaluator> otherRules;
		private final DecisionType overriddenEffectAsDecision;

		/**
		 * Instantiates the evaluator a list of rules with same Effect, inferring the effect from the first rule in the list
		 * 
		 * @param rulesWithSameEffect
		 *            combined Rules, all expected to have the same Effect. Must be non-null and non-empty.
		 */
		OverridingEffectFirstRuleCombiningAlgEvaluator(final Collection<RuleEvaluator> rulesWithOverridingEffect, final Collection<RuleEvaluator> otherRules)
		{
			super(rulesWithOverridingEffect);
			assert otherRules != null && !otherRules.isEmpty() && rulesWithOverridingEffect.iterator().next().getEffect() != otherRules.iterator().next().getEffect();
			// first rule's effect assumed the same for all
			this.overriddenEffectAsDecision = otherRules.iterator().next().getEffect() == EffectType.DENY ? DecisionType.DENY : DecisionType.PERMIT;
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
					return ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE, indeterminateFromRulesWithOverridingEffect.getStatus());
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
			ExtendedDecision firstIndeterminateInOverriddenEffect = null;
			for (final RuleEvaluator rule : otherRules)
			{
				final DecisionResult evalResult = rule.evaluate(context);
				final DecisionType decision = evalResult.getDecision();
				if (decision == overriddenEffectAsDecision)
				{
					// Permit/Deny
					updatablePepActions.add(evalResult.getPepActions());
					return evalResult;
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
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> updatableApplicablePolicyIdList)
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

	private static final class DPOverridesPolicyCombiningAlgEvaluator extends BaseCombiningAlg.Evaluator<Decidable>
	{
		/**
		 * Helper to combine (not-overriding) decision results until a decision overrides or there is no more element to combine
		 */
		private static final class DecisionResultCollector
		{
			private final UpdatableList<JAXBElement<IdReferenceType>> combinedApplicablePolicyIdList;
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
				combinedApplicablePolicyIdList = returnApplicablePolicyIdList ? UpdatableCollections.<JAXBElement<IdReferenceType>> newUpdatableList() : UpdatableCollections
						.<JAXBElement<IdReferenceType>> emptyList();
			}

			/**
			 * Return new result's applicable policies combined (added last) with the ones previously found, or only the ones combined so far if result == null
			 * 
			 */
			List<JAXBElement<IdReferenceType>> getApplicablePolicies(final DecisionResult result)
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
			void addSubResultIndeterminateOverridingEffect(final DecisionResult result)
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
				resultCollector.addSubResultIndeterminateOverridingEffect(result);
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

		DPOverridesPolicyCombiningAlgEvaluator(final Iterable<? extends Decidable> combinedElements, final EffectType overridingEffect)
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
		public final ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList)
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

			final List<JAXBElement<IdReferenceType>> combinedApplicablePolicies = resultCollector.getApplicablePolicies(null);
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
						: decisionForOverridingEffect.getDecision(), firstIndeterminateWithOverridingEffect.getStatus());
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

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderedDPOverridesCombiningAlg.class);

	private final EffectType overridingEffect;
	private final EffectType overriddenEffect;

	private final CombiningAlg.Evaluator evaluatorIfEmptyRuleWithOverridingEffect;
	private final CombiningAlg.Evaluator evaluatorIfEmptyRuleWithOverriddenEffectAndNoneWithOverridingOne;

	OrderedDPOverridesCombiningAlg(final String algId, final EffectType overridingEffect)
	{
		super(algId, Decidable.class);
		this.overridingEffect = overridingEffect;
		if (overridingEffect == EffectType.DENY)
		{
			overriddenEffect = EffectType.PERMIT;
			evaluatorIfEmptyRuleWithOverridingEffect = CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
			evaluatorIfEmptyRuleWithOverriddenEffectAndNoneWithOverridingOne = CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
		}
		else
		{
			// Overriding Effect is Permit
			overriddenEffect = EffectType.DENY;
			evaluatorIfEmptyRuleWithOverridingEffect = CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
			evaluatorIfEmptyRuleWithOverriddenEffectAndNoneWithOverridingOne = CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
		}
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends Decidable>> params, final Iterable<? extends Decidable> combinedElements)
			throws UnsupportedOperationException, IllegalArgumentException
	{
		/*
		 * If combined elements are Rules, we can optimize
		 */
		if (!RuleEvaluator.class.isAssignableFrom(getCombinedElementType()))
		{
			return new DPOverridesPolicyCombiningAlgEvaluator(Preconditions.checkNotNull(combinedElements), EffectType.DENY);
		}

		// combined elements are Rules, we can optimize
		// if no Rules -> NotApplicable
		if (combinedElements == null)
		{
			LOGGER.warn("{}: no rule to combine -> optimization: replacing with equivalent evaluator returning constant NotApplicable decision", this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
		if (!combinedEltIterator.hasNext())
		{
			// empty (no Rules)
			LOGGER.warn("{}: no rule to combine -> optimization: replacing with equivalent evaluator returning constant NotApplicable decision", this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		/*
		 * There is at least one Rule. Prepare to iterate over Rules, we will reorder rules with overriding Effect (e.g. Deny for deny-overrides algorithm) before rules with overridden Effect (e.g.
		 * Permit for deny-overrides algorithm) since order does not matter and deny decision prevails
		 */
		final Deque<RuleEvaluator> nonEmptyRulesWithOverridingEffect = new ArrayDeque<>();
		final Deque<RuleEvaluator> rulesWithOverriddenEffect = new ArrayDeque<>();

		/*
		 * 
		 * If we find any empty Rule in overridden Effect (no target/condition/pep_action), we don't need to look at other rules with such Effect; since such rule is enough to return the overridden
		 * Effect as decision, if there is no rule with overriding Effect (e.g. if algorithm is deny-overrides, then if there is no applicable Deny rule, if there is any empty Permit rule, the result
		 * is Permit)
		 */
		/*
		 * if atLeastOneEmptyPermitRule (resp. atLeastOneEmptyDenyRule) in case of deny-overrides (resp. permit-overrides) algorithm
		 */
		boolean atLeastOneEmptyRuleWithOverriddenEffect = false;

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
							"{}: {} with Effect={} is empty (no target/condition/pep_actions) => always returns {} => {} combining algorithm will always return {} => other combined rules have no effect => will be ignored/removed.",
							this, rule, this.overridingEffect, this.overridingEffect, this.getId(), this.overridingEffect);
					return evaluatorIfEmptyRuleWithOverridingEffect;
				}

				/*
				 * Rule is not empty, i.e. has a target/condition, therefore may not necessarily return its (overriding) Effect as decision
				 */
				nonEmptyRulesWithOverridingEffect.add(rule);
				continue;
			}

			/*
			 * Rule Effect = {overridden_Effect} (e.g. Permit if algorithm is deny-overrides)
			 * 
			 * Process rule with overridden Effect only if no empty Rule with such Effect has been found yet. Indeed, as mentioned earlier, if there is an empty rule with such overridden Effect, we
			 * already know that the result is always the overridden Effect (so no need to process other rules with such Effect), if there is no applicable rule with overridING Effect. Indeed, only
			 * rules with overriding Effect may change the final result in this case.
			 */
			if (atLeastOneEmptyRuleWithOverriddenEffect)
			{
				// ignore this new Rule with overridden Effect
				LOGGER.warn(
						"{}: Ignoring/removing {} (Effect={}) because it does not affect the result, which is only affected by empty Rule ({}) found previously (always returns {}), and {} rule(s).",
						this, rule, overriddenEffect, rulesWithOverriddenEffect, overriddenEffect, overridingEffect);
				// continue looking for rules with overriding Effect
				continue;
			}

			// No empty Rule with overridden Effect found yet; what about this one?
			if (rule.isEmptyEquivalent())
			{
				/*
				 * This is the first declared empty Rule with overridden Effect -> always returns the overridden Effect as decision; we can ignore/remove other Rules with overridden Effect (have no
				 * effect anymore)
				 */
				LOGGER.warn(
						"{}: {} (Effect={}) is empty (no target/condition/pep_actions) => always returns {} => {} combining algorithm will always return {} unless some {} rule applies => other combined {} rules have no effect => will be ignored/removed.",
						this, rule, overriddenEffect, overriddenEffect, this.getId(), overriddenEffect, overridingEffect, overriddenEffect);
				atLeastOneEmptyRuleWithOverriddenEffect = true;
				rulesWithOverriddenEffect.clear();
				rulesWithOverriddenEffect.addLast(rule);
				// continue looking for rules with overriding Effect
				continue;
			}

			/*
			 * Non-empty Rule with overridden Effect
			 */
			rulesWithOverriddenEffect.addLast(rule);
		}

		/*
		 * There is at least one rule and there is no empty Rule with overriding Effect
		 */
		if (nonEmptyRulesWithOverridingEffect.isEmpty())
		{
			/*
			 * no Rule with overriding Effect (whether empty or not) -> at least one Rule with overriding Effect and all rules are rules with overriding Effect
			 */
			if (atLeastOneEmptyRuleWithOverriddenEffect)
			{
				/*
				 * no Rule with overriding Effect but one empty rule with overridden Effect -> final result is the overridden Effect always
				 */
				LOGGER.warn(
						"{}: the only combined rule is empty {} Rule ({}) => {} combining algorithm will always return this {} => optimization: replacing with equivalent evaluator returning constant {} decision",
						this, this.overriddenEffect, rulesWithOverriddenEffect, this.getId(), this.overriddenEffect, this.overriddenEffect);
				return evaluatorIfEmptyRuleWithOverriddenEffectAndNoneWithOverridingOne;
			}

			/*
			 * At least one Rule with overridden Effect and all rules are non-empty rules with same (overridden) Effect
			 */
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(rulesWithOverriddenEffect);
		}

		// There is at least one non-empty rule with overriding Effect
		if (rulesWithOverriddenEffect.isEmpty())
		{
			/*
			 * No rule with overridden Effect -> only non-empty rules with same overriding Effect
			 */
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(nonEmptyRulesWithOverridingEffect);
		}

		// At least one Rule with overridden Effect and only non-empty rules with overriding Effect
		return new OverridingEffectFirstRuleCombiningAlgEvaluator(nonEmptyRulesWithOverridingEffect, rulesWithOverriddenEffect);
	}
}
