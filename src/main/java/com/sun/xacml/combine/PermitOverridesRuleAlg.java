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

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;


/**
 * This is the standard Permit Overrides rule combining algorithm. It
 * allows a single evaluation of Permit to take precedence over any number
 * of deny, not applicable or indeterminate results. Note that since
 * this implementation does an ordered evaluation, this class also
 * supports the Ordered Permit Overrides algorithm.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class PermitOverridesRuleAlg extends RuleCombiningAlgorithm
{

    /**
     * The standard URN used to identify this algorithm
     */
    public static final String algId =
        "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:" +
        "permit-overrides";

    // a URI form of the identifier
    private static final URI identifierURI = URI.create(algId);

    /**
     * Standard constructor. 
     */
    public PermitOverridesRuleAlg() {
        super(identifierURI);
    }

    /**
     * Protected constructor used by the ordered version of this algorithm.
     *
     * @param identifier the algorithm's identifier
     */
    protected PermitOverridesRuleAlg(URI identifier) {
        super(identifier);
    }

    /**
     * Applies the combining rule to the set of rules based on the
     * evaluation context.
     *
     * @param context the context from the request
     * @param parameters a (possibly empty) non-null <code>List</code> of
     *                   <code>CombinerParameter<code>s
     * @param ruleElements the rules to combine
     *
     * @return the result of running the combining algorithm
     */
    public Result combine(EvaluationCtx context, CombinerParametersType parameters,
                          List ruleElements) {
        boolean atLeastOneError = false;
        boolean potentialPermit = false;
        boolean atLeastOneDeny = false;
        Result firstIndeterminateResult = null;
        Iterator it = ruleElements.iterator();

        while (it.hasNext()) {
            Rule rule = (Rule)(it.next());
            Result result = rule.evaluate(context);
            int value = result.getDecision().ordinal();
            
            // if there was a value of PERMIT, then regardless of what
            // else we've seen, we always return PERMIT
            if (value == Result.DECISION_PERMIT)
                return result;
            
            // if it was INDETERMINATE, then we couldn't figure something
            // out, so we keep track of these cases...
            if (value == Result.DECISION_INDETERMINATE) {
                atLeastOneError = true;

                // there are no rules about what to do if multiple cases
                // cause errors, so we'll just return the first one
                if (firstIndeterminateResult == null)
                    firstIndeterminateResult = result;
                
                // if the Rule's effect is PERMIT, then we can't let this
                // alg return DENY, since this Rule might have permitted
                // if it could do its stuff
                if (rule.getEffect().ordinal() == Result.DECISION_PERMIT)
                    potentialPermit = true;
            } else {
                // keep track of whether we had at least one rule that
                // actually pertained to the request
                if (value == Result.DECISION_DENY)
                    atLeastOneDeny = true;
            }
        }
        
        // we didn't explicitly PERMIT, but we might have had some Rule
        // been evaluated, so we have to return INDETERMINATE
        if (potentialPermit)
            return firstIndeterminateResult;
        
        // some Rule said DENY, so since nothing could have permitted,
        // we return DENY
        if (atLeastOneDeny)
            return new Result(DecisionType.DENY, null, 
                              context.getResourceId().encode(), null, null, context.getIncludeInResults());
        
        // we didn't find anything that said DENY, but if we had a
        // problem with one of the Rules, then we're INDETERMINATE
        if (atLeastOneError)
            return firstIndeterminateResult;
        
        // if we hit this point, then none of the rules actually applied
        // to us, so we return NOT_APPLICABLE
        return new Result(DecisionType.NOT_APPLICABLE,
                          context.getResourceId().encode());
    }
}
