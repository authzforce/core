package com.thalesgroup.authzforce.core.attr;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:dateTime value. This class supports parsing xs:dateTime values. All
 * objects of this class are immutable and thread-safe.
 */
public class DateTimeAttributeValue extends BaseTimeAttributeValue<DateTimeAttributeValue>
{
	/**
	 * XACML URI of this datatype
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#dateTime";

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
	public static final AttributeValue.Factory<DateTimeAttributeValue> FACTORY = new AttributeValue.Factory<DateTimeAttributeValue>(DateTimeAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public DateTimeAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new DateTimeAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> from a string representation of date/time
	 * 
	 * @param dateTime
	 *            string representation of date/time
	 * @throws IllegalArgumentException
	 *             if {@code dateTime} is not a valid string representation for this value datatype
	 */
	public DateTimeAttributeValue(String dateTime) throws IllegalArgumentException
	{
		super(identifier, dateTime);
	}

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> that represents the supplied date
	 * 
	 * @param dateTime
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date and
	 *            time
	 * @throws IllegalArgumentException
	 *             if {@code dateTime} does not correspond to a valid xs:dateTime
	 */
	public DateTimeAttributeValue(XMLGregorianCalendar dateTime) throws IllegalArgumentException
	{
		super(identifier, dateTime);
	}

	/**
	 * @see BaseTimeAttributeValue#BaseTimeAttributeValue(AttributeValueType)
	 */
	public DateTimeAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> that represents the supplied date
	 * 
	 * @param dateTime
	 *            a <code>GregorianCalendar</code> object representing the specified date and time
	 * @throws IllegalArgumentException
	 *             if {@code dateTime} does not correspond to a valid xs:dateTime
	 */
	public DateTimeAttributeValue(GregorianCalendar dateTime)
	{
		this(XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(dateTime));
	}

	@Override
	protected QName getXmlSchemaType()
	{
		return DatatypeConstants.DATETIME;
	}

	@Override
	public DateTimeAttributeValue add(DurationAttributeValue durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value);
		return new DateTimeAttributeValue(durationVal);
	}

	@Override
	public DateTimeAttributeValue subtract(DurationAttributeValue durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.value.negate());
		return new DateTimeAttributeValue(durationVal);
	}

	/**
	 * Create the date AttributeValue corresponding to the date part of this date-time attribute
	 * value.
	 * 
	 * @return date part as attribute value
	 */
	public DateAttributeValue toDate()
	{
		final XMLGregorianCalendar dateCalendar = (XMLGregorianCalendar) value.clone();
		// we only want the date, so unset time fields
		dateCalendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
		return new DateAttributeValue(dateCalendar);
	}

	/**
	 * Create the time AttributeValue corresponding to the time part of this date-time attribute
	 * value.
	 * 
	 * @return time part as attribute value
	 */
	public TimeAttributeValue toTime()
	{
		final XMLGregorianCalendar timeCalendar = (XMLGregorianCalendar) value.clone();
		// we only want the time, so unset all non-time fields
		timeCalendar.setYear(DatatypeConstants.FIELD_UNDEFINED);
		timeCalendar.setMonth(DatatypeConstants.FIELD_UNDEFINED);
		timeCalendar.setDay(DatatypeConstants.FIELD_UNDEFINED);
		return new TimeAttributeValue(timeCalendar);
	}
}
