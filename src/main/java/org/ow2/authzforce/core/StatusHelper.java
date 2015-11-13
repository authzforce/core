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
package org.ow2.authzforce.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

/**
 * Simplifies XACML Status handling.
 */
public class StatusHelper extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Status
{

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

	/**
	 * STATUS OK (as specified by XACML standard)
	 */
	public static final StatusHelper OK = new StatusHelper(STATUS_OK, null);

	/**
	 * Constructor that takes only the status code.
	 * 
	 * @param code
	 *            status code
	 * @param message
	 *            status message
	 */
	public StatusHelper(String code, String message)
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
	public StatusHelper(List<String> codes, String message)
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
	public StatusHelper(List<String> codes, String message, StatusDetail detail) throws IllegalArgumentException
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

	// /**
	// * For testing instantiation of StatusHelper
	// *
	// * @param args
	// */
	// public static void main(String[] args)
	// {
	// final String[] codes = { "XXX", "YYY", "ZZZ" };
	// final List<String> codeList = Arrays.asList(codes);
	// final StatusHelper status = new StatusHelper(codeList, "OK", null);
	// System.out.println(status);
	// }

}
