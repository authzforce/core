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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.authzforce.core.pdp.api.PolicyVersion;

/**
 * View of all statically resolved policies applicable by the PDP or specific PolicySet evaluator (root policy as well
 * as policies referenced directly or indirectly from the root one)
 *
 */
public final class StaticApplicablePolicyView implements Iterable<Entry<String, PolicyVersion>>
{
	private static final IllegalArgumentException ILLEGAL_ARGUMENTS_EXCEPTION = new IllegalArgumentException("Null root policy ID/version");
	private static final UnsupportedOperationException UNSUPPORTED_REMOVE_OPERATION_EXCEPTION = new UnsupportedOperationException();
	private final Entry<String, PolicyVersion> rootPolicyEntry;
	private final Map<String, PolicyVersion> refPolicies;

	private final class IteratorImpl implements Iterator<Entry<String, PolicyVersion>>
	{

		private Iterator<Entry<String, PolicyVersion>> refPolicyIterator;

		private IteratorImpl()
		{
			refPolicyIterator = refPolicies.entrySet().iterator();
		}

		private boolean isFirst = true;

		@Override
		public boolean hasNext()
		{
			return isFirst || refPolicyIterator.hasNext();
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

			return refPolicyIterator.next();
		}

		@Override
		public void remove()
		{
			throw UNSUPPORTED_REMOVE_OPERATION_EXCEPTION;
		}

	}

	/**
	 * Creates view of applicable policies from the root policy metadata and map of referenced policies
	 * 
	 * @param rootPolicyId
	 *            (static) root policy ID
	 * @param rootPolicyVersion
	 *            (static) root policy version
	 * @param refPolicies
	 *            map of (statically) referenced policies by policy ID
	 */
	public StaticApplicablePolicyView(String rootPolicyId, PolicyVersion rootPolicyVersion,
			Map<String, PolicyVersion> refPolicies)
	{
		if (rootPolicyId == null || rootPolicyVersion == null)
		{
			throw ILLEGAL_ARGUMENTS_EXCEPTION;
		}

		this.rootPolicyEntry = new SimpleImmutableEntry<>(rootPolicyId, rootPolicyVersion);
		this.refPolicies = refPolicies == null ? Collections.<String, PolicyVersion> emptyMap() : refPolicies;
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
	 * Get the root policy version
	 * 
	 * @return policy version
	 */
	public PolicyVersion rootPolicyVersion()
	{
		return this.rootPolicyEntry.getValue();
	}

	/**
	 * Policies referenced directly or indirectly from the root policy; empty map if none
	 * 
	 * @return referenced policies (by policy ID)
	 */
	public Map<String, PolicyVersion> refPolicies()
	{
		return this.refPolicies;
	}

	/**
	 * The root policy entry is always the first item in the iteration
	 */
	@Override
	public Iterator<Entry<String, PolicyVersion>> iterator()
	{
		return new IteratorImpl();
	}

	/**
	 * Get applicable policy matching a given policy ID
	 * 
	 * @param policyId
	 *            policy ID to be matched
	 * @return matching applicable policy version; null if no match
	 */
	public PolicyVersion get(String policyId)
	{
		return rootPolicyEntry.getKey().equals(policyId) ? rootPolicyEntry.getValue() : refPolicies.get(policyId);
	}
}