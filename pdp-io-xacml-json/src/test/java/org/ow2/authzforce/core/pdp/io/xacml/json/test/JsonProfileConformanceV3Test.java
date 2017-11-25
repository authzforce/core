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
package org.ow2.authzforce.core.pdp.io.xacml.json.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.xacml.json.model.LimitsCheckingJSONObject;
import org.ow2.authzforce.xacml.json.model.Xacml3JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * XACML JSON Profile conformance testing class. For tests testing validation of XACML policy syntax, the PDP is expected to reject the policy before receiving any Request. For these tests, the
 * Request.xml and Response.xml test class are absent, to indicate that an invalid policy syntax is expected.
 * <p>
 * For tests testing validation of XACML Request syntax, the PDP is expected to reject the request before evaluation. For these tests, the original Policy.xml and Response.xml must be renamed to
 * Policy.xml.ignore and Response.xml.ignore to indicate to this test class, that an invalid Request syntax is expected.
 */
public class JsonProfileConformanceV3Test
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
	public final static String REF_POLICIES_DIRNAME_SUFFIX = "Repository";

	/**
	 * Suffix of filename of an AttributeProvider configuration. The actual filename is the concatenation of the test ID and this suffix.
	 */
	public static final String ATTRIBUTE_PROVIDER_FILENAME_SUFFIX = "AttributeProvider.xml";

	private static final boolean ENABLE_XPATH = false;

	private static final String REQUEST_PREPROC_ID = "urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:default-lax";

	private static final String RESULT_POSTPROC_ID = "urn:ow2:authzforce:feature:pdp:result-postproc:xacml-json:default";

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
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonProfileConformanceV3Test.class);

	/**
	 * assertEquals() for XACML/JSON responses (handles normalization of the responses)
	 * 
	 * @param testId
	 *            test identifier
	 * @param expectedResponse
	 *            expected response
	 * @param actualResponseFromPDP
	 *            actual response
	 */
	public static void assertNormalizedEquals(final String testId, final JSONObject expectedResponse, final JSONObject actualResponseFromPDP) throws JAXBException
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
		final JSONObject normalizedExpectedResponse = Xacml3JsonUtils.canonicalizeResponse(expectedResponse);
		final JSONObject normalizedActualResponse = Xacml3JsonUtils.canonicalizeResponse(actualResponseFromPDP);
		Assert.assertTrue(normalizedActualResponse.similar(normalizedExpectedResponse), "Test '" + testId
				+ "' (StatusMessage/StatusDetail/nested StatusCode elements removed/ignored for comparison): expected: <" + normalizedExpectedResponse + "> ; actual: <" + normalizedActualResponse
				+ ">");
	}

	public static Collection<Object[]> params(final String testResourcesRootDirectory) throws URISyntaxException, IOException
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		/*
		 * Each sub-directory of the root directory is data for a specific test. So we configure a test for each directory
		 */
		final URL testRootDir = ResourceUtils.getURL(testResourcesRootDirectory);
		final Path testRootPath = Paths.get(testRootDir.toURI());
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testRootPath))
		{
			for (final Path path : stream)
			{
				if (Files.isDirectory(path))
				{
					// specific test's resources directory location, used as parameter to PdpTest(String)
					testParams.add(new Object[] { path });
				}
			}
		}
		catch (final DirectoryIteratorException ex)
		{
			// I/O error encounted during the iteration, the cause is an IOException
			throw ex.getCause();
		}

		return testParams;
	}

	@Test(dataProvider = "getTestDirectories")
	public void test(final Path testDirectoryPath) throws Exception
	{
		LOGGER.debug("******************************");
		LOGGER.debug("Starting PDP test of directory '{}'", testDirectoryPath);

		// Response file
		final Path expectedRespFile = testDirectoryPath.resolve(EXPECTED_RESPONSE_FILENAME_SUFFIX);
		final JSONObject expectedResponse;
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(expectedRespFile.toFile()), StandardCharsets.UTF_8)))
		{
			expectedResponse = new LimitsCheckingJSONObject(reader, MAX_JSON_STRING_LENGTH, MAX_JSON_CHILDREN_COUNT, MAX_JSON_DEPTH);
			if (!expectedResponse.has("Response"))
			{
				throw new IllegalArgumentException("Invalid XACML JSON Response file: " + expectedRespFile + ". Expected root key: \"Response\"");
			}

			Xacml3JsonUtils.RESPONSE_SCHEMA.validate(expectedResponse);
		}

		// Request file
		final JSONObject jsonRequest;
		final Path reqFile = testDirectoryPath.resolve(REQUEST_FILENAME_SUFFIX);
		try (InputStream inputStream = new FileInputStream(reqFile.toFile()))
		{
			jsonRequest = new JSONObject(new JSONTokener(inputStream));
			if (!jsonRequest.has("Request"))
			{
				throw new IllegalArgumentException("Invalid XACML JSON Request file: " + reqFile + ". Expected root key: \"Request\"");
			}

			Xacml3JsonUtils.REQUEST_SCHEMA.validate(jsonRequest);
		}

		final Path rootPolicyFile = testDirectoryPath.resolve(ROOT_POLICY_FILENAME_SUFFIX);
		// referenced policies if any
		final Path refPoliciesDir = testDirectoryPath.resolve(REF_POLICIES_DIRNAME_SUFFIX);

		final Path attributeProviderConfFile = testDirectoryPath.resolve(ATTRIBUTE_PROVIDER_FILENAME_SUFFIX);

		/*
		 * So far we assume the PDP engine configuration files are valid, because for the moment we only test Request/Response in JSON Profile since JSON Profile only applies to these elements (not to
		 * policies) at the moment. If some day, JSON Profile addresses policy format too, then we should do like in ConformanceV3fromV2 class from pdp-testutils package (policy syntax validation).
		 */
		final PdpEngineConfiguration pdpEngineConf = TestUtils.newPdpEngineConfiguration(rootPolicyFile.toUri().toURL().toString(), Files.exists(refPoliciesDir) ? refPoliciesDir.toUri().toURL()
				.toString() : null, ENABLE_XPATH, Files.exists(attributeProviderConfFile) ? attributeProviderConfFile.toUri().toURL().toString() : null, REQUEST_PREPROC_ID, RESULT_POSTPROC_ID);
		try (final PdpEngineInoutAdapter<JSONObject, JSONObject> pdp = PdpEngineXacmlJsonAdapters.newXacmlJsonInoutAdapter(pdpEngineConf))
		{
			// this is an evaluation test with request/response (not a policy syntax check)
			LOGGER.debug("Request that is sent to the PDP: {}", jsonRequest);
			final JSONObject actualResponse = pdp.evaluate(jsonRequest);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Response that is received from the PDP :  {}", actualResponse);
			}

			assertNormalizedEquals(testDirectoryPath.toString(), expectedResponse, actualResponse);
		}
	}

	public static void main(final String[] args) throws Exception
	{
		/*
		 * Exemple of arg: "classpath:conformance/xacml-3.0-core/mandatory/IIIA004"
		 */
		final URL testDir = ResourceUtils.getURL("target/generated-test-resources/conformance/xacml-3.0-core/mandatory/IIIA004");
		final Path testDirPath = Paths.get(testDir.toURI());
		new JsonProfileConformanceV3Test().test(testDirPath);
	}
}