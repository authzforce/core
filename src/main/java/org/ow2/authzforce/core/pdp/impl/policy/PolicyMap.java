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

import java.util.Map;
import java.util.Map.Entry;
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
	public Entry<PolicyVersion, P> get(final String id, final VersionPatterns versionPatterns)
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