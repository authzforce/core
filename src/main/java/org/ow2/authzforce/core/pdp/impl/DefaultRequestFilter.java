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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Default Request filter for Individual Decision Requests only (no support of Multiple Decision Profile in particular)
 *
 * @version $Id: $
 */
public final class DefaultRequestFilter extends BaseRequestFilter
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
		 * Request filter ID, as returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-filter:default-lax";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean requireContentForXPath, Processor xmlProcessor)
		{
			return new DefaultRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, true, requireContentForXPath, xmlProcessor);
		}

		/**
		 * Singleton instance of Factory for DefaultRequestFilters
		 * 
		 */
		public static final RequestFilter.Factory INSTANCE = new LaxFilterFactory();
	}

	/**
	 *
	 * Factory for this type of request filter that does NOT allow duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (NOT complying fully with XACML
	 * 3.0 core spec, §7.3.3).
	 *
	 */
	public static final class StrictFilterFactory implements RequestFilter.Factory
	{
		private static final String ID = "urn:ow2:authzforce:feature:pdp:request-filter:default-strict";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean requireContentForXPath, Processor xmlProcessor)
		{
			return new DefaultRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, false, requireContentForXPath, xmlProcessor);
		}
	}

	private DefaultRequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean allowAttributeDuplicates, boolean requireContentForXPath,
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
		 * No support for Multiple Decision Profile -> no support for repeated categories as specified in Multiple Decision Profile. So we keep track of attribute categories to check duplicates.
		 */
		final Set<String> attrCategoryNames = new HashSet<>();
		final MutableIndividualDecisionRequest individualDecisionRequest;
		try
		{
			individualDecisionRequest = new MutableIndividualDecisionRequest(isApplicablePolicyIdListReturned);
		} catch (IllegalArgumentException e)
		{
			throw new IndeterminateEvaluationException("Invalid RequestDefaults/XPathVersion", StatusHelper.STATUS_SYNTAX_ERROR, e);
		}

		for (final Attributes jaxbAttributes : attributesList)
		{
			final String categoryName = jaxbAttributes.getCategory();
			if (!attrCategoryNames.add(categoryName))
			{
				throw new IndeterminateEvaluationException("Unsupported repetition of Attributes[@Category='" + categoryName
						+ "'] (feature 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories' is not supported)", StatusHelper.STATUS_SYNTAX_ERROR);
			}

			final SingleCategoryAttributes<?> categorySpecificAttributes = xacmlAttrsParser.parseAttributes(jaxbAttributes, xPathCompiler);
			if (categorySpecificAttributes == null)
			{
				// skip this empty Attributes
				continue;
			}

			individualDecisionRequest.put(categoryName, categorySpecificAttributes);
		}

		return Collections.singletonList(individualDecisionRequest);
	}
}
