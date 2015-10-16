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

import javax.xml.datatype.Duration;

import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Representation of an xs:yearMonthDuration value. This class supports parsing xs:yearMonthDuration
 * values. All objects of this class are immutable and thread-safe. The choice of the Java type
 * Duration is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of
 * javax.xml.datatype package.
 * 
 */
public class YearMonthDurationAttributeValue extends DurationAttributeValue<YearMonthDurationAttributeValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<YearMonthDurationAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<YearMonthDurationAttributeValue>(YearMonthDurationAttributeValue.class, TYPE_URI)
	{

		@Override
		public YearMonthDurationAttributeValue getInstance(String val)
		{
			return new YearMonthDurationAttributeValue(val);
		}

	};

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

	/**
	 * Instantiates year-month duration from string representation of xs:dayTimeDuration value.
	 * 
	 * @param value
	 *            a string representing the desired duration
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xs:dayTimeDuration
	 */
	public YearMonthDurationAttributeValue(String value) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, value);
	}

	@Override
	protected Duration parse(String stringForm) throws IllegalArgumentException
	{
		return XML_TEMPORAL_DATATYPE_FACTORY.newDurationYearMonth(stringForm);
	}

	@Override
	public YearMonthDurationAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}

}
