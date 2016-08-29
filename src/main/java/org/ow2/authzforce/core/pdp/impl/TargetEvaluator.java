/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
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
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;

/**
 * Represents the TargetType XML type in XACML.
 *
 * @version $Id: $
 */
public final class TargetEvaluator implements BooleanEvaluator
{

	private static final IllegalArgumentException NULL_OR_EMPTY_XACML_ANYOF_LIST_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Cannot create Target evaluator: no input XACML/JAXB AnyOf element");

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TargetEvaluator.class);

	// Have a copy of AnyOf evaluators to avoid cast from JAXB AnyOf in super JAXB type
	// non-null
	private final List<AnyOfEvaluator> anyOfEvaluatorList;

	/**
	 * Instantiates Target (evaluator) from XACML-Schema-derived <code>Target</code>.
	 *
	 * @param jaxbAnyOfList
	 *            XACML-schema-derived JAXB AnyOf elements
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default XPath version
	 * @param expFactory
	 *            Expression factory
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the child AnyOf elements is invalid
	 */
	public TargetEvaluator(List<AnyOf> jaxbAnyOfList, XPathCompiler xPathCompiler, ExpressionFactory expFactory)
			throws IllegalArgumentException
	{
		if (jaxbAnyOfList == null || jaxbAnyOfList.isEmpty())
		{
			throw NULL_OR_EMPTY_XACML_ANYOF_LIST_ARGUMENT_EXCEPTION;
		}

		anyOfEvaluatorList = new ArrayList<>(jaxbAnyOfList.size());
		int childIndex = 0;
		for (final AnyOf jaxbAnyOf : jaxbAnyOfList)
		{
			final AnyOfEvaluator anyOfEvaluator;
			try
			{
				anyOfEvaluator = new AnyOfEvaluator(jaxbAnyOf.getAllOves(), xPathCompiler, expFactory);
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid <Target>'s <AnyOf>#" + childIndex, e);
			}

			anyOfEvaluatorList.add(anyOfEvaluator);
			childIndex++;
		}
	}

	/**
	 * Determines whether this <code>Target</code> matches the input request (whether it is applicable). If any of the
	 * AnyOf doesn't match the request context so it's a NO_MATCH result. Here is the table shown in the specification:
	 * <code>
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
	 *             if Indeterminate (error evaluating target)
	 */
	@Override
	public boolean evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
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
		for (final AnyOfEvaluator anyOfEvaluator : anyOfEvaluatorList)
		{
			final boolean isMatched;
			try
			{
				isMatched = anyOfEvaluator.match(context);
				if (LOGGER.isDebugEnabled())
				{
					// Beware of autoboxing which causes call to Boolean.valueOf(...), Integer.valueOf(...)
					LOGGER.debug("Target/AnyOf#{} -> {}", childIndex, isMatched);
				}
			}
			catch (IndeterminateEvaluationException e)
			{
				if (LOGGER.isDebugEnabled())
				{
					// Beware of autoboxing which causes call to Integer.valueOf(...)
					LOGGER.debug("Target/AnyOf#{} -> Indeterminate", childIndex, e);
				}
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
		throw new IndeterminateEvaluationException("Error evaluating <Target>/<AnyOf>#" + lastIndeterminateChildIndex,
				lastIndeterminate.getStatusCode(), lastIndeterminate);
	}

}
