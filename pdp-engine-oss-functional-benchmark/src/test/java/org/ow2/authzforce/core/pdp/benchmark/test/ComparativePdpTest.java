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
package org.ow2.authzforce.core.pdp.benchmark.test;

import com.att.research.xacml.api.pdp.PDPEngine;
import com.att.research.xacml.api.pdp.PDPEngineFactory;
import com.att.research.xacml.api.pdp.PDPException;
import com.att.research.xacml.std.dom.DOMRequest;
import com.att.research.xacml.std.dom.DOMResponse;
import com.att.research.xacml.std.dom.DOMStructureException;
import com.att.research.xacml.util.FactoryException;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.io.BaseXacmlJaxbResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.io.IndividualXacmlJaxbRequest;
import org.ow2.authzforce.core.pdp.api.io.PdpEngineInoutAdapter;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.StandardAttributeValueFactories;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.impl.io.PdpEngineAdapters;
import org.ow2.authzforce.core.pdp.impl.io.SingleDecisionXacmlJaxbRequestPreprocessor;
import org.ow2.authzforce.core.pdp.testutil.TestUtils;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.wso2.balana.ConfigurationStore;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.UnknownIdentifierException;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

/**
 * Comparative testing of XACML PDP Engines: AuthzForce, AT&T XACML, WSO2 Balana.
 *
 */
@RunWith(value = Parameterized.class)
public class ComparativePdpTest
{
	/**
	 * Name of root directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_ROOT_DIRECTORY_LOCATION = "target/test-classes/ComparativePdpTest";

	/**
	 * XACML request filename
	 */
	public final static String REQUEST_FILENAME = "request.xml";

	/**
	 * XACML policy filename
	 */
	public final static String POLICY_FILENAME = "policy.xml";

	/**
	 * Expected XACML response filename
	 */
	public final static String EXPECTED_RESPONSE_FILENAME = "response.xml";

	/**
	 * AuthzForce PDP configuration directory, relative to TEST_RESOURCES_ROOT_DIRECTORY_LOCATION
	 */
	public static final String AUTHZFORCE_CE_PDP_CONF_DIRNAME = "configs/authzforce-ce";

	/**
	 * Name of system property to be set to the current test case's directory, and used in AuthzForce PDP config
	 */
	public static final String TEST_CASE_DIR_SYS_PROP_NAME = "org.ow2.authzforce.test.case.dir";

	/**
	 * ATT-XACML PDP configuration directory, relative to TEST_RESOURCES_ROOT_DIRECTORY_LOCATION
	 */
	public static final String ATT_XACML_PDP_CONF_DIRNAME = "configs/att-xacml";

	/**
	 * ATT-XACML PDP config filename
	 */
	public static final String ATT_XACML_PDP_CONF_FILENAME = "xacml.properties";

	/**
	 * WSO2 Balana PDP configuration directory, relative to TEST_RESOURCES_ROOT_DIRECTORY_LOCATION
	 */
	public static final String WSO2_BALANA_PDP_CONF_DIRNAME = "configs/wso2-balana";

	/**
	 * WSO2 Balana PDP configuration filename
	 */
	public static final String WSO2_BALANA_PDP_CONF_FILENAME = "balana.xml";

	private static final Logger LOGGER = LoggerFactory.getLogger(ComparativePdpTest.class);
	private static final XmlnsFilteringParserFactory XACML_PARSER_FACTORY = XacmlJaxbParsingUtils.getXacmlParserFactory(false);

	private interface PdpEngineInvoker
	{
		/**
		 * Policy evaluation
		 * 
		 * @param testCaseDirPath
		 *            case directory where policy, request and expected response file for the given test case are located input XACML request
		 * @return output XACML response
		 * @throws IOException I/O error trying to call the PDP engine
		 * @throws JAXBException error unmarshalling output XACML response with JAXB API
		 */
		Response eval(Path testCaseDirPath) throws IOException, JAXBException;

	}

