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
package org.ow2.authzforce.core.pdp.testutil.test.json;

import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.io.xacml.json.BaseXacmlJsonResultPostprocessor;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.xacml.json.model.LimitsCheckingJSONObject;
import org.ow2.authzforce.xacml.json.model.XacmlJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * XACML JSON Profile conformance testing class. For tests testing validation of XACML policy syntax, the PDP is expected to reject the policy before receiving any Request. For these tests, the
 * Request.xml and Response.xml test class are absent, to indicate that an invalid policy syntax is expected.
 * <p>
 * For tests testing validation of XACML Request syntax, the PDP is expected to reject the request before evaluation. For these tests, the original Policy.xml and Response.xml must be renamed to
 * Policy.xml.ignore and Response.xml.ignore to indicate to this test class, that an invalid Request syntax is expected.
 */
public abstract class XacmlJsonTest
{
    /**
     * Suffix of filename of root XACML Policy(Set). The actual filename is the concatenation of the test ID and this suffix.
     */
    public static final String ROOT_POLICY_FILENAME_SUFFIX = "Policy.xml";

    /**
     * Suffix of filename of XACML request to send to the PDP. The actual filename is the concatenation of the test ID and this suffix.
     */
    public static final String REQUEST_FILENAME_SUFFIX = "Request.json";

    /**
     * Suffix of filename of the expected XACML response from the PDP. The actual filename is the concatenation of the test ID and this suffix.
     */
    public static final String EXPECTED_RESPONSE_FILENAME_SUFFIX = "Response.json";

    /**
     * Suffix of name of directory containing files of XACML Policy(Set) that can be referenced from root policy via Policy(Set)IdReference. The actual directory name is the concatenation of the test
     * ID and this suffix.
     */
    public final static String POLICIES_DIRNAME_SUFFIX = "Policies";

    /**
     * Suffix of filename of an AttributeProvider configuration. The actual filename is the concatenation of the test ID and this suffix.
     */
    public static final String ATTRIBUTE_PROVIDER_FILENAME_SUFFIX = "AttributeProvider.xml";

    /**
     * PDP Configuration file name
     */
    public final static String PDP_CONF_FILENAME = "pdp.xml";

    /**
     * PDP extensions schema
     */
    public final static String PDP_EXTENSION_XSD_LOCATION = "classpath:pdp-ext.xsd";

    /**
     * Spring-supported location to XML catalog (may be prefixed with classpath:, etc.)
     */
    public final static String XML_CATALOG_LOCATION = "classpath:catalog.xml";

    private static final boolean ENABLE_XPATH = false;

    // private static final String RESULT_POSTPROC_ID = "urn:ow2:authzforce:feature:pdp:result-postproc:xacml-json:default";

    private static final int MAX_JSON_STRING_LENGTH = 100;

    /*
     * Max number of child elements - key-value pairs or items - in JSONObject/JSONArray
     */
    private static final int MAX_JSON_CHILDREN_COUNT = 100;

    private static final int MAX_JSON_DEPTH = 10;

    // private final XmlnsFilteringParserFactory XACML_PARSER_FACTORY = XacmlJaxbParsingUtils.getXacmlParserFactory(ENABLE_XPATH);

    /**
     * the logger we'll use for all messages
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XacmlJsonTest.class);

    /**
     * For each test folder {@code testRootDir}/{@code testSubDirectoryName}, it creates the test parameters: path to the test directory containing Request.json, Response.json, etc., and the ID of request filter to be applied (null means the default lax variant of the single-decision request preprocessor
     *
     * @param testRootDir     path to root directory of all test data
     * @param requestFilterId PDP request filter ID to be used for the tests
     * @return test data
     */
    public static Collection<Object[]> getTestData(final String testRootDir, final String requestFilterId) throws URISyntaxException, IOException
    {
        final Collection<Object[]> testData = new ArrayList<>();
        /*
         * Each sub-directory of the root directory is data for a specific test. So we configure a test for each directory
         */
        final URL testRootDirLocation = ResourceUtils.getURL(testRootDir);
        final Path testRootDirPath = Paths.get(testRootDirLocation.toURI());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(testRootDirPath, Files::isDirectory))
        {
            for (final Path testDirPath : stream)
            {
                // specific test's resources directory location and request filter ID, used as parameters to test(...)
                testData.add(new Object[]{testDirPath, requestFilterId});
            }
        }

