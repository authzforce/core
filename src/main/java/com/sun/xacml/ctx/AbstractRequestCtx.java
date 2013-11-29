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
package com.sun.xacml.ctx;

import java.io.OutputStream;
import java.util.Set;

import org.w3c.dom.Node;

import com.sun.xacml.Indenter;
import com.sun.xacml.xacmlv3.Attributes;

/**
 * Represents a XACML request made to the PDP. This is the class that contains all the data used to start
 * a policy evaluation.abstract class has been defined to give a unique interface for both XACML 2.0
 * and XACML 3.0 RequestCtx
 */
public abstract class AbstractRequestCtx {

    //XACML version of the request
    protected int xacmlVersion;

    // Hold onto the root of the document for XPath searches
    protected Node documentRoot = null;

    /**
     * XACML3 attributes as <code>Attributes</code> objects
     */
    protected Set<Attributes> attributesSet = null;

    protected boolean isSearch;

    /**
     * Returns the root DOM node of the document used to create this object, or null if this object
     * was created by hand (ie, not through the <code>getInstance</code> method) or if the root node
     * was not provided to the constructor.
     *
     * @return the root DOM node or null
     */
    public Node getDocumentRoot() {
        return documentRoot;
    }    

    public void setSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }

    public boolean isSearch() {
        return isSearch;
    }

    public int getXacmlVersion() {
        return xacmlVersion;
    }

    public void setXacmlVersion(int xacmlVersion) {
        this.xacmlVersion = xacmlVersion;
    }

    /**
     *  Returns a <code>Set</code> containing <code>Attribute</code> objects.
     *
     * @return  the request' s all attributes as <code>Set</code>
     */
    public Set<Attributes> getAttributesSet() {
        return attributesSet;
    }

    /**
     * Encodes this <code>AbstractRequestCtx</code> into its XML representation and writes this encoding to the given
     * <code>OutputStream</code> with indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public abstract void encode(OutputStream output, Indenter indenter);

    /**
     * Encodes this <code>AbstractRequestCtx</code>  into its XML representation and writes this encoding to the given
     * <code>OutputStream</code>. No indentation is used.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public abstract void encode(OutputStream output);    

}