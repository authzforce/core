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
package com.sun.xacml;

import com.sun.xacml.ctx.Status;


/**
 * This is used as the return value for the various target matching functions.
 * It communicates that either the target matches the input request, the
 * target doesn't match the input request, or the result is Indeterminate.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class MatchResult
{

    /**
     * An integer value indicating the the target matches the request
     */
    public static final int MATCH = 0;

    /**
     * An integer value indicating that the target doesn't match the request
     */
    public static final int NO_MATCH = 1;

    /**
     * An integer value indicating the the result is Indeterminate
     */
    public static final int INDETERMINATE = 2;

    //
    private int result;
    private Status status;

    /**
     * Constructor that creates a <code>MatchResult</code> with no Status
     *
     * @param result the applicable result
     */
    public MatchResult(int result) {
        this(result, null);
    }

    /**
     * Constructor that creates a <code>MatchResult</code>, including Status 
     * data
     *
     * @param result the applicable result
     * @param status the error information
     * 
     * @throws IllegalArgumentException if the input result isn't a valid value
     */
    public MatchResult(int result, Status status)
        throws IllegalArgumentException
    {
        
        // check if input result is a valid value
        if ((result != MATCH) && 
            (result != NO_MATCH) &&
            (result != INDETERMINATE))
            throw new IllegalArgumentException("Input result is not a valid" +
                                               "value");
        
        this.result = result;
        this.status = status;
    }

    /**
     * Returns the applicable result
     *
     * @return the applicable result
     */
    public int getResult() {
        return result;
    }

    /**
     * Returns the status if there was an error, or null if no error occurred
     *
     * @return the error status data or null
     */
    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        String matchResult = null;

        if (result == 0) {
            matchResult = "MATCH";
        } else if (result == 1) {
            matchResult = "NO_MATCH";
        } else if (result == 2) {
            matchResult = "INDETERMINATE";
        }

        String msg = "MatchResult: " + matchResult;

        if (status != null) {
            msg += " " + status.toString();
        }

        return msg;
    }
    
}