        return testData;
    }

    /**
     * assertEquals() for XACML/JSON responses (handles normalization of the responses)
     *
     * @param testId                test identifier
     * @param expectedResponse      expected response
     * @param actualResponseFromPDP actual response
     */
    public static void assertNormalizedEquals(final String testId, final JSONObject expectedResponse, final JSONObject actualResponseFromPDP)
    {
        if (testId == null)
        {
            throw new IllegalArgumentException("Undefined test ID");
        }

        if (expectedResponse == null)
        {
            throw new IllegalArgumentException("Undefined expected response for response equality check");
        }

        if (actualResponseFromPDP == null)
        {
            throw new IllegalArgumentException("Undefined actual response  for response equality check");
        }

        // normalize responses for comparison
        final JSONObject normalizedExpectedResponse = XacmlJsonUtils.canonicalizeResponse(expectedResponse, true);
        final JSONObject normalizedActualResponse = XacmlJsonUtils.canonicalizeResponse(actualResponseFromPDP, true);
        Assert.assertTrue(normalizedActualResponse.similar(normalizedExpectedResponse),
                "Test '" + testId + "' (StatusMessage/StatusDetail/nested StatusCode elements removed/ignored for comparison): expected: <" + normalizedExpectedResponse + "> ; actual: <"
                        + normalizedActualResponse + ">");
    }

    @Test(dataProvider = "getTestDirectories")
    public void test(final Path testDirectoryPath, final String reqFilterId) throws Exception
    {
        LOGGER.debug("******************************");
        LOGGER.debug("Starting PDP test in directory '{}'", testDirectoryPath);

        // Response file
        final Path expectedRespFilepath = testDirectoryPath.resolve(EXPECTED_RESPONSE_FILENAME_SUFFIX);
        // If no Response file, it is just a static policy or request syntax error check
        final JSONObject expectedResponse;
        if (Files.exists(expectedRespFilepath))
        {
            try (final BufferedReader reader = Files.newBufferedReader(expectedRespFilepath, StandardCharsets.UTF_8))
            {
                expectedResponse = new LimitsCheckingJSONObject(reader, MAX_JSON_STRING_LENGTH, MAX_JSON_CHILDREN_COUNT, MAX_JSON_DEPTH);
                if (!expectedResponse.has("Response"))
                {
                    throw new IllegalArgumentException("Invalid XACML JSON Response file: " + expectedRespFilepath + ". Expected root key: \"Response\"");
                }

                XacmlJsonUtils.RESPONSE_SCHEMA.validate(expectedResponse);
            }
        } else
        {
            expectedResponse = null;
            // Do nothing except logging -> request = null
            LOGGER.debug("Response file '{}' does not exist -> Static Policy/Request syntax error check", expectedRespFilepath);
        }

        // Request file
        final Path reqFilepath = testDirectoryPath.resolve(REQUEST_FILENAME_SUFFIX);
        // If no Request file, it is just a static policy syntax error check
        final JSONObject request;
        if (Files.exists(reqFilepath))
        {
            try (InputStream inputStream = new FileInputStream(reqFilepath.toFile()))
            {
                request = new JSONObject(new JSONTokener(inputStream));
                if (!request.has("Request"))
                {
                    throw new IllegalArgumentException("Invalid XACML JSON Request file: " + reqFilepath + ". Expected root key: \"Request\"");
                }

                try
                {
                    XacmlJsonUtils.REQUEST_SCHEMA.validate(request);
                } catch (ValidationException e)
                {
                    // we found a syntax error in request
                    if (expectedResponse == null)
                    {
                        // this is a Request syntax error check and we found the syntax error as
                        // expected -> success
                        LOGGER.debug("Successfully found syntax error as expected in Request located at: {}", reqFilepath);
                        return;
                    }

                    // Unexpected error
                    throw e;
                }
            }
        } else
        {
            request = null;
            // do nothing except logging -> request = null
            LOGGER.debug("Request file '{}' does not exist -> Static policy syntax error check (Request/Response ignored)", reqFilepath);
        }

        /*
         * Create PDP
         */
        final PdpEngineConfiguration pdpEngineConf;
        final Path pdpConfFile = testDirectoryPath.resolve(PDP_CONF_FILENAME);
        if (Files.notExists(pdpConfFile))
        {
            /*
             * Policies directory. If it exists, root Policy file is expected to be in there. This is the case for IIE*** conformance tests
             */
            final Path policiesDir = testDirectoryPath.resolve(POLICIES_DIRNAME_SUFFIX);
            /*
            Attribute Provider config
             */
            final Path attributeProviderConfFile = testDirectoryPath.resolve(ATTRIBUTE_PROVIDER_FILENAME_SUFFIX);
            final Optional<Path> optAttributeProviderConfFile = Files.isRegularFile(attributeProviderConfFile) ? Optional.of(attributeProviderConfFile) : Optional.empty();

            try
            {
                if (Files.isDirectory(policiesDir))
                {
                    final Path rootPolicyFile = policiesDir.resolve(ROOT_POLICY_FILENAME_SUFFIX);
                    pdpEngineConf = TestUtils.newPdpEngineConfiguration(TestUtils.getPolicyRef(rootPolicyFile), policiesDir, ENABLE_XPATH, optAttributeProviderConfFile, reqFilterId, BaseXacmlJsonResultPostprocessor.DefaultFactory.ID);
                } else
                {
                    final Path rootPolicyFile = testDirectoryPath.resolve(ROOT_POLICY_FILENAME_SUFFIX);
                    pdpEngineConf = TestUtils.newPdpEngineConfiguration(rootPolicyFile, ENABLE_XPATH, optAttributeProviderConfFile, reqFilterId, BaseXacmlJsonResultPostprocessor.DefaultFactory.ID);
                }
            } catch (final IllegalArgumentException e)
            {
                // we found syntax error in policy
                if (request == null)
                {
                    // this is a policy syntax error check and we found the syntax error as
                    // expected -> success
                    LOGGER.debug("Successfully found syntax error as expected in policy(ies) with path: {}*", testDirectoryPath);
                    return;
                }

                // Unexpected error
                throw e;
            }

        } else
        {
            /*
             * PDP configuration filename found in test directory -> create PDP from it
             */
            // final String pdpExtXsdLocation = testResourceLocationPrefix + PDP_EXTENSION_XSD_FILENAME;
            File pdpExtXsdFile = null;
            try
            {
                pdpExtXsdFile = ResourceUtils.getFile(PDP_EXTENSION_XSD_LOCATION);
            } catch (final FileNotFoundException e)
            {
                LOGGER.debug("No PDP extension configuration file '{}' found -> JAXB-bound PDP extensions not allowed.", PDP_EXTENSION_XSD_LOCATION);
            }

            try
            {
                /*
                 * Load the PDP configuration from the configuration, and optionally, the PDP extension XSD if this file exists, and the XML catalog required to resolve these extension XSDs
                 */
                pdpEngineConf = pdpExtXsdFile == null ? PdpEngineConfiguration.getInstance(pdpConfFile.toString())
                        : PdpEngineConfiguration.getInstance(pdpConfFile.toString(), XML_CATALOG_LOCATION, PDP_EXTENSION_XSD_LOCATION);
            } catch (final IOException e)
            {
                throw new RuntimeException("Error parsing PDP configuration from file '" + pdpConfFile + "' with extension XSD '" + PDP_EXTENSION_XSD_LOCATION + "' and XML catalog file '"
                        + XML_CATALOG_LOCATION + "'", e);
            }
        }

        try (final PdpEngineInoutAdapter<JSONObject, JSONObject> pdp = PdpEngineXacmlJsonAdapters.newXacmlJsonInoutAdapter(pdpEngineConf))
        {
            if (request == null)
            {
                // this is a policy syntax error check and we didn't found the syntax error as
                // expected
                org.junit.Assert.fail("Failed to find syntax error as expected in policy(ies)  with path: " + testDirectoryPath + "*");
            }
            else if (expectedResponse == null)
            {
                /*
                 * No expected response, so it is not a PDP evaluation test, but request or policy syntax error check. We got here, so request and policy OK. This is unexpected.
                 */
                org.junit.Assert.fail("Missing response file '" + expectedRespFilepath + "' or failed to find syntax error as expected in either request located at '" + reqFilepath
                        + "' or policy(ies) with path '" + testDirectoryPath + "*'");

            }
            else
            {
                // this is an evaluation test with request/response (not a policy syntax check)
                LOGGER.debug("Request that is sent to the PDP: {}", request);
                final JSONObject actualResponse = pdp.evaluate(request);
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Response that is received from the PDP :  {}", actualResponse);
                }

                assertNormalizedEquals("Test failed for directory "+ testDirectoryPath, expectedResponse, actualResponse);
            }
        }
        catch (final IllegalArgumentException e)
        {
            // we found syntax error in policy
            if (request == null)
            {
                // this is a policy syntax error check and we found the syntax error as
                // expected -> success
                LOGGER.debug("Successfully found syntax error as expected in policy(ies) with path: {}*", testDirectoryPath);
                return;
            }

            // Unexpected error
            throw e;
        }
    }
}