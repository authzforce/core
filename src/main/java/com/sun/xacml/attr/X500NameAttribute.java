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
package com.sun.xacml.attr;

import java.net.URI;

import javax.security.auth.x500.X500Principal;

import org.w3c.dom.Node;

import com.sun.xacml.attr.xacmlv3.AttributeValue;

/**
 * Representation of an X500 Name.
 *
 * @since 1.0
 * @author Marco Barreno
 * @author Seth Proctor
 */
public class X500NameAttribute extends AttributeValue
{
   
    /**
     * Official name of this type
     */
    public static final String identifier =
        "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";
 
    // the actual value being stored
    private X500Principal value;

    /**
     * URI version of name for this type
     */
    public static final URI identifierURI = URI.create(identifier);

    /**
     * Creates a new <code>X500NameAttribute</code> that represents the
     * value supplied.
     *
     * @param value the X500 Name to be represented
     */
    public X500NameAttribute(X500Principal value) {
        super(identifierURI);
        this.value = value;
    }

    /**
     * Returns a new <codeX500NameAttribute</code> that represents
     * the X500 Name at a particular DOM node.
     *
     * @param root the <code>Node</code> that contains the desired value
     * @return a new <code>X500NameAttribute</code> representing the
     *         appropriate value
     * @throws IllegalArgumentException if value is improperly specified
     */
    public static X500NameAttribute getInstance(Node root) 
        throws IllegalArgumentException
    {
        return getInstance(root.getFirstChild().getNodeValue());
    }

    /**
     * Returns a new <code>X500NameAttribute</code> that represents
     * the X500 Name value indicated by the string provided.
     *
     * @param value a string representing the desired value
     * @return a new <code>X500NameAttribute</code> representing the
     *         appropriate value
     * @throws IllegalArgumentException if value is improperly specified
     */
    public static X500NameAttribute getInstance(String value)
        throws IllegalArgumentException
    {
        return new X500NameAttribute(new X500Principal(value));
    }

    /**
     * Returns the name value represented by this object
     *
     * @return the name
     */
    public X500Principal getValue() {
        return value;
    }

    /**
     * Returns true if the input is an instance of this class and if its
     * value equals the value contained in this class. This method 
     * deviates slightly from the XACML spec in the way that it handles
     * RDNs with multiple attributeTypeAndValue pairs and some
     * additional canonicalization steps. This method uses
     * the procedure used by 
     * <code>javax.security.auth.x500.X500Principal.equals()</code>, while the
     * XACML spec uses a slightly different procedure. In practice, it is
     * expected that this difference will not be noticeable. For more
     * details, refer to the javadoc for <code>X500Principal.equals()</code> 
     * and the XACML specification.
     *
     * @param o the object to compare
     *
     * @return true if this object and the input represent the same value
     */
    public boolean equals(Object o) {
        if (! (o instanceof X500NameAttribute))
            return false;

        X500NameAttribute other = (X500NameAttribute)o;

        return value.equals(other.value);
    }

    /**
     * Returns the hashcode value used to index and compare this object with
     * others of the same type. Typically this is the hashcode of the backing
     * data object.
     *
     * @return the object's hashcode value
     */
    public int hashCode() {
        return value.hashCode();
    }

    public String encode() {
        return value.getName();
    }

}
