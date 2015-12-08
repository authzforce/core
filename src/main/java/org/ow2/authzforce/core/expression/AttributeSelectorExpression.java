/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.expression;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.AttributeProvider;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.XACMLBindingUtils;
import org.ow2.authzforce.core.expression.Expressions.XPathEvaluator;
import org.ow2.authzforce.core.value.AttributeValue;
import org.ow2.authzforce.core.value.Bag;
import org.ow2.authzforce.core.value.BagDatatype;
import org.ow2.authzforce.core.value.Bags;
import org.ow2.authzforce.core.value.Datatype;
import org.ow2.authzforce.core.value.DatatypeConstants;
import org.ow2.authzforce.core.value.DatatypeFactory;
import org.ow2.authzforce.core.value.XPathValue;

import com.sun.xacml.ParsingException;

/**
 * Implements AttributeSelector support, which uses XPath expressions (using Saxon parser) to resolve values from the Request or elsewhere. The
 * AttributeSelector feature in optional in the XACML core specification, and this implementation is experimental (not to be used in production).
 * <p>
 * Reasons for using SAXON's native API (s9api) in XPath evaluation instead of standard Java APIs (e.g. JAXP):
 * 
 * <ol>
 * <li>Performance: See http://www.saxonica.com/documentation9.5/javadoc/net /sf/saxon/s9api/package-summary.html:
 * <p>
 * <i>This package provides Saxon's preferred Java API for XSLT, XQuery, XPath, and XML Schema processing. The interface is designed to hide as much as possible
 * of the detail of the implementation. However, the API architecture faithfully reflects the internal architecture of the Saxon product, unlike standard APIs
 * such as JAXP and XQJ which in many cases force compromises in the design and performance of the application.</i>
 * </p>
 * </li>
 * 
 * <li>Functional: s9api provides XPATH 3.0 support, whereas standard Java APIs designed for XPATH 1.0 support only. See
 * http://www.saxonica.com/html/documentation/conformance/jaxp.html. However, for the moment, only XPath 1.0 and 2.0 are supported by this class. But we prepare
 * for the future.</li>
 * </ol>
 * </p>
 * 
 * @param <AV>
 *            AttributeSelector evaluation results' primitive returnType
 */
