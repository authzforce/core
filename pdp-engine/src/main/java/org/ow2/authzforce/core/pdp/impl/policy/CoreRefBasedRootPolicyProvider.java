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

import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticRootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.ow2.authzforce.core.xmlns.pdp.StaticRefBasedRootPolicyProvider;

import com.google.common.base.Preconditions;

/**
 * This Root policy provider retrieves the root policy from a {@link CloseableRefPolicyProvider} statically (once and for all), based on a XACML PolicySetIdReference.
 */
public class CoreRefBasedRootPolicyProvider implements StaticRootPolicyProvider
{
	private static final String NULL_REF_POLICY_PROVIDER_CONF_MESSAGE = "Undefined refPolicyProvider. Root policy provider '" + CoreRefBasedRootPolicyProvider.class
			+ "' requires a refPolicyProvider.";
	private static final String ILLEGAL_XML_CONF_ARG_MESSAGE = "Undefined XML/JAXB configuration";
	private static final String ILLEGAL_XACML_POLICY_REF_ARG_MESSAGE = "Undefined XACML PolicySetIdReference";

	/**
	 * Provider factory
	 * 
	 */
	public static class Factory extends RootPolicyProvider.Factory<StaticRefBasedRootPolicyProvider>
	{

		@Override
		public Class<StaticRefBasedRootPolicyProvider> getJaxbClass()
		{
			return StaticRefBasedRootPolicyProvider.class;
		}

		@Override
		public RootPolicyProvider getInstance(final StaticRefBasedRootPolicyProvider jaxbConf, final XmlnsFilteringParserFactory xacmlParserFactory, final ExpressionFactory expressionFactory,
				final CombiningAlgRegistry combiningAlgRegistry, final Optional<CloseableRefPolicyProvider> optionalRefPolicyProvider, final EnvironmentProperties environmentProperties)
		{
			Preconditions.checkNotNull(jaxbConf, ILLEGAL_XML_CONF_ARG_MESSAGE);
			Preconditions.checkArgument(optionalRefPolicyProvider.isPresent(), NULL_REF_POLICY_PROVIDER_CONF_MESSAGE);
			return new CoreRefBasedRootPolicyProvider(jaxbConf.getPolicyRef(), optionalRefPolicyProvider.get());
		}
	}

	private final StaticTopLevelPolicyElementEvaluator rootPolicy;

	/**
	 * Creates instance with the root PolicySet retrieved from the refPolicyprovider once and for all
	 * 
	 * @param policyRef
	 *            Policy(Set)Id reference to be resolved by the {@code refPolicyProvider}
	 * @param refPolicyProvider
	 *            (mandatory) Policy-by-reference Provider used by this Root Policy Provider to resolve policy references, if not null. If null, policy references are not supported.
	 * @throws IllegalArgumentException
	 *             if {@code policyRef} is null/invalid, or if {@code refPolicyProvider == null || !(refPolicyProvider instanceof StaticRefPolicyProvider)} or no PolicySet matching {@code policyRef}
	 *             could be resolved by the refPolicyProvider
	 */
	public CoreRefBasedRootPolicyProvider(final IdReferenceType policyRef, final CloseableRefPolicyProvider refPolicyProvider) throws IllegalArgumentException
	{
		Preconditions.checkNotNull(policyRef, ILLEGAL_XACML_POLICY_REF_ARG_MESSAGE);
		Preconditions.checkNotNull(refPolicyProvider, NULL_REF_POLICY_PROVIDER_CONF_MESSAGE);
		Preconditions.checkArgument(refPolicyProvider instanceof StaticRefPolicyProvider, "RefPolicyProvider arg '" + refPolicyProvider + "'  incompatible with "
				+ CoreRefBasedRootPolicyProvider.class + ". Expected: instance of " + StaticRefPolicyProvider.class + ". Make sure the PDP extension of type "
				+ CloseableRefPolicyProvider.Factory.class + " corresponding to the refPolicyProvider in PDP configuration can create instances of " + StaticRefPolicyProvider.class);

		final String policySetId = policyRef.getValue();
		final VersionPatterns versionPatterns = new VersionPatterns(policyRef.getVersion(), policyRef.getEarliestVersion(), policyRef.getLatestVersion());
		try
		{
			rootPolicy = ((StaticRefPolicyProvider) refPolicyProvider).get(TopLevelPolicyElementType.POLICY_SET, policySetId, Optional.of(versionPatterns), null);
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

	@Override
	public StaticTopLevelPolicyElementEvaluator getPolicy()
	{
		return rootPolicy;
	}

	@Override
	public void close()
	{
		// Nothing to close - erase exception from the close() signature
	}
}
