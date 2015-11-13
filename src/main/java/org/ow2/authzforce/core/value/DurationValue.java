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
import javax.xml.datatype.Duration;

import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;

/**
 * Superclass of duration attribute values, i.e. XML schema dayTime/yearMonthDuration values. The
 * choice of the Java type Duration is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of
 * javax.xml.datatype package.
 * 
 * @param <DAV>
 *            Concrete DurationAttributeValue type subclass
 * 
 * 
 */
public abstract class DurationAttributeValue<DAV extends DurationAttributeValue<DAV>> extends SimpleAttributeValue<Duration, DAV>
{

	/**
	 * Instantiates duration attribute value from string representation
	 * 
	 * @param datatype
	 *            duration datatype
	 * @param val
	 *            string representation of the XML duration
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this datatype
	 */
	protected DurationAttributeValue(Datatype<DAV> datatype, String val) throws IllegalArgumentException
	{
		super(datatype, val);
	}

	/**
	 * Compares internal duration value ({@link Duration}) to another, using
	 * {@link Duration#compare(Duration)}
	 * 
	 * @param o
	 *            compared duration value
	 * @return result of {@link Duration#compare(Duration)}
	 * @throws IndeterminateEvaluationException
	 *             if and only if result is {@link DatatypeConstants#INDETERMINATE}
	 */
	public final int compare(DAV o) throws IndeterminateEvaluationException
	{
		final int result = this.value.compare(o.value);
		if (result == DatatypeConstants.INDETERMINATE)
		{
			throw new IndeterminateEvaluationException(StatusHelper.STATUS_PROCESSING_ERROR, "Comparison of XML schema duration '" + this.value + "' to '" + o.value + "' is indeterminate");
		}

		return result;
	}
}
