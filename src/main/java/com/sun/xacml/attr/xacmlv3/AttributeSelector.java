/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.attr.xacmlv3;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.AttributeFinder;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeSelectorId;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.XPathAttributeValue;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;

/**
 * Implements AttributeSelector support, which uses XPath expressions (using Saxon parser) to
 * resolve values from the Request or elsewhere. The AttributeSelector feature in optional in the
 * XACML core specification, and this implementation is experimental (not to be used in production).
 * <p>
 * Reasons for using SAXON's native API (s9api) in XPath evaluation instead of standard Java APIs
 * (e.g. JAXP):
 * 
 * <ol>
 * <li>Performance: See http://www.saxonica.com/documentation9.5/javadoc/net
 * /sf/saxon/s9api/package-summary.html:
 * <p>
 * <i>This package provides Saxon's preferred Java API for XSLT, XQuery, XPath, and XML Schema
 * processing. The interface is designed to hide as much as possible of the detail of the
 * implementation. However, the API architecture faithfully reflects the internal architecture of
 * the Saxon product, unlike standard APIs such as JAXP and XQJ which in many cases force
 * compromises in the design and performance of the application.</i>
 * </p>
 * </li>
 * 
 * <li>Functional: s9api provides XPATH 3.0 support, whereas standard Java APIs designed for XPATH
 * 1.0 support only. See http://www.saxonica.com/html/documentation/conformance/jaxp.html. However,
 * for the moment, only XPath 1.0 and 2.0 are supported by this class. But we prepare for the
 * future.</li>
 * </ol>
 * </p>
 * 
 * @param <T>
 *            AttributeSelector evaluation results' primitive datatype
 */
public class AttributeSelector<T extends AttributeValue> extends AttributeSelectorType implements Expression<BagResult<T>>
{
	/*
	 * Wrapper around XPathExecutable that provides the original XPath expression from which the
	 * XPathExecutable was compiled, via toString() method.
	 */
	private static class XPathExecutableWrapper
	{
		private final XPathExecutable exec;
		private final String expr;

		private XPathExecutableWrapper(XPathExecutable xpathExe, String xpathExpression)
		{
			this.exec = xpathExe;
			this.expr = xpathExpression;
		}

		@Override
		public String toString()
		{
			return expr;
		}
	}

