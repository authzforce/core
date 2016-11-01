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
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * This is the standard XACML 3.0 Deny-Overrides policy/rule combining
 * algorithm. It allows a single evaluation of Deny to take precedence over any
 * number of permit, not applicable or indeterminate results. Note that since
 * this implementation does an ordered evaluation, this class also supports the
 * Ordered-Deny-Overrides-algorithm.
 *
 * @version $Id: $
 */
final class DenyOverridesAlg extends BaseCombiningAlg<Decidable> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DenyOverridesAlg.class);

	private static final class PolicyCombiningAlgEvaluator extends DPOverridesPolicyCombiningAlgEvaluator {
		private PolicyCombiningAlgEvaluator(final Iterable<? extends Decidable> combinedElements) {
			super(combinedElements);
		}

		@Override
		protected ExtendedDecision getOverridingDPResult(final DecisionResult result,
				final UpdatablePepActions outPepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
				final DPOverridesAlgResultCombiner resultHelper) {
			switch (result.getDecision()) {
			case DENY:
				if (outApplicablePolicyIdList != null) {
					outApplicablePolicyIdList.addAll(resultHelper.getApplicablePolicies(result));
				}

				outPepActions.add(result.getPepActions());
				return ExtendedDecisions.SIMPLE_DENY;
			case PERMIT:
				resultHelper.addSubResultDP(result);
				break;
			case INDETERMINATE:
				resultHelper.addSubResultIndeterminate(result);
				break;
			default:
				break;
			}

			return null;
		}

		@Override
		protected ExtendedDecision getFinalResult(final PepActions combinedPermitPepActions,
				final UpdatablePepActions outPepActions,
				final List<JAXBElement<IdReferenceType>> combinedApplicablePolicies,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
				final ExtendedDecision firstIndeterminateD, final ExtendedDecision firstIndeterminateP) {
			/*
			 * If any Indeterminate{D}, then: if ( any Indeterminate{P} or any
			 * Permit ) -> Indeterminate{DP}; else -> Indeterminate{D} (this is
			 * a simplified equivalent of the algo in the spec)
			 */
			/*
			 * atLeastOnePermit == true <=> permitPepActions != null
			 */
			if (firstIndeterminateD != null) {
				if (outApplicablePolicyIdList != null) {
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return ExtendedDecisions
						.newIndeterminate(
								firstIndeterminateP != null || combinedPermitPepActions != null
										? DecisionType.INDETERMINATE : DecisionType.DENY,
								firstIndeterminateD.getStatus());
			}

			// if we got a PERMIT or Indeterminate{P}, return it, otherwise it's
			// NOT_APPLICABLE
			if (combinedPermitPepActions != null) {
				if (outApplicablePolicyIdList != null) {
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				outPepActions.add(combinedPermitPepActions);
				return ExtendedDecisions.SIMPLE_PERMIT;
			}

			if (firstIndeterminateP != null) {
				if (outApplicablePolicyIdList != null) {
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return firstIndeterminateP;
			}

			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	}

	DenyOverridesAlg(final String algId) {
		super(algId, Decidable.class);
	}

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends Decidable>> params,
			final Iterable<? extends Decidable> combinedElements)
			throws UnsupportedOperationException, IllegalArgumentException {
		/*
		 * If combined elements are Rules, we can optimize
		 */
		if (!RuleEvaluator.class.isAssignableFrom(getCombinedElementType())) {
			return new PolicyCombiningAlgEvaluator(Preconditions.checkNotNull(combinedElements));
		}

		// combined elements are Rules, we can optimize
		// if no Rules -> NotApplicable
		if (combinedElements == null) {
			LOGGER.warn(
					"{}: no rule to combine -> optimization: replacing with equivalent evaluator returning constant NotApplicable decision",
					this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
		if (!combinedEltIterator.hasNext()) {
			// empty (no Rules)
			LOGGER.warn(
					"{}: no rule to combine -> optimization: replacing with equivalent evaluator returning constant NotApplicable decision",
					this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		/*
		 * There is at least one Rule. Prepare to iterate over Rules, we will
		 * reorder deny rules before permit rules since order does not matter
		 * and deny decision prevails
		 */
		final Deque<RuleEvaluator> nonEmptyDenyRules = new ArrayDeque<>();
		final Deque<RuleEvaluator> permitRules = new ArrayDeque<>();

		/*
		 * 
		 * If we find any empty Permit Rule (no target/condition/pep_action), we
		 * don't need to look at other Permit rules since it is enough to return
		 * Permit if there is no Deny
		 */
		boolean atLeastOneEmptyPermitRule = false;

		while (combinedEltIterator.hasNext()) {
			final RuleEvaluator rule = (RuleEvaluator) combinedEltIterator.next();
			if (rule.getEffect() == EffectType.DENY) {
				/*
				 * If rule's effect is Deny and it has no
				 * target/condition/pep_actions, then rule will always return
				 * deny -> deny-overrides alg always evaluates to Deny
				 * (ignore/remove all other rules)
				 */
				if (rule.isEmptyEquivalent()) {
					LOGGER.warn(
							"{}: {} with effect Deny is empty (no target/condition/pep_actions) => always returns Deny => deny-overrides combining algorithm will always return Deny => other combined rules have no effect => will be ignored/removed.",
							this, rule);
					return CombiningAlgEvaluators.DENY_CONSTANT_EVALUATOR;
				}

				/*
				 * Rule is not empty, i.e. has a target/condition, therefore may
				 * not necessarily return Deny
				 */
				nonEmptyDenyRules.add(rule);
				continue;
			}

			/*
			 * Rule Effect = Permit
			 * 
			 * Process Permit rule only if no empty Permit Rule found yet.
			 * Indeed, as mentioned earlier, if there is an empty Permit rule,
			 * we already know the result is always Permit (so no need to
			 * process other Permit rules), if there is no Deny rule. Only Deny
			 * rules may change the final result in this case.
			 */
			if (atLeastOneEmptyPermitRule) {
				// ignore this new Permit Rule
				LOGGER.warn(
						"{}: Ignoring/removing {} with effect Permit because does not affect the result, only affected by empty Permit Rule ({}) found previously (always returns Permit), and Deny rule(s).",
						this, rule, permitRules);
				// continue looking for Deny rules
				continue;
			}

			// No empty Permit Rule found yet; what about this one?
			if (rule.isEmptyEquivalent()) {
				/*
				 * This is the first declared empty Permit Rule -> always
				 * returns Permit; we can ignore/remove other Permit Rules (have
				 * no effect anymore)
				 */
				LOGGER.warn(
						"{}: {} with effect Permit is empty (no target/condition/pep_actions) => always returns Permit => deny-overrides combining algorithm will always return this Permit unless some Deny rule applies => other combined Permit rules have no effect => will be ignored/removed.",
						this, rule);
				atLeastOneEmptyPermitRule = true;
				permitRules.clear();
				permitRules.addLast(rule);
				// continue looking for Deny rules
				continue;
			}

			/*
			 * Non-empty Permit Rule
			 */
			permitRules.addLast(rule);
		}

		/*
		 * There is at least one rule and there is no empty Deny Rule
		 */
		if (nonEmptyDenyRules.isEmpty()) {
			/*
			 * no Deny Rule (whether empty or not) -> at least one Permit Rule
			 * and all rules are Permit rules
			 */
			if (atLeastOneEmptyPermitRule) {
				/*
				 * no Deny Rule but one empty Permit rule -> final result is
				 * Permit always
				 */
				LOGGER.warn(
						"{}: the only combined rule is empty Permit Rule ({}) => deny-overrides combining algorithm will always return this Permit => optimization: replacing with equivalent evaluator returning constant Permit decision",
						this, permitRules);
				return CombiningAlgEvaluators.PERMIT_CONSTANT_EVALUATOR;
			}

			/*
			 * At least one Permit Rule and all rules are non-empty Permit rules
			 */
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(permitRules);
		}

		// There is at least one non-empty Deny rule
		if (permitRules.isEmpty()) {
			/*
			 * No Permit rule -> only non-empty Deny rules
			 */
			return new CombiningAlgEvaluators.RulesWithSameEffectEvaluator(nonEmptyDenyRules);
		}

		// At least one Permit Rule and only non-empty Deny rules
		return new OverridingEffectFirstRuleCombiningAlgEvaluator(nonEmptyDenyRules, permitRules);
	}

}
