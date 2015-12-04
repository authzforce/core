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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.Bag;

/**
 * Mutable Individual Decision Request
 */
public class ImmutableIndividualDecisionRequest implements IndividualDecisionRequest
{
	private final Map<AttributeGUID, Bag<?>> attributes;
	private final Map<String, XdmNode> extraContentsByCategory;
	private final List<Attributes> attributesToIncludeInResult;
	private final boolean returnApplicablePolicyIdList;

	/**
	 * Create new instance
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param extraContentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param includedInResult
	 *            attributes to be include in the final Result
	 * @param returnPolicyIdList
	 *            XACML Request's ReturnPolicyIdList flag
	 */
	public ImmutableIndividualDecisionRequest(Map<AttributeGUID, Bag<?>> namedAttributes, Map<String, XdmNode> extraContentNodesByCategory,
			List<Attributes> includedInResult, boolean returnPolicyIdList)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = Collections.unmodifiableMap(namedAttributes);
		extraContentsByCategory = Collections.unmodifiableMap(extraContentNodesByCategory);
		attributesToIncludeInResult = Collections.unmodifiableList(includedInResult);
		returnApplicablePolicyIdList = returnPolicyIdList;
	}

	/**
	 * Create new instance as a clone of an existing request.
	 * 
	 * @param baseRequest
	 *            replicated existing request. Further changes to it are not reflected back to this new instance.
	 */
	public ImmutableIndividualDecisionRequest(IndividualDecisionRequest baseRequest)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		this(baseRequest.getNamedAttributes(), baseRequest.getExtraContentsByCategory(), baseRequest.getReturnedAttributes(), baseRequest
				.isApplicablePolicyIdListReturned());
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
