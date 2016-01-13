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
package org.ow2.authzforce.core.value;

import java.util.Objects;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Representation of an RFC 822 email address. The valid syntax for such a name is described in IETF RFC 2821, Section 4.1.2, 4019 Command Argument Syntax,
 * under the term "Mailbox". Mailbox = Local-part "@" Domain
 * <p>
 * N.B.: This is more restrictive than a generic RFC 822 name.
 */
public final class RFC822NameValue extends SimpleValue<String>
{

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";

	private final String localPart;

	private final String domainPartLowerCase;

	/**
	 * Creates instance from InternetAddress
	 * 
	 * @param address
	 */
	public RFC822NameValue(InternetAddress address)
	{
		this(address.toString());
	}

	private static String validate(String stringForm) throws IllegalArgumentException
	{
		try
		{
			final InternetAddress email = new InternetAddress(stringForm, true);
			email.validate();
		} catch (AddressException e)
		{
			throw new IllegalArgumentException("Invalid RFC822Name: " + stringForm, e);
		}

		/*
		 * The result value SHALL be the "string in the form it was originally represented in XML form" to make sure the string-from-ipAddress function works as
		 * expected
		 */
		return stringForm;
	}

	/**
	 * Creates a new <code>RFC822NameAttributeValue</code> that represents the value supplied.
	 * 
	 * @param value
	 *            the email address to be represented
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation of XACML rfc822Name
	 */
	public RFC822NameValue(String value) throws IllegalArgumentException
	{
		super(TYPE_URI, validate(value));
		/*
		 * The validation with InternetAddress class in parse() method is not enough because InternetAddress is much less restrictive than this XACML type,
		 * since it takes names without '@' such as "sun" or "sun.com" as valid.
		 */
		final String[] parts = this.value.split("@", 2);
		if (parts.length < 2)
		{
			throw new IllegalArgumentException("Invalid value for type '" + dataType + "': '" + this.value + "' missing local/domain part.");
		}

		this.localPart = parts[0];
		this.domainPartLowerCase = parts[1].toLowerCase();
	}

	// /**
	// * @return the localPart
	// */
	// public String getLocalPart()
	// {
	// return localPart;
	// }
	//
	// /**
	// * @return the domainPartLowerCase
	// */
	// public String getDomainPartLowerCase()
	// {
	// return domainPartLowerCase;
	// }

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(localPart, domainPartLowerCase);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof RFC822NameValue))
		{
			return false;
		}

		final RFC822NameValue other = (RFC822NameValue) obj;
		/*
		 * if (domainPartLowerCase == null) { if (other.domainPartLowerCase != null) { return false; } } else
		 */
		return domainPartLowerCase.equals(other.domainPartLowerCase) && localPart.equals(other.localPart);
	}

	/**
	 * Implements function 'urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match'
	 * 
	 * @param maybePartialRfc822Name
	 *            used as first argument to the function
	 * @return true if match
	 */
	public boolean match(String maybePartialRfc822Name)
	{
		final int arobaseIndex = maybePartialRfc822Name.indexOf('@');
		if (arobaseIndex != -1)
		{
			/*
			 * Case #1: arg is expected to be a complete mail address equal to this (ignore case on the domain part)
			 */
			if (arobaseIndex < 1 || arobaseIndex > maybePartialRfc822Name.length() - 2)
			{
				throw new IllegalArgumentException("Invalid first arg to function 'rfc822Name-match': " + maybePartialRfc822Name
						+ " missing local-part/domain-part");
			}

			final String otherLocalPart = maybePartialRfc822Name.substring(0, arobaseIndex);
			final String otherDomainPart = maybePartialRfc822Name.substring(arobaseIndex + 1);
			return this.localPart.equals(otherLocalPart) && this.domainPartLowerCase.equalsIgnoreCase(otherDomainPart);
		}

		if (maybePartialRfc822Name.isEmpty())
		{
			throw new IllegalArgumentException("Invalid first arg to function 'rfc822Name-match': empty string");
		}

		if (maybePartialRfc822Name.charAt(0) == '.')
		{
			// this is case #3 : a sub-domain of this domain (ignore case)
			/*
			 * Either the arg without the dot is equal to this domain-part (ignore case), or the arg is a suffix of this domain-part (with the dot! if you
			 * removed the dot, it could be a suffix witouth being a valid subdomain; e.g. ".east.sun.com" matches domain-part "isrg.east.sun.com" but must not
			 * match "northeast.sun.com" although it is a valid suffix without the dot)
			 */
			final String otherToLowerCase = maybePartialRfc822Name.toLowerCase();
			return this.domainPartLowerCase.endsWith(otherToLowerCase) || this.domainPartLowerCase.equals(otherToLowerCase.substring(1));
		}

		// this is case #2: the arg is a domain equal (ignore case) to this domain-part
		return this.domainPartLowerCase.equalsIgnoreCase(maybePartialRfc822Name);
	}

	@Override
	public String printXML()
	{
		return this.value;
	}

	// /**
	// * Test a few values
	// *
	// * @param args
	// */
	// public static void main(String[] args)
	// {
	// String[] testVals = { "", "@", ".", "x@sun.com", "y@z", ".EAST.sun.com", "SUN", "Sun.com",
	// "@Sun", "@Sun.com", "anderson@", "Anderson@", "Anderson@SUN.COM", "anderson@sun.COM",
	// "anderson@issrg.east.SUN.COM", "anderson@issrg.sun.COM", "Anderson@issrg.northeast.SUN.com"
	// };
	// for (String val : testVals)
	// {
	// try
	// {
	// RFC822NameAttributeValue attrVal = new RFC822NameAttributeValue(val);
	// System.out.println(val + " -> OK: local-part=" + attrVal.getLocalPart() + ", domain-part=" +
	// attrVal.getDomainPartLowerCase());
	// for (String arg0 : testVals)
	// {
	// try
	// {
	// boolean isMatched = attrVal.match(arg0);
	// System.out.println("x500Name-match(" + arg0 + ", " + attrVal.value + ") -> " + isMatched);
	// } catch (Exception e)
	// {
	// System.out.println("x500Name-match(" + arg0 + ", " + attrVal.value + ") -> " + e);
	// }
	// }
	// } catch (Exception e)
	// {
	// System.out.println(val + " -> KO: " + e);
	// }
	// }
	// }

}
