/*
 * Copyright 2012-2022 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.expression;

import com.google.common.base.Preconditions;
import net.sf.saxon.s9api.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MissingAttributeDetail;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.expression.AttributeSelectorExpression;
import org.ow2.authzforce.core.pdp.api.expression.VariableReference;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.*;

/**
 * Static utility methods pertaining to {@link AttributeSelectorExpression} instances.
 */
public final class AttributeSelectorExpressions
{
    private AttributeSelectorExpressions()
    {
        // hide constructor
    }

    /**
     * Extensible AttributeSelector evaluator, that may be extended to support ContextSelectorId, and which uses SAXON parser to evaluate XPath expressions. The AttributeSelector feature in optional
     * in the XACML core specification, and this implementation is experimental (not to be used in production).
     * <p>
     * Reasons for using SAXON's native API (s9api) in XPath evaluation instead of standard Java APIs (e.g. JAXP):
     *
     * <ol>
     * <li>Performance: See <a href="http://www.saxonica.com/documentation9.5/javadoc/net/sf/saxon/s9api/package-summary.html">Package net.sf.saxon.s9api Description</a>:
     * <p>
     * <i>This package provides Saxon's preferred Java API for XSLT, XQuery, XPath, and XML Schema processing. The interface is designed to hide as much as possible of the detail of the
     * implementation. However, the API architecture faithfully reflects the internal architecture of the Saxon product, unlike standard APIs such as JAXP and XQJ which in many cases force compromises
     * in the design and performance of the application.</i>
     * </p>
     * </li>
     *
     * <li>Functional: s9api provides XPATH 3.0 support, whereas standard Java APIs designed for XPATH 1.0 support only. See http://www.saxonica.com/html/documentation/conformance/jaxp.html. However,
     * for the moment, only XPath 1.0 and 2.0 are supported by this class. But we prepare for the future.</li>
     * </ol>
     * </p>
     *
     * @param <AV> AttributeSelector evaluation results' primitive returnType
     * @version $Id: $
     */
    private static abstract class ExtensibleAttributeSelectorExpression<AV extends AttributeValue> implements AttributeSelectorExpression<AV>
    {
        private static final IllegalArgumentException NULL_INPUT_XPATH_EXPRESSION_BAG_EXCEPTION = new IllegalArgumentException("Input xpathExpression bag is null or empty");

        private static final IndeterminateEvaluationException NULL_CONTENT_ARG_EXCEPTION = new IndeterminateEvaluationException("Undefined <Content>", XacmlStatusCode.PROCESSING_ERROR.value());

        // the logger we'll use for all messages
        // private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSelector.class);
        private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException("XPath version/compiler undefined but required for AttributeSelector evaluation");
        private static final IllegalArgumentException NULL_ATTRIBUTE_FACTORY_EXCEPTION = new IllegalArgumentException("AttributeSelector's returnType factory undefined");

        private static final String NODE_DESCRIPTION_FORMAT = "type=%s, name=%s, value=%s";

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
                    content = Collections.singletonList(nodeStrVal);
                    break;

