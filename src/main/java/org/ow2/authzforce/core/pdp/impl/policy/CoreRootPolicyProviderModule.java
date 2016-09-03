/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.BaseStaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.CloseableStaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticRootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.xmlns.pdp.StaticRootPolicyProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.springframework.util.ResourceUtils;

/**
 * This is a simple implementation of <code>RootPolicyProviderModule</code> that supports static retrieval of the root policy. Its constructor accepts a location that represent a Spring-compatible
 * resource URL, and it is resolved to the actual policy when the module is initialized. Beyond this, there is no modifying or re-loading the policy represented by this class.
 */
public class CoreRootPolicyProviderModule implements StaticRootPolicyProviderModule
{
	private static final IllegalArgumentException ILLEGAL_XACML_PARSER_ARG_EXCEPTION = new IllegalArgumentException("Undefined XACML parser factory");
	private static final IllegalArgumentException ILLEGAL_ROOT_POLICY_URL_ARG_EXCEPTION = new IllegalArgumentException("Undefined root policy URL");

	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RootPolicyProviderModule.Factory<StaticRootPolicyProvider>
	{

		@Override
		public Class<StaticRootPolicyProvider> getJaxbClass()
		{
			return StaticRootPolicyProvider.class;
		}

		@Override
		public <REF_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> RootPolicyProviderModule getInstance(final StaticRootPolicyProvider jaxbConf, final XACMLParserFactory xacmlParserFactory,
				final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final REF_POLICY_PROVIDER_CONF jaxbRefPolicyProviderConf,
				final RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF> refPolicyProviderModuleFactory, final int maxPolicySetRefDepth, final EnvironmentProperties environmentProperties)
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

			return CoreRootPolicyProviderModule.getInstance(rootPolicyURL, xacmlParserFactory, expressionFactory, combiningAlgRegistry, jaxbRefPolicyProviderConf, refPolicyProviderModuleFactory,
					maxPolicySetRefDepth, environmentProperties);
		}
	}

	// the LOGGER we'll use for all messages
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(BaseStaticRootPolicyProviderModule.class);

	private final StaticTopLevelPolicyElementEvaluator rootPolicy;

	/**
	 * Creates a <code>BaseStaticRootPolicyProviderModule</code> with the root Policy already resolved once and for all
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
	public CoreRootPolicyProviderModule(final Policy jaxbPolicy, final Map<String, String> namespacePrefixesByURI, final ExpressionFactory expressionFactory,
			final CombiningAlgRegistry combiningAlgRegistry)
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
	 * Creates a <code>BaseStaticRootPolicyProviderModule</code> with the root PolicySet already resolved once and for all
	 * 
	 * @param jaxbPolicySet
	 *            root PolicySet (JAXB) to be parsed
	 * @param namespacePrefixesByURI
	 *            namespace prefix-URI mappings from the original XACML PolicySet (XML) document, to be used for namespace-aware XPath evaluation
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param jaxbRefPolicyProviderConf
	 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in {@code jaxbPolicySet}; may be null if support of PolicyReferences is disabled or
	 *            this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2
	 *            -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param xacmlParserFactory
	 *            XACML Parser factory; may be null if {@code jaxbRefPolicyProviderConf} as it is meant to be used by the RefPolicyProvider module
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf}
	 * @param environmentProperties
	 *            global PDP configuration environment properties
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code jaxbPolicySet } null/invalid, or {@code expressionFactory == null || combiningAlgRegistry == null || xacmlParserFactory == null}; OR ({@code
	 *             jaxbRefPolicyProviderConf != null} AND ({@code refPolicyProviderModFactory == null || xacmlParserFactory == null} OR no PolicySet matching {@code policySetRef} could be resolved by the
	 *             refPolicyProvider OR policy reference too deep (longer than {@code maxPolicySetRefDepth}))
	 */
	public <CONF extends AbstractPolicyProvider> CoreRootPolicyProviderModule(final PolicySet jaxbPolicySet, final Map<String, String> namespacePrefixesByURI,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final XACMLParserFactory xacmlParserFactory, final CONF jaxbRefPolicyProviderConf,
			final RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, final int maxPolicySetRefDepth, final EnvironmentProperties environmentProperties) throws IllegalArgumentException
	{
		if (jaxbRefPolicyProviderConf == null)
		{
			// refPolicyProvider null
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

		// jaxbRefPolicyProviderConf != null
		try (final CloseableStaticRefPolicyProvider refPolicyProvider = new BaseStaticRefPolicyProvider(jaxbRefPolicyProviderConf, refPolicyProviderModFactory, xacmlParserFactory, expressionFactory,
				combiningAlgRegistry, maxPolicySetRefDepth, environmentProperties))
		{
			try
			{
				rootPolicy = PolicyEvaluators.getInstanceStatic(jaxbPolicySet, null, namespacePrefixesByURI, expressionFactory, combiningAlgRegistry, refPolicyProvider, null);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid PolicySet: " + jaxbPolicySet.getPolicySetId(), e);
			}
		}
		catch (final IOException e)
		{
			throw new RuntimeException("Failed to close refPolicyProvider", e);
		}
	}

	/**
	 * Creates a <code>BaseStaticRootPolicyProviderModule</code> with the root Policy already resolved once and for all
	 * 
	 * @param rootPolicyURL
	 *            location of root Policy(Set) (JAXB) to be parsed
	 * @param xacmlParserFactory
	 *            XACML Policy(Set) parser factory
	 * @param combiningAlgRegistry
	 *            registry of policy/rule combining algorithms
	 * @param expressionFactory
	 *            Expression factory for parsing Expressions used in the policy(set)
	 * @param jaxbRefPolicyProviderConf
	 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in policy located at {@code rootPolicyURL}; may be null if support of PolicyReferences
	 *            is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2
	 *            -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf}
	 * @param environmentProperties
	 *            global PDP configuration environment properties
	 *
	 * @return instance of this class
	 * 
	 */
	public static <CONF extends AbstractPolicyProvider> CoreRootPolicyProviderModule getInstance(final URL rootPolicyURL, final XACMLParserFactory xacmlParserFactory,
			final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final CONF jaxbRefPolicyProviderConf,
			final RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, final int maxPolicySetRefDepth, final EnvironmentProperties environmentProperties)
	{
		if (rootPolicyURL == null)
		{
			throw ILLEGAL_ROOT_POLICY_URL_ARG_EXCEPTION;
		}

		if (xacmlParserFactory == null)
		{
			throw ILLEGAL_XACML_PARSER_ARG_EXCEPTION;
		}

		final NamespaceFilteringParser parser;
		try
		{
			parser = xacmlParserFactory.getInstance();
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final Object jaxbPolicyOrPolicySetObj;
		try
		{
			jaxbPolicyOrPolicySetObj = parser.parse(rootPolicyURL);
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + rootPolicyURL, e);
		}

		if (jaxbPolicyOrPolicySetObj instanceof Policy)
		{
			return new CoreRootPolicyProviderModule((Policy) jaxbPolicyOrPolicySetObj, parser.getNamespacePrefixUriMap(), expressionFactory, combiningAlgRegistry);
		}
		else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
		{
			return new CoreRootPolicyProviderModule((PolicySet) jaxbPolicyOrPolicySetObj, parser.getNamespacePrefixUriMap(), expressionFactory, combiningAlgRegistry, xacmlParserFactory,
					jaxbRefPolicyProviderConf, refPolicyProviderModFactory, maxPolicySetRefDepth, environmentProperties);
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
	public TopLevelPolicyElementEvaluator getPolicy(final EvaluationContext context) throws IndeterminateEvaluationException, IllegalArgumentException
	{
		return rootPolicy;
	}

	@Override
	public void close() throws IOException
	{
		// nothing to close
	}
}
