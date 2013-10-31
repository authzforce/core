/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
/*
 * $Id: PrefixResolverDefault.java,v 1.2.4.1 2005/09/15 08:15:51 suresh_emailid Exp $
 */
package com.sun.xacml.finder.impl;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class implements a generic PrefixResolver that
 * can be used to perform prefix-to-namespace lookup
 * for the XPath object.
 * @xsl.usage general
 */
public class PrefixResolverDefault //implements PrefixResolver
{

    /**
     * The context to resolve the prefix from, if the context
     * is not given.
     */
    Node m_context;

    /**
     * Construct a PrefixResolverDefault object.
     * @param xpathExpressionContext The context from
     * which XPath expression prefixes will be resolved.
     * Warning: This will not work correctly if xpathExpressionContext
     * is an attribute node.
     */
    public PrefixResolverDefault(Node xpathExpressionContext) {
        m_context = xpathExpressionContext;
    }

    /**
     * Given a namespace, get the corrisponding prefix.  This assumes that
     * the PrevixResolver hold's it's own namespace context, or is a namespace
     * context itself.
     * @param prefix Prefix to resolve.
     * @return Namespace that prefix resolves to, or null if prefix
     * is not bound.
     */
    public String getNamespaceForPrefix(String prefix) {
        return getNamespaceForPrefix(prefix, m_context);
    }

    /**
     * Given a namespace, get the corrisponding prefix.
     * Warning: This will not work correctly if namespaceContext
     * is an attribute node.
     * @param prefix Prefix to resolve.
     * @param namespaceContext Node from which to start searching for a
     * xmlns attribute that binds a prefix to a namespace.
     * @return Namespace that prefix resolves to, or null if prefix
     * is not bound.
     */
    public String getNamespaceForPrefix(String prefix,
            org.w3c.dom.Node namespaceContext) {

        Node parent = namespaceContext;
        String namespace = null;

        if (prefix.equals("xml")) {
            namespace = "http://www.w3.org/XML/1998/namespace";
        } else {
            int type;

            while ((null != parent) && (null == namespace)
                    && (((type = parent.getNodeType()) == Node.ELEMENT_NODE)
                    || (type == Node.ENTITY_REFERENCE_NODE))) {
                if (type == Node.ELEMENT_NODE) {
                    if (parent.getNodeName().indexOf(prefix + ":") == 0) {
                        return parent.getNamespaceURI();
                    }
                    NamedNodeMap nnm = parent.getAttributes();

                    for (int i = 0; i < nnm.getLength(); i++) {
                        Node attr = nnm.item(i);
                        String aname = attr.getNodeName();
                        boolean isPrefix = aname.startsWith("xmlns:");

                        if (isPrefix || aname.equals("xmlns")) {
                            int index = aname.indexOf(':');
                            String p = isPrefix ? aname.substring(index + 1) : "";

                            if (p.equals(prefix)) {
                                namespace = attr.getNodeValue();

                                break;
                            }
                        }
                    }
                }

                parent = parent.getParentNode();
            }
        }

        return namespace;
    }

    /**
     * Return the base identifier.
     *
     * @return null
     */
    public String getBaseIdentifier() {
        return null;
    }

    /**
     * @see PrefixResolver#handlesNullPrefixes()
     */
    public boolean handlesNullPrefixes() {
        return false;
    }
}
