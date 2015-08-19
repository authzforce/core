package com.thalesgroup.authzforce.core.attr;

import javax.xml.datatype.Duration;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:dayTimeDuration value. This class supports parsing xs:dayTimeDuration
 * values. All objects of this class are immutable and thread-safe.
 */
public class DayTimeDurationAttributeValue extends DurationAttributeValue
{

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";

	/**
	 * Generic type info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(TYPE_URI);

	/**
	 * Bag datatype definition of this attribute value
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(TYPE_URI, true);

	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<DayTimeDurationAttributeValue> FACTORY = new AttributeValue.Factory<DayTimeDurationAttributeValue>(DayTimeDurationAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return TYPE_URI;
		}

		@Override
		public DayTimeDurationAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new DayTimeDurationAttributeValue(TYPE, jaxbAttributeValue);
		}

	};

	/**
	 * Creates instance from XML/JAXB value
	 * 
	 * @param jaxbAttrVal
	 *            JAXB AttributeValue
	 * @throws IllegalArgumentException
	 *             if not valid value for datatype {@value #TYPE_URI}
	 * @see DurationAttributeValue#DurationAttributeValue(DatatypeDef, AttributeValueType)
	 */
	public DayTimeDurationAttributeValue(DatatypeDef datatype, AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(datatype, jaxbAttrVal);
	}

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
		super(TYPE, val);
	}

	@Override
	protected Duration parse(String stringForm)
	{
		return XML_TEMPORAL_DATATYPE_FACTORY.newDurationDayTime(stringForm);
	}
}
