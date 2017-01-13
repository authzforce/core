/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.ow2.authzforce.core.pdp.api.BaseRequestFilter;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.JaxbXACMLAttributesParser;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

/**
 * Request filter implementing Multiple Decision Profile, section 2.3 (repeated attribute categories). Other schemes are
 * not supported.
 *
 * @version $Id: $
 */
public final class MultiDecisionRequestFilter extends BaseRequestFilter
{
	/**
	 *
	 * Factory for this type of request filter that allows duplicate &lt;Attribute&gt; with same meta-data in the same
	 * &lt;Attributes&gt; element of a Request (complying with XACML 3.0 core spec, §7.3.3).
	 *
	 */
	public static final class LaxFilterFactory implements RequestFilter.Factory
	{
		/**
		 * Request filter ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-filter:multiple:repeated-attribute-categories-lax";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(final DatatypeFactoryRegistry datatypeFactoryRegistry,
				final boolean strictAttributeIssuerMatch, final boolean requireContentForXPath,
				final Processor xmlProcessor)
		{
			return new MultiDecisionRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, true,
					requireContentForXPath, xmlProcessor);
		}
	}

	/**
	 *
	 * Factory for this type of request filter that does NOT allow duplicate &lt;Attribute&gt; with same meta-data in
	 * the same &lt;Attributes&gt; element of a Request (NOT complying with XACML 3.0 core spec, §7.3.3).
	 *
	 */
	public static final class StrictFilterFactory implements RequestFilter.Factory
	{
		/**
		 * Request filter ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-filter:multiple:repeated-attribute-categories-strict";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(final DatatypeFactoryRegistry datatypeFactoryRegistry,
				final boolean strictAttributeIssuerMatch, final boolean requireContentForXPath,
				final Processor xmlProcessor)
		{
			return new MultiDecisionRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, false,
					requireContentForXPath, xmlProcessor);
		}
	}

	// private static Logger LOGGER = LoggerFactory.getLogger(MultiDecisionRequestFilter.class);

	private MultiDecisionRequestFilter(final DatatypeFactoryRegistry datatypeFactoryRegistry,
			final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
			final boolean requireContentForXPath, final Processor xmlProcessor)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath,
				xmlProcessor);
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends IndividualDecisionRequest> filter(final List<Attributes> attributesList,
			final JaxbXACMLAttributesParser xacmlAttrsParser, final boolean isApplicablePolicyIdListReturned,
			final boolean combinedDecision, final XPathCompiler xPathCompiler,
			final Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
	{
		/*
		 * Parse Request attributes and group possibly repeated categories to implement Multiple Decision Profile, §2.3.
		 */
		/*
		 * We would like that the order of attributes (more particularly attribute categories) included in the result be
		 * in the same order as in the request (more particularly, attribute categories in order of first occurrence in
		 * the case of Multiple Decision Request); because "Clients generally appreciate having things returned in the
		 * same order they were presented." (See Java LinkedHashMap javadoc description.) Therefore, we use a
		 * LinkedHashMap for the Map<CategoryName,Attributes> below. If the impact on performance proves to be too
		 * negative, we might switch to a simpler Map implementation not preserving iteration order. Unfortunately,
		 * Koloboke - that we are using as HashMap alternative to JDK - does not support LinkedHashMap equivalent at the
		 * moment: https://github.com/leventov/Koloboke/issues/47 (we should keep an eye on it). So until this resolved,
		 * we use JDK LinkedHashMap.
		 */
		final Map<String, Queue<SingleCategoryAttributes<?>>> multiReqAttrAlternativesByCategory = new LinkedHashMap<>();
		for (final Attributes jaxbAttributes : attributesList)
		{
			final String categoryName = jaxbAttributes.getCategory();
			final SingleCategoryAttributes<?> categoryAttributesAlternative = xacmlAttrsParser
					.parseAttributes(jaxbAttributes, xPathCompiler);
			if (categoryAttributesAlternative == null)
			{
				// skip this empty Attributes
				continue;
			}

			final Queue<SingleCategoryAttributes<?>> oldAttrAlternatives = multiReqAttrAlternativesByCategory
					.get(categoryName);
			final Queue<SingleCategoryAttributes<?>> newAttrAlternatives;
			if (oldAttrAlternatives == null)
			{
				newAttrAlternatives = new ArrayDeque<>();
				multiReqAttrAlternativesByCategory.put(categoryName, newAttrAlternatives);
			}
			else
			{
				newAttrAlternatives = oldAttrAlternatives;
			}

			newAttrAlternatives.add(categoryAttributesAlternative);
		}

