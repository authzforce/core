/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
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
package com.sun.xacml.combine;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.PolicySet;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;

/**
 * This is the standard Permit Overrides policy combining algorithm. It allows a
 * single evaluation of Permit to take precedence over any number of deny, not
 * applicable or indeterminate results. Note that since this implementation does
 * an ordered evaluation, this class also supports the Ordered Permit Overrides
 * algorithm.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class PermitOverridesPolicyAlg extends PolicyCombiningAlgorithm {

	private static final Logger log4jLogger = LoggerFactory
			.getLogger(PermitOverridesPolicyAlg.class);

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:"
			+ "permit-overrides";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);

	/**
	 * Standard constructor.
	 */
	public PermitOverridesPolicyAlg() {
		super(identifierURI);
	}

	/**
	 * Protected constructor used by the ordered version of this algorithm.
	 * 
	 * @param identifier
	 *            the algorithm's identifier
	 */
	protected PermitOverridesPolicyAlg(URI identifier) {
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
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		Obligations denyObligations = new Obligations();
		Status firstIndeterminateStatus = null;
		Iterator it = policyElements.iterator();

		/**
		 * BEGINING
		 * Romain Guignard
		 */
//		AuditLogs audit = AuditLogs.getInstance();
		/**
		 * END
		 */
//		List<MatchPolicies> policiesList = new ArrayList<MatchPolicies>();
		while (it.hasNext()) {
			Object policy = (it.next());
			MatchResult match = null;
			Result result = null;
			// make sure that the policy matches the context
			if(policy instanceof Policy) {
				match = ((Policy)policy).match(context);				
			} else if(policy instanceof PolicySet) {
				match = ((PolicySet)policy).match(context);
			}

			if (match.getResult() == MatchResult.INDETERMINATE) {
				atLeastOneError = true;

				// keep track of the first error, regardless of cause
				if (firstIndeterminateStatus == null)
					firstIndeterminateStatus = match.getStatus();
			} else if (match.getResult() == MatchResult.MATCH) {
				// now we evaluate the policy
				if(policy instanceof Policy) {
				result = ((Policy)policy).evaluate(context);
				} else if(policy instanceof PolicySet) {
					result = ((PolicySet)policy).evaluate(context);	
				}
				/**
				 * BEGINING
				 * Romain Guignard
				 */
//				log4jLogger.debug("Found a policy that match the request");
//				log4jLogger.debug("PolicyId: " + policy.getId());
//				log4jLogger.debug("Policy_Version: " + policy.getVersion());
//				MatchPolicies matchpolicies = new MatchPolicies();
//				matchpolicies.setPolicyId(policy.getId().toString());
//				matchpolicies.setPolicyVersion(policy.getVersion());
//				policiesList.add(matchpolicies);
				/**
				 * END
				 */
				int effect = result.getDecision().ordinal();

				// this is a little different from DenyOverrides...

				if (effect == Result.DECISION_PERMIT) {
					/**
					 * BEGINING
					 * Romain Guignard
					 */
//					audit.setMatchPolicies(policiesList);
					/**
					 * END
					 */
					return result;
				}
				if (effect == Result.DECISION_DENY) {
					atLeastOneDeny = true;
					denyObligations = result.getObligations();
				} else if (effect == Result.DECISION_INDETERMINATE) {
					atLeastOneError = true;

					// keep track of the first error, regardless of cause
					if (firstIndeterminateStatus == null)
						firstIndeterminateStatus = result.getStatus();
				}
			}
			/**
			 * BEGINING
			 * Romain Guignard
			 */
//			audit.setMatchPolicies(policiesList);
			/**
			 * END
			 */
		}

		// if we got a DENY, return it
		if (atLeastOneDeny)
			return new Result(DecisionType.DENY, context.getResourceId()
					.encode(), denyObligations);

		// if we got an INDETERMINATE, return it
		if (atLeastOneError)
			return new Result(DecisionType.INDETERMINATE,
					firstIndeterminateStatus, context.getResourceId().encode());

		// if we got here, then nothing applied to us
		return new Result(DecisionType.NOT_APPLICABLE, context
				.getResourceId().encode());
	}
}
