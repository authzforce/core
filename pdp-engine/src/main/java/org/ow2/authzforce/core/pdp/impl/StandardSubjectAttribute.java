/*
 * Copyright 2012-2023 THALES.
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
 * Attributes defined in Appendix B.4 (Subject attributes) with standard fixed data-type
 */
public enum StandardSubjectAttribute
{
	/**
	 * Access subject category attribute:
	 * <p>
	 * urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address
	 */
	ACCESS_SUBJECT_IP_ADDRESS(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_IP_ADDRESS.value()), StandardDatatypes.IPADDRESS),

	/**
	 * Access subject category attribute:
	 * <p>
	 * urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name
	 */
	ACCESS_SUBJECT_DNS_NAME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_DNS_NAME.value()), StandardDatatypes.DNSNAME),

	/**
	 * Intermediary subject category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address
	 */
	INTERMEDIARY_SUBJECT_IP_ADDRESS(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_INTERMEDIARY_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_IP_ADDRESS.value()), StandardDatatypes.IPADDRESS),

	/**
	 * Intermediary subject category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name
	 */
	INTERMEDIARY_SUBJECT_DNS_NAME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_INTERMEDIARY_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_DNS_NAME.value()), StandardDatatypes.DNSNAME),

	/**
	 * Subject codebase category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address
	 */
	SUBJECT_CODEBASE_IP_ADDRESS(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_SUBJECT_CODEBASE.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_IP_ADDRESS.value()), StandardDatatypes.IPADDRESS),

	/**
	 * Subject codebase category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name
	 */
	SUBJECT_CODEBASE_DNS_NAME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_SUBJECT_CODEBASE.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_DNS_NAME.value()), StandardDatatypes.DNSNAME),

	/**
	 * Recipient subject category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address
	 */
	RECIPIENT_SUBJECT_IP_ADDRESS(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_RECIPIENT_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_IP_ADDRESS.value()), StandardDatatypes.IPADDRESS),

	/**
	 * Recipient subject category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name
	 */
	RECIPIENT_SUBJECT_DNS_NAME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_RECIPIENT_SUBJECT.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_DNS_NAME.value()), StandardDatatypes.DNSNAME),

	/**
	 * Subject's requesting machine category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address
	 */
	SUBJECT_REQUESTING_MACHINE_IP_ADDRESS(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_SUBJECT_REQUESTING_MACHINE.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_IP_ADDRESS.value()), StandardDatatypes.IPADDRESS),

	/**
	 * Subject's requesting machine category attribute: urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name
	 */
	SUBJECT_REQUESTING_MACHINE_DNS_NAME(AttributeFqns.newInstance(XacmlAttributeCategory.XACML_1_0_SUBJECT_REQUESTING_MACHINE.value(), Optional.empty(), XacmlAttributeId.XACML_1_0_SUBJECT_DNS_NAME.value()), StandardDatatypes.DNSNAME);


	private final AttributeFqn attributeFqn;
	private final AttributeDatatype<?> attributeDatatype;

	/**
	 * Constructor
	 * @param attributeFqn attribute name
	 * @param datatype attribute datatype
	 */
	StandardSubjectAttribute(final AttributeFqn attributeFqn, final AttributeDatatype<?> datatype)
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

	private static final Map<AttributeFqn, StandardSubjectAttribute> ID_TO_STD_ATTR_MAP = Maps.uniqueIndex(Arrays.asList(StandardSubjectAttribute.values()),
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
	public static StandardSubjectAttribute getInstance(final AttributeFqn attributeFqn)
	{
		return ID_TO_STD_ATTR_MAP.get(attributeFqn);
	}
}
