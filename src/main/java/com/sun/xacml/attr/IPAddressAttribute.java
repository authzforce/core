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

import com.sun.xacml.ParsingException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URI;

import org.w3c.dom.Node;

import com.sun.xacml.attr.xacmlv3.AttributeValue;

/**
 * Represents the IPAddress datatype introduced in XACML 2.0. All objects of
 * this class are immutable and all methods of the class are thread-safe.
 * <p>
 * To create an instance of an ipAddress from an encoded String or a DOM
 * Node you should use the <code>getInstance</code> methods provided by
 * this class. To construct an ipAddress instance directly, you must use
 * the constructors provided by <code>IPv4AddressAttribute</code> and
 * <code>IPv6AddressAttribute</code>. These will both create an attribute
 * of XACML type ipAddress, but will handle the differences in these
 * two representations correctly.
 *
 * @since 2.0
 * @author Seth Proctor
 */
public abstract class IPAddressAttribute extends AttributeValue
{

    /**
     * Official name of this type
     */
    public static final String identifier =
        "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";

    /**
     * URI version of name for this type
     */
    public static final URI identifierURI = URI.create(identifier);

    // the required address
    private InetAddress address;

    // the optional mask
    private InetAddress mask;

    // this is the optional port-range
    private PortRange range;

    /**
     * Creates the new <code>IPAddressAttribute</code> with all the optional
     * components.
     *
     * @param address a non-null <code>InetAddress</code>
     * @param mask an <code>InetAddress</code> or null if there is no mask
     * @param portRange a non-null <code>PortRange</code>
     */
    protected IPAddressAttribute(InetAddress address, InetAddress mask,
                                 PortRange range) {
        super(identifierURI);
        this.address = address;
        this.mask = mask;
        this.range = range;
    }

    /**
     * Returns a new <code>IPAddressAttribute</code> that represents
     * the name at a particular DOM node.
     *
     * @param root the <code>Node</code> that contains the desired value
     *
     * @return a new <code>IPAddressAttribute</code> representing the
     *         appropriate value (null if there is a parsing error)
     *
     * @throws ParsingException if any of the address components is invalid
     */
    public static IPAddressAttribute getInstance(Node root)
        throws ParsingException
    {
        return getInstance(root.getFirstChild().getNodeValue());
    }

    /**
     * Returns a new <code>IPAddressAttribute</code> that represents
     * the name indicated by the <code>String</code> provided.
     *
     * @param value a string representing the address
     *
     * @return a new <code>IPAddressAttribute</code>
     *
     * @throws ParsingException if any of the address components is invalid
     */
    public static IPAddressAttribute getInstance(String value)
        throws ParsingException
    {
        try {
            // an IPv6 address starts with a '['
            if (value.indexOf('[') == 0)
                return IPv6AddressAttribute.getV6Instance(value);
            else
                return IPv4AddressAttribute.getV4Instance(value);
        } catch (UnknownHostException uhe) {
            throw new ParsingException("Failed to parse an IPAddress", uhe);
        }
    }

    /**
     * Returns the address represented by this object.
     *
     * @return the address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Returns the mask represented by this object, or null if there is no
     * mask.
     *
     * @return the mask or null
     */
    public InetAddress getMask() {
        return mask;
    }

    /**
     * Returns the port range represented by this object which will be
     * unbound if no range was specified.
     *
     * @return the range
     */
    public PortRange getRange() {
        return range;
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
        if (! (o instanceof IPAddressAttribute))
            return false;

        IPAddressAttribute other = (IPAddressAttribute)o;

        if (! address.equals(other.address))
            return false;

        if (mask != null) {
            if (other.mask == null)
                return false;

            if (! mask.equals(other.mask))
                return false;
        } else {
            if (other.mask != null)
                return false;
        }

        if (! range.equals(other.range))
            return false;

        return true;
    }

    /**
     * Returns the hashcode value used to index and compare this object with
     * others of the same type.
     *
     * @return the object's hashcode value
     */
    public int hashCode() {
        
        // FIXME: what should the hashcode be?
        
        return 0;
    }

    /**
     * Converts to a String representation.
     *
     * @return the String representation
     */
    public String toString() {
        return "IPAddressAttribute: \"" + encode() + "\"";
    }

}
