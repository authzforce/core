package com.thalesgroup.authzforce.core.attr;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:date value. This class supports parsing xs:date values. All objects of
 * this class are immutable and thread-safe.
 */
public class DateAttributeValue extends BaseTimeAttributeValue<DateAttributeValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#date";

	/**
	 * Primitive datatype info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(identifier);

	/**
	 * Bag datatype info
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(identifier, true);

	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<DateAttributeValue> FACTORY = new AttributeValue.Factory<DateAttributeValue>(DateAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public DateAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new DateAttributeValue(jaxbAttributeValue);
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
		super(identifier, date);
	}

	/**
	 * Creates a new <code>DateAttributeValue</code> that represents the supplied date
	 * 
	 * @param date
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date
	 * @throws IllegalArgumentException
	 *             if {@code date} does not correspond to a valid xs:date
	 */
	public DateAttributeValue(XMLGregorianCalendar date) throws IllegalArgumentException
	{
		super(identifier, date);
	}

	/**
	 * @see BaseTimeAttributeValue#BaseTimeAttributeValue(AttributeValueType)
	 */
	public DateAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	@Override
	protected QName getXmlSchemaType()
	{
		return DatatypeConstants.DATE;
	}

	@Override
	public DateAttributeValue add(DurationAttributeValue durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new DateAttributeValue(durationVal);
	}

	@Override
	public DateAttributeValue subtract(DurationAttributeValue durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new DateAttributeValue(durationVal);
	}
}
