<<<<<<< HEAD

/*
 * @(#)Obligation.java
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.xacml;

import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeValue;

import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Result;

import java.io.OutputStream;
import java.io.PrintStream;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

=======
/**
 * Copyright (C) 2011-2013 Thales Services - ThereSIS - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

>>>>>>> 3.x
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

<<<<<<< HEAD
=======
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.cond.xacmlv3.Expression;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Result;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLDatatypes;
>>>>>>> 3.x

/**
 * Represents the ObligationType XML type in XACML. This also stores all the
 * AttriubteAssignmentType XML types.
<<<<<<< HEAD
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class Obligation
{

    // the obligation id
    private URI id;

    // effect to fulfill on, as defined in Result
    private int fulfillOn;

    // the attribute assignments
    private List assignments;

    /**
     * Constructor that takes all the data associated with an obligation.
     * The attribute assignment list contains <code>Attribute</code> objects,
     * but only the fields used by the AttributeAssignmentType are used.
     *
     * @param id the obligation's id
     * @param fulfillOn the effect denoting when to fulfill this obligation
     * @param assignments a <code>List</code> of <code>Attribute</code>s
     */
    public Obligation(URI id, int fulfillOn, List assignments) {
        this.id = id;
        this.fulfillOn = fulfillOn;
        this.assignments = Collections.
            unmodifiableList(new ArrayList(assignments));
    }

    /**
     * Creates an instance of <code>Obligation</code> based on the DOM root
     * node.
     *
     * @param root the DOM root of the ObligationType XML type
     *
     * @return an instance of an obligation
     *
     * @throws ParsingException if the structure isn't valid
     */
    public static Obligation getInstance(Node root) throws ParsingException {
        URI id;
        int fulfillOn = -1;
        List assignments = new ArrayList();

        AttributeFactory attrFactory = AttributeFactory.getInstance();
        NamedNodeMap attrs = root.getAttributes();

        try {
            id = new URI(attrs.getNamedItem("ObligationId").getNodeValue());
        } catch (Exception e) {
            throw new ParsingException("Error parsing required attriubte " +
                                       "ObligationId", e);
        }

        String effect = null;

        try {
            effect = attrs.getNamedItem("FulfillOn").getNodeValue();
        } catch (Exception e) {
            throw new ParsingException("Error parsing required attriubte " +
                                       "FulfillOn", e);
        }

        if (effect.equals("Permit")) {
            fulfillOn = Result.DECISION_PERMIT;
        } else if (effect.equals("Deny")) {
            fulfillOn = Result.DECISION_DENY;
        } else {
            throw new ParsingException("Invlid Effect type: " + effect);
        }

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("AttributeAssignment")) {
                try {
                    URI attrId =
                        new URI(node.getAttributes().
                                getNamedItem("AttributeId").getNodeValue());
                    AttributeValue attrValue = attrFactory.createValue(node);
                    assignments.add(new Attribute(attrId, null, null,
                                                  attrValue));
                } catch (URISyntaxException use) {
                    throw new ParsingException("Error parsing URI", use);
                } catch (UnknownIdentifierException uie) {
                    throw new ParsingException("Unknown AttributeId", uie);
                } catch (Exception e) {
                    throw new ParsingException("Error parsing attribute " +
                                               "assignments", e);
                }
            }
        }

        return new Obligation(id, fulfillOn, assignments);
    }
    
    /**
     * Returns the id of this obligation
     *
     * @return the id
     */
    public URI getId() {
        return id;
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
     * <code>List</code> contains objects of type <code>Attribute</code>
     * with only the correct attribute fields being used.
     *
     * @return the assignments
     */
    public List getAssignments() {
        return assignments;
    }

    /**
     * Encodes this <code>Obligation</code> into its XML form and writes this
     * out to the provided <code>OutputStream<code> with no indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>Obligation</code> into its XML form and writes this
     * out to the provided <code>OutputStream<code> with indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();
        
        out.println(indent + "<Obligation ObligationId=\"" + id.toString() +
                    "\" FulfillOn=\"" + Result.DECISIONS[fulfillOn] + "\">");

        indenter.in();
        
        Iterator it = assignments.iterator();

        while (it.hasNext()) {
            Attribute attr = (Attribute)(it.next());
            out.println(indenter.makeString() +
                        "<AttributeAssignment AttributeId=\"" +
                        attr.getId().toString() + "\" DataType=\"" +
                        attr.getType().toString() + "\">" +
                        attr.getValue().encode() +
                        "</AttributeAssignment>");
        }

        indenter.out();

        out.println(indent + "</Obligation>");
    }
=======
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Obligation extends ObligationType {

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
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Obligation.class);

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
	 */
	public Obligation(String id, int fulfillOn, List assignments,
			boolean isIndeterminate) {
		this.obligationId = id;
		this.fulfillOn = fulfillOn;
		this.attributeAssignment = Collections.unmodifiableList(new ArrayList(
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
							Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0
									.value())));
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
	 * 
	 * @return an instance of an obligation
	 * 
	 * @throws ParsingException
	 *             if the structure isn't valid
	 */
	public static Obligation getInstance(ObligationExpressionType root,
			EvaluationCtx context) throws ParsingException {
		String id;
		int fulfillOn = -1;
		List<AttributeAssignmentType> assignments = new ArrayList<AttributeAssignmentType>();
		boolean indeterminate = false;
		EvaluationResult result = null;

		AttributeFactory attrFactory = AttributeFactory.getInstance();

		id = root.getObligationId();

		fulfillOn = root.getFulfillOn().ordinal();

		for (AttributeAssignmentExpressionType attrsAssignment : root
				.getAttributeAssignmentExpression()) {
			URI attrId = URI.create(attrsAssignment.getAttributeId());
			URI attrCategory = null;
			if (attrsAssignment.getCategory() != null) {
				attrCategory = URI.create(attrsAssignment.getCategory());
			}
			String issuer = attrsAssignment.getIssuer();

			ExpressionType myExpr = null;
			try {
				myExpr = Expression.getInstance(((JAXBElement<ExpressionType>) attrsAssignment.getExpression()).getValue());
			} catch (UnknownIdentifierException e) {
				LOGGER.error(e);
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
				AttributeValue attr = null;
				for (Serializable attributeAssignmentType : bag.getContent()) {
					AttributeAssignmentType attrAsgnType = new AttributeAssignmentType();
					attrAsgnType.getContent().add(attributeAssignmentType);
					attrAsgnType.setAttributeId(attrId.toASCIIString());
					attrAsgnType.setCategory(attrsAssignment.getCategory());
					attrAsgnType.setDataType(datatype.toASCIIString());
					attrAsgnType.setIssuer(issuer);					
					try {
						attr = attrFactory.createValue(bag.getDataType(), attributeAssignmentType.toString());
					} catch (UnknownIdentifierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
				URI datatype = URI.create(XACMLDatatypes.XACML_DATATYPE_STRING.value());
				if (attrExpression.getDataType() != null) {
					datatype = URI.create(attrExpression.getDataType());
				}
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
	public List<AttributeAssignmentType> getAssignments() {
		return attributeAssignment;
	}

	/**
	 * Return true if and AttributeAssignement return an indeterminate
	 * evaluation
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
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Marshaller u = jc.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
>>>>>>> 3.x

}
