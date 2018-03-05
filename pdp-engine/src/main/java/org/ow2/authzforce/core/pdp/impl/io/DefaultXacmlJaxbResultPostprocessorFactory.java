/**
 * Copyright 2012-2018 Thales Services SAS.
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

import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor.Factory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

/**
 *
 * Default factory creating instances of {@link BaseXacmlJaxbResultPostprocessor}
 *
 */
public final class DefaultXacmlJaxbResultPostprocessorFactory extends Factory
{
	/**
	 * Result postprocessor ID, as returned by {@link #getId()}
	 */
	public static final String ID = "urn:ow2:authzforce:feature:pdp:result-postproc:xacml-xml:default";

	/**
	 * No-arg constructor
	 */
	public DefaultXacmlJaxbResultPostprocessorFactory()
	{
		super(ID);
	}

	@Override
	public DecisionResultPostprocessor<IndividualXacmlJaxbRequest, Response> getInstance(final int clientRequestErrorVerbosityLevel)
	{
		return new BaseXacmlJaxbResultPostprocessor(clientRequestErrorVerbosityLevel);
	}
}