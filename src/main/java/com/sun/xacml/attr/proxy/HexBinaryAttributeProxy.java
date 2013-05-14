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
package com.sun.xacml.attr.proxy;

import com.sun.xacml.attr.AttributeProxy;
import com.sun.xacml.attr.HexBinaryAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;

import org.w3c.dom.Node;


/**
 * A proxy class that is provided mainly for the run-time configuration
 * code to use.
 *
 * @since 1.2
 * @author Seth Proctor
 */
public class HexBinaryAttributeProxy implements AttributeProxy
{

    public AttributeValue getInstance(Node root) throws Exception {
        return HexBinaryAttribute.getInstance(root);
    }

    public AttributeValue getInstance(String value) throws Exception {
        return HexBinaryAttribute.getInstance(value);
    }

}
