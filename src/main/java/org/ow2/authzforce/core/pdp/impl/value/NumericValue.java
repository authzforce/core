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

import java.util.Deque;

/**
 * Superclass of all numeric Attribute Values (integer, double...)
 *
 * @param <N>
 *            actual Java type of the underlying numeric value (Integer, Double...)
 * @param <NAV>
 *            Concreate NumericAttributeValue type subclass
 * 
 * @version $Id: $
 */
public abstract class NumericValue<N extends Number, NAV extends NumericValue<N, NAV>> extends SimpleValue<N>
{
	/**
	 * <p>
	 * Constructor for NumericValue.
	 * </p>
	 *
	 * @param datatypeId
	 *            a {@link java.lang.String} object.
	 * @param val
	 *            a N object.
	 */
	protected NumericValue(String datatypeId, N val)
	{
		super(datatypeId, val);
	}

	/**
	 * Returns the absolute value of <code>this</code>. Used by the XACML "abs" functions.
	 *
	 * @return the absolute value
	 */
	public abstract NAV abs();

	/**
	 * Adds numbers to this. Used by the XACML numeric *-add functions.
	 *
	 * @param others
	 *            values to add to this value
	 * @return sum of this and the others. 0 is returned if {@code offset >= others.length}.
	 */
	public abstract NAV add(Deque<NAV> others);

	/**
	 * Multiply <code>this</code> by other numbers, starting to multiply others from a specific offset (index in the array). Used by the XACML "multiply" functions.
	 *
	 * @param others
	 *            other values to add
	 * @return product of this by the others
	 */
	public abstract NAV multiply(Deque<NAV> others);

	/**
	 * Divide <code>this</code> by some other number. Used by XACML *-divide functions.
	 *
	 * @param divisor
	 *            number by which <code>this</code> is divided
	 * @return the result quotient
	 * @throws java.lang.ArithmeticException
	 *             if divisor is zero
	 */
	public abstract NAV divide(NAV divisor) throws ArithmeticException;

	/**
	 * Substract a number from this. Used by XACML numeric *-subtract functions.
	 *
	 * @param subtractedVal
	 *            value to be subtracted from <code>this</code>
	 * @return this - substractedVal
	 */
	public abstract NAV subtract(NAV subtractedVal);
}
