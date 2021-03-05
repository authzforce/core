/*
 * Copyright 2012-2021 THALES.
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
package org.ow2.authzforce.core.pdp.testutil;

import com.google.common.base.Preconditions;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.impl.DefaultEnvironmentProperties;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.core.pdp.testutil.ext.TestAttributeProvider;
import org.ow2.authzforce.core.pdp.testutil.ext.xmlns.TestAttributeProviderDescriptor;
import org.ow2.authzforce.core.xmlns.pdp.InOutProcChain;
import org.ow2.authzforce.core.xmlns.pdp.Pdp;
import org.ow2.authzforce.core.xmlns.pdp.StaticPolicyProvider;
import org.ow2.authzforce.core.xmlns.pdp.TopLevelPolicyElementRef;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestUtils
{

	private static final class MarshallableWithToString
	{
		private final Object jaxbAnnotatedObject;
		private final Marshaller marshaller;

		private MarshallableWithToString(final Object jaxbAnnotatedObject, final Marshaller marshaller)
		{
			assert jaxbAnnotatedObject != null && marshaller != null;
			this.jaxbAnnotatedObject = jaxbAnnotatedObject;
			this.marshaller = marshaller;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return this.jaxbAnnotatedObject.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof MarshallableWithToString))
			{
				return false;
			}

			return this.jaxbAnnotatedObject.equals(((MarshallableWithToString) obj).jaxbAnnotatedObject);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			final StringWriter strWriter = new StringWriter();
			try
			{
				this.marshaller.marshal(this.jaxbAnnotatedObject, strWriter);
			}
			catch (final JAXBException e)
			{
				throw new RuntimeException(e);
			}
			return strWriter.toString();
		}
	}

	/**
	 * JAXB context for (un)marshalling TestAttributeProvider configuration
	 */
	public static final JAXBContext TEST_ATTRIBUTE_PROVIDER_JAXB_CONTEXT;
	static
	{
		try
		{
			TEST_ATTRIBUTE_PROVIDER_JAXB_CONTEXT = JAXBContext.newInstance(TestAttributeProviderDescriptor.class);
		}
		catch (final JAXBException e)
		{
			throw new RuntimeException("Error instantiating JAXB context for unmarshalling TestAttributeProvider configurations", e);
		}
	}

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

	/**
	 * This creates the XACML request from file on classpath
	 * 
	 * @param requestFile
	 *            file path (with Spring-supported URL prefixes: 'classpath:', etc.) path to the request file, relative to classpath
	 * @param unmarshaller
	 *            XACML unmarshaller
	 * @return the XML/JAXB Request or null if any error
	 * @throws JAXBException
	 *             error reading XACML 3.0 Request from the file at {@code requestFileLocation}
	 * @throws MalformedURLException requestFile could not be converted to a URL
	 * @throws IllegalArgumentException requestFile is invalid
	 */
	public static Request createRequest(final Path requestFile, final XmlnsFilteringParser unmarshaller) throws JAXBException, IllegalArgumentException, MalformedURLException
	{

		LOGGER.debug("Request file to read: {}", requestFile);
		return (Request) unmarshaller.parse(requestFile.toUri().toURL());
	}

	/**
	 * This creates the XACML response from file on classpath
	 * 
	 * @param responseFile
	 *            path to the response file
	 * @param unmarshaller
	 *            XACML unmarshaller
	 * @return the XML/JAXB Response or null if any error
	 * @throws JAXBException
	 *             error reading XACML 3.0 Request from the file at {@code responseFileLocation}
	 * @throws MalformedURLException requestFile could not be converted to a URL
	 * @throws IllegalArgumentException invalid requestFile
	 */
	public static Response createResponse(final Path responseFile, final XmlnsFilteringParser unmarshaller) throws JAXBException, IllegalArgumentException, MalformedURLException
	{
		LOGGER.debug("Response file to read: {}", responseFile);
		return (Response) unmarshaller.parse(responseFile.toUri().toURL());
	}

	public static String printResponse(final Response response)
	{
		final StringWriter writer = new StringWriter();
		try
		{
			final Marshaller marshaller = Xacml3JaxbHelper.createXacml3Marshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(response, writer);
		}
		catch (final Exception e)
		{
			LOGGER.error("Error marshalling Response", e);
		}

		return writer.toString();
	}

	/**
	 * Normalize a XACML response for comparison with another normalized one. In particular, it removes every Result's status as we choose to ignore the Status. Indeed, a PDP implementation might
	 * return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
	 * 
	 * @param response
	 *            input XACML Response
	 * @return normalized response
	 */
	public static Response normalizeForComparison(final Response response)
	{
		final List<Result> results = new ArrayList<>();
		/*
		 * We iterate over all results, because for each results, we don't compare everything. In particular, we choose to ignore the Status. Indeed, a PDP implementation might return a perfectly
		 * XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
		 */
		for (final Result result : response.getResults())
		{
			// We ignore the status, so set it to null in both expected and tested response to avoid
			// StatusMessage/StatusDetail/nested StatusCode comparison
			// conserve root status code if any
			final Status oldStatus = result.getStatus();
			final Status newStatus;
			if (oldStatus == null)
			{
				newStatus = null;
			}
			else
			{
				final StatusCode oldStatusCode = oldStatus.getStatusCode();
				// status OK is useless, equivalent to no status (no error)
				if (oldStatusCode.getValue().equals(XacmlStatusCode.OK.value()))
				{
					newStatus = null;
				}
				else if (oldStatus.getStatusMessage() == null && oldStatus.getStatusDetail() == null && oldStatusCode.getStatusCode() == null)
				{
					// if there is only a StatusCode (no nested one), keep it as is
					newStatus = oldStatus;
				}
				else
				{
					// keep only the statusCode
					newStatus = new Status(oldStatusCode, null, null);
				}
			}

			results.add(new Result(result.getDecision(), newStatus, result.getObligations(), result.getAssociatedAdvice(), normalizeAttributeCategories(result.getAttributes()),
			        result.getPolicyIdentifierList()));
		}

		return new Response(results);
	}

	private static final Comparator<Attributes> ATTRIBUTES_COMPARATOR = (arg0, arg1) -> {
		if (arg0 == null || arg1 == null)
		{
			throw new IllegalArgumentException("Invalid Attribtues args for comparator");
		}

		return arg0.getCategory().compareTo(arg1.getCategory());
	};

	private static List<Attributes> normalizeAttributeCategories(final List<Attributes> attributesList)
	{
		// Attributes categories may be in different order than expected although it is still compliant (order does not matter to the spec)
		// always use the same order (lexicographical here)
		final SortedSet<Attributes> sortedSet = new TreeSet<>(ATTRIBUTES_COMPARATOR);
		sortedSet.addAll(attributesList);
		return new ArrayList<>(sortedSet);
	}

	/**
	 * Gets policy ref from XACML policy file
	 * 
	 * @param path
	 *            path to XACML Policy(Set) file
	 * @return Policy(Set)Id
	 * @throws JAXBException
	 *             unmarshalling error
	 */
	public static TopLevelPolicyElementRef getPolicyRef(final Path path) throws JAXBException
	{
		/*
		 * Unmarshall without schema validation because some test policy files are intendedly invalid to test XACML syntax validation
		 */
		final Object policyOrPolicySet = Xacml3JaxbHelper.XACML_3_0_JAXB_CONTEXT.createUnmarshaller().unmarshal(path.toFile());
		final boolean isPolicySet;
		final String policyId;
		if (policyOrPolicySet instanceof PolicySet)
		{
			isPolicySet = true;
			policyId = ((PolicySet) policyOrPolicySet).getPolicySetId();
		}
		else
		{
			isPolicySet = false;
			policyId = ((Policy) policyOrPolicySet).getPolicyId();
		}

		return new TopLevelPolicyElementRef(policyId, null, isPolicySet);
	}

	private static PdpEngineConfiguration newPdpEngineConfiguration(final TopLevelPolicyElementRef rootPolicyRef, final List<String> policyLocations, final boolean enableXPath,
	        final Optional<Path> attributeProviderConfFile, final String requestPreprocId, final String resultPostprocId) throws JAXBException, IllegalArgumentException, IOException
	{
		Preconditions.checkNotNull(rootPolicyRef, "Root policy reference (ID, version) undefined");
		Preconditions.checkNotNull(policyLocations, "Policy location(s) undefined");

		final Pdp jaxbPDP = new Pdp();
		jaxbPDP.setEnableXPath(enableXPath);

		final StaticPolicyProvider jaxbPolicyProvider = new StaticPolicyProvider();
		jaxbPolicyProvider.setId("policyProvider");
		jaxbPolicyProvider.getPolicySetsAndPolicyLocations().addAll(policyLocations);
		jaxbPDP.getPolicyProviders().add(jaxbPolicyProvider);

		// set max PolicySet reference depth to max possible depth automatically
		jaxbPDP.setMaxPolicyRefDepth(BigInteger.valueOf(jaxbPolicyProvider.getPolicySetsAndPolicyLocations().size()));
		jaxbPDP.setRootPolicyRef(rootPolicyRef);

		// test attribute provider
		if (attributeProviderConfFile.isPresent())
		{
			final Unmarshaller unmarshaller = TEST_ATTRIBUTE_PROVIDER_JAXB_CONTEXT.createUnmarshaller();
			@SuppressWarnings("unchecked")
			final JAXBElement<TestAttributeProviderDescriptor> testAttributeProviderElt = (JAXBElement<TestAttributeProviderDescriptor>) unmarshaller
			        .unmarshal(attributeProviderConfFile.get().toFile());
			jaxbPDP.getAttributeProviders().add(testAttributeProviderElt.getValue());
		}

		if (requestPreprocId != null)
		{
			final InOutProcChain ioProcChain = new InOutProcChain(requestPreprocId, resultPostprocId);
			jaxbPDP.getIoProcChains().add(ioProcChain);
		}

		return new PdpEngineConfiguration(jaxbPDP, new DefaultEnvironmentProperties());

	}

	/**
	 * Creates PDP engine configuration
	 * 
	 * @param policiesDirectory
	 *            directory containing files of XACML Policy(Set)s, including the root policy and other policies referred to from the root policy {@code rootPolicyId} via Policy(Set)IdReference.
	 * @param rootPolicyRef
	 *            ID (and optional version) of root XACML policy, to be located in <code>policiesDirectory</code>
	 * @param enableXPath
	 *            Enable support for AttributeSelectors and xpathExpression datatype. Reminder: AttributeSelector and xpathExpression datatype support are marked as optional in XACML 3.0 core
	 *            specification, so set this to false if you are testing mandatory features only.
	 * @param attributeProviderConfFile
	 *            (optional) {@link TestAttributeProvider} XML configuration location
	 * @param requestPreprocId
	 *            Request preprocessor ID
	 * @param resultPostprocId
	 *            Result postprocessor ID
	 * @return PDP instance
	 * @throws IllegalArgumentException
	 *             invalid XACML policy located at {@code rootPolicyLocation} or {@code refPoliciesDirectoryLocation}
	 * @throws IOException
	 *             if error closing some resources used by the PDP after {@link IllegalArgumentException} occurred
	 * @throws JAXBException
	 *             cannot create Attribute Provider configuration (XML) unmarshaller
	 */
	public static PdpEngineConfiguration newPdpEngineConfiguration(final TopLevelPolicyElementRef rootPolicyRef, final Path policiesDirectory, final boolean enableXPath,
	        final Optional<Path> attributeProviderConfFile, final String requestPreprocId, final String resultPostprocId) throws IllegalArgumentException, IOException, JAXBException
	{
		final List<String> policyLocations = new ArrayList<>();

		/*
		 * Root policy expected to be in the policies directory as well
		 */
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(policiesDirectory))
		{
			for (final Path path : stream)
			{
				if (Files.isRegularFile(path))
				{
					policyLocations.add(path.toString());
				}
			}
		}
		catch (final DirectoryIteratorException ex)
		{
			// I/O error encounted during the iteration, the cause is an IOException
			throw ex.getCause();
		}

		return newPdpEngineConfiguration(rootPolicyRef, policyLocations, enableXPath, attributeProviderConfFile, requestPreprocId, resultPostprocId);
	}

	/**
	 * Creates PDP engine configuration
	 * 
	 * @param rootPolicyFile
	 *            ID of root XACML policy, to be located in <code>policiesDirectoryLocation</code> (with Spring-supported URL prefixes: 'classpath:', etc.)
	 * @param enableXPath
	 *            Enable support for AttributeSelectors and xpathExpression datatype. Reminder: AttributeSelector and xpathExpression datatype support are marked as optional in XACML 3.0 core
	 *            specification, so set this to false if you are testing mandatory features only.
	 * @param attributeProviderConfFile
	 *            (optional) {@link TestAttributeProvider} XML configuration location
	 * @param requestPreprocId
	 *            Request preprocessor ID
	 * @param resultPostprocId
	 *            Result postprocessor ID
	 * @return PDP instance
	 * @throws IllegalArgumentException
	 *             invalid XACML policy located at {@code rootPolicyLocation} or {@code refPoliciesDirectoryLocation}
	 * @throws IOException
	 *             if error closing some resources used by the PDP after {@link IllegalArgumentException} occurred
	 * @throws JAXBException
	 *             cannot create Attribute Provider configuration (XML) unmarshaller
	 */
	public static PdpEngineConfiguration newPdpEngineConfiguration(final Path rootPolicyFile, final boolean enableXPath, final Optional<Path> attributeProviderConfFile, final String requestPreprocId,
	        final String resultPostprocId) throws IllegalArgumentException, IOException, JAXBException
	{
		final TopLevelPolicyElementRef rootPolicyRef = TestUtils.getPolicyRef(rootPolicyFile);
		return newPdpEngineConfiguration(rootPolicyRef, Collections.singletonList(rootPolicyFile.toString()), enableXPath, attributeProviderConfFile, requestPreprocId, resultPostprocId);
	}

	/**
	 * assertEquals() for XACML responses (handles normalization of the responses)
	 * 
	 * @param testId
	 *            test identifier
	 * @param expectedResponse
	 *            expected response
	 * @param actualResponseFromPDP
	 *            actual response
	 * @throws JAXBException
	 *             error creating JAXB Marshaller for XACML output
	 */
	public static void assertNormalizedEquals(final String testId, final Response expectedResponse, final Response actualResponseFromPDP) throws JAXBException
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
		final Response normalizedExpectedResponse = TestUtils.normalizeForComparison(expectedResponse);
		final Response normalizedActualResponse = TestUtils.normalizeForComparison(actualResponseFromPDP);
		final Marshaller marshaller = Xacml3JaxbHelper.createXacml3Marshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		assertEquals("Test '" + testId + "' (Status elements removed/ignored for comparison): ", new MarshallableWithToString(normalizedExpectedResponse, marshaller),
		        new MarshallableWithToString(normalizedActualResponse, marshaller));
	}
}
