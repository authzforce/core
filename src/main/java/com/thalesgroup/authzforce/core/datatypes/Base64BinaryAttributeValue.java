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
package com.thalesgroup.authzforce.core.datatypes;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;

/**
 * Representation of an xs:base64Binary value. This class supports parsing xs:base64Binary values.
 * All objects of this class are immutable and all methods of the class are thread-safe. The choice
 * of the Java type byte[] is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 */
public final class Base64BinaryAttributeValue extends SimpleAttributeValue<byte[], Base64BinaryAttributeValue>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#base64Binary";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<Base64BinaryAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<Base64BinaryAttributeValue>(Base64BinaryAttributeValue.class, TYPE_URI)
	{
		@Override
		public Base64BinaryAttributeValue getInstance(String val)
		{
			return new Base64BinaryAttributeValue(val);
		}

	};

	/**
	 * Creates instance from lexical representation of xs:base64Binary
	 * 
	 * @param val
	 *            string representation of xs:base64Binary
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this value datatype
	 */
	public Base64BinaryAttributeValue(String val) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, val);
	}

	@Override
	protected byte[] parse(String stringForm)
	{
		return DatatypeConverter.parseBase64Binary(stringForm);
	}

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Arrays.hashCode(value);
		}

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

		if (!(obj instanceof Base64BinaryAttributeValue))
		{
			return false;
		}

		final Base64BinaryAttributeValue other = (Base64BinaryAttributeValue) obj;

		/*
		 * if (value == null) { if (other.value != null) { return false; } } else
		 */
		return Arrays.equals(value, other.value);
	}

	@Override
	public Base64BinaryAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

}
