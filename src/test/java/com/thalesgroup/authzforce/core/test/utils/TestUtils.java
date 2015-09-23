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
package com.thalesgroup.authzforce.core.test.utils;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.PDP;
import com.thalesgroup.authzforce.core.DefaultRequestFilter;
import com.thalesgroup.authzforce.core.IndividualDecisionRequest;
import com.thalesgroup.authzforce.core.PdpConfigurationParser;
import com.thalesgroup.authzforce.core.RequestFilter;
import com.thalesgroup.authzforce.core.XACMLBindingUtils;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinder;
import com.thalesgroup.authzforce.core.attr.CloseableAttributeFinderImpl;
import com.thalesgroup.authzforce.core.attr.StandardDatatypeFactoryRegistry;
import com.thalesgroup.authzforce.core.eval.EvaluationContext;
import com.thalesgroup.authzforce.core.eval.ExpressionFactory;
import com.thalesgroup.authzforce.core.eval.ExpressionFactoryImpl;
import com.thalesgroup.authzforce.core.eval.IndeterminateEvaluationException;
import com.thalesgroup.authzforce.core.eval.IndividualDecisionRequestContext;
import com.thalesgroup.authzforce.core.func.StandardFunctionRegistry;
import com.thalesgroup.authzforce.pdp.model._2015._06.BaseStaticPolicyFinder;
import com.thalesgroup.authzforce.pdp.model._2015._06.Pdp;

public class TestUtils
{
	/**
	 * XACML standard Expression factory/parser
	 */
	public static final ExpressionFactory STD_EXPRESSION_FACTORY;
	static
	{
		final CloseableAttributeFinder ctxOnlyAttrFinder = new CloseableAttributeFinderImpl(null);
		STD_EXPRESSION_FACTORY = new ExpressionFactoryImpl(StandardDatatypeFactoryRegistry.INSTANCE, StandardFunctionRegistry.INSTANCE, ctxOnlyAttrFinder, 0, false, null);
	}

	/**
	 * Default (basic) request filter, supporting only XACML core mandatory features of Individual
	 * Decision requests (no support for AttributeSelectors)
	 */
	private static final RequestFilter BASIC_REQUEST_FILTER = new DefaultRequestFilter(StandardDatatypeFactoryRegistry.INSTANCE, false, null, null);

	public static final String POLICY_DIRECTORY = "policies";
	public static final String REQUEST_DIRECTORY = "requests";
	public static final String RESPONSE_DIRECTORY = "responses";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

	/**
	 * This creates the XACML request from file on classpath: {@code rootDirectory}/
	 * {@code versionDirectory}/{@value #REQUEST_DIRECTORY}/{@code requestFilename}
	 * 
	 * @param rootDirectory
	 *            root directory of the request files
	 * @param versionDirectory
	 *            version directory of the request files
	 * @param requestFilename
	 *            request file name
	 * @return String or null if any error
	 * @throws JAXBException
	 *             error reading XACML 3.0 Request from the file
	 */
	public static Request createRequest(String rootDirectory, String versionDirectory, String requestFilename) throws JAXBException
	{
		/**
		 * Get absolute path/URL to request file in a portable way, using current class loader. As
		 * per javadoc, the name of the resource passed to ClassLoader.getResource() is a
		 * '/'-separated path name that identifies the resource. So let's build it. Note: do not use
		 * File.separator as path separator, as it will be turned into backslash "\\" on Windows,
		 * and will be URL-encoded (%5c) by the getResource() method (not considered path separator
		 * by this method), and file will not be found as a result.
		 */
		String requestFileResourceName = rootDirectory + "/" + versionDirectory + "/" + REQUEST_DIRECTORY + "/" + requestFilename;
		URL requestFileURL = Thread.currentThread().getContextClassLoader().getResource(requestFileResourceName);
		if (requestFileURL == null)
		{
			throw new IllegalArgumentException("No XACML Request file found at location: 'classpath:" + requestFileResourceName + "'");
		}

		LOGGER.debug("Request file to read: {}", requestFileURL);
		Unmarshaller u = XACMLBindingUtils.createXacml3Unmarshaller();
		Request request = (Request) u.unmarshal(requestFileURL);
		return request;
	}

