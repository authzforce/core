package org.ow2.authzforce.core.value;

import java.util.AbstractMap.SimpleEntry;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import com.sun.xacml.PortRange;

/**
 * Represents the DNSName datatype introduced in XACML 2.0.
 * 
 */
public final class DNSNameValue extends SimpleValue<String>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:2.0:data-type:dnsName";

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
	 * These fields are not actually needed in the XACML core specification since no function uses them, but it might be useful for new XACML profile or custom
	 * functions dealing with network access control for instance.
	 */
	// the required hostname
	private final String hostname;

	// the optional port portRange
	private final transient PortRange portRange;

	/*
	 * true if the hostname starts with a '*', therefore this field is derived from hostname
	 */
	// private final boolean isAnySubdomain;

	/**
	 * Private helper that tests whether the given string is valid.
	 * 
	 * TODO: find out whether it's better to use DomainValidator from Apache commons-validator instead
	 */
	private static boolean isValidHostName(String hostname)
	{
		assert hostname != null;
		return HOSTNAME_PATTERN.matcher(hostname).matches();
	}

	private static Entry<String, PortRange> parseDnsName(String dnsName) throws IllegalArgumentException
	{
		assert dnsName != null;

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
	 * Returns a new <code>DNSNameAttributeValue</code> that represents the name indicated by the <code>String</code> provided.
	 * 
	 * @param val
	 *            a string representing the name
	 * @throws IllegalArgumentException
	 *             if format of {@code val} does not comply with the dnsName datatype definition
	 */
	public DNSNameValue(String val) throws IllegalArgumentException
	{
		super(TYPE_URI, val);
		final Entry<String, PortRange> hostAndPortRange = parseDnsName(this.value);
		this.hostname = hostAndPortRange.getKey();
		this.portRange = hostAndPortRange.getValue();

		// see if hostname started with a '*' character
		// this.isAnySubdomain = hostname.charAt(0) == '*' ? true : false;
	}

	// /**
	// * Returns the host name represented by this object.
	// *
	// * @return the host name
	// */
	// public String getHostName()
	// {
	// return hostname;
	// }

	// /**
	// * Returns the port portRange represented by this object which will be unbound if no portRange was specified.
	// *
	// * @return the port portRange
	// */
	// public PortRange getPortRange()
	// {
	// return portRange;
	// }

	// /**
	// * Returns true if the leading character in the hostname is a '*', and therefore represents a matching subdomain, or false otherwise.
	// *
	// * @return true if the name represents a subdomain, false otherwise
	// */
	// public boolean isAnySubdomain()
	// {
	// return isAnySubdomain;
	// }

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			// hash regardless of letter case
			hashCode = Objects.hash(hostname.toLowerCase(Locale.US), portRange);
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
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof DNSNameValue))
		{
			return false;
		}

		final DNSNameValue other = (DNSNameValue) obj;

		// hostname and portRange are not null
		/*
		 * if (hostname == null) { if (other.hostname != null) return false; } else
		 */

		return hostname.equalsIgnoreCase(other.hostname) && portRange.equals(other.portRange);
	}

	@Override
	public String printXML()
	{
		return this.value.toString();
	}

}
