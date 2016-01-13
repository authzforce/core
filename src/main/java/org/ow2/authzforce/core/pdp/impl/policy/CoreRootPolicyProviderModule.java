/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

import org.ow2.authzforce.core.pdp.api.BaseStaticRootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.IPolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.RootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.xmlns.pdp.StaticRootPolicyProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.springframework.util.ResourceUtils;

/**
 * This is a simple implementation of <code>RootPolicyProviderModule</code> that supports static retrieval of the root policy. Its constructor accepts a
 * location that represent a Spring-compatible resource URL, and it is resolved to the actual policy when the module is initialized. Beyond this, there is no
 * modifying or re-loading the policy represented by this class.
 * <p>
 * Note that this class is designed to complement <code>BaseStaticRefPolicyProviderModule</code>. The reason is that when you define a configuration for your
 * PDP, it's easier to specify the two sets of policies by using two different Provider modules.
 */
public class CoreRootPolicyProviderModule extends BaseStaticRootPolicyProviderModule
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
		public <REF_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> RootPolicyProviderModule getInstance(StaticRootPolicyProvider jaxbConf,
				XACMLParserFactory xacmlParserFactory, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry,
				REF_POLICY_PROVIDER_CONF jaxbRefPolicyProviderConf, RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF> refPolicyProviderModuleFactory,
				int maxPolicySetRefDepth, EnvironmentProperties environmentProperties)
		{
			final String policyLocation = environmentProperties.replacePlaceholders(jaxbConf.getPolicyLocation());
			final URL rootPolicyURL;
			try
			{
				// try to load the policy location as a Spring resource
				rootPolicyURL = ResourceUtils.getURL(policyLocation);
			} catch (FileNotFoundException ioe)
			{
				throw new IllegalArgumentException("No root policy (as Spring resource) found at the following URL: " + jaxbConf.getPolicyLocation(), ioe);
			}

			return CoreRootPolicyProviderModule.getInstance(rootPolicyURL, xacmlParserFactory, expressionFactory, combiningAlgRegistry,
					jaxbRefPolicyProviderConf, refPolicyProviderModuleFactory, maxPolicySetRefDepth, environmentProperties);
		}
	}

	// the LOGGER we'll use for all messages
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(BaseStaticRootPolicyProviderModule.class);

	private final IPolicyEvaluator rootPolicy;

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
	public CoreRootPolicyProviderModule(Policy jaxbPolicy, Map<String, String> namespacePrefixesByURI, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry)
	{
		super(expressionFactory, combiningAlgRegistry, null, null, null, 0, null);

		try
		{
			rootPolicy = PolicyEvaluator.getInstance(jaxbPolicy, null, namespacePrefixesByURI, expressionFactory, combiningAlgRegistry);
		} catch (IllegalArgumentException e)
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
	 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in {@code jaxbPolicySet}; may be null if
	 *            support of PolicyReferences is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
	 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param xacmlParserFactory
	 *            XACML Parser factory; may be null if {@code jaxbRefPolicyProviderConf} as it is meant to be used by the RefPolicyProvider module
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf}
	 * @param environmentProperties
	 *            global PDP configuration environment properties
	 */
	public <CONF extends AbstractPolicyProvider> CoreRootPolicyProviderModule(PolicySet jaxbPolicySet, Map<String, String> namespacePrefixesByURI,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, XACMLParserFactory xacmlParserFactory,
			CONF jaxbRefPolicyProviderConf, RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, int maxPolicySetRefDepth,
			EnvironmentProperties environmentProperties)
	{
		super(expressionFactory, combiningAlgRegistry, xacmlParserFactory, jaxbRefPolicyProviderConf, refPolicyProviderModFactory, maxPolicySetRefDepth,
				environmentProperties);

		try
		{
			rootPolicy = PolicySetEvaluator.getInstance(jaxbPolicySet, null, namespacePrefixesByURI, expressionFactory, combiningAlgRegistry,
					refPolicyProvider, null);
		} catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid PolicySet: " + jaxbPolicySet.getPolicySetId(), e);
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
	 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in policy located at {@code rootPolicyURL};
	 *            may be null if support of PolicyReferences is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
	 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf}
	 * @param environmentProperties
	 *            global PDP configuration environment properties
	 *
	 * @return instance of this class
	 * 
	 */
	public static <CONF extends AbstractPolicyProvider> CoreRootPolicyProviderModule getInstance(URL rootPolicyURL, XACMLParserFactory xacmlParserFactory,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, CONF jaxbRefPolicyProviderConf,
			RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, int maxPolicySetRefDepth, EnvironmentProperties environmentProperties)
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
		} catch (JAXBException e)
		{
			throw new IllegalArgumentException("Failed to create JAXB unmarshaller for XML Policy(Set)", e);
		}

		final Object jaxbPolicyOrPolicySetObj;
		try
		{
			jaxbPolicyOrPolicySetObj = parser.parse(rootPolicyURL);
		} catch (JAXBException e)
		{
			throw new IllegalArgumentException("Failed to unmarshall Policy(Set) XML document from policy location: " + rootPolicyURL, e);
		}

		if (jaxbPolicyOrPolicySetObj instanceof Policy)
		{
			return new CoreRootPolicyProviderModule((Policy) jaxbPolicyOrPolicySetObj, parser.getNamespacePrefixUriMap(), expressionFactory,
					combiningAlgRegistry);
		} else if (jaxbPolicyOrPolicySetObj instanceof PolicySet)
		{
			return new CoreRootPolicyProviderModule((PolicySet) jaxbPolicyOrPolicySetObj, parser.getNamespacePrefixUriMap(), expressionFactory,
					combiningAlgRegistry, xacmlParserFactory, jaxbRefPolicyProviderConf, refPolicyProviderModFactory, maxPolicySetRefDepth,
					environmentProperties);
		} else
		{
			throw new IllegalArgumentException("Unexpected element found as root of the policy document: "
					+ jaxbPolicyOrPolicySetObj.getClass().getSimpleName());
		}
	}

	@Override
	public IPolicyEvaluator getRootPolicy()
	{
		return rootPolicy;
	}
}
