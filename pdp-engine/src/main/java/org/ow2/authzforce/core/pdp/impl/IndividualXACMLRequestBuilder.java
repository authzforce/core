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
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.AttributeFQN;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.ImmutablePdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.IndividualXACMLRequest;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;

import com.google.common.collect.ImmutableList;

/**
 * (Mutable) Individual Decision Request builder, used only by {@link MultiDecisionRequestFilter}, to build an immutable decision request
 *
 * @version $Id: $
 */
final class IndividualXACMLRequestBuilder
{
	private static final IllegalArgumentException UNDEF_ATTRIBUTES_EXCEPTION = new IllegalArgumentException("Undefined attributes");
	private static final IllegalArgumentException UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute category");

	// initialized not null by constructors
	private final Map<AttributeFQN, AttributeBag<?>> namedAttributes;
	private final Map<String, XdmNode> contentNodesByCategory;
	private final List<Attributes> attributesToIncludeInResult;
	private final boolean isApplicablePolicyIdListReturned;

	/**
	 * Creates empty request (no attribute)
	 *
	 * @param returnPolicyIdList
	 *            equivalent of XACML ReturnPolicyIdList
	 */
	IndividualXACMLRequestBuilder(final boolean returnPolicyIdList)
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
	IndividualXACMLRequestBuilder(final IndividualXACMLRequestBuilder baseRequest)
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
	public void put(final String categoryName, final SingleCategoryAttributes<?> categorySpecificAttributes) throws IllegalArgumentException
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

	public IndividualXACMLRequest build()
	{
		return new IndividualXACMLRequest(ImmutablePdpDecisionRequest.getInstance(this.namedAttributes, this.contentNodesByCategory, this.isApplicablePolicyIdListReturned),
				ImmutableList.copyOf(this.attributesToIncludeInResult));
	}

}
