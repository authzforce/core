package com.thalesgroup.authzforce.core.attr;

import javax.xml.bind.DatatypeConverter;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:hexBinary value. This class supports parsing xs:hexBinary values. All
 * objects of this class are immutable and all methods of the class are thread-safe. The choice of
 * the Java type byte[] is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 */
public class HexBinaryAttributeValue extends PrimitiveAttributeValue<byte[]>
{
	/**
	 * Official name of this type
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#hexBinary";

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
	public static final AttributeValue.Factory<HexBinaryAttributeValue> FACTORY = new AttributeValue.Factory<HexBinaryAttributeValue>(HexBinaryAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public HexBinaryAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new HexBinaryAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(AttributeValueType)
	 */
	public HexBinaryAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	/**
	 * Creates a new <code>HexBinaryAttributeValue</code> that represents the byte [] value
	 * supplied.
	 * 
	 * @param value
	 *            the <code>byte []</code> value to be represented
	 */
	public HexBinaryAttributeValue(byte[] value)
	{
		super(identifier, value, value);
	}

	/**
	 * Returns a new <code>HexBinaryAttributeValue</code> that represents the xsi:hexBinary value
	 * indicated by the string provided.
	 * 
	 * @param val
	 *            a string representing the desired value
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:hexBinary
	 */
	public HexBinaryAttributeValue(String val) throws IllegalArgumentException
	{
		super(identifier, val);
	}

	@Override
	protected byte[] parse(String stringForm)
	{
		return DatatypeConverter.parseHexBinary(stringForm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue#toString(java.lang.Object)
	 */
	@Override
	public String toString(byte[] val)
	{
		return DatatypeConverter.printHexBinary(val);
	}

}
