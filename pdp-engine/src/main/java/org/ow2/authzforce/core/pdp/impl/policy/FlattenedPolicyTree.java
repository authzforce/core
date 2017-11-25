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

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.policy.PolicyRefsMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

/**
 * Flattened view of a policy tree made of a root policy and, if any (e.g. the root policy is a XACML PolicySet enclosing Policy(Set)IdReferences), policies referenced from it. (This assumes that all
 * policy references have been statically resolved.)
 * 
 * @version $Id: $
 */
public final class FlattenedPolicyTree implements Iterable<PrimaryPolicyMetadata>
{
	private static final IllegalArgumentException ILLEGAL_ARGUMENTS_EXCEPTION = new IllegalArgumentException("Undefined root policy metadata");

	private final PrimaryPolicyMetadata immutableRootPolicyMetadata;
	private final Optional<PolicyRefsMetadata> immutableRootPolicyRefsMetadata;

	private transient ImmutableMap<String, PrimaryPolicyMetadata> lazilyFilledMapOfRefPolicies = null;
	private transient ImmutableMap<String, PrimaryPolicyMetadata> lazilyFilledMapOfRefPolicySets = null;

	/**
	 * Creates view of applicable policies from the root policy metadata and map of referenced policies. All fields made immutable by this constructor.
	 *
	 * @param rootPolicyMetadata
	 *            root policy metadata
	 * @param rootPolicyRefsMetadata
	 *            (optional) root policy's extra metadata, if they is any
	 */
	public FlattenedPolicyTree(final PrimaryPolicyMetadata rootPolicyMetadata, final Optional<PolicyRefsMetadata> rootPolicyRefsMetadata)
	{
		if (rootPolicyMetadata == null)
		{
			throw ILLEGAL_ARGUMENTS_EXCEPTION;
		}

		this.immutableRootPolicyMetadata = rootPolicyMetadata;
		this.immutableRootPolicyRefsMetadata = rootPolicyRefsMetadata;
	}

	/**
	 * Get the root policy type
	 *
	 * @return root policy type
	 */
	public PrimaryPolicyMetadata rootPolicyMetadata()
	{
		return this.immutableRootPolicyMetadata;
	}

	/**
	 * Get the extra metadata of the root policy
	 *
	 * @return policy version
	 */
	public Optional<PolicyRefsMetadata> rootPolicyRefsMetadata()
	{
		return this.immutableRootPolicyRefsMetadata;
	}

	private void initMaps()
	{
		if (immutableRootPolicyRefsMetadata.isPresent())
		{
			final Set<PrimaryPolicyMetadata> allRefPolicies = immutableRootPolicyRefsMetadata.get().getRefPolicies();
			final int maxNumOfRefPolicies;
			final int maxNumOfRefPolicySets;
			if (this.rootPolicyMetadata().getType() == TopLevelPolicyElementType.POLICY)
			{
				maxNumOfRefPolicies = 1 + allRefPolicies.size();
				maxNumOfRefPolicySets = allRefPolicies.size();
			}
			else
			{
				maxNumOfRefPolicies = allRefPolicies.size();
				maxNumOfRefPolicySets = 1 + allRefPolicies.size();
			}

			final Map<String, PrimaryPolicyMetadata> mutableMapOfRefPolicies = HashCollections.newUpdatableMap(maxNumOfRefPolicies);
			final Map<String, PrimaryPolicyMetadata> mutableMapOfRefPolicySets = HashCollections.newUpdatableMap(maxNumOfRefPolicySets);

			for (final PrimaryPolicyMetadata refPolicyMetadata : allRefPolicies)
			{
				if (refPolicyMetadata.getType() == TopLevelPolicyElementType.POLICY)
				{
					mutableMapOfRefPolicies.put(refPolicyMetadata.getId(), refPolicyMetadata);
				}
				else
				{
					mutableMapOfRefPolicySets.put(refPolicyMetadata.getId(), refPolicyMetadata);
				}
			}

			this.lazilyFilledMapOfRefPolicies = ImmutableMap.copyOf(mutableMapOfRefPolicies);
			this.lazilyFilledMapOfRefPolicySets = ImmutableMap.copyOf(mutableMapOfRefPolicySets);
		}
		else
		{
			if (this.rootPolicyMetadata().getType() == TopLevelPolicyElementType.POLICY)
			{
				this.lazilyFilledMapOfRefPolicies = ImmutableMap.of(this.immutableRootPolicyMetadata.getId(), immutableRootPolicyMetadata);
				this.lazilyFilledMapOfRefPolicySets = ImmutableMap.of();
			}
			else
			{
				this.lazilyFilledMapOfRefPolicies = ImmutableMap.of();
				this.lazilyFilledMapOfRefPolicySets = ImmutableMap.of(this.immutableRootPolicyMetadata.getId(), immutableRootPolicyMetadata);
			}
		}
	}

	private Map<String, PrimaryPolicyMetadata> getMapOfRefPolicies()
	{
		if (this.lazilyFilledMapOfRefPolicies == null)
		{
			initMaps();
		}

		return this.lazilyFilledMapOfRefPolicies;

	}

	private Map<String, PrimaryPolicyMetadata> getMapOfRefPolicySets()
	{
		if (this.lazilyFilledMapOfRefPolicySets == null)
		{
			initMaps();
		}

		return this.lazilyFilledMapOfRefPolicySets;
	}

	/**
	 * Type-specific policy iterator
	 * 
	 * @param policyType
	 *            policy type (Policy or PolicySet)
	 *
	 * @return unmodifiable iterator over applicable policies of requested type
	 */
	public Iterator<PrimaryPolicyMetadata> policyIterator(final TopLevelPolicyElementType policyType)
	{
		return policyType == TopLevelPolicyElementType.POLICY ? getMapOfRefPolicies().values().iterator() : getMapOfRefPolicySets().values().iterator();
	}

	/**
	 * Get applicable policy matching a given policy type and ID
	 * 
	 * @param policyType
	 *            policy type (Policy or PolicySet)
	 *
	 * @param policyId
	 *            policy ID to be matched
	 * @return matching applicable policy version; null if no match
	 */
	public PrimaryPolicyMetadata getPolicy(final TopLevelPolicyElementType policyType, final String policyId)
	{
		return policyType == TopLevelPolicyElementType.POLICY ? getMapOfRefPolicies().get(policyId) : getMapOfRefPolicySets().get(policyId);
	}

	/**
	 * The first element is the root policy in the tree, then, if any (e.g. the root policy is a XACML PolicySet enclosing Policy(Set)IdReferences), come the policies referenced from it. (This assumes
	 * that all policy references, e.g. Policy(Set)IdReferences, have been statically resolved.)
	 */
	@Override
	public Iterator<PrimaryPolicyMetadata> iterator()
	{
		final Iterator<PrimaryPolicyMetadata> rootPolicyIterator = Iterators.singletonIterator(this.immutableRootPolicyMetadata);
		return immutableRootPolicyRefsMetadata.isPresent() ? Iterators.concat(rootPolicyIterator, this.immutableRootPolicyRefsMetadata.get().getRefPolicies().iterator()) : rootPolicyIterator;
	}
}
