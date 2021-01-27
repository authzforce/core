/**
 * Copyright 2012-2021 THALES.
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
package org.ow2.authzforce.core.pdp.io.xacml.json;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.io.ImmutableNamedXacmlAttributeParsingResult;
import org.ow2.authzforce.core.pdp.api.io.NamedXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.NamedXacmlAttributeParsingResult;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryAttributes.NamedAttributeIteratorConverter;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryXacmlAttributesParser;
import org.ow2.authzforce.core.pdp.api.io.XacmlRequestAttributeParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;

/**
 * XACML/JSON (Profile) processing utilities
 * 
 */
public final class XacmlJsonParsingUtils
{
	/**
	 * XACML/JSON named Attribute parser
	 */
	public static final class NamedXacmlJsonAttributeParser extends NamedXacmlAttributeParser<JSONObject>
	{

		private static final IllegalArgumentException NULL_ATTRIBUTE_ID_ARGUMENT_EXCEPTION = new IllegalArgumentException("Invalid XACML Attribute: AttributeId property undefined (but required).");

		private static <AV extends AttributeValue> NamedXacmlAttributeParsingResult<AV> parseNamedAttribute(final AttributeFqn attName, final Iterable<Object> nonEmptyInputXacmlJsonAttValues,
		        final int numOfValues, final AttributeValueFactory<AV> attValFactory, final XPathCompiler xPathCompiler) throws UnsupportedOperationException, IllegalArgumentException
		{
			assert attName != null && nonEmptyInputXacmlJsonAttValues != null && numOfValues > 0 && attValFactory != null;

			final Collection<AV> attValues = new ArrayDeque<>(numOfValues);
			/*
			 * JSON value may be a JSONObject or primitive (Boolean, Number, String)
			 */
			for (final Object inputXacmlAttValue : nonEmptyInputXacmlJsonAttValues)
			{
				/*
				 * Warning: JSONObject does not implement Serializable
				 */
				if (!(inputXacmlAttValue instanceof Serializable))
				{
					/*
					 * TODO: support array of JSONObjects as attribute value
					 */
					throw new UnsupportedOperationException("Unsupported type of item in Value array of attribute '" + attName + "': " + inputXacmlAttValue.getClass().getSimpleName());
				}

				final AV resultValue = attValFactory.getInstance(Collections.singletonList((Serializable) inputXacmlAttValue), Collections.emptyMap(), xPathCompiler);
				attValues.add(resultValue);
			}

			return new ImmutableNamedXacmlAttributeParsingResult<>(attName, attValFactory.getDatatype(), ImmutableList.copyOf(attValues));
		}

		protected NamedXacmlJsonAttributeParser(final AttributeValueFactoryRegistry attributeValueFactoryRegistry) throws IllegalArgumentException
		{
			super(attributeValueFactoryRegistry);
		}

		@Override
		protected NamedXacmlAttributeParsingResult<?> parseNamedAttribute(final String attributeCategoryId, final JSONObject inputXacmlAttribute, final XPathCompiler xPathCompiler)
		        throws IllegalArgumentException
		{
			final String attrId = inputXacmlAttribute.optString("AttributeId", null);
			if (attrId == null)
			{
				throw NULL_ATTRIBUTE_ID_ARGUMENT_EXCEPTION;
			}

			final String attrIssuer = inputXacmlAttribute.optString("Issuer", null);
			final AttributeFqn attrName = AttributeFqns.newInstance(attributeCategoryId, Optional.ofNullable(attrIssuer), attrId);
			final String jsonDatatypeId = inputXacmlAttribute.optString("DataType", null);

			// The XACML schema specifies there should be at least one AttributeValue
			final Object attrValuesObj = inputXacmlAttribute.opt("Value");
			if (attrValuesObj == null)
			{
				throw new IllegalArgumentException("Undefined Value(s) for Attribute '" + attrName + "'");
			}

			final String actualDatatypeId;
			final Iterable<Object> jsonAttVals;
			final int numOfVals;
			if (attrValuesObj instanceof JSONArray)
			{
				if (jsonDatatypeId == null)
				{
					throw new IllegalArgumentException("Invalid Attribute '" + attrName + "': value is JSONArray but DataType undefined (cannot be inferred).");
				}

				actualDatatypeId = jsonDatatypeId;
				final JSONArray attValsJsonArray = (JSONArray) attrValuesObj;
				numOfVals = attValsJsonArray.length();
				if (numOfVals == 0)
				{
					throw new IllegalArgumentException("Invalid Attribute '" + attrName + "': no value (empty JSONArray).");
				}

				jsonAttVals = attValsJsonArray;
			}
			else
			{
				/*
				 * Not a JSON array (but JSONObject or primitive)
				 */
				if (jsonDatatypeId == null)
				{
					if (attrValuesObj instanceof Boolean)
					{
						actualDatatypeId = StandardDatatypes.BOOLEAN.getId();
					}
					/*
					 * Number subtypes possibly returned by JSONObject.opt(...) according to JSONObject#stringToValue(...)
					 */
					else if (attrValuesObj instanceof Integer || attrValuesObj instanceof Long)
					{
						actualDatatypeId = StandardDatatypes.INTEGER.getId();
					}
					else if (attrValuesObj instanceof Double)
					{
						actualDatatypeId = StandardDatatypes.DOUBLE.getId();
					}
					else
					{
						// default
						actualDatatypeId = StandardDatatypes.STRING.getId();
					}
				}
				else
				{
					actualDatatypeId = jsonDatatypeId;
				}

				numOfVals = 1;
				jsonAttVals = Collections.singleton(attrValuesObj);
			}

			/**
			 * Determine the attribute datatype to make sure it is supported and all values are of the same datatype. Indeed, XACML spec says for Attribute Bags (7.3.2): "There SHALL be no notion of a
			 * bag containing bags, or a bag containing values of differing types; i.e., a bag in XACML SHALL contain only values that are of the same data-type."
			 * <p>
			 * So we can obtain the datatypeURI/datatype class from the first value.
			 */
			final AttributeValueFactory<?> attValFactory = getAttributeValueFactory(actualDatatypeId, attrName);
			return parseNamedAttribute(attrName, jsonAttVals, numOfVals, attValFactory, xPathCompiler);
		}

	}

