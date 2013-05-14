/**
 * Copyright (C) ${year} T0101841 <${email}>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.sun.xacml.combine;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationsType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;

/**
 * This is the standard Deny Overrides policy combining algorithm. It allows a
 * single evaluation of Deny to take precedence over any number of permit, not
 * applicable or indeterminate results. Note that since this implementation does
 * an ordered evaluation, this class also supports the Ordered Deny Overrides
 * algorithm.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class DenyOverridesPolicyAlg extends PolicyCombiningAlgorithm {

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:"
			+ "deny-overrides";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);

	/**
	 * Standard constructor.
	 */
	public DenyOverridesPolicyAlg() {
		super(identifierURI);
	}

	/**
	 * Protected constructor used by the ordered version of this algorithm.
	 * 
	 * @param identifier
	 *            the algorithm's identifier
	 */
	protected DenyOverridesPolicyAlg(URI identifier) {
		super(identifier);
	}

	/**
	 * Applies the combining rule to the set of policies based on the evaluation
	 * context.
	 * 
	 * @param context
	 *            the context from the request
	 * @param parameters
	 *            a (possibly empty) non-null <code>List</code> of
	 *            <code>CombinerParameter<code>s
	 * @param policyElements
	 *            the policies to combine
	 * 
	 * @return the result of running the combining algorithm
	 */
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List policyElements) {
		boolean atLeastOnePermit = false;
		ObligationsType permitObligations = new ObligationsType();
		Iterator it = policyElements.iterator();

		while (it.hasNext()) {
			Policy policy = ((Policy) (it.next()));

			// make sure that the policy matches the context
			MatchResult match = policy.match(context);

			if (match.getResult() == MatchResult.INDETERMINATE) {
				return new Result(DecisionType.DENY, context.getResourceId()
						.encode());
			}

			if (match.getResult() == MatchResult.MATCH) {
				// evaluate the policy
				Result result = policy.evaluate(context);
				int effect = result.getDecision().ordinal();

				// unlike in the RuleCombining version of this alg, we always
				// return DENY if any Policy returns DENY or INDETERMINATE
				if ((effect == Result.DECISION_DENY)
						|| (effect == Result.DECISION_INDETERMINATE))
					return new Result(DecisionType.DENY, context
							.getResourceId().encode(), result.getObligations());

				// remember if at least one Policy said PERMIT
				if (effect == Result.DECISION_PERMIT) {
					atLeastOnePermit = true;
					permitObligations = result.getObligations();
				}
			}
		}

		// if we got a PERMIT, return it, otherwise it's NOT_APPLICABLE
		if (atLeastOnePermit)
			return new Result(DecisionType.PERMIT, context.getResourceId()
					.encode(), permitObligations);
		else
			return new Result(DecisionType.NOT_APPLICABLE, context
					.getResourceId().encode());
	}

}
