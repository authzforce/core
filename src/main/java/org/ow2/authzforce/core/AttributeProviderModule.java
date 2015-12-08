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

import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.Datatype;

/**
 * Attribute provider module, in charge of retrieving attribute values from a specific source in a specific way.
 * 
 */
public interface AttributeProviderModule
{
	/**
	 * Provides values of attribtues matching the given AttributeDesignator data. If no value found, but no other error occurred, an empty bag is returned. This
	 * method may need to invoke the context data to look for other attribute values, so a module writer must take care not to create a scenario that loops
	 * forever.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/id/category, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD
	 * anyURI [1], not in java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * If this is an AttributeSelector-only Provider module, always return null.
	 * 
	 * @param attributeGUID
	 *            the global identifier (Category,Issuer,AttributeId) of the attribute to find
	 * @param context
	 *            the representation of the request data
	 * @param attributeDatatype
	 *            expected return datatype ( {@code AV is the expected type of every element in the bag})
	 * 
	 * @return the bag of attribute values
	 * @throws IndeterminateEvaluationException
	 *             if some error occurs, esp. error retrieving the attribute values
	 */
	<AV extends AttributeValue> Bag<AV> get(AttributeGUID attributeGUID, Datatype<AV> attributeDatatype, EvaluationContext context)
			throws IndeterminateEvaluationException;

	/**
	 * Returns a non-null non-empty <code>Set</code> of <code>AttributeDesignator</code>s provided/supported by this module.
	 * 
	 * @return a non-null non-empty <code>Set</code> of supported <code>AttributeDesignatorType</code>s
	 */
	Set<AttributeDesignatorType> getProvidedAttributes();

}
