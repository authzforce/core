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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.BaseRequestFilter;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.JaxbXACMLAttributesParser;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.StatusHelper;

/**
 * Request filter implementing Multiple Decision Profile, section 2.3 (repeated attribute categories). Other schemes are not supported.
 *
 * @version $Id: $
 */
public final class MultiDecisionRequestFilter extends BaseRequestFilter
{
	/**
	 *
	 * Factory for this type of request filter that allows duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (complying with XACML 3.0 core spec,
	 * §7.3.3).
	 *
	 */
	public static final class LaxFilterFactory implements RequestFilter.Factory
	{
		/**
		 * Request filter ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:xacml:request-filter:multiple:repeated-attribute-categories-lax";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean requireContentForXPath, Processor xmlProcessor)
		{
			return new MultiDecisionRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, true, requireContentForXPath, xmlProcessor);
		}
	}

	/**
	 *
	 * Factory for this type of request filter that does NOT allow duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (NOT complying with XACML 3.0
	 * core spec, §7.3.3).
	 *
	 */
	public static final class StrictFilterFactory implements RequestFilter.Factory
	{
		/**
		 * Request filter ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:xacml:request-filter:multiple:repeated-attribute-categories-strict";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean requireContentForXPath, Processor xmlProcessor)
		{
			return new MultiDecisionRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, false, requireContentForXPath, xmlProcessor);
		}
	}

	// private static Logger LOGGER = LoggerFactory.getLogger(MultiDecisionRequestFilter.class);

	private MultiDecisionRequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean allowAttributeDuplicates, boolean requireContentForXPath,
			Processor xmlProcessor)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, xmlProcessor);
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends IndividualDecisionRequest> filter(List<Attributes> attributesList, JaxbXACMLAttributesParser xacmlAttrsParser, boolean isApplicablePolicyIdListReturned,
			boolean combinedDecision, XPathCompiler xPathCompiler, Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
	{
		/*
		 * Parse Request attributes and group possibly repeated categories to implement Multiple Decision Profile, §2.3.
		 */
		final Map<String, Queue<SingleCategoryAttributes<?>>> multiReqAttrAlternativesByCategory = new HashMap<>();
		for (final Attributes jaxbAttributes : attributesList)
		{
			final String categoryName = jaxbAttributes.getCategory();
			final SingleCategoryAttributes<?> categoryAttributesAlternative = xacmlAttrsParser.parseAttributes(jaxbAttributes, xPathCompiler);
			if (categoryAttributesAlternative == null)
			{
				// skip this empty Attributes
				continue;
			}

			final Queue<SingleCategoryAttributes<?>> oldAttrAlternatives = multiReqAttrAlternativesByCategory.get(categoryName);
			final Queue<SingleCategoryAttributes<?>> newAttrAlternatives;
			if (oldAttrAlternatives == null)
			{
				newAttrAlternatives = new ArrayDeque<>();
				multiReqAttrAlternativesByCategory.put(categoryName, newAttrAlternatives);
			} else
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
		} catch (IllegalArgumentException e)
		{
			throw new IndeterminateEvaluationException("Invalid RequestDefaults/XPathVersion", StatusHelper.STATUS_SYNTAX_ERROR, e);
		}
		/*
		 * Generate the Multiple Individual Decision Requests starting with initialIndividualReq and cloning/adding new attributes/content for each new attribute category's Attributes alternative in
		 * requestAttrAlternativesByCategory
		 */
		/*
		 * XACML Multiple Decision Profile, § 2.3.3: "For each combination of repeated <Attributes> elements, one Individual Decision Request SHALL be created. This Individual Request SHALL be
		 * identical to the original request context with one exception: only one <Attributes> element of each repeated category SHALL be present."
		 */
		final List<MutableIndividualDecisionRequest> individualRequests = new ArrayList<>();
		individualRequests.add(initialIndividualReq);
		// for each attribute category
		for (final Entry<String, Queue<SingleCategoryAttributes<?>>> multiReqAttrAlternativesByCategoryEntry : multiReqAttrAlternativesByCategory.entrySet())
		{
			final String categoryName = multiReqAttrAlternativesByCategoryEntry.getKey();
			final Queue<SingleCategoryAttributes<?>> categoryAlternatives = multiReqAttrAlternativesByCategoryEntry.getValue();
			/*
			 * Get the first category (<Attributes>) alternative to be added to the individual requests existing in the individualRequests already, i.e. the "old" ones; whereas the other alternatives
			 * (if any) will be added to new individual request cloned from these "old" ones.
			 */
			final SingleCategoryAttributes<?> categoryAlternative0 = categoryAlternatives.poll();
			if (categoryAlternative0 == null)
			{
				// no alternative / no repeated category
				continue;
			}

			final ListIterator<MutableIndividualDecisionRequest> individualRequestsIterator = individualRequests.listIterator();
			while (individualRequestsIterator.hasNext())
			{
				final MutableIndividualDecisionRequest oldReq = individualRequestsIterator.next();
				/*
				 * Before we add the first category alternative (categoryAlternative0) to the oldReq already created (the "old" one), we clone it for every other alternative, then add this other
				 * alternative to the new clone. Note that we called categoryAlternatives.poll() before, removing the first alternative, so categoryAlternatives only contains the other alternatives
				 * now.
				 */
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

		}

		return individualRequests;
	}
}
