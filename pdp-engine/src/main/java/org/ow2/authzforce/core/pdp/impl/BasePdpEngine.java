/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.impl;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.policy.CloseablePolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersionPatterns;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluators;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;

/**
 * This is the core XACML PDP engine implementation.
 *
 * @version $Id: $
 */
public final class BasePdpEngine implements CloseablePdpEngine
{
	private static final IllegalArgumentException NULL_REQUEST_ARGUMENT_EXCEPTION = new IllegalArgumentException("No input Decision Request");
	private static final IllegalArgumentException NULL_MDP_CTX_ARGUMENT_EXCEPTION = new IllegalArgumentException("No input Multiple Decision Request context");

	private static final Logger LOGGER = LoggerFactory.getLogger(BasePdpEngine.class);

	private static class NonIssuedLikeIssuedAttributeHandlingRequestBuilder implements DecisionRequestBuilder<ImmutableDecisionRequest>
	{
		private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;
		private final Map<String, XdmNode> extraContentsByCategory;

		private NonIssuedLikeIssuedAttributeHandlingRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
		{
			namedAttributes = expectedTotalNumOfAttributes < 0 ? HashCollections.newUpdatableMap() : HashCollections.newUpdatableMap(expectedTotalNumOfAttributes);
			extraContentsByCategory = expectedNumOfAttributeCategories < 0 ? HashCollections.newUpdatableMap() : HashCollections.newUpdatableMap(expectedNumOfAttributeCategories);
		}

		@Override
		public Bag<?> putNamedAttributeIfAbsent(final AttributeFqn attributeId, final AttributeBag<?> attributeValues)
		{
			return namedAttributes.putIfAbsent(attributeId, attributeValues);
		}

		@Override
		public final XdmNode putContentIfAbsent(final String category, final XdmNode content)
		{
			return extraContentsByCategory.putIfAbsent(category, content);
		}

		@Override
		public final ImmutableDecisionRequest build(final boolean returnApplicablePolicies)
		{
			return ImmutableDecisionRequest.getInstance(namedAttributes, extraContentsByCategory, returnApplicablePolicies);
		}

		@Override
		public final void reset()
		{
			namedAttributes.clear();
			extraContentsByCategory.clear();
		}
	}

	private static final class IssuedToNonIssuedAttributeCopyingRequestBuilder extends NonIssuedLikeIssuedAttributeHandlingRequestBuilder
	{
		private IssuedToNonIssuedAttributeCopyingRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
		{
			/*
			 * Since copying attributes with non-null Issuer to equivalent attributes but without Issuer, we'll get at most twice as many attributes as the ones put by the caller
			 */
			super(expectedNumOfAttributeCategories, 2 * expectedTotalNumOfAttributes);
		}

		@Override
		public Bag<?> putNamedAttributeIfAbsent(final AttributeFqn attributeFqn, final AttributeBag<?> attributeValues)
		{
			assert attributeFqn != null;

			/*
			 * Put the non-issued version of the attribute first
			 */
			final AttributeFqn nonAttributeFqn = AttributeFqns.newInstance(attributeFqn.getCategory(), Optional.empty(), attributeFqn.getId());
			super.putNamedAttributeIfAbsent(nonAttributeFqn, attributeValues);
			return super.putNamedAttributeIfAbsent(attributeFqn, attributeValues);
		}
	}

	/**
	 * Individual decision request evaluator
	 *
	 * @version $Id: $
	 */
	private static abstract class IndividualDecisionRequestEvaluator
	{

		private final RootPolicyEvaluator rootPolicyEvaluator;
		private final Optional<CloseableNamedAttributeProviderRegistry> attProviders;

		/**
		 * Creates an evaluator
		 *
		 * @param rootPolicyEvaluator
		 *            root policy evaluator that this request evaluator uses to evaluate individual decision request
		 * @param attributeProviders attribute providers
		 * @throws IllegalArgumentException
		 *             if {@code stdEnvAttributeSource} is null or not supported
		 */
		protected IndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, Optional<CloseableNamedAttributeProviderRegistry> attributeProviders) throws IllegalArgumentException
		{
			assert rootPolicyEvaluator != null;
			this.rootPolicyEvaluator = rootPolicyEvaluator;
			this.attProviders = attributeProviders;
		}

		protected static final EvaluationContext newEvaluationContext(final DecisionRequest request)
		{
			assert request != null;
			return new IndividualDecisionRequestContext(request.getNamedAttributes(), request.getExtraContentsByCategory(), request.isApplicablePolicyIdListReturned(), Optional.of(request.getCreationTimestamp()));
		}

