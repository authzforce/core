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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

/**
 * Superclass of all numeric Attribute Values (integer, double...)
 * 
 * @param <N>
 *            actual Java type of the underlying numeric value (Integer, Double...)
 * @param <T>
 *            type of result returned by arithmetic functions with this type of arguments:
 *            {@link #abs()}, {@link #add(T[], int)}, etc. Basically, we expect that arithmetic
 *            functions applied to args of type T will return a result of the same type T.
 * 
 */
public abstract class NumericAttributeValue<N extends Number, T extends NumericAttributeValue<N, T>> extends PrimitiveAttributeValue<N>
{
	protected NumericAttributeValue(String datatype, N val)
	{
		super(datatype, val, val);
	}

	/**
	 * Instantiates a numeric attribute value
	 * 
	 * @param stringForm
	 *            string representation of attribute value
	 * @throws IllegalArgumentException
	 *             if <code>val</code> is not a valid string representation for this numeric
	 *             datatype
	 */
	protected NumericAttributeValue(String datatype, String val) throws IllegalArgumentException
	{
		super(datatype, val);
	}

	/**
	 * @see PrimitiveAttributeValue#BasePrimitiveAttributeValue(AttributeValueType)
	 */
	protected NumericAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	/**
	 * Returns the absolute value of <code>this</code>. Used by the XACML "abs" functions.
	 * 
	 * @return the absolute value
	 */
	public abstract T abs();

	/**
	 * Adds numbers to this. Used by the XACML numeric *-add functions.
	 * 
	 * @param others
	 *            values to add to this value
	 * @param offset
	 *            index in <code>others</code> where to start adding values
	 * @return sum of this and the others
	 */
	public abstract T add(T[] others, int offset);

	/**
	 * Multiply <code>this</code> by other numbers, starting to multiply others from a
	 * specific offset (index in the array). Used by the XACML "multiply" functions.
	 * 
	 * @param others
	 *            other values to add
	 * @param offset
	 *            index in <code>others</code> where to start adding array values
	 * @return product of this by the others
	 */
	public abstract T multiply(T[] others, int offset);

	/**
	 * Divide <code>this</code> by some other number. Used by XACML *-divide functions.
	 * 
	 * @param divisor
	 *            number by which <code>this</code> is divided
	 * 
	 * @return the result quotient
	 * @throws ArithmeticException
	 *             if divisor is zero
	 */
	public abstract T divide(T divisor) throws ArithmeticException;

	/**
	 * Substract a number from this. Used by XACML numeric *-subtract
	 * functions.
	 * 
	 * @param subtractedVal
	 *            value to be subtracted from <code>this</code>
	 * @return this - substractedVal
	 */
	public abstract T subtract(T subtractedVal);
}
