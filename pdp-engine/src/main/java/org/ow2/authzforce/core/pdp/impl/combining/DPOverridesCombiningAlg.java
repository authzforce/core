/*
 * Copyright 2012-2023 THALES.
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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.impl.combining.CombiningAlgEvaluators.RulesWithSameEffectEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Standard *-overrides combining algorithm.
 */
class DPOverridesCombiningAlg<T extends Decidable> extends BaseCombiningAlg<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DPOverridesCombiningAlg.class);

    private static final IndeterminateEvaluationException UNKNOWN_OVERRIDING_EFFECT_RULES_EVALUATION_ERROR = new IndeterminateEvaluationException("Unknown error while evaluating the Rules with the overriding Effect", XacmlStatusCode.PROCESSING_ERROR.value());

    private final EffectType overridingEffect;
    private final CombiningAlgEvaluators.ConstantDecisionEvaluator constantOverridingEffectDecisionEvaluator;
    protected final CombiningAlgEvaluators.ConstantDecisionEvaluator constantOverriddenEffectDecisionEvaluator;

    private final boolean isAnyIndeterminateIgnored;
    private final CombiningAlgEvaluators.ConstantDecisionEvaluator defaultCombiningAlgEvaluator;

    /**
     * Constructor
     *
     * @param algId            combining algorithm ID
     * @param overridingEffect overriding effect (e.g. Deny for deny-overrides algorithm)
     * @param ignoreIndeterminates ignore Indeterminate results, e.g. in *-unless* standard combining algorithm
     * @param returnOverriddenEffectAsDefaultDecision if true, overridden_effect returned as decision by default (e.g. for standard X-unless-Y algorithms, i.e. DPUnlessPDCombiningAlg), i.e. if all NotApplicable or Indeterminate and {@code ignoreIndeterminates}; else NotApplicable is the default.
     * //@param isOrdered        true iff combined elements must be evaluated in order of declaration, i.e. in same order as in 'combinedElements' argument of {@link #getInstance(Iterable, Iterable)}. If false, the
     * //                        order is changed, in particular optimized by evaluating rules with overriding Effect first.
     */
    protected DPOverridesCombiningAlg(final String algId, final Class<T> combinedType, final EffectType overridingEffect, final boolean ignoreIndeterminates, final boolean returnOverriddenEffectAsDefaultDecision)
    {
        super(algId, combinedType);
        this.overridingEffect = overridingEffect;
        if (overridingEffect == EffectType.DENY)
        {
            constantOverridingEffectDecisionEvaluator = CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
            constantOverriddenEffectDecisionEvaluator = CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
        } else
        {
            // Overriding Effect is Permit
            constantOverridingEffectDecisionEvaluator = CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
            constantOverriddenEffectDecisionEvaluator = CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
        }

        this.isAnyIndeterminateIgnored = ignoreIndeterminates;
        this.defaultCombiningAlgEvaluator = returnOverriddenEffectAsDefaultDecision? constantOverriddenEffectDecisionEvaluator: CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
    }

    /**
     * Constructor for *-overrides algorithms
     *
     * @param algId            combining algorithm ID
     * @param combinedType type of combined element (Rule, Policy, PolicySet)
     * @param overridingEffect overriding effect (e.g. Deny for deny-overrides algorithm)
     * //@param isOrdered        true iff combined elements must be evaluated in order of declaration, i.e. in same order as in 'combinedElements' argument of {@link #getInstance(Iterable, Iterable)}. If false, the
     * //                        order is changed, in particular optimized by evaluating rules with overriding Effect first.
     */
    DPOverridesCombiningAlg(final String algId, final Class<T> combinedType, final EffectType overridingEffect/*, final boolean isOrdered*/)
    {
        this(algId, combinedType, overridingEffect, false, false);
    }

    /**
     * Provides the proper rule combiner for combining {overridden_effect} rules preceded by {overriding_effect} rules or not.
     * This is an interface so that the subclass {@link DPUnlessPDCombiningAlg} can override this as it differs in the way this rule combiner works.
     */
    protected interface OverriddenEffectRuleCombiningHelper {

        /**
         *
         * @return evaluator to be called
         */
        CombiningAlg.Evaluator getOverriddenEffectRuleCombiningAlgEvaluator();

        ExtendedDecision evaluateAfterOverridingEffectRules(ExtendedDecision extDecisionFromOverridingEffectRules, EvaluationContext context, Optional<EvaluationContext> mdpContext, UpdatableList<PepAction> updatablePepActions);
    }

    protected OverriddenEffectRuleCombiningHelper getOverriddenEffectRulesCombiningAlgEvaluator(boolean atLeastOneEmptyRuleWithOverriddenEffect, List<RuleEvaluator> nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction, List<RuleEvaluator> nonEmptyRulesWithOverriddenEffectAndNoPepAction)
    {
        final CombiningAlg.Evaluator overriddenEffectRuleCombiner;
        if (atLeastOneEmptyRuleWithOverriddenEffect)
        {
            /*
        If atLeastOneEmptyRuleWithOverriddenEffect, the decision is fixed in advance to be the overridden effect in this sub-combiner, so we only need the rules with PEP actions and same effect, to combine them in the result.
         */
            if (nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction.isEmpty())
            {
                /*
                Constant decision (= overridden_effect) and no PEP actions
                 */
                overriddenEffectRuleCombiner = constantOverriddenEffectDecisionEvaluator;
            } else
            {
                /*
                Constructor arg 'returnOnFirstApplicable' set to false because in the XACML spec pseudo-code of standard x-overrides (and x-unless-y) algorithms, as long as no rule evaluates to the overriding_effect, we continue to evaluate the rules (and we collect PEP actions if any).
                 */
                overriddenEffectRuleCombiner = new RulesWithSameEffectEvaluator(nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction, false);
            }
        } else
        {
            /*
            There is no empty {overridden_effect} rule

            Constructor arg 'returnOnFirstApplicable' set to false because in the XACML spec pseudo-code of standard x-overrides (and x-unless-y) algorithms, as long as no rule evaluates to the overriding_effect, we continue to evaluate the rules (and we collect PEP actions if any).
             */
            if (nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction.isEmpty() && nonEmptyRulesWithOverriddenEffectAndNoPepAction.isEmpty())
            {
			/*
			There is no {overridden_effect} rule.
			 */
                overriddenEffectRuleCombiner = CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
            } else
            {
                overriddenEffectRuleCombiner = new RulesWithSameEffectEvaluator(nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction, nonEmptyRulesWithOverriddenEffectAndNoPepAction, false);
            }
        }

        return new OverriddenEffectRuleCombiningHelper()
        {
            @Override
            public CombiningAlg.Evaluator getOverriddenEffectRuleCombiningAlgEvaluator()
            {
                return overriddenEffectRuleCombiner;
            }

            @Override
            public ExtendedDecision evaluateAfterOverridingEffectRules(ExtendedDecision extDecisionFromOverridingEffectRules, EvaluationContext context, Optional<EvaluationContext> mdpContext, UpdatableList<PepAction> updatablePepActions)
            {
                final ExtendedDecision extDecisionFromRulesWithOverriddenEffect = overriddenEffectRuleCombiner.evaluate(context, mdpContext, updatablePepActions, null);
                final DecisionType decisionFromRulesWithOverriddenEffect = extDecisionFromRulesWithOverriddenEffect.getDecision();

                // if atLeastOneError{overriding_effect} (XACML spec)
                if (extDecisionFromOverridingEffectRules.getDecision() == DecisionType.INDETERMINATE)
                {
				/*
				 If atLeastOneError{overridden_effect} || atLeastOne{overridden_effect} (which is equivalent to decisionFromRulesWithOverriddenEffect = Indeterminate (| Deny) | Permit; which is equivalent to decisionFromRulesWithOverriddenEffect != NotApplicable)
				 */
                    if (decisionFromRulesWithOverriddenEffect != DecisionType.NOT_APPLICABLE)
                    {
                        // return Indeterminate{DP}
                        return ExtendedDecisions.newIndeterminate(null, extDecisionFromOverridingEffectRules.getCauseForIndeterminate().orElse(UNKNOWN_OVERRIDING_EFFECT_RULES_EVALUATION_ERROR));
                    }

                    return extDecisionFromOverridingEffectRules;
                }

			/*
			The rest of the algorithm in the XACML spec's pseudo-code ( If atLeastOne{overridden_effect} | If atLeastOneError{overridden_effect} | etc. ) is equivalent to return extDecisionFromRulesWithOverriddenEffect as the result
			 */
                return extDecisionFromRulesWithOverriddenEffect;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends T>> params, final Iterable<? extends T> combinedElements)
            throws UnsupportedOperationException, IllegalArgumentException
    {
        // if no element -> NotApplicable
        if (combinedElements == null)
        {
            LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant {} decision", this, defaultCombiningAlgEvaluator.getReturnedDecision().getDecision());
            return defaultCombiningAlgEvaluator;
        }

        final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
        if (!combinedEltIterator.hasNext())
        {
            // empty (no element to combine)
            LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant {} decision", this, defaultCombiningAlgEvaluator.getReturnedDecision().getDecision());
            return defaultCombiningAlgEvaluator;
        }

        // There is at least one combined element (Type of combined element(s) is either Rule or Policy/PolicySet)
        // If combined elements are not Rules...
        if (!RuleEvaluator.class.isAssignableFrom(getCombinedElementType()))
        {
            return new OrderPreservingCombiningAlgEvaluator(combinedElements, this.overridingEffect, this.isAnyIndeterminateIgnored, defaultCombiningAlgEvaluator.getReturnedDecision());
        }

        /*
         * Else combined elements are Rule(s) and there is at least one Rule, we can optimize the combining algorithm:
         * ("empty rule" here means it has no Target/Condition/PEP action (obligation/advice) )
         * ("overriding_effect" is Permit in permit-overrides algorithm, or Deny in deny-overrides)
         * ("overridden_effect" is the opposite Effect to the "overriding effect", e.g. Deny in permit-overrides)
         * (PEP actions = Obligations/Advice)
         *
         * 1) If there is at least one empty rule with overriding effect, the combining algorithm shall always return overriding_effect as decision and the PEP actions from all applicable non-empty rules that return the same decision and have at least one PEP action, if any;
         *
         * 2) If there is no rule with overriding effect, i.e. only rules with overridden effect (and at least one as already checked), and if at least one empty rule, then the combining algorithm shall always return overridden_effect as decision and the PEP actions from all applicable non-empty rules that have the same effect and at least one PEP action, if any.
         *
         * In other cases, as a general optimization, we can group rules together in the following lists (order preserved):
         * 1) Non-empty overriding_effect rules with at least one PEP action (Obligation/Advice): the combining algorithm will evaluated these first; if any one is applicable, return the overriding_effect as decision with PEP actions collected from this list. The other lists below are ignored.
         *
         * 2) Non-empty overriding_effect rules without PEP action: the algorithm evaluates these if nothing applicable in the previous list; if any one is applicable, return overriding_effect as decision right away (without PEP action), and therefore ignore the other lists below.
         *
         * 3) Non-empty overridden_effect rules with at least one PEP action (Obligation/Advice): the algo evaluates these if nothing applicable in previous lists; if any one is applicable, return overridden_effect as decision with PEP actions collected from this list. The list below is ignored.
         *
         * 4) Non-empty overridden_effect rules without PEP action: the algorithm evaluates these if nothing applicable in the previous list; if any one is applicable, return overridden_effect as decision right away (without PEP action).
         *
         * NB: as a result, the order of evaluation of the rules differs from the order of declaration in the policy, but not the order in which the PEP actions are returned in the end, which is the only thing that matters in the final result for the client (PEP).
         */
        boolean atLeastOneEmptyRuleWithOverridingEffect = false;
        final List<RuleEvaluator> nonEmptyRulesWithOverridingEffectAndAtLeastOnePepAction = new ArrayList<>();
        final List<RuleEvaluator> nonEmptyRulesWithOverridingEffectAndNoPepAction = new ArrayList<>();

        boolean atLeastOneEmptyRuleWithOverriddenEffect = false;
        final List<RuleEvaluator> nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction = new ArrayList<>();
        final List<RuleEvaluator> nonEmptyRulesWithOverriddenEffectAndNoPepAction = new ArrayList<>();

        while (combinedEltIterator.hasNext())
        {
            final RuleEvaluator rule = (RuleEvaluator) combinedEltIterator.next();
            if (rule.getEffect() == overridingEffect)
            {
                if (rule.isEmptyEquivalent())
                {
                    LOGGER.warn(
                            "{}: {} with Effect={} is empty (no target/condition/pep_actions) => always returns {} => rule combining algorithm will always return {} with PEP actions (Obligations/Advice) from other rules with same effect if any",
                            this, rule, this.overridingEffect, this.overridingEffect, this.overridingEffect);
                    atLeastOneEmptyRuleWithOverridingEffect = true;
                } else if (rule.hasAnyPepAction())
                {
                    nonEmptyRulesWithOverridingEffectAndAtLeastOnePepAction.add(rule);
                } else
                {
                    nonEmptyRulesWithOverridingEffectAndNoPepAction.add(rule);
                }
            } else
            {
                // overridden_effect Rule
                if (rule.isEmptyEquivalent())
                {
                    atLeastOneEmptyRuleWithOverriddenEffect = true;
                } else if (rule.hasAnyPepAction())
                {
                    nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction.add(rule);
                } else
                {
                    nonEmptyRulesWithOverriddenEffectAndNoPepAction.add(rule);
                }
            }
        }

        if (atLeastOneEmptyRuleWithOverridingEffect)
        {
            if (nonEmptyRulesWithOverridingEffectAndAtLeastOnePepAction.isEmpty())
            {
                /*
                Constant decision (= overriding_effect) and no PEP actions
                 */
                LOGGER.info(
                        "{}: the only combined rule(s) is/are {} Rule(s) without PEP action => algorithm will always return {} => optimization: replacing with equivalent evaluator returning constant {} decision",
                        this, this.overridingEffect, this.overridingEffect, this.overridingEffect);
                return constantOverridingEffectDecisionEvaluator;
            }

            // There is at least one PEP action possibly returned
            return new RulesWithSameEffectEvaluator(nonEmptyRulesWithOverridingEffectAndAtLeastOnePepAction, true);
        }

  		/*
		 Create the sub-combining-algorithm that will combine only the {overridden_effect} rules and is called only after the {overriding_effect} rules and only if none of them returned the Effect (applicable).
		 */
        final OverriddenEffectRuleCombiningHelper overriddenEffectRulesCombiner = getOverriddenEffectRulesCombiningAlgEvaluator(atLeastOneEmptyRuleWithOverriddenEffect, nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction, nonEmptyRulesWithOverriddenEffectAndNoPepAction);

        // No empty {overriding_effect} rule
        if (nonEmptyRulesWithOverridingEffectAndAtLeastOnePepAction.isEmpty() && nonEmptyRulesWithOverridingEffectAndNoPepAction.isEmpty())
        {
			/*
			There is no {overriding_effect} rule, i.e. there are only overridden_effect rules (at least one) (we already checked that there is at least one rule).
			 */
            return overriddenEffectRulesCombiner.getOverriddenEffectRuleCombiningAlgEvaluator();
        }

        // Else there is at least one non-empty {overriding_effect} rule
        final RulesWithSameEffectEvaluator overridingEffectRulesCombiner = new RulesWithSameEffectEvaluator(nonEmptyRulesWithOverridingEffectAndAtLeastOnePepAction, nonEmptyRulesWithOverridingEffectAndNoPepAction, true);
        return new OverridingEffectFirstRuleCombiningAlgEvaluator(overridingEffect, overridingEffectRulesCombiner, overriddenEffectRulesCombiner);
    }

    /**
     * "Ordered" Deny/Permit-overrides policy combining algorithm evaluator; "ordered" means combined elements are evaluated in the same order as in the input collection (constructor parameter
     * 'combinedElements'), i.e. no re-ordering.
     */
    private static final class OrderPreservingCombiningAlgEvaluator extends BaseCombiningAlg.Evaluator<Decidable>
    {
        private static final SubDecisionHandler OVERRIDING_SUBDECISIONHANDLER = (result, resultCollector) -> true;
        private static final SubDecisionHandler OVERRIDDEN_DP_SUBDECISIONHANDLER = (result, resultCollector) ->
        {
            resultCollector.addSubResultDP(result);
            return false;
        };
        private static final SubDecisionHandler NOT_APPLICABLE_SUBDECISIONHANDLER = (result, resultCollector) -> false;
        private static final SubDecisionHandler INDETERMINATE_OVERRIDING_EFFECT_SUBDECISIONHANDLER = (result, resultCollector) ->
        {
            resultCollector.addSubResultIndeterminateInOverridingEffect(result);
            return false;
        };
        private static final SubDecisionHandler INDETERMINATE_OVERRIDDEN_EFFECT_SUBDECISIONHANDLER = (result, resultCollector) ->
        {
            resultCollector.addSubResultIndeterminateInOverriddenEffect(result);
            return false;
        };
        private static final SubDecisionHandler INDETERMINATE_DP_SUBDECISIONHANDLER = (result, resultCollector) ->
        {
            resultCollector.addSubResultIndeterminateDP(result);
            return false;
        };
        private final ExtendedDecision decisionForOverridingEffect;
        private final ExtendedDecision decisionForOverriddenEffect;

        private final ExtendedDecision defaultDecision;

        private final Map<DecisionType, SubDecisionHandler> resultHandlersByDecisionType = new EnumMap<>(DecisionType.class);

        private final DecisionResultCollectorFactory resultCollectorFactory;

        /**
         *
         * @param combinedElements combined elements
         * @param overridingEffect overriding Effect, e.g. Permit in Permit-overrides
         * @param ignoreIndeterminates ignore Indeterminate decisions like in standard *-unless-* combining algorithms
         * @param defaultDecision default decision always returned if all NotApplicable or Indeterminate and Indeterminate decisions are ignored.
         */
        OrderPreservingCombiningAlgEvaluator(final Iterable<? extends Decidable> combinedElements, final EffectType overridingEffect, boolean ignoreIndeterminates, ExtendedDecision defaultDecision)
        {
            super(combinedElements);
            assert overridingEffect != null && defaultDecision != null;
            this.resultHandlersByDecisionType.put(DecisionType.NOT_APPLICABLE, NOT_APPLICABLE_SUBDECISIONHANDLER);
            final Map<DecisionType, SubDecisionHandler> indeterminateResultHandlersByExtendedIndeterminateType = new EnumMap<>(DecisionType.class);
            indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.INDETERMINATE, INDETERMINATE_DP_SUBDECISIONHANDLER);
            if (overridingEffect == EffectType.DENY)
            {
                // deny-overrides
                this.decisionForOverridingEffect = ExtendedDecisions.SIMPLE_DENY;
                this.decisionForOverriddenEffect = ExtendedDecisions.SIMPLE_PERMIT;
                this.resultHandlersByDecisionType.put(DecisionType.DENY, OVERRIDING_SUBDECISIONHANDLER);
                indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.DENY, INDETERMINATE_OVERRIDING_EFFECT_SUBDECISIONHANDLER);
                this.resultHandlersByDecisionType.put(DecisionType.PERMIT, OVERRIDDEN_DP_SUBDECISIONHANDLER);
                indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.PERMIT, INDETERMINATE_OVERRIDDEN_EFFECT_SUBDECISIONHANDLER);
            } else
            {
                this.decisionForOverridingEffect = ExtendedDecisions.SIMPLE_PERMIT;
                this.decisionForOverriddenEffect = ExtendedDecisions.SIMPLE_DENY;
                this.resultHandlersByDecisionType.put(DecisionType.PERMIT, OVERRIDING_SUBDECISIONHANDLER);
                indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.PERMIT, INDETERMINATE_OVERRIDING_EFFECT_SUBDECISIONHANDLER);
                this.resultHandlersByDecisionType.put(DecisionType.DENY, OVERRIDDEN_DP_SUBDECISIONHANDLER);
                indeterminateResultHandlersByExtendedIndeterminateType.put(DecisionType.DENY, INDETERMINATE_OVERRIDDEN_EFFECT_SUBDECISIONHANDLER);
            }

            this.defaultDecision = defaultDecision;
            if(ignoreIndeterminates) {
                // The handler for Indeterminate decision is like the NotApplicable one, does nothing except return false.
                this.resultHandlersByDecisionType.put(DecisionType.INDETERMINATE, NOT_APPLICABLE_SUBDECISIONHANDLER);
                this.resultCollectorFactory = DECISION_RESULT_COLLECTOR_FACTORY_IGNORING_INDETERMINATES;
            } else {
                this.resultHandlersByDecisionType.put(DecisionType.INDETERMINATE, new IndeterminateSubDecisionHandler(indeterminateResultHandlersByExtendedIndeterminateType));
                this.resultCollectorFactory = DECISION_RESULT_COLLECTOR_FACTORY_PRESERVING_INDETERMINATES;
            }
        }

        @Override
        public ExtendedDecision evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final UpdatableList<PepAction> outPepActions, final UpdatableList<PrimaryPolicyMetadata> outApplicablePolicyIdList)
        {
            assert outPepActions != null;

			/*
			Combining algorithm evaluation. See for example: XACML core §C.2 Deny-overrides
			 */
            final DecisionResultCollector resultCollector = this.resultCollectorFactory.newInstance(outApplicablePolicyIdList != null);

            for (final Decidable combinedElement : getCombinedElements())
            {
                // evaluate the policy
                final DecisionResult result = combinedElement.evaluate(context, mdpContext);
                final SubDecisionHandler resultHandler = resultHandlersByDecisionType.get(result.getDecision());
                final boolean isTheResultDecisionOverriding = resultHandler.handle(result, resultCollector);

                /*
                 * XACML §7.18: Obligations & Advice: do not return Obligations/Advice of the rule, policy, or policy set that does not match the decision resulting from evaluating the enclosing
                 * policy set. For example, if the final decision is Permit, we should add to outPepActions only the PEP actions from Permit decisions (permitPepActions)
                 */
                if (isTheResultDecisionOverriding)
                {
                    /*
                     * result decision overrides all others, return it right away after updating output applicable policies and PEP actions
                     */
                    if (outApplicablePolicyIdList != null)
                    {
                        outApplicablePolicyIdList.addAll(resultCollector.getApplicablePolicies(result));
                    }

                    outPepActions.addAll(result.getPepActions());
                    /*
                     Collect PEP actions from the current element only. Section 7.8 says: "no obligations or advice SHALL be returned to the PEP if the rule, policies, or policy sets from which they are drawn are not evaluated".
                     */
                    return this.decisionForOverridingEffect;
                }

                // The result decision is not the one that overrides, we go on
            }

            /*
             * There was no overriding Deny/Permit decision, i.e. no Deny (resp. no Permit) in case of deny-overrides (resp. permit-overrides) alg, else: if any Indeterminate{DP}, then Indeterminate{DP}
             *
             * From XACML x-overrides alg's pseudo-core (e.g. Annex C.2 Deny-overrides):
             * if(atLeastOneErrorDP) return Indeterminate{DP};
             */
            // In case of DPUnlessPDCombiningAlg, firstIndeterminateDP set null (Indeterminates are ignored)
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
            final UpdatableList<PepAction> combinedPepActionsAppliedToOverriddenEffect = resultCollector.getPepActions();
            // combinedPepActionsAppliedTo${overridden_effect} != null if at least one Deny/Permit result in resultCollector (see Javadoc of resultCollector.getPepActions() ), i.e. if at least one {overridden_effect} result (because it cannot be the {overridding_effect} at this stage anymore)

            // In case of DPUnlessPDCombiningAlg, firstIndeterminateWithOverridingEffect and firstIndeterminateWithOverriddenEffect set null (Indeterminates are ignored).
            final ExtendedDecision firstIndeterminateWithOverridingEffect = resultCollector.getFirstIndeterminateWithOverridingEffect();
            final ExtendedDecision firstIndeterminateWithOverriddenEffect = resultCollector.getFirstIndeterminateWithOverriddenEffect();

            /*
             * ${overriding_effect} = Deny (resp. Permit) and ${overridden_effect} = Permit (resp. Deny) in case of deny-overrides (resp. permit-overrides) algorithm.
             */

            // In case of DPUnlessPDCombiningAlg, since firstIndeterminateWithOverridingEffect = null, skip.
            /*
             * The spec (pseudo-code) says:
             * - if atLeastOneError{overriding_effect} && (atLeastOneError{overridden_effect} || atLeastOne{overridden_effect}), return Indeterminate{DP};
             * - if atLeastOneError{overriding_effect} return Indeterminate{overriding_effect};
             *
             * Can be re-written as follows:
             * if atLeastOneError{overriding_effect}
             * {
             *  indeterminate_type = atLeastOneError{overridden_effect} || atLeastOne{overridden_effect} ? {DP}: {overriding_effect};
             *  return Indeterminate{indeterminate_type};
             * }
             */
            // if atLeastOneError{overriding_effect}
            if (firstIndeterminateWithOverridingEffect != null)
            {
                // atLeastOneErrorD
                if (outApplicablePolicyIdList != null)
                {
                    outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
                }

                // extended_indeterminate_type = atLeastOneError{overridden_effect} || atLeastOne{overridden_effect} ? {DP}: {overriding_effect};
                // atLeastOne${overridden_effect} == true <=> combinedPepActionsAppliedTo${overridden_effect} != null (see Javadoc of resultCollector.getPepActions() )
                final DecisionType extIndeterminateType = firstIndeterminateWithOverriddenEffect != null || combinedPepActionsAppliedToOverriddenEffect != null ? DecisionType.INDETERMINATE /* DP */ : decisionForOverridingEffect.getDecision();
                return ExtendedDecisions.newIndeterminate(extIndeterminateType,
                        firstIndeterminateWithOverridingEffect.getCauseForIndeterminate().orElse(new IndeterminateEvaluationException("Unknown cause for " + firstIndeterminateWithOverridingEffect + " in deny/permit-overrides combining algorithm", XacmlStatusCode.PROCESSING_ERROR.value())));
            }

            /*
             * The rest of the spec (pseudo-code) says:
             * if atLeastOne{overridden_effect} return {overriden_effect};
             * if atLeastOneError{overridden_effect} return Indeterminate{overriden_effect};
             *
             * If we got a decision with overridden effect (e.g. Permit in case of deny-overrides algo) or Indeterminate{overridden_effect}, return it, otherwise it's NOT_APPLICABLE
             */
            if (combinedPepActionsAppliedToOverriddenEffect != null)
            {
                if (outApplicablePolicyIdList != null)
                {
                    outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
                }

                outPepActions.addAll(combinedPepActionsAppliedToOverriddenEffect);
                return decisionForOverriddenEffect;
            }

            // In case of DPUnlessPDCombiningAlg, since firstIndeterminateWithOverriddenEffect = null, skip.
            if (firstIndeterminateWithOverriddenEffect != null)
            {
                if (outApplicablePolicyIdList != null)
                {
                    outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
                }

                return firstIndeterminateWithOverriddenEffect;
            }

            return defaultDecision;
        }

        private interface SubDecisionHandler
        {
            /**
             * Handles the decision result of one of the combined elements, in particular, adds the result to a collection of previous results for final processing, if the decision does not overrides.
             *
             * @param result          decision result of a combined element to handle
             * @param resultCollector used to collect the result (added/combined with previous ones), if the decision does not override, to be reused later at the end of the evaluation
             * @return true iff the result overrides all others (is final), e.g. Deny for deny-overrides
             */
            boolean handle(DecisionResult result, DecisionResultCollector resultCollector);
        }

        /**
         * Helper to combine (not-overriding) decision results until a decision overrides or there is no more element to combine
         * Indeterminate results may be handled differently, e.g. ignored in the case of *-unless-* standard algorithms. See the subclasses for the different modes.
         *
         * Mutable / not thread-safe.
         */
        private static abstract class DecisionResultCollector {
            protected final UpdatableList<PrimaryPolicyMetadata> combinedApplicablePolicyIdList;

            /**
             * Replaces atLeastOnePermit (resp. atLeastOneDeny) from description of permit-overrides (resp. deny-overrides) in the XACML spec.
             * <p>
             * atLeastOnePermit (resp. atLeastOneDeny) == false <=> combinedPepActions == null.
             * <p>
             * At this point, we don't know yet whether the PEP actions of combined/children's Permit/Deny decisions will be added to the final result's PEP actions, since we don't know yet whether
             * the final decision is Permit/Deny.
             */
            private UpdatableList<PepAction> combinedPepActions = null;

            DecisionResultCollector(final boolean returnApplicablePolicyIdList)
            {
                /*
                 * Since we may combine multiple elements before returning a final decision, we have to collect them in a list; and since we don't know yet whether the final decision is NotApplicable,
                 * we cannot add collected applicable policies straight to outApplicablePolicyIdList. So we create a temporary list until we know the final decision applies.
                 */
                combinedApplicablePolicyIdList = returnApplicablePolicyIdList ? UpdatableCollections.newUpdatableList()
                        : UpdatableCollections.emptyList();
            }

            /**
             * Return new result's applicable policies combined (added last) with the ones previously found, or only the ones combined so far if result == null
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
                    combinedPepActions = UpdatableCollections.newUpdatableList();
                }

                combinedPepActions.addAll(result.getPepActions());
            }

            /**
             * Get combined PEP actions of intermediate results, null if no Deny/Permit result collected, i.e. non-null of at least one DP result (may be empty if no PEP action part of the DP results)
             */
            UpdatableList<PepAction> getPepActions()
            {
                return combinedPepActions;
            }

            /**
             * Add intermediate (not final a priori) Indeterminate${overriding_effect} result (update applicable policies, etc.). May be ignored in some cases, e.g. *-unless-* algorithms.
             */
            abstract void addSubResultIndeterminateInOverridingEffect(final DecisionResult result);

            /**
             * Add intermediate (not final a priori) Indeterminate${overridden_effect} result (update applicable policies, etc.). May be ignored in some cases, e.g. *-unless-* algorithms.
             */
            abstract void addSubResultIndeterminateInOverriddenEffect(final DecisionResult result);

            /**
             * Add intermediate (not final a priori) IndeterminateDP result (update applicable policies, etc.). May be ignored in some cases, e.g. *-unless-* algorithms.
             */
            abstract void addSubResultIndeterminateDP(final DecisionResult result);

            /**
             * Get any occurred IndeterminateDP result. May be ignored in some cases, e.g. *-unless-* algorithms.
             */
            abstract ExtendedDecision getFirstIndeterminateDP();

            /**
             * Get any occurred Indeterminate${overriding_effect} result. May be ignored in some cases, e.g. *-unless-* algorithms.
             */
            abstract ExtendedDecision getFirstIndeterminateWithOverridingEffect();

            /**
             * Get any occurred Indeterminate${overridden_effect} result. May be ignored in some cases, e.g. *-unless-* algorithms.
             */
            abstract ExtendedDecision getFirstIndeterminateWithOverriddenEffect();
        }

        private interface DecisionResultCollectorFactory {
            DecisionResultCollector newInstance(boolean returnApplicablePolicyIdList);
        }

        private static final DecisionResultCollectorFactory DECISION_RESULT_COLLECTOR_FACTORY_IGNORING_INDETERMINATES = DecisionResultCollectorIgnoringIndeterminates::new;

        private static final DecisionResultCollectorFactory DECISION_RESULT_COLLECTOR_FACTORY_PRESERVING_INDETERMINATES = DecisionResultCollectorPreservingIndeterminates::new;

        /*
        DecisionResultCollector Ignoring Indeterminate results, e.g. for standard *-unless-* algorithms where Indeterminate results are ignored.
         */
        private static final class DecisionResultCollectorIgnoringIndeterminates extends DecisionResultCollector {

            DecisionResultCollectorIgnoringIndeterminates(boolean returnApplicablePolicyIdList)
            {
                super(returnApplicablePolicyIdList);
            }

            @Override
            void addSubResultIndeterminateInOverridingEffect(DecisionResult result)
            {
                // ignore
            }

            @Override
            void addSubResultIndeterminateInOverriddenEffect(DecisionResult result)
            {
                // ignore
            }

            @Override
            void addSubResultIndeterminateDP(DecisionResult result)
            {
                // ignore
            }

            /**
             * Get any occurred IndeterminateDP result
             */
            ExtendedDecision getFirstIndeterminateDP()
            {
                return null;
            }

            /**
             * Get any occurred Indeterminate${overriding_effect} result
             */
            ExtendedDecision getFirstIndeterminateWithOverridingEffect()
            {
                return null;
            }

            /**
             * Get any occurred Indeterminate${overridden_effect} result
             */
            ExtendedDecision getFirstIndeterminateWithOverriddenEffect()
            {
                return null;
            }
        }

        /*
        Opposite to DecisionResultCollectorIgnoringIndeterminates, e.g. for standard *-overrides algorithms where Indeterminate results may change the final combined result if no {overriding_effect} was returned by any combined element.
         */
        private static final class DecisionResultCollectorPreservingIndeterminates extends DecisionResultCollector
        {

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

            DecisionResultCollectorPreservingIndeterminates(boolean returnApplicablePolicyIdList)
            {
                super(returnApplicablePolicyIdList);
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
        }

        private record IndeterminateSubDecisionHandler(
                Map<DecisionType, SubDecisionHandler> indeterminateResultHandlersByExtendedIndeterminateType) implements SubDecisionHandler
        {

            @Override
            public boolean handle(final DecisionResult result, final DecisionResultCollector resultCollector)
            {
                final DecisionType extIndeterminate = result.getExtendedIndeterminate();
                assert extIndeterminate != null && extIndeterminate != DecisionType.NOT_APPLICABLE;
                return this.indeterminateResultHandlersByExtendedIndeterminateType.get(extIndeterminate).handle(result, resultCollector);
            }
        }
    }

    /**
     * Combining algorithm evaluator that evaluates rules with overriding Effect first, then rules with overridden effect
     * rules (with overridden effect)
     */
    protected static class OverridingEffectFirstRuleCombiningAlgEvaluator implements CombiningAlg.Evaluator
    {
        private final RulesWithSameEffectEvaluator overridingEffectRulesCombiner;
        private final OverriddenEffectRuleCombiningHelper overriddenEffectRulesCombiner;

        private final DecisionType overridingDecision;

        /**
         * Constructor
         *
         * @param overridingEffect              overriding Effect
         * @param overridingEffectRulesCombiner Evaluator combining Rules with overriding Effect. Must be non-null.
         * @param overriddenEffectRulesCombiner Evaluator combining Rules with the opposite/overridden Effect. Must be non-null.
         */
         OverridingEffectFirstRuleCombiningAlgEvaluator(final EffectType overridingEffect, final RulesWithSameEffectEvaluator overridingEffectRulesCombiner, final OverriddenEffectRuleCombiningHelper overriddenEffectRulesCombiner)
        {
            assert overridingEffect != null && overridingEffectRulesCombiner != null && overriddenEffectRulesCombiner != null;
            this.overridingDecision = overridingEffect == EffectType.DENY ? DecisionType.DENY : DecisionType.PERMIT;
            this.overridingEffectRulesCombiner = overridingEffectRulesCombiner;
            this.overriddenEffectRulesCombiner = overriddenEffectRulesCombiner;
        }

        @Override
        public ExtendedDecision evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final UpdatableList<PepAction> updatablePepActions,
                                         final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
        {
            final ExtendedDecision extDecisionFromRulesWithOverridingEffect = overridingEffectRulesCombiner.evaluate(context, mdpContext, updatablePepActions, updatableApplicablePolicyIdList);
            final DecisionType decisionFromRulesWithOverridingEffect = extDecisionFromRulesWithOverridingEffect.getDecision();
            if (decisionFromRulesWithOverridingEffect == overridingDecision)
            {
				/*
				Overriding effect returned as decision
				 */
                return extDecisionFromRulesWithOverridingEffect;
            }

            // Decision is NotApplicable or Indeterminate. Final decision depends on overriddenEffectRulesCombiner result
            return overriddenEffectRulesCombiner.evaluateAfterOverridingEffectRules(extDecisionFromRulesWithOverridingEffect, context, mdpContext, updatablePepActions);
        }
    }
}
