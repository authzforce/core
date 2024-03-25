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
package org.ow2.authzforce.core.pdp.impl.io;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.core.pdp.api.io.MultipleXacmlRequestPreprocHelper;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryXacmlAttributesParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * XACML/XML Request preprocessor implementing Multiple Decision Profile, section 2.3 (repeated attribute categories). Other schemes are not supported.
 *
 * @version $Id: $
 */
public final class MultiDecisionXacmlJaxbRequestPreprocessor extends BaseXacmlJaxbRequestPreprocessor
{
	private static final MultipleXacmlRequestPreprocHelper<IndividualXacmlJaxbRequest, Attributes, Attributes> MDP_PREPROC_HELPER = new MultipleXacmlRequestPreprocHelper<>(
			IndividualXacmlJaxbRequest::new)
	{

		@Override
		protected Attributes validate(final Attributes inputRawAttributeCategoryObject)
		{
			/*
			 * Same type as input/output, nothing to do.
			 */
			return inputRawAttributeCategoryObject;
		}

	};

	/**
	 *
	 * Factory for this type of request preprocessor that allows duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (complying with XACML 3.0 core
	 * spec, §7.3.3).
	 *
	 */
	public static final class LaxVariantFactory extends BaseXacmlJaxbRequestPreprocessor.Factory
	{
		/**
		 * Request preprocessor ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:multiple:repeated-attribute-categories-lax";

		/**
		 * Constructor
		 */
		public LaxVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
		        final boolean requireContentForXPath, final Set<String> extraPdpFeatures)
		{
			return new MultiDecisionXacmlJaxbRequestPreprocessor(datatypeFactoryRegistry, strictAttributeIssuerMatch, true, requireContentForXPath, extraPdpFeatures);
		}
	}

	/**
	 *
	 * Factory for this type of request preprocessor that does NOT allow duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (NOT complying with XACML
	 * 3.0 core spec, §7.3.3).
	 *
	 */
	public static final class StrictVariantFactory extends BaseXacmlJaxbRequestPreprocessor.Factory
	{
		/**
		 * Request preprocessor ID, returned by {@link #getId()}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:multiple:repeated-attribute-categories-strict";

		/**
		 * Constructor
		 */
		public StrictVariantFactory()
		{
			super(ID);
		}

		@Override
		public DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest> getInstance(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch,
		        final boolean requireContentForXPath, final Set<String> extraPdpFeatures)
		{
			return new MultiDecisionXacmlJaxbRequestPreprocessor(datatypeFactoryRegistry, strictAttributeIssuerMatch, false, requireContentForXPath, extraPdpFeatures);
		}
	}

	private MultiDecisionXacmlJaxbRequestPreprocessor(final AttributeValueFactoryRegistry datatypeFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
	        final boolean requireContentForXPath, final Set<String> extraPdpFeatures)
	{
		super(datatypeFactoryRegistry, strictAttributeIssuerMatch, allowAttributeDuplicates, requireContentForXPath, extraPdpFeatures);
	}

	/** {@inheritDoc} */
	@Override
	public List<IndividualXacmlJaxbRequest> process(final List<Attributes> attributesList, final SingleCategoryXacmlAttributesParser<Attributes> xacmlAttrsParser,
													final boolean isApplicablePolicyIdListReturned, final boolean combinedDecision, final Optional<XPathCompilerProxy> xPathCompiler, final Map<String, String> namespaceURIsByPrefix)
	        throws IndeterminateEvaluationException
	{
		return MDP_PREPROC_HELPER.process(attributesList, xacmlAttrsParser, isApplicablePolicyIdListReturned, xPathCompiler);
	}
}
