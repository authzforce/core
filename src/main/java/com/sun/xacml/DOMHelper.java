package com.sun.xacml;

import org.w3c.dom.Node;

public class DOMHelper {

	public static String getLocalName(Node child) {

		String localName = child.getLocalName();
		if (localName == null)
			return child.getNodeName();
		return localName;

	}
}