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
/**
 * 
 */
package com.sun.xacml.combine;

import java.net.URI;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.xacmlv3.Policy;

/**
 * @author Romain Ferrari
 * 
 */
public class PermitUnlessDenyPolicyAlg extends PolicyCombiningAlgorithm {

	/**
	 * The standard URN used to identify this algorithm
	 */
	public static final String algId = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:"
			+ "permit-unless-deny";

	// a URI form of the identifier
	private static final URI identifierURI = URI.create(algId);
	
	/**
	 * Standard constructor
	 */
	public PermitUnlessDenyPolicyAlg() {
		super(identifierURI);
	}

	/**
	 * @param identifier
	 */
	public PermitUnlessDenyPolicyAlg(URI identifier) {
		super(identifier);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.xacml.combine.PolicyCombiningAlgorithm#combine(com.sun.xacml.
	 * EvaluationCtx, java.util.List, java.util.List)
	 */
	@Override
	public Result combine(EvaluationCtx context, CombinerParametersType parameters,
			List policyElements) {
		Result result = null;
		for (Policy policy : (List<Policy>) policyElements) {
			// make sure that the policy matches the context
			MatchResult match = policy.match(context);
			if (match.getResult() == MatchResult.MATCH) {
				result = policy.evaluate(context);
				int value = result.getDecision().ordinal();
				if (value == DecisionType.DENY.ordinal()) {
					return result;
				} 
			}
		}
		return new Result(DecisionType.PERMIT, context
				.getResourceId().encode(), result.getObligations());
	}

}
