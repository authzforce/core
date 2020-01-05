/**
 * Copyright 2012-2020 THALES.
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.cli.PdpCommandLineCallable;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.json.model.XacmlJsonUtils;
import org.testng.Assert;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import picocli.CommandLine;

public class CliTest
{

	private static final String TEST_DATA_DIR = "src/test/resources/conformance/xacml-3.0-core/mandatory";

	@Test
	public void testXml() throws UnsupportedEncodingException, JAXBException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(baos, true, "UTF-8"))
		{
			/*
			 * Redirect system.out to the byte stream
			 */
			System.setOut(ps);
			/*
			 * Should throw IllegalArgumentException for invalid pdp config, not NPE (because of relative path with no parent path which used to cause NPE when trying to get the parent directory path)
			 */
			CommandLine.call(new PdpCommandLineCallable(), System.out, "-p", TEST_DATA_DIR + "/pdp.xml", TEST_DATA_DIR + "/IIA001/Request.xml");
			System.setOut(System.out);
		}

		final String output = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		System.out.println(output);
		final Response expectedXacmlJaxbObj = (Response) Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(new File(TEST_DATA_DIR + "/IIA001/Response.xml"));

		final Response actualXacmlJaxbObj;
		try
		{
			actualXacmlJaxbObj = (Response) Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(new StringReader(output));
			TestUtils.assertNormalizedEquals(TEST_DATA_DIR + "/IIA001", expectedXacmlJaxbObj, actualXacmlJaxbObj);
		}
		catch (final JAXBException e)
		{
			Assert.fail("Invalid XACML/XML Response returned", e);
		}

	}

	@Test
	public void testJson() throws IOException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (PrintStream ps = new PrintStream(baos, true, "UTF-8"))
		{
			/*
			 * Redirect system.out to the byte stream
			 */
			System.setOut(ps);
			/*
			 * Should throw IllegalArgumentException for invalid pdp config, not NPE (because of relative path with no parent path which used to cause NPE when trying to get the parent directory path)
			 */
			CommandLine.call(new PdpCommandLineCallable(), System.out, "-p", "-tXACML_JSON", TEST_DATA_DIR + "/pdp.xml", TEST_DATA_DIR + "/IIA001/Request.json");
			System.setOut(System.out);
		}

		final String output = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		System.out.println(output);

		final JSONObject normalizedExpectedResponse;
		try (final BufferedReader reader = Files.newBufferedReader(Paths.get(TEST_DATA_DIR + "/IIA001/Response.json"), StandardCharsets.UTF_8))
		{
			normalizedExpectedResponse = XacmlJsonUtils.canonicalizeResponse(new JSONObject(new JSONTokener(reader)));
		}
		final JSONObject normalizedActualResponse = XacmlJsonUtils.canonicalizeResponse(new JSONObject(output));
		Assert.assertTrue(normalizedActualResponse.similar(normalizedExpectedResponse), "Actual XACML/JSON Response does not match expected");
	}

	/**
	 * Non-regression test for https://github.com/authzforce/core/issues/9
	 */
	@Test
	public void IssueGH9()
	{
		/*
		 * Should throw IllegalArgumentException for invalid pdp config, not NPE (because of relative path with no parent path which used to cause NPE when trying to get the parent directory path)
		 */
		try
		{
			CommandLine.call(new PdpCommandLineCallable(), System.out, "pom.xml", TEST_DATA_DIR + "/IIA001/Request.json");
		}
		catch (final CommandLine.ExecutionException e)
		{
			assertTrue(e.getCause().getClass() == IllegalArgumentException.class);
		}
	}

}
