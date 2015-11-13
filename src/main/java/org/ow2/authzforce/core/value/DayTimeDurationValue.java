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

import javax.xml.datatype.Duration;

import com.thalesgroup.authzforce.core.EvaluationContext;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;

/**
 * Representation of an xs:dayTimeDuration value. This class supports parsing xs:dayTimeDuration
 * values. All objects of this class are immutable and thread-safe.
 */
public class DayTimeDurationAttributeValue extends DurationAttributeValue<DayTimeDurationAttributeValue>
{

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<DayTimeDurationAttributeValue> FACTORY = new SimpleAttributeValue.StringContentOnlyFactory<DayTimeDurationAttributeValue>(DayTimeDurationAttributeValue.class, TYPE_URI)
	{

		@Override
		public DayTimeDurationAttributeValue getInstance(String val)
		{
			return new DayTimeDurationAttributeValue(val);
		}

	};

	/**
	 * Creates instance from string representation
	 * 
	 * @param val
	 *            string representation of xs:dayTimeDuration
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for xs:dayTimeDuration
	 */
	public DayTimeDurationAttributeValue(String val) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, val);
	}

	@Override
	protected Duration parse(String stringForm)
	{
		return XML_TEMPORAL_DATATYPE_FACTORY.newDurationDayTime(stringForm);
	}

	@Override
	public DayTimeDurationAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this;
	}
}
