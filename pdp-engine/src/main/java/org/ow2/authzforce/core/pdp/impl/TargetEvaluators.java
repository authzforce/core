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
import java.util.List;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

/**
 * XACML Target evaluators.
 *
 * @version $Id: $
 */
public final class TargetEvaluators
{

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TargetEvaluators.class);

	/**
	 * Empty Target evaluator that always evaluates to True (match all requests)
	 */
	public static final BooleanEvaluator MATCH_ALL_TARGET_EVALUATOR = new BooleanEvaluator()
	{

		@Override
		public boolean evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			LOGGER.debug("Target null/empty -> True");
			return true;
		}
	};

	private static final class NonEmptyTargetEvaluator implements BooleanEvaluator
	{
		// Have a copy of AnyOf evaluators to avoid cast from JAXB AnyOf in
		// super JAXB type
		// non-null
		private final List<AnyOfEvaluator> anyOfEvaluatorList;

		private NonEmptyTargetEvaluator(final List<AnyOf> jaxbAnyOfList, final XPathCompiler xPathCompiler,
				final ExpressionFactory expFactory) throws IllegalArgumentException
		{
			assert jaxbAnyOfList != null && !jaxbAnyOfList.isEmpty();

			anyOfEvaluatorList = new ArrayList<>(jaxbAnyOfList.size());
			int childIndex = 0;
			for (final AnyOf jaxbAnyOf : jaxbAnyOfList)
			{
				final AnyOfEvaluator anyOfEvaluator;
				try
				{
					anyOfEvaluator = new AnyOfEvaluator(jaxbAnyOf.getAllOves(), xPathCompiler, expFactory);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Invalid <Target>'s <AnyOf>#" + childIndex, e);
				}

				anyOfEvaluatorList.add(anyOfEvaluator);
				childIndex++;
			}
		}

		/**
		 * Determines whether this <code>Target</code> matches the input request
		 * (whether it is applicable). If any of the AnyOf doesn't match the
		 * request context so it's a NO_MATCH result. Here is the table shown in
		 * the specification: <code>
		 * 		<AnyOf> values 				<Target> value
		 * 		All Match?					Match?
		 * 		At Least one "No Match"		No Match?
		 * 		Otherwise					Indeterminate?
		 * </code> Also if Target empty (no AnyOf), return "Match"
		 *
		 * @param context
		 *            the representation of the request
		 * @return true if and only if Match (else No-match)
		 * @throws org.ow2.authzforce.core.pdp.api.
		 *             IndeterminateEvaluationException if Indeterminate (error
		 *             evaluating target)
		 */
		@Override
		public boolean evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			// logic is quite similar to AllOf evaluation
			// at the end, lastIndeterminate == null iff no Indeterminate
			// occurred
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
						// Beware of autoboxing which causes call to
						// Boolean.valueOf(...), Integer.valueOf(...)
						LOGGER.debug("Target/AnyOf#{} -> {}", childIndex, isMatched);
					}
				}
				catch (final IndeterminateEvaluationException e)
				{
					if (LOGGER.isDebugEnabled())
					{
						// Beware of autoboxing which causes call to
						// Integer.valueOf(...)
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

			// No False but at least one Indeterminate (lastIndeterminate !=
			// null)
			throw new IndeterminateEvaluationException(
					"Error evaluating <Target>/<AnyOf>#" + lastIndeterminateChildIndex,
					lastIndeterminate.getStatusCode(), lastIndeterminate);
		}
	}

	/**
	 * Instantiates Target (evaluator) from XACML-Schema-derived
	 * <code>Target</code>.
	 *
	 * @param target
	 *            XACML-schema-derived JAXB Target element
	 * @param xPathCompiler
	 *            XPath compiler corresponding to enclosing policy(set) default
	 *            XPath version
	 * @param expressionFactory
	 *            Expression factory for parsing XACML Expressions
	 * @return instance of Target evaluator
	 * @throws java.lang.IllegalArgumentException
	 *             if target is not null/empty AND: either ({@code expFactory}
	 *             is null OR one of the Match elements in one of the
	 *             AnyOf/AllOf elements in {@code target} is invalid
	 */
	public static BooleanEvaluator getInstance(final Target target, final XPathCompiler xPathCompiler,
			final ExpressionFactory expressionFactory) throws IllegalArgumentException
	{
		if (target == null)
		{
			return MATCH_ALL_TARGET_EVALUATOR;
		}

		final List<AnyOf> anyOfs = target.getAnyOves();
		if (anyOfs == null || anyOfs.isEmpty())
		{
			return MATCH_ALL_TARGET_EVALUATOR;
		}

		return new NonEmptyTargetEvaluator(anyOfs, xPathCompiler, expressionFactory);
	}

	private TargetEvaluators()
	{
		// prevent instantiation
	}
}
