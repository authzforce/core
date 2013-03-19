/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.TargetMatch;
import com.sun.xacml.TargetSection;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * @author Romain Ferrari
 * 
 */
public class AllOf extends AllOfType {

	/**
	 * List of SubjectMatch, ResourceMatch, ActionMatch, or EnvironmentMatch
	 */
	List<TargetMatch> matches;

	/**
	 * Constructor that creates a <code>AllOfSelection</code> from components.
	 * 
	 * @param matches
	 *            a <code>List</code> of <code>TargetMatch</code> elements
	 */
	public AllOf(List<TargetMatch> matches) {
		this.matches = matches;

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

		List<TargetMatch> targetMatches = new ArrayList<TargetMatch>();
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
						if (XACMLAttributeId.XACML_1_0_SUBJECT_CATEGORY_SUBJECT
								.value().equals(myCategory)) {
							targetMatches.add(TargetMatch.getInstance(child,
									TargetMatch.SUBJECT, metaData));
						} else if (XACMLAttributeId.XACML_3_0_RESOURCE_CATEGORY_RESOURCE
								.value().equals(myCategory)) {
							targetMatches.add(TargetMatch.getInstance(child,
									TargetMatch.RESOURCE, metaData));
						} else if (XACMLAttributeId.XACML_3_0_ACTION_CATEGORY_ACTION
								.value().equals(myCategory)) {
							targetMatches.add(TargetMatch.getInstance(child,
									TargetMatch.ACTION, metaData));
						}
					}
				}
			}
		}

		if (targetMatches.isEmpty()) {
			throw new ParsingException("AllOf must contain at least one Match");
		}

		return new AllOf(targetMatches);
	}

	public static List<TargetSection> getTargetSection(Node root,
			PolicyMetaData metadata) {
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
						try {
							if (XACMLAttributeId.XACML_1_0_SUBJECT_CATEGORY_SUBJECT
									.value().equals(myCategory)) {

								targetSection.add(TargetSection.getInstance(
										child, TargetMatch.SUBJECT, metadata));

							} else if (XACMLAttributeId.XACML_3_0_RESOURCE_CATEGORY_RESOURCE
									.value().equals(myCategory)) {
								targetSection.add(TargetSection.getInstance(
										child, TargetMatch.RESOURCE, metadata));
							} else if (XACMLAttributeId.XACML_3_0_ACTION_CATEGORY_ACTION
									.value().equals(myCategory)) {
								targetSection.add(TargetSection.getInstance(
										child, TargetMatch.ACTION, metadata));
							}
						} catch (ParsingException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		return null;
	}
}