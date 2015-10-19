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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Represents the TargetType XML type in XACML.
 * 
 */
public class TargetEvaluator extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Target
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
	 * @throws ParsingException
	 */
	public TargetEvaluator(oasis.names.tc.xacml._3_0.core.schema.wd_17.Target jaxbTarget, XPathCompiler xPathCompiler, Expression.Factory expFactory) throws ParsingException
	{
		final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf> jaxbAnyOfList = jaxbTarget.getAnyOves();
		if (jaxbAnyOfList.isEmpty())
		{
			evaluatableAnyOfList = null;
			// make the super field anyOves unmodifiable to prevent inconsistency with
			// evaluatableAnyOfList
			this.anyOves = Collections.EMPTY_LIST;
			return;
		}

		evaluatableAnyOfList = new ArrayList<>(jaxbAnyOfList.size());
		int childIndex = 0;
		for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf jaxbAnyOf : jaxbAnyOfList)
		{
			final AnyOfEvaluator anyOfEvaluator;
			try
			{
				anyOfEvaluator = new AnyOfEvaluator(jaxbAnyOf, xPathCompiler, expFactory);
			} catch (ParsingException e)
			{
				throw new ParsingException("Error parsing <Target>'s <AnyOf>#" + childIndex, e);
			}

			evaluatableAnyOfList.add(anyOfEvaluator);
			childIndex++;
		}

		this.anyOves = Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf> unmodifiableList(evaluatableAnyOfList);
	}

	/**
	 * Determines whether this <code>Target</code> matches the input request (whether it is
	 * applicable). If any of the AnyOf doesn't match the request context so it's a NO_MATCH result.
	 * Here is the table shown in the specification: <code> 
	 * 		<AnyOf> values 				<Target> value
	 * 		All “Match”					“Match”
	 * 		At Least one "No Match"		“No Match”
	 * 		Otherwise					“Indeterminate”
	 * </code> Also if Target empty (no AnyOf), return "Match"
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return true if and only if Match (else No-match)
	 * @throws IndeterminateEvaluationException
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
		throw new IndeterminateEvaluationException("Error evaluating <Target>'s <AnyOf>#" + lastIndeterminateChildIndex, lastIndeterminate.getStatusCode(), lastIndeterminate);
	}

}
