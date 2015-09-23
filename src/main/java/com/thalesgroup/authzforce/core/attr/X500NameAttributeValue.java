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
package com.thalesgroup.authzforce.core.attr;

import javax.security.auth.x500.X500Principal;

/**
 * Representation of an X.500 Directory Name.
 * 
 */
public class X500NameAttributeValue extends SimpleAttributeValue<String, X500NameAttributeValue>
{

	/**
	 * XACML datatype URI
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<X500NameAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<X500NameAttributeValue>(X500NameAttributeValue.class, TYPE_URI)
	{

		@Override
		protected X500NameAttributeValue getInstance(String val)
		{
			return new X500NameAttributeValue(val);
		}

	};

	/**
	 * Creates a new <code>X500NameAttributeValue</code> that represents the value supplied.
	 * 
	 * @param value
	 *            the X500 Name to be represented
	 * @throws IllegalArgumentException
	 *             if value does not correspond to a valid XACML X500Name
	 */
	public X500NameAttributeValue(X500Principal value) throws IllegalArgumentException
	{
		this(value.toString());
	}

	/**
	 * Returns a new <code>X500NameAttributeValue</code> that represents the X500 Name value
	 * indicated by the string provided.
	 * 
	 * @param value
	 *            a string representing the desired value
	 * @throws IllegalArgumentException
	 *             if value is not a valid XACML X500Name
	 */
	public X500NameAttributeValue(String value) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, value);
	}

	@Override
	protected String parse(String stringForm)
	{
		return new X500Principal(stringForm).getName(X500Principal.CANONICAL);
	}

	/**
	 * Implements XACML function 'urn:oasis:names:tc:xacml:1.0:function:x500Name-match' with this as
	 * first argument.
	 * 
	 * @param other
	 *            the second argument
	 * @return true if and only if this matches some terminal sequence of RDNs from the
	 *         <code>other</other>'s value when compared using x500Name-equal.
	 */
	public boolean match(X500NameAttributeValue other)
	{
		return other.value.endsWith(this.value);
	}

	@Override
	public X500NameAttributeValue one()
	{
		return this;
	}

}