	/**
	 * Base XACML/JSON Attributes parser
	 * 
	 * @param <BAG>
	 *            type of bag resulting from parsing XACML AttributeValues
	 */
	private static abstract class BaseXacmlJsonAttributesParser<BAG extends Iterable<? extends AttributeValue>> implements SingleCategoryXacmlAttributesParser<JSONObject>
	{
		private final XacmlRequestAttributeParser<JSONObject, BAG> xacmlReqAttributeParser;
		private final NamedAttributeIteratorConverter<BAG> namedAttrIterConverter;

		private BaseXacmlJsonAttributesParser(final XacmlRequestAttributeParser<JSONObject, BAG> xacmlRequestAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
		{
			assert xacmlRequestAttributeParser != null && namedAttributeIteratorConverter != null;

			this.xacmlReqAttributeParser = xacmlRequestAttributeParser;
			this.namedAttrIterConverter = namedAttributeIteratorConverter;
		}

		/**
		 * Parse Content in Category object into XPath data model for XPath evaluation
		 * 
		 * @param categoryId
		 *            CategoryId
		 * @param categoryContent
		 *            the Category/Content string (see XACML JSON Profile §4.2.3)
		 * 
		 * @return null if Content parsing not supported or disabled
		 * @throws IndeterminateEvaluationException
		 *             if any Content parsing error occurs
		 */
		protected abstract XdmNode parseContent(String categoryId, String categoryContent) throws IndeterminateEvaluationException;

		@Override
		public SingleCategoryAttributes<?, JSONObject> parseAttributes(final JSONObject requestAttributeCategory, final XPathCompiler xPathCompiler) throws IndeterminateEvaluationException
		{
			assert requestAttributeCategory != null;

			final String categoryId = requestAttributeCategory.getString("CategoryId");
			/*
			 * Ignore requestAttributeCategory.optString("Id"), as it is primarily intended to be referenced in multiple requests when implementing MultiRequests of Multiple Decision Profile, not
			 * implemented here.
			 */
			final JSONArray categoryAttrs = requestAttributeCategory.optJSONArray("Attribute");
			final String categoryContent = requestAttributeCategory.optString("Content", null);
			final XdmNode extraContent = parseContent(categoryId, categoryContent);

			/*
			 * Let's iterate over the attributes to convert the list to a map indexed by the attribute category/id/issuer for quicker access during request evaluation. There might be multiple
			 * occurrences of <Attribute> with same meta-data (id, etc.), so the map value type need to be expandable/appendable to merge new values when new occurrences are found, e.g. Collection.
			 */
			final Map<AttributeFqn, BAG> namedAttrMap;
			final JSONObject categoryObjectToIncludeInResult;
			if (categoryAttrs == null || categoryAttrs.length() == 0)
			{
				if (extraContent == null)
				{
					/*
					 * Skipping this <Attributes> because no <Attribute> and no extra Content parsed
					 */
					return null;
				}

				namedAttrMap = Collections.emptyMap();
				categoryObjectToIncludeInResult = null;
			}
			else
			{
				namedAttrMap = HashCollections.newUpdatableMap();
				/*
				 * Assume categoryAttrs as immutable. For performance enhancement, we could reuse/modify it directly to create the list of Attributes included in Result (IncludeInResult=true).
				 * However, this JSON object coming from the JSON parser after parsing the XACML/JSON request may be immutable (depending on the JSON parser). So we must create a new one, to make sure
				 * it works with any JSON parser.
				 */
				final List<JSONObject> returnedAttributes = new ArrayList<>(categoryAttrs.length());
				for (final Object attrObj : categoryAttrs)
				{
					/*
					 * JSONArray item (Object) may be Boolean, JSONArray, JSONObject, Number, String
					 */
					if (!(attrObj instanceof JSONObject))
					{
						throw new IndeterminateEvaluationException("Invalid XACML Attribute: invalid JSON element type (" + attrObj.getClass().getSimpleName() + "). Expected: JSON object.",
						        XacmlStatusCode.SYNTAX_ERROR.value());
					}

					final JSONObject attrJsonObj = (JSONObject) attrObj;

					/*
					 * Update the attribute map with new values resulting from parsing the new XACML AttributeValues
					 */
					try
					{
						xacmlReqAttributeParser.parseNamedAttribute(categoryId, attrJsonObj, xPathCompiler, namedAttrMap);
					}
					catch (final IllegalArgumentException e)
					{
						throw new IndeterminateEvaluationException("Invalid Attributes/Attribute element", XacmlStatusCode.SYNTAX_ERROR.value(), e);
					}

					// Check IncludeInResult
					if (attrJsonObj.optBoolean("IncludeInResult", false))
					{
						/*
						 * Remove IncludeInResult as it is optional in JSON and we don't need in the Result
						 */
						attrJsonObj.remove("IncludeInResult");
						returnedAttributes.add(attrJsonObj);
					}

				}

				/*
				 * If there are Attribute objects to include, create Category objects with these - without Content - to be included in the Result.
				 */

				if (returnedAttributes.isEmpty())
				{
					categoryObjectToIncludeInResult = null;
				}
				else
				{
					categoryObjectToIncludeInResult = new JSONObject();
					categoryObjectToIncludeInResult.put("CategoryId", categoryId);
					/*
					 * WARNING: optString("Id") returns empty string '' if there is no such key!
					 */
					final String jsonObjectId = requestAttributeCategory.optString("Id", null);
					if (jsonObjectId != null)
					{
						categoryObjectToIncludeInResult.put("Id", jsonObjectId);
					}
					categoryObjectToIncludeInResult.put("Attribute", returnedAttributes);
				}
			}

			return new SingleCategoryAttributes<>(categoryId, namedAttrMap.entrySet(), namedAttrIterConverter, categoryObjectToIncludeInResult, extraContent);
		}
	}

