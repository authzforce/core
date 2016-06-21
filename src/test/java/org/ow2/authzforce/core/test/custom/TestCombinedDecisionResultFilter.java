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
package org.ow2.authzforce.core.test.custom;

import java.util.Collections;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.StatusHelper;

/**
 * XACML Result filter implementing the functionality 'urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision' from <a
 * href="http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/xacml-3.0-multiple-v1.0.html">XACML v3.0 Multiple Decision Profile Version 1.0</a>. Edited by Erik Rissanen. 18 May 2014. OASIS Committee
 * Specification 02.
 * <p>
 * Used here for testing Authzforce Result filter extension mechanism, i.e. plugging a custom decision Result filter into the PDP engine.
 * <p>
 * NB: the spec does not mention the inclusion of element {@code <PolicyIdentifierList>}. At least, it does not say to remove it, so in theory there is no reason why we should not include it. However,
 * in this test implementation, we don't include any {@code <PolicyIdentifierList>} in the final result, for the sake of simplicity. So BEWARE!
 *
 */
public class TestCombinedDecisionResultFilter implements DecisionResultFilter
{
	public static final String ID = "urn:ow2:authzforce:feature:pdp:result-filter:multiple:test-combined-decision";

	private static final List<Result> INDETERMINATE_RESULT_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR,
			null), null, null, null, null));

	private static final List<Result> INDETERMINATE_RESULT_SINGLETON_LIST_BECAUSE_NO_INDIVIDUAL = Collections.singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(
			StatusHelper.STATUS_PROCESSING_ERROR, "No <Result> to combine!"), null, null, null, null));

	private static final List<Result> PERMIT_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.PERMIT, StatusHelper.OK, null, null, null, null));
	private static final List<Result> DENY_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.DENY, StatusHelper.OK, null, null, null, null));
	private static final List<Result> NOT_APPLICABLE_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.NOT_APPLICABLE, StatusHelper.OK, null, null, null, null));

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public boolean supportsMultipleDecisionCombining()
	{
		return true;
	}

	@Override
	public List<Result> filter(List<Result> individualResults)
	{
		if (individualResults.isEmpty())
		{
			return INDETERMINATE_RESULT_SINGLETON_LIST_BECAUSE_NO_INDIVIDUAL;
		}

		DecisionType combinedDecision = DecisionType.INDETERMINATE;
		for (final Result individualResult : individualResults)
		{
			if (individualResult.getDecision() == DecisionType.INDETERMINATE)
			{
				// either all result must be indeterminate or we return Indeterminate as final result anyway
				return INDETERMINATE_RESULT_SINGLETON_LIST;
			}

			if (individualResult.getObligations() != null || individualResult.getAssociatedAdvice() != null)
			{
				return INDETERMINATE_RESULT_SINGLETON_LIST;
			}

			final DecisionType individualDecision = individualResult.getDecision();
			// if combinedDecision not initialized yet (indeterminate), set it to the result's decision
			if (combinedDecision == DecisionType.INDETERMINATE)
			{
				combinedDecision = individualDecision;
			} else if (individualDecision != combinedDecision)
			{
				return INDETERMINATE_RESULT_SINGLETON_LIST;
			}

			// individualDecision == combinedDecision
		}

		switch (combinedDecision)
		{
		case PERMIT:
			return PERMIT_SINGLETON_LIST;
		case DENY:
			return DENY_SINGLETON_LIST;
		case NOT_APPLICABLE:
			return NOT_APPLICABLE_SINGLETON_LIST;
		default:
			return INDETERMINATE_RESULT_SINGLETON_LIST;
		}
	}

}
