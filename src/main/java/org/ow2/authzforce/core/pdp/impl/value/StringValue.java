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

import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:string value. This class supports parsing xs:string values. All objects of this class are immutable and all methods of the class are
 * thread-safe.
 */
public final class StringValue extends SimpleValue<String> implements Comparable<StringValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#string";

	private static final StringValue TRUE = new StringValue("true");
	private static final StringValue FALSE = new StringValue("false");

	/**
	 * Convert the lexical XSD string argument into a String value, using {@link javax.xml.bind.DatatypeConverter#parseString(String)}.
	 * 
	 * @param val
	 *            A string containing a lexical representation of xsd:string
	 * @return instance
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xsd:string
	 */
	public static StringValue parse(String val) throws IllegalArgumentException
	{
		return new StringValue(DatatypeConverter.parseString(val));
	}

	/**
	 * Convert string argument - assumed a valid xsd:string into a String value. Use with caution as no xsd:string format validation is done here. For internal
	 * purposes only. If you need proper input validation, use {@link #parse(String)} instead.
	 * 
	 * @param validXsdString
	 *            A string containing a valid lexical representation of xsd:string
	 */
	public StringValue(String validXsdString)
	{
		super(TYPE_URI, validXsdString);
	}

	@Override
	public int compareTo(StringValue o)
	{
		return this.value.compareTo(o.value);
	}

	/**
	 * Same as {@link String#equalsIgnoreCase(String)} on attribute values
	 * 
	 * @param otherAttribute
	 * @return true if the other attribute value is not null and it represents an equivalent String ignoring case; false otherwise
	 */
	public boolean equalsIgnoreCase(StringValue otherAttribute)
	{
		return this.value.equalsIgnoreCase(otherAttribute.value);
	}

	/**
	 * @see String#trim()
	 * @return StringAttributeValue with value resulting from <code>value.trim()</code>
	 */
	public StringValue trim()
	{
		final String result = value.trim();
		// if the value is same as result, return itself, else return new value from result
		return result.equals(value) ? this : new StringValue(result);
	}

	/**
	 * @see String#toLowerCase(Locale)
	 * @param locale
	 *            Locale
	 * @return StringAttributeValue with value resulting from <code>value.toLowerCase(L)</code>
	 */
	public StringValue toLowerCase(Locale locale)
	{
		final String result = value.toLowerCase(locale);
		// if the value is same as result, return itself, else return new value from result
		return result.equals(value) ? this : new StringValue(result);
	}

	/**
	 * Get string representation of boolean
	 * 
	 * @param value
	 *            boolean
	 * @return string equivalent ("true" or "false")
	 */
	public static StringValue getInstance(Boolean value)
	{
		return value ? TRUE : FALSE;
	}

	/**
	 * Converts BooleanAttributeValue to String
	 * 
	 * @param value
	 *            boolean
	 * @return string equivalent ("true" or "false")
	 */
	public static StringValue getInstance(BooleanValue value)
	{
		return value == BooleanValue.TRUE ? TRUE : FALSE;
	}

	@Override
	public String printXML()
	{
		return this.value;
	}

}
