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
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";

	/**
	 * Generic type info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(identifier);

	/**
	 * Bag datatype definition of this attribute value
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(identifier, true);

	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<DayTimeDurationAttributeValue> FACTORY = new AttributeValue.Factory<DayTimeDurationAttributeValue>(DayTimeDurationAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public DayTimeDurationAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new DayTimeDurationAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * @see DurationAttributeValue#DurationAttributeValue(AttributeValueType)
	 */
	public DayTimeDurationAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
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
		super(identifier, val);
	}

	@Override
	protected Duration parse(String stringForm)
	{
		return XML_TEMPORAL_DATATYPE_FACTORY.newDurationDayTime(stringForm);
	}
}
