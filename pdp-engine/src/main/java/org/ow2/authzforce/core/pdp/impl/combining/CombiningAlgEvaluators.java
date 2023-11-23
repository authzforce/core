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

import com.google.common.collect.ImmutableList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Common Combining Algorithm evaluators
 */
final class CombiningAlgEvaluators
{
    static final ConstantDecisionEvaluator NOT_APPLICABLE_CONSTANT_EVALUATOR = new ConstantDecisionEvaluator()
    {

        @Override
        public ExtendedDecision getReturnedDecision()
        {
            return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
        }
    };
    static final ConstantDecisionEvaluator DENY_CONSTANT_EVALUATOR = new ConstantDecisionEvaluator()
    {

        @Override
        public ExtendedDecision getReturnedDecision()
        {
            return ExtendedDecisions.SIMPLE_DENY;
        }
    };
    static final ConstantDecisionEvaluator PERMIT_CONSTANT_EVALUATOR = new ConstantDecisionEvaluator()
    {

        @Override
        public ExtendedDecision getReturnedDecision()
        {
            return ExtendedDecisions.SIMPLE_PERMIT;
        }
    };

    private CombiningAlgEvaluators()
    {
    }

    /**
     * Evaluator that returns always the same decision (fixed)
     */
    public static abstract class ConstantDecisionEvaluator implements CombiningAlg.Evaluator
    {

        private static final Logger LOGGER = LoggerFactory.getLogger(ConstantDecisionEvaluator.class);

        protected abstract ExtendedDecision getReturnedDecision();

        @Override
        public final ExtendedDecision evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final UpdatableList<PepAction> updatablePepActions,
                                               final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
        {
            LOGGER.debug(
                    "This evaluator constantly returns the same decision, which results from an optimization of the combining algorithm and combined elements (if any) initially defined in the policy. Check the policy initialization logs for more information on this optimization.");
            return getReturnedDecision();
        }
    }

    /*
     * Rule combining algorithm evaluator where all rules must have the same Effect, and that works in 2 modes:
     *
     * 1) The decision is fixed in advance, i.e. the common effect is always returned as decision and we only collect the PEP actions (Obligations/Advice) from the rules to put in the Result.
     *
     * 2) The decision is not fixed: the evaluator returns the common_effect (Permit/Deny) as decision if at least one is applicable (and returns the Effect); else NotApplicable, unless one returned Indeterminate, in which case the evaluator returns the first Indeterminate result in the evaluation.
     *
     * This combinging algorithm evaluator is meant to be used by XACML combining algorithms where one Effect has priority / overrides the other, e.g. the standard x-overrides and x-unless-y combining algorithms.
     */
    static class RulesWithSameEffectEvaluator implements CombiningAlg.Evaluator
    {
        private final ImmutableList<RuleEvaluator> rulesWithSameEffectAndPepActions;
        private final ImmutableList<RuleEvaluator> rulesWithSameEffectAndNoPepAction;
        private final DecisionType commonEffectAsDecision;
        private final ExtendedDecision commonEffectAsExtDecision;
        private final boolean isDecisionReturnedOnFirstApplicableRule;
        private final ExtendedDecision presetFinalDecision;

        private RulesWithSameEffectEvaluator(final Collection<? extends RuleEvaluator> rulesWithSameEffectAndPepActions, final Collection<? extends RuleEvaluator> rulesWithSameEffectAndNoPepAction, final boolean isFinalDecisionFixedInAdvance, final boolean returnOnFirstApplicable)
        {
            assert rulesWithSameEffectAndPepActions != null && rulesWithSameEffectAndNoPepAction != null && (!rulesWithSameEffectAndPepActions.isEmpty() || !rulesWithSameEffectAndNoPepAction.isEmpty());
            // first rule's effect assumed the same for all
            final EffectType commonEffect = rulesWithSameEffectAndPepActions.isEmpty() ? rulesWithSameEffectAndNoPepAction.iterator().next().getEffect() : rulesWithSameEffectAndPepActions.iterator().next().getEffect();
            assert haveSameEffect(commonEffect, rulesWithSameEffectAndPepActions);
            assert haveSameEffect(commonEffect, rulesWithSameEffectAndNoPepAction);
            if (commonEffect == EffectType.DENY)
            {
                this.commonEffectAsDecision = DecisionType.DENY;
                this.commonEffectAsExtDecision = ExtendedDecisions.SIMPLE_DENY;
            } else
            {
                this.commonEffectAsDecision = DecisionType.PERMIT;
                this.commonEffectAsExtDecision = ExtendedDecisions.SIMPLE_PERMIT;
            }
            this.presetFinalDecision = isFinalDecisionFixedInAdvance? commonEffectAsExtDecision: null;
            this.rulesWithSameEffectAndPepActions = ImmutableList.copyOf(rulesWithSameEffectAndPepActions);
            this.rulesWithSameEffectAndNoPepAction = ImmutableList.copyOf(rulesWithSameEffectAndNoPepAction);
            this.isDecisionReturnedOnFirstApplicableRule = returnOnFirstApplicable;
        }

