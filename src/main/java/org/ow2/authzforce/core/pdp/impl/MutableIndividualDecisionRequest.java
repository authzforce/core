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
package org.ow2.authzforce.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.Bag;

/**
 * Mutable Individual Decision Request
 */
public class MutableIndividualDecisionRequest implements IndividualDecisionRequest
{
	private final Map<AttributeGUID, Bag<?>> attributes;
	private final Map<String, XdmNode> extraContentsByCategory;
	private final List<Attributes> attributesToIncludeInResult;
	private final boolean returnApplicablePolicyIdList;

	/**
	 * Creates empty request (no attribute)
	 * 
	 * @param returnPolicyIdList
	 *            equivalent of XACML ReturnPolicyIdList
	 */
	public MutableIndividualDecisionRequest(boolean returnPolicyIdList)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = new HashMap<>();
		extraContentsByCategory = new HashMap<>();
		attributesToIncludeInResult = new ArrayList<>();
		returnApplicablePolicyIdList = returnPolicyIdList;
	}

	/**
	 * Create new instance as a clone of an existing request.
	 * 
	 * @param baseRequest
	 *            replicated existing request. Further changes to it are not reflected back to this new instance.
	 */
	public MutableIndividualDecisionRequest(IndividualDecisionRequest baseRequest)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = new HashMap<>(baseRequest.getNamedAttributes());
		extraContentsByCategory = new HashMap<>(baseRequest.getExtraContentsByCategory());
		attributesToIncludeInResult = new ArrayList<>(baseRequest.getReturnedAttributes());
		returnApplicablePolicyIdList = baseRequest.isApplicablePolicyIdListReturned();
	}

	/**
	 * Put attributes of a specific category in request.
	 * 
	 * @param categoryName
	 *            category URI
	 * @param categorySpecificAttributes
	 *            attributes in category {@code categoryName}
	 * @throws IllegalArgumentException
	 *             if {@code categoryName} or {@code attributes} is null
	 */
	public void put(String categoryName, SingleCategoryAttributes<?> categorySpecificAttributes) throws IllegalArgumentException
	{
		if (categoryName == null)
		{
			throw new IllegalArgumentException("Undefined attribute category");
		}

		if (categorySpecificAttributes == null)
		{
			throw new IllegalArgumentException("Undefined attributes");
		}

		/*
		 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee that attribute values remain constant
		 * during the evaluation of the request, as mandated by the XACML spec, section 7.3.5: <p> <i>
		 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
		 * </i></p>
		 */
		for (final Entry<AttributeGUID, Bag<?>> attrEntry : categorySpecificAttributes)
		{
			attributes.put(attrEntry.getKey(), attrEntry.getValue());
		}

		extraContentsByCategory.put(categoryName, categorySpecificAttributes.getExtraContent());
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
	@Override
	public Map<AttributeGUID, Bag<?>> getNamedAttributes()
	{
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getAttributesIncludedInResult()
	 */
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
	@Override
	public Map<String, XdmNode> getExtraContentsByCategory()
	{
		return this.extraContentsByCategory;
	}

	/**
	 * @return the returnApplicablePolicyIdList
	 */
	@Override
	public boolean isApplicablePolicyIdListReturned()
	{
		return returnApplicablePolicyIdList;
	}

}
