package com.thalesgroup.authzforce.core.attr;

import javax.xml.datatype.Duration;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:yearMonthDuration value. This class supports parsing xs:yearMonthDuration
 * values. All objects of this class are immutable and thread-safe. The choice of the Java type
 * Duration is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of
 * javax.xml.datatype package.
 * 
 */
public class YearMonthDurationAttributeValue extends DurationAttributeValue
{
	/**
	 * Official name of this type
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";

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
	public static final AttributeValue.Factory<YearMonthDurationAttributeValue> FACTORY = new AttributeValue.Factory<YearMonthDurationAttributeValue>(YearMonthDurationAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public YearMonthDurationAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new YearMonthDurationAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * @see DurationAttributeValue#DurationAttributeValue(AttributeValueType)
	 */
	public YearMonthDurationAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

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
		super(identifier, value);
	}

	@Override
	protected Duration parse(String stringForm) throws IllegalArgumentException
	{
		return XML_TEMPORAL_DATATYPE_FACTORY.newDurationYearMonth(stringForm);
	}
}
