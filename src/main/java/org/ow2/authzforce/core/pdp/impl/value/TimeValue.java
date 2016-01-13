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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Representation of an xs:time value. This class supports parsing xs:time values. All objects of this class are immutable and thread-safe.
 * 
 */
public final class TimeValue extends BaseTimeValue<TimeValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#time";

	/**
	 * Creates a new <code>TimeAttributeValue</code> from a string representation of time
	 * 
	 * @param time
	 *            string representation of time
	 * @throws IllegalArgumentException
	 *             if {@code time} is not a valid string representation of xs:time
	 */
	public TimeValue(String time) throws IllegalArgumentException
	{
		this(XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(time));
	}

	/**
	 * Creates a new <code>TimeAttributeValue</code> that represents the supplied time but uses default timezone and offset values.
	 * 
	 * @param time
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time; all date fields assumed unset
	 * @throws IllegalArgumentException
	 *             if {@code time == null}
	 */
	private TimeValue(XMLGregorianCalendar time) throws IllegalArgumentException
	{
		super(TYPE_URI, time, DatatypeConstants.TIME);
	}

	/**
	 * Creates a new instance from a Calendar
	 * 
	 * @param timeCalendar
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time; beware that this method modifies {@code calendar} by unsetting all
	 *            date fields (year, month, day): e.g. for the year, {@code calendar.setYear(DatatypeConstants.FIELD_UNDEFINED)}
	 * @return new instance
	 * @throws IllegalArgumentException
	 *             if {@code calendar == null}
	 */
	public static TimeValue getInstance(XMLGregorianCalendar timeCalendar)
	{
		// we only want the time, so unset all non-time fields
		timeCalendar.setYear(DatatypeConstants.FIELD_UNDEFINED);
		timeCalendar.setMonth(DatatypeConstants.FIELD_UNDEFINED);
		timeCalendar.setDay(DatatypeConstants.FIELD_UNDEFINED);
		return new TimeValue(timeCalendar);
	}

	@Override
	public TimeValue add(DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new TimeValue(cal);
	}

	@Override
	public TimeValue subtract(DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new TimeValue(cal);
	}

}
