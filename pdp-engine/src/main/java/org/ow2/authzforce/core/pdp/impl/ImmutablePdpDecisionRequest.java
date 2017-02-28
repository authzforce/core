/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl;

import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndividualPdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Immutable implementation of {@link IndividualPdpDecisionRequest} to be used as input to {@link BasePdpEngine}
 */
public final class ImmutablePdpDecisionRequest implements IndividualPdpDecisionRequest
{

	private final Map<AttributeGUID, Bag<?>> attributes;
	private final Map<String, XdmNode> extraContentsByCategory;
	private final boolean isApplicablePolicyListReturned;

	/**
	 * Create new instance
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param extraContentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param includedInResult
	 *            attributes to be include in the final Result
	 * @param returnApplicablePolicies
	 *            return list of applicable policy identifiers; equivalent of XACML Request's ReturnPolicyIdList flag
	 */
	ImmutablePdpDecisionRequest(final Map<AttributeGUID, Bag<?>> namedAttributes, final Map<String, XdmNode> extraContentNodesByCategory, final boolean returnApplicablePolicies)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = namedAttributes == null ? null : HashCollections.newImmutableMap(namedAttributes);
		extraContentsByCategory = extraContentNodesByCategory == null ? null : HashCollections.newImmutableMap(extraContentNodesByCategory);
		this.isApplicablePolicyListReturned = returnApplicablePolicies;
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
		return isApplicablePolicyListReturned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[namedAttributes=" + attributes + ", extraContentsByCategory=" + extraContentsByCategory + ", returnApplicablePolicyIdList=" + isApplicablePolicyListReturned + "]";
	}

}
