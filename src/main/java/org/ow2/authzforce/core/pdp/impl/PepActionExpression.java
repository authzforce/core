/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PEP action (obligation/advice) expression evaluator
 *
 * @param <JAXB_PEP_ACTION>
 *            PEP action type in XACML/JAXB model (Obligation/Advice)
 * @version $Id: $
 */
public final class PepActionExpression<JAXB_PEP_ACTION>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PepActionExpression.class);

	private String actionId;
	private final transient JAXB_PEP_ACTION emptyPepAction;
	private final transient List<AttributeAssignmentExpressionEvaluator> evaluatableAttributeAssignmentExpressions;

	private final PepActionFactory<JAXB_PEP_ACTION> pepActionFactory;

	private final String infoPrefix;

	private final EffectType appliesTo;

	/**
	 * Constructor that takes all the data associated with an PEP action (obligation/advice) expression.
	 *
	 * @param pepActionFactory
	 *            PEP action factory
	 * @param pepActionId
	 *            the obligation's id
	 * @param appliesTo
	 *            the type of decision to which the PEP action applies (ObligationExpression's FulfillOn / AdviceExpression's AppliesTo)
	 * @param jaxbAssignmentExps
	 *            a <code>List</code> of <code>AttributeAssignmentExpression</code>s
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws java.lang.IllegalArgumentException
	 *             one of the AttributeAssignmentExpressions' Expression is invalid
	 */
	public PepActionExpression(PepActionFactory<JAXB_PEP_ACTION> pepActionFactory, String pepActionId, EffectType appliesTo, List<AttributeAssignmentExpression> jaxbAssignmentExps,
			XPathCompiler xPathCompiler, ExpressionFactory expFactory) throws IllegalArgumentException
	{
		this.actionId = pepActionId;
		this.appliesTo = appliesTo;

		if (jaxbAssignmentExps == null || jaxbAssignmentExps.isEmpty())
		{
			emptyPepAction = pepActionFactory.getInstance(null, pepActionId);
			this.evaluatableAttributeAssignmentExpressions = Collections.emptyList();
		} else
		{
			emptyPepAction = null;
			this.evaluatableAttributeAssignmentExpressions = new ArrayList<>(jaxbAssignmentExps.size());
			for (final AttributeAssignmentExpression jaxbAttrAssignExp : jaxbAssignmentExps)
			{
				final AttributeAssignmentExpressionEvaluator attrAssignExp;
				try
				{
					attrAssignExp = new AttributeAssignmentExpressionEvaluator(jaxbAttrAssignExp, xPathCompiler, expFactory);
				} catch (IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid AttributeAssignmentExpression[@AttributeId=" + jaxbAttrAssignExp.getAttributeId() + "]/Expression", e);
				}

				this.evaluatableAttributeAssignmentExpressions.add(attrAssignExp);
			}
		}

		this.pepActionFactory = pepActionFactory;
		this.infoPrefix = pepActionFactory.getActionXmlElementName() + "Expression[@" + pepActionFactory.getActionXmlElementName() + "=" + actionId + "]";
	}

	/**
	 * The type of decision to which the PEP action applies (ObligationExpression's FulfillOn / AdviceExpression's AppliesTo)
	 *
	 * @return appliesTo/fulfillOn property
	 */
	public EffectType getAppliesTo()
	{
		return appliesTo;
	}

	/**
	 * ID of the PEP action (ObligationId / AdviceId)
	 *
	 * @return action ID
	 */
	public String getActionId()
	{
		return this.actionId;
	}

	/**
	 * Evaluates to a PEP action (obligation/advice).
	 *
	 * @param context
	 *            evaluation context
	 * @return an instance of a PEP action in JAXB model (JAXB Obligation/Advice)
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if any of the attribute assignment expressions evaluates to "Indeterminate" (see XACML 3.0 core spec, section 7.18)
	 */
	public JAXB_PEP_ACTION evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// if no assignmentExpression
		if (this.emptyPepAction != null)
		{
			return this.emptyPepAction;
		}

		// else there are assignmentExpressions
		final List<AttributeAssignment> assignments = new ArrayList<>();
		for (final AttributeAssignmentExpressionEvaluator attrAssignmentExpr : this.evaluatableAttributeAssignmentExpressions)
		{
			/*
			 * Section 5.39 of XACML 3.0 core spec says there may be multiple AttributeAssignments resulting from one AttributeAssignmentExpression
			 */
			final List<AttributeAssignment> attrAssignsFromExpr;
			try
			{
				attrAssignsFromExpr = attrAssignmentExpr.evaluate(context);
				LOGGER.debug("{}/{} -> {}", this.infoPrefix, attrAssignmentExpr, attrAssignsFromExpr);
			} catch (IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(infoPrefix + ": Error evaluating AttributeAssignmentExpression[@AttributeId=" + attrAssignmentExpr.getAttributeId() + "]/Expression",
						e.getStatusCode(), e);
			}

			assignments.addAll(attrAssignsFromExpr);
		}

		return pepActionFactory.getInstance(assignments, actionId);
	}
}
