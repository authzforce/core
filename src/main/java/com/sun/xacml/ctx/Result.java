/*
 * @(#)Result.java
 *
 * Copyright 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml.ctx;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCodeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetailType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.Obligation;
import com.sun.xacml.ParsingException;

/**
 * Represents the ResultType XML object from the Context schema. Any number of
 * these may included in a <code>ResponseCtx</code>. This class encodes the
 * decision effect, as well as an optional resource identifier and optional
 * status data. Any number of obligations may also be included.
 * 
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public class Result extends ResultType {

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

	// string versions of the 4 Decision types used for encoding
	public static final String[] DECISIONS = { "Permit", "Deny",
			"Indeterminate", "NotApplicable" };

	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Result.class);

	// the resource identifier or null if there is none
	private String resource = null;

	/**
	 * Constructs a <code>Result</code> object with default status data (OK).
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision) throws IllegalArgumentException {
		this(decision, null, null, null);
	}

	/**
	 * Constructs a <code>Result</code> object with default status data (OK),
	 * and obligations, but no resource identifier.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param obligations
	 *            the obligations the PEP must handle
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, ObligationsType obligations)
			throws IllegalArgumentException {
		this(decision, null, null, obligations);
	}

	/**
	 * Constructs a <code>Result</code> object with status data but without a
	 * resource identifier. Typically the decision is DECISION_INDETERMINATE in
	 * this case, though that's not always true.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, StatusType status) throws IllegalArgumentException {
		this(decision, status, null, null);
	}

	/**
	 * Constructs a <code>Result</code> object with status data and obligations
	 * but without a resource identifier. Typically the decision is
	 * DECISION_INDETERMINATE in this case, though that's not always true.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param obligations
	 *            the obligations the PEP must handle
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, StatusType status, ObligationsType obligations)
			throws IllegalArgumentException {
		this(decision, status, null, obligations);
	}

	/**
	 * Constructs a <code>Result</code> object with a resource identifier, but
	 * default status data (OK). The resource being named must match the
	 * resource (or a descendent of the resource in the case of a hierarchical
	 * resource) from the associated request.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param resource
	 *            a <code>String</code> naming the resource
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, String resource)
			throws IllegalArgumentException {
		this(decision, null, resource, null);
	}
	

	/**
	 * Constructs a <code>Result</code> object with a resource identifier, and
	 * obligations, but default status data (OK). The resource being named must
	 * match the resource (or a descendent of the resource in the case of a
	 * hierarchical resource) from the associated request.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param resource
	 *            a <code>String</code> naming the resource
	 * @param obligations
	 *            the obligations the PEP must handle
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, String resource, ObligationsType obligations)
			throws IllegalArgumentException {
		this(decision, null, resource, obligations);
	}

	/**
	 * Constructs a <code>Result</code> object with status data and a resource
	 * identifier.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param resource
	 *            a <code>String</code> naming the resource
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, StatusType status, String resource)
			throws IllegalArgumentException {
		this(decision, status, resource, null);
	}
	
	/**
	 * Constructs a <code>Result</code> object with status data and a resource
	 * identifier.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param resource
	 *            a <code>String</code> naming the resource
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, StatusType status, String resource, ObligationsType obligations, List<AttributesType> attributes)
			throws IllegalArgumentException {
		this(decision, status, resource, obligations, null, attributes);
	}

	/**
	 * Constructs a <code>Result</code> object with status data, a resource
	 * identifier, and obligations.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param resource
	 *            a <code>String</code> naming the resource
	 * @param obligations
	 *            the obligations the PEP must handle
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, StatusType status, String resource, ObligationsType obligations)
			throws IllegalArgumentException {
		this(decision, status, resource, obligations, null, null);
	}

	/**
	 * Support of Advices added (XACML 3.0) Constructs a <code>Result</code>
	 * object with status data, a resource identifier, obligations and advices.
	 * 
	 * @param decision
	 *            the decision effect to include in this result. This must be
	 *            one of the four fields in this class.
	 * @param status
	 *            the <code>Status</code> to include in this result
	 * @param resource
	 *            a <code>String</code> naming the resource
	 * @param obligations
	 *            the obligations the PEP must handle
	 * @param advices
	 *            A list of advice that provide supplemental information to the
	 *            PEP
	 * 
	 * @throws IllegalArgumentException
	 *             if decision is not valid
	 */
	public Result(DecisionType decision, StatusType status, String resource, ObligationsType obligations, AssociatedAdviceType advices, List<AttributesType> attributes) throws IllegalArgumentException {
		// check that decision is valid
		if ((decision.ordinal() != DECISION_PERMIT) && (decision.ordinal() != DECISION_DENY)
				&& (decision.ordinal() != DECISION_INDETERMINATE)
				&& (decision.ordinal() != DECISION_NOT_APPLICABLE))
			throw new IllegalArgumentException("invalid decision value");

		this.decision = decision;
		this.resource = resource;

		if (status == null) {
			this.status = new StatusType();
			StatusCodeType code = new StatusCodeType();
			StatusDetailType details = new StatusDetailType();
			code.setValue("urn:oasis:names:tc:xacml:1.0:status:ok");
			this.status.setStatusCode(code);
			this.status.setStatusDetail(details);
		} else {
			this.status = status;
		}

		if (obligations == null) {
			this.obligations = new ObligationsType();
		} else {
			this.obligations = obligations;
		}

		if (advices == null) {
			this.associatedAdvice = new AssociatedAdviceType();
		} else {
			this.associatedAdvice = advices;
		}

		if (attributes == null) {
			this.attributes = new ArrayList<AttributesType>();
		} else {
			this.attributes = attributes;
		}
	}

	/**
	 * Creates a new instance of a <code>Result</code> based on the given DOM
	 * root node. A <code>ParsingException</code> is thrown if the DOM root
	 * doesn't represent a valid ResultType.
	 * 
	 * @param root
	 *            the DOM root of a ResultType
	 * 
	 * @return a new <code>Result</code>
	 * 
	 * @throws ParsingException
	 *             if the node is invalid
	 */
	public static Result getInstance(Node root) throws ParsingException {
		DecisionType decision = null;
		StatusType status = null;
		String resource = null;
		ObligationsType obligations = null;
		AssociatedAdviceType advices = null;
		List<AttributesType> attributes = null;

		NamedNodeMap attrs = root.getAttributes();
		Node resourceAttr = attrs.getNamedItem("ResourceId");
		if (resourceAttr != null) {
			resource = resourceAttr.getNodeValue();
		}

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getNodeName();

			if (name.equals("Decision")) {
				String type = node.getFirstChild().getNodeValue();
				for (int j = 0; j < DECISIONS.length; j++) {
					if (DECISIONS[j].equals(type)) {
						//FIXME: check value
						decision.valueOf(type);
						break;
					}
				}

				if (decision.ordinal() == -1) {
					throw new ParsingException("Unknown Decision: " + type);
				}
			} else if (name.equals("Status")) {
				status = Status.getInstance(node);
			} else if (name.equals("Obligations")) {
				obligations = parseObligations(node);
			} else if (name.equals("AssociatedAdvice")) {
				advices = parseAdvices(root);
			} else if (name.equals("Attributes")) {
				attributes = parseAttributes(node);
			}
		}

		return new Result(decision, status, resource, obligations, advices, attributes);
	}

	/**
	 * Helper method that handles the obligations
	 */
	private static ObligationsType parseObligations(Node root) throws ParsingException {
		ObligationsType obligations = new ObligationsType();

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("Obligation"))
				obligations.getObligation().add(Obligation.getInstance(node));
		}

		if (obligations.getObligation().size() == 0)
			throw new ParsingException("ObligationsType must not be empty");

		return obligations;
	}

	/**
	 * Helper method that handles the Advices
	 */
	private static AssociatedAdviceType parseAdvices(Node root)
			throws ParsingException {
		AssociatedAdviceType advices = new AssociatedAdviceType();

		NodeList nodes = root.getChildNodes();
		AdviceType advice = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("Advice")) {
				advice = new AdviceType();
				JAXBContext jc;
				try {
					jc = JAXBContext.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
					Unmarshaller u = jc.createUnmarshaller();
					advices.getAdvice().add((AdviceType)u.unmarshal(root));
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return advices;
	}
	
	/**
	 * Helper method that handles the Attributes
	 */
	private static List<AttributesType> parseAttributes(Node root)
			throws ParsingException {
		List<AttributesType> attributes = new ArrayList<AttributesType>();

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("Attribute")) {
				JAXBContext jc;
				try {
					jc = JAXBContext.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
					Unmarshaller u = jc.createUnmarshaller();
					attributes.add((AttributesType)u.unmarshal(root));
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (attributes.size() == 0) {
			throw new ParsingException("Advice must not be empty");
		}

		return attributes;
	}

	/**
	 * Returns the decision associated with this <code>Result</code>. This will
	 * be one of the four <code>DECISION_*</code> fields in this class.
	 * 
	 * @return the decision effect
	 */
	public DecisionType getDecision() {
		return decision;
	}

	/**
	 * Returns the status data included in this <code>Result</code>. Typically
	 * this will be <code>STATUS_OK</code> except when the decision is
	 * <code>INDETERMINATE</code>.
	 * 
	 * @return status associated with this Result
	 */
	public StatusType getStatus() {
		return status;
	}

	/**
	 * Returns the resource to which this Result applies, or null if none is
	 * specified.
	 * 
	 * @return a resource identifier or null
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Sets the resource identifier if it has not already been set before. The
	 * core code does not set the resource identifier, so this is useful if you
	 * want to write wrapper code that needs this information.
	 * 
	 * @param resource
	 *            the resource identifier
	 * 
	 * @return true if the resource identifier was set, false if it already had
	 *         a value
	 */
	public boolean setResource(String resource) {
		if (this.resource != null)
			return false;

		this.resource = resource;

		return true;
	}

	/**
	 * Returns the set of obligations that the PEP must fulfill, which may be
	 * empty.
	 * 
	 * @return the set of obligations
	 */
	public ObligationsType getObligations() {
		return obligations;
	}

	/**
	 * Adds an obligation to the set of obligations that the PEP must fulfill
	 * 
	 * @param obligation
	 *            the <code>Obligation</code> to add
	 */
	public void addObligation(Obligation obligation) {
		if (obligation != null) {
			obligations.getObligation().add(obligation);
		}
	}
	
	public void addObligation(ObligationExpressionType obligation, EvaluationCtx context) {
		if(obligation != null) {
			try {
				obligations.getObligation().add(Obligation.getInstance(obligation, context));
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void addAdvice(AdviceType advice) {
		if (advice != null) {
			associatedAdvice.getAdvice().add(advice);
		}
	}

	/**
	 * @return the advices
	 */
	public AssociatedAdviceType getAdvices() {
		return associatedAdvice;
	}

	/**
	 * @param advices
	 *            the advices to set
	 */
	public void setAdvices(AssociatedAdviceType advices) {
		this.associatedAdvice = advices;
	}

	/**
	 * @return the attributes
	 */
	public List<AttributesType> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<AttributesType> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Encodes this <code>Result</code> into its XML form and writes this out to
	 * the provided <code>OutputStream<code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Result</code> into its XML form and writes this out to
	 * the provided <code>OutputStream<code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Marshaller u = jc.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e) {
			LOGGER.error(e);
		}  
	}
}
