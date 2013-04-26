/*
 * @(#)Target.java
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * Represents the TargetType XML type in XACML. This also stores several other
 * XML types: Subjects, Resources, Actions, and Environments (in XACML 2.0 and
 * later). The target is used to quickly identify whether the parent element (a
 * policy set, policy, or rule) is applicable to a given request.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Target extends TargetType {

	// the version of XACML of the policy containing this target
	private int xacmlVersion = Integer
			.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value());

	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(Target.class);

	public Target(AnyOf anyof, int version) {
		anyOf = new ArrayList<AnyOfType>();
		this.xacmlVersion = version;
		this.anyOf.add(anyof);
	}

	public Target(AnyOf anyof) {
		anyOf = new ArrayList<AnyOfType>();
		this.anyOf.add(anyof);
	}

	public Target(List<AnyOf> anyof, int version) {
		anyOf = new ArrayList<AnyOfType>();
		this.xacmlVersion = version;
		this.anyOf.addAll(anyof);
	}

	public Target(List<AnyOf> anyof) {
		anyOf = new ArrayList<AnyOfType>();
		this.anyOf.addAll(anyof);
	}

	/**
	 * Creates a <code>Target</code> by parsing a node.
	 * 
	 * @param root
	 *            the node to parse for the <code>Target</code>
	 * @return a new <code>Target</code> constructed by parsing
	 * 
	 * @throws ParsingException
	 *             if the DOM node is invalid
	 */
	public static Target getInstance(Node root, PolicyMetaData metaData)
			throws ParsingException {
		List<AnyOf> anyOf = new ArrayList<AnyOf>();

		int version = metaData.getXACMLVersion();
		NodeList myChildren = root.getChildNodes();

		for (int i = 0; i < myChildren.getLength(); i++) {
			Node child = myChildren.item(i);
			if ("AnyOf".equals(DOMHelper.getLocalName(child))) {
				anyOf.add(AnyOf.getInstance(child, metaData));
			}
		}

		return new Target(anyOf, version);
	}

	/**
	 * Returns whether or not this <code>Target</code> matches any request. If
	 * the list of anyOf elements is empty it means that the target match any
	 * context.
	 * 
	 * @param version
	 *            the version of the context
	 * 
	 * @return true if this Target matches any request, false otherwise
	 */
	public boolean matchesAny(int version) {
		boolean matchAny = true;
		for (AnyOfType anyOf : this.anyOf) {
			for (AllOfType allOf : anyOf.getAllOf()) {
				matchAny = allOf.getMatch().isEmpty();
			}
		}

		return matchAny;
	}

	/**
	 * Determines whether this <code>Target</code> matches the input request
	 * (whether it is applicable).
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the target and the request
	 */
	public MatchResult match(EvaluationCtx context) {
		MatchResult result = null;

		// before matching, see if this target matches any request

		if (matchesAny(context.getVersion())) {
			return new MatchResult(MatchResult.MATCH);
		}
		
		

		for (AnyOfType anyOfList : this.anyOf) {
			for (AllOfType allOfList : anyOfList.getAllOf()) {
				for (MatchType matchList : allOfList.getMatch()) {
					if (matchList.getAttributeDesignator() != null) {
						result = ((Match)matchList).match(context);
//						result = new MatchResult(MatchResult.MATCH);
					} else if (matchList.getAttributeSelector() != null) {
						result = ((Match)matchList).match(context);
//						result = new MatchResult(MatchResult.MATCH);
					} else {
						LOGGER.error("failed to match any element of Target");
						return new MatchResult(MatchResult.NO_MATCH);
					}
					if(result.getResult() != MatchResult.MATCH) {
						return result;
					}
				}
			}
		}

		// if we got here, then everything matched
		return result;
	}

	/**
	 * Encodes this <code>Target</code> into its XML representation and writes
	 * this encoding to the given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output) {
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Target</code> into its XML representation and writes
	 * this encoding to the given <code>OutputStream</code> with indentation.
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
