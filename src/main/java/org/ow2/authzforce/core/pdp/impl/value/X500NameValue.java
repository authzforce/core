/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.value;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

/**
 * Representation of an X.500 Directory Name.
 *
 * 
 * @version $Id: $
 */
public final class X500NameValue extends SimpleValue<String>
{
	/**
	 * XACML datatype URI
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";

	private final LdapName ldapName;

	/**
	 * Returns a new <code>X500NameAttributeValue</code> that represents the X500 Name value indicated by the string provided.
	 *
	 * @param value
	 *            a string representing the desired value
	 * @throws java.lang.IllegalArgumentException
	 *             if value is not a valid XACML X500Name
	 */
	public X500NameValue(String value) throws IllegalArgumentException
	{
		super(TYPE_URI, value);
		try
		{
			this.ldapName = new LdapName(value);
		} catch (InvalidNameException e)
		{
			throw new IllegalArgumentException("Invalid value (X.500 Name) for datatype: " + TYPE_URI, e);
		}
	}

	/**
	 * Implements XACML function 'urn:oasis:names:tc:xacml:1.0:function:x500Name-match' with this as first argument.
	 *
	 * @param other
	 *            the second argument
	 * @return true if and only if this matches some terminal sequence of RDNs from the <code>other</other>'s value when compared using x500Name-equal.
	 */
	public boolean match(X500NameValue other)
	{
		/*
		 * As the Javadoc says, "The right most RDN is at index 0, and the left most RDN is at index n-1. For example, the distinguished name: " CN=Steve Kille,
		 * O=Isode Limited, C=GB " is numbered in the following sequence ranging from 0 to 2: {C=GB, O=Isode Limited, CN=Steve Kille}" Therefore RDNs are in
		 * reverse order of declaration in the string representation, so to check the match against a terminal sequence of RDNs, we don't use the endsWith()
		 * function, but startsWith()
		 */
		return other.ldapName.startsWith(this.ldapName.getRdns());
	}

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.ldapName.hashCode();
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof X500NameValue))
		{
			return false;
		}

		final X500NameValue other = (X500NameValue) obj;
		/*
		 * This equals() has the same effect as the algorithm described in the spec
		 */
		return ldapName.equals(other.ldapName);
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return this.value;
	}

	// For quick testing
	// public static void main(String[] args) throws InvalidNameException
	// {
	// // System.out.println(new LdapName("cn=John Smith, o=Medico Corp, c=US").equals(new
	// // LdapName("cn= John Smith,o =Medico Corp, C=US")));
	// // System.out.println(new LdapName("ou=test+cn=bob,dc =example,dc=com"));
	// System.out.println(new LdapName("cn=John Smith, o=Medico Corp, c=US").endsWith(new
	// LdapName("o=Medico Corp, c=US").getRdns()));
	// }

}