	private static final class AuthzForcePdpEngineInvoker implements PdpEngineInvoker
	{
		private static final AttributeValueFactoryRegistry STD_ATTRIBUTE_VALUE_FACTORIES = StandardAttributeValueFactories.getRegistry(false, Optional.empty());

		private static final DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest> DEFAULT_XACML_JAXB_REQ_PREPROC = SingleDecisionXacmlJaxbRequestPreprocessor.LaxVariantFactory.INSTANCE
				.getInstance(STD_ATTRIBUTE_VALUE_FACTORIES, false, false, Set.of());
		private static final DecisionResultPostprocessor<IndividualXacmlJaxbRequest, Response> DEFAULT_XACML_JAXB_RESULT_POSTPROC = new BaseXacmlJaxbResultPostprocessor(0);
		private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();
		// private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
		static
		{
			XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
			XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
			XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
			XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
			XML_INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

			/*
			 * TODO: set Woodstox-specific output properties
			 */
		}

		private static final String PDP_CONF_FILENAME = "pdp.xml";
		private static final String XML_CATALOG_FILENAME = "catalog.xml";
		private static final String PDP_EXT_XSD_FILENAME = "pdp-ext.xsd";

		private final String catalogLocation;
		private final String extXsdLocation;
		private final File pdpConfFile;

		private AuthzForcePdpEngineInvoker(final Path pdpConfigurationDirectoryPath) throws IllegalArgumentException
		{
			this.catalogLocation = pdpConfigurationDirectoryPath.resolve(XML_CATALOG_FILENAME).toString();
			this.extXsdLocation = pdpConfigurationDirectoryPath.resolve(PDP_EXT_XSD_FILENAME).toString();
			this.pdpConfFile = pdpConfigurationDirectoryPath.resolve(PDP_CONF_FILENAME).toFile();
		}

		@Override
		public Response eval(final Path testCaseDirPath) throws IOException, JAXBException
		{
			System.setProperty(TEST_CASE_DIR_SYS_PROP_NAME, testCaseDirPath.toAbsolutePath().toString());
			final PdpEngineConfiguration pdpConf = PdpEngineConfiguration.getInstance(pdpConfFile, catalogLocation, extXsdLocation);

			final Path requestFilePath = testCaseDirPath.resolve(REQUEST_FILENAME);
			// try
			// {
			/*
			 * FIXME: reuse the same Unmarshaller per thread (JAXB RI's Unmarshaller is not thread safe officially).
			 */
			final Unmarshaller unmarshaller = Xacml3JaxbHelper.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			/*
			 * WARNING: No XACML schema validation for fair comparison with other PdpEngines, because there is none in other PdpEngines, although it is easy to do with AuthzForce
			 */
			final Request xacmlRequest;
			try (final FileInputStream fis = new FileInputStream(requestFilePath.toFile()); final BufferedInputStream is = new BufferedInputStream(fis))
			{

				// xacmlRequest = (Request) unmarshaller.unmarshal(is);
				final XMLStreamReader xmlReader = XML_INPUT_FACTORY.createXMLStreamReader(is);
				xacmlRequest = (Request) unmarshaller.unmarshal(xmlReader);
			}
			catch (final XMLStreamException e)
			{
				throw new IllegalArgumentException("AuthzForce PDP engine - Bad input XML", e);
			}

			try (final PdpEngineInoutAdapter<Request, Response> xacmlJaxbIoPdp = PdpEngineAdapters.newInoutAdapter(Request.class, Response.class, new BasePdpEngine(pdpConf),
			        DEFAULT_XACML_JAXB_REQ_PREPROC, DEFAULT_XACML_JAXB_RESULT_POSTPROC))
			{
				return xacmlJaxbIoPdp.evaluate(xacmlRequest);
			}
		}
	}

	private static final class AttXacmlPdpEngineInvoker implements PdpEngineInvoker
	{
		private final PDPEngineFactory pdpEngineFactory;
		private final File pdpConfFile;