		/*
		 * Create initial individual request from which all others will be created/cloned
		 */
		// returnPolicyIdList not supported so always set to false
		final MutableIndividualDecisionRequest initialIndividualReq;
		try
		{
			initialIndividualReq = new MutableIndividualDecisionRequest(isApplicablePolicyIdListReturned);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IndeterminateEvaluationException("Invalid RequestDefaults/XPathVersion",
					StatusHelper.STATUS_SYNTAX_ERROR, e);
		}
		/*
		 * Generate the Multiple Individual Decision Requests starting with initialIndividualReq and cloning/adding new
		 * attributes/content for each new attribute category's Attributes alternative in
		 * requestAttrAlternativesByCategory
		 */
		/*
		 * XACML Multiple Decision Profile, § 2.3.3: "For each combination of repeated <Attributes> elements, one
		 * Individual Decision Request SHALL be created. This Individual Request SHALL be identical to the original
		 * request context with one exception: only one <Attributes> element of each repeated category SHALL be
		 * present."
		 */
		final List<MutableIndividualDecisionRequest> individualRequests = new ArrayList<>();
		individualRequests.add(initialIndividualReq);
		// for each attribute category
		/*
		 * As explained at the beginning of the method, at this point, we want to make sure that entries are returned in
		 * the same order (of first occurrence in the case of Multiple Decision Request) as the categories in the
		 * request, where each category matches the key in the entry; because "Clients generally appreciate having
		 * things returned in the same order they were presented." So the map should guarantee that the iteration order
		 * is the same as insertion order used previously (e.g. LinkedHashMap).
		 */
		for (final Entry<String, Queue<SingleCategoryAttributes<?>>> multiReqAttrAlternativesByCategoryEntry : multiReqAttrAlternativesByCategory
				.entrySet())
		{
			final String categoryName = multiReqAttrAlternativesByCategoryEntry.getKey();
			final Queue<SingleCategoryAttributes<?>> categoryAlternatives = multiReqAttrAlternativesByCategoryEntry
					.getValue();
			/*
			 * Get the first category (<Attributes>) alternative to be added to the individual requests existing in the
			 * individualRequests already, i.e. the "old" ones; whereas the other alternatives (if any) will be added to
			 * new individual request cloned from these "old" ones.
			 */
			final SingleCategoryAttributes<?> categoryAlternative0 = categoryAlternatives.poll();
			if (categoryAlternative0 == null)
			{
				// no alternative / no repeated category
				continue;
			}

			final ListIterator<MutableIndividualDecisionRequest> individualRequestsIterator = individualRequests
					.listIterator();
			while (individualRequestsIterator.hasNext())
			{
				final MutableIndividualDecisionRequest oldReq = individualRequestsIterator.next();
				/*
				 * Before we add the first category alternative (categoryAlternative0) to the oldReq already created
				 * (the "old" one), we clone it for every other alternative, then add this other alternative to the new
				 * clone. Note that we called categoryAlternatives.poll() before, removing the first alternative, so
				 * categoryAlternatives only contains the other alternatives now.
				 */
				try
				{
					for (final SingleCategoryAttributes<?> otherCategoryAlternative : categoryAlternatives)
					{
						// clone the request
						final MutableIndividualDecisionRequest newReq = new MutableIndividualDecisionRequest(oldReq);
						newReq.put(categoryName, otherCategoryAlternative);
						// add it to the final list of individual requests
						individualRequestsIterator.add(newReq);
					}

					// Now we are done cloning, we can add the first category alternative to
					// individualReqCtx
					oldReq.put(categoryName, categoryAlternative0);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IndeterminateEvaluationException(
							"Error converting Multiple Decision Request into multiple Individual Decision Requests",
							StatusHelper.STATUS_PROCESSING_ERROR, e);
				}
			}

		}

		return individualRequests;
	}
}
