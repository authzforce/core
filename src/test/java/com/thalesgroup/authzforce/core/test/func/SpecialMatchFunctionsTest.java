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

import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class SpecialMatchFunctionsTest extends GeneralFunctionTest
{

	public SpecialMatchFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?, ?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_X500NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:x500Name-match";
	private static final String NAME_RFC822NAME_MATCH = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match";

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		return Arrays.asList(
				// urn:oasis:names:tc:xacml:1.0:function:x500Name-match
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameAttributeValue("O=Medico Corp,C=US"), new X500NameAttributeValue("cn=John Smith,o=Medico Corp, c=US")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_X500NAME_MATCH, Arrays.asList(new X500NameAttributeValue("O=Another Corp,C=US"), new X500NameAttributeValue("cn=John Smith,o=Medico Corp, c=US")), BooleanAttributeValue.FALSE },

				// urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@SUN.COM")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anne.Anderson@sun.com")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("anderson@sun.com")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Anderson@east.sun.com")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("sun.com"), new RFC822NameAttributeValue("Baxter@SUN.COM")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue("sun.com"), new RFC822NameAttributeValue("Anderson@east.sun.com")), BooleanAttributeValue.FALSE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue(".east.sun.com"), new RFC822NameAttributeValue("Anderson@east.sun.com")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue(".east.sun.com"), new RFC822NameAttributeValue("anne.anderson@ISRG.EAST.SUN.COM")), BooleanAttributeValue.TRUE },
				new Object[] { NAME_RFC822NAME_MATCH, Arrays.asList(new StringAttributeValue(".east.sun.com"), new RFC822NameAttributeValue("Anderson@sun.com")), BooleanAttributeValue.FALSE });
	}

}