		private AttXacmlPdpEngineInvoker(final Path pdpConfigurationDirectoryPath) throws IllegalArgumentException
		{
			try
			{
				pdpEngineFactory = PDPEngineFactory.newInstance();
			}
			catch (final FactoryException e)
			{
				throw new IllegalArgumentException("ATT XACML engine - Init error", e);
			}

			this.pdpConfFile = pdpConfigurationDirectoryPath.resolve(ATT_XACML_PDP_CONF_FILENAME).toFile();
		}

		@Override
		public Response eval(final Path testCaseDirPath) throws IOException, JAXBException
		{
			final Path policyPath = testCaseDirPath.resolve(POLICY_FILENAME);
			final Properties xacmlProps = new Properties();
			try (final InputStream in = new FileInputStream(pdpConfFile))
			{
				xacmlProps.load(in);
			}

			xacmlProps.setProperty("root.file", policyPath.toAbsolutePath().toString());
			final PDPEngine pdpEngine;

			try
			{
				pdpEngine = pdpEngineFactory.newEngine(xacmlProps);
			}
			catch (final FactoryException e)
			{
				throw new IllegalArgumentException("ATT XACML engine - Invalid PDP engine configuration", e);
			}

			final Path requestFilePath = testCaseDirPath.resolve(REQUEST_FILENAME);
			final ByteArrayOutputStream os;

			// final Unmarshaller unmarshaller = ATT_JAXB_CTX.createUnmarshaller();
			// final JAXBElement<RequestType> jaxbElementRequest;
			final com.att.research.xacml.api.Request attXacmlRequest;
			// final com.att.research.xacml.api.Request attXacmlRequest = JaxpRequest.newInstance(jaxbElementRequest.getValue());
			try (final FileInputStream fis = new FileInputStream(requestFilePath.toFile()); final BufferedInputStream is = new BufferedInputStream(fis))
			{
				// jaxbElementRequest = unmarshaller.unmarshal(new StreamSource(is), RequestType.class);
				// alternative
				attXacmlRequest = DOMRequest.load(is);
			}
			catch (final DOMStructureException e)
			{
				throw new IllegalArgumentException("ATT XACML engine - Bad Request", e);
			}

			final com.att.research.xacml.api.Response attXacmlResponse;
			try
			{
				attXacmlResponse = pdpEngine.decide(attXacmlRequest);
			}
			catch (final PDPException e)
			{
				throw new RuntimeException("ATT XACML engine - PDP call error", e);
			}

			os = new ByteArrayOutputStream();
			try
			{
				DOMResponse.convert(attXacmlResponse, os);
			}
			catch (final DOMStructureException e)
			{
				throw new RuntimeException("ATT XACML engine - Bad Response", e);
			}

			final Unmarshaller schemaValidatingUnmarshaller = Xacml3JaxbHelper.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			return (Response) schemaValidatingUnmarshaller.unmarshal(new ByteArrayInputStream(os.toByteArray()));

		}
	}

	private static final class Wso2BalanaPdpEngineInvoker implements PdpEngineInvoker
	{
		private static final RequestCtxFactory REQUEST_FACTORY = RequestCtxFactory.getFactory();
		private final File pdpConfFile;

		private Wso2BalanaPdpEngineInvoker(final Path pdpConfigurationDirectoryPath) throws IllegalArgumentException
		{
			pdpConfFile = pdpConfigurationDirectoryPath.resolve(WSO2_BALANA_PDP_CONF_FILENAME).toFile();
		}

		@Override
		public Response eval(final Path testCaseDirPath) throws IOException, JAXBException
		{
			System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, testCaseDirPath.toAbsolutePath().toString());
			final PDPConfig pdpConfig;
			try
			{
				final ConfigurationStore configStore = new ConfigurationStore(pdpConfFile);
				pdpConfig = configStore.getDefaultPDPConfig();
			}
			catch (final UnknownIdentifierException | org.wso2.balana.ParsingException e)
			{
				throw new IllegalArgumentException("WSO2 Balana engine - Invalid PDP configuration", e);
			}

