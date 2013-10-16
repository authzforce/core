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
package com.sun.xacml;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.cond.xacmlv3.Expression;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Result;
import com.thalesgroup.authzforce.xacml.schema.XACMLDatatypes;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * Represents the ObligationType XML type in XACML. This also stores all the
 * AttriubteAssignmentType XML types.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Obligation extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation {

	// the obligation id
//	private URI id;

	// effect to fulfill on, as defined in Result
	private int fulfillOn;

	// the attribute assignments
//	private List<AttributeAssignmentType> assignments;

	private boolean isIndeterminate = false;
	
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Obligation.class);

	/**
	 * Constructor that takes all the data associated with an obligation. The
	 * attribute assignment list contains <code>Attribute</code> objects, but
	 * only the fields used by the AttributeAssignmentType are used.
	 * 
	 * @param id
	 *            the obligation's id
	 * @param fulfillOn
	 *            the effect denoting when to fulfill this obligation
	 * @param assignments
	 *            a <code>List</code> of <code>Attribute</code>s
	 */
	public Obligation(String id, int fulfillOn, List assignments) {
		this(id, fulfillOn, assignments, false);
	}

	/**
	 * Constructor that takes all the data associated with an obligation. The
	 * attribute assignment list contains <code>Attribute</code> objects, but
	 * only the fields used by the AttributeAssignmentType are used.
	 * 
	 * @param id
	 *            the obligation's id
	 * @param fulfillOn
	 *            the effect denoting when to fulfill this obligation
	 * @param assignments
	 *            a <code>List</code> of <code>Attribute</code>s
	 * @param isIndeterminate 
	 */
	public Obligation(String id, int fulfillOn, List assignments,
			boolean isIndeterminate) {
		this.obligationId = id;
		this.fulfillOn = fulfillOn;
		this.attributeAssignments = Collections.unmodifiableList(new ArrayList(
				assignments));
		this.isIndeterminate = isIndeterminate;
	}

	/**
	 * Creates an instance of <code>Obligation</code> based on the DOM root
	 * node.
	 * 
	 * @param root
	 *            the DOM root of the ObligationType XML type
	 * 
	 * @return an instance of an obligation
	 * 
	 * @throws ParsingException
	 *             if the structure isn't valid
	 */
	public static Obligation getInstance(Node root) throws ParsingException {
		String id;
		int fulfillOn = -1;
		List assignments = new ArrayList();

		AttributeFactory attrFactory = AttributeFactory.getInstance();
		NamedNodeMap attrs = root.getAttributes();

		try {
			id = attrs.getNamedItem("ObligationId").getNodeValue();
		} catch (Exception e) {
			throw new ParsingException("Error parsing required attriubte "
					+ "ObligationId", e);
		}

		String effect = null;

		try {
			effect = attrs.getNamedItem("FulfillOn").getNodeValue();
		} catch (Exception e) {
			throw new ParsingException("Error parsing required attriubte "
					+ "FulfillOn", e);
		}

		if (effect.equals("Permit")) {
			fulfillOn = Result.DECISION_PERMIT;
		} else if (effect.equals("Deny")) {
			fulfillOn = Result.DECISION_DENY;
		} else {
			throw new ParsingException("Invalid Effect type: " + effect);
		}

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("AttributeAssignment")
					|| node.getNodeName().equals(
							"AttributeAssignmentExpression")) {
				try {
					URI attrId = new URI(node.getAttributes()
							.getNamedItem("AttributeId").getNodeValue());
					AttributeValue attrValue = attrFactory.createValue(node);
					assignments.add(new Attribute(attrId, null, null,
							attrValue,
							PolicyMetaData.XACML_VERSION_3_0));
				} catch (URISyntaxException use) {
					throw new ParsingException("Error parsing URI", use);
				} catch (UnknownIdentifierException uie) {
					throw new ParsingException("Unknown AttributeId", uie);
				} catch (Exception e) {
					throw new ParsingException("Error parsing attribute "
							+ "assignments", e);
				}
			}
		}

		return new Obligation(id, fulfillOn, assignments);
	}

	/**
	 * Creates an instance of <code>Obligation</code> based on the DOM root
	 * node.
	 * 
	 * @param root
	 *            the DOM root of the ObligationType XML type
	 * @param context 
	 * 
	 * @return an instance of an obligation
	 * 
	 * @throws ParsingException
	 *             if the structure isn't valid
	 */
	public static Obligation getInstance(ObligationExpression root,
			EvaluationCtx context) throws ParsingException {
		String id;
		int fulfillOn = -1;
		List<AttributeAssignment> assignments = new ArrayList<AttributeAssignment>();
		boolean indeterminate = false;
		EvaluationResult result = null;

		AttributeFactory attrFactory = AttributeFactory.getInstance();

		id = root.getObligationId();

		fulfillOn = root.getFulfillOn().ordinal();

		for (AttributeAssignmentExpression attrsAssignment : root
				.getAttributeAssignmentExpressions()) {
			URI attrId = URI.create(attrsAssignment.getAttributeId());
			String issuer = attrsAssignment.getIssuer();

			ExpressionType myExpr = null;
			try {
				myExpr = Expression.getInstance(attrsAssignment.getExpression().getValue());
			} catch (UnknownIdentifierException e) {
				LOGGER.error("Error parsing Expression", e);
				return new Obligation(id, fulfillOn, assignments, true);
			}
			
			// Check what type of expression this is
			if (myExpr instanceof ApplyType) {
				// TODO: Not Implemented
				throw new ParsingException("Obligation with Apply not implemented yet");
			} else if (myExpr instanceof AttributeSelectorType) {
				// TODO: Not Implemented
				throw new ParsingException("Obligation with AttributeSelector not implemented yet");
//			Not implemented yet	
			} else if (myExpr instanceof AttributeValueType) {
				AttributeValueType attrValue = (AttributeValueType)myExpr;
				URI datatype = URI.create(XACMLDatatypes.XACML_DATATYPE_STRING.value());
				if (attrValue.getDataType() != null) {
					datatype = URI.create(attrValue.getDataType());
				}
				/*
				 * Evaluation
				 */
				result = ((AttributeValue)attrValue).evaluate(context);
				if (result.indeterminate()) {
					indeterminate = true;
				}
				// an AD/AS will always return a bag
//				BagAttribute bag = (BagAttribute) (result.getAttributeValue());
				AttributeValueType bag = result.getAttributeValue();
				for (Serializable attributeAssignmentType : bag.getContent()) {
					AttributeAssignment attrAsgnType = new AttributeAssignment();
					attrAsgnType.getContent().add(attributeAssignmentType);
					attrAsgnType.setAttributeId(attrId.toASCIIString());
					attrAsgnType.setCategory(attrsAssignment.getCategory());
					attrAsgnType.setDataType(datatype.toASCIIString());
					attrAsgnType.setIssuer(issuer);					
					assignments.add(attrAsgnType);
				}
			} else if (myExpr instanceof FunctionType) {
				// TODO: Not Implemented
				throw new ParsingException("Obligation with FunctionType not implemented yet");
			} else if (myExpr instanceof VariableReferenceType) {
				// TODO: Not Implemented
				throw new ParsingException("Obligation with VariableReference not implemented yet");
			} else if (myExpr instanceof AttributeDesignator) {
				AttributeDesignator attrExpression = (AttributeDesignator) myExpr;
				/*
				 * Evaluation
				 */
				result = attrExpression.evaluate(context);
				if (result.indeterminate()) {
					indeterminate = true;
				}
			}
		}

		return new Obligation(id, fulfillOn, assignments, indeterminate);
	}

	/**
	 * Returns the id of this obligation
	 * 
	 * @return the id
	 */
	public String getId() {
		return obligationId;
	}

	/**
	 * Returns effect that will cause this obligation to be included in a
	 * response
	 * 
	 * @return the fulfillOn effect
	 */
	public int getFulfillOn() {
		return fulfillOn;
	}

	/**
	 * Returns the attribute assignment data in this obligation. The
	 * <code>List</code> contains objects of type <code>Attribute</code> with
	 * only the correct attribute fields being used.
	 * 
	 * @return the assignments
	 */
	public List<AttributeAssignment> getAssignments() {
		return attributeAssignments;
	}

	/**
	 * Return true if AttributeAssignement return an indeterminate
	 * evaluation
	 * @return true if and only if Indeterminate AttributeAssignment
	 */
	public boolean getIsIndeterminate() {
		return isIndeterminate;
	}

	/**
	 * Encodes this <code>Obligation</code> into its XML form and writes this
	 * out to the provided <code>OutputStream<code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Obligation</code> into its XML form and writes this
	 * out to the provided <code>OutputStream<code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		try {
			Marshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e) {
			LOGGER.error("Error marshalling Obligation", e);
		}
	}

}
