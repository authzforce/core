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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.AttributeGUID;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.impl.policy.RootPolicyEvaluator;
import org.ow2.authzforce.core.xmlns.pdp.StandardEnvironmentAttributeSource;

/**
 * Individual decision request evaluator
 *
 * @version $Id: $
 */
public abstract class IndividualDecisionRequestEvaluator
{

	private interface RequestAndPdpIssuedNamedAttributesMerger
	{
		Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes);
	}

	private static final RequestAndPdpIssuedNamedAttributesMerger REQUEST_OVERRIDES_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes)
		{
			/*
			 * Request attribute values override PDP issued ones. Do not modify pdpIssuedAttributes directly as this may be used for other requests (Multiple Decision Profile) as well. so we must not
			 * modify it but clone it before individual decision request processing.
			 */
			final Map<AttributeGUID, Bag<?>> mergedAttributes;
			if (pdpIssuedAttributes != null)
			{
				mergedAttributes = new HashMap<>(pdpIssuedAttributes);
			}
			else if (requestAttributes != null) // pdpIssuedAttributes == null
			{
				return new HashMap<>(requestAttributes);
			}
			else
			{ // pdpIssuedAttributes == null && requestAttributes == null
				return null;
			}

			mergedAttributes.putAll(requestAttributes);
			return mergedAttributes;
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
			final Map<AttributeGUID, Bag<?>> mergedAttributes;
			if (requestAttributes != null)
			{
				mergedAttributes = new HashMap<>(requestAttributes);
			}
			else if (pdpIssuedAttributes != null) // requestAttributes == null
			{
				return new HashMap<>(pdpIssuedAttributes);
			}
			else
			{ // requestAttributes == null && pdpIssuedAttributes == null
				return null;
			}

			mergedAttributes.putAll(pdpIssuedAttributes);
			return mergedAttributes;

		}

	};

	private static final RequestAndPdpIssuedNamedAttributesMerger REQUEST_ONLY_ATTRIBUTES_MERGER = new RequestAndPdpIssuedNamedAttributesMerger()
	{

		@Override
		public Map<AttributeGUID, Bag<?>> merge(final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final Map<AttributeGUID, Bag<?>> requestAttributes)
		{
			// PDP values completely ignored
			return requestAttributes == null ? null : new HashMap<>(requestAttributes);
		}

	};

	private final RootPolicyEvaluator rootPolicyEvaluator;
	private final RequestAndPdpIssuedNamedAttributesMerger reqAndPdpIssuedAttributesMerger;

	/**
	 * Creates an evaluator
	 *
	 * @param rootPolicyEvaluator
	 *            root policy evaluator that this request evaluator uses to evaluate individual decision request
	 * @param stdEnvAttributeSource
	 *            (mandatory) Defines the source for the standard environment attributes specified in §10.2.5: current-time, current-date and current-dateTime. The options are:
	 *            <ul>
	 *            <li>REQUEST_ELSE_PDP: the default choice, that complies with the XACML standard (§10.2.5): "If values for these attributes are not present in the decision request, then their values
	 *            MUST be supplied by the context handler", in our case, "context handler" means the PDP. In other words, the attribute values come from request by default, or from the PDP if (and
	 *            *only if* in this case) they are not set in the request. Issue: what if the decision request only specifies current-time but not current-dateTime, and the policy requires both?
	 *            Should the PDP provides its own value for current-dateTime? This could cause some inconsistencies since current-time and current-dateTime would come from two different
	 *            sources/environments. With this option, we have a strict interpretation of the spec, i.e. if any of these attribute is not set in the request, the PDP uses its own value instead. So
	 *            BEWARE. Else you have the other options below.</li>
	 *            <li>REQUEST_ONLY: always use the value from the request, or nothing if the value is not set in the request, in which case this results in Indeterminate (missing attribute) if the
	 *            policy evaluation requires it.</li>
	 *            <li>PDP_ONLY: always use the values from the PDP. In other words, Request values are simply ignored; PDP values systematically override the ones from the request. This also
	 *            guarantees that they are always set (by the PDP). NB: note that the XACML standard (§10.2.5) says: "If values for these attributes are not present in the decision request, then their
	 *            values MUST be supplied by the context handler" but it does NOT say "If AND ONLY IF values..." So this option could still be considered XACML compliant in a strict sense.</li>
	 *            </ul>
	 * @throws IllegalArgumentException
	 *             if {@code stdEnvAttributeSource} is null or not supported
	 */
	protected IndividualDecisionRequestEvaluator(final RootPolicyEvaluator rootPolicyEvaluator, final StandardEnvironmentAttributeSource stdEnvAttributeSource) throws IllegalArgumentException
	{
		assert rootPolicyEvaluator != null && stdEnvAttributeSource != null;
		this.rootPolicyEvaluator = rootPolicyEvaluator;
		switch (stdEnvAttributeSource)
		{
			case PDP_ONLY:
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
	}

	/**
	 * <p>
	 * evaluate
	 * </p>
	 *
	 * @param request
	 *            a non-null {@link org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest} object.
	 * @param pdpIssuedAttributes
	 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
	 * @param returnUsedAttributes
	 *            true iff the list of attributes used for evaluation must be included in the result
	 * @return the evaluation result.
	 */
	protected final DecisionResult evaluate(final IndividualDecisionRequest request, final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes, final boolean returnUsedAttributes)
	{
		assert request != null;

		// convert to EvaluationContext
		final Map<AttributeGUID, Bag<?>> mergedNamedAttributes = reqAndPdpIssuedAttributesMerger.merge(pdpIssuedAttributes, request.getNamedAttributes());
		final EvaluationContext ctx = new IndividualDecisionRequestContext(mergedNamedAttributes, request.getExtraContentsByCategory(), request.isApplicablePolicyIdListReturned(),
				returnUsedAttributes);
		return rootPolicyEvaluator.findAndEvaluate(ctx);
	}

	/**
	 * <p>
	 * evaluate
	 * </p>
	 *
	 * @param individualDecisionRequests
	 *            a {@link java.util.List} of individual decision requests.
	 * @param pdpIssuedAttributes
	 *            a {@link java.util.Map} of PDP-issued attributes including at least the standard environment attributes: current-time, current-date, current-dateTime.
	 * @return a {@link java.util.List} of evaluation results (one per individual decision request).
	 */
	protected abstract <INDIVIDUAL_DECISION_REQ_T extends IndividualDecisionRequest> List<Result> evaluate(List<INDIVIDUAL_DECISION_REQ_T> individualDecisionRequests,
			final Map<AttributeGUID, Bag<?>> pdpIssuedAttributes);
}
