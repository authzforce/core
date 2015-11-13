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

import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.PdpExtensionLoader;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyFinder;

import com.sun.xacml.ParsingException;

/**
 * This is the interface that all modules responsible for finding the root/top-level policy to evaluate.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for
 * retrieving the root policy and any policy referenced by it. Therefore, these resources must be released by calling {@link #close()} when it is no longer
 * needed.
 * 
 */
public abstract class RootPolicyFinderModule implements Closeable
{
	protected final BaseRefPolicyFinder refPolicyFinder;

	/**
	 * Creates instance
	 * 
	 * @param expressionFactory
	 *            (mandatory) Expression factory
	 * @param refPolicyFinder
	 *            referenced policy finder; null iff Policy references not supported
	 * @param combiningAlgRegistry
	 *            (mandatory) registry of policy/rule combining algorithms
	 * @param jaxbRefPolicyFinderConf
	 *            (optional) XML/JAXB configuration of RefPolicyFinder module used for resolving Policy(Set)(Id)References in root policy; may be null if
	 *            support of PolicyReferences is disabled or this RootPolicyFinder module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyFinder derived from
	 *            {@code jaxbRefPolicyFinderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyFinderConf == null}, this parameter is ignored.
	 */
	protected RootPolicyFinderModule(ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry,
			AbstractPolicyFinder jaxbRefPolicyFinderConf, int maxPolicySetRefDepth)
	{
		if (expressionFactory == null)
		{
			throw new IllegalArgumentException("Undefined Expression factory");
		}

		if (combiningAlgRegistry == null)
		{
			throw new IllegalArgumentException("Undefined CombiningAlgorithm registry");
		}

		// create ref-policy finder
		if (jaxbRefPolicyFinderConf == null)
		{
			this.refPolicyFinder = null;
		} else
		{
			final RefPolicyFinderModule.Factory<AbstractPolicyFinder> refPolicyFinderModFactory = PdpExtensionLoader.getJaxbBoundExtension(
					RefPolicyFinderModule.Factory.class, jaxbRefPolicyFinderConf.getClass());
			this.refPolicyFinder = new BaseRefPolicyFinder(jaxbRefPolicyFinderConf, refPolicyFinderModFactory, expressionFactory, combiningAlgRegistry,
					maxPolicySetRefDepth);
		}
	}

	/**
	 * RootPolicyFinderModule factory
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module (initialization parameter)
	 * 
	 */
	public static abstract class Factory<CONF_T extends AbstractPolicyFinder> extends JaxbBoundPdpExtension<CONF_T>
	{
		/**
		 * Create RootPolicyFinderModule instance
		 * 
		 * @param conf
		 *            module configuration
		 * @param expressionFactory
		 *            Expression factory for parsing Expressions in the root policy(set)
		 * @param combiningAlgRegistry
		 *            registry of combining algorithms for instantiating algorithms used in the root policy(set) *
		 * @param jaxbRefPolicyFinderConf
		 *            XML/JAXB configuration of RefPolicyFinder module used for resolving Policy(Set)(Id)References in root policy; may be null if support of
		 *            PolicyReferences is disabled or this RootPolicyFinder module already supports these.
		 * @param maxPolicySetRefDepth
		 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyFinder derived from
		 *            {@code jaxbRefPolicyFinderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyFinderConf == null}, this parameter is ignored.
		 * 
		 * @return the module instance
		 */
		public abstract RootPolicyFinderModule getInstance(CONF_T conf, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry,
				AbstractPolicyFinder jaxbRefPolicyFinderConf, int maxPolicySetRefDepth);
	}

	/**
	 * Tries to find one and only one matching policy given the request represented by the context data. If no policies are found, null must be returned.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of looking for a matching policy, null if none found matching the request
	 * @throws ParsingException
	 *             Error parsing a policy before matching. The policy finder module may parse policies lazily or on the fly, i.e. only when the policies are
	 *             requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining the one policy matching the {@code context}, e.g. if more than one policy is found
	 */
	public abstract IPolicyEvaluator findPolicy(EvaluationContext context) throws IndeterminateEvaluationException, ParsingException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.policy.RootPolicyFinderModule#invalidateCache()
	 */
	@Override
	public void close() throws IOException
	{
		if (refPolicyFinder != null)
		{
			refPolicyFinder.close();
		}
	}

	/**
	 * Root policy finder module that resolves statically the root policy when it is initialized, i.e. it is context-independent. Concretely, this means for any
	 * given context:
	 * 
	 * <pre>
	 * this.findPolicy(context) == this.findPolicy(null)
	 * </pre>
	 */
	public static abstract class Static extends RootPolicyFinderModule
	{
		protected Static(ExpressionFactory defaultExpressionFactory, CombiningAlgRegistry combiningAlgRegistry, AbstractPolicyFinder jaxbRefPolicyFinderConf,
				int maxPolicySetRefDepth)
		{
			super(defaultExpressionFactory, combiningAlgRegistry, jaxbRefPolicyFinderConf, maxPolicySetRefDepth);
		}

		/**
		 * Get the statically resolved root policy
		 * 
		 * @return root policy
		 */
		public abstract IPolicyEvaluator getRootPolicy();

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.policy.RootPolicyFinderModule#findPolicy(com.thalesgroup .authzforce.core.test .EvaluationCtx)
		 */
		@Override
		public final IPolicyEvaluator findPolicy(EvaluationContext context) throws IndeterminateEvaluationException, ParsingException
		{
			return getRootPolicy();
		}

	}

}
