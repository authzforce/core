<<<<<<< HEAD
/*
 * @(#)PermitOverridesPolicyAlg.java
 *
 * Copyright 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml.combine;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.audit.MatchPolicies;
=======
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
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.PolicySet;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;
>>>>>>> 3.x

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

	private static final org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger
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
<<<<<<< HEAD
	public Result combine(EvaluationCtx context, List parameters,
			List policyElements) {
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		Set denyObligations = new HashSet();
		Status firstIndeterminateStatus = null;
=======
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List policyElements) {
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		ObligationsType denyObligations = new ObligationsType();
		StatusType firstIndeterminateStatus = null;
>>>>>>> 3.x
		Iterator it = policyElements.iterator();

		/**
		 * BEGINING
		 * Romain Guignard
		 */
//		AuditLogs audit = AuditLogs.getInstance();
		/**
		 * END
		 */
<<<<<<< HEAD
		List<MatchPolicies> policiesList = new ArrayList<MatchPolicies>();
		while (it.hasNext()) {
			AbstractPolicy policy = ((PolicyCombinerElement) (it.next()))
					.getPolicy();

			// make sure that the policy matches the context
			MatchResult match = policy.match(context);
=======
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
>>>>>>> 3.x

			if (match.getResult() == MatchResult.INDETERMINATE) {
				atLeastOneError = true;

				// keep track of the first error, regardless of cause
				if (firstIndeterminateStatus == null)
					firstIndeterminateStatus = match.getStatus();
			} else if (match.getResult() == MatchResult.MATCH) {
				// now we evaluate the policy
<<<<<<< HEAD
				Result result = policy.evaluate(context);
=======
				if(policy instanceof Policy) {
				result = ((Policy)policy).evaluate(context);
				} else if(policy instanceof PolicySet) {
					result = ((PolicySet)policy).evaluate(context);	
				}
>>>>>>> 3.x
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
<<<<<<< HEAD
				int effect = result.getDecision();
=======
				int effect = result.getDecision().ordinal();
>>>>>>> 3.x

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
<<<<<<< HEAD
					denyObligations.addAll(result.getObligations());
=======
					denyObligations = result.getObligations();
>>>>>>> 3.x
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
<<<<<<< HEAD
			return new Result(Result.DECISION_DENY, context.getResourceId()
=======
			return new Result(DecisionType.DENY, context.getResourceId()
>>>>>>> 3.x
					.encode(), denyObligations);

		// if we got an INDETERMINATE, return it
		if (atLeastOneError)
<<<<<<< HEAD
			return new Result(Result.DECISION_INDETERMINATE,
					firstIndeterminateStatus, context.getResourceId().encode());

		// if we got here, then nothing applied to us
		return new Result(Result.DECISION_NOT_APPLICABLE, context
				.getResourceId().encode());
	}

=======
			return new Result(DecisionType.INDETERMINATE,
					firstIndeterminateStatus, context.getResourceId().encode());

		// if we got here, then nothing applied to us
		return new Result(DecisionType.NOT_APPLICABLE, context
				.getResourceId().encode());
	}
>>>>>>> 3.x
}
