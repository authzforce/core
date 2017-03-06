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

import java.io.IOException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.BaseStaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.CloseableStaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.RefPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticRootPolicyProviderModule;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.ow2.authzforce.core.xmlns.pdp.StaticRefBasedRootPolicyProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * This Root policy provider module retrieves the root policy from a {@link RefPolicyProviderModule} statically (once and for all), based on a XACML PolicySetIdReference.
 */
public class CoreRefBasedRootPolicyProviderModule implements StaticRootPolicyProviderModule
{
	private static final IllegalArgumentException NULL_REF_POLICY_PROVIDER_CONF = new IllegalArgumentException("Undefined refPolicyProvider. Root policy provider module '"
			+ CoreRefBasedRootPolicyProviderModule.class + "' requires a refPolicyProvider module configuration to be defined.");
	private static final IllegalArgumentException ILLEGAL_XML_CONF_ARG_EXCEPTION = new IllegalArgumentException("Undefined XML/JAXB configuration");
	private static final IllegalArgumentException ILLEGAL_XACML_POLICY_REF_ARG_EXCEPTION = new IllegalArgumentException("Undefined XACML PolicySetIdReference");

	/**
	 * Module factory
	 * 
	 */
	public static class Factory extends RootPolicyProviderModule.Factory<StaticRefBasedRootPolicyProvider>
	{

		@Override
		public Class<StaticRefBasedRootPolicyProvider> getJaxbClass()
		{
			return StaticRefBasedRootPolicyProvider.class;
		}

		@Override
		public <REF_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> RootPolicyProviderModule getInstance(final StaticRefBasedRootPolicyProvider jaxbConf,
				final XACMLParserFactory xacmlParserFactory, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry,
				final REF_POLICY_PROVIDER_CONF jaxbRefPolicyProviderConf, final RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF> refPolicyProviderModuleFactory,
				final int maxPolicySetRefDepth, final EnvironmentProperties environmentProperties)
		{
			if (jaxbConf == null)
			{
				throw ILLEGAL_XML_CONF_ARG_EXCEPTION;
			}

			return new CoreRefBasedRootPolicyProviderModule(jaxbConf.getPolicyRef(), expressionFactory, combiningAlgRegistry, xacmlParserFactory, jaxbRefPolicyProviderConf,
					refPolicyProviderModuleFactory, maxPolicySetRefDepth, environmentProperties);
		}
	}

	private final StaticTopLevelPolicyElementEvaluator rootPolicy;

	/**
	 * Creates instance with the root PolicySet retrieved from the refPolicyprovider once and for all
	 * 
	 * @param policyRef
	 *            Policy(Set)Id reference to be resolved by the refPolicyProvider module
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
	 *            PDP configuration environment properties
	 * @throws IllegalArgumentException
	 *             if {@code policySetRef} is null/invalid, or if {@code jaxbRefPolicyProviderConf == null || expressionFactory == null || combiningAlgRegistry == null || xacmlParserFactory == null}
	 *             or no PolicySet matching {@code policySetRef} could be resolved by the refPolicyProvider
	 */
	public <CONF extends AbstractPolicyProvider> CoreRefBasedRootPolicyProviderModule(final IdReferenceType policyRef, final ExpressionFactory expressionFactory,
			final CombiningAlgRegistry combiningAlgRegistry, final XACMLParserFactory xacmlParserFactory, final CONF jaxbRefPolicyProviderConf,
			final RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, final int maxPolicySetRefDepth, final EnvironmentProperties environmentProperties) throws IllegalArgumentException
	{
		if (jaxbRefPolicyProviderConf == null)
		{
			throw NULL_REF_POLICY_PROVIDER_CONF;
		}

		/*
		 * The refPolicyProviderModule is not instantiated here but in the BaseRefPolicyProvider since it is the one using this resource (refPolicyProviderModule), therefore responsible for closing it
		 * (call Closeable#close()) when it is done using them. We apply the basic principle that is the class creating the resource, that manages/closes it.
		 */
		try (final CloseableStaticRefPolicyProvider refPolicyProvider = new BaseStaticRefPolicyProvider(jaxbRefPolicyProviderConf, refPolicyProviderModFactory, xacmlParserFactory, expressionFactory,
				combiningAlgRegistry, maxPolicySetRefDepth, environmentProperties))
		{

			if (policyRef == null)
			{
				throw ILLEGAL_XACML_POLICY_REF_ARG_EXCEPTION;
			}

			final String policySetId = policyRef.getValue();
			final VersionPatterns versionPatterns = new VersionPatterns(policyRef.getVersion(), policyRef.getEarliestVersion(), policyRef.getLatestVersion());
			try
			{
				rootPolicy = refPolicyProvider.get(TopLevelPolicyElementType.POLICY_SET, policySetId, versionPatterns, null);
			}
			catch (final IndeterminateEvaluationException e)
			{
				throw new IllegalArgumentException("Failed to find a root PolicySet with id = '" + policySetId + "', " + versionPatterns, e);
			}

			if (rootPolicy == null)
			{
				throw new IllegalArgumentException("No policy found by the refPolicyProvider for the specified PolicySetIdReference: PolicySetId = '" + policySetId + "'; " + versionPatterns);
			}
		}
		catch (final IOException e)
		{
			throw new RuntimeException("Failed to close refPolicyProvider", e);
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
