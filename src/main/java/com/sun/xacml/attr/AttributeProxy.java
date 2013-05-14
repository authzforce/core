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
package com.sun.xacml.attr;

import org.w3c.dom.Node;
import com.sun.xacml.attr.xacmlv3.AttributeValue;


/**
 * Used by the <code>AttributeFactory</code> to create new attributes.
 * Typically a new proxy class is created which in turn knows how to create
 * a specific kind of attribute, and then this proxy class is installed in
 * the <code>AttributeFactory</code>.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public interface AttributeProxy
{

    /**
     * Tries to create a new <code>AttributeValue</code> based on the given
     * DOM root node.
     *
     * @param root the DOM root of some attribute data
     *
     * @return an <code>AttributeValue</code> representing the given data
     *
     * @throws Exception if the data couldn't be used (the exception is
     *                   typically wrapping some other exception)
     */
    public AttributeValue getInstance(Node root) throws Exception;

    /**
     * Tries to create a new <code>AttributeValue</code> based on the given
     * String data.
     *
     * @param value the text form of some attribute data
     *
     * @return an <code>AttributeValue</code> representing the given data
     *
     * @throws Exception if the data couldn't be used (the exception is
     *                   typically wrapping some other exception)
     */
    public AttributeValue getInstance(String value) throws Exception;

}
