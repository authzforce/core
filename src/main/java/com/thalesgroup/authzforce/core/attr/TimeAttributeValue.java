package com.thalesgroup.authzforce.core.attr;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

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
	 * Primitive datatype info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(TYPE_URI);

	/**
	 * Bag datatype info
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(TYPE_URI, true);

	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<TimeAttributeValue> FACTORY = new AttributeValue.Factory<TimeAttributeValue>(TimeAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return TYPE_URI;
		}

		@Override
		public TimeAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new TimeAttributeValue(jaxbAttributeValue);
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
		super(TYPE, time);
	}

	/**
	 * Creates a new <code>TimeAttributeValue</code> that represents the supplied time but uses
	 * default timezone and offset values.
	 * 
	 * @param time
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time
	 * @throws IllegalArgumentException
	 *             if {@code time} does not correspond to a valid xs:time
	 */
	public TimeAttributeValue(XMLGregorianCalendar time) throws IllegalArgumentException
	{
		super(TYPE, time);
	}

	/**
	 * Creates instance from XML/JAXB value
	 * 
	 * @param jaxbAttrVal
	 *            JAXB AttributeValue
	 * @throws IllegalArgumentException
	 *             if not valid value for datatype {@value #TYPE_URI}
	 * @see BaseTimeAttributeValue#BaseTimeAttributeValue(DatatypeDef, AttributeValueType)
	 */
	public TimeAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(TYPE, jaxbAttrVal);
	}

	@Override
	protected QName getXmlSchemaType()
	{
		return DatatypeConstants.TIME;
	}

	@Override
	public TimeAttributeValue add(DurationAttributeValue durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new TimeAttributeValue(durationVal);
	}

	@Override
	public TimeAttributeValue subtract(DurationAttributeValue durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new TimeAttributeValue(durationVal);
	}
}
