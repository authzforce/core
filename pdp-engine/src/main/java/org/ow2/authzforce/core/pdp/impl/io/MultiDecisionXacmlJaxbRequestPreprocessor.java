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
package org.ow2.authzforce.core.pdp.impl.io;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryXacmlAttributesParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.ImmutableList;

/**
 * XACML/XML Request preprocessor implementing Multiple Decision Profile, section 2.3 (repeated attribute categories). Other schemes are not supported.
 *
 * @version $Id: $
 */
public final class MultiDecisionXacmlJaxbRequestPreprocessor extends BaseXacmlJaxbRequestPreprocessor
{
	/**
	 * (Mutable) {@link IndividualXacmlJaxbRequest} builder
	 *
	 * @version $Id: $
	 */
	private static final class IndividualXacmlJaxbRequestBuilder
	{
		private static final IllegalArgumentException UNDEF_ATTRIBUTES_EXCEPTION = new IllegalArgumentException("Undefined attributes");
		private static final IllegalArgumentException UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute category");

		// initialized not null by constructors
		private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;
		private final Map<String, XdmNode> contentNodesByCategory;
		private final List<Attributes> attributesToIncludeInResult;
		private final boolean isApplicablePolicyIdListReturned;

		/**
		 * Creates empty request (no attribute)
		 *
		 * @param returnPolicyIdList
		 *            equivalent of XACML ReturnPolicyIdList
		 */
		private IndividualXacmlJaxbRequestBuilder(final boolean returnPolicyIdList)
		{
			// these maps/lists may be updated later by put(...) method defined in this class
			namedAttributes = HashCollections.newUpdatableMap();
			contentNodesByCategory = HashCollections.newUpdatableMap();
			attributesToIncludeInResult = new ArrayList<>();
			isApplicablePolicyIdListReturned = returnPolicyIdList;
		}

		/**
		 * Create new instance as a clone of an existing request.
		 *
		 * @param baseRequest
		 *            replicated existing request. Further changes to it are not reflected back to this new instance.
		 */
		private IndividualXacmlJaxbRequestBuilder(final IndividualXacmlJaxbRequestBuilder baseRequest)
		{
			assert baseRequest != null;

			// these maps/lists may be updated later by put(...) method defined in this class
			namedAttributes = HashCollections.newUpdatableMap(baseRequest.namedAttributes);
			contentNodesByCategory = HashCollections.newUpdatableMap(baseRequest.contentNodesByCategory);
			isApplicablePolicyIdListReturned = baseRequest.isApplicablePolicyIdListReturned;
			attributesToIncludeInResult = new ArrayList<>(baseRequest.attributesToIncludeInResult);
		}

		/**
		 * Put attributes of a specific category in request.
		 *
		 * @param categoryName
		 *            category URI
		 * @param categorySpecificAttributes
		 *            attributes in category {@code categoryName}
		 * @throws java.lang.IllegalArgumentException
		 *             if {@code categoryName == null || categorySpecificAttributes == null} or duplicate attribute category (this method was already called with same {@code categoryName})
		 */
		private void put(final String categoryName, final SingleCategoryAttributes<?, Attributes> categorySpecificAttributes) throws IllegalArgumentException
		{
			if (categoryName == null)
			{
				throw UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION;
			}

			if (categorySpecificAttributes == null)
			{
				throw UNDEF_ATTRIBUTES_EXCEPTION;
			}

			// extraContentsByCategory initialized not null by constructors
			assert contentNodesByCategory != null;
			final XdmNode newContentNode = categorySpecificAttributes.getExtraContent();
			if (newContentNode != null)
			{
				final XdmNode duplicate = contentNodesByCategory.putIfAbsent(categoryName, newContentNode);
				if (duplicate != null)
				{
					throw new IllegalArgumentException("Duplicate Attributes[@Category] in Individual Decision Request (not allowed): " + categoryName);
				}
			}

			/*
			 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee that attribute values remain constant during the evaluation of the request, as
			 * mandated by the XACML spec, section 7.3.5: <p> <i>
			 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
			 * </i></p>
			 */
			for (final Entry<AttributeFqn, AttributeBag<?>> attrEntry : categorySpecificAttributes)
			{
				namedAttributes.put(attrEntry.getKey(), attrEntry.getValue());
			}

			final Attributes catSpecificAttrsToIncludeInResult = categorySpecificAttributes.getAttributesToIncludeInResult();
			if (catSpecificAttrsToIncludeInResult != null)
			{
				attributesToIncludeInResult.add(catSpecificAttrsToIncludeInResult);
			}

		}

