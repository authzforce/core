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
package com.sun.xacml.support.finder;

import com.sun.xacml.ctx.Status;


/**
 * This is an exception thrown by the support code when there's an error
 * trying to resolve a top-level policy
 *
 * @since 2.0
 * @author Seth Proctor
 */
public class TopLevelPolicyException extends Exception
{

    // status explaining the error
    private Status status;

    /**
     * Constructs a new <code>TopLevelPolicyException</code> with no message
     * or cause.
     *
     * @param status the <code>Status</code> associated with this error
     */
    public TopLevelPolicyException(Status status) {
        this.status = status;
    }

    /**
     * Constructs a new <code>TopLevelPolicyException</code> with a message,
     * but no cause. The message is saved for later retrieval by the
     * {@link java.lang#Throwable.getMessage() Throwable.getMessage()}
     * method.
     *
     * @param status the <code>Status</code> associated with this error
     * @param message the detail message (<code>null</code> if nonexistent
     *                or unknown)
     */
    public TopLevelPolicyException(Status status, String message) {
        super(message);

        this.status = status;
    }

    /**
     * Constructs a new <code>TopLevelPolicyException</code> with a cause,
     * but no message. The cause is saved for later retrieval by the
     * {@link java.lang#Throwable.getCause() Throwable.getCause()}
     * method.
     *
     * @param status the <code>Status</code> associated with this error
     * @param cause the cause (<code>null</code> if nonexistent
     *              or unknown)
     */
    public TopLevelPolicyException(Status status, Throwable cause) {
        super(cause);

        this.status = status;
    }

    /**
     * Constructs a new <code>TopLevelPolicyException</code> with a message
     * and a cause. The message and cause are saved for later retrieval
     * by the
     * {@link java.lang#Throwable.getMessage() Throwable.getMessage()} and
     * {@link java.lang#Throwable.getCause() Throwable.getCause()}
     * methods.
     *
     * @param status the <code>Status</code> associated with this error
     * @param message the detail message (<code>null</code> if nonexistent
     *                or unknown)
     * @param cause the cause (<code>null</code> if nonexistent
     *              or unknown)
     */
    public TopLevelPolicyException(Status status, String message,
                                   Throwable cause) {
        super(message, cause);

        this.status = status;
    }

    /**
     * Returns the status information associated with this error.
     *
     * @return associated status
     */
    public Status getStatus() {
        return status;
    }

}
