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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Representation of an xs:date value. This class supports parsing xs:date values. All objects of this class are immutable and thread-safe.
 */
public final class DateValue extends BaseTimeValue<DateValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#date";

	/**
	 * Creates a new <code>DateAttributeValue</code> from a string representation of date
	 * 
	 * @param date
	 *            string representation of date
	 * @throws IllegalArgumentException
	 *             if {@code date} is not a valid string representation of xs:date
	 */
	public DateValue(String date) throws IllegalArgumentException
	{
		this(XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(date));
	}

	/**
	 * Creates instance from Calendar
	 * 
	 * @param date
	 *            date (all time fields assumed unset)
	 * @throws IllegalArgumentException
	 *             if {@code date == null}
	 */
	private DateValue(XMLGregorianCalendar date) throws IllegalArgumentException
	{
		super(TYPE_URI, date, DatatypeConstants.DATE);
	}

	/**
	 * Creates a new <code>DateAttributeValue</code> from a Calendar
	 * 
	 * @param calendar
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date; beware that this method modifies {@code calendar} by unsetting all
	 *            time fields:
	 *            {@code calendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED)}
	 * @return new instance
	 * @throws IllegalArgumentException
	 *             if {@code calendar == null}
	 */
	public static DateValue getInstance(XMLGregorianCalendar calendar) throws IllegalArgumentException
	{
		// we only want the date, so unset time fields
		calendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
				DatatypeConstants.FIELD_UNDEFINED);
		return new DateValue(calendar);
	}

	@Override
	public DateValue add(DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new DateValue(cal);
	}

	@Override
	public DateValue subtract(DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new DateValue(cal);
	}
}
