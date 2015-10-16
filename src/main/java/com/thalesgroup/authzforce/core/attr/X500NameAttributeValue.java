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

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

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
		public X500NameAttributeValue getInstance(String val)
		{
			return new X500NameAttributeValue(val);
		}

	};

	private final LdapName ldapName;

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
		try
		{
			this.ldapName = new LdapName(value);
		} catch (InvalidNameException e)
		{
			throw new IllegalArgumentException("Invalid value (X.500 Name) for datatype: " + TYPE_URI, e);
		}
	}

	@Override
	protected String parse(String stringForm)
	{
		/*
		 * The result value SHALL be the
		 * "string in the form it was originally represented in XML form" to make sure the
		 * string-from-x500Name function works as specified in the spec.
		 */
		return stringForm;
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
		/*
		 * As the Javadoc says,
		 * "The right most RDN is at index 0, and the left most RDN is at index n-1. For example, the distinguished name: "
		 * CN=Steve Kille, O=Isode Limited, C=GB
		 * " is numbered in the following sequence ranging from 0 to 2: {C=GB, O=Isode Limited, CN=Steve Kille}"
		 * Therefore RDNs are in reverse order of declaration in the string representation, so to
		 * check the match against a terminal sequence of RDNs, we don't use the endsWith()
		 * function, but startsWith()
		 */
		return other.ldapName.startsWith(this.ldapName.getRdns());
	}

	private int hashCode = 0;

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.ldapName.hashCode();
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}
		final X500NameAttributeValue other = (X500NameAttributeValue) obj;
		/*
		 * This equals() has the same effect as the algorithm described in the spec
		 */
		return ldapName.equals(other.ldapName);
	}

	@Override
	public X500NameAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
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