                /*
                 * TODO: the commented cases below are more complex to handle. Further checking/testing is required before providing support for them. But first of all, are these cases worth the
                 * trouble? Find a few good use cases for them. In the meantime, do not remove these lines of code below, unless to rewrite/refactor with same quality level.
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

        protected final AttributeSelectorId attributeSelectorId;
        private final boolean mustBePresent;
        private final AttributeValueFactory<?> attrFactory;
        private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;
        private final transient IndeterminateEvaluationException missingAttributesContentException;

        protected final MissingAttributeDetail xacmlMissingAttributeDetail;


        private final transient Bag.Validator mustBePresentEnforcer;
        protected final transient XPathCompilerProxy xPathCompiler;
        private final transient Optional<XPathCompilerProxy> optXPathCompiler;
        private final transient XPathExecutable xPathEvaluator;
        private final transient List<VariableReference<?>> xpathVariables;
        private final transient BagDatatype<AV> returnType;

        // cached method results
        private transient volatile String toString = null;
        private transient volatile int hashCode = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public final Optional<Bag<AV>> getValue()
        {
            // depends on the evaluation context
            return Optional.empty();
        }

        /**
         * Creates instance from XACML model
         *
         * @param attrSelectorCategory XACML AttributeSelector's Category
         * @param attrSelectorPath     XACML AttributeSelector's Path
         * @param contextSelectorId    XACML AttributeSelector's ContextSelectorId
         * @param xPathCompiler        XPATH compiler used for compiling {@code attrSelectorElement.getPath()} and XPath given by {@code attrSelectorElement.getContextSelectorId()} if not null
         * @param attrFactory          attribute factory to create the AttributeValue(s) from the XML node(s) resolved by XPath
         * @throws java.lang.IllegalArgumentException if {@code attrSelectorElement == null || xPathCompiler == null || attrFactory == null}; or {@code attrSelectorElement.getContextSelectorId() != null} but
         *                                            {@code attrProvider == null}; or {@code attrSelectorElement.getPath()} is not a valid XPath expression
         */
        private ExtensibleAttributeSelectorExpression(final String attrSelectorCategory, final String attrSelectorPath, final Optional<String> contextSelectorId, final boolean mustBePresent, final XPathCompilerProxy xPathCompiler, final AttributeValueFactory<AV> attrFactory)
                throws IllegalArgumentException
        {
            if (attrFactory == null)
            {
                throw NULL_ATTRIBUTE_FACTORY_EXCEPTION;
            }

            if (xPathCompiler == null)
            {
                throw NULL_XPATH_COMPILER_EXCEPTION;
            }

            this.attributeSelectorId = new AttributeSelectorId(attrSelectorCategory, attrSelectorPath, contextSelectorId);
            this.attrFactory = attrFactory;
            this.returnType = attrFactory.getDatatype().getBagDatatype();

            final String attributeCategory = attributeSelectorId.getCategory();

            this.xPathCompiler = xPathCompiler;
            this.optXPathCompiler = Optional.of(this.xPathCompiler);

            try
            {
                this.xPathEvaluator = xPathCompiler.compile(attributeSelectorId.getPath());
            } catch (final SaxonApiException e)
            {
                throw new IllegalArgumentException("AttributeSelector's Path is not a valid XPath " + xPathCompiler.getXPathVersion().getVersionNumber() + " expression: '" + attributeSelectorId.getPath() + "'", e);
            }

            final List<VariableReference<?>> allowedVars = xPathCompiler.getAllowedVariables();
            this.xpathVariables = new ArrayList<>(allowedVars.size());
            for(final Iterator<net.sf.saxon.s9api.QName> varNames = xPathEvaluator.iterateExternalVariables(); varNames.hasNext();) {
                final net.sf.saxon.s9api.QName xpathVarName = varNames.next();
                final Optional<VariableReference<?>> varRef = allowedVars.stream().filter(allowedVar -> allowedVar.getXPathVariableName().equals(xpathVarName)).findAny();
                if(varRef.isEmpty()) {
                    throw new IllegalArgumentException("Unexpected variable '"+xpathVarName+"' in XPath expression. Not matching any (XACML) Policy VariableDefinition in: " + allowedVars);
                }

                xpathVariables.add(varRef.get());
            }

            // error messages/exceptions
            this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException("Missing request context for evaluating AttributeSelector '" + this.attributeSelectorId + "'",
                    XacmlStatusCode.PROCESSING_ERROR.value());
            this.missingAttributesContentException = new IndeterminateEvaluationException(this + ": No <Content> element found in Attributes of Category='" + attributeCategory + "'",
                    XacmlStatusCode.SYNTAX_ERROR.value());

            // Empty string (undefined) for ContextSelectorId (AttributeId) if undefined is allowed as xs:anyURI
            this.xacmlMissingAttributeDetail = new MissingAttributeDetail(null, attrSelectorCategory, contextSelectorId.orElse(""), attrFactory.getDatatype().getId(), null);

            this.mustBePresent = mustBePresent;
            this.mustBePresentEnforcer = mustBePresent ? new Bags.NonEmptinessValidator(new ImmutableXacmlStatus(xacmlMissingAttributeDetail, Optional.empty(), Optional.of(this + " not found in context"))) : Bags.DUMB_VALIDATOR;
        }

        protected abstract ImmutableXacmlStatus getXpathEvalErrorStatus();

