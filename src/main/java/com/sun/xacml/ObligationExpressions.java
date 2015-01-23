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
package com.sun.xacml;

import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thalesgroup.authzforce.core.PdpModelHandler;

public class ObligationExpressions extends oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions {
	
	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ObligationExpressions.class);

	public static oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions getInstance(
			Set<ObligationExpression> obligations) {
		oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions oblgExpr = new oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions();
		oblgExpr.getObligationExpressions().addAll(obligations);

		return oblgExpr;
	}

	public static oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions getInstance(Node root) {
		NodeList nodes = root.getChildNodes();
		oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions obligationExpressions = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("ObligationExpression")) {
				final JAXBElement<oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions> match;
				try {
					Unmarshaller u = PdpModelHandler.XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
					match =  u.unmarshal(root, oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationExpressions.class);
					obligationExpressions = match.getValue();
				} catch (Exception e) {
					LOGGER.error("Error unmarshalling ObligationExpressions", e);
				}
				
				break;
			}
		}
		
		return obligationExpressions;
	}
}
