package com.thalesgroup.authzforce.core;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Content;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.attr.AttributeGUID;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.CategorySpecificAttributes;
import com.thalesgroup.authzforce.core.attr.DatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.eval.BagResult;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLCategory;
import com.thalesgroup.authzforce.xacml.schema.XACMLResourceScope;

/**
 * XACML Request filter; applies some validation and processing of the input request prior to the
 * policy evaluation. A typical request filter may support the MultiRequests element, and more
 * generally the Multiple Decision Profile by creating multiple Individual Decision Requests
 * (EvaluationContext) from the original XACML request, as defined in XACML Multiple Decision
 * Profile specification, section 2; and then call the policy evaluation engine for each Individual
 * Decision Request. At the end, the results (one per Individual Decision Request) may be combined
 * by a {@link DecisionResultFilter}.
 * 
 * <p>
 * This replaces and supersedes the former, now obsolete, ResourceFinder, which used to correspond
 * to one mode of the Multiple Decision Profile for requesting multiple decisions.
 * </p>
 * 
 */
public abstract class RequestFilter
{

	/**
	 * RefPolicyFinderModuleFactory of RequestFilter
	 * 
	 */
	public static interface Factory extends PdpExtension
	{
		/**
		 * RefPolicyFinderModuleFactory for instantiating a RequestPreProcessor
		 * 
		 * @param datatypeFactoryRegistry
		 *            attribute datatype factory for parsing XACML Request AttributeValues into Java
		 *            types compatible with/optimized for the policy evaluation engine
		 * 
		 * @param requireContentForXPath
		 *            true iff XPath evaluation against Attributes/Content element is required (e.g.
		 *            AttributeSelector, XPath-based funtion...). A preprocessor may skip Content
		 *            parsing for XPath evaluation, if and only if this is false. (Be aware that a
		 *            preprocessor may support the MultipleDecision Profile or Hierarchical Profile
		 *            and therefore require Content parsing for other purposes defined by these
		 *            profiles.)
		 * @param attributesContentJaxbCtx
		 *            JAXBContext that was used to unmarshall Attributes/Content elements in the
		 *            Request. This context is used to create a new instance of marshaller to pass
		 *            it as JAXBSource to {@code xmlProcesor} to convert to XDM data model for XPATH
		 *            evaluation. May be null if {@code requireContentForXPath} is false.
		 * @param xmlProcessor
		 *            XML processor for parsing Attributes/Content prior to XPATH evaluation (e.g.
		 *            AttributeSelectors). May be null if {@code requireContentForXPath} is false.
		 * @return instance of RequestFilter
		 */
		RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor);
	}

	protected static interface XACMLAttributesParser
	{

		/**
		 * Parses XACML Attributes element into internal Java type expected by/optimized for the
		 * policy evaluation engine
		 * 
		 * @param jaxbAttributes
		 *            Attributes element unmarshalled by JAXB. If the result of this method is not
		 *            null, this parameter is changed by this method to the final Attributes to be
		 *            included in the final Result, i.e. all Attribute elements with IncludeInresult
		 *            = false and the Content are removed after this method returns (a non-null
		 *            result).
		 * @return Attributes parsing result; null if nothing to parse, i.e. no Attribute and (no
		 *         Content or Content parsing skipped because xmlDocumentBuilder == null);
		 * @throws IndeterminateEvaluationException
		 *             if any parsing error occurs
		 */
		CategorySpecificAttributes parse(Attributes jaxbAttributes) throws IndeterminateEvaluationException;
	}

	private static abstract class XACMLAttributesParserFactory
	{
		private final DatatypeFactoryRegistry datatypeFactoryRegistry;

		protected XACMLAttributesParserFactory(DatatypeFactoryRegistry datatypeFactoryRegistry)
		{
			this.datatypeFactoryRegistry = datatypeFactoryRegistry;
		}

		protected abstract class BaseXACMLAttributesParser implements XACMLAttributesParser
		{
			/**
			 * Parse Attributes/Content to XPath data model for XPath evaluation
			 * 
			 * @param categoryName
			 * 
			 * @return null if Content parsing not supported or disabled
			 * @throws IndeterminateEvaluationException
			 *             if any Content parsing error occurs
			 */
			protected abstract XdmNode parseContent(String categoryName, Content jaxbContent) throws IndeterminateEvaluationException;

			@Override
			public CategorySpecificAttributes parse(Attributes jaxbAttributes) throws IndeterminateEvaluationException
			{
				final String categoryName = jaxbAttributes.getCategory();
				final List<Attribute> categoryAttrs = jaxbAttributes.getAttributes();
				final XdmNode extraContent = parseContent(categoryName, jaxbAttributes.getContent());
				if (categoryAttrs.isEmpty() && extraContent == null)
				{
					/*
					 * Skipping this <Attributes> because no <Attribute> and no extra Content parsed
					 */
					return null;
				}

				/*
				 * Ignore jaxbAttrCategory.getId(), as it is primarily intended to be referenced in
				 * multiple requests when implementing MultiRequests of Multiple Decision Profile,
				 * not implemented here.
				 */
				final Map<AttributeGUID, BagResult<? extends AttributeValue>> attrMap = new HashMap<>();

				/*
				 * Let's iterate over the attributes to convert the list to a map indexed by the
				 * attribute category/id/issuer for quicker access during request evaluation. We use
				 * an iterator to be able to remove the current element from the list when
				 * isIncludeInResult=false so that we have the list of attributes to be included in
				 * the result right away at the end of the iteration, without having to create a new
				 * list.
				 */
				// attributes included in result initialized to all attributes in the <Attributes>
				final Iterator<Attribute> categoryAttrsIterator = categoryAttrs.iterator();
				while (categoryAttrsIterator.hasNext())
				{
					final Attribute jaxbAttr = categoryAttrsIterator.next();
					final AttributeGUID attrGUID = new AttributeGUID(categoryName, jaxbAttr.getIssuer(), jaxbAttr.getAttributeId());

					// The XACML schema specifies there should be at least one AttributeValue
					final List<AttributeValueType> jaxbAttrValues = jaxbAttr.getAttributeValues();
					if (jaxbAttrValues.isEmpty())
					{
						throw new IndeterminateEvaluationException("Missing AttributeValue(s) for Attribute " + attrGUID + " (cf. XACML 3.0 schema)", Status.STATUS_SYNTAX_ERROR);
					}

					/**
					 * Determine the attribute bag datatype to make sure it is supported and all
					 * values are of the same datatype Indeed, XACML spec says for Attribute Bags
					 * (7.3.2): "There SHALL be no notion of a bag containing bags, or a bag
					 * containing values of differing types; i.e., a bag in XACML SHALL contain only
					 * values that are of the same data-type."
					 * <p>
					 * The bag datatypeURI/datatype class is obtained from first value.
					 */
					final String bagDatatypeURI = jaxbAttrValues.get(0).getDataType();
					final AttributeValue.Factory<? extends AttributeValue> bagDatatypeFactory = datatypeFactoryRegistry.getExtension(bagDatatypeURI);
					if (bagDatatypeFactory == null)
					{
						throw new IndeterminateEvaluationException("Unsupported AttributeValue DataType in Attribute" + attrGUID + ": " + bagDatatypeURI, Status.STATUS_SYNTAX_ERROR);
					}

					final BagResult<?> attributeBag = parseJaxbAttributeValues(attrGUID, jaxbAttrValues, bagDatatypeFactory.getInstanceClass(), bagDatatypeFactory);

					/*
					 * XACML Multiple Decision Profile, § 2.3.3: "... If such a <Attributes> element
					 * contains a 'scope' attribute having any value other than 'Immediate', then
					 * the Individual Request SHALL be further processed according to the processing
					 * model specified in Section 4." We do not support 'scope' other than
					 * 'Immediate' so throw an error if different.
					 */
					if (attrGUID.equals(RESOURCE_SCOPE_ATTRIBUTE_GUID) && !attributeBag.value().equals(XACMLResourceScope.IMMEDIATE.value()))
					{
						throw UNSUPPORTED_MULTIPLE_SCOPE_EXCEPTION;
					}

					attrMap.put(attrGUID, attributeBag);

					// Remove attribute from categoryAttrs, and therefore from jaxbAttrCategory, if
					// IncludeInResult = false
					if (!jaxbAttr.isIncludeInResult())
					{
						categoryAttrsIterator.remove();
					}
				}

				/*
				 * Now there are only <Attribute>s with IncludeInResult=true or no <Attribute> at
				 * all (if all with IncludeInResult=false) in jaxbAttrCategory. Remove <Content> to
				 * keep only the final <Attributes> to include in the Result.
				 */
				jaxbAttributes.setContent(null);
				final Attributes attrsToIncludeInResult;
				if (categoryAttrs.isEmpty())
				{
					/*
					 * Nothing left to include (no Attribute element in category or all with
					 * IncludeInResult = false, therefore all removed in the for loop above)
					 */
					attrsToIncludeInResult = null;
				} else
				{
					attrsToIncludeInResult = jaxbAttributes;
				}

				return new CategorySpecificAttributes(attrMap, attrsToIncludeInResult, extraContent);
			}
		}

		protected abstract XACMLAttributesParser getInstance();
	}

	private static class NamedXACMLAttributesParserFactory extends XACMLAttributesParserFactory
	{
		/**
		 * 
		 * XACML Attributes Parser that only parses the named attributes (Attribute elements), not
		 * the Content
		 * 
		 */
		private class NamedXACMLAttributesParser extends BaseXACMLAttributesParser
		{
			@Override
			protected XdmNode parseContent(String categoryName, Content jaxbContent) throws IndeterminateEvaluationException
			{
				// Content parsing not supported
				return null;
			}
		}

		private final NamedXACMLAttributesParser singletonInstance;

		private NamedXACMLAttributesParserFactory(DatatypeFactoryRegistry datatypeFactoryRegistry)
		{
			super(datatypeFactoryRegistry);
			singletonInstance = new NamedXACMLAttributesParser();
		}

		@Override
		public final XACMLAttributesParser getInstance()
		{
			return singletonInstance;
		}

	}

	private static class XACMLAttributesWithContentParserFactory extends XACMLAttributesParserFactory
	{
		/**
		 * XACML Attributes Parser that not only parses the named attributes (Attribute elements),
		 * but also the Content, on the contrary to {@link NamedXACMLAttributesParser} which only
		 * parses the Attribute elements
		 */
		private class XACMLAttributesWithContentParser extends BaseXACMLAttributesParser
		{
			// XML document builder for parsing Content to XPath data model for XPath evaluation
			private final DocumentBuilder xmlDocBuilder;

			private XACMLAttributesWithContentParser(DocumentBuilder xmlDocumentBuilder)
			{
				assert xmlDocumentBuilder != null;
				xmlDocBuilder = xmlDocumentBuilder;
			}

			@Override
			public final XdmNode parseContent(String categoryName, Content jaxbContent) throws IndeterminateEvaluationException
			{
				if (jaxbContent == null)
				{
					// nothing to parse
					return null;
				}
				try
				{
					final JAXBSource jaxbSrc = new JAXBSource(attributesContentMarshallJaxbCtx, jaxbContent);
					return xmlDocBuilder.build(jaxbSrc);
				} catch (JAXBException | SaxonApiException e)
				{
					throw new IndeterminateEvaluationException("Error parsing Content of Attributes[@Category=" + categoryName + "] for XPath evaluation", Status.STATUS_SYNTAX_ERROR, e);
				}

			}
		}

		private final Processor xmlProc;
		// JAXBContext that was used to unmarshall Attributes/Content elements in the Request.
		private final JAXBContext attributesContentMarshallJaxbCtx;

		private XACMLAttributesWithContentParserFactory(DatatypeFactoryRegistry datatypeFactoryRegistry, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor)
		{
			super(datatypeFactoryRegistry);
			assert attributesContentJaxbCtx != null;
			assert xmlProcessor != null;
			this.attributesContentMarshallJaxbCtx = attributesContentJaxbCtx;
			this.xmlProc = xmlProcessor;
		}

		@Override
		public final XACMLAttributesParser getInstance()
		{
			return new XACMLAttributesWithContentParser(xmlProc.newDocumentBuilder());
		}
	}

	protected static AttributeGUID RESOURCE_SCOPE_ATTRIBUTE_GUID = new AttributeGUID(XACMLCategory.XACML_3_0_RESOURCE_CATEGORY_RESOURCE.value(), null, XACMLAttributeId.XACML_RESOURCE_SCOPE.value());

	protected static final IndeterminateEvaluationException UNSUPPORTED_MULTIPLE_SCOPE_EXCEPTION = new IndeterminateEvaluationException("Unsupported resource scope. Expected scope: none or " + XACMLResourceScope.IMMEDIATE.value()
			+ ". (Profile 'urn:oasis:names:tc:xacml:3.0:profile:multiple:scope' not supported.)", Status.STATUS_SYNTAX_ERROR);

	/**
	 * Indeterminate exception to be thrown iff RequestDefaults element not supported by the request
	 * preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_REQUEST_DEFAULTS_EXCEPTION = new IndeterminateEvaluationException("Unsupported feature: <RequestDefaults>", Status.STATUS_SYNTAX_ERROR);

	/**
	 * Indeterminate exception to be thrown iff MultiRequests element not supported by the request
	 * preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_MULTI_REQUESTS_EXCEPTION = new IndeterminateEvaluationException("Unsupported feature: <MultiRequests>", Status.STATUS_SYNTAX_ERROR);

	/**
	 * Parses XACML Request AttributeValues
	 * 
	 * @param attributeGUID
	 *            attribute's global ID
	 * @param jaxbAttributeValues
	 *            AttributeValues from XACML Request
	 * @param expectedDatatypeClass
	 *            internal Java type compatible with/optimized for the policy evaluation engine, and
	 *            into which all values in {@code jaxbAttributeValues} must be parsed/converted
	 * @param datatypeFactory
	 *            factory for creating instances of {@code expectedDatatype} from values in
	 *            {@code jaxbAttributeValues}
	 * @return bag of attribute values ready for evaluation
	 * @throws IndeterminateEvaluationException
	 *             if any value in {@code jaxbAttributeValues} is not valid for the dataype
	 *             specified by {@code expectedDatatype}
	 */
	private static <T extends AttributeValue> BagResult<T> parseJaxbAttributeValues(AttributeGUID attributeGUID, List<AttributeValueType> jaxbAttributeValues, Class<T> expectedDatatypeClass, AttributeValue.Factory<? extends AttributeValue> datatypeFactory) throws IndeterminateEvaluationException
	{
		final DatatypeDef datatype = datatypeFactory.getDatatype();

		// Parse attribute values to Java type compatible with evaluation engine
		final T[] evaluationReadyValues = (T[]) Array.newInstance(expectedDatatypeClass, jaxbAttributeValues.size());
		int valIndex = 0;
		for (final AttributeValueType jaxbAttrVal : jaxbAttributeValues)
		{
			/*
			 * XACML spec says for Attribute Bags (7.3.2): "There SHALL be no notion of a bag
			 * containing bags, or a bag containing values of differing types; i.e., a bag in XACML
			 * SHALL contain only values that are of the same data-type." So we check that all
			 * values have same datatype.
			 */
			if (!jaxbAttrVal.getDataType().equals(datatype))
			{
				throw new IndeterminateEvaluationException("Invalid Attribute: AttributeValues of different DataTypes ('" + datatype + "' and '" + jaxbAttrVal.getDataType() + "') in Attribute" + attributeGUID, Status.STATUS_SYNTAX_ERROR);
			}

			try
			{
				evaluationReadyValues[valIndex] = expectedDatatypeClass.cast(datatypeFactory.getInstance(jaxbAttrVal));
			} catch (IllegalArgumentException | ClassCastException e)
			{
				throw new IndeterminateEvaluationException("Invalid AttributeValue #" + valIndex + " in Attribute" + attributeGUID + " for datatype " + datatype, Status.STATUS_SYNTAX_ERROR, e);
			}

			valIndex++;
		}

		return new BagResult<>(evaluationReadyValues, expectedDatatypeClass, datatype);
	}

	private final XACMLAttributesParserFactory xacmlAttrsParserFactory;

	/**
	 * Creates instance of request filter.
	 * 
	 * @param datatypeFactoryRegistry
	 *            registry of factories for attribute datatypes
	 * @param requireContentForXPath
	 *            true iff Attributes/Content parsing (into XDM) for XPath evaluation is required
	 * @param attributesContentJaxbCtx
	 *            JAXBContext that was used to unmarshall Attributes/Content elements in the
	 *            Request. This context is used to create a new instance of marshaller to pass it as
	 *            JAXBSource to {@code xmlProcesor} to convert to XDM data model for XPATH
	 *            evaluation. May be null if {@code requireContentForXPath} is false.
	 * 
	 * @param xmlProcessor
	 *            XML processor for parsing Attributes/Content elements into XDM for XPath
	 *            evaluation. May be null if {@code requireContentForXPath} is false.
	 */
	protected RequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx, Processor xmlProcessor)
	{
		this.xacmlAttrsParserFactory = requireContentForXPath ? new XACMLAttributesWithContentParserFactory(datatypeFactoryRegistry, attributesContentJaxbCtx, xmlProcessor) : new NamedXACMLAttributesParserFactory(datatypeFactoryRegistry);
	}

	/**
	 * Get a XACML Attributes element Parser instance. Must be called once for each Request. E.g. if
	 * Content parsing is required as specified by {@code requireContentForXPath parameter} of
	 * {@link RequestFilter#RequestParser(DatatypeFactoryRegistry, boolean, Processor)}, this allows
	 * to instantiate a new XML DocumentBuilder for each Request because it is not thread-safe.
	 */
	protected XACMLAttributesParser getXACMLAttributesParserInstance()
	{
		return xacmlAttrsParserFactory.getInstance();
	}

	/**
	 * Filters (validates and/or transform) a Request, may result in multiple individual decision
	 * requests, e.g. if implementing the Multiple Decision Profile
	 * 
	 * @param req
	 *            input Request
	 * 
	 * @return indidividual decision evaluation contexts (one per each Individiual Decision Request,
	 *         as defined in Multiple Decision Profile, or a singleton list if no multiple decision
	 *         requested or supported by the filter)
	 *         <p>
	 *         TODO: why return a List here and not an array for example?
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this
	 */
	public abstract List<IndividualDecisionRequest> filter(Request req) throws IndeterminateEvaluationException;
}
