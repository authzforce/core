/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.policy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

import org.ow2.authzforce.core.Decidable;
import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.PepActions;
import org.ow2.authzforce.core.TargetEvaluator;
import org.ow2.authzforce.core.combining.CombiningAlg;
import org.ow2.authzforce.core.combining.CombiningAlgParameter;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * Generic Policy(Set) evaluator. Evaluates to a Decision.
 * 
 * @param <T>
 *            type of combined child elements in evaluated Policy(Set)
 * 
 */
public class GenericPolicyEvaluator<T extends Decidable> implements IPolicyEvaluator
{
	private final static Logger LOGGER = LoggerFactory.getLogger(GenericPolicyEvaluator.class);
	private static final ParsingException NULL_POLICY_ID_EXCEPTION = new ParsingException("Undefined Policy(Set)Id (required)");
	private static final ParsingException NULL_VERSION_EXCEPTION = new ParsingException("Undefined Policy(Set) Version (required)");

	private final String policyId;
	private final String version;
	private final TargetEvaluator targetEvaluator;
	private final CombiningAlg.Evaluator combiningAlgEvaluator;
	private final PolicyPepActionExpressionsEvaluator pepActionExps;
	private final Set<String> localVariableIds;
	private final String combiningAlgId;

	private transient final String toString;
	private transient final int hashCode;

	/**
	 * Instantiates an evaluator
	 * 
	 * @param combinedElementClass
	 *            combined element class
	 * 
	 * @param policyTarget
	 *            policy(Set) Target
	 * @param combinedElements
	 *            child elements combined in the policy(set) by {@code combiningAlg}
	 * @param combinerParameters
	 *            combining algorithm parameters
	 * @param localVariableIds
	 *            IDs of variables defined locally (in policy {@code policyId})
	 * @param policyId
	 *            identifier of the policy that this evaluator evaluates
	 * @param policyVersion
	 *            policy version
	 * @param combiningAlgId
	 *            (policy/rule-)combining algorithm ID
	 * @param obligationExps
	 *            ObligationExpressions
	 * @param adviceExps
	 *            AdviceExpressions
	 * @param defaultXPathCompiler
	 *            Default XPath compiler corresponding to the Policy(Set) default XPath version
	 * @param expressionFactory
	 *            Expression factory/parser
	 * @param combiningAlgRegistry
	 *            rule/policy combining algorithm registry
	 * @throws ParsingException
	 */
	public GenericPolicyEvaluator(Class<T> combinedElementClass, String policyId, String policyVersion, Target policyTarget, String combiningAlgId,
			List<? extends T> combinedElements, List<CombiningAlgParameter<? extends T>> combinerParameters, ObligationExpressions obligationExps,
			AdviceExpressions adviceExps, Set<String> localVariableIds, XPathCompiler defaultXPathCompiler, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry) throws ParsingException
	{
		if (policyId == null)
		{
			throw NULL_POLICY_ID_EXCEPTION;
		}

		this.policyId = policyId;
		if (policyVersion == null)
		{
			throw NULL_VERSION_EXCEPTION;
		}

		this.version = policyVersion;

		this.toString = this.getClass().getSimpleName() + "[" + this.policyId + "#v" + this.version + "]";
		/*
		 * Note that we ignore the PolicyIssuer in the hashCode because it is ignored/unused as well in PolicyIdReferences. So we consider it is useless for
		 * identification in the XACML model.
		 */
		this.hashCode = Objects.hash(this.getClass(), this.policyId, this.version);

		try
		{
			this.targetEvaluator = new TargetEvaluator(policyTarget, defaultXPathCompiler, expressionFactory);
		} catch (ParsingException e)
		{
			throw new ParsingException(this + ": Error parsing Target", e);
		}

		this.combiningAlgId = combiningAlgId;
		final CombiningAlg<T> combiningAlg;
		try
		{
			combiningAlg = combiningAlgRegistry.getAlgorithm(combiningAlgId, combinedElementClass);
		} catch (UnknownIdentifierException e)
		{
			throw new ParsingException(this + ": Unknown combining algorithm ID = " + combiningAlgId, e);
		}

		this.combiningAlgEvaluator = combiningAlg.getInstance(combinerParameters, combinedElements);
		this.pepActionExps = PolicyPepActionExpressionsEvaluator.getInstance(obligationExps, adviceExps, defaultXPathCompiler, expressionFactory);
		this.localVariableIds = localVariableIds == null ? Collections.<String> emptySet() : localVariableIds;
	}

