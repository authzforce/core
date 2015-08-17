/**
 * 
 */
package com.thalesgroup.authzforce.core.eval;

import com.sun.xacml.ctx.Status;

/**
 * Exception wrapper for XACML Indeterminate/error caused by evaluation
 */
public class IndeterminateEvaluationException extends Exception
{
	private final String xacmlStatusCode;

	/**
	 * Creates exception with message and XACML StatusCode (e.g.
	 * {@link com.thalesgroup.authzforce.core.test.ctx.Status#STATUS_PROCESSING_ERROR})
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
	 * {@link com.thalesgroup.authzforce.core.test.ctx.Status#STATUS_PROCESSING_ERROR}), and internal cause for error
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
		return new Status(xacmlStatusCode, this.getMessage());
	}
}
