/*
 * Copyright 2012-2022 THALES.
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

import com.google.common.collect.Maps;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Attributes defined in Appendix B.5 (Resource attributes) with standard fixed data-type
 */
public enum StandardResourceAttribute
{
	/**
	 * urn:oasis:names:tc:xacml:2.0:resource:target-namespace
	 */
	TARGET_NAMESPACE(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_3_0_RESOURCE.value(), Optional.empty(), XacmlAttributeId.XACML_2_0_RESOURCE_TARGET_NAMESPACE.value()), StandardDatatypes.ANYURI);

	private final AttributeFqn attributeFqn;
	private final AttributeDatatype<?> attributeDatatype;

	StandardResourceAttribute(final AttributeFqn attributeFqn, final AttributeDatatype<?> datatype)
	{
		this.attributeFqn = attributeFqn;
		this.attributeDatatype = datatype;
	}

	/**
	 * Get attribute GUID
	 * 
	 * @return attribute GUID (AttributeId, Issuer, Category)
	 */
	public AttributeFqn getFQN()
	{
		return this.attributeFqn;
	}

	/**
	 * Get attribute data-type
	 *
	 * @return attribute data-type
	 */
	public AttributeDatatype<?> getDatatype() {
		return this.attributeDatatype;
	}

	private static final Map<AttributeFqn, StandardResourceAttribute> ID_TO_STD_ATTR_MAP = Maps.uniqueIndex(Arrays.asList(StandardResourceAttribute.values()),
			input ->
			{
				assert input != null;
				return input.getFQN();
			});

	/**
	 * Get the standard environment attribute corresponding to the given ID
	 * 
	 * @param attributeFqn
	 *            standard attribute ID
	 * @return StandardEnvironmentAttribute corresponding to given ID, or null if there is no standard environment attribute with such ID
	 */
	public static StandardResourceAttribute getInstance(final AttributeFqn attributeFqn)
	{
		return ID_TO_STD_ATTR_MAP.get(attributeFqn);
	}
}
