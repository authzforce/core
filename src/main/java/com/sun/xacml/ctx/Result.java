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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.Obligation;
import com.sun.xacml.ParsingException;
import com.thalesgroup.authzforce.core.PdpModelHandler;

/**
 * Represents the oasis.names.tc.xacml._3_0.core.schema.wd_17.Result XML object from the Context
 * schema. Any number of these may included in a <code>ResponseCtx</code>. This class encodes the
 * decision effect, as well as an optional resource identifier and optional status data. Any number
 * of obligations may also be included.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public class Result extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Result
{

	/**
	 * The decision to permit the request
	 */
	public static final int DECISION_PERMIT = 0;

	/**
	 * The decision to deny the request
	 */
	public static final int DECISION_DENY = 1;

	/**
	 * The decision that a decision about the request cannot be made
	 */
	public static final int DECISION_INDETERMINATE = 2;

	/**
	 * The decision that nothing applied to us
	 */
	public static final int DECISION_NOT_APPLICABLE = 3;

	/**
	 * String versions of the 4 Decision types used for encoding
	 */
	public static final String[] DECISIONS = { "Permit", "Deny", "Indeterminate", "NotApplicable" };

	private static final Logger LOGGER = LoggerFactory.getLogger(Result.class);

	/**
	 * Constructs a <code>Result</code> object with default status data (OK).
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be one of the four fields
	 *            in this class.
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision) throws IllegalArgumentException
	{
		this(decision, null, null, null);
	}

	/**
	 * Constructs a <code>Result</code> object with default status data (OK), and obligations, but
	 * no resource identifier.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be one of the four fields
	 *            in this class.
	 * @param obligations
	 *            the obligations the PEP must handle
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, Obligations obligations) throws IllegalArgumentException
	{
		this(decision, null, obligations, null, null);
	}

	/**
	 * Constructs a <code>Result</code> object with status data but without a resource identifier.
	 * Typically the decision is DECISION_INDETERMINATE in this case, though that's not always true.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be one of the four fields
	 *            in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status) throws IllegalArgumentException
	{
		this(decision, status, null, null);
	}

	/**
	 * Constructs a <code>Result</code> object with status data and obligations but without a
	 * resource identifier. Typically the decision is DECISION_INDETERMINATE in this case, though
	 * that's not always true.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be one of the four fields
	 *            in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param obligations
	 *            the obligations the PEP must handle
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status, Obligations obligations)
			throws IllegalArgumentException
	{
		this(decision, status, obligations, null, null);
	}

	/**
	 * Constructs a <code>Result</code> object with status data and a resource identifier.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be one of the four fields
	 *            in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param obligations list of obligations to be fulfilled by the PEP
	 * @param attributes A list of attributes that were part of the request. The choice of which attributes are included here is made with the IncludeInResult attribute of the <Attribute> elements of the request. See XACML 3.0 spec section 5.46.
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status, Obligations obligations,
			List<Attributes> attributes) throws IllegalArgumentException
	{
		this(decision, status, obligations, null, attributes);
	}

	/**
	 * Support of Advices added (XACML 3.0) Constructs a <code>Result</code> object with status
	 * data, a resource identifier, obligations and advices.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be one of the four fields
	 *            in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param obligations
	 *            the obligations the PEP must handle
	 * @param advices
	 *            A list of advice that provide supplemental information to the PEP
	 * @param attributes list of attributes that were part of the request with IncludeInResult=true
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, oasis.names.tc.xacml._3_0.core.schema.wd_17.Status status, Obligations obligations,
			AssociatedAdvice advices, List<Attributes> attributes) throws IllegalArgumentException
	{
		// check that decision is valid
		if ((decision != DecisionType.PERMIT) && (decision != DecisionType.DENY) && (decision != DecisionType.INDETERMINATE)
				&& (decision != DecisionType.NOT_APPLICABLE))
			throw new IllegalArgumentException("invalid decision value");

		this.decision = decision;

		if (status == null)
		{
			this.status = new oasis.names.tc.xacml._3_0.core.schema.wd_17.Status();
			StatusCode code = new StatusCode();
			oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail details = new oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail();
			code.setValue("urn:oasis:names:tc:xacml:1.0:status:ok");
			this.status.setStatusCode(code);
			this.status.setStatusDetail(details);
		} else
		{
			this.status = status;
		}
		/*
		 * obligations must be null if no obligations. If you create new Obligations() in this case,
		 * the result Obligations will be marshalled to empty <Obligations /> element which is NOT
		 * VALID against the XACML schema.
		 */
		this.obligations = obligations;

		/*
		 * associatedAdvice must be null if advices is null. If you create new AssociatedAdvice() in
		 * this case, the result element will be marshalled to empty <AssociatedAdvice /> element
		 * which is NOT VALID against the XACML schema.
		 */
		this.associatedAdvice = advices;

		if (attributes == null)
		{
			this.attributes = new ArrayList<>();
		} else
		{
			this.attributes = attributes;
		}
	}

	/**
	 * Add obligation to the result. Here the obligation is the result of the evaluation of the ObligationExpression in the current evaluation context
	 * 
	 * @param obligation
	 * @param context
	 */
	public void addObligation(ObligationExpression obligation, EvaluationCtx context)
	{
		if (obligation != null)
		{
			if(obligations == null) {
				obligations = new Obligations();
			}
			
			try
			{
				obligations.getObligations().add(Obligation.getInstance(obligation, context));
			} catch (ParsingException e)
			{
				LOGGER.error("Error instantiating ObligationExpression", e);
			}
		}
	}

	/**
	 * Encodes this <code>Result</code> into its XML form and writes this out to the provided
	 * <code>OutputStream<code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Result</code> into its XML form and writes this out to the provided
	 * <code>OutputStream<code> with indentation.
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
			Marshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Result", e);
		}
	}
	
	@Override
	public String toString() {
		final StringWriter out = new StringWriter();
		try
		{
			final Marshaller marshaller = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createMarshaller();
			// Property to remove XML prolog
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Result to String", e);
		}

		return out.toString();
	}
}
