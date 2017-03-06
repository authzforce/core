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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;

import com.google.common.collect.Maps;

/**
 * 10.2.5 Attributes
 * 
 * The implementation MUST support the attributes associated with the following identifiers as specified by XACML. If values for these attributes are not present in the decision request, then their
 * values MUST be supplied by the context handler. So, unlike most other attributes, their semantics are not transparent to the PDP.
 */
public enum StandardEnvironmentAttribute
{
	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-time
	 */
	CURRENT_TIME(new AttributeGUID(XACMLAttributeCategory.XACML_3_0_ENVIRONMENT.value(), Optional.empty(), XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_TIME.value())),

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-date
	 */
	CURRENT_DATE(new AttributeGUID(XACMLAttributeCategory.XACML_3_0_ENVIRONMENT.value(), Optional.empty(), XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATE.value())),

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-dateTime
	 */
	CURRENT_DATETIME(new AttributeGUID(XACMLAttributeCategory.XACML_3_0_ENVIRONMENT.value(), Optional.empty(), XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATETIME.value()));

	private final AttributeGUID attributeGUID;

	private StandardEnvironmentAttribute(final AttributeGUID attributeGUID)
	{
		this.attributeGUID = attributeGUID;
	}

	/**
	 * Get attribute GUID
	 * 
	 * @return attribute GUID (AttributeId, Issuer, Category)
	 */
	public AttributeGUID getGUID()
	{
		return this.attributeGUID;
	}

	private static final Map<AttributeGUID, StandardEnvironmentAttribute> ID_TO_STD_ATTR_MAP = Maps.uniqueIndex(Arrays.asList(StandardEnvironmentAttribute.values()),
			new com.google.common.base.Function<StandardEnvironmentAttribute, AttributeGUID>()
			{

				@Override
				public AttributeGUID apply(final StandardEnvironmentAttribute input)
				{
					assert input != null;
					return input.getGUID();
				}

			});

	/**
	 * Get the standard environment attribute corresponding to the given ID
	 * 
	 * @param attributeGUID
	 *            standard attribute ID
	 * @return StandardEnvironmentAttribute corresponding to given ID, or null if there is no standard environment attribute with such ID
	 */
	public static StandardEnvironmentAttribute getInstance(final AttributeGUID attributeGUID)
	{
		return ID_TO_STD_ATTR_MAP.get(attributeGUID);
	}
}
