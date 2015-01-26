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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;

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
