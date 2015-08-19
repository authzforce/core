package com.thalesgroup.authzforce.core.attr;

import javax.xml.bind.DatatypeConverter;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:base64Binary value. This class supports parsing xs:base64Binary values.
 * All objects of this class are immutable and all methods of the class are thread-safe. The choice
 * of the Java type byte[] is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 */
public class Base64BinaryAttributeValue extends PrimitiveAttributeValue<byte[]>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#base64Binary";

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
	public static final AttributeValue.Factory<Base64BinaryAttributeValue> FACTORY = new AttributeValue.Factory<Base64BinaryAttributeValue>(Base64BinaryAttributeValue.class)
	{

		@Override
		public final String getId()
		{
			return TYPE_URI;
		}

		@Override
		public final Base64BinaryAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new Base64BinaryAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * Creates instance from XML/JAXB value
	 * 
	 * @param jaxbAttrVal
	 *            JAXB AttributeValue
	 * @throws IllegalArgumentException
	 *             if not valid value for datatype {@value #TYPE_URI}
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(DatatypeDef, AttributeValueType)
	 */
	public Base64BinaryAttributeValue(AttributeValueType jaxbAttrVal)
	{
		super(TYPE, jaxbAttrVal);
	}

	/**
	 * Creates instance from lexical representation of xs:base64Binary
	 * 
	 * @param val
	 *            string representation of xs:base64Binary
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this value datatype
	 */
	public Base64BinaryAttributeValue(String val) throws IllegalArgumentException
	{
		super(TYPE, val);
	}

	@Override
	protected byte[] parse(String stringForm)
	{
		return DatatypeConverter.parseBase64Binary(stringForm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue#toString(java.lang.Object)
	 */
	@Override
	public String toString(byte[] val)
	{
		return DatatypeConverter.printBase64Binary(val);
	}

}
