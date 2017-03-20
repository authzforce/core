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
package org.ow2.authzforce.core.pdp.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.DecisionResultFilter.FilteringResultCollector;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.IndividualXACMLRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.xmlns.pdp.StandardEnvironmentAttributeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Individual decision request evaluator
 *
 * @version $Id: $
 */
public abstract class IndividualDecisionRequestEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(IndividualDecisionRequestEvaluator.class);

	private interface RequestAndPdpIssuedNamedAttributesMerger
	{
		/**
		 * Return an updatable map after merging {@code pdpIssuedAttributes} and {@code requestAttributes} or one of each into it, depending on the implementation
		 * 
		 * @param pdpIssuedAttributes
		 * @param requestAttributes
		 * @return updatable map resulting from merger, or null if nothing merged
		 */
		Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes);
	}

	private static final IndeterminateEvaluationException newReqMissingStdEnvAttrException(final AttributeGUID attrGUID)
	{
		return new IndeterminateEvaluationException("The standard environment attribute ( " + attrGUID
				+ " ) is not present in the REQUEST although at least one of the others is! (PDP standardEnvironmentAttributeSource = REQUEST_ELSE_PDP.)", StatusHelper.STATUS_MISSING_ATTRIBUTE);
	}

	private static final Map<AttributeGUID, Bag<?>> STD_ENV_RESET_MAP = HashCollections.<AttributeGUID, Bag<?>> newImmutableMap(StandardEnvironmentAttribute.CURRENT_DATETIME.getGUID(),
			Bags.empty(StandardDatatypes.DATETIME_FACTORY.getDatatype(), newReqMissingStdEnvAttrException(StandardEnvironmentAttribute.CURRENT_DATETIME.getGUID())),
			StandardEnvironmentAttribute.CURRENT_DATE.getGUID(),
			Bags.empty(StandardDatatypes.DATE_FACTORY.getDatatype(), newReqMissingStdEnvAttrException(StandardEnvironmentAttribute.CURRENT_DATE.getGUID())),
			StandardEnvironmentAttribute.CURRENT_TIME.getGUID(),
			Bags.empty(StandardDatatypes.TIME_FACTORY.getDatatype(), newReqMissingStdEnvAttrException(StandardEnvironmentAttribute.CURRENT_TIME.getGUID())));

	private static final RequestAndPdpIssuedNamedAttributesMerger REQUEST_OVERRIDES_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes)
		{
			/*
			 * Request attribute values override PDP issued ones. Do not modify pdpIssuedAttributes directly as this may be used for other requests (Multiple Decision Profile) as well. so we must not
			 * modify it but clone it before individual decision request processing.
			 */
			if (pdpIssuedAttributes == null)
			{
				return requestAttributes == null ? null : HashCollections.newUpdatableMap(requestAttributes);
			}

			// pdpIssuedAttributes != null
			if (requestAttributes == null)
			{
				return HashCollections.newUpdatableMap(pdpIssuedAttributes);
			}
			// requestAttributes != null

			/**
			 * 
			 * XACML standard (§10.2.5) says: "If values for these [the standard environment attributes, i.e. current-time, current-date, current-dateTime] attributes are not present in the decision
			 * request, then their values MUST be supplied by the context handler ". In our case, "context handler" means the PDP. In other words, the attribute values come from request by default, or
			 * from the PDP if (and *only if* in this case) they are not set in the request. More precisely, if any of these standard environment attributes is provided in the request, none of the PDP
			 * values is used, even if some policy requires one that is missing from the request. Indeed, this is to avoid such case when the decision request specifies at least one date/time
			 * attribute, e.g. current-time, but not all of them, e.g. not current-dateTime, and the policy requires both the one(s) provided and the one(s) not provided. In this case, if the PDP
			 * provides its own value(s) for the missing attributes (e.g. current-dateTime), this may cause some inconsistencies since we end up having date/time attributes coming from two different
			 * sources/environments (current-time and current-dateTime for instance).
			 */
			if (requestAttributes.containsKey(StandardEnvironmentAttribute.CURRENT_DATETIME.getGUID()) || requestAttributes.containsKey(StandardEnvironmentAttribute.CURRENT_DATE.getGUID())
					|| requestAttributes.containsKey(StandardEnvironmentAttribute.CURRENT_TIME.getGUID()))
			{
				/*
				 * Request has at least one standard env attribute -> make sure all PDP values are ignored (overridden by STD_ENV_RESET_MAP no matter whether requestAttributes contains all of them or
				 * not)
				 */
				// mappings in order of increasing priority
				return HashCollections.newUpdatableMap(pdpIssuedAttributes, STD_ENV_RESET_MAP, requestAttributes);
			}

			// mappings in order of increasing priority
			return HashCollections.newUpdatableMap(pdpIssuedAttributes, requestAttributes);
		}

	};

	private static final RequestAndPdpIssuedNamedAttributesMerger PDP_OVERRIDES_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes)
		{

			// PDP issued attribute values override request attribute values
			/*
			 * Do not modify pdpIssuedAttributes directly as this may be used for other requests (Multiple Decision Profile) as well. so we must not modify it but clone it before individual decision
			 * request processing.
			 */
			if (pdpIssuedAttributes == null)
			{
				return requestAttributes == null ? null : HashCollections.newUpdatableMap(requestAttributes);
			}

			// pdpIssuedAttributes != null
			if (requestAttributes == null)
			{
				return HashCollections.newUpdatableMap(pdpIssuedAttributes);
			}
			// requestAttributes != null

			// mappings of pdpIssuedAttributes have priority
			return HashCollections.newUpdatableMap(requestAttributes, pdpIssuedAttributes);

		}

	};

	private static final RequestAndPdpIssuedNamedAttributesMerger REQUEST_ONLY_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes)
		{
			// PDP values completely ignored
			return requestAttributes == null ? null : HashCollections.newUpdatableMap(requestAttributes);
		}

	};

	private static final class DefaultResultCollector implements FilteringResultCollector
	{
		private final List<Result> results;

		private DefaultResultCollector(final int numberOfFilteredResults)
		{

			results = new ArrayList<>(numberOfFilteredResults);
		}

		@Override
		public List<Result> addResult(final IndividualXACMLRequest request, final PdpDecisionResult result)
		{
			results.add(result.toXACMLResult(request.getAttributesToBeReturned()));
			return null;
		}

		@Override
		public List<Result> getFilteredResults()
		{
			return results;
		}

	}

	private static final DecisionResultFilter DEFAULT_RESULT_FILTER = new DecisionResultFilter()
	{
		private static final String ID = "urn:ow2:authzforce:feature:pdp:result-filter:default";

		@Override
		public String getId()
		{
			return ID;
		}

		@Override
		public boolean supportsMultipleDecisionCombining()
		{
			return false;
		}

		@Override
		public FilteringResultCollector newResultCollector(final int numberOfInputResults)
		{
			return new DefaultResultCollector(numberOfInputResults);
		}

	};

	private final RootPolicyEvaluator rootPolicyEvaluator;
	private final RequestAndPdpIssuedNamedAttributesMerger reqAndPdpIssuedAttributesMerger;

	private final DecisionResultFilter decisionResultFilter;

	/**
	 * Creates an evaluator
	 *
	 * @param rootPolicyEvaluator
	 *            root policy evaluator that this request evaluator uses to evaluate individual decision request
	 * @param stdEnvAttributeSource
	 *            (mandatory) Defines the source for the standard environment attributes specified in §10.2.5: current-time, current-date and current-dateTime. The options are:
	 *            <ul>
	 *            <li>REQUEST_ELSE_PDP: the default choice, that complies with the XACML standard (§10.2.5): "If values for these attributes are not present in the decision request, then their values
	 *            MUST be supplied by the context handler", in our case, " context handler" means the PDP. In other words, the attribute values come from request by default, or from the PDP if (and
	 *            *only if* in this case) they are not set in the request. Issue: what if the decision request only specifies current-time but not current-dateTime, and the policy requires both?
	 *            Should the PDP provides its own value for current-dateTime? This could cause some inconsistencies since current-time and current-dateTime would come from two different
	 *            sources/environments. With this option, we have a strict interpretation of the spec, i.e. if any of these attribute is not set in the request, the PDP uses its own value instead. So
	 *            BEWARE. Else you have the other options below.</li>
	 *            <li>REQUEST_ONLY: always use the standard environment attribute value from the request, or nothing if the value is not set in the request, in which case this results in Indeterminate
	 *            (missing attribute) if the policy evaluation requires it.</li>
	 *            <li>PDP_ONLY: always use the standard environment attribute values from the PDP. In other words, Request values are simply ignored; PDP values for standard environment attributes
	 *            systematically override the ones from the request. This also guarantees that they are always set (by the PDP). NB: note that the XACML standard (§10.2.5) says: "If values for these
	 *            attributes are not present in the decision request, then their values MUST be supplied by the context handler " but it does NOT say "If AND ONLY IF values..." So this option could
	 *            still be considered XACML compliant in a strict sense.</li>
	 *            </ul>
	 * @param resultFilter
	 *            Decision Result filter
	 * @throws IllegalArgumentException
	 *             if {@code stdEnvAttributeSource} is null or not supported
	 */
	protected IndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource, final DecisionResultFilter resultFilter)
			throws IllegalArgumentException
	{
		assert rootPolicyEvaluator != null && stdEnvAttributeSource != null;
		this.rootPolicyEvaluator = rootPolicyEvaluator;
		switch (stdEnvAttributeSource)
		{
			case PDP_ONLY:
				/*
				 * PDP_ONLY means the standard environment attribute values come from the PDP only (not the Request), this does not affect other attributes. In other words, only PDP's standard
				 * environment attribute values override.
				 */
				this.reqAndPdpIssuedAttributesMerger = PDP_OVERRIDES_ATTRIBUTES_MERGER;
				break;
			case REQUEST_ONLY:
				this.reqAndPdpIssuedAttributesMerger = REQUEST_ONLY_ATTRIBUTES_MERGER;
				break;
			case REQUEST_ELSE_PDP:
				this.reqAndPdpIssuedAttributesMerger = REQUEST_OVERRIDES_ATTRIBUTES_MERGER;
				break;
			default:
				throw new IllegalArgumentException("Unsupported standardEnvAttributeSource: " + stdEnvAttributeSource + ". Expected: " + Arrays.toString(StandardEnvironmentAttributeSource.values()));
		}

		this.decisionResultFilter = resultFilter == null ? DEFAULT_RESULT_FILTER : resultFilter;
	}

	final boolean supportsMultipleDecisionCombining()
	{
		return this.decisionResultFilter.supportsMultipleDecisionCombining();
	}

	protected final FilteringResultCollector beginMultipleDecisions(final int numOfRequests)
	{
		/*
		 * There will be at most as many results as requests, so we prepare to filter at most numOfRequests results
		 */
		return decisionResultFilter.newResultCollector(numOfRequests);
	}

	/**
	 * <p>
	 * Evaluate an Individual Decision Request, with option to return attributes used by the evaluation, e.g. to improve caching mechanisms
	 * </p>
	 *
	 * @param request
	 *            a non-null {@link PdpDecisionRequest} object.
	 * @param pdpIssuedAttributes
	 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
	 * @param returnUsedAttributes
	 *            true iff the list of attributes used for evaluation must be included in the result
	 * @return the evaluation result.
	 */
	protected final PdpDecisionResult evaluate(final PdpDecisionRequest request, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final boolean returnUsedAttributes)
	{
		assert request != null;
		LOGGER.debug("Evaluating Individual Decision Request: {}", request);

		// convert to EvaluationContext
		final Map<AttributeGUID, Bag<?>> mergedNamedAttributes = reqAndPdpIssuedAttributesMerger.merge(pdpIssuedAttributes, request.getNamedAttributes());
		final EvaluationContext ctx = new IndividualDecisionRequestContext(mergedNamedAttributes, request.getContentNodesByCategory(), request.isApplicablePolicyIdListReturned(), returnUsedAttributes);
		return rootPolicyEvaluator.findAndEvaluate(ctx);
	}

	/**
	 * <p>
	 * Evaluate an Individual Decision Request.
	 * </p>
	 *
	 * @param individualDecisionRequest
	 *            an individual decision request
	 * @return the evaluation result pair
	 */
	protected abstract PdpDecisionResult evaluate(PdpDecisionRequest individualDecisionRequest, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes);

	/**
	 * <p>
	 * Evaluate multiple Individual Decision Requests with same PDP-issued attribute values (e.g. current date/time) in order to return decision results in internal model (more efficient than JAXB
	 * model derived from XACML schema as in {@link #evaluateToJAXB(List, Map)}).
	 * </p>
	 *
	 * @param individualDecisionRequests
	 *            a {@link java.util.List} of individual decision requests.
	 * @param pdpIssuedAttributes
	 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
	 * @return individual decision request-result pairs, where the list of the requests is the same as {@code individualDecisionRequests}.
	 * @throws IndeterminateEvaluationException
	 *             if an error occurred preventing any request evaluation
	 */
	protected abstract <INDIVIDUAL_DECISION_REQ_T extends PdpDecisionRequest> Map<INDIVIDUAL_DECISION_REQ_T, ? extends PdpDecisionResult> evaluate(
			List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes) throws IndeterminateEvaluationException;

	/**
	 * <p>
	 * Evaluate multiple Individual Decision Requests with same PDP-issued attribute values (e.g. current date/time) in order to return JAXB {@link Result}s. Use only if you need to produce a final
	 * XACML/JAXB Result or Response for serialization (esp. to interoperate with external systems, where external means outside the current runtime JVM), else use
	 * {@link #evaluate(PdpDecisionRequest, Map)} which is more optimal.
	 * </p>
	 *
	 * @param individualDecisionRequests
	 *            a {@link java.util.List} of individual decision requests.
	 * @param pdpIssuedAttributes
	 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
	 * @return a {@link java.util.List} of XACML {@link Result}s (one per individual decision request), ready to be included in a final XACML Response.
	 */
	protected abstract List<Result> evaluateToJAXB(List<? extends IndividualXACMLRequest> individualDecisionRequests, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes);

}
