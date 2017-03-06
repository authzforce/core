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
