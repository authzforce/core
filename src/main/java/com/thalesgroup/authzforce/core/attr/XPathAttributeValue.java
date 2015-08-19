package com.thalesgroup.authzforce.core.attr;

import javax.xml.namespace.QName;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * Representation of XACML XPath expression datatype. This class supports parsing xs:string values.
 * All objects of this class are immutable and all methods of the class are thread-safe.
 */
public class XPathAttributeValue extends PrimitiveAttributeValue<String>
{
	private static final QName XPATH_CATEGORY_ATTRIBUTE_QNAME = new QName(XACMLVersion.V3_0.getNamespace(), "XPathCategory");

	private final String xpathCategory;

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression";

	/**
	 * Primitive datatype info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(TYPE_URI);

	/**
	 * Instantiates from XPath expression.
	 * 
	 * @param value
	 *            the <code>String</code> value to be represented
	 * @param xpathCategory
	 *            XPathCategory
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation for this value datatype
	 */
	public XPathAttributeValue(String value, String xpathCategory) throws IllegalArgumentException
	{
		super(TYPE, value);
		this.xpathCategory = xpathCategory;
		this.getOtherAttributes().put(XPATH_CATEGORY_ATTRIBUTE_QNAME, xpathCategory);
	}

	/**
	 * Creates instance from XML/JAXB value
	 * 
	 * @param jaxbAttrVal
	 *            JAXB AttributeValue
	 * @throws IllegalArgumentException
	 *             if not valid value for datatype {@value #TYPE_URI}
	 * @see PrimitiveAttributeValue#PrimitiveAttributeValue(DatatypeDef, AttributeValueType)
	 */
	public XPathAttributeValue(AttributeValueType jaxbAttrVal) throws IllegalArgumentException
	{
		super(TYPE, jaxbAttrVal);
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