        /**
         * Creates instance from XACML model
         *
         * @param attrSelectorElement XACML AttributeSelector
         * @param xPathCompiler       XPATH compiler used for compiling {@code attrSelectorElement.getPath()} and XPath given by {@code attrSelectorElement.getContextSelectorId()} if not null
         * @param attrFactory         attribute factory to create the AttributeValue(s) from the XML node(s) resolved by XPath
         * @throws java.lang.IllegalArgumentException if {@code attrSelectorElement == null || xPathCompiler == null || attrFactory == null}; or {@code attrSelectorElement.getContextSelectorId() != null} but
         *                                            {@code attrProvider == null}; or {@code attrSelectorElement.getPath()} is not a valid XPath expression
         */
        private ExtensibleAttributeSelectorExpression(final AttributeSelectorType attrSelectorElement, final XPathCompilerProxy xPathCompiler, final AttributeValueFactory<AV> attrFactory)
                throws IllegalArgumentException
        {
            this(attrSelectorElement.getCategory(), attrSelectorElement.getPath(), Optional.ofNullable(attrSelectorElement.getContextSelectorId()), attrSelectorElement.isMustBePresent(), xPathCompiler, attrFactory);
        }

        @Override
        public final AttributeSelectorId getAttributeSelectorId()
        {
            return this.attributeSelectorId;
        }

