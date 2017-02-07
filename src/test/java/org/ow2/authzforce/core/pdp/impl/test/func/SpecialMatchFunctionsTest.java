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
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.RFC822NameValue;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.X500NameValue;
import org.ow2.authzforce.core.pdp.impl.test.utils.FunctionTest;

@RunWith(Parameterized.class)
public class SpecialMatchFunctionsTest extends FunctionTest
{

	public SpecialMatchFunctionsTest(final String functionName, final List<Value> inputs, final Value expectedResult)
	{
		super(functionName, null, inputs, expectedResult);
	}

	private static final String NAME_X500NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:x500Name-match";
	private static final String NAME_RFC822NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
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
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new RFC822NameValue("Anderson@sun.com")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new RFC822NameValue("Anderson@SUN.COM")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new RFC822NameValue("Anne.Anderson@sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new RFC822NameValue("anderson@sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("Anderson@sun.com"), new RFC822NameValue("Anderson@east.sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("sun.com"), new RFC822NameValue("Anderson@sun.com")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("sun.com"), new RFC822NameValue("Baxter@SUN.COM")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue("sun.com"), new RFC822NameValue("Anderson@east.sun.com")), BooleanValue.FALSE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue(".east.sun.com"), new RFC822NameValue("Anderson@east.sun.com")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue(".east.sun.com"), new RFC822NameValue("anne.anderson@ISRG.EAST.SUN.COM")), BooleanValue.TRUE },
				//
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringValue(".east.sun.com"), new RFC822NameValue("Anderson@sun.com")), BooleanValue.FALSE });
	}

}