			final PDP pdp = new PDP(pdpConfig);

			final Path requestFilePath = testCaseDirPath.resolve(REQUEST_FILENAME);
			final AbstractRequestCtx balanaRequest;
			try (final FileInputStream fis = new FileInputStream(requestFilePath.toFile()); final BufferedInputStream is = new BufferedInputStream(fis))
			{
				balanaRequest = REQUEST_FACTORY.getRequestCtx(is);
			}
			catch (final org.wso2.balana.ParsingException e)
			{
				throw new IllegalArgumentException("WSO2 Balana engine - Bad Request", e);
			}

			final ResponseCtx balanaResponse = pdp.evaluate(balanaRequest);

			final Unmarshaller schemaValidatingUnmarshaller = Xacml3JaxbHelper.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
			return (Response) schemaValidatingUnmarshaller.unmarshal(new StringReader(balanaResponse.encode()));
		}

	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> params() throws URISyntaxException, IOException
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		/*
		 * Each sub-directory of the root directory is data for a specific test. So we configure a test for each directory
		 */
		final URL testRootDir = ResourceUtils.getURL(TEST_RESOURCES_ROOT_DIRECTORY_LOCATION);
		final Path testRootPath = Paths.get(testRootDir.toURI());

		final PdpEngineInvoker authzforcePdpEngineInvoker = new AuthzForcePdpEngineInvoker(testRootPath.resolve(AUTHZFORCE_CE_PDP_CONF_DIRNAME));
		final PdpEngineInvoker attPdpEngineInvoker = new AttXacmlPdpEngineInvoker(testRootPath.resolve(ATT_XACML_PDP_CONF_DIRNAME));
		final PdpEngineInvoker wso2PdpEngineInvoker = new Wso2BalanaPdpEngineInvoker(testRootPath.resolve(WSO2_BALANA_PDP_CONF_DIRNAME));
		final PdpEngineInvoker[] pdpEngineInvokers = { authzforcePdpEngineInvoker, attPdpEngineInvoker, wso2PdpEngineInvoker };

		final Path testCasesRootPath = testRootPath.resolve("test-cases");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testCasesRootPath))
		{
			for (final Path path : stream)
			{
				if (Files.isDirectory(path))
				{
					/*
					 * Specific test's resources directory location, used as parameter to ComparativePdpTest(String)
					 */
					for (final PdpEngineInvoker pdpEngineInvoker : pdpEngineInvokers)
					{
						testParams.add(new Object[] { pdpEngineInvoker, path });
					}
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

	private final PdpEngineInvoker pdpEngineInvoker;
	private final Path testDirPath;

	/**
	 * Constructor
	 * 
	 * @param testDirPath
	 *            subdirectory of {@value #TEST_RESOURCES_ROOT_DIRECTORY_LOCATION} where data for a specific test case are located
	 */
	public ComparativePdpTest(final PdpEngineInvoker pdpEngineInvoker, final Path testDirPath)
	{
		this.pdpEngineInvoker = pdpEngineInvoker;
		this.testDirPath = testDirPath;
	}

	@Test
	public void policyEval() throws IOException, JAXBException
	{
		final Response actualResponse;
		try
		{
			actualResponse = pdpEngineInvoker.eval(this.testDirPath);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("XACML Response received from the PDP: {}", TestUtils.printResponse(actualResponse));
			}
		}
		catch (final JAXBException e)
		{
			e.printStackTrace();
			Assert.fail("PDP returned invalid response: " + e.getMessage());
			return;
		}

		final XmlnsFilteringParser unmarshaller = XACML_PARSER_FACTORY.getInstance();
		final Response expectedResponse = TestUtils.createResponse(this.testDirPath.resolve(EXPECTED_RESPONSE_FILENAME), unmarshaller);
		TestUtils.assertNormalizedEquals(testDirPath.toString(), expectedResponse, actualResponse, true);
	}

}