	/**
	 * This creates the XACML request from file on classpath:
	 * <p>
	 * {@code rootDirectory}/{@code versionDirectory}/{@value #RESPONSE_DIRECTORY}/
	 * {@code responseFilename}
	 * </p>
	 * 
	 * 
	 * @param rootDirectory
	 *            root directory of the request files
	 * @param versionDirectory
	 *            version directory of the request files
	 * @param responseFilename
	 *            request file name
	 * @return String or null if any error
	 * @throws JAXBException
	 *             error reading XACML 3.0 Request from the file
	 */
	public static Response createResponse(String rootDirectory, String versionDirectory, String responseFilename) throws JAXBException
	{
		/**
		 * Get absolute path/URL to response file in a portable way, using current class loader. As
		 * per javadoc, the name of the resource passed to ClassLoader.getResource() is a
		 * '/'-separated path name that identifies the resource. So let's build it. Note: do not use
		 * File.separator as path separator, as it will be turned into backslash "\\" on Windows,
		 * and will be URL-encoded (%5c) by the getResource() method (not considered path separator
		 * by this method), and file will not be found as a result.
		 */
		String responseFileResourceName = rootDirectory + "/" + versionDirectory + "/" + RESPONSE_DIRECTORY + "/" + responseFilename;
		URL responseFileURL = Thread.currentThread().getContextClassLoader().getResource(responseFileResourceName);
		LOGGER.debug("Response file to read: {}", responseFileURL);
		Unmarshaller u = XACMLBindingUtils.createXacml3Unmarshaller();
		Response response = (Response) u.unmarshal(responseFileURL);
		return response;
	}

	public static String printRequest(Request request)
	{
		StringWriter writer = new StringWriter();
		try
		{
			Marshaller u = XACMLBindingUtils.createXacml3Marshaller();
			u.marshal(request, writer);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Request", e);
		}

		return writer.toString();
	}

	public static String printResponse(Response response)
	{
		StringWriter writer = new StringWriter();
		try
		{
			Marshaller marshaller = XACMLBindingUtils.createXacml3Marshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(response, writer);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Response", e);
		}

		return writer.toString();
	}

	/**
	 * Normalize a XACML response for comparison with another normalized one. In particular, it
	 * removes every Result's status as we choose to ignore the Status. Indeed, a PDP implementation
	 * might return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail
	 * that we would not expect.
	 * 
	 * @param response
	 *            input XACML Response
	 */
	private static void normalizeForComparison(Response response)
	{
		/*
		 * We iterate over all results, because for each results, we don't compare everything. In
		 * particular, we choose to ignore the Status. Indeed, a PDP implementation might return a
		 * perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would
		 * not expect.
		 */
		for (Result result : response.getResults())
		{
			// We ignore the status, so set it to null in both expected and tested response to avoid
			// Status comparison
			result.setStatus(null);
		}
	}

	// Replaced by Response#equals()
	// TODO: remove it once Response#equals() checks OK.
	// public static boolean match(Response normalizedActualResponse, Response
	// normalizedExpectedResponse)
	// {
	// if (normalizedActualResponse.getResults().size() !=
	// normalizedExpectedResponse.getResults().size())
	// {
	// LOGGER.debug("Number of results in tested response  (={}) differs from expected (={})",
	// normalizedActualResponse.getResults().size(),
	// normalizedExpectedResponse.getResults().size());
	// return false;
	// }
	//
	// /*
	// * We iterate over all results, because for each results, we don't compare everything. In
	// * particular, we choose to ignore the Status. Indeed, a PDP implementation might return a
	// * perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would
	// * not expect.
	// */
	// Iterator<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> expectedResultsIterator =
	// normalizedExpectedResponse.getResults().iterator();
	// Iterator<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> testedResultsIterator =
	// normalizedActualResponse.getResults().iterator();
	// int i = 0;
	// while (expectedResultsIterator.hasNext())
	// {
	// Result expectedResult = expectedResultsIterator.next();
	// Result testedResult = testedResultsIterator.next();
	// // We ignore the status, so set it to null in both expected and tested response to avoid
	// // Status comparison
	// expectedResult.setStatus(null);
	// testedResult.setStatus(null);
	// if (!testedResult.equals(expectedResult))
	// {
	// LOGGER.debug("Result #" + i +
	// " in tested response ( {} ) does not match (Status ignored) the expected one ( {} )",
	// testedResult, expectedResult);
	// return false;
	// }
	//
	// i++;
	// }
	//
	// return true;
	// }

