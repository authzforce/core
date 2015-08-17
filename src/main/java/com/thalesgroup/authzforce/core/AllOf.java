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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * XACML AllOf
 * 
 */
public class AllOf extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AllOf.class);

	private static final IllegalArgumentException NO_MATCH_EXCEPTION = new IllegalArgumentException("<AllOf> empty. Must contain at least one <Match>");

	// Store the list of Matches as evaluatable Match types to avoid casting from JAXB MatchType
	// during evaluation
	private final List<Match> evaluatableMatchList = new ArrayList<>();

	/**
	 * Instantiates AllOf (evaluator) from XACML-Schema-derived <code>AllOf</code>.
	 * 
	 * @param jaxbAllOf
	 *            XACML-schema-derived JAXB AllOf
	 * @param policyDefaults
	 *            policy(set) default parameters, e.g. XPath version
	 * @param expFactory
	 *            Expression factory
	 * @throws ParsingException
	 */
	public AllOf(oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Match> jaxbMatches = jaxbAllOf.getMatches();
		if (jaxbMatches.isEmpty())
		{
			throw NO_MATCH_EXCEPTION;
		}

		int matchIndex = 0;
		for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.Match jaxbMatch : jaxbMatches)
		{
			final Match match;
			try
			{
				match = new Match(jaxbMatch, policyDefaults, expFactory);
			} catch (ParsingException e)
			{
				throw new ParsingException("Error parsing <AllOf>'s <Match>#" + matchIndex, e);
			}

			evaluatableMatchList.add(match);
			matchIndex++;
		}

		this.matches = Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.Match> unmodifiableList(evaluatableMatchList);
	}

	/**
	 * Determines whether this <code>AllOf</code> matches the input request (whether it is
	 * applicable).Here is the table shown in the specification: <code>
	 * 		<Match> values 						<AllOf> value 
	 * 		All True				 			“Match” 
	 * 		No False and at least 
	 * 		one "Indeterminate" 				“Indeterminate”
	 * 		At least one False					"No Match"
	 * </code>
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return true iff Match, else No match
	 * @throws IndeterminateEvaluationException
	 *             Indeterminate
	 */
	public boolean match(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// atLeastOneIndeterminate = true iff lastIndeterminate != null
		IndeterminateEvaluationException lastIndeterminate = null;

		// index of the current Match in this AllOf
		int childIndex = 0;

		// index of last Indeterminate for enhanced error message
		int lastIndeterminateChildIndex = -1;

		/*
		 * By construction, there must be at least one Match
		 */
		for (final Match match : evaluatableMatchList)
		{
			final boolean isMatched;
			try
			{
				isMatched = match.match(context);
				LOGGER.debug("AllOf/Match#{} -> {}", childIndex, isMatched);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.debug("AllOf/Match#{} -> Indeterminate", childIndex, e);
				lastIndeterminate = e;
				lastIndeterminateChildIndex = childIndex;
				continue;
			}

			/*
			 * At least one False -> No match
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
		throw new IndeterminateEvaluationException("Error evaluating <AllOf>'s <Match>#" + lastIndeterminateChildIndex, lastIndeterminate.getStatusCode(), lastIndeterminate);
	}
}