public class AttributeSelectorExpression<AV extends AttributeValue> extends AttributeSelectorType implements Expression<Bag<AV>>
{
	// the logger we'll use for all messages
	// private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSelector.class);
	private static final IllegalArgumentException NULL_XACML_ATTRIBUTE_SELECTOR_EXCEPTION = new IllegalArgumentException(
			"AttributeSelector's input XACML/JAXB AttributeSelector element undefined");
	private static final IllegalArgumentException NULL_ATTRIBUTE_Provider_BUT_NON_NULL_CONTEXT_SELECTOR_ID_EXCEPTION = new IllegalArgumentException(
			"Attribute Provider undefined but required for non-null ContextSelectorId in AttributeSelector");
	private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException(
			"XPath version/compiler undefined but required for AttributeSelector evaluation");
	private static final IllegalArgumentException NULL_ATTRIBUTE_FACTORY_EXCEPTION = new IllegalArgumentException(
			"AttributeSelector's returnType factory undefined");
	private static final UnsupportedOperationException UNSUPPORTED_PATH_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"<AttributeSelector>'s Path field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CONTEXT_SELECTOR_ID_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"<AttributeSelector>'s ContextSelectorId field is read-only");
	private static final UnsupportedOperationException UNSUPPORTED_CATEGORY_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"<AttributeSelector>'s Category field is read-only");

	private static final UnsupportedOperationException UNSUPPORTED_DATATYPE_SET_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"<AttributeSelector>'s DataType field is read-only");

	private static final String NODE_DESCRIPTION_FORMAT = "type=%s, name=%s, value=%s";

	private static String getDescription(XdmNode node)
	{
		return String.format(NODE_DESCRIPTION_FORMAT, node.getNodeKind(), node.getNodeName(), node.getStringValue());
	}

	private static AttributeValueType xdmToJaxbAttributeValue(String attrDatatype, XdmNode node) throws ParsingException
	{
		final Map<QName, String> otherAttributes;
		final List<Serializable> content;
		final String nodeStrVal = node.getStringValue();
		switch (node.getNodeKind())
		{
		case ATTRIBUTE:
			/*
			 * We only take the attribute value. (For XPath getting an attribute Value, the result XdmNode still holds the attribute QName.)
			 */
		case TEXT:
			otherAttributes = null;
			content = Collections.<Serializable> singletonList(nodeStrVal);
			break;

		/*
		 * TODO: the commented cases below are more complex to handle. Further checking/testing is required before providing support for them. But first of all,
		 * are these cases worth the trouble? Find a few good use cases for them. In the meantime, do not remove these lines of code below, unless to
		 * rewrite/refactor with same quality level.
		 */
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

		return new AttributeValueType(content, attrDatatype, otherAttributes);
	}

	private final transient String missingAttributeMessage;

	private final transient AttributeProvider attrProvider;

	private final transient AttributeGUID contextSelectorGUID;

	private final transient XPathCompiler xpathCompiler;
	private final transient Expressions.XPathEvaluator xpathEvaluator;

	private final transient DatatypeFactory<?> attrFactory;

	private final AttributeSelectorId id;

	private final transient BagDatatype<AV> returnType;

	private final transient IndeterminateEvaluationException missingAttributeForUnknownReasonException;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;
	private final transient IndeterminateEvaluationException missingAttributesContentException;
	private final transient String missingContextSelectorAttributeExceptionMessage;
	private final transient String xpathEvalExceptionMessage;

	// cached method results
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;
	private final Datatype<AV> attributeType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang .String)
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setCategory(java.lang .String)
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setAttributeId(java.lang .String)
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
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType#setDataType(java.lang .String)
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
	 * @param xPathCompiler
	 *            XPATH compiler used for compiling {@code attrSelectorElement.getPath()} and XPath given by {@code attrSelectorElement.getContextSelectorId()}
	 *            if not null
	 * @param attrProvider
	 *            AttributeProvider for finding value of the attribute identified by ContextSelectorId in {@code attrSelectorElement}; may be null if
	 *            ContextSelectorId not specified
	 * @param attrFactory
	 *            attribute factory to create the AttributeValue(s) from the XML node(s) resolved by XPath
	 * @throws XPathExpressionException
	 *             if the Path could not be compiled to an XPath expression (using <code>namespaceContextNode</code> if non-null)
	 * @throws IllegalArgumentException
	 *             if {@code attrSelectorElement}, {@code xpathCompiler} or {@code attrFactory} is null; or ContextSelectorId is not null but
	 *             {@code attrProvider} is null
	 */
	public AttributeSelectorExpression(AttributeSelectorType attrSelectorElement, XPathCompiler xPathCompiler, AttributeProvider attrProvider,
			DatatypeFactory<AV> attrFactory) throws XPathExpressionException, IllegalArgumentException
	{
		if (attrSelectorElement == null)
		{
			throw NULL_XACML_ATTRIBUTE_SELECTOR_EXCEPTION;
		}

		if (attrFactory == null)
		{
			throw NULL_ATTRIBUTE_FACTORY_EXCEPTION;
		}

		if (xPathCompiler == null)
		{
			throw NULL_XPATH_COMPILER_EXCEPTION;
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
			this.attrProvider = null;
			this.missingContextSelectorAttributeExceptionMessage = null;
			this.xpathEvalExceptionMessage = this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + category + "'";
			this.xpathCompiler = null;
		} else
		{
			if (attrProvider == null)
			{
				throw NULL_ATTRIBUTE_Provider_BUT_NON_NULL_CONTEXT_SELECTOR_ID_EXCEPTION;
			}

			this.contextSelectorGUID = new AttributeGUID(category, null, contextSelectorId);
			this.attrProvider = attrProvider;
			this.missingContextSelectorAttributeExceptionMessage = this + ": No value found for attribute designated by Category=" + category
					+ " and ContextSelectorId=" + contextSelectorId;
			this.xpathEvalExceptionMessage = this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + category
					+ "' selected by ContextSelectorId='" + contextSelectorId + "'";
			this.xpathCompiler = xPathCompiler;
		}

		this.id = new AttributeSelectorId(attrSelectorElement);
		this.attrFactory = attrFactory;
		this.returnType = attrFactory.getBagDatatype();
		this.attributeType = attrFactory.getDatatype();
		this.xpathEvaluator = new XPathEvaluator(path, xPathCompiler);

		// error messages/exceptions
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException(
				"Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.id + "' because request context undefined",
				StatusHelper.STATUS_MISSING_ATTRIBUTE);
		this.missingAttributesContentException = new IndeterminateEvaluationException(this + ": No <Content> element found in Attributes of Category="
				+ category, StatusHelper.STATUS_SYNTAX_ERROR);
		this.missingAttributeMessage = this + " not found in context";
		this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(StatusHelper.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage
				+ " for unknown reason");
	}

	/**
	 * Returns the data type of the attribute values that the evaluation of this selector will return
	 * 
	 * @return the data type of the values found by this selector
	 */
	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return this.returnType;
	}

	private void validateResult(Bag<AV> result) throws IndeterminateEvaluationException
	{
		if (mustBePresent && result.isEmpty())
		{
			throw new IndeterminateEvaluationException(StatusHelper.STATUS_MISSING_ATTRIBUTE, missingAttributeMessage, result.getReasonWhyEmpty());
		}
	}

	/**
	 * Invokes the <code>AttributeProvider</code> used by the given <code>EvaluationContext</code> to try to resolve an attribute value. If the selector is
	 * defined with MustBePresent as true, then failure to find a matching value will result in Indeterminate, otherwise it will result in an empty bag. To
	 * support the com.thalesgroup.authzforce.core.test.basic selector functionality defined in the XACML specification, use a Provider that has only the
	 * <code>SelectorModule</code> as a module that supports selector finding.
	 * 
	 * @param context
	 *            representation of the request to search
	 * 
	 * @return a result containing a bag either empty because no values were found or containing at least one value, or status associated with an Indeterminate
	 *         result
	 */
	@Override
	public Bag<AV> evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Bag<AV> ctxResult = context.getAttributeSelectorResult(id, attributeType);
		// IF AttributeSelector already resolved in context
		if (ctxResult != null)
		{
			validateResult(ctxResult);
			return ctxResult;
		}

		// ELSE AttributeSelector not yet resolved in context, we have to do it now
		// get the DOM root of the request document
		final XdmNode contentNode = context.getAttributesContent(category);
		try
		{
			if (contentNode == null)
			{
				throw this.missingAttributesContentException;
			}

			final XdmItem contextNode;
			if (contextSelectorGUID == null)
			{
				contextNode = contentNode;
			} else
			{
				final Bag<XPathValue> bag = attrProvider.get(contextSelectorGUID, DatatypeConstants.XPATH.TYPE, context);
				if (bag == null)
				{
					throw this.missingAttributeForUnknownReasonException;
				}

				if (bag.isEmpty())
				{
					throw new IndeterminateEvaluationException(missingContextSelectorAttributeExceptionMessage, StatusHelper.STATUS_MISSING_ATTRIBUTE,
							bag.getReasonWhyEmpty());
				}

				final String contextSelectorPath = bag.getSingleValue().getUnderlyingValue();
				try
				{
					contextNode = xpathCompiler.evaluateSingle(contextSelectorPath, contentNode);
				} catch (SaxonApiException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error evaluating XPath='" + contextSelectorPath + "' from ContextSelectorId='"
							+ contextSelectorId + "' against Content of Attributes of Category=" + category, StatusHelper.STATUS_SYNTAX_ERROR, e);
				}

				if (contextNode == null)
				{
					throw new IndeterminateEvaluationException(this + ": No node returned by evaluation of XPath='" + contextSelectorPath
							+ "' from ContextSelectorId='" + contextSelectorId + "' against Content of Attributes of Category=" + category,
							StatusHelper.STATUS_SYNTAX_ERROR);
				}
			}

			/*
			 * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new XPathSelector each time the expression is to be
			 * evaluated. However, the XPathSelector is serially reusable within a single thread. See Saxon Javadoc.
			 */
			final XPathSelector xpathSelector = xpathEvaluator.load();
			final XdmValue xpathEvalResult;
			try
			{
				xpathSelector.setContextItem(contextNode);
				xpathEvalResult = xpathSelector.evaluate();
			} catch (SaxonApiException e)
			{
				throw new IndeterminateEvaluationException(this.xpathEvalExceptionMessage, StatusHelper.STATUS_SYNTAX_ERROR, e);
			}

			// The values in a bag are not ordered (§7.3.2 of XACML core spec) but may contain
			// duplicates
			final Deque<AV> resultBag = new ArrayDeque<>();
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
						throw new IndeterminateEvaluationException(
								this + ": Error creating attribute value of type '" + dataType + "' from result #" + xpathEvalResultItemIndex
										+ " of evaluating XPath against XML node from Content of Attributes Category='" + category
										+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'") + ": "
										+ xpathEvalResultItem, StatusHelper.STATUS_SYNTAX_ERROR, e);
					}
				} else
				{
					throw new IndeterminateEvaluationException(this + ": Invalid type of result #" + xpathEvalResultItemIndex
							+ " from evaluating XPath against XML node from Content of Attributes Category='" + category
							+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'")
							+ xpathEvalResultItem.getClass().getName(), StatusHelper.STATUS_SYNTAX_ERROR);
				}

				final AttributeValue attrVal;
				try
				{
					attrVal = attrFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(), this.xpathCompiler);
				} catch (IllegalArgumentException e)
				{
					throw new IndeterminateEvaluationException(this + ": Error creating attribute value of type '" + dataType + "' from result #"
							+ xpathEvalResultItemIndex + " of evaluating XPath against XML node from Content of Attributes Category='" + category + "'"
							+ (contextSelectorId == null ? "" : " selected by ContextSelectorId='" + contextSelectorId + "'") + ": " + xpathEvalResultItem,
							StatusHelper.STATUS_SYNTAX_ERROR, e);
				}

				resultBag.add(returnType.getElementType().cast(attrVal));
				xpathEvalResultItemIndex++;
			}

			final Bag<AV> result = Bags.getInstance(attributeType, resultBag);
			context.putAttributeSelectorResultIfAbsent(id, result);
			validateResult(result);
			return result;
		} catch (IndeterminateEvaluationException e)
		{
			/**
			 * If error occurred, we put the empty value to prevent retry in the same context, which may succeed at another time in the same context, resulting
			 * in different value of the same attribute at different times during evaluation within the same context, therefore inconsistencies. The value(s)
			 * must remain constant during the evaluation context, as explained in section 7.3.5 Attribute Retrieval of XACML core spec:
			 * <p>
			 * Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values
			 * is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of
			 * that attribute shall use 3313 the same bag of values that was initially tested.)
			 * </p>
			 * Therefore, if no value found, we keep it that way until evaluation is done for the current request context.
			 * <p>
			 * We could put the null value to indicate the evaluation error, instead of an empty Bag, but it would make the result of the code used at the start
			 * of this method ambiguous/confusing:
			 * <p>
			 * <code>
			 * final Bag<T> contextBag = context.getAttributeSelectorResult(id,...)
			 * </code>
			 * </p>
			 * <p>
			 * Indeed, contextBag could be null for one of these two reasons:
			 * <ol>
			 * <li>The attribute selector has never been requested in this context;
			 * <li>It has been requested before in this context but could not be found: error occurred (IndeterminateEvaluationException)</li>
			 * </ol>
			 * To avoid this confusion, we put an empty Bag (with some error info saying why this is empty).
			 * </p>
			 */
			final Bag<AV> result = Bags.empty(attributeType, e);
			context.putAttributeSelectorResultIfAbsent(id, result);
			validateResult(result);
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		/*
		 * Because this class is immutable (excluding toString which is derived from others), we can do caching (and lazy init) of this method result.
		 */
		if (toString == null)
		{
			toString = "AttributeSelector [" + (category != null ? "category=" + category + ", " : "")
					+ (contextSelectorId != null ? "contextSelectorId=" + contextSelectorId + ", " : "") + (path != null ? "path=" + path + ", " : "")
					+ (dataType != null ? "dataType=" + dataType + ", " : "") + "mustBePresent=" + mustBePresent + "]";
		}

		return toString;
	}

	@Override
	public JAXBElement<AttributeSelectorType> getJAXBElement()
	{
		return XACMLBindingUtils.XACML_3_0_OBJECT_FACTORY.createAttributeSelector(this);
	}

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.id.hashCode();
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof AttributeSelectorExpression))
		{
			return false;
		}

		final AttributeSelectorExpression<?> other = (AttributeSelectorExpression<?>) obj;
		return this.id.equals(other.id);
	}

}
