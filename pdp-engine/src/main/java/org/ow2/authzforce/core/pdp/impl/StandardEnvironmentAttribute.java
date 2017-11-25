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

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

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
	CURRENT_TIME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_3_0_ENVIRONMENT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_TIME.value())),

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-date
	 */
	CURRENT_DATE(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_3_0_ENVIRONMENT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATE.value())),

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-dateTime
	 */
	CURRENT_DATETIME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_3_0_ENVIRONMENT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATETIME.value()));

	private final AttributeFqn AttributeFqn;

	private StandardEnvironmentAttribute(final AttributeFqn AttributeFqn)
	{
		this.AttributeFqn = AttributeFqn;
	}

	/**
	 * Get attribute GUID
	 * 
	 * @return attribute GUID (AttributeId, Issuer, Category)
	 */
	public AttributeFqn getFQN()
	{
		return this.AttributeFqn;
	}

	private static final Map<AttributeFqn, StandardEnvironmentAttribute> ID_TO_STD_ATTR_MAP = Maps.uniqueIndex(Arrays.asList(StandardEnvironmentAttribute.values()),
			new com.google.common.base.Function<StandardEnvironmentAttribute, AttributeFqn>()
			{

				@Override
				public AttributeFqn apply(final StandardEnvironmentAttribute input)
				{
					assert input != null;
					return input.getFQN();
				}

			});

	/**
	 * Get the standard environment attribute corresponding to the given ID
	 * 
	 * @param AttributeFqn
	 *            standard attribute ID
	 * @return StandardEnvironmentAttribute corresponding to given ID, or null if there is no standard environment attribute with such ID
	 */
	public static StandardEnvironmentAttribute getInstance(final AttributeFqn AttributeFqn)
	{
		return ID_TO_STD_ATTR_MAP.get(AttributeFqn);
	}
}
