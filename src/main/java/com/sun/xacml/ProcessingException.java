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
package com.sun.xacml;


/**
 * Runtime exception that's thrown if any unexpected error occurs. This could
 * appear, for example, if you try to match a referernced policy that can't
 * be resolved.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class ProcessingException extends RuntimeException
{

    /**
     * Constructs a new <code>ProcessingException</code> with no message
     * or cause.
     */
    public ProcessingException() {

    }

    /**
     * Constructs a new <code>ProcessingException</code> with a message,
     * but no cause. The message is saved for later retrieval by the
     * {@link java.lang#Throwable.getMessage() Throwable.getMessage()}
     * method.
     *
     * @param message the detail message (<code>null</code> if nonexistent
     *                or unknown)
     */
    public ProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new <code>ProcessingException</code> with a cause,
     * but no message. The cause is saved for later retrieval by the
     * {@link java.lang#Throwable.getCause() Throwable.getCause()}
     * method.
     *
     * @param cause the cause (<code>null</code> if nonexistent
     *              or unknown)
     */
    public ProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new <code>ProcessingException</code> with a message
     * and a cause. The message and cause are saved for later retrieval
     * by the
     * {@link java.lang#Throwable.getMessage() Throwable.getMessage()} and
     * {@link java.lang#Throwable.getCause() Throwable.getCause()}
     * methods.
     *
     * @param message the detail message (<code>null</code> if nonexistent
     *                or unknown)
     * @param cause the cause (<code>null</code> if nonexistent
     *              or unknown)
     */
    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
