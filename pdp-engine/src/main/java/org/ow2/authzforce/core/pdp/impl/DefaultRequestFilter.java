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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.AttributeFQN;
import org.ow2.authzforce.core.pdp.api.BaseRequestFilter;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.ImmutablePdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualXACMLRequest;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.JaxbXACMLAttributesParser;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequestFactory;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;

import com.google.common.collect.ImmutableList;

/**
 * Default Request filter for Individual Decision Requests only (no support of Multiple Decision Profile in particular)
 *
 * @version $Id: $
 */
public final class DefaultRequestFilter extends BaseRequestFilter
{
	private static final PdpDecisionRequestFactory<ImmutablePdpDecisionRequest> DEFAULT_REQUEST_FACTORY = new PdpDecisionRequestFactory<ImmutablePdpDecisionRequest>()
	{

		@Override
		public ImmutablePdpDecisionRequest getInstance(final Map<AttributeFQN, AttributeBag<?>> namedAttributes, final Map<String, XdmNode> extraContentsByCategory,
				final boolean returnApplicablePolicies)
		{
			return ImmutablePdpDecisionRequest.getInstance(namedAttributes, extraContentsByCategory, returnApplicablePolicies);
		}
	};

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
			return new DefaultRequestFilter(datatypeFactoryRegistry, DEFAULT_REQUEST_FACTORY, strictAttributeIssuerMatch, true, requireContentForXPath, xmlProcessor);
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
			return new DefaultRequestFilter(datatypeFactoryRegistry, DEFAULT_REQUEST_FACTORY, strictAttributeIssuerMatch, false, requireContentForXPath, xmlProcessor);
		}
	}

	private final PdpDecisionRequestFactory<ImmutablePdpDecisionRequest> reqFactory;

	/**
	 * Creates instance of default request filter
	 * 
	 * @param datatypeFactoryRegistry
	 *            attribute datatype registry
	 * @param requestFactory
	 *            decision request factory
	 * @param strictAttributeIssuerMatch
	 *            true iff strict attribute Issuer match must be enforced (in particular request attributes with empty Issuer only match corresponding AttributeDesignators with empty Issuer)
	 * @param allowAttributeDuplicates
	 *            true iff duplicate Attribute (with same metadata) elements in Request (for multi-valued attributes) must be allowed
	 * @param requireContentForXPath
	 *            true iff Content elements must be parsed, else ignored
	 * @param xmlProcessor
	 *            XML processor for parsing Content elements iff {@code requireContentForXPath}
	 */
	public DefaultRequestFilter(final DatatypeFactoryRegistry datatypeFactoryRegistry, final PdpDecisionRequestFactory<ImmutablePdpDecisionRequest> requestFactory,
			final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates, final boolean requireContentForXPath, final Processor xmlProcessor)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, xmlProcessor);
		assert requestFactory != null;
		reqFactory = requestFactory;
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends IndividualXACMLRequest> filter(final List<Attributes> attributesList, final JaxbXACMLAttributesParser xacmlAttrsParser, final boolean isApplicablePolicyIdListReturned,
			final boolean combinedDecision, final XPathCompiler xPathCompiler, final Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
	{
		final Map<AttributeFQN, AttributeBag<?>> namedAttributes = HashCollections.newUpdatableMap(attributesList.size());
		final Map<String, XdmNode> extraContentsByCategory = HashCollections.newUpdatableMap(attributesList.size());
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

			final XdmNode newContentNode = categorySpecificAttributes.getExtraContent();
			if (newContentNode != null)
			{
				final XdmNode duplicate = extraContentsByCategory.putIfAbsent(categoryName, newContentNode);
				/*
				 * No support for Multiple Decision Profile -> no support for repeated categories as specified in Multiple Decision Profile. So we must check duplicate attribute categories.
				 */
				if (duplicate != null)
				{
					throw new IndeterminateEvaluationException("Unsupported repetition of Attributes[@Category='" + categoryName
							+ "'] (feature 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories' is not supported)", StatusHelper.STATUS_SYNTAX_ERROR);
				}
			}

			/*
			 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee that attribute values remain constant during the evaluation of the request, as
			 * mandated by the XACML spec, section 7.3.5: <p> <i>
			 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
			 * </i></p>
			 */
			for (final Entry<AttributeFQN, AttributeBag<?>> attrEntry : categorySpecificAttributes)
			{
				namedAttributes.put(attrEntry.getKey(), attrEntry.getValue());
			}

			final Attributes catSpecificAttrsToIncludeInResult = categorySpecificAttributes.getAttributesToIncludeInResult();
			if (catSpecificAttrsToIncludeInResult != null)
			{
				attributesToIncludeInResult.add(catSpecificAttrsToIncludeInResult);
			}
		}

		return Collections.singletonList(new IndividualXACMLRequest(reqFactory.getInstance(namedAttributes, extraContentsByCategory, isApplicablePolicyIdListReturned), ImmutableList
				.copyOf(attributesToIncludeInResult)));
	}
}