	/**
	 * Policy(Set) evaluation which option to skip Target evaluation. The option is to be used by Only-one-applicable algorithm with value 'true', after calling
	 * {@link #isApplicable(EvaluationContext)} in particular.
	 * 
	 * @param context
	 *            evaluation context
	 * @param skipTarget
	 *            whether to evaluate the Target.
	 * @return decision result
	 */
	@Override
	public DecisionResult evaluate(EvaluationContext context, boolean skipTarget)
	{
		try
		{
			final DecisionResult algResult;
			if (skipTarget)
			{
				// evaluate with combining algorithm
				algResult = combiningAlgEvaluator.eval(context);
				LOGGER.debug("{}/Algorithm -> {}", policyId, algResult);
			} else
			{
				// evaluate target
				IndeterminateEvaluationException targetMatchIndeterminateException = null;
				try
				{
					if (!isApplicable(context))
					{
						LOGGER.debug("{} -> NotApplicable", policyId);
						return DecisionResult.NOT_APPLICABLE;
					}
				} catch (IndeterminateEvaluationException e)
				{
					targetMatchIndeterminateException = e;
					/*
					 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error,
					 * therefore lower level than error)
					 */
					LOGGER.info("{}/Target -> Indeterminate", policyId, e);
				}

				// evaluate with combining algorithm
				algResult = combiningAlgEvaluator.eval(context);
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
				 * If any of the attribute assignment expressions in an obligation or advice expression with a matching FulfillOn or AppliesTo attribute
				 * evaluates to "Indeterminate", then the whole rule, policy, or policy set SHALL be "Indeterminate" (see XACML 3.0 core spec, section 7.18).
				 */
				final PepActions pepActions;
				try
				{
					pepActions = pepActionExps.evaluate(algResultDecision, context);
				} catch (IndeterminateEvaluationException e)
				{
					/*
					 * Before we lose the exception information, log it at a higher level because it is an evaluation error (but no critical application error,
					 * therefore lower level than error)
					 */
					LOGGER.info("{}/{Obligation|Advice}Expressions -> Indeterminate", policyId, e);
					return new DecisionResult(e.getStatus());
				}

				if (pepActions != null)
				{
					algResult.add(pepActions.getObligations(), pepActions.getAdvices());
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

	@Override
	public boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException
	{
		/*
		 * Null or empty Target matches all
		 */
		if (targetEvaluator == null)
		{
			LOGGER.debug("{}/Target (none/empty) -> Match", policyId);
			return true;
		}

		final boolean isMatched = targetEvaluator.match(context);
		LOGGER.debug("{}/Target -> Match={}", policyId, isMatched);
		return isMatched;
	}

	@Override
	public DecisionResult evaluate(EvaluationContext context)
	{
		return evaluate(context, false);
	}

	@Override
	public String toString()
	{
		return toString;
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		// if not both PolicyEvaluators or not both PolicySetEvaluators
		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final GenericPolicyEvaluator<?> other = (GenericPolicyEvaluator<?>) obj;
		/*
		 * We ignore the policyIssuer because it is no part of PolicyReferences, therefore we consider it is not part of the Policy uniqueness
		 */
		return this.policyId.equals(other.policyId) && this.version.equals(other.version);
	}

	@Override
	public String getPolicyId()
	{
		return this.policyId;
	}

	@Override
	public String getCombiningAlgId()
	{
		return this.combiningAlgId;
	}

}
