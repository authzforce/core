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
