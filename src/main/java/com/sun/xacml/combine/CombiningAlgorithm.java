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

import com.sun.xacml.EvaluationCtx;

import com.sun.xacml.ctx.Result;

import java.net.URI;

import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParametersType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;


/**
 * The base type for all combining algorithms. It provides one method that
 * must be implemented.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public abstract class CombiningAlgorithm
{

    // the identifier for the algorithm
    private URI identifier;

    /**
     * Constructor that takes the algorithm's identifier.
     *
     * @param identifier the algorithm's identifier
     */
    public CombiningAlgorithm(URI identifier) {
        this.identifier = identifier;
    }

    /**
     * Combines the results of the inputs based on the context to produce
     * some unified result. This is the one function of a combining algorithm.
     *
     * @param context the representation of the request
     * @param parameters a (possibly empty) non-null <code>List</code> of
     *                   <code>CombinerParameter<code>s provided for general
     *                   use (for all pre-2.0 policies this must be empty)
     * @param inputs a <code>List</code> of <code>CombinerElements</code>s to
     *               evaluate and combine
     *
     * @return a single unified result based on the combining logic
     */
    public abstract Result combine(EvaluationCtx context, CombinerParametersType parameters,
                                   List inputs);
    
    /**
     * Returns the identifier for this algorithm.
     *
     * @return the algorithm's identifier
     */
    public URI getIdentifier() {
        return identifier;
    }

}