		protected final DecisionResult evaluateInNewContext(final DecisionRequest request, final Optional<EvaluationContext> mdpCtx)
		{
			assert request != null;
			final EvaluationContext evalCtx = newEvaluationContext(request);
			if(this.attProviders.isPresent())
			{
					try
					{
						this.attProviders.get().beginIndividualDecisionRequest(evalCtx, mdpCtx);
					} catch (IndeterminateEvaluationException e)
					{
						LOGGER.error("Error calling one of the AttributeProvider's beginIndividualDecisionRequest(...)", e);
						return DecisionResults.newIndeterminate(DecisionType.NOT_APPLICABLE, e, null);
					}
			}

			return rootPolicyEvaluator.findAndEvaluate(evalCtx, mdpCtx);
		}

		/**
		 * <p>
		 * Evaluate Individual Decision Request in an existing request context
		 * </p>
		 *
		 * @param evalCtx
		 *            existing Individual Decision evaluation context
		 * @param mdpCtx
		 * 	 the context of the Multiple Decision request that the {@code evalCtx} belongs to if the Multiple Decision Profile is used.
		 * @return the evaluation result.
		 */
		protected final DecisionResult evaluateReusingContext(final EvaluationContext evalCtx, final Optional<EvaluationContext> mdpCtx)
		{
			if(this.attProviders.isPresent())
			{
				try
				{
					this.attProviders.get().beginIndividualDecisionRequest(evalCtx, mdpCtx);
				} catch (IndeterminateEvaluationException e)
				{
					LOGGER.error("Error calling one of the AttributeProvider's beginIndividualDecisionRequest(...)", e);
					return DecisionResults.newIndeterminate(DecisionType.NOT_APPLICABLE, e, null);
				}
			}

			return rootPolicyEvaluator.findAndEvaluate(evalCtx, mdpCtx);
		}

		/**
		 * <p>
		 * Evaluate an Individual Decision Request from which a new request context is created to evaluate the request
		 * </p>
		 *
		 * @param individualDecisionRequest
		 *            a non-null {@link DecisionRequest} object, i.e. representation of Individual Decision Request (as defined by Multiple Decision Profile of XACML).
		 * @return the evaluation result.
		 */
		protected abstract DecisionResult evaluate(final DecisionRequest individualDecisionRequest);

