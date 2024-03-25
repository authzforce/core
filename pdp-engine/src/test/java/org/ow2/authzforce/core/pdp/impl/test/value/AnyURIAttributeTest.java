/*
 * Copyright 2012-2024 THALES.
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
package org.ow2.authzforce.core.pdp.impl.test.value;

import net.sf.saxon.lib.StandardURIChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * XACML anyURI validation test. This test is no longer used since we refer to the definition of anyURI datatype given in XSD 1.1, which has the same value space as the string datatype. This is
 * confirmed by <a href="http://www.saxonica.com/html/documentation9.4/changes/intro93/xsd11-93.html">SAXON documentation</a>.
 * <p>
 * Although XACML 3.0 still refers to XSD 1.0 and its stricter definition of anyURI, we prefer to anticipate and use the definition from XSD 1.1 for XACML AttributeValues of datatype anyURI. However,
 * this does not affect XACML schema validation of Policy/PolicySet/Request documents, where the XSD 1.0 definition of anyURI still applies.
 * <p>
 * This class is kept for the record only.
 */
@RunWith(value = Parameterized.class)
public class AnyURIAttributeTest
{
	@Parameters
	public static Collection<Object[]> data()
	{
		final Object[][] data = new Object[][] { { "http://datypic.com", "absolute URI (also a URL)", true }, { "mailto:info@datypic.com", "absolute URI", true },
		        { "../%C3%A9dition.html", "relative URI containing escaped non-ASCII character", true }, { "../édition.html", "relative URI containing escaped non-ASCII character", true },
		        { "http://datypic.com/prod.html#shirt", "URI with fragment identifier", true }, { "../prod.html#shirt", "relative URI with fragment identifier", true },
		        { "", "an empty value is allowed", true }, { "http://datypic.com#frag1#frag2", "too many # characters", false },
		        { "http://datypic.com#f% rag", "% character followed by something other than two hexadecimal digits", false } };
		return Arrays.asList(data);
	}

	private final String value;
	private final String comment;
	private final boolean isValid;

	public AnyURIAttributeTest(String anyURI, String comment, boolean isValid)
	{
		this.value = anyURI;
		this.comment = comment;
		this.isValid = isValid;
	}

	@Test
	public void test()
	{
		final boolean actualIsValidResult = StandardURIChecker.getInstance().isValidURI(this.value);
		Assert.assertEquals("Test failed on: '" + this.value + "' (" + this.comment + ")", isValid, actualIsValidResult);
	}

}
