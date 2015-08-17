package com.thalesgroup.authzforce.core.eval;

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PepActions;

/**
 * Evaluator of a Rule's PEP action (Obligation/Advice) expressions
 * 
 */
public class RulePepActionExpressions extends PepActionExpressions
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RulePepActionExpressions.class);

	private final EffectType appliesToEffect;
	private final List<ObligationExpression> obligationExpList = new ArrayList<>();
	private final List<AdviceExpression> adviceExpList = new ArrayList<>();

	@Override
	protected void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp, ExpressionFactory expFactory) throws ParsingException
	{
		if (jaxbObligationExp.getFulfillOn() != appliesToEffect)
		{
			if (LOGGER.isWarnEnabled())
			{
				LOGGER.warn("Ignored ObligationExpression[@ObligationId='{}'] because @FulfillOn = {} does not match required Effect = {}", jaxbObligationExp.getObligationId(), jaxbObligationExp.getFulfillOn(), appliesToEffect);
			}

			// skip this obligation
			return;
		}

		final ObligationExpression obligationExp = new ObligationExpression(jaxbObligationExp, policyDefaults, expFactory);
		obligationExpList.add(obligationExp);

	}

	@Override
	protected void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp, ExpressionFactory expFactory) throws ParsingException
	{
		if (jaxbAdviceExp.getAppliesTo() != appliesToEffect)
		{
			if (LOGGER.isWarnEnabled())
			{
				LOGGER.warn("Ignored AdviceExpression[@AdviceId='{}'] because @AppliesTo = {} does not match required Effect = {}", jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(), appliesToEffect);
			}

			// skip this obligation
			return;
		}

		final AdviceExpression obligationExp = new AdviceExpression(jaxbAdviceExp, policyDefaults, expFactory);
		adviceExpList.add(obligationExp);
	}

	@Override
	protected List<ObligationExpression> getObligationExpressionList()
	{
		return this.obligationExpList;
	}

	@Override
	protected List<AdviceExpression> getAdviceExpressionList()
	{
		return this.adviceExpList;
	}

	private RulePepActionExpressions(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory, EffectType effect) throws ParsingException
	{
		super(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory);
		if (effect == null)
		{
			throw new IllegalArgumentException("Undefined Rule's Effect to which obligations/advice must apply");
		}

		this.appliesToEffect = effect;
	}

	/**
	 * Instantiates the evaluator with given XACML-schema-derived
	 * ObligationExpressions/AdviceExpressions an Effect to be match by these expressions (a priori
	 * specific to a Rule)
	 * 
	 * @param jaxbObligationExpressions
	 *            XACML-schema-derived ObligationExpressions
	 * @param jaxbAdviceExpressions
	 *            XACML-schema-derived AdviceExpressions
	 * @param policyDefaults
	 *            enclosing policy default parameters, e.g. XPath version
	 * @param expFactory
	 *            Expression factory for parsing the AttributeAssignmentExpressions in the
	 *            Obligation/Advice Expressions
	 * @param effect
	 *            rule's Effect to be matched by ObligationExpressions/FulfillOn and
	 *            AdviceExpressions/AppliesTo
	 * @return Rule's Obligation/Advice expressions evaluator
	 * @throws ParsingException
	 *             if error parsing one of the AttributeAssignmentExpressions
	 */
	public static RulePepActionExpressions getInstance(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory, EffectType effect) throws ParsingException
	{
		if ((jaxbObligationExpressions == null || jaxbObligationExpressions.getObligationExpressions().isEmpty()) && (jaxbAdviceExpressions == null || jaxbAdviceExpressions.getAdviceExpressions().isEmpty()))
		{
			return null;
		}

		return new RulePepActionExpressions(jaxbObligationExpressions, jaxbAdviceExpressions, policyDefaults, expFactory, effect);
	}

	/**
	 * Evaluates the PEP action (obligations/Advice) expressions in a given evaluation context
	 * 
	 * @param context
	 *            evaluation context
	 * @return PEP actions (obligations/advices) or null if none
	 * @throws IndeterminateEvaluationException
	 *             error evaluating one of ObligationExpression/AdviceExpressions'
	 *             AttributeAssignmentExpressions' expressions
	 */
	public PepActions evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return super.evaluate(this.obligationExpList, this.adviceExpList, context);
	}

}
