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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf;

/**
 * AnyOf evaluator
 *
 * @version $Id: $
 */
public final class AnyOfEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AnyOfEvaluator.class);

	private static final IllegalArgumentException NO_ALL_OF_EXCEPTION = new IllegalArgumentException(
			"<AnyOf> empty. Must contain at least one <AllOf>");

	// Store the list of AllOf as evaluatable AllOf types to avoid casting from
	// JAXB AllOfType
	// during evaluation
	private final transient List<AllOfEvaluator> evaluatableAllOfList;

	/**
	 * Constructor that creates a new <code>AnyOf</code> evaluator based on the
	 * given XACML-schema-derived JAXB AnyOf.
	 *
	 * @param jaxbAllOfList
	 *            JAXB AllOf elements
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default
	 *            XPath version
	 * @param expFactory
	 *            Expression factory
	 * @throws java.lang.IllegalArgumentException
	 *             null {@code expFactory} or null/empty {@code jaxbAllOfList}
	 *             or one of the child Match elements in one of the AllOf
	 *             elements of {@code jaxbAllOfList} is invalid
	 */
	public AnyOfEvaluator(final List<AllOf> jaxbAllOfList, final XPathCompiler xPathCompiler,
			final ExpressionFactory expFactory) throws IllegalArgumentException
	{
		if (jaxbAllOfList == null || jaxbAllOfList.isEmpty())
		{
			throw NO_ALL_OF_EXCEPTION;
		}

		this.evaluatableAllOfList = new ArrayList<>(jaxbAllOfList.size());
		int matchIndex = 0;
		for (final AllOf jaxbAllOf : jaxbAllOfList)
		{
			final AllOfEvaluator allOfEvaluator;
			try
			{
				allOfEvaluator = new AllOfEvaluator(jaxbAllOf.getMatches(), xPathCompiler, expFactory);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid <AnyOf>'s <AllOf>#" + matchIndex, e);
			}

			evaluatableAllOfList.add(allOfEvaluator);
			matchIndex++;
		}
	}

	/**
	 * Determines whether this <code>AnyOf</code> matches the input request
	 * (whether it is applicable). If all the AllOf values is No_Match so it's a
	 * No_Match. If all matches it's a Match. If None matches and at least one
	 * “Indeterminate�? it's Indeterminate
	 *
	 * <pre>
	 * 		AllOf values 						AnyOf value
	 * 		At Least one "Match"	 			“Match�?
	 * 		None matches and
	 * 		at least one Indeterminate 			“Indeterminate�?
	 * 		All "No Match"						"No Match"
	 * </pre>
	 *
	 * @param context
	 *            the representation of the request
	 * @return true if and only if Match (else No-match)
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if Indeterminate
	 */
	public boolean match(final EvaluationContext context) throws IndeterminateEvaluationException
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
		for (final AllOfEvaluator allOfEvaluator : evaluatableAllOfList)
		{
			final boolean isMatched;
			try
			{
				isMatched = allOfEvaluator.match(context);
				if (LOGGER.isDebugEnabled())
				{
					// Beware of autoboxing which causes call to
					// Boolean.valueOf(...), Integer.valueOf(...)
					LOGGER.debug("AnyOf/AllOf#{} -> {}", childIndex, isMatched);
				}
			}
			catch (final IndeterminateEvaluationException e)
			{
				if (LOGGER.isDebugEnabled())
				{
					// Beware of autoboxing which causes call to
					// Integer.valueOf(...)
					LOGGER.debug("AnyOf/AllOf#{} -> Indeterminate", childIndex, e);
				}
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

		// No Match and at least one Indeterminate (lastIndeterminate != null)
		// -> Indeterminate
		throw new IndeterminateEvaluationException("Error evaluating <AnyOf>'s <AllOf>#" + lastIndeterminateChildIndex,
				lastIndeterminate.getStatusCode(), lastIndeterminate);
	}

}
