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
import javax.xml.datatype.Duration;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;

/**
 * Superclass of duration attribute values, i.e. XML schema dayTime/yearMonthDuration values. The choice of the Java type Duration is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of javax.xml.datatype package.
 *
 * @param <DAV>
 *            Concrete DurationAttributeValue type subclass
 * @author cdangerv
 * @version $Id: $
 */
public abstract class DurationValue<DAV extends DurationValue<DAV>> extends SimpleValue<Duration>
{

	/**
	 * Instantiates duration attribute value from string representation
	 *
	 * @param datatypeId
	 *            duration datatype ID
	 * @param duration
	 *            duration
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this datatype
	 */
	public DurationValue(String datatypeId, Duration duration) throws IllegalArgumentException
	{
		super(datatypeId, duration);
	}

	/**
	 * Compares internal duration value ({@link Duration}) to another, using {@link Duration#compare(Duration)}
	 *
	 * @param o
	 *            compared duration value
	 * @return result of {@link Duration#compare(Duration)}
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if and only if result is {@link javax.xml.datatype.DatatypeConstants#INDETERMINATE}
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

	/** {@inheritDoc} */
	@Override
	public final String printXML()
	{
		return this.value.toString();
	}
}
