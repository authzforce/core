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
	 * Evaluator that uses the result of RulesWithSameEffectEvaluator to return the opposite if NotApplicable or Indeterminate{DP} if Indeterminate, else the same result.
	 */
	static class RulesWithSameEffectEvaluator implements CombiningAlg.Evaluator
	{
		private final ImmutableList<RuleEvaluator> rulesWithSameEffect;
		private final DecisionType commonDecision;

		RulesWithSameEffectEvaluator(final Collection<? extends RuleEvaluator> rulesWithSameEffect)
		{
			assert rulesWithSameEffect != null && !rulesWithSameEffect.isEmpty();
			// first rule's effect assumed the same for all
			this.commonDecision = rulesWithSameEffect.iterator().next().getEffect() == EffectType.DENY ? DecisionType.DENY : DecisionType.PERMIT;
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