		private IndividualXacmlJaxbRequest build()
		{
			return new IndividualXacmlJaxbRequest(ImmutableDecisionRequest.getInstance(this.namedAttributes, this.contentNodesByCategory, this.isApplicablePolicyIdListReturned),
					ImmutableList.copyOf(this.attributesToIncludeInResult));
		}

	}

	/**
	 *
	 * Factory for this type of request preprocessor that allows duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (complying with XACML 3.0 core
	 * spec, §7.3.3).
	 *
	 */
	public static final class LaxVariantFactory extends BaseXacmlJaxbRequestPreprocessor.Factory
	{
		/**
		 * Request preprocessor ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:multiple:repeated-attribute-categories-lax";

		/**
		 * Constructor
		 */
		public LaxVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
				final boolean requireContentForXPath, final Processor xmlProcessor, final Set<String> extraPdpFeatures)
		{
			return new MultiDecisionXacmlJaxbRequestPreprocessor(datatypeFactoryRegistry, strictAttributeIssuerMatch, true, requireContentForXPath, xmlProcessor, extraPdpFeatures);
		}
	}

	/**
	 *
	 * Factory for this type of request preprocessor that does NOT allow duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (NOT complying with XACML
	 * 3.0 core spec, §7.3.3).
	 *
	 */
	public static final class StrictVariantFactory extends BaseXacmlJaxbRequestPreprocessor.Factory
	{
		/**
		 * Request preprocessor ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:multiple:repeated-attribute-categories-strict";

		/**
		 * Constructor
		 */
		public StrictVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
				final boolean requireContentForXPath, final Processor xmlProcessor, final Set<String> extraPdpFeatures)
		{
			return new MultiDecisionXacmlJaxbRequestPreprocessor(datatypeFactoryRegistry, strictAttributeIssuerMatch, false, requireContentForXPath, xmlProcessor, extraPdpFeatures);
		}
	}

	private MultiDecisionXacmlJaxbRequestPreprocessor(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
			final boolean requireContentForXPath, final Processor xmlProcessor, final Set<String> extraPdpFeatures)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, xmlProcessor, extraPdpFeatures);
	}

	/** {@inheritDoc} */
	@Override
	public List<IndividualXacmlJaxbRequest> process(final List<Attributes> attributesList, final SingleCategoryXacmlAttributesParser<Attributes> xacmlAttrsParser,
			final boolean isApplicablePolicyIdListReturned, final boolean combinedDecision, final XPathCompiler xPathCompiler, final Map<String, String> namespaceURIsByPrefix)
			throws IndeterminateEvaluationException
	{
		/*
		 * Parse Request attributes and group possibly repeated categories to implement Multiple Decision Profile, §2.3.
		 */
		/*
		 * We would like that the order of attributes (more particularly attribute categories) included in the result be in the same order as in the request (more particularly, attribute categories in
		 * order of first occurrence in the case of Multiple Decision Request); because "Clients generally appreciate having things returned in the same order they were presented." (See Java
		 * LinkedHashMap javadoc description.) Therefore, we use a LinkedHashMap for the Map<CategoryName,Attributes> below. If the impact on performance proves to be too negative, we might switch to
		 * a simpler Map implementation not preserving iteration order. Unfortunately, Koloboke - that we are using as HashMap alternative to JDK - does not support LinkedHashMap equivalent at the
		 * moment: https://github.com/leventov/Koloboke/issues/47 (we should keep an eye on it). So until this resolved, we use JDK LinkedHashMap.
		 */
		final Map<String, Queue<SingleCategoryAttributes<?, Attributes>>> multiReqAttrAlternativesByCategory = new LinkedHashMap<>();
		for (final Attributes jaxbAttributes : attributesList)
		{
			final SingleCategoryAttributes<?, Attributes> categoryAttributesAlternative = xacmlAttrsParser.parseAttributes(jaxbAttributes, xPathCompiler);
			if (categoryAttributesAlternative == null)
			{
				// skip this empty Attributes
				continue;
			}

			final String categoryId = categoryAttributesAlternative.getCategoryId();
			final Queue<SingleCategoryAttributes<?, Attributes>> oldAttrAlternatives = multiReqAttrAlternativesByCategory.get(categoryId);
			final Queue<SingleCategoryAttributes<?, Attributes>> newAttrAlternatives;
			if (oldAttrAlternatives == null)
			{
				newAttrAlternatives = new ArrayDeque<>();
				multiReqAttrAlternativesByCategory.put(categoryId, newAttrAlternatives);
			}
			else
			{
				newAttrAlternatives = oldAttrAlternatives;
			}

			newAttrAlternatives.add(categoryAttributesAlternative);
		}

		/*
		 * Create mutable initial individual request from which all others will be created/cloned
		 */
		// returnPolicyIdList not supported so always set to false
		final IndividualXacmlJaxbRequestBuilder initialIndividualReqBuilder;
		try
		{
			initialIndividualReqBuilder = new IndividualXacmlJaxbRequestBuilder(isApplicablePolicyIdListReturned);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IndeterminateEvaluationException("Invalid RequestDefaults/XPathVersion", XacmlStatusCode.SYNTAX_ERROR.value(), e);
		}
		/*
		 * Generate the Multiple Individual Decision Requests starting with initialIndividualReq and cloning/adding new attributes/content for each new attribute category's Attributes alternative in
		 * requestAttrAlternativesByCategory
		 */
		/*
		 * XACML Multiple Decision Profile, § 2.3.3: "For each combination of repeated <Attributes> elements, one Individual Decision Request SHALL be created. This Individual Request SHALL be
		 * identical to the original request context with one exception: only one <Attributes> element of each repeated category SHALL be present."
		 */
		final List<IndividualXacmlJaxbRequestBuilder> individualRequestBuilders = new ArrayList<>();
		individualRequestBuilders.add(initialIndividualReqBuilder);
		/*
		 * In order to create the final individual decision requests, for each attribute category, add each alternative to individual request builders
		 */
		final List<IndividualXacmlJaxbRequest> finalIndividualRequests = new ArrayList<>();
		/*
		 * As explained at the beginning of the method, at this point, we want to make sure that entries are returned in the same order (of first occurrence in the case of Multiple Decision Request)
		 * as the categories in the request, where each category matches the key in the entry; because "Clients generally appreciate having things returned in the same order they were presented." So
		 * the map should guarantee that the iteration order is the same as insertion order used previously (e.g. LinkedHashMap).
		 */
		final Iterator<Entry<String, Queue<SingleCategoryAttributes<?, Attributes>>>> multiReqAttrAlternativesByCategoryIterator = multiReqAttrAlternativesByCategory.entrySet().iterator();
		boolean isLastCategory = !multiReqAttrAlternativesByCategoryIterator.hasNext();
		while (!isLastCategory)
		{
			final Entry<String, Queue<SingleCategoryAttributes<?, Attributes>>> multiReqAttrAlternativesByCategoryEntry = multiReqAttrAlternativesByCategoryIterator.next();
			final String categoryName = multiReqAttrAlternativesByCategoryEntry.getKey();
			final Queue<SingleCategoryAttributes<?, Attributes>> categorySpecificAlternatives = multiReqAttrAlternativesByCategoryEntry.getValue();
			isLastCategory = !multiReqAttrAlternativesByCategoryIterator.hasNext();
			final ListIterator<IndividualXacmlJaxbRequestBuilder> individualRequestsIterator = individualRequestBuilders.listIterator();
			while (individualRequestsIterator.hasNext())
			{
				final IndividualXacmlJaxbRequestBuilder oldIndividualReqBuilder = individualRequestsIterator.next();
				/*
				 * New newIndividualReqBuilders created below from this $oldIndividualReqBuilder will replace it in the list of $individualRequestBuilders (and will be used in their turn as
				 * $oldIndividualReqBuilders). So remove current $oldIndividualReqBuilder from the list
				 */
				individualRequestsIterator.remove();

				/*
				 * Before we add the first category alternative (categoryAlternative0) to the oldReq already created (the "old" one), we clone it for every other alternative, then add this other
				 * alternative to the new clone. Note that we called categoryAlternatives.poll() before, removing the first alternative, so categoryAlternatives only contains the other alternatives
				 * now.
				 */
				for (final SingleCategoryAttributes<?, Attributes> otherCategoryAlternative : categorySpecificAlternatives)
				{
					// clone the request
					final IndividualXacmlJaxbRequestBuilder newIndividualReqBuilder = new IndividualXacmlJaxbRequestBuilder(oldIndividualReqBuilder);
					newIndividualReqBuilder.put(categoryName, otherCategoryAlternative);
					if (isLastCategory)
					{
						// we can finalize the request build
						finalIndividualRequests.add(newIndividualReqBuilder.build());
					}
					else
					{
						/*
						 * add the new request builder to the list of builders for the next round
						 */
						individualRequestsIterator.add(newIndividualReqBuilder);
					}
				}
			}

		}

		return finalIndividualRequests;
	}
}
