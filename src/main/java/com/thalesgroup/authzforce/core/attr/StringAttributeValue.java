package com.thalesgroup.authzforce.core.attr;

import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:string value. This class supports parsing xs:string values. All objects
 * of this class are immutable and all methods of the class are thread-safe.
 */
public class StringAttributeValue extends PrimitiveAttributeValue<String> implements Comparable<StringAttributeValue>
{
	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<StringAttributeValue> FACTORY = new AttributeValue.Factory<StringAttributeValue>(StringAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public StringAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new StringAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * Official name of this type
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#string";

	/**
	 * StringAttributeValue type as generic type (primitive)
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(StringAttributeValue.identifier);

	/**
	 * StringAttributeValue bag type as generic type
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(StringAttributeValue.identifier, true);

	private static final StringAttributeValue TRUE = new StringAttributeValue("true");
	private static final StringAttributeValue FALSE = new StringAttributeValue("false");

	/**
	 * Creates a new <code>StringAttributeValue</code> that represents the String value supplied.
	 * 
	 * @param value
	 *            the <code>String</code> value to be represented
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xs:string
	 */
	public StringAttributeValue(String value) throws IllegalArgumentException
	{
		super(identifier, value);
	}

	/**
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(AttributeValueType)
	 */
	public StringAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	@Override
	public int compareTo(StringAttributeValue o)
	{
		return this.value.compareTo(o.value);
	}

	/**
	 * Same as {@link String#equalsIgnoreCase(String)} on attribute values
	 * 
	 * @param otherAttribute
	 * @return true if the other attribute value is not null and it represents an equivalent String
	 *         ignoring case; false otherwise
	 */
	public boolean equalsIgnoreCase(StringAttributeValue otherAttribute)
	{
		return this.value.equalsIgnoreCase(otherAttribute.value);
	}

	@Override
	protected String parse(String stringForm)
	{
		return DatatypeConverter.parseString(stringForm);
	}

	/**
	 * @see String#trim()
	 * @return StringAttributeValue with value resulting from <code>value.trim()</code>
	 */
	public StringAttributeValue trim()
	{
		final String result = value.trim();
		return result == value ? this : new StringAttributeValue(result);
	}

	/**
	 * @see String#toLowerCase(Locale)
	 * @param locale
	 *            Locale
	 * @return StringAttributeValue with value resulting from <code>value.toLowerCase(L)</code>
	 */
	public StringAttributeValue toLowerCase(Locale locale)
	{
		final String result = value.toLowerCase(locale);
		return result == value ? this : new StringAttributeValue(result);
	}

	@Override
	public String toString()
	{
		return DatatypeConverter.printString(value);
	}

	/**
	 * Get string representation of boolean
	 * 
	 * @param value
	 *            boolean
	 * @return string equivalent ("true" or "false")
	 */
	public static StringAttributeValue getInstance(Boolean value)
	{
		return value ? TRUE : FALSE;
	}

	// Testing
	// public static void main(String[] args) {
	// String s1 = "test";
	// String result = s1.trim();
	// System.out.println(result == s1);
	//
	// String s2 = "test ";
	// String result2 = s2.trim();
	// System.out.println(result2 == s2);
	//
	// }

}
