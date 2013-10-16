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

import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.BindingUtility;

/**
 * @author Romain Ferrari
 * 
 */
public class AdviceExpressions extends oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdviceExpressions.class);

	public static oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions getInstance(
			Set<AdviceExpression> advice) {
		oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions adviceExpr = new oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions();
		adviceExpr.getAdviceExpressions().addAll(advice);

		return adviceExpr;
	}

	public static oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions getInstance(Node root) {
		NodeList nodes = root.getChildNodes();
		oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions adviceExpressions = null;
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("AdviceExpression")) {
				final JAXBElement<oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions> match;
				try {
					Unmarshaller u = BindingUtility.XACML30_JAXB_CONTEXT.createUnmarshaller();
					match =  u.unmarshal(root, oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressions.class);
					adviceExpressions = match.getValue();
				} catch (Exception e) {
					LOGGER.error("Error unmarshalling AdviceExpressions", e);
				}
				
				break;
			}
		}
		
		return adviceExpressions;
	}

}
