package com.thalesgroup.authzforce.pdp.core.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;

public class TestUtils {

	/**
	 * the logger we'll use for all messages
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
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
	public static RequestType createRequest(String rootDirectory,
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
			LOGGER.error("Error while reading expected response from file ", e);
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
	public static ResponseType createResponse(String rootDirectory,
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
	private static RequestType marshallRequestType(Node root) {
		JAXBElement<RequestType> allOf = null;
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Unmarshaller u = jc.createUnmarshaller();
			allOf = ((JAXBElement<RequestType>) u.unmarshal(root));
		} catch (Exception e) {
			System.err.println(e);
		}

		return allOf.getValue();
	}
	
	@SuppressWarnings("unchecked")
	private static ResponseType marshallResponseType(Node root) {
		JAXBElement<ResponseType> allOf = null;
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Unmarshaller u = jc.createUnmarshaller();
			allOf = (JAXBElement<ResponseType>) u.unmarshal(root);
		} catch (Exception e) {
			System.err.println(e);
		}

		return allOf.getValue();
	}

	public static String printRequest(RequestType request) {
		StringWriter writer = new StringWriter();
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Marshaller u = jc.createMarshaller();
			u.marshal(request, writer);
		} catch (Exception e) {
			LOGGER.equals(e);
		}

		return writer.toString();
	}

	public static String printResponse(ResponseType response) {
		StringWriter writer = new StringWriter();
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Marshaller u = jc.createMarshaller();
			u.marshal(response, writer);
		} catch (Exception e) {
			LOGGER.equals(e);
		}

		return writer.toString();
	}

	public static boolean match(ResponseCtx response,
			ResponseType expectedResponse) {

		boolean finalResult = false;

		ResponseType xacmlResponse = new ResponseType();
		Iterator<ResultType> myIt = response.getResults().iterator();

		while (myIt.hasNext()) {
			Result result = (Result) myIt.next();
			ResultType resultType = result;
			xacmlResponse.getResult().add(resultType);
		}

		finalResult = matchResult(xacmlResponse.getResult(),
				expectedResponse.getResult());
		if (finalResult) {
			int i = 0;
			for (ResultType result : xacmlResponse.getResult()) {
				finalResult = matchObligations(result.getObligations(),
						expectedResponse.getResult().get(i).getObligations());
			}
		} else {
			// Obligation comparison failed
			LOGGER.error("Result comparaison failed");
			return finalResult;
		}
		if (finalResult) {
			int i = 0;
			for (ResultType result : xacmlResponse.getResult()) {
				finalResult = matchAdvices(result.getAssociatedAdvice(),
						expectedResponse.getResult().get(i)
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

	private static boolean matchResult(List<ResultType> currentResult,
			List<ResultType> expectedResult) {
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
			for (ResultType result : currentResult) {
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

	private static boolean matchObligations(ObligationsType obligationsType,
			ObligationsType obligationsType2) {
		boolean returnData = true;

		if (obligationsType != null && obligationsType2 != null) {
			if (!obligationsType.equals(obligationsType2)) {
				returnData = false;
			}
		}

		return returnData;
	}

	private static boolean matchAdvices(
			AssociatedAdviceType associatedAdviceType,
			AssociatedAdviceType associatedAdviceType2) {
		boolean returnData = true;
		if (associatedAdviceType != null && associatedAdviceType2 != null) {
			if (!associatedAdviceType.equals(associatedAdviceType2)) {
				returnData = false;
			}
		}

		return returnData;
	}

}
