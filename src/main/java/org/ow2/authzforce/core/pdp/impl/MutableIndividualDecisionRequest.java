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
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.value.Bag;

import com.koloboke.collect.map.hash.HashObjObjMaps;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

/**
 * Mutable Individual Decision Request
 *
 * @version $Id: $
 */
public final class MutableIndividualDecisionRequest implements IndividualDecisionRequest
{
	private static final IllegalArgumentException UNDEF_ATTRIBUTES_EXCEPTION = new IllegalArgumentException(
			"Undefined attributes");
	private static final IllegalArgumentException UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION = new IllegalArgumentException(
			"Undefined attribute category");

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
		namedAttributes = HashObjObjMaps.newUpdatableMap();
		extraContentsByCategory = HashObjObjMaps.newUpdatableMap();
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
		namedAttributes = baseNamedAttributes == null ? HashObjObjMaps.<AttributeGUID, Bag<?>>newUpdatableMap()
				: HashObjObjMaps.newUpdatableMap(baseNamedAttributes);
		extraContentsByCategory = baseExtraContentsByCategory == null
				? HashObjObjMaps.<String, XdmNode>newUpdatableMap()
				: HashObjObjMaps.newUpdatableMap(baseExtraContentsByCategory);
		attributesToIncludeInResult = baseReturnedAttributes == null ? new ArrayList<Attributes>()
				: new ArrayList<>(baseRequest.getReturnedAttributes());
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
	 *             if {@code categoryName == null || attributes == null} or duplicate attribute category
	 *             ({@link #put(String, SingleCategoryAttributes)} already called with same {@code categoryName}
	 */
	public void put(final String categoryName, final SingleCategoryAttributes<?> categorySpecificAttributes)
			throws IllegalArgumentException
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
		final XdmNode oldVal = extraContentsByCategory.put(categoryName, categorySpecificAttributes.getExtraContent());
		if (oldVal != null)
		{
			throw new IllegalArgumentException(
					"Duplicate Attributes[@Category] in Individual Decision Request (not allowed): " + categoryName);
		}

		/*
		 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee
		 * that attribute values remain constant during the evaluation of the request, as mandated by the XACML spec,
		 * section 7.3.5: <p> <i>
		 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
		 * </i></p>
		 */
		for (final Entry<AttributeGUID, Bag<?>> attrEntry : categorySpecificAttributes)
		{
			namedAttributes.put(attrEntry.getKey(), attrEntry.getValue());
		}

		final Attributes catSpecificAttrsToIncludeInResult = categorySpecificAttributes
				.getAttributesToIncludeInResult();
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
