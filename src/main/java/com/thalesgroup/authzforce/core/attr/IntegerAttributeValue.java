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

import java.math.BigInteger;
import java.util.Deque;

import javax.xml.bind.DatatypeConverter;

import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Representation of an xs:integer value. This class supports parsing xs:integer values. All objects
 * of this class are immutable and all methods of the class are thread-safe. The actual type of the
 * underlying value is BigInteger. See
 * https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
 * 
 */
public class IntegerAttributeValue extends NumericAttributeValue<BigInteger, IntegerAttributeValue> implements Comparable<IntegerAttributeValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#integer";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<IntegerAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<IntegerAttributeValue>(IntegerAttributeValue.class, TYPE_URI)
	{

		@Override
		public IntegerAttributeValue getInstance(String val)
		{
			return new IntegerAttributeValue(val);
		}

	};

	/*
	 * WARNING: these static variables must be declared before ZERO static variable, because the
	 * latter needs the former to get initialized, and static variables are initialized in order of
	 * declaration.
	 */
	private static final BigInteger MAX_INT_AS_BIGINT = BigInteger.valueOf(Integer.MAX_VALUE);

	private static final BigInteger MIN_INT_AS_BIGINT = BigInteger.valueOf(Integer.MIN_VALUE);

	/**
	 * Value zero
	 */
	public static final IntegerAttributeValue ZERO = new IntegerAttributeValue(BigInteger.ZERO);

	/**
	 * Creates instance from integer argument
	 * 
	 * @param val
	 */
	public IntegerAttributeValue(BigInteger val)
	{
		super(FACTORY.instanceDatatype, val);
	}

	/**
	 * Creates instance from long argument, mostly for easy writing of tests
	 * <p>
	 * Be aware that type long is not equivalent to xsd:integer type, BigInteger is. See
	 * https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
	 * </p>
	 * 
	 * @param val
	 */
	public IntegerAttributeValue(long val)
	{
		this(BigInteger.valueOf(val));
	}

	/**
	 * Creates instance from lexical representation of xsd:integer
	 * 
	 * @param val
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:integer
	 */
	public IntegerAttributeValue(String val) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, val);
	}

	@Override
	public int compareTo(IntegerAttributeValue o)
	{
		return this.value.compareTo(o.value);
	}

	@Override
	public IntegerAttributeValue abs()
	{
		return new IntegerAttributeValue(this.value.abs());
	}

	@Override
	public IntegerAttributeValue add(Deque<IntegerAttributeValue> others)
	{
		BigInteger sum = value;
		while (!others.isEmpty())
		{
			sum = sum.add(others.poll().value);
		}

		return new IntegerAttributeValue(sum);
	}

	@Override
	public IntegerAttributeValue multiply(Deque<IntegerAttributeValue> others)
	{
		BigInteger product = value;
		while (!others.isEmpty())
		{
			product = product.multiply(others.poll().value);
		}

		return new IntegerAttributeValue(product);
	}

	@Override
	protected BigInteger parse(String stringForm) throws IllegalArgumentException
	{
		return DatatypeConverter.parseInteger(stringForm);
	}

	@Override
	public IntegerAttributeValue divide(IntegerAttributeValue divisor) throws ArithmeticException
	{
		return new IntegerAttributeValue(value.divide(divisor.value));
	}

	@Override
	public IntegerAttributeValue subtract(IntegerAttributeValue subtractedVal)
	{
		return new IntegerAttributeValue(value.subtract(subtractedVal.value));
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
	public IntegerAttributeValue remainder(IntegerAttributeValue divisor) throws ArithmeticException
	{
		return new IntegerAttributeValue(value.remainder(divisor.value));
	}

	/**
	 * Converts this integer to a double
	 * 
	 * @return <code>this</code> as a double
	 * @throws IllegalArgumentException
	 *             if this integer is outside the range which can be represented by a double
	 */
	public DoubleAttributeValue toDouble() throws IllegalArgumentException
	{
		final double doubleVal = value.doubleValue();
		if (Double.isInfinite(doubleVal) || Double.isNaN(doubleVal))
		{
			// this BigInteger has too great a magnitude to represent as a double
			throw new IllegalArgumentException("integer argument outside the range which can be represented by a double");
		}

		return new DoubleAttributeValue(doubleVal);
	}

	/**
	 * Converts this to an int, checking for lost information. If the value of this BigInteger is
	 * out of the range of the int type, then an ArithmeticException is thrown.
	 * <p>
	 * 
	 * @see "https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow"
	 *      <p>
	 *      TODO: replace with Java 8 native equivalent - BigInteger#intValueExact() - after upgrade
	 *      to Java 8
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
	public IntegerAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

}
