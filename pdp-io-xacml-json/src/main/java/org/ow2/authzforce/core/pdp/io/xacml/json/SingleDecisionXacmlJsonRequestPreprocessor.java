/*
 * Copyright 2012-2024 THALES.
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

import com.google.common.collect.ImmutableList;
import net.sf.saxon.s9api.XdmNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryAttributes;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryXacmlAttributesParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import java.util.*;
import java.util.Map.Entry;

/**
 * Default XACML/JSON - according to XACML JSON Profile - Request preprocessor for Individual Decision Requests only (no support of Multiple Decision Profile in particular)
 *
 * @version $Id: $
 */
public final class SingleDecisionXacmlJsonRequestPreprocessor extends BaseXacmlJsonRequestPreprocessor
{
	private static final IndeterminateEvaluationException INVALID_REQUEST_CATEGORY_ARRAY_ELEMENT_TYPE_EXCEPTION = new IndeterminateEvaluationException(
	        "Invalid Request/Category array: the type of one of the items is invalid (not JSON object as expected)", XacmlStatusCode.SYNTAX_ERROR.value());
	private static final DecisionRequestFactory<ImmutableDecisionRequest> DEFAULT_REQUEST_FACTORY = ImmutableDecisionRequest::getInstance;

	/**
	 *
	 * Factory for this type of request preprocessor that allows duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (complying with XACML 3.0 core
	 * spec, §7.3.3) but using JSON-Profile-defined format.
	 *
	 */
	public static final class LaxVariantFactory extends BaseXacmlJsonRequestPreprocessor.Factory
	{
		/**
		 * Request preprocessor ID, as returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:default-lax";

		/**
		 * Constructor
		 */
		public LaxVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
		        final boolean requireContentForXPath, final Set<String> extraPdpFeatures)
		{
			return new SingleDecisionXacmlJsonRequestPreprocessor(datatypeFactoryRegistry, DEFAULT_REQUEST_FACTORY, strictAttributeIssuerMatch, true, requireContentForXPath/* , xmlProcessor */,
			        extraPdpFeatures);
		}

