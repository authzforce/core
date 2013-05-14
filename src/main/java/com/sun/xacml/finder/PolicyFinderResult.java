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
package com.sun.xacml.finder;

import com.sun.xacml.ObligationExpressions;
import com.sun.xacml.PolicySet;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.xacmlv3.AdviceExpressions;
import com.sun.xacml.xacmlv3.Policy;


/**
 * This is used as the return value for the findPolicy() methods in the
 * <code>PolicyFinder</code>. It communicates either a found policy that
 * applied to the request (eg, the target matches), an Indeterminate state,
 * or no applicable policies.
 * <p>
 * The OnlyOneApplicable combining logic is used in looking for a policy,
 * so the result from calling findPolicy can never be more than one policy.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class PolicyFinderResult
{

    // the single policy being returned
    private Policy policy;
    
    private PolicySet policySet;
    private PolicyCombiningAlgorithm policyCombiningAlg;

    // status that represents an error occurred
    private Status status;

	private String type;

    /**
     * Creates a result saying that no applicable policies were found.
     */
    public PolicyFinderResult() {
        policy = null;
        status = null;
    }

    /**
     * Creates a result containing a single applicable policy.
     *
     * @param policy the applicable policy
     */
    public PolicyFinderResult(Policy policy) {
        this.policy = policy;
        status = null;
        this.type = "Policy";
    }
    
    /**
     * Creates a result containing a single applicable policy.
     *
     * @param policy the applicable policy
     */
    public PolicyFinderResult(PolicySet policySet, PolicyCombiningAlgorithm policyCombiningAlg) {
        this.policySet = policySet;
        this.policyCombiningAlg = policyCombiningAlg;
        status = null;
        this.type = "PolicySet";
    }

    /**
     * Create a result of Indeterminate, including Status data.
     *
     * @param status the error information
     */
    public PolicyFinderResult(Status status) {
        policy = null;
        this.status = status;
    }

    /**
     * Returns true if the result was NotApplicable.
     *
     * @return true if the result was NotApplicable
     */
    public boolean notApplicable() {
        return ((policy == null) && (status == null) && (policySet == null));
    }

    /**
     * Returns true if the result was Indeterminate.
     *
     * @return true if there was an error
     */
    public boolean indeterminate() {
        return (status != null);
    }

    /**
     * Returns the found policy, or null if there was an error or no policy
     * was found.
     *
     * @return the applicable policy or null
     */
    public Policy getPolicy() {
        return policy;
    }
    
    /**
     * Returns the found policy, or null if there was an error or no policy
     * was found.
     *
     * @return the applicable policy or null
     */
    public PolicySet getPolicySet() {
        return policySet;
    }

    /**
     * Returns the status if there was an error, or null if no error occurred.
     *
     * @return the error status data or null
     */
    public Status getStatus() {
        return status;
    }

	public String getType() {
		return type;
	}

}
