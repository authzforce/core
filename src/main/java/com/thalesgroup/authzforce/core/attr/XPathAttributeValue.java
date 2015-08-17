package com.thalesgroup.authzforce.core.attr;

import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.xacml.schema.XACMLDatatype;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * Representation of XACML XPath expression datatype. This class supports parsing xs:string values. All objects
 * of this class are immutable and all methods of the class are thread-safe.
 */
public class XPathAttributeValue extends PrimitiveAttributeValue<String>
{
	private static final QName XPATH_CATEGORY_ATTRIBUTE_QNAME = new QName(XACMLVersion.V3_0.getNamespace(), "XPathCategory");
	
	private final String xpathCategory;
	
	/**
	 * Official name of this type
	 */
	public static final String identifier = XACMLDatatype.XPATH_EXPRESSION.value();

	/**
	 * Instantiates from XPath expression.
	 * 
	 * @param value
	 *            the <code>String</code> value to be represented
	 * @param xpathCategory XPathCategory
	 * @throws IllegalArgumentException if {@code value} is not a valid string representation for this value datatype
	 */
	public XPathAttributeValue(String value, String xpathCategory) throws IllegalArgumentException
	{
		super(identifier, value);
		this.xpathCategory = xpathCategory;
		this.getOtherAttributes().put(XPATH_CATEGORY_ATTRIBUTE_QNAME, xpathCategory);
	}

	/**
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(AttributeValueType)
	 */
	public XPathAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(jaxbAttrVal);
		this.xpathCategory = this.getOtherAttributes().get(XPATH_CATEGORY_ATTRIBUTE_QNAME);
	}

	@Override
	protected String parse(String stringForm)
	{
		return stringForm;
	}

	/**
	 * @return the xpathCategory
	 */
	public String getXpathCategory()
	{
		return xpathCategory;
	}

}
