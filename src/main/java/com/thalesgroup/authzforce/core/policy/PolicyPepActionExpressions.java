package com.thalesgroup.authzforce.core.policy;

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PepActions;
import com.thalesgroup.authzforce.core.eval.AdviceExpression;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.ObligationExpression;
import com.thalesgroup.authzforce.core.eval.PepActionExpressions;

/**
 * Evaluator of a Policy(Set)'s PEP action (Obligation/Advice) expressions
 * 
 */
public class PolicyPepActionExpressions extends PepActionExpressions
{
	private final List<ObligationExpression> denyObligationExpList = new ArrayList<>();
	private final List<ObligationExpression> permitObligationExpList = new ArrayList<>();
	private final List<AdviceExpression> denyAdviceExpList = new ArrayList<>();
	private final List<AdviceExpression> permitAdviceExpList = new ArrayList<>();

	@Override
	protected void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp, ExpressionFactory expFactory) throws ParsingException
	{
		final ObligationExpression obligationExp = new ObligationExpression(jaxbObligationExp, policyDefaults, expFactory);
		final List<ObligationExpression> modifiedList = obligationExp.getFulfillOn() == EffectType.DENY ? denyObligationExpList : permitObligationExpList;
		modifiedList.add(obligationExp);
	}

	@Override
	protected void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp, ExpressionFactory expFactory) throws ParsingException
	{
		final AdviceExpression adviceExp = new AdviceExpression(jaxbAdviceExp, policyDefaults, expFactory);
		final List<AdviceExpression> modifiedList = adviceExp.getAppliesTo() == EffectType.DENY ? denyAdviceExpList : permitAdviceExpList;
		modifiedList.add(adviceExp);
	}

	@Override
	protected List<ObligationExpression> getObligationExpressionList()
	{
		final List<ObligationExpression> resultList = new ArrayList<>(denyObligationExpList);
		resultList.addAll(permitObligationExpList);
		return resultList;
	}

	@Override
	protected List<AdviceExpression> getAdviceExpressionList()
	{
		final List<AdviceExpression> resultList = new ArrayList<>(denyAdviceExpList);
		resultList.addAll(permitAdviceExpList);
		return resultList;
	}

	private PolicyPepActionExpressions(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		super(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory);
	}

	/**
	 * Instantiates the evaluator with given XACML-schema-derived
	 * ObligationExpressions/AdviceExpressions (a priori specific to a Policy(Set))
	 * 
	 * @param jaxbObligationExpressions
	 *            XACML-schema-derived ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML-schema-derived AdviceExpressions
	 * @param policyDefaults
	 *            policy's default parameters, e.g. XPath version
	 * @param expFactory
	 *            Expression factory for parsing the AttributeAssignmentExpressions in the
	 *            Obligation/Advice Expressions
	 * @return Policy's Obligation/Advice expressions evaluator
	 * @throws ParsingException
	 *             if error parsing one of the AttributeAssignmentExpressions
	 */
	public static PolicyPepActionExpressions getInstance(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		if ((jaxbObligationExpressions == null || jaxbObligationExpressions.getObligationExpressions().isEmpty()) && (jaxbAdviceExpressions == null || jaxbAdviceExpressions.getAdviceExpressions().isEmpty()))
		{
			return null;
		}

		return new PolicyPepActionExpressions(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory);
	}

	/**
	 * Evaluates the PEP action (obligations/Advice) expressions for a given decision and evaluation
	 * context
	 * 
	 * @param decision
	 *            PERMIT/DENY decision to select the Obligation/Advice expressions to apply, based
	 *            on FulfillOn/AppliesTo, typically resuling from evaluation of the parent
	 *            Policy(Set)/Rule
	 * @param context
	 *            evaluation context
	 * @return PEP actions (obligations/advices) or null if none
	 * @throws IndeterminateEvaluationException
	 *             error evaluating one of ObligationExpression/AdviceExpressions'
	 *             AttributeAssignmentExpressions' expressions
	 */
	public PepActions evaluate(DecisionType decision, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final List<ObligationExpression> matchingObligationExpList;
		final List<AdviceExpression> matchingAdviceExpList;
		switch (decision)
		{
			case DENY:
				matchingObligationExpList = denyObligationExpList;
				matchingAdviceExpList = denyAdviceExpList;
				break;
			case PERMIT:
				matchingObligationExpList = permitObligationExpList;
				matchingAdviceExpList = permitAdviceExpList;
				break;
			default:
				throw new IllegalArgumentException("Invalid input decision (" + decision + "): PEP actions (Obligation/AdviceExpressions) may be evaluated only for decision PERMIT/DENY");
		}

		return super.evaluate(matchingObligationExpList, matchingAdviceExpList, context);
	}

}
