/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.CloseablePDP;
import org.ow2.authzforce.core.pdp.api.DecisionCache;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter.FilteringResultCollector;
import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EnvironmentPropertyName;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.RequestFilter;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.XMLUtils;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlg;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.DateTimeValue;
import org.ow2.authzforce.core.pdp.api.value.DateValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.TimeValue;
import org.ow2.authzforce.core.pdp.impl.combining.ImmutableCombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.func.ImmutableFunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluators;
import org.ow2.authzforce.core.pdp.impl.policy.StaticApplicablePolicyView;
import org.ow2.authzforce.core.pdp.impl.value.ImmutableDatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.impl.value.StandardDatatypeFactoryRegistry;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.core.xmlns.pdp.StandardEnvironmentAttributeSource;
import org.ow2.authzforce.xacml.identifiers.XACMLDatatypeId;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * This is the core XACML PDP engine implementation.
 *
 * @version $Id: $
 */
public final class BasePdpEngine implements CloseablePDP<ImmutablePdpDecisionRequest>
{
	// the logger we'll use for all messages
	private static final Logger LOGGER = LoggerFactory.getLogger(BasePdpEngine.class);

	private static final IllegalArgumentException NULL_PDP_MODEL_HANDLER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined PDP configuration model handler");
	private static final IllegalArgumentException ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException("No input Individual Decision Request");

	/*
	 * The default behavior for getting the standard environment attributes (current date/time) is the one complying strictly with the XACML spec: if request does not have values for these attributes,
	 * the "context handler" (PDP in this case) must provide them. So we use PDP values if it does not override any existing value in the request.
	 */
	private static final StandardEnvironmentAttributeSource DEFAULT_STD_ENV_ATTRIBUTE_SOURCE = StandardEnvironmentAttributeSource.REQUEST_ELSE_PDP;

