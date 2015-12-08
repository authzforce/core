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
package org.ow2.authzforce.core.combining;

import org.ow2.authzforce.core.Decidable;
import org.ow2.authzforce.core.PdpExtensionRegistry;

import com.sun.xacml.UnknownIdentifierException;

/**
 * Provides a registry mechanism for adding and retrieving combining algorithms.
 */
public interface CombiningAlgRegistry extends PdpExtensionRegistry<CombiningAlg<?>>
{

	/**
	 * Tries to return the correct combinging algorithm based on the given algorithm ID.
	 * 
	 * @param algId
	 *            the identifier by which the algorithm is known
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI for example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param combinedElementType
	 *            type of combined element
	 * 
	 * @return a combining algorithm
	 * 
	 * @throws UnknownIdentifierException
	 *             algId is unknown
	 */
	<T extends Decidable> CombiningAlg<T> getAlgorithm(String algId, Class<T> combinedElementType) throws UnknownIdentifierException;

}
