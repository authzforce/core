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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetSection;
import com.sun.xacml.attr.xacmlv3.AttributeDesignator;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * @author Romain Ferrari
 * 
 */
public class AllOf extends AllOfType {

	/**
	 * the version of XACML used by the containing Match element
	 */
    private int xacmlVersion;

	/**
	 * Constructor that creates a <code>AllOfSelection</code> from components.
	 * 
	 * @param matches
	 *            a <code>List</code> of <code>TargetMatch</code> elements
	 */
	public AllOf(List<MatchType> match, int version) {
		if(match == null) {
			this.match = new ArrayList<MatchType>();
		} else {
			this.match = match;
		}
		this.xacmlVersion = version;
	}

	private static MatchType unmarshallMatchType(Node root) {
		JAXBElement<MatchType> match = null;
		try {
			JAXBContext jc = JAXBContext
					.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
			Unmarshaller u = jc.createUnmarshaller();
			match = (JAXBElement<MatchType>) u.unmarshal(root);
		} catch (Exception e) {
			System.err.println(e);
		}

		return match.getValue();
	}

	/**
	 * creates a new <code>AllOfSelection</code> by parsing DOM node.
	 * 
	 * @param root
	 *            DOM node
	 * @param metaData
	 *            policy meta data
	 * @return <code>AllOfSelection</code>
	 * @throws ParsingException
	 *             throws, if the DOM node is invalid
	 */
	public static AllOf getInstance(Node root, PolicyMetaData metaData)
			throws ParsingException {

		List<MatchType> matchType = new ArrayList<MatchType>();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("Match".equals(DOMHelper.getLocalName(child))) {
				matchType.add(Match.getInstance(child, metaData));
			}
		}

		if (matchType.isEmpty()) {
			throw new ParsingException("AllOf must contain at least one Match");
		}

		return new AllOf(matchType, Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value()));
	}

	public static List<TargetSection> getTargetSection(Node root,
			PolicyMetaData metadata) throws ParsingException {
		List<TargetSection> targetSection = new ArrayList<TargetSection>();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("Match".equals(DOMHelper.getLocalName(child))) {
				NodeList myNodes = child.getChildNodes();
				for (int j = 0; j < myNodes.getLength(); j++) {

					if ("AttributeDesignator".equals(myNodes.item(j)
							.getNodeName())) {
						String myCategory = myNodes.item(j).getAttributes()
								.getNamedItem("Category").getNodeValue();
						targetSection.add(TargetSection
								.getInstance(AttributeDesignator.getInstance(
										child, myCategory, metadata)));
					} else if ("AttributeSelector".equals(myNodes.item(j)
							.getNodeName())) {

					} else {
						throw new ParsingException(
								"Unknow Element: "
										+ myNodes.item(j).getNodeName()
										+ ". "
										+ "Supported element of Match are AttribtueDesignator and AttributeSelector.");
					}
				}
			}
		}

		return targetSection;
	}
}