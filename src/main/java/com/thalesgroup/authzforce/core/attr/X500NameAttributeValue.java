package com.thalesgroup.authzforce.core.attr;

import javax.security.auth.x500.X500Principal;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an X.500 Directory Name.
 * 
 */
public class X500NameAttributeValue extends PrimitiveAttributeValue<String>
{

	/**
	 * XACML datatype URI
	 */
	public static final String identifier = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";

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
	public static final AttributeValue.Factory<X500NameAttributeValue> FACTORY = new AttributeValue.Factory<X500NameAttributeValue>(X500NameAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public X500NameAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new X500NameAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(AttributeValueType)
	 */
	public X500NameAttributeValue(AttributeValueType jaxbAttrVal)
	{
		super(jaxbAttrVal);
	}

	/**
	 * Creates a new <code>X500NameAttributeValue</code> that represents the value supplied.
	 * 
	 * @param value
	 *            the X500 Name to be represented
	 * @throws IllegalArgumentException
	 *             if value does not correspond to a valid XACML X500Name
	 */
	public X500NameAttributeValue(X500Principal value) throws IllegalArgumentException
	{
		this(value.toString());
	}

	/**
	 * Returns a new <code>X500NameAttributeValue</code> that represents the X500 Name value
	 * indicated by the string provided.
	 * 
	 * @param value
	 *            a string representing the desired value
	 * @throws IllegalArgumentException
	 *             if value is not a valid XACML X500Name
	 */
	public X500NameAttributeValue(String value) throws IllegalArgumentException
	{
		super(identifier, value);
	}

	@Override
	protected String parse(String stringForm)
	{
		return new X500Principal(stringForm).getName(X500Principal.CANONICAL);
	}

	/**
	 * Implements XACML function 'urn:oasis:names:tc:xacml:1.0:function:x500Name-match' with this as
	 * first argument.
	 * 
	 * @param other
	 *            the second argument
	 * @return true if and only if this matches some terminal sequence of RDNs from the
	 *         <code>other</other>'s value when compared using x500Name-equal.
	 */
	public boolean match(X500NameAttributeValue other)
	{
		return other.value.endsWith(this.value);
	}

}
