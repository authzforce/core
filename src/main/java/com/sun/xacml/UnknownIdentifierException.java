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
 * Exception that gets thrown if an unknown identifier was used, such as the
 * identifier used in any of the standard factories.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class UnknownIdentifierException extends Exception
{

    /**
     * Creates an <code>UnknownIdentifierException</code> with no data
     */
    public UnknownIdentifierException() {

    }

    /**
     * Creates an <code>UnknownIdentifierException</code> with a message
     *
     * @param message the message
     */
    public UnknownIdentifierException(String message) {
        super(message);
    }

}
