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
import java.util.Objects;

import javax.xml.namespace.QName;

import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression.Utils.XPathEvaluator;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Representation of XACML XPath expression datatype. This class supports parsing xs:string values.
 * All objects of this class are immutable and all methods of the class are thread-safe.
 */
public class XPathAttributeValue extends SimpleAttributeValue<String, XPathAttributeValue>
{
	/**
	 * QName of XPathCategory attribute in xpathExpression. This is allowed by XACML schema as part
	 * of:
	 * 
	 * <pre>
	 * {@code
	 * <xs:anyAttribute namespace="##any" processContents="lax"/>
	 * }
	 * </pre>
	 * 
	 * ... therefore namespace returned by JAXB is empty "". More info:
	 * https://jaxb.java.net/tutorial/section_6_2_7_5
	 * -Collecting-Unspecified-Attributes-XmlAnyAttribute
	 * .html#Collecting%20Unspecified%20Attributes:%20XmlAnyAttribute
	 */
	private static final QName XPATH_CATEGORY_ATTRIBUTE_QNAME = new QName("", "XPathCategory");

	private final String xpathCategory;

	private final XPathEvaluator xpathEvaluator;

	private final IndeterminateEvaluationException missingAttributesContentException;

	private final String xpathEvalExceptionMessage;

	// lasy eval: evaluated only when calling evaluate(EvaluationContext)
	private XdmValue xpathEvalResult = null;

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
		public XPathAttributeValue getInstance(String value, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			return new XPathAttributeValue(value, otherXmlAttributes.get(XPATH_CATEGORY_ATTRIBUTE_QNAME), xPathCompiler);
		}

	};

	private static final IllegalArgumentException NULL_XPATH_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined XPathCategory for XPath expression value");
	private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException("Undefined XPath version/compiler (possibly missing RequestDefaults/PolicyDefaults element)");

	/**
	 * Instantiates from XPath expression.
	 * 
	 * @param xpath
	 *            XPath
	 * @param xpathCategory
	 *            XPathCategory
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating {@code xpath}
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation for this value datatype
	 */
	public XPathAttributeValue(String xpath, String xpathCategory, XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		super(FACTORY.instanceDatatype, xpath);
		if (xpathCategory == null)
		{
			throw NULL_XPATH_CATEGORY_EXCEPTION;
		}

		if (xPathCompiler == null)
		{
			throw NULL_XPATH_COMPILER_EXCEPTION;
		}

		this.xpathEvaluator = new XPathEvaluator(xpath, xPathCompiler);
		/*
		 * Please note that StandardURIChecker maintains a thread-local cache of validated URIs
		 * (cache size is 50 and eviction policy is LRU)
		 */
		if (!StandardURIChecker.getInstance().isValidURI(xpathCategory))
		{
			throw new IllegalArgumentException("Invalid value for XPathCategory (xs:anyURI): " + xpathCategory);
		}

		this.xpathCategory = xpathCategory;
		this.getOtherAttributes().put(XPATH_CATEGORY_ATTRIBUTE_QNAME, xpathCategory);
		this.missingAttributesContentException = new IndeterminateEvaluationException(this + ": No <Content> element found in Attributes of Category=" + xpathCategory, Status.STATUS_SYNTAX_ERROR);
		this.xpathEvalExceptionMessage = this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + xpathCategory + "'";
	}

	@Override
	protected String parse(String str)
	{
		return str;
	}

	/**
	 * @return the xpathCategory
	 */
	public String getXpathCategory()
	{
		return xpathCategory;
	}

	@Override
	public boolean isStatic()
	{
		/*
		 * XPathAttributeValue is an exception among attribute values, i.e. its evaluation is not
		 * static, on the contrary to other AttributeValues, as it depends on the context to get the
		 * matching nodes by XPath.
		 */
		return false;
	}

	@Override
	public XPathAttributeValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		final XdmNode contentNode = context.getAttributesContent(this.xpathCategory);
		if (contentNode == null)
		{
			throw this.missingAttributesContentException;
		}

		/*
		 * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new
		 * XPathSelector each time the expression is to be evaluated. However, the XPathSelector is
		 * serially reusable within a single thread. See Saxon Javadoc.
		 */
		final XPathSelector xpathSelector = xpathEvaluator.load();
		try
		{
			xpathSelector.setContextItem(contentNode);
			this.xpathEvalResult = xpathSelector.evaluate();
		} catch (SaxonApiException e)
		{
			throw new IndeterminateEvaluationException(this.xpathEvalExceptionMessage, Status.STATUS_SYNTAX_ERROR, e);
		}

		// return self as usual
		return this;
	}

	private int hashCode = 0;

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			// hash regardless of letter case
			hashCode = Objects.hash(xpathCategory, value);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final XPathAttributeValue other = (XPathAttributeValue) obj;
		return xpathCategory.equals(other.xpathCategory) && value.equals(value);
	}

	/**
	 * Get the node set resulting from XPath evaluation against the request (context)'s
	 * Attributes/Content
	 * 
	 * @return the xpathEvalResult, requires to call {@link #evaluate(EvaluationContext)} first;
	 *         otherwise null.
	 */
	public XdmValue getXpathEvaluationResult()
	{
		return xpathEvalResult;
	}
}
