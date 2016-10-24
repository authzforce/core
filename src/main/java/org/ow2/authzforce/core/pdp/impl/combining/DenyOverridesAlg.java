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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

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

import com.google.common.base.Preconditions;

/**
 * This is the standard XACML 3.0 Deny-Overrides policy/rule combining algorithm. It allows a single evaluation of Deny to take precedence over any number of permit, not applicable or indeterminate
 * results. Note that since this implementation does an ordered evaluation, this class also supports the Ordered-Deny-Overrides-algorithm.
 *
 * @version $Id: $
 */
final class DenyOverridesAlg extends BaseCombiningAlg<Decidable>
{

	private static final class Evaluator extends DPOverridesAlgEvaluator
	{
		private Evaluator(final Iterable<? extends Decidable> combinedElements)
		{
			super(combinedElements);
		}

		@Override
		protected ExtendedDecision getOverridingDPResult(final DecisionResult result, final UpdatablePepActions outPepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList, final DPOverridesAlgResultCombiner resultHelper)
		{
			switch (result.getDecision())
			{
				case DENY:
					if (outApplicablePolicyIdList != null)
					{
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
		protected ExtendedDecision getFinalResult(final PepActions combinedPermitPepActions, final UpdatablePepActions outPepActions,
				final List<JAXBElement<IdReferenceType>> combinedApplicablePolicies, final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList,
				final ExtendedDecision firstIndeterminateD, final ExtendedDecision firstIndeterminateP)
		{
			/*
			 * If any Indeterminate{D}, then: if ( any Indeterminate{P} or any Permit ) -> Indeterminate{DP}; else -> Indeterminate{D} (this is a simplified equivalent of the algo in the spec)
			 */
			/*
			 * atLeastOnePermit == true <=> permitPepActions != null
			 */
			if (firstIndeterminateD != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return ExtendedDecisions.newIndeterminate(firstIndeterminateP != null || combinedPermitPepActions != null ? DecisionType.INDETERMINATE : DecisionType.DENY,
						firstIndeterminateD.getStatus());
			}

			// if we got a PERMIT or Indeterminate{P}, return it, otherwise it's NOT_APPLICABLE
			if (combinedPermitPepActions != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				outPepActions.add(combinedPermitPepActions);
				return ExtendedDecisions.SIMPLE_PERMIT;
			}

			if (firstIndeterminateP != null)
			{
				if (outApplicablePolicyIdList != null)
				{
					outApplicablePolicyIdList.addAll(combinedApplicablePolicies);
				}

				return firstIndeterminateP;
			}

			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}
	}

	DenyOverridesAlg(final String algId)
	{
		super(algId, Decidable.class);
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
			return new Evaluator(Preconditions.checkNotNull(combinedElements));
		}

		// combined elements are Rules, we can optimize
		// if no Rules -> NotApplicable
		if (combinedElements == null)
		{
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
		if (!combinedEltIterator.hasNext())
		{
			// empty (no Rules)
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		/*
		 * Prepare to iterate over Rules we will reorder deny rules before permit rules since order does not matter and deny decision prevails
		 */
		final Deque<RuleEvaluator> denyRules = new ArrayDeque<>();
		final Deque<RuleEvaluator> permitRules = new ArrayDeque<>();

		/*
		 * 
		 * If we find any empty Permit Rule, we don't need to look at other Permit rules since it is enough to return Permit if there is no Deny
		 */
		final boolean emptyPermitRuleFound = false;

		while (combinedEltIterator.hasNext())
		{
			final RuleEvaluator rule = (RuleEvaluator) combinedEltIterator.next();
			if (rule.getEffect() == EffectType.DENY)
			{
				if (rule.isEmpty())
				{
					/*
					 * Rule always evaluates to Deny, which is sufficient for deny-overrides alg evaluation -> ignore/remove all other rules
					 */
					// TODO
				}
			}
			else
			{
				// rule Effect = Permit
				// TODO
			}
		}

		// TODO

		/*
		 * 
		 * - iterate over rules: if rule's effect is deny, if rule has no target/condition/pep_actions, // rule will always return deny if LOGGER.warnEnabled && (!denyRules.isEmpty() or
		 * !permitRules.isEmpty()), // not the first rule - log WARN
		 * "Policy XXX's combining alg is deny-overrides and its first declared Rule with Effect=Deny but no target/condition/pep_actions is Rule YYY, result is always Deny, therefore Rule YYY's decision overrides all -> Ignoring/removing all other rules"
		 * return new CombiningAlg.Evaluator() {return rule.evaluate()} // rule has a target/condition, therefore may not necessarily return Deny denyRules.add(rule); else // rule effect is Permit
		 * if(emptyPermitRuleFound) // already one empty permit rule found is enough continue; if rule has no target/condition/pep_actions, // rule will always return permit, which is sufficient for
		 * deny-overrides alg evaluation, ignore/remove all other permit rules if LOGGER.warnEnabled && !permitRules.isEmpty(), // not the first permit rule - log WARN
		 * "Policy XXX's combining alg is deny-overrides and its first declared Rule with Effect=Permit but no target/condition/pep_action is Rule YYY, result is always Permit therefore all other Permit rules will have no effect -> ignoring/removing all other permit rules"
		 * permitRules.clear(); emptyPermitRuleFound = true; // there may be other Deny rules, so we go on permitRules.add(rule)
		 * 
		 * assert !denyRules.isEmpty || !permitRules.isEmpty
		 * 
		 * if(denyRules.isEmpty) // pas de deny rule // !permitRules.isEmpty if(emptyPermitRuleFound) final permitRule = permitRules.get(0) return new CombiningAlg.Evaluator() { return
		 * permitRule.evaluate(); } // no empty permit rule found (and no deny rule) return new CombiningAlg.Evaluator() { firstIndP = null for(rule: permitRules) if(decision = Permit) return rule's
		 * permit result (with pep actions) // evalResult = Ind(P) if(firstIndP == null) firstIndP = evalResult
		 * 
		 * return firstIndP }
		 * 
		 * else // !denyRules.isEmpty if(permitRules.isEmpty) return new CombiningAlg.Evaluator() { firstIndeterminateD = null for(rule: denyRules) if(decision == DENY) return rule's deny result (with
		 * pep actions)
		 * 
		 * // indeterminate(D), no permit rule if(firstIndeterminateD == null) firstIndeterminateD = ...
		 * 
		 * // firstIndeterminateD != null return firstIndeterminateD } else // !permitRules.isEmpty return new CombiningAlg.Evaluator() { firstIndeterminateD = null for(rule: denyRules) if(decision ==
		 * DENY) return rule's deny result (with pep actions)
		 * 
		 * // indeterminate(D), result depends on wether at least one Indeterminate(P) or Permit if(firstIndeterminateD == null) firstIndeterminateD = ...
		 * 
		 * // firstIndeterminateD != null // !permitRules.isEmpty -> Indeterminate(DP) return Indeterminate(DP) }
		 */
	}

}
