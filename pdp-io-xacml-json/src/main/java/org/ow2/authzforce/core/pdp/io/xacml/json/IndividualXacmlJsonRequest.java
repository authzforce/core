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
package org.ow2.authzforce.core.pdp.io.xacml.json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;

import com.google.common.collect.ImmutableList;

/**
 * (Immutable) Individual decision request using XACML-schema-derived JAXB-annotated objects (XACML Attributes elements), as defined by Multiple Decision Profile of XACML. This differs from
 * {@link DecisionRequest} only by the fact that the XACML request may require in addition, esp. in a Multiple Decision, that a sequence of Attributes elements from the request be included in the
 * XACML Result as well, in order for the requester to correlate with the Attributes elements in the request, i.e. the individual requests.
 *
 */
public final class IndividualXacmlJsonRequest implements DecisionRequest
{
	private final ImmutableDecisionRequest baseRequest;
	private final List<JSONObject> attributesByCategoryToBeReturned;

	/**
	 * Creates instance from an XACML-agnostic request
	 * 
	 * @param baseRequest
	 *            base request in XACML-agnostic model
	 * @param attributesToBeReturned
	 *            attributes to be included in corresponding XACML Result
	 */
	public IndividualXacmlJsonRequest(final ImmutableDecisionRequest baseRequest, final ImmutableList<JSONObject> attributesToBeReturned)
	{
		assert baseRequest != null;

		this.baseRequest = baseRequest;
		this.attributesByCategoryToBeReturned = attributesToBeReturned == null ? Collections.emptyList() : attributesToBeReturned;
	}

	@Override
	public Map<AttributeFqn, AttributeBag<?>> getNamedAttributes()
	{
		return baseRequest.getNamedAttributes();
	}

	@Override
	public Map<String, XdmNode> getExtraContentsByCategory()
	{
		return baseRequest.getExtraContentsByCategory();
	}

	@Override
	public boolean isApplicablePolicyIdListReturned()
	{
		return baseRequest.isApplicablePolicyIdListReturned();
	}

	/**
	 * Category objects to be included in corresponding result.
	 * 
	 * @return Category objects to be included in the decision result; non-null empty list if none
	 */
	public List<JSONObject> getAttributesByCategoryToBeReturned()
	{
		return this.attributesByCategoryToBeReturned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		/*
		 * attributesToBeReturned ignored for the PdpDecisionRequest fields to be only ones used for matching keys in DecisionCaches
		 */
		return baseRequest.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof IndividualXacmlJsonRequest))
		{
			return false;
		}

		/*
		 * attributesToBeReturned ignored for the PdpDecisionRequest fields to be only ones used for matching keys in DecisionCaches
		 */
		return baseRequest.equals(((IndividualXacmlJsonRequest) obj).baseRequest);
	}

}
