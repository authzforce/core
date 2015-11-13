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
import org.ow2.authzforce.core.policy.RefPolicyFinderModule.Factory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyFinder;

import com.sun.xacml.ParsingException;
import com.sun.xacml.VersionConstraints;

/**
 * This class is used by the PDP to find policies referenced by Policy(Set)IdReference used in evaluation.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for
 * retrieving policies. Therefore, these resources must be released by calling {@link #close()} when it is no longer needed.
 */
public class BaseRefPolicyFinder implements Closeable, RefPolicyFinder
{
	/*
	 * Max PolicySet Reference depth. As there might be a need to use multiple ref policy finder modules in the future, we need to keep this global max as the
	 * global RefPolicyFinder's field.
	 */
	private final int maxPolicySetRefDepth;

	private final RefPolicyFinderModule refPolicyFinderMod;

	/**
	 * Creates RefPolicyFinder instance
	 * 
	 * @param refPolicyFinderMod
	 *            referenced Policy finder module (supports Policy(Set)IdReferences)
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1 -> PolicySet2 -> ...
	 * 
	 */
	private BaseRefPolicyFinder(RefPolicyFinderModule refPolicyFinderMod, int maxPolicySetRefDepth)
	{
		this.refPolicyFinderMod = refPolicyFinderMod;
		if (maxPolicySetRefDepth < 0)
		{
			throw new IllegalArgumentException("Invalid max PolicySet Reference depth: " + maxPolicySetRefDepth + ". Required: >= 0");
		}

		this.maxPolicySetRefDepth = maxPolicySetRefDepth;
	}

	/**
	 * Creates RefPolicyFinder instance
	 * 
	 * @param refPolicyFinderModFactory
	 *            refPolicyFinder module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyFinder}
	 * @param jaxbRefPolicyFinder
	 *            XML/JAXB configuration of RefPolicyFinder module
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1 -> PolicySet2 -> ...
	 * @param expressionFactory
	 *            Expression factory for parsing XACML Expressions in the policies
	 * @param combiningAlgRegistry
	 *            Combining algorithm registry for getting implementations of algorithms used in the policies
	 */
	public BaseRefPolicyFinder(AbstractPolicyFinder jaxbRefPolicyFinder, Factory<AbstractPolicyFinder> refPolicyFinderModFactory,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, int maxPolicySetRefDepth)
	{
		this(refPolicyFinderModFactory.getInstance(jaxbRefPolicyFinder, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry), maxPolicySetRefDepth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyFinder#isStatic()
	 */
	@Override
	public boolean isStatic()
	{
		return refPolicyFinderMod.isStatic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyFinder#findPolicy(java.lang.String, com.sun.xacml.VersionConstraints, java.lang.Class, java.util.Deque)
	 */
	@Override
	public <T extends IPolicyEvaluator> T findPolicy(String idRef, VersionConstraints constraints, Class<T> refPolicyType, Deque<String> policySetRefChain)
			throws ParsingException, IndeterminateEvaluationException
	{
		if (refPolicyFinderMod == null)
		{
			throw new IndeterminateEvaluationException("No RefPolicyFinder defined to resolve any Policy(Set)IdReference", StatusHelper.STATUS_PROCESSING_ERROR);
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

		return refPolicyFinderMod.findPolicy(idRef, refPolicyType, constraints, newPolicySetRefChain);
	}

	@Override
	public void close() throws IOException
	{
		this.refPolicyFinderMod.close();
	}
}
