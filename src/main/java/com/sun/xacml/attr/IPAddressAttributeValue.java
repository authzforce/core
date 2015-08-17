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
import java.net.InetAddress;
import java.util.Collections;
import java.util.Objects;

import javax.xml.ws.Holder;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import com.google.common.net.InetAddresses;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.PrimitiveAttributeValue;
import com.thalesgroup.authzforce.core.eval.DatatypeDef;

/**
 * Represents the IPAddress datatype introduced in XACML 2.0. All objects of this class are
 * immutable and all methods of the class are thread-safe.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class IPAddressAttributeValue extends PrimitiveAttributeValue<String>
{
	/*
	 * These fields are not actually needed in the XACML core specification since no function uses
	 * them, but it might be useful for new XACML profile or custom functions dealing with network
	 * access control for instance.
	 */
	private final InetAddress address;
	private final InetAddress mask;
	private final PortRange portRange;

	private static void parseIPAddress(String val, Holder<InetAddress> returnedAddress, Holder<InetAddress> returnedMask,
			Holder<PortRange> returnedRange)
	{
		// an IPv6 address starts with a '['
		if (val.indexOf('[') == 0)
		{
			parseIPv6Address(val, returnedAddress, returnedMask, returnedRange);
		}

		parseIPv4Address(val, returnedAddress, returnedMask, returnedRange);
	}

	/*
	 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
	 * contrary to the JDK InetAddress.getByName(). Therefore no UnknownHostException to handle.
	 */
	private static void parseIPv4Address(String val, Holder<InetAddress> returnedAddress, Holder<InetAddress> returnedMask,
			Holder<PortRange> returnedRange)
	{
		final InetAddress address;
		final InetAddress mask;
		final PortRange range;

		// start out by seeing where the delimiters are
		int maskPos = val.indexOf("/");
		int rangePos = val.indexOf(":");

		// now check to see which components we have
		if (maskPos == rangePos)
		{
			/*
			 * the string is just an address InetAddresses deliberately avoids all nameservice
			 * lookups (e.g. no DNS) on the contrary to the JDK InetAddress.getByName().
			 */
			address = InetAddresses.forString(val);
			mask = null;
			range = new PortRange();
		} else if (maskPos != -1)
		{
			// there is also a mask (and maybe a range)
			/*
			 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
			 * contrary to the JDK InetAddress.getByName().
			 */
			address = InetAddresses.forString(val.substring(0, maskPos));
			if (rangePos != -1)
			{
				// there's a range too, so get it and the mask
				/*
				 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
				 * contrary to the JDK InetAddress.getByName().
				 */
				mask = InetAddresses.forString(val.substring(maskPos + 1, rangePos));
				range = PortRange.getInstance(val.substring(rangePos + 1, val.length()));
			} else
			{
				// there's no range, so just get the mask
				/*
				 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
				 * contrary to the JDK InetAddress.getByName().
				 */
				mask =InetAddresses.forString(val.substring(maskPos + 1, val.length()));
				// if the range is null, then create it as unbound
				range = new PortRange();
			}
		} else
		{
			// there is a range, but no mask
			/*
			 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
			 * contrary to the JDK InetAddress.getByName().
			 */
			address = InetAddresses.forString(val.substring(0, rangePos));
			mask = null;
			range = PortRange.getInstance(val.substring(rangePos + 1, val.length()));
		}

		returnedAddress.value = address;
		returnedMask.value = mask;
		returnedRange.value = range;
	}

	/*
	 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
	 * contrary to the JDK InetAddress.getByName(). Therefore no UnknownHostException to handle.
	 */
	private static void parseIPv6Address(String val, Holder<InetAddress> returnedAddress, Holder<InetAddress> returnedMask,
			Holder<PortRange> returnedRange)
	{
		// Let's validate
		final InetAddress address;
		final InetAddress mask;
		final PortRange range;
		int len = val.length();

		// get the required address component
		int endIndex = val.indexOf(']');
		/*
		 * InetAddresses deliberately avoids all nameservice lookups (e.g. no DNS) on the
		 * contrary to the JDK InetAddress.getByName().
		 */
		address = InetAddresses.forString(val.substring(1, endIndex));

		// see if there's anything left in the string
		if (endIndex != (len - 1))
		{
			// if there's a mask, it's also an IPv6 address
			if (val.charAt(endIndex + 1) == '/')
			{
				int startIndex = endIndex + 3;
				endIndex = val.indexOf(']', startIndex);
				mask = InetAddresses.forString(val.substring(startIndex, endIndex));
			} else
			{
				mask = null;
			}

			// finally, see if there's a port range, if we're not finished
			if ((endIndex != (len - 1)) && (val.charAt(endIndex + 1) == ':'))
			{
				range = PortRange.getInstance(val.substring(endIndex + 2, len));
			} else
			{
				range = new PortRange();
			}
		} else
		{
			mask = null;
			range = new PortRange();
		}

		returnedAddress.value = address;
		returnedMask.value = mask;
		returnedRange.value = range;
	}

	/**
	 * Instantiates from string representation
	 * @param val string form of IP address
	 */
	public IPAddressAttributeValue(String val)
	{
		this(new AttributeValueType(Collections.<Serializable>singletonList(val), identifier, null));
	}

	/**
	 * Instantiates from XACML AttributeValue
	 * @param jaxbAttrVal
	 */
	public IPAddressAttributeValue(AttributeValueType jaxbAttrVal)
	{
		super(jaxbAttrVal);
		final Holder<InetAddress> addressHolder = new Holder<>();
		final Holder<InetAddress> maskHolder = new Holder<>();
		final Holder<PortRange> rangeHolder = new Holder<>();
		 parseIPAddress(this.value, addressHolder, maskHolder, rangeHolder);
		address = addressHolder.value;
		mask = maskHolder.value;
		portRange = rangeHolder.value;
	}

	/**
	 * Official name of this type
	 */
	public static final String identifier = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";
	
	/**
	 * Generic type info
	 */
	public static final DatatypeDef TYPE = new DatatypeDef(identifier);
	
	/**
	 * RefPolicyFinderModuleFactory instance
	 */
	public static final AttributeValue.Factory<IPAddressAttributeValue> FACTORY = new  AttributeValue.Factory<IPAddressAttributeValue>(IPAddressAttributeValue.class) {

		@Override
		public String getId()
		{
			return identifier;
		}

		@Override
		public IPAddressAttributeValue getInstance(AttributeValueType jaxbAttributeValue)
		{
			return new IPAddressAttributeValue(jaxbAttributeValue);
		}
		
	};

	@Override
	protected String parse(String stringForm)
	{
		return stringForm;
	}

	/**
	 * Returns the address represented by this object.
	 * 
	 * @return the address
	 */
	public InetAddress getAddress()
	{
		return address;
	}

	/**
	 * Returns the mask represented by this object, or null if there is no mask.
	 * 
	 * @return the mask or null
	 */
	public InetAddress getMask()
	{
		return mask;
	}

	/**
	 * Returns the port range represented by this object which will be unbound if no range was
	 * specified.
	 * 
	 * @return the range
	 */
	public PortRange getRange()
	{
		return portRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(address, mask, portRange);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
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
		final IPAddressAttributeValue other = (IPAddressAttributeValue) obj;
		if (address == null)
		{
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (mask == null)
		{
			if (other.mask != null)
				return false;
		} else if (!mask.equals(other.mask))
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