        @Override
        public final boolean isNonEmptyBagRequired()
        {
            return this.mustBePresent;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Returns the data type of the attribute values that the evaluation of this selector will return
         */
        @Override
        public final Datatype<Bag<AV>> getReturnType()
        {
            return this.returnType;
        }

        private Bag<AV> checkContextForCachedEvalResult(final EvaluationContext context) throws IndeterminateEvaluationException
        {
            /*
             * Check the context whether the evaluation result is not already there
             */
            if (context == null)
            {
                throw missingAttributeBecauseNullContextException;
            }

            final Bag<AV> ctxResult = context.getAttributeSelectorResult(this);
            // IF AttributeSelector already resolved in context
            if (ctxResult != null)
            {
                this.mustBePresentEnforcer.validate(ctxResult);
                return ctxResult;
            }

            return null;
        }

        private Bag<AV> handleRecoverableIndeterminate(final IndeterminateEvaluationException e, final EvaluationContext context) throws IndeterminateEvaluationException
        {
            /*
             * If a non-fatal/recoverable error occurred during AttributeSelector evaluation, we put the empty value to prevent retry in the same context, which may succeed at another time in the same
             * context, resulting in different value of the same attribute at different times during evaluation within the same context, therefore inconsistencies. The value(s) must remain constant
             * during the evaluation context, as explained in section 7.3.5 Attribute Retrieval of XACML core spec:
             * <p>
             * Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before
             * it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use 3313 the same bag of values that was initially tested.)
             * </p>
             * Therefore, if no value found, we keep it that way until evaluation is done for the current request context.
             * <p>
             * We could put the null value to indicate the evaluation error, instead of an empty Bag, but it would make checking the context for any cached result next time a bit ambiguous/confusing
             * (see method checkContextForCachedEvalResult()), for instance:
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
            final Bag<AV> result = Bags.empty(this.returnType.getElementType(), e);
            context.putAttributeSelectorResultIfAbsent(this, result);
            mustBePresentEnforcer.validate(result);
            return result;
        }

        private Bag<AV> evaluateFinal(final XdmItem xPathEvaluationContextItem, final EvaluationContext context) throws IndeterminateEvaluationException
        {
            /*
             * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new XPathSelector each time the expression is to be evaluated. However, the XPathSelector is serially
             * reusable within a single thread. See Saxon Javadoc.
             */
            final XPathSelector xpathSelector = xPathEvaluator.load();
            final XdmValue xpathEvalResult;
            try
            {
                for(final VariableReference<?> xpathVar: xpathVariables) {
                    final Value val = context.getVariableValue(xpathVar.getVariableId(), xpathVar.getReturnType());
                    xpathSelector.setVariable(xpathVar.getXPathVariableName(), val.getXdmValue());
                }
                xpathSelector.setContextItem(xPathEvaluationContextItem);
                xpathEvalResult = xpathSelector.evaluate();
            } catch (final SaxonApiException e)
            {
                throw new IndeterminateEvaluationException(getXpathEvalErrorStatus(), e);
            }

            final Datatype<AV> attributeDatatype = returnType.getElementType();
            /*
             * The values in a bag are not ordered (§7.3.2 of XACML core spec) but may contain duplicates
             */
            final Collection<AV> resultBag = new ArrayDeque<>(xpathEvalResult.size());
            int xpathEvalResultItemIndex = 0;
            for (final XdmItem xpathEvalResultItem : xpathEvalResult)
            {
                final AttributeValueType jaxbAttrVal;
                if (xpathEvalResultItem instanceof XdmAtomicValue)
                {
                    final String strVal = xpathEvalResultItem.getStringValue();
                    jaxbAttrVal = new AttributeValueType(Collections.singletonList(strVal), attributeDatatype.getId(), null);
                } else if (xpathEvalResultItem instanceof XdmNode)
                {
                    try
                    {
                        jaxbAttrVal = xdmToJaxbAttributeValue(attributeDatatype.getId(), (XdmNode) xpathEvalResultItem);
                    } catch (final IllegalArgumentException e)
                    {
                        final Optional<String> contextSelectorId = attributeSelectorId.getContextSelectorId();
                        throw new IndeterminateEvaluationException(
                                this + ": Error creating attribute value of type '" + attributeDatatype + "' from result #" + xpathEvalResultItemIndex
                                        + " of evaluating XPath against XML node from Content of Attributes Category='" + attributeSelectorId.getCategory()
                                        + (contextSelectorId.map(id -> "' selected by ContextSelectorId='" + id + "'").orElse("")) + ": " + xpathEvalResultItem,
                                XacmlStatusCode.SYNTAX_ERROR.value(), e);
                    }
                } else
                {
                    final Optional<String> contextSelectorId = attributeSelectorId.getContextSelectorId();
                    throw new IndeterminateEvaluationException(this + ": Invalid type of result #" + xpathEvalResultItemIndex
                            + " from evaluating XPath against XML node from Content of Attributes Category='" + attributeSelectorId.getCategory()
                            + (contextSelectorId.map(id -> "' selected by ContextSelectorId='" + id + "'").orElse("")) + ": " + xpathEvalResultItem.getClass().getName(),
                            XacmlStatusCode.SYNTAX_ERROR.value());
                }

                final AttributeValue attrVal;
                try
                {
                    attrVal = attrFactory.getInstance(jaxbAttrVal.getContent(), jaxbAttrVal.getOtherAttributes(), optXPathCompiler);
                } catch (final IllegalArgumentException e)
                {
                    final Optional<String> contextSelectorId = attributeSelectorId.getContextSelectorId();
                    throw new IndeterminateEvaluationException(
                            this + ": Error creating attribute value of type '" + attributeDatatype + "' from result #" + xpathEvalResultItemIndex
                                    + " of evaluating XPath against XML node from Content of Attributes Category='" + attributeSelectorId.getCategory() + "'"
                                    + (contextSelectorId.map(id -> "' selected by ContextSelectorId='" + id + "'").orElse("")) + ": " + xpathEvalResultItem,
                            XacmlStatusCode.SYNTAX_ERROR.value(), e);
                }

                resultBag.add(attributeDatatype.cast(attrVal));
                xpathEvalResultItemIndex++;
            }

            final Bag<AV> result = Bags.newBag(attributeDatatype, resultBag);
            context.putAttributeSelectorResultIfAbsent(this, result);
            this.mustBePresentEnforcer.validate(result);
            return result;
        }

        @Override
        public final Bag<AV> evaluate(final XdmNode contentElement, final Optional<XPathExecutable> contextPathEvaluator, final EvaluationContext context) throws IndeterminateEvaluationException
        {
            final Bag<AV> cachedResult = checkContextForCachedEvalResult(context);
            if (cachedResult != null)
            {
                return cachedResult;
            }

            // AttributeSelector not yet evaluated in context, we have to do it now
            if (contentElement == null)
            {
                throw NULL_CONTENT_ARG_EXCEPTION;
            }

            final XdmItem finalXPathEvaluationContextItem;
            try
            {
                if (contextPathEvaluator.isPresent())
                {
                    /*
                     * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new XPathSelector each time the expression is to be evaluated. However, the XPathSelector is
                     * serially reusable within a single thread. See SAXON Javadoc.
                     */
                    final XPathSelector contextPathSelector = contextPathEvaluator.get().load();
                    try
                    {
                        for(final Map.Entry<VariableReference<?>, Value> var: context.getVariables()) {
                            contextPathSelector.setVariable(var.getKey().getXPathVariableName(), var.getValue().getXdmValue());
                        }
                        contextPathSelector.setContextItem(contentElement);
                        finalXPathEvaluationContextItem = contextPathSelector.evaluateSingle();
                    } catch (final SaxonApiException e)
                    {
                        throw new IndeterminateEvaluationException(
                                this + ": Error evaluating XPath = '" + contextPathEvaluator.get().getUnderlyingExpression().getInternalExpression().toString() + "' against <Content> element",
                                XacmlStatusCode.PROCESSING_ERROR.value(), e);
                    }

                    if (finalXPathEvaluationContextItem == null)
                    {
                        throw new IndeterminateEvaluationException(this + ": No node returned by evaluation of XPath = '"
                                + contextPathEvaluator.get().getUnderlyingExpression().getInternalExpression().toString() + "' against <Content> element", XacmlStatusCode.SYNTAX_ERROR.value());
                    }
                } else
                {
                    finalXPathEvaluationContextItem = contentElement;
                }

                return evaluateFinal(finalXPathEvaluationContextItem, context);
            } catch (final IndeterminateEvaluationException e)
            {
                return handleRecoverableIndeterminate(e, context);
            }
        }

        protected abstract XdmItem getFinalXPathEvaluationContextItem(XdmNode contentElement, EvaluationContext context, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;

        /**
         * {@inheritDoc}
         */
        @Override
        public final Bag<AV> evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
        {
            final Bag<AV> cachedResult = checkContextForCachedEvalResult(context);
            if (cachedResult != null)
            {
                return cachedResult;
            }

            // ELSE AttributeSelector not yet resolved in context, we have to do it now
            // get the DOM root of the request document
            final XdmNode contentElement = context.getAttributesContent(attributeSelectorId.getCategory());
            try
            {
                if (contentElement == null)
                {
                    throw this.missingAttributesContentException;
                }

                final XdmItem finalXPathEvaluationContextItem = getFinalXPathEvaluationContextItem(contentElement, context, mdpContext);
                return evaluateFinal(finalXPathEvaluationContextItem, context);
            } catch (final IndeterminateEvaluationException e)
            {
                return handleRecoverableIndeterminate(e, context);
            }
        }

        @Override
        public XPathExecutable getXPath(final Bag<XPathValue> xpathExpressionBag) throws IllegalArgumentException
        {
            if (xpathExpressionBag == null || xpathExpressionBag.isEmpty())
            {
                throw NULL_INPUT_XPATH_EXPRESSION_BAG_EXCEPTION;
            }

            final String xpathExpression = xpathExpressionBag.getSingleElement().getUnderlyingValue();

            try
            {
                return xPathCompiler.compile(xpathExpression);
            } catch (final SaxonApiException e)
            {
                throw new IllegalArgumentException("Input value given as context selector value is not a valid XPath " + xPathCompiler.getXPathVersion().getVersionNumber() + " expression: '" + xpathExpression + "'",
                        e);
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */

        /**
         * {@inheritDoc}
         */
        @Override
        public final String toString()
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

        /**
         * {@inheritDoc}
         */
        @Override
        public final int hashCode()
        {
            if (hashCode == 0)
            {
                hashCode = this.attributeSelectorId.hashCode();
            }

            return hashCode;
        }

        /**
         * Equal iff the Category, Path and ContextSelectorId are equal (Datatype is ignored)
         */
        @Override
        public final boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }

            if (!(obj instanceof ExtensibleAttributeSelectorExpression))
            {
                return false;
            }

            final ExtensibleAttributeSelectorExpression<?> other = (ExtensibleAttributeSelectorExpression<?>) obj;
            return this.attributeSelectorId.equals(other.attributeSelectorId);
        }

    }

    private static final class AttributeSelectorExpressionWithoutContextSelector<AV extends AttributeValue> extends ExtensibleAttributeSelectorExpression<AV>
    {
        private final ImmutableXacmlStatus xpathEvalErrStatus;

        private AttributeSelectorExpressionWithoutContextSelector(final String attributeSelectorCategory, final String attributeSelectorPath, boolean mustBePresent, final XPathCompilerProxy xPathCompiler,
                                                                  final AttributeValueFactory<AV> attributeFactory) throws IllegalArgumentException
        {
            super(attributeSelectorCategory, attributeSelectorPath, Optional.empty(), mustBePresent, xPathCompiler, attributeFactory);
            this.xpathEvalErrStatus =  new ImmutableXacmlStatus(XacmlStatusCode.SYNTAX_ERROR.value(), Optional.of(this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + attributeSelectorCategory + "'"));
        }

        @Override
        protected ImmutableXacmlStatus getXpathEvalErrorStatus()
        {
            return xpathEvalErrStatus;
        }

        @Override
        protected XdmItem getFinalXPathEvaluationContextItem(final XdmNode contentElement, final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
        {
            return contentElement;
        }

        @Override
        public Optional<AttributeFqn> getContextSelectorFQN()
        {
            return Optional.empty();
        }
    }

    private static final class AttributeSelectorExpressionWithContextSelector<AV extends AttributeValue> extends ExtensibleAttributeSelectorExpression<AV>
    {
        private final Optional<AttributeFqn> contextSelectorFQN;
        private final SingleNamedAttributeProvider<XPathValue> attrProvider;
        private final IndeterminateEvaluationException missingAttributeForUnknownReasonException;
        private final ImmutableXacmlStatus missingContextSelectorAttributeErrorStatus;
        private final String xpathEvalErrMsgSuffix;
        private final ImmutableXacmlStatus xpathEvalErrStatus;

        private AttributeSelectorExpressionWithContextSelector(final AttributeSelectorType attrSelectorElement, final XPathCompilerProxy xPathCompiler, final AttributeValueFactory<AV> attrFactory,
                                                               final SingleNamedAttributeProvider<XPathValue> attrProvider) throws IllegalArgumentException
        {
            super(attrSelectorElement, xPathCompiler, attrFactory);
            assert attributeSelectorId.getContextSelectorId().isPresent() && attrProvider != null;

            final String attributeCategory = attributeSelectorId.getCategory();
            final String contextSelectorId = attributeSelectorId.getContextSelectorId().get();
            this.contextSelectorFQN = Optional.of(AttributeFqns.newInstance(attributeCategory, Optional.empty(), contextSelectorId));
            this.attrProvider = attrProvider;
            this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException(this + " not found in context for unknown reason", xacmlMissingAttributeDetail, Optional.empty());
            this.missingContextSelectorAttributeErrorStatus = new ImmutableXacmlStatus(xacmlMissingAttributeDetail, Optional.empty(), Optional.of(this + ": No value found for attribute designated by Category=" + attributeCategory + " and ContextSelectorId=" + contextSelectorId));
            this.xpathEvalErrMsgSuffix = "' from ContextSelectorId='" + contextSelectorId + "' against Content of Attributes of Category=" + attributeCategory;
            this.xpathEvalErrStatus =  new ImmutableXacmlStatus(XacmlStatusCode.SYNTAX_ERROR.value(), Optional.of(this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + attributeCategory + xpathEvalErrMsgSuffix));
        }

        @Override
        public Optional<AttributeFqn> getContextSelectorFQN()
        {
            return this.contextSelectorFQN;
        }

        @Override
        protected ImmutableXacmlStatus getXpathEvalErrorStatus()
        {
            return xpathEvalErrStatus;
        }

        @Override
        protected XdmItem getFinalXPathEvaluationContextItem(final XdmNode contentElement, final EvaluationContext context, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
        {
            final Bag<XPathValue> bag = attrProvider.get(context, mdpContext);
            if (bag == null)
            {
                throw this.missingAttributeForUnknownReasonException;
            }

            if (bag.isEmpty())
            {
                throw new IndeterminateEvaluationException(missingContextSelectorAttributeErrorStatus, bag.getReasonWhyEmpty());
            }

            final String contextSelectorPath = bag.getSingleElement().getUnderlyingValue();
            final XdmItem contextNode;
            try
            {
                contextNode = xPathCompiler.evaluateSingle(contextSelectorPath, contentElement);
            } catch (final SaxonApiException e)
            {
                throw new IndeterminateEvaluationException(this + ": Error evaluating XPath='" + contextSelectorPath + xpathEvalErrMsgSuffix, XacmlStatusCode.SYNTAX_ERROR.value(), e);
            }

            if (contextNode == null)
            {
                throw new IndeterminateEvaluationException(this + ": No node returned by evaluation of XPath='" + contextSelectorPath + xpathEvalErrMsgSuffix, XacmlStatusCode.SYNTAX_ERROR.value());
            }

            return contextNode;
        }

    }

    /**
     * Creates instance from XACML AttributeSelector without ContextSelectorId
     *
     * @param attributeSelectorCategory XACML AttributeSelector's Category
     * @param attributeSelectorPath     XACML AttributeSelector's Path
     * @param xPathCompiler             XPath compiler used for compiling {@code attributeSelectorElement.getPath()} and XPath given by {@code attributeSelectorElement.getContextSelectorId()}.
     * @param attributeFactory          attribute factory to create the AttributeValue(s) from the XML node(s) resolved by XPath
     * @return instance of AttributeSelector expression
     * @throws java.lang.IllegalArgumentException if {@code attributeSelectorElement == null || xPathCompiler == null || attributeFactory == null}; or {@code attributeSelectorElement.getContextSelectorId() != null} but
     *                                            {@code attributeProvider == null}; or {@code attributeSelectorElement.getPath()} is not a valid XPath expression
     */
    public static <AV extends AttributeValue> AttributeSelectorExpression<AV> newInstance(final String attributeSelectorCategory, final String attributeSelectorPath, final boolean mustBePresent, final XPathCompilerProxy xPathCompiler, final AttributeValueFactory<AV> attributeFactory) throws IllegalArgumentException
    {
        Preconditions.checkArgument(attributeSelectorCategory != null && attributeSelectorPath != null && xPathCompiler != null && attributeFactory != null, "Invalid arguments");
        return new AttributeSelectorExpressionWithoutContextSelector<>(attributeSelectorCategory, attributeSelectorPath, mustBePresent, xPathCompiler, attributeFactory);
    }

    /**
     * Creates instance from XACML AttributeSelector with ContextSelectorId
     *
     * @param attributeSelectorElement         XACML AttributeSelector
     * @param xPathCompiler                    XPATH compiler used for compiling {@code attributeSelectorElement.getPath()} and XPath given by {@code attributeSelectorElement.getContextSelectorId()} if not null
     * @param contextSelectorAttributeProvider AttributeProvider for finding value of the attribute identified by ContextSelectorId in {@code attributeSelectorElement}; may be null if
     *                                         {@code attributeSelectorElement.getContextSelectorId() == null}
     * @param attributeFactory                 attribute factory to create the AttributeValue(s) from the XML node(s) resolved by XPath
     * @return instance of AttributeSelector expression
     * @throws java.lang.IllegalArgumentException if {@code attributeSelectorElement == null || xPathCompiler == null || attributeFactory == null}; or {@code attributeSelectorElement.getContextSelectorId() != null} but
     *                                            {@code attributeProvider == null}; or {@code attributeSelectorElement.getPath()} is not a valid XPath expression
     */
    public static <AV extends AttributeValue> AttributeSelectorExpression<AV> newInstance(final AttributeSelectorType attributeSelectorElement, final XPathCompilerProxy xPathCompiler, final AttributeValueFactory<AV> attributeFactory,
                                                                                          final SingleNamedAttributeProvider<XPathValue> contextSelectorAttributeProvider) throws IllegalArgumentException
    {
        Preconditions.checkArgument(attributeSelectorElement != null && xPathCompiler != null && attributeFactory != null && contextSelectorAttributeProvider != null, "Invalid arguments");
        final String contextSelectorId = attributeSelectorElement.getContextSelectorId();
        Preconditions.checkArgument(contextSelectorId != null, "AttributeSelector's contextSelectorId argument must not be null for this factory method");
        return new AttributeSelectorExpressionWithContextSelector<>(attributeSelectorElement, xPathCompiler, attributeFactory, contextSelectorAttributeProvider);
    }
}