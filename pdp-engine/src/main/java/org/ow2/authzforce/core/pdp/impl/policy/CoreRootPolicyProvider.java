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
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticRootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.springframework.util.ResourceUtils;

/**
 * This is a simple {@link StaticRootPolicyProvider} implementation that supports static retrieval of the root policy. Its constructor accepts a location that represent a Spring-compatible resource
 * URL, and it is resolved to the actual policy at initialization time. Beyond this, there is no modifying or re-loading of the policy.
 */
public class CoreRootPolicyProvider implements StaticRootPolicyProvider
{
	private static final IllegalArgumentException ILLEGAL_XACML_PARSER_ARG_EXCEPTION = new IllegalArgumentException("Undefined XACML parser factory");
	private static final IllegalArgumentException ILLEGAL_ROOT_POLICY_URL_ARG_EXCEPTION = new IllegalArgumentException("Undefined root policy URL");

	/**
	 * Provider factory
	 * 
	 */
	public static class Factory extends RootPolicyProvider.Factory<org.ow2.authzforce.core.xmlns.pdp.StaticRootPolicyProvider>
	{

		@Override
		public Class<org.ow2.authzforce.core.xmlns.pdp.StaticRootPolicyProvider> getJaxbClass()
		{
			return org.ow2.authzforce.core.xmlns.pdp.StaticRootPolicyProvider.class;
		}

		@Override
		public RootPolicyProvider getInstance(final org.ow2.authzforce.core.xmlns.pdp.StaticRootPolicyProvider jaxbConf, final XmlnsFilteringParserFactory xacmlParserFactory,
				final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final Optional<CloseableRefPolicyProvider> refPolicyProvider,
				final EnvironmentProperties environmentProperties)
		{
			final String policyLocation = environmentProperties.replacePlaceholders(jaxbConf.getPolicyLocation());
			final URL rootPolicyURL;
			try
			{
				// try to load the policy location as a Spring resource
				rootPolicyURL = ResourceUtils.getURL(policyLocation);
			}
			catch (final FileNotFoundException ioe)
			{
				throw new IllegalArgumentException("No root policy (as Spring resource) found at the following URL: " + jaxbConf.getPolicyLocation(), ioe);
			}

			final StaticRefPolicyProvider staticRefPolicyProvider;
			if (refPolicyProvider.isPresent())
			{

				if (!(refPolicyProvider.get() instanceof StaticRefPolicyProvider))
				{
					throw new IllegalArgumentException("RefPolicyProvider arg '" + refPolicyProvider + "'  is not compatible with " + CoreRootPolicyProvider.class + ". Expected: instance of "
							+ StaticRefPolicyProvider.class + ". Make sure the PDP extension of type " + CloseableRefPolicyProvider.Factory.class
							+ " corresponding to the refPolicyProvider in PDP configuration can create instances of " + StaticRefPolicyProvider.class);
				}

				staticRefPolicyProvider = (StaticRefPolicyProvider) refPolicyProvider.get();
			}
			else
			{
				staticRefPolicyProvider = null;
			}

			return CoreRootPolicyProvider.getInstance(rootPolicyURL, xacmlParserFactory, expressionFactory, combiningAlgRegistry, Optional.ofNullable(staticRefPolicyProvider));
		}
	}

	private final StaticTopLevelPolicyElementEvaluator rootPolicy;

	/**
	 * Creates a <code>CoreRootPolicyProvider</code> with the root Policy already resolved once and for all
	 * 
	 * @param jaxbPolicy
	 *            root Policy (JAXB) to be parsed
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML Policy (XML) document, to be used for namespace-aware XPath evaluation
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 */
	public CoreRootPolicyProvider(final Policy jaxbPolicy, final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry)
	{
		try
		{
			rootPolicy = PolicyEvaluators.getInstance(jaxbPolicy, null, namespacePrefixesByURI, expressionFactory, combiningAlgRegistry);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid Policy: " + jaxbPolicy.getPolicyId(), e);
		}
	}

