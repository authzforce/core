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
