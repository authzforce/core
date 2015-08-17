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

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Represents the DNSName datatype introduced in XACML 2.0. All objects of this class are immutable
 * and all methods of the class are thread-safe.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class DNSNameAttributeValue extends PrimitiveAttributeValue<String>
{
	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<DNSNameAttributeValue> FACTORY = new AttributeValue.Factory<DNSNameAttributeValue>(DNSNameAttributeValue.class)
	{

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public DNSNameAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new DNSNameAttributeValue(jaxbAttributeValue);
		}

	};

	/**
	 * Official name of this type
	 */
	public static final String identifier = "urn:oasis:names:tc:xacml:2.0:data-type:dnsName";

	/**
	 * Generic type info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(identifier);

	/**
	 * <p>
	 * hostname = *( domainlabel "." ) toplabel [ "." ]
	 * </p>
	 * <p>
	 * domainlabel = alphanum | alphanum *( alphanum | "-" ) alphanum
	 * </p>
	 * <p>
	 * toplabel = alpha | alpha *( alphanum | "-" ) alphanum
	 * </p>
	 */
	private static final Pattern HOSTNAME_PATTERN;

	static
	{
		final String domainlabel = "\\w[[\\w|\\-]*\\w]?";
		final String toplabel = "[a-zA-Z][[\\w|\\-]*\\w]?";
		final String pattern = "[\\*\\.]?[" + domainlabel + "\\.]*" + toplabel + "\\.?";
		HOSTNAME_PATTERN = Pattern.compile(pattern);
	}

	/*
	 * These fields are not actually needed in the XACML core specification since no function uses
	 * them, but it might be useful for new XACML profile or custom functions dealing with network
	 * access control for instance.
	 */
	// the required hostname
	private final String hostname;

	// the optional port portRange
	private final PortRange portRange;

	// true if the hostname starts with a '*'
	private final boolean isAnySubdomain;

	/**
	 * Private helper that tests whether the given string is valid.
	 * 
	 * TODO: find out whether it's better to use DomainValidator from Apache commons-validator
	 * instead
	 */
	private static boolean isValidHostName(String hostname)
	{
		return HOSTNAME_PATTERN.matcher(hostname).matches();
	}

	/**
	 * Creates instance from XACML AttributeValueType
	 * 
	 * @param jaxbAttrVal
	 */
	public DNSNameAttributeValue(AttributeValueType jaxbAttrVal)
	{
		super(jaxbAttrVal);
		final Entry<String, PortRange> hostAndPortRange = parseDnsName(this.value);
		this.hostname = hostAndPortRange.getKey();
		this.portRange = hostAndPortRange.getValue();

		// see if hostname started with a '*' character
		this.isAnySubdomain = hostname.charAt(0) == '*' ? true : false;
	}

	private static Entry<String, PortRange> parseDnsName(String dnsName)
	{
		final String host;
		final PortRange range;
		final int portSep = dnsName.indexOf(':');
		if (portSep == -1)
		{
			// there is no port portRange, so just use the name
			host = dnsName;
			range = new PortRange();
		} else
		{
			// split the name and the port portRange
			host = dnsName.substring(0, portSep);
			// validate port portRange
			range = PortRange.getInstance(dnsName.substring(portSep + 1, dnsName.length()));
		}

		// verify that the hostname is valid before we store it
		if (!isValidHostName(host))
		{
			throw new IllegalArgumentException("Bad hostname: " + host);
		}

		return new SimpleEntry<>(host, range);
	}

	/**
	 * Returns a new <code>DNSNameAttributeValue</code> that represents the name indicated by the
	 * <code>String</code> provided.
	 * 
	 * @param val
	 *            a string representing the name
	 */
	public DNSNameAttributeValue(String val)
	{
		this(new AttributeValueType(Collections.<Serializable> singletonList(val), identifier, null));
	}

	@Override
	protected String parse(String val)
	{
		return val.toLowerCase();
	}

	/**
	 * Returns the host name represented by this object.
	 * 
	 * @return the host name
	 */
	public String getHostName()
	{
		return hostname;
	}

	/**
	 * Returns the port portRange represented by this object which will be unbound if no portRange
	 * was specified.
	 * 
	 * @return the port portRange
	 */
	public PortRange getPortRange()
	{
		return portRange;
	}

	/**
	 * Returns true if the leading character in the hostname is a '*', and therefore represents a
	 * matching subdomain, or false otherwise.
	 * 
	 * @return true if the name represents a subdomain, false otherwise
	 */
	public boolean isAnySubdomain()
	{
		return isAnySubdomain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(hostname, portRange);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * We override the equals because for hostname, we can use equalsIgnoreCase() instead of
	 * equals() to compare, and PortRange.equals() for the portRange attribute (more optimal than
	 * String equals)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DNSNameAttributeValue other = (DNSNameAttributeValue) obj;
		if (hostname == null)
		{
			if (other.hostname != null)
				return false;
		} else if (!hostname.equalsIgnoreCase(other.hostname))
			return false;
		if (portRange == null)
		{
			if (other.portRange != null)
				return false;
		} else if (!portRange.equals(other.portRange))
			return false;
		return true;
	}

}
