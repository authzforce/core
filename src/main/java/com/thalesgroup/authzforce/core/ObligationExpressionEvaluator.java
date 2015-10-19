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
package com.thalesgroup.authzforce.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Evaluates an ObligationExpression to an Obligation.
 * 
 */
public class ObligationExpressionEvaluator extends oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ObligationExpressionEvaluator.class);
	private static final UnsupportedOperationException UNSUPPORTED_SET_FULFILL_ON_OPERATION_EXCEPTION = new UnsupportedOperationException("Unsupported operation: ObligationExpression/FulfillOn attribute is read-only");

	private final transient Obligation nullAssignmentsObligation;
	private final transient List<AttributeAssignmentExpressionEvaluator> evaluatableAttributeAssignmentExpressions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression#setFulfillOn(oasis.names
	 * .tc.xacml._3_0.core.schema.wd_17.EffectType)
	 */
	@Override
	public final void setFulfillOn(EffectType fulfillOn)
	{
		/*
		 * Make fulfillOn field read-only because this ObligationExpression is selected or ignored
		 * by parent Rule/Policy evaluator based on 'fulfillOn' field once and for all at
		 * initialization time. See PolicyPepActionExpressionsEvaluator and
		 * RulePepActionExpressionsEvaluator classes.
		 */
		throw UNSUPPORTED_SET_FULFILL_ON_OPERATION_EXCEPTION;
	}

	/**
	 * Instantiates Obligation expression from JAXB equivalent model in XACML
	 * 
	 * @param jaxbObligationExp
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws ParsingException
	 *             error parsing one of AttributeAssignmentExpressions' Expression
	 */
	public ObligationExpressionEvaluator(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression jaxbObligationExp, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws ParsingException
	{
		this(jaxbObligationExp.getObligationId(), jaxbObligationExp.getFulfillOn(), jaxbObligationExp.getAttributeAssignmentExpressions(), xPathCompiler, expFactory);
	}

	/**
	 * Constructor that takes all the data associated with an obligation expression.
	 * 
	 * @param id
	 *            the obligation's id
	 * @param fulfillOn
	 *            the effect denoting when to fulfill this obligation
	 * @param jaxbAssignmentExps
	 *            a <code>List</code> of <code>AttributeAssignmentExpression</code>s
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws ParsingException
	 *             error parsing one of the AttributeAssignmentExpressions' Expression
	 */
	public ObligationExpressionEvaluator(String id, EffectType fulfillOn, List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression> jaxbAssignmentExps, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws ParsingException
	{
		this.obligationId = id;
		this.fulfillOn = fulfillOn;

		if (jaxbAssignmentExps == null || jaxbAssignmentExps.isEmpty())
		{
			nullAssignmentsObligation = new Obligation(null, id);
			this.evaluatableAttributeAssignmentExpressions = Collections.EMPTY_LIST;
		} else
		{
			nullAssignmentsObligation = null;
			this.evaluatableAttributeAssignmentExpressions = new ArrayList<>(jaxbAssignmentExps.size());
			for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression jaxbAttrAssignExp : jaxbAssignmentExps)
			{
				final AttributeAssignmentExpressionEvaluator attrAssignExp;
				try
				{
					attrAssignExp = new AttributeAssignmentExpressionEvaluator(jaxbAttrAssignExp, xPathCompiler, expFactory);
				} catch (ParsingException e)
				{
					throw new ParsingException("Error parsing AttributeAssignmentExpression[@AttributeId=" + jaxbAttrAssignExp.getAttributeId() + "]/Expression", e);
				}

				this.evaluatableAttributeAssignmentExpressions.add(attrAssignExp);
			}
		}

		/*
		 * set JAXB field 'attributeAssignmentExpressions' immutable to avoid inconsistency with
		 * evaluatableAttributeAssignmentExpressions
		 */
		this.attributeAssignmentExpressions = Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression> unmodifiableList(this.evaluatableAttributeAssignmentExpressions);
	}

	// /**
	// * Evaluates to an Obligation with a given decision to match fulfillOn
	// *
	// * @param matchDecision
	// * decision for this obligation to apply
	// * @param context
	// * evaluation context
	// * @return the obligation if its fullfillOn applies to <code>matchDecision</code>, null if not
	// * @throws IndeterminateEvaluationException
	// * if any of the attribute assignment expressions evaluates to "Indeterminate" (see
	// * XACML 3.0 core spec, section 7.18)
	// */
	// public Obligation evaluate(DecisionType matchDecision, EvaluationContext context) throws
	// IndeterminateEvaluationException
	// {
	// if (this.fulfillOnAsDecision != matchDecision)
	// {
	// return null;
	// }
	//
	// return evaluate(context);
	// }

	/**
	 * Evaluates to an <code>Obligation</code> regardless of fulfillOn.
	 * 
	 * @param context
	 *            evaluation context
	 * 
	 * @return an instance of an obligation
	 * 
	 * @throws IndeterminateEvaluationException
	 *             if any of the attribute assignment expressions evaluates to "Indeterminate" (see
	 *             XACML 3.0 core spec, section 7.18)
	 */
	public Obligation evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// if no assignmentExpression
		if (nullAssignmentsObligation != null)
		{
			return nullAssignmentsObligation;
		}

		// else there are assignmentExpressions
		final List<AttributeAssignment> assignments = new ArrayList<>();
		for (final AttributeAssignmentExpressionEvaluator attrAssignmentExpr : this.evaluatableAttributeAssignmentExpressions)
		{
			/*
			 * Section 5.39 of XACML 3.0 core spec says there may be multiple AttributeAssignments
			 * resulting from one AttributeAssignmentExpression
			 */
			final List<AttributeAssignment> attrAssignsFromExpr;
			try
			{
				attrAssignsFromExpr = attrAssignmentExpr.evaluate(context);
				LOGGER.debug("ObligationExpression[@ObligationId={}]/{} -> {}", this.obligationId, attrAssignmentExpr, attrAssignsFromExpr);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Error evaluating AttributeAssignmentExpression[@AttributeId=" + attrAssignmentExpr.getAttributeId() + "]/Expression", e.getStatusCode(), e);
			}

			if (attrAssignsFromExpr != null)
			{
				assignments.addAll(attrAssignsFromExpr);
			}
		}

		return new Obligation(assignments, this.obligationId);
	}

}
