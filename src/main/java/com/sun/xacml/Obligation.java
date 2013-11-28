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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpression;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.cond.xacmlv3.ExpressionTools;
import com.sun.xacml.ctx.Result;
import com.thalesgroup.authzforce.BindingUtility;

/**
 * Represents the ObligationType XML type in XACML. This also stores all the AttriubteAssignmentType
 * XML types.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Obligation extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation
{

	// the obligation id
	// private URI id;

	// effect to fulfill on, as defined in Result
	private int fulfillOn;

	// the attribute assignments
	// private List<AttributeAssignmentType> assignments;

	private boolean isIndeterminate = false;

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Obligation.class);

	/**
	 * Constructor that takes all the data associated with an obligation. The attribute assignment
	 * list contains <code>Attribute</code> objects, but only the fields used by the
	 * AttributeAssignmentType are used.
	 * 
	 * @param id
	 *            the obligation's id
	 * @param fulfillOn
	 *            the effect denoting when to fulfill this obligation
	 * @param assignments
	 *            a <code>List</code> of <code>Attribute</code>s
	 */
	public Obligation(String id, int fulfillOn, List<AttributeAssignment> assignments)
	{
		this(id, fulfillOn, assignments, false);
	}

	/**
	 * Constructor that takes all the data associated with an obligation. The attribute assignment
	 * list contains <code>Attribute</code> objects, but only the fields used by the
	 * AttributeAssignmentType are used.
	 * 
	 * @param id
	 *            the obligation's id
	 * @param fulfillOn
	 *            the effect denoting when to fulfill this obligation
	 * @param assignments
	 *            a <code>List</code> of <code>Attribute</code>s
	 * @param isIndeterminate
	 */
	public Obligation(String id, int fulfillOn, List<AttributeAssignment> assignments, boolean isIndeterminate)
	{
		this.obligationId = id;
		this.fulfillOn = fulfillOn;
		this.attributeAssignments = Collections.unmodifiableList(assignments);
		this.isIndeterminate = isIndeterminate;
	}

	/**
	 * Creates an instance of <code>Obligation</code> based on the DOM root node.
	 * 
	 * @param root
	 *            the DOM root of the ObligationType XML type
	 * 
	 * @return an instance of an obligation
	 * 
	 * @throws ParsingException
	 *             if the structure isn't valid
	 */
	public static Obligation getInstance(Node root) throws ParsingException
	{
		String id;
		int fulfillOn = -1;
		List<AttributeAssignment> assignments = new ArrayList<>();

		AttributeFactory attrFactory = AttributeFactory.getInstance();
		NamedNodeMap attrs = root.getAttributes();

		try
		{
			id = attrs.getNamedItem("ObligationId").getNodeValue();
		} catch (Exception e)
		{
			throw new ParsingException("Error parsing required attriubte " + "ObligationId", e);
		}

		String effect = null;

		try
		{
			effect = attrs.getNamedItem("FulfillOn").getNodeValue();
		} catch (Exception e)
		{
			throw new ParsingException("Error parsing required attriubte " + "FulfillOn", e);
		}

		if (effect.equals("Permit"))
		{
			fulfillOn = Result.DECISION_PERMIT;
		} else if (effect.equals("Deny"))
		{
			fulfillOn = Result.DECISION_DENY;
		} else
		{
			throw new ParsingException("Invalid Effect type: " + effect);
		}

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeName().equals("AttributeAssignment") || node.getNodeName().equals("AttributeAssignmentExpression"))
			{
				try
				{
					URI attrId = new URI(node.getAttributes().getNamedItem("AttributeId").getNodeValue());
					String cat = node.getAttributes().getNamedItem("Category").getNodeValue();
					String issuer = node.getAttributes().getNamedItem("Issuer").getNodeValue();
					AttributeValue attrValue = attrFactory.createValue(node);
					AttributeAssignment assignt = new AttributeAssignment();
					assignt.setAttributeId(attrId.toASCIIString());
					assignt.setCategory(cat);
					assignt.setIssuer(issuer);
					assignt.setDataType(attrValue.getDataType());
					assignt.getContent().addAll(attrValue.getContent());
					assignments.add(assignt);
				} catch (URISyntaxException use)
				{
					throw new ParsingException("Error parsing URI", use);
				} catch (UnknownIdentifierException uie)
				{
					throw new ParsingException("Unknown AttributeId", uie);
				} catch (Exception e)
				{
					throw new ParsingException("Error parsing attribute " + "assignments", e);
				}
			}
		}

		return new Obligation(id, fulfillOn, assignments);
	}

	/**
	 * Creates an instance of <code>Obligation</code> based on the DOM root node.
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
	public static Obligation getInstance(ObligationExpression root, EvaluationCtx context) throws ParsingException
	{
		int fulfillOn = root.getFulfillOn().ordinal();
		boolean indeterminate = false;
		String id = root.getObligationId();
		
		final List<AttributeAssignment> assignments = new ArrayList<>();
		for (AttributeAssignmentExpression attrAssignmentExpr : root.getAttributeAssignmentExpressions())
		{
			String attrId = attrAssignmentExpr.getAttributeId();
			String issuer = attrAssignmentExpr.getIssuer();

			ExpressionType myExpr = ExpressionTools.getInstance(attrAssignmentExpr.getExpression().getValue(), null, null);

			if (myExpr instanceof FunctionType)
			{
				throw new ParsingException(
						"<Function> NOT allowed as <AttributeAssignmentExpression> of <ObligationExpression> BUT only as child of an <Apply> element");
			}

			if (!(myExpr instanceof Evaluatable))
			{
				throw new ParsingException(myExpr.getClass().getSimpleName()
						+ " element NOT supported as <AttributeAssignmentExpression> of <ObligationExpression>");
			}

			// Expression is Evaluatable
			final Evaluatable evaluatable = (Evaluatable) myExpr;
			final EvaluationResult result = evaluatable.evaluate(context);
			/*
			 * According to XACML 3.0 spec, if result is empty bag, no AttributeAssignment; if
			 * non-empty bag, one <AttributeAssignment> per value in the bag.
			 */
			if (result.indeterminate())
			{
				LOGGER.error(
						"Evaluation of {} element as <AttributeAssignmentExpression> (id='{}') of <ObligationExpression> (id='{}') returned Indeterminate",
						myExpr.getClass().getSimpleName(), attrId, id);
				indeterminate = true;
				// assignments remains an empty list
			} else
			{
				assignments.addAll(toAssignments(id, attrAssignmentExpr.getCategory(), attrId, issuer, result.getAttributeValue(), true));
			}
		}

		return new Obligation(id, fulfillOn, assignments, indeterminate);
	}

	private static List<AttributeAssignment> toAssignments(String obligationId, String category, String attrId, String issuer,
			AttributeValue attrVal, boolean isRootBag) throws ParsingException
	{
		final List<AttributeAssignment> assignts = new ArrayList<>();
		if (attrVal.isBag())
		{
			// nested bags (not the root bag in attribute value) not allowed
			if (!isRootBag)
			{
				throw new ParsingException(
						String.format(
								"Evaluation of <AttributeAssignmentExpression> (id='%s') of <ObligationExpression> (id='%s') resulted in bag with nested bags, which is not allowed",
								attrId, obligationId));
			}

			// If bag, create one assignment per value
			for (final AttributeValue childVal : ((BagAttribute) attrVal).getValues())
			{
				assignts.addAll(toAssignments(obligationId, category, attrId, issuer, childVal, false));
			}
		} else
		{
			final AttributeAssignment attrAssignt = new AttributeAssignment();
			attrAssignt.setCategory(category);
			attrAssignt.setAttributeId(attrId);
			attrAssignt.setIssuer(issuer);
			attrAssignt.setDataType(attrVal.getDataType());
			attrAssignt.getContent().addAll(attrVal.getContent());
			assignts.add(attrAssignt);
		}

		return assignts;
	}

	/**
	 * Returns the id of this obligation
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return obligationId;
	}

	/**
	 * Returns effect that will cause this obligation to be included in a response
	 * 
	 * @return the fulfillOn effect
	 */
	public int getFulfillOn()
	{
		return fulfillOn;
	}

	/**
	 * Returns the attribute assignment data in this obligation. The <code>List</code> contains
	 * objects of type <code>Attribute</code> with only the correct attribute fields being used.
	 * 
	 * @return the assignments
	 */
	public List<AttributeAssignment> getAssignments()
	{
		return attributeAssignments;
	}

	/**
	 * Return true if AttributeAssignement return an indeterminate evaluation
	 * 
	 * @return true if and only if Indeterminate AttributeAssignment
	 */
	public boolean getIsIndeterminate()
	{
		return isIndeterminate;
	}

	/**
	 * Encodes this <code>Obligation</code> into its XML form and writes this out to the provided
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
	 * Encodes this <code>Obligation</code> into its XML form and writes this out to the provided
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
			Marshaller u = BindingUtility.XACML3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Obligation", e);
		}
	}

}