	private static final class ContentSkippingXacmlJsonAttributesParser<BAG extends Iterable<? extends AttributeValue>> extends BaseXacmlJsonAttributesParser<BAG>
	{
		private ContentSkippingXacmlJsonAttributesParser(final XacmlRequestAttributeParser<JSONObject, BAG> xacmlJsonAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
		{
			super(xacmlJsonAttributeParser, namedAttributeIteratorConverter);
		}

		@Override
		protected XdmNode parseContent(final String categoryName, final String categoryContent)
		{
			/*
			 * Content parsing not supported
			 */
			return null;
		}
	}

	private static final IllegalArgumentException NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined namedAttributeIteratorConverter");
	private static final IllegalArgumentException NULL_XACML_JSON_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION = new IllegalArgumentException(
	        "Undefined XACML/JSON Attribute parser (null xacmlJsonAttributeParser)");

	/**
	 * 
	 * Factory for XACML/JSON Attribute parser that only parses the named attributes (Attribute elements), not the Content
	 * 
	 * @param <BAG>
	 *            resulting from parsing XACML AttributeValues
	 */
	public static final class ContentSkippingXacmlJsonAttributesParserFactory<BAG extends Iterable<? extends AttributeValue>> implements SingleCategoryXacmlAttributesParser.Factory<JSONObject>
	{
		private final SingleCategoryXacmlAttributesParser<JSONObject> instance;

		/**
		 * Creates instance
		 * 
		 * @param xacmlJsonAttributeParser
		 *            parser used to parse each XACML/JSON Attribute
		 * @param namedAttributeIteratorConverter
		 *            converts iterator over attributes with values produced by {@code xacmlJsonAttributeParser}, into constant-valued/immutable attribute iterator
		 * @throws IllegalArgumentException error
		 *             {@code if(xacmlJsonAttributeParser == null || namedAttributeIteratorConverter == null)}
		 */
		public ContentSkippingXacmlJsonAttributesParserFactory(final XacmlRequestAttributeParser<JSONObject, BAG> xacmlJsonAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter) throws IllegalArgumentException
		{
			if (xacmlJsonAttributeParser == null)
			{
				throw NULL_XACML_JSON_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION;
			}

			if (namedAttributeIteratorConverter == null)
			{
				throw NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION;
			}

			instance = new ContentSkippingXacmlJsonAttributesParser<>(xacmlJsonAttributeParser, namedAttributeIteratorConverter);
		}

