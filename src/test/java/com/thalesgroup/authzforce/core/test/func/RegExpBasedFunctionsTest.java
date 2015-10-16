/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.thalesgroup.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class RegExpBasedFunctionsTest extends GeneralFunctionTest
{

	public RegExpBasedFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?, ?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_STRING_REGEXP_MATCH = "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match";
	private static final String NAME_ANYURI_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match";
	private static final String NAME_IPADDRESS_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match";
	private static final String NAME_DNSNAME_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match";
	private static final String NAME_RFC822NAME_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match";
	private static final String NAME_X500NAME_REGEXP_MATCH = "urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
				new Object[] { NAME_STRING_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("John.*"), new StringAttributeValue("John Doe")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_STRING_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("John.*"), new StringAttributeValue("Jane Doe")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
				new Object[] { NAME_ANYURI_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^http://.+"), new AnyURIAttributeValue("http://www.thalesgroup.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_ANYURI_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^http://.+"), new AnyURIAttributeValue("https://www.thalesgroup.com")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"), new IPAddressAttributeValue("10.10.10.190")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"), new IPAddressAttributeValue("10.144.10.190")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"), new IPAddressAttributeValue("10.10.10.10/255.255.255.0:80")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"), new IPAddressAttributeValue("192.168.1.10/255.255.255.0:8080")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"), new IPAddressAttributeValue("[1fff:0:a88:85a5::ac1f]:8001")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"), new IPAddressAttributeValue("[1eee:0:a88:85a5::ac1f]:8001")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
				new Object[] { NAME_DNSNAME_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("\\.com$"), new DNSNameAttributeValue("thalesgroup.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_DNSNAME_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("\\.org$"), new DNSNameAttributeValue("thalesgroup.com")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
				new Object[] { NAME_RFC822NAME_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"), new RFC822NameAttributeValue("anne.anderson@sun.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_REGEXP_MATCH, Arrays.asList(new StringAttributeValue("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"), new RFC822NameAttributeValue("anderson@sun.com")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
				new Object[] { NAME_X500NAME_REGEXP_MATCH, Arrays.asList(new StringAttributeValue(".*dc=example,dc=com"), new X500NameAttributeValue("ou=test,dc=example,dc=com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_X500NAME_REGEXP_MATCH, Arrays.asList(new StringAttributeValue(".*dc=example,dc=com"), new X500NameAttributeValue("ou=test,dc=sun,dc=com")), BooleanAttributeValue.FALSE });
	}

}
