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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.DOMHelper;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.ctx.Attribute;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * @author Romain Ferrari
 * 
 */
public class PolicyIssuer extends oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer {
	
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PolicyIssuer.class);

	public static oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer getInstance(Node root) {
		Content content = new Content();
		List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute> attribute = new ArrayList<oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute>();
		
		// Setting elements
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("Attribute".equals(DOMHelper.getLocalName(child))) {
				try {
					attribute.add(Attribute.getInstance(child,  PolicyMetaData.XACML_VERSION_3_0));
				} catch (ParsingException e) {
					LOGGER.error("Error parsing Attribute from DOM node", e);
				}
			} else if ("Content".equals(DOMHelper.getLocalName(child))) {
				content.getContent().add(child.getTextContent());
			}
		}
		
		return new PolicyIssuer(content, attribute);
	}
	
	public PolicyIssuer(Content content, List<oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute> attributes) {
		this.content = content;
		this.attributes = attributes;
	}

}