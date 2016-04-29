/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.value;

import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;

import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.XMLUtils.XPathEvaluator;

/**
 * Representation of XACML xpathExpression datatype. All objects of this class are immutable and all methods of the class are thread-safe.
 * <p>
 * XACML 3.0 Core spec, §A.3.15: "An XPath expression evaluates to a node-set, which is a set of XML nodes that match the expression. A node or node-set is not in the formal data-type system of XACML.
 * All comparison or other operations on node-sets are performed in isolation of the particular [XPATH-based] function specified. The context nodes and namespace mappings of the XPath expressions are
 * defined by the XPath data-type, see section B.3."
 * <p>
 * In short, the xpathExpression is evaluated in the context of calling XPath-based functions on a given evaluation context only. These functions typically use {@link #evaluate(EvaluationContext)} to
 * get the matching node-set.
 * <p>
 * WARNING: this class is not optimized for request-time evaluation but for policy initialization-time. Therefore, its use is not recommended for evaluating xpathExpressions in XACML Request. We
 * consider it not useful in the latter case, as the Requester (PEP) could evaluate the xpathExpressions in the first place, and does not need the PDP to do it.
 */
public final class XPathValue extends SimpleValue<String>
{
	/**
	 * QName of XPathCategory attribute in xpathExpression. This is allowed by XACML schema as part of:
	 * 
	 * <pre>
	 * {@code
	 * <xs:anyAttribute namespace="##any" processContents="lax"/>
	 * }
	 * </pre>
	 * 
	 * ... therefore namespace returned by JAXB is empty "". More info: https://jaxb.java.net/tutorial/section_6_2_7_5 -Collecting-Unspecified-Attributes-XmlAnyAttribute
	 * .html#Collecting%20Unspecified%20Attributes:%20XmlAnyAttribute
	 */
	public static final QName XPATH_CATEGORY_ATTRIBUTE_QNAME = new QName("", "XPathCategory");

	private final String xpathCategory;

	private final transient XPathEvaluator xpathEvaluator;

	private final IndeterminateEvaluationException missingAttributesContentException;

	private final String xpathEvalExceptionMessage;

	private IndeterminateEvaluationException missingContextException;

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression";

	private static final IllegalArgumentException NULL_XPATH_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined XPathCategory for XPath expression value");
	private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException(
			"Undefined XPath version/compiler (possibly missing RequestDefaults/PolicyDefaults element)");

	/**
	 * Instantiates from XPath expression.
	 * 
	 * @param xpath
	 *            XPath
	 * @param otherXmlAttributes
	 *            other XML attributes on the xpathExpression AttributeValue node, one of which is expected to be the {@value #XPATH_CATEGORY_ATTRIBUTE_QNAME }
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating {@code xpath}
	 * @throws IllegalArgumentException
	 *             if {@code value} is not a valid string representation for this value datatype
	 */
	public XPathValue(String xpath, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		super(TYPE_URI, xpath);
		this.xpathCategory = otherXmlAttributes.get(XPATH_CATEGORY_ATTRIBUTE_QNAME);
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
		 * Please note that StandardURIChecker maintains a thread-local cache of validated URIs (cache size is 50 and eviction policy is LRU)
		 */
		if (!StandardURIChecker.getInstance().isValidURI(xpathCategory))
		{
			throw new IllegalArgumentException("Invalid value for XPathCategory (xs:anyURI): " + xpathCategory);
		}

		this.missingAttributesContentException = new IndeterminateEvaluationException(this + ": No <Content> element found in Attributes of Category=" + xpathCategory,
				StatusHelper.STATUS_SYNTAX_ERROR);
		this.xpathEvalExceptionMessage = this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + xpathCategory + "'";
		this.missingContextException = new IndeterminateEvaluationException(this + ":  undefined evaluation context: XPath value cannot be evaluated", StatusHelper.STATUS_PROCESSING_ERROR);
	}

	/**
	 * Convenient method to get the XML nodes ("node-set") matching the XPath expression from the Content node of the XACML Attributes element with category <i>XPathCategory</i> in this
	 * {@code context}. <i>XPathCategory</i> is extracted from the attribute of the same name in {@code otherXmlAttributes} argument passed to {@link #XPathValue(String, Map, XPathCompiler)} when
	 * creating this instance. To be used by XPath-based functions defined in section A.3.15 of XACML 3.0 Core specification.
	 * 
	 * @param context
	 *            current evaluation context
	 * @return node-set
	 * @throws IndeterminateEvaluationException
	 *             error evaluating the XPath expression
	 */
	public XdmValue evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw this.missingContextException;
		}

		final XdmNode contentNode = context.getAttributesContent(this.xpathCategory);
		if (contentNode == null)
		{
			throw this.missingAttributesContentException;
		}

		/*
		 * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new XPathSelector each time the expression is to be evaluated. However, the XPathSelector is serially
		 * reusable within a single thread. See Saxon Javadoc.
		 */
		final XPathSelector xpathSelector = xpathEvaluator.load();
		try
		{
			xpathSelector.setContextItem(contentNode);
			return xpathSelector.evaluate();
		} catch (SaxonApiException e)
		{
			throw new IndeterminateEvaluationException(this.xpathEvalExceptionMessage, StatusHelper.STATUS_SYNTAX_ERROR, e);
		}
	}

	private transient volatile int hashCode = 0; // Effective Java - Item 9

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
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof XPathValue))
		{
			return false;
		}

		final XPathValue other = (XPathValue) obj;
		return this.xpathCategory.equals(other.xpathCategory) && this.value.equals(other.value);
	}

	@Override
	public String printXML()
	{
		return this.value;
	}
}
