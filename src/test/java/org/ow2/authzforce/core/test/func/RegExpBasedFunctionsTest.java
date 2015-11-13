/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.test.utils.FunctionTest;
import org.ow2.authzforce.core.value.AnyURIValue;
import org.ow2.authzforce.core.value.BooleanValue;
import org.ow2.authzforce.core.value.DNSNameValue;
import org.ow2.authzforce.core.value.IPAddressValue;
import org.ow2.authzforce.core.value.RFC822NameValue;
import org.ow2.authzforce.core.value.StringValue;
import org.ow2.authzforce.core.value.Value;
import org.ow2.authzforce.core.value.X500NameValue;

@RunWith(Parameterized.class)
public class RegExpBasedFunctionsTest extends FunctionTest
{

	public RegExpBasedFunctionsTest(String functionName, List<Value> inputs, Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
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
				new Object[] { NAME_STRING_REGEXP_MATCH, Arrays.asList(new StringValue("John.*"), new StringValue("John Doe")), BooleanValue.TRUE },
				new Object[] { NAME_STRING_REGEXP_MATCH, Arrays.asList(new StringValue("John.*"), new StringValue("Jane Doe")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
				new Object[] { NAME_ANYURI_REGEXP_MATCH, Arrays.asList(new StringValue("^http://.+"), new AnyURIValue("http://www.thalesgroup.com")),
						BooleanValue.TRUE },
				new Object[] { NAME_ANYURI_REGEXP_MATCH, Arrays.asList(new StringValue("^http://.+"), new AnyURIValue("https://www.thalesgroup.com")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH,
						Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"), new IPAddressValue("10.10.10.190")),
						BooleanValue.TRUE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH,
						Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"), new IPAddressValue("10.144.10.190")),
						BooleanValue.FALSE },
				new Object[] {
						NAME_IPADDRESS_REGEXP_MATCH,
						Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"), new IPAddressValue(
								"10.10.10.10/255.255.255.0:80")), BooleanValue.TRUE },
				new Object[] {
						NAME_IPADDRESS_REGEXP_MATCH,
						Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"), new IPAddressValue(
								"192.168.1.10/255.255.255.0:8080")), BooleanValue.FALSE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH,
						Arrays.asList(new StringValue("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"), new IPAddressValue("[1fff:0:a88:85a5::ac1f]:8001")),
						BooleanValue.TRUE },
				new Object[] { NAME_IPADDRESS_REGEXP_MATCH,
						Arrays.asList(new StringValue("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"), new IPAddressValue("[1eee:0:a88:85a5::ac1f]:8001")),
						BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
				new Object[] { NAME_DNSNAME_REGEXP_MATCH, Arrays.asList(new StringValue("\\.com$"), new DNSNameValue("thalesgroup.com")), BooleanValue.TRUE },
				new Object[] { NAME_DNSNAME_REGEXP_MATCH, Arrays.asList(new StringValue("\\.org$"), new DNSNameValue("thalesgroup.com")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
				new Object[] { NAME_RFC822NAME_REGEXP_MATCH,
						Arrays.asList(new StringValue("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"), new RFC822NameValue("anne.anderson@sun.com")), BooleanValue.TRUE },
				new Object[] { NAME_RFC822NAME_REGEXP_MATCH,
						Arrays.asList(new StringValue("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"), new RFC822NameValue("anderson@sun.com")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
				new Object[] { NAME_X500NAME_REGEXP_MATCH,
						Arrays.asList(new StringValue(".*dc=example,dc=com"), new X500NameValue("ou=test,dc=example,dc=com")), BooleanValue.TRUE },
				new Object[] { NAME_X500NAME_REGEXP_MATCH, Arrays.asList(new StringValue(".*dc=example,dc=com"), new X500NameValue("ou=test,dc=sun,dc=com")),
						BooleanValue.FALSE });
	}

}
