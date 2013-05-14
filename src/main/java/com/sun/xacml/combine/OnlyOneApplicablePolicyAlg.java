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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.xacmlv3.Policy;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;


/**
 * This is the standard Only One Applicable Policy combining algorithm. This
 * is a special algorithm used at the root of a policy/pdp to make sure that
 * pdp only selects one policy per request.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class OnlyOneApplicablePolicyAlg extends PolicyCombiningAlgorithm
{

    /**
     * The standard URN used to identify this algorithm
     */
    public static final String algId =
        "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:" +
        "only-one-applicable";

    // a URI form of the identifier
    private static final URI identifierURI = URI.create(algId);

    /**
     * Standard constructor.
     */
    public OnlyOneApplicablePolicyAlg() {
        super(identifierURI);
    }

    /**
     * Applies the combining rule to the set of policies based on the
     * evaluation context.
     *
     * @param context the context from the request
     * @param parameters a (possibly empty) non-null <code>List</code> of
     *                   <code>CombinerParameter<code>s
     * @param policyElements the policies to combine
     *
     * @return the result of running the combining algorithm
     */
    public Result combine(EvaluationCtx context, CombinerParametersType parameters,
                          List policyElements) {
        boolean atLeastOne = false;
        Policy selectedPolicy = null;
        Iterator it = policyElements.iterator();

        while (it.hasNext()) {
//            Policy policy = ((Policy)(it.next())).getPolicy();
        	Policy policy = ((Policy)(it.next()));

            // see if the policy matches the context
            MatchResult match = policy.match(context);
            int result = match.getResult();

            // if there is an error in trying to match any of the targets,
            // we always return INDETERMINATE immediately
            if (result == MatchResult.INDETERMINATE)
                return new Result(DecisionType.INDETERMINATE,
                                  match.getStatus(),
                                  context.getResourceId().encode());
            
            if (result == MatchResult.MATCH) {
                // if this isn't the first match, then this is an error
                if (atLeastOne) {
                    List code = new ArrayList();
                    code.add(Status.STATUS_PROCESSING_ERROR);
                    String message = "Too many applicable policies";
                    return new Result(DecisionType.INDETERMINATE,
                                      new Status(code, message),
                                      context.getResourceId().encode());
                }

                // if this was the first applicable policy in the set, then
                // remember it for later
                atLeastOne = true;
                selectedPolicy = policy;
            }
        }

        // if we got through the loop and found exactly one match, then
        // we return the evaluation result of that policy
        if (atLeastOne) {
            return selectedPolicy.evaluate(context);
        }

        // if we didn't find a matching policy, then we don't apply
        return new Result(DecisionType.NOT_APPLICABLE,
                          context.getResourceId().encode());
    }
}
