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
import java.util.List;

import org.ow2.authzforce.core.DecisionResult;
import org.ow2.authzforce.core.EvaluationContext;
import org.ow2.authzforce.core.IndeterminateEvaluationException;
import org.ow2.authzforce.core.PdpExtensionLoader;
import org.ow2.authzforce.core.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.expression.ExpressionFactory;
import org.ow2.authzforce.core.expression.ExpressionFactoryImpl;
import org.ow2.authzforce.core.func.FunctionRegistry;
import org.ow2.authzforce.core.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeFinder;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.ParsingException;

/**
 * Root policy evaluator, used by the PDP to find and evaluate the root (a.k.a. top-level) policy matching a given request context.
 * <p>
 * Implements {@link Closeable} because it may very likely hold resources such as network resources to get policies remotely, policy caches to speed up finding,
 * etc. Therefore, you are required to call {@link #close()} when you no longer need an instance - especially before replacing with a new instance (with
 * different modules) - in order to make sure these resources are released properly by each underlying module (e.g. invalidate the policy caches).
 */
public interface RootPolicyEvaluator extends Closeable
{
	/**
	 * Finds one and only one policy applicable to the given request context and evaluates the request context against it. This will always do a Target match to
	 * make sure that the given policy applies.
	 * 
	 * @param context
	 *            the representation of the request data
	 * 
	 * @return the result of evaluating the request against the applicable policy; or NotApplicable if none is applicable; or Indeterminate if error determining
	 *         an applicable policy or more than one applies or evaluation of the applicable policy returned Indeterminate Decision
	 */
	DecisionResult findAndEvaluate(EvaluationContext context);

	/**
	 * 
	 * Static view of policy finder. The root policy is resolved once and for all at initialization time, and is then used for all evaluation requests.
	 *
	 */
	class StaticView implements RootPolicyEvaluator
	{
		private final IPolicyEvaluator staticRootPolicyEvaluator;
		private final ExpressionFactory expressionFactory;

		private StaticView(RootPolicyFinderModule.Static staticFinderModule, ExpressionFactory expressionFactoryForClosing) throws IOException
		{
			assert staticFinderModule != null && expressionFactoryForClosing != null;
			this.expressionFactory = expressionFactoryForClosing;
			this.staticRootPolicyEvaluator = staticFinderModule.getRootPolicy();
			staticFinderModule.close();
		}

		@Override
		public DecisionResult findAndEvaluate(EvaluationContext context)
		{
			return staticRootPolicyEvaluator.evaluate(context);
		}

		@Override
		public void close() throws IOException
		{
			this.expressionFactory.close();
		}
	}

