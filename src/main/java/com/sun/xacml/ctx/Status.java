/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.ctx;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PdpModelHandler;

/**
 * Represents the status data that is included in a ResultType. By default, the status is OK.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Status extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Status
{
	/*
	 * TODO: make enum constants out of these
	 */

	/**
	 * Standard identifier for the OK status
	 */
	public static final String STATUS_OK = "urn:oasis:names:tc:xacml:1.0:status:ok";

	/**
	 * Standard identifier for the MissingAttribute status
	 */
	public static final String STATUS_MISSING_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:status:missing-attribute";

	/**
	 * Standard identifier for the SyntaxError status
	 */
	public static final String STATUS_SYNTAX_ERROR = "urn:oasis:names:tc:xacml:1.0:status:syntax-error";

	/**
	 * Standard identifier for the ProcessingError status
	 */
	public static final String STATUS_PROCESSING_ERROR = "urn:oasis:names:tc:xacml:1.0:status:processing-error";

	private static final Logger LOGGER = LoggerFactory.getLogger(Status.class);

	// a single OK object we'll use most of the time
	private static final Status okStatus;

	// initialize the OK Status object
	static
	{
		List<String> code = new ArrayList<>();
		code.add(STATUS_OK);
		okStatus = new Status(code);
	}

	/**
	 * Constructor that takes only the status code.
	 * 
	 * @param code
	 *            a <code>List</code> of <code>String</code> codes, typically just one code, but
	 *            this may contain any number of minor codes after the first item in the list, which
	 *            is the major code
	 */
	public Status(List<String> code)
	{
		this(code, null, null);
	}

	/**
	 * Constructor that takes both the status code and a message to include with the status.
	 * 
	 * @param code
	 *            a <code>List</code> of <code>String</code> codes, typically just one code, but
	 *            this may contain any number of minor codes after the first item in the list, which
	 *            is the major code
	 * @param message
	 *            a message to include with the code
	 */
	public Status(List<String> code, String message)
	{
		this(code, message, null);
	}

	/**
	 * Max depth of status code. StatusCode in XACML schema is a recursive structure like an error
	 * stacktrace that allows chaining status codes endlessly unless the implementation enforces a
	 * maximum depth as done here.
	 */
	public static final int MAX_STATUS_CODE_DEPTH = 10;

	/**
	 * Constructor that takes the status code, an optional message, and some detail to include with
	 * the status. Note that the specification explicitly says that a status code of OK, SyntaxError
	 * or ProcessingError may not appear with status detail, so an exception is thrown if one of
	 * these status codes is used and detail is included.
	 * 
	 * @param codes
	 *            a <code>List</code> of <code>String</code> codes, typically just one code, but
	 *            this may contain any number of minor codes after the first item in the list, which
	 *            is the major code
	 * @param message
	 *            a message to include with the code, or null if there should be no message
	 * @param detail
	 *            the status detail to include, or null if there is no detail
	 * 
	 * @throws IllegalArgumentException
	 *             if detail is included for a status code that doesn't allow detail
	 */
	public Status(List<String> codes, String message, StatusDetail detail) throws IllegalArgumentException
	{
		if (codes == null)
		{
			throw new IllegalArgumentException("status code value undefined");
		}

		// if the code is ok, syntax error or processing error, there
		// must not be any detail included
		if (detail != null)
		{
			String c = codes.iterator().next();
			if (c.equals(STATUS_OK) || c.equals(STATUS_SYNTAX_ERROR) || c.equals(STATUS_PROCESSING_ERROR))
			{
				throw new IllegalArgumentException("status detail cannot be " + "included with " + c);
			}
		}

		final StatusCode statusCodeFromStrings = stringsToStatusCode(codes.iterator(), MAX_STATUS_CODE_DEPTH);
		if (statusCodeFromStrings == null)
		{
			throw new IllegalArgumentException("Invalid status code values: " + codes);
		}

		this.statusCode = statusCodeFromStrings;
		this.statusMessage = message;
		this.statusDetail = detail;
	}

	/**
	 * Buils the chain of status codes (recursive) (similar to error stacktrace)
	 * 
	 * @param codesIterator
	 * @param depth
	 * @return
	 */
	private static StatusCode stringsToStatusCode(Iterator<String> codesIterator, int depth)
	{
		if (!codesIterator.hasNext())
		{
			return null;
		}

		final String codeVal = codesIterator.next();
		if (codeVal == null)
		{
			throw new IllegalArgumentException("Null status code found");
		}

		final StatusCode statusCode = new StatusCode();
		statusCode.setValue(codeVal);
		if (depth == 0)
		{
			// stop the recursivity here
			return statusCode;
		}

		final StatusCode nextStatusCode = stringsToStatusCode(codesIterator, depth - 1);
		if (nextStatusCode != null)
		{
			statusCode.setStatusCode(nextStatusCode);
		}

		return statusCode;
	}

	/**
	 * For testing instantiation of Status
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		final String[] codes = {"XXX", "YYY", "ZZZ"};
		final List<String> codeList = Arrays.asList(codes);
		final Status status = new Status(codeList, "OK", null);
		System.out.println(status);
	}

	/**
	 * Gets a <code>Status</code> instance that has the OK status and no other information. This is
	 * the default status data for all responses except Indeterminate ones.
	 * 
	 * @return an instance with <code>STATUS_OK</code>
	 */
	public static Status getOkInstance()
	{
		return okStatus;
	}

	/**
	 * Creates a new instance of <code>Status</code> based on the given DOM root node. A
	 * <code>ParsingException</code> is thrown if the DOM root doesn't represent a valid StatusType.
	 * 
	 * @param root
	 *            the DOM root of a StatusType
	 * 
	 * @return a new <code>Status</code>
	 * 
	 * @throws ParsingException
	 *             if the node is invalid
	 */
	public static Status getInstance(Node root) throws ParsingException
	{
		List<String> code = null;
		String message = null;
		StatusDetail detail = null;

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			String name = node.getNodeName();

			if (name.equals("StatusCode"))
			{
				code = parseStatusCode(node);
			} else if (name.equals("StatusMessage"))
			{
				message = node.getFirstChild().getNodeValue();
			} else if (name.equals("StatusDetail"))
			{
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					detail = new StatusDetail();
					detail.getAnies().add((Element) node);
				} else
				{
					throw new UnsupportedOperationException("StatusDetail node type is not ELEMENT as expected");
				}
			}
		}

		return new Status(code, message, detail);
	}

	/**
	 * Private helper that parses the status code
	 */
	private static List<String> parseStatusCode(Node root)
	{
		// get the top-level code
		String val = root.getAttributes().getNamedItem("Value").getNodeValue();
		List<String> code = new ArrayList<>();
		code.add(val);

		// now get the list of all sub-codes, and work through them
		NodeList list = ((Element) root).getElementsByTagName("StatusCode");
		for (int i = 0; i < list.getLength(); i++)
		{
			Node node = list.item(i);
			code.add(node.getAttributes().getNamedItem("Value").getNodeValue());
		}

		return code;
	}

	/**
	 * Encodes this status data into its XML representation and writes this encoding to the given
	 * <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this status data into its XML representation and writes this encoding to the given
	 * <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter)
	{
		PrintStream out = new PrintStream(output);
		try
		{
			Marshaller marshaller = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Response", e);
		}
	}

	@Override
	public String toString()
	{
		OutputStream output = new ByteArrayOutputStream();
		encode(output, new Indenter(0));
		return output.toString();
	}

}
