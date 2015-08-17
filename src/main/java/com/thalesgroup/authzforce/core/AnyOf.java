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
/**
 * 
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
 * AnyOf evaluator
 * 
 */
public class AnyOf extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AnyOf.class);

	private static final IllegalArgumentException NO_ALL_OF_EXCEPTION = new IllegalArgumentException("<AnyOf> empty. Must contain at least one <AllOf>");

	// Store the list of AllOf as evaluatable AllOf types to avoid casting from JAXB AllOfType
	// during evaluation
	private final List<AllOf> evaluatableAllOfList = new ArrayList<>();

	/**
	 * Constructor that creates a new <code>AnyOf</code> evaluator based on the given
	 * XACML-schema-derived JAXB AnyOf.
	 * 
	 * @param jaxbAnyOf
	 *            JAXB AnyOf
	 * @param policyDefaults
	 *            enclosing policy(set) default parameters, e.g. XPath version
	 * @param expFactory
	 *            Expression factory
	 * 
	 * @throws ParsingException
	 *             if AnyOf element is invalid
	 */
	public AnyOf(oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf jaxbAnyOf, DefaultsType policyDefaults, ExpressionFactory expFactory) throws ParsingException
	{
		final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf> jaxbAllOfList = jaxbAnyOf.getAllOves();
		if (jaxbAllOfList.isEmpty())
		{
			throw NO_ALL_OF_EXCEPTION;
		}

		int matchIndex = 0;
		for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : jaxbAllOfList)
		{
			final AllOf allOf;
			try
			{
				allOf = new AllOf(jaxbAllOf, policyDefaults, expFactory);
			} catch (ParsingException e)
			{
				throw new ParsingException("Error parsing <AllOf>'s <Match>#" + matchIndex, e);
			}

			evaluatableAllOfList.add(allOf);
			matchIndex++;
		}

		this.allOves = Collections.<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf> unmodifiableList(evaluatableAllOfList);
	}

	/**
	 * Determines whether this <code>AnyOf</code> matches the input request (whether it is
	 * applicable). If all the AllOf values is No_Match so it's a No_Match. If all matches it's a
	 * Match. If None matches and at least one “Indeterminate” it's Indeterminate
	 * 
	 * <pre>
	 * 		AllOf values 						AnyOf value 
	 * 		At Least one "Match"	 			“Match” 
	 * 		None matches and 
	 * 		at least one Indeterminate 			“Indeterminate”
	 * 		All "No Match"						"No Match"
	 * </pre>
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return true if and only if Match (else No-match)
	 * @throws IndeterminateEvaluationException
	 *             if Indeterminate
	 */
	public boolean match(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// atLeastOneIndeterminate = true iff lastIndeterminate != null
		IndeterminateEvaluationException lastIndeterminate = null;

		// index of the current AllOf in this AnyOf
		int childIndex = 0;

		// index of last Indeterminate for enhanced error message
		int lastIndeterminateChildIndex = -1;

		/*
		 * By construction, there must be at least one AllOf
		 */
		for (final AllOf allOf : evaluatableAllOfList)
		{
			final boolean isMatched;
			try
			{
				isMatched = allOf.match(context);
				LOGGER.debug("AnyOf/AllOf#{} -> {}", childIndex, isMatched);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.debug("AnyOf/AllOf#{} -> Indeterminate", childIndex, e);
				lastIndeterminate = e;
				lastIndeterminateChildIndex = childIndex;
				continue;
			}

			/*
			 * At least one Match -> Match
			 */
			if (isMatched)
			{
				return true;
			}

			// No Match -> continue, all must be true to match
			childIndex += 1;
		}

		// No True (Match) occurred
		// lastIndeterminate == null iff no Indeterminate occurred
		if (lastIndeterminate == null)
		{
			// No True/Indeterminate, i.e. all "No match" -> No match
			return false;
		}

		// No Match and at least one Indeterminate (lastIndeterminate != null) -> Indeterminate
		throw new IndeterminateEvaluationException("Error evaluating <AnyOf>'s <AllOf>#" + lastIndeterminateChildIndex, lastIndeterminate.getStatusCode(), lastIndeterminate);
	}

}
