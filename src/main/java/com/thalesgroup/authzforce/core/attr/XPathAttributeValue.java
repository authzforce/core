/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.attr;

import java.util.Map;

import javax.xml.namespace.QName;

import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * Representation of XACML XPath expression datatype. This class supports parsing xs:string values.
 * All objects of this class are immutable and all methods of the class are thread-safe.
 */
public class XPathAttributeValue extends SimpleAttributeValue<String, XPathAttributeValue>
{
	private static final QName XPATH_CATEGORY_ATTRIBUTE_QNAME = new QName(XACMLVersion.V3_0.getNamespace(), "XPathCategory");

	private final String xpathCategory;

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression";

	/**
	 * Datatype factory instance
	 */
	public static final AttributeValue.Factory<XPathAttributeValue> FACTORY = new SimpleAttributeValue.Factory<XPathAttributeValue>(XPathAttributeValue.class, TYPE_URI)
	{
		@Override
		public XPathAttributeValue getInstance(String value, Map<QName, String> otherXmlAttributes) throws IllegalArgumentException
		{
			return new XPathAttributeValue(value, otherXmlAttributes.get(XPATH_CATEGORY_ATTRIBUTE_QNAME));
		}

	};

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
		super(FACTORY.instanceDatatype, value);
		this.xpathCategory = xpathCategory;
		this.getOtherAttributes().put(XPATH_CATEGORY_ATTRIBUTE_QNAME, xpathCategory);
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

	@Override
	public XPathAttributeValue one()
	{
		return this;
	}

}
