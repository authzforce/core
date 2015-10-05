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
package com.thalesgroup.authzforce.core.eval;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression.Datatype;

/**
 * Bag datatype for bags of primitive datatypes
 * 
 * @param <AV>
 */
public class BagDatatype<AV extends AttributeValue<AV>> extends Expression.Datatype<Bag<AV>>
{

	private static final IllegalArgumentException NULL_BAG_ELEMENT_TYPE_EXCEPTION = new IllegalArgumentException("Undefined bag elementType arg");

	/**
	 * Bag datatype ID, for internal identification purposes. This is an invalid URI on purpose, to
	 * avoid conflict with any custom XACML datatype URI (datatype extension).
	 */
	private static String ID = "#BAG#";

	/**
	 * Bad datatype constructor, same {@link Datatype#Datatype(Class, String, Datatype)}, except the
	 * last parameter is mandatory (non-null value)
	 * 
	 * @param bagClass
	 * @param elementType
	 * @throws IllegalArgumentException
	 *             if {@code elementType == null}
	 */
	public BagDatatype(Class<Bag<AV>> bagClass, Datatype<AV> elementType) throws IllegalArgumentException
	{
		super(bagClass, ID, elementType);
		if (elementType == null)
		{
			throw NULL_BAG_ELEMENT_TYPE_EXCEPTION;
		}
	}

	/**
	 * Returns the bag element datatype (datatype of every element in a bag of this datatype)
	 * 
	 * @return bag element datatype
	 */
	public Datatype<AV> getElementType()
	{
		return (Datatype<AV>) this.subTypeParam;
	}
}