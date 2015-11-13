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
package com.thalesgroup.authzforce.core.test.attr;

import java.util.Arrays;
import java.util.Collection;

import net.sf.saxon.lib.StandardURIChecker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class AnyURIAttributeTest
{
	@Parameters
	public static Collection<Object[]> data()
	{
		Object[][] data = new Object[][] { { "http://datypic.com", "absolute URI (also a URL)", true }, { "mailto:info@datypic.com", "absolute URI", true }, { "../%C3%A9dition.html", "relative URI containing escaped non-ASCII character", true },
				{ "../édition.html", "relative URI containing escaped non-ASCII character", true }, { "http://datypic.com/prod.html#shirt", "URI with fragment identifier", true }, { "../prod.html#shirt", "relative URI with fragment identifier", true }, { "", "an empty value is allowed", true },
				{ "http://datypic.com#frag1#frag2", "too many # characters", false }, { "http://datypic.com#f% rag", "% character followed by something other than two hexadecimal digits", false } };
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
