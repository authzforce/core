/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.Collection;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
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
		public final ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> updatableApplicablePolicyIdList)
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
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions updatablePepActions,
				final UpdatableList<JAXBElement<IdReferenceType>> updatableApplicablePolicyIdList)
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
