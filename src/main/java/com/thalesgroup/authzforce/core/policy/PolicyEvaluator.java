/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.policy;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.combine.CombinerElement;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.thalesgroup.authzforce.core.PepActions;
import com.thalesgroup.authzforce.core.Target;
import com.thalesgroup.authzforce.core.eval.Decidable;
import com.thalesgroup.authzforce.core.eval.DecisionResult;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Policy(Set) evaluator
 * 
 * @param <T>
 *            type of combined child elements in evaluated Policy(Set)
 * 
 */
public class PolicyEvaluator<T extends Decidable>
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PolicyEvaluator.class);

	private final Target target;

	// Child elements combined by combining algorithm
	private final List<? extends T> combinedElements;

	// Policy(Set)CombinerParameters
	private final List<CombinerElement<? extends T>> combinerParameters;

	private final CombiningAlgorithm<T> combiningAlg;
	private final PolicyPepActionExpressionsEvaluator pepActionExps;

	private final String policyId;

	private final Set<String> localVariableIds;

	/**
	 * Instantiates an evaluator
	 * 
	 * @param target
	 *            policy(Set) target
	 * @param combinedElements
	 *            child elements combined in the policy(set) by {@code combiningAlg}
	 * @param combinerParameters
	 *            combining algorithm parameters
	 * @param combiningAlg
	 *            combining algorithm
	 * @param pepActionExps
	 *            PEP action expressions associated to the policy(set)
	 * @param localVariableIds
	 *            IDs of variables defined locally (in policy {@code policyId})
	 * @param policyId
	 *            identifier of the policy that this evaluator evaluates
	 */
	public PolicyEvaluator(Target target, List<? extends T> combinedElements, List<CombinerElement<? extends T>> combinerParameters, CombiningAlgorithm<T> combiningAlg, PolicyPepActionExpressionsEvaluator pepActionExps, Set<String> localVariableIds, String policyId)
	{
		this.policyId = policyId;
		this.target = target;
		this.combinedElements = combinedElements;
		this.combinerParameters = combinerParameters;
		this.combiningAlg = combiningAlg;
		this.pepActionExps = pepActionExps;
		this.localVariableIds = localVariableIds == null ? Collections.EMPTY_SET : localVariableIds;
	}

	/**
	 * Implements "isApplicable()" defined by Only-one-applicable algorithm (section C.9), i.e.
	 * checks whether the target matches the evaluation context.
	 * 
	 * @param context
	 *            evaluation context to match against the target
	 * @return true iff it is applicable (target matches)
	 * @throws IndeterminateEvaluationException
	 *             if Target evaluation in this context is "Indeterminate"
	 */
	public boolean matchTarget(EvaluationContext context) throws IndeterminateEvaluationException
	{
		/*
		 * Null or empty Target matches all
		 */
		if (target == null)
		{
			LOGGER.debug("{}/Target (none/empty) -> Match", policyId);
			return true;
		}

		final boolean isMatched = target.match(context);
		LOGGER.debug("{}/Target -> Match={}", policyId, isMatched);
		return isMatched;
	}

	/**
	 * Policy(Set) evaluation which option to skip Target evaluation. The option is to be used by
	 * Only-one-applicable algorithm with value 'true', after calling
	 * {@link #matchTarget(EvaluationContext)} in particular.
	 * 
	 * @param context
	 *            evaluation context
	 * @param skipTarget
	 *            whether to evaluate the Target.
	 * @return decision result
	 */
	public DecisionResult eval(EvaluationContext context, boolean skipTarget)
	{
		try
		{
			final DecisionResult algResult;
			if (skipTarget)
			{
				// evaluate with combining algorithm
				algResult = combiningAlg.combine(context, combinerParameters, combinedElements);
				LOGGER.debug("{}/Algorithm -> {}", policyId, algResult);
			} else
			{
				// evaluate target
				IndeterminateEvaluationException targetMatchIndeterminateException = null;
				try
				{
					if (!matchTarget(context))
					{
						LOGGER.debug("{} -> NotApplicable", policyId);
						return DecisionResult.NOT_APPLICABLE;
					}
				} catch (IndeterminateEvaluationException e)
				{
					targetMatchIndeterminateException = e;
					/*
					 * Before we lose the exception information, log it at a higher level because it
					 * is an evaluation error (but no critical application error, therefore lower
					 * level than error)
					 */
					LOGGER.info("{}/Target -> Indeterminate", policyId, e);
				}

				// evaluate with combining algorithm
				algResult = combiningAlg.combine(context, combinerParameters, combinedElements);
				LOGGER.debug("{}/Algorithm -> {}", policyId, algResult);

				if (targetMatchIndeterminateException != null)
				{
					// FIXME: implement Extended Indeterminates according to table 7 section 7.14
					// (XACML
					// 3.0)
					if (algResult.getDecision() == DecisionType.NOT_APPLICABLE)
					{
						return algResult;
					}

					// everything else considered as Indeterminate
					return new DecisionResult(targetMatchIndeterminateException.getStatus());
				}
			}

			// target match not indeterminate
			final DecisionType algResultDecision = algResult.getDecision();
			switch (algResultDecision)
			{
				case NOT_APPLICABLE:
				case INDETERMINATE:
					return algResult;
				default:
					if (pepActionExps == null)
					{
						return algResult;
					}

					/*
					 * If any of the attribute assignment expressions in an obligation or advice
					 * expression with a matching FulfillOn or AppliesTo attribute evaluates to
					 * "Indeterminate", then the whole rule, policy, or policy set SHALL be
					 * "Indeterminate" (see XACML 3.0 core spec, section 7.18).
					 */
					final PepActions pepActions;
					try
					{
						pepActions = pepActionExps.evaluate(algResultDecision, context);
					} catch (IndeterminateEvaluationException e)
					{
						/*
						 * Before we lose the exception information, log it at a higher level
						 * because it is an evaluation error (but no critical application error,
						 * therefore lower level than error)
						 */
						LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", policyId, e);
						return new DecisionResult(e.getStatus());
					}

					if (pepActions != null)
					{
						algResult.add(pepActions);
					}

					return algResult;
			}
		} finally
		{
			// remove local variables from context
			for (final String varId : this.localVariableIds)
			{
				context.removeVariable(varId);
			}
		}
	}

	/**
	 * Get combining algorithm
	 * 
	 * @return get combining algorithm
	 */
	public CombiningAlgorithm<?> getCombiningAlg()
	{
		return this.combiningAlg;
	}

}
