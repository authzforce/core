/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.expression;

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

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.AttributeProvider;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.XMLUtils;
import org.ow2.authzforce.core.pdp.api.XMLUtils.XPathEvaluator;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;

/**
 * AttributeSelector evaluator, which uses XPath expressions (using Saxon parser) to resolve values from the Request or elsewhere. The AttributeSelector feature in optional in the XACML core
 * specification, and this implementation is experimental (not to be used in production).
 * <p>
 * Reasons for using SAXON's native API (s9api) in XPath evaluation instead of standard Java APIs (e.g. JAXP):
 *
 * <ol>
 * <li>Performance: See http://www.saxonica.com/documentation9.5/javadoc/net /sf/saxon/s9api/package-summary.html:
 * <p>
 * <i>This package provides Saxon's preferred Java API for XSLT, XQuery, XPath, and XML Schema processing. The interface is designed to hide as much as possible of the detail of the implementation.
 * However, the API architecture faithfully reflects the internal architecture of the Saxon product, unlike standard APIs such as JAXP and XQJ which in many cases force compromises in the design and
 * performance of the application.</i>
 * </p>
 * </li>
 *
 * <li>Functional: s9api provides XPATH 3.0 support, whereas standard Java APIs designed for XPATH 1.0 support only. See http://www.saxonica.com/html/documentation/conformance/jaxp.html. However, for
 * the moment, only XPath 1.0 and 2.0 are supported by this class. But we prepare for the future.</li>
 * </ol>
 * </p>
 *
 * @param <AV>
 *            AttributeSelector evaluation results' primitive returnType
 * 
 * @version $Id: $
 */
public final class AttributeSelectorExpression<AV extends AttributeValue> implements Expression<Bag<AV>>
{
	private interface ContextNodeSelector
	{

		XdmItem get(XdmNode contentNode, EvaluationContext context) throws IndeterminateEvaluationException;

	}

	// the logger we'll use for all messages
	// private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSelector.class);
	private static final IllegalArgumentException NULL_XACML_ATTRIBUTE_SELECTOR_EXCEPTION = new IllegalArgumentException("AttributeSelector's input XACML/JAXB AttributeSelector element undefined");
	private static final IllegalArgumentException NULL_ATTRIBUTE_Provider_BUT_NON_NULL_CONTEXT_SELECTOR_ID_EXCEPTION = new IllegalArgumentException(
			"Attribute Provider undefined but required for non-null ContextSelectorId in AttributeSelector");
	private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException("XPath version/compiler undefined but required for AttributeSelector evaluation");
	private static final IllegalArgumentException NULL_ATTRIBUTE_FACTORY_EXCEPTION = new IllegalArgumentException("AttributeSelector's returnType factory undefined");

	private static final String NODE_DESCRIPTION_FORMAT = "type=%s, name=%s, value=%s";

	private static final ContextNodeSelector CONTENT_NODE_SELECTOR = new ContextNodeSelector()
	{

		@Override
		public XdmItem get(final XdmNode contentNode, final EvaluationContext context)
		{
			return contentNode;
		}

	};

	private static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();

	private static final class AttributeDefinedContextNodeSelector implements ContextNodeSelector
	{
		private final AttributeProvider attrProvider;
		private final AttributeGUID contextSelectorId;
		private final String missingContextSelectorAttributeExceptionMessage;
		private final IndeterminateEvaluationException missingAttributeForUnknownReasonException;
		private final String xpathEvalErrMsgSuffix;
		private final XPathCompiler xPathCompiler;

		private AttributeDefinedContextNodeSelector(final String contentNodeSrcAttributeCategory, final AttributeGUID contextSelectorGUID, final AttributeProvider attrProvider,
				final XPathCompiler xPathCompiler, final String missingContextSelectorAttributeExceptionMessage, final IndeterminateEvaluationException missingAttributeForUnknownReasonException)
		{
			assert attrProvider != null && contextSelectorGUID != null && xPathCompiler != null;

			this.attrProvider = attrProvider;
			this.contextSelectorId = contextSelectorGUID;
			this.xPathCompiler = xPathCompiler;
			this.xpathEvalErrMsgSuffix = "' from ContextSelectorId='" + contextSelectorId + "' against Content of Attributes of Category=" + contentNodeSrcAttributeCategory;
			this.missingContextSelectorAttributeExceptionMessage = missingContextSelectorAttributeExceptionMessage;
			this.missingAttributeForUnknownReasonException = missingAttributeForUnknownReasonException;
		}

