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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Representation of an xs:time value. This class supports parsing xs:time values. All objects of
 * this class are immutable and thread-safe.
 * 
 */
public class TimeAttributeValue extends BaseTimeAttributeValue<TimeAttributeValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#time";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<TimeAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<TimeAttributeValue>(TimeAttributeValue.class, TYPE_URI)
	{

		@Override
		public TimeAttributeValue getInstance(String val)
		{
			return new TimeAttributeValue(val);
		}

	};

	/**
	 * Creates a new <code>TimeAttributeValue</code> from a string representation of time
	 * 
	 * @param time
	 *            string representation of time
	 * @throws IllegalArgumentException
	 *             if {@code time} is not a valid string representation of xs:time
	 */
	public TimeAttributeValue(String time) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, time);
	}

	/**
	 * Creates a new <code>TimeAttributeValue</code> that represents the supplied time but uses
	 * default timezone and offset values.
	 * 
	 * @param time
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time; all
	 *            date fields assumed unset
	 * @throws IllegalArgumentException
	 *             if {@code time == null}
	 */
	private TimeAttributeValue(XMLGregorianCalendar time) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, time);
	}

	/**
	 * Creates a new instance from a Calendar
	 * 
	 * @param timeCalendar
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time; beware
	 *            that this method modifies {@code calendar} by unsetting all date fields (year,
	 *            month, day): e.g. for the year,
	 *            {@code calendar.setYear(DatatypeConstants.FIELD_UNDEFINED)}
	 * @return new instance
	 * @throws IllegalArgumentException
	 *             if {@code calendar == null}
	 */
	public static TimeAttributeValue getInstance(XMLGregorianCalendar timeCalendar)
	{
		// we only want the time, so unset all non-time fields
		timeCalendar.setYear(DatatypeConstants.FIELD_UNDEFINED);
		timeCalendar.setMonth(DatatypeConstants.FIELD_UNDEFINED);
		timeCalendar.setDay(DatatypeConstants.FIELD_UNDEFINED);
		return new TimeAttributeValue(timeCalendar);
	}

	@Override
	protected QName getXmlSchemaType()
	{
		return DatatypeConstants.TIME;
	}

	@Override
	public TimeAttributeValue add(DurationAttributeValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new TimeAttributeValue(cal);
	}

	@Override
	public TimeAttributeValue subtract(DurationAttributeValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new TimeAttributeValue(cal);
	}

	@Override
	public TimeAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

}
