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

import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thalesgroup.authzforce.core.PdpModelHandler;

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
					Unmarshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
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
