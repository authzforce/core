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

import java.io.Closeable;
import java.io.IOException;
import java.util.Deque;

import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.StatusHelper;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.core.policy.RefPolicyProviderModule.Factory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;

/**
 * This class is used by the PDP to find policies referenced by Policy(Set)IdReference used in evaluation.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for
 * retrieving policies. Therefore, these resources must be released by calling {@link #close()} when it is no longer needed.
 */
public class BaseRefPolicyProvider implements Closeable, RefPolicyProvider
{
	/*
	 * Max PolicySet Reference depth. As there might be a need to use multiple ref policy Provider modules in the future, we need to keep this global max as the
	 * global RefPolicyProvider's field.
	 */
	private final int maxPolicySetRefDepth;

	private final RefPolicyProviderModule refPolicyProviderMod;

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param refPolicyProviderMod
	 *            referenced Policy Provider module (supports Policy(Set)IdReferences)
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1 -> PolicySet2 -> ...
	 * 
	 */
	private BaseRefPolicyProvider(RefPolicyProviderModule refPolicyProviderMod, int maxPolicySetRefDepth)
	{
		this.refPolicyProviderMod = refPolicyProviderMod;
		if (maxPolicySetRefDepth < 0)
		{
			throw new IllegalArgumentException("Invalid max PolicySet Reference depth: " + maxPolicySetRefDepth + ". Required: >= 0");
		}

		this.maxPolicySetRefDepth = maxPolicySetRefDepth;
	}

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProvider}
	 * @param jaxbRefPolicyProvider
	 *            XML/JAXB configuration of RefPolicyProvider module
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1 -> PolicySet2 -> ...
	 * @param expressionFactory
	 *            Expression factory for parsing XACML Expressions in the policies
	 * @param combiningAlgRegistry
	 *            Combining algorithm registry for getting implementations of algorithms used in the policies
	 */
	public BaseRefPolicyProvider(AbstractPolicyProvider jaxbRefPolicyProvider, Factory<AbstractPolicyProvider> refPolicyProviderModFactory,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, int maxPolicySetRefDepth)
	{
		this(refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry), maxPolicySetRefDepth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyProvider#isStatic()
	 */
	@Override
	public boolean isStatic()
	{
		return refPolicyProviderMod.isStatic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyProvider#findPolicy(java.lang.String, com.sun.xacml.VersionConstraints, java.lang.Class, java.util.Deque)
	 */
	@Override
	public <T extends IPolicyEvaluator> T get(String idRef, VersionConstraints constraints, Class<T> refPolicyType, Deque<String> policySetRefChain)
			throws ParsingException, IndeterminateEvaluationException
	{
		if (refPolicyProviderMod == null)
		{
			throw new IndeterminateEvaluationException("No RefPolicyProvider defined to resolve any Policy(Set)IdReference", StatusHelper.STATUS_PROCESSING_ERROR);
		}

		final Deque<String> newPolicySetRefChain;
		if (refPolicyType == PolicySetEvaluator.class)
		{
			newPolicySetRefChain = Utils.checkAndUpdatePolicySetRefChain(policySetRefChain, idRef, maxPolicySetRefDepth);
		} else
		{
			// not a PolicySetIdReference, nothing to change in the chain
			newPolicySetRefChain = policySetRefChain;
		}

		return refPolicyProviderMod.findPolicy(idRef, refPolicyType, constraints, newPolicySetRefChain);
	}

	@Override
	public void close() throws IOException
	{
		this.refPolicyProviderMod.close();
	}
}
