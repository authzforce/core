/**
 * Copyright (C) 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.test.custom;

import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;

/**
 * Represents the dnsName-value datatype <i>XACML Data Loss Prevention / Network Access Control (DLP/NAC) Profile Version 1.0<i>. Edited by John Tolbert,
 * Richard Hill, Crystal Hayes, David Brossard, Hal Lockhart, and Steven Legg. 16 February 2015. OASIS Committee Specification 01.
 * http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/cs01/xacml-3.0-dlp-nac-v1.0-cs01.html. Latest version:
 * http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html.
 * <p>
 * It is basically the same as XACML Core dnsName datatype except that the hostname may use a wildcard as left-most subdomain, and the part after ':' is limited
 * to a port number only.
 * <p>
 * Used here for testing Authzforce datatype extension mechanism, i.e. plugging a custom simple datatype into the PDP engine.
 *
 * 
 * @version $Id: $
 */
public final class TestDNSNameWithPortValue extends SimpleValue<String>
{
	/**
	 * Data type URI
	 */
	public static final String ID = "urn:oasis:names:tc:xacml:3.0:data-type:dnsName-value";

	public static final class Factory extends SimpleValue.StringContentOnlyFactory<TestDNSNameWithPortValue>
	{

		public Factory()
		{
			super(TestDNSNameWithPortValue.class, ID, URI.create(Function.XACML_NS_3_0 + "dnsName-value"));
		}

		@Override
		public TestDNSNameWithPortValue getInstance(String val)
		{
			return new TestDNSNameWithPortValue(val);
		}

	}

	public static final Factory FACTORY = new Factory();

	private static final int UNDEFINED_PORT = -1;

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
	 * Difference with XACML Core dnsName datatype is that there is no wildcard accepted in left-most part
	 */
	private static final Pattern HOSTNAME_PATTERN;
	static
	{
		final String domainlabel = "\\w[[\\w|\\-]*\\w]?";
		final String toplabel = "[a-zA-Z][[\\w|\\-]*\\w]?";
		final String pattern = "[" + domainlabel + "\\.]*" + toplabel + "\\.?";
		HOSTNAME_PATTERN = Pattern.compile(pattern);
	}

	/*
	 * These fields are not actually needed in the XACML core specification since no function uses them, but it might be useful for new XACML profile or custom
	 * functions dealing with network access control for instance.
	 */
	// the required hostname
	private final transient String hostname;

	// the optional port
	private final transient int port;

	/**
	 * Private helper that tests whether the given string is valid.
	 * 
	 * TODO: find out whether it's better to use DomainValidator from Apache commons-validator instead, but first make sure this issue is fixed:
	 * https://issues.apache.org/jira/browse/VALIDATOR-366
	 */
	private static boolean isValidHostName(String hostname)
	{
		assert hostname != null;
		return HOSTNAME_PATTERN.matcher(hostname).matches();
	}

	private static Entry<String, Integer> parseDnsName(String dnsName) throws IllegalArgumentException
	{
		assert dnsName != null;

		final String host;
		final Integer port;
		final int portSep = dnsName.indexOf(':');
		if (portSep == -1)
		{
			// there is no port portRange, so just use the name
			host = dnsName;
			port = UNDEFINED_PORT;
		} else
		{
			// split the name and the port
			host = dnsName.substring(0, portSep);
			// validate port portRange
			port = Integer.valueOf(dnsName.substring(portSep + 1, dnsName.length()));
		}

		// verify that the hostname is valid before we store it
		if (!isValidHostName(host))
		{
			throw new IllegalArgumentException("Bad hostname: " + host);
		}

		return new SimpleEntry<>(host, port);
	}

	/**
	 * Returns a new <code>DNSNameAttributeValue</code> that represents the name indicated by the <code>String</code> provided.
	 *
	 * @param val
	 *            a string representing the name
	 * @throws java.lang.IllegalArgumentException
	 *             if format of {@code val} does not comply with the dnsName datatype definition
	 */
	public TestDNSNameWithPortValue(String val) throws IllegalArgumentException
	{
		super(ID, val);
		final Entry<String, Integer> hostAndPort = parseDnsName(this.value);
		this.hostname = hostAndPort.getKey();
		this.port = hostAndPort.getValue();
	}

	/**
	 * Returns the host name represented by this object. Used by {@link TestDNSNameValueEqualFunction}
	 *
	 * @return the host name
	 */
	public String getHostName()
	{
		return hostname;
	}

	// /**
	// * Returns the port represented by this object
	// *
	// * @return the port
	// */
	// public int getPort()
	// {
	// return port;
	// }

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			// hash regardless of letter case
			hashCode = Objects.hash(hostname.toLowerCase(Locale.US), port);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * We override the equals because for hostname, we can use equalsIgnoreCase() instead of equals() to compare, and PortRange.equals() for the portRange
	 * attribute (more optimal than String equals)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof TestDNSNameWithPortValue))
		{
			return false;
		}

		final TestDNSNameWithPortValue other = (TestDNSNameWithPortValue) obj;

		// hostname and portRange are not null
		/*
		 * if (hostname == null) { if (other.hostname != null) return false; } else
		 */

		return hostname.equalsIgnoreCase(other.hostname) && port == other.port;
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return this.value;
	}

}
