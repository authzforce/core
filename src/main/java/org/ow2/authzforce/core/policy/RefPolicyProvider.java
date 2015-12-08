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
package org.ow2.authzforce.core.policy;

import java.util.ArrayDeque;
import java.util.Deque;

import org.ow2.authzforce.core.IndeterminateEvaluationException;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;

/**
 * Policy-by-reference provider, used by the PDP to get policies referenced by Policy(Set)IdReference in PolicySets.
 */
public interface RefPolicyProvider
{
	/**
	 * Utilities for RefPolicyProvider sub-modules
	 *
	 */
	class Utils
	{
		static final int UNLIMITED_POLICY_REF_DEPTH = -1;

		static Deque<String> checkAndUpdatePolicySetRefChain(Deque<String> policySetRefChain, String nextPolicySetIdRef, int maxPolicySetRefDepth)
				throws ParsingException
		{
			// nextPolicySetIdRef assumed to be a PolicySetIdReference
			final Deque<String> newPolicySetRefChain;
			if (policySetRefChain == null)
			{
				newPolicySetRefChain = new ArrayDeque<>();
			} else
			{
				/*
				 * Check for circular reference (loop). We check only the policy ID because we consider that a mere reference back to the same ID is not
				 * allowed, no matter what the version is.
				 */
				if (policySetRefChain.contains(nextPolicySetIdRef))
				{
					throw new ParsingException("Invalid PolicySetIdReference: circular reference (loop) detected: " + policySetRefChain + " -> "
							+ nextPolicySetIdRef);
				}

				// validate reference depth
				final int actualPolicySetRefDepth = policySetRefChain.size();
				if (maxPolicySetRefDepth != UNLIMITED_POLICY_REF_DEPTH && actualPolicySetRefDepth > maxPolicySetRefDepth)
				{
					throw new ParsingException("Depth of Policy Reference (" + actualPolicySetRefDepth + ") > max allowed (" + maxPolicySetRefDepth + "): "
							+ policySetRefChain);
				}

				newPolicySetRefChain = new ArrayDeque<>(policySetRefChain);
			}

			newPolicySetRefChain.add(nextPolicySetIdRef);
			return newPolicySetRefChain;
		}

		/**
		 * Checks whether the given Policy reference chain's depth does not exceed the given limit
		 * 
		 * @param policySetRefChain
		 *            Policy reference chain to be checked
		 * @param maxPolicySetRefDepth
		 *            max allowed Policy reference depth
		 * @throws ParsingException
		 *             error if check failed (depth exceeded)
		 */
		static void checkPolicySetRefChain(Deque<String> policySetRefChain, int maxPolicySetRefDepth) throws ParsingException
		{
			// validate reference depth
			final int actualPolicySetRefDepth = policySetRefChain.size();
			if (maxPolicySetRefDepth != UNLIMITED_POLICY_REF_DEPTH && actualPolicySetRefDepth > maxPolicySetRefDepth)
			{
				throw new ParsingException("Depth of Policy Reference (" + actualPolicySetRefDepth + ") > max allowed (" + maxPolicySetRefDepth + "): "
						+ policySetRefChain);
			}
		}
	}

	/**
	 * Whether policy reference resolution is static, i.e. policies that can be referenced are resolved and parsed once and for all at initialization time
	 * 
	 * @return true iff policy resolution is static
	 */
	boolean isStatic();

	/**
	 * Finds a policy based on an id reference. This may involve using the reference as indexing data to lookup a policy.
	 * 
	 * @param policyIdRef
	 *            the identifier used to resolve the policy by its Policy(Set)Id
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            See also:
	 *            </p>
	 *            <p>
	 *            https://java.net/projects/jaxb/lists/users/archive/2011-07/message/16
	 *            </p>
	 *            <p>
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the
	 *            java.net.URI constructor.
	 * @param refPolicyType
	 *            type of policy element requested (policy or policySet)
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy, matched against its Version attribute
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the policy using reference {@code idRef}. Therefore this argument does not include idRef.
	 *            This chain is used to control all PolicySetIdReferences found within the result policy, i.e. detect loops (circular references) and validate
	 *            reference depth.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from
	 *            Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * 
	 * @return the policy matching the policy reference; or null if no match
	 * @throws ParsingException
	 *             Error parsing found policy. The policy Provider module may parse policies lazily or on the fly, i.e. only when the policy is requested/looked
	 *             for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type {@code policyType}
	 */
	<P extends IPolicyEvaluator> P get(String policyIdRef, VersionConstraints constraints, Class<P> refPolicyType, Deque<String> policySetRefChain)
			throws ParsingException, IndeterminateEvaluationException;

}