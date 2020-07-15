/**
 * Copyright 2012-2020 THALES.
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
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.CloseablePolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.CloseableStaticPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.PolicyEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersionPatterns;
import org.ow2.authzforce.core.pdp.api.policy.StaticPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
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
	 * 
	 * @param <PE>
	 * @param rootPolicyProvider
	 * @param rootPolicyElementType
	 *            type of policy element (XACML Policy or XACML PolicySet); if undefined, try with XACML Policy first, and if fails, try XACML PolicySet
	 * @param rootPolicyId
	 * @param optRootPolicyVersionPatterns
	 * @param context
	 * @param logger
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IndeterminateEvaluationException
	 */
	private static <PE extends TopLevelPolicyElementEvaluator> PE getRootPolicyEvaluator(final CloseablePolicyProvider<PE> rootPolicyProvider,
	        final Optional<TopLevelPolicyElementType> rootPolicyElementType, final String rootPolicyId, final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns,
	        final EvaluationContext context, final Logger logger) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		if (rootPolicyElementType.isPresent())
		{
			return rootPolicyProvider.get(rootPolicyElementType.get(), rootPolicyId, optRootPolicyVersionPatterns, null, context);
		}

		final PE xacmlPolicyEvaluator = rootPolicyProvider.get(TopLevelPolicyElementType.POLICY, rootPolicyId, optRootPolicyVersionPatterns, null, context);
		if (xacmlPolicyEvaluator != null)
		{
			logger.debug("Root policy element type undefined. Searched for XACML Policy first and found.");
			return xacmlPolicyEvaluator;
		}

		logger.debug("Root policy element type undefined. Searched for XACML Policy, not found. Searching for XACML PolicySet...");
		return rootPolicyProvider.get(TopLevelPolicyElementType.POLICY_SET, rootPolicyId, optRootPolicyVersionPatterns, null, context);
	}

	/**
	 * Root Policy Evaluator base implementation.
	 */
	public static class Base implements RootPolicyEvaluator
	{
		private static final IllegalArgumentException NULL_EXPRESSIONFACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XACML Expression parser/factory (expressionFactory)");
		private static final IllegalArgumentException NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Root Policy Provider (rootPolicyProvider)");

		private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);

		private final CloseablePolicyProvider<?> rootPolicyProvider;

		private final Optional<TopLevelPolicyElementType> rootPolicyElementType;
		private final String rootPolicyId;
		private final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns;

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
		 * @param policyProvider
		 *            Root Policy Provider - mandatory
		 * @param rootPolicyElementType
		 *            type of root policy element (XACML Policy or XACML PolicySet). If undefined, try with XACML Policy, and else (if it fails) with XACML PolicySet.
		 * @param rootPolicyId
		 *            root Policy(Set) ID
		 * @param optRootPolicyVersionPatterns
		 *            root policy version patterns to be matched
		 * 
		 * @throws java.lang.IllegalArgumentException
		 *             If {@code expressionFactory == null || rootPolicyProvider == null}
		 */
		public Base(final CloseablePolicyProvider<?> policyProvider, final Optional<TopLevelPolicyElementType> rootPolicyElementType, final String rootPolicyId,
		        final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns, final ExpressionFactory xacmlExpressionFactory) throws IllegalArgumentException
		{
			if (xacmlExpressionFactory == null)
			{
				throw NULL_EXPRESSIONFACTORY_ARGUMENT_EXCEPTION;
			}

			if (policyProvider == null)
			{
				throw NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION;
			}

			this.rootPolicyProvider = policyProvider;
			this.isRootPolicyProviderStatic = rootPolicyProvider instanceof StaticPolicyProvider;
			this.rootPolicyElementType = rootPolicyElementType;
			this.rootPolicyId = rootPolicyId;
			this.optRootPolicyVersionPatterns = optRootPolicyVersionPatterns;

			// Initialize ExpressionFactory
			this.expressionFactory = xacmlExpressionFactory;
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
			final PolicyEvaluator policyEvaluator;
			try
			{
				policyEvaluator = getRootPolicyEvaluator(this.rootPolicyProvider, this.rootPolicyElementType, this.rootPolicyId, this.optRootPolicyVersionPatterns, context, LOGGER);
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

			if (policyEvaluator == null)
			{
				return DecisionResults.SIMPLE_NOT_APPLICABLE;
			}

			return policyEvaluator.evaluate(context, true);
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
		 *         {@link CloseableStaticPolicyProvider#close()} and therefore not useable anymore. The resulting static view must be used instead.
		 * @throws IOException
		 *             error closing the evaluator's policy provider responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext)}
		 * @throws IndeterminateEvaluationException
		 *             if error resolving the policy
		 */
		public RootPolicyEvaluator toStatic() throws IOException, IndeterminateEvaluationException
		{
			/*
			 * If staticView not yet initialized and root policy provider is actually static (in which case staticView can be initialized)
			 */
			if (staticView == null && isRootPolicyProviderStatic)
			{
				staticView = new StaticView((CloseableStaticPolicyProvider) rootPolicyProvider, this.rootPolicyElementType, this.rootPolicyId, this.optRootPolicyVersionPatterns,
				        this.expressionFactory);
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
		private static final Logger LOGGER = LoggerFactory.getLogger(StaticView.class);
		private final StaticTopLevelPolicyElementEvaluator staticRootPolicyEvaluator;
		private final ExpressionFactory expressionFactory;
		private transient final FlattenedPolicyTree staticApplicablePolicies;

		private StaticView(final CloseableStaticPolicyProvider staticPolicyProvider, final Optional<TopLevelPolicyElementType> rootPolicyElementType, final String rootPolicyId,
		        final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns, final ExpressionFactory expressionFactoryForClosing) throws IOException, IndeterminateEvaluationException
		{
			assert staticPolicyProvider != null && expressionFactoryForClosing != null;
			this.expressionFactory = expressionFactoryForClosing;
			this.staticRootPolicyEvaluator = getRootPolicyEvaluator(staticPolicyProvider, rootPolicyElementType, rootPolicyId, optRootPolicyVersionPatterns, null, LOGGER);
			if (this.staticRootPolicyEvaluator == null)
			{
				throw new IllegalArgumentException("No such " + (rootPolicyElementType.isPresent() ? rootPolicyElementType.get() : "Policy(Set)") + " found: ID = '" + rootPolicyId + "'"
				        + (optRootPolicyVersionPatterns.isPresent() ? ", version pattern = " + optRootPolicyVersionPatterns.get() : ""));
			}

			this.staticApplicablePolicies = new FlattenedPolicyTree(staticRootPolicyEvaluator.getPrimaryPolicyMetadata(), staticRootPolicyEvaluator.getPolicyRefsMetadata());

			staticPolicyProvider.close();
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
