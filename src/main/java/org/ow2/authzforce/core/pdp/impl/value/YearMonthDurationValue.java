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

/**
 * Representation of an xs:yearMonthDuration value. This class supports parsing xs:yearMonthDuration values. All objects of this class are immutable and
 * thread-safe. The choice of the Java type Duration is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * and documentation of javax.xml.datatype package.
 *
 * 
 * @version $Id: $
 */
public final class YearMonthDurationValue extends DurationValue<YearMonthDurationValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";

	/**
	 * Instantiates duration attribute value from string representation
	 * 
	 * @param value
	 *            string representation of the XML duration
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this datatype
	 */

	/**
	 * Instantiates year-month duration from string representation of xs:dayTimeDuration value.
	 *
	 * @param value
	 *            a string representing the desired duration
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xs:dayTimeDuration
	 */
	public YearMonthDurationValue(String value) throws IllegalArgumentException
	{
		super(TYPE_URI, XML_TEMPORAL_DATATYPE_FACTORY.newDurationYearMonth(value));
	}

}
