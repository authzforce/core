/**
 * Copyright (C) 2012-2013 Thales Services - ThereSIS - All rights reserved.
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Romain Ferrari
 * 
 */
public class AdviceExpressions extends AdviceExpressionsType {

	public static AdviceExpressionsType getInstance(
			Set<AdviceExpressionType> advice) {
		AdviceExpressionsType adviceExpr = new AdviceExpressionsType();
		adviceExpr.getAdviceExpression().addAll(advice);

		return adviceExpr;
	}

	public static AdviceExpressionsType getInstance(Node root) {
		NodeList nodes = root.getChildNodes();
		AdviceExpressionsType adviceExpressions = null;
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("AdviceExpression")) {
				JAXBElement<AdviceExpressionsType> match = null;
				try {
					JAXBContext jc = JAXBContext
							.newInstance("oasis.names.tc.xacml._3_0.core.schema.wd_17");
					Unmarshaller u = jc.createUnmarshaller();
					match = (JAXBElement<AdviceExpressionsType>) u
							.unmarshal(root);
				} catch (Exception e) {
					System.err.println(e);
				}

				adviceExpressions = match.getValue();
			}
		}
		
		return adviceExpressions;
	}

}
