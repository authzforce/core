/**
 * Copyright 2012-2018 THALES.
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
package org.ow2.authzforce.core.pdp.cli.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ow2.authzforce.core.pdp.cli.PdpCommandLineCallable;

import picocli.CommandLine;

public class CliTest
{

	private static final String TEST_DATA_DIR = "src/test/resources/conformance/xacml-3.0-core/mandatory";

	@Test
	public void test()
	{
		/*
		 * Should throw IllegalArgumentException for invalid pdp config, not NPE (because of relative path with no
		 * parent path which used to cause NPE when trying to get the parent directory path)
		 */
		CommandLine.call(new PdpCommandLineCallable(), System.out, TEST_DATA_DIR + "/pdp.xml",
				TEST_DATA_DIR + "/IIA001/Request.xml");
	}

	/**
	 * Non-regression test for https://github.com/authzforce/core/issues/9
	 */
	@Test
	public void IssueGH9()
	{
		/*
		 * Should throw IllegalArgumentException for invalid pdp config, not NPE (because of relative path with no
		 * parent path which used to cause NPE when trying to get the parent directory path)
		 */
		try
		{
			CommandLine.call(new PdpCommandLineCallable(), System.out, "pom.xml",
					TEST_DATA_DIR + "/IIA001/Request.xml");
		}
		catch (final CommandLine.ExecutionException e)
		{
			assertTrue(e.getCause().getClass() == IllegalArgumentException.class);
		}
	}

}
