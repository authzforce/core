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

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:hexBinary value. This class supports parsing xs:hexBinary values. All
 * objects of this class are immutable and all methods of the class are thread-safe. The choice of
 * the Java type byte[] is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 */
public class HexBinaryAttributeValue extends SimpleAttributeValue<byte[], HexBinaryAttributeValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#hexBinary";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<HexBinaryAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<HexBinaryAttributeValue>(HexBinaryAttributeValue.class, TYPE_URI)
	{

		@Override
		protected HexBinaryAttributeValue getInstance(String val)
		{
			return new HexBinaryAttributeValue(val);
		}

	};

	/**
	 * Creates a new <code>HexBinaryAttributeValue</code> that represents the byte [] value
	 * supplied.
	 * 
	 * @param value
	 *            the <code>byte []</code> value to be represented
	 */
	public HexBinaryAttributeValue(byte[] value)
	{
		super(FACTORY.instanceDatatype, value, value);
	}

	/**
	 * Returns a new <code>HexBinaryAttributeValue</code> that represents the xsi:hexBinary value
	 * indicated by the string provided.
	 * 
	 * @param val
	 *            a string representing the desired value
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:hexBinary
	 */
	public HexBinaryAttributeValue(String val) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, val);
	}

	@Override
	protected byte[] parse(String stringForm)
	{
		return DatatypeConverter.parseHexBinary(stringForm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.attr.SimpleAttributeValue#toString(java.lang.Object)
	 */
	@Override
	public String toString(byte[] val)
	{
		return DatatypeConverter.printHexBinary(val);
	}

	@Override
	public HexBinaryAttributeValue one()
	{
		return this;
	}

}
