/*
 * @(#)TargetMatch.java
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

package com.sun.xacml.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.sun.xacml.attr.xacmlv3.AttributeSelector;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionTypeException;
import com.sun.xacml.cond.xacmlv3.EvaluationResult;
import com.sun.xacml.ctx.Status;

/**
 * Represents the SubjectMatch, ResourceMatch, ActionMatch, or EnvironmentMatch
 * (in XACML 2.0 and later) XML types in XACML, depending on the value of the
 * type field. This is the part of the Target that actually evaluates whether
 * the specified attribute values in the Target match the corresponding
 * attribute values in the request context.

 * @author Romain Ferrari
 */
public class Match extends MatchType {

	/**
	 * An integer value indicating that this class represents a SubjectMatch
	 */
	public static final int SUBJECT = 0;

	/**
	 * An integer value indicating that this class represents a ResourceMatch
	 */
	public static final int RESOURCE = 1;

	/**
	 * An integer value indicating that this class represents an ActionMatch
	 */
	public static final int ACTION = 2;

	/**
	 * An integer value indicating that this class represents an
	 * EnvironmentMatch
	 */
	public static final int ENVIRONMENT = 3;
	
	/**
	 * XACML 3.0
	 * An integer value indicating that this class represents an
	 * AttributeDesignator
	 */
	public static final int ATTRIBUTE_DESIGNATOR = 4;
	
	/**
	 * XACML 3.0
	 * An integer value indicating that this class represents an
	 * Match
	 */
	public static final int MATCH = 5;
	
	/**
	 * XACML 3.0
	 * An integer value indicating that this class represents an
	 * AttributeSelector
	 */
	public static final int ATTRIBUTE_SELECTOR = 6;
	
	/**
	 * XACML 3.0
	 * An integer value indicating that this class represents an
	 * AttributeValue
	 */
	public static final int ATTRIBUTE_VALUE = 7;

