/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
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
