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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xacml.finder.impl;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Node;

/**
 *
 * @author najmi
 */
public class NamespaceContextImpl implements NamespaceContext {

    PrefixResolverDefault prefixResolver = null;

    public NamespaceContextImpl(Node namespaceNode) {
        prefixResolver = new PrefixResolverDefault(namespaceNode);
    }

    public String getNamespaceURI(String prefix) {
        return prefixResolver.getNamespaceForPrefix(prefix);
    }

    public String getPrefix(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator getPrefixes(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
