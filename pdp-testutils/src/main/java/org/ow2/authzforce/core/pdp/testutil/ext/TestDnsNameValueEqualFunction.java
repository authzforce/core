/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.testutil.ext;

import org.ow2.authzforce.core.pdp.api.func.EqualTypeMatchFunction;

/**
 * Implements the dnsName-value-equal function from <i>XACML Data Loss Prevention / Network Access Control (DLP/NAC) Profile Version 1.0<i>. Edited by John
 * Tolbert, Richard Hill, Crystal Hayes, David Brossard, Hal Lockhart, and Steven Legg. 16 February 2015. OASIS Committee Specification 01.
 * http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/cs01/xacml-3.0-dlp-nac-v1.0-cs01.html. Latest version:
 * http://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html.
 * <p>
 * Used here for testing Authzforce function extension mechanism, i.e. plugging a custom function into the PDP engine.
 */
public class TestDnsNameValueEqualFunction extends EqualTypeMatchFunction<TestDnsNameWithPortValue>
{
	/**
	 * Function identifier
	 */
	public static final String ID = "urn:oasis:names:tc:xacml:3.0:function:dnsName-value-equal";

	private static final Matcher<TestDnsNameWithPortValue> MATCHER = new Matcher<TestDnsNameWithPortValue>()
	{
		@Override
		public boolean match(TestDnsNameWithPortValue arg0, TestDnsNameWithPortValue arg1)
		{
			// ports are ignored as per spec
			return arg0.getHostName().equalsIgnoreCase(arg1.getHostName());
		}
	};

	public TestDnsNameValueEqualFunction()
	{
		super(ID, TestDnsNameWithPortValue.FACTORY.getDatatype(), MATCHER);
	}
}
