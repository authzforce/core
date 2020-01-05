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

import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.io.MultipleXacmlRequestPreprocHelper;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryXacmlAttributesParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;

/**
 * XACML/JSON - according to XACML JSON Profile - Request preprocessor implementing Multiple Decision Profile, section 2.3 (repeated attribute categories). Other schemes are not supported.
 *
 * @version $Id: $
 */
public final class MultipleDecisionXacmlJsonRequestPreprocessor extends BaseXacmlJsonRequestPreprocessor
{

	private static final IndeterminateEvaluationException INVALID_REQUEST_CATEGORY_ARRAY_ELEMENT_TYPE_EXCEPTION = new IndeterminateEvaluationException(
	        "Invalid Request/Category array: the type of one of the items is invalid (not JSON object as expected)", XacmlStatusCode.SYNTAX_ERROR.value());

	private static final MultipleXacmlRequestPreprocHelper<IndividualXacmlJsonRequest, Object, JSONObject> MDP_PREPROC_HELPER = new MultipleXacmlRequestPreprocHelper<IndividualXacmlJsonRequest, Object, JSONObject>(
	        (pdpEngineIndividualRequest, inputAttributeCategory) -> new IndividualXacmlJsonRequest(pdpEngineIndividualRequest, inputAttributeCategory))
	{

		@Override
		protected JSONObject validate(final Object inputRawAttributeCategoryObject) throws IndeterminateEvaluationException
		{
			if (!(inputRawAttributeCategoryObject instanceof JSONObject))
			{
				throw INVALID_REQUEST_CATEGORY_ARRAY_ELEMENT_TYPE_EXCEPTION;
			}

			return (JSONObject) inputRawAttributeCategoryObject;
		}

	};

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
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:multiple:repeated-attribute-categories-lax";

		/**
		 * Constructor
		 */
		public LaxVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
		        final boolean requireContentForXPath, final Processor xmlProcessor, final Set<String> extraPdpFeatures)
		{
			return new MultipleDecisionXacmlJsonRequestPreprocessor(datatypeFactoryRegistry, strictAttributeIssuerMatch, true, requireContentForXPath/* , xmlProcessor */, extraPdpFeatures);
		}

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
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:multiple:repeated-attribute-categories-strict";

		/**
		 * Constructor
		 */
		public StrictVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
		        final boolean requireContentForXPath, final Processor xmlProcessor, final Set<String> extraPdpFeatures)
		{
			return new MultipleDecisionXacmlJsonRequestPreprocessor(datatypeFactoryRegistry, strictAttributeIssuerMatch, false, requireContentForXPath/* , xmlProcessor */, extraPdpFeatures);
		}
	}

	/**
	 * Creates instance of default request preprocessor
	 * 
	 * @param datatypeFactoryRegistry
	 *            attribute datatype registry
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
	public MultipleDecisionXacmlJsonRequestPreprocessor(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
	        final boolean requireContentForXPath/* , final Processor xmlProcessor */, final Set<String> extraPdpFeatures)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, /* xmlProcessor, */extraPdpFeatures);
	}

	@Override
	public List<IndividualXacmlJsonRequest> process(final JSONArray jsonArrayOfRequestAttributeCategoryObjects, final SingleCategoryXacmlAttributesParser<JSONObject> xacmlAttrsParser,
	        final boolean isApplicablePolicyIdListReturned, final boolean combinedDecision, final XPathCompiler xPathCompiler, final Map<String, String> namespaceURIsByPrefix)
	        throws IndeterminateEvaluationException
	{
		return MDP_PREPROC_HELPER.process(jsonArrayOfRequestAttributeCategoryObjects, xacmlAttrsParser, isApplicablePolicyIdListReturned, combinedDecision, xPathCompiler, namespaceURIsByPrefix);
	}
}
