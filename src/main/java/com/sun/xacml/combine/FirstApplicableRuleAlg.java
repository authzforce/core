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
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.ctx.Result;
import com.thalesgroup.authzforce.audit.annotations.Audit;
import com.thalesgroup.authzforce.audit.annotations.Audit.Type;


/**
 * This is the standard First Applicable rule combining algorithm. It looks
 * through the set of rules, finds the first one that applies, and returns
 * that evaluation result.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class FirstApplicableRuleAlg extends RuleCombiningAlgorithm
{

    /**
     * The standard URN used to identify this algorithm
     */
    public static final String algId =
        "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:" +
        "first-applicable";

    // a URI form of the identifier
    private static final URI identifierURI = URI.create(algId);

    /**
     * Standard constructor.
     */
    public FirstApplicableRuleAlg() {
        super(identifierURI);
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
//    @Audit(type = Audit.Type.RULE)
    public Result combine(EvaluationCtx context, CombinerParametersType parameters,
                          List ruleElements) {
        Result result = null;
        for (Rule rule : (List<Rule>)ruleElements) {
			result = rule.evaluate(context);
			int value = result.getDecision().ordinal();
			if (value != Result.DECISION_NOT_APPLICABLE) {
				return result;
			}
		}
     // if we got here, then none of the rules applied
        return new Result(DecisionType.NOT_APPLICABLE,
                          context.getResourceId().encode());
    }
}
