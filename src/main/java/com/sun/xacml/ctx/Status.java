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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

/**
 * Represents the status data that is included in a ResultType. By default, the status is OK.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Status extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Status
{

	/**
	 * Standard TYPE_URI for the OK status
	 */
	public static final String STATUS_OK = "urn:oasis:names:tc:xacml:1.0:status:ok";

	/**
	 * Standard TYPE_URI for the MissingAttribute status
	 */
	public static final String STATUS_MISSING_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:status:missing-attribute";

	/**
	 * Standard TYPE_URI for the SyntaxError status
	 */
	public static final String STATUS_SYNTAX_ERROR = "urn:oasis:names:tc:xacml:1.0:status:syntax-error";

	/**
	 * Standard TYPE_URI for the ProcessingError status
	 */
	public static final String STATUS_PROCESSING_ERROR = "urn:oasis:names:tc:xacml:1.0:status:processing-error";

	/**
	 * STATUS OK (as specified by XACML standard)
	 */
	public static final Status OK = new Status(STATUS_OK, null);

	/**
	 * Constructor that takes only the status code.
	 * 
	 * @param code
	 *            status code
	 * @param message
	 *            status message
	 */
	public Status(String code, String message)
	{
		this(Collections.singletonList(code), message, null);
	}

	/**
	 * Constructor that takes both the status code and a message to include with the status.
	 * 
	 * @param codes
	 *            a <code>List</code> of <code>String</code> codes, typically just one code, but
	 *            this may contain any number of minor codes after the first item in the list, which
	 *            is the major code
	 * @param message
	 *            a message to include with the code
	 */
	public Status(List<String> codes, String message)
	{
		this(codes, message, null);
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
			final String c = codes.iterator().next();
			if (c.equals(STATUS_OK) || c.equals(STATUS_SYNTAX_ERROR) || c.equals(STATUS_PROCESSING_ERROR))
			{
				throw new IllegalArgumentException("status detail cannot be included with " + c);
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
		final String[] codes = { "XXX", "YYY", "ZZZ" };
		final List<String> codeList = Arrays.asList(codes);
		final Status status = new Status(codeList, "OK", null);
		System.out.println(status);
	}

}
