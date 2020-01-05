/**
 * Copyright 2012-2020 THALES.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.s9api.XPathCompiler;

import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.MutableAttributeBag;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.io.IssuedToNonIssuedCopyingLaxXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.NamedXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.NonIssuedLikeIssuedLaxXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.NonIssuedLikeIssuedStrictXacmlAttributeParser;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryXacmlAttributesParser;
import org.ow2.authzforce.core.pdp.api.io.XacmlRequestAttributeParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.io.xacml.json.XacmlJsonParsingUtils.ContentSkippingXacmlJsonAttributesParserFactory;
import org.ow2.authzforce.core.pdp.io.xacml.json.XacmlJsonParsingUtils.FullXacmlJsonAttributesParserFactory;
import org.ow2.authzforce.core.pdp.io.xacml.json.XacmlJsonParsingUtils.NamedXacmlJsonAttributeParser;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.ow2.authzforce.xacml.json.model.XacmlJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenient base class for {@link DecisionRequestPreprocessor} implementations supporting XACML/JSON (XACML-JSON-Profile-standard-compliant) input
 * 
 */
public abstract class BaseXacmlJsonRequestPreprocessor implements DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest>
{
	private static final IndeterminateEvaluationException MISSING_REQUEST_OBJECT_EXCEPTION = new IndeterminateEvaluationException("Missing Request object", XacmlStatusCode.SYNTAX_ERROR.value());

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseXacmlJsonRequestPreprocessor.class);

	private static final IllegalArgumentException NULL_REQUEST_ARGUMENT_EXCEPTION = new IllegalArgumentException("Null request arg");
	private static final UnsupportedOperationException UNSUPPORTED_MODE_EXCEPTION = new UnsupportedOperationException(
			"Unsupported BaseXacmlJaxbRequestPreprocessor mode: allowAttributeDuplicates == false && strictAttributeIssuerMatch == false");

	/**
	 * Indeterminate exception to be thrown iff CombinedDecision element is not supported
	 */
	private static final IndeterminateEvaluationException UNSUPPORTED_COMBINED_DECISION_EXCEPTION = new IndeterminateEvaluationException("Unsupported CombinedDecision value in Request: 'true'",
			XacmlStatusCode.SYNTAX_ERROR.value());

	/**
	 * Indeterminate exception to be thrown iff RequestDefaults element not supported by the request preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_REQUEST_DEFAULTS_EXCEPTION = new IndeterminateEvaluationException("Unsupported element in Request: <RequestDefaults>",
			XacmlStatusCode.SYNTAX_ERROR.value());

	/**
	 * Indeterminate exception to be thrown iff MultiRequests element not supported by the request preprocessor
	 */
	protected static final IndeterminateEvaluationException UNSUPPORTED_MULTI_REQUESTS_EXCEPTION = new IndeterminateEvaluationException("Unsupported element in Request: <MultiRequests>",
			XacmlStatusCode.SYNTAX_ERROR.value());

	private final SingleCategoryXacmlAttributesParser.Factory<JSONObject> xacmlAttrsParserFactory;
	private final boolean isCombinedDecisionSupported;

	/**
	 * Creates instance of request pre-processor.
	 * 
	 * @param attributeValueFactoryRegistry
	 *            registry of datatype-specific attribute value factories (parsers)
	 * @param strictAttributeIssuerMatch
	 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case that
	 *            the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full compliance with
	 *            the XACML 3.0 Attribute Evaluation: "If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by
	 *            AttributeId and DataType attributes alone."
	 * @param allowAttributeDuplicates
	 *            true iff the pre-processor should allow defining multi-valued attributes by repeating the same XACML Attribute (same AttributeId) within a XACML Attributes element (same Category).
	 *            Indeed, not allowing this is not fully compliant with the XACML spec according to a discussion on the xacml-dev mailing list (see
	 *            {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), referring to the XACML 3.0 core spec, §7.3.3, that indicates that multiple occurrences of the
	 *            same &lt;Attribute&gt; with same meta-data but different values should be considered equivalent to a single &lt;Attribute&gt; element with same meta-data and merged values
	 *            (multi-valued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in a multi-value bag
	 *            during evaluation of the &lt;AttributeDesignator&gt;.
	 *            <p>
	 *            Setting this parameter to {@code false} is not fully compliant, but provides better performance, especially if you know the Requests to be well-formed, i.e. all AttributeValues of a
	 *            given Attribute are grouped together in the same &lt;Attribute&gt; element. Combined with {@code strictAttributeIssuerMatch == true}, this is the most efficient alternative (although
	 *            not fully compliant).
	 * @param requireContentForXPath
	 *            true iff Attributes/Content parsing (into XDM) for XPath evaluation is required
	 * 
	 * @param extraPdpFeatures
	 *            extra - non-mandatory per XACML 3.0 core specification - features supported by PDP engine. Any feature requested by any request is checked against this before processing the request
	 *            further. If some feature is not supported, an Indeterminate Result is returned.
	 * @throws UnsupportedOperationException
	 *             if {@code strictAttributeIssuerMatch == false && allowAttributeDuplicates == false} which is not supported
	 */
	protected BaseXacmlJsonRequestPreprocessor(final AttributeValueFactoryRegistry attributeValueFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
			final boolean requireContentForXPath, /* final Processor xmlProcessor, */final Set<String> extraPdpFeatures) throws UnsupportedOperationException
	{
		final NamedXacmlAttributeParser<JSONObject> namedXacmlAttParser = new NamedXacmlJsonAttributeParser(attributeValueFactoryRegistry);
		if (allowAttributeDuplicates)
		{
			final XacmlRequestAttributeParser<JSONObject, MutableAttributeBag<?>> xacmlAttributeParser = strictAttributeIssuerMatch ? new NonIssuedLikeIssuedLaxXacmlAttributeParser<>(
					namedXacmlAttParser) : new IssuedToNonIssuedCopyingLaxXacmlAttributeParser<>(namedXacmlAttParser);
			this.xacmlAttrsParserFactory = requireContentForXPath ? new FullXacmlJsonAttributesParserFactory<>(xacmlAttributeParser,
					SingleCategoryAttributes.MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER/* , xmlProcessor */) : new ContentSkippingXacmlJsonAttributesParserFactory<>(xacmlAttributeParser,
					SingleCategoryAttributes.MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER);
		}
		else // allowAttributeDuplicates == false
		if (strictAttributeIssuerMatch)
		{
			final XacmlRequestAttributeParser<JSONObject, AttributeBag<?>> xacmlAttributeParser = new NonIssuedLikeIssuedStrictXacmlAttributeParser<>(namedXacmlAttParser);
			this.xacmlAttrsParserFactory = requireContentForXPath ? new FullXacmlJsonAttributesParserFactory<>(xacmlAttributeParser, SingleCategoryAttributes.IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER/*
																																																	 * ,
																																																	 * xmlProcessor
																																																	 */)
					: new ContentSkippingXacmlJsonAttributesParserFactory<>(xacmlAttributeParser, SingleCategoryAttributes.IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER);
		}
		else
		{
			/*
			 * allowAttributeDuplicates == false && strictAttributeIssuerMatch == false is not supported, because it would require using mutable bags for "Issuer-less" attributes (updated for each
			 * possible Attribute with same meta-data except a defined Issuer), whereas the goal of 'allowAttributeDuplicates == false' is to use immutable Bags in the first place, i.e. to avoid going
			 * through mutable bags. A solution would consist to create two collections of attributes, one with immutable bags, and the other with mutable ones for Issuer-less attributes. However, we
			 * consider it is not worth providing an implementation for this natively, so far. Can always been a custom RequestPreprocessor provided as an extension.
			 */
			throw UNSUPPORTED_MODE_EXCEPTION;
		}

		this.isCombinedDecisionSupported = extraPdpFeatures.contains(DecisionResultPostprocessor.Features.XACML_MULTIPLE_DECISION_PROFILE_COMBINED_DECISION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor#getInputRequestType()
	 */
	@Override
	public final Class<JSONObject> getInputRequestType()
	{
		return JSONObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor#getOutputRequestType()
	 */
	@Override
	public final Class<IndividualXacmlJsonRequest> getOutputRequestType()
	{
		return IndividualXacmlJsonRequest.class;
	}

	/**
	 * Pre-processes (validates and/or transforms) a Request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile or Hierarchical Resource profile
	 * 
	 * @param jsonArrayOfRequestAttributeCategoryObjects
	 *            array of XACML JSON Category objects, null if none
	 * @param xacmlAttrsParser
	 *            XACML Attributes element Parser instance, used to parse each Attributes in {@code attributesList}.
	 * @param isApplicablePolicyIdListReturned
	 *            XACML Request's property {@code returnPolicyIdList}.
	 * @param combinedDecision
	 *            XACML Request's property {@code isCombinedDecision}
	 * @param xPathCompiler
	 *            xpathExpression compiler, corresponding to the XACML RequestDefaults element, or null if no RequestDefaults element.
	 * 
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation
	 * 
	 * @return individual decision requests, as defined in Multiple Decision Profile, e.g. a singleton list if no multiple decision requested or supported by the pre-processor
	 *         <p>
	 *         Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this pre-processor
	 */
	public abstract List<IndividualXacmlJsonRequest> process(JSONArray jsonArrayOfRequestAttributeCategoryObjects, SingleCategoryXacmlAttributesParser<JSONObject> xacmlAttrsParser,
			boolean isApplicablePolicyIdListReturned, boolean combinedDecision, XPathCompiler xPathCompiler, Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException;

	@Override
	public final List<IndividualXacmlJsonRequest> process(final JSONObject request, final Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
	{
		if (request == null)
		{
			throw NULL_REQUEST_ARGUMENT_EXCEPTION;
		}

		try
		{
			XacmlJsonUtils.REQUEST_SCHEMA.validate(request);
		}
		catch (final ValidationException e)
		{
			LOGGER.debug(e.toJSON().toString(4));
			throw new IndeterminateEvaluationException("Invalid Request", XacmlStatusCode.SYNTAX_ERROR.value(), e);
		}

		final JSONObject requestJsonObj = request.optJSONObject("Request");
		if (requestJsonObj == null)
		{
			throw MISSING_REQUEST_OBJECT_EXCEPTION;
		}

		/*
		 * No support for MultiRequests (§2.4 of Multiple Decision Profile).
		 */
		if (requestJsonObj.has("MultiRequests"))
		{
			/*
			 * According to 7.19.1 Unsupported functionality, return Indeterminate with syntax-error code for unsupported element
			 */
			throw UNSUPPORTED_MULTI_REQUESTS_EXCEPTION;
		}

		/*
		 * No support for CombinedDecision = true if result processor does not support it. (The use of the CombinedDecision attribute is specified in Multiple Decision Profile.)
		 */
		final boolean combinedDecisionRequested;
		if (requestJsonObj.optBoolean("CombinedDecision", false))
		{
			if (!this.isCombinedDecisionSupported)
			{
				/*
				 * According to XACML core spec, 5.42, "If the PDP does not implement the relevant functionality in [Multiple Decision Profile], then the PDP must return an Indeterminate with a status
				 * code of urn:oasis:names:tc:xacml:1.0:status:processing-error if it receives a request with this attribute set to "true".
				 */
				throw UNSUPPORTED_COMBINED_DECISION_EXCEPTION;
			}

			combinedDecisionRequested = true;
		}
		else
		{
			combinedDecisionRequested = false;
		}

		final boolean returnPolicyIdList = requestJsonObj.optBoolean("ReturnPolicyIdList", false);
		final XPathCompiler xPathCompiler = requestJsonObj.has("XPathVersion") ? XmlUtils.newXPathCompiler(requestJsonObj.getString("XPathVersion"), namespaceURIsByPrefix) : null;
		final SingleCategoryXacmlAttributesParser<JSONObject> xacmlAttrsParser = xacmlAttrsParserFactory.getInstance();
		return process(requestJsonObj.optJSONArray("Category"), xacmlAttrsParser, returnPolicyIdList, combinedDecisionRequested, xPathCompiler, namespaceURIsByPrefix);
	}

	/**
	 * Convenient base class for {@link org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor.Factory} implementations supporting XACML/JSON (XACML-JSON-Profile-standard-compliant) input
	 * 
	 */
	public static abstract class Factory implements DecisionRequestPreprocessor.Factory<JSONObject, IndividualXacmlJsonRequest>
	{
		private final String id;

		protected Factory(final String id)
		{
			this.id = id;
		}

		@Override
		public final String getId()
		{
			return id;
		}

		@Override
		public final Class<JSONObject> getInputRequestType()
		{
			return JSONObject.class;
		}

		@Override
		public final Class<IndividualXacmlJsonRequest> getOutputRequestType()
		{
			return IndividualXacmlJsonRequest.class;
		}
	}
}
