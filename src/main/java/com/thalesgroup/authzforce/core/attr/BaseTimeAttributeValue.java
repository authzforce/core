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

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Superclass of date/time attribute values, i.e. XML schema date/time values. The choice of the
 * Java type <code>XMLGregorianCalendar</code> is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 * @param <TAV>
 *            type of result returned by arithmetic functions with this type of arguments:
 *            {@link #add(DurationAttributeValue)}, {@link #subtract(DurationAttributeValue)}, etc.
 *            Basically, we expect that arithmetic functions applied to this type T will return a
 *            result of the same type T.
 */
/*
 * Do not replace "Time" with "Temporal" in the class name because it is NOT used for Durations
 * (dayTimeDuration, yearMonthDuration...)
 */
public abstract class BaseTimeAttributeValue<TAV extends BaseTimeAttributeValue<TAV>> extends SimpleAttributeValue<XMLGregorianCalendar, TAV>
{
	protected abstract QName getXmlSchemaType();

	/**
	 * Instantiate date/time attribute value
	 * 
	 * @param datatype
	 *            datatype URI
	 * @param val
	 *            string representation of instance of this datatype
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this value datatype
	 */
	protected BaseTimeAttributeValue(Datatype<TAV> datatype, String val) throws IllegalArgumentException
	{
		super(datatype, val);
	}

	/**
	 * Instantiate date/time attribute value
	 * 
	 * @param datatype
	 *            datatype URI
	 * @param val
	 *            string representation of instance of this datatype
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null || val == null}
	 */
	protected BaseTimeAttributeValue(Datatype<TAV> datatype, XMLGregorianCalendar val) throws IllegalArgumentException
	{
		super(datatype, val, val.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.attr.SimpleAttributeValue#parse(java.lang.String)
	 */
	@Override
	protected XMLGregorianCalendar parse(String stringForm)
	{
		final XMLGregorianCalendar cal = XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(stringForm);
		final QName expectedSchemaType = getXmlSchemaType();
		if (!cal.getXMLSchemaType().equals(expectedSchemaType))
		{
			throw new IllegalArgumentException("Invalid XML schema type (" + cal.getXMLSchemaType() + ") of value '" + stringForm + "'. Expected: " + expectedSchemaType);
		}

		return cal;
	}

	/**
	 * Add duration to this time
	 * 
	 * @param durationVal
	 *            duration value
	 * @return this + durationVal
	 */
	abstract public TAV add(DurationAttributeValue<?> durationVal);

	/**
	 * Subtract duration to this time
	 * 
	 * @param durationVal
	 *            duration value
	 * @return this - durationVal
	 */
	abstract public TAV subtract(DurationAttributeValue<?> durationVal);

	/**
	 * Compares internal date/time value ({@link XMLGregorianCalendar}) to another, using
	 * {@link XMLGregorianCalendar#compare(XMLGregorianCalendar)}
	 * 
	 * @param o
	 *            compared date/time value
	 * @return result of {@link XMLGregorianCalendar#compare(XMLGregorianCalendar)}
	 * @throws IndeterminateEvaluationException
	 *             if and only if result is {@link DatatypeConstants#INDETERMINATE}
	 */
	public final int compare(BaseTimeAttributeValue<TAV> o) throws IndeterminateEvaluationException
	{
		final int result = this.value.compare(o.value);
		if (result == DatatypeConstants.INDETERMINATE)
		{
			throw new IndeterminateEvaluationException(Status.STATUS_PROCESSING_ERROR, "Comparison of XML schema date/time '" + this.value + "' to '" + o.value + "' is indeterminate");
		}

		return result;
	}
}
