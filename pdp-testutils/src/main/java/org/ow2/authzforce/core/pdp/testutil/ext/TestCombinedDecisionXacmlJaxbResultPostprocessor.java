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
package org.ow2.authzforce.core.pdp.testutil.ext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.ImmutableSet;

/**
 * XACML Result postprocessor implementing the functionality 'urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision' from <a
 * href="http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/xacml-3.0-multiple-v1.0.html">XACML v3.0 Multiple Decision Profile Version 1.0</a>. Edited by Erik Rissanen. 18 May 2014. OASIS Committee
 * Specification 02.
 * <p>
 * Used here for testing Authzforce Result postprocessor extension mechanism, i.e. plugging a custom decision Result postprocessor into the PDP engine.
 * <p>
 * NB: the spec does not mention the inclusion of element {@code <PolicyIdentifierList>}. At least, it does not say to remove it, so in theory there is no reason why we should not include it. However,
 * in this test implementation, we don't include any {@code <PolicyIdentifierList>} in the final result, for the sake of simplicity. So BEWARE!
 *
 */
public class TestCombinedDecisionXacmlJaxbResultPostprocessor extends BaseXacmlJaxbResultPostprocessor
{
	private static final Set<String> FEATURES = ImmutableSet.of(DecisionResultPostprocessor.Features.XACML_MULTIPLE_DECISION_PROFILE_COMBINED_DECISION);

	private static final Response SIMPLE_INDETERMINATE_RESPONSE = new Response(Collections.singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(XacmlStatusCode.PROCESSING_ERROR
			.value(), Optional.empty()), null, null, null, null)));

	// private static final List<Result> INDETERMINATE_RESULT_SINGLETON_LIST_BECAUSE_NO_INDIVIDUAL = Collections.singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(
	// StatusHelper.STATUS_PROCESSING_ERROR, "No <Result> to combine!"), null, null, null, null));

	private static final Response SIMPLE_PERMIT_RESPONSE = new Response(Collections.singletonList(new Result(DecisionType.PERMIT, StatusHelper.OK, null, null, null, null)));
	private static final Response SIMPLE_DENY_RESPONSE = new Response(Collections.singletonList(new Result(DecisionType.DENY, StatusHelper.OK, null, null, null, null)));
	private static final Response SIMPLE_NOT_APPLICABLE_RESPONSE = new Response(Collections.singletonList(new Result(DecisionType.NOT_APPLICABLE, StatusHelper.OK, null, null, null, null)));

	private TestCombinedDecisionXacmlJaxbResultPostprocessor(final int clientRequestErrorVerbosityLevel) throws IllegalArgumentException
	{
		super(clientRequestErrorVerbosityLevel);
	}

	@Override
	public Set<String> getFeatures()
	{
		return FEATURES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.BaseXacmlJaxbResultPostprocessor#process(java.util.Map)
	 */
	@Override
	public Response process(final Collection<Entry<IndividualXacmlJaxbRequest, ? extends DecisionResult>> resultsByRequest)
	{
		DecisionType combinedDecision = DecisionType.INDETERMINATE;
		for (final Entry<? extends IndividualXacmlJaxbRequest, ? extends DecisionResult> resultEntry : resultsByRequest)
		{
			final DecisionResult result = resultEntry.getValue();
			if (result.getDecision() == DecisionType.INDETERMINATE)
			{
				// either all result must be indeterminate or we return Indeterminate as final result anyway
				return SIMPLE_INDETERMINATE_RESPONSE;
			}

			final ImmutablePepActions pepActions = result.getPepActions();
			if (pepActions != null && (!pepActions.getObligatory().isEmpty() || !pepActions.getAdvisory().isEmpty()))
			{
				return SIMPLE_INDETERMINATE_RESPONSE;
			}

			final DecisionType individualDecision = result.getDecision();
			// if combinedDecision not initialized yet (indeterminate), set it to the result's decision
			if (combinedDecision == DecisionType.INDETERMINATE)
			{
				combinedDecision = individualDecision;
			}
			else
			// combinedDecision != Indeterminate
			if (individualDecision != combinedDecision)
			{
				return SIMPLE_INDETERMINATE_RESPONSE;
			}
		}

		switch (combinedDecision)
		{
			case PERMIT:
				return SIMPLE_PERMIT_RESPONSE;
			case DENY:
				return SIMPLE_DENY_RESPONSE;
			case NOT_APPLICABLE:
				return SIMPLE_NOT_APPLICABLE_RESPONSE;
			default:
				return SIMPLE_INDETERMINATE_RESPONSE;
		}
	}

	/**
	 *
	 * Factory for this type of result postprocessor filter that allows duplicate &lt;Attribute&gt; with same meta-data in the same &lt;Attributes&gt; element of a Request (complying with XACML 3.0
	 * core spec, §7.3.3).
	 *
	 */
	public static final class Factory extends BaseXacmlJaxbResultPostprocessor.Factory
	{
		/**
		 * ID of this {@link PdpExtension}
		 */
		public static final String ID = "urn:ow2:authzforce:feature:pdp:result-postproc:xacml-xml:multiple:test-combined-decision";

		/**
		 * Constructor
		 */
		public Factory()
		{
			super(ID);
		}

		@Override
		public DecisionResultPostprocessor<IndividualXacmlJaxbRequest, Response> getInstance(final int clientRequestErrorVerbosityLevel)
		{
			return new TestCombinedDecisionXacmlJaxbResultPostprocessor(clientRequestErrorVerbosityLevel);
		}

	}

}
