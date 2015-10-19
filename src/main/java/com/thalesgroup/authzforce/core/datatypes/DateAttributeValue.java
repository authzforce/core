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
package com.thalesgroup.authzforce.core.datatypes;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;

/**
 * Representation of an xs:date value. This class supports parsing xs:date values. All objects of
 * this class are immutable and thread-safe.
 */
public class DateAttributeValue extends BaseTimeAttributeValue<DateAttributeValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#date";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<DateAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<DateAttributeValue>(DateAttributeValue.class, TYPE_URI)
	{

		@Override
		public DateAttributeValue getInstance(String val)
		{
			return new DateAttributeValue(val);
		}

	};

	/**
	 * Creates a new <code>DateAttributeValue</code> from a string representation of date
	 * 
	 * @param date
	 *            string representation of date
	 * @throws IllegalArgumentException
	 *             if {@code date} is not a valid string representation of xs:date
	 */
	public DateAttributeValue(String date) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, date);
	}

	/**
	 * Creates instance from Calendar
	 * 
	 * @param date
	 *            date (all time fields assumed unset)
	 * @throws IllegalArgumentException
	 *             if {@code date == null}
	 */
	private DateAttributeValue(XMLGregorianCalendar date) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, date);
	}

	/**
	 * Creates a new <code>DateAttributeValue</code> from a Calendar
	 * 
	 * @param calendar
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date; beware
	 *            that this method modifies {@code calendar} by unsetting all time fields:
	 *            {@code calendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED)}
	 * @return new instance
	 * @throws IllegalArgumentException
	 *             if {@code calendar == null}
	 */
	public static DateAttributeValue getInstance(XMLGregorianCalendar calendar) throws IllegalArgumentException
	{
		// we only want the date, so unset time fields
		calendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
		return new DateAttributeValue(calendar);
	}

	@Override
	protected QName getXmlSchemaType()
	{
		return DatatypeConstants.DATE;
	}

	@Override
	public DateAttributeValue add(DurationAttributeValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new DateAttributeValue(cal);
	}

	@Override
	public DateAttributeValue subtract(DurationAttributeValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new DateAttributeValue(cal);
	}

	@Override
	public DateAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}
}
