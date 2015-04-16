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
package com.thalesgroup.authzforce.core;

import java.io.Serializable;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.sun.xacml.attr.AttributeProxy;
import com.sun.xacml.attr.xacmlv3.AttributeValue;

public abstract class AbstractPrimitiveAttributeProxy implements AttributeProxy
{

	@Override
	public AttributeValue getInstance(AttributeValueType value) throws Exception
	{
		 final List<Serializable> valList = value.getContent();
		 if(valList == null ) {
			 throw new IllegalArgumentException("Undefined AttributeValue");
		 }
		 
		 if(valList.isEmpty()) {
			 throw new IllegalArgumentException("AttributeValue content empty");
		 }
		 
		 final Serializable val0 = valList.get(0);
		 if(!(val0 instanceof String)) {
			 throw new IllegalArgumentException("Invalid type AttributeValue content: " + val0.getClass()+" (Element node). Expected: " + String.class + " (Text node)");
		 }
		 
		 return getInstance((String) val0);
	}
	
	/**
	 * Get AttributeValue instance from String representation of primitive type of value
	 * @param value
	 * @return AttributeValue instance
	 */
	public abstract AttributeValue getInstance(String value) throws Exception;	
}