	/**
	 * Returns a new PDP instance with a new root XACML policy loaded from {@code rootDir}/
	 * {@code versionDir}/{@value #POLICY_DIRECTORY}/{@code policyFilename} and supporting only
	 * mandatory XACML core features (standard attribute datatypes and functions...)
	 * 
	 * @param rootDir
	 *            test root directory name
	 * @param versionDir
	 *            XACML version directory name
	 * 
	 * @param policyFilename
	 *            PDP's root policy filename
	 * @return a PDP instance
	 */
	public static PDP getPDPNewInstance(String rootDir, String versionDir, String policyFilename)
	{
		return getPDPNewInstance(rootDir + "/" + versionDir + "/" + POLICY_DIRECTORY + "/", policyFilename);
	}

	/**
	 * Creates PDP from policies and global configuration located at classpath:{@code pathPrefix} +
	 * {@code policyfilename}
	 * 
	 * @param pathPrefix
	 *            prefix to append before policy filename to have the actual policy file path in the
	 *            classpath
	 * @param policyfilename
	 *            XACML policy filename relative to pathPrefix. If pathPrefix is null, filename is
	 *            considered at the root of the classpath
	 * @return PDP instance
	 */
	public static PDP getPDPNewInstance(String pathPrefix, String policyfilename)
	{
		/**
		 * Get absolute path/URL to policy file in a portable way, using current class loader. As
		 * per javadoc, the name of the resource passed to ClassLoader.getResource() is a
		 * '/'-separated path name that identifies the resource. So let's build it. Note: do not use
		 * File.separator as path separator, as it will be turned into backslash "\\" on Windows,
		 * and will be URL-encoded (%5c) by the getResource() method (not considered path separator
		 * by this method), and file will not be found as a result.
		 */
		String policyFileResourceName = pathPrefix + policyfilename;
		URL policyFileURL = Thread.currentThread().getContextClassLoader().getResource(policyFileResourceName);
		BaseStaticPolicyFinder jaxbRootPolicyFinder = new BaseStaticPolicyFinder();
		jaxbRootPolicyFinder.setId("root");
		jaxbRootPolicyFinder.setPolicyLocation(policyFileURL.toString());

		Pdp jaxbPDP = new Pdp();
		jaxbPDP.setRootPolicyFinder(jaxbRootPolicyFinder);
		final PDP pdp;
		try
		{
			pdp = PdpConfigurationParser.getPDP(jaxbPDP);
		} catch (IllegalArgumentException e)
		{
			throw new RuntimeException("Error parsing policy from location: " + policyFileURL, e);
		}

		return pdp;
	}

	public static EvaluationContext createContext(Request request) throws IndeterminateEvaluationException
	{
		/*
		 * The request filter used here does not support, therefore filters out AttributeSelectors,
		 * so make sure there's no AttributeSelector in the Target/Match elements of the PolicySet.
		 */
		IndividualDecisionRequest individualDecisionReq = BASIC_REQUEST_FILTER.filter(request).get(0);
		return new IndividualDecisionRequestContext(individualDecisionReq);
	}

	/**
	 * assertEquals() for XACML responses (handles normalization of the responses)
	 * 
	 * @param testId
	 *            test identifier
	 * @param expectedResponse
	 *            expected response
	 * @param actualResponse
	 *            actual response
	 */
	public static void assertNormalizedEquals(String testId, Response expectedResponse, Response actualResponse)
	{
		// normalize responses for comparison
		TestUtils.normalizeForComparison(expectedResponse);
		TestUtils.normalizeForComparison(actualResponse);
		assertEquals("Test '" + testId + "' (Status elements removed/ignored for comparison): ", expectedResponse, actualResponse);
	}
}