	/**
	 * Creates a <code>CoreRootPolicyProvider</code> with the root PolicySet already resolved once and for all
	 * 
	 * @param jaxbPolicySet
	 *            root PolicySet (JAXB) to be parsed
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML PolicySet (XML) document, to be used for namespace-aware XPath evaluation
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param refPolicyProvider
	 *            (optional) Policy-by-reference Provider. Iff present, Policy references are supported.
	 * @throws IllegalArgumentException
	 *             if {@code jaxbPolicySet } null/invalid, or {@code expressionFactory == null || combiningAlgRegistry == null})
	 */
	public CoreRootPolicyProvider(final PolicySet jaxbPolicySet, final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory,
			final CombiningAlgRegistry combiningAlgRegistry, final Optional<StaticRefPolicyProvider> refPolicyProvider) throws IllegalArgumentException
	{
		if (!refPolicyProvider.isPresent())
		{
			// no refPolicyProvider
			try
			{
				rootPolicy = PolicyEvaluators.getInstanceStatic(jaxbPolicySet, null, namespacePrefixesByURI, expressionFactory, combiningAlgRegistry, null, null);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid PolicySet: " + jaxbPolicySet.getPolicySetId(), e);
			}

			return;
		}

		// refPolicyProvider is present
		try
		{
			rootPolicy = PolicyEvaluators.getInstanceStatic(jaxbPolicySet, null, namespacePrefixesByURI, expressionFactory, combiningAlgRegistry, refPolicyProvider.get(), null);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid PolicySet: " + jaxbPolicySet.getPolicySetId(), e);
		}
	}

	/**
	 * Creates a <code>CoreRootPolicyProvider</code> with the root Policy already resolved once and for all
	 * 
	 * @param rootPolicyURL
	 *            location of root Policy(Set) (JAXB) to be parsed
	 * @param xacmlParserFactory
	 *            XACML Policy(Set) parser factory
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param refPolicyProvider
	 *            Policy-by-reference provider
	 * @return instance of this class
	 * 
	 */
	public static CoreRootPolicyProvider getInstance(final URL rootPolicyURL, final XmlnsFilteringParserFactory xacmlParserFactory, final ExpressionFactory expressionFactory,
			final CombiningAlgRegistry combiningAlgRegistry, final Optional<StaticRefPolicyProvider> refPolicyProvider)
	{
		if (rootPolicyURL == null)
		{
			throw ILLEGAL_ROOT_POLICY_URL_ARG_EXCEPTION;
		}

		if (xacmlParserFactory == null)
		{
			throw ILLEGAL_XACML_PARSER_ARG_EXCEPTION;
		}

		final XmlnsFilteringParser xacmlParser;
		try
		{
			xacmlParser = xacmlParserFactory.getInstance();
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final Object jaxbPolicyOrPolicySetObj;
		try
		{
			jaxbPolicyOrPolicySetObj = xacmlParser.parse(rootPolicyURL);
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + rootPolicyURL, e);
		}

		if (jaxbPolicyOrPolicySetObj instanceof Policy)
		{
			return new CoreRootPolicyProvider((Policy) jaxbPolicyOrPolicySetObj, xacmlParser.getNamespacePrefixUriMap(), expressionFactory, combiningAlgRegistry);
		}
		else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
		{
			return new CoreRootPolicyProvider((PolicySet) jaxbPolicyOrPolicySetObj, xacmlParser.getNamespacePrefixUriMap(), expressionFactory, combiningAlgRegistry, refPolicyProvider);
		}
		else
		{
			throw new IllegalArgumentException("Unexpected element found as root of the policy document: " + jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
		}
	}

	@Override
	public StaticTopLevelPolicyElementEvaluator getPolicy()
	{
		return rootPolicy;
	}

	@Override
	public void close()
	{
		// nothing to close
	}
}
