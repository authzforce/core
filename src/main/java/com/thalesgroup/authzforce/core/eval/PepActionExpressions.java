package com.thalesgroup.authzforce.core.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PepActions;

/**
 * Evaluator of PEP action (Obligation/Advice) expressions of a Policy(Set) or Rule
 * 
 */
public abstract class PepActionExpressions
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PepActionExpressions.class);
	private final ObligationExpressions jaxbObligationExpressions;
	private final AdviceExpressions jaxbAdviceExpressions;
	protected final DefaultsType policyDefaults;

	protected abstract void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp, ExpressionFactory expFactory) throws ParsingException;

	protected abstract void add(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp, ExpressionFactory expFactory) throws ParsingException;

	protected abstract List<ObligationExpression> getObligationExpressionList();

	protected abstract List<AdviceExpression> getAdviceExpressionList();

	protected PepActionExpressions(ObligationExpressions jaxbObligationExpressions, AdviceExpressions jaxbAdviceExpressions, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		this.policyDefaults = policyDefaults;

		if (jaxbObligationExpressions == null)
		{
			this.jaxbObligationExpressions = null;
		} else
		{
			final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression> jaxbObligationExpList = jaxbObligationExpressions.getObligationExpressions();
			if (jaxbObligationExpList.isEmpty())
			{
				this.jaxbObligationExpressions = null;
			} else
			{
				for (oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp : jaxbObligationExpList)
				{
					try
					{
						add(jaxbObligationExp, expFactory);
					} catch (ParsingException e)
					{
						throw new ParsingException("Error parsing ObligationExpression[@ObligationId='" + jaxbObligationExp.getObligationId() + "']/AttributeAssignmentExpression/Expression elements", e);
					}
				}

				// get list of ObligationExpressions after parsing/filtering (based on FulfillOn)
				final List<ObligationExpression> obligExpList = getObligationExpressionList();
				if (obligExpList == null)
				{
					this.jaxbObligationExpressions = null;
				} else
				{
					this.jaxbObligationExpressions = new ObligationExpressions(Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression> unmodifiableList(obligExpList));

				}
			}
		}

		if (jaxbAdviceExpressions == null)
		{
			this.jaxbAdviceExpressions = null;
		} else
		{
			final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression> jaxbAdviceExpList = jaxbAdviceExpressions.getAdviceExpressions();
			if (jaxbAdviceExpList.isEmpty())
			{
				this.jaxbAdviceExpressions = null;
			} else
			{
				for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp : jaxbAdviceExpList)
				{
					try
					{
						add(jaxbAdviceExp, expFactory);
					} catch (ParsingException e)
					{
						throw new ParsingException("Error parsing one of the AdviceExpression[@AdviceId='" + jaxbAdviceExp.getAdviceId() + "']/AttributeAssignmentExpression/Expression elements", e);
					}
				}

				// get list of AdviceExpressions after parsing/filtering (based on AppliesTo)
				final List<AdviceExpression> adviceExpList = getAdviceExpressionList();
				if (adviceExpList == null)
				{
					this.jaxbAdviceExpressions = null;
				} else
				{
					this.jaxbAdviceExpressions = new AdviceExpressions(Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression> unmodifiableList(adviceExpList));

				}
			}
		}
	}

	/**
	 * Get the corresponding XACML/JAXB ObligationExpressions element
	 * 
	 * @return ObligationExpressions element
	 */
	public final ObligationExpressions getObligationExpressions()
	{
		return jaxbObligationExpressions;
	}

	/**
	 * Get the corresponding XACML/JAXB AdviceExpressions element
	 * 
	 * @return AdviceExpressions element
	 */
	public final AdviceExpressions getAdviceExpressions()
	{
		return jaxbAdviceExpressions;
	}

	protected static PepActions evaluate(List<ObligationExpression> obligationExpList, List<AdviceExpression> adviceExpList, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final List<Obligation> obligations;
		if (obligationExpList.isEmpty())
		{
			obligations = null;
		} else
		{
			obligations = new ArrayList<>();
			for (final ObligationExpression obligationExp : obligationExpList)
			{
				final Obligation obligation;
				try
				{
					obligation = obligationExp.evaluate(context);
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("ObligationExpression[@ObligationId={}] -> {}", obligationExp.getObligationId(), obligation);
					}
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException("Error evaluating one of the ObligationExpression[@ObligationId=" + obligationExp.getObligationId() + "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
				}

				obligations.add(obligation);
			}
		}

		final List<Advice> advices;
		if (adviceExpList.isEmpty())
		{
			advices = null;
		} else
		{
			advices = new ArrayList<>();
			for (final AdviceExpression adviceExp : adviceExpList)
			{
				final Advice advice;
				try
				{
					advice = adviceExp.evaluate(context);
				} catch (IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException("Error evaluating one of the AdviceExpression[@AdviceId=" + adviceExp.getAdviceId() + "]/AttributeAssignmentExpression/Expression elements", e.getStatusCode(), e);
				}
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("AdviceExpression[@AdviceId={}] -> {}", adviceExp.getAdviceId(), advice);
				}

				advices.add(advice);
			}
		}

		return obligations == null && advices == null ? null : new PepActions(obligations, advices);
	}

}
