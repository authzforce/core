/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.io.xacml.json.test;

import java.io.IOException;

import org.json.JSONObject;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.ow2.authzforce.core.pdp.io.xacml.json.BaseXacmlJsonResultPostprocessor;
import org.ow2.authzforce.core.pdp.io.xacml.json.IndividualXacmlJsonRequest;
import org.ow2.authzforce.core.pdp.io.xacml.json.SingleDecisionXacmlJsonRequestPreprocessor;

/**
 * Utilities to create PDP Engine Adapters supporting JSON Request/Response according to JSON Profile of XACML
 */
public final class PdpEngineXacmlJsonAdapters
{

	/**
	 * Creates a new PDP engine supporting XACML/XML (JAXB) input/output according to XACML 3.0 core specification.
	 * 
	 * @param configuration
	 *            PDP engine configuration
	 * 
	 * @return new instance of {@link PdpEngineInoutAdapter} supporting standard XACML 3.0 XML input/output
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code configuration.getXacmlExpressionFactory() == null || configuration.getRootPolicyProvider() == null}
	 * @throws java.io.IOException
	 *             error closing {@code configuration.getRootPolicyProvider()} when static resolution is to be used
	 */
	public static PdpEngineInoutAdapter<JSONObject, JSONObject> newXacmlJsonInoutAdapter(final PdpEngineConfiguration configuration) throws IllegalArgumentException, IOException
	{
		final DecisionResultPostprocessor<IndividualXacmlJsonRequest, JSONObject> defaultResultPostproc = new BaseXacmlJsonResultPostprocessor(configuration.getClientRequestErrorVerbosityLevel());
		final DecisionRequestPreprocessor<JSONObject, IndividualXacmlJsonRequest> defaultReqPreproc = SingleDecisionXacmlJsonRequestPreprocessor.LaxVariantFactory.INSTANCE.getInstance(
				configuration.getAttributeValueFactoryRegistry(), configuration.isStrictAttributeIssuerMatchEnabled(), configuration.isXpathEnabled(), XmlUtils.SAXON_PROCESSOR,
				defaultResultPostproc.getFeatures());

		return PdpEngineAdapters.newInoutAdapter(JSONObject.class, JSONObject.class, configuration, defaultReqPreproc, defaultResultPostproc);
	}

}
