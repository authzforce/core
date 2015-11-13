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
package org.ow2.authzforce.core;

import java.io.Serializable;
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
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Content;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.ow2.authzforce.core.CategorySpecificAttributes.MutableBag;
import org.ow2.authzforce.core.expression.AttributeGUID;
import org.ow2.authzforce.core.value.DatatypeFactory;
import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;
import org.ow2.authzforce.xacml.identifiers.XACMLCategory;
import org.ow2.authzforce.xacml.identifiers.XACMLResourceScope;

/**
 * XACML Request filter; applies some validation and processing of the input request prior to the policy evaluation. A typical request filter may support the
 * MultiRequests element, and more generally the Multiple Decision Profile by creating multiple Individual Decision Requests (EvaluationContext) from the
 * original XACML request, as defined in XACML Multiple Decision Profile specification, section 2; and then call the policy evaluation engine for each
 * Individual Decision Request. At the end, the results (one per Individual Decision Request) may be combined by a {@link DecisionResultFilter}.
 * 
 * <p>
 * This replaces and supersedes the former, now obsolete, ResourceFinder, which used to correspond to one mode of the Multiple Decision Profile for requesting
 * multiple decisions.
 * </p>
 * 
 */
public abstract class RequestFilter
{

	/**
	 * Factory of RequestFilters
	 * 
	 */
	public interface Factory extends PdpExtension
	{
		/**
		 * Create instance of RequestFilter
		 * 
		 * @param datatypeFactoryRegistry
		 *            attribute datatype factory for parsing XACML Request AttributeValues into Java types compatible with/optimized for the policy evaluation
		 *            engine
		 * 
		 * @param requireContentForXPath
		 *            true iff XPath evaluation against Attributes/Content element is required (e.g. AttributeSelector, XPath-based funtion...). A preprocessor
		 *            may skip Content parsing for XPath evaluation, if and only if this is false. (Be aware that a preprocessor may support the
		 *            MultipleDecision Profile or Hierarchical Profile and therefore require Content parsing for other purposes defined by these profiles.)
		 * @param attributesContentJaxbCtx
		 *            JAXBContext that was used to unmarshall Attributes/Content elements in the Request. This context is used to create a new instance of
		 *            marshaller to pass it as JAXBSource to {@code xmlProcesor} to convert to XDM data model for XPATH evaluation. May be null if
		 *            {@code requireContentForXPath} is false.
		 * @param xmlProcessor
		 *            XML processor for parsing Attributes/Content prior to XPATH evaluation (e.g. AttributeSelectors). May be null if
		 *            {@code requireContentForXPath} is false.
		 * @return instance of RequestFilter
		 */
		RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx,
				Processor xmlProcessor);
	}

	protected interface XACMLAttributesParser
	{

		/**
		 * Parses XACML Attributes element into internal Java type expected by/optimized for the policy evaluation engine
		 * 
		 * @param jaxbAttributes
		 *            Attributes element unmarshalled by JAXB. If the result of this method is not null, this parameter is changed by this method to the final
		 *            Attributes to be included in the final Result, i.e. all Attribute elements with IncludeInresult = false and the Content are removed after
		 *            this method returns (a non-null result).
		 * @param xPathCompiler
		 *            XPath compiler for compiling any XPath expressions in attribute values (e.g. xpathExpression datatype)
		 * @return Attributes parsing result; null if nothing to parse, i.e. no Attribute and (no Content or Content parsing skipped because xmlDocumentBuilder
		 *         == null);
		 * @throws IndeterminateEvaluationException
		 *             if any parsing error occurs
		 */
		CategorySpecificAttributes parse(Attributes jaxbAttributes, XPathCompiler xPathCompiler) throws IndeterminateEvaluationException;
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
			public CategorySpecificAttributes parse(Attributes jaxbAttributes, XPathCompiler xPathCompiler) throws IndeterminateEvaluationException
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
				 * Ignore jaxbAttrCategory.getId(), as it is primarily intended to be referenced in multiple requests when implementing MultiRequests of
				 * Multiple Decision Profile, not implemented here.
				 */

				/*
				 * Let's iterate over the attributes to convert the list to a map indexed by the attribute category/id/issuer for quicker access during request
				 * evaluation. There might be multiple occurrences of <Attribute> with same meta-data (id, etc.), so the map value type need to be
				 * expandable/appendable to merge new values when new occurrences are found, e.g. Collection.
				 */
				final Map<AttributeGUID, MutableBag<?>> attrMap = new HashMap<>();
				/*
				 * Attributes included in result initialized to all attributes in the <Attributes> jaxbAttributes. We use an iterator to be able to remove the
				 * current <Attribute> element from the list when isIncludeInResult=false so that we have the list of attributes to be included in the result
				 * right away at the end of the iteration: jaxbAttributes itself. So no need to create a new list.
				 */
				final Iterator<Attribute> categoryAttrsIterator = categoryAttrs.iterator();
				while (categoryAttrsIterator.hasNext())
				{
					final Attribute jaxbAttr = categoryAttrsIterator.next();
					final AttributeGUID attrGUID = new AttributeGUID(categoryName, jaxbAttr.getIssuer(), jaxbAttr.getAttributeId());

					// The XACML schema specifies there should be at least one AttributeValue
					final List<AttributeValueType> jaxbAttrValues = jaxbAttr.getAttributeValues();
					if (jaxbAttrValues.isEmpty())
					{
						throw new IndeterminateEvaluationException("Missing AttributeValue(s) for Attribute " + attrGUID + " (cf. XACML 3.0 schema)",
								StatusHelper.STATUS_SYNTAX_ERROR);
					}

					/**
					 * Determine the attribute bag datatype to make sure it is supported and all values are of the same datatype Indeed, XACML spec says for
					 * Attribute Bags (7.3.2): "There SHALL be no notion of a bag containing bags, or a bag containing values of differing types; i.e., a bag in
					 * XACML SHALL contain only values that are of the same data-type."
					 * <p>
					 * The bag datatypeURI/datatype class is obtained from first value.
					 */
					final AttributeValueType jaxbAttrVal0 = jaxbAttrValues.get(0);
					final String bagDatatypeURI = jaxbAttrVal0.getDataType();
					final DatatypeFactory<?> bagElementDatatypeFactory = datatypeFactoryRegistry.getExtension(bagDatatypeURI);
					if (bagElementDatatypeFactory == null)
					{
						throw new IndeterminateEvaluationException("Unsupported AttributeValue DataType in Attribute" + attrGUID + ": " + bagDatatypeURI,
								StatusHelper.STATUS_SYNTAX_ERROR);
					}

					/*
					 * Input AttributeValues have now been validated. Let's check any existing values for the same attrGUID (<Attribute> with same meta-data) in
					 * the map. As discussed on the xacml-dev mailing list (see https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html), the
					 * following excerpt from the XACML 3.0 core spec, §7.3.3, indicates that multiple occurrences of the same <Attribute> with same meta-data
					 * but different values should be considered equivalent to a single <Attribute> element with same meta-data and merged values (multi-valued
					 * Attribute). Moreover, the conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in
					 * a multi-value bag during evaluation of the AttributeDesignator.
					 * 
					 * Therefore, we choose to merge the attribute values here if this is a new occurrence of the same Attribute, i.e. attrMap.get(attrGUID) !=
					 * null. In this case, we can reuse the list already created for the previous occurrence to store the new values resulting from parsing.
					 */
					final MutableBag<?> previousVals = attrMap.get(attrGUID);
					final MutableBag<?> valsToUpdate;
					if (previousVals == null)
					{
						/*
						 * First occurrence of this attribute ID (attrGUID). Check whether this is not an unsupported resource-scope attribute. XACML Multiple
						 * Decision Profile, § 2.3.3: "... If such a <Attributes> element contains a 'scope' attribute having any value other than 'Immediate',
						 * then the Individual Request SHALL be further processed according to the processing model specified in Section 4." We do not support
						 * 'scope' other than 'Immediate' so throw an error if different.
						 */
						if (attrGUID.equals(RESOURCE_SCOPE_ATTRIBUTE_GUID))
						{
							final List<Serializable> jaxbContent = jaxbAttrVal0.getContent();
							if (!jaxbContent.isEmpty() && !jaxbContent.get(0).equals(XACMLResourceScope.IMMEDIATE.value()))
							{
								throw UNSUPPORTED_MULTIPLE_SCOPE_EXCEPTION;
							}
						}

						valsToUpdate = new MutableBag<>(bagElementDatatypeFactory);
						attrMap.put(attrGUID, valsToUpdate);
					} else
					{
						/*
						 * Collection of values already in the map for this Attribute id, reuse/update it directly
						 */
						valsToUpdate = previousVals;
					}

					/*
					 * Update valsToUpdate with new values resulting from parsing the new XACML AttributeValues
					 */
					valsToUpdate.add(jaxbAttrValues, extraContent, xPathCompiler);

					// Remove attribute from categoryAttrs, and therefore from jaxbAttrCategory, if
					// IncludeInResult = false
					if (!jaxbAttr.isIncludeInResult())
					{
						categoryAttrsIterator.remove();
					}
				}

				/*
				 * Now there are only <Attribute>s with IncludeInResult=true or no <Attribute> at all (if all with IncludeInResult=false) in jaxbAttrCategory.
				 * Remove <Content> to keep only the final <Attributes> to include in the Result.
				 */
				jaxbAttributes.setContent(null);
				final Attributes attrsToIncludeInResult;
				if (categoryAttrs.isEmpty())
				{
					/*
					 * Nothing left to include (no Attribute element in category or all with IncludeInResult = false, therefore all removed in the for loop
					 * above)
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
		 * XACML Attributes Parser that only parses the named attributes (Attribute elements), not the Content
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
		 * XACML Attributes Parser that not only parses the named attributes (Attribute elements), but also the Content, on the contrary to
		 * {@link NamedXACMLAttributesParser} which only parses the Attribute elements
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
					throw new IndeterminateEvaluationException("Error parsing Content of Attributes[@Category=" + categoryName + "] for XPath evaluation",
							StatusHelper.STATUS_SYNTAX_ERROR, e);
				}

			}
		}

		private final Processor xmlProc;
		// JAXBContext that was used to unmarshall Attributes/Content elements in the Request.
		private final JAXBContext attributesContentMarshallJaxbCtx;

		private XACMLAttributesWithContentParserFactory(DatatypeFactoryRegistry datatypeFactoryRegistry, JAXBContext attributesContentJaxbCtx,
				Processor xmlProcessor)
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

	protected static final AttributeGUID RESOURCE_SCOPE_ATTRIBUTE_GUID = new AttributeGUID(XACMLCategory.XACML_3_0_RESOURCE_CATEGORY_RESOURCE.value(), null,
			XACMLAttributeId.XACML_RESOURCE_SCOPE.value());

	protected static final IndeterminateEvaluationException UNSUPPORTED_MULTIPLE_SCOPE_EXCEPTION = new IndeterminateEvaluationException(
			"Unsupported resource scope. Expected scope: none or " + XACMLResourceScope.IMMEDIATE.value()
					+ ". (Profile 'urn:oasis:names:tc:xacml:3.0:profile:multiple:scope' not supported.)", StatusHelper.STATUS_SYNTAX_ERROR);

	/**
	 * Indeterminate exception to be thrown iff RequestDefaults element not supported by the request preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_REQUEST_DEFAULTS_EXCEPTION = new IndeterminateEvaluationException(
			"Unsupported feature: <RequestDefaults>", StatusHelper.STATUS_SYNTAX_ERROR);

	/**
	 * Indeterminate exception to be thrown iff MultiRequests element not supported by the request preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_MULTI_REQUESTS_EXCEPTION = new IndeterminateEvaluationException(
			"Unsupported feature: <MultiRequests>", StatusHelper.STATUS_SYNTAX_ERROR);

	private final XACMLAttributesParserFactory xacmlAttrsParserFactory;

	/**
	 * Creates instance of request filter.
	 * 
	 * @param datatypeFactoryRegistry
	 *            registry of factories for attribute datatypes
	 * @param requireContentForXPath
	 *            true iff Attributes/Content parsing (into XDM) for XPath evaluation is required
	 * @param attributesContentJaxbCtx
	 *            JAXBContext that was used to unmarshall Attributes/Content elements in the Request. This context is used to create a new instance of
	 *            marshaller to pass it as JAXBSource to {@code xmlProcesor} to convert to XDM data model for XPATH evaluation. May be null if
	 *            {@code requireContentForXPath} is false.
	 * 
	 * @param xmlProcessor
	 *            XML processor for parsing Attributes/Content elements into XDM for XPath evaluation. May be null if {@code requireContentForXPath} is false.
	 */
	protected RequestFilter(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean requireContentForXPath, JAXBContext attributesContentJaxbCtx,
			Processor xmlProcessor)
	{
		this.xacmlAttrsParserFactory = requireContentForXPath ? new XACMLAttributesWithContentParserFactory(datatypeFactoryRegistry, attributesContentJaxbCtx,
				xmlProcessor) : new NamedXACMLAttributesParserFactory(datatypeFactoryRegistry);
	}

	/**
	 * Get a XACML Attributes element Parser instance. Must be called once for each Request. E.g. if Content parsing is required as specified by
	 * {@code requireContentForXPath parameter} of {@link RequestFilter#RequestParser(DatatypeFactoryRegistry, boolean, Processor)}, this allows to instantiate
	 * a new XML DocumentBuilder for each Request because it is not thread-safe.
	 */
	protected XACMLAttributesParser getXACMLAttributesParserInstance()
	{
		return xacmlAttrsParserFactory.getInstance();
	}

	/**
	 * Filters (validates and/or transform) a Request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile
	 * 
	 * @param req
	 *            input Request
	 * 
	 * @return indidividual decision evaluation contexts (one per each Individiual Decision Request, as defined in Multiple Decision Profile, or a singleton
	 *         list if no multiple decision requested or supported by the filter)
	 *         <p>
	 *         Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this
	 */
	public abstract List<IndividualDecisionRequest> filter(Request req) throws IndeterminateEvaluationException;
}
