/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.audit;

import java.net.URI;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

public class AttributesResolved {
	
	
	private URI attributeId;
	
	private AttributeValueType attributeValue;

	public URI getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(URI attributeId) {
		this.attributeId = attributeId;
	}

	public AttributeValueType getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(AttributeValueType attributeValue) {
		this.attributeValue = attributeValue;
	}

}
