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
package org.ow2.authzforce.core.pdp.impl.combining;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.ExtendedDecisions;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.UpdatablePepActions;
import org.ow2.authzforce.core.pdp.api.combining.BaseCombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.pdp.impl.rule.RuleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the standard First-Applicable policy/rule combining algorithm. It looks through the set of policies/rules, finds the first one that applies, and returns that evaluation result.
 *
 * @version $Id: $
 */
final class FirstApplicableCombiningAlg<T extends Decidable> extends BaseCombiningAlg<T>
{

	private static final class Evaluator extends BaseCombiningAlg.Evaluator<Decidable>
	{

		private Evaluator(final Iterable<? extends Decidable> combinedElements)
		{
			super(combinedElements);
		}

		@Override
		public ExtendedDecision evaluate(final EvaluationContext context, final UpdatablePepActions outPepActions, final UpdatableList<JAXBElement<IdReferenceType>> outApplicablePolicyIdList)
		{
			for (final Decidable combinedElement : getCombinedElements())
			{
				// evaluate the policy
				final DecisionResult result = combinedElement.evaluate(context);
				final DecisionType decision = result.getDecision();

				/*
				 * In case of PERMIT, DENY, or INDETERMINATE, we always just return that decision, so only on a rule that doesn't apply do we keep going...
				 */
				switch (decision)
				{
					case PERMIT:
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						outPepActions.add(result.getPepActions());
						return ExtendedDecisions.SIMPLE_PERMIT;
					case DENY:
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						outPepActions.add(result.getPepActions());
						return ExtendedDecisions.SIMPLE_DENY;
					case INDETERMINATE:
						if (outApplicablePolicyIdList != null)
						{
							outApplicablePolicyIdList.addAll(result.getApplicablePolicies());
						}

						return result;
					default:
						break;
				}

			}

			// if we got here, then none of the rules applied
			return ExtendedDecisions.SIMPLE_NOT_APPLICABLE;
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(FirstApplicableCombiningAlg.class);

	/** {@inheritDoc} */
	@Override
	public CombiningAlg.Evaluator getInstance(final Iterable<CombiningAlgParameter<? extends T>> params, final Iterable<? extends T> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException
	{
		// if no element combined -> decision is overridden Effect
		if (combinedElements == null)
		{
			LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant decision NotApplicable", this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		final Iterator<? extends Decidable> combinedEltIterator = combinedElements.iterator();
		if (!combinedEltIterator.hasNext())
		{
			// empty (no element to combine)
			LOGGER.warn("{}: no element to combine -> optimization: replacing with equivalent evaluator returning constant decision NotApplicable", this);
			return CombiningAlgEvaluators.NOT_APPLICABLE_CONSTANT_EVALUATOR;
		}

		if (!RuleEvaluator.class.isAssignableFrom(getCombinedElementType()))
		{
			// combined elements are not rules but policies
			return new Evaluator(combinedElements);
		}

		// combined elements are Rules, we can optimize
		/*
		 * There is at least one Rule. Prepare to iterate over Rules.
		 */
		/*
		 * If we found any empty rule
		 */
		final Deque<RuleEvaluator> finalRules = new ArrayDeque<>();
		while (combinedEltIterator.hasNext())
		{
			final RuleEvaluator rule = (RuleEvaluator) combinedEltIterator.next();
			finalRules.add(rule);
			if (rule.isAlwaysApplicable())
			{
				/*
				 * The algorithm won't go further than that
				 */
				break;
			}
		}

		/*
		 * if(combinedEltIterator.hasNext()), combinedElements has more elements than finalRules, so finalRules is a subset of combinedElements; else they have the same elements
		 */
		return new Evaluator(combinedEltIterator.hasNext() ? finalRules : combinedElements);
	}

	FirstApplicableCombiningAlg(final String algId, final Class<T> combinedType)
	{
		super(algId, combinedType);
	}

}
