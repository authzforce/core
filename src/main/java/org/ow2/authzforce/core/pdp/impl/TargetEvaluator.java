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
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the TargetType XML type in XACML.
 *
 * @author cdangerv
 * @version $Id: $
 */
public class TargetEvaluator
{

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TargetEvaluator.class);

	// Have a copy of evaluatable AnyOfs to avoid cast from JAXB AnyOf in super JAXB type
	private final transient List<AnyOfEvaluator> evaluatableAnyOfList;

	/**
	 * Instantiates Target (evaluator) from XACML-Schema-derived <code>Target</code>.
	 *
	 * @param jaxbTarget
	 *            XACML-schema-derived JAXB Target
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the child AnyOf elements is invalid
	 */
	public TargetEvaluator(Target jaxbTarget, XPathCompiler xPathCompiler, ExpressionFactory expFactory) throws IllegalArgumentException
	{
		final List<AnyOf> jaxbAnyOfList = jaxbTarget.getAnyOves();
		if (jaxbAnyOfList.isEmpty())
		{
			evaluatableAnyOfList = null;
			return;
		}

		evaluatableAnyOfList = new ArrayList<>(jaxbAnyOfList.size());
		int childIndex = 0;
		for (final AnyOf jaxbAnyOf : jaxbAnyOfList)
		{
			final AnyOfEvaluator anyOfEvaluator;
			try
			{
				anyOfEvaluator = new AnyOfEvaluator(jaxbAnyOf, xPathCompiler, expFactory);
			} catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid <Target>'s <AnyOf>#" + childIndex, e);
			}

			evaluatableAnyOfList.add(anyOfEvaluator);
			childIndex++;
		}
	}

	/**
	 * Determines whether this <code>Target</code> matches the input request (whether it is applicable). If any of the AnyOf doesn't match the request context
	 * so it's a NO_MATCH result. Here is the table shown in the specification: <code>
	 * 		<AnyOf> values 				<Target> value
	 * 		All Match?					Match?
	 * 		At Least one "No Match"		No Match?
	 * 		Otherwise					Indeterminate?
	 * </code> Also if Target empty (no AnyOf), return "Match"
	 *
	 * @param context
	 *            the representation of the request
	 * @return true if and only if Match (else No-match)
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if Indetermiante (error evaluating target)
	 */
	public boolean match(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// Target empty matches all
		if (evaluatableAnyOfList == null)
		{
			return true;
		}

		// logic is quite similar to AllOf evaluation
		// at the end, lastIndeterminate == null iff no Indeterminate occurred
		IndeterminateEvaluationException lastIndeterminate = null;

		// index of the current AnyOf in this Target
		int childIndex = 0;

		// index of last Indeterminate for enhanced error message
		int lastIndeterminateChildIndex = -1;

		/*
		 * By construction, there must be at least one Match
		 */
		for (final AnyOfEvaluator anyOfEvaluator : evaluatableAnyOfList)
		{
			final boolean isMatched;
			try
			{
				isMatched = anyOfEvaluator.match(context);
				LOGGER.debug("Target/AnyOf#{} -> {}", childIndex, isMatched);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.debug("Target/AnyOf#{} -> Indeterminate", childIndex, e);
				lastIndeterminate = e;
				lastIndeterminateChildIndex = childIndex;
				continue;
			}

			/*
			 * At least one False ("No match") -> No match
			 */
			if (!isMatched)
			{
				return false;
			}

			// True (Match) -> continue, all must be true to match
			childIndex += 1;
		}

		// No False (=NO_MATCH) occurred
		// lastIndeterminate == null iff no Indeterminate occurred
		if (lastIndeterminate == null)
		{
			// No False/Indeterminate, i.e. all True -> Match
			return true;
		}

		// No False but at least one Indeterminate (lastIndeterminate != null)
		throw new IndeterminateEvaluationException("Error evaluating <Target>/<AnyOf>#" + lastIndeterminateChildIndex, lastIndeterminate.getStatusCode(),
				lastIndeterminate);
	}

}
