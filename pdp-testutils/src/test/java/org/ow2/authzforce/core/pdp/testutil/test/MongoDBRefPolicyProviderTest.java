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
package org.ow2.authzforce.core.pdp.testutil.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.apache.cxf.helpers.IOUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue.StringParseableValueFactory;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.PdpModelHandler;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.pdp.impl.expression.DepthLimitingExpressionFactory;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.ow2.authzforce.core.pdp.impl.value.StandardAttributeValueFactories;
import org.ow2.authzforce.core.pdp.testutil.PdpTest;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.core.pdp.testutil.ext.MongoDbRefPolicyProvider;
import org.ow2.authzforce.core.pdp.testutil.ext.PolicyPojo;
import org.ow2.authzforce.core.pdp.testutil.ext.xmlns.MongoDBBasedPolicyProvider;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

import com.google.common.base.Charsets;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class MongoDBRefPolicyProviderTest
{
	private static MongoServer DB_SERVER;
	private static MongoCollection POLICY_COLLECTION;
	private static String[] SAMPLE_POLICY_FILENAMES = { "permit-all-policy-0.1.0.xml", "permit-all-policy-0.1.xml", "permit-all-policyset-0.1.0.xml", "root-rbac-policyset-0.1.xml",
			"root-rbac-policyset-1.2.xml", "rbac-pps-employee-1.0.xml" };

	private static CloseableRefPolicyProvider POLICY_PROVIDER_MODULE;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		final PdpModelHandler pdpModelHandler = new PdpModelHandler("classpath:catalog.xml", "classpath:pdp-ext.xsd");
		final Pdp pdpConf;
		try (final InputStream is = MongoDBRefPolicyProviderTest.class.getResourceAsStream(PdpTest.PDP_CONF_FILENAME))
		{
			pdpConf = pdpModelHandler.unmarshal(new StreamSource(is), Pdp.class);
		}

		final AbstractPolicyProvider policyProviderConf = pdpConf.getRefPolicyProvider();
		if (!(policyProviderConf instanceof MongoDBBasedPolicyProvider))
		{
			throw new RuntimeException("Invalid type of refPolicyProvider in pdp.xml. Expected: " + MongoDBBasedPolicyProvider.class);
		}

		final MongoDBBasedPolicyProvider mongodbBasedPolicyProviderConf = (MongoDBBasedPolicyProvider) policyProviderConf;
		final BigInteger maxAllowedIntVal = BigInteger.valueOf(Integer.MAX_VALUE);

		final AttributeValueFactoryRegistry valFactoryReg = StandardAttributeValueFactories.getRegistry(false, Optional.of(maxAllowedIntVal));
		final AttributeValueFactory<?> intValFactory = valFactoryReg.getExtension(StandardDatatypes.INTEGER.getId());
		final FunctionRegistry funcReg = StandardFunction.getRegistry(false, (StringParseableValueFactory<IntegerValue>) intValFactory);
		try (final ExpressionFactory expressionFactory = new DepthLimitingExpressionFactory(valFactoryReg, funcReg, null, 0, false, false))
		{
			POLICY_PROVIDER_MODULE = new MongoDbRefPolicyProvider.Factory().getInstance(mongodbBasedPolicyProviderConf, XacmlJaxbParsingUtils.getXacmlParserFactory(false), 10, expressionFactory,
					StandardCombiningAlgorithm.REGISTRY, null);
		}

		/*
		 * Use in-memory MongoDB server from https://github.com/bwaldvogel/mongo-java-server
		 */
		/*
		 * 
		 * Discarded alternatives for embedded MongoDB server:
		 * 
		 * - fakemongo/fongo: does not implement the wire protocol so not an actual server listening on the network, therefore useless to test the RESTfulRefPolicyProviderModule;
		 * 
		 * - de.flapdoodle.embed.mongo: overkill for unit tests (requires to download and run the actual MongoDB executable).
		 */
		DB_SERVER = new MongoServer(new MemoryBackend());
		final InetSocketAddress socketAddress = new InetSocketAddress(mongodbBasedPolicyProviderConf.getServerHost(), mongodbBasedPolicyProviderConf.getServerPort());
		DB_SERVER.bind(socketAddress);

		final MongoClient dbClient = new MongoClient(new ServerAddress(socketAddress));
		final Jongo testDbWrapper = new Jongo(dbClient.getDB(mongodbBasedPolicyProviderConf.getDbName()));
		POLICY_COLLECTION = testDbWrapper.getCollection(mongodbBasedPolicyProviderConf.getCollectionName());
		// populate database with sample policies
		final Unmarshaller unmarshaller = Xacml3JaxbHelper.createXacml3Unmarshaller();
		for (final String policyFilename : SAMPLE_POLICY_FILENAMES)
		{
			final String policyContent;
			try (final InputStream is = MongoDBRefPolicyProviderTest.class.getResourceAsStream(policyFilename))
			{
				policyContent = IOUtils.toString(is, Charsets.UTF_8.name());
			}
			final Object jaxbObj = unmarshaller.unmarshal(new StringReader(policyContent));
			final String policyTypeId;
			final String policyId;
			final String policyVersion;
			if (jaxbObj instanceof Policy)
			{
				final Policy policy = (Policy) jaxbObj;
				policyTypeId = MongoDbRefPolicyProvider.XACML3_POLICY_TYPE_ID;
				policyId = policy.getPolicyId();
				policyVersion = policy.getVersion();
			}
			else
			{
				// PolicySet
				policyTypeId = MongoDbRefPolicyProvider.XACML3_POLICYSET_TYPE_ID;
				final PolicySet policySet = (PolicySet) jaxbObj;
				policyId = policySet.getPolicySetId();
				policyVersion = policySet.getVersion();
			}

			POLICY_COLLECTION.insert(new PolicyPojo(policyId, policyVersion, policyTypeId, policyContent));
		}

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{

		if (POLICY_PROVIDER_MODULE != null)
		{
			POLICY_PROVIDER_MODULE.close();
		}

		if (DB_SERVER != null)
		{
			DB_SERVER.shutdown();
		}

	}

	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{
	}

	// @Test
	// public void testGetPolicyDirectly() throws IllegalArgumentException, IndeterminateEvaluationException
	// {
	// // Wrong ID, no version pattern
	// // final long docCount = POLICY_COLLECTION.count();
	// final PolicyPOJO policyPOJO = POLICY_COLLECTION.findOne("{type: #, id: 'permit-all', version: {regex: #}}", MongoDBRefPolicyProviderModule.XACML3_POLICY_TYPE_ID, ).as(PolicyPOJO.class);
	// assertNotNull(policyPOJO);
	// }

	@Test
	public void testGetPolicyWithWrongId() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Wrong ID, no version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY, "unexpected-policy-id", Optional.empty(), null, null);
		assertNull(policyEvaluator);
	}

	@Test
	public void testGetPolicyWithValidIdWithoutVersionPattern() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, no version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY, "permit-all", Optional.empty(), null, null);
		assertNotNull(policyEvaluator);
		assertEquals(TopLevelPolicyElementType.POLICY, policyEvaluator.getPolicyElementType());
		assertEquals("permit-all", policyEvaluator.getPolicyId());
	}

	@Test
	public void testGetPolicyWithValidIdButInvalidLiteralVersion() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, invalid literal version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY, "permit-all", Optional.of(new VersionPatterns("1.0", null, null)), null,
				null);
		assertNull(policyEvaluator);
	}

	@Test
	public void testGetPolicyWithValidIdAndLiteralVersion() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, valid literal version pattern (a PolicySet with same version and id also exists, make sure the right policy type is returned)
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY, "permit-all", Optional.of(new VersionPatterns("0.1.0", null, null)), null,
				null);
		assertNotNull(policyEvaluator);
		assertEquals(TopLevelPolicyElementType.POLICY, policyEvaluator.getPolicyElementType());
		assertEquals("permit-all", policyEvaluator.getPolicyId());
		assertEquals("0.1.0", policyEvaluator.getPolicyVersion().toString());
	}

	@Test
	public void testGetPolicyWithValidIdButInvalidVersionPattern() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, invalid version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY, "permit-all", Optional.of(new VersionPatterns("1.+", null, null)), null,
				null);
		assertNull(policyEvaluator);
	}

	@Test
	public void testGetPolicyWithValidIdAndValidVersionPattern() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, valid version pattern (a PolicySet with same version and id also exists, make sure the right policy type is returned)
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY, "permit-all", Optional.of(new VersionPatterns("0.*", null, null)), null,
				null);
		assertNotNull(policyEvaluator);
		assertEquals(TopLevelPolicyElementType.POLICY, policyEvaluator.getPolicyElementType());
		assertEquals("permit-all", policyEvaluator.getPolicyId());
		assertEquals("0.1", policyEvaluator.getPolicyVersion().toString());
	}

	@Test
	public void testGetPolicySetWithWrongId() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Wrong ID, no version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY_SET, "unexpected-policyset-id", Optional.empty(), null, null);
		assertNull(policyEvaluator);
	}

	@Test
	public void testGetPolicySetWithValidIdWithoutVersionPattern() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, no version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY_SET, "root-rbac-policyset", Optional.empty(), null, null);
		assertNotNull(policyEvaluator);
		assertEquals(TopLevelPolicyElementType.POLICY_SET, policyEvaluator.getPolicyElementType());
		assertEquals("root-rbac-policyset", policyEvaluator.getPolicyId());
	}

	@Test
	public void testGetPolicySetWithValidIdButInvalidLiteralVersion() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, invalid literal version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY_SET, "root-rbac-policyset",
				Optional.of(new VersionPatterns("1.0", null, null)), null, null);
		assertNull(policyEvaluator);
	}

	@Test
	public void testGetPolicySetWithValidIdAndLiteralVersion() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, valid literal version pattern (a PolicySet with same version and id also exists, make sure the right policy type is returned)
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY_SET, "permit-all", Optional.of(new VersionPatterns("0.1.0", null, null)),
				null, null);
		assertNotNull(policyEvaluator);
		assertEquals(TopLevelPolicyElementType.POLICY_SET, policyEvaluator.getPolicyElementType());
		assertEquals("permit-all", policyEvaluator.getPolicyId());
		assertEquals("0.1.0", policyEvaluator.getPolicyVersion().toString());
	}

	@Test
	public void testGetPolicySetWithValidIdButInvalidVersionPattern() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, invalid version pattern
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY_SET, "root-rbac-policyset",
				Optional.of(new VersionPatterns("2.+", null, null)), null, null);
		assertNull(policyEvaluator);
	}

	@Test
	public void testGetPolicySetWithValidIdAndValidVersionPattern() throws IllegalArgumentException, IndeterminateEvaluationException
	{
		// Valid ID, valid version pattern (a PolicySet with same version and id also exists, make sure the right policy type is returned)
		final TopLevelPolicyElementEvaluator policyEvaluator = POLICY_PROVIDER_MODULE.get(TopLevelPolicyElementType.POLICY_SET, "root-rbac-policyset",
				Optional.of(new VersionPatterns("1.*", null, null)), null, null);
		assertNotNull(policyEvaluator);
		assertEquals(TopLevelPolicyElementType.POLICY_SET, policyEvaluator.getPolicyElementType());
		assertEquals("root-rbac-policyset", policyEvaluator.getPolicyId());
		assertEquals("1.2", policyEvaluator.getPolicyVersion().toString());
	}

	@Test
	public void testPdpInstantiationWithMongoDBBasedPolicyProvider() throws IllegalArgumentException, IndeterminateEvaluationException, IOException, JAXBException
	{
		final XmlnsFilteringParser xacmlParser = XacmlJaxbParsingUtils.getXacmlParserFactory(false).getInstance();
		final Request req = TestUtils.createRequest("classpath:org/ow2/authzforce/core/pdp/testutil/test/request.xml", xacmlParser);
		final Response expectedResp = TestUtils.createResponse("classpath:org/ow2/authzforce/core/pdp/testutil/test/response.xml", xacmlParser);
		final Response actualResp;
		try (final PdpEngineInoutAdapter<Request, Response> pdpEngine = PdpEngineAdapters.newXacmlJaxbInoutAdapter(PdpEngineConfiguration.getInstance(
				"classpath:org/ow2/authzforce/core/pdp/testutil/test/pdp.xml", "classpath:catalog.xml", "classpath:pdp-ext.xsd")))
		{
			actualResp = pdpEngine.evaluate(req);
		}
		TestUtils.assertNormalizedEquals("", expectedResp, actualResp);
	}

}
