/**
 * 
 */
package com.sun.xacml.xacmlv3;

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ContentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuerType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.ctx.Attribute;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;

/**
 * @author Romain Ferrari
 * 
 */
public class PolicyIssuer extends PolicyIssuerType {
	
	/**
	 * Logger used for all classes
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger
			.getLogger(PolicyIssuer.class);

	public static PolicyIssuerType getInstance(Node root) {
		ContentType content = new ContentType();
		List<AttributeType> attribute = new ArrayList<AttributeType>();
		
		// Setting elements
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("Attribute".equals(DOMHelper.getLocalName(child))) {
				try {
					attribute.add(Attribute.getInstance(child, Integer.parseInt(XACMLAttributeId.XACML_VERSION_3_0.value())));
				} catch (NumberFormatException e) {
					LOGGER.error(e);
				} catch (ParsingException e) {
					LOGGER.error(e);
				}
			} else if ("Content".equals(DOMHelper.getLocalName(child))) {
				content.getContent().add(child.getTextContent());
			}
		}
		
		return new PolicyIssuer(content, attribute);
	}
	
	public PolicyIssuer(ContentType content, List<AttributeType> attribute) {
		this.content = content;
		this.attribute = attribute;
	}

}