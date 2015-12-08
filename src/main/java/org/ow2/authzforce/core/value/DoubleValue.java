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

import java.util.Deque;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:double value. This class supports parsing xs:double values. All objects of this class are immutable and all methods of the class are
 * thread-safe. The choice of the Java type Double is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 */
public final class DoubleValue extends NumericValue<Double, DoubleValue> implements Comparable<DoubleValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#double";

	/**
	 * Value zero
	 */
	public static final DoubleValue ZERO = new DoubleValue(0);

	/**
	 * Creates a new <code>DoubleAttributeValue</code> that represents the double value supplied.
	 * 
	 * @param value
	 *            the <code>double</code> value to be represented
	 */
	public DoubleValue(double value)
	{
		super(TYPE_URI, value);
	}

	/**
	 * Creates instance from lexical representation of xs:double
	 * 
	 * @param val
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:double
	 */
	public DoubleValue(String val) throws IllegalArgumentException
	{
		this(DatatypeConverter.parseDouble(val));
	}

	@Override
	public int compareTo(DoubleValue o)
	{
		return this.value.compareTo(o.value);
	}

	@Override
	public DoubleValue abs()
	{
		return new DoubleValue(Math.abs(this.value));
	}

	@Override
	public DoubleValue add(Deque<DoubleValue> others)
	{

		double sum = value;
		while (!others.isEmpty())
		{
			sum += others.poll().value;
		}

		return new DoubleValue(sum);
	}

	@Override
	public DoubleValue multiply(Deque<DoubleValue> others)
	{
		double product = value;
		while (!others.isEmpty())
		{
			product *= others.poll().value;
		}

		return new DoubleValue(product);
	}

	private static final ArithmeticException ILLEGAL_DIV_BY_ZERO_EXCEPTION = new ArithmeticException("Illegal division by zero");

	@Override
	public DoubleValue divide(DoubleValue divisor) throws ArithmeticException
	{
		/*
		 * Quotes from Java Language Specification (Java SE 7 Edition), §4.2.3. Floating-Point Types, Formats, and Values
		 * http://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.2 Quotes: "A NaN value is used to represent the result of certain invalid
		 * operations such as dividing zero by zero. [...] 1.0/0.0 has the value positive infinity, while the value of 1.0/-0.0 is negative
		 * infinity." Also "Example 4.2.4-1. Floating-point Operations" shows that 0.0/0.0 = NaN. Negative/Positive Infinity and NaN have their equivalent in
		 * XML schema (INF, -INF, Nan), so we can return the result of division by zero as it is (JAXB will convert it properly).
		 */

		final Double result = new Double(value / divisor.value);
		if (result.isInfinite() || result.isNaN())
		{
			throw ILLEGAL_DIV_BY_ZERO_EXCEPTION;
		}

		return new DoubleValue(result);
	}

	/**
	 * @see Math#floor(double)
	 * @return result of Math#floor(double) as AttributeValue
	 * 
	 */
	public DoubleValue floor()
	{
		return new DoubleValue(Math.floor(value));
	}

	/**
	 * Rounds the double using default IEEE754 rounding mode . According to XACML core spec, §7.5 Arithmetic evaluation,
	 * "rounding - is set to round-half-even (IEEE 854 §4.1)" ( {@link java.math.RoundingMode#HALF_EVEN}). This method uses {@link Math#rint(double)} that does
	 * the equivalent of the {@link java.math.RoundingMode#HALF_EVEN}.
	 * 
	 * @return result of Math#rint(double) as AttributeValue
	 * 
	 */
	public DoubleValue roundIEEE754Default()
	{
		return new DoubleValue(Math.rint(value));
	}

	// For quick testing
	// public static void main(String... args)
	// {
	// Double arg1 = new Double("1");
	// Double divisor = new Double("0");
	// Double result = arg1 / divisor;
	// System.out.println(result); // Infinity!
	// arg1 = new Double("-1");
	// result = arg1 / divisor;
	// System.out.println(result); // -Infinity!
	//
	// Double positiveZero = new Double("0.");
	// Double negativeZero = new Double("-0.");
	// System.out.println(positiveZero.equals(negativeZero));
	// double inf = DatatypeConverter.parseDouble("INF");
	// System.out.println(DatatypeConverter.printDouble(inf));
	// }

	@Override
	public DoubleValue subtract(DoubleValue subtractedVal)
	{
		return new DoubleValue(this.value - subtractedVal.value);
	}

	/**
	 * Converts this double value to a long, as specified by {@link Double#longValue()}
	 * 
	 * @return <code>this</code> as an integer
	 */
	public long longValue()
	{
		return value.longValue();
	}

	@Override
	public String printXML()
	{
		return DatatypeConverter.printDouble(this.value);
	}

}