	/**
	 * Root Policy Finder base implementation.
	 */
	class Base implements RootPolicyEvaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);

		private final RootPolicyFinderModule rootPolicyFinderMod;

		private final ExpressionFactory expressionFactory;

		/**
		 * Creates a root policy finder. If you want static resolution, i.e. use the same constant root policy (resolved at initialization time) for all
		 * evaluations, use the static root policy finder provided by {@link #toStatic()} after calling this constructor; then {@link #close()} this instance.
		 * 
		 * @param attributeFactory
		 *            attribute value factory - mandatory
		 * @param functionRegistry
		 *            function registry - mandatory
		 * @param jaxbAttributeFinderConfs
		 *            XML/JAXB configurations of Attribute Finders for AttributeDesignator/AttributeSelector evaluation; may be null for static expression
		 *            evaluation (out of context), in which case AttributeSelectors/AttributeDesignators are not supported
		 * @param maxVariableReferenceDepth
		 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition ->... ('->' represents a VariableReference)
		 * @param allowAttributeSelectors
		 *            allow use of AttributeSelectors (experimental, not for production, use with caution)
		 * 
		 * @param jaxbRootPolicyFinderConf
		 *            (mandatory) root policy finder's XML/JAXB configuration
		 * @param combiningAlgRegistry
		 *            (mandatory) XACML policy/rule combining algorithm registry
		 * @param jaxbRefPolicyFinderConf
		 *            (optional) policy-by-reference finder's XML/JAXB configuration, for resolving policies referred to by Policy(Set)IdReference in policies
		 *            found by root policy finder
		 * @param maxPolicySetRefDepth
		 *            max allowed PolicySetIdReference chain: PolicySet1 (PolicySetIdRef1) -> PolicySet2 (PolicySetIdRef2) -> ...
		 * @throws IllegalArgumentException
		 *             if one of the mandatory arguments is null; or if any of attribute finder modules created from {@code jaxbAttributeFinderConfs} does not
		 *             provide any attribute; or it is in conflict with another one already registered to provide the same or part of the same attributes.
		 * @throws IOException
		 */
		public Base(DatatypeFactoryRegistry attributeFactory, FunctionRegistry functionRegistry, List<AbstractAttributeFinder> jaxbAttributeFinderConfs,
				int maxVariableReferenceDepth, boolean allowAttributeSelectors, CombiningAlgRegistry combiningAlgRegistry,
				AbstractPolicyFinder jaxbRootPolicyFinderConf, AbstractPolicyFinder jaxbRefPolicyFinderConf, int maxPolicySetRefDepth)
				throws IllegalArgumentException, IOException
		{
			if (jaxbRootPolicyFinderConf == null || combiningAlgRegistry == null)
			{
				throw new IllegalArgumentException(
						"Invalid arguments to root policy finder creation: missing one of these args: root policy finder's XML/JAXB configuration (jaxbRootPolicyFinderConf), XACML Expression parser/factory (expressionFactory), combining algorithm registry (combiningAlgRegistry)");
			}

			// Initialize ExpressionFactory
			this.expressionFactory = new ExpressionFactoryImpl(attributeFactory, functionRegistry, jaxbAttributeFinderConfs, maxVariableReferenceDepth,
					allowAttributeSelectors);

			final RootPolicyFinderModule.Factory<AbstractPolicyFinder> rootPolicyFinderModFactory = PdpExtensionLoader.getJaxbBoundExtension(
					RootPolicyFinderModule.Factory.class, jaxbRootPolicyFinderConf.getClass());

			rootPolicyFinderMod = rootPolicyFinderModFactory.getInstance(jaxbRootPolicyFinderConf, this.expressionFactory, combiningAlgRegistry,
					jaxbRefPolicyFinderConf, maxPolicySetRefDepth);
		}

		@Override
		public DecisionResult findAndEvaluate(EvaluationContext context)
		{
			final IPolicyEvaluator policy;
			try
			{
				policy = rootPolicyFinderMod.findPolicy(context);
			} catch (IndeterminateEvaluationException e)
			{
				LOGGER.info("Error finding applicable root policy to evaluate with root policy finder module {}", rootPolicyFinderMod, e);
				return new DecisionResult(e.getStatus());
			} catch (ParsingException e)
			{
				LOGGER.warn("Error parsing one of the possible root policies (handled by root policy finder module {})", rootPolicyFinderMod, e);
				return e.getIndeterminateResult();
			}

			if (policy == null)
			{
				return DecisionResult.NOT_APPLICABLE;
			}

			return policy.evaluate(context, true);
		}

		@Override
		public void close() throws IOException
		{
			this.expressionFactory.close();
			this.rootPolicyFinderMod.close();
		}

		/**
		 * Gets the static version of this policy finder, i.e. a policy finder using the same constant root policy resolved by this finder (once and for all)
		 * when calling this method. This root policy will be used for all evaluations. This is possible only for finders independent from the evaluation
		 * context (static resolution).
		 * 
		 * @return static view of this policy finder; or null if none could be created because the finder depends on the evaluation context to find the root
		 *         policy (no static resolution is possible). If not null, this finder's sub-module responsible for finding the policy in
		 *         {@link #findAndEvaluate(EvaluationContext)} is closed (calling {@link RootPolicyFinderModule#close()} and therefore not useable anymore. The
		 *         resulting static view must be used instead.
		 * @throws IOException
		 *             error closing the finder's sub-module responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext)}
		 */
		public RootPolicyEvaluator toStatic() throws IOException
		{
			if (rootPolicyFinderMod instanceof RootPolicyFinderModule.Static)
			{
				return new StaticView((RootPolicyFinderModule.Static) rootPolicyFinderMod, this.expressionFactory);
			}

			return null;
		}

	}
}
