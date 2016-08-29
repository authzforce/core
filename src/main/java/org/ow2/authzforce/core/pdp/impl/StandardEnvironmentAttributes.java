/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;
import org.ow2.authzforce.xacml.identifiers.XACMLCategory;

/**
 * 10.2.5 Attributes
 * 
 * The implementation MUST support the attributes associated with the following identifiers as specified by XACML. If
 * values for these attributes are not present in the decision request, then their values MUST be supplied by the
 * context handler. So, unlike most other attributes, their semantics are not transparent to the PDP.
 */
final class StandardEnvironmentAttributes
{

	private StandardEnvironmentAttributes()
	{
		// prevent instantiation
	}

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-time
	 */
	static final AttributeGUID CURRENT_TIME_ATTRIBUTE_GUID = new AttributeGUID(
			XACMLCategory.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value(), null,
			XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_TIME.value());

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-date
	 */
	static final AttributeGUID CURRENT_DATE_ATTRIBUTE_GUID = new AttributeGUID(
			XACMLCategory.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value(), null,
			XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATE.value());

	/**
	 * urn:oasis:names:tc:xacml:1.0:environment:current-dateTime
	 */
	static final AttributeGUID CURRENT_DATETIME_ATTRIBUTE_GUID = new AttributeGUID(
			XACMLCategory.XACML_3_0_ENVIRONMENT_CATEGORY_ENVIRONMENT.value(), null,
			XACMLAttributeId.XACML_1_0_ENVIRONMENT_CURRENT_DATETIME.value());
}
