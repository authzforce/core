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

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Representation of an xs:dateTime value. This class supports parsing xs:dateTime values. All objects of this class are immutable and thread-safe.
 */
public final class DateTimeValue extends BaseTimeValue<DateTimeValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#dateTime";

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> from a string representation of date/time
	 * 
	 * @param dateTime
	 *            string representation of date/time
	 * @throws IllegalArgumentException
	 *             if {@code dateTime} is not a valid string representation for this value datatype
	 */
	public DateTimeValue(String dateTime) throws IllegalArgumentException
	{
		this(XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(dateTime));
	}

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> that represents the supplied date
	 * 
	 * @param dateTime
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date and time
	 * @throws IllegalArgumentException
	 *             if {@code dateTime} does not correspond to a valid xs:dateTime
	 */
	public DateTimeValue(XMLGregorianCalendar dateTime) throws IllegalArgumentException
	{
		super(TYPE_URI, dateTime, DatatypeConstants.DATETIME);
	}

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> that represents the supplied date
	 * 
	 * @param dateTime
	 *            a <code>GregorianCalendar</code> object representing the specified date and time
	 * @throws IllegalArgumentException
	 *             if {@code dateTime} does not correspond to a valid xs:dateTime
	 */
	public DateTimeValue(GregorianCalendar dateTime)
	{
		this(XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(dateTime));
	}

	@Override
	public DateTimeValue add(DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new DateTimeValue(cal);
	}

	@Override
	public DateTimeValue subtract(DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new DateTimeValue(cal);
	}
}
