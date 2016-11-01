package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.Collection;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.impl.combining.CombiningAlgEvaluators.RulesWithSameEffectEvaluator;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Rule combining algorithm evaluator for "unordered" - as opposed to
 * "ordered-*" - deny/permit-overrides algorithms, where the rules are
 * re-ordered by Effect so that the ones with the overriding effect (e.g. Deny
 * in case of deny-overrides algorithm, or Permit in case of permit-overrides)
 * are evaluated first. This allows for algorithm optimizations compared to
 * "ordered-*" variants.
 *
 */
final class OverridingEffectFirstRuleCombiningAlgEvaluator extends RulesWithSameEffectEvaluator {
	private final ImmutableList<RuleEvaluator> otherRules;
	private final DecisionType overriddenEffectAsDecision;

	/**
	 * Instantiates the evaluator a list of rules with same Effect, inferring
	 * the effect from the first rule in the list
	 * 
	 * @param rulesWithSameEffect
	 *            combined Rules, all expected to have the same Effect. Must be
	 *            non-null and non-empty.
	 */
	OverridingEffectFirstRuleCombiningAlgEvaluator(final Collection<RuleEvaluator> rulesWithOverridingEffect,
			final Collection<RuleEvaluator> otherRules) {
		super(rulesWithOverridingEffect);
		assert otherRules != null && !otherRules.isEmpty()
				&& rulesWithOverridingEffect.iterator().next().getEffect() != otherRules.iterator().next().getEffect();
		// first rule's effect assumed the same for all
		this.overriddenEffectAsDecision = otherRules.iterator().next().getEffect() == EffectType.DENY
				? DecisionType.DENY : DecisionType.PERMIT;
		this.otherRules = ImmutableList.copyOf(otherRules);
	}

	/**
	 * Evaluate rules with overridden Effect in the case when the evaluation of the rules with overriding Effect returned Indeterminate
	 * @param indeterminateFromRulesWithOverridingEffect
	 *            Indeterminate result from previous evaluation of rules with
	 *            overridING effect
	 * @return final decision
	 */
	private ExtendedDecision evaluateRulesWithOverriddenEffect(final EvaluationContext context, 
			final ExtendedDecision indeterminateFromRulesWithOverridingEffect) {
		/*
		 * indeterminateFromRulesWithOverridingEffect's decision assumed
		 * Indeterminate{overriding_effect}, overriding_effect = D (resp. P) if
		 * overriding Effect is Deny (resp. Permit)
		 */
		assert indeterminateFromRulesWithOverridingEffect != null
				&& indeterminateFromRulesWithOverridingEffect.getDecision() == DecisionType.INDETERMINATE;

		for (final RuleEvaluator rule : otherRules) {
			final DecisionResult evalResult = rule.evaluate(context);
			if (evalResult.getDecision() != DecisionType.NOT_APPLICABLE) {
				/**
				 * decision is the overridden Effect or
				 * Indeterminate{overridden_effect}, which we have to combine
				 * with previous result (from rules with overriding Effect)
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
				return ExtendedDecisions.newIndeterminate(DecisionType.INDETERMINATE,
						indeterminateFromRulesWithOverridingEffect.getStatus());
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
	private ExtendedDecision evaluateRulesWithOverriddenEffect(final EvaluationContext context, final UpdatablePepActions updatablePepActions) {
		ExtendedDecision firstIndeterminateInOverriddenEffect = null;
		for (final RuleEvaluator rule : otherRules) {
			final DecisionResult evalResult = rule.evaluate(context);
			final DecisionType decision = evalResult.getDecision();
			if (decision == overriddenEffectAsDecision) {
				// Permit/Deny
				updatablePepActions.add(evalResult.getPepActions());
				return evalResult;
			}

			/*
			 * If the decision is
			 * Indeterminate, save the indeterminate cause for the final
			 * Indeterminate result (if first Indeterminate), only used if no
			 * other rule with determinate result checked above is found.
			 */
			if (decision == DecisionType.INDETERMINATE && firstIndeterminateInOverriddenEffect == null) {
				// first Indeterminate for overridden effect
				firstIndeterminateInOverriddenEffect = evalResult;
			}
		}
		
		/*
		 * All decisions were NotApplicable or Indeterminate{overridden_effect}
		 */
		// at Least One Indeterminate
		if (firstIndeterminateInOverriddenEffect != null) {
			return firstIndeterminateInOverriddenEffect;
		}

		// All decisions were NotApplicable -> NotApplicable
		return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
	}

	@Override
	public ExtendedDecision evaluate(EvaluationContext context, UpdatablePepActions updatablePepActions,
			UpdatableList<JAXBElement<IdReferenceType>> updatableApplicablePolicyIdList) {
		final ExtendedDecision extDecisionFromRulesWithOverridingEffect = super.evaluate(context, updatablePepActions,
				updatableApplicablePolicyIdList);
		switch (extDecisionFromRulesWithOverridingEffect.getDecision()) {
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