		@Override
		public XdmItem get(final XdmNode contentNode, final EvaluationContext context) throws IndeterminateEvaluationException
		{
			final Bag<XPathValue> bag = attrProvider.get(contextSelectorId, StandardDatatypes.XPATH_FACTORY.getDatatype(), context);
			if (bag == null)
			{
				throw this.missingAttributeForUnknownReasonException;
			}

			if (bag.isEmpty())
			{
				throw new IndeterminateEvaluationException(missingContextSelectorAttributeExceptionMessage, StatusHelper.STATUS_MISSING_ATTRIBUTE, bag.getReasonWhyEmpty());
			}

			final String contextSelectorPath = bag.getSingleElement().getUnderlyingValue();
			final XdmItem contextNode;
			try
			{
				contextNode = xPathCompiler.evaluateSingle(contextSelectorPath, contentNode);
			}
			catch (final SaxonApiException e)
			{
				throw new IndeterminateEvaluationException(this + ": Error evaluating XPath='" + contextSelectorPath + xpathEvalErrMsgSuffix, StatusHelper.STATUS_SYNTAX_ERROR, e);
			}

			if (contextNode == null)
			{
				throw new IndeterminateEvaluationException(this + ": No node returned by evaluation of XPath='" + contextSelectorPath + xpathEvalErrMsgSuffix, StatusHelper.STATUS_SYNTAX_ERROR);
			}

			return contextNode;
		}

	}

	private static String getDescription(final XdmNode node)
	{
		return String.format(NODE_DESCRIPTION_FORMAT, node.getNodeKind(), node.getNodeName(), node.getStringValue());
	}

	private static AttributeValueType xdmToJaxbAttributeValue(final String attrDatatype, final XdmNode node) throws IllegalArgumentException
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
				otherAttributes = Collections.emptyMap();
				content = Collections.<Serializable> singletonList(nodeStrVal);
				break;

			/*
			 * TODO: the commented cases below are more complex to handle. Further checking/testing is required before providing support for them. But first of all, are these cases worth the trouble?
			 * Find a few good use cases for them. In the meantime, do not remove these lines of code below, unless to rewrite/refactor with same quality level.
			 */
			// case Node.DOCUMENT_NODE:
			// case Node.ELEMENT_NODE:
			// final Unmarshaller u;
			// try
			// {
			// u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			// } catch (JAXBException e)
			// {
			// throw new IllegalArgumentException("Cannot create AttributeValue from XML node", e);
			// }
			//
			// final Object attrValue;
			// try
			// {
			// attrValue = u.unmarshal(node);
			// } catch (JAXBException e)
			// {
			// throw new
			// IllegalArgumentException(String.format("Cannot create AttributeValue from XML node: %s",
			// getDescription(node.getUnderlyingNode())), e);
			// }
			//
			// if (!(attrValue instanceof JAXBElement))
			// {
			// throw new
			// IllegalArgumentException(String.format("Cannot create AttributeValue from XML node: %s",
			// getDescription(node.getUnderlyingNode())));
			// }
			// xacmlAttrVal.getContent().add((Serializable) attrValue);
			// break;

