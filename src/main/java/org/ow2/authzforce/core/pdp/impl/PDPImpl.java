/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.CloseablePDP;
import org.ow2.authzforce.core.pdp.api.DecisionCache;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.XMLUtils;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluators;
import org.ow2.authzforce.core.pdp.impl.policy.StaticApplicablePolicyView;
import org.ow2.authzforce.core.xmlns.pdp.StandardEnvironmentAttributeSource;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koloboke.collect.map.hash.HashObjObjMaps;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

/**
 * This is the core XACML PDP engine implementation. To build an XACML policy
 * engine, you start by instantiating this object directly or in a easier and
 * preferred way, using {@link PdpConfigurationParser}.
 *
 * @version $Id: $
 */
public final class PDPImpl implements CloseablePDP {
	private static final IllegalArgumentException ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"No input Individual Decision Request");

	// the logger we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(PDPImpl.class);

	/*
	 * The default behavior for getting the standard environment attributes
	 * (current date/time) is the one complying strictly with the XACML spec: if
	 * request does not have values for these attributes, the "context handler"
	 * (PDP in this case) must provide them. So we use PDP values if it does not
	 * override any existing value in the request.
	 */
	private static final StandardEnvironmentAttributeSource DEFAULT_STD_ENV_ATTRIBUTE_SOURCE = StandardEnvironmentAttributeSource.REQUEST_ELSE_PDP;

	/**
	 * Indeterminate response iff CombinedDecision element not supported because
	 * the request parser does not support any scheme from
	 * MultipleDecisionProfile section 2.
	 */
	private static final Response UNSUPPORTED_COMBINED_DECISION_RESPONSE = new Response(
			Collections.<Result> singletonList(new Result(DecisionType.INDETERMINATE,
					new StatusHelper(StatusHelper.STATUS_SYNTAX_ERROR, "Unsupported feature: CombinedDecision='true'"),
					null, null, null, null)));

	private interface StandardEnvironmentAttributeIssuer {
		Map<AttributeGUID, Bag<?>> get();
	}

	private static final StandardEnvironmentAttributeIssuer NULL_STD_ENV_ATTRIBUTE_ISSUER = new StandardEnvironmentAttributeIssuer() {

		@Override
		public Map<AttributeGUID, Bag<?>> get() {
			return null;
		}
	};

	private static final StandardEnvironmentAttributeIssuer DEFAULT_TZ_BASED_STD_ENV_ATTRIBUTE_ISSUER = new StandardEnvironmentAttributeIssuer() {

		@Override
		public Map<AttributeGUID, Bag<?>> get() {
			/*
			 * Set the standard current date/time attribute according to XACML
			 * core spec:
			 * "This identifier indicates the current time at the context handler. In practice it is the time at which the request context was created."
			 * (§B.7). XACML standard (§10.2.5) says: "If values for these
			 * attributes are not present in the decision request, then their
			 * values MUST be supplied by the context handler".
			 */
			// current datetime in default timezone
			final DateTimeValue currentDateTimeValue = new DateTimeValue(new GregorianCalendar());
			return HashObjObjMaps.<AttributeGUID, Bag<?>> newImmutableMapOf(
					// current date-time
					StandardEnvironmentAttribute.CURRENT_DATETIME.getGUID(),
					Bags.singleton(StandardDatatypes.DATETIME_FACTORY.getDatatype(), currentDateTimeValue),
					// current date
					StandardEnvironmentAttribute.CURRENT_DATE.getGUID(),
					Bags.singleton(StandardDatatypes.DATE_FACTORY.getDatatype(),
							DateValue.getInstance(
									(XMLGregorianCalendar) currentDateTimeValue.getUnderlyingValue().clone())),
					// current time
					StandardEnvironmentAttribute.CURRENT_TIME.getGUID(),
					Bags.singleton(StandardDatatypes.TIME_FACTORY.getDatatype(), TimeValue
							.getInstance((XMLGregorianCalendar) currentDateTimeValue.getUnderlyingValue().clone())));
		}
	};

	private static final DecisionResultFilter DEFAULT_RESULT_FILTER = new DecisionResultFilter() {
		private static final String ID = "urn:ow2:authzforce:feature:pdp:result-filter:default";

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public List<Result> filter(final List<Result> results) {
			return results;
		}

		@Override
		public boolean supportsMultipleDecisionCombining() {
			return false;
		}

	};

	private static final class NonCachingIndividualDecisionRequestEvaluator extends IndividualDecisionRequestEvaluator {
		private NonCachingIndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator,
				final StandardEnvironmentAttributeSource stdEnvAttributeSource) {
			super(rootPolicyEvaluator, stdEnvAttributeSource);
		}

		@Override
		protected <INDIVIDUAL_DECISION_REQ_T extends IndividualDecisionRequest> List<Result> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests,
				final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes) {
			final List<Result> results = new ArrayList<>(individualDecisionRequests.size());
			for (final IndividualDecisionRequest individuaDecisionRequest : individualDecisionRequests) {
				if (individuaDecisionRequest == null) {
					throw new RuntimeException(
							"One of the individual decision requests returned by the request filter is invalid (null).");
				}

				final PdpDecisionResult decisionResult = evaluate(individuaDecisionRequest, pdpIssuedAttributes, false);
				results.add(decisionResult.toXACMLResult(individuaDecisionRequest.getReturnedAttributes()));
			}

			return results;
		}

	}

	private static final class CachingIndividualRequestEvaluator extends IndividualDecisionRequestEvaluator {
		// the logger we'll use for all messages
		private static final Logger _LOGGER = LoggerFactory.getLogger(CachingIndividualRequestEvaluator.class);

		private static final Result INVALID_DECISION_CACHE_RESULT = new Result(DecisionType.INDETERMINATE,
				new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR, "Internal error"), null, null, null, null);

		private final DecisionCache decisionCache;

		private CachingIndividualRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator,
				final StandardEnvironmentAttributeSource stdEnvAttributeSource, final DecisionCache decisionCache) {
			super(rootPolicyEvaluator, stdEnvAttributeSource);
			assert decisionCache != null;
			this.decisionCache = decisionCache;
		}

		@Override
		public <INDIVIDUAL_DECISION_REQ_T extends IndividualDecisionRequest> List<Result> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests,
				final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes) {
			final Map<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> cachedResultsByRequest = decisionCache
					.getAll(individualDecisionRequests);
			if (cachedResultsByRequest == null) {
				// error, return indeterminate result as only result
				_LOGGER.error("Invalid decision cache result: null");
				return Collections.singletonList(INVALID_DECISION_CACHE_RESULT);
			}

			/*
			 * At least check that we have as many results from cache as input
			 * requests (For each request with no result in cache, there must
			 * still be an entry with value null.)
			 */
			if (cachedResultsByRequest.size() != individualDecisionRequests.size()) {
				// error, return indeterminate result as only result
				if (LOGGER.isErrorEnabled()) {
					// Beware of autoboxing which causes call to
					// Integer.valueOf(...)
					_LOGGER.error(
							"Invalid decision cache result: number of returned decision results ({}) != number of input (individual) decision requests ({})",
							cachedResultsByRequest.size(), individualDecisionRequests.size());
				}
				return Collections.singletonList(INVALID_DECISION_CACHE_RESULT);
			}

			final Set<Entry<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult>> cachedRequestResultEntries = cachedResultsByRequest
					.entrySet();
			final List<Result> results = new ArrayList<>(cachedRequestResultEntries.size());
			final Map<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> newResultsByRequest = HashObjObjMaps
					.newUpdatableMap();
			for (final Entry<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> cachedRequestResultPair : cachedRequestResultEntries) {
				final PdpDecisionResult finalResult;
				final INDIVIDUAL_DECISION_REQ_T individuaDecisionRequest = cachedRequestResultPair.getKey();
				final PdpDecisionResult cachedResult = cachedRequestResultPair.getValue();
				if (cachedResult == null) {
					// result not in cache -> evaluate request
					if (individuaDecisionRequest == null) {
						throw new RuntimeException(
								"One of the entry keys (individual decision request) returned by the decision cache implementation '"
										+ decisionCache + "' is invalid (null).");
					}

					finalResult = evaluate(individuaDecisionRequest, pdpIssuedAttributes, true);
					newResultsByRequest.put(individuaDecisionRequest, finalResult);
				} else {
					finalResult = cachedResult;
				}

				results.add(finalResult.toXACMLResult(individuaDecisionRequest.getReturnedAttributes()));
			}

			decisionCache.putAll(newResultsByRequest);
			return results;
		}
	}

	private final RequestFilter reqFilter;
	private final IndividualDecisionRequestEvaluator individualReqEvaluator;
	private final DecisionCache decisionCache;
	private final RootPolicyEvaluator rootPolicyEvaluator;
	private final DecisionResultFilter resultFilter;
	private final StandardEnvironmentAttributeIssuer pdpStdEnvAttributeIssuer;
	private final int badRequestStatusDetailLevel;

	/**
	 * Constructs a new <code>PDP</code> object with the given configuration
	 * information.
	 *
	 * @param attributeFactory
	 *            attribute value factory - mandatory
	 * @param functionRegistry
	 *            function registry - mandatory
	 * @param jaxbAttributeProviderConfs
	 *            XML/JAXB configurations of Attribute Providers for
	 *            AttributeDesignator/AttributeSelector evaluation; may be null
	 *            for static expression evaluation (out of context), in which
	 *            case AttributeSelectors/AttributeDesignators are not supported
	 * @param maxVariableReferenceDepth
	 *            max depth of VariableReference chaining: VariableDefinition ->
	 *            VariableDefinition ->... ('->' represents a
	 *            VariableReference); strictly negative value means no limit
	 * 
	 * @param enableXPath
	 *            allow XPath evaluation, i.e. AttributeSelectors and
	 *            xpathExpressions (experimental, not for production, use with
	 *            caution)
	 * @param requestFilterId
	 *            ID of request filter (XACML Request processing prior to policy
	 *            evaluation) - mandatory
	 * @param decisionResultFilter
	 *            decision result filter (XACML Result processing after policy
	 *            evaluation, before creating/returning final XACML Response)
	 * @param jaxbDecisionCacheConf
	 *            decision response cache XML/JAXB configuration
	 * @param jaxbRootPolicyProviderConf
	 *            root policy Provider's XML/JAXB configuration - mandatory
	 * @param combiningAlgRegistry
	 *            XACML policy/rule combining algorithm registry - mandatory
	 * @param jaxbRefPolicyProviderConf
	 *            policy-by-reference Provider's XML/JAXB configuration, for
	 *            resolving policies referred to by Policy(Set)IdReference in
	 *            policies found by root policy Provider
	 * @param maxPolicySetRefDepth
	 *            max allowed PolicySetIdReference chain: PolicySet1
	 *            (PolicySetIdRef1) -> PolicySet2 (PolicySetIdRef2) -> ...; a
	 *            strictly negative value means no limit
	 * @param strictAttributeIssuerMatch
	 *            true iff strict Attribute Issuer matching is enabled, i.e.
	 *            AttributeDesignators without Issuer only match request
	 *            Attributes without Issuer (and same AttributeId, Category...).
	 *            This mode is not fully compliant with XACML 3.0, §5.29, in the
	 *            case that the Issuer is indeed not present on a
	 *            AttributeDesignator; but it performs better and is recommended
	 *            when all AttributeDesignators have an Issuer (best practice).
	 *            Reminder: the XACML 3.0 specification for AttributeDesignator
	 *            evaluation (5.29) says: "If the Issuer is not present in the
	 *            attribute designator, then the matching of the attribute to
	 *            the named attribute SHALL be governed by AttributeId and
	 *            DataType attributes alone." if one of the mandatory arguments
	 *            is null
	 * @param stdEnvAttributeSource
	 *            source for standard environment
	 *            current-time/current-date/current-dateTime attribute values
	 *            (request or PDP, etc.)
	 * @param badRequestStatusDetailLevel
	 *            level of detail in the StatusDetail returned in the
	 *            Indeterminate Result when the Request format/syntax is invalid
	 * @param environmentProperties
	 *            PDP configuration environment properties
	 * @throws java.lang.IllegalArgumentException
	 *             if there is not any extension found for type
	 *             {@link org.ow2.authzforce.core.pdp.api.RequestFilter.Factory}
	 *             with ID {@code requestFilterId}; or if one of the mandatory
	 *             arguments is null; or if any Attribute Provider module
	 *             created from {@code jaxbAttributeProviderConfs} does not
	 *             provide any attribute; or it is in conflict with another one
	 *             already registered to provide the same or part of the same
	 *             attributes; of if there is no extension supporting
	 *             {@code jaxbDecisionCacheConf}
	 * @throws java.io.IOException
	 *             error closing the root policy Provider when static resolution
	 *             is to be used; or error closing the attribute Provider
	 *             modules created from {@code jaxbAttributeProviderConfs}, when
	 *             and before an {@link IllegalArgumentException} is raised
	 */
	public PDPImpl(final DatatypeFactoryRegistry attributeFactory, final FunctionRegistry functionRegistry,
			final List<AbstractAttributeProvider> jaxbAttributeProviderConfs, final int maxVariableReferenceDepth,
			final boolean enableXPath, final CombiningAlgRegistry combiningAlgRegistry,
			final AbstractPolicyProvider jaxbRootPolicyProviderConf,
			final AbstractPolicyProvider jaxbRefPolicyProviderConf, final int maxPolicySetRefDepth,
			final String requestFilterId, final boolean strictAttributeIssuerMatch,
			final StandardEnvironmentAttributeSource stdEnvAttributeSource,
			final DecisionResultFilter decisionResultFilter, final AbstractDecisionCache jaxbDecisionCacheConf,
			final int badRequestStatusDetailLevel, final EnvironmentProperties environmentProperties)
					throws IllegalArgumentException, IOException {
		final RequestFilter.Factory requestFilterFactory = requestFilterId == null
				? DefaultRequestFilter.LaxFilterFactory.INSTANCE
				: PdpExtensionLoader.getExtension(RequestFilter.Factory.class, requestFilterId);

		final RequestFilter requestFilter = requestFilterFactory.getInstance(attributeFactory,
				strictAttributeIssuerMatch, enableXPath, XMLUtils.SAXON_PROCESSOR);

		final RootPolicyEvaluators.Base candidateRootPolicyEvaluator = new RootPolicyEvaluators.Base(attributeFactory,
				functionRegistry, jaxbAttributeProviderConfs, maxVariableReferenceDepth, enableXPath,
				combiningAlgRegistry, jaxbRootPolicyProviderConf, jaxbRefPolicyProviderConf, maxPolicySetRefDepth,
				strictAttributeIssuerMatch, environmentProperties);
		// Use static resolution if possible
		final RootPolicyEvaluator staticRootPolicyEvaluator = candidateRootPolicyEvaluator.toStatic();
		if (staticRootPolicyEvaluator == null) {
			this.rootPolicyEvaluator = candidateRootPolicyEvaluator;
		} else {
			this.rootPolicyEvaluator = staticRootPolicyEvaluator;
		}

		this.reqFilter = requestFilter;

		// decision cache
		if (jaxbDecisionCacheConf == null) {
			this.decisionCache = null;
		} else {
			final DecisionCache.Factory<AbstractDecisionCache> responseCacheStoreFactory = PdpExtensionLoader
					.getJaxbBoundExtension(DecisionCache.Factory.class, jaxbDecisionCacheConf.getClass());
			this.decisionCache = responseCacheStoreFactory.getInstance(jaxbDecisionCacheConf);
		}

		final StandardEnvironmentAttributeSource validStdEnvAttrSrc = stdEnvAttributeSource == null
				? DEFAULT_STD_ENV_ATTRIBUTE_SOURCE : stdEnvAttributeSource;
		this.pdpStdEnvAttributeIssuer = validStdEnvAttrSrc == StandardEnvironmentAttributeSource.REQUEST_ONLY
				? NULL_STD_ENV_ATTRIBUTE_ISSUER : DEFAULT_TZ_BASED_STD_ENV_ATTRIBUTE_ISSUER;
		this.individualReqEvaluator = this.decisionCache == null
				? new NonCachingIndividualDecisionRequestEvaluator(rootPolicyEvaluator, validStdEnvAttrSrc)
				: new CachingIndividualRequestEvaluator(rootPolicyEvaluator, validStdEnvAttrSrc, this.decisionCache);
		this.resultFilter = decisionResultFilter == null ? DEFAULT_RESULT_FILTER : decisionResultFilter;

		this.badRequestStatusDetailLevel = badRequestStatusDetailLevel;
	}

	/** {@inheritDoc} */
	@Override
	public <R extends IndividualDecisionRequest> List<Result> evaluate(final List<R> individualDecisionRequests) {
		if (individualDecisionRequests == null) {
			throw ILLEGAL_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision requests with extra common
		 * attributes set by the PDP once for all: standard environment
		 * attributes, i.e. current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler"
		 * . These current date/time values must be set here once before every
		 * individual request is evaluated to make sure they all use the same
		 * value for current-time/current-date/current-dateTime, if they use the
		 * one from PDP.
		 */
		final List<Result> results = individualReqEvaluator.evaluate(individualDecisionRequests,
				this.pdpStdEnvAttributeIssuer.get());
		return resultFilter.filter(results);
	}

	/** {@inheritDoc} */
	@Override
	public Response evaluate(final Request request, final Map<String, String> namespaceURIsByPrefix) {
		if (request == null) {
			throw ILLEGAL_ARGUMENT_EXCEPTION;
		}

		/*
		 * No support for CombinedDecision = true if no decisionCombiner
		 * defined. (The use of the CombinedDecision attribute is specified in
		 * Multiple Decision Profile.)
		 */
		if (request.isCombinedDecision() && !resultFilter.supportsMultipleDecisionCombining()) {
			/*
			 * According to XACML core spec, 5.42, "If the PDP does not
			 * implement the relevant functionality in [Multiple Decision
			 * Profile], then the PDP must return an Indeterminate with a status
			 * code of urn:oasis:names:tc:xacml:1.0:status:processing-error if
			 * it receives a request with this attribute set to "true".
			 */
			return UNSUPPORTED_COMBINED_DECISION_RESPONSE;
		}

		/*
		 * The request parser may return multiple individual decision requests
		 * from a single Request, e.g. if the request parser implements the
		 * Multiple Decision profile or Hierarchical Resource profile
		 */
		final List<? extends IndividualDecisionRequest> individualDecisionRequests;
		try {
			individualDecisionRequests = reqFilter.filter(request, namespaceURIsByPrefix);
		} catch (final IndeterminateEvaluationException e) {
			LOGGER.info("Invalid or unsupported input XACML Request syntax", e);
			return new Response(Collections.<Result> singletonList(new Result(DecisionType.INDETERMINATE,
					e.getStatus(badRequestStatusDetailLevel), null, null, null, null)));
		}

		final List<Result> results = evaluate(individualDecisionRequests);
		return new Response(results);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		rootPolicyEvaluator.close();
		if (decisionCache != null) {
			decisionCache.close();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Response evaluate(final Request request) {
		return evaluate(request, null);
	}

	/**
	 * Get the PDP's root policy and policies referenced - directly or
	 * indirectly - from the root policy, if all are statically resolved
	 *
	 * @return the root and referenced policies; null if any of these policies
	 *         is not statically resolved (once and for all)
	 */
	public StaticApplicablePolicyView getStaticApplicablePolicies() {
		return this.rootPolicyEvaluator.getStaticApplicablePolicies();
	}

}
