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
package com.sun.xacml.attr;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.xmlbeans.XmlAnyURI;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;

import com.sun.xacml.attr.xacmlv3.AttributeValue;

/**
 * Representation of an xs:anyURI value. This class supports parsing xs:anyURI values.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class AnyURIAttribute extends AttributeValue
{

	/**
	 * Official name of this type
	 */
	public static final String identifier = "http://www.w3.org/2001/XMLSchema#anyURI";

	/*
	 * the URI value that this class represents <p> WARNING: java.net.URI cannot be used here for
	 * XACML datatype, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD
	 * anyURI [1], not in java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI </p>
	 */
	private String value;

	/**
	 * Creates a new <code>AnyURIAttribute</code> that represents the URI value supplied.
	 * 
	 * @param value
	 *            the URI to be represented
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML datatype, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI. [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 */
	public AnyURIAttribute(String value)
	{
		super(identifier);
		
		// validate anyURI
		final XmlOptions validationOps = new XmlOptions();
		validationOps.setValidateStrict();
		final Collection<XmlError> errors = new ArrayList<>();
		validationOps.setErrorListener(errors);
		XmlAnyURI xmlAnyURI;
		try
		{
			xmlAnyURI = XmlAnyURI.Factory.parse(String.format("<xml-fragment>%s</xml-fragment>", value));
			xmlAnyURI.validate(validationOps);
		} catch (XmlException e)
		{
			throw new IllegalArgumentException("Invalid anyURI '" + value+"'", e);
		}
		
		if(!errors.isEmpty()) {
			throw new IllegalArgumentException("Invalid anyURI '" + value +"': "+ errors);
		}
		
		this.content.add(value);
		this.value = value;
	}

	/**
	 * Returns a new <code>AnyURIAttribute</code> that represents the xs:anyURI at a particular DOM
	 * node.
	 * 
	 * @param root
	 *            the <code>Node</code> that contains the desired value
	 * 
	 * @return a new <code>AnyURIAttribute</code> representing the appropriate value (null if there
	 *         is a parsing error)
	 * @throws URISyntaxException 
	 */
	public static AnyURIAttribute getInstance(Node root) throws URISyntaxException
	{
		return getInstance(root.getFirstChild().getNodeValue());
	}

	/**
	 * Returns a new <code>AnyURIAttribute</code> that represents the xs:anyURI value indicated by
	 * the <code>String</code> provided.
	 * 
	 * @param value
	 *            a string representing the desired value
	 * 
	 * @return a new <code>AnyURIAttribute</code> representing the appropriate value
	 * @throws URISyntaxException 
	 */
	public static AnyURIAttribute getInstance(String value) throws URISyntaxException
	{
		
		return new AnyURIAttribute(value);
	}

	/**
	 * Returns the URI value represented by this object.
	 * 
	 * @return the URI value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Returns true if the input is an instance of this class and if its value equals the value
	 * contained in this class.
	 * 
	 * @param o
	 *            the object to compare
	 * 
	 * @return true if this object and the input represent the same value
	 */
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof AnyURIAttribute))
			return false;

		AnyURIAttribute other = (AnyURIAttribute) o;

		return value.equals(other.value);
	}

	/**
	 * Returns the hashcode value used to index and compare this object with others of the same
	 * type. Typically this is the hashcode of the backing data object.
	 * 
	 * @return the object's hashcode value
	 */
	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	/**
	 * Converts to a String representation.
	 * 
	 * @return the String representation
	 */
	@Override
	public String toString()
	{
		return "AnyURIAttribute: \"" + value + "\"";
	}

	/**
     *
     */
	@Override
	public String encode()
	{
		return value;
	}

}
