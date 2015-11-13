/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thalesgroup.authzforce.core.policy;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;
import com.thalesgroup.authzforce.core.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.StatusHelper;

/**
 * This class is used by the PDP to find policies referenced by Policy(Set)IdReference used in
 * evaluation.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a
 * cache, a disk, a connection to a remote server, etc. for retrieving policies. Therefore, these
 * resources must be released by calling {@link #close()} when it is no longer needed.
 */
public class RefPolicyFinder implements Closeable
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(RefPolicyFinder.class);
	private static final int UNLIMITED_POLICY_REF_DEPTH = -1;

	/*
	 * Max PolicySet Reference depth. As there might be a need to use multiple ref policy finder
	 * modules in the future, we need to keep this global max as the global RefPolicyFinder's field.
	 */
	private final int maxPolicySetRefDepth;
	private final ParsingException invalidPolicyRefDepthException;

	private final RefPolicyFinderModule refPolicyFinderMod;

	/**
	 * Creates RefPolicyFinder instance
	 * 
	 * @param refPolicyFinderMod
	 *            referenced Policy finder module (supports Policy(Set)IdReferences)
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1
	 *            -> PolicySet2 -> ...
	 * 
	 */
	public RefPolicyFinder(RefPolicyFinderModule refPolicyFinderMod, int maxPolicySetRefDepth)
	{
		this.refPolicyFinderMod = refPolicyFinderMod;
		if (maxPolicySetRefDepth < 0)
		{
			throw new IllegalArgumentException("Invalid max PolicySet Reference depth: " + maxPolicySetRefDepth + ". Required: >= 0");
		}

		this.maxPolicySetRefDepth = maxPolicySetRefDepth;
		this.invalidPolicyRefDepthException = new ParsingException("Depth of PolicySetIdReference > max allowed (" + maxPolicySetRefDepth + ")");
	}

	/**
	 * Whether policy reference resolution is static, i.e. policies that can be referenced are
	 * resolved and parsed once and for all at initialization time
	 * 
	 * @return true iff policy resolution is static
	 */
	public boolean isStatic()
	{
		return refPolicyFinderMod.isStatic();
	}

	/**
	 * Finds a policy based on an id reference. This may involve using the reference as indexing
	 * data to lookup a policy.
	 * 
	 * @param idRef
	 *            the identifier used to resolve the policy by its Policy(Set)Id
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here, because not equivalent to XML schema
	 *            anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
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
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not
	 *            all possible values of xs:anyURI can be passed to the java.net.URI constructor.
	 * @param refPolicyType
	 *            type of policy element requested (policy or policySet)
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy, matched against
	 *            its Version attribute
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the policy using reference
	 *            {@code idRef}. Therefore this argument does not include idRef. This chain is used
	 *            to control all PolicySetIdReferences found within the result policy, i.e. detect
	 *            loops and validate reference depth against last argument passed to
	 *            {@link #RefPolicyFinder(RefPolicyFinderModule, int)}.
	 *            <p>
	 *            (Do not use a Queue as it is FIFO, and we need LIFO and iteration in order of
	 *            insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * 
	 * @return the policy matching the policy reference
	 * @throws ParsingException
	 *             Error parsing found policy. The policy finder module may parse policies lazily or
	 *             on the fly, i.e. only when the policy is requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type {@code policyType}
	 */
	public <T extends IPolicy> T findPolicy(String idRef, VersionConstraints constraints, Class<T> refPolicyType, Deque<String> policySetRefChain) throws ParsingException, IndeterminateEvaluationException
	{
		if (refPolicyFinderMod == null)
		{
			throw new IndeterminateEvaluationException("No RefPolicyFinder defined to resolve any Policy(Set)IdReference", StatusHelper.STATUS_PROCESSING_ERROR);
		}

		final Deque<String> newPolicySetRefChain;
		if (refPolicyType == PolicySetEvaluator.class)
		{
			if (policySetRefChain == null)
			{
				newPolicySetRefChain = new ArrayDeque<>();
			} else
			{
				/*
				 * Check for circular reference (loop). We check only the policy ID because we
				 * consider that a mere reference back to the same ID is not allowed, no matter what
				 * the version is.
				 */
				if (policySetRefChain.contains(idRef))
				{
					throw new ParsingException("Invalid PolicySetIdReference: circular reference (loop) detected: " + policySetRefChain + " -> " + idRef);
				}

				// validate reference depth
				if (maxPolicySetRefDepth != UNLIMITED_POLICY_REF_DEPTH && policySetRefChain.size() > maxPolicySetRefDepth)
				{
					throw invalidPolicyRefDepthException;
				}

				newPolicySetRefChain = new ArrayDeque<>(policySetRefChain);
			}

			newPolicySetRefChain.add(idRef);
		} else
		{
			// not a PolicySetIdReference, nothing to change in the chain
			newPolicySetRefChain = policySetRefChain;
		}

		return refPolicyFinderMod.findPolicy(idRef, refPolicyType, constraints, newPolicySetRefChain);
	}

	@Override
	public void close() throws IOException
	{
		this.refPolicyFinderMod.close();
	}
}
