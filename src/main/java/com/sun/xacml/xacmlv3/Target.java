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
package com.sun.xacml.xacmlv3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.core.PdpModelHandler;

/**
 * Represents the TargetType XML type in XACML. This also stores several other XML types: Subjects,
 * Resources, Actions, and Environments (in XACML 2.0 and later). The target is used to quickly
 * identify whether the parent element (a policy set, policy, or rule) is applicable to a given
 * request.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Target extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Target
{

	// the version of XACML of the policy containing this target
	// private int xacmlVersion = XACMLVersion.V3_0.value();

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Target.class);

	public Target(AnyOf anyof, int version)
	{
		anyOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf>();
		// this.xacmlVersion = version;
		this.anyOves.add(anyof);
	}

	public Target(AnyOf anyof)
	{
		anyOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf>();
		this.anyOves.add(anyof);
	}

	public Target(List<AnyOf> anyof, int version)
	{
		anyOves = new ArrayList<>();
		// this.xacmlVersion = version;
		this.anyOves.addAll(anyof);
	}

	public Target(List<AnyOf> anyof)
	{
		anyOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf>();
		this.anyOves.addAll(anyof);
	}

	/**
	 * Creates Target handler from Target element as defined in OASIS XACML model
	 * 
	 * @param targetElement
	 * @param metadata
	 * @throws ParsingException
	 *             if Target element is invalid
	 */
	public Target(oasis.names.tc.xacml._3_0.core.schema.wd_17.Target targetElement, PolicyMetaData metadata) throws ParsingException
	{
		anyOves = new ArrayList<>();
		// this.xacmlVersion = version;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf anyOfElement : targetElement.getAnyOves())
		{
			final AnyOf anyOf = AnyOf.getInstance(anyOfElement, metadata);
			this.anyOves.add(anyOf);
		}
	}

	/**
	 * Creates a <code>Target</code> by parsing a node.
	 * 
	 * @param root
	 *            the node to parse for the <code>Target</code>
	 * @param metaData
	 * @return a new <code>Target</code> constructed by parsing
	 * 
	 * @throws ParsingException
	 *             if the DOM node is invalid
	 */
	public static Target getInstance(Node root, PolicyMetaData metaData) throws ParsingException
	{
		List<AnyOf> anyOf = new ArrayList<>();

		int version = metaData.getXACMLVersion();
		NodeList myChildren = root.getChildNodes();

		for (int i = 0; i < myChildren.getLength(); i++)
		{
			Node child = myChildren.item(i);
			if ("AnyOf".equals(DOMHelper.getLocalName(child)))
			{
				anyOf.add(AnyOf.getInstance(child, metaData));
			}
		}

		return new Target(anyOf, version);
	}

	/**
	 * Determines whether this <code>Target</code> matches the input request (whether it is
	 * applicable). If any of the AnyOf doesn't match the request context so it's a NO_MATCH result.
	 * Here is the table shown in the specification: <code> 
	 * 		<AnyOf> values 				<Target> value
	 * 		All “Match”					“Match”
	 * 		At Least one "No Match"		“No Match”
	 * 		Otherwise					“Indeterminate”
	 * </code>
	 * Also if Target empty (no AnyOf), return "Match"
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the
	 *         {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf} and the request
	 */
	public MatchResult match(EvaluationCtx context)
	{
		MatchResult lastMatch = null;
		int childIndex = 0;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf jaxbAnyOf : this.getAnyOves())
		{
			final AnyOf anyOf = (AnyOf) jaxbAnyOf;
			final MatchResult matchResult = anyOf.match(context);
			if (matchResult == null)
			{
				/*
				 * TODO: provide a way to identify the <Target>
				 */
				final Status status = new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR),
						"Error processing (Policy(Set)|Rule)#?<Target> / AnyOf#?" + childIndex);
				return new MatchResult(MatchResult.INDETERMINATE, status);
			}

			// matchResult != null at this point
			final int matchResultId = matchResult.getResult();
			/*
			 * If it is not MATCH, it is either NO_MATCH-> return NO_MATCH; or INDETERMINATE ->
			 * return INDETERMINATE, so in both cases we return the result as is
			 */
			if (matchResultId != MatchResult.MATCH)
			{
				// It is neither MATCH or NO_MATCH, so INDETERMINATE, return
				return matchResult;
			}

			// it is a MATCH (only other possible case)
			lastMatch = matchResult;
			childIndex += 1;
		}

		/*
		 * if lastMatch == null, i.e. no AnyOf, i.e. empty Target, return Match, else (lastMatch !=
		 * null) All MATCH, return MATCH which is what lastMatch is already
		 */
		return lastMatch == null ? new MatchResult(MatchResult.MATCH) : lastMatch;
	}

	/**
	 * Encodes this <code>Target</code> into its XML representation and writes this encoding to the
	 * given <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this <code>Target</code> into its XML representation and writes this encoding to the
	 * given <code>OutputStream</code> with indentation.
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
			LOGGER.error("Error Marshalling Target", e);
		}
	}

}