	/**
	 * Mapping from the 4 match types to their string representations
	 */
	public static final String[] NAMES = { "Subject", "Resource", "Action",
			"Environment" , "AttributeDesignator", "Match", "AttributeSelector", "AttributeValue" };
	
	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Match.class);

	// the type of this target match
	private int type;

	// the function used for matching
	private Function function;

	// the designator or selector
	private Evaluatable eval;

	public Match(AttributeValueType attrValue, ExpressionType exprType) throws ParsingException {
		if(attrValue == null) {
			throw new ParsingException("A Match element need to contained at list one AttributeValue");
		} else {
			this.attributeValue = attrValue;
		}
		if(exprType == null) {
			throw new ParsingException("A Match element need to contained at list one AttributeDesignator " +
					"or one AttributeSelector");
		} else {
			if(exprType instanceof AttributeDesignatorType) {
				this.attributeDesignator = (AttributeDesignatorType)exprType;
			} else if(exprType instanceof AttributeSelectorType) {
				this.attributeSelector = (AttributeSelectorType)exprType;
			}
		}
	}

	/**
	 * Constructor that creates a <code>TargetMatch</code> from components.
	 * 
	 * @param type
	 *            an integer indicating whether this class represents a
	 *            SubjectMatch, ResourceMatch, or ActionMatch
	 * @param function
	 *            the <code>Function</code> that represents the MatchId
	 * @param eval
	 *            the <code>AttributeDesignator</code> or
	 *            <code>AttributeSelector</code> to be used to select attributes
	 *            from the request context
	 * @param attrValue
	 *            the <code>AttributeValue</code> to compare against
	 * 
	 * @throws IllegalArgumentException
	 *             if the input type isn't a valid value
	 */
	public Match(int type, Function function, Evaluatable eval,
			AttributeValue attrValue) throws IllegalArgumentException {

		// check if input type is a valid value
		if ((type != SUBJECT) && (type != RESOURCE) && (type != ACTION)
				&& (type != ENVIRONMENT))
			throw new IllegalArgumentException("Unknown TargetMatch type");

		this.type = type;
		this.function = function;
		this.eval = eval;
		this.attributeValue = attrValue;
	}

	/**
	 * Creates a <code>TargetMatch</code> by parsing a node, using the input
	 * prefix to determine whether this is a SubjectMatch, ResourceMatch, or
	 * ActionMatch.
	 * 
	 * @param root
	 *            the node to parse for the <code>TargetMatch</code>
	 * @param metaData
	 *            the policy's meta-data
	 * 
	 * @return a new <code>TargetMatch</code> constructed by parsing
	 * 
	 * @throws ParsingException
	 *             if there was an error during parsing
	 */
	public static Match getInstance(Node root, PolicyMetaData metaData) throws ParsingException {
		Function function;
//		Evaluatable eval = null;
		ExpressionType eval = null;
		AttributeValueType attrValue = null;

		AttributeFactory attrFactory = AttributeFactory.getInstance();

		// get the function type, making sure that it's really a correct
		// Target function
		String funcName = root.getAttributes().getNamedItem("MatchId").getNodeValue();
		FunctionFactory factory = FunctionFactory.getTargetInstance();
		try {
			URI funcId = new URI(funcName);
			function = factory.createFunction(funcId);
		} catch (URISyntaxException use) {
			throw new ParsingException("Error parsing TargetMatch", use);
		} catch (UnknownIdentifierException uie) {
			throw new ParsingException("Unknown MatchId", uie);
		} catch (FunctionTypeException fte) {
			// try to create an abstract function
			try {
				URI funcId = new URI(funcName);
				function = factory.createAbstractFunction(funcId, root);
			} catch (Exception e) {
				// any exception here is an error
				throw new ParsingException("invalid abstract function", e);
			}
		}

		// next, get the designator or selector being used, and the attribute
		// value paired with it
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getNodeName();

			if (name.equals("AttributeDesignator")) {
				eval = AttributeDesignator.getInstance(node);
			} else if (name.equals("AttributeSelector")) {
				eval = AttributeSelector.getInstance(node, metaData);
			} else if (name.equals("AttributeValue")) {
				attrValue = AttributeValue.getInstance(node, metaData);
			}
		}

		// finally, check that the inputs are valid for this function
		List inputs = new ArrayList();
		inputs.add(attrValue);
		inputs.add(eval);
		function.checkInputsNoBag(inputs);

		return new Match(attrValue, eval);
	}

	/**
	 * Returns the type of this <code>TargetMatch</code>, either
	 * <code>SUBJECT</code>, <code>RESOURCE</code>, <code>ACTION</code>, or
	 * <code>ENVIRONMENT</code>.
	 * 
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the <code>Function</code> used to do the matching.
	 * 
	 * @return the match function
	 */
	public Function getMatchFunction() {
		return function;
	}

	/**
	 * Returns the <code>AttributeValue</code> used by the matching function.
	 * 
	 * @return the <code>AttributeValue</code> for the match
	 */
	public AttributeValueType getMatchValue() {
		return this.attributeValue;
	}

	/**
	 * Returns the <code>AttributeDesignator</code> or
	 * <code>AttributeSelector</code> used by the matching function.
	 * 
	 * @return the designator or selector for the match
	 */
	public Evaluatable getMatchEvaluatable() {
		return eval;
	}

	/**
	 * Determines whether this <code>TargetMatch</code> matches the input
	 * request (whether it is applicable)
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the TargetMatch and the request
	 */
	public MatchResult match(EvaluationCtx context) {
		// start by evaluating the AD/AS
		EvaluationResult result = eval.evaluate(context);

		if (result.indeterminate()) {
			// in this case, we don't ask the function for anything, and we
			// simply return INDETERMINATE
			return new MatchResult(MatchResult.INDETERMINATE,
					result.getStatus());
		}

		// an AD/AS will always return a bag
		BagAttribute bag = (BagAttribute) (result.getAttributeValue());

		if (!bag.isEmpty()) {
			// we got back a set of attributes, so we need to iterate through
			// them, seeing if at least one matches
			Iterator it = bag.iterator();
			boolean atLeastOneError = false;
			Status firstIndeterminateStatus = null;

			while (it.hasNext()) {
				ArrayList inputs = new ArrayList();

				inputs.add(this.attributeValue);
				inputs.add(it.next());

				// do the evaluation
				MatchResult match = evaluateMatch(inputs, context);

				// we only need one match for this whole thing to match
				if (match.getResult() == MatchResult.MATCH)
					return match;

				// if it was INDETERMINATE, we want to remember for later
				if (match.getResult() == MatchResult.INDETERMINATE) {
					atLeastOneError = true;

					// there are no rules about exactly what status data
					// should be returned here, so like in the combining
					// algs, we'll just track the first error
					if (firstIndeterminateStatus == null)
						firstIndeterminateStatus = match.getStatus();
				}
			}

			// if we got here, then nothing matched, so we'll either return
			// INDETERMINATE or NO_MATCH
			if (atLeastOneError)
				return new MatchResult(MatchResult.INDETERMINATE,
						firstIndeterminateStatus);
			else
				return new MatchResult(MatchResult.NO_MATCH);

		} else {
			// this is just an optimization, since the loop above will
			// actually handle this case, but this is just a little
			// quicker way to handle an empty bag
			return new MatchResult(MatchResult.NO_MATCH);
		}
	}

	/**
	 * Private helper that evaluates an individual match.
	 */
	private MatchResult evaluateMatch(List inputs, EvaluationCtx context) {
		// first off, evaluate the function
		EvaluationResult result = function.evaluate(inputs, context);

		// if it was indeterminate, then that's what we return immediately
		if (result.indeterminate())
			return new MatchResult(MatchResult.INDETERMINATE,
					result.getStatus());

		// otherwise, we figure out if it was a match
		BooleanAttribute bool = (BooleanAttribute) (result.getAttributeValue());

		if (bool.getValue())
			return new MatchResult(MatchResult.MATCH);
		else
			return new MatchResult(MatchResult.NO_MATCH);
	}

	/**
	 * Encodes this <code>TargetMatch</code> into its XML representation and
	 * writes this encoding to the given <code>OutputStream</code> with no
	 * indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>TargetMatch</code> into its XML representation and
	 * writes this encoding to the given <code>OutputStream</code> with
	 * indentation.
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