        /**
         * Creates an evaluator that works in the second mode described in the class-level Javadoc description.
         *
         * @param rulesWithSameEffectAndPepActions rules with common Effect and at least one PEP action (Obligation/Advice). Must be non-empty if {@code rulesWithSameEffectAndNoPepAction} is empty.
         * @param rulesWithSameEffectAndNoPepAction rules with same Effect and no PEP action. Must be non-empty if {@code rulesWithSameEffectAndPepActions} is empty.
         * @param returnOnFirstApplicable          true iff the combined decision should be returned as soon as a Rule evaluates to the common Effect (Permit/Deny). This is the case for example with standard *-overrides and *-unless-* rule combining algorithms when the rules' Effect is the overriding effect (e.g. Deny in deny-overrides / permit-unless-deny).
         */
        RulesWithSameEffectEvaluator(final Collection<? extends RuleEvaluator> rulesWithSameEffectAndPepActions, final Collection<? extends RuleEvaluator> rulesWithSameEffectAndNoPepAction, final boolean returnOnFirstApplicable)
        {
            this(rulesWithSameEffectAndPepActions, rulesWithSameEffectAndNoPepAction, false, returnOnFirstApplicable);
        }

        /**
         * Creates an evaluator that always returns the common Effect as decision (the decision is fixed in advance and Indeterminate/NotApplicable are ignored), and with PEP actions collected from applicable rules.
         * E.g. in x-overrides algorithms, this is the case if there is at least one empty rule (no target/condition/pep action) with the overriding Effect (e.g. Deny for deny-overrides), in which case the decision is already known but we still need to collect Obligations/Advice from other rules.
         *
         * @param rulesWithSameEffectAndPepActions rules with common Effect and at least one PEP action (Obligation/Advice). Must be non-empty.
         * @param returnOnFirstApplicable          true iff the combined decision should be returned as soon as a Rule evaluates to the common Effect (Permit/Deny). This is the case for example with standard *-overrides and *-unless-* rule combining algorithms when the rules' Effect is the overriding effect (e.g. Deny in deny-overrides / permit-unless-deny).
         */
        RulesWithSameEffectEvaluator(final Collection<? extends RuleEvaluator> rulesWithSameEffectAndPepActions, final boolean returnOnFirstApplicable)
        {
            this(rulesWithSameEffectAndPepActions, List.of(), true, returnOnFirstApplicable);
        }

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

        @Override
        public ExtendedDecision evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final UpdatableList<PepAction> updatablePepActions,
                                         final UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList)
        {
			/*
			 If the finalDecision != null (Permit/Deny decision always returned), any Indeterminate error is ignored. But for troubleshooting purposes, it may be useful to save the Indeterminate error(s) for logging later
			 */
            ExtendedDecision finalDecision = presetFinalDecision; // null if final decision not fixed in advance (see the constructor)
            ExtendedDecision firstIndeterminate = null;

            for (final RuleEvaluator rule : rulesWithSameEffectAndPepActions)
            {
                final DecisionResult evalResult = rule.evaluate(context, mdpContext);
                final DecisionType decision = evalResult.getDecision();
                if (decision == commonEffectAsDecision)
                {
                    updatablePepActions.addAll(evalResult.getPepActions());
                    if (isDecisionReturnedOnFirstApplicableRule)
                    {
                        return commonEffectAsExtDecision;
                    }

                    finalDecision = commonEffectAsExtDecision;
                    continue;
                }

				/*
				 Decision is NotApplicable or Indeterminate.
				 If finalDecision already fixed (either fixed in advance or some rule returned the common Effect Permit/Deny successfully already),
				 we don't care, ignore.
				 Else if decision Indeterminate, evalResult is Indeterminate(common_effect), we only need to keep the first Indeterminate rules for later

			 Even if the algorithm is one of the standard X-unless-Y, although Indeterminate errors are ignored in the final result, it may be useful to save the Indeterminate error(s) for logging later.
				 */
                if (finalDecision == null && decision == DecisionType.INDETERMINATE && firstIndeterminate == null)
                {
                    // this is the first Indeterminate
                    firstIndeterminate = evalResult;
                }
            }

            if (finalDecision != null)
            {
				/*
				TODO: for troubleshooting purposes, it may be useful to report the firstIndeterminate somehow for logging later, although it is ignored in the final result.
				 */
                // PEP actions are already in updatablePepActions
                return finalDecision;
            }

			/*
			finalDecision == null (presetFinalDecision == null) and all rules returned either NotApplicable or Indeterminate, so we need to keep going with the other rules without PEP action
			 */
            for (final RuleEvaluator rule : rulesWithSameEffectAndNoPepAction)
            {
                final DecisionResult evalResult = rule.evaluate(context, mdpContext);
                final DecisionType decision = evalResult.getDecision();
                if (decision == commonEffectAsDecision)
                {
                    finalDecision = commonEffectAsExtDecision;
					/*
					We are done, since we know the final result and there is no PEP action in the remaining rules.
				TODO: for troubleshooting purposes, it may be useful to report the firstIndeterminate somehow for logging later, although it is ignored in the final result.
				 */
                    return finalDecision;
                }

                // finalDecision == null and last decision is either NotApplicable or Indeterminate
                // If decision Indeterminate, evalResult is Indeterminate(common_effect)
				/*
				Even if the algorithm is one of the standard X-unless-Y, although Indeterminate errors are ignored in the final result, it may be useful to save the Indeterminate error(s) for logging later.
				 */
                if (decision == DecisionType.INDETERMINATE && firstIndeterminate == null)
                {
                    // this is the first Indeterminate
                    firstIndeterminate = evalResult;
                }
            }

            // finalDecision == null, only NotApplicable/Indeterminate returned so far
            // If no Indeterminate -> all NotApplicable
            return firstIndeterminate == null ? ExtendedDecisions.SIMPLE_NOT_APPLICABLE : firstIndeterminate;
        }
    }
}
