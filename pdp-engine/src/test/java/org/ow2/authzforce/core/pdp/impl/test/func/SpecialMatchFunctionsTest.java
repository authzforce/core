/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.impl.test.func;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.value.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class SpecialMatchFunctionsTest extends StandardFunctionTest
{

	public SpecialMatchFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_X500NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:x500Name-match";
	private static final String NAME_RFC822NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params()
    {
		return Arrays.asList(
		// urn:oasis:names:tc:xacml:1.0:function:x500Name-match
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameValue("O=Medico Corp,C=US"), new X500NameValue("cn=John Smith,o=Medico Corp, c=US")), BooleanValue.TRUE },
				//
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameValue("O=Medico Corp,C=US"), new X500NameValue("cn=John Smith, o=Medico Corp, c=US")), BooleanValue.TRUE },
				//
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameValue("O=Medico Corp,C=US"), new X500NameValue("cn=John Smith\\,O=Medico Corp, c=US")), BooleanValue.FALSE },
				//
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameValue("O=Medico Corp,C=US"), new X500NameValue("cn=John Smith\\, O=Medico Corp, c=US")), BooleanValue.FALSE },
				//
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameValue("O=Another Corp,C=US"), new X500NameValue("cn=John Smith,o=Medico Corp, c=US")), BooleanValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new Rfc822NameValue("Anderson@sun.com")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new Rfc822NameValue("Anderson@SUN.COM")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new Rfc822NameValue("Anne.Anderson@sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new Rfc822NameValue("anderson@sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new Rfc822NameValue("Anderson@east.sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("sun.com"), new Rfc822NameValue("Anderson@sun.com")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("sun.com"), new Rfc822NameValue("Baxter@SUN.COM")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("sun.com"), new Rfc822NameValue("Anderson@east.sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue(".east.sun.com"), new Rfc822NameValue("Anderson@east.sun.com")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue(".east.sun.com"), new Rfc822NameValue("anne.anderson@ISRG.EAST.SUN.COM")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue(".east.sun.com"), new Rfc822NameValue("Anderson@sun.com")), BooleanValue.FALSE });
	}

}
