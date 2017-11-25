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

import java.io.IOException;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.RootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticRootPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RootPolicyEvaluator} implementations
 *
 * 
 * @version $Id: $
 */
public final class RootPolicyEvaluators
{

	/**
	 * Root Policy Evaluator base implementation.
	 */
	public static class Base implements RootPolicyEvaluator
	{
		private static final IllegalArgumentException NULL_EXPRESSIONFACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XACML Expression parser/factory (expressionFactory)");
		private static final IllegalArgumentException NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Root Policy Provider (rootPolicyProvider)");

		private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);

		private final RootPolicyProvider rootPolicyProvider;

		private transient final ExpressionFactory expressionFactory;

		private transient final boolean isRootPolicyProviderStatic;

		private transient volatile StaticView staticView = null;

		/**
		 * Creates a root policy evaluator. If you want static resolution, i.e. use the same constant root policy (resolved at initialization time) for all evaluations, use the static root policy
		 * Provider provided by {@link #toStatic()} after calling this constructor; then {@link #close()} this instance.
		 * 
		 * @param xacmlExpressionFactory
		 *            XACML expression factory
		 * 
		 * @param rootPolicyProvider
		 *            Root Policy Provider - mandatory
		 * 
		 * @throws java.lang.IllegalArgumentException
		 *             If {@code expressionFactory == null || rootPolicyProvider == null}
		 */
		public Base(final ExpressionFactory xacmlExpressionFactory, final RootPolicyProvider rootPolicyProvider) throws IllegalArgumentException
		{
			if (xacmlExpressionFactory == null)
			{
				throw NULL_EXPRESSIONFACTORY_ARGUMENT_EXCEPTION;
			}

			if (rootPolicyProvider == null)
			{
				throw NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION;
			}

			// Initialize ExpressionFactory
			this.expressionFactory = xacmlExpressionFactory;

			this.rootPolicyProvider = rootPolicyProvider;
			this.isRootPolicyProviderStatic = rootPolicyProvider instanceof StaticRootPolicyProvider;
		}

		@Override
		public void close() throws IOException
		{
			this.expressionFactory.close();
			this.rootPolicyProvider.close();
		}

		@Override
		public DecisionResult findAndEvaluate(final EvaluationContext context)
		{
			final PolicyEvaluator policy;
			try
			{
				policy = rootPolicyProvider.getPolicy(context);
			}
			catch (final IndeterminateEvaluationException e)
			{
				LOGGER.info("Root policy Provider {} could not find an applicable root policy to evaluate", rootPolicyProvider, e);
				return DecisionResults.newIndeterminate(null, e, null);
			}
			catch (final IllegalArgumentException e)
			{
				LOGGER.warn("One of the possible root policies (resolved by the root policy provider {}) is invalid", rootPolicyProvider, e);
				return DecisionResults.newIndeterminate(null, new IndeterminateEvaluationException(e.getMessage(), XacmlStatusCode.PROCESSING_ERROR.value()), null);
			}

			if (policy == null)
			{
				return DecisionResults.SIMPLE_NOT_APPLICABLE;
			}

			return policy.evaluate(context, true);
		}

		@Override
		public FlattenedPolicyTree getStaticApplicablePolicies()
		{
			return staticView == null ? null : staticView.getStaticApplicablePolicies();
		}

		/**
		 * Gets the static version of this policy evaluator, i.e. a policy evaluator using the same constant root policy resolved by the internal root policy provider (once and for all) when calling
		 * this method. This root policy will be used for all evaluations. This is possible only if the root policy provider is static, i.e. independent from the evaluation context (static
		 * resolution).
		 * 
		 * @return static view of this policy evaluator; or null if none could be created because the internal root policy provider depends on the evaluation context to find the root policy (no static
		 *         resolution is possible). If not null, this evaluator's policy provider responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext)} is closed (calling
		 *         {@link RootPolicyProvider#close()} and therefore not useable anymore. The resulting static view must be used instead.
		 * @throws IOException
		 *             error closing the evaluator's policy provider responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext)}
		 */
		public RootPolicyEvaluator toStatic() throws IOException
		{
			/*
			 * If staticView not yet initialized and root policy provider is actually static (in which case staticView can be initialized)
			 */
			if (staticView == null && isRootPolicyProviderStatic)
			{
				staticView = new StaticView((StaticRootPolicyProvider) rootPolicyProvider, this.expressionFactory);
			}

			return staticView;
		}

	}

	/**
	 * 
	 * Static view of policy evaluator. The root policy is resolved once and for all at initialization time, and is then used for all evaluation requests.
	 *
	 */
	static class StaticView implements RootPolicyEvaluator
	{
		private final StaticTopLevelPolicyElementEvaluator staticRootPolicyEvaluator;
		private final ExpressionFactory expressionFactory;
		private transient final FlattenedPolicyTree staticApplicablePolicies;

		private StaticView(final StaticRootPolicyProvider staticProvider, final ExpressionFactory expressionFactoryForClosing) throws IOException
		{
			assert staticProvider != null && expressionFactoryForClosing != null;
			this.expressionFactory = expressionFactoryForClosing;
			this.staticRootPolicyEvaluator = staticProvider.getPolicy();
			this.staticApplicablePolicies = new FlattenedPolicyTree(staticRootPolicyEvaluator.getPrimaryPolicyMetadata(), staticRootPolicyEvaluator.getPolicyRefsMetadata());

			staticProvider.close();
		}

		@Override
		public void close() throws IOException
		{
			this.expressionFactory.close();
		}

		@Override
		public DecisionResult findAndEvaluate(final EvaluationContext context)
		{
			return staticRootPolicyEvaluator.evaluate(context);
		}

		@Override
		public FlattenedPolicyTree getStaticApplicablePolicies()
		{
			return staticApplicablePolicies;
		}
	}
}
