/**
 * Copyright (C) 2012-2015 Thales Services SAS.
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
package org.ow2.authzforce.core;

import org.ow2.authzforce.core.expression.AttributeDesignator;
import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.Datatype;

/**
 * Attribute provider used to resolve {@link AttributeDesignator}s.
 * 
 */
public interface AttributeProvider
{
	/**
	 * Provides values of the attribute matching the given designator data. If no value found, but no other error occurred, an empty bag is returned.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/category/id, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD
	 * anyURI [1], not in java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeGUID
	 *            the global identifier (Category,Issuer,AttributeId) of the attribute to find
	 * @param context
	 *            the representation of the request data
	 * @param attributeDatatype
	 *            attribute datatype (expected datatype of every element in the result bag} )
	 * @return the result of retrieving the attribute, which will be a bag of values of type defined by {@code attributeDatatype}
	 * @throws IndeterminateEvaluationException
	 *             if any error finding the attribute value(s)
	 */
	<AV extends AttributeValue> Bag<AV> get(AttributeGUID attributeGUID, Datatype<AV> attributeDatatype, EvaluationContext context)
			throws IndeterminateEvaluationException;

}
