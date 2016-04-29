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

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:boolean value. This class supports parsing xs:boolean values. All objects of this class are immutable and all methods of the class are thread-safe. The choice of the Java
 * type boolean is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 */
public final class BooleanValue extends SimpleValue<Boolean>
{
	private final int hashCode;

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#boolean";

	/**
	 * Single instance of BooleanAttributeValue that represents true. Initialized by the static initializer below.
	 */
	public static final BooleanValue TRUE = new BooleanValue(true);

	/**
	 * Single instance of BooleanAttributeValue that represents false. Initialized by the static initializer below.
	 */
	public static final BooleanValue FALSE = new BooleanValue(false);

	/**
	 * Convert a boolean value from string, according to the XML Schema definition. Adapted from {@link net.sf.saxon.value.BooleanValue#fromString(CharSequence)}, but without whitespace trimming. This
	 * is meant to replace {@link DatatypeConverter#parseBoolean(String)} which is flawed and does not comply with XSD definition of boolean type as of now (JDK7/8). See
	 * https://java.net/jira/browse/JAXB-901, and https://java.net/jira/browse/JAXB-902. E.g. DatatypeConverter.parseBoolean("not") throws NullPointerException instead of IllegalArgumentException as
	 * expected according to javadoc.
	 * 
	 * @param s
	 *            XSD-compliant string representation of boolean
	 * @return boolean value corresponding to {@code s}
	 * @throws IllegalArgumentException
	 *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes for xsd:boolean.
	 */
	public static BooleanValue getInstance(String s) throws IllegalArgumentException
	{
		// implementation designed to avoid creating new objects
		// contrary to Saxon's original code, we don't allow whitespaces to apply the XML schema
		// spec
		// strictly
		// s = Whitespace.trimWhitespace(s);
		switch (s.length())
		{
		case 1:
			char c = s.charAt(0);
			if (c == '1')
			{
				return TRUE;
			}

			if (c == '0')
			{
				return FALSE;
			}
			break;

		case 4:
			if (s.charAt(0) == 't' && s.charAt(1) == 'r' && s.charAt(2) == 'u' && s.charAt(3) == 'e')
			{
				return TRUE;
			}
			break;

		case 5:
			if (s.charAt(0) == 'f' && s.charAt(1) == 'a' && s.charAt(2) == 'l' && s.charAt(3) == 's' && s.charAt(4) == 'e')
			{
				return FALSE;
			}
			break;

		default:
		}

		throw new IllegalArgumentException("The string '" + (s.length() > 5 ? (s.substring(0, 5) + "... (content omitted)") : s) + "' is not a valid xs:boolean value.");
	}

	/**
	 * Creates a new <code>BooleanAttributeValue</code> that represents the boolean value supplied.
	 * <p>
	 * This constructor is private because it should not be used by anyone other than the static initializer in this class. Instead, please use one of the getInstance methods, which will ensure that
	 * only two BooleanAttributeValue objects are created, thus avoiding excess object creation.
	 */
	private BooleanValue(boolean value)
	{
		super(TYPE_URI, value);
		hashCode = this.value.hashCode();
	}

	/**
	 * Get BooleanAttributeValue.TRUE (resp. FALSE) instance if <code>b</code> (resp. if ! <code>b</code>)
	 * 
	 * @param b
	 *            boolean input
	 * @return instance
	 */
	public static BooleanValue valueOf(boolean b)
	{
		return b ? TRUE : FALSE;
	}

	/**
	 * not(this)
	 * 
	 * @return <code>!value</code>
	 */
	public BooleanValue not()
	{
		return value ? FALSE : TRUE;
	}

	// public static void main(String[] args)
	// {
	// System.out.println(fromString("not"));
	// }

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BooleanValue))
		{
			return false;
		}

		final BooleanValue other = (BooleanValue) obj;

		/*
		 * if (value == null) { if (other.value != null) { return false; } } else
		 */
		/*
		 * WARNING: this part is not correct for array comparison, so we need to override equals if V is an array type.
		 */
		return value == other.value.booleanValue();
	}

	@Override
	public String printXML()
	{
		return DatatypeConverter.printBoolean(this.value);
	}
}
