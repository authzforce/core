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
/**
 * 
 */
package com.thalesgroup.authzforce.core;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Exception wrapper for XACML Indeterminate/error caused by evaluation
 * <p>
 * TODO: although we consider Exceptions as a good solution (from a Java standpoint) to propagate
 * error information with a full traceable stacktrace, from a functional/logical point of view, we
 * could improve performance by using return codes instead, whenever possible, especially where we
 * don't lose any useful error info by doing so: cf.
 * http://java-performance.info/throwing-an-exception-in-java-is-very-slow/
 * </p>
 */
public class IndeterminateEvaluationException extends Exception
{
	private final String xacmlStatusCode;

	/**
	 * Creates exception with message and XACML StatusCode (e.g.
	 * {@link StatusHelper#STATUS_PROCESSING_ERROR})
	 * 
	 * @param message
	 *            exception message
	 * @param statusCode
	 *            XACML StatusCode value
	 */
	public IndeterminateEvaluationException(String message, String statusCode)
	{
		super(message);
		this.xacmlStatusCode = statusCode;
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g.
	 * {@link StatusHelper#STATUS_PROCESSING_ERROR}), and internal cause for error
	 * 
	 * @param message
	 *            exception message
	 * @param statusCode
	 *            XACML StatusCode value
	 * @param cause
	 *            internal cause of error
	 */
	public IndeterminateEvaluationException(String message, String statusCode, Throwable cause)
	{
		super(message, cause);
		this.xacmlStatusCode = statusCode;
	}

	/**
	 * Get XACML status code for this "Indeterminate"
	 * 
	 * @return StatusCode value
	 */
	public String getStatusCode()
	{
		return xacmlStatusCode;
	}

	/**
	 * Get Status
	 * 
	 * @return status
	 */
	public Status getStatus()
	{
		return new StatusHelper(xacmlStatusCode, this.getMessage());
	}
}
