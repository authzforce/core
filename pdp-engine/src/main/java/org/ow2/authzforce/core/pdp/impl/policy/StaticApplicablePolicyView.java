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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import org.ow2.authzforce.core.pdp.api.policy.ExtraPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;

/**
 * View of all statically resolved policies applicable by the PDP or specific PolicySet evaluator (root policy as well
 * as policies referenced directly or indirectly from the root one)
 *
 * 
 * @version $Id: $
 */
public final class StaticApplicablePolicyView implements Iterable<Entry<String, PolicyVersion>>
{
	private static final IllegalArgumentException ILLEGAL_ARGUMENTS_EXCEPTION = new IllegalArgumentException(
			"Null root policy ID/version/extra metadata");
	private static final UnsupportedOperationException UNSUPPORTED_REMOVE_OPERATION_EXCEPTION = new UnsupportedOperationException();

	private final Entry<String, PolicyVersion> rootPolicyEntry;
	private final TopLevelPolicyElementType rootPolicyType;
	private final ExtraPolicyMetadata extraRootPolicyMetadata;

	private final class PolicySetIteratorImpl implements Iterator<Entry<String, PolicyVersion>>
	{

		private final Iterator<Entry<String, PolicyVersion>> refPolicySetIterator;

		private boolean isFirst = true;

		private PolicySetIteratorImpl()
		{
			refPolicySetIterator = extraRootPolicyMetadata.getRefPolicySets().entrySet().iterator();
		}

		@Override
		public boolean hasNext()
		{
			return isFirst || refPolicySetIterator.hasNext();
		}

		@Override
		public Entry<String, PolicyVersion> next()
		{
			if (isFirst)
			{
				// first item is the root policy
				// next one is not the first anymore
				isFirst = false;
				return rootPolicyEntry;
			}

			return refPolicySetIterator.next();
		}

		@Override
		public void remove()
		{
			throw UNSUPPORTED_REMOVE_OPERATION_EXCEPTION;
		}

	}

	/**
	 * Creates view of applicable policies from the root policy metadata and map of referenced policies. All fields made
	 * immutable by this constructor.
	 *
	 * @param rootPolicyType
	 *            root policy type
	 * @param rootPolicyId
	 *            (static) root policy ID
	 * @param extraRootPolicyMetadata
	 *            root policy's extra metadata
	 */
	public StaticApplicablePolicyView(TopLevelPolicyElementType rootPolicyType, String rootPolicyId,
			ExtraPolicyMetadata extraRootPolicyMetadata)
	{
		if (rootPolicyType == null || rootPolicyId == null || extraRootPolicyMetadata == null)
		{
			throw ILLEGAL_ARGUMENTS_EXCEPTION;
		}

		this.rootPolicyType = rootPolicyType;
		this.rootPolicyEntry = new SimpleImmutableEntry<>(rootPolicyId, extraRootPolicyMetadata.getVersion());
		this.extraRootPolicyMetadata = extraRootPolicyMetadata;
	}

	/**
	 * Get the root policy type
	 *
	 * @return root policy type
	 */
	public TopLevelPolicyElementType rootPolicyType()
	{
		return this.rootPolicyType;
	}

	/**
	 * Get the root policy ID
	 *
	 * @return root policy ID
	 */
	public String rootPolicyId()
	{
		return this.rootPolicyEntry.getKey();
	}

	/**
	 * Get the extra metadata of the root policy
	 *
	 * @return policy version
	 */
	public ExtraPolicyMetadata rootPolicyExtraMetadata()
	{
		return this.extraRootPolicyMetadata;
	}

	/**
	 * If {@link #rootPolicyType()} returns {@link TopLevelPolicyElementType#POLICY}, it is the only item in the
	 * iteration (since there is no child Policy(Set)). Else this iterates over policies referenced directly or
	 * indirectly from the root policySet.
	 *
	 * @return iterator over applicable Policies
	 */
	public Iterator<Entry<String, PolicyVersion>> policyIterator()
	{
		return (rootPolicyType == TopLevelPolicyElementType.POLICY ? Collections.singleton(rootPolicyEntry)
				: extraRootPolicyMetadata.getRefPolicies().entrySet()).iterator();
	}

	/**
	 * If {@link #rootPolicyType()} returns {@link TopLevelPolicyElementType#POLICY}, there is no item in this
	 * iteration, else the root policy(set) entry is always the first item in the iteration, then the policies
	 * referenced directly or indirectly from the root policySet.
	 *
	 * @return iterator over applicable PolicySets
	 */
	public Iterator<Entry<String, PolicyVersion>> policySetIterator()
	{
		// PolicySetIteratorImpl is an inner class, therefore it has state
		return rootPolicyType == TopLevelPolicyElementType.POLICY
				? Collections.<Entry<String, PolicyVersion>>emptyIterator() : new PolicySetIteratorImpl();
	}

	/**
	 * Get applicable Policy matching a given policy ID
	 *
	 * @param policyId
	 *            policy ID to be matched
	 * @return matching applicable policy version; null if no match
	 */
	public PolicyVersion getPolicy(String policyId)
	{
		return rootPolicyType == TopLevelPolicyElementType.POLICY && rootPolicyEntry.getKey().equals(policyId)
				? rootPolicyEntry.getValue() : extraRootPolicyMetadata.getRefPolicies().get(policyId);
	}

	/**
	 * Get applicable PolicySet matching a given policy ID
	 *
	 * @param policyId
	 *            policy ID to be matched
	 * @return matching applicable policy version; null if no match
	 */
	public PolicyVersion getPolicySet(String policyId)
	{
		return rootPolicyType == TopLevelPolicyElementType.POLICY_SET && rootPolicyEntry.getKey().equals(policyId)
				? rootPolicyEntry.getValue() : extraRootPolicyMetadata.getRefPolicySets().get(policyId);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Same as {@link #policySetIterator()}
	 */
	@Override
	public Iterator<Entry<String, PolicyVersion>> iterator()
	{
		return policySetIterator();
	}
}
