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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.BaseRequestFilter;
import org.ow2.authzforce.core.pdp.api.ImmutableIndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.JaxbXACMLAttributesParser;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;

import com.koloboke.collect.map.hash.HashObjObjMaps;

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
		public RequestFilter getInstance(final DatatypeFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean requireContentForXPath,
				final Processor xmlProcessor)
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
		public RequestFilter getInstance(final DatatypeFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean requireContentForXPath,
				final Processor xmlProcessor)
		{
			return new DefaultRequestFilter(datatypeFactoryRegistry, strictAttributeIssuerMatch, false, requireContentForXPath, xmlProcessor);
		}
	}

	private DefaultRequestFilter(final DatatypeFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
			final boolean requireContentForXPath, final Processor xmlProcessor)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, xmlProcessor);
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends IndividualDecisionRequest> filter(final List<Attributes> attributesList, final JaxbXACMLAttributesParser xacmlAttrsParser, final boolean isApplicablePolicyIdListReturned,
			final boolean combinedDecision, final XPathCompiler xPathCompiler, final Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
	{
		final Map<AttributeGUID, Bag<?>> namedAttributes = HashObjObjMaps.newUpdatableMap(attributesList.size());
		final Map<String, XdmNode> extraContentsByCategory = HashObjObjMaps.newUpdatableMap(attributesList.size());
		/*
		 * attributesToIncludeInResult.size() <= attributesList.size()
		 */
		final List<Attributes> attributesToIncludeInResult = new ArrayList<>(attributesList.size());

		for (final Attributes jaxbAttributes : attributesList)
		{
			final String categoryName = jaxbAttributes.getCategory();
			final SingleCategoryAttributes<?> categorySpecificAttributes = xacmlAttrsParser.parseAttributes(jaxbAttributes, xPathCompiler);
			if (categorySpecificAttributes == null)
			{
				// skip this empty Attributes
				continue;
			}

			final XdmNode oldVal = extraContentsByCategory.put(categoryName, categorySpecificAttributes.getExtraContent());
			/*
			 * No support for Multiple Decision Profile -> no support for repeated categories as specified in Multiple Decision Profile. So we must check duplicate attribute categories.
			 */
			if (oldVal != null)
			{
				throw new IndeterminateEvaluationException("Unsupported repetition of Attributes[@Category='" + categoryName
						+ "'] (feature 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories' is not supported)", StatusHelper.STATUS_SYNTAX_ERROR);
			}

			/*
			 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee that attribute values remain constant during the evaluation of the request, as
			 * mandated by the XACML spec, section 7.3.5: <p> <i>
			 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
			 * </i></p>
			 */
			for (final Entry<AttributeGUID, Bag<?>> attrEntry : categorySpecificAttributes)
			{
				namedAttributes.put(attrEntry.getKey(), attrEntry.getValue());
			}

			final Attributes catSpecificAttrsToIncludeInResult = categorySpecificAttributes.getAttributesToIncludeInResult();
			if (catSpecificAttrsToIncludeInResult != null)
			{
				attributesToIncludeInResult.add(catSpecificAttrsToIncludeInResult);
			}
		}

		return Collections.singletonList(new ImmutableIndividualDecisionRequest(namedAttributes, extraContentsByCategory, attributesToIncludeInResult, isApplicablePolicyIdListReturned));
	}
}