		/**
		 * <p>
		 * Evaluate multiple Individual Decision Requests with same PDP-issued attribute values (e.g. current date/time) in order to return decision results in internal model.
		 * </p>
		 *
		 * @param individualDecisionRequests
		 *            a {@link java.util.List} of individual decision requests.
		 * @param mdpCtx
		 * 	 the context of the Multiple Decision request that the {@code individualDecisionRequests} belong to
		 * @return individual decision request-result pairs, where the list of the requests is the same as {@code individualDecisionRequests}.
		 * @throws IndeterminateEvaluationException
		 *             if an error occurred preventing any request evaluation
		 */
		protected abstract <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
		        List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final EvaluationContext mdpCtx) throws IndeterminateEvaluationException;

	}

	private static final class NonCachingIndividualDecisionRequestEvaluator extends IndividualDecisionRequestEvaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(NonCachingIndividualDecisionRequestEvaluator.class);

		private static final RuntimeException NULL_INDIVIDUAL_DECISION_REQUEST_EXCEPTION = new RuntimeException(
		        "One of the individual decision requests returned by the request filter is invalid (null).");

		private NonCachingIndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, Optional<CloseableNamedAttributeProviderRegistry> attributeProviders)
		{
			super(rootPolicyEvaluator, attributeProviders);
		}

		@Override
		protected DecisionResult evaluate(final DecisionRequest request)
		{
			assert request != null;
			LOGGER.debug("Evaluating Individual Decision Request: {}", request);
			return evaluateInNewContext(request, Optional.empty());
		}

		@Override
		protected <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
		        final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final EvaluationContext mdpContext)
		{
			assert individualDecisionRequests != null && mdpContext != null;

			final Optional<EvaluationContext> optEvalCtx = Optional.of(mdpContext);
			final Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> resultsByRequest = new ArrayDeque<>(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				if (individualDecisionRequest == null)
				{
					throw NULL_INDIVIDUAL_DECISION_REQUEST_EXCEPTION;
				}

				final DecisionResult decisionResult = evaluateInNewContext(individualDecisionRequest, optEvalCtx);
				resultsByRequest.add(new SimpleImmutableEntry<>(individualDecisionRequest, decisionResult));
			}

			return resultsByRequest;
		}

	}

	private static final class IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext extends IndividualDecisionRequestEvaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext.class);

		private static final IndeterminateEvaluationException INDETERMINATE_EVALUATION_EXCEPTION = new IndeterminateEvaluationException("Internal error in decision cache: null result",
		        XacmlStatusCode.PROCESSING_ERROR.value());

		private final DecisionCache decisionCache;

		private IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext(final RootPolicyEvaluator rootPolicyEvaluator,
																			 final DecisionCache decisionCache, final Optional<CloseableNamedAttributeProviderRegistry> attributeProviders)
		{
			super(rootPolicyEvaluator, attributeProviders);
			assert decisionCache != null;
			this.decisionCache = decisionCache;
		}

		@Override
		protected DecisionResult evaluate(final DecisionRequest individualDecisionRequest)
		{
			assert individualDecisionRequest != null;
			LOGGER.debug("Evaluating Individual Decision Request: {}", individualDecisionRequest);
			final DecisionResult cachedResult = decisionCache.get(individualDecisionRequest, null);
			if (cachedResult == null)
			{
				LOGGER.debug("No result found in cache for Individual Decision Request: {}. Computing new result from policy evaluation...", individualDecisionRequest);
				final DecisionResult newResult = evaluateInNewContext(individualDecisionRequest, Optional.empty());
				LOGGER.debug("Caching new Result for Individual Decision Request: {} -> {}", individualDecisionRequest, newResult);
				decisionCache.put(individualDecisionRequest, newResult, null);
				return newResult;
			}

			LOGGER.debug("Result found in cache for Individual Decision Request: {} -> {}", individualDecisionRequest, cachedResult);
			return cachedResult;
		}

		@Override
		protected <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
		        final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final EvaluationContext mdpContext) throws IndeterminateEvaluationException
		{
			assert individualDecisionRequests != null && mdpContext != null;

			final Map<INDIVIDUAL_DECISION_REQ_T, DecisionResult> cachedResultsByRequest = decisionCache.getAll(individualDecisionRequests);
			if (cachedResultsByRequest == null)
			{
				// error, return indeterminate result as only result
				LOGGER.error("Invalid decision cache result: null");
				throw INDETERMINATE_EVALUATION_EXCEPTION;
			}

			final Optional<EvaluationContext> optEvalCtx = Optional.of(mdpContext);

			/*
			 * There will be at most as many new results (not in cache) as there are individual decision requests
			 */
			final Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> finalResultsByRequest = new ArrayDeque<>(individualDecisionRequests.size());
			final Map<INDIVIDUAL_DECISION_REQ_T, DecisionResult> newResultsByRequest = HashCollections.newUpdatableMap(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				LOGGER.debug("Evaluating Individual Decision Request: {}", individualDecisionRequest);
				final DecisionResult finalResult;
				/*
				 * Check whether there is any decision result in cache for this request
				 */
				final DecisionResult cachedResult = cachedResultsByRequest.get(individualDecisionRequest);
				if (cachedResult == null)
				{
					LOGGER.debug("No result found in cache for Individual Decision Request: {}. Computing new result from policy evaluation...", individualDecisionRequest);
					finalResult = evaluateInNewContext(individualDecisionRequest, optEvalCtx);
					LOGGER.debug("Caching new Result for Individual Decision Request: {} -> {}", individualDecisionRequest, finalResult);
					newResultsByRequest.put(individualDecisionRequest, finalResult);
				}
				else
				{

					LOGGER.debug("Result found in cache for Individual Decision Request: {} -> {}", individualDecisionRequest, cachedResult);
					finalResult = cachedResult;
				}

				finalResultsByRequest.add(new SimpleImmutableEntry<>(individualDecisionRequest, finalResult));
			}

			if (!newResultsByRequest.isEmpty())
			{
				decisionCache.putAll(newResultsByRequest);
			}

			return finalResultsByRequest;
		}

	}

	private static final class IndividualRequestEvaluatorWithCacheUsingEvaluationContext extends IndividualDecisionRequestEvaluator
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(IndividualRequestEvaluatorWithCacheUsingEvaluationContext.class);

		private final DecisionCache decisionCache;

		private IndividualRequestEvaluatorWithCacheUsingEvaluationContext(final RootPolicyEvaluator rootPolicyEvaluator,
																		  final DecisionCache decisionCache, final Optional<CloseableNamedAttributeProviderRegistry> attributeProviders)
		{
			super(rootPolicyEvaluator, attributeProviders);
			assert decisionCache != null;
			this.decisionCache = decisionCache;
		}

		private <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> DecisionResult evaluateWithDecisionCache(final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest, final Optional<EvaluationContext> mdpCtx)
		{
			assert individualDecisionRequest != null;
			LOGGER.debug("Evaluating Individual Decision Request: {}", individualDecisionRequest);
			/*
			 * Check whether there is any decision result in cache for this request
			 */
			final EvaluationContext evalCtx = newEvaluationContext(individualDecisionRequest);
			final DecisionResult cachedResult = decisionCache.get(individualDecisionRequest, evalCtx);
			if (cachedResult == null)
			{
				LOGGER.debug("No result found in cache for Individual Decision Request: {}. Computing new result from policy evaluation...", individualDecisionRequest);
				final DecisionResult finalResult = evaluateReusingContext(evalCtx, mdpCtx);
				LOGGER.debug("Caching new Result for Individual Decision Request: {} -> {}", individualDecisionRequest, finalResult);
				decisionCache.put(individualDecisionRequest, finalResult, evalCtx);
				return finalResult;
			}

			LOGGER.debug("Result found in cache for Individual Decision Request: {} -> {}", individualDecisionRequest, cachedResult);
			return cachedResult;
		}

		@Override
		protected DecisionResult evaluate(final DecisionRequest individualDecisionRequest)
		{
			assert individualDecisionRequest != null;
			return evaluateWithDecisionCache(individualDecisionRequest, Optional.empty());
		}

		@Override
		protected <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
		        final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, EvaluationContext mdpContext)
		{
			assert individualDecisionRequests != null && mdpContext != null;

			final Optional<EvaluationContext> optEvalCtx = Optional.of(mdpContext);

			/*
			 * There will be at most as many new results (not in cache) as there are individual decision requests
			 */
			final Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> finalResultsByRequest = new ArrayDeque<>(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				final DecisionResult finalResult = evaluateWithDecisionCache(individualDecisionRequest, optEvalCtx);
				finalResultsByRequest.add(new SimpleImmutableEntry<>(individualDecisionRequest, finalResult));
			}

			return finalResultsByRequest;
		}

	}

	private final boolean strictAttributeIssuerMatch;
	private final IndividualDecisionRequestEvaluator individualReqEvaluator;
	private final RootPolicyEvaluator rootPolicyEvaluator;
	private final Optional<CloseableNamedAttributeProviderRegistry> attProviders;
	private final Optional<DecisionCache> decisionCache;

	/**
	 * Constructs a new PDP engine with the given configuration information.
	 *
	 * @param attributeProviders
	 *            Attribute Providers - mandatory
	 * @param policyProvider
	 * 	 *            Policy Provider - mandatory
	 * @param rootPolicyId
	 *            root Policy(Set) ID
	 * @param rootPolicyElementType
	 *            type of root policy element (XACML Policy or XACML PolicySet). If undefined, try with XACML Policy, and else (if it fails) with XACML PolicySet.
	 * @param rootPolicyVersionPatterns
	 *            version pattern to be matched by root policy version
	 * @param decisionCache
	 *            (optional) decision response cache
	 * @param strictAttributeIssuerMatch
	 *            true iff strict Attribute Issuer matching is enabled, i.e. AttributeDesignators without Issuer only match request Attributes without Issuer (and same AttributeId, Category...). This
	 *            mode is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it performs better and is recommended when all
	 *            AttributeDesignators have an Issuer (best practice). Reminder: the XACML 3.0 specification for AttributeDesignator evaluation (5.29) says: "If the Issuer is not present in the
	 *            attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone."
	 * @throws java.lang.IllegalArgumentException
	 *             if one of the mandatory arguments is null ({@code xacmlExpressionFactory}, {@code rootPolicyProvider})
	 * @throws java.io.IOException
	 *             error closing the root policy Provider when static resolution is to be used
	 */
	public BasePdpEngine(final CloseablePolicyProvider<?> policyProvider, final Optional<TopLevelPolicyElementType> rootPolicyElementType,
	        final String rootPolicyId, final Optional<PolicyVersionPatterns> rootPolicyVersionPatterns, final boolean strictAttributeIssuerMatch,
						 final Optional<CloseableNamedAttributeProviderRegistry> attributeProviders,
						 final Optional<DecisionCache> decisionCache) throws IllegalArgumentException, IOException
	{
		final RootPolicyEvaluators.Base candidateRootPolicyEvaluator = new RootPolicyEvaluators.Base(policyProvider, rootPolicyElementType, rootPolicyId, rootPolicyVersionPatterns);
		// Use static resolution if possible
		final RootPolicyEvaluator staticRootPolicyEvaluator;
		try
		{
			staticRootPolicyEvaluator = candidateRootPolicyEvaluator.toStatic();
		}
		catch (final IndeterminateEvaluationException e)
		{
			throw new IllegalArgumentException("No valid " + (rootPolicyElementType.isPresent() ? rootPolicyElementType.get() : "Policy(Set)") + " '" + rootPolicyId + "' matching version (pattern): "
			        + (rootPolicyVersionPatterns.isPresent() ? rootPolicyVersionPatterns.get() : "latest"), e);
		}
		this.rootPolicyEvaluator = Objects.requireNonNullElse(staticRootPolicyEvaluator, candidateRootPolicyEvaluator);

		this.strictAttributeIssuerMatch = strictAttributeIssuerMatch;

		this.attProviders = attributeProviders;

		this.decisionCache = decisionCache;
		this.individualReqEvaluator = this.decisionCache.map(cache -> cache.isEvaluationContextRequired()? new IndividualRequestEvaluatorWithCacheUsingEvaluationContext(rootPolicyEvaluator, cache, attProviders)
				: new IndividualRequestEvaluatorWithCacheIgnoringEvaluationContext(rootPolicyEvaluator, cache, attProviders)).orElse(new NonCachingIndividualDecisionRequestEvaluator(rootPolicyEvaluator, attProviders));
	}

	/**
	 * Constructs a new PDP engine with the given configuration information.
	 *
	 * @param configuration
	 *            PDP engine configuration
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code configuration.getXacmlExpressionFactory() == null || configuration.getRootPolicyProvider() == null}
	 * @throws java.io.IOException
	 *             error closing {@code configuration.getRootPolicyProvider()} when static resolution is to be used
	 */
	public BasePdpEngine(final PdpEngineConfiguration configuration) throws IllegalArgumentException, IOException
	{
		this(configuration.getPolicyProvider(), configuration.getRootPolicyElementType(), configuration.getRootPolicyId(),
		        configuration.getRootPolicyVersionPatterns(), configuration.isStrictAttributeIssuerMatchEnabled(), configuration.getAttributeProviders(), configuration.getDecisionCache());
	}

	@Override
	public Iterable<PrimaryPolicyMetadata> getApplicablePolicies()
	{
		return this.rootPolicyEvaluator.getStaticApplicablePolicies();
	}

	@Override
	public DecisionRequestBuilder<?> newRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
	{
		return this.strictAttributeIssuerMatch ? new NonIssuedLikeIssuedAttributeHandlingRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes)
		        : new IssuedToNonIssuedAttributeCopyingRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DecisionResult evaluate(final DecisionRequest individualDecisionRequest)
	{
		if (individualDecisionRequest == null)
		{
			throw NULL_REQUEST_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision request with extra common attributes set by the PDP once for all: standard environment attributes, i.e. current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler" . These current date/time values must be set here once
		 * before an individual request is evaluated to make sure it uses the same value for current-time/current-date/current-dateTime during the entire evaluation, if they use the one from PDP.
		 */
		return individualReqEvaluator.evaluate(individualDecisionRequest);
	}

	/** {@inheritDoc} */
	@Override
	public <INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(
	        final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, EvaluationContext mdpContext) throws IndeterminateEvaluationException
	{
		if (individualDecisionRequests == null)
		{
			throw NULL_REQUEST_ARGUMENT_EXCEPTION;
		}

		if (mdpContext == null)
		{
			throw NULL_MDP_CTX_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision requests
		 */
		/* We call the attProviders' beginMultipleDecisionRequest(mdpContext) in case they want to set attributes in the scope of a Multiple Decision Request, i.e. common to all {@code individualDecisionRequests}, such as standard current-date, current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler" . These current date/time values must be set here once
			* before every individual request is evaluated to make sure they all use the same value for current-time/current-date/current-dateTime, if they use the one from PDP.
		 */
		attProviders.ifPresent(registry -> registry.beginMultipleDecisionRequest(mdpContext));
		return individualReqEvaluator.evaluate(individualDecisionRequests, mdpContext);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		rootPolicyEvaluator.close();
		if(attProviders.isPresent()) {
			attProviders.get().close();
		}
		if (decisionCache.isPresent())
		{
			decisionCache.get().close();
		}
	}

}
