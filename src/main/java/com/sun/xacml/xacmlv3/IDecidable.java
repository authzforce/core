/**
 * Copyright (C) 2011-2014 Thales Services SAS - All rights reserved.
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
 * 
 */
package com.sun.xacml.xacmlv3;

import java.net.URI;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.Rule;
import com.sun.xacml.combine.CombiningAlgorithm;
import com.sun.xacml.ctx.Result;

/**
 * "Decidable" policy element, i.e. policy element that is evaluated to an access control decision:
 * Permit, Deny, etc. As of XACML 3.0, such elements are Rule, Policy and PolicySets, therefore they
 * must implement this interface.
 * 
 */
public interface IDecidable
{
	/**
	 * Given the input context sees whether or not the request matches this policy element. This
	 * must be called by combining algorithms before they evaluate a policy. This is also used in
	 * the initial policy finding operation to determine which top-level policies might apply to the
	 * request.
	 * 
	 * FIXME: match method should not be visible at this level but should be part of the evaluate
	 * method implementation. No need to make 2 function calls for this as match is already part of
	 * the evaluation (see {@link Rule#evaluate(EvaluationCtx)}).
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the policy and the request
	 */
	MatchResult match(EvaluationCtx context);

	/**
	 * Tries to evaluate the policy by calling the combining algorithm on the given policies or
	 * rules. The <code>match</code> method must always be called first, and must always return
	 * MATCH, before this method is called.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 */
	Result evaluate(EvaluationCtx context);

	/**
	 * Get element ID
	 * 
	 * @return ID
	 */
	URI getId();

	/**
	 * Returns the Set of obligations for this policy, which may be empty
	 * 
	 * @return the policy's obligations
	 */
	ObligationExpressions getObligationExpressions();

	/**
	 * Returns the target for this policy
	 * 
	 * @return the policy's target
	 */
	oasis.names.tc.xacml._3_0.core.schema.wd_17.Target getTarget();

	/**
	 * Returns the given description of this policy or null if there is no description
	 * 
	 * @return the description or null
	 */
	String getDescription();
}
