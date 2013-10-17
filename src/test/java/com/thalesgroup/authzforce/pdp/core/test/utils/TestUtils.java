package com.thalesgroup.authzforce.pdp.core.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.xacml.BindingUtility;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;

public class TestUtils {

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestUtils.class);

	/**
	 * This creates the XACML request from a file
	 * 
	 * @param rootDirectory
	 *            root directory of the request files
	 * @param versionDirectory
	 *            version directory of the request files
	 * @param requestId
	 *            request file name
	 * @return String or null if any error
	 */
	public static Request createRequest(String rootDirectory,
			String versionDirectory, String requestId) {

		File file = new File(".");
		try {
			String filePath = file.getCanonicalPath() + File.separator
					+ TestConstants.RESOURCE_PATH.value() + File.separator
					+ rootDirectory + File.separator + versionDirectory
					+ File.separator + TestConstants.REQUEST_DIRECTORY.value()
					+ File.separator + requestId;

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setIgnoringComments(true);
			factory.setNamespaceAware(true);
			DocumentBuilder db = factory.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(filePath));
			return marshallRequestType(doc);
		} catch (Exception e) {
			LOGGER.error("Error while reading expected request from file ", e);
			// ignore any exception and return null
		}
		return null;
	}

	/**
	 * This creates the expected XACML response from a file
	 * 
	 * @param rootDirectory
	 *            root directory of the response files
	 * @param versionDirectory
	 *            version directory of the response files
	 * @param responseId
	 *            response file name
	 * @return ResponseCtx or null if any error
	 */
	public static Response createResponse(String rootDirectory,
			String versionDirectory, String responseId) {

		File file = new File(".");
		try {
			String filePath = file.getCanonicalPath() + File.separator
					+ TestConstants.RESOURCE_PATH.value() + File.separator
					+ rootDirectory + File.separator + versionDirectory
					+ File.separator + TestConstants.RESPONSE_DIRECTORY.value()
					+ File.separator + responseId;
			LOGGER.debug("File to read: " + filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setIgnoringComments(true);
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			DocumentBuilder db = factory.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(filePath));
			return marshallResponseType(doc);
		} catch (Exception e) {
			LOGGER.error("Error while reading expected response from file ", e);
			// ignore any exception and return null
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static Request marshallRequestType(Node root) {
		Request request = null;
		try {
			Unmarshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createUnmarshaller();
			JAXBElement<Request> jaxbElt = (JAXBElement<Request>)u.unmarshal(root);
			request = jaxbElt.getValue();
		} catch (Exception e) {
			LOGGER.error("Error unmarshalling Request", e);
		}

		return request;
	}
	
	@SuppressWarnings("unchecked")
	private static Response marshallResponseType(Node root) {
		Response allOf = null;
		try {
			Unmarshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createUnmarshaller();
			allOf = (Response) u.unmarshal(root);
		} catch (Exception e) {
			LOGGER.error("Error unmarshalling Response", e);
		}

		return allOf;
	}

	public static String printRequest(Request request) {
		StringWriter writer = new StringWriter();
		try {
			Marshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createMarshaller();
			u.marshal(request, writer);
		} catch (Exception e) {
			LOGGER.error("Error marshalling Request", e);
		}

		return writer.toString();
	}

	public static String printResponse(Response response) {
		StringWriter writer = new StringWriter();
		try {
			Marshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createMarshaller();
			u.marshal(response, writer);
		} catch (Exception e) {
			LOGGER.error("Error marshalling Response", e);
		}

		return writer.toString();
	}

	public static boolean match(ResponseCtx response,
			Response expectedResponse) {

		boolean finalResult = false;

		Response xacmlResponse = new Response();
		Iterator<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> myIt = response.getResults().iterator();

		while (myIt.hasNext()) {
			Result result = (Result) myIt.next();
			oasis.names.tc.xacml._3_0.core.schema.wd_17.Result resultType = result;
			xacmlResponse.getResults().add(resultType);
		}

		finalResult = matchResult(xacmlResponse.getResults(),
				expectedResponse.getResults());
		if (finalResult) {
			int i = 0;
			for (oasis.names.tc.xacml._3_0.core.schema.wd_17.Result result : xacmlResponse.getResults()) {
				finalResult = matchObligations(result.getObligations(),
						expectedResponse.getResults().get(i).getObligations());
			}
		} else {
			// Obligation comparison failed
			LOGGER.error("Result comparaison failed");
			return finalResult;
		}
		if (finalResult) {
			int i = 0;
			for (oasis.names.tc.xacml._3_0.core.schema.wd_17.Result result : xacmlResponse.getResults()) {
				finalResult = matchAdvices(result.getAssociatedAdvice(),
						expectedResponse.getResults().get(i)
								.getAssociatedAdvice());
			}
		} else {
			// Advice comparison failed
			LOGGER.error("Obligations comparaison failed");
			return finalResult;
		}
		
		if(!finalResult){
			// Advice comparison failed
			LOGGER.error("Advice comparaison failed");
			return finalResult;
		}
		
		// Everything gone right
		return finalResult;
	}

	private static boolean matchResult(List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> currentResult,
			List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Result> expectedResult) {
		boolean resultCompare = false;
		// Compare the number of results
		LOGGER.debug("Begining result number comparison");
		if (currentResult.size() != expectedResult.size()) {
			LOGGER.error("Number of result differ from expected");
			LOGGER.error("Current: " + currentResult.size());
			LOGGER.error("Expected: " + expectedResult.size());
			resultCompare = false;
		} else {
			resultCompare = true;
		}
		if (resultCompare) {
			LOGGER.debug("Result number comparaison OK");
			int i = 0;
			LOGGER.debug("Begining result decision comparison");
			for (oasis.names.tc.xacml._3_0.core.schema.wd_17.Result result : currentResult) {
				// Compare the decision
				resultCompare = result.getDecision().equals(
						expectedResult.get(i).getDecision());
				if (!resultCompare) {
					LOGGER.error("Result " + i + " differ from expected.");
					LOGGER.error("Current: " + result.getDecision().value());
					LOGGER.error("Expected: "
							+ expectedResult.get(i).getDecision().value());
				}
			}
			LOGGER.debug("Result decision comparaison OK");
		}

		return resultCompare;
	}

	private static boolean matchObligations(Obligations obligationsType,
			Obligations obligationsType2) {
		boolean returnData = true;

		if (obligationsType != null && obligationsType2 != null) {
			if (!obligationsType.equals(obligationsType2)) {
				returnData = false;
			}
		}

		return returnData;
	}

	private static boolean matchAdvices(
			AssociatedAdvice associatedAdvice,
			AssociatedAdvice associatedAdvice2) {
		boolean returnData = true;
		if (associatedAdvice != null && associatedAdvice2 != null) {
			if (!associatedAdvice.equals(associatedAdvice2)) {
				returnData = false;
			}
		}

		return returnData;
	}

}
