/*
 * Copyright 2012-2023 THALES.
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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.cli.PdpCommandLineCallable;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.json.model.XacmlJsonUtils;
import org.testng.Assert;
import picocli.CommandLine;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertSame;

public class CliTest
{

    private static final String TEST_DATA_DIR = "src/test/resources/conformance/xacml-3.0-core/mandatory";

    @Test
    public void testXml() throws JAXBException
    {
        final CommandLine cmdLine = new CommandLine(new PdpCommandLineCallable());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8))
        {
            /*
             * Redirect system.out to the byte stream
             */
            System.setOut(ps);
            cmdLine.execute("-p", TEST_DATA_DIR + "/pdp.xml", TEST_DATA_DIR + "/IIA001/Request.xml");
        }
        System.setOut(System.out);
        final String output = baos.toString(StandardCharsets.UTF_8);
        final Response expectedXacmlJaxbObj = (Response) Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(new File(TEST_DATA_DIR + "/IIA001/Response.xml"));
        final Response actualXacmlJaxbObj;
        try
        {
            actualXacmlJaxbObj = (Response) Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(new StringReader(output));
            TestUtils.assertNormalizedEquals(TEST_DATA_DIR + "/IIA001", expectedXacmlJaxbObj, actualXacmlJaxbObj, true);
        } catch (final JAXBException e)
        {
            Assert.fail("Invalid XACML/XML Response returned", e);
        }

    }

    @Test
    public void testJson() throws IOException
    {
        final CommandLine cmd = new CommandLine(new PdpCommandLineCallable());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8))
        {
            /*
             * Redirect system.out to the byte stream
             */
            System.setOut(ps);
            cmd.execute("-p", "-tXACML_JSON", TEST_DATA_DIR + "/pdp.xml", TEST_DATA_DIR + "/IIA001/Request.json");
        }
        System.setOut(System.out);
        final String output = baos.toString(StandardCharsets.UTF_8);
        final JSONObject normalizedExpectedResponse;
        try (final BufferedReader reader = Files.newBufferedReader(Paths.get(TEST_DATA_DIR + "/IIA001/Response.json"), StandardCharsets.UTF_8))
        {
            normalizedExpectedResponse = XacmlJsonUtils.canonicalizeResponse(new JSONObject(new JSONTokener(reader)), true);
        }
        final JSONObject normalizedActualResponse = XacmlJsonUtils.canonicalizeResponse(new JSONObject(output), true);
        Assert.assertTrue(normalizedActualResponse.similar(normalizedExpectedResponse), "Actual XACML/JSON Response does not match expected");
    }

    /**
     * Non-regression test for <a href="https://github.com/authzforce/core/issues/9">Issue GH-9: Getting Started Problem</a>
     */
    @Test
    public void IssueGH9()
    {
        final CommandLine cmd = new CommandLine(new PdpCommandLineCallable());
        /*
         * Should throw IllegalArgumentException for invalid pdp config, not NPE (because of relative path with no parent path which used to cause NPE when trying to get the parent directory path)
         */
        try
        {
            cmd.execute("pom.xml", TEST_DATA_DIR + "/IIA001/Request.json");
        } catch (final CommandLine.ExecutionException e)
        {
            assertSame(e.getCause().getClass(), IllegalArgumentException.class);
        }
    }

}
