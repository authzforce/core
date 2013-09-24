/**
 * 
 */
package com.sun.xacml.xacmlv3;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;

/**
 * @author Romain Ferrari
 * 
 */
public class PolicyDefaults extends DefaultsType {

	public PolicyDefaults(String xpathVersion) {
		this.xPathVersion = xpathVersion;
	}

	public static DefaultsType getInstance(Node root) {
		String xpathVersion = null;
		// Setting elements
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("XPathVersion".equals(DOMHelper.getLocalName(child))) {
				xpathVersion = child.getNodeValue();
			}
		}
		
		return new PolicyDefaults(xpathVersion);
	}

}