		@Override
		public SingleCategoryXacmlAttributesParser<JSONObject> getInstance()
		{
			return instance;
		}

	}

	private static final class FullXacmlJsonAttributesParser<BAG extends Iterable<? extends AttributeValue>> extends BaseXacmlJsonAttributesParser<BAG>
	{
		// XML document builder for parsing XML Content to XPath data model for XPath evaluation
		// private final DocumentBuilder xmlDocBuilder;

		private FullXacmlJsonAttributesParser(final XacmlRequestAttributeParser<JSONObject, BAG> xacmlJsonAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter/*
		                                                                                   * , final DocumentBuilder xmlDocBuilder
		                                                                                   */)
		{
			super(xacmlJsonAttributeParser, namedAttributeIteratorConverter);
			// assert xmlDocBuilder != null;
			// this.xmlDocBuilder = xmlDocBuilder;
		}

		@Override
		public XdmNode parseContent(final String categoryId, final String categoryContent) throws IndeterminateEvaluationException
		{
			if (categoryContent == null)
			{
				// nothing to parse
				return null;
			}

			/*
			 * TODO: XACML JSON Profile - Content (optional) is not supported yet
			 */
			throw new IndeterminateEvaluationException("XACML JSON Profile - Content elements are not supported", XacmlStatusCode.SYNTAX_ERROR.value());

			// XACML spec, 7.3.7: the document node must be the single child element of Content.
			// Element childElt = null;
			// for (final Serializable node : categoryContent.getContent())
			// {
			// if (node instanceof Element)
			// {
			// childElt = (Element) node;
			// break;
			// }
			// }
			//
			// if (childElt == null)
			// {
			// throw new IndeterminateEvaluationException("Invalid Content of Attributes[@Category=" + categoryId + "] for XPath evaluation: no child element", StatusHelper.STATUS_SYNTAX_ERROR);
			// }
			//
			// try
			// {
			// return xmlDocBuilder.wrap(childElt);
			// }
			// catch (final IllegalArgumentException e)
			// {
			// throw new IndeterminateEvaluationException("Error parsing Content of Attributes[@Category=" + categoryId + "] for XPath evaluation", StatusHelper.STATUS_SYNTAX_ERROR, e);
			// }

		}

	}

	/**
	 * 
	 * Factory for XACML/JSON Attribute Parser that parses the named attributes (Attribute elements), and the free-form Content
	 * 
	 * @param <BAG>
	 *            resulting from parsing XACML AttributeValues
	 */
	public static final class FullXacmlJsonAttributesParserFactory<BAG extends Iterable<? extends AttributeValue>> implements SingleCategoryXacmlAttributesParser.Factory<JSONObject>
	{
		// private static final IllegalArgumentException NULL_XML_PROCESSOR_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XML processor (null xmlProcessor)");
		private final XacmlRequestAttributeParser<JSONObject, BAG> xacmlJsonAttributeParser;
		private final NamedAttributeIteratorConverter<BAG> namedAttrIterConverter;

		// private final Processor xmlProc;

		/**
		 * Creates instance
		 * 
		 * @param xacmlJsonAttributeParser
		 *            parser used to parse each XACML/JSON Attribute
		 * @param namedAttributeIteratorConverter
		 *            converts iterator over attributes with values produced by {@code xacmlJsonAttributeParser}, into constant-valued/immutable attribute iterator
		 * 
		 * @throws IllegalArgumentException error
		 *             {@code if(xacmlJsonAttributeParser == null || namedAttributeIteratorConverter == null || xmlProcessor == null)}
		 */
		public FullXacmlJsonAttributesParserFactory(final XacmlRequestAttributeParser<JSONObject, BAG> xacmlJsonAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter/* , final Processor xmlProcessor */)
		{
			if (xacmlJsonAttributeParser == null)
			{
				throw NULL_XACML_JSON_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION;
			}

			if (namedAttributeIteratorConverter == null)
			{
				throw NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION;
			}

			// if (xmlProcessor == null)
			// {
			// throw NULL_XML_PROCESSOR_ARGUMENT_EXCEPTION;
			// }

			this.xacmlJsonAttributeParser = xacmlJsonAttributeParser;
			this.namedAttrIterConverter = namedAttributeIteratorConverter;
			// this.xmlProc = xmlProcessor;
		}

		@Override
		public SingleCategoryXacmlAttributesParser<JSONObject> getInstance()
		{
			// create instance of inner class (has access to this.xmlProc)
			return new FullXacmlJsonAttributesParser<>(xacmlJsonAttributeParser, namedAttrIterConverter/* , xmlProc.newDocumentBuilder() */);
		}
	}

	private XacmlJsonParsingUtils()
	{
	}

}
