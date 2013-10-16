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
/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.BindingUtility;
import com.sun.xacml.DOMHelper;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetMatchGroup;
import com.sun.xacml.TargetSection;
import com.sun.xacml.ctx.Status;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * @author Romain Ferrari
 * 
 */
public class AnyOf extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf {

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnyOf.class);
	
	// the list of match groups
    private List matchGroups;

	// the match type contained in this group
    private int matchType;

    // the version of XACML used by the containing Target
    private int xacmlVersion;

    public AnyOf() {
    	this.allOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf>();
    	this.matchGroups = Collections.unmodifiableList(new ArrayList());
    	this.matchType = -1;
        this.xacmlVersion = PolicyMetaData.XACML_VERSION_3_0;
    }
    
	public AnyOf(oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf allOfType, int xacmlVersion) {
		this(Arrays.asList(allOfType), xacmlVersion);
	}

	/**
	 * Constructor that creates a new <code>AnyOfSelection</code> based on the
	 * given elements.
	 * 
	 * @param allOfSelections
	 *            a <code>List</code> of <code>AllOfSelection</code> elements
	 */
	public AnyOf(List<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf> allOfType, int xacmlVersion) {
		if (allOfType == null) {
			this.allOves = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf>();
		} else {
			this.allOves = allOfType;
		}
        this.xacmlVersion = xacmlVersion;
	}

	private static oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf unmarshallAllOfType(Node root) {
		final JAXBElement<oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf> allOf;
		try {
			Unmarshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createUnmarshaller();
			allOf = u.unmarshal(root, oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf.class);
			return allOf.getValue();
		} catch (Exception e) {
			LOGGER.error("Error unmarshalling AllOf", e);
		}
		
		return null;
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
		List allOf = new ArrayList();
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

	public MatchResult match(EvaluationCtx context) {
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
