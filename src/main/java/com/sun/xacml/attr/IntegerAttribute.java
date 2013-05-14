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
import com.sun.xacml.attr.xacmlv3.AttributeValue;

import org.w3c.dom.Node;


/**
 * Representation of an xs:integer value. This class supports parsing
 * xs:integer values. All objects of this class are immutable and
 * all methods of the class are thread-safe.
 *
 * @since 1.0
 * @author Marco Barreno
 * @author Steve Hanna
 */
public class IntegerAttribute extends AttributeValue
{
    /**
     * Official name of this type
     */
    public static final String identifier =
        "http://www.w3.org/2001/XMLSchema#integer";
 
    /**
     * URI version of name for this type
     */
    public static final URI identifierURI = URI.create(identifier);

    /**
     * The actual long value that this object represents.
     */
    private long value;

    /**
     * Creates a new <code>IntegerAttribute</code> that represents
     * the long value supplied.
     *
     * @param value the <code>long</code> value to be represented
     */
    public IntegerAttribute(long value) {
        super(identifierURI);
        this.value = value;
        this.content.add(value);
    }

    /**
     * Returns a new <code>IntegerAttribute</code> that represents
     * the xs:integer at a particular DOM node.
     *
     * @param root the <code>Node</code> that contains the desired value
     * @return a new <code>IntegerAttribute</code> representing the
     *         appropriate value (null if there is a parsing error)
     * @throws NumberFormatException if the string form isn't a number
     */
    public static IntegerAttribute getInstance(Node root)
        throws NumberFormatException
    {
        return getInstance(root.getFirstChild().getNodeValue());
    }

    /**
     * Returns a new <code>IntegerAttribute</code> that represents
     * the xs:integer value indicated by the string provided.
     *
     * @param value a string representing the desired value
     * @return a new <code>IntegerAttribute</code> representing the
     *         appropriate value (null if there is a parsing error)
     * @throws NumberFormatException if the string isn't a number
     */
    public static IntegerAttribute getInstance(String value)
        throws NumberFormatException
    {
        // Leading '+' is allowed per XML schema and not
        // by Long.parseLong. Strip it, if present.
        if ((value.length() >= 1) && (value.charAt(0) == '+'))
            value = value.substring(1);
        return new IntegerAttribute(Long.parseLong(value));
    }

    /**
     * Returns the <code>long</code> value represented by this object.
     *
     * @return the <code>long</code> value
     */
    public long getValue() {
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
        if (! (o instanceof IntegerAttribute))
            return false;

        IntegerAttribute other = (IntegerAttribute)o;

        return (value == other.value);
    }

    /**
     * Returns the hashcode value used to index and compare this object with
     * others of the same type. Typically this is the hashcode of the backing
     * data object.
     *
     * @return the object's hashcode value
     */
    public int hashCode() {
        return (int)value;
    }

    /**
     *
     */
    public String encode() {
        return String.valueOf(value);
    }

}
