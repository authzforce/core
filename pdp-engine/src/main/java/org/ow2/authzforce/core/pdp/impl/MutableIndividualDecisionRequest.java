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

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Mutable Individual Decision Request
 *
 * @version $Id: $
 */
public final class MutableIndividualDecisionRequest implements IndividualDecisionRequest
{
	private static final IllegalArgumentException UNDEF_ATTRIBUTES_EXCEPTION = new IllegalArgumentException("Undefined attributes");
	private static final IllegalArgumentException UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute category");

	private final Map<AttributeGUID, Bag<?>> namedAttributes;

	// initialized not null by constructors
	private final Map<String, XdmNode> extraContentsByCategory;
	private final List<Attributes> attributesToIncludeInResult;
	private final boolean returnApplicablePolicyIdList;

	/**
	 * Creates empty request (no attribute)
	 *
	 * @param returnPolicyIdList
	 *            equivalent of XACML ReturnPolicyIdList
	 */
	public MutableIndividualDecisionRequest(final boolean returnPolicyIdList)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		namedAttributes = HashCollections.newUpdatableMap();
		extraContentsByCategory = HashCollections.newUpdatableMap();
		attributesToIncludeInResult = new ArrayList<>();
		returnApplicablePolicyIdList = returnPolicyIdList;
	}

	/**
	 * Create new instance as a clone of an existing request.
	 *
	 * @param baseRequest
	 *            replicated existing request. Further changes to it are not reflected back to this new instance.
	 */
	public MutableIndividualDecisionRequest(final IndividualDecisionRequest baseRequest)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		final Map<AttributeGUID, Bag<?>> baseNamedAttributes = baseRequest.getNamedAttributes();
		final Map<String, XdmNode> baseExtraContentsByCategory = baseRequest.getExtraContentsByCategory();
		final List<Attributes> baseReturnedAttributes = baseRequest.getReturnedAttributes();
		namedAttributes = baseNamedAttributes == null ? HashCollections.<AttributeGUID, Bag<?>> newUpdatableMap() : HashCollections.newUpdatableMap(baseNamedAttributes);
		extraContentsByCategory = baseExtraContentsByCategory == null ? HashCollections.<String, XdmNode> newUpdatableMap() : HashCollections.newUpdatableMap(baseExtraContentsByCategory);
		attributesToIncludeInResult = baseReturnedAttributes == null ? new ArrayList<>() : new ArrayList<>(baseRequest.getReturnedAttributes());
		returnApplicablePolicyIdList = baseRequest.isApplicablePolicyIdListReturned();
	}

	/**
	 * Put attributes of a specific category in request.
	 *
	 * @param categoryName
	 *            category URI
	 * @param categorySpecificAttributes
	 *            attributes in category {@code categoryName}
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code categoryName == null || attributes == null} or duplicate attribute category ({@link #put(String, SingleCategoryAttributes)} already called with same {@code categoryName}
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
		assert extraContentsByCategory != null;
		final XdmNode newContentNode = categorySpecificAttributes.getExtraContent();
		if (newContentNode != null)
		{
			final XdmNode duplicate = extraContentsByCategory.putIfAbsent(categoryName, newContentNode);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getNamedAttributes()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<AttributeGUID, Bag<?>> getNamedAttributes()
	{
		return namedAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getAttributesIncludedInResult()
	 */
	/** {@inheritDoc} */
	@Override
	public List<Attributes> getReturnedAttributes()
	{
		return this.attributesToIncludeInResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getExtraContentsByCategory()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<String, XdmNode> getExtraContentsByCategory()
	{
		return this.extraContentsByCategory;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicablePolicyIdListReturned()
	{
		return returnApplicablePolicyIdList;
	}

}
