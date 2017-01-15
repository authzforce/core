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
package org.ow2.authzforce.core.pdp.impl;

import java.util.Arrays;
import java.util.Map;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;
import org.ow2.authzforce.xacml.identifiers.XACMLCategory;

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
	CURRENT_TIME(new AttributeGUID(XACMLCategory.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value(), null, XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_TIME.value())),

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-date
	 */
	CURRENT_DATE(new AttributeGUID(XACMLCategory.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value(), null, XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATE.value())),

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-dateTime
	 */
	CURRENT_DATETIME(new AttributeGUID(XACMLCategory.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value(), null, XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATETIME.value()));

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
