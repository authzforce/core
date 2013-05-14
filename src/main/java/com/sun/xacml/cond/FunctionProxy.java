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
package com.sun.xacml.cond;

import org.w3c.dom.Node;


/**
 * Used by abstract functions to define how new functions are created by
 * the factory. Note that all functions using XPath are defined to be
 * abstract functions, so they must be created using this interface.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public interface FunctionProxy
{

    /**
     * Creates an instance of some abstract function. If the function
     * being created is not using XPath, then the version parameter can be
     * ignored, otherwise a value must be present and the version must
     * be acceptable.
     *
     * @param root the DOM root of the apply statement containing the function
     * @param xpathVersion the version specified in the contianing policy, or
     *                     null if no version was specified
     *
     * @return the function
     *
     * @throws Exception if the underlying code experienced any error
     */
    public Function getInstance(Node root, String xpathVersion)
        throws Exception;

}