			default:
				throw new IllegalArgumentException("Cannot create AttributeValue from XML node (type not supported): " + getDescription(node));
		}

		return new AttributeValueType(content, attrDatatype, otherAttributes);
	}

	private final transient AttributeSelectorId attributeSelectorId;
	private final transient ContextNodeSelector contextNodeSelector;
	private final transient XPathCompiler xpathCompiler;
	private final transient XMLUtils.XPathEvaluator xpathEvaluator;
	private final transient DatatypeFactory<?> attrFactory;
	private final transient BagDatatype<AV> returnType;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;
	private final transient IndeterminateEvaluationException missingAttributesContentException;
	private final transient String xpathEvalExceptionMessage;
	private final transient Bag.Validator mustBePresentEnforcer;

	// cached method results
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

	/** {@inheritDoc} */
	@Override
	public Bag<AV> getValue()
	{
		// depends on the evaluation context
		return null;
	}

	/**
	 * Creates instance from XACML model
	 *
	 * @param attrSelectorElement
	 *            XACML AttributeSelector
	 * @param xPathCompiler
	 *            XPATH compiler used for compiling {@code attrSelectorElement.getPath()} and XPath given by {@code attrSelectorElement.getContextSelectorId()} if not null
	 * @param attrProvider
	 *            AttributeProvider for finding value of the attribute identified by ContextSelectorId in {@code attrSelectorElement}; may be null if ContextSelectorId not specified
	 * @param attrFactory
	 *            attribute factory to create the AttributeValue(s) from the XML node(s) resolved by XPath
	 * @throws javax.xml.xpath.XPathExpressionException
	 *             if the Path could not be compiled to an XPath expression (using <code>namespaceContextNode</code> if non-null)
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code attrSelectorElement}, {@code xpathCompiler} or {@code attrFactory} is null; or ContextSelectorId is not null but {@code attrProvider} is null
	 */
	public AttributeSelectorExpression(final AttributeSelectorType attrSelectorElement, final XPathCompiler xPathCompiler, final AttributeProvider attrProvider, final DatatypeFactory<AV> attrFactory)
			throws XPathExpressionException, IllegalArgumentException
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

		this.attributeSelectorId = new AttributeSelectorId(attrSelectorElement);
		this.attrFactory = attrFactory;
		this.returnType = attrFactory.getBagDatatype();

		final String attributeCategory = attributeSelectorId.getCategory();
		final String contextSelectorId = attributeSelectorId.getContextSelectorId();
		final String missingAttributeMessage = this + " not found in context";
		if (attributeSelectorId.getContextSelectorId() == null)
		{
			this.contextNodeSelector = CONTENT_NODE_SELECTOR;
			this.xpathEvalExceptionMessage = this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + attributeCategory + "'";
			this.xpathCompiler = null;
		}
		else
		{
			if (attrProvider == null)
			{
				throw NULL_ATTRIBUTE_Provider_BUT_NON_NULL_CONTEXT_SELECTOR_ID_EXCEPTION;
			}

			final AttributeGUID contextSelectorGUID = new AttributeGUID(attributeSelectorId.getCategory(), null, contextSelectorId);
			final String missingContextSelectorAttributeExceptionMessage = this + ": No value found for attribute designated by Category=" + attributeCategory + " and ContextSelectorId="
					+ contextSelectorId;
			final IndeterminateEvaluationException missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(missingAttributeMessage + " for unknown reason",
					StatusHelper.STATUS_MISSING_ATTRIBUTE);
			this.contextNodeSelector = new AttributeDefinedContextNodeSelector(attributeCategory, contextSelectorGUID, attrProvider, xPathCompiler, missingContextSelectorAttributeExceptionMessage,
					missingAttributeForUnknownReasonException);
			this.xpathEvalExceptionMessage = this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + attributeCategory + "' selected by ContextSelectorId='"
					+ contextSelectorId + "'";
			this.xpathCompiler = xPathCompiler;
		}

		this.xpathEvaluator = new XPathEvaluator(attributeSelectorId.getPath(), xPathCompiler);

		// error messages/exceptions
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException("Missing Attributes/Attribute for evaluation of AttributeDesignator '" + this.attributeSelectorId
				+ "' because request context undefined", StatusHelper.STATUS_MISSING_ATTRIBUTE);
		this.missingAttributesContentException = new IndeterminateEvaluationException(this + ": No <Content> element found in Attributes of Category=" + attributeCategory,
				StatusHelper.STATUS_SYNTAX_ERROR);

		this.mustBePresentEnforcer = attrSelectorElement.isMustBePresent() ? new Bags.NonEmptinessValidator(missingAttributeMessage) : Bags.DUMB_VALIDATOR;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns the data type of the attribute values that the evaluation of this selector will return
	 */
	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return this.returnType;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public Bag<AV> evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Datatype<AV> attributeDatatype = returnType.getElementType();
		final Bag<AV> ctxResult = context.getAttributeSelectorResult(attributeSelectorId, attributeDatatype);
		// IF AttributeSelector already resolved in context
		if (ctxResult != null)
		{
			this.mustBePresentEnforcer.validate(ctxResult);
			return ctxResult;
		}

		// ELSE AttributeSelector not yet resolved in context, we have to do it now
		// get the DOM root of the request document
		final XdmNode contentNode = context.getAttributesContent(attributeSelectorId.getCategory());
		try
		{
			if (contentNode == null)
			{
				throw this.missingAttributesContentException;
			}

			final XdmItem contextNode = this.contextNodeSelector.get(contentNode, context);

			/*
			 * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new XPathSelector each time the expression is to be evaluated. However, the XPathSelector is serially
			 * reusable within a single thread. See Saxon Javadoc.
			 */
			final XPathSelector xpathSelector = xpathEvaluator.load();
			final XdmValue xpathEvalResult;
			try
			{
				xpathSelector.setContextItem(contextNode);
				xpathEvalResult = xpathSelector.evaluate();
			}
			catch (final SaxonApiException e)
			{
				throw new IndeterminateEvaluationException(this.xpathEvalExceptionMessage, StatusHelper.STATUS_SYNTAX_ERROR, e);
			}

			// The values in a bag are not ordered (§7.3.2 of XACML core spec) but may contain
			// duplicates
			final Deque<AV> resultBag = new ArrayDeque<>(xpathEvalResult.size());
			int xpathEvalResultItemIndex = 0;
			for (final XdmItem xpathEvalResultItem : xpathEvalResult)
			{
				final AttributeValueType jaxbAttrVal;
				if (xpathEvalResultItem instanceof XdmAtomicValue)
				{
					final String strVal = xpathEvalResultItem.getStringValue();
					jaxbAttrVal = new AttributeValueType(Collections.<Serializable> singletonList(strVal), attributeDatatype.getId(), null);
				}
				else if (xpathEvalResultItem instanceof XdmNode)
				{
					try
					{
						jaxbAttrVal = xdmToJaxbAttributeValue(attributeDatatype.getId(), (XdmNode) xpathEvalResultItem);
					}
					catch (final IllegalArgumentException e)
					{
						final String contextSelectorId = attributeSelectorId.getContextSelectorId();
						throw new IndeterminateEvaluationException(this + ": Error creating attribute value of type '" + attributeDatatype + "' from result #" + xpathEvalResultItemIndex
								+ " of evaluating XPath against XML node from Content of Attributes Category='" + attributeSelectorId.getCategory()
								+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'") + ": " + xpathEvalResultItem, StatusHelper.STATUS_SYNTAX_ERROR, e);
					}
				}
				else
				{
					final String contextSelectorId = attributeSelectorId.getContextSelectorId();
					throw new IndeterminateEvaluationException(this + ": Invalid type of result #" + xpathEvalResultItemIndex
							+ " from evaluating XPath against XML node from Content of Attributes Category='" + attributeSelectorId.getCategory()
							+ (contextSelectorId == null ? "" : "' selected by ContextSelectorId='" + contextSelectorId + "'") + xpathEvalResultItem.getClass().getName(),
							StatusHelper.STATUS_SYNTAX_ERROR);
				}

				final AttributeValue attrVal;
				try
				{
					attrVal = attrFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(),
							this.xpathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					final String contextSelectorId = attributeSelectorId.getContextSelectorId();
					throw new IndeterminateEvaluationException(this + ": Error creating attribute value of type '"
							+ attributeDatatype + "' from result #" + xpathEvalResultItemIndex
							+ " of evaluating XPath against XML node from Content of Attributes Category='"
							+ attributeSelectorId.getCategory() + "'"
							+ (contextSelectorId == null ? ""
									: " selected by ContextSelectorId='" + contextSelectorId + "'")
							+ ": " + xpathEvalResultItem, StatusHelper.STATUS_SYNTAX_ERROR, e);
				}

				resultBag.add(attributeDatatype.cast(attrVal));
				xpathEvalResultItemIndex++;
			}

			final Bag<AV> result = Bags.getInstance(attributeDatatype, resultBag);
			context.putAttributeSelectorResultIfAbsent(attributeSelectorId, result);
			this.mustBePresentEnforcer.validate(result);
			return result;
		}
		catch (final IndeterminateEvaluationException e)
		{
			/**
			 * If error occurred, we put the empty value to prevent retry in the same context, which may succeed at another time in the same context, resulting in different value of the same attribute
			 * at different times during evaluation within the same context, therefore inconsistencies. The value(s) must remain constant during the evaluation context, as explained in section 7.3.5
			 * Attribute Retrieval of XACML core spec:
			 * <p>
			 * Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before
			 * it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use 3313 the same bag of values that was initially tested.)
			 * </p>
			 * Therefore, if no value found, we keep it that way until evaluation is done for the current request context.
			 * <p>
			 * We could put the null value to indicate the evaluation error, instead of an empty Bag, but it would make the result of the code used at the start of this method ambiguous/confusing:
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
			final Bag<AV> result = Bags.empty(attributeDatatype, e);
			context.putAttributeSelectorResultIfAbsent(attributeSelectorId, result);
			mustBePresentEnforcer.validate(result);
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		/*
		 * Because this class is immutable (excluding toString which is derived from others), we can do caching (and lazy init) of this method result.
		 */
		if (toString == null)
		{
			toString = "AttributeSelector [" + this.attributeSelectorId + ", dataType= " + this.returnType.getElementType() + ", mustBePresent= "
					+ (mustBePresentEnforcer == Bags.DUMB_VALIDATOR ? "false" : "true") + "]";
		}

		return toString;
	}

	/** {@inheritDoc} */
	@Override
	public JAXBElement<AttributeSelectorType> getJAXBElement()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.attributeSelectorId.hashCode();
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
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
		return this.attributeSelectorId.equals(other.attributeSelectorId);
	}

}
