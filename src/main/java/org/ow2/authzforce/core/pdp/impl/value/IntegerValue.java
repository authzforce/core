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

import java.math.BigInteger;
import java.util.Deque;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:integer value. This class supports parsing xs:integer values. All objects of this class are immutable and all methods of the class are thread-safe. The actual type of the
 * underlying value is BigInteger. See https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
 * 
 */
public final class IntegerValue extends NumericValue<BigInteger, IntegerValue> implements Comparable<IntegerValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#integer";

	/*
	 * WARNING: these static variables must be declared before ZERO static variable, because the latter needs the former to get initialized, and static variables are initialized in order of
	 * declaration.
	 */
	private static final BigInteger MAX_INT_AS_BIGINT = BigInteger.valueOf(Integer.MAX_VALUE);

	private static final BigInteger MIN_INT_AS_BIGINT = BigInteger.valueOf(Integer.MIN_VALUE);

	/**
	 * Value zero
	 */
	public static final IntegerValue ZERO = new IntegerValue(BigInteger.ZERO);

	/**
	 * Creates instance from integer argument
	 * 
	 * @param val
	 *            Java equivalent of xsd:integer
	 */
	public IntegerValue(BigInteger val)
	{
		super(TYPE_URI, val);
	}

	/**
	 * Creates instance from long argument, mostly for easy writing of tests
	 * <p>
	 * Be aware that type long is not equivalent to xsd:integer type, BigInteger is. See https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
	 * </p>
	 * 
	 * @param val
	 *            integer value as Java long
	 */
	public IntegerValue(long val)
	{
		this(BigInteger.valueOf(val));
	}

	/**
	 * Creates instance from lexical representation of xsd:integer
	 * 
	 * @param val
	 *            String representation of xsd:integer
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:integer
	 */
	public IntegerValue(String val) throws IllegalArgumentException
	{
		this(DatatypeConverter.parseInteger(val));
	}

	@Override
	public int compareTo(IntegerValue o)
	{
		return this.value.compareTo(o.value);
	}

	@Override
	public IntegerValue abs()
	{
		return new IntegerValue(this.value.abs());
	}

	@Override
	public IntegerValue add(Deque<IntegerValue> others)
	{
		BigInteger sum = value;
		while (!others.isEmpty())
		{
			sum = sum.add(others.poll().value);
		}

		return new IntegerValue(sum);
	}

	@Override
	public IntegerValue multiply(Deque<IntegerValue> others)
	{
		BigInteger product = value;
		while (!others.isEmpty())
		{
			product = product.multiply(others.poll().value);
		}

		return new IntegerValue(product);
	}

	@Override
	public IntegerValue divide(IntegerValue divisor) throws ArithmeticException
	{
		return new IntegerValue(value.divide(divisor.value));
	}

	@Override
	public IntegerValue subtract(IntegerValue subtractedVal)
	{
		return new IntegerValue(value.subtract(subtractedVal.value));
	}

	/**
	 * Returns this % <code>divisor</code>
	 * 
	 * @param divisor
	 *            second argument
	 * @return this % divisor using {@link BigInteger#remainder(BigInteger)}
	 * @throws ArithmeticException
	 *             if divisor is zero
	 */
	public IntegerValue remainder(IntegerValue divisor) throws ArithmeticException
	{
		return new IntegerValue(value.remainder(divisor.value));
	}

	/**
	 * Converts this integer to a double as specified by {@link BigInteger#doubleValue()}
	 * 
	 * @return <code>this</code> as a double
	 * @throws IllegalArgumentException
	 *             if this integer is outside the range which can be represented by a double
	 */
	public double doubleValue() throws IllegalArgumentException
	{
		final double doubleVal = value.doubleValue();
		if (Double.isInfinite(doubleVal) || Double.isNaN(doubleVal))
		{
			// this BigInteger has too great a magnitude to represent as a double
			throw new IllegalArgumentException("integer argument outside the range which can be represented by a double");
		}

		return doubleVal;
	}

	/**
	 * 
	 * Converts this to an int, checking for lost information. If the value of this BigInteger is out of the range of the int type, then an ArithmeticException is thrown.
	 * <p>
	 * TODO: replace with Java 8 native equivalent - BigInteger#intValueExact() - after upgrade to Java 8
	 * 
	 * @see <a href="https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow">The CERT Oracle Secure Coding Standard for Java - NUM00-J. Detect or prevent
	 *      integer overflow</a>
	 * 
	 * @return this converted to an int
	 * @throws ArithmeticException
	 *             if the value of this will not exactly fit in a int.
	 */
	public int intValueExact() throws ArithmeticException
	{
		if (value.compareTo(MAX_INT_AS_BIGINT) == 1 || value.compareTo(MIN_INT_AS_BIGINT) == -1)
		{
			throw new ArithmeticException("Integer overflow");
		}

		return value.intValue();
	}

	@Override
	public String printXML()
	{
		return DatatypeConverter.printInteger(this.value);
	}

}
