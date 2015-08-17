package com.thalesgroup.authzforce.core.attr;

import javax.xml.bind.DatatypeConverter;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Representation of an xs:boolean value. This class supports parsing xs:boolean values. All objects
 * of this class are immutable and all methods of the class are thread-safe. The choice of the Java
 * type boolean is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 
 */
public class BooleanAttributeValue extends PrimitiveAttributeValue<Boolean>
{

	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<BooleanAttributeValue> FACTORY = new AttributeValue.Factory<BooleanAttributeValue>(BooleanAttributeValue.class)
	{

		@Override
		public final String getId()
		{
			return identifier;
		}

		@Override
		public final BooleanAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new BooleanAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * Official name of this type
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#boolean";

	/**
	 * Single instance of BooleanAttributeValue that represents true. Initialized by the static
	 * initializer below.
	 */
	public static BooleanAttributeValue TRUE = new BooleanAttributeValue(true);

	/**
	 * Single instance of BooleanAttributeValue that represents false. Initialized by the static
	 * initializer below.
	 */
	public static BooleanAttributeValue FALSE = new BooleanAttributeValue(false);

	/**
	 * Primitive datatype definition of this attribute value
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(BooleanAttributeValue.identifier);

	/**
	 * Bag datatype definition of this attribute value
	 */
	public static final DatatypeDef BAG_TYPE = new DatatypeDef(BooleanAttributeValue.identifier, true);

	/**
	 * Instantiates from XACML AttributeValue
	 * 
	 * @param jaxbAttrVal
	 *            XACML attribute value
	 * @throws IllegalArgumentException
	 *             if first value in {@code jaxbAttrVal.getContent()} is not a valid string
	 *             representation of xs:boolean
	 */
	public BooleanAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
	}

	/**
	 * Creates instance from string representation using
	 * {@link DatatypeConverter#parseBoolean(String)} to convert to boolean value.
	 * 
	 * @param val
	 * @return instance
	 * @throws IllegalArgumentException
	 *             if string parameter does not conform to lexical value space defined in XML Schema
	 *             Part 2: Datatypes for xsd:boolean.
	 */
	public static BooleanAttributeValue getInstance(String val) throws IllegalArgumentException
	{
		return valueOf(DatatypeConverter.parseBoolean(val));
	}

	/**
	 * Creates a new <code>BooleanAttributeValue</code> that represents the boolean value supplied.
	 * <p>
	 * This constructor is private because it should not be used by anyone other than the static
	 * initializer in this class. Instead, please use one of the getInstance methods, which will
	 * ensure that only two BooleanAttributeValue objects are created, thus avoiding excess object
	 * creation.
	 */
	private BooleanAttributeValue(boolean value)
	{
		super(identifier, value, value);
	}

	/**
	 * Get BooleanAttributeValue.TRUE (resp. FALSE) instance if <code>b</code> (resp. if !
	 * <code>b</code>)
	 * 
	 * @param b
	 * @return instance
	 */
	public static BooleanAttributeValue valueOf(boolean b)
	{
		return b ? TRUE : FALSE;
	}

	@Override
	protected Boolean parse(String stringForm) throws IllegalArgumentException
	{
		return DatatypeConverter.parseBoolean(stringForm);
	}

	/**
	 * not(this)
	 * 
	 * @return <code>!value</code>
	 */
	public BooleanAttributeValue not()
	{
		return value ? FALSE : TRUE;
	}

	@Override
	public String toString()
	{
		return DatatypeConverter.printBoolean(value);
	}

}
