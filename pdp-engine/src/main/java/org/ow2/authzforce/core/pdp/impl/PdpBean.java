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
package org.ow2.authzforce.core.pdp.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PdpEngine;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SystemPropertyUtils;

/**
 * JavaBean for the PDP to be used/called as JNDI resource.
 *
 * In JEE application servers such as Glassfish, you could use class org.glassfish.resources.custom.factory.JavaBeanFactory for registering the custom JNDI resource. More info:
 * http://docs.oracle.com/cd/E26576_01/doc.312/e24930/jndi.htm#giywi
 *
 * For Tomcat, see http://tomcat.apache.org/tomcat-7.0-doc/jndi-resources-howto.html# Adding_Custom_Resource_Factories.
 *
 * @version $Id: $
 */
public final class PdpBean implements PdpEngine
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdpBean.class);

	private PdpEngine pdp;

	private String confLocation = null;

	private boolean initialized = false;

	private String extSchemaLocation = null;

	private String catalogLocation = null;

	/**
	 * Configuration file. Only the 'defaultPDP' configuration will be loaded, i.e. 'pdp' element with 'name' matching the 'defaultPDP' attribute of the root 'config' element
	 *
	 * @param filePath
	 *            configuration file path used as argument to {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to resolve the resource; any placeholder ${...} in the path
	 *            will be replaced with the corresponding system property value
	 * @throws java.lang.IllegalArgumentException
	 *             if there is an unresolvable placeholder in {@code filePath}
	 */
	public void setConfigFile(final String filePath) throws IllegalArgumentException
	{
		confLocation = SystemPropertyUtils.resolvePlaceholders(filePath);
		init();
	}

	/**
	 * Configuration schema file. Used only for validating XML configurations (enclosed with 'xml' tag) of PDP extension modules in PDP configuration file set with {@link #setConfigFile(String)}
	 *
	 * @param filePath
	 *            configuration file path used as argument to {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to resolve the resource; any placeholder ${...} in the path
	 *            will be replaced with the corresponding system property value
	 * @throws java.lang.IllegalArgumentException
	 *             if there is an unresolvable placeholder in {@code filePath}
	 */
	public void setSchemaFile(final String filePath) throws IllegalArgumentException
	{
		extSchemaLocation = SystemPropertyUtils.resolvePlaceholders(filePath);
		init();
	}

	/**
	 * Set XML catalog for resolving XML entities used in XML schema
	 *
	 * @param filePath
	 *            configuration file path used as argument to {@link org.springframework.core.io.DefaultResourceLoader#getResource(String)} to resolve the resource; any placeholder ${...} in the path
	 *            will be replaced with the corresponding system property value
	 * @throws java.lang.IllegalArgumentException
	 *             if there is an unresolvable placeholder in {@code filePath}
	 */
	public void setCatalogFile(final String filePath) throws IllegalArgumentException
	{
		catalogLocation = SystemPropertyUtils.resolvePlaceholders(filePath);
		init();
	}

	private boolean init()
	{
		if (!initialized && catalogLocation != null && extSchemaLocation != null && confLocation != null)
		{
			LOGGER.info("Loading PDP configuration from file {} with extension schema location '{}' and XML catalog location '{}'", confLocation, extSchemaLocation, catalogLocation);
			try
			{
				final PdpEngineConfiguration conf = PdpEngineConfiguration.getInstance(confLocation, catalogLocation, extSchemaLocation);
				pdp = new BasePdpEngine(conf);
			}
			catch (IOException | IllegalArgumentException e)
			{
				throw new RuntimeException("Error parsing PDP configuration from location: " + confLocation, e);
			}

			initialized = true;
		}

		return initialized;
	}

	@Override
	public DecisionRequestBuilder<?> newRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
	{
		return pdp.newRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes);
	}

	private void checkInit()
	{
		if (!initialized)
		{
			final String cause;
			if (confLocation == null)
			{
				cause = "Missing parameter: configuration file";
			}
			else if (extSchemaLocation == null)
			{
				cause = "Missing parameter: extension schema file";
			}
			else if (catalogLocation == null)
			{
				cause = "Missing parameter: XML catalog file";
			}
			else
			{
				cause = "Check previous errors.";
			}

			throw new RuntimeException("PDP not initialized: " + cause);
		}
	}

	/** {@inheritDoc} */
	@Override
	public DecisionResult evaluate(final DecisionRequest individualDecisionRequest)
	{
		checkInit();
		return pdp.evaluate(individualDecisionRequest);
	}

	@Override
	public <INDIVIDUAL_DECISION_REQUEST extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQUEST, ? extends DecisionResult>> evaluate(final List<INDIVIDUAL_DECISION_REQUEST> requests)
			throws IndeterminateEvaluationException
	{
		checkInit();
		return pdp.evaluate(requests);
	}

	@Override
	public Iterable<PrimaryPolicyMetadata> getApplicablePolicies()
	{
		return this.pdp.getApplicablePolicies();
	}

}
