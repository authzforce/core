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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetMatchGroup;
import com.sun.xacml.TargetSection;
import com.sun.xacml.ctx.Status;

public class AnyOf extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf {

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnyOf.class);
	
	// private static final Logger LOGGER = LoggerFactory
	// .getLogger(AnyOf.class);

	// the list of match groups
	private List matchGroups;

	// the match type contained in this group
	/**
	 * FIXME: this variable is never used
	 */
	private int matchType;

	// the version of XACML used by the containing Target
	/**
	 * FIXME: this variable is never used
	 */
	private int xacmlVersion;

	public AnyOf() {
		this.allOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf>();
		this.matchGroups = Collections.unmodifiableList(new ArrayList());
		this.matchType = -1;
		this.xacmlVersion = PolicyMetaData.XACML_VERSION_3_0;
	}

	/**
	 * Constructor that creates a new <code>AnyOfSelection</code> based on the
	 * given elements.
	 * 
	 * @param allOfSelections
	 *            a <code>List</code> of <code>AllOfSelection</code> elements
	 */
	public AnyOf(List<AllOf> allOfType, int xacmlVersion) {
		if (allOfType == null) {
			this.allOves = new ArrayList<>();
		} else {
			this.allOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf>(
					allOfType);
		}
		this.xacmlVersion = xacmlVersion;
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

		if (allOfList.isEmpty()) {
			throw new ParsingException(
					"AnyOf element must contain at least one AllOf element");
		}

		return new AnyOf(allOfList, PolicyMetaData.XACML_VERSION_3_0);
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
		List<AllOf> allOf = new ArrayList<AllOf>();
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

		return new AnyOf(allOf, PolicyMetaData.XACML_VERSION_3_0);
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
		MatchResult result = null;
		MatchResult resultInd = null;
		boolean indeterminate = false;
		for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf jaxbAllOf : this.getAllOves()) {
			AllOf allOf = (AllOf) jaxbAllOf;
			result = allOf.match(context);
			if(result.getResult() == MatchResult.MATCH) {
				return result;
			} else if(result.getResult() == MatchResult.INDETERMINATE) {
				indeterminate = true;
				result = new MatchResult(MatchResult.INDETERMINATE, result.getStatus());
			}
		}
		// If we got here then none matched
		if(indeterminate) {
			result = resultInd;
		}

		return result;
	}

	public MatchResult oldMatch(EvaluationCtx context) {
		// if we apply to anything, then we always match
		if (matchGroups.isEmpty()) {
			return new MatchResult(MatchResult.MATCH);
		}

		// there are specific matching elements, so prepare to iterate
		// through the list
		Iterator it = matchGroups.iterator();
		Status firstIndeterminateStatus = null;

		// in order for this section to match, one of the groups must match
		while (it.hasNext()) {
			// get the next group and try matching it
			TargetMatchGroup group = (TargetMatchGroup) (it.next());
			MatchResult result = group.match(context);

			// we only need one match, so if this matched, then we're done
			if (result.getResult() == MatchResult.MATCH) {
				return result;
			}

			// if we didn't match then it was either a NO_MATCH or
			// INDETERMINATE...in the second case, we need to remember
			// it happened, 'cause if we don't get a MATCH, then we'll
			// be returning INDETERMINATE
			if (result.getResult() == MatchResult.INDETERMINATE) {
				if (firstIndeterminateStatus == null) {
					firstIndeterminateStatus = result.getStatus();
				}
			}
		}

		// if we got here, then none of the sub-matches passed, so
		// we have to see if we got any INDETERMINATE cases
		if (firstIndeterminateStatus == null) {
			return new MatchResult(MatchResult.NO_MATCH);
		} else {
			return new MatchResult(MatchResult.INDETERMINATE,
					firstIndeterminateStatus);
		}
	}

}