	// the logger we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSelector.class);
	private static final IllegalArgumentException NULL_XACML_ATTRIBUTE_SELECTOR_EXCEPTION = new IllegalArgumentException("XACML/JAXB AttributeSelector element undefined");
	private static final IllegalArgumentException NULL_ATTRIBUTE_FINDER_BUT_NON_NULL_CONTEXT_SELECTOR_ID_EXCEPTION = new IllegalArgumentException("Attribute finder undefined but required for non-null ContextSelectorId in AttributeSelector");
	private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException("XPath compiler undefined");
	private static final IllegalArgumentException NULL_ATTRIBUTE_FACTORY_EXCEPTION = new IllegalArgumentException("Attribute datatype factory undefined");
	private static final UnsupportedOperationException UNSUPPORTED_PATH_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("Path field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CONTEXT_SELECTOR_ID_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("ContextSelectorId field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("Category field is read-only");

	private static final UnsupportedOperationException UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION = new UnsupportedOperationException("DataType field is read-only");

	private static final String NODE_DESCRIPTION_FORMAT = "type=%s, name=%s, value=%s";

	private static String getDescription(XdmNode node)
	{
		return String.format(NODE_DESCRIPTION_FORMAT, node.getNodeKind(), node.getNodeName(), node.getStringValue());
	}

	private static AttributeValueType xdmToJaxbAttributeValue(String attrDatatype, XdmNode node) throws ParsingException
	{
		final AttributeValueType xacmlAttrVal = new AttributeValueType();
		xacmlAttrVal.setDataType(attrDatatype);
		switch (node.getNodeKind())
		{
			case ATTRIBUTE:
				xacmlAttrVal.getOtherAttributes().put(new QName(node.getNodeName().getNamespaceURI(), node.getNodeName().getLocalName()), node.getStringValue());
				break;

			case TEXT:
				xacmlAttrVal.getContent().add(node.getStringValue());
				break;

			// case Node.DOCUMENT_NODE:
			// case Node.ELEMENT_NODE:
			// final Unmarshaller u;
			// try
			// {
			// u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			// } catch (JAXBException e)
			// {
			// throw new ParsingException("Cannot create AttributeValue from XML node", e);
			// }
			//
			// final Object attrValue;
			// try
			// {
			// attrValue = u.unmarshal(node);
			// } catch (JAXBException e)
			// {
			// throw new
			// ParsingException(String.format("Cannot create AttributeValue from XML node: %s",
			// getDescription(node.getUnderlyingNode())), e);
			// }
			//
			// if (!(attrValue instanceof JAXBElement))
			// {
			// throw new
			// ParsingException(String.format("Cannot create AttributeValue from XML node: %s",
			// getDescription(node.getUnderlyingNode())));
			// }
			// xacmlAttrVal.getContent().add((Serializable) attrValue);
			// break;

			default:
				throw new ParsingException("Cannot create AttributeValue from XML node (type not supported): " + getDescription(node));
		}

		return xacmlAttrVal;
	}

	private final IndeterminateEvaluationException missingAttributeException;
	private final String missingAttributeMessage;
	private final DatatypeDef returnType;

	private final AttributeFinder attrFinder;

	private final AttributeGUID contextSelectorGUID;

	private final XPathExecutableWrapper xpathExecWrapper;

	private final AttributeValue.Factory<? extends AttributeValue> attrFactory;

	private final AttributeSelectorId id;

	private final Class<T> datatypeClass;

	private final XPathCompiler xpathCompiler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang
	 * .String)
	 */
	@Override
	public final void setDataType(String value)
	{
		// prevent de-synchronization of dataType with returnType while keeping field final
		throw UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setCategory(java.lang
	 * .String)
	 */
	@Override
	public final void setCategory(String value)
	{
		// prevent de-synchronization with this.attrGUID's Category while keeping field final
		throw UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setAttributeId(java.lang
	 * .String)
	 */
	@Override
	public final void setContextSelectorId(String value)
	{
		// prevent de-synchronization of this.attrGUID's AttributeId while keeping field final
		throw UNSUPPORTED_CONTEXT_SELECTOR_ID_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang
	 * .String)
	 */
	@Override
	public final void setPath(String value)
	{
		// prevent de-synchronization of Path with xpathExpr while keeping field final
		throw UNSUPPORTED_PATH_SET_OPERATION_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
	 */
	@Override
	public boolean isStatic()
	{
		// depends on the evaluation context
		return false;
	}

	/**
	 * Creates instance from XACML model
	 * 
	 * @param attrSelectorElement
	 *            XACML AttributeSelector
	 * @param xpathCompiler
	 *            XPATH compiler for compiling {@code attrSelectorElement.getPath()}
	 * @param attrFinder
	 *            AttributeFinder for finding value of the attribute identified by ContextSelectorId
	 *            in {@code attrSelectorElement}; may be null if ContextSelectorId not specified
	 * @param attrFactory
	 *            attribute factory to create the AttributeValue(s) from the XML node(s) resolved by
	 *            XPath
	 * @param datatypeClass
	 *            evaluation result's primitive datatype class
	 * @throws XPathExpressionException
	 *             if the Path could not be compiled to an XPath expression (using
	 *             <code>namespaceContextNode</code> if non-null)
	 * @throws IllegalArgumentException
	 *             if {@code attrSelectorElement}, {@code xpathCompiler} or {@code attrFactory} is
	 *             null; or ContextSelectorId is not null but {@code attrFinder} is null
	 */
	public AttributeSelector(AttributeSelectorType attrSelectorElement, XPathCompiler xpathCompiler, AttributeFinder attrFinder, AttributeValue.Factory<? extends AttributeValue> attrFactory, Class<T> datatypeClass) throws XPathExpressionException, IllegalArgumentException
	{
		if (attrSelectorElement == null)
		{
			throw NULL_XACML_ATTRIBUTE_SELECTOR_EXCEPTION;
		}

		// JAXB attributes
		this.category = attrSelectorElement.getCategory();
		this.dataType = attrSelectorElement.getDataType();
		this.mustBePresent = attrSelectorElement.isMustBePresent();
		this.contextSelectorId = attrSelectorElement.getContextSelectorId();
		this.path = attrSelectorElement.getPath();

		// others
		if (contextSelectorId == null)
		{
			this.contextSelectorGUID = null;
			this.attrFinder = null;
		} else
		{
			this.contextSelectorGUID = new AttributeGUID(category, null, contextSelectorId);
			if (attrFinder == null)
			{
				throw NULL_ATTRIBUTE_FINDER_BUT_NON_NULL_CONTEXT_SELECTOR_ID_EXCEPTION;
			}

			this.attrFinder = attrFinder;
		}

		if (xpathCompiler == null)
		{
			throw NULL_XPATH_COMPILER_EXCEPTION;
		}

		if (attrFactory == null)
		{
			throw NULL_ATTRIBUTE_FACTORY_EXCEPTION;
		}

		this.id = new AttributeSelectorId(attrSelectorElement);
		this.xpathCompiler = xpathCompiler;
		this.attrFactory = attrFactory;
		this.returnType = new DatatypeDef(dataType, true);
		this.missingAttributeMessage = "No attribute matching " + this;
		this.missingAttributeException = new IndeterminateEvaluationException(Status.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage);
		this.datatypeClass = datatypeClass;

		final XPathExecutable xpathExec;
		try
		{
			xpathExec = xpathCompiler.compile(path);
		} catch (SaxonApiException e)
		{
			throw new IllegalArgumentException(this + ": Invalid XPath", e);
		}

		xpathExecWrapper = new XPathExecutableWrapper(xpathExec, path);
	}

	/**
	 * Returns the data type of the attribute values that this selector will resolve
	 * 
	 * @return the data type of the values found by this selector
	 */
	@Override
	public DatatypeDef getReturnType()
	{
		return this.returnType;
	}

	/**
	 * Invokes the <code>AttributeFinder</code> used by the given <code>EvaluationContext</code> to
	 * try to resolve an attribute value. If the selector is defined with MustBePresent as true,
	 * then failure to find a matching value will result in Indeterminate, otherwise it will result
	 * in an empty bag. To support the com.thalesgroup.authzforce.core.test.basic selector
	 * functionality defined in the XACML specification, use a finder that has only the
	 * <code>SelectorModule</code> as a module that supports selector finding.
	 * 
	 * @param context
	 *            representation of the request to search
	 * 
	 * @return a result containing a bag either empty because no values were found or containing at
	 *         least one value, or status associated with an Indeterminate result
	 */
	@Override
	public BagResult<T> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw new IndeterminateEvaluationException("Missing Attributes/Content for evaluation of AttributeSelector '" + id + "' because request context undefined", Status.STATUS_MISSING_ATTRIBUTE);
		}

		final BagResult<T> ctxResult = context.getAttributeSelectorResult(id, datatypeClass, dataType);
		final BagResult<T> result;
		if (ctxResult != null)
		{
			result = ctxResult;
		} else
		{
			// get the DOM root of the request document
			final XdmNode contentNode = context.getAttributesContent(category);
			if (contentNode == null)
			{
				throw new IndeterminateEvaluationException(this + ": No Content element found in Attributes of Category=" + category, Status.STATUS_SYNTAX_ERROR);
			}

			final XdmItem contextNode;
			if (contextSelectorGUID == null)
			{
				contextNode = contentNode;
			} else
			{
				final BagResult<XPathAttributeValue> bag = attrFinder.findAttribute(returnType, contextSelectorGUID, context, XPathAttributeValue.class);
				if (bag == null || bag.isEmpty())
				{
					throw new IndeterminateEvaluationException(this + ": No value found for attribute designated by Category=" + category + " and ContextSelectorId=" + contextSelectorId, Status.STATUS_MISSING_ATTRIBUTE);
				}

				final String contextSelectorPath = bag.values()[0].getValue();
				try
				{
					contextNode = xpathCompiler.evaluateSingle(contextSelectorPath, contentNode);
				} catch (SaxonApiException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error evaluating XPath='" + contextSelectorPath + "' from ContextSelectorId='" + contextSelectorId + "' against Content of Attributes of Category=" + category, Status.STATUS_SYNTAX_ERROR, e);
				}

				if (contextNode == null)
				{
					throw new IndeterminateEvaluationException(this + ": No node returned by evaluation of XPath='" + contextSelectorPath + "' from ContextSelectorId='" + contextSelectorId + "' against Content of Attributes of Category=" + category, Status.STATUS_SYNTAX_ERROR);
				}
			}

			/*
			 * An XPathExecutable is immutable, and therefore thread-safe. It is simplest to load a
			 * new XPathSelector each time the expression is to be evaluated. However, the
			 * XPathSelector is serially reusable within a single thread. See Saxon Javadoc.
			 */
			final XPathSelector xpathSelector = xpathExecWrapper.exec.load();
			final XdmValue xpathEvalResult;
			try
			{
				xpathSelector.setContextItem(contextNode);
				xpathEvalResult = xpathSelector.evaluate();
			} catch (SaxonApiException e)
			{
				throw new IndeterminateEvaluationException(this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + category + contextSelectorId == null ? "" : ("' selected by ContextSelectorId='" + contextSelectorId + "'"), Status.STATUS_SYNTAX_ERROR, e);
			}

			// preserve order of results
			final Queue<T> resultBag = new ArrayDeque<>();
			int xpathEvalResultItemIndex = 0;
			for (final XdmItem xpathEvalResultItem : xpathEvalResult)
			{
				final AttributeValueType jaxbAttrVal;
				if (xpathEvalResultItem instanceof XdmAtomicValue)
				{
					final String strVal = xpathEvalResultItem.getStringValue();
					jaxbAttrVal = new AttributeValueType(Collections.<Serializable> singletonList(strVal), dataType, null);
				} else if (xpathEvalResultItem instanceof XdmNode)
				{
					try
					{
						jaxbAttrVal = xdmToJaxbAttributeValue(dataType, (XdmNode) xpathEvalResultItem);
					} catch (ParsingException e)
					{
						throw new IndeterminateEvaluationException(this + ": Error creating attribute value of type '" + dataType + "' from result #" + xpathEvalResultItemIndex + " of evaluating XPath against XML node from Content of Attributes Category='" + category
								+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'") + ": " + xpathEvalResultItem, Status.STATUS_SYNTAX_ERROR, e);
					}
				} else
				{
					throw new IndeterminateEvaluationException(this + ": Invalid type of result #" + xpathEvalResultItemIndex + " from evaluating XPath against XML node from Content of Attributes Category='" + category
							+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'") + xpathEvalResultItem.getClass().getName(), Status.STATUS_SYNTAX_ERROR);
				}

				final AttributeValue attrVal;
				try
				{
					attrVal = attrFactory.getInstance(jaxbAttrVal);
				} catch (IllegalArgumentException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error creating attribute value of type '" + dataType + "' from result #" + xpathEvalResultItemIndex + " of evaluating XPath against XML node from Content of Attributes Category='" + category
							+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'") + ": " + xpathEvalResultItem, Status.STATUS_SYNTAX_ERROR, e);
				}

				resultBag.add(datatypeClass.cast(attrVal));
				xpathEvalResultItemIndex++;
			}

			result = new BagResult<>(resultBag, datatypeClass, returnType);
			context.putAttributeSelectorResultIfAbsent(id, result);
		}

		// see if it's an empty bag
		if (result.isEmpty())
		{
			// see if this is an error or not
			if (mustBePresent)
			{
				// this is an error
				LOGGER.info(missingAttributeMessage);
				throw missingAttributeException;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "AttributeSelector [" + (category != null ? "category=" + category + ", " : "") + (contextSelectorId != null ? "contextSelectorId=" + contextSelectorId + ", " : "") + (path != null ? "path=" + path + ", " : "") + (dataType != null ? "dataType=" + dataType + ", " : "")
				+ "mustBePresent=" + mustBePresent + "]";
	}

	@Override
	public JAXBElement<AttributeSelectorType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeSelector(this);
	}

}
