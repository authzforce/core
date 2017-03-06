/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepActions;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;

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

	private final String actionId;
	private final transient JAXB_PEP_ACTION emptyPepAction;
	private final transient List<AttributeAssignmentExpressionEvaluator> evaluatableAttributeAssignmentExpressions;

	private final PepActions.Factory<JAXB_PEP_ACTION> pepActionFactory;

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
	 *            the type of decision to which the PEP action applies (ObligationExpression's FulfillOn /
	 *            AdviceExpression's AppliesTo)
	 * @param jaxbAssignmentExps
	 *            a <code>List</code> of <code>AttributeAssignmentExpression</code>s
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws java.lang.IllegalArgumentException
	 *             one of the AttributeAssignmentExpressions' Expression is invalid
	 */
	public PepActionExpression(final PepActions.Factory<JAXB_PEP_ACTION> pepActionFactory, final String pepActionId,
			final EffectType appliesTo, final List<AttributeAssignmentExpression> jaxbAssignmentExps,
			final XPathCompiler xPathCompiler, final ExpressionFactory expFactory) throws IllegalArgumentException
	{
		this.actionId = pepActionId;
		this.appliesTo = appliesTo;

		if (jaxbAssignmentExps == null || jaxbAssignmentExps.isEmpty())
		{
			emptyPepAction = pepActionFactory.getInstance(null, pepActionId);
			this.evaluatableAttributeAssignmentExpressions = Collections.emptyList();
		}
		else
		{
			emptyPepAction = null;
			this.evaluatableAttributeAssignmentExpressions = new ArrayList<>(jaxbAssignmentExps.size());
			for (final AttributeAssignmentExpression jaxbAttrAssignExp : jaxbAssignmentExps)
			{
				final AttributeAssignmentExpressionEvaluator attrAssignExp;
				try
				{
					attrAssignExp = new AttributeAssignmentExpressionEvaluator(jaxbAttrAssignExp, xPathCompiler,
							expFactory);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid AttributeAssignmentExpression[@AttributeId="
							+ jaxbAttrAssignExp.getAttributeId() + "]/Expression", e);
				}

				this.evaluatableAttributeAssignmentExpressions.add(attrAssignExp);
			}
		}

		this.pepActionFactory = pepActionFactory;
		this.infoPrefix = pepActionFactory.getActionXmlElementName() + "Expression[@"
				+ pepActionFactory.getActionXmlElementName() + "=" + actionId + "]";
	}

	/**
	 * The type of decision to which the PEP action applies (ObligationExpression's FulfillOn / AdviceExpression's
	 * AppliesTo)
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
	 *             if any of the attribute assignment expressions evaluates to "Indeterminate" (see XACML 3.0 core spec,
	 *             section 7.18)
	 */
	public JAXB_PEP_ACTION evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
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
			 * Section 5.39 of XACML 3.0 core spec says there may be multiple AttributeAssignments resulting from one
			 * AttributeAssignmentExpression
			 */
			final List<AttributeAssignment> attrAssignsFromExpr;
			try
			{
				attrAssignsFromExpr = attrAssignmentExpr.evaluate(context);
				LOGGER.debug("{}/{} -> {}", this.infoPrefix, attrAssignmentExpr, attrAssignsFromExpr);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(
						infoPrefix + ": Error evaluating " + attrAssignmentExpr + "/Expression", e.getStatusCode(), e);
			}

			assignments.addAll(attrAssignsFromExpr);
		}

		return pepActionFactory.getInstance(assignments, actionId);
	}
}
