/*
 * Copyright 2012-2024 THALES.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepAction;
import org.ow2.authzforce.core.pdp.api.PepActionAttributeAssignment;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * PEP action (obligation/advice) expression evaluator
 *
 * @version $Id: $
 */
public final class PepActionExpression
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PepActionExpression.class);

	private final String actionId;

	private final boolean isMandatory;

	private final List<AttributeAssignmentExpressionEvaluator> evaluableAttributeAssignmentExpressions;

	private transient final String toString;

	/**
	 * Constructor that takes all the data associated with an PEP action (obligation/advice) expression.
	 *
	 * @param pepActionId
	 *            the obligation's id
	 * @param isMandatory
	 *            true iff the PEP action is mandatory (XACML Obligation, as opposed to Advice)
	 * @param jaxbAssignmentExps
	 *            a <code>List</code> of <code>AttributeAssignmentExpression</code>s
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory for parsing/instantiating AttributeAssignment expressions
	 * @throws java.lang.IllegalArgumentException
	 *             one of the AttributeAssignmentExpressions' Expression is invalid
	 */
	public PepActionExpression(final String pepActionId, final boolean isMandatory, final List<AttributeAssignmentExpression> jaxbAssignmentExps,
	        final ExpressionFactory expFactory, final Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException
	{
		Preconditions.checkArgument(pepActionId != null, "Undefined PEP action (obligation/advice) ID");
		this.actionId = pepActionId;
		this.isMandatory = isMandatory;
		this.toString = (isMandatory ? "Obligation " : "Advice ") + "'" + actionId + "'";

		if (jaxbAssignmentExps == null || jaxbAssignmentExps.isEmpty())
		{
			this.evaluableAttributeAssignmentExpressions = Collections.emptyList();
		}
		else
		{
			this.evaluableAttributeAssignmentExpressions = new ArrayList<>(jaxbAssignmentExps.size());
			for (final AttributeAssignmentExpression jaxbAttrAssignExp : jaxbAssignmentExps)
			{
				final AttributeAssignmentExpressionEvaluator attrAssignExp;
				try
				{
					attrAssignExp = new AttributeAssignmentExpressionEvaluator(jaxbAttrAssignExp, expFactory, xPathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid " + toString + ": Invalid AttributeAssignmentExpression[@AttributeId=" + jaxbAttrAssignExp.getAttributeId() + "]", e);
				}

				this.evaluableAttributeAssignmentExpressions.add(attrAssignExp);
			}
		}
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

	@Override
	public String toString()
	{
		return toString;
	}

	/**
	 * Evaluates to a PEP action (obligation/advice).
	 *
	 * @param context
	 *            Individual Decision evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used.
	 * @return an instance of a PEP action in JAXB model (JAXB Obligation/Advice)
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if any of the attribute assignment expressions evaluates to "Indeterminate" (see XACML 3.0 core spec, section 7.18)
	 */
	public PepAction evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
	{
		// else there are assignmentExpressions
		final List<PepActionAttributeAssignment<?>> assignments = new ArrayList<>();
		for (final AttributeAssignmentExpressionEvaluator attrAssignmentExpr : this.evaluableAttributeAssignmentExpressions)
		{
			/*
			 * Section 5.39 of XACML 3.0 core spec says there may be multiple AttributeAssignments resulting from one AttributeAssignmentExpression
			 */
			final Collection<PepActionAttributeAssignment<?>> attrAssignsFromExpr;
			try
			{
				attrAssignsFromExpr = attrAssignmentExpr.evaluate(context, mdpContext);
				LOGGER.debug("{}/{} -> {}", this, attrAssignmentExpr, attrAssignsFromExpr);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this + ": Error evaluating " + attrAssignmentExpr, e);
			}

			assignments.addAll(attrAssignsFromExpr);
		}

		return new PepAction(actionId, isMandatory, ImmutableList.copyOf(assignments));
	}
}
