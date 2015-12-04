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
import org.ow2.authzforce.core.XACMLParsers.XACMLParserFactory;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

import com.sun.xacml.ParsingException;

/**
 * This is the interface that all modules responsible for providing the root/top-level policy to evaluate.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for
 * retrieving the root policy and any policy referenced by it. Therefore, these resources must be released by calling {@link #close()} when it is no longer
 * needed.
 * 
 */
public abstract class RootPolicyProviderModule implements Closeable
{
	protected final BaseRefPolicyProvider refPolicyProvider;

	/**
	 * Creates instance
	 * 
	 * @param expressionFactory
	 *            (mandatory) Expression factory
	 * @param refPolicyProvider
	 *            referenced policy Provider; null iff Policy references not supported
	 * @param combiningAlgRegistry
	 *            (mandatory) registry of policy/rule combining algorithms
	 * @param jaxbRefPolicyProviderConf
	 *            (optional) XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in root policy; may be null if
	 *            support of PolicyReferences is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
	 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param xacmlParserFactory
	 *            XACML Parser Factory
	 * @throws IllegalArgumentException
	 *             if {@code jaxbRefPolicyProviderConf != null && (expressionFactory == null || combiningAlgRegistry == null || xacmlParserFactory == null)}
	 */
	protected RootPolicyProviderModule(AbstractPolicyProvider jaxbRefPolicyProviderConf, XACMLParserFactory xacmlParserFactory,
			ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, int maxPolicySetRefDepth) throws IllegalArgumentException
	{
		// create ref-policy Provider
		if (jaxbRefPolicyProviderConf == null)
		{
			this.refPolicyProvider = null;
		} else
		{
			final RefPolicyProviderModule.Factory<AbstractPolicyProvider> refPolicyProviderModFactory = PdpExtensionLoader.getJaxbBoundExtension(
					RefPolicyProviderModule.Factory.class, jaxbRefPolicyProviderConf.getClass());
			this.refPolicyProvider = new BaseRefPolicyProvider(jaxbRefPolicyProviderConf, refPolicyProviderModFactory, xacmlParserFactory, expressionFactory,
					combiningAlgRegistry, maxPolicySetRefDepth);
		}
	}

	/**
	 * RootPolicyProviderModule factory
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module (initialization parameter)
	 * 
	 */
	public static abstract class Factory<CONF_T extends AbstractPolicyProvider> extends JaxbBoundPdpExtension<CONF_T>
	{
		/**
		 * Create RootPolicyProviderModule instance
		 * 
		 * @param conf
		 *            module configuration
		 * @param xacmlParserFactory
		 *            XACML parser factory for parsing any XACML Policy(Set)
		 * @param expressionFactory
		 *            Expression factory for parsing Expressions in the root policy(set)
		 * @param combiningAlgRegistry
		 *            registry of combining algorithms for instantiating algorithms used in the root policy(set) *
		 * @param jaxbRefPolicyProviderConf
		 *            XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in root policy; may be null if support of
		 *            PolicyReferences is disabled or this RootPolicyProvider module already supports these.
		 * @param maxPolicySetRefDepth
		 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
		 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is
		 *            ignored.
		 * 
		 * @return the module instance
		 */
		public abstract RootPolicyProviderModule getInstance(CONF_T conf, XACMLParserFactory xacmlParserFactory, ExpressionFactory expressionFactory,
				CombiningAlgRegistry combiningAlgRegistry, AbstractPolicyProvider jaxbRefPolicyProviderConf, int maxPolicySetRefDepth);
	}

	/**
	 * Tries to find one and only one matching policy given the request represented by the context data. If no policies are found, null must be returned.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of looking for a matching policy, null if none found matching the request
	 * @throws ParsingException
	 *             Error parsing a policy before matching. The policy Provider module may parse policies lazily or on the fly, i.e. only when the policies are
	 *             requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining the one policy matching the {@code context}, e.g. if more than one policy is found
	 */
	public abstract IPolicyEvaluator findPolicy(EvaluationContext context) throws IndeterminateEvaluationException, ParsingException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.policy.RootPolicyProviderModule#invalidateCache()
	 */
	@Override
	public void close() throws IOException
	{
		if (refPolicyProvider != null)
		{
			refPolicyProvider.close();
		}
	}

	/**
	 * Root policy Provider module that resolves statically the root policy when it is initialized, i.e. it is context-independent. Concretely, this means for
	 * any given context:
	 * 
	 * <pre>
	 * this.findPolicy(context) == this.findPolicy(null)
	 * </pre>
	 */
	public static abstract class Static extends RootPolicyProviderModule
	{
		protected Static(AbstractPolicyProvider jaxbRefPolicyProviderConf, XACMLParserFactory xacmlParserFactory, ExpressionFactory defaultExpressionFactory,
				CombiningAlgRegistry combiningAlgRegistry, int maxPolicySetRefDepth)
		{
			super(jaxbRefPolicyProviderConf, xacmlParserFactory, defaultExpressionFactory, combiningAlgRegistry, maxPolicySetRefDepth);
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
		 * @see com.thalesgroup.authzforce.core.policy.RootPolicyProviderModule#findPolicy(com.thalesgroup .authzforce.core.test .EvaluationCtx)
		 */
		@Override
		public final IPolicyEvaluator findPolicy(EvaluationContext context) throws IndeterminateEvaluationException, ParsingException
		{
			return getRootPolicy();
		}

	}

}
