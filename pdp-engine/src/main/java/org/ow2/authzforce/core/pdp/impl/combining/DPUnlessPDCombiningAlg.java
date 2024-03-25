/*
 * Copyright 2012-2024 THALES.
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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.impl.combining.CombiningAlgEvaluators.RulesWithSameEffectEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;

import java.util.List;
import java.util.Optional;

/**
 * X-unless-Y combining algorithm (deny-unless-permit or permit-unless-deny)
 * <p>
 * Quite similar to the Y-overrides algorithm, except Y is returned instead of Indeterminate / NotApplicable results.
 *
 * @version $Id: $
 */
final class DPUnlessPDCombiningAlg<T extends Decidable> extends DPOverridesCombiningAlg<T>
{
    /**
     * Constructor
     *
     * @param algId            combining algorithm ID
     * @param overridingEffect overriding Effect, e.g. Permit if algId is "deny-unless-permit"
     */
    DPUnlessPDCombiningAlg(final String algId, final Class<T> combinedType, final EffectType overridingEffect)
    {
        super(algId, combinedType, overridingEffect, true, true);
    }

    @Override
    protected OverriddenEffectRuleCombiningHelper getOverriddenEffectRulesCombiningAlgEvaluator(boolean atLeastOneEmptyRuleWithOverriddenEffect, List<RuleEvaluator> nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction, List<RuleEvaluator> nonEmptyRulesWithOverriddenEffectAndNoPepAction)
    {
        /*
		 Here the algo differs from DPOverridesCombiningAlg:
			the final decision is already known/fixed to be {overridden_effect} when this sub-combiner is called, so we only need to collect PEP actions from {overridden_effect} rules, to combine them in the result (others without PEP action are ignored).

            Indeed, in the XACML spec, the pseudo-code of standard *-overrides and *-unless-* algorithms iterates over all combined elements (e.g. rules), unless a Rule evaluates to {overriding_effect}.
            Therefore Obligations/Advice from all rules should be returned when the result is the {overridden_effect}.
		 */
        final CombiningAlg.Evaluator overriddenEffectRuleCombiner = nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction.isEmpty() ? constantOverriddenEffectDecisionEvaluator :
                new RulesWithSameEffectEvaluator(nonEmptyRulesWithOverriddenEffectAndAtLeastOnePepAction, false);

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
                return overriddenEffectRuleCombiner.evaluate(context, mdpContext, updatablePepActions, null);
            }
        };
    }

}
