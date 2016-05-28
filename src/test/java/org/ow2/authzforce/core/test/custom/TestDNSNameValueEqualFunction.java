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
package org.ow2.authzforce.core.test.custom;

import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;

/**
 * Implements the dnsName-value-equal function from <i>XACML Data Loss Prevention / Network Access Control (DLP/NAC) Profile Version 1.0<i>. Edited by John
 * Tolbert, Richard Hill, Crystal Hayes, David Brossard, Hal Lockhart, and Steven Legg. 16 February 2015. OASIS Committee Specification 01.
 * http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/cs01/xacml-3.0-dlp-nac-v1.0-cs01.html. Latest version:
 * http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html.
 * <p>
 * Used here for testing Authzforce function extension mechanism, i.e. plugging a custom function into the PDP engine.
 */
public class TestDNSNameValueEqualFunction extends EqualTypeMatchFunction<TestDNSNameWithPortValue>
{
	/**
	 * Function identifier
	 */
	public static final String ID = "urn:oasis:names:tc:xacml:3.0:function:dnsName-value-equal";

	private static final Matcher<TestDNSNameWithPortValue> MATCHER = new Matcher<TestDNSNameWithPortValue>()
	{
		@Override
		public boolean match(TestDNSNameWithPortValue arg0, TestDNSNameWithPortValue arg1)
		{
			// ports are ignored as per spec
			return arg0.getHostName().equalsIgnoreCase(arg1.getHostName());
		}
	};

	public TestDNSNameValueEqualFunction()
	{
		super(ID, TestDNSNameWithPortValue.FACTORY.getDatatype(), MATCHER);
	}
}
