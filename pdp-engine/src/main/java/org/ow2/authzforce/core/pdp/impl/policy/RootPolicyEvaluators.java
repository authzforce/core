/*
 * Copyright 2012-2024 THALES.
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

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.policy.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * {@link RootPolicyEvaluator} implementations
 * 
 * @version $Id: $
 */
public final class RootPolicyEvaluators
{
	/**
	 * 
	 * @param <PE> type of top-level policy evaluator to be returned
	 * @param rootPolicyProvider root policy provider
	 * @param rootPolicyElementType
	 *            type of policy element (XACML Policy or XACML PolicySet); if undefined, try with XACML Policy first, and if fails, try XACML PolicySet
	 * @param rootPolicyId root policy ID
	 * @param optRootPolicyVersionPatterns optional root policy version pattern to be matched
	 * @param context individual decision request evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used.
	 * @param logger logger
	 * @return instance of the policy evaluator for the root policy
	 * @throws IllegalArgumentException the resolved policy is invalid
	 * @throws IndeterminateEvaluationException error in the root policy provider trying to get the matching root policy
	 */
	private static <PE extends TopLevelPolicyElementEvaluator> PE getRootPolicyEvaluator(final CloseablePolicyProvider<PE> rootPolicyProvider,
	        final Optional<TopLevelPolicyElementType> rootPolicyElementType, final String rootPolicyId, final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns,
	        final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final Logger logger) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		if (rootPolicyElementType.isPresent())
		{
			return rootPolicyProvider.get(rootPolicyElementType.get(), rootPolicyId, optRootPolicyVersionPatterns, null, context, mdpContext);
		}

		final PE xacmlPolicyEvaluator = rootPolicyProvider.get(TopLevelPolicyElementType.POLICY, rootPolicyId, optRootPolicyVersionPatterns, null, context, mdpContext);
		if (xacmlPolicyEvaluator != null)
		{
			logger.debug("Root policy element type undefined. Searched for XACML Policy first and found.");
			return xacmlPolicyEvaluator;
		}

		logger.debug("Root policy element type undefined. Searched for XACML Policy, not found. Searching for XACML PolicySet...");
		return rootPolicyProvider.get(TopLevelPolicyElementType.POLICY_SET, rootPolicyId, optRootPolicyVersionPatterns, null, context, mdpContext);
	}

	/**
	 * Root Policy Evaluator base implementation.
	 */
	public static class Base implements RootPolicyEvaluator
	{
		private static final IllegalArgumentException NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Root Policy Provider (rootPolicyProvider)");

		private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);

		private final CloseablePolicyProvider<?> rootPolicyProvider;

		private final Optional<TopLevelPolicyElementType> rootPolicyElementType;
		private final String rootPolicyId;
		private final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns;

		private transient final boolean isRootPolicyProviderStatic;

		private transient volatile StaticView staticView = null;

		/**
		 * Creates a root policy evaluator. If you want static resolution, i.e. use the same constant root policy (resolved at initialization time) for all evaluations, use the static root policy
		 * Provider provided by {@link #toStatic()} after calling this constructor; then {@link #close()} this instance.
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
		        final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns) throws IllegalArgumentException
		{
			if (policyProvider == null)
			{
				throw NULL_ROOTPOLICYPROVIDER_ARGUMENT_EXCEPTION;
			}

			this.rootPolicyProvider = policyProvider;
			this.isRootPolicyProviderStatic = rootPolicyProvider instanceof StaticPolicyProvider;
			this.rootPolicyElementType = rootPolicyElementType;
			this.rootPolicyId = rootPolicyId;
			this.optRootPolicyVersionPatterns = optRootPolicyVersionPatterns;
		}

		@Override
		public void close() throws IOException
		{
			this.rootPolicyProvider.close();
		}

		@Override
		public DecisionResult findAndEvaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext)
		{
			final PolicyEvaluator policyEvaluator;
			try
			{
				policyEvaluator = getRootPolicyEvaluator(this.rootPolicyProvider, this.rootPolicyElementType, this.rootPolicyId, this.optRootPolicyVersionPatterns, context, mdpContext, LOGGER);
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

			return policyEvaluator.evaluate(context, mdpContext, true);
		}

		@Override
		public FlattenedPolicyTree getStaticApplicablePolicies()
		{
			return staticView == null ? null : staticView.getStaticApplicablePolicies();
		}

		/**
		 * Gets the static version of this policy evaluator, i.e. a policy evaluator using the same constant root policy resolved by the internal root policy provider (once and for all) when calling
		 * this method. This root policy will be used for all evaluations. This is possible only if the root policy provider is static, i.e. independent of the evaluation context (static
		 * resolution).
		 * 
		 * @return static view of this policy evaluator; or null if none could be created because the internal root policy provider depends on the evaluation context to find the root policy (no static
		 *         resolution is possible). If not null, this evaluator's policy provider responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext, Optional)} is closed (calling
		 *         {@link CloseableStaticPolicyProvider#close()} ) and therefore not usable anymore. The resulting static view must be used instead.
		 * @throws IOException
		 *             error closing the evaluator's policy provider responsible for finding the policy in {@link #findAndEvaluate(EvaluationContext, Optional)}
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
				staticView = new StaticView((CloseableStaticPolicyProvider) rootPolicyProvider, this.rootPolicyElementType, this.rootPolicyId, this.optRootPolicyVersionPatterns);
			}

			return staticView;
		}

	}

	/**
	 * 
	 * Static view of policy evaluator. The root policy is resolved once and for all at initialization time, and is then used for all evaluation requests.
	 *
	 */
	private static final class StaticView implements RootPolicyEvaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(StaticView.class);
		private final StaticTopLevelPolicyElementEvaluator staticRootPolicyEvaluator;
		private transient final FlattenedPolicyTree staticApplicablePolicies;

		private StaticView(final CloseableStaticPolicyProvider staticPolicyProvider, final Optional<TopLevelPolicyElementType> rootPolicyElementType, final String rootPolicyId,
		        final Optional<PolicyVersionPatterns> optRootPolicyVersionPatterns) throws IOException, IndeterminateEvaluationException
		{
			assert staticPolicyProvider != null;
			this.staticRootPolicyEvaluator = getRootPolicyEvaluator(staticPolicyProvider, rootPolicyElementType, rootPolicyId, optRootPolicyVersionPatterns, null, Optional.empty(), LOGGER);
			if (this.staticRootPolicyEvaluator == null)
			{
				throw new IllegalArgumentException("No such " + (rootPolicyElementType.isPresent() ? rootPolicyElementType.get() : "Policy(Set)") + " found: ID = '" + rootPolicyId + "'"
				        + (optRootPolicyVersionPatterns.map(policyVersionPatterns -> ", version pattern = " + policyVersionPatterns).orElse("")));
			}

			this.staticApplicablePolicies = new FlattenedPolicyTree(staticRootPolicyEvaluator.getPrimaryPolicyMetadata(), staticRootPolicyEvaluator.getPolicyRefsMetadata());

			staticPolicyProvider.close();
		}

		@Override
		public void close()
		{
			// nothing to close
		}


		@Override
		public DecisionResult findAndEvaluate(final EvaluationContext context, Optional<EvaluationContext> mdpContext)
		{
			return staticRootPolicyEvaluator.evaluate(context, mdpContext);
		}

		@Override
		public FlattenedPolicyTree getStaticApplicablePolicies()
		{
			return staticApplicablePolicies;
		}
	}
}
