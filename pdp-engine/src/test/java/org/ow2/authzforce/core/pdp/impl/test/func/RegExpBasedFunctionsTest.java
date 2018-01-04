/**
 * Copyright 2012-2018 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.impl.test.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.DnsNameWithPortRangeValue;
import org.ow2.authzforce.core.pdp.api.value.IpAddressValue;
import org.ow2.authzforce.core.pdp.api.value.Rfc822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;

@RunWith(Parameterized.class)
public class RegExpBasedFunctionsTest extends StandardFunctionTest
{

	public RegExpBasedFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
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
		return Arrays
				.asList(
				// urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
				new Object[] { NAME_STRING_REGEXP_MATCH, Arrays.asList(new StringValue("John.*"), new StringValue("John Doe")), BooleanValue.TRUE },
						new Object[] { NAME_STRING_REGEXP_MATCH, Arrays.asList(new StringValue("John.*"), new StringValue("Jane Doe")), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
						new Object[] { NAME_ANYURI_REGEXP_MATCH, Arrays.asList(new StringValue("^http://.+"), new AnyUriValue("http://www.example.com")), BooleanValue.TRUE },
						new Object[] { NAME_ANYURI_REGEXP_MATCH, Arrays.asList(new StringValue("^http://.+"), new AnyUriValue("https://www.example.com")), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
						new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"), new IpAddressValue("10.10.10.190")),
								BooleanValue.TRUE },
						new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"), new IpAddressValue("10.144.10.190")),
								BooleanValue.FALSE },
						new Object[] { NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"), new IpAddressValue("10.10.10.10/255.255.255.0:80")),
								BooleanValue.TRUE },
						new Object[] { NAME_IPADDRESS_REGEXP_MATCH,
								Arrays.asList(new StringValue("^10\\.10\\.10\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])/255\\.255\\.255\\.0:80$"), new IpAddressValue("192.168.1.10/255.255.255.0:8080")),
								BooleanValue.FALSE },
						new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringValue("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"), new IpAddressValue("[1fff:0:a88:85a5::ac1f]:8001")),
								BooleanValue.TRUE },
						new Object[] { NAME_IPADDRESS_REGEXP_MATCH, Arrays.asList(new StringValue("^\\[1fff(:[0-9a-f]*)+\\](:[0-9]{1,5})?$"), new IpAddressValue("[1eee:0:a88:85a5::ac1f]:8001")),
								BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
						new Object[] { NAME_DNSNAME_REGEXP_MATCH, Arrays.asList(new StringValue("\\.com$"), new DnsNameWithPortRangeValue("example.com")), BooleanValue.TRUE },
						new Object[] { NAME_DNSNAME_REGEXP_MATCH, Arrays.asList(new StringValue("\\.org$"), new DnsNameWithPortRangeValue("example.com")), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
						new Object[] { NAME_RFC822NAME_REGEXP_MATCH, Arrays.asList(new StringValue("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"), new Rfc822NameValue("anne.anderson@sun.com")), BooleanValue.TRUE },
						new Object[] { NAME_RFC822NAME_REGEXP_MATCH, Arrays.asList(new StringValue("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+@.+"), new Rfc822NameValue("anderson@sun.com")), BooleanValue.FALSE },

						// urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
						new Object[] { NAME_X500NAME_REGEXP_MATCH, Arrays.asList(new StringValue(".*dc=example,dc=com"), new X500NameValue("ou=test,dc=example,dc=com")), BooleanValue.TRUE },
						new Object[] { NAME_X500NAME_REGEXP_MATCH, Arrays.asList(new StringValue(".*dc=example,dc=com"), new X500NameValue("ou=test,dc=sun,dc=com")), BooleanValue.FALSE });
	}

}