		/**
		 * Singleton instance of this factory
		 * 
		 */
		public static final DecisionRequestPreprocessor.Factory<JSONObject, IndividualXacmlJsonRequest> INSTANCE = new LaxVariantFactory();
	}

	/**
	 *
	 * Factory for this type of request preprocessor that does NOT allow duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (NOT complying fully with
	 * XACML 3.0 core spec, §7.3.3) but using JSON-Profile-defined format.
	 *
	 */
	public static final class StrictVariantFactory extends BaseXacmlJsonRequestPreprocessor.Factory
	{
		/**
		 * Request preprocessor ID, as returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:default-strict";

		/**
		 * Constructor
		 */
		public StrictVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
		        final boolean requireContentForXPath, final Set<String> extraPdpFeatures)
		{
			return new SingleDecisionXacmlJsonRequestPreprocessor(datatypeFactoryRegistry, DEFAULT_REQUEST_FACTORY, strictAttributeIssuerMatch, false, requireContentForXPath/* , xmlProcessor */,
			        extraPdpFeatures);
		}
	}

	private final DecisionRequestFactory<ImmutableDecisionRequest> reqFactory;

	/**
	 * Creates instance of default request preprocessor
	 * 
	 * @param datatypeFactoryRegistry
	 *            attribute datatype registry
	 * @param requestFactory
	 *            decision request factory
	 * @param strictAttributeIssuerMatch
	 *            true iff strict attribute Issuer match must be enforced (in particular request attributes with empty Issuer only match corresponding AttributeDesignators with empty Issuer)
	 * @param allowAttributeDuplicates
	 *            true iff duplicate Attribute (with same metadata) elements in Request (for multi-valued attributes) must be allowed
	 * @param requireContentForXPath
	 *            true iff Content elements must be parsed, else ignored
	 * @param extraPdpFeatures
	 *            extra - not mandatory per XACML 3.0 core specification - features supported by the PDP engine. This preprocessor checks whether it is supported by the PDP before processing the
	 *            request further.
	 */
	public SingleDecisionXacmlJsonRequestPreprocessor(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final DecisionRequestFactory<ImmutableDecisionRequest> requestFactory,
	        final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates, final boolean requireContentForXPath/* , final Processor xmlProcessor */,
	        final Set<String> extraPdpFeatures)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, /* xmlProcessor, */extraPdpFeatures);
		assert requestFactory != null;
		reqFactory = requestFactory;
	}

	@Override
	public List<IndividualXacmlJsonRequest> process(final JSONArray jsonArrayOfRequestAttributeCategoryObjects, final SingleCategoryXacmlAttributesParser<JSONObject> xacmlAttrsParser,
													final boolean isApplicablePolicyIdListReturned, final boolean combinedDecision, final Optional<XPathCompilerProxy> xPathCompiler, final Map<String, String> namespaceURIsByPrefix)
	        throws IndeterminateEvaluationException
	{
		final Map<AttributeFqn, AttributeBag<?>> namedAttributes = HashCollections.newUpdatableMap(jsonArrayOfRequestAttributeCategoryObjects.length());
		/*
		 * TODO: Content object not supported yet (optional in XACML)
		 */
		final Map<String, XdmNode> extraContentsByCategory = Collections.emptyMap() /* HashCollections.newUpdatableMap(requestAttributeCategoryObjects.length()) */;

		/*
		 * requestAttributeCategoryObjectsIncludedInResult.size() <= jsonArrayOfRequestAttributeCategoryObjects.size()
		 */
		final List<JSONObject> requestAttributeCategoryObjectsIncludedInResult = new ArrayList<>(jsonArrayOfRequestAttributeCategoryObjects.length());

		for (final Object requestAttributeCategoryObject : jsonArrayOfRequestAttributeCategoryObjects)
		{
			if (!(requestAttributeCategoryObject instanceof JSONObject))
			{
				throw INVALID_REQUEST_CATEGORY_ARRAY_ELEMENT_TYPE_EXCEPTION;
			}

			final JSONObject requestAttCatJsonObj = (JSONObject) requestAttributeCategoryObject;
			final SingleCategoryAttributes<?, JSONObject> categorySpecificAttributes = xacmlAttrsParser.parseAttributes(requestAttCatJsonObj, xPathCompiler);
			if (categorySpecificAttributes == null)
			{
				// skip this empty Attributes
				continue;
			}

			/*
			 * TODO: Content object not supported yet (optional in XACML)
			 */
			// final XdmNode newContentNode = categorySpecificAttributes.getExtraContent();
			// if (newContentNode != null)
			// {
			// final XdmNode duplicate = extraContentsByCategory.putIfAbsent(categoryName, newContentNode);
			// /*
			// * No support for Multiple Decision Profile -> no support for repeated categories as specified in Multiple Decision Profile. So we must check duplicate attribute categories.
			// */
			// if (duplicate != null)
			// {
			// throw new IndeterminateEvaluationException("Unsupported repetition of Attributes[@Category='" + categoryName
			// + "'] (feature 'urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories' is not supported)", StatusHelper.STATUS_SYNTAX_ERROR);
			// }
			// }

			/*
			 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee that attribute values remain constant during the evaluation of the request, as
			 * mandated by the XACML spec, section 7.3.5: <p> <i>
			 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
			 * </i></p>
			 */
			for (final Entry<AttributeFqn, AttributeBag<?>> attrEntry : categorySpecificAttributes)
			{
				namedAttributes.put(attrEntry.getKey(), attrEntry.getValue());
			}

			final JSONObject catSpecificAttrsToIncludeInResult = categorySpecificAttributes.getAttributesToIncludeInResult();
			if (catSpecificAttrsToIncludeInResult != null)
			{
				requestAttributeCategoryObjectsIncludedInResult.add(catSpecificAttrsToIncludeInResult);
			}
		}

		final ImmutableDecisionRequest pdpEngineReq = reqFactory.getInstance(namedAttributes, extraContentsByCategory, isApplicablePolicyIdListReturned);
		return Collections.singletonList(new IndividualXacmlJsonRequest(pdpEngineReq, ImmutableList.copyOf(requestAttributeCategoryObjectsIncludedInResult)));
	}
}
