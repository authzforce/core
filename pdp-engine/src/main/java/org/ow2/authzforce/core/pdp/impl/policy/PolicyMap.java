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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;

/**
 * Map that provides convenient access to a policy based on the policy ID and version pattern to help resolve policy references
 *
 * @param <P>
 *            policy type
 */
public final class PolicyMap<P>
{
	/*
	 * Map: Policy(Set)Id -> Version -> Policy(Set), versions sorted from latest to earliest non-null immutable map
	 */
	private final Map<String, PolicyVersions<P>> policiesById;

	/**
	 * Create instance from map
	 * 
	 * @param policyMap
	 *            policies indexed by ID and version
	 */
	public PolicyMap(final Map<String, Map<PolicyVersion, P>> policyMap)
	{
		assert policyMap != null;
		final Map<String, PolicyVersions<P>> updatableMap = HashCollections.newUpdatableMap(policyMap.size());
		for (final Entry<String, Map<PolicyVersion, P>> entry : policyMap.entrySet())
		{
			final PolicyVersions<P> versions = new PolicyVersions<>(entry.getValue());
			updatableMap.put(entry.getKey(), versions);
		}

		this.policiesById = HashCollections.newImmutableMap(updatableMap);
	}

	/**
	 * Get latest policy version matching a policy reference
	 * 
	 * @param id
	 *            policy ID
	 * @param versionPatterns
	 *            patterns that the returned policy version must match
	 * @return policy version latest version of policy with ID {@code id} and version matching {@code versionPatterns}
	 */
	public Entry<PolicyVersion, P> get(final String id, final Optional<VersionPatterns> versionPatterns)
	{
		final PolicyVersions<P> policyVersions = policiesById.get(id);
		// id not matched
		if (policyVersions == null)
		{
			return null;
		}

		return policyVersions.getLatest(versionPatterns);
	}

	/**
	 * Get all policies in the map
	 * 
	 * @return all policies (with versions)
	 */
	public Set<Entry<String, PolicyVersions<P>>> entrySet()
	{
		return policiesById.entrySet();
	}
}