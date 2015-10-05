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
package com.thalesgroup.authzforce.core.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Represents the AdviceExpression XML type in XACML that can be evaluated to an Advice. The
 * AppliesTo field is read-only.
 * 
 */
public class AdviceExpression extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AdviceExpression.class);
	private static final UnsupportedOperationException UNSUPPORTED_SET_APPLIES_TO_OPERATION_EXCEPTION = new UnsupportedOperationException("Unsupported operation: AdviceExpression/AppliesTo attribute is read-only");

	// private final DecisionType appliesToAsDecision;
	private final Advice nullAssignmentsAdvice;
	private final List<AttributeAssignmentExpression> evaluatableAttributeAssignmentExpressions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression#setAppliesTo(oasis.names.tc.
	 * xacml._3_0.core.schema.wd_17.EffectType)
	 */
	@Override
	public final void setAppliesTo(EffectType value)
	{
		/*
		 * Make fulfillOn field read-only because this ObligationExpression is selected or ignored
		 * by parent Rule/Policy evaluator based on 'fulfillOn' field once and for all at
		 * initialization time. See PolicyPepActionExpressionsEvaluator and
		 * RulePepActionExpressionsEvaluator classes.
		 */
		throw UNSUPPORTED_SET_APPLIES_TO_OPERATION_EXCEPTION;
	}

	/**
	 * Instantiates Advice expression from JAXB equivalent model in XACML
	 * 
	 * @param jaxbAdviceExp
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws ParsingException
	 *             error parsing one of the AttributeAssignmentExpressions' Expression
	 */
	public AdviceExpression(oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression jaxbAdviceExp, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws ParsingException
	{
		this(jaxbAdviceExp.getAdviceId(), jaxbAdviceExp.getAppliesTo(), jaxbAdviceExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
	}

	/**
	 * Constructor that takes all the data associated with an advice expression.
	 * 
	 * @param id
	 *            the advice id
	 * @param appliesTo
	 *            the effect denoting when to apply this advice
	 * @param jaxbAssignmentExps
	 *            a <code>List</code> of <code>AttributeAssignmentExpression</code>s
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws ParsingException
	 *             error parsing one of the AttributeAssignmentExpressions' Expression
	 */
	public AdviceExpression(String id, EffectType appliesTo, List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression> jaxbAssignmentExps, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws ParsingException
	{
		this.adviceId = id;

		this.appliesTo = appliesTo;

		if (jaxbAssignmentExps == null || jaxbAssignmentExps.isEmpty())
		{
			this.nullAssignmentsAdvice = new Advice(null, id);
			evaluatableAttributeAssignmentExpressions = null;
		} else
		{
			this.nullAssignmentsAdvice = null;
			evaluatableAttributeAssignmentExpressions = new ArrayList<>();
			for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression jaxbAttrAssignExp : jaxbAssignmentExps)
			{
				final AttributeAssignmentExpression attrAssignExp;
				try
				{
					attrAssignExp = new AttributeAssignmentExpression(jaxbAttrAssignExp, xPathCompiler, expFactory);
				} catch (ParsingException e)
				{
					throw new ParsingException("Error parsing AttributeAssignmentExpression[@AttributeId=" + jaxbAttrAssignExp.getAttributeId() + "]/Expression", e);
				}

				this.evaluatableAttributeAssignmentExpressions.add(attrAssignExp);
			}
		}

		/*
		 * set JAXB field immutable to avoid inconsistency with
		 * evaluatableAttributeAssignmentExpressions
		 */
		this.attributeAssignmentExpressions = Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression> unmodifiableList(this.evaluatableAttributeAssignmentExpressions);
	}

	// /**
	// * Evaluates to an Advice with a given decision to match appliesTo
	// *
	// * @param matchDecision
	// * decision for this advice to apply
	// * @param context
	// * evaluation context
	// * @return the advice if its appliesTo applies to <code>matchDecision</code>, null if not
	// * @throws IndeterminateEvaluationException
	// * if any of the attribute assignment expressions evaluates to "Indeterminate" (see
	// * XACML 3.0 core spec, section 7.18)
	// */
	// public Advice evaluate(DecisionType matchDecision, EvaluationContext context) throws
	// IndeterminateEvaluationException
	// {
	// if (this.appliesToAsDecision != matchDecision)
	// {
	// return null;
	// }
	//
	// return evaluate(context);
	// }

	/**
	 * Evaluates to an <code>Advice</code> regardless of AppliesTo.
	 * 
	 * @param context
	 *            evaluation context
	 * 
	 * @return an instance of an advice
	 * 
	 * @throws IndeterminateEvaluationException
	 *             if any of the attribute assignment expressions evaluates to "Indeterminate" (see
	 *             XACML 3.0 core spec, section 7.18)
	 */
	public Advice evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// if no assignmentExpression
		if (this.nullAssignmentsAdvice != null)
		{
			return nullAssignmentsAdvice;
		}

		// else there are assignmentExpressions
		final List<AttributeAssignment> assignments = new ArrayList<>();
		for (final AttributeAssignmentExpression attrAssignmentExpr : this.evaluatableAttributeAssignmentExpressions)
		{
			/*
			 * Section 5.39 of XACML 3.0 core spec says there may be multiple AttributeAssignments
			 * resulting from one AttributeAssignmentExpression
			 */
			final List<AttributeAssignment> attrAssignsFromExpr;
			try
			{
				attrAssignsFromExpr = attrAssignmentExpr.evaluate(context);
				LOGGER.debug("{} -> {}", attrAssignmentExpr, attrAssignsFromExpr);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Error evaluating AttributeAssignmentExpression[@AttributeId=" + attrAssignmentExpr.getAttributeId() + "]/Expression", e.getStatusCode(), e);
			}

			if (attrAssignsFromExpr != null)
			{
				assignments.addAll(attrAssignsFromExpr);
			}
		}

		return new Advice(assignments, this.adviceId);
	}

}
