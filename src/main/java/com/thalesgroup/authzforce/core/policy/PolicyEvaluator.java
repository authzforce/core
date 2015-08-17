package com.thalesgroup.authzforce.core.policy;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.slf4j.Logger;

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
	private final Target target;

	// Child elements combined by combining algorithm
	private final List<? extends T> combinedElements;

	// Policy(Set)CombinerParameters
	private final List<CombinerElement<? extends T>> combinerParameters;

	private final CombiningAlgorithm<T> combiningAlg;
	private final PolicyPepActionExpressions pepActionExps;

	private final Logger logger;

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
	 * @param logger
	 *            logger
	 */
	public PolicyEvaluator(Target target, List<? extends T> combinedElements, List<CombinerElement<? extends T>> combinerParameters, CombiningAlgorithm<T> combiningAlg, PolicyPepActionExpressions pepActionExps, Logger logger)
	{
		this.logger = logger;
		this.target = target;
		this.combinedElements = combinedElements;
		this.combinerParameters = combinerParameters;
		this.combiningAlg = combiningAlg;
		this.pepActionExps = pepActionExps;
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
			logger.debug("{}/Target (none/empty) -> Match", this);
			return true;
		}

		final boolean isMatched = target.match(context);
		logger.debug("{}/Target -> Match={}", this, isMatched);
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
		final DecisionResult algResult;
		if (skipTarget)
		{
			// evaluate with combining algorithm
			algResult = combiningAlg.combine(context, combinerParameters, combinedElements);
			logger.debug("{}/Algorithm -> {}", this, algResult);
		} else
		{
			// evaluate target
			IndeterminateEvaluationException targetMatchIndeterminateException = null;
			try
			{
				if (!matchTarget(context))
				{
					logger.debug("{} -> NotApplicable", this);
					return DecisionResult.NOT_APPLICABLE;
				}
			} catch (IndeterminateEvaluationException e)
			{
				targetMatchIndeterminateException = e;
				/*
				 * Before we lose the exception information, log it at a higher level because it is
				 * an evaluation error (but no critical application error, therefore lower level
				 * than error)
				 */
				logger.info("{}/Target -> Indeterminate", this, e);
			}

			// evaluate with combining algorithm
			algResult = combiningAlg.combine(context, combinerParameters, combinedElements);
			logger.debug("{}/Algorithm -> {}", this, algResult);

			if (targetMatchIndeterminateException != null)
			{
				// FIXME: implement Extended Indeterminates according to table 7 section 7.14 (XACML
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
					 * Before we lose the exception information, log it at a higher level because it
					 * is an evaluation error (but no critical application error, therefore lower
					 * level than error)
					 */
					logger.info("{}/{Obligation|Advice}Expressions -> Indeterminate", this, e);
					return new DecisionResult(e.getStatus());
				}

				if (pepActions != null)
				{
					algResult.add(pepActions);
				}

				return algResult;
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
