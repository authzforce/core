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

import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Representation of an xs:string value. This class supports parsing xs:string values. All objects
 * of this class are immutable and all methods of the class are thread-safe.
 */
public class StringAttributeValue extends SimpleAttributeValue<String, StringAttributeValue> implements Comparable<StringAttributeValue>
{

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#string";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<StringAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<StringAttributeValue>(StringAttributeValue.class, TYPE_URI)
	{

		@Override
		public StringAttributeValue getInstance(String val)
		{
			return new StringAttributeValue(val);
		}

	};

	private static final StringAttributeValue TRUE = new StringAttributeValue("true");
	private static final StringAttributeValue FALSE = new StringAttributeValue("false");

	/**
	 * Creates a new <code>StringAttributeValue</code> that represents the String value supplied.
	 * 
	 * @param value
	 *            the <code>String</code> value to be represented
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xs:string
	 */
	public StringAttributeValue(String value) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, value);
	}

	@Override
	public int compareTo(StringAttributeValue o)
	{
		return this.value.compareTo(o.value);
	}

	/**
	 * Same as {@link String#equalsIgnoreCase(String)} on attribute values
	 * 
	 * @param otherAttribute
	 * @return true if the other attribute value is not null and it represents an equivalent String
	 *         ignoring case; false otherwise
	 */
	public boolean equalsIgnoreCase(StringAttributeValue otherAttribute)
	{
		return this.value.equalsIgnoreCase(otherAttribute.value);
	}

	@Override
	protected String parse(String stringForm)
	{
		return DatatypeConverter.parseString(stringForm);
	}

	/**
	 * @see String#trim()
	 * @return StringAttributeValue with value resulting from <code>value.trim()</code>
	 */
	public StringAttributeValue trim()
	{
		final String result = value.trim();
		return result == value ? this : new StringAttributeValue(result);
	}

	/**
	 * @see String#toLowerCase(Locale)
	 * @param locale
	 *            Locale
	 * @return StringAttributeValue with value resulting from <code>value.toLowerCase(L)</code>
	 */
	public StringAttributeValue toLowerCase(Locale locale)
	{
		final String result = value.toLowerCase(locale);
		return result == value ? this : new StringAttributeValue(result);
	}

	/**
	 * Get string representation of boolean
	 * 
	 * @param value
	 *            boolean
	 * @return string equivalent ("true" or "false")
	 */
	public static StringAttributeValue getInstance(Boolean value)
	{
		return value ? TRUE : FALSE;
	}

	@Override
	public StringAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

}
