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
package com.sun.xacml.xacmlv3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetSection;
import com.sun.xacml.ctx.Status;

public class AnyOf extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf {

	/**
	 * Constructor that creates a new <code>AnyOfSelection</code> based on the
	 * given elements.
	 * 
	 * @param allOfType
	 *            a <code>List</code> of <code>AllOf</code> elements
	 */
	public AnyOf(List<AllOf> allOfType) {
		if (allOfType == null || allOfType.isEmpty()) {
			throw new IllegalArgumentException("<AnyOf> empty. Must contain at least one <AllOf>");
		}
		
		this.allOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf>(
				allOfType);
	}

	/**
	 * creates a <code>AnyOf</code> handler based on JAXB AnyOf element.
	 * 
	 * @param anyOf
	 *            JAXB AnyOf
	 * @param metadata
	 * @return a new <code>AnyOf</code>
	 * 
	 * @throws ParsingException
	 *             if AnyOf element is invalid
	 */
	public static AnyOf getInstance(
			oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf anyOf,
			PolicyMetaData metadata) throws ParsingException {
		final List<AllOf> allOfList = new ArrayList<>();
		for (final oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf allOfElement : anyOf
				.getAllOves()) {
			allOfList.add(AllOf.getInstance(allOfElement, metadata));
		}

		return new AnyOf(allOfList);
	}

	/**
	 * creates a <code>AnyOfSelection</code> based on its DOM node.
	 * 
	 * @param root
	 *            the node to parse for the AnyOfSelection
	 * @param metaData
	 *            meta-date associated with the policy
	 * 
	 * @return a new <code>AnyOfSelection</code> constructed by parsing
	 * 
	 * @throws ParsingException
	 *             if the DOM node is invalid
	 */
	public static AnyOf getInstance(Node root, PolicyMetaData metaData)
			throws ParsingException {
		List<AllOf> allOf = new ArrayList<>();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("AllOf".equals(DOMHelper.getLocalName(child))) {
				allOf.add(AllOf.getInstance(child, metaData));
			}
		}

		if (allOf.isEmpty()) {
			throw new ParsingException("AnyOf must contain at least one AllOf");
		}

		return new AnyOf(allOf);
	}

	public static List<TargetSection> getTargetSection(Node children,
			PolicyMetaData metaData) throws ParsingException {
		Node child = (Node) children.getChildNodes();
		if ("AllOf".equals(DOMHelper.getLocalName(child))) {
			return AllOf.getTargetSection(child, metaData);
		}
		return null;
	}

	/**
	 * Determines whether this <code>AnyOf</code> matches the input request
	 * (whether it is applicable). If all the AllOf values is No_Match so it's a
	 * No_Match. If all matches it's a Match. If None matches and at least one
	 * “Indeterminate” it's Indeterminate <code>
	 * 		<AllOf> values 						<AnyOf> value 
	 * 		At Least one "Match"	 			“Match” 
	 * 		None matches and 
	 * 		at least one Indeterminate 			“Indeterminate”
	 * 		All "No Match"						"No Match"
	 * </code>
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of trying to match the {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf} and the request
	 */
	public MatchResult match(EvaluationCtx context) {
		/*
		 * By construction, there must be at least one AllOf
		 * Let's check it to be sure
		 */
		final List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf> allOfList = this.getAllOves();
		if(allOfList == null || allOfList.isEmpty()) {
			/*
			 * TODO: provide a way to identify the <AnyOf>
			 */
			final Status status = new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR), "Invalid (Policy(Set)|Rule)#?<Target> / AnyOf#?: 0 <AllOf>. Must contain at least one <AllOf>"); 
			return new MatchResult(MatchResult.INDETERMINATE, status );
		}
		
		MatchResult firstIndeterminate = null;
		MatchResult lastNoMatch = null;
		int childIndex = 0;		
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : allOfList)
		{
			final AllOf allOf = (AllOf) jaxbAllOf;
			final MatchResult matchResult = allOf.match(context);
			if (matchResult == null)
			{
				// Invalid value for AllOf
				/*
				 * TODO: provide a way to identify the <AnyOf>
				 */
				final Status status = new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR), "Error processing (Policy(Set)/Rule)#?<Target> / AnyOf#? / AllOf#" + childIndex); 
				return new MatchResult(MatchResult.INDETERMINATE, status );
			}
			
			// matchResult != null at this point
			final int matchResultId = matchResult.getResult();
			/*
			 * Check MATCH value first
			 * 
			 * FIXME: MatchResult should be an enum type to make this kind of check simpler
			 * with switch statements
			 */
			if (matchResultId == MatchResult.MATCH)
			{
				// At least one MATCH, so return MATCH which is what this matchResult is already
				return matchResult;
			}

			if (matchResultId == MatchResult.INDETERMINATE)
			{
				firstIndeterminate = matchResult;
			} else
			{
				// it is a NO_MATCH (only other possible case)
				lastNoMatch = matchResult;
			}
			
			childIndex += 1;
		}

		// No MATCH occurred
		// firstIndeterminate == null iff no Indeterminate occurred
		if (firstIndeterminate == null)
		{
			// No MATCH/Indeterminate, i.e. all NO_MATCH, return NO_MATCH
			return lastNoMatch;
		}

		// No MATCH but at least one Indeterminate (firstIndeterminate != null)
		return firstIndeterminate;
	}

}
