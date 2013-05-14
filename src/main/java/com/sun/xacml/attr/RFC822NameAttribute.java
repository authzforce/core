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

import java.net.URI;

import org.w3c.dom.Node;

import com.sun.xacml.attr.xacmlv3.AttributeValue;


/**
 * Representation of an rfc822Name (ie, an email address).
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class RFC822NameAttribute extends AttributeValue
{

    /**
     * Official name of this type
     */
    public static final String identifier =
        "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";

    /**
     * URI version of name for this type
     */
    public static final URI identifierURI = URI.create(identifier);

    // the actual value being stored
    private String value;

    /**
     * Creates a new <code>RFC822NameAttribute</code> that represents the
     * value supplied.
     *
     * @param value the email address to be represented
     */
    public RFC822NameAttribute(String value) {
         super(identifierURI);

        // check that the string is an address, ie, that it has one and only
        // one '@' character in it
        String [] parts = value.split("@");
        if (parts.length != 2) {
            // this is malformed input
            throw new IllegalArgumentException("invalid RFC822Name: " + value);
        }

        // cannonicalize the name
        this.value = parts[0] + "@" + parts[1].toLowerCase();
    }

    /**
     * Returns a new <code>RFC822NameAttribute</code> that represents
     * the email address at a particular DOM node.
     *
     * @param root the <code>Node</code> that contains the desired value
     * @return a new <code>RFC822NameAttribute</code> representing the
     *         appropriate value
     */
    public static RFC822NameAttribute getInstance(Node root) {
        return getInstance(root.getFirstChild().getNodeValue());
    }

    /**
     * Returns a new <code>RFC822NameAttribute</code> that represents
     * the email address value indicated by the string provided.
     *
     * @param value a string representing the desired value
     * @return a new <code>RFC822NameAttribute</code> representing the
     *         appropriate value
     */
    public static RFC822NameAttribute getInstance(String value) {
        return new RFC822NameAttribute(value);
    }

    /**
     * Returns the name value represented by this object
     *
     * @return the name
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns true if the input is an instance of this class and if its
     * value equals the value contained in this class.
     *
     * @param o the object to compare
     *
     * @return true if this object and the input represent the same value
     */
    public boolean equals(Object o) {
        if (! (o instanceof RFC822NameAttribute))
            return false;

        RFC822NameAttribute other = (RFC822NameAttribute)o;

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

    /**
     *
     */
    public String encode() {
        return value;
    }
    
}