	/**
	 * Indeterminate response iff CombinedDecision element not supported because the request parser does not support any scheme from MultipleDecisionProfile section 2.
	 */
	private static final Response UNSUPPORTED_COMBINED_DECISION_RESPONSE = new Response(Collections.<Result> singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(
			StatusHelper.STATUS_SYNTAX_ERROR, "Unsupported feature: CombinedDecision='true'"), null, null, null, null)));

	private interface StandardEnvironmentAttributeIssuer
	{
		Map<AttributeGUID, Bag<?>> get();
	}

	private static final StandardEnvironmentAttributeIssuer NULL_STD_ENV_ATTRIBUTE_ISSUER = new StandardEnvironmentAttributeIssuer()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> get()
		{
			return null;
		}
	};

	private static final StandardEnvironmentAttributeIssuer DEFAULT_TZ_BASED_STD_ENV_ATTRIBUTE_ISSUER = new StandardEnvironmentAttributeIssuer()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> get()
		{
			/*
			 * Set the standard current date/time attribute according to XACML core spec:
			 * "This identifier indicates the current time at the context handler. In practice it is the time at which the request context was created." (§B.7). XACML standard (§10.2.5) says: "If
			 * values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler".
			 */
			// current datetime in default timezone
			final DateTimeValue currentDateTimeValue = new DateTimeValue(new GregorianCalendar());
			return HashCollections.<AttributeGUID, Bag<?>> newImmutableMap(
					// current date-time
					StandardEnvironmentAttribute.CURRENT_DATETIME.getGUID(),
					Bags.singleton(StandardDatatypes.DATETIME_FACTORY.getDatatype(), currentDateTimeValue),
					// current date
					StandardEnvironmentAttribute.CURRENT_DATE.getGUID(),
					Bags.singleton(StandardDatatypes.DATE_FACTORY.getDatatype(), DateValue.getInstance((XMLGregorianCalendar) currentDateTimeValue.getUnderlyingValue().clone())),
					// current time
					StandardEnvironmentAttribute.CURRENT_TIME.getGUID(),
					Bags.singleton(StandardDatatypes.TIME_FACTORY.getDatatype(), TimeValue.getInstance((XMLGregorianCalendar) currentDateTimeValue.getUnderlyingValue().clone())));
		}
	};

	private static class NonIssuedLikeIssuedAttributeHandlingRequestBuilder implements PdpDecisionRequestBuilder<ImmutablePdpDecisionRequest>
	{
		private final Map<AttributeGUID, Bag<?>> namedAttributes;
		private final Map<String, XdmNode> extraContentsByCategory;

		private NonIssuedLikeIssuedAttributeHandlingRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
		{
			namedAttributes = expectedTotalNumOfAttributes < 0 ? HashCollections.newUpdatableMap() : HashCollections.newUpdatableMap(expectedTotalNumOfAttributes);
			extraContentsByCategory = expectedNumOfAttributeCategories < 0 ? HashCollections.newUpdatableMap() : HashCollections.newUpdatableMap(expectedNumOfAttributeCategories);
		}

		@Override
		public Bag<?> putNamedAttributeIfAbsent(final AttributeGUID attributeId, final Bag<?> attributeValues)
		{
			return namedAttributes.putIfAbsent(attributeId, attributeValues);
		}

		@Override
		public final XdmNode putContentIfAbsent(final String category, final XdmNode content)
		{
			return extraContentsByCategory.putIfAbsent(category, content);
		}

		@Override
		public final ImmutablePdpDecisionRequest build(final boolean returnApplicablePolicies)
		{
			return new ImmutablePdpDecisionRequest(namedAttributes, extraContentsByCategory, returnApplicablePolicies);
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
		public Bag<?> putNamedAttributeIfAbsent(final AttributeGUID attributeGUID, final Bag<?> attributeValues)
		{
			/*
			 * Put the non-issued version of the attribute first
			 */
			final AttributeGUID nonIssuedAttributeGUID = new AttributeGUID(attributeGUID.getCategory(), null, attributeGUID.getId());
			super.putNamedAttributeIfAbsent(nonIssuedAttributeGUID, attributeValues);
			return super.putNamedAttributeIfAbsent(attributeGUID, attributeValues);
		}
	}

	private static final class NonCachingIndividualDecisionRequestEvaluator extends IndividualDecisionRequestEvaluator
	{
		private NonCachingIndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource,
				final DecisionResultFilter resultFilter)
		{
			super(rootPolicyEvaluator, stdEnvAttributeSource, resultFilter);
		}

		@Override
		protected PdpDecisionResult evaluate(final PdpDecisionRequest individualDecisionRequest, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
		{
			return evaluate(individualDecisionRequest, pdpIssuedAttributes, false);
		}

		@Override
		protected <INDIVIDUAL_DECISION_REQ_T extends PdpDecisionRequest> Map<INDIVIDUAL_DECISION_REQ_T, ? extends PdpDecisionResult> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException
		{
			final Map<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> resultsByRequest = HashCollections.newUpdatableMap(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				if (individualDecisionRequest == null)
				{
					throw new RuntimeException("One of the individual decision requests returned by the request filter is invalid (null).");
				}

				final PdpDecisionResult decisionResult = evaluate(individualDecisionRequest, pdpIssuedAttributes, false);
				resultsByRequest.put(individualDecisionRequest, decisionResult);
			}

			return resultsByRequest;
		}

		@Override
		protected List<Result> evaluateToJAXB(final List<? extends IndividualDecisionRequest> individualDecisionRequests, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
		{
			final FilteringResultCollector filteringResultCollector = beginMultipleDecisions(individualDecisionRequests.size());
			for (final IndividualDecisionRequest individualDecisionRequest : individualDecisionRequests)
			{
				if (individualDecisionRequest == null)
				{
					throw new RuntimeException("One of the individual decision requests returned by the request filter is invalid (null).");
				}

				final PdpDecisionResult decisionResult = evaluate(individualDecisionRequest, pdpIssuedAttributes, false);
				final List<Result> finalResults = filteringResultCollector.addResult(individualDecisionRequest, decisionResult);
				if (finalResults != null)
				{
					return finalResults;
				}
			}

			return filteringResultCollector.getFilteredResults();
		}
	}

	private static final class CachingIndividualRequestEvaluator extends IndividualDecisionRequestEvaluator
	{
		// the logger we'll use for all messages
		private static final Logger _LOGGER = LoggerFactory.getLogger(CachingIndividualRequestEvaluator.class);

		private static final IndeterminateEvaluationException INDETERMINATE_EVALUATION_EXCEPTION = new IndeterminateEvaluationException("Internal error in decision cache: null result",
				StatusHelper.STATUS_PROCESSING_ERROR);

		private static final Result INVALID_DECISION_CACHE_RESULT = new Result(DecisionType.INDETERMINATE, new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR, "Internal error"), null, null, null,
				null);

		private final DecisionCache decisionCache;

		private CachingIndividualRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource,
				final DecisionResultFilter resultFilter, final DecisionCache decisionCache)
		{
			super(rootPolicyEvaluator, stdEnvAttributeSource, resultFilter);
			assert decisionCache != null;
			this.decisionCache = decisionCache;
		}

		@Override
		protected PdpDecisionResult evaluate(final PdpDecisionRequest individualDecisionRequest, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
		{
			final PdpDecisionResult cachedResult = decisionCache.get(individualDecisionRequest);
			if (cachedResult != null)
			{
				return cachedResult;
			}

			final PdpDecisionResult finalResult = evaluate(individualDecisionRequest, pdpIssuedAttributes, true);
			decisionCache.put(individualDecisionRequest, finalResult);
			return finalResult;
		}

		@Override
		public <INDIVIDUAL_DECISION_REQ_T extends PdpDecisionRequest> Map<INDIVIDUAL_DECISION_REQ_T, ? extends PdpDecisionResult> evaluate(
				final List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException
		{
			final Map<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> cachedResultsByRequest = decisionCache.getAll(individualDecisionRequests);
			if (cachedResultsByRequest == null)
			{
				// error, return indeterminate result as only result
				_LOGGER.error("Invalid decision cache result: null");
				throw INDETERMINATE_EVALUATION_EXCEPTION;
			}

			/*
			 * There will be at most as many new results (not in cache) as there are individual decision requests
			 */
			final Map<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> finalResultsByRequest = HashCollections.newUpdatableMap(individualDecisionRequests.size());
			final Map<INDIVIDUAL_DECISION_REQ_T, PdpDecisionResult> newResultsByRequest = HashCollections.newUpdatableMap(individualDecisionRequests.size());
			for (final INDIVIDUAL_DECISION_REQ_T individualDecisionRequest : individualDecisionRequests)
			{
				final PdpDecisionResult finalResult;
				/*
				 * Check whether there is any decision result in cache for this request
				 */
				final PdpDecisionResult cachedResult = cachedResultsByRequest.get(individualDecisionRequest);
				if (cachedResult == null)
				{
					// result not in cache -> evaluate request
					finalResult = evaluate(individualDecisionRequest, pdpIssuedAttributes, true);
					newResultsByRequest.put(individualDecisionRequest, finalResult);
				}
				else
				{
					finalResult = cachedResult;
				}

				finalResultsByRequest.put(individualDecisionRequest, finalResult);
			}

			if (!newResultsByRequest.isEmpty())
			{
				decisionCache.putAll(newResultsByRequest);
			}

			return finalResultsByRequest;
		}

		@Override
		public List<Result> evaluateToJAXB(final List<? extends IndividualDecisionRequest> individualDecisionRequests, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes)
		{
			final Map<? extends IndividualDecisionRequest, PdpDecisionResult> cachedResultsByRequest = decisionCache.getAll(individualDecisionRequests);
			if (cachedResultsByRequest == null)
			{
				// error, return indeterminate result as only result
				_LOGGER.error("Invalid decision cache result: null");
				return Collections.singletonList(INVALID_DECISION_CACHE_RESULT);
			}

			/*
			 * There will be at most as many new results (not in cache) as there are individual decision requests
			 */
			final FilteringResultCollector filteringResultCollector = beginMultipleDecisions(individualDecisionRequests.size());
			final Map<IndividualDecisionRequest, PdpDecisionResult> newResultsByRequest = HashCollections.newUpdatableMap(individualDecisionRequests.size());

			try
			{
				for (final IndividualDecisionRequest individualDecisionRequest : individualDecisionRequests)
				{
					final PdpDecisionResult finalResult;
					/*
					 * Check whether there is any decision result in cache for this request
					 */
					final PdpDecisionResult cachedResult = cachedResultsByRequest.get(individualDecisionRequest);
					if (cachedResult == null)
					{
						// result not in cache -> evaluate request
						finalResult = evaluate(individualDecisionRequest, pdpIssuedAttributes, true);
						newResultsByRequest.put(individualDecisionRequest, finalResult);
					}
					else
					{
						finalResult = cachedResult;
					}

					final List<Result> finalResults = filteringResultCollector.addResult(individualDecisionRequest, finalResult);
					if (finalResults != null)
					{
						return finalResults;
					}
				}

				return filteringResultCollector.getFilteredResults();
			}
			finally
			{
				if (!newResultsByRequest.isEmpty())
				{
					decisionCache.putAll(newResultsByRequest);
				}
			}
		}

	}

	private final boolean strictAttributeIssuerMatch;
	private final RequestFilter reqFilter;
	private final IndividualDecisionRequestEvaluator individualReqEvaluator;
	private final DecisionCache decisionCache;
	private final RootPolicyEvaluator rootPolicyEvaluator;
	private final StandardEnvironmentAttributeIssuer pdpStdEnvAttributeIssuer;
	private final int badRequestStatusDetailLevel;

	/**
	 * Constructs a new <code>PDP</code> object with the given configuration information.
	 *
	 * @param attributeFactory
	 *            attribute value factory - mandatory
	 * @param functionRegistry
	 *            function registry - mandatory
	 * @param jaxbAttributeProviderConfs
	 *            XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static expression evaluation (out of context), in which case
	 *            AttributeSelectors/AttributeDesignators are not supported
	 * @param maxVariableReferenceDepth
	 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition ->... ('->' represents a VariableReference); strictly negative value means no limit
	 * 
	 * @param enableXPath
	 *            allow XPath evaluation, i.e. AttributeSelectors and xpathExpressions (experimental, not for production, use with caution)
	 * @param requestFilterId
	 *            ID of request filter (XACML Request processing prior to policy evaluation) - mandatory
	 * @param decisionResultFilter
	 *            decision result filter (XACML Result processing after policy evaluation, before creating/returning final XACML Response)
	 * @param jaxbDecisionCacheConf
	 *            decision response cache XML/JAXB configuration
	 * @param jaxbRootPolicyProviderConf
	 *            root policy Provider's XML/JAXB configuration - mandatory
	 * @param combiningAlgRegistry
	 *            XACML policy/rule combining algorithm registry - mandatory
	 * @param jaxbRefPolicyProviderConf
	 *            policy-by-reference Provider's XML/JAXB configuration, for resolving policies referred to by Policy(Set)IdReference in policies found by root policy Provider
	 * @param maxPolicySetRefDepth
	 *            max allowed PolicySetIdReference chain: PolicySet1 (PolicySetIdRef1) -> PolicySet2 (PolicySetIdRef2) -> ...; a strictly negative value means no limit
	 * @param strictAttributeIssuerMatch
	 *            true iff strict Attribute Issuer matching is enabled, i.e. AttributeDesignators without Issuer only match request Attributes without Issuer (and same AttributeId, Category...). This
	 *            mode is not fully compliant with XACML 3.0, §5.29, in the case that the Issuer is indeed not present on a AttributeDesignator; but it performs better and is recommended when all
	 *            AttributeDesignators have an Issuer (best practice). Reminder: the XACML 3.0 specification for AttributeDesignator evaluation (5.29) says: "If the Issuer is not present in the
	 *            attribute designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and DataType attributes alone." if one of the mandatory arguments is
	 *            null
	 * @param stdEnvAttributeSource
	 *            source for standard environment current-time/current-date/current-dateTime attribute values (request or PDP, etc.)
	 * @param badRequestStatusDetailLevel
	 *            level of detail in the StatusDetail returned in the Indeterminate Result when the Request format/syntax is invalid
	 * @param environmentProperties
	 *            PDP configuration environment properties
	 * @throws java.lang.IllegalArgumentException
	 *             if there is not any extension found for type {@link org.ow2.authzforce.core.pdp.api.RequestFilter.Factory} with ID {@code requestFilterId}; or if one of the mandatory arguments is
	 *             null; or if any Attribute Provider module created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in conflict with another one already registered to
	 *             provide the same or part of the same attributes; of if there is no extension supporting {@code jaxbDecisionCacheConf}
	 * @throws java.io.IOException
	 *             error closing the root policy Provider when static resolution is to be used; or error closing the attribute Provider modules created from {@code jaxbAttributeProviderConfs}, when
	 *             and before an {@link IllegalArgumentException} is raised
	 */
	public BasePdpEngine(final DatatypeFactoryRegistry attributeFactory, final FunctionRegistry functionRegistry, final List<AbstractAttributeProvider> jaxbAttributeProviderConfs,
			final int maxVariableReferenceDepth, final boolean enableXPath, final CombiningAlgRegistry combiningAlgRegistry, final AbstractPolicyProvider jaxbRootPolicyProviderConf,
			final AbstractPolicyProvider jaxbRefPolicyProviderConf, final int maxPolicySetRefDepth, final String requestFilterId, final boolean strictAttributeIssuerMatch,
			final StandardEnvironmentAttributeSource stdEnvAttributeSource, final DecisionResultFilter decisionResultFilter, final AbstractDecisionCache jaxbDecisionCacheConf,
			final int badRequestStatusDetailLevel, final EnvironmentProperties environmentProperties) throws IllegalArgumentException, IOException
	{
		this.strictAttributeIssuerMatch = strictAttributeIssuerMatch;

		final RequestFilter.Factory requestFilterFactory = requestFilterId == null ? DefaultRequestFilter.LaxFilterFactory.INSTANCE : PdpExtensionLoader.getExtension(RequestFilter.Factory.class,
				requestFilterId);

		final RequestFilter requestFilter = requestFilterFactory.getInstance(attributeFactory, strictAttributeIssuerMatch, enableXPath, XMLUtils.SAXON_PROCESSOR);

		final RootPolicyEvaluators.Base candidateRootPolicyEvaluator = new RootPolicyEvaluators.Base(attributeFactory, functionRegistry, jaxbAttributeProviderConfs, maxVariableReferenceDepth,
				enableXPath, combiningAlgRegistry, jaxbRootPolicyProviderConf, jaxbRefPolicyProviderConf, maxPolicySetRefDepth, strictAttributeIssuerMatch, environmentProperties);
		// Use static resolution if possible
		final RootPolicyEvaluator staticRootPolicyEvaluator = candidateRootPolicyEvaluator.toStatic();
		if (staticRootPolicyEvaluator == null)
		{
			this.rootPolicyEvaluator = candidateRootPolicyEvaluator;
		}
		else
		{
			this.rootPolicyEvaluator = staticRootPolicyEvaluator;
		}

		this.reqFilter = requestFilter;

		// decision cache
		if (jaxbDecisionCacheConf == null)
		{
			this.decisionCache = null;
		}
		else
		{
			final DecisionCache.Factory<?> responseCacheStoreFactory = PdpExtensionLoader.getJaxbBoundExtension(DecisionCache.Factory.class, jaxbDecisionCacheConf.getClass());
			this.decisionCache = ((DecisionCache.Factory<AbstractDecisionCache>) responseCacheStoreFactory).getInstance(jaxbDecisionCacheConf);
		}

		final StandardEnvironmentAttributeSource validStdEnvAttrSrc = stdEnvAttributeSource == null ? DEFAULT_STD_ENV_ATTRIBUTE_SOURCE : stdEnvAttributeSource;
		this.pdpStdEnvAttributeIssuer = validStdEnvAttrSrc == StandardEnvironmentAttributeSource.REQUEST_ONLY ? NULL_STD_ENV_ATTRIBUTE_ISSUER : DEFAULT_TZ_BASED_STD_ENV_ATTRIBUTE_ISSUER;
		this.individualReqEvaluator = this.decisionCache == null ? new NonCachingIndividualDecisionRequestEvaluator(rootPolicyEvaluator, validStdEnvAttrSrc, decisionResultFilter)
				: new CachingIndividualRequestEvaluator(rootPolicyEvaluator, validStdEnvAttrSrc, decisionResultFilter, this.decisionCache);

		this.badRequestStatusDetailLevel = badRequestStatusDetailLevel;
	}

	private static boolean isXpathBased(final Function<?> function)
	{
		/*
		 * A function is said "XPath-based" iff it takes at least one XPathExpression parameter. Regarding higher-order function, as of now, we only provide higher-order functions defined in the XACML
		 * (3.0) Core specification, which are not XPath-based, or if a higher-order function happens to take a XPathExpression parameter, it is actually a parameter to the first-order sub-function.
		 * Plus it is not possible to add extensions that are higher-order functions in this PDP implementation. Therefore, it is enough to check first-order functions (class FirstOrderFunction) only.
		 * (Remember that such functions may be used as parameter to a higher-order function.)
		 */
		if (function instanceof FirstOrderFunction)
		{
			final List<? extends Datatype<?>> paramTypes = ((FirstOrderFunction<?>) function).getParameterTypes();
			for (final Datatype<?> paramType : paramTypes)
			{
				if (paramType.getId().equals(XACMLDatatypeId.XPATH_EXPRESSION.value()))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Get PDP instance
	 *
	 * @param pdpJaxbConf
	 *            (JAXB-bound) PDP configuration
	 * @param envProps
	 *            PDP configuration environment properties (e.g. PARENT_DIR)
	 * @return PDP instance
	 * @throws java.lang.IllegalArgumentException
	 *             invalid PDP configuration
	 * @throws java.io.IOException
	 *             if any error occurred closing already created {@link Closeable} modules (policy Providers, attribute Providers, decision cache)
	 */
	public static BasePdpEngine getInstance(final Pdp pdpJaxbConf, final EnvironmentProperties envProps) throws IllegalArgumentException, IOException
	{
		/*
		 * Initialize all parameters of ExpressionFactoryImpl: attribute datatype factories, functions, etc.
		 */

		final boolean enableXPath = pdpJaxbConf.isEnableXPath();

		// Attribute datatypes
		// Extensions
		final List<String> datatypeExtensionIdentifiers = pdpJaxbConf.getAttributeDatatypes();
		final Set<DatatypeFactory<?>> datatypeExtensions = HashCollections.newUpdatableSet(datatypeExtensionIdentifiers.size());
		for (final String datatypeId : datatypeExtensionIdentifiers)
		{
			final DatatypeFactory<?> datatypeFactory = PdpExtensionLoader.getExtension(DatatypeFactory.class, datatypeId);
			datatypeExtensions.add(datatypeFactory);
		}

		/*
		 * Merge with standards if required, or use the standards as is if no extension
		 */
		final DatatypeFactoryRegistry datatypeFactoryRegistry;
		if (pdpJaxbConf.isUseStandardDatatypes())
		{
			final DatatypeFactoryRegistry stdRegistry = StandardDatatypeFactoryRegistry.getRegistry(enableXPath);
			if (datatypeExtensionIdentifiers.isEmpty())
			{
				datatypeFactoryRegistry = stdRegistry;
			}
			else
			{
				datatypeFactoryRegistry = new ImmutableDatatypeFactoryRegistry(HashCollections.newImmutableSet(stdRegistry.getExtensions(), datatypeExtensions));
			}
		}
		else
		{
			datatypeFactoryRegistry = new ImmutableDatatypeFactoryRegistry(datatypeExtensions);
		}

		// Functions
		// Extensions (only non-generic functions supported in configuration)
		final List<String> nonGenericFunctionExtensionIdentifiers = pdpJaxbConf.getFunctions();
		final Set<Function<?>> nonGenericFunctionExtensions = HashCollections.newUpdatableSet(nonGenericFunctionExtensionIdentifiers.size());
		for (final String funcId : nonGenericFunctionExtensionIdentifiers)
		{
			final Function<?> function = PdpExtensionLoader.getExtension(Function.class, funcId);
			if (!enableXPath && isXpathBased(function))
			{
				throw new IllegalArgumentException("XPath-based function not allowed (because configuration parameter 'enableXPath' = false): " + function);
			}

			nonGenericFunctionExtensions.add(function);
		}

		/*
		 * Merge with standards if required, or use the standards as is if no extension
		 */
		final FunctionRegistry functionRegistry;
		if (pdpJaxbConf.isUseStandardFunctions())
		{
			final FunctionRegistry stdRegistry = StandardFunction.getRegistry(enableXPath);
			if (nonGenericFunctionExtensionIdentifiers.isEmpty())
			{
				functionRegistry = stdRegistry;
			}
			else
			{
				functionRegistry = new ImmutableFunctionRegistry(HashCollections.newImmutableSet(stdRegistry.getNonGenericFunctions(), nonGenericFunctionExtensions),
						stdRegistry.getGenericFunctionFactories());
			}
		}
		else
		{
			functionRegistry = new ImmutableFunctionRegistry(nonGenericFunctionExtensions, null);
		}

		// Combining Algorithms
		// Extensions
		final List<String> algExtensionIdentifiers = pdpJaxbConf.getCombiningAlgorithms();
		final Set<CombiningAlg<?>> algExtensions = HashCollections.newUpdatableSet(algExtensionIdentifiers.size());
		for (final String algId : algExtensionIdentifiers)
		{
			final CombiningAlg<?> alg = PdpExtensionLoader.getExtension(CombiningAlg.class, algId);
			algExtensions.add(alg);
		}

		/*
		 * Merge with standards if required, or use the standards as is if no extension
		 */
		final CombiningAlgRegistry combiningAlgRegistry;
		if (pdpJaxbConf.isUseStandardCombiningAlgorithms())
		{
			if (algExtensions.isEmpty())
			{
				combiningAlgRegistry = StandardCombiningAlgorithm.REGISTRY;
			}
			else
			{
				combiningAlgRegistry = new ImmutableCombiningAlgRegistry(HashCollections.newImmutableSet(StandardCombiningAlgorithm.REGISTRY.getExtensions(), algExtensions));
			}
		}
		else
		{
			combiningAlgRegistry = new ImmutableCombiningAlgRegistry(algExtensions);
		}

		// Decision combiner
		final String resultFilterId = pdpJaxbConf.getResultFilter();
		final DecisionResultFilter decisionResultFilter = resultFilterId == null ? null : PdpExtensionLoader.getExtension(DecisionResultFilter.class, resultFilterId);

		// decision cache
		final AbstractDecisionCache jaxbDecisionCache = pdpJaxbConf.getDecisionCache();

		final BigInteger bigMaxVarRefDepth = pdpJaxbConf.getMaxVariableRefDepth();
		final int maxVarRefDepth;
		try
		{
			maxVarRefDepth = bigMaxVarRefDepth == null ? -1 : bigMaxVarRefDepth.intValueExact();
		}
		catch (final ArithmeticException e)
		{
			throw new IllegalArgumentException("Invalid maxVariableRefDepth: " + bigMaxVarRefDepth, e);
		}

		final BigInteger bigMaxPolicyRefDepth = pdpJaxbConf.getMaxPolicyRefDepth();
		final int maxPolicyRefDepth;
		try
		{
			maxPolicyRefDepth = bigMaxPolicyRefDepth == null ? -1 : bigMaxPolicyRefDepth.intValueExact();
		}
		catch (final ArithmeticException e)
		{
			throw new IllegalArgumentException("Invalid maxPolicyRefDepth: " + bigMaxPolicyRefDepth, e);
		}

		return new BasePdpEngine(datatypeFactoryRegistry, functionRegistry, pdpJaxbConf.getAttributeProviders(), maxVarRefDepth, enableXPath, combiningAlgRegistry,
				pdpJaxbConf.getRootPolicyProvider(), pdpJaxbConf.getRefPolicyProvider(), maxPolicyRefDepth, pdpJaxbConf.getRequestFilter(), pdpJaxbConf.isStrictAttributeIssuerMatch(),
				pdpJaxbConf.getStandardEnvAttributeSource(), decisionResultFilter, jaxbDecisionCache, pdpJaxbConf.getBadRequestStatusDetailLevel().intValue(), envProps);
	}

	/**
	 * Create PDP instance
	 * <p>
	 * To allow using file paths relative to the parent folder of the configuration file (located at confLocation) anywhere in this configuration file (including in PDP extensions'), we define a
	 * property 'PARENT_DIR', so that the placeholder ${PARENT_DIR} can be used as prefix for file paths in the configuration file. E.g. if confLocation = 'file:///path/to/configurationfile', then
	 * ${PARENT_DIR} will be replaced by 'file:///path/to'. If confLocation is not a file on the filesystem, then ${PARENT_DIR} is undefined.
	 *
	 * @param confFile
	 *            PDP configuration file
	 * @param modelHandler
	 *            PDP configuration model handler
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confFile}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration in {@code confFile}
	 */
	public static BasePdpEngine getInstance(final File confFile, final PdpModelHandler modelHandler) throws IOException, IllegalArgumentException
	{
		if (confFile == null || !confFile.exists())
		{
			// no property replacement of PARENT_DIR
			throw new IllegalArgumentException("Invalid configuration file location: No file exists at: " + confFile);
		}

		if (modelHandler == null)
		{
			throw NULL_PDP_MODEL_HANDLER_ARGUMENT_EXCEPTION;
		}

		// configuration file exists
		final Pdp pdpJaxbConf;
		try
		{
			pdpJaxbConf = modelHandler.unmarshal(new StreamSource(confFile), Pdp.class);
		}
		catch (final JAXBException e)
		{
			throw new IllegalArgumentException("Invalid PDP configuration file", e);
		}

		// Set property PARENT_DIR in environment properties for future
		// replacement in configuration strings by PDP extensions using file
		// paths
		final String propVal = confFile.getParentFile().toURI().toString();
		LOGGER.debug("Property {} = {}", EnvironmentPropertyName.PARENT_DIR, propVal);
		final EnvironmentProperties envProps = new DefaultEnvironmentProperties(Collections.singletonMap(EnvironmentPropertyName.PARENT_DIR, propVal));
		return getInstance(pdpJaxbConf, envProps);
	}

	/**
	 * Create PDP instance. Locations here can be any resource string supported by Spring ResourceLoader. More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html
	 * /resources.html.
	 * <p>
	 * To allow using file paths relative to the parent folder of the configuration file (located at confLocation) anywhere in this configuration file (including in PDP extensions'), we define a
	 * property 'PARENT_DIR', so that the placeholder ${PARENT_DIR} can be used as prefix for file paths in the configuration file. E.g. if confLocation = 'file:///path/to/configurationfile', then
	 * ${PARENT_DIR} will be replaced by 'file:///path/to'. If confLocation is not a file on the filesystem, then ${PARENT_DIR} is undefined.
	 *
	 * @param confLocation
	 *            location of PDP configuration file
	 * @param modelHandler
	 *            PDP configuration model handler
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static BasePdpEngine getInstance(final String confLocation, final PdpModelHandler modelHandler) throws IOException, IllegalArgumentException
	{
		File confFile = null;
		try
		{
			confFile = ResourceUtils.getFile(confLocation);
		}
		catch (final FileNotFoundException e)
		{
			throw new IllegalArgumentException("Invalid PDP configuration location: " + confLocation, e);
		}

		return getInstance(confFile, modelHandler);
	}

	/**
	 * Create PDP instance. Locations here can be any resource string supported by Spring ResourceLoader. More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html
	 * /resources.html
	 *
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 *
	 * @param confFile
	 *            PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD namespace import for each extension used in the PDP
	 *            configuration, for example:
	 *
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?>
	 * <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
	 * 	<xs:annotation>
	 * 		<xs:documentation xml:lang="en">
	 * 			Import here the schema(s) of any XSD-defined PDP extension that you want to use in a PDP configuration: attribute finders, policy finders, etc.
	 * 			Indicate only the namespace here and use the XML catalog to resolve the schema location.
	 * 		</xs:documentation>
	 * 	</xs:annotation>
	 * 	<!-- Do not specify schema locations here. Define the schema locations in the XML catalog instead (see file 'catalog.xml'). -->
	 * 	<!--  Adding TestAttributeProvider extension for example -->
	 * 	<xs:import namespace="http://authzforce.github.io/core/xmlns/test/3" />
	 * </xs:schema>
	 * 			}
	 * </pre>
	 *
	 *            In this example, the file at {@code catalogLocation} must define the schemaLocation for the imported namespace above using a line like this (for an XML-formatted catalog):
	 * 
	 *            <pre>
	 *            {@literal
	 *            <uri name="http://authzforce.github.io/core/xmlns/test/3" uri=
	 * 	"classpath:org.ow2.authzforce.core.test.xsd" />
	 *            }
	 * </pre>
	 * 
	 *            We assume that this XML type is an extension of one the PDP extension base types, 'AbstractAttributeProvider' (that extends 'AbstractPdpExtension' like all other extension base
	 *            types) in this case.
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the extension XSD specified as 'extensionXsdLocation' argument (may be null if 'extensionXsdLocation' is null)
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static BasePdpEngine getInstance(final File confFile, final String catalogLocation, final String extensionXsdLocation) throws IOException, IllegalArgumentException
	{
		return getInstance(confFile, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	/**
	 * Create PDP instance. Locations here may be any resource string supported by Spring ResourceLoader. More info: http://docs.spring.io/spring/docs/current/spring-framework-reference/html
	 * /resources.html
	 *
	 * For example: classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd...
	 *
	 * @param confLocation
	 *            location of PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd)
	 * @param extensionXsdLocation
	 *            location of user-defined extension XSD (may be null if no extension to load), if exists; in such XSD, there must be a XSD namespace import for each extension used in the PDP
	 *            configuration, for example:
	 *
	 *            <pre>
	 * {@literal
	 * 		  <?xml version="1.0" encoding="UTF-8"?>
	 * <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
	 * 	<xs:annotation>
	 * 		<xs:documentation xml:lang="en">
	 * 			Import here the schema(s) of any XSD-defined PDP extension that you want to use in a PDP configuration: attribute finders, policy finders, etc.
	 * 			Indicate only the namespace here and use the XML catalog to resolve the schema location.
	 * 		</xs:documentation>
	 * 	</xs:annotation>
	 * 	<!-- Do not specify schema locations here. Define the schema locations in the XML catalog instead (see file 'catalog.xml'). -->
	 * 	<!--  Adding TestAttributeProvider extension for example -->
	 * 	<xs:import namespace="http://authzforce.github.io/core/xmlns/test/3" />
	 * </xs:schema>
	 * 			}
	 * </pre>
	 *
	 *            In this example, the file at {@code catalogLocation} must define the schemaLocation for the imported namespace above using a line like this (for an XML-formatted catalog):
	 * 
	 *            <pre>
	 *            {@literal
	 *            <uri name="http://authzforce.github.io/core/xmlns/test/3" uri=
	 * 	"classpath:org.ow2.authzforce.core.test.xsd" />
	 *            }
	 * </pre>
	 * 
	 *            We assume that this XML type is an extension of one the PDP extension base types, 'AbstractAttributeProvider' (that extends 'AbstractPdpExtension' like all other extension base
	 *            types) in this case.
	 * @param catalogLocation
	 *            location of XML catalog for resolving XSDs imported by the extension XSD specified as 'extensionXsdLocation' argument (may be null if 'extensionXsdLocation' is null)
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static BasePdpEngine getInstance(final String confLocation, final String catalogLocation, final String extensionXsdLocation) throws IOException, IllegalArgumentException
	{
		return getInstance(confLocation, new PdpModelHandler(catalogLocation, extensionXsdLocation));
	}

	/**
	 * Create PDP instance.
	 *
	 * @param confLocation
	 *            location of PDP configuration XML file, compliant with the PDP XML schema (pdp.xsd). This location may be any resource string supported by Spring ResourceLoader. For example:
	 *            classpath:com/myapp/aaa.xsd, file:///data/bbb.xsd, http://myserver/ccc.xsd... More info: http://docs.spring.io/spring/docs/current/spring-framework- reference/html/resources.html
	 * @return PDP instance
	 * @throws java.io.IOException
	 *             I/O error reading from {@code confLocation}
	 * @throws java.lang.IllegalArgumentException
	 *             Invalid PDP configuration at {@code confLocation}
	 */
	public static BasePdpEngine getInstance(final String confLocation) throws IOException, IllegalArgumentException
	{
		return getInstance(confLocation, null, null);
	}

	@Override
	public PdpDecisionRequestBuilder<ImmutablePdpDecisionRequest> newRequestBuilder(final int expectedNumOfAttributeCategories, final int expectedTotalNumOfAttributes)
	{
		return this.strictAttributeIssuerMatch ? new NonIssuedLikeIssuedAttributeHandlingRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes)
				: new IssuedToNonIssuedAttributeCopyingRequestBuilder(expectedNumOfAttributeCategories, expectedTotalNumOfAttributes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PdpDecisionResult evaluate(final ImmutablePdpDecisionRequest individualDecisionRequest)
	{
		if (individualDecisionRequest == null)
		{
			throw ILLEGAL_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision request with extra common attributes set by the PDP once for all: standard environment attributes, i.e. current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler" . These current date/time values must be set here once
		 * before an individual request is evaluated to make sure it uses the same value for current-time/current-date/current-dateTime during the entire evaluation, if they use the one from PDP.
		 */
		return individualReqEvaluator.evaluate(individualDecisionRequest, this.pdpStdEnvAttributeIssuer.get());
	}

	/** {@inheritDoc} */
	@Override
	public Map<ImmutablePdpDecisionRequest, ? extends PdpDecisionResult> evaluate(final List<ImmutablePdpDecisionRequest> individualDecisionRequests) throws IndeterminateEvaluationException
	{
		if (individualDecisionRequests == null)
		{
			throw ILLEGAL_ARGUMENT_EXCEPTION;
		}

		/*
		 * Evaluate the individual decision requests with extra common attributes set by the PDP once for all: standard environment attributes, i.e. current-time, etc. XACML standard (§10.2.5) says:
		 * "If values for these attributes are not present in the decision request, then their values MUST be supplied by the context handler" . These current date/time values must be set here once
		 * before every individual request is evaluated to make sure they all use the same value for current-time/current-date/current-dateTime, if they use the one from PDP.
		 */
		return individualReqEvaluator.evaluate(individualDecisionRequests, this.pdpStdEnvAttributeIssuer.get());
	}

	/** {@inheritDoc} */
	@Override
	public Response evaluate(final Request request, final Map<String, String> namespaceURIsByPrefix)
	{
		if (request == null)
		{
			throw ILLEGAL_ARGUMENT_EXCEPTION;
		}

		/*
		 * No support for CombinedDecision = true if no decisionCombiner defined. (The use of the CombinedDecision attribute is specified in Multiple Decision Profile.)
		 */
		if (request.isCombinedDecision() && !this.individualReqEvaluator.supportsMultipleDecisionCombining())
		{
			/*
			 * According to XACML core spec, 5.42, "If the PDP does not implement the relevant functionality in [Multiple Decision Profile], then the PDP must return an Indeterminate with a status
			 * code of urn:oasis:names:tc:xacml:1.0:status:processing-error if it receives a request with this attribute set to "true".
			 */
			return UNSUPPORTED_COMBINED_DECISION_RESPONSE;
		}

		/*
		 * The request parser may return multiple individual decision requests from a single Request, e.g. if the request parser implements the Multiple Decision profile or Hierarchical Resource
		 * profile
		 */
		final List<? extends IndividualDecisionRequest> individualDecisionRequests;
		try
		{
			individualDecisionRequests = reqFilter.filter(request, namespaceURIsByPrefix);
		}
		catch (final IndeterminateEvaluationException e)
		{
			LOGGER.info("Invalid or unsupported input XACML Request syntax", e);
			return new Response(Collections.<Result> singletonList(new Result(DecisionType.INDETERMINATE, e.getStatus(badRequestStatusDetailLevel), null, null, null, null)));
		}

		final List<Result> results = individualReqEvaluator.evaluateToJAXB(individualDecisionRequests, this.pdpStdEnvAttributeIssuer.get());
		return new Response(results);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		rootPolicyEvaluator.close();
		if (decisionCache != null)
		{
			decisionCache.close();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Response evaluate(final Request request)
	{
		return evaluate(request, null);
	}

	/**
	 * Get the PDP's root policy and policies referenced - directly or indirectly - from the root policy, if all are statically resolved
	 *
	 * @return the root and referenced policies; null if any of these policies is not statically resolved (once and for all)
	 */
	public StaticApplicablePolicyView getStaticApplicablePolicies()
	{
		return this.rootPolicyEvaluator.getStaticApplicablePolicies();
	}

}
