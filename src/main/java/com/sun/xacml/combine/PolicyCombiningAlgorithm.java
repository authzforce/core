/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ctx.Result;


/**
 * The base type for all Policy combining algorithms. Unlike in Rule 
 * Combining Algorithms, each policy must be matched before they're evaluated
 * to make sure they apply. Also, in combining policies, obligations must be
 * handled correctly. Specifically, no obligation may be included in the
 * <code>Result</code> that doesn't match the effect being returned. So, if
 * INDETERMINATE or NOT_APPLICABLE is the returned effect, no obligations
 * may be included in the result. If the effect of the combining algorithm
 * is PERMIT or DENY, then obligations with a matching fulfillOn effect
 * are also included in the result.
 *
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public abstract class PolicyCombiningAlgorithm extends CombiningAlgorithm
{

    /**
     * Constructor that takes the algorithm's identifier.
     *
     * @param identifier the algorithm's identifier
     */
    public PolicyCombiningAlgorithm(URI identifier) {
        super(identifier);
    }

    /**
     * Combines the policies based on the context to produce some unified
     * result. This is the one function of a combining algorithm.
     * <p>
     * Note that unlike in the RuleCombiningAlgorithms, here you must
     * explicitly match the sub-policies to make sure that you should
     * consider them, and you must handle Obligations.
     *
     * @param context the representation of the request
     * @param parameters a (possibly empty) non-null <code>List</code> of
     *                   <code>CombinerParameter<code>s
     * @param policyElements a <code>List</code> of
     *                       <code>CombinerElement<code>s
     *
     * @return a single unified result based on the combining logic
     */
    public abstract Result combine(EvaluationCtx context, CombinerParametersType parameters,
                                   List policyElements);

